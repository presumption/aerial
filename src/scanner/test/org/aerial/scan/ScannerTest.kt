package org.aerial.scan

import picocli.CommandLine
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ScannerTest {
    @Test
    fun `should return error when no paths were provided`() {
        assertFailsWith<IllegalArgumentException> {
            val cmd = Scanner()
            CommandLine(cmd).parseArgs()
            cmd.run()
        }
    }
}
