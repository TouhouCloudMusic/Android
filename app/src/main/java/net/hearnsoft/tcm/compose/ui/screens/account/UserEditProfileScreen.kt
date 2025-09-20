package net.hearnsoft.tcm.compose.ui.screens.account

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.dialog.InputDialog
import com.moriafly.salt.ui.dialog.YesNoDialog
import net.hearnsoft.tcm.compose.ui.crop.ImageCropActivity
import net.hearnsoft.tcm.compose.R
import net.hearnsoft.tcm.compose.ui.screens.ScreenRoute
import net.hearnsoft.tcm.compose.ui.utils.getFullImageUrl
import net.hearnsoft.tcm.compose.ui.viewmodel.UserViewModel
import net.hearnsoft.tcm.compose.utils.Logger

@UnstableSaltUiApi
@Composable
fun UserEditProfileScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    userViewModel: UserViewModel,
) {

    val context = LocalContext.current

    val user = userViewModel.user.collectAsState().value
    val successMsg = userViewModel.successMessage.collectAsState().value
    val errorMsg = userViewModel.error.collectAsState().value
    val isLoading = userViewModel.isLoading.collectAsState().value

    val bioText = remember { mutableStateOf(user?.bio ?: "") }
    val showBioDialog = remember { mutableStateOf(false) }
    val showLogoutDialog = remember { mutableStateOf(false) }

    // 需要记住当前裁剪的类型
    var currentCropType by remember { mutableStateOf<String?>(null) }

    // 图片刷新状态
    var avatarRefreshKey by remember { mutableIntStateOf(0) }
    var bannerRefreshKey by remember { mutableIntStateOf(0) }

    // 裁剪结果处理
    val cropLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val croppedUri = result.data?.getParcelableExtra<Uri>(
                ImageCropActivity.RESULT_CROPPED_URI
            )
            croppedUri?.let { uri ->
                when (currentCropType) {
                    "avatar" -> userViewModel.uploadAvatar(uri)
                    "banner" -> userViewModel.uploadProfileBanner(uri)
                    else -> Logger.warn("UserEditProfileScreen", "Unknown crop type: $currentCropType")
                }
                currentCropType = null // 重置类型
            }
        }
    }

    // 图片选择器 - 头像
    val avatarImagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { imageUri ->
            currentCropType = "avatar"
            ImageCropActivity.startAvatarCrop(context, imageUri, cropLauncher)  // 直接传递Uri
        }
    }

    // 图片选择器 - 横幅
    val bannerImagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { imageUri ->
            currentCropType = "banner"
            ImageCropActivity.startBannerCrop(context, imageUri, cropLauncher)  // 直接传递Uri
        }
    }

    // 监听上传完成，刷新图片
    LaunchedEffect(user?.avatarUrl) {
        if (user?.avatarUrl != null) {
            avatarRefreshKey++
        }
    }

    LaunchedEffect(user?.bannerUrl) {
        if (user?.bannerUrl != null) {
            bannerRefreshKey++
        }
    }

    LaunchedEffect(user) {
        if (user == null) {
            // 用户未登录，导航回账户主界面
            navController.navigate(ScreenRoute.Account.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(successMsg) {
        successMsg?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            userViewModel.clearSuccessMessage()
        }
    }

    LaunchedEffect(errorMsg) {
        errorMsg?.let { msg ->
            Logger.err("UserEditProfileScreen", "Error: $msg")
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            userViewModel.clearError()
        }
    }

    // 监听用户信息变化，更新简介文本
    LaunchedEffect(user?.bio) {
        bioText.value = user?.bio ?: ""
    }

    if (showBioDialog.value) {
        InputDialog(
            title = "编辑简介",
            text = bioText.value,
            onDismissRequest = {
                showBioDialog.value = false
            },
            onConfirm = {
                userViewModel.updateBioText(bioText.value)
                showBioDialog.value = false
            },
            onChange = { text ->
                bioText.value = text
            },
            confirmText = "保存",
            cancelText = "取消",
        )
    }

    if (showLogoutDialog.value) {
        YesNoDialog(
            title = "确认退出登录？",
            content = "退出后将使用访客身份浏览内容",
            onDismissRequest = {
                showLogoutDialog.value = false
            },
            onConfirm = {
                userViewModel.signOut()
                showLogoutDialog.value = false
            },
            cancelText = "取消",
            confirmText = "确定",
        )
    }

    Column(modifier.fillMaxSize()) {
        // 用户资料编辑界面内容
        Box(Modifier.fillMaxWidth().height(200.dp)) {
            // 背景横幅图片
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(getFullImageUrl(user?.bannerUrl))
                    .placeholder(R.drawable.test_res1)
                    .error(R.drawable.test_res1)
                    .crossfade(true)
                    .memoryCacheKey("banner_${user?.bannerUrl}_$bannerRefreshKey") // 强制刷新缓存
                    .diskCacheKey("banner_${user?.bannerUrl}_$bannerRefreshKey")   // 强制刷新磁盘缓存
                    .build(),
                contentDescription = "User Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // 半透明遮罩
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.DarkGray.copy(alpha = 0.1f),
                                Color.DarkGray.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            // 头像
            Box(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                // 用户头像
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(getFullImageUrl(user?.avatarUrl))
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .crossfade(true)
                        .memoryCacheKey("avatar_${user?.avatarUrl}_$avatarRefreshKey") // 强制刷新缓存
                        .diskCacheKey("avatar_${user?.avatarUrl}_$avatarRefreshKey")   // 强制刷新磁盘缓存
                        .build(),
                    contentDescription = "User Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                )
                // 添加加载指示器
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            if (isLoading && currentCropType == "avatar")
                                Color.Black.copy(alpha = 0.6f)
                            else
                                Color.Black.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                        .clip(CircleShape)
                        .clickable {
                            if (!isLoading) { // 加载时禁用点击
                                avatarImagePicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        }
                ) {
                    if (isLoading && currentCropType == "avatar") {
                        // 显示加载指示器
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.Center),
                            color = SaltTheme.colors.highlight,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            contentDescription = "Change Avatar",
                            tint = Color.White,
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    color = SaltTheme.colors.highlight,
                                    shape = CircleShape
                                )
                                .padding(2.dp)
                                .align(Alignment.Center),
                            painter = painterResource(R.drawable.ic_change_photo_24px)
                        )
                    }
                }
            }

            Button(
                onClick = {
                    if (!isLoading) { // 加载时禁用点击
                        bannerImagePicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                },
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.BottomEnd),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SaltTheme.colors.highlight.copy(alpha = 0.8f),
                    contentColor = Color.White
                ),
                enabled = !isLoading // 加载时禁用按钮
            ) {
                if (isLoading && currentCropType == "banner") {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }
                Text(
                    text = if (isLoading && currentCropType == "banner") "上传中..." else "更换横幅图片",
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.size(8.dp))

        Column(Modifier.fillMaxWidth()) {
            Item(
                text = "用户名",
                sub = user?.name ?: "未登录",
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = {
                    // do nothing
                },
                enabled = false // 用户名不可编辑
            )
            Item(
                text = "简介",
                sub = if (user != null) {
                    if (!user.bio.isNullOrBlank()) {
                        user.bio
                    } else {
                        "这个人很懒，什么都没留下"
                    }
                } else {
                    "未登录"
                },
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = {
                    if (user != null) {
                        bioText.value = user.bio ?: ""
                        showBioDialog.value = true
                    }
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                // 点击退出登录，显示确认对话框
                Logger.info("UserEditProfileScreen", "Request logout")
                showLogoutDialog.value = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SaltTheme.colors.highlight,
                contentColor = Color.White
            ),
        ) {
            Text(
                text = "退出登录",
                color = Color.White
            )
        }

    }

}