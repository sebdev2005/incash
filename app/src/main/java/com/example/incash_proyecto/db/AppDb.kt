package com.example.incash_proyecto.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/** Base de datos local (SQLite) de la app. */
class AppDb private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, "incash.db", null, 1) {

    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """create table usuarios (
                id        integer primary key autoincrement,
                nombre    text not null,
                email     text not null unique,
                pass_hash text not null,
                salt      text not null
            )"""
        )
        // fecha en texto ISO (yyyy-MM-dd): así ordena y compara bien como texto.
        db.execSQL(
            """create table movimientos (
                id          integer primary key autoincrement,
                usuario_id  integer not null references usuarios(id) on delete cascade,
                tipo        text    not null check (tipo in ('ingreso','gasto')),
                monto       integer not null check (monto > 0),
                categoria   text    not null,
                metodo_pago text,
                descripcion text    not null,
                fecha       text    not null
            )"""
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    companion object {
        private var instancia: AppDb? = null

        @Synchronized
        fun get(context: Context): AppDb = instancia ?: AppDb(context).also { instancia = it }
    }
}
