package br.com.fiap.letssign.model

import androidx.annotation.StringRes

/** A localized validation message, optionally with format arguments (e.g. a signer name). */
data class ValidationIssue(
    @param:StringRes val messageRes: Int,
    val args: List<String> = emptyList(),
)
