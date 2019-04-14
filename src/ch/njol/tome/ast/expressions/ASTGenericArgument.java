package ch.njol.tome.ast.expressions;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTLink;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.compiler.Token.WordToken;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.definitions.IRGenericParameter;
import ch.njol.tome.ir.definitions.IRGenericTypeDefinition;
import ch.njol.tome.ir.definitions.IRMemberRedefinition;
import ch.njol.tome.ir.uses.IRMemberUse;
import ch.njol.tome.ir.uses.IRSimpleTypeUse;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.parser.Parser;

/**
 * A generic argument to a {@link ASTTypeWithGenericArguments type use}.
 */
public class ASTGenericArgument extends AbstractASTElement {
	
	public @Nullable ASTGenericArgumentValue value;
	
	private @Nullable ASTGenericArgumentLink parameter;
	
	private static class ASTGenericArgumentLink extends ASTLink<IRMemberRedefinition> {
		
		@Override
		protected @Nullable IRMemberRedefinition tryLink(final String name) {
			final ASTTypeWithGenericArguments type = getParentOfType(ASTTypeWithGenericArguments.class);
			if (type == null)
				return null;
			IRMemberUse memberUse = type.getIR().getMemberByName(name);
			if (memberUse == null)
				return null;
			IRMemberRedefinition redefinition = memberUse.redefinition();
			if (redefinition instanceof IRAttributeRedefinition || redefinition instanceof IRGenericTypeDefinition)
				return redefinition;
			// TODO warn about wrong type?
			return null;
		}
		
		private static ASTGenericArgumentLink parse(final Parser parent) {
			return parseAsAnyIdentifier(new ASTGenericArgumentLink(), parent);
		}
		
	}
	
	@Override
	public String toString() {
		return (parameter != null ? parameter + ": " : "") + value;
	}
	
	public boolean hasParameter() {
		return parameter != null;
	}
	
	public @Nullable IRMemberRedefinition getDefinition(final IRTypeUse baseType, final int index) {
		if (parameter != null)
			return parameter.get();
		if (baseType instanceof IRSimpleTypeUse) {
			final List<? extends IRGenericParameter> genericParameters = ((IRSimpleTypeUse) baseType).getDefinition().genericParameters();
			if (index < genericParameters.size())
				return genericParameters.get(index).definition();
		}
		return null;
	}
	
	public static ASTGenericArgument parse(final Parser parent) {
		return parent.one(p -> {
			final ASTGenericArgument ast = new ASTGenericArgument();
			if (p.peekNext() instanceof WordToken && p.peekNext(':', 1, true)) {
				ast.parameter = ASTGenericArgumentLink.parse(p);
				p.next(); // skip ':'
			}
			if (ast.parameter == null && p.peekNext('{')) {
				ast.value = ASTGenericArgumentPredicateValue.parse(p);
			} else if (p.peekNext('?')) {
				ast.value = ASTGenericArgumentTypeBoundValue.parse(p);
			} else {
				ast.value = ASTGenericArgumentExpressionValue.parse(p);
			}
			return ast;
		});
	}
	
}
