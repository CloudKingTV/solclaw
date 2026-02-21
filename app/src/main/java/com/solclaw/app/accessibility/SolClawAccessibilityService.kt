package com.solclaw.app.accessibility

import android.accessibilityservice.AccessibilityService
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

data class ScreenSnapshot(
    val packageName: String = "",
    val elements: List<ScreenElement> = emptyList(),
    val timestamp: Long = 0L
)

class SolClawAccessibilityService : AccessibilityService() {

    private val screenParser = ScreenParser()

    companion object {
        private const val TAG = "SolClawA11y"

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        private val _foregroundApp = MutableStateFlow(ForegroundAppInfo())
        val foregroundApp: StateFlow<ForegroundAppInfo> = _foregroundApp.asStateFlow()

        private val _screenSnapshot = MutableStateFlow(ScreenSnapshot())
        val screenSnapshot: StateFlow<ScreenSnapshot> = _screenSnapshot.asStateFlow()
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

                // Parse the full screen tree
                parseCurrentScreen(pkg)
            }

            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                // Only re-parse on significant content changes to avoid flooding
                val pkg = event.packageName?.toString() ?: "unknown"
                val source = event.source
                if (source != null) {
                    // Only log, don't full-parse on every content change
                    Log.v(TAG, "Content changed → pkg=$pkg")
                    source.recycle()
                }
            }
        }
    }

    private fun parseCurrentScreen(packageName: String) {
        try {
            val rootNode = rootInActiveWindow ?: run {
                Log.w(TAG, "No root node available for parsing")
                return
            }

            val elements = screenParser.parse(rootNode)

            _screenSnapshot.value = ScreenSnapshot(
                packageName = packageName,
                elements = elements,
                timestamp = System.currentTimeMillis()
            )

            Log.i(TAG, "Screen snapshot: ${elements.size} elements from $packageName")
            rootNode.recycle()
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing screen: ${e.message}", e)
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
