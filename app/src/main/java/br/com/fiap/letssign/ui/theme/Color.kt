package br.com.fiap.letssign.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import br.com.fiap.letssign.model.DocumentStatus
import br.com.fiap.letssign.model.EventType

val Indigo = Color(0xFF5B4CF5)
val IndigoLight = Color(0xFF7062FF)
val IndigoDark = Color(0xFF2A1F8F)
val IndigoDeep = Color(0xFF17123D)
val Violet = Color(0xFF8B5CF6)
val Teal = Color(0xFF14B8A6)
val Amber = Color(0xFFF59E0B)
val Emerald = Color(0xFF10B981)
val Sky = Color(0xFF0EA5E9)
val Slate = Color(0xFF64748B)
val Ink = Color(0xFF0F1222)

/** Signature ink and the guide line of the signature pad. */
val SignatureInk = Color(0xFF1E1B4B)
val SignatureGuide = Color(0xFFCBD5E1)
val SignatureHint = Color(0xFF94A3B8)

/** Brand gradient used by headers and highlights. */
val BrandGradient = Brush.linearGradient(listOf(IndigoLight, Indigo, IndigoDark))

/** Colors that tell signers apart on the field placement screen. */
val SignerPalette = listOf(
    Color(0xFF5B4CF5), Color(0xFF0EA5E9), Color(0xFFE11D48), Color(0xFF10B981),
    Color(0xFFF59E0B), Color(0xFF8B5CF6), Color(0xFF14B8A6), Color(0xFFDB2777),
)

fun signerColor(index: Int): Color = SignerPalette[Math.floorMod(index, SignerPalette.size)]

fun statusColor(status: DocumentStatus): Color = when (status) {
    DocumentStatus.DRAFT -> Slate
    DocumentStatus.SENT -> Sky
    DocumentStatus.PARTIALLY_SIGNED -> Amber
    DocumentStatus.COMPLETED -> Emerald
}

fun eventColor(type: EventType): Color = when (type) {
    EventType.CREATED, EventType.NEW_VERSION -> Slate
    EventType.SENT -> Indigo
    EventType.VIEWED -> Amber
    EventType.SIGNED, EventType.COMPLETED -> Emerald
}
