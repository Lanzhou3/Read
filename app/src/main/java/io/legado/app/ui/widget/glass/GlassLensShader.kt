package io.legado.app.ui.widget.glass

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import androidx.annotation.RequiresApi

/** Rounded lens: curved edge displacement and wavelength-dependent background sampling. */
@RequiresApi(33)
internal object GlassLensShader {
    fun effect(
        originX: Float, originY: Float, width: Float, height: Float,
        radius: Float, density: Float, strength: Int,
        refraction: Float = 1f, dispersion: Float = 1f
    ): RenderEffect {
        val shader = RuntimeShader(SOURCE)
        shader.setFloatUniform("origin", originX, originY)
        shader.setFloatUniform("size", width, height)
        shader.setFloatUniform("radius", radius.coerceAtMost(minOf(width, height) / 2f))
        shader.setFloatUniform("edgeWidth", 14f * density)
        shader.setFloatUniform("depth", (5f + strength.coerceIn(0, 100) * 0.055f) * density * refraction)
        shader.setFloatUniform("dispersion", dispersion)
        val lens = RenderEffect.createRuntimeShaderEffect(shader, "content")
        // Small blur keeps the refracted backdrop identifiable. Controls are drawn afterwards.
        val blur = RenderEffect.createBlurEffect(2f * density, 2f * density, Shader.TileMode.CLAMP)
        return RenderEffect.createChainEffect(lens, blur)
    }

    private const val SOURCE = """
        uniform shader content;
        uniform float2 origin;
        uniform float2 size;
        uniform float radius;
        uniform float edgeWidth;
        uniform float depth;
        uniform float dispersion;

        half4 main(float2 position) {
            float2 p = position - origin - size * 0.5;
            float2 q = abs(p) - (size * 0.5 - radius);
            float2 outside = max(q, float2(0.0));
            float outsideLength = length(outside);
            float distance = outsideLength + min(max(q.x, q.y), 0.0) - radius;
            float2 normal = outsideLength > 0.001
                ? outside / max(outsideLength, 0.001) * sign(p)
                : (q.x > q.y ? float2(sign(p.x), 0.0) : float2(0.0, sign(p.y)));
            // Curved perimeter flattens smoothly into the center of the lens.
            float edge = 1.0 - smoothstep(0.0, edgeWidth, max(-distance, 0.0));
            float bend = depth * edge * edge;
            float2 offset = normal * bend;
            float2 uv = position - offset;
            float2 chroma = normal * (0.20 * bend + 0.35 * edge) * dispersion;
            half4 green = content.eval(uv);
            half red = content.eval(uv - chroma).r;
            half blue = content.eval(uv + chroma).b;
            return half4(red, green.g, blue, green.a);
        }
    """
}
