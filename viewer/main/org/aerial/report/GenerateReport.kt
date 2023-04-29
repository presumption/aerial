package org.aerial.report

import com.google.gson.GsonBuilder
import org.aerial.lib.fromJson
import org.aerial.report.ExampleType.*
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.io.File
import java.util.concurrent.Callable


@Command(
    name = "report",
    mixinStandardHelpOptions = true,
    description = ["Generates a visual feature report."]
)
class GenerateReport : Callable<Int> {
    @Option(
        names = ["--app"],
        description = ["App name. This will be in the title of your report."]
    )
    var app: String = ""

    @Option(names = ["-d", "--dir"], description = ["Input directory. Default build/."])
    var input: String = "build/"

    @Option(
        names = ["-o", "--output"],
        description = ["Output file path (JSON). Default build/report.json"]
    )
    var output: String = "build/report.json"

    override fun call(): Int {
        if (app.isEmpty()) {
            CommandLine(this).usage(System.out)
            return 1
        }

        println("Reading from input folder: $input")
        val inputDir = File(input)
        val componentsFile = inputDir.resolve("components.json")
        val crosscutsFile = inputDir.resolve("crosscuts.json")
        val journeysFile = inputDir.resolve("journeys.json")
        val examplesFile = inputDir.resolve("examples.json")
        val variablesFile = inputDir.resolve("variables.json")

        val gson = GsonBuilder().setPrettyPrinting().create()
        val components = gson.fromJson<List<org.aerial.scan.Component>>(componentsFile.reader())
        val crosscuts = gson.fromJson<List<org.aerial.scan.Crosscut>>(crosscutsFile.reader())
        val journeys = gson.fromJson<List<org.aerial.scan.Journey>>(journeysFile.reader())
        val examples = gson.fromJson<List<org.aerial.scan.Example>>(examplesFile.reader())
        val variables = gson.fromJson<List<org.aerial.scan.Variable>>(variablesFile.reader())

        val report = collate(components, crosscuts, journeys, examples, variables)
        println("Writing report to: $output")
        File(output).writeText(gson.toJson(report))

        return 0
    }

    private fun collate(
        scannedComponents: List<org.aerial.scan.Component>,
        scannedCrosscuts: List<org.aerial.scan.Crosscut>,
        scannedJourneys: List<org.aerial.scan.Journey>,
        scannedExamples: List<org.aerial.scan.Example>,
        scannedVariables: List<org.aerial.scan.Variable>
    ): Report {
        val errors = mutableListOf<String>()

        // TODO check for component name/feature duplicates
        val components = mapComponents(errors = errors, components = scannedComponents)

        // TODO check for variable name/value duplicates and collect errors
        val variables = scannedVariables.map(::mapVariable)

        // TODO check for crosscut duplicates and collect errors
        val crosscuts = scannedCrosscuts.map(::mapCrosscut)

        // TODO check for journey duplicates and collect errors
        val journeys = scannedJourneys.map(::mapJourney)

        // TODO check for variable duplicates
        val examples = mapExamples(
            components = components, variables = variables, journeys = journeys,
            examples = scannedExamples, errors = errors
        )

        if (errors.isNotEmpty()) {
            throw CollatingException(
                "Problems encountered while parsing files!\n" + errors.joinToString("\n")
            )
        }

        return Report(
            app = app,
            components = components,
            journeys = journeys,
            crosscuts = crosscuts,
            examples = examples,
            variables = variables
        )
    }
}

fun mapComponents(
    components: List<org.aerial.scan.Component>, errors: MutableList<String>
): List<Component> {
    val result = mutableListOf<Component>()

    for (component in components) {
        try {
            result.add(mapComponent(component))
        } catch (e: Throwable) {
            errors.add("Cannot process component at ${component.file}:${component.line}: ${e.message}")
        }
    }
    return result
}

fun mapComponent(component: org.aerial.scan.Component): Component {
    val file =
        component.file ?: throw CollatingException("File not found for component ${component.name}")
    return Component(
        component = component.name,
        desc = component.desc,
        tags = component.tags.toList(),
        features = component.features.toList(),
        file = file,
        line = component.line,
    )
}

fun mapExamples(
    components: List<Component>, journeys: List<Journey>,
    variables: List<Variable>,
    examples: List<org.aerial.scan.Example>,
    errors: MutableList<String>
): List<Example> {
    val variableLookup = variablesReverseLookup(variables)
    val componentLookup = componentsReverseLookup(components)
    val journeyLookup = journeys.map { item -> item.name }.toSet()
    val grouped = groupExamplesByName(examples = examples)

    val result = mutableListOf<Example>()
    for (example in grouped) {
        try {
            result.add(
                mapExample(
                    componentLookup = componentLookup,
                    journeys = journeyLookup,
                    variableLookup = variableLookup,
                    name = example.key,
                    examples = example.value
                )
            )
        } catch (e: Throwable) {
            errors.add("${e.message}")
        }
    }
    return result
}

