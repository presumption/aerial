package org.aerial.report

// sync report/main/ui/Report.elm

data class Report(
    val app: String,
    val components: List<Component>,
    val examples: List<Example>,
    val crosscuts: List<Crosscut>,
    val journeys: List<Journey>,
    val variables: List<Variable>
)

data class Component(
    val component: String,
    val desc: String,
    val tags: List<String>,
    val features: List<String>,
    val file: String,
    val line: Int
)

data class Example(
    val category: Category,
    val example: String,
    val variables: Map<String, String>,
    val tags: List<String>,
    val type: ExampleType,
    val locations: List<Loc>
)

sealed class Category {
    data class ComponentCategory(val component: String, val feature: String) : Category()
    data class JourneyCategory(val journey: String) : Category()
}

enum class ExampleType {
    EXAMPLE, HOW_TO, TODO
}

data class Loc(val file: String, val line: Int)

data class Crosscut(
    val name: String,
    val file: String,
    val line: Int
)

data class Journey(
    val name: String,
    val desc: String,
    val file: String,
    val line: Int
)

data class Variable(
    val name: String,
    val values: List<String>,
    val file: String,
    val line: Int
)
