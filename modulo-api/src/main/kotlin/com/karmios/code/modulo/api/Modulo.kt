package com.karmios.code.modulo.api

import com.jessecorbett.diskord.api.model.Message
import com.jessecorbett.diskord.dsl.Bot
import com.karmios.code.modulo.api.persist.CoreSettings
import com.karmios.code.modulo.api.persist.ModuleSavedData
import com.karmios.code.modulo.api.persist.ModuleSettings
import kotlinx.coroutines.CoroutineScope

interface Modulo {
    val modules: List<Mod>
    val bot: Bot
    val botId: String
    val scope: CoroutineScope

    val coreSettings: CoreSettings
    val <T : ModuleSettings> ModuloModule<T, *>.settings: T
    val <T : ModuleSavedData> ModuloModule<*, T>.savedData: T
    fun ModuleSettings.save()
    fun ModuleSavedData.save()

    fun isAdmin(msg: Message): Boolean
    suspend fun Message.triggerTyping()
}
