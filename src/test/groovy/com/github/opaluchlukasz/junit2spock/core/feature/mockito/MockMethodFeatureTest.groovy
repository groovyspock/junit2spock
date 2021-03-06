package com.github.opaluchlukasz.junit2spock.core.feature.mockito

import com.github.opaluchlukasz.junit2spock.core.ASTNodeFactory
import org.eclipse.jdt.core.dom.AST
import org.eclipse.jdt.core.dom.VariableDeclarationStatement
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

class MockMethodFeatureTest extends Specification {

    private static final AST ast = AST.newAST(AST.JLS8)
    @Shared private ASTNodeFactory nodeFactory = new ASTNodeFactory({
        get: ast
    })

    @Subject private MockMethodFeature mockMethodFeature = new MockMethodFeature(nodeFactory)

    def 'should return false for non-mock method declarations'() {
        expect:
        !mockMethodFeature.applicable(node).isPresent()

        where:
        node << [new Object(), nodeFactory.fieldDeclaration(nodeFactory.variableDeclarationFragment('variable'),
                nodeFactory.simpleType('SomeClass'))]
    }

    def 'should return false for mock method declarations'() {
        given:
        def someVar = 'someVar'
        def mockMethod = nodeFactory.methodInvocation('mock', [])
        def type = nodeFactory.simpleType('SomeClass')
        VariableDeclarationStatement statement = nodeFactory.variableDeclarationStatement(someVar, type, mockMethod)

        expect:
        mockMethodFeature.applicable(statement).isPresent()
    }

    def 'should return Spock\'s mock for mockito mock method'() {
        given:
        def someVar = 'someVar'
        def mockMethod = nodeFactory.methodInvocation('mock', [])
        def type = nodeFactory.simpleType('SomeClass')
        VariableDeclarationStatement statement = nodeFactory.variableDeclarationStatement(someVar, type, mockMethod)

        expect:
        mockMethodFeature.apply(statement).toString() == 'SomeClass someVar=Mock(SomeClass.class);\n'
    }
}
