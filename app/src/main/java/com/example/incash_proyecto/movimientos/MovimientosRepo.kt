package com.example.incash_proyecto.movimientos

import com.example.incash_proyecto.db.DbConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Types
import java.time.LocalDate

// Fechas como texto ISO: el java.sql.Date de Android no convierte a LocalDate.
// shortcut: sin usuario_id, todos ven todos los movimientos; filtrar por usuario cuando exista el login real (JDBC se salta RLS).
object MovimientosRepo {

    private suspend fun <T> conDb(bloque: (Connection) -> T): T = withContext(Dispatchers.IO) {
        val c = DbConfig()
        DriverManager.getConnection(c.getJdbcUrl(), c.user, c.pass).use(bloque)
    }

    /** tipo = null trae todos; limite = null trae todos. */
    suspend fun listar(tipo: String?, limite: Int?): List<Movimiento> = conDb { db ->
        db.prepareStatement(
            "select id, tipo, monto, categoria, metodo_pago, descripcion, fecha from movimientos " +
                "where (?::text is null or tipo = ?) order by fecha desc, id desc limit ?"
        ).use { st ->
            st.setString(1, tipo)
            st.setString(2, tipo)
            st.setObject(3, limite, Types.INTEGER)
            val rs = st.executeQuery()
            buildList {
                while (rs.next()) add(
                    Movimiento(
                        rs.getLong(1), rs.getString(2), rs.getLong(3), rs.getString(4),
                        rs.getString(5), rs.getString(6), LocalDate.parse(rs.getString(7))
                    )
                )
            }
        }
    }

    /** (ingresos, gastos) desde el día indicado. */
    suspend fun totalesDesde(desde: LocalDate): Pair<Long, Long> = conDb { db ->
        db.prepareStatement(
            "select coalesce(sum(monto) filter (where tipo = 'ingreso'), 0), " +
                "coalesce(sum(monto) filter (where tipo = 'gasto'), 0) from movimientos where fecha >= ?::date"
        ).use { st ->
            st.setString(1, desde.toString())
            val rs = st.executeQuery()
            rs.next()
            rs.getLong(1) to rs.getLong(2)
        }
    }

    /** Inserta si id es null; si no, actualiza. */
    suspend fun guardar(m: Movimiento) = conDb { db ->
        val sql = if (m.id == null)
            "insert into movimientos (tipo, monto, categoria, metodo_pago, descripcion, fecha) values (?, ?, ?, ?, ?, ?::date)"
        else
            "update movimientos set tipo = ?, monto = ?, categoria = ?, metodo_pago = ?, descripcion = ?, fecha = ?::date where id = ?"
        db.prepareStatement(sql).use { st ->
            st.setString(1, m.tipo)
            st.setLong(2, m.monto)
            st.setString(3, m.categoria)
            st.setString(4, m.metodoPago)
            st.setString(5, m.descripcion)
            st.setString(6, m.fecha.toString())
            if (m.id != null) st.setLong(7, m.id)
            st.executeUpdate()
        }
    }

    suspend fun eliminar(id: Long) = conDb { db ->
        db.prepareStatement("delete from movimientos where id = ?").use { st ->
            st.setLong(1, id)
            st.executeUpdate()
        }
    }
}
