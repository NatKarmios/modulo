package com.karmios.modulo.core.bot

import com.jessecorbett.diskord.api.model.Message
import com.jessecorbett.diskord.dsl.Bot
import com.jessecorbett.diskord.util.authorId
import com.karmios.modulo.api.Modulo
import com.karmios.modulo.api.ModuloCmd
import kotlinx.coroutines.launch

const val DB_FILE         = "modulo.db"
const val SETTINGS_FILE   = "moduloSettings.json"
const val SAVED_DATA_FILE = "moduloSavedVars.json"
suspend fun ModuloCmd.invoke(msg: Message, modulo: Modulo) {
    with(modulo) {
        if (msg.authorId == botId || adminOnly && !isAdmin(msg))
            return
        scope.launch { action(msg) }
    }
}

typealias Handler<T> = suspend Modulo.(T) -> Unit
typealias EventRegisterFunc<T> = Bot.(suspend (T) -> Unit) -> Unit
