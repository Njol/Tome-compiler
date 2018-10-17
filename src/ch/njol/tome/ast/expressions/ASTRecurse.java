package ch.njol.tome.ast.expressions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTAttribute;
import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.ASTInterfaces.ASTMethodCall;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.ast.members.ASTAttributeDeclaration;
import ch.njol.tome.compiler.Token;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.expressions.IRAttributeAccess;
import ch.njol.tome.ir.expressions.IRExpression;
import ch.njol.tome.ir.expressions.IRThis;
import ch.njol.tome.ir.expressions.IRUnknownExpression;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.ir.uses.IRUnknownTypeUse;
import ch.njol.tome.parser.Parser;

/**
 * The keyword 'recurse' which calls the current method, with optionally new arguments (and all unspecified arguments left the same).
 */
public class ASTRecurse extends AbstractASTElement implements ASTExpression, ASTMethodCall {
	public List<ASTArgument> arguments = new ArrayList<>();
	
	@Override
	public @Nullable IRAttributeRedefinition attribute() {
		final ASTAttribute attr = getParentOfType(ASTAttribute.class);
		return attr == null ? null : attr.getIR();
	}
	
	@Override
	public IRTypeUse getIRType() {
		final IRAttributeRedefinition attribute = attribute();
		return attribute == null ? new IRUnknownTypeUse(getIRContext()) : attribute.mainResultType();
	}
	
	@Override
	public String toString() {
		return "recurse(...)";
	}
	
	@Override
	public @Nullable String hoverInfo(final Token token) {
		final ASTAttributeDeclaration attribute = getParentOfType(ASTAttributeDeclaration.class);
		return attribute == null ? null : attribute.hoverInfo(token);
	}
	
	public static ASTRecurse parse(final Parser parent) {
		return parent.one(p -> {
			final ASTRecurse ast = new ASTRecurse();
			p.one("recurse");
			final int[] i = {0};
			p.oneGroup('(', () -> {
				do {
					ast.arguments.add(ASTArgument.parse(p, i[0]));
					i[0]++;
				} while (p.try_(','));
			}, ')');
			return ast;
		});
	}
	
	@Override
	public IRExpression getIR() {
		final IRAttributeRedefinition attribute = attribute();
		if (attribute == null)
			return new IRUnknownExpression("Internal compiler error", this);
		return new IRAttributeAccess(attribute.isStatic() ? null : IRThis.makeNew(this),
				attribute, ASTArgument.makeIRArgumentMap(attribute.definition(), arguments), false, false, false);
	}
}
