package org.aerial.scan

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class Result(
    var system: String? = null,
    val errors: MutableList<String> = mutableListOf()
)

fun Result.combine(other: Result): Result {
    if (this.system == null) {
        this.system = other.system
    }
    this.errors.addAll(other.errors)
    return this
}

fun toSystemFile(result: Result): String {
    val json = mapOf(
        Pair("system", result.system)
    )
    return Json.encodeToString(json)
}
