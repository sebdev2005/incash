package com.example.incash_proyecto.movimientos

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import androidx.lifecycle.lifecycleScope
import com.example.incash_proyecto.R
import android.widget.RadioGroup
import kotlinx.coroutines.launch
import java.time.LocalDate

class MovimientoFormActivity : AppCompatActivity() {

    companion object {
        const val EXTRA = "movimiento"
    }

    private var fecha: LocalDate = LocalDate.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movimiento_form)

        val existente = IntentCompat.getSerializableExtra(intent, EXTRA, Movimiento::class.java)

        val rgTipo = findViewById<RadioGroup>(R.id.rgTipo)
        val etMonto = findViewById<EditText>(R.id.etMonto)
        val etDescripcion = findViewById<EditText>(R.id.etDescripcion)
        val spCategoria = findViewById<Spinner>(R.id.spCategoria)
        val spMetodo = findViewById<Spinner>(R.id.spMetodo)
        val tvFecha = findViewById<TextView>(R.id.tvFecha)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val btnEliminar = findViewById<Button>(R.id.btnEliminar)

        spCategoria.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, CATEGORIAS)
        spMetodo.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, METODOS_PAGO)

        if (existente != null) {
            findViewById<TextView>(R.id.tvTituloForm).text = "Editar movimiento"
            rgTipo.check(if (existente.esIngreso) R.id.rbIngreso else R.id.rbGasto)
            etMonto.setText(existente.monto.toString())
            etDescripcion.setText(existente.descripcion)
            spCategoria.setSelection(CATEGORIAS.indexOf(existente.categoria).coerceAtLeast(0))
            spMetodo.setSelection(METODOS_PAGO.indexOf(existente.metodoPago).coerceAtLeast(0))
            fecha = existente.fecha
            btnEliminar.visibility = View.VISIBLE
        }

        tvFecha.text = etiquetaFecha(fecha)
        tvFecha.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                fecha = LocalDate.of(y, m + 1, d)
                tvFecha.text = etiquetaFecha(fecha)
            }, fecha.year, fecha.monthValue - 1, fecha.dayOfMonth).show()
        }

        btnGuardar.setOnClickListener {
            val monto = etMonto.text.toString().toLongOrNull()
            val descripcion = etDescripcion.text.toString().trim()
            if (monto == null || monto <= 0) {
                Toast.makeText(this, "Ingresa un monto mayor a 0", Toast.LENGTH_SHORT).show()
                etMonto.requestFocus()
                return@setOnClickListener
            }
            if (descripcion.isEmpty()) {
                Toast.makeText(this, "Ingresa una descripción", Toast.LENGTH_SHORT).show()
                etDescripcion.requestFocus()
                return@setOnClickListener
            }
            val mov = Movimiento(
                id = existente?.id,
                tipo = if (rgTipo.checkedRadioButtonId == R.id.rbIngreso) "ingreso" else "gasto",
                monto = monto,
                categoria = spCategoria.selectedItem as String,
                metodoPago = spMetodo.selectedItem as String,
                descripcion = descripcion,
                fecha = fecha
            )
            ejecutar(btnGuardar, if (existente == null) "Movimiento creado" else "Movimiento actualizado") {
                MovimientosRepo.guardar(this, mov)
            }
        }

        btnEliminar.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Eliminar movimiento")
                .setMessage("¿Seguro que quieres eliminar este movimiento?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar") { _, _ ->
                    ejecutar(btnEliminar, "Movimiento eliminado") { MovimientosRepo.eliminar(this, existente!!.id!!) }
                }
                .show()
        }
    }

    private fun ejecutar(boton: Button, exito: String, accion: suspend () -> Unit) {
        boton.isEnabled = false // evita guardar dos veces
        lifecycleScope.launch {
            try {
                accion()
                Toast.makeText(this@MovimientoFormActivity, exito, Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Log.e("MOVIMIENTOS", "Error guardando", e)
                Toast.makeText(this@MovimientoFormActivity, "No se pudo completar, revisa tu conexión", Toast.LENGTH_LONG).show()
                boton.isEnabled = true
            }
        }
    }
}
