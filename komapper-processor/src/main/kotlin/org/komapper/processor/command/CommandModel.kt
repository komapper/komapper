package org.komapper.processor.command

import com.google.devtools.ksp.symbol.KSFile

class CommandModel(
    val command: Command,
) {

    val containingFiles: List<KSFile>
        get() =
            listOf<KSFile?>(command.classDeclaration.containingFile).filterNotNull()

    fun createFileName(): Pair<String, String> {
        return command.packageName to command.fileName
    }
}
