package org.aerial.scan

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class ConfigTest {
    @Test
    fun `should read config file`() {
        val result = readConfig(File("examples/fourpaws/aerial.json").inputStream())
        assertEquals(
            Result(
                system = "FourPaws Pet Store"
            ), result
        )
    }
}
