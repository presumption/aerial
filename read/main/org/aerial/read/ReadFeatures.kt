package org.aerial.read

import com.google.gson.GsonBuilder
import org.aerial.read.ExampleType.*
import picocli.CommandLine
import picocli.CommandLine.*
import java.io.File
import java.util.concurrent.Callable
import kotlin.system.exitProcess

fun main(args: Array<String>): Unit =
    exitProcess(CommandLine(ReadFeatures()).execute(*args))

@Command(
    name = "read",
    mixinStandardHelpOptions = true,
    description = ["Reads features from specified files."]
)
class ReadFeatures : Callable<Int> {
    @Parameters(
        paramLabel = "FILE",
        description = ["Input filepaths"]
    )
    lateinit var filenames: Array<String>

    @Option(names = ["-o", "--output"], description = ["Output directory"])
    var output: String = "build/"

    @Option(
        names = ["-x", "--exclude"],
        description = [
            "Exclude files and folders with names that start with these prefixes.",
            "Good for excluding hidden files and such."]
    )
    var excludePrefixes: List<String> = listOf(".", "__")

    override fun call(): Int {
        println("Excluding files and folders starting with: $excludePrefixes")
        val contents = processFiles(filenames.map { filename -> File(filename) }.toTypedArray())

        val gson = GsonBuilder().setPrettyPrinting().create()

        val componentsFile = "components.json"
        val variablesFile = "variables.json"
        val crosscutsFile = "crosscuts.json"
        val examplesFile = "examples.json"

        if (contents.errors.isNotEmpty()) {
            throw ParsingException(
                "Problems encountered while parsing files!\n" + contents.errors.joinToString("\n")
            )
        }

        println("Output folder: $output")
        println("Writing ${contents.components.size} features to $componentsFile")
        println("Writing ${contents.variables.size} variables to $variablesFile")
        println("Writing ${contents.crosscuts.size} crosscuts to $crosscutsFile")
        println("Writing ${contents.examples.size} examples to $examplesFile")

        File(output).mkdirs()
        File(output).resolve(componentsFile).writeText(gson.toJson(contents.components))
        File(output).resolve(variablesFile).writeText(gson.toJson(contents.variables))
        File(output).resolve(crosscutsFile).writeText(gson.toJson(contents.crosscuts))
        File(output).resolve(examplesFile).writeText(gson.toJson(contents.examples))

        return 0
    }

    private fun processFiles(files: Array<File>): Content {
        val content = Content(
            components = mutableSetOf(),
            variables = mutableSetOf(),
            crosscuts = mutableSetOf(),
            examples = mutableSetOf(),
            errors = mutableListOf()
        )

        for (file in files) {
            if (shouldExclude(file.name)) {
                continue
            }
            if (file.isDirectory) {
                content.combine(processFiles(file.listFiles() ?: emptyArray()))
            } else {
                content.combine(processFile(file))
            }
        }

        return content
    }

    private fun shouldExclude(name: String): Boolean {
        for (prefix in excludePrefixes) {
            if (name.startsWith(prefix)) {
                return true
            }
        }
        return false
    }

    private fun processFile(file: File): Content {
        println("Processing file: ${file.path}")

        val content = Content(
            components = mutableSetOf(),
            variables = mutableSetOf(),
            crosscuts = mutableSetOf(),
            examples = mutableSetOf(),
            errors = mutableListOf()
        )

        val lines = file.readLines()

        var i = 0
        while (i < lines.size) {
            val cur = i
            i++ // may be set to a different value later, if parsed line successfully

            try {
                val result = next(lines, cur)
                i = if (result.skipLines >= 1) (cur + result.skipLines) else (cur + 1)
                setLine(file.absolutePath, result.parsed)
                content.add(result.parsed)
            } catch (e: Throwable) {
                content.errors.add("File $file at line $cur: ${e.message}")
            }
        }
        return content
    }
}

data class LineResult(
    val skipLines: Int,
    val parsed: Any?
)

@Throws(ParsingException::class)
fun next(lines: List<String>, lineIndex: Int): LineResult {
    val line = lines[lineIndex]

    when {
        isExample(line) -> {
            return readExample(lines, lineIndex)
        }
        containsKeyword(KW_CROSS_CUT, line) -> {
            return readCrossCut(lines, lineIndex)
        }
        containsKeyword(KW_COMPONENT, line) -> {
            return readComponent(lines, lineIndex)
        }
        containsKeyword(KW_VARIABLE, line) -> {
            return readVariable(lines, lineIndex)
        }
    }

    return LineResult(1, null)
}

