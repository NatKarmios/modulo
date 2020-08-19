@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.karmios.modulo.core.ext.basiccommands

import com.karmios.modulo.api.Modulo
import com.karmios.modulo.api.ModuloModule
import com.karmios.modulo.api.persist.ModuleSavedData
import com.karmios.modulo.api.persist.ModuleSettings
import org.pf4j.Extension
import org.slf4j.LoggerFactory


@Extension
class BasicCommands : ModuloModule<ModuleSettings, BasicCommandsSavedData>() {
    override val name = "Basic Commands"
    override val description = "Basic commands for core bot functionality"
    override val commands = commandList

    override val defaultSavedData = BasicCommandsSavedData()
    override suspend fun onInit(modulo: Modulo) {
        with(modulo) {
            val helpMd = helpMarkdown
            val helpHash = helpMd.hash
            if (savedData.helpHash == helpHash) {
                log.info("No helpfile changes detected; using existing help URL")
                return
            }
            log.info("Changes in commands detected - generating new help URL")
            savedData.helpUrl = getHelpUrl(helpMd) ?: ""
            savedData.helpHash = helpHash
            savedData.save()
        }
    }

    private val log = LoggerFactory.getLogger(javaClass)
}

class BasicCommandsSavedData(
        var helpUrl: String = "",
        var helpHash : String = ""
): ModuleSavedData()
