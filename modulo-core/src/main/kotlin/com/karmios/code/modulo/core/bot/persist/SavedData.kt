package com.karmios.code.modulo.core.bot.persist

import com.beust.klaxon.Klaxon
import com.beust.klaxon.TypeAdapter
import com.beust.klaxon.TypeFor
import com.karmios.code.modulo.api.Mod
import com.karmios.code.modulo.api.persist.ModuleSavedData
import com.karmios.code.modulo.api.ModuloModule
import com.karmios.code.modulo.core.bot.SAVED_DATA_FILE
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException
import kotlin.reflect.KClass

private val List<Mod>.defaultSavedData: Map<String, ModuleSavedData>
    get() = mapOf(
            *this.filter { it.defaultSavedData != null }
                    .map { m -> Pair(m.name, m.defaultSavedData!!) }
                    .toTypedArray()
    )


class ModuleSavedDataWrapper (
        @TypeFor(field = "savedData", adapter = ModuleSavedDataTypeAdapter::class)
        val moduleName: String,

        val savedData: ModuleSavedData
)

class ModuleSavedDataTypeAdapter: TypeAdapter<ModuleSavedData> {
    override fun classFor(type: Any): KClass<out ModuleSavedData> =
            modules.defaultSavedData[type]?.let { it::class } ?: throw IllegalArgumentException("Unknown module name $type")

    companion object {
        lateinit var modules: List<Mod>
    }
}


data class SavedData(
        var modulesSavedData: List<ModuleSavedDataWrapper> = emptyList()
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun verifyModuleSavedData(modules: List<Mod>) {
        modules.defaultSavedData.forEach{ (moduleName, defaultSavedData) ->
            logger.debug("Verifying $moduleName SavedData...")
            if (moduleName !in modulesSavedData.map { it.moduleName }) {
                logger.debug("$moduleName SavedData are not present!")
                modulesSavedData += ModuleSavedDataWrapper(moduleName, defaultSavedData)
            } else {
                logger.debug("$moduleName SavedData are present.")
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : ModuleSavedData> getModuleSavedData(module: ModuloModule<*, T>): T =
            module.defaultSavedData?.let { defaultSavedData ->
                modulesSavedData.find { it.savedData::class == defaultSavedData::class }?.let { it.savedData as T }
            } ?: throw IllegalArgumentException("Tried to get saved data for module '${module.name}', but none exists!")
}


fun readSavedData(): SavedData {
    return try {
        Klaxon().parse<SavedData>(File(SAVED_DATA_FILE))!!
    } catch (ex: Exception) {
        when (ex) {
            is NullPointerException, is FileNotFoundException -> {
                SavedData()
            }
            else -> throw ex
        }
    }
}

fun loadSavedData(modules: List<Mod>): SavedData {
    ModuleSavedDataTypeAdapter.modules = modules
    return readSavedData().let {
        it.verifyModuleSavedData(modules)
        it.write()
        it
    }
}

fun SavedData.write() {
    File(SAVED_DATA_FILE).writeText(Klaxon().toJsonString(this))
}
