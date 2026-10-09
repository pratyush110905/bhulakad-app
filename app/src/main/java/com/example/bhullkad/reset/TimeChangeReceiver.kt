package com.example.bhullkad.reset

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.bhullkad.BhullkadApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TimeChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val actions = setOf(
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED
        )
        if (intent.action !in actions) return
        
        val pending = goAsync()
        val manager = (context.applicationContext as BhullkadApp).resetManager
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                manager.syncNow()
            } finally {
                pending.finish()
            }
        }
    }
}
