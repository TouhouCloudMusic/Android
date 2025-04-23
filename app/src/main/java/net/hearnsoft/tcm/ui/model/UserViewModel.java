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
import java.util.concurrent.CompletableFuture;

import io.vavr.concurrent.Future;

public class UserViewModel extends AndroidViewModel {
    private static final String TAG = UserViewModel.class.getSimpleName();

    private final ThcdbApiAdapter apiAdapter;
    private final UserRepository userRepository;

    // 当前登录用户的资料
    private final MutableLiveData<UserProfileModel> currentUserProfileLiveData = new MutableLiveData<>();
    // 查看的用户资料（可以是当前用户或其他用户）
    private final MutableLiveData<UserProfileModel> viewingUserProfileLiveData = new MutableLiveData<>();
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

    public boolean isLoggedIn() {
        return userRepository.isLoggedIn();
    }

    public CompletableFuture<Result<UserProfileModel>> login(String username, String password) {
        loadingLiveData.postValue(true);

        return userRepository.signIn(username, password)
            .thenApply(profile -> {
                currentUserProfileLiveData.postValue(profile);
                viewingUserProfileLiveData.postValue(profile);
                loadingLiveData.postValue(false);
                return Result.success(profile);
            })
            .exceptionally(error -> {
                loadingLiveData.postValue(false);
                return Result.error(error.getMessage());
            });
    }

    public CompletableFuture<Result<UserProfileModel>> login(AuthCredential auth) {
        loadingLiveData.postValue(true);

        return userRepository.signIn(auth)
            .thenApply(profile -> {
                currentUserProfileLiveData.postValue(profile);
                viewingUserProfileLiveData.postValue(profile);
                loadingLiveData.postValue(false);
                return Result.success(profile);
            })
            .exceptionally(error -> {
                loadingLiveData.postValue(false);
                return Result.error(error.getMessage());
            });
    }

    public CompletableFuture<Result<UserProfileModel>> register(String username, String password) {
        loadingLiveData.postValue(true);

        return userRepository.signUp(username, password)
            .thenApply(profile -> {
                currentUserProfileLiveData.postValue(profile);
                viewingUserProfileLiveData.postValue(profile);
                loadingLiveData.postValue(false);
                return Result.success(profile);
            })
            .exceptionally(error -> {
                loadingLiveData.postValue(false);
                return Result.error(error.getMessage());
            });
    }

    public CompletableFuture<Result<UserProfileModel>> register(AuthCredential auth) {
        loadingLiveData.postValue(true);

        return userRepository.signUp(auth)
            .thenApply(profile -> {
                currentUserProfileLiveData.postValue(profile);
                viewingUserProfileLiveData.postValue(profile);
                loadingLiveData.postValue(false);
                return Result.success(profile);
            })
            .exceptionally(error -> {
                loadingLiveData.postValue(false);
                return Result.error(error.getMessage());
            });
    }

    public CompletableFuture<Result<?>> logout() {
        loadingLiveData.postValue(true);

        return userRepository.signOut()
            .thenApply(success -> {
                if (success) {
                    // 清空当前用户信息
                    currentUserProfileLiveData.postValue(null);
                    viewingUserProfileLiveData.postValue(null);
                    loadingLiveData.postValue(false);
                    return Result.success(true);
                } else {
                    loadingLiveData.postValue(false);
                    return Result.error("Logout error");
                }
            })
            .exceptionally(error -> {
                loadingLiveData.postValue(false);
                return Result.error(error.getMessage());
            });
    }

    public CompletableFuture<Result<?>> updateBio(String bio) {
        loadingLiveData.postValue(true);

        return userRepository.postBio(bio)
            .thenApply(success -> {
                if (success) {
                    // 更新当前用户资料
                    UserProfileModel currentProfile = currentUserProfileLiveData.getValue();
                    if (currentProfile != null) {
                        currentProfile.setBio(bio);
                        currentUserProfileLiveData.postValue(currentProfile);
                        viewingUserProfileLiveData.postValue(currentProfile);
                    }
                    loadingLiveData.postValue(false);
                    return Result.success(true);
                } else {
                    loadingLiveData.postValue(false);
                    return Result.error("更新个人简介失败");
                }
            })
            .exceptionally(error -> {
                loadingLiveData.postValue(false);
                return Result.error(error.getMessage());
            });
    }

