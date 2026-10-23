package io.legado.app

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.legado.app.ui.widget.image.ModernBookCoverDrawable
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ModernBookCoverDrawableTest {
    @Test
    fun repeatedDrawingKeepsTheCoverOpaqueAndItsColorStable() {
        val cover = ModernBookCoverDrawable()
        cover.setBounds(0, 0, 40, 56)
        cover.bind("雨后书店", night = false, eInk = false)
        val first = Bitmap.createBitmap(40, 56, Bitmap.Config.ARGB_8888)
        val second = Bitmap.createBitmap(40, 56, Bitmap.Config.ARGB_8888)
        try {
            cover.draw(Canvas(first))
            cover.draw(Canvas(second))
            val before = first.getPixel(20, 4)
            val after = second.getPixel(20, 4)
            assertEquals("Scrolling must not fade the cover", before, after)
            assertEquals(255, Color.alpha(after))
        } finally {
            first.recycle()
            second.recycle()
        }
    }
}
