package org.aerial

import org.aerial.scan.Scanner
import picocli.CommandLine
import picocli.CommandLine.Command
import kotlin.system.exitProcess

@Command(
    name = "aerial",
    description = ["Aerial CLI"],
    mixinStandardHelpOptions = true,
    subcommands = [Scanner::class]
)
class Aerial {
    @CommandLine.Spec
    lateinit var spec: CommandLine.Model.CommandSpec
}

fun main(args: Array<String>): Unit = exitProcess(CommandLine(Aerial()).execute(*args))
