package dev.mindroid.host

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject

/**
 * Minimal JS-driven native widget renderer.
 *
 * Architecture:
 *  JS side declares a virtual widget tree as JSON (see WidgetSpec).
 *  The renderer materializes it into Android Views inside a host ViewGroup.
 *
 * Widget types supported in v1:
 *   - View       (plain container)
 *   - Text       (TextView)
 *   - Button     (Button)
 *   - ScrollView (vertical scroll container)
 *   - Column     (LinearLayout vertical)
 *   - Row        (LinearLayout horizontal)
 */
class NativeRenderer(private val context: Context) {
    private val eventHandlers = mutableMapOf<String, () -> Unit>()

    /**
     * Takes a JSON widget tree (array of WidgetSpec nodes) and renders it into [container].
     * Clears existing children first.
     */
    fun render(container: ViewGroup, specJson: String) {
        container.removeAllViews()
        eventHandlers.clear()
        val nodes = JSONArray(specJson)
        for (i in 0 until nodes.length()) {
            val node = nodes.getJSONObject(i)
            container.addView(buildView(node))
        }
    }

    /**
     * Dispatches a registered event handler identified by [handlerId].
     * Called from the JS bridge when a user action is received.
     */
    fun dispatch(handlerId: String) {
        eventHandlers[handlerId]?.invoke()
    }

    private fun buildView(spec: JSONObject): View {
        val type = spec.optString("type", "View")
        val children = spec.optJSONArray("children")

        return when (type) {
            "Text" -> buildText(spec)
            "Button" -> buildButton(spec)
            "ScrollView" -> buildScroll(spec, children)
            "Column" -> buildLinear(spec, children, LinearLayout.VERTICAL)
            "Row" -> buildLinear(spec, children, LinearLayout.HORIZONTAL)
            else -> buildLinear(spec, children, LinearLayout.VERTICAL)
        }
    }

    private fun buildText(spec: JSONObject): TextView {
        return TextView(context).apply {
            text = spec.optString("text", "")
            textSize = spec.optDouble("size", 14.0).toFloat()
        }
    }

    private fun buildButton(spec: JSONObject): Button {
        val handlerId = spec.optString("onPress", "")
        return Button(context).apply {
            text = spec.optString("text", "")
            setOnClickListener {
                if (handlerId.isNotBlank()) {
                    eventHandlers[handlerId]?.invoke()
                }
            }
        }
    }

    private fun buildScroll(spec: JSONObject, children: JSONArray?): ScrollView {
        val inner = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        addChildren(inner, children)
        return ScrollView(context).apply {
            addView(inner)
        }
    }

    private fun buildLinear(spec: JSONObject, children: JSONArray?, orientation: Int): LinearLayout {
        return LinearLayout(context).apply {
            this.orientation = orientation
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            addChildren(this, children)
        }
    }

    private fun addChildren(parent: ViewGroup, children: JSONArray?) {
        children ?: return
        for (i in 0 until children.length()) {
            parent.addView(buildView(children.getJSONObject(i)))
        }
    }
}
