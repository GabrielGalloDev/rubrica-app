package br.com.fiap.letssign.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import br.com.fiap.letssign.R

// Variable font: a single file covers every weight.
@OptIn(ExperimentalTextApi::class)
private fun jakarta(weight: Int) = Font(
    R.font.plus_jakarta_sans,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val Jakarta = FontFamily(jakarta(400), jakarta(500), jakarta(600), jakarta(700), jakarta(800))

val LetsSignTypography = Typography().run {
    fun TextStyle.styled(weight: FontWeight, tracking: Double = 0.0) =
        copy(fontFamily = Jakarta, fontWeight = weight, letterSpacing = tracking.sp)
    Typography(
        displayLarge = displayLarge.styled(FontWeight.ExtraBold, -1.0),
        displayMedium = displayMedium.styled(FontWeight.ExtraBold, -0.8),
        displaySmall = displaySmall.styled(FontWeight.Bold, -0.6),
        headlineLarge = headlineLarge.styled(FontWeight.ExtraBold, -0.6),
        headlineMedium = headlineMedium.styled(FontWeight.Bold, -0.5),
        headlineSmall = headlineSmall.styled(FontWeight.Bold, -0.3),
        titleLarge = titleLarge.styled(FontWeight.Bold, -0.2),
        titleMedium = titleMedium.styled(FontWeight.SemiBold),
        titleSmall = titleSmall.styled(FontWeight.SemiBold),
        bodyLarge = bodyLarge.styled(FontWeight.Normal),
        bodyMedium = bodyMedium.styled(FontWeight.Normal),
        bodySmall = bodySmall.styled(FontWeight.Medium),
        labelLarge = labelLarge.styled(FontWeight.SemiBold),
        labelMedium = labelMedium.styled(FontWeight.SemiBold),
        labelSmall = labelSmall.styled(FontWeight.SemiBold),
    )
}
