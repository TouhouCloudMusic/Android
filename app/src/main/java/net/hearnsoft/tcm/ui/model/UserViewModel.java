package net.hearnsoft.tcm.ui.model;

import android.app.Application;
import android.content.ContentResolver;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import net.hearnsoft.tcm.domain.model.user.UserProfileModel;
import net.hearnsoft.tcm.domain.repository.UserRepository;
import net.hearnsoft.tcm.infrastructure.adapter.http.Constants;
import net.hearnsoft.tcm.infrastructure.adapter.http.ThcdbApiAdapter;
import net.hearnsoft.tcm.infrastructure.adapter.http.ThcdbApiAdapter.ThcdbApiException;
import net.hearnsoft.thcdb_sdk.model.AuthCredential;

import java.util.List;

import io.vavr.concurrent.Future;

public class UserViewModel extends AndroidViewModel {
    private static final String TAG = UserViewModel.class.getSimpleName();

    private final ThcdbApiAdapter apiAdapter;
    private final UserRepository userRepository;

    // 当前登录用户的资料
    private final MutableLiveData<UserProfileModel> currentUserProfileLiveData = new MutableLiveData<>();
    // 查看的用户资料（可以是当前用户或其他用户）
    private final MutableLiveData<UserProfileModel> viewingUserProfileLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> successLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<String>> userRoleListLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);

    public UserViewModel(@NonNull Application application) {
        super(application);
        apiAdapter = new ThcdbApiAdapter(application, Constants.API_HOST);
        userRepository = new UserRepository(apiAdapter);
    }

    // 获取当前登录用户资料的LiveData
    public LiveData<UserProfileModel> getCurrentUserProfile() {
        return currentUserProfileLiveData;
    }

    // 获取正在查看的用户资料的LiveData
    public LiveData<UserProfileModel> getUserProfile() {
        return viewingUserProfileLiveData;
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

    public LiveData<List<String>> getUserRoleList() {
        return userRoleListLiveData;
    }

    public boolean isLoggedIn() {
        return userRepository.isLoggedIn();
    }

    public void login(String username, String password) {
        loadingLiveData.postValue(true);

        Future.fromCompletableFuture(userRepository.signIn(username, password))
            .onSuccess(profile -> {
                currentUserProfileLiveData.postValue(profile);
                viewingUserProfileLiveData.postValue(profile); // 登录时两者都是当前用户
                successLiveData.postValue(true);
                loadingLiveData.postValue(false);
            })
            .onFailure(error -> {
                handleError(error);
                successLiveData.postValue(false);
                loadingLiveData.postValue(false);
            });
    }

    public void login(AuthCredential auth) {
        loadingLiveData.postValue(true);

        Future.fromCompletableFuture(userRepository.signIn(auth))
            .onSuccess(profile -> {
                currentUserProfileLiveData.postValue(profile);
                viewingUserProfileLiveData.postValue(profile);
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

        Future.fromCompletableFuture(userRepository.signUp(username, password))
            .onSuccess(profile -> {
                currentUserProfileLiveData.postValue(profile);
                viewingUserProfileLiveData.postValue(profile);
                successLiveData.postValue(true);
                loadingLiveData.postValue(false);
            })
            .onFailure(error -> {
                handleError(error);
                successLiveData.postValue(false);
                loadingLiveData.postValue(false);
            });
    }

    public void register(AuthCredential auth) {
        loadingLiveData.postValue(true);

        Future.fromCompletableFuture(userRepository.signUp(auth))
            .onSuccess(profile -> {
                currentUserProfileLiveData.postValue(profile);
                viewingUserProfileLiveData.postValue(profile);
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

        Future.fromCompletableFuture(userRepository.signOut())
            .onSuccess(success -> {
                if (success) {
                    // 清空当前用户信息
                    currentUserProfileLiveData.postValue(null);
                    viewingUserProfileLiveData.postValue(null);
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

    public void updateBio(String bio) {
        loadingLiveData.postValue(true);

        Future.fromCompletableFuture(userRepository.postBio(bio))
            .onSuccess(success -> {
                if (success) {
                    // 更新当前用户资料
                    currentUserProfileLiveData.postValue(currentUserProfileLiveData.getValue());
                    viewingUserProfileLiveData.postValue(viewingUserProfileLiveData.getValue());
                    successLiveData.postValue(true);
                } else {
                    errorLiveData.postValue("Update bio failed");
                    successLiveData.postValue(false);
                }
            })
            .onFailure(error -> {
                handleError(error);
                successLiveData.postValue(false);
            });
    }

    // 获取指定用户名的用户资料（查看其他用户资料）
    public void getProfileByUsername(String username) {
        if (userRoleListLiveData.getValue() == null) {
            getUserRoles();
        }

        loadingLiveData.postValue(true);

        Future.fromCompletableFuture(userRepository.profileWithName(username))
            .onSuccess(profile -> {
                viewingUserProfileLiveData.postValue(profile);
                loadingLiveData.postValue(false);
            })
            .onFailure(error -> {
                handleError(error);
                loadingLiveData.postValue(false);
            });
    }

    // 获取当前登录用户的资料，并设置为查看的用户
    public void loadCurrentUserProfile() {
        if (!userRepository.isLoggedIn()) {
            errorLiveData.postValue("Not logged in");
            return;
        }
        if (userRoleListLiveData.getValue() == null) {
            getUserRoles();
        }

        loadingLiveData.postValue(true);

        Future.fromCompletableFuture(userRepository.getCurrentUserProfile())
            .onSuccess(profile -> {
                currentUserProfileLiveData.postValue(profile);
                viewingUserProfileLiveData.postValue(profile);
                loadingLiveData.postValue(false);
            })
            .onFailure(error -> {
                handleError(error);
                loadingLiveData.postValue(false);
            });
    }

    public void getUserRoles() {
        loadingLiveData.postValue(true);

        Future.fromCompletableFuture(apiAdapter.getDefaultApi().userRoles())
            .onSuccess(roles -> {
                List<String> rolesList = roles.getData();
                if (rolesList == null) {
                    handleError(new ThcdbApiException("Roles list is null"));
                    loadingLiveData.postValue(false);
                    return;
                }
                userRoleListLiveData.postValue(rolesList);
                loadingLiveData.postValue(false);
            }).onFailure(error -> {
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

        Future.fromCompletableFuture(userRepository.uploadAvatar(avatarUri, resolver))
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

    public void uploadProfileBanner(Uri bannerUri, ContentResolver resolver) {
        if (bannerUri == null) {
            errorLiveData.postValue("Image URI is null");
            return;
        }

        loadingLiveData.postValue(true);

        Future.fromCompletableFuture(userRepository.uploadProfileBanner(bannerUri, resolver))
            .onSuccess(success -> {
                if (success) {
                    successLiveData.postValue(true);
                    // After successful upload, refresh user profile to get the updated banner
                    getCurrentUserProfile();
                } else {
                    errorLiveData.postValue("Banner upload failed");
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
                userRepository.clearSession();
                errorLiveData.postValue("Session expired. Please login again.");
            } else {
                errorLiveData.postValue(apiError.getMessage());
            }
        } else {
            errorLiveData.postValue(error.getMessage());
        }
    }

    public void clearError() {
        errorLiveData.postValue(null);
    }
}