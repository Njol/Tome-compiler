package ch.njol.tome.ast.expressions;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTLink;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.compiler.Token.WordToken;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.uses.IRAttributeUse;
import ch.njol.tome.ir.uses.IRSimpleTypeUse;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.parser.Parser;

/**
 * A generic argument to a type use.
 */
public class ASTGenericArgument extends AbstractASTElement {
	
	public boolean wildcard;
	public @Nullable ASTGenericArgumentValue value;
	
	private @Nullable ASTGenericArgumentLink attribute;
	
	private static class ASTGenericArgumentLink extends ASTLink<IRAttributeRedefinition> {
		
		@Override
		protected @Nullable IRAttributeRedefinition tryLink(String name) {
			ASTTypeWithGenericArguments type = getParentOfType(ASTTypeWithGenericArguments.class);
			if (type == null)
				return null;
			final IRAttributeUse attributeUse = type.baseType.getIR().getAttributeByName(name);
			if (attributeUse == null)
				return null;
			return attributeUse.redefinition();
		}
		
		private static ASTGenericArgumentLink parse(Parser parent) {
			return parseAsAnyIdentifier(new ASTGenericArgumentLink(), parent);
		}
		
	}
	
	@Override
	public String toString() {
		return (attribute != null ? attribute + ": " : "") + value;//(wildcard ? "?" + (extends_ == null ? "" : " extends " + extends_) + (super_ == null ? "" : " super " + super_) : value);
	}
	
	public @Nullable IRAttributeRedefinition attribute(final IRTypeUse baseType, final int index) {
		if (attribute != null) {
			final IRAttributeRedefinition attributeRedefinition = attribute.get();
			return attributeRedefinition == null ? null : attributeRedefinition;
		}
		if (baseType instanceof IRSimpleTypeUse) {
			final List<IRAttributeRedefinition> positionalGenericParameters = ((IRSimpleTypeUse) baseType).getDefinition().positionalGenericParameters();
			if (index < positionalGenericParameters.size())
				return positionalGenericParameters.get(index);
		}
		return null;
	}
	
	public static ASTGenericArgument parse(final Parser parent) {
		return parent.one(p -> {
			final ASTGenericArgument ast = new ASTGenericArgument();
			if (p.peekNext() instanceof WordToken && p.peekNext(':', 1, true)) {
				ast.attribute = ASTGenericArgumentLink.parse(p);
				p.next(); // skip ':'
			}
			ast.value = ASTGenericArgumentValue.parse(p);
			return ast;
		});
	}
	
}
