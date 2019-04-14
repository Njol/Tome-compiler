package ch.njol.tome.ast.toplevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTMember;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeExpression;
import ch.njol.tome.ast.AbstractASTElementWithIR;
import ch.njol.tome.ast.expressions.ASTExpressions.ASTTypeExpressions;
import ch.njol.tome.compiler.Token.WordToken;
import ch.njol.tome.ir.definitions.IRTypeDefinition;
import ch.njol.tome.ir.definitions.IRUnknownTypeDefinition;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.parser.Parser;

// TODO remove this? (can use a static variable instead)
public class ASTTypeAliasDeclaration extends AbstractASTElementWithIR<IRTypeDefinition> implements ASTTypeDeclaration<IRTypeDefinition> {
	
	public final ASTTopLevelElementModifiers modifiers;
	
	public @Nullable WordToken name;
	public List<ASTGenericParameterDeclaration<?>> genericParameters = new ArrayList<>();
	public @Nullable ASTTypeExpression<?> aliasOf;
	
	public ASTTypeAliasDeclaration(final ASTTopLevelElementModifiers modifiers) {
		this.modifiers = modifiers;
	}
	
	@Override
	public @Nullable WordToken nameToken() {
		return name;
	}
	
	@Override
	public List<? extends ASTMember> declaredMembers() {
		return Collections.EMPTY_LIST;
	}
	
	@Override
	public @Nullable IRTypeUse parentTypes() {
		return null;
	}
	
	@Override
	public List<ASTGenericParameterDeclaration<?>> genericParameters() {
		return genericParameters;
	}
	
	@Override
	public String toString() {
		return "alias " + name;
	}
	
	public static ASTTypeAliasDeclaration finishParsing(final Parser p, final ASTTopLevelElementModifiers modifiers) {
		final ASTTypeAliasDeclaration ast = new ASTTypeAliasDeclaration(modifiers);
		p.one("alias");
		p.until(() -> {
			ast.name = p.oneTypeIdentifierToken();
			p.tryGroup('<', () -> {
				final ASTGenericParameterDeclaration<?> genericParameterDeclaration = ASTGenericParameterDeclaration.parse(p);
				if (genericParameterDeclaration != null)
					ast.genericParameters.add(genericParameterDeclaration);
			}, '>');
			p.one('=');
			ast.aliasOf = ASTTypeExpressions.parse(p, false, false);
		}, ';', false);
		return p.done(ast);
	}
	
	@Override
	protected IRTypeDefinition calculateIR() {
		return new IRUnknownTypeDefinition(getIRContext(), "not implemented", this);
//			return aliasOf.interpret(new InterpreterContext(null));
	}
	
}
