package org.aerial.report

import org.aerial.report.ExampleType.EXAMPLE
import org.aerial.report.ExampleType.HOW_TO
import org.aerial.report.ExampleType.TODO
import kotlin.test.Test
import kotlin.test.assertEquals

class GenerateReportTest {
    @Test
    fun `example is mapped`() {
        val componentLookup = mapOf("Feature 1A" to "Component 1")
        val journeys = setOf("Journey", "Other journey")
        val variableLookup = mapOf(
            "Value 1A" to "Variable 1",
            "Value 2A" to "Variable 2",
            "Value 2B" to "Variable 2"
        )
        val example1 = example1(name = "User can log in", category = "Journey")
        val example2 = example2(name = "User can log in", category = "Journey")
        val examples = listOf(example1, example2)

        val result = mapExample(componentLookup, journeys, variableLookup, "User can log in", examples)
        val expected = Example(
            category = Category.JourneyCategory("Journey"),
            example = "User can log in",
            variables = mapOf("Variable 1" to "Value 1A", "Variable 2" to "Value 2B"),
            tags = listOf("tag1a", "tag2b"),
            type = EXAMPLE,
            locations = listOf(Loc("file1", 11), Loc("file2", 22)),
        )
        assertEquals(expected, result)
    }

    @Test
    fun `examples with the same name are grouped`() {
        val example1 = example(name = "User can log in", category = "Login", file = "file1")
        val example2 = example(name = "User can log in", category = "Login", file = "file2")
        val example3 = example(name = "User can sign up", category = "Sign up", file = "file3")
        val example4 = example(name = "User can sign up", category = "Sign up", file = "file4")

        val expected = mapOf(
            "User can log in" to listOf(example1, example2),
            "User can sign up" to listOf(example3, example4),
        )

        val result = groupExamplesByName(listOf(example1, example2, example3, example4))
        assertEquals(expected, result)
    }

    @Test
    fun `variables are arranged for reverse lookup`() {
        val var1 = Variable(
            name = "Variable 1", values = listOf("Value 1A", "Value 1B"), file = "file1", line = 10
        )

        val var2 = Variable(
            name = "Variable 2", values = listOf("Value 2A", "Value 2B"), file = "file2", line = 20
        )

        val expected = mapOf(
            "Value 1A" to "Variable 1",
            "Value 1B" to "Variable 1",
            "Value 2A" to "Variable 2",
            "Value 2B" to "Variable 2"
        )
        assertEquals(expected, variablesReverseLookup(listOf(var1, var2)))
    }

    @Test
    fun `components are arranged for reverse lookup`() {
        val component1 = Component(
            component = "Test component 1",
            desc = "Test component 1",
            tags = listOf("tag1a", "tag1b"),
            features = listOf("Feature 1A", "Feature 1B"),
            file = "file1",
            line = 100,
        )
        val component2 = Component(
            component = "Test component 2",
            desc = "Test component 2",
            tags = listOf("tag2a", "tag2b"),
            features = listOf("Feature 2A", "Feature 2B"),
            file = "file2",
            line = 200,
        )

        val expected = mapOf(
            "Feature 1A" to "Test component 1",
            "Feature 1B" to "Test component 1",
            "Feature 2A" to "Test component 2",
            "Feature 2B" to "Test component 2"
        )
        assertEquals(expected, componentsReverseLookup(listOf(component1, component2)))
    }

    @Test
    fun `TODO type takes precedence`() {
        val result = determineType(
            listOf(
                org.aerial.scan.ExampleType.TODO,
                org.aerial.scan.ExampleType.HOW_TO,
                org.aerial.scan.ExampleType.EXAMPLE
            )
        )
        assertEquals(TODO, result)
    }


    @Test
    fun `HOW-TO type takes precedence over EXAMPLE`() {
        val result = determineType(
            listOf(
                org.aerial.scan.ExampleType.HOW_TO, org.aerial.scan.ExampleType.EXAMPLE
            )
        )
        assertEquals(HOW_TO, result)
    }


    @Test
    fun `EXAMPLE type is selected if no other types are specified`() {
        val result = determineType(
            listOf(
                org.aerial.scan.ExampleType.EXAMPLE, org.aerial.scan.ExampleType.EXAMPLE
            )
        )
        assertEquals(EXAMPLE, result)
    }

    private fun example(
        name: String,
        category: String,
        variables: Set<String> = setOf(""),
        tags: Set<String> = setOf(""),
        type: org.aerial.scan.ExampleType = org.aerial.scan.ExampleType.EXAMPLE,
        file: String = "file",
        line: Int = 100
    ): org.aerial.scan.Example {
        return org.aerial.scan.Example(
            name = name,
            category = category,
            variables = variables,
            tags = tags,
            type = type,
            file = file,
            line = line
        )
    }

    private fun example1(name: String, category: String): org.aerial.scan.Example {
        return org.aerial.scan.Example(
            name = name,
            category = category,
            variables = setOf("Value 1A"),
            tags = setOf("tag1a"),
            type = org.aerial.scan.ExampleType.EXAMPLE,
            file = "file1",
            line = 11
        )
    }

    private fun example2(name: String, category: String): org.aerial.scan.Example {
        return org.aerial.scan.Example(
            name = name,
            category = category,
            variables = setOf("Value 2B"),
            tags = setOf("tag2b"),
            type = org.aerial.scan.ExampleType.EXAMPLE,
            file = "file2",
            line = 22
        )
    }
}
