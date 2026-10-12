@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package org.mpc.presentation.theme

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readValue
import kotlinx.cinterop.value
import platform.CoreGraphics.CGFloatVar
import platform.CoreGraphics.CGRectZero
import platform.UIKit.UIAccessibilityContrastHigh
import platform.UIKit.UIColor
import platform.UIKit.UITraitCollection
import platform.UIKit.UIUserInterfaceStyle.UIUserInterfaceStyleDark
import platform.UIKit.UIUserInterfaceStyle.UIUserInterfaceStyleLight
import platform.UIKit.UIView
import platform.UIKit.resolvedColorWithTraitCollection
import platform.UIKit.systemBackgroundColor
import platform.UIKit.systemBlueColor
import platform.UIKit.systemCyanColor
import platform.UIKit.systemGreenColor
import platform.UIKit.systemIndigoColor
import platform.UIKit.systemMintColor
import platform.UIKit.systemOrangeColor
import platform.UIKit.systemPinkColor
import platform.UIKit.systemPurpleColor
import platform.UIKit.systemRedColor
import platform.UIKit.systemTealColor
import platform.UIKit.systemYellowColor
import kotlin.math.pow

private val AppleTimetableColors = listOf(
    UIColor.systemBlueColor,
    UIColor.systemGreenColor,
    UIColor.systemOrangeColor,
    UIColor.systemPurpleColor,
    UIColor.systemTealColor,
    UIColor.systemIndigoColor,
    UIColor.systemPinkColor,
    UIColor.systemMintColor,
    UIColor.systemRedColor,
    UIColor.systemCyanColor,
    UIColor.systemYellowColor,
)

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun platformTimetablePalette(darkTheme: Boolean): List<TimetableBlockColors> {
    val traitRevision = remember { mutableIntStateOf(0) }
    val observer = remember { TimetableTraitObserver() }

    UIKitView(
        modifier = Modifier.size(0.dp),
        properties = UIKitInteropProperties(isNativeAccessibilityEnabled = false),
        factory = {
            observer.onTraitsChanged = { traitRevision.intValue += 1 }
            observer
        },
        update = { view ->
            view.overrideUserInterfaceStyle =
                if (darkTheme) UIUserInterfaceStyleDark else UIUserInterfaceStyleLight
        },
        onRelease = { view -> view.onTraitsChanged = null },
    )

    return remember(darkTheme, traitRevision.intValue) {
        val traits = observer.traitCollection
        val isHighContrast = traits.accessibilityContrast == UIAccessibilityContrastHigh
        val tint =
            when {
                isHighContrast && darkTheme -> 0.46f
                isHighContrast -> 0.38f
                darkTheme -> 0.34f
                else -> 0.22f
            }
        val background = UIColor.systemBackgroundColor.resolvedColorWithTraitCollection(traits).toRgb()

        AppleTimetableColors.map { systemColor ->
            val resolvedColor = systemColor.resolvedColorWithTraitCollection(traits).toRgb()
            val container = background.mix(resolvedColor, tint)
            val content =
                if (contrastRatio(container, Black) >= MINIMUM_TEXT_CONTRAST) {
                    Black
                } else {
                    White
                }
            TimetableBlockColors(container.toComposeColor(), content.toComposeColor())
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private class TimetableTraitObserver : UIView(frame = CGRectZero.readValue()) {
    var onTraitsChanged: (() -> Unit)? = null

    override fun traitCollectionDidChange(previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        onTraitsChanged?.invoke()
    }
}

private data class Rgb(
    val red: Float,
    val green: Float,
    val blue: Float,
) {
    fun mix(other: Rgb, amount: Float) = Rgb(
        red = red * (1f - amount) + other.red * amount,
        green = green * (1f - amount) + other.green * amount,
        blue = blue * (1f - amount) + other.blue * amount,
    )

    fun relativeLuminance(): Float = LUMINANCE_RED_WEIGHT * red.toLinearSrgb() +
        LUMINANCE_GREEN_WEIGHT * green.toLinearSrgb() +
        LUMINANCE_BLUE_WEIGHT * blue.toLinearSrgb()

    fun toComposeColor() = Color(red = red, green = green, blue = blue)
}

@OptIn(ExperimentalForeignApi::class)
private fun UIColor.toRgb(): Rgb = memScoped {
    val red = alloc<CGFloatVar>()
    val green = alloc<CGFloatVar>()
    val blue = alloc<CGFloatVar>()
    val alpha = alloc<CGFloatVar>()
    if (getRed(red.ptr, green.ptr, blue.ptr, alpha.ptr)) {
        Rgb(red = red.value.toFloat(), green = green.value.toFloat(), blue = blue.value.toFloat())
    } else {
        val white = alloc<CGFloatVar>()
        if (getWhite(white.ptr, alpha.ptr)) {
            Rgb(red = white.value.toFloat(), green = white.value.toFloat(), blue = white.value.toFloat())
        } else {
            Black
        }
    }
}

private fun Float.toLinearSrgb(): Float = if (this <= SRGB_LINEAR_THRESHOLD) {
    this / SRGB_LINEAR_DIVISOR
} else {
    ((this + SRGB_OFFSET) / SRGB_SCALE).pow(SRGB_EXPONENT)
}

private fun contrastRatio(first: Rgb, second: Rgb): Float {
    val firstLuminance = first.relativeLuminance()
    val secondLuminance = second.relativeLuminance()
    val lighter = maxOf(firstLuminance, secondLuminance)
    val darker = minOf(firstLuminance, secondLuminance)
    return (lighter + WCAG_LUMINANCE_OFFSET) / (darker + WCAG_LUMINANCE_OFFSET)
}

private val Black = Rgb(0f, 0f, 0f)
private val White = Rgb(1f, 1f, 1f)
private const val MINIMUM_TEXT_CONTRAST = 4.5f

// WCAG relative-luminance and contrast constants for sRGB colors.
private const val LUMINANCE_RED_WEIGHT = 0.2126f
private const val LUMINANCE_GREEN_WEIGHT = 0.7152f
private const val LUMINANCE_BLUE_WEIGHT = 0.0722f
private const val SRGB_LINEAR_THRESHOLD = 0.04045f
private const val SRGB_LINEAR_DIVISOR = 12.92f
private const val SRGB_OFFSET = 0.055f
private const val SRGB_SCALE = 1.055f
private const val SRGB_EXPONENT = 2.4f
private const val WCAG_LUMINANCE_OFFSET = 0.05f
