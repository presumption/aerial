package org.aerial.report

// sync report/main/ui/Report.elm

data class Report(
    val components: List<Component>,
    val examples: List<Example>,
    val crosscuts: List<Crosscut>,
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
    val component: String,
    val feature: String,
    val example: String,
    val variables: Map<String, String>,
    val tags: List<String>,
    val type: ExampleType,
    val locations: List<Loc>
)

enum class ExampleType {
    EXAMPLE, HOW_TO
}

data class Loc(val file: String, val line: Int)

data class Crosscut(
    val name: String,
    val file: String,
    val line: Int
)


data class Variable(
    val name: String,
    val values: List<String>,
    val file: String,
    val line: Int
)
