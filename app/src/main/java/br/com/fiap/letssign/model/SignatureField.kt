package br.com.fiap.letssign.model

/**
 * Signature field. Position and size are relative to the page size (0..1), with the origin at
 * the top-left corner, so they don't depend on the device resolution.
 */
data class SignatureField(
    val id: String,
    val signerId: String,
    val page: Int,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
) {
    val isInsidePage: Boolean
        get() = x >= 0f && y >= 0f && width > 0f && height > 0f &&
            x + width <= 1.0001f && y + height <= 1.0001f
}
