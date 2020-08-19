@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.karmios.modulo.core.bot.persist

import com.karmios.modulo.api.Mod
import com.karmios.modulo.api.ModuloModule
import com.karmios.modulo.core.bot.ModuloCore
import com.karmios.modulo.core.bot.DB_FILE
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection


fun ModuloCore.initDB(modules: List<Mod>) {
    connectToDB()

    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
    transaction {
        (modules.flatMap { it.dbTables })
            .forEach { table: Table -> logger.info("Creating table '${table.tableName}'") ; SchemaUtils.create(table) }
    }
}

fun ModuloCore.connectToDB() {
    var connected = false
    val coreSettings = this.coreSettings
    try {
        if (coreSettings.postgresUrl == "") {
            return
        }

        Database.connect(
                "jdbc:postgresql://${coreSettings.postgresUrl}",
                "org.postgresql.Driver",
                coreSettings.postgresUser,
                coreSettings.postgresPass
        )
        connected = true
    } finally {
        if (!connected) {
            logger.info("Defaulting to local SQLite database")
            Database.connect("jdbc:sqlite:$DB_FILE")
        }
    }
}
