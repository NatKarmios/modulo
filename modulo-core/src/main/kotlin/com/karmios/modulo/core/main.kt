package com.karmios.code.modulo.core

import com.karmios.code.modulo.api.ModuloModule
import com.karmios.code.modulo.core.bot.ModuloCore
import org.apache.log4j.Level
import org.apache.log4j.spi.RootLogger
import org.pf4j.*
import kotlin.system.exitProcess


fun main() {
    val pluginManager: PluginManager = object : DefaultPluginManager() {
        override fun createExtensionFinder(): ExtensionFinder? {
            return ScanningExtensionFinder(this)
        }
    }

    pluginManager.loadPlugins()
    pluginManager.startPlugins()

    val modules = pluginManager.getExtensions(ModuloModule::class.java)

    // Initialise the bot
    val core = ModuloCore(modules)

    // Run the bot
    core.run()

    // Disable logging to ensure the restart flag is the last thing to be printed
    RootLogger.getRootLogger().level = Level.OFF

    println()
    println(if (core.restart) 1 else 0)
    exitProcess(0)
}
