package com.karmios.modulo.api


import com.jessecorbett.diskord.api.common.*
import com.jessecorbett.diskord.api.gateway.events.*
import com.karmios.modulo.api.persist.ModuleSavedData
import com.karmios.modulo.api.persist.ModuleSettings
import org.jetbrains.exposed.sql.Table
import org.pf4j.ExtensionPoint
import java.util.Collections.emptyList


abstract class ModuloModule<SettingsType : ModuleSettings, SavedDataType : ModuleSavedData> : ExtensionPoint {
    abstract val name: String
    open val description: String? = null

    open val commands: List<ModuloCmd> = emptyList()

    open val defaultSettings: SettingsType? = null
    open val defaultSavedData: SavedDataType? = null

    open val dbTables: List<Table> = emptyList()

    open suspend fun onInit(modulo: Modulo) {}
//    open suspend fun onStart(modulo: Modulo) {}
//    open suspend fun onStop(modulo: Modulo) {}

    open val onChannelCreate: Listeners<Channel> = emptyList()
    open val onChannelDelete: Listeners<Channel> = emptyList()
    open val onChannelPinsUpdate: Listeners<ChannelPinUpdate> = emptyList()
    open val onChannelUpdate: Listeners<Channel> = emptyList()
    open val onUserBan: Listeners<GuildBan> = emptyList()
    open val onUserUnban: Listeners<GuildBan> = emptyList()
    open val onGuildCreate: Listeners<CreatedGuild> = emptyList()
    open val onGuildDelete: Listeners<UnavailableGuild> = emptyList()
    open val onEmojiUpdate: Listeners<GuildEmojiUpdate> = emptyList()
    open val onIntegrationUpdate: Listeners<GuildIntegrationUpdate> = emptyList()
    open val onInviteCreate: Listeners<GuildInviteCreate> = emptyList()
    open val onInviteDelete: Listeners<GuildInviteDelete> = emptyList()
    open val onUserJoin: Listeners<GuildMemberAdd> = emptyList()
    open val onUserLeave: Listeners<GuildMemberRemove> = emptyList()
    open val onMemberChunk: Listeners<GuildMembersChunk> = emptyList()
    open val onMemberUpdate: Listeners<GuildMemberUpdate> = emptyList()
    open val onRoleCreate: Listeners<GuildRoleCreate> = emptyList()
    open val onRoleDelete: Listeners<GuildRoleDelete> = emptyList()
    open val onRoleUpdate: Listeners<GuildRoleUpdate> = emptyList()
    open val onStickerUpdate: Listeners<GuildStickersUpdate> = emptyList()
    open val onMessage: Listeners<Message> = emptyList()
    open val onMessageDelete: Listeners<MessageDelete> = emptyList()
    open val onMessageBulkDelete: Listeners<BulkMessageDelete> = emptyList()
    open val onReactAdd: Listeners<MessageReactionAdd> = emptyList()
    open val onReactRemove: Listeners<MessageReactionRemove> = emptyList()
    open val onReactRemoveAll: Listeners<MessageReactionRemoveAll> = emptyList()
    open val onReactRemoveEmoji: Listeners<MessageReactionRemoveEmoji> = emptyList()
    open val onMessageUpdate: Listeners<Message> = emptyList()
    open val onUserPresenceUpdate: Listeners<PresenceUpdate> = emptyList()
    open val onBotStart: Listeners<Ready> = emptyList()
    open val onBotResume: Listeners<Resumed> = emptyList()
    open val onThreadCreate: Listeners<GuildThread> = emptyList()
    open val onThreadDelete: Listeners<ThreadDelete> = emptyList()
    open val onThreadListSync: Listeners<ThreadListSync> = emptyList()
    open val onThreadMembersUpdate: Listeners<ThreadMembersUpdate> = emptyList()
    open val onThreadMemberUpdate: Listeners<ThreadMember> = emptyList()
    open val onThreadUpdate: Listeners<GuildThread> = emptyList()
    open val onUserTyping: Listeners<TypingStart> = emptyList()
    open val onUserUpdate: Listeners<User> = emptyList()
    open val onVoiceServerUpdated: Listeners<VoiceServerUpdate> = emptyList()
    open val onUserVoiceStateChange: Listeners<VoiceState> = emptyList()
    open val onWebhookUpdate: Listeners<WebhookUpdate> = emptyList()
}

typealias Mod = ModuloModule<*, *>
typealias Listener<T> = suspend Modulo.(T) -> Unit
typealias Listeners<T> = List<Listener<T>>
