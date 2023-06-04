package org.aerial.scan

data class Feature(
    val name: List<String>,
    val loc: Loc?
)

data class Scenario(
    val name: String,
    val loc: Loc?
)
    
//data class Example(
//    var name: String?,
//    val tags: Set<String>,
//    var loc: Loc?
//)

data class Loc(
    val file: String,
    val line: Int
)

//data class Content(
//    val domains: MutableSet<Domain>,
//    val features: MutableSet<Feature>,
//    val examples: MutableSet<Example>,
//    val variables: MutableSet<Variable>,
//    val crosscuts: MutableSet<Crosscut>,
//    val journeys: MutableSet<Journey>,
//    val errors: MutableList<String>
//) {
//    fun add(obj: Any?) {
//        when (obj) {
//            null -> {}
//            is Example -> {
//                examples.add(obj)
//            }
//            is Component -> {
//                domains.add(obj)
//            }
//            is Crosscut -> {
//                crosscuts.add(obj)
//            }
//            is Journey -> {
//                journeys.add(obj)
//            }
//            is Variable -> {
//                variables.add(obj)
//            }
//        }
//    }
//
//    fun combine(other: Content) {
//        this.domains.addAll(other.domains)
//        this.examples.addAll(other.examples)
//        this.variables.addAll(other.variables)
//        this.crosscuts.addAll(other.crosscuts)
//        this.journeys.addAll(other.journeys)
//        this.errors.addAll(other.errors)
//    }
//}
