package org.aerial

import org.aerial.scan.ScanFeatures
import org.aerial.report.GenerateReport
import picocli.CommandLine
import picocli.CommandLine.Command
import java.util.concurrent.Callable
import kotlin.system.exitProcess

@Command(
    name = "aerial",
    description = ["Aerial CLI"],
    mixinStandardHelpOptions = true,
    subcommands = [ScanFeatures::class, GenerateReport::class]
)
class Aerial : Callable<Int> {

    override fun call(): Int {
        CommandLine(this).usage(System.out)
        return 1
    }
}

fun main(args: Array<String>): Unit = exitProcess(CommandLine(Aerial()).execute(*args))
