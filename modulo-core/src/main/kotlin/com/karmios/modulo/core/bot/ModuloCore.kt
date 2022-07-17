@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.karmios.modulo.core.bot

import com.jessecorbett.diskord.api.common.Message
import com.jessecorbett.diskord.api.gateway.EventDispatcher
import com.jessecorbett.diskord.bot.BotBase
import com.jessecorbett.diskord.bot.BotContext
import com.jessecorbett.diskord.bot.bot as diskord
import com.jessecorbett.diskord.bot.classicCommands
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
    val logger: Logger = LoggerFactory.getLogger(this::class.java)

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

            diskord(coreSettings.botToken) {
                _botBase = this

                var registered = false;
                registerModule { dispatcher, context ->
                    if (registered) return@registerModule
                    registered = true

                    _bot = context
                    assignListeners(dispatcher)
                    this@ModuloCore.modules.forEach {
                        runBlocking {
                            it.onInit(this@ModuloCore)
                        }
                    }
                }

                classicCommands(coreSettings.commandPrefix) {
                    val commands = this@ModuloCore.modules.flatMap(Mod::commands)
                    commands.forEach { cmd ->
                        command(cmd.name) { msg ->
                            if (msg.author.isBot != true || cmd.allowBots) {
                                cmd.invoke(msg, this@ModuloCore)
                            }
                        }
                    }
                }
            }
        }
    }

    // <editor-fold desc="props">

    override val bot: BotContext
        get() = _bot
    private lateinit var _bot: BotContext

    override val botBase: BotBase
        get() = _botBase
    private lateinit var _botBase: BotBase

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
                    id = this.global().getUser().id
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
        bot.channel(channelId).triggerTypingIndicator()
    }

    @Suppress("DuplicatedCode")
    private fun assignListeners(dispatcher: EventDispatcher<Unit>) {
        infix fun <T> EventRegisterFunc<T>.handledBy(mapper: (Mod) -> List<Handler<T>>) {
            this { arg -> modules.flatMap(mapper).forEach { func -> scope.launch { func(arg) } } }
        }

        with(dispatcher) {
            ::onChannelCreate handledBy Mod::onChannelCreate
            ::onChannelDelete handledBy Mod::onChannelDelete
            ::onChannelPinsUpdate handledBy Mod::onChannelPinsUpdate
            ::onChannelUpdate handledBy Mod::onChannelUpdate
            ::onGuildBanAdd handledBy Mod::onUserBan
            ::onGuildBanRemove handledBy Mod::onUserUnban
            ::onGuildEmojiUpdate handledBy Mod::onEmojiUpdate
            ::onGuildIntegrationsUpdate handledBy Mod::onIntegrationUpdate
            ::onGuildInviteCreate handledBy Mod::onInviteCreate
            ::onGuildInviteDelete handledBy Mod::onInviteDelete
            ::onGuildMemberAdd handledBy Mod::onUserJoin
            ::onGuildMemberRemove handledBy Mod::onUserLeave
            ::onGuildMembersChunk handledBy Mod::onMemberChunk
            ::onGuildMemberUpdate handledBy Mod::onMemberUpdate
            ::onGuildRoleCreate handledBy Mod::onRoleCreate
            ::onGuildRoleDelete handledBy Mod::onRoleDelete
            ::onGuildRoleUpdate handledBy Mod::onRoleUpdate
            ::onGuildStickersUpdate handledBy Mod::onStickerUpdate
            ::onMessageCreate handledBy Mod::onMessage
            ::onMessageDelete handledBy Mod::onMessageDelete
            ::onMessageDeleteBulk handledBy Mod::onMessageBulkDelete
            ::onMessageReactionAdd handledBy Mod::onReactAdd
            ::onMessageReactionRemove handledBy Mod::onReactRemove
            ::onMessageReactionRemoveAll handledBy Mod::onReactRemoveAll
            ::onMessageReactionRemoveEmoji handledBy Mod::onReactRemoveEmoji
            ::onMessageUpdate handledBy Mod::onMessageUpdate
            ::onPresenceUpdate handledBy Mod::onUserPresenceUpdate
            ::onReady handledBy Mod::onBotStart
            ::onResume handledBy Mod::onBotResume
            ::onThreadCreate handledBy Mod::onThreadCreate
            ::onThreadDelete handledBy Mod::onThreadDelete
            ::onThreadListSync handledBy Mod::onThreadListSync
            ::onThreadMemberUpdate handledBy Mod::onThreadMemberUpdate
            ::onThreadMembersUpdate handledBy Mod::onThreadMembersUpdate
            ::onThreadUpdate handledBy Mod::onThreadUpdate
            ::onTypingStart handledBy Mod::onUserTyping
            ::onUserUpdate handledBy Mod::onUserUpdate
            ::onVoiceServerUpdate handledBy Mod::onVoiceServerUpdated
            ::onVoiceStateUpdate handledBy Mod::onUserVoiceStateChange
            ::onWebhookUpdate handledBy Mod::onWebhookUpdate
        }
    }

    // </editor-fold>
}
