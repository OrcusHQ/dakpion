package com.orcuspay.dakpion

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import com.orcuspay.dakpion.util.Diag

/**
 * Read-only view of the [Diag] event trail for remote debugging:
 *
 *   adb shell content query --uri content://com.dakpion.app.diag/events
 *
 * Needed because some ROMs suppress app logs *and* time out service dumps.
 * Exposes only pipeline events (timestamps, sender ids, outcomes) — never
 * SMS bodies or credentials.
 */
class DiagProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor {
        val cursor = MatrixCursor(arrayOf("_id", "event"))
        val ctx = context ?: return cursor
        Diag.events(ctx).forEachIndexed { i, e -> cursor.addRow(arrayOf(i, e)) }
        return cursor
    }

    override fun getType(uri: Uri): String = "vnd.android.cursor.dir/vnd.dakpion.diag"
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
