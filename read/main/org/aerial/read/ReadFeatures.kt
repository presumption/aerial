package org.aerial.read

import com.google.gson.GsonBuilder
import org.aerial.lib.filtermap
import org.aerial.read.ExampleType.*
import picocli.CommandLine
import picocli.CommandLine.*
import java.io.File
import java.util.concurrent.Callable

@Command(
    name = "read",
    mixinStandardHelpOptions = true,
    description = ["Reads features from specified files."]
)
class ReadFeatures : Callable<Int> {
    @Parameters(
        paramLabel = "FILE",
        description = ["Files/folders to scan."]
    )
    var filenames: Array<String> = arrayOf()

    @Option(names = ["-o", "--output"], description = ["Output folder. Default build/."])
    var output: String = "build/"

    @Option(
        names = ["-x", "--add-exclude"],
        description = [
            "Add an exclusion pattern to the list. Files and folders that match any exclusion pattern will not be scanned. Wildcard support: only leading and trailing wildcards are supported."]
    )
    var addExclusions: List<String> = listOf()

    @Option(
        names = ["--do-not-read-gitignore"],
        description = [
            "By default, if there is a .gitignore in the root folder, its contents will be added to the exclusions list. This is usually desired. This option disables this behavior."]
    )
    var doNotReadGitIgnore: Boolean = false

    @Option(
        names = ["--scan-non-tests"],
        description = [
            "By default, only test files will be scanned (files with \"test\" in the name, not case-sensitive). Enable this option to scan both test and non-test files. Note: files with \"aerial\" in the name will always be scanned."]
    )
    var scanNonTests: Boolean = false

    @Option(
        names = ["--scan-extensionless"],
        description = [
            "By default, only files that have an extension will be scanned. Enable this option to scan both files with and without an extension. Note: files with \"aerial\" in the name will always be scanned."]
    )
    var scanExtensionless: Boolean = false

    fun getExclusions(): List<String> {
        return addExclusions + standardExcludeList
    }

    override fun call(): Int {
        if (filenames.isEmpty()) {
            CommandLine(this).usage(System.out)
            return 1
        }

        val exclusions = getExclusions()
        println("Exclusions: $exclusions")
        val contents = scanFiles(
            readGitignore = !doNotReadGitIgnore,
            scanNonTests = scanNonTests,
            scanExtensionless = scanExtensionless,
            exclusions = exclusions,
            files = filenames.map { filename -> File(filename) }.toTypedArray()
        )

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
}

val standardExcludeList = listOf(
    ".*",
    "__*",
    "*.css",
    "*.scss",
    "*.less",
    "*.png",
    "*.jpg",
    "*.jpeg",
    "*.ico",
    "*.svg",
    "*.json",
    "*.yml",
    "*.xml",
    "*.lock",
    "*.jar"
)

private fun scanFiles(
    readGitignore: Boolean,
    scanNonTests: Boolean,
    scanExtensionless: Boolean,
    exclusions: Collection<String>,
    files: Array<File>
): Content {
    val content = Content(
        components = mutableSetOf(),
        variables = mutableSetOf(),
        crosscuts = mutableSetOf(),
        examples = mutableSetOf(),
        errors = mutableListOf()
    )

    for (file in files) {
        if (isExcluded(file.name, exclusions)) {
            continue
        }
        if (file.isDirectory) {
            val childExclusions =
                if (readGitignore) {
                    val additionalExclusions = readGitignore(file.path)
                    if (additionalExclusions.isNotEmpty()) {
                        println("Loaded additional exclusions from ${file.path}/.gitignore: $additionalExclusions")
                    }
                    additionalExclusions.union(exclusions)
                } else {
                    exclusions
                }
            content.combine(
                scanFiles(
                    readGitignore = readGitignore,
                    scanNonTests = scanNonTests,
                    scanExtensionless = scanExtensionless,
                    exclusions = childExclusions,
                    files = file.listFiles() ?: emptyArray()
                )
            )
        } else {
            if (shouldScanFile(file.name, scanNonTests, scanExtensionless)) {
                content.combine(scanFile(file))
            }
        }
    }

    return content
}

private fun scanFile(file: File): Content {
    println("Scanning file: ${file.path}")

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
            setFilename(file.absolutePath, result.parsed)
            content.add(result.parsed)
        } catch (e: Throwable) {
            content.errors.add("File $file at line $cur: ${e.message}")
        }
    }
    return content
}

fun shouldScanFile(name: String, scanNonTests: Boolean, scanExtensionless: Boolean): Boolean {
    if (name.contains("aerial")) {
        return true
    }
    val isScanNonTestsOk = scanNonTests ||
            (name.lowercase().contains("test"))
    val isScanExtensionlessOk = scanExtensionless || hasExtension(name)
    return isScanNonTestsOk && isScanExtensionlessOk
}

fun hasExtension(name: String): Boolean {
    return (name.lastIndexOf(".") in 0 until name.length - 1)
}

private fun readGitignore(folder: String): List<String> {
    val gitignore = File(folder).resolve(".gitignore")
    return if (gitignore.exists()) {
        cleanUpExclusions(gitignore.readLines())
    } else {
        listOf()
    }
}

