package net.hearnsoft.tcm.compose.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.moriafly.salt.ui.SaltColors
import com.moriafly.salt.ui.SaltDynamicColors
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.saltConfigs

// 自定义的东方同音鉴配色方案
@Composable
fun touhouLightColors(): SaltColors = SaltColors(
    highlight = Primary, // 高亮色 - 使用主色调
    text = TextPrimary, // 主要文字颜色
    subText = TextSecondary, // 次要文字颜色
    background = Background, // 主背景色
    subBackground = Surface, // 次要背景色
    popup = White, // 弹窗背景色
    stroke = Outline, // 描边颜色
    onHighlight = TextOnPrimary // 高亮色上的文字颜色
)

@Composable
fun touhouDarkColors(): SaltColors = SaltColors(
    highlight = DarkPrimary, // 暗色主题的高亮色
    text = DarkTextPrimary, // 暗色主题的主要文字颜色
    subText = DarkTextSecondary, // 暗色主题的次要文字颜色
    background = DarkBackground, // 暗色主题的主背景色
    subBackground = DarkSurface, // 暗色主题的次要背景色
    popup = DarkSurfaceVariant, // 暗色主题的弹窗背景色
    stroke = DarkOutline, // 暗色主题的描边颜色
    onHighlight = DarkTextPrimary // 暗色主题的高亮色变体
)

@Composable
fun TouhouCloudMusicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // 预留动态颜色开关，后续可以通过SharedPreferences或其他状态管理控制
    useDynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val dynamicColors = when {
        // 优先检查是否启用动态颜色且支持Android 12+
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            SaltDynamicColors(
                light = dynamicLightColorScheme(context).toSaltColors(),
                dark = dynamicDarkColorScheme(context).toSaltColors()
            )
        }
        // 使用自定义的东方同音鉴配色
        else -> {
            SaltDynamicColors(
                light = touhouLightColors(),
                dark = touhouDarkColors()
            )
        }
    }

    SaltTheme(
        configs = saltConfigs(
            isDarkTheme = darkTheme,
            indication = ripple()
        ),
        dynamicColors = dynamicColors,
        textStyles = SaltTheme.textStyles,
        dimens = SaltTheme.dimens,
        shapes = SaltTheme.shapes,
        content = content
    )
}

// 扩展函数：将Material3 ColorScheme转换为SaltColors
@Composable
private fun ColorScheme.toSaltColors(): SaltColors = SaltColors(
    highlight = this.primary, // 使用primary作为高亮色
    text = this.onBackground, // 主要文字颜色
    subText = this.onSurfaceVariant, // 次要文字颜色
    background = this.background, // 主背景色
    subBackground = this.surface, // 次要背景色
    popup = this.surfaceContainer, // 弹窗背景色
    stroke = this.outline, // 描边颜色
    onHighlight = this.primary // 高亮色上的文字颜色
)

// 定义主题颜色
object Theme {
    object colors {
        // 亮色主题颜色
        val primary = Primary
        val textPrimary = TextPrimary
        val textSecondary = TextSecondary
        val background = Background
        val surface = Surface
        val white = White
        val outline = Outline
        val textOnPrimary = TextOnPrimary

        // 暗色主题颜色
        val darkPrimary = DarkPrimary
        val darkTextPrimary = DarkTextPrimary
        val darkTextSecondary = DarkTextSecondary
        val darkBackground = DarkBackground
        val darkSurface = DarkSurface
        val darkSurfaceVariant = DarkSurfaceVariant
        val darkOutline = DarkOutline

        val alphaStroke = Color.DarkGray.copy(alpha = 0.3f) // 20%透明度的深灰色
    }
}
