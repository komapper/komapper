package org.komapper.processor.command

import org.komapper.core.template.sql.NoCacheSqlNodeFactory
import org.komapper.core.template.sql.SqlException
import org.komapper.core.template.sql.SqlNode
import org.komapper.processor.Context

internal class SqlAssembler(context: Context, private val command: Command) {

    private val nodeFactory = NoCacheSqlNodeFactory()

    fun assemble(): String {
        val node = nodeFactory.get(command.sql)
        val buf = StringBuilder(command.sql.length + 100)
        visit(buf, node)
        return buf.toString()
    }

    private fun visit(buf: StringBuilder, node: SqlNode): StringBuilder = when (node) {
        is SqlNode.Statement -> {
            node.nodeList.fold(buf, ::visit)
        }

        is SqlNode.Set -> {
            visit(buf, node.left)
                .let {
                    it.append(node.keyword)
                }.let {
                    visit(it, node.right)
                }
        }

        is SqlNode.Clause -> {
            buf.append(node.keyword).let {
                node.nodeList.fold(it, ::visit)
            }
        }

        is SqlNode.BiLogicalOp -> {
            buf.append(node.keyword).let {
                node.nodeList.fold(it, ::visit)
            }
        }

        is SqlNode.Token -> {
            buf.append(node.token)
        }

        is SqlNode.Paren -> {
            buf.append("(").let {
                visit(it, node.node)
            }.let {
                it.append(")")
            }
        }

        is SqlNode.BindValueDirective -> {
            buf.append(node.token).let {
                visit(it, node.node)
            }.let {
                node.nodeList.fold(it, ::visit)
            }
        }

        is SqlNode.EmbeddedValueDirective -> {
            buf.append(node.token)
        }

        is SqlNode.PartialDirective -> {
            val partial = command.sqlPartialMap[node.expression]
                ?: throw SqlException(
                    "The const \"${node.expression}\" is not found " +
                        "in the file \"${command.classDeclaration.containingFile?.fileName}\". " +
                        "The const must be annotated with @KomapperPartial.",
                )
            buf.append(partial)
        }

        is SqlNode.LiteralValueDirective -> {
            buf.append(node.token).let {
                node.nodeList.fold(it, ::visit)
            }
        }

        is SqlNode.IfBlock -> {
            val ifBuf = buf.append(node.ifDirective.token).let {
                node.ifDirective.nodeList.fold(it, ::visit)
            }

            val elseIfBuf = node.elseifDirectives.fold(ifBuf) { s, elseIfDirective ->
                s.append(elseIfDirective.token).let {
                    elseIfDirective.nodeList.fold(it, ::visit)
                }
            }

            val elseBuf = elseIfBuf.append(node.elseDirective?.token ?: "").let {
                node.elseDirective?.nodeList?.fold(it, ::visit)
            }

            (elseBuf ?: elseIfBuf).append(node.endDirective.token)
        }

        is SqlNode.ForBlock -> {
            val forDirective = node.forDirective
            buf.append(forDirective.token).let {
                forDirective.nodeList.fold(it, ::visit)
            }.let {
                it.append(node.endDirective.token)
            }
        }

        is SqlNode.IfDirective,
        is SqlNode.ElseifDirective,
        is SqlNode.ElseDirective,
        is SqlNode.EndDirective,
        is SqlNode.ForDirective,
        -> error("unreachable")
    }
}
