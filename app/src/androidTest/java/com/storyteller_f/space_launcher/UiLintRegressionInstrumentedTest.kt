package com.storyteller_f.space_launcher

import android.content.ComponentName
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.SystemClock
import android.os.UserHandle
import android.view.MotionEvent
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.storyteller_f.space_launcher.data.AppItem
import com.storyteller_f.space_launcher.ui.AppAdapter
import com.storyteller_f.space_launcher.ui.AppDiff
import com.storyteller_f.space_launcher.ui.WidgetPickerDiff
import com.storyteller_f.space_launcher.ui.WidgetPickerItem
import com.storyteller_f.space_launcher.widget.WidgetCellLayout
import com.storyteller_f.space_launcher.widget.WidgetDragHelper
import com.storyteller_f.space_launcher.widget.WidgetPanelScrollView
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UiLintRegressionInstrumentedTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    @Test
    fun gridDelegatesUnhandledTapsAndAccessibilityClicks() {
        instrumentation.runOnMainSync {
            val grid = WidgetCellLayout(context)
            WidgetDragHelper(grid, {}, {}).attach()
            grid.layout(0, 0, 300, 300)
            var clicks = 0
            grid.setOnClickListener { clicks++ }
            touch(grid, MotionEvent.ACTION_DOWN, 20f, 20f)
            touch(grid, MotionEvent.ACTION_UP, 20f, 20f)
            assertEquals(1, clicks)
            assertTrue(grid.performAccessibilityAction(AccessibilityNodeInfo.ACTION_CLICK, null))
            assertEquals(2, clicks)
            touch(grid, MotionEvent.ACTION_DOWN, 20f, 20f)
            touch(grid, MotionEvent.ACTION_CANCEL, 20f, 20f)
            assertEquals(2, clicks)
        }
    }

    @Test
    fun scrollViewClicksAreAccessibleAndDoNotFireForScrollOrCancel() {
        instrumentation.runOnMainSync {
            val scroll = WidgetPanelScrollView(context)
            scroll.addView(View(context), android.widget.FrameLayout.LayoutParams(300, 1200))
            scroll.measure(
                View.MeasureSpec.makeMeasureSpec(300, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(300, View.MeasureSpec.EXACTLY)
            )
            scroll.layout(0, 0, 300, 300)
            var clicks = 0
            scroll.setOnClickListener { clicks++ }
            touch(scroll, MotionEvent.ACTION_DOWN, 100f, 100f)
            touch(scroll, MotionEvent.ACTION_UP, 100f, 100f)
            assertEquals(1, clicks)
            assertTrue(scroll.performAccessibilityAction(AccessibilityNodeInfo.ACTION_CLICK, null))
            assertEquals(2, clicks)
            touch(scroll, MotionEvent.ACTION_DOWN, 100f, 100f)
            touch(scroll, MotionEvent.ACTION_MOVE, 100f, 20f)
            touch(scroll, MotionEvent.ACTION_UP, 100f, 100f)
            touch(scroll, MotionEvent.ACTION_DOWN, 100f, 100f)
            touch(scroll, MotionEvent.ACTION_CANCEL, 100f, 100f)
            assertEquals(2, clicks)
        }
    }

    @Test
    fun appDiffDistinguishesProfilesAndRenderedChanges() {
        val app = app("Alpha")
        assertTrue(AppDiff.areItemsTheSame(app, app.copy(label = "Renamed")))
        assertFalse(AppDiff.areContentsTheSame(app, app.copy(label = "Renamed")))
        assertFalse(AppDiff.areItemsTheSame(app, app.copy(user = UserHandle.getUserHandleForUid(1000000))))
        assertFalse(AppDiff.areItemsTheSame(app, app.copy(componentName = ComponentName("example", "Other"))))
        assertFalse(AppDiff.areContentsTheSame(app, app.copy(icon = ColorDrawable(Color.BLUE))))
        assertTrue(AppDiff.areContentsTheSame(app, app.copy()))
    }

    @Test
    fun widgetDiffTracksLabelsSizesAndIcons() {
        val info = android.appwidget.AppWidgetManager.getInstance(context).installedProviders.first()
        val item = WidgetPickerItem(info, "Widget", "App", 50, 50, 1)
        assertTrue(WidgetPickerDiff.areItemsTheSame(item, item.copy(label = "Renamed")))
        assertFalse(WidgetPickerDiff.areItemsTheSame(item, item.copy(profile = UserHandle.getUserHandleForUid(1000000))))
        assertFalse(WidgetPickerDiff.areItemsTheSame(item, item.copy(provider = ComponentName("example", "Other"))))
        assertTrue(WidgetPickerDiff.areContentsTheSame(item, item.copy()))
        assertFalse(WidgetPickerDiff.areContentsTheSame(item, item.copy(label = "Renamed")))
        assertFalse(WidgetPickerDiff.areContentsTheSame(item, item.copy(appLabel = "Other")))
        assertFalse(WidgetPickerDiff.areContentsTheSame(item, item.copy(minWidth = 100)))
        assertFalse(WidgetPickerDiff.areContentsTheSame(item, item.copy(minHeight = 100)))
        assertFalse(WidgetPickerDiff.areContentsTheSame(item, item.copy(iconResource = 2)))
    }

    @Test
    fun appFilteringUsesGranularUpdatesAndKeepsQueryWhenAppsChange() {
        val original = mutableListOf(app("Alpha"), app("Beta"))
        lateinit var adapter: AppAdapter
        var fullRefreshes = 0
        instrumentation.runOnMainSync {
            adapter = AppAdapter(original) {}
            adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onChanged() { fullRefreshes++ }
            })
            original.clear()
            assertEquals(2, adapter.itemCount)
            adapter.filter("ALP")
        }
        awaitLabels(adapter, listOf("Alpha"))
        instrumentation.runOnMainSync {
            adapter.updateApps(listOf(app("Alpine"), app("Gamma")))
        }
        awaitLabels(adapter, listOf("Alpine"))
        instrumentation.runOnMainSync { adapter.filter("") }
        awaitLabels(adapter, listOf("Alpine", "Gamma"))
        instrumentation.runOnMainSync { assertEquals(0, fullRefreshes) }
    }

    private fun awaitLabels(adapter: AppAdapter, expected: List<String>) {
        val deadline = SystemClock.uptimeMillis() + 5000
        var labels = emptyList<String>()
        do {
            instrumentation.runOnMainSync { labels = adapter.currentList.map { it.label } }
            if (labels == expected) return
            SystemClock.sleep(20)
        } while (SystemClock.uptimeMillis() < deadline)
        assertEquals(expected, labels)
    }

    private fun app(label: String) = AppItem(
        label, "example", ComponentName("example", label), ColorDrawable(Color.RED), UserHandle.getUserHandleForUid(0)
    )

    private fun touch(view: View, action: Int, x: Float, y: Float) {
        val time = SystemClock.uptimeMillis()
        val event = MotionEvent.obtain(time, time, action, x, y, 0)
        try { view.onTouchEvent(event) } finally { event.recycle() }
    }
}
