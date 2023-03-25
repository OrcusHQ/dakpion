package com.orcuspay.dakpion

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage


class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != null && intent.action == SMS_RECEIVED) {
            val bundle = intent.extras
            if (bundle != null) {
                val pdus = bundle.getString(SMS_BUNDLE) as Array<*>? ?: return
                val format = bundle.getString(EXTRA_BUNDLE)
                val messages = mutableListOf<SmsMessage>()
                var smsMessage: String? = ""
                var phoneNumber: String? = ""
                for (i in pdus.indices) {
                    messages[i] = SmsMessage.createFromPdu(pdus[i] as ByteArray, format)
                    smsMessage += messages[i].messageBody
                    phoneNumber = messages[i].originatingAddress
                }

                // Trigger your background task here, passing the message and phone number
                startBackgroundTask(context, smsMessage!!, phoneNumber!!)
            }
        }
    }

    private fun startBackgroundTask(context: Context, smsMessage: String, phoneNumber: String) {
        // You can start a background service, a Worker or any other background task here
    }

    companion object {
        const val SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED"
        const val SMS_BUNDLE = "pdus"
        const val EXTRA_BUNDLE = "pdus"
    }
}
