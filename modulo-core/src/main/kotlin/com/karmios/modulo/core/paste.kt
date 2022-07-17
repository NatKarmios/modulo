package com.karmios.modulo.core

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.github.kittinunf.fuel.coroutines.awaitString
import com.github.kittinunf.fuel.httpPost
import com.karmios.modulo.core.PasteResponseType.*
import org.slf4j.LoggerFactory
import kotlin.text.StringBuilder

private const val BASE_URL = "https://www.toptal.com/developers/hastebin"
private const val ENDPOINT_URL = "$BASE_URL/documents"


enum class PasteResponseType {
    ID,
    URL,
    RAW_URL
}

suspend fun paste(content: String, responseType: PasteResponseType = RAW_URL): String? {
    val log = LoggerFactory.getLogger("paste.kt")

    val rawJson = try {
        ENDPOINT_URL.httpPost().body(content).awaitString()
    } catch (e: Exception) {
        log.warn("Failed to paste to '$ENDPOINT_URL!'\n${e.stackTrace}")
        return null
    }

    val pasteId = (Parser.default().parse(StringBuilder(rawJson)) as JsonObject).string("key")
    return when (responseType) {
        ID -> pasteId
        URL -> "$BASE_URL/$pasteId"
        RAW_URL -> "$BASE_URL/raw/$pasteId"
    }
}
