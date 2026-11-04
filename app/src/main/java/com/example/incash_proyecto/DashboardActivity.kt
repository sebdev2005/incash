package com.example.incash_proyecto

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.incash_proyecto.movimientos.MovimientosActivity

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        findViewById<TextView>(R.id.tvSaludo).text = "Hola, ${Sesion.nombre(this)}"
        findViewById<Button>(R.id.btnMovimientos).setOnClickListener {
            startActivity(Intent(this, MovimientosActivity::class.java))
        }
        findViewById<TextView>(R.id.tvCerrarSesion).setOnClickListener { Sesion.cerrar(this) }
    }
}
