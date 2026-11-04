package com.example.incash_proyecto.dashboard

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.incash_proyecto.R
import com.example.incash_proyecto.Sesion

class PerfilFragment : Fragment(R.layout.fragment_perfil) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<TextView>(R.id.tvEmail).text = Sesion.nombre(requireContext())
        view.findViewById<View>(R.id.btnCerrarSesion).setOnClickListener { Sesion.cerrar(requireActivity()) }
    }
}
