package org.aerial.scan

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.InputStream

@OptIn(ExperimentalSerializationApi::class)
fun readConfig(content: InputStream): Result {
    val config: Map<String, String> = Json.decodeFromStream(content)
    return Result(system = config["system"])
}
