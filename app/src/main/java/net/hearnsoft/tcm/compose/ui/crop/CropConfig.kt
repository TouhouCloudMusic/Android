package net.hearnsoft.tcm.compose.ui.crop

import android.os.Parcelable
import com.tanishranjan.cropkit.CropRatio
import com.tanishranjan.cropkit.CropShape
import com.tanishranjan.cropkit.GridLinesType
import kotlinx.parcelize.Parcelize

@Parcelize
data class CropConfig(
    val cropShape: CropShapeConfig = CropShapeConfig.Square,
    val gridLinesType: GridLinesType = GridLinesType.GRID,
    val title: String = "裁剪图片",
    val filePrefix: String = "cropped",
    val enableRotation: Boolean = true,
    val enableFlip: Boolean = true,
    val quality: Int = 90
) : Parcelable

@Parcelize
sealed class CropShapeConfig : Parcelable {
    @Parcelize
    object Original : CropShapeConfig()

    @Parcelize
    object FreeForm : CropShapeConfig()

    @Parcelize
    object Square : CropShapeConfig()

    @Parcelize
    data class AspectRatio(val ratio: Float) : CropShapeConfig()

    // 预定义的常用比例
    companion object {
        val Portrait_9_16 = AspectRatio(CropRatio.PORTRAIT_9_16)
        val Landscape_16_9 = AspectRatio(CropRatio.LANDSCAPE_16_9)
        val Portrait_4_5 = AspectRatio(CropRatio.PORTRAIT_4_5)
        val Landscape_5_4 = AspectRatio(CropRatio.LANDSCAPE_5_4)
        val Portrait_3_4 = AspectRatio(CropRatio.PORTRAIT_3_4)
        val Landscape_4_3 = AspectRatio(CropRatio.LANDSCAPE_4_3)

        val Banner_3_1 = AspectRatio(3.0f)

        // 自定义比例
        fun customRatio(width: Float, height: Float) = AspectRatio(width / height)
    }
}

// 扩展函数：将CropShapeConfig转换为CropShape
fun CropShapeConfig.toCropShape(): CropShape = when (this) {
    is CropShapeConfig.Original -> CropShape.Original
    is CropShapeConfig.FreeForm -> CropShape.FreeForm
    is CropShapeConfig.Square -> CropShape.AspectRatio(CropRatio.SQUARE)
    is CropShapeConfig.AspectRatio -> CropShape.AspectRatio(this.ratio)
}