fun mapExample(
    componentLookup: Map<String, String>,
    journeys: Set<String>,
    variableLookup: Map<String, String>,
    name: String,
    examples: List<org.aerial.scan.Example>
): Example {
    val category = findCategory(componentLookup, journeys, name, examples)

    val type = determineType(examples.map { example -> example.type })

    val locations = examples.map { example ->
        val file = example.file
            ?: throw CollatingException("File not found for example ${example.name}")
        Loc(file, example.line)
    }

    val allVariables = examples.map { example -> example.variables }.flatten().toSet()
    val variables = reconstructVariables(variableLookup, allVariables)

    val tags = examples.map { example -> example.tags }.flatten().toSet().toList()

    return Example(
        category = category,
        example = name,
        variables = variables,
        tags = tags,
        type = type,
        locations = locations
    )
}

fun findCategory(
    componentLookup: Map<String, String>, journeys: Set<String>,
    name: String,
    examples: List<org.aerial.scan.Example>
): Category {
    val exampleCategories = examples.map { example -> example.category }.toSet().toList()
    val category = if (exampleCategories.size == 1) exampleCategories[0] else
        throw CollatingException("Multiple categories found for example $name!")

    val component = componentLookup[category]
    if (component != null) {
        return Category.ComponentCategory(component = component, feature = category)
    }

    if (journeys.contains(category)) {
        return Category.JourneyCategory(journey = category)
    }

    throw CollatingException("Category $category for example $name could not be mapped to either feature or journey!")
}

fun determineType(types: List<org.aerial.scan.ExampleType>): ExampleType {
    val mappedTypes =
        types.map { type ->
            when (type) {
                org.aerial.scan.ExampleType.TODO -> TODO
                org.aerial.scan.ExampleType.HOW_TO -> HOW_TO
                org.aerial.scan.ExampleType.EXAMPLE -> EXAMPLE
            }
        }
    return when {
        mappedTypes.contains(TODO) -> TODO
        mappedTypes.contains(HOW_TO) -> HOW_TO
        else -> EXAMPLE
    }
}

fun variablesReverseLookup(variables: List<Variable>): Map<String, String> {
    val map = mutableMapOf<String, String>()
    for (variable in variables) {
        for (value in variable.values) {
            map[value] = variable.name
        }
    }
    return map
}

fun componentsReverseLookup(components: List<Component>): Map<String, String> {
    val map = mutableMapOf<String, String>()
    for (component in components) {
        for (value in component.features) {
            map[value] = component.component
        }
    }
    return map
}

fun groupExamplesByName(examples: List<org.aerial.scan.Example>): Map<String, List<org.aerial.scan.Example>> {
    val result = mutableMapOf<String, MutableList<org.aerial.scan.Example>>()

    for (example in examples) {
        val list = result[example.name] ?: mutableListOf()
        list.add(example)
        result[example.name] = list
    }
    return result
}

fun reconstructVariables(lookup: Map<String, String>, values: Set<String>): Map<String, String> {
    val map = mutableMapOf<String, String>()
    for (value in values) {
        val key: String = lookup[value]
            ?: throw CollatingException("Failed to find a variable corresponding to value $value")
        map[key] = value
    }
    return map
}

fun mapCrosscut(crosscut: org.aerial.scan.Crosscut): Crosscut {
    val file =
        crosscut.file ?: throw CollatingException("File not found for crosscut: ${crosscut.name}")
    return Crosscut(
        name = crosscut.name, file = file, line = crosscut.line
    )
}

fun mapJourney(journey: org.aerial.scan.Journey): Journey {
    val file =
        journey.file ?: throw CollatingException("File not found for journey: ${journey.name}")
    return Journey(
        name = journey.name, desc = journey.desc, file = file, line = journey.line
    )
}

fun mapVariable(variable: org.aerial.scan.Variable): Variable {
    val file =
        variable.file ?: throw CollatingException("File not found for variable: ${variable.name}")
    return Variable(
        name = variable.name, values = variable.values.toList(), file = file, line = variable.line
    )

}

class CollatingException(message: String) : Exception(message) {}