fun isExcluded(name: String, exclusions: Collection<String>): Boolean {
    for (exclusion in exclusions) {
        // exclusion = *temp*, name = my-temp-file
        if (exclusion.startsWith("*") &&
            exclusion.endsWith("*") &&
            name.contains(exclusion.drop(1).dropLast(1))
        ) {
            return true
        }
        // exclusion = *.log, name = my.log
        if (exclusion.startsWith("*") && name.endsWith(exclusion.drop(1))) {
            return true
        }
        // exclusion = build/*, name = build/my.jar
        if (exclusion.endsWith("*") && name.startsWith(exclusion.dropLast(1))) {
            return true
        }
        if (name == exclusion) {
            return true
        }
    }
    return false
}

fun cleanUpExclusions(lines: List<String>): List<String> {
    return lines.filtermap { line ->
        val exclusion = line
            .replaceAfter('#', "")
            .replace("#", "")
            .trim()
        if (exclusion.endsWith("/*")) {
            exclusion.dropLast(2) + "*"
        } else if (exclusion.endsWith("/")) {
            exclusion.dropLast(1)
        } else {
            exclusion.ifEmpty { null }
        }
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

fun readExample(lines: List<String>, lineIndex: Int): LineResult {
    // aerial:example Booking flights
    val res = readExampleTag(lines[lineIndex])
    var skip = 1

    // test "I book a flight for myself and my cat."
    val name = readExampleName(res.indent, lines[lineIndex + skip])
    skip += 1

    // variables:
    // * Business class
    // * Vegetarian menu
    val variables = mutableSetOf<String>()
    if (lineIndex + skip < lines.size && containsKeyword(
            KW_EXAMPLE_VARIABLES,
            lines[lineIndex + skip]
        )
    ) {
        skip += 1
        val vars = readList(lines, lineIndex + skip)
        variables.addAll(vars)
        skip += vars.size
    }

    // tags: flights, travel, cat-friendly
    val tags = mutableSetOf<String>()
    if (lineIndex + skip < lines.size && containsKeyword(KW_TAGS, lines[lineIndex + skip])) {
        tags.addAll(readInlineListAfterKeyword(KW_TAGS, lines[lineIndex + skip]))
        skip += 1
    }

    return LineResult(
        skip,
        Example(
            name = name, feature = res.feature, variables = variables, tags = tags,
            type = res.type, file = null, line = lineIndex + 1
        )
    )
}

fun readExampleName(indent: String, line: String): String {
    val name = try {
        extractWithinQuotes(line.trim())
    } catch (e: ParsingException) {
        line.removePrefix(indent)
    }
    return name.trim().ifEmpty { throw ParsingException("Empty example name") }
}

fun readCrossCut(lines: List<String>, lineIndex: Int): LineResult {
    // aerial:cross-cut Undo history
    val name = readAfterKeyword(KW_CROSS_CUT, lines[lineIndex])
    return LineResult(1, Crosscut(name = name, file = null, line = lineIndex + 1))
}

data class Res(val feature: String, val type: ExampleType, val indent: String)

fun readExampleTag(text: String): Res {
    // aerial:example Booking flights
    return when {
        containsKeyword(KW_HOW_TO, text) -> {
            val split = splitByKeyword(KW_HOW_TO, text)
            Res(feature = split.second, type = HOW_TO, indent = split.first)
        }
        containsKeyword(KW_TODO, text) -> {
            val split = splitByKeyword(KW_TODO, text)
            Res(feature = split.second, type = TODO, indent = split.first)
        }
        else -> {
            try {
                val split = splitByKeyword(KW_EXAMPLE, text)
                Res(feature = split.second, type = EXAMPLE, indent = split.first)
            } catch (e: ParsingException) {
                throw ParsingException("Failed to parse use case name: ${e.message}")
            }
        }
    }
}

fun readComponent(lines: List<String>, lineIndex: Int): LineResult {
    // aerial:feature Booking flights
    val name = readAfterKeyword(KW_COMPONENT, lines[lineIndex])
    var skip = 1

    // desc: Want to travel? Book a flight!
    val desc = readAfterKeyword(KW_COMPONENT_DESC, lines[lineIndex + skip])
    skip += 1

    // tags: flights, travel, cat-friendly
    val tags = mutableSetOf<String>()
    if (containsKeyword(KW_TAGS, lines[lineIndex + skip])) {
        tags.addAll(readInlineListAfterKeyword(KW_TAGS, lines[lineIndex + skip]))
        skip += 1
    }

    // use-cases:
    // * Book a new flight
    // * Cancel a booked flight
    ensureKeyword(KW_COMPONENT_FEATURES, lines[lineIndex + skip])
    skip += 1
    val useCases = readList(lines, lineIndex + skip)
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
            line = lineIndex + 1
        )
    )
}

fun readVariable(lines: List<String>, lineIndex: Int): LineResult {
    // aerial:variable Cat colors
    val name = readAfterKeyword(KW_VARIABLE, lines[lineIndex])

    // * Pink
    // * Black-and-white
    // * Glittery
    val values = readList(lines, lineIndex + 1)
    return LineResult(
        skipLines = 1 + values.size,
        parsed = Variable(
            name = name,
            values = values.toSet(),
            file = null,
            line = lineIndex + 1
        )
    )
}

fun setFilename(file: String, obj: Any?) {
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
