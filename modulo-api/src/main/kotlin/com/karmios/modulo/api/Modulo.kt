package com.karmios.modulo.api

import com.jessecorbett.diskord.api.common.Message
import com.jessecorbett.diskord.bot.BotContext
import com.karmios.modulo.api.persist.CoreSettings
import com.karmios.modulo.api.persist.ModuleSavedData
import com.karmios.modulo.api.persist.ModuleSettings
import kotlinx.coroutines.CoroutineScope

interface Modulo {
    val modules: List<Mod>
    val bot: BotContext
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
