package com.solclaw.app.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ForegroundAppInfo(
    val packageName: String = "",
    val className: String = "",
    val timestamp: Long = 0L
)

class SolClawAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "SolClawA11y"

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        private val _foregroundApp = MutableStateFlow(ForegroundAppInfo())
        val foregroundApp: StateFlow<ForegroundAppInfo> = _foregroundApp.asStateFlow()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        _isRunning.value = true
        Log.i(TAG, "SolClaw Accessibility Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                val pkg = event.packageName?.toString() ?: "unknown"
                val cls = event.className?.toString() ?: "unknown"

                Log.d(TAG, "Window changed → pkg=$pkg class=$cls")

                _foregroundApp.value = ForegroundAppInfo(
                    packageName = pkg,
                    className = cls,
                    timestamp = System.currentTimeMillis()
                )
            }

            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                val pkg = event.packageName?.toString() ?: "unknown"
                Log.v(TAG, "Content changed → pkg=$pkg")
            }
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "SolClaw Accessibility Service interrupted")
    }

    override fun onDestroy() {
        _isRunning.value = false
        Log.i(TAG, "SolClaw Accessibility Service destroyed")
        super.onDestroy()
    }
}
