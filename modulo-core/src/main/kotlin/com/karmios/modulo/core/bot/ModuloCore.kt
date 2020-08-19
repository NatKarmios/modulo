@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.karmios.modulo.core.bot

import com.jessecorbett.diskord.api.model.*
import com.jessecorbett.diskord.dsl.Bot
import com.jessecorbett.diskord.dsl.bot
import com.jessecorbett.diskord.dsl.command
import com.jessecorbett.diskord.dsl.commands
import com.jessecorbett.diskord.util.authorId
import com.karmios.modulo.api.persist.CoreSettings
import com.karmios.modulo.api.persist.ModuleSavedData
import com.karmios.modulo.api.persist.ModuleSettings
import com.karmios.modulo.core.bot.persist.initDB
import com.karmios.modulo.core.bot.persist.loadSavedData
import com.karmios.modulo.core.bot.persist.loadSettings
import com.karmios.modulo.core.bot.persist.write
import com.karmios.modulo.api.Mod
import com.karmios.modulo.api.Modulo
import com.karmios.modulo.api.ModuloModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess


class ModuloCore(override val modules: List<Mod>): Modulo {
    var restart = false
    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    // <editor-fold desc="persist">

    private val settings = loadSettings(modules)
    private val savedData = loadSavedData(modules)

    override val coreSettings: CoreSettings
        get() = this.settings.getSettingsByClass(CoreSettings())!!

    override val <T : ModuleSettings> ModuloModule<T, *>.settings: T
        get() = this@ModuloCore.settings.getSettingsByModule(this)

    override val <T : ModuleSavedData> ModuloModule<*, T>.savedData: T
        get() = this@ModuloCore.savedData.getModuleSavedData(this)

    override fun ModuleSettings.save() {
        settings.write()
    }

    override fun ModuleSavedData.save() {
        savedData.write()
    }

    // </editor-fold>

    init {
        if (coreSettings.botToken == "") {
            logger.error("Please enter your bot token in '$SETTINGS_FILE'")
            exitProcess(1)
        }
        initDB(modules)
    }

    fun run() {
        runBlocking {
            _scope = this
            val coreSettings = this@ModuloCore.coreSettings

            if (coreSettings.botToken == "") {
                logger.error("Please supply a bot token via the TOKEN environment variable!")
                exitProcess(1)
            }

            bot(coreSettings.botToken) {
                _bot = this

                commands(prefix = coreSettings.commandPrefix) {
                    (modules.flatMap(Mod::commands))
                        .forEach { with(it) {
                            command(name, allowBots) { invoke(this, this@ModuloCore) }
                        } }
                }

                assignListeners()
                runBlocking { modules.forEach { it.onInit(this@ModuloCore) } }
            }
        }
    }

    // <editor-fold desc="props">

    override val bot: Bot
        get() = _bot
    private lateinit var _bot: Bot

    override val scope: CoroutineScope
        get() = _scope
    private lateinit var _scope: CoroutineScope

    // </editor-fold>

    // <editor-fold desc="helpers">

    override val botId: String by lazy {
        var id: String? = null
        runBlocking {
            launch {
                with(bot) {
                    id = clientStore.discord.getUser().id
                }
            }
        }
        id!!
    }

    override fun isAdmin(msg: Message): Boolean = coreSettings.let { coreSettings ->
        coreSettings.adminUsers.contains(msg.authorId) ||
                coreSettings.adminRoleIds.intersect(msg.partialMember?.roleIds ?: emptyList()).isNotEmpty()
    }

    override suspend fun Message.triggerTyping() {
        bot.clientStore.channels[channelId].triggerTypingIndicator()
    }

    @Suppress("DuplicatedCode")
    private fun Bot.assignListeners() {
        infix fun <T> EventRegisterFunc<T>.handledBy(mapper: (Mod) -> List<Handler<T>>) {
            this { modules.flatMap(mapper).forEach { func -> scope.launch { func(it) } } }
        }

        Bot::started handledBy Mod::onBotStart
        Bot::resumed handledBy Mod::onBotResume
        Bot::channelCreated handledBy Mod::onChannelCreate
        Bot::channelUpdated handledBy Mod::onChannelUpdate
        Bot::channelDeleted handledBy Mod::onChannelDelete
        Bot::pinsUpdated handledBy Mod::onChannelPinsUpdate
        Bot::userBanned handledBy Mod::onUserBan
        Bot::userUnbanned handledBy Mod::onUserUnban
        Bot::guildEmojiUpdated handledBy Mod::onEmojiUpdate
        Bot::guildIntegrationsUpdated handledBy Mod::onIntegrationUpdate
        Bot::userJoinedGuild handledBy Mod::onUserJoin
        Bot::guildMemberUpdated handledBy Mod::onMemberUpdate
        Bot::userLeftGuild handledBy Mod::onUserLeave
        Bot::roleCreated handledBy Mod::onRoleCreate
        Bot::roleUpdated handledBy Mod::onRoleUpdate
        Bot::roleDeleted handledBy Mod::onRoleDelete
        Bot::messageCreated handledBy Mod::onMessage
        Bot::messageUpdated handledBy Mod::onMessageUpdate
        Bot::messageDeleted handledBy Mod::onMessageDelete
        Bot::messagesBulkDeleted handledBy Mod::onMessageBulkDelete
        Bot::reactionAdded handledBy Mod::onReactAdd
        Bot::reactionRemoved handledBy Mod::onReactRemove
        Bot::allReactionsRemoved handledBy Mod::onReactRemoveAll
        Bot::userPresenceUpdated handledBy Mod::onUserPresenceUpdate
        Bot::userTyping handledBy Mod::onUserTyping
        Bot::userUpdated handledBy Mod::onUserUpdate
        Bot::userVoiceStateChanged handledBy Mod::onUserVoiceStateChange
        Bot::voiceServerUpdated handledBy Mod::onVoiceServerUpdated
        Bot::webhookUpdated handledBy Mod::onWebhookUpdate
    }

    // </editor-fold>
}
