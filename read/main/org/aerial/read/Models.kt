package org.aerial.read

data class Component(
    val name: String,
    val desc: String,
    val tags: Set<String>,
    val features: Set<String>,
    var file: String?,
    val line: Int
)

data class Example(
    val name: String,
    val feature: String,
    val variables: Set<String>,
    val tags: Set<String>,
    val type: ExampleType,
    var file: String?,
    val line: Int
)

enum class ExampleType {
    EXAMPLE, HOW_TO, TODO
}

data class Variable(
    val name: String,
    val values: Set<String>,
    var file: String?,
    val line: Int
)

data class Crosscut(
    val name: String,
    var file: String?,
    val line: Int
)

data class Content(
    val components: MutableSet<Component>,
    val examples: MutableSet<Example>,
    val variables: MutableSet<Variable>,
    val crosscuts: MutableSet<Crosscut>,
    val errors: MutableList<String>
) {
    fun add(obj: Any?) {
        when (obj) {
            null -> {}
            is Example -> {
                examples.add(obj)
            }
            is Component -> {
                components.add(obj)
            }
            is Crosscut -> {
                crosscuts.add(obj)
            }
            is Variable -> {
                variables.add(obj)
            }
        }
    }

    fun combine(other: Content) {
        this.components.addAll(other.components)
        this.examples.addAll(other.examples)
        this.variables.addAll(other.variables)
        this.crosscuts.addAll(other.crosscuts)
        this.errors.addAll(other.errors)
    }
}
