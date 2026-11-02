package com.example.incash_proyecto.db


data class DbConfig(
    val host: String = "aws-0-us-east-1.pooler.supabase.com",
    val port: String = "6543",
    val dbName: String = "postgres",
    val user: String = "postgres.uorbbbriztinwmkanlxf",
    val pass: String = "dueSalktIJvJefcp"
) {

    fun getJdbcUrl(): String =
        "jdbc:postgresql://$host:$port/$dbName?sslmode=require&prepareThreshold=0"
}
