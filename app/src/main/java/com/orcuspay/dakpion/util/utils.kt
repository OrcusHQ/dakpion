package com.orcuspay.dakpion.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.Modifier
import java.text.SimpleDateFormat
import java.util.*


inline fun Modifier.ifTrue(
    predicate: Boolean,
    ifFalse: () -> Modifier = { Modifier },
    ifTrue: () -> Modifier
): Modifier {
    return then(if (predicate) ifTrue() else ifFalse())
}

fun Date.toSimpleDateString(): String {
    val format = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    return format.format(this)
}

fun Date.toLogTimeFormat(): String {
    val format = SimpleDateFormat("dd.MM.yyyy hh:mma", Locale.getDefault())
    return format.format(this)
}

fun Context.openLink(url: String) {

    val link = if (!url.startsWith("http://") && !url.startsWith("https://")) {
        "https://$url"
    } else url

    val webIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse(link)
    )
    webIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    startActivity(webIntent)
}