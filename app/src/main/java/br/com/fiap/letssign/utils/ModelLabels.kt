package br.com.fiap.letssign.utils

import androidx.annotation.StringRes
import br.com.fiap.letssign.R
import br.com.fiap.letssign.model.DocumentStatus
import br.com.fiap.letssign.model.EventType
import br.com.fiap.letssign.model.SignerRole
import br.com.fiap.letssign.model.SignerStatus

@get:StringRes
val DocumentStatus.labelRes: Int
    get() = when (this) {
        DocumentStatus.DRAFT -> R.string.status_draft
        DocumentStatus.SENT -> R.string.status_sent
        DocumentStatus.PARTIALLY_SIGNED -> R.string.status_partially_signed
        DocumentStatus.COMPLETED -> R.string.status_completed
    }

@get:StringRes
val SignerRole.labelRes: Int
    get() = when (this) {
        SignerRole.CONTRACTOR -> R.string.role_contractor
        SignerRole.CONTRACTED -> R.string.role_contracted
        SignerRole.WITNESS -> R.string.role_witness
    }

@get:StringRes
val SignerStatus.labelRes: Int
    get() = when (this) {
        SignerStatus.PENDING -> R.string.signer_status_pending
        SignerStatus.SIGNED -> R.string.signer_status_signed
    }

@get:StringRes
val EventType.titleRes: Int
    get() = when (this) {
        EventType.CREATED -> R.string.event_created
        EventType.NEW_VERSION -> R.string.event_new_version
        EventType.SENT -> R.string.event_sent
        EventType.VIEWED -> R.string.event_viewed
        EventType.SIGNED -> R.string.event_signed
        EventType.COMPLETED -> R.string.event_completed
    }

/** Event detail; receives the actor name as the first format argument. */
@get:StringRes
val EventType.detailRes: Int
    get() = when (this) {
        EventType.CREATED -> R.string.event_created_detail
        EventType.NEW_VERSION -> R.string.event_new_version_detail
        EventType.SENT -> R.string.event_sent_detail
        EventType.VIEWED -> R.string.event_viewed_detail
        EventType.SIGNED -> R.string.event_signed_detail
        EventType.COMPLETED -> R.string.event_completed_detail
    }
