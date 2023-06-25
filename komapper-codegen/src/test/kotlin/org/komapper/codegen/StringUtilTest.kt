package org.komapper.codegen

import kotlin.test.Test
import kotlin.test.assertEquals

class StringUtilTest {

    @Test
    fun snakeToUpperCamelCase() {
        assertEquals("", StringUtil.snakeToUpperCamelCase(""))
        assertEquals("AaaBbbCcc", StringUtil.snakeToUpperCamelCase("aaa_bbb_ccc"))
        assertEquals("Abc", StringUtil.snakeToUpperCamelCase("abc"))
        assertEquals("Aa1BbbCcc", StringUtil.snakeToUpperCamelCase("aa1_bbb_ccc"))
    }

    @Test
    fun snakeToLowerCamelCase() {
        assertEquals("", StringUtil.snakeToLowerCamelCase(""))
        assertEquals("aaaBbbCcc", StringUtil.snakeToLowerCamelCase("aaa_bbb_ccc"))
        assertEquals("abc", StringUtil.snakeToLowerCamelCase("abc"))
        assertEquals("aa1BbbCcc", StringUtil.snakeToLowerCamelCase("aa1_bbb_ccc"))
    }

    @Test
    fun snakeToUpperCamelCase_singularize() {
        assertEquals("Car", StringUtil.snakeToUpperCamelCase("cars", true))
        assertEquals("House", StringUtil.snakeToUpperCamelCase("houses", true))
        assertEquals("Book", StringUtil.snakeToUpperCamelCase("books", true))
        assertEquals("Bird", StringUtil.snakeToUpperCamelCase("birds", true))
        assertEquals("Pencil", StringUtil.snakeToUpperCamelCase("pencils", true))
        assertEquals("Kiss", StringUtil.snakeToUpperCamelCase("kisses", true))
        assertEquals("Wish", StringUtil.snakeToUpperCamelCase("wishes", true))
        assertEquals("Match", StringUtil.snakeToUpperCamelCase("matches", true))
        assertEquals("Fox", StringUtil.snakeToUpperCamelCase("foxes", true))
        assertEquals("Quiz", StringUtil.snakeToUpperCamelCase("quizzes", true))
        assertEquals("Boy", StringUtil.snakeToUpperCamelCase("boys", true))
        assertEquals("Holiday", StringUtil.snakeToUpperCamelCase("holidays", true))
        assertEquals("Key", StringUtil.snakeToUpperCamelCase("keys", true))
        assertEquals("Guy", StringUtil.snakeToUpperCamelCase("guys", true))
        assertEquals("Party", StringUtil.snakeToUpperCamelCase("parties", true))
        assertEquals("Lady", StringUtil.snakeToUpperCamelCase("ladies", true))
        assertEquals("Story", StringUtil.snakeToUpperCamelCase("stories", true))
        assertEquals("Nanny", StringUtil.snakeToUpperCamelCase("nannies", true))
        assertEquals("City", StringUtil.snakeToUpperCamelCase("cities", true))
        assertEquals("Life", StringUtil.snakeToUpperCamelCase("lives", true))
        assertEquals("Leaf", StringUtil.snakeToUpperCamelCase("leaves", true))
        assertEquals("Thief", StringUtil.snakeToUpperCamelCase("thieves", true))
        assertEquals("Wife", StringUtil.snakeToUpperCamelCase("wives", true))
        assertEquals("Tomato", StringUtil.snakeToUpperCamelCase("tomatoes", true))
        assertEquals("Potato", StringUtil.snakeToUpperCamelCase("potatoes", true))
        assertEquals("Echo", StringUtil.snakeToUpperCamelCase("echoes", true))
        assertEquals("Hero", StringUtil.snakeToUpperCamelCase("heroes", true))
        assertEquals("Man", StringUtil.snakeToUpperCamelCase("men", true))
        assertEquals("Woman", StringUtil.snakeToUpperCamelCase("women", true))
        assertEquals("Child", StringUtil.snakeToUpperCamelCase("children", true))
        assertEquals("Foot", StringUtil.snakeToUpperCamelCase("feet", true))
        assertEquals("Tooth", StringUtil.snakeToUpperCamelCase("teeth", true))
        assertEquals("Goose", StringUtil.snakeToUpperCamelCase("geese", true))
        assertEquals("Mouse", StringUtil.snakeToUpperCamelCase("mice", true))
        assertEquals("Analysis", StringUtil.snakeToUpperCamelCase("analyses", true))
        assertEquals("Index", StringUtil.snakeToUpperCamelCase("indices", true))
        assertEquals("Cactus", StringUtil.snakeToUpperCamelCase("cacti", true))

        // uncountable word
        assertEquals("Fish", StringUtil.snakeToUpperCamelCase("fish", true))
        assertEquals("Sheep", StringUtil.snakeToUpperCamelCase("sheep", true))
        assertEquals("Deer", StringUtil.snakeToUpperCamelCase("deer", true))
        assertEquals("Moose", StringUtil.snakeToUpperCamelCase("moose", true))
        assertEquals("Aircraft", StringUtil.snakeToUpperCamelCase("aircraft", true))

        // already singular, but ends with `s`
        assertEquals("Alias", StringUtil.snakeToUpperCamelCase("alias", true))
        assertEquals("Analysis", StringUtil.snakeToUpperCamelCase("analysis", true))
        assertEquals("Success", StringUtil.snakeToUpperCamelCase("success", true))
        assertEquals("Status", StringUtil.snakeToUpperCamelCase("status", true))

        // singularize each words
        assertEquals("EmployeeProject", StringUtil.snakeToUpperCamelCase("employees_projects", true))
        assertEquals("OrderProduct", StringUtil.snakeToUpperCamelCase("orders_products", true))
    }
}