    // 获取指定用户名的用户资料（查看其他用户资料）
    public CompletableFuture<Result<UserProfileModel>> getProfileByUsername(String username) {
        if (userRoleListLiveData.getValue() == null) {
            getUserRoles();
        }

        loadingLiveData.postValue(true);

        return userRepository.profileWithName(username)
            .thenApply(profile -> {
                viewingUserProfileLiveData.postValue(profile);
                loadingLiveData.postValue(false);
                return Result.success(profile);
            })
            .exceptionally(error -> {
                loadingLiveData.postValue(false);
                return Result.error(error.getMessage());
            });
    }

    // 获取当前登录用户的资料，并设置为查看的用户
    public CompletableFuture<Result<UserProfileModel>> loadCurrentUserProfile() {
        if (!userRepository.isLoggedIn()) {
            return CompletableFuture.completedFuture(Result.error("未登录"));
        }

        if (userRoleListLiveData.getValue() == null) {
            getUserRoles();
        }

        loadingLiveData.postValue(true);

        return userRepository.getCurrentUserProfile()
            .thenApply(profile -> {
                currentUserProfileLiveData.postValue(profile);
                viewingUserProfileLiveData.postValue(profile);
                loadingLiveData.postValue(false);
                return Result.success(profile);
            })
            .exceptionally(error -> {
                loadingLiveData.postValue(false);
                String errorMsg = error.getMessage();

                if (error instanceof ThcdbApiException &&
                    ((ThcdbApiException) error).getErrorCode() != null &&
                    ((ThcdbApiException) error).getErrorCode() == 401) {
                    // 处理未授权错误 - 清除本地会话
                    userRepository.clearSession();
                    errorMsg = "会话已过期，请重新登录";
                }

                return Result.error(errorMsg);
            });
    }

    public void getUserRoles() {
        loadingLiveData.postValue(true);

        Future.fromCompletableFuture(apiAdapter.getDefaultApi().userRoles())
            .onSuccess(roles -> {
                List<String> rolesList = roles.getData();
                if (rolesList == null) {
                    loadingLiveData.postValue(false);
                    return;
                }
                userRoleListLiveData.postValue(rolesList);
                loadingLiveData.postValue(false);
            }).onFailure(error -> {
                loadingLiveData.postValue(false);
            });
    }

    public CompletableFuture<Result<Boolean>> uploadAvatar(Uri avatarUri, ContentResolver resolver) {
        if (avatarUri == null) {
            return CompletableFuture.completedFuture(Result.error("图片URI为空"));
        }

        loadingLiveData.postValue(true);

        return userRepository.uploadAvatar(avatarUri, resolver)
            .thenCompose(success -> {
                if (success) {
                    // 上传成功后刷新用户资料以获取更新的头像
                    return userRepository.getCurrentUserProfile()
                        .thenApply(profile -> {
                            currentUserProfileLiveData.postValue(profile);
                            viewingUserProfileLiveData.postValue(profile);
                            loadingLiveData.postValue(false);
                            return Result.success(true);
                        })
                        .exceptionally(error -> {
                            loadingLiveData.postValue(false);
                            // 头像上传成功但刷新资料失败，仍然返回成功结果
                            return Result.success(true);
                        });
                } else {
                    loadingLiveData.postValue(false);
                    return CompletableFuture.completedFuture(Result.error("头像上传失败"));
                }
            })
            .exceptionally(error -> {
                loadingLiveData.postValue(false);
                return Result.error(error.getMessage());
            });
    }

    public CompletableFuture<Result<Boolean>> uploadProfileBanner(Uri bannerUri, ContentResolver resolver) {
        if (bannerUri == null) {
            return CompletableFuture.completedFuture(Result.error("图片URI为空"));
        }

        loadingLiveData.postValue(true);

        return userRepository.uploadProfileBanner(bannerUri, resolver)
            .thenCompose(success -> {
                if (success) {
                    // 上传成功后刷新用户资料以获取更新的背景图
                    return userRepository.getCurrentUserProfile()
                        .thenApply(profile -> {
                            currentUserProfileLiveData.postValue(profile);
                            viewingUserProfileLiveData.postValue(profile);
                            loadingLiveData.postValue(false);
                            return Result.success(true);
                        })
                        .exceptionally(error -> {
                            loadingLiveData.postValue(false);
                            // 背景图上传成功但刷新资料失败，仍然返回成功结果
                            return Result.success(true);
                        });
                } else {
                    loadingLiveData.postValue(false);
                    return CompletableFuture.completedFuture(Result.error("背景图上传失败"));
                }
            })
            .exceptionally(error -> {
                loadingLiveData.postValue(false);
                return Result.error(error.getMessage());
            });
    }
}