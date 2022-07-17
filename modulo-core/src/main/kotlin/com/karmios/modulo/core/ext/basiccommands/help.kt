package com.karmios.modulo.core.ext.basiccommands

import com.karmios.modulo.api.Modulo
import com.karmios.modulo.api.ModuloCmd
import com.karmios.modulo.api.ModuloModule
import com.karmios.modulo.core.paste
import com.karmios.modulo.api.persist.ModuleSavedData
import com.karmios.modulo.api.persist.ModuleSettings


private const val BASE_HELP_URL = "https://natkarmios.github.io/MarkdownViewer/?url=https://cors-anywhere.herokuapp.com/"
private const val BASE_HEADER_LEVEL = 1
private const val CMD_HEADER_LEVEL = 2

internal suspend fun getHelpUrl(md: String) = paste(md)?.let { BASE_HELP_URL + it }

internal val Modulo.helpMarkdown: String
    get() {
        val modules = modules.filter { it.commands.isNotEmpty() }
        return StringBuilder()
                .append("# ").appendLine(coreSettings.helpFileTitle).appendLine()
                .appendLine("**Sections:**")
                .let { sb -> modules.map { it.name }.forEach { sb.appendLine("- [${it}](#${it.asHtmlId})") }; sb }
                .appendLine()
                .appendLine("***Notes:***")
                .appendLine("- *Command usage examples are in the form `${coreSettings.commandPrefix}command [required argument] <optional argument>`*")
                .appendLine("- *Commands marked with \\* are* ***admin-only*** *commands.*")
                .appendLine()
                .let { modules.fold(it) { sb, mod -> mod.buildMarkdown(sb, coreSettings.commandPrefix) } }
                .toString()
    }

fun <S : ModuleSettings, D : ModuleSavedData> ModuloModule<S, D>.buildMarkdown(sb: StringBuilder, cmdPrefix: String): StringBuilder {
    sb.appendLine("<h$BASE_HEADER_LEVEL id=\"${name.asHtmlId}\">${name}</h$BASE_HEADER_LEVEL>").appendLine()
    description?.let { sb.appendLine(it).appendLine() }
    commands.forEach { it.buildMarkdown(sb, cmdPrefix) }
    return sb
}

fun ModuloCmd.buildMarkdown(sb: StringBuilder, cmdPrefix: String): StringBuilder {
    for (i in (1..CMD_HEADER_LEVEL))
        sb.append("#")
    if (adminOnly)
        sb.append(" \\*")
    sb.append(" `$name`")
    sb.appendLine().appendLine()
    description?.let { sb.appendLine(it).appendLine() }
    if (usageExamples.isNotEmpty())
        sb.appendLine("**Usage:**")
    usageExamples.map(String::trim)
            .forEach { sb.appendLine("- `$cmdPrefix$name${if (it == "") "" else " $it"}`") }
    sb.appendLine()
    return sb
}

private val String.asHtmlId
  get() = this.lowercase().replace(" ", "-")
