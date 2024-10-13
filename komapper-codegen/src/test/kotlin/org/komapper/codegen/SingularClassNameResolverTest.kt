package org.komapper.codegen

import kotlin.test.Test
import kotlin.test.assertEquals

class SingularClassNameResolverTest {
    private val converter = SingularClassNameResolver("", "")

    @Test
    fun resolve() {
        assertEquals("Car", converter.resolve(table("cars")))
        assertEquals("House", converter.resolve(table("houses")))
        assertEquals("Book", converter.resolve(table("books")))
        assertEquals("Bird", converter.resolve(table("birds")))
        assertEquals("Pencil", converter.resolve(table("pencils")))
        assertEquals("Kiss", converter.resolve(table("kisses")))
        assertEquals("Wish", converter.resolve(table("wishes")))
        assertEquals("Match", converter.resolve(table("matches")))
        assertEquals("Fox", converter.resolve(table("foxes")))
        assertEquals("Quiz", converter.resolve(table("quizzes")))
        assertEquals("Boy", converter.resolve(table("boys")))
        assertEquals("Holiday", converter.resolve(table("holidays")))
        assertEquals("Key", converter.resolve(table("keys")))
        assertEquals("Guy", converter.resolve(table("guys")))
        assertEquals("Party", converter.resolve(table("parties")))
        assertEquals("Lady", converter.resolve(table("ladies")))
        assertEquals("Story", converter.resolve(table("stories")))
        assertEquals("Nanny", converter.resolve(table("nannies")))
        assertEquals("City", converter.resolve(table("cities")))
        assertEquals("Life", converter.resolve(table("lives")))
        assertEquals("Leaf", converter.resolve(table("leaves")))
        assertEquals("Thief", converter.resolve(table("thieves")))
        assertEquals("Wife", converter.resolve(table("wives")))
        assertEquals("Tomato", converter.resolve(table("tomatoes")))
        assertEquals("Potato", converter.resolve(table("potatoes")))
        assertEquals("Echo", converter.resolve(table("echoes")))
        assertEquals("Hero", converter.resolve(table("heroes")))
        assertEquals("Man", converter.resolve(table("men")))
        assertEquals("Woman", converter.resolve(table("women")))
        assertEquals("Child", converter.resolve(table("children")))
        assertEquals("Foot", converter.resolve(table("feet")))
        assertEquals("Tooth", converter.resolve(table("teeth")))
        assertEquals("Goose", converter.resolve(table("geese")))
        assertEquals("Mouse", converter.resolve(table("mice")))
        assertEquals("Analysis", converter.resolve(table("analyses")))
        assertEquals("Index", converter.resolve(table("indices")))
        assertEquals("Cactus", converter.resolve(table("cacti")))

        // uncountable word
        assertEquals("Fish", converter.resolve(table("fish")))
        assertEquals("Sheep", converter.resolve(table("sheep")))
        assertEquals("Deer", converter.resolve(table("deer")))
        assertEquals("Moose", converter.resolve(table("moose")))
        assertEquals("Aircraft", converter.resolve(table("aircraft")))

        // already singular, but ends with `s`
        assertEquals("Alias", converter.resolve(table("alias")))
        assertEquals("Analysis", converter.resolve(table("analysis")))
        assertEquals("Success", converter.resolve(table("success")))
        assertEquals("Status", converter.resolve(table("status")))

        // singularize each words
        assertEquals("EmployeeProject", converter.resolve(table("employees_projects")))
        assertEquals("OrderProduct", converter.resolve(table("orders_products")))
    }

    private fun table(name: String): Table = MutableTable().apply { this.name = name }
}