//fun generateReport(
//    features: Set<Feature>, variables: Set<Variable>, useCases: Set<UseCase>, examples: Set<Example>
//): Report {
//    val lookup = mapExamplesToUseCases(examples, useCases, variables)
//    val useCaseData = mutableListOf<UseCaseData>()
//    for ((useCase, exampleSet) in lookup) {
//        val groups = partitionExamplesByVariables(exampleSet)
//        useCaseData.add(
//            UseCaseData(
//                useCase = useCase,
//                groups = groups
//            )
//        )
//    }
//
//    return Report(
//        features = features.toSortedSet(compareBy(Feature::function)),
//        useCases = useCaseData,
//        suggestions = sortedMapOf()
//    )
//
////    val allSuggestions = mutableMapOf<String, List<String>>()
////
////    for (variant in variants.values) {
////        // compare axis values from the test cases with the values from axes definitions
////
////        val suggestions = mutableListOf<String>()
////
////        for ((axisName, axisValues) in variant.axes) {
//////            val definedValues = axisByValue(axisValues.first(), axes).values
////            val testedValues = variant.axes.getOrDefault(axisName, emptySet())
////
//////            for (untestedValue in definedValues.subtract(testedValues)) {
//////                suggestions += "Value [$untestedValue] for axis [$axisName] is not exercised."
//////            }
////
//////            for (rogueValue in testedValues.subtract(definedValues)) {
//////                suggestions += "Value [$rogueValue] for axis [$axisName] is not found in axis definition."
//////            }
////
////            // use defined values as reference
//////            variant.axes[axisName] = definedValues
////        }
////
////        val untestedPairs = pairs(variant.axes.entries.map(axisEntryMapper))
////
////        for (case in variant.cases) {
//////            val testedPairs = pairs1(case.parameters.entries
//////                .map { (key, value) -> axisValueMapper(key, value) }
//////                .toList())
////
//////            for (testedPair in testedPairs) {
//////                untestedPairs -= Pair(testedPair.first, testedPair.second)
//////                untestedPairs -= Pair(testedPair.second, testedPair.first)
//////            }
////        }
////        for (untestedPair in untestedPairs) {
////            suggestions += "Value pair $untestedPair is not exercised."
////        }
////
////        if (suggestions.isNotEmpty()) {
////            allSuggestions[variant.name] = suggestions
////        }
////    }
////
////    return Report(
////        variants = variants.values.sortedBy(Variant::feature),
////        suggestions = allSuggestions
////    )
//}
//
//fun partitionExamplesByVariables(examples: Set<ReportExample>): List<Group> {
//    val lookup = mutableMapOf<Set<String>, MutableSet<ReportExample>>()
//    for (example in examples) {
//        val set = lookup.getOrDefault(example.variables.keys, mutableSetOf())
//        set.add(example)
//        lookup[example.variables.keys] = set
//    }
//
//    val results = mutableListOf<Group>()
//    for ((groupVariables, groupExamples) in lookup) {
//        val group = Group(
//            variables = groupVariables.toSortedSet(),
//            examples = groupExamples
//        )
//        results.add(group)
//    }
//    return results
//}
//
//fun mapExamplesToUseCases(examples: Set<Example>, useCases: Set<UseCase>, variables: Set<Variable>)
//        : Map<UseCase, Set<ReportExample>> {
//    val lookup = groupExamplesByUseCase(examples, variables)
//
//    val result = mutableMapOf<UseCase, Set<ReportExample>>()
//    for (useCase in useCases) {
//        result[useCase] = lookup.remove(useCase.name) ?: mutableSetOf()
//    }
//    if (lookup.isNotEmpty()) {
//        throw ReadException("Failed to map examples to use cases! $lookup")
//    }
//    return result
//}
//
//fun groupExamplesByUseCase(examples: Set<Example>, variables: Set<Variable>)
//        : MutableMap<String, MutableSet<ReportExample>> {
//    val result = mutableMapOf<String, MutableSet<ReportExample>>()
////    for (example in examples) {
////        val set = result.getOrDefault(example.useCase, mutableSetOf())
////        set.add(
////            ReportExample(
////                name = example.name,
////                variables = reconstructVariableKeys(example.variables, variables),
////                file = example.file,
////                line = example.line
////            )
////        )
////        result[example.useCase] = set
////    }
//    return result
//}
//
//fun reconstructVariableKeys(values: Set<String>, variables: Set<Variable>)
//        : Map<String, String> {
//    val result = mutableMapOf<String, String>()
//
//    for (value in values) {
//        for (variable in variables) {
//            if (variable.values.contains(value)) {
//                result[variable.name] = value
//                break
//            }
//        }
//    }
//    if (values.size != result.size) {
//        val valuesMissingKeys = values.subtract(result.values.toSet())
//        throw ProcessingException("Failed to reconstruct keys! Missing keys: $valuesMissingKeys")
//    }
//
//    return result
//}
//
//
//fun buildVariablesLookup(useCases: Map<UseCase, Set<ReportExample>>)
//        : Map<UseCase, SortedSet<String>> {
//    val result = mutableMapOf<UseCase, SortedSet<String>>()
//
//    for ((useCase, examples) in useCases) {
//        result[useCase] = examples.stream()
//            .flatMap { example -> example.variables.keys.stream() }
//            .collect(Collectors.toSet())
//            .toSortedSet()
//    }
//
//    return result
//}
//
//class ProcessingException(message: String?) : Exception(message) {
//}
