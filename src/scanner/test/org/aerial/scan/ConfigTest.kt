package org.aerial.scan

import picocli.CommandLine
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class ConfigTest {
    @Test
    fun `should write the system description file based on the config file`() {
        val command = Scanner()
        CommandLine(command).parseArgs("examples/fourpaws/aerial.json")
        command.run()

        val result = File("build/system.json").readText().trim()
        val expected = File("examples/fourpaws_scan_results/system.json").readText().trim()

        assertEquals(result, expected)
    }
}
