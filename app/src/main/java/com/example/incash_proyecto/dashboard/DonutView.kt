package com.example.incash_proyecto.dashboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.incash_proyecto.R
import kotlin.math.min

/** Gráfico circular simple: disco neutro + anillo con un arco por categoría. */
class DonutView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val discoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.inc_neutral_soft)
    }
    private val arcoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
    }
    private val rect = RectF()
    private var datos: List<Pair<Float, Int>> = emptyList()

    /** Pares (valor, color). Los valores <= 0 se ignoran. */
    fun setDatos(nuevos: List<Pair<Long, Int>>) {
        datos = nuevos.filter { it.first > 0 }.map { it.first.toFloat() to it.second }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val grosor = 12f * resources.displayMetrics.density
        val radio = min(width, height) / 2f
        val cx = width / 2f
        val cy = height / 2f
        canvas.drawCircle(cx, cy, radio, discoPaint)

        val total = datos.sumOf { it.first.toDouble() }.toFloat()
        if (total <= 0f) return

        val r = radio - grosor / 2f
        rect.set(cx - r, cy - r, cx + r, cy + r)
        arcoPaint.strokeWidth = grosor
        val hueco = if (datos.size > 1) 2f else 0f
        var inicio = -90f
        for ((valor, color) in datos) {
            val barrido = valor / total * 360f
            arcoPaint.color = color
            canvas.drawArc(rect, inicio, (barrido - hueco).coerceAtLeast(0.5f), false, arcoPaint)
            inicio += barrido
        }
    }
}
