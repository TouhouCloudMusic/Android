package net.hearnsoft.tcm.compose.ui.crop

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.moriafly.salt.ui.SaltTheme
import com.tanishranjan.cropkit.CropDefaults
import com.tanishranjan.cropkit.ImageCropper
import com.tanishranjan.cropkit.rememberCropController
import net.hearnsoft.tcm.compose.R
import net.hearnsoft.tcm.compose.ui.theme.TouhouCloudMusicTheme
import net.hearnsoft.tcm.compose.utils.BitmapUtils
import net.hearnsoft.tcm.compose.utils.Logger

class ImageCropActivity : ComponentActivity() {

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_CROP_CONFIG = "extra_crop_config"
        const val RESULT_CROPPED_URI = "result_cropped_uri"

        /**
         * 启动裁剪Activity
         * @param context 上下文
         * @param imageUri 要裁剪的图片Uri
         * @param config 裁剪配置
         * @param launcher Activity结果启动器
         */
        fun startForResult(
            context: Context,
            imageUri: Uri,
            config: CropConfig,
            launcher: ActivityResultLauncher<Intent>
        ) {
            val intent = Intent(context, ImageCropActivity::class.java).apply {
                putExtra(EXTRA_IMAGE_URI, imageUri)
                putExtra(EXTRA_CROP_CONFIG, config)
            }
            launcher.launch(intent)
        }

        /**
         * 便捷方法：启动头像裁剪
         */
        fun startAvatarCrop(
            context: Context,
            imageUri: Uri,
            launcher: ActivityResultLauncher<Intent>
        ) {
            val config = CropConfig(
                cropShape = CropShapeConfig.Square,
                title = context.getString(R.string.crop_avatar),
                filePrefix = "avatar"
            )
            startForResult(context, imageUri, config, launcher)
        }

        /**
         * 便捷方法：启动横幅裁剪
         */
        fun startBannerCrop(
            context: Context,
            imageUri: Uri,
            launcher: ActivityResultLauncher<Intent>
        ) {
            val config = CropConfig(
                cropShape = CropShapeConfig.Banner_3_1,
                title = context.getString(R.string.crop_banner),
                filePrefix = "banner"
            )
            startForResult(context, imageUri, config, launcher)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        val imageUri = intent.getParcelableExtra<Uri>(EXTRA_IMAGE_URI)
        val config = intent.getParcelableExtra<CropConfig>(EXTRA_CROP_CONFIG)
            ?: CropConfig() // 默认配置

        if (imageUri == null) {
            Logger.err("ImageCropActivity", "Image URI is null")
            Toast.makeText(this, getString(R.string.invalid_image_uri), Toast.LENGTH_SHORT).show()
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        setContent {
            TouhouCloudMusicTheme {
                ImageCropContent(
                    imageUri = imageUri,
                    config = config,
                    onCropComplete = { croppedBitmap ->
                        val uri = BitmapUtils.saveBitmapToTempFile(
                            context = this,
                            bitmap = croppedBitmap,
                            prefix = config.filePrefix,
                            quality = config.quality
                        )

                        uri?.let {
                            val resultIntent = Intent().apply {
                                putExtra(RESULT_CROPPED_URI, it)
                            }
                            setResult(RESULT_OK, resultIntent)
                        } ?: setResult(RESULT_CANCELED)

                        finish()
                    },
                    onCancel = {
                        setResult(RESULT_CANCELED)
                        finish()
                    }
                )
            }
        }
    }

    @Composable
    private fun ImageCropContent(
        imageUri: Uri,
        config: CropConfig,
        onCropComplete: (Bitmap) -> Unit,
        onCancel: () -> Unit
    ) {
        val context = LocalContext.current
        var bitmap by remember { mutableStateOf<Bitmap?>(null) }
        var isLoading by remember { mutableStateOf(true) }

        // 加载图片
        LaunchedEffect(imageUri) {
            try {
                val loadedBitmap = imageUri.toBitmap(context)
                bitmap = loadedBitmap
                isLoading = false
            } catch (e: Exception) {
                Logger.err("ImageCropActivity", "Failed to load image", e)
                onCancel()
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            if (isLoading || bitmap == null) {
                // 显示加载状态
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("加载图片中...", color = Color.White)
                }
            } else {
                var cropShape by remember { mutableStateOf(config.cropShape.toCropShape()) }

                val cropController = rememberCropController(
                    bitmap = bitmap!!,
                    cropOptions = CropDefaults.cropOptions(
                        cropShape = cropShape,
                        gridLinesType = config.gridLinesType
                    )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding()
                        .padding(horizontal = 24.dp)
                ) {
                    // 顶部工具栏
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onCancel) {
                            Text("取消", color = Color.White)
                        }

                        Text(
                            text = config.title,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )

                        TextButton(
                            onClick = {
                                val croppedBitmap = cropController.crop()
                                onCropComplete(croppedBitmap)
                            }
                        ) {
                            Text("完成", color = SaltTheme.colors.highlight)
                        }
                    }

                    // 裁剪组件
                    ImageCropper(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        cropController = cropController
                    )

                    // 底部工具栏
                    if (config.enableRotation || config.enableFlip) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            if (config.enableRotation) {
                                IconButton(
                                    onClick = { cropController.rotateAntiClockwise() }
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_rotate_acw),
                                        contentDescription = "逆时针旋转",
                                        tint = Color.White
                                    )
                                }

                                IconButton(
                                    onClick = { cropController.rotateClockwise() }
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_rotate_cw),
                                        contentDescription = "顺时针旋转",
                                        tint = Color.White
                                    )
                                }
                            }

                            if (config.enableFlip) {
                                IconButton(
                                    onClick = { cropController.flipHorizontally() }
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_flip_horiz),
                                        contentDescription = "水平翻转",
                                        tint = Color.White
                                    )
                                }

                                IconButton(
                                    onClick = { cropController.flipVertically() }
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_flip_vert),
                                        contentDescription = "垂直翻转",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 扩展函数：Uri转Bitmap
private fun Uri.toBitmap(context: Context): Bitmap? {
    return try {
        val source = ImageDecoder.createSource(context.contentResolver, this)
        ImageDecoder.decodeBitmap(source)
    } catch (e: Exception) {
        Logger.err("ImageCropActivity", "Failed to convert Uri to Bitmap", e)
        null
    }
}