package com.karmios.modulo.core

import io.github.classgraph.ClassGraph
import org.pf4j.AbstractExtensionFinder
import org.pf4j.Extension
import org.pf4j.PluginManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory

// Adapted from https://github.com/ludup/jadaptive-app-builder
class ScanningExtensionFinder(pluginManager: PluginManager) : AbstractExtensionFinder(pluginManager) {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    override fun readPluginsStorages(): MutableMap<String, MutableSet<String>> {
        log.debug("Reading extensions storages from plugins")
        val result = mutableMapOf<String, MutableSet<String>>()
        val plugins = pluginManager.plugins
        plugins.forEach { plugin ->
            val pluginId = plugin.descriptor.pluginId
            log.debug("Reading extensions storage from plugin '{}'", pluginId)
            val bucket = mutableSetOf<String>()
            if (plugin.plugin != null) {
                scanClasses(bucket)
            }
            result[pluginId] = bucket
        }
        return result
    }

    override fun readClasspathStorages(): MutableMap<String?, MutableSet<String>> {
        log.debug("Reading extensions storages from classpath")
        val result = mutableMapOf<String?, MutableSet<String>>()
        val bucket = mutableSetOf<String>()
        scanClasses(bucket)
        result[null] = bucket;
        return result;
    }

    private fun scanClasses(bucket: MutableSet<String>) {
        val scanResult = ClassGraph()
                .enableAllInfo()
                .addClassLoader(javaClass.classLoader)
                .scan()
        scanResult.use {
            scanResult.getClassesWithAnnotation(Extension::class.java.name).forEach { classInfo ->
                log.info("Found extension {}", classInfo.name)
                bucket.add(classInfo.name)
            }
        }
        debugExtensions(bucket)
    }
}
