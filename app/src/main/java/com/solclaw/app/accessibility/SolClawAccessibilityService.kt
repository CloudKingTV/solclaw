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
    lateinit var actionExecutor: ActionExecutor
        private set

    companion object {
        private const val TAG = "SolClawA11y"

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        private val _foregroundApp = MutableStateFlow(ForegroundAppInfo())
        val foregroundApp: StateFlow<ForegroundAppInfo> = _foregroundApp.asStateFlow()

        private val _screenSnapshot = MutableStateFlow(ScreenSnapshot())
        val screenSnapshot: StateFlow<ScreenSnapshot> = _screenSnapshot.asStateFlow()

        private var _instance: SolClawAccessibilityService? = null
        val instance: SolClawAccessibilityService? get() = _instance
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        actionExecutor = ActionExecutor(this)
        _instance = this
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
                val pkg = event.packageName?.toString() ?: "unknown"
                Log.v(TAG, "Content changed → pkg=$pkg")
            }
        }
    }

    fun parseCurrentScreen(packageName: String? = null): ScreenSnapshot {
        try {
            val rootNode = rootInActiveWindow ?: run {
                Log.w(TAG, "No root node available for parsing")
                return _screenSnapshot.value
            }

            val pkg = packageName ?: _foregroundApp.value.packageName
            val elements = screenParser.parse(rootNode)

            val snapshot = ScreenSnapshot(
                packageName = pkg,
                elements = elements,
                timestamp = System.currentTimeMillis()
            )
            _screenSnapshot.value = snapshot

            Log.i(TAG, "Screen snapshot: ${elements.size} elements from $pkg")
            return snapshot
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing screen: ${e.message}", e)
            return _screenSnapshot.value
        }
    }

    /**
     * Test action: Launch Chrome, wait, then parse the screen and tap the address bar.
     */
    fun testLaunchChromeAndTapAddressBar() {
        Log.i(TAG, "=== TEST: Launch Chrome and tap address bar ===")

        // Step 1: Launch Chrome
        val launchResult = actionExecutor.launchApp("com.android.chrome")
        Log.i(TAG, "Launch result: $launchResult")

        // Note: In a real agent loop, we'd wait for TYPE_WINDOW_STATE_CHANGED
        // before parsing. For now, we schedule a delayed parse.
        android.os.Handler(mainLooper).postDelayed({
            // Step 2: Parse the screen
            val snapshot = parseCurrentScreen("com.android.chrome")
            Log.i(TAG, "Chrome screen: ${snapshot.elements.size} elements")

            // Step 3: Find and tap the address/search bar
            val addressBar = snapshot.elements.find { element ->
                element.type == ElementType.INPUT ||
                element.text.contains("Search", ignoreCase = true) ||
                element.contentDescription.contains("Search", ignoreCase = true) ||
                element.contentDescription.contains("address", ignoreCase = true) ||
                element.viewId.contains("url_bar", ignoreCase = true) ||
                element.viewId.contains("search_box", ignoreCase = true)
            }

            if (addressBar != null) {
                Log.i(TAG, "Found address bar: ${addressBar.toCompactString()}")
                val tapResult = actionExecutor.tap(addressBar)
                Log.i(TAG, "Tap result: $tapResult")
            } else {
                Log.w(TAG, "Address bar not found in ${snapshot.elements.size} elements")
                snapshot.elements.forEach { Log.d(TAG, "  ${it.toCompactString()}") }
            }
        }, 2000) // 2 second delay for Chrome to load
    }

    override fun onInterrupt() {
        Log.w(TAG, "SolClaw Accessibility Service interrupted")
    }

    override fun onDestroy() {
        _instance = null
        _isRunning.value = false
        Log.i(TAG, "SolClaw Accessibility Service destroyed")
        super.onDestroy()
    }
}
