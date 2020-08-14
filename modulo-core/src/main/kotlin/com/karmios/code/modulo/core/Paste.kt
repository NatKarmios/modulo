package com.karmios.code.modulo.core

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.karmios.code.modulo.core.PasteResponseType.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlin.text.StringBuilder

private const val BASE_URL = "https://hastebin.com"
private const val ENDPOINT_URL = "$BASE_URL/documents"


enum class PasteResponseType {
    ID,
    URL,
    RAW_URL
}

suspend fun paste(content: String, responseType: PasteResponseType = RAW_URL): String? {
    val channel = Channel<String?>()

    khttp.async.post(
        url = ENDPOINT_URL,
        data = content,
        onResponse = {
            channel.sendBlocking(this.text)
            channel.close()
        },
        onError = {
            channel.sendBlocking(null)
            channel.close()
        }
    )
    val rawJson = channel.receive() ?: return null

    val pasteId = (Parser.default().parse(StringBuilder(rawJson)) as JsonObject).string("key")
    return when (responseType) {
        ID -> pasteId
        URL -> "$BASE_URL/$pasteId"
        RAW_URL -> "$BASE_URL/raw/$pasteId"
    }
}
