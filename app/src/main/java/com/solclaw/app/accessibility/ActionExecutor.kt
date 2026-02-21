package com.solclaw.app.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

enum class ScrollDirection {
    UP, DOWN, LEFT, RIGHT
}

sealed class ActionResult {
    data class Success(val message: String) : ActionResult()
    data class Failure(val message: String) : ActionResult()
}

class ActionExecutor(private val service: AccessibilityService) {

    companion object {
        private const val TAG = "SolClawAction"
    }

    /**
     * Tap on a specific node found by its ScreenElement id.
     * Walks the accessibility tree to find the matching node.
     */
    fun tap(element: ScreenElement): ActionResult {
        val rootNode = service.rootInActiveWindow ?: return ActionResult.Failure("No root window")

        val targetNode = findNodeByBounds(rootNode, element.bounds)
        if (targetNode != null) {
            val clickTarget = findClickableAncestor(targetNode) ?: targetNode
            val result = clickTarget.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Log.i(TAG, "tap #${element.id} '${element.text}' → $result")
            return if (result) {
                ActionResult.Success("Tapped '${element.text}'")
            } else {
                // Fallback: gesture tap at center of bounds
                tapAtCoordinates(element.bounds.centerX().toFloat(), element.bounds.centerY().toFloat())
            }
        }

        // Node not found by bounds, try gesture tap
        Log.w(TAG, "Node not found for #${element.id}, falling back to gesture tap")
        return tapAtCoordinates(element.bounds.centerX().toFloat(), element.bounds.centerY().toFloat())
    }

    /**
     * Tap at specific screen coordinates using gesture API.
     */
    fun tapAtCoordinates(x: Float, y: Float): ActionResult {
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
            .build()

        var gestureResult = false
        service.dispatchGesture(gesture, object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                gestureResult = true
                Log.i(TAG, "Gesture tap at ($x, $y) completed")
            }
            override fun onCancelled(gestureDescription: GestureDescription?) {
                Log.w(TAG, "Gesture tap at ($x, $y) cancelled")
            }
        }, null)

        return ActionResult.Success("Gesture tap at ($x, $y)")
    }

    /**
     * Type text into the currently focused input field, or into a specific element.
     */
    fun type(text: String, element: ScreenElement? = null): ActionResult {
        val rootNode = service.rootInActiveWindow ?: return ActionResult.Failure("No root window")

        val targetNode = if (element != null) {
            findNodeByBounds(rootNode, element.bounds)
        } else {
            findFocusedInput(rootNode)
        }

        if (targetNode == null) {
            return ActionResult.Failure("No input field found")
        }

        // Focus the node first
        targetNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
        targetNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)

        // Set the text
        val args = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        }
        val result = targetNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        Log.i(TAG, "type '$text' → $result")

        return if (result) {
            ActionResult.Success("Typed '$text'")
        } else {
            ActionResult.Failure("Failed to type text")
        }
    }

    /**
     * Scroll in a direction using gesture swipe.
     */
    fun scroll(direction: ScrollDirection): ActionResult {
        val displayMetrics = service.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels.toFloat()
        val screenHeight = displayMetrics.heightPixels.toFloat()
        val centerX = screenWidth / 2
        val centerY = screenHeight / 2
        val swipeDistance = screenHeight * 0.3f

        val path = Path()
        when (direction) {
            ScrollDirection.UP -> {
                path.moveTo(centerX, centerY)
                path.lineTo(centerX, centerY + swipeDistance) // swipe down to scroll up
            }
            ScrollDirection.DOWN -> {
                path.moveTo(centerX, centerY)
                path.lineTo(centerX, centerY - swipeDistance) // swipe up to scroll down
            }
            ScrollDirection.LEFT -> {
                path.moveTo(centerX, centerY)
                path.lineTo(centerX + swipeDistance, centerY)
            }
            ScrollDirection.RIGHT -> {
                path.moveTo(centerX, centerY)
                path.lineTo(centerX - swipeDistance, centerY)
            }
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 300))
            .build()

        service.dispatchGesture(gesture, object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                Log.i(TAG, "Scroll ${direction.name} completed")
            }
            override fun onCancelled(gestureDescription: GestureDescription?) {
                Log.w(TAG, "Scroll ${direction.name} cancelled")
            }
        }, null)

        return ActionResult.Success("Scrolling ${direction.name.lowercase()}")
    }

    /**
     * Press the Android back button.
     */
    fun back(): ActionResult {
        val result = service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        Log.i(TAG, "back() → $result")
        return if (result) ActionResult.Success("Pressed back") else ActionResult.Failure("Back failed")
    }

    /**
     * Press the Android home button.
     */
    fun home(): ActionResult {
        val result = service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
        Log.i(TAG, "home() → $result")
        return if (result) ActionResult.Success("Pressed home") else ActionResult.Failure("Home failed")
    }

    /**
     * Launch an app by its package name.
     */
    fun launchApp(packageName: String): ActionResult {
        return try {
            val intent = service.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                service.startActivity(intent)
                Log.i(TAG, "Launched app: $packageName")
                ActionResult.Success("Launched $packageName")
            } else {
                Log.w(TAG, "No launch intent for: $packageName")
                ActionResult.Failure("App not found: $packageName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch $packageName: ${e.message}")
            ActionResult.Failure("Launch failed: ${e.message}")
        }
    }

    // --- Private helpers ---

    private fun findNodeByBounds(root: AccessibilityNodeInfo, targetBounds: Rect): AccessibilityNodeInfo? {
        val nodeBounds = Rect()
        root.getBoundsInScreen(nodeBounds)
        if (nodeBounds == targetBounds) return root

        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            val found = findNodeByBounds(child, targetBounds)
            if (found != null) return found
        }
        return null
    }

    private fun findClickableAncestor(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isClickable) return node
        val parent = node.parent ?: return null
        return if (parent.isClickable) parent else null
    }

    private fun findFocusedInput(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        if (focused != null) return focused

        // Walk tree looking for a focused/editable node
        if (root.isFocused && root.isEditable) return root
        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            val found = findFocusedInput(child)
            if (found != null) return found
        }
        return null
    }
}
