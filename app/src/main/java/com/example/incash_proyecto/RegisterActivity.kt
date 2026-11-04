package com.example.incash_proyecto

import android.os.Bundle
import android.text.Html
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.CheckBox
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

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val cbTerms = findViewById<CheckBox>(R.id.cbTerms)
        val tvTerms = findViewById<TextView>(R.id.tvTerms)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        // Formato para los términos con negrita
        val termsText = "Acepto los <b>Términos y condiciones</b> y la <b>Política de privacidad</b>"
        tvTerms.text = Html.fromHtml(termsText, Html.FROM_HTML_MODE_LEGACY)

        // Tocar sobre el texto de los términos también marca/desmarca el checkbox
        tvTerms.setOnClickListener {
            cbTerms.isChecked = !cbTerms.isChecked
        }

        btnRegister.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (fullName.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa tu nombre completo", Toast.LENGTH_SHORT).show()
                etFullName.requestFocus()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa tu correo electrónico", Toast.LENGTH_SHORT).show()
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

            if (confirmPassword.isEmpty()) {
                Toast.makeText(this, "Por favor, confirma tu contraseña", Toast.LENGTH_SHORT).show()
                etConfirmPassword.requestFocus()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                etConfirmPassword.requestFocus()
                return@setOnClickListener
            }

            if (!cbTerms.isChecked) {
                Toast.makeText(this, "Debes aceptar los Términos y la Política de privacidad", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Creando tu cuenta...", Toast.LENGTH_SHORT).show()
            btnRegister.isEnabled = false

            lifecycleScope.launch {
                val registroExitoso = realizarRegistro(fullName, email, password)
                btnRegister.isEnabled = true

                if (registroExitoso) {
                    Toast.makeText(this@RegisterActivity, "¡Cuenta creada exitosamente!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@RegisterActivity, "Error al registrar la cuenta. Revisa tu conexión.", Toast.LENGTH_LONG).show()
                }
            }
        }

        tvLogin.setOnClickListener {
            finish()
        }
    }

    private suspend fun realizarRegistro(fullName: String, email: String, pass: String): Boolean {
        return withContext(Dispatchers.IO) {
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
                Log.e("DB_ERROR", "Error conectando a BD durante el registro", e)
                false
            }
        }
    }
}
