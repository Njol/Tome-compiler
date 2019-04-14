package ch.njol.tome.ast.expressions;

import java.util.Collections;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.tome.ast.ASTLink;
import ch.njol.tome.ast.AbstractASTElementWithIR;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.expressions.IRAttributeAccess;
import ch.njol.tome.ir.expressions.IRExpression;
import ch.njol.tome.ir.expressions.IRThis;
import ch.njol.tome.ir.expressions.IRUnknownExpression;
import ch.njol.tome.parser.Parser;

/**
 * A meta access without a target (like '~a'), i.e. targets 'this'. Can thus also not be null-safe ('this' is never null).
 * <p>
 * TODO different symbol? maybe '::' like in Java?
 * <p>
 * TODO how to handle allResults here?
 * <p>
 * TODO how about just removing this altogether? can just use 'this' in front of it - using functions makes code already complicated anyway.
 */
public class ASTUnqualifiedMetaAccess extends AbstractASTElementWithIR<IRExpression> implements ASTExpression<IRExpression> {
	
	public @Nullable ASTUnqualifiedMetaAccessLink attribute;
	
	public static class ASTUnqualifiedMetaAccessLink extends ASTLink<IRAttributeRedefinition> {
		
		@Override
		protected @Nullable IRAttributeRedefinition tryLink(final String name) {
			final ASTTypeDeclaration<?> mc = getParentOfType(ASTTypeDeclaration.class);
			if (mc == null)
				return null;
			return mc.getIR().getAttributeByName(name);
		}
		
		private static ASTUnqualifiedMetaAccessLink parse(final Parser parent) {
			return parseAsVariableIdentifier(new ASTUnqualifiedMetaAccessLink(), parent);
		}
	}
	
	@Override
	public String toString() {
		return "~" + attribute;
	}
	
	public static ASTUnqualifiedMetaAccess parse(final Parser parent) {
		return parent.one(p -> {
			final ASTUnqualifiedMetaAccess ast = new ASTUnqualifiedMetaAccess();
			p.one("::");
			ast.attribute = ASTUnqualifiedMetaAccessLink.parse(p);
			return ast;
		});
	}
	
	@Override
	protected IRExpression calculateIR() {
		final IRAttributeRedefinition attributeRedefinition = attribute != null ? attribute.get() : null;
		if (attributeRedefinition == null) {
			final ASTTypeDeclaration<?> selfAST = getParentOfType(ASTTypeDeclaration.class);
			return new IRUnknownExpression("Cannot find an attribute with the name " + (attribute != null ? attribute.getName() : "<unknown>")
					+ " in the type " + (selfAST == null ? "<unknown type>" : selfAST.name()), this);
		}
		return new IRAttributeAccess(IRThis.makeNew(this), attributeRedefinition, Collections.EMPTY_MAP, false, false, true);
	}
	
}
