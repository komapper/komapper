package org.komapper.core.template.sql

import org.komapper.core.template.sql.SqlLocation as Loc

sealed class SqlNode {
    abstract fun toText(): String

    data class Statement(val nodeList: List<SqlNode>) : SqlNode() {
        override fun toText(): String = nodeList.toText()
    }

    data class Set(val location: Loc, val keyword: String, val left: SqlNode, val right: SqlNode) : SqlNode() {
        override fun toText(): String = left.toText() + keyword + right.toText()
    }

    sealed class Clause : SqlNode() {
        abstract val location: Loc
        abstract val keyword: String
        abstract val nodeList: List<SqlNode>
        override fun toText(): String = keyword + nodeList.toText()

        data class Select(
            override val location: Loc,
            override val keyword: String,
            override val nodeList: List<SqlNode>,
        ) : Clause()

        data class From(
            override val location: Loc,
            override val keyword: String,
            override val nodeList: List<SqlNode>,
        ) : Clause()

        data class Where(
            override val location: Loc,
            override val keyword: String,
            override val nodeList: List<SqlNode>,
        ) : Clause()

        data class Having(
            override val location: Loc,
            override val keyword: String,
            override val nodeList: List<SqlNode>,
        ) : Clause()

        data class GroupBy(
            override val location: Loc,
            override val keyword: String,
            override val nodeList: List<SqlNode>,
        ) : Clause()

        data class OrderBy(
            override val location: Loc,
            override val keyword: String,
            override val nodeList: List<SqlNode>,
        ) : Clause()

        data class ForUpdate(
            override val location: Loc,
            override val keyword: String,
            override val nodeList: List<SqlNode>,
        ) : Clause()

        data class Option(
            override val location: Loc,
            override val keyword: String,
            override val nodeList: List<SqlNode>,
        ) : Clause()
    }

    sealed class BiLogicalOp : SqlNode() {
        abstract val location: Loc
        abstract val keyword: String
        abstract val nodeList: List<SqlNode>
        override fun toText(): String = keyword + nodeList.toText()

        data class And(
            override val location: Loc,
            override val keyword: String,
            override val nodeList: List<SqlNode>,
        ) : BiLogicalOp()

        data class Or(
            override val location: Loc,
            override val keyword: String,
            override val nodeList: List<SqlNode>,
        ) : BiLogicalOp()
    }

    data class Paren(val node: SqlNode) : SqlNode() {
        override fun toText(): String = "(${node.toText()})"
    }

    data class IfBlock(
        val ifDirective: IfDirective,
        val elseifDirectives: List<ElseifDirective>,
        val elseDirective: ElseDirective?,
        val endDirective: EndDirective,
    ) : SqlNode() {
        override fun toText(): String =
            ifDirective.toText() + elseifDirectives.toText() + (elseDirective?.toText() ?: "") + endDirective.toText()
    }

    data class ForBlock(
        val forDirective: ForDirective,
        val endDirective: EndDirective,
    ) : SqlNode() {
        override fun toText(): String = forDirective.toText() + endDirective.toText()
    }

    data class WithBlock(
        val withDirective: WithDirective,
        val endDirective: EndDirective,
    ) : SqlNode() {
        override fun toText(): String = withDirective.toText() + endDirective.toText()
    }

    data class IfDirective(
        val location: Loc,
        val token: String,
        val expression: String,
        val nodeList: List<SqlNode>,
    ) : SqlNode() {
        override fun toText(): String = token + nodeList.toText()
    }

    data class ElseifDirective(
        val location: Loc,
        val token: String,
        val expression: String,
        val nodeList: List<SqlNode>,
    ) : SqlNode() {
        override fun toText(): String = token + nodeList.toText()
    }

    data class ElseDirective(val location: Loc, val token: String, val nodeList: List<SqlNode>) : SqlNode() {
        override fun toText(): String = token + nodeList.toText()
    }

    data class EndDirective(val location: Loc, val token: String) : SqlNode() {
        override fun toText(): String = token
    }

    data class ForDirective(
        val location: Loc,
        val token: String,
        val identifier: String,
        val expression: String,
        val nodeList: List<SqlNode>,
    ) : SqlNode() {
        override fun toText(): String = token + nodeList.toText()
    }

    data class WithDirective(
        val location: Loc,
        val token: String,
        val expression: String,
        val nodeList: List<SqlNode>,
    ) : SqlNode() {
        override fun toText(): String = token + nodeList.toText()
    }

    data class BindValueDirective(
        val location: Loc,
        val token: String,
        val expression: String,
        val node: SqlNode,
        val nodeList: List<SqlNode>,
    ) : SqlNode() {
        override fun toText(): String = token + node.toText() + nodeList.toText()
    }

    data class EmbeddedValueDirective(val location: Loc, val token: String, val expression: String) : SqlNode() {
        override fun toText(): String = token
    }

    data class PartialDirective(val location: Loc, val token: String, val expression: String) : SqlNode() {
        override fun toText(): String = token
    }

    data class LiteralValueDirective(
        val location: Loc,
        val token: String,
        val expression: String,
        val node: SqlNode,
        val nodeList: List<SqlNode>,
    ) : SqlNode() {
        override fun toText(): String = token + node.toText() + nodeList.toText()
    }

    sealed interface Blank {
        val token: String
    }

    sealed class Token : SqlNode() {
        abstract val token: String
        override fun toText(): String = token

        data class Eol(override val token: String) : Token(), Blank
        data class Comment(override val token: String) : Token()
        data class Word(override val token: String) : Token()
        data class Space(override val token: String) : Token(), Blank {
            companion object {
                private val MAP = listOf(
                    "\u0009",
                    "\u000B",
                    "\u000C",
                    "\u001C",
                    "\u001D",
                    "\u001E",
                    "\u001F",
                    "\u0020",
                ).associateWith(Token::Space)

                fun of(token: String): Space = MAP.getOrElse(token) {
                    Space(
                        token,
                    )
                }
            }
        }

        data class Other(override val token: String) : Token() {
            companion object {
                private val MAP =
                    listOf(",", "=", ">", "<", "-", "+", "*", "/", "(", ")", ";").associateWith(Token::Other)

                fun of(token: String): Other = MAP.getOrElse(token) {
                    Other(
                        token,
                    )
                }
            }
        }
    }
}

private fun List<SqlNode>.toText(): String {
    return this.joinToString(separator = "") { it.toText() }
}
