package ch.njol.tome.ast.members;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.ASTInterfaces.ASTMember;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeExpression;
import ch.njol.tome.ast.ASTLink;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.ast.expressions.ASTExpressions;
import ch.njol.tome.ast.expressions.ASTExpressions.ASTTypeExpressions;
import ch.njol.tome.compiler.Token.LowercaseWordToken;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.definitions.IRMemberRedefinition;
import ch.njol.tome.parser.Parser;
import ch.njol.util.StringUtils;

// TODO remove this? can be done with code generation, assuming it can work with incomplete types (what incomplete types?)
public class ASTDelegation extends AbstractASTElement implements ASTMember {
	
	public List<ASTDelegationLink> methods = new ArrayList<>();
	public List<ASTTypeExpression<?>> types = new ArrayList<>();
	public List<ASTExpression<?>> expressions = new ArrayList<>();
	public @Nullable ASTExpression<?> joinWith;
	
	private static class ASTDelegationLink extends ASTLink<IRAttributeRedefinition> {
		@Override
		protected @Nullable IRAttributeRedefinition tryLink(final String name) {
			final ASTTypeDeclaration<?> type = getParentOfType(ASTTypeDeclaration.class);
			if (type == null)
				return null;
			return type.getIR().getAttributeByName(name);
		}
		
		private static ASTDelegationLink parse(final Parser parent) {
			return parseAsVariableIdentifier(new ASTDelegationLink(), parent);
		}
	}
	
	@Override
	public boolean isInherited() {
		return true;
	}
	
	@Override
	public String toString() {
		return "delegate " + StringUtils.join(methods, ", ") + StringUtils.join(types, ", ") + " to ...";
	}
	
	public static ASTDelegation parse(final Parser parent) {
		final Parser p = parent.start();
		final ASTDelegation ast = new ASTDelegation();
		p.one("delegate");
		p.until(() -> {
			do {
				if (!ast.methods.isEmpty() || ast.types.isEmpty() && p.peekNext() instanceof LowercaseWordToken && (p.peekNext(',', 1, true) || p.peekNext("to", 1, true)))
					ast.methods.add(ASTDelegationLink.parse(p));
				else
					ast.types.add(ASTTypeExpressions.parse(p, true, true));
			} while (p.try_(','));
			p.one("to");
			do {
				ast.expressions.add(ASTExpressions.parse(p));
			} while (p.try_(','));
			if (p.try_("join")) {
				p.one("with");
				ast.joinWith = ASTExpressions.parse(p);
			}
		}, ';', false);
		return p.done(ast);
	}
	
	@Override
	public List<? extends IRMemberRedefinition> getIRMembers() {
		return Collections.EMPTY_LIST; // TODO
	}
	
}
