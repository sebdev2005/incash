package com.example.incash_proyecto.movimientos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.incash_proyecto.R
import com.example.incash_proyecto.Sesion
import android.widget.Button
import android.widget.RadioGroup
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDate

class MovimientosActivity : AppCompatActivity() {

    private val porPagina = 10
    private var limite: Int? = porPagina
    private var carga: Job? = null

    private lateinit var lista: LinearLayout
    private lateinit var filtro: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movimientos)

        lista = findViewById(R.id.listaMovimientos)
        filtro = findViewById(R.id.rgFiltro)

        filtro.setOnCheckedChangeListener { _, _ -> cargar() }
        findViewById<TextView>(R.id.tvVerMas).setOnClickListener {
            limite = null
            cargar()
        }
        findViewById<TextView>(R.id.tvCerrarSesion).setOnClickListener { Sesion.cerrar(this) }
        findViewById<Button>(R.id.btnNuevo).setOnClickListener {
            startActivity(Intent(this, MovimientoFormActivity::class.java))
        }
    }

    // Se recarga al volver del formulario.
    override fun onResume() {
        super.onResume()
        cargar()
    }

    private fun cargar() {
        val tipo = when (filtro.checkedRadioButtonId) {
            R.id.rbIngresos -> "ingreso"
            R.id.rbGastos -> "gasto"
            else -> null
        }
        carga?.cancel() // evita que una respuesta vieja pise el filtro nuevo
        carga = lifecycleScope.launch {
            try {
                val hoy = LocalDate.now()
                val (ingresos, gastos) = MovimientosRepo.totalesDesde(this@MovimientosActivity, hoy.withDayOfMonth(1))
                val movimientos = MovimientosRepo.listar(this@MovimientosActivity, tipo, limite)
                findViewById<TextView>(R.id.tvIngresosMes).text = "Ingresos: +" + formatoCop(ingresos)
                findViewById<TextView>(R.id.tvGastosMes).text = "Gastos: -" + formatoCop(gastos)
                pintar(movimientos, hoy)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Log.e("MOVIMIENTOS", "Error cargando movimientos", e)
                Toast.makeText(this@MovimientosActivity, "No se pudieron cargar los movimientos", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun pintar(movimientos: List<Movimiento>, hoy: LocalDate) {
        lista.removeAllViews()
        var fechaActual: LocalDate? = null
        for (m in movimientos) {
            if (m.fecha != fechaActual) {
                fechaActual = m.fecha
                lista.addView(TextView(this).apply {
                    text = etiquetaFecha(m.fecha, hoy)
                    setTextColor(0xFF5F6368.toInt())
                    textSize = 13f
                    setPadding(0, (16 * resources.displayMetrics.density).toInt(), 0, 0)
                })
            }
            val fila = layoutInflater.inflate(R.layout.item_movimiento, lista, false)
            fila.findViewById<TextView>(R.id.tvDescripcion).text = m.descripcion
            fila.findViewById<TextView>(R.id.tvDetalle).text =
                listOfNotNull(m.categoria, m.metodoPago).joinToString(" · ")
            fila.findViewById<TextView>(R.id.tvMonto).text =
                (if (m.esIngreso) "+" else "-") + formatoCop(m.monto)
            fila.setOnClickListener {
                startActivity(Intent(this, MovimientoFormActivity::class.java).putExtra(MovimientoFormActivity.EXTRA, m))
            }
            lista.addView(fila)
        }
        findViewById<View>(R.id.tvVacio).visibility = if (movimientos.isEmpty()) View.VISIBLE else View.GONE
        findViewById<View>(R.id.tvVerMas).visibility =
            if (limite != null && movimientos.size == limite) View.VISIBLE else View.GONE
    }
}
