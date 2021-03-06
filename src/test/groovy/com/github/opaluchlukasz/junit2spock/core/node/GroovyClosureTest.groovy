package com.github.opaluchlukasz.junit2spock.core.node

import com.github.opaluchlukasz.junit2spock.core.ASTNodeFactory
import com.github.opaluchlukasz.junit2spock.core.AstProvider
import com.github.opaluchlukasz.junit2spock.core.util.TestConfig
import org.eclipse.jdt.core.dom.AST
import org.eclipse.jdt.core.dom.Expression
import org.springframework.test.context.ContextConfiguration
import spock.lang.Shared
import spock.lang.Specification

import java.sql.Statement

import static com.github.opaluchlukasz.junit2spock.core.builder.BlockBuilder.aBlock
import static com.github.opaluchlukasz.junit2spock.core.builder.IfStatementBuilder.anIfStatement
import static com.github.opaluchlukasz.junit2spock.core.util.StringUtil.SEPARATOR
import static org.eclipse.jdt.core.dom.AST.JLS8
import static org.eclipse.jdt.core.dom.AST.newAST
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.EQUALS

@ContextConfiguration(classes = TestConfig.class)
class GroovyClosureTest extends Specification {

    private static final AST ast = newAST(JLS8)
    private static final AstProvider AST_PROVIDER = {
        get: ast
    }
    @Shared private ASTNodeFactory nodeFactory = new ASTNodeFactory(AST_PROVIDER)

    def 'should return closure without arguments when none provided'() {
        given:
        List<Statement> statements = [nodeFactory.expressionStatement(nodeFactory.methodInvocation('someMethod', []))]

        expect:
        new GroovyClosure(nodeFactory, AST_PROVIDER, statements, null, []).toString() == "{$SEPARATOR\t\t\tsomeMethod()\n\t\t}"
    }

    def 'should return closure with a cast'() {
        given:
        List<Statement> statements = [nodeFactory.expressionStatement(nodeFactory.methodInvocation('someMethod', []))]

        expect:
        new GroovyClosure(nodeFactory, AST_PROVIDER, statements, nodeFactory.typeLiteral(nodeFactory.simpleType('Boolean')), [])
                .toString() == "{$SEPARATOR\t\t\tsomeMethod()\n\t\t} as Boolean.class"
    }

    def 'should return closure with arguments'() {
        given:
        List<Statement> statements = [nodeFactory.expressionStatement(nodeFactory.methodInvocation('someMethod', []))]
        GroovyClosure closure = new GroovyClosure(nodeFactory, AST_PROVIDER, statements, null, arguments)

        expect:
        closure.toString() == expected

        where:
        arguments                                                                                | expected
        [nodeFactory.singleVariableDeclaration(nodeFactory.simpleType(String.simpleName), 'a')]  | "{ String a ->$SEPARATOR\t\t\tsomeMethod()\n\t\t}"
        [nodeFactory.singleVariableDeclaration(nodeFactory.simpleType(String.simpleName), 'a'),
         nodeFactory.singleVariableDeclaration(nodeFactory.simpleType(Integer.simpleName), 'b')] | "{ String a, Integer b ->$SEPARATOR\t\t\tsomeMethod()\n\t\t}"
    }

    def 'should handle IfStatement within the closure'() {
        List<Statement> statements = [anIfStatement(ast)
                                              .withExpression(nodeFactory.infixExpression(EQUALS, nodeFactory.simpleName('a'), nodeFactory.simpleName('b')))
                                              .withThenStatement(aBlock(ast).withStatement(nodeFactory.returnStatement(nodeFactory.booleanLiteral(false))).build())
                                              .withElseStatement(aBlock(ast).withStatement(nodeFactory.returnStatement(nodeFactory.booleanLiteral(true))).build())
                                              .build()]
        GroovyClosure closure = new GroovyClosure(nodeFactory, AST_PROVIDER, statements, null, [])

        expect:
        closure.toString() == "{$SEPARATOR\t\t\tif (a == b) {$SEPARATOR\t\t\t\treturn false\n\t\t\t} else {$SEPARATOR\t\t\t\treturn true\n\t\t\t}$SEPARATOR\t\t}"
    }

    def 'should return an expression'() {
        given:
        List<Statement> statements = [nodeFactory.expressionStatement(nodeFactory.methodInvocation('someMethod', []))]

        expect:
        new GroovyClosure(nodeFactory, AST_PROVIDER, statements, null, []).asExpression() instanceof Expression
    }

    def 'should return same toString representation for expression and closure'() {
        given:
        List<Statement> statements = [nodeFactory.expressionStatement(nodeFactory.methodInvocation('someMethod', []))]
        GroovyClosure closure = new GroovyClosure(nodeFactory, AST_PROVIDER, statements, null, [])

        expect:
        closure.asExpression().toString().equals(closure.toString())
    }
}