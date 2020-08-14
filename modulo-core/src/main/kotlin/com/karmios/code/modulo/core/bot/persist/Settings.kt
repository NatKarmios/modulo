package com.karmios.code.modulo.core.bot.persist

import com.beust.klaxon.*
import com.karmios.code.modulo.api.Mod
import com.karmios.code.modulo.api.persist.ModuleSettings
import com.karmios.code.modulo.api.ModuloModule
import com.karmios.code.modulo.api.persist.CoreSettings
import com.karmios.code.modulo.core.bot.SETTINGS_FILE
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException
import kotlin.reflect.KClass

private val Any.prettyJsonString: String
    get() {
        val builder = StringBuilder(Klaxon().toJsonString(this))
        return (Parser.default().parse(builder) as JsonBase).toJsonString(true)
    }

private val List<Mod>.defaultSettings: Map<String, ModuleSettings>
    get() = mapOf(
            *this.filter { it.defaultSettings != null }
                    .map { m -> Pair(m.name, m.defaultSettings!!) }
                    .let { listOf(Pair("core", CoreSettings.defaults)) + it }
                    .toTypedArray()
    )


class ModuleSettingsWrapper (
        @TypeFor(field = "settings", adapter = ModuleSettingsTypeAdapter::class)
        val moduleName: String,

        val settings: ModuleSettings
)

class ModuleSettingsTypeAdapter: TypeAdapter<ModuleSettings> {
    override fun classFor(type: Any): KClass<out ModuleSettings> =
            modules.defaultSettings[type]?.let { it::class } ?: throw IllegalArgumentException("Unknown module name $type")

    companion object {
        lateinit var modules: List<Mod>
    }
}


class Settings(
        var modulesSettings: List<ModuleSettingsWrapper> = emptyList()
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun verifyModuleSettings(modules: List<Mod>) {
        modules.defaultSettings.forEach{ (moduleName, defaultSettings) ->
            logger.debug("Verifying $moduleName settings...")
            if (moduleName !in modulesSettings.map { it.moduleName }) {
                logger.debug("$moduleName settings are not present!")
                modulesSettings += ModuleSettingsWrapper(moduleName, defaultSettings)
            } else {
                logger.debug("$moduleName settings are present.")
            }
        }
    }

    fun <T : ModuleSettings> getSettingsByModule(module: ModuloModule<T, *>): T =
            module.defaultSettings?.let { getSettingsByClass(it) } ?:
            throw IllegalArgumentException("Tried to get settings for module '${module.name}', but none exist!")

    @Suppress("UNCHECKED_CAST")
    fun <T: ModuleSettings> getSettingsByClass(instance: T): T? =
            modulesSettings.find { it.settings::class == instance::class }?.let { it.settings as T }
}


fun readSettings(): Settings {
    return try {
        Klaxon().parse<Settings>(File(SETTINGS_FILE))!!
    } catch (ex: Exception) {
        when (ex) {
            is NullPointerException, is FileNotFoundException -> {
                Settings()
            }
            else -> throw ex
        }
    }
}

fun loadSettings(modules: List<Mod>): Settings {
    ModuleSettingsTypeAdapter.modules = modules
    return readSettings().let {
        it.verifyModuleSettings(modules)
        it.write()
        it
    }
}

fun Settings.write() {
    File(SETTINGS_FILE).writeText(this.prettyJsonString)
}
