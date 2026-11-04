package com.example.incash_proyecto.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.incash_proyecto.MainActivity
import com.example.incash_proyecto.R
import com.example.incash_proyecto.SessionManager

class PerfilFragment : Fragment(R.layout.fragment_perfil) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<TextView>(R.id.tvEmail).text = SessionManager.email(requireContext()) ?: "—"
        view.findViewById<View>(R.id.btnCerrarSesion).setOnClickListener {
            SessionManager.cerrarSesion(requireContext())
            startActivity(
                Intent(requireContext(), MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        }
    }
}
