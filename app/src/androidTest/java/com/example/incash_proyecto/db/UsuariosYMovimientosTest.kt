package com.example.incash_proyecto.db

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.incash_proyecto.Sesion
import com.example.incash_proyecto.movimientos.Movimiento
import com.example.incash_proyecto.movimientos.MovimientosRepo
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class UsuariosYMovimientosTest {

    private val ctx: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val prefs = ctx.getSharedPreferences("sesion", Context.MODE_PRIVATE)
    private val sufijo = System.nanoTime()
    private val correoA = "a$sufijo@test.com"
    private val correoB = "b$sufijo@test.com"
    private var sesionPrevia: Map<String, *> = emptyMap<String, Any>()

    @Before
    fun guardarSesion() {
        sesionPrevia = prefs.all
    }

    @After
    fun limpiar() {
        // Borra los usuarios de prueba (sus movimientos se van en cascada) y deja la sesión como estaba.
        AppDb.get(ctx).writableDatabase.delete("usuarios", "email in (?, ?)", arrayOf(correoA, correoB))
        prefs.edit().clear().apply {
            sesionPrevia["usuario_id"]?.let { putLong("usuario_id", it as Long) }
            sesionPrevia["nombre"]?.let { putString("nombre", it as String) }
        }.commit()
    }

    @Test
    fun registroLoginYMovimientosPorUsuario() = runBlocking {
        val a = UsuariosRepo.registrar(ctx, "A", correoA, "clave1234")
        val b = UsuariosRepo.registrar(ctx, "B", correoB, "clave1234")
        assertNotNull(a)
        assertNotNull(b)

        // Correo repetido (sin importar mayúsculas) y contraseñas
        assertNull(UsuariosRepo.registrar(ctx, "Otro", correoA.uppercase(), "clave1234"))
        assertNull(UsuariosRepo.login(ctx, correoA, "otraclave"))
        assertEquals(a!!.id, UsuariosRepo.login(ctx, correoA, "clave1234")?.id)

        // A crea un gasto; B no debe verlo
        Sesion.iniciar(ctx, a)
        MovimientosRepo.guardar(ctx, Movimiento(null, "gasto", 1000, "Otros", "Efectivo", "prueba", LocalDate.now()))
        assertEquals(1, MovimientosRepo.listar(ctx, null, null).size)
        assertEquals(0L to 1000L, MovimientosRepo.totalesDesde(ctx, LocalDate.now()))

        Sesion.iniciar(ctx, b!!)
        assertEquals(0, MovimientosRepo.listar(ctx, null, null).size)
        assertEquals(0L to 0L, MovimientosRepo.totalesDesde(ctx, LocalDate.now()))
    }
}
