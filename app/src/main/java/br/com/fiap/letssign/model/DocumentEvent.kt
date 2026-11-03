package br.com.fiap.letssign.model

enum class EventType { CREATED, NEW_VERSION, SENT, VIEWED, SIGNED, COMPLETED }

/**
 * Entry of the document history. The text shown to the user is built from [type] and
 * [actorName] in the current language; [description] is only kept for older records.
 */
data class DocumentEvent(
    val id: String,
    val type: EventType,
    val actorName: String,
    val timestamp: Long,
    val description: String = "",
)
