package net.hearnsoft.tcm.domain.repository;

import net.hearnsoft.tcm.domain.model.user.UserProfileModel;
import net.hearnsoft.tcm.infrastructure.adapter.http.ThcdbApiAdapter;
import net.hearnsoft.thcdb_sdk.model.AuthCredential;
import net.hearnsoft.thcdb_sdk.model.UserProfile;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Build;

import java.util.concurrent.CompletableFuture;

import lombok.Getter;

public class UserRepository {
    private final ThcdbApiAdapter apiAdapter;
    @Getter
    private UserProfileModel cachedCurrentUser;

    public UserRepository(ThcdbApiAdapter apiAdapter) {
        this.apiAdapter = apiAdapter;
    }

    public CompletableFuture<UserProfileModel> signIn(String username, String password) {
        return apiAdapter.getUser().signIn(username, password)
            .thenApply(this::convertAndCacheCurrentUser);
    }

    public CompletableFuture<UserProfileModel> signIn(AuthCredential auth) {
        return apiAdapter.getUser().signIn(auth)
            .thenApply(this::convertAndCacheCurrentUser);
    }

    public CompletableFuture<UserProfileModel> signUp(String username, String password) {
        return apiAdapter.getUser().signUp(username, password)
            .thenApply(this::convertAndCacheCurrentUser);
    }

    public CompletableFuture<UserProfileModel> signUp(AuthCredential auth) {
        return apiAdapter.getUser().signUp(auth)
            .thenApply(this::convertAndCacheCurrentUser);
    }

    public CompletableFuture<Boolean> signOut() {
        return apiAdapter.getUser().signOut()
            .thenApply(success -> {
                if (success) {
                    clearCache();
                }
                return success;
            });
    }

    public CompletableFuture<UserProfileModel> getCurrentUserProfile() {
        CompletableFuture<UserProfileModel> future;
        if (!apiAdapter.isLoggedIn()) {
            IllegalStateException exception = new IllegalStateException("Not logged in");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                future = CompletableFuture.failedFuture(exception);
            } else {
                future = new CompletableFuture<>();
                future.completeExceptionally(exception);
            }
            return future;
        }

        return apiAdapter.getUser().profile()
            .thenApply(this::convertAndCacheCurrentUser);
    }

    public CompletableFuture<UserProfileModel> profileWithName(String username) {
        return apiAdapter.getUser().profileWithName(username)
            .thenApply(UserProfileModel::fromApiModel);
    }

    public CompletableFuture<Boolean> uploadAvatar(Uri avatarUri, ContentResolver resolver) {
        return apiAdapter.getUser().uploadAvatar(avatarUri, resolver)
            .thenCompose(success -> {
                if (success) {
                    // 刷新缓存的用户信息
                    return getCurrentUserProfile().thenApply(userProfile -> true);
                } else {
                    return CompletableFuture.completedFuture(false);
                }
            });
    }

    public CompletableFuture<Boolean> uploadProfileBanner(Uri bannerUri, ContentResolver resolver) {
        return apiAdapter.getUser().uploadProfileBanner(bannerUri, resolver)
            .thenCompose(success -> {
                if (success) {
                    // 刷新缓存的用户信息
                    return getCurrentUserProfile().thenApply(userProfile -> true);
                } else {
                    return CompletableFuture.completedFuture(false);
                }
            });
    }

    public boolean isLoggedIn() {
        return apiAdapter.isLoggedIn();
    }

    public void clearSession() {
        apiAdapter.clearSession();
        clearCache();
    }

    private UserProfileModel convertAndCacheCurrentUser(UserProfile apiProfile) {
        UserProfileModel model = UserProfileModel.fromApiModel(apiProfile);
        this.cachedCurrentUser = model;
        return model;
    }

    private void clearCache() {
        this.cachedCurrentUser = null;
    }
}