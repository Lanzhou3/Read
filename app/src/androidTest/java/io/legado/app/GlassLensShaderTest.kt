package io.legado.app

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ColorSpace
import android.graphics.HardwareRenderer
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RenderNode
import android.graphics.Shader
import android.hardware.HardwareBuffer
import android.media.ImageReader
import android.widget.FrameLayout
import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import io.legado.app.ui.widget.glass.GlassLensShader
import io.legado.app.ui.widget.glass.GlassOpticsView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 33)
class GlassLensShaderTest {
    @Test
    fun opticalBackgroundDoesNotExpandWrapContentChrome() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val context = ApplicationProvider.getApplicationContext<android.content.Context>()
            val surface = FrameLayout(context)
            surface.addView(GlassOpticsView(context), FrameLayout.LayoutParams(-1, -1))
            surface.addView(View(context), FrameLayout.LayoutParams(-1, 88))
            surface.measure(View.MeasureSpec.makeMeasureSpec(200, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.AT_MOST))
            assertEquals("The foreground determines chrome height", 88, surface.measuredHeight)
        }
    }

    @Test
    fun lensDisplacesBackgroundAndSeparatesChannelsOnlyNearPerimeter() {
        val flat = render(0f, 0f)
        val lens = render(1f, 1f)
        try {
            var displaced = 0
            var dispersed = 0
            for (y in 62..82) for (x in 33..44) {
                val before = flat.getPixel(x, y)
                val after = lens.getPixel(x, y)
                if (abs(Color.green(before) - Color.green(after)) > 8) displaced++
                if (abs(Color.red(after) - Color.blue(after)) > 8) dispersed++
            }
            assertTrue("No visible background refraction: $displaced", displaced > 100)
            assertTrue("No wavelength separation: $dispersed", dispersed > 100)
            for (y in 65..79) for (x in 90..134) {
                val before = flat.getPixel(x, y)
                val after = lens.getPixel(x, y)
                assertTrue("Lens center must stay undistorted", abs(Color.green(before) - Color.green(after)) <= 2)
                assertTrue("Dispersion must be confined to the curved edge", abs(Color.red(after) - Color.blue(after)) <= 2)
            }
            println("Glass optics: displaced=$displaced, dispersed=$dispersed, center=unchanged")
        } finally {
            flat.recycle()
            lens.recycle()
        }
    }

    private fun render(refraction: Float, dispersion: Float): Bitmap {
        val width = 224
        val height = 144
        val reader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2,
            HardwareBuffer.USAGE_GPU_SAMPLED_IMAGE or HardwareBuffer.USAGE_GPU_COLOR_OUTPUT)
        val renderer = HardwareRenderer()
        try {
            val source = RenderNode("Test stripes").apply { setPosition(0, 0, width, height) }
            val canvas = source.beginRecording()
            val paint = Paint().apply {
                shader = LinearGradient(0f, 0f, 12f, 0f, Color.BLACK, Color.WHITE, Shader.TileMode.MIRROR)
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            source.endRecording()
            source.setRenderEffect(GlassLensShader.effect(32f, 32f, 160f, 80f, 24f, 1f, 70,
                refraction, dispersion))
            val root = RenderNode("Test root").apply { setPosition(0, 0, width, height) }
            root.beginRecording().drawRenderNode(source)
            root.endRecording()
            renderer.setSurface(reader.surface)
            renderer.setContentRoot(root)
            renderer.createRenderRequest().setWaitForPresent(true).syncAndDraw()
            val image = requireNotNull(reader.acquireNextImage()) { "Hardware renderer produced no frame" }
            image.use {
                val buffer = requireNotNull(it.hardwareBuffer)
                buffer.use {
                    val hardware = requireNotNull(Bitmap.wrapHardwareBuffer(buffer,
                        ColorSpace.get(ColorSpace.Named.SRGB)))
                    try { return requireNotNull(hardware.copy(Bitmap.Config.ARGB_8888, false)) }
                    finally { hardware.recycle() }
                }
            }
        } finally {
            renderer.destroy()
            reader.close()
        }
    }
}
