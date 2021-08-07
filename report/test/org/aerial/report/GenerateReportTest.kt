package org.aerial.report

import org.aerial.report.ExampleType.HOW_TO
import kotlin.test.Test
import kotlin.test.assertEquals

class GenerateReportTest {
    private fun components(): List<Component> {
        return listOf(
            Component(
                component = "Auth",
                desc = "Authentication component",
                tags = listOf(),
                features = listOf("Login", "Logout"),
                file = "auth-component-file",
                line = 10,
            )
        )
    }

    private fun variables(): List<Variable> {
        return listOf(
            Variable(
                name = "User type",
                values = listOf("Admin", "Regular"),
                file = "variables-file",
                line = 100,
            )
        )
    }

    @Test
    fun `examples with the same name are merged`() {
        val errors = mutableListOf<String>()

        val exampleName = "User can log in"
        val featureName = "Login"
        val examples = listOf(
            org.aerial.read.Example(
                name = exampleName,
                feature = featureName,
                variables = setOf("Admin"),
                tags = setOf("tag1"),
                type = org.aerial.read.ExampleType.EXAMPLE,
                file = "file1",
                line = 111
            ),
            org.aerial.read.Example(
                name = exampleName,
                feature = featureName,
                variables = setOf("Regular"),
                tags = setOf("tag2"),
                type = org.aerial.read.ExampleType.EXAMPLE,
                file = "file2",
                line = 222
            ),
            org.aerial.read.Example(
                name = exampleName,
                feature = featureName,
                variables = setOf(),
                tags = setOf("tag3"),
                type = org.aerial.read.ExampleType.HOW_TO,
                file = "file3",
                line = 333
            )
        )

        val expected = listOf(
            Example(
                component = "Auth",
                feature = featureName,
                example = exampleName,
                variables = mapOf("User type" to "Admin", "User type" to "Regular"),
                tags = listOf("tag1", "tag2", "tag3"),
                type = HOW_TO,
                locations = listOf(
                    Loc("file1", 111),
                    Loc("file2", 222),
                    Loc("file3", 333)
                ),
            )
        )

        val result = mapExamples(
            components = components(), variables = variables(),
            examples = examples, errors = errors
        )
        assertEquals(expected, result)
        assertEquals(errors.size, 0)
    }

//    @Test
//    fun `reconstruct variable keys`() {
//        val vars = setOf(
//            Variable(name = "Color", values = setOf("Black", "Pink"), file = "", line = 0),
//            Variable(name = "Finish", values = setOf("Sparkly", "Glossy"), file = "", line = 0)
//        )
//        val result = reconstructVariableKeys(setOf("Pink", "Sparkly"), vars)
//
//        val expected = mapOf("Color" to "Pink", "Finish" to "Sparkly")
//        assertEquals(expected, result)
//    }

//    @Test
//    fun `fails to reconstruct variable keys`() {
//        val vars = setOf(
//            Variable(name = "Color", values = setOf("Black", "Pink"), file = "", line = 0),
//            Variable(name = "Finish", values = setOf("Sparkly", "Glossy"), file = "", line = 0)
//        )
//
//        assertThrows<ProcessingException> { reconstructVariableKeys(setOf("Pink", "Sparkly", "Fluffy"), vars) }
//    }
}
