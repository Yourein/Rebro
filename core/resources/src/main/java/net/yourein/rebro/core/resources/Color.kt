package net.yourein.rebro.core.resources

import androidx.compose.ui.graphics.Color

/**
 * Rebro アプリのテーマで利用するカラー定義。
 *
 * Figma: https://www.figma.com/design/V8tMVZSV37qeOyfYbRstI7/Rebro?node-id=3-7
 */
object RebroColor {
    /** Rebro Brand Color */
    val Brand = Color(0xFFFAA755)

    /** Rebro Highlight Color */
    val Highlight = Color(0xFF36ADF8)

    /** Rebro Background Color */
    val Background = Color(0xFF1A1A1A)

    /** Rebro Surface Color — Background より少し明るい。BottomNavigation 等の浮き上がった面に使う */
    val Surface = Color(0xFF2A2A2A)

    /** Rebro Text Primary Color */
    val TextPrimary = Color(0xFFF0F0F0)

    /** Rebro Text Secondary */
    val TextSecondary = Color(0xFFC0C0C0)
}
