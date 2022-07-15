package com.karmios.modulo.api

import com.jessecorbett.diskord.api.common.Message
import java.util.Collections.emptyList


data class ModuloCmd(
        val name         : String,
        val action       : suspend Modulo.(Message) -> Unit,
        val adminOnly    : Boolean      = false,
        val allowBots    : Boolean      = false,

        val description  : String?      = null,
        val usageExamples: List<String> = emptyList()
)
