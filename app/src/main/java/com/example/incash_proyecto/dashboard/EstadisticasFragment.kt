package com.example.incash_proyecto.dashboard

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.incash_proyecto.R

/** Pantalla base: aquí irán las gráficas en la entrega final. */
class EstadisticasFragment : Fragment(R.layout.fragment_placeholder) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<TextView>(R.id.tvTitulo).text = "Estadísticas"
        view.findViewById<TextView>(R.id.tvDescripcion).text = "Aquí verás las gráficas de tus finanzas. Próximamente."
    }
}
