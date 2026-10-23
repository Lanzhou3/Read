package io.legado.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.view.ContextThemeWrapper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.legado.app.lib.theme.view.ThemeBottomNavigationVIew
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WholeItemNavigationTest {
    @Test
    fun selectionCoversTheLabelAndMovesWithNavigation() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val base = ApplicationProvider.getApplicationContext<Context>()
            val context = ContextThemeWrapper(base, R.style.AppTheme_Light)
            val root = LayoutInflater.from(context).inflate(R.layout.activity_main, null)
            val density = context.resources.displayMetrics.density
            root.measure(View.MeasureSpec.makeMeasureSpec((400 * density).toInt(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec((800 * density).toInt(), View.MeasureSpec.EXACTLY))
            root.layout(0, 0, root.measuredWidth, root.measuredHeight)
            val nav = root.findViewById<ThemeBottomNavigationVIew>(R.id.bottom_navigation_view)
            var received = 0
            nav.setOnItemSelectedListener { received = it.itemId; true }
            val shelf = nav.findViewById<View>(R.id.menu_bookshelf)
            val profile = nav.findViewById<View>(R.id.menu_my_config)
            fun labelBackgroundAlpha(item: View): Int {
                val drawable = item.background.constantState!!.newDrawable().mutate()
                drawable.state = item.drawableState
                drawable.setBounds(0, 0, item.width, item.height)
                val bitmap = Bitmap.createBitmap(item.width, item.height, Bitmap.Config.ARGB_8888)
                return try {
                    drawable.draw(Canvas(bitmap))
                    Color.alpha(bitmap.getPixel(item.width / 2, item.height - (8 * density).toInt()))
                } finally { bitmap.recycle() }
            }
            assertTrue("The checked surface must extend behind the label", labelBackgroundAlpha(shelf) > 200)
            assertEquals(0, labelBackgroundAlpha(profile))
            profile.performClick()
            assertEquals(R.id.menu_my_config, received)
            assertEquals(R.id.menu_my_config, nav.selectedItemId)
            assertEquals(0, labelBackgroundAlpha(shelf))
            assertTrue("The complete checked surface must follow selection", labelBackgroundAlpha(profile) > 200)
        }
    }
}
