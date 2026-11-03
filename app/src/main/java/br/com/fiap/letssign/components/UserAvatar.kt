package br.com.fiap.letssign.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import br.com.fiap.letssign.ui.theme.Dimens

@Composable
fun UserAvatar(name: String, color: Color = MaterialTheme.colorScheme.primary, modifier: Modifier = Modifier) {
    Surface(color = color.copy(alpha = 0.14f), shape = CircleShape, modifier = modifier.size(Dimens.avatar)) {
        Box(contentAlignment = Alignment.Center) {
            Text(initials(name), color = color, fontWeight = FontWeight.Bold)
        }
    }
}

fun initials(name: String): String =
    name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
