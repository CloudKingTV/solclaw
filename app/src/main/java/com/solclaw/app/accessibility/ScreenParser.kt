package com.solclaw.app.accessibility

import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

class ScreenParser {

    companion object {
        private const val TAG = "SolClawParser"

        // Class name hints for type classification
        private val BUTTON_CLASSES = setOf(
            "android.widget.Button",
            "android.widget.ImageButton",
            "com.google.android.material.button.MaterialButton",
            "androidx.appcompat.widget.AppCompatButton"
        )
        private val INPUT_CLASSES = setOf(
            "android.widget.EditText",
            "android.widget.AutoCompleteTextView",
            "androidx.appcompat.widget.AppCompatEditText",
            "com.google.android.material.textfield.TextInputEditText"
        )
        private val IMAGE_CLASSES = setOf(
            "android.widget.ImageView",
            "androidx.appcompat.widget.AppCompatImageView"
        )
        private val TOGGLE_CLASSES = setOf(
            "android.widget.Switch",
            "android.widget.CheckBox",
            "android.widget.ToggleButton",
            "android.widget.RadioButton",
            "com.google.android.material.switchmaterial.SwitchMaterial",
            "androidx.appcompat.widget.SwitchCompat"
        )
        private val SCROLL_CLASSES = setOf(
            "android.widget.ScrollView",
            "android.widget.HorizontalScrollView",
            "android.widget.ListView",
            "androidx.recyclerview.widget.RecyclerView",
            "androidx.core.widget.NestedScrollView"
        )
    }

    private var nextId = 0

    fun parse(rootNode: AccessibilityNodeInfo?): List<ScreenElement> {
        if (rootNode == null) return emptyList()

        nextId = 0
        val elements = mutableListOf<ScreenElement>()
        traverseNode(rootNode, elements, parentContext = "")

        Log.i(TAG, "Parsed ${elements.size} elements from screen")
        elements.forEach { element ->
            Log.d(TAG, element.toCompactString())
        }

        return elements
    }

    private fun traverseNode(
        node: AccessibilityNodeInfo,
        elements: MutableList<ScreenElement>,
        parentContext: String
    ) {
        // Determine if this node is meaningful enough to include
        val text = node.text?.toString()?.trim() ?: ""
        val contentDesc = node.contentDescription?.toString()?.trim() ?: ""
        val className = node.className?.toString() ?: ""
        val viewId = node.viewIdResourceName?.toString() ?: ""
        val isClickable = node.isClickable
        val isEditable = node.isEditable
        val isCheckable = node.isCheckable
        val isScrollable = node.isScrollable

        // Skip invisible or non-useful nodes
        if (!node.isVisibleToUser) {
            recycleChildren(node)
            return
        }

        val type = classifyType(className, isClickable, isEditable, isCheckable, isScrollable)
        val hasContent = text.isNotEmpty() || contentDesc.isNotEmpty()
        val isInteractive = isClickable || isEditable || isCheckable || isScrollable

        // Include node if it has content or is interactive
        if (hasContent || isInteractive) {
            val bounds = Rect()
            node.getBoundsInScreen(bounds)

            elements.add(
                ScreenElement(
                    id = nextId++,
                    type = type,
                    text = text,
                    contentDescription = contentDesc,
                    viewId = viewId,
                    bounds = bounds,
                    isClickable = isClickable,
                    isEditable = isEditable,
                    isChecked = node.isChecked,
                    isScrollable = isScrollable,
                    parentContext = parentContext
                )
            )
        }

        // Build context string for children
        val contextForChildren = when {
            text.isNotEmpty() && isClickable -> "\"$text\" button"
            text.isNotEmpty() -> "\"$text\""
            contentDesc.isNotEmpty() -> "\"$contentDesc\""
            type == ElementType.SCROLL || type == ElementType.LIST -> "scrollable"
            viewId.isNotEmpty() -> viewId.substringAfterLast("/")
            else -> parentContext
        }

        // Recurse into children
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            traverseNode(child, elements, contextForChildren)
            child.recycle()
        }
    }

    private fun classifyType(
        className: String,
        isClickable: Boolean,
        isEditable: Boolean,
        isCheckable: Boolean,
        isScrollable: Boolean
    ): ElementType {
        return when {
            INPUT_CLASSES.contains(className) || isEditable -> ElementType.INPUT
            TOGGLE_CLASSES.contains(className) || isCheckable -> ElementType.TOGGLE
            BUTTON_CLASSES.contains(className) -> ElementType.BUTTON
            IMAGE_CLASSES.contains(className) -> ElementType.IMAGE
            SCROLL_CLASSES.contains(className) || isScrollable -> ElementType.SCROLL
            isClickable && className.contains("TextView") -> ElementType.LINK
            isClickable -> ElementType.BUTTON
            className.contains("TextView") || className.contains("Text") -> ElementType.TEXT
            else -> ElementType.UNKNOWN
        }
    }

    private fun recycleChildren(node: AccessibilityNodeInfo) {
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            child.recycle()
        }
    }
}