fun isExample(line: String): Boolean {
    return containsKeyword(KW_EXAMPLE, line) ||
            containsKeyword(KW_HOW_TO, line) ||
            containsKeyword(KW_TODO, line)
}

fun readExample(lines: List<String>, line: Int): LineResult {
    // aerial:example Booking flights
    val (useCase, type) = readExampleFeature(lines[line])
    var skip = 1

    // test "I book a flight for myself and my cat."
    val name =
        try {
            extractWithinQuotes(lines[line + skip])
        } catch (e: ParsingException) {
            throw ParsingException("Empty example name: ${e.message}")
        }
    skip += 1

    // variables:
    // * Business class
    // * Vegetarian menu
    val variables = mutableSetOf<String>()
    if (line + skip < lines.size && containsKeyword(KW_EXAMPLE_VARIABLES, lines[line + skip])) {
        skip += 1
        val vars = readList(lines, line + skip)
        variables.addAll(vars)
        skip += vars.size
    }

    // tags: flights, travel, cat-friendly
    val tags = mutableSetOf<String>()
    if (line + skip < lines.size && containsKeyword(KW_TAGS, lines[line + skip])) {
        tags.addAll(readInlineListAfterKeyword(KW_TAGS, lines[line + skip]))
        skip += 1
    }

    return LineResult(
        skip,
        Example(
            name = name, feature = useCase, variables = variables, tags = tags,
            type = type, file = null, line = line
        )
    )
}

fun readCrossCut(lines: List<String>, line: Int): LineResult {
    // aerial:cross-cut Undo history
    val name = readAfterKeyword(KW_CROSS_CUT, lines[line])
    return LineResult(1, Crosscut(name = name, file = null, line = line))
}

fun readExampleFeature(text: String): Pair<String, ExampleType> {
    return when {
        containsKeyword(KW_HOW_TO, text) -> {
            val useCase = extractAfterKeyword(KW_HOW_TO, text)
            useCase to HOW_TO
        }
        containsKeyword(KW_TODO, text) -> {
            val useCase = extractAfterKeyword(KW_TODO, text)
            useCase to TODO
        }
        else -> {
            try {
                val useCase = readAfterKeyword(KW_EXAMPLE, text)
                useCase to EXAMPLE
            } catch (e: ParsingException) {
                throw ParsingException("Failed to parse use case name: ${e.message}")
            }
        }
    }
}

fun readComponent(lines: List<String>, line: Int): LineResult {
    // aerial:feature Booking flights
    val name = readAfterKeyword(KW_COMPONENT, lines[line])
    var skip = 1

    // desc: Want to travel? Book a flight!
    val desc = readAfterKeyword(KW_COMPONENT_DESC, lines[line + skip])
    skip += 1

    // tags: flights, travel, cat-friendly
    val tags = mutableSetOf<String>()
    if (containsKeyword(KW_TAGS, lines[line + skip])) {
        tags.addAll(readInlineListAfterKeyword(KW_TAGS, lines[line + skip]))
        skip += 1
    }

    // use-cases:
    // * Book a new flight
    // * Cancel a booked flight
    ensureKeyword(KW_COMPONENT_FEATURES, lines[line + skip])
    skip += 1
    val useCases = readList(lines, line + skip)
    skip += useCases.size

    return LineResult(
        skipLines = skip,
        parsed =
        Component(
            name = name,
            desc = desc,
            tags = tags.toSet(),
            features = useCases.toSet(),
            file = null,
            line = line
        )
    )
}

fun readVariable(lines: List<String>, line: Int): LineResult {
    // aerial:variable Cat colors
    val name = readAfterKeyword(KW_VARIABLE, lines[line])

    // * Pink
    // * Black-and-white
    // * Glittery
    val values = readList(lines, line + 1)
    return LineResult(
        skipLines = 1 + values.size,
        parsed = Variable(
            name = name,
            values = values.toSet(),
            file = null,
            line = line
        )
    )
}

fun setLine(file: String, obj: Any?) {
    when (obj) {
        null -> {}
        is Example -> {
            obj.file = file
        }
        is Component -> {
            obj.file = file
        }
        is Crosscut -> {
            obj.file = file
        }
        is Variable -> {
            obj.file = file
        }
    }
}
