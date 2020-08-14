package com.karmios.code.modulo.api

import com.jessecorbett.diskord.api.model.Message


data class ModuloCmd(
        val name         : String,
        val action       : suspend Modulo.(Message) -> Unit,
        val adminOnly    : Boolean      = false,
        val allowBots    : Boolean      = false,

        val description  : String?      = null,
        val usageExamples: List<String> = emptyList()
)
