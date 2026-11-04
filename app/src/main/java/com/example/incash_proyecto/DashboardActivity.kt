package com.example.incash_proyecto

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.example.incash_proyecto.dashboard.DashboardFragment
import com.example.incash_proyecto.dashboard.EstadisticasFragment
import com.example.incash_proyecto.dashboard.PerfilFragment
import com.example.incash_proyecto.movimientos.MovimientoFormActivity
import com.example.incash_proyecto.movimientos.MovimientosActivity

class DashboardActivity : AppCompatActivity() {

    enum class Tab { INICIO, ESTADISTICAS, PERFIL }

    private var tabActual = Tab.INICIO

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val root = findViewById<View>(R.id.root)
        val bottomNav = findViewById<LinearLayout>(R.id.bottomNav)
        WindowCompat.getInsetsController(window, root).isAppearanceLightStatusBars = true
        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            root.updatePadding(top = bars.top)
            bottomNav.updatePadding(bottom = bars.bottom)
            insets
        }

        findViewById<View>(R.id.navInicio).setOnClickListener { seleccionar(Tab.INICIO) }
        findViewById<View>(R.id.navMovimientos).setOnClickListener { irAMovimientos() }
        findViewById<View>(R.id.navEstadisticas).setOnClickListener { seleccionar(Tab.ESTADISTICAS) }
        findViewById<View>(R.id.navPerfil).setOnClickListener { seleccionar(Tab.PERFIL) }
        findViewById<View>(R.id.fabAdd).setOnClickListener {
            startActivity(Intent(this, MovimientoFormActivity::class.java))
        }

        onBackPressedDispatcher.addCallback(this) {
            if (tabActual != Tab.INICIO) seleccionar(Tab.INICIO) else finish()
        }

        if (savedInstanceState == null) {
            seleccionar(Tab.INICIO)
        } else {
            tabActual = Tab.values()[savedInstanceState.getInt(KEY_TAB, 0)]
            actualizarUi()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_TAB, tabActual.ordinal)
    }

    fun seleccionar(tab: Tab) {
        tabActual = tab
        val fragment: Fragment = when (tab) {
            Tab.INICIO -> DashboardFragment()
            Tab.ESTADISTICAS -> EstadisticasFragment()
            Tab.PERFIL -> PerfilFragment()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        actualizarUi()
    }

    fun irAEstadisticas() = seleccionar(Tab.ESTADISTICAS)

    // MovimientosActivity sigue siendo una Activity propia; se puede pasar a Fragment más adelante.
    fun irAMovimientos() = startActivity(Intent(this, MovimientosActivity::class.java))

    private fun actualizarUi() {
        val azul = ContextCompat.getColor(this, R.color.inc_blue)
        val gris = ContextCompat.getColor(this, R.color.inc_gray)
        mapOf(
            Tab.INICIO to R.id.navInicio,
            Tab.ESTADISTICAS to R.id.navEstadisticas,
            Tab.PERFIL to R.id.navPerfil
        ).forEach { (tab, id) ->
            val item = findViewById<LinearLayout>(id)
            (item.getChildAt(1) as TextView).setTextColor(if (tab == tabActual) azul else gris)
        }
        (findViewById<LinearLayout>(R.id.navMovimientos).getChildAt(1) as TextView).setTextColor(gris)
        findViewById<View>(R.id.fabAdd).visibility = if (tabActual == Tab.INICIO) View.VISIBLE else View.GONE
    }

    private companion object {
        const val KEY_TAB = "tab_actual"
    }
}
