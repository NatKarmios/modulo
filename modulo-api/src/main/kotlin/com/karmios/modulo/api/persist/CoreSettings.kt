package com.karmios.modulo.api.persist

data class CoreSettings(
        val botToken       : String       = "",
        val commandPrefix  : String       = "?",
        val adminUsers     : List<String> = emptyList(),
        val adminRoleIds   : List<String> = emptyList(),
        val helpFileTitle  : String       = "Modulo - Command Reference",
        val postgresUrl    : String       = "",
        val postgresUser   : String       = "",
        val postgresPass   : String       = ""
): ModuleSettings() {
    companion object {
        val defaults = CoreSettings()
    }
}
