package com.example.incash_proyecto

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.incash_proyecto.db.DbConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.DriverManager

class   MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (SessionManager.haySesion(this)) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }
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
        val loginExitoso = withContext(Dispatchers.IO) {
            try {
                val config = DbConfig()
                val connection: Connection = DriverManager.getConnection(
                    config.getJdbcUrl(),
                    config.user,
                    config.pass
                )

                val isValid = connection.isValid(5)
                connection.close()

                isValid

            } catch (e: Throwable) {
                Log.e("DB_ERROR", "Error conectando a BD", e)
                false
            }
        }

        if (loginExitoso) {
            Toast.makeText(this@MainActivity, "¡Bienvenido!", Toast.LENGTH_SHORT).show()

            val intent = Intent(this@MainActivity, DashboardActivity::class.java)

            SessionManager.guardarSesion(this@MainActivity, email)

            startActivity(intent)

            finish()
        } else {
            Toast.makeText(this@MainActivity, "Error: Revisa tus credenciales o conexión", Toast.LENGTH_LONG).show()
        }
    }
}