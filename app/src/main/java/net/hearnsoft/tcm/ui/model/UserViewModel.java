package net.hearnsoft.tcm.ui.model;

import static net.hearnsoft.tcm.infrastructure.adapter.http.ApiEndpoints.BASE_URL;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import net.hearnsoft.tcm.application.service.SyncUserService;
import net.hearnsoft.tcm.domain.repository.UserRepository;
import net.hearnsoft.tcm.infrastructure.adapter.http.APICore;
import net.hearnsoft.tcm.infrastructure.adapter.http.ErrorCode;
import net.hearnsoft.tcm.infrastructure.adapter.http.UserApi;

import org.openapitools.client.models.AuthCredential;
import org.openapitools.client.models.Message;
import org.openapitools.client.models.UserProfile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import io.vavr.concurrent.Future;

public class UserViewModel extends AndroidViewModel {
    private static final String TAG = UserViewModel.class.getSimpleName();

    private final SyncUserService userService;
    private final MutableLiveData<UserProfile> userProfileLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Message> messageLiveData = new MutableLiveData<>();

    public UserViewModel(@NonNull Application application) {
        super(application);
        userService = new UserApi(BASE_URL);
    }

    public LiveData<UserProfile> getUserProfile() {
        return userProfileLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public LiveData<Message> getMessage() {
        return messageLiveData;
    }

    public void login(String username, String password) {
        AuthCredential creds = new AuthCredential(username, password);
        Future.fromCompletableFuture(userService.signInSync(creds))
            .onSuccess(userProfileLiveData::postValue)
            .onFailure(error -> {
                errorLiveData.postValue(error.getMessage());
            });
    }

    public void register(String username, String password) {
        AuthCredential creds = new AuthCredential(username, password);
        Future.fromCompletableFuture(userService.signUpSync(creds))
            .onSuccess(userProfileLiveData::postValue)
            .onFailure(error -> {
                errorLiveData.postValue(error.getMessage());
            });
    }

    public void logout() {
        Future.fromCompletableFuture(userService.signOutSync())
            .onSuccess(message -> {
                messageLiveData.postValue(message);
                userProfileLiveData.postValue(null); // 清除当前用户信息
            })
            .onFailure(error -> {
                errorLiveData.postValue(error.getMessage());
            });
    }

    public void getProfileByUsername(String username) {
        // 利用 UserApi 中提供的 getProfile 方法
        Future.fromCompletableFuture(userService.getProfileByUsernameSync(username))
            .onSuccess(userProfileLiveData::postValue)
            .onFailure(error -> {
                errorLiveData.postValue(error.getMessage());
            });
    }

    public void uploadAvatar(Uri avatarUri, ContentResolver resolver) {
        if (avatarUri == null) {
            errorLiveData.postValue("Image URI is null");
            return;
        }

        File tempFile = null;

        try {
            // Get file name from Uri
            String fileName = getFileNameFromUri(avatarUri, resolver);

            // Create a temporary file to store the content
            tempFile = File.createTempFile("avatar_", fileName, getApplication().getCacheDir());

            // Copy content from Uri to the file
            ContentResolver contentResolver = getApplication().getContentResolver();
            try (InputStream inputStream = contentResolver.openInputStream(avatarUri);
                 FileOutputStream outputStream = new FileOutputStream(tempFile)) {

                if (inputStream == null) {
                    errorLiveData.postValue("Cannot read from the selected file");
                    return;
                }

                byte[] buffer = new byte[4 * 1024]; // 4k buffer
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                outputStream.flush();

                Future.fromCompletableFuture(userService.uploadAvatarSync(tempFile))
                    .onSuccess(messageLiveData::postValue)
                    .onFailure(error -> {
                        errorLiveData.postValue(error.getMessage());
                    });
            }
        } catch (java.io.IOException e) {
            errorLiveData.postValue("Error processing file: " + e.getMessage());
        } finally {
            try {
                if (tempFile != null) {
                    tempFile.delete();
                }
            } catch (Exception e) {
                errorLiveData.postValue("Error deleting temporary file: " + e.getMessage());
            }
        }
    }

    @SuppressLint("Range")
    private String getFileNameFromUri(Uri uri, ContentResolver contentResolver) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}