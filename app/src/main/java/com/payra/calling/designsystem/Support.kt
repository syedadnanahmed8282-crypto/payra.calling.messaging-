package com.payra.calling.designsystem

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast

const val SupportEmail = "support@payra.app"

/** Opens the email app with a ready message to Payra support (optionally with a screenshot attached). */
fun emailSupport(context: Context, subject: String, body: String, attachment: Uri? = null) {
    try {
        val intent = Intent(if (attachment != null) Intent.ACTION_SEND else Intent.ACTION_SENDTO).apply {
            if (attachment != null) {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(SupportEmail))
                putExtra(Intent.EXTRA_STREAM, attachment)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                data = Uri.parse("mailto:$SupportEmail")
            }
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        context.startActivity(Intent.createChooser(intent, "Send with").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: ActivityNotFoundException) {
        Log.w("PayraSupport", "no email app", e)
        Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Log.e("PayraSupport", "could not open the email app", e)
        Toast.makeText(context, "Could not open the email app", Toast.LENGTH_SHORT).show()
    }
}
