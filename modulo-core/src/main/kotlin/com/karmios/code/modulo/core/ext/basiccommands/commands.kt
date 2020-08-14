package com.karmios.code.modulo.core.ext.basiccommands

import com.jessecorbett.diskord.api.model.Message
import com.jessecorbett.diskord.util.sendMessage
import com.karmios.code.modulo.api.Modulo
import com.karmios.code.modulo.api.ModuloCmd

internal val BasicCommands.commandList: List<ModuloCmd>
        get() {
            suspend fun Modulo.help(msg: Message) {
                if (savedData.helpUrl == "") {
                    with(bot) { msg.reply("<@${msg.author.id}>, couldn't upload help file.") }
                } else {
                    with(bot) {
                        msg.reply("<@${msg.author.id}>") {
                            title = coreSettings.helpFileTitle
                            url = savedData.helpUrl
                        }
                    }
                }
            }

            suspend fun Modulo.sonar(msg: Message) {
                msg.guildId?.let {
                    val args = msg.content.split(" ").filter { it.isNotEmpty() }

                    // Match for the channel ID, parsed from a mention
                    Regex("^<#([0-9]+)>$").find(args[1])?.let {
                        val channelId = it.groupValues[1]
                        // Match for the rest of the string, i.e. the message to be sonar'd
                        Regex("<#$channelId>\\s+(.+)$").find(msg.content)?.let {
                            with(bot) { clientStore.channels[channelId].sendMessage(it.groupValues[1]) }
                        }
                    }

                }
            }

            suspend fun Modulo.shutDown(msg: Message) {
                with(bot) {
                    msg.reply("Shutting down.")
                    shutdown()
                }
            }

            suspend fun Modulo.listRoles(msg: Message) {
                msg.guildId?.let { guildId ->
                    bot.clientStore.guilds[guildId]
                }?.let { guild ->
                    guild.getRoles()
                            .sortedBy { -it.position }
                            .dropLast(1)
                            .joinToString("\n") { "`${it.id}` ${it.name}" }
                }?.let { roles ->
                    with(bot) {
                        msg.reply(roles)
                    }
                }
            }

            return listOf(
                    ModuloCmd(
                            "help",
                            { help(it) },
                            description = "Provides a URL to a command help file",
                            usageExamples = listOf("")
                    ),
                    ModuloCmd("sonar",
                            Modulo::sonar,
                            adminOnly = true,
                            description = "Relays a message to the specified channel",
                            usageExamples = listOf(
                                    "[channel] [message]",
                                    "#channel Hello world."
                            )
                    ),
                    ModuloCmd(
                            "shutdown",
                            Modulo::shutDown,
                            adminOnly = true,
                            description = "Shuts down the bot",
                            usageExamples = listOf("")
                    ),
                    ModuloCmd(
                            "listRoles",
                            Modulo::listRoles,
                            adminOnly = true,
                            description = "Lists all of the server's roles and their respective IDs",
                            usageExamples = listOf("")
                    )
            )
        }
