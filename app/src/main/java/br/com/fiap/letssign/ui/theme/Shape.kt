package br.com.fiap.letssign.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val LetsSignShapes = Shapes(
    extraSmall = RoundedCornerShape(14.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

object AppShapes {
    val field = RoundedCornerShape(16.dp)
    val button = RoundedCornerShape(18.dp)
    val card = RoundedCornerShape(22.dp)
    val cardLarge = RoundedCornerShape(24.dp)
    val sheet = RoundedCornerShape(28.dp)
    val bottomBar = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val header = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
    val headerLarge = RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp)
    val iconBox = RoundedCornerShape(14.dp)
    val tile = RoundedCornerShape(20.dp)
    val message = RoundedCornerShape(16.dp)
    val pdfPage = RoundedCornerShape(4.dp)
    val signatureField = RoundedCornerShape(3.dp)
}
