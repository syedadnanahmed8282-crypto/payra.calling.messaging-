package com.payra.calling.designsystem

import android.content.Context
import android.content.Intent
import android.widget.Toast

/** Opens the phone's share sheet with an invitation text. */
fun shareInvite(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Join me on Payra, a fast and friendly chat and calling app!")
        }
        context.startActivity(Intent.createChooser(intent, "Invite friends").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: Exception) {
        Toast.makeText(context, "Nothing to share with", Toast.LENGTH_SHORT).show()
    }
}

fun shareText(context: Context, text: String, title: String = "Share") {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, title).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: Exception) {
        Toast.makeText(context, "Nothing to share with", Toast.LENGTH_SHORT).show()
    }
}
