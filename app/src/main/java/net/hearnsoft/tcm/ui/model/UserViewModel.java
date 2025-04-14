package net.hearnsoft.tcm.ui.model;

import android.app.Application;
import android.content.ContentResolver;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import net.hearnsoft.tcm.infrastructure.adapter.http.Constants;
import net.hearnsoft.tcm.infrastructure.adapter.http.ThcdbApiAdapter;
import net.hearnsoft.tcm.infrastructure.adapter.http.ThcdbApiAdapter.ThcdbApiException;
import net.hearnsoft.thcdb_sdk.model.UserProfile;

import io.vavr.concurrent.Future;

public class UserViewModel extends AndroidViewModel {
    private static final String TAG = UserViewModel.class.getSimpleName();

    private final ThcdbApiAdapter apiAdapter;
    private final MutableLiveData<UserProfile> userProfileLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> successLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);

    public UserViewModel(@NonNull Application application) {
        super(application);
        apiAdapter = new ThcdbApiAdapter(application, Constants.API_HOST);
    }

    public LiveData<UserProfile> getUserProfile() {
        return userProfileLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public LiveData<Boolean> getSuccess() {
        return successLiveData;
    }

    public LiveData<Boolean> isLoading() {
        return loadingLiveData;
    }

    public boolean isLoggedIn() {
        return apiAdapter.isLoggedIn();
    }

    public void login(String username, String password) {
        loadingLiveData.postValue(true);

        Future.fromCompletableFuture(apiAdapter.getUser().signIn(username, password))
            .onSuccess(profile -> {
                userProfileLiveData.postValue(profile);
                successLiveData.postValue(true);
                loadingLiveData.postValue(false);
            })
            .onFailure(error -> {
                handleError(error);
                successLiveData.postValue(false);
                loadingLiveData.postValue(false);
            });
    }

    public void register(String username, String password) {
        loadingLiveData.postValue(true);

        Future.fromCompletableFuture(apiAdapter.getUser().signUp(username, password))
            .onSuccess(profile -> {
                userProfileLiveData.postValue(profile);
                successLiveData.postValue(true);
                loadingLiveData.postValue(false);
            })
            .onFailure(error -> {
                handleError(error);
                successLiveData.postValue(false);
                loadingLiveData.postValue(false);
            });
    }

    public void logout() {
        loadingLiveData.postValue(true);

        Future.fromCompletableFuture(apiAdapter.getUser().signOut())
            .onSuccess(success -> {
                if (success) {
                    userProfileLiveData.postValue(null); // Clear current user info
                    successLiveData.postValue(true);
                } else {
                    errorLiveData.postValue("Logout failed");
                    successLiveData.postValue(false);
                }
                loadingLiveData.postValue(false);
            })
            .onFailure(error -> {
                handleError(error);
                successLiveData.postValue(false);
                loadingLiveData.postValue(false);
            });
    }

    public void getProfileByUsername(String username) {
        loadingLiveData.postValue(true);

        Future.fromCompletableFuture(apiAdapter.getUser().profileWithName(username))
            .onSuccess(profile -> {
                userProfileLiveData.postValue(profile);
                loadingLiveData.postValue(false);
            })
            .onFailure(error -> {
                handleError(error);
                loadingLiveData.postValue(false);
            });
    }

    public void getCurrentUserProfile() {
        if (!apiAdapter.isLoggedIn()) {
            errorLiveData.postValue("Not logged in");
            return;
        }

        loadingLiveData.postValue(true);

        Future.fromCompletableFuture(apiAdapter.getUser().profile())
            .onSuccess(profile -> {
                userProfileLiveData.postValue(profile);
                loadingLiveData.postValue(false);
            })
            .onFailure(error -> {
                handleError(error);
                loadingLiveData.postValue(false);
            });
    }

    public void uploadAvatar(Uri avatarUri, ContentResolver resolver) {
        if (avatarUri == null) {
            errorLiveData.postValue("Image URI is null");
            return;
        }

        loadingLiveData.postValue(true);

        Future.fromCompletableFuture(apiAdapter.getUser().uploadAvatar(avatarUri, resolver))
            .onSuccess(success -> {
                if (success) {
                    successLiveData.postValue(true);
                    // After successful upload, refresh user profile to get the updated avatar
                    getCurrentUserProfile();
                } else {
                    errorLiveData.postValue("Avatar upload failed");
                    successLiveData.postValue(false);
                }
                loadingLiveData.postValue(false);
            })
            .onFailure(error -> {
                handleError(error);
                successLiveData.postValue(false);
                loadingLiveData.postValue(false);
            });
    }

    private void handleError(Throwable error) {
        if (error instanceof ThcdbApiException) {
            ThcdbApiException apiError = (ThcdbApiException) error;
            Integer errorCode = apiError.getErrorCode();

            if (errorCode != null && errorCode == 401) {
                // Handle unauthorized error specially - e.g., clear local session
                apiAdapter.clearSession();
                errorLiveData.postValue("Session expired. Please login again.");
            } else {
                errorLiveData.postValue(apiError.getMessage());
            }
        } else {
            errorLiveData.postValue(error.getMessage());
        }
    }
}