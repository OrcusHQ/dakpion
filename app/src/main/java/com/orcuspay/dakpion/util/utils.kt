package com.orcuspay.dakpion.util

import androidx.compose.ui.Modifier


inline fun Modifier.ifTrue(
    predicate: Boolean,
    ifFalse: () -> Modifier = { Modifier },
    ifTrue: () -> Modifier
): Modifier {
    return then(if (predicate) ifTrue() else ifFalse())
}
