package com.example.incash_proyecto

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.incash_proyecto.db.UsuariosRepo
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Con sesión guardada se entra directo al Dashboard.
        if (Sesion.usuarioId(this) != -1L) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa tu correo", Toast.LENGTH_SHORT).show()
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Ingresa un correo electrónico válido", Toast.LENGTH_SHORT).show()
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa tu contraseña", Toast.LENGTH_SHORT).show()
                etPassword.requestFocus()
                return@setOnClickListener
            }

            if (password.length < 8) {
                Toast.makeText(this, "La contraseña debe tener al menos 8 caracteres", Toast.LENGTH_SHORT).show()
                etPassword.requestFocus()
                return@setOnClickListener
            }

            Toast.makeText(this, "Verificando credenciales...", Toast.LENGTH_SHORT).show()
            btnLogin.isEnabled = false

            lifecycleScope.launch {
                realizarLogin(email, password)
                btnLogin.isEnabled = true
            }
        }


        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private suspend fun realizarLogin(email: String, pass: String) {
        val usuario = try {
            UsuariosRepo.login(this, email, pass)
        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo iniciar sesión, intenta de nuevo", Toast.LENGTH_LONG).show()
            return
        }

        if (usuario != null) {
            Sesion.iniciar(this, usuario)
            Toast.makeText(this@MainActivity, "¡Bienvenido, ${usuario.nombre}!", Toast.LENGTH_SHORT).show()

            val intent = Intent(this@MainActivity, DashboardActivity::class.java)
            startActivity(intent)

            finish()
        } else {
            Toast.makeText(this@MainActivity, "Correo o contraseña incorrectos", Toast.LENGTH_LONG).show()
        }
    }
}
