package com.example.incash_proyecto.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.incash_proyecto.DashboardActivity
import com.example.incash_proyecto.R
import com.example.incash_proyecto.movimientos.Movimiento
import com.example.incash_proyecto.movimientos.MovimientosRepo
import com.example.incash_proyecto.movimientos.formatoCop
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private data class Datos(
        val ingresosTotal: Long,
        val gastosTotal: Long,
        val ingresosMes: Long,
        val gastosMes: Long,
        val recientes: List<Movimiento>,
        val gastosDelMes: List<Movimiento>
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<View>(R.id.btnNotificaciones).setOnClickListener { aviso("Notificaciones: próximamente") }
        view.findViewById<View>(R.id.tvMes).setOnClickListener { aviso("Selector de mes: próximamente") }
        view.findViewById<View>(R.id.tvVerEstadisticas).setOnClickListener {
            (activity as? DashboardActivity)?.irAEstadisticas()
        }
        view.findViewById<View>(R.id.tvVerTodos).setOnClickListener {
            (activity as? DashboardActivity)?.irAMovimientos()
        }
    }

    // Recarga al volver de crear/editar un movimiento.
    override fun onResume() {
        super.onResume()
        cargarDatos()
    }

    private fun aviso(texto: String) = Toast.makeText(requireContext(), texto, Toast.LENGTH_SHORT).show()

    private fun cargarDatos() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val inicioMes = LocalDate.now().withDayOfMonth(1)
                val datos = coroutineScope {
                    val total = async { MovimientosRepo.totalesDesde(LocalDate.of(2000, 1, 1)) }
                    val mes = async { MovimientosRepo.totalesDesde(inicioMes) }
                    val recientes = async { MovimientosRepo.listar(null, 4) }
                    val gastos = async { MovimientosRepo.listar("gasto", null) }
                    val t = total.await()
                    val m = mes.await()
                    Datos(
                        t.first, t.second, m.first, m.second,
                        recientes.await(),
                        gastos.await().filter { !it.fecha.isBefore(inicioMes) }
                    )
                }
                pintar(datos)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Log.e("DASHBOARD", "Error cargando datos", e)
                context?.let { Toast.makeText(it, "No se pudieron cargar los datos", Toast.LENGTH_LONG).show() }
            }
        }
    }

    private fun pintar(d: Datos) {
        val v = view ?: return
        val ctx = requireContext()

        // Saldo y totales
        val saldo = d.ingresosTotal - d.gastosTotal
        v.findViewById<TextView>(R.id.tvSaldo).text = if (saldo < 0) "-" + formatoCop(-saldo) else formatoCop(saldo)
        v.findViewById<TextView>(R.id.tvIngresosCard).text = formatoCop(d.ingresosTotal)
        v.findViewById<TextView>(R.id.tvGastosCard).text = formatoCop(d.gastosTotal)

        // Resumen mensual
        v.findViewById<TextView>(R.id.tvIngresosMes).text = "+" + formatoCop(d.ingresosMes)
        v.findViewById<TextView>(R.id.tvGastosMes).text = "-" + formatoCop(d.gastosMes)

        // Gastos por categoría (top 4 del mes)
        val porCategoria = d.gastosDelMes
            .groupBy { it.categoria }
            .map { (categoria, lista) -> categoria to lista.sumOf { it.monto } }
            .sortedByDescending { it.second }
        val top = porCategoria.take(4)
        v.findViewById<TextView>(R.id.tvTotalCategorias).text = formatoCop(top.sumOf { it.second })

        val colores = listOf(R.color.inc_blue, R.color.inc_navy, R.color.inc_gray, R.color.inc_border)
            .map { ContextCompat.getColor(ctx, it) }
        v.findViewById<DonutView>(R.id.donut).setDatos(top.mapIndexed { i, par -> par.second to colores[i] })

        val llCategorias = v.findViewById<LinearLayout>(R.id.llCategorias)
        llCategorias.removeAllViews()
        if (top.isEmpty()) llCategorias.addView(textoVacio("Sin gastos este mes"))
        top.forEachIndexed { i, (categoria, monto) ->
            val fila = LayoutInflater.from(ctx).inflate(R.layout.item_categoria, llCategorias, false)
            fila.findViewById<TextView>(R.id.tvIcono).text = emojiCategoria(categoria)
            fila.findViewById<TextView>(R.id.tvNombre).text = categoria
            fila.findViewById<TextView>(R.id.tvMonto).text = formatoCop(monto)
            separar(fila, i)
            llCategorias.addView(fila)
        }

        // Últimos movimientos
        val llUltimos = v.findViewById<LinearLayout>(R.id.llUltimos)
        llUltimos.removeAllViews()
        if (d.recientes.isEmpty()) llUltimos.addView(textoVacio("Aún no tienes movimientos"))
        d.recientes.forEachIndexed { i, m ->
            val fila = LayoutInflater.from(ctx).inflate(R.layout.item_movimiento_reciente, llUltimos, false)
            fila.findViewById<TextView>(R.id.tvIcono).text = emojiCategoria(m.categoria)
            fila.findViewById<TextView>(R.id.tvNombre).text = m.descripcion.ifBlank { m.categoria }
            fila.findViewById<TextView>(R.id.tvTipo).text = if (m.esIngreso) "Ingreso" else "Gasto"
            fila.findViewById<TextView>(R.id.tvMonto).apply {
                text = (if (m.esIngreso) "+" else "-") + formatoCop(m.monto)
                setTextColor(ContextCompat.getColor(ctx, if (m.esIngreso) R.color.inc_blue else R.color.inc_navy))
            }
            separar(fila, i)
            llUltimos.addView(fila)
        }

        // Presupuesto de alimentación (límite fijo por ahora)
        val gastadoAlimentacion = porCategoria.firstOrNull { it.first == "Alimentación" }?.second ?: 0L
        val restante = PRESUPUESTO_ALIMENTACION - gastadoAlimentacion
        v.findViewById<TextView>(R.id.tvPresupuestoMonto).text =
            formatoCop(gastadoAlimentacion) + " / " + formatoCop(PRESUPUESTO_ALIMENTACION)
        v.findViewById<ProgressBar>(R.id.pbPresupuesto).progress =
            (gastadoAlimentacion * 100 / PRESUPUESTO_ALIMENTACION).coerceIn(0, 100).toInt()
        v.findViewById<TextView>(R.id.tvPresupuestoRestante).text =
            if (restante >= 0) "Te quedan " + formatoCop(restante)
            else "Superaste tu presupuesto por " + formatoCop(-restante)
    }

    private fun textoVacio(texto: String) = TextView(requireContext()).apply {
        setTextAppearance(R.style.Inc_Small)
        text = texto
    }

    private fun separar(fila: View, indice: Int) {
        if (indice == 0) return
        (fila.layoutParams as LinearLayout.LayoutParams).topMargin = (12 * resources.displayMetrics.density).toInt()
    }

    private fun emojiCategoria(categoria: String) = when (categoria) {
        "Alimentación" -> "🍔"
        "Transporte" -> "🚌"
        "Entretenimiento" -> "🎮"
        "Compras" -> "🛒"
        "Salario" -> "💰"
        else -> "📦"
    }

    private companion object {
        const val PRESUPUESTO_ALIMENTACION = 400_000L
    }
}
