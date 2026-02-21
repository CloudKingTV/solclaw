package com.solclaw.app.accessibility

import android.graphics.Rect

enum class ElementType {
    BUTTON, TEXT, INPUT, IMAGE, TOGGLE, LINK, LIST, SCROLL, UNKNOWN
}

data class ScreenElement(
    val id: Int,
    val type: ElementType,
    val text: String,
    val contentDescription: String,
    val viewId: String,
    val bounds: Rect,
    val isClickable: Boolean,
    val isEditable: Boolean,
    val isChecked: Boolean,
    val isScrollable: Boolean,
    val parentContext: String
) {
    fun toCompactString(): String {
        val parts = mutableListOf<String>()
        parts.add("#$id")
        parts.add(type.name.lowercase())
        if (text.isNotEmpty()) parts.add("\"$text\"")
        if (contentDescription.isNotEmpty()) parts.add("desc=\"$contentDescription\"")
        if (isClickable) parts.add("clickable")
        if (isEditable) parts.add("editable")
        if (isChecked) parts.add("checked")
        if (parentContext.isNotEmpty()) parts.add("in=$parentContext")
        return parts.joinToString(" ")
    }
}
