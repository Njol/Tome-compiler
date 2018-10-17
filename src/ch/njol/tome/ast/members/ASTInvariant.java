package ch.njol.tome.ast.members;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.ASTInterfaces.ASTMember;
import ch.njol.tome.ast.ASTInterfaces.NamedASTElement;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.ast.expressions.ASTExpressions;
import ch.njol.tome.compiler.Token.LowercaseWordToken;
import ch.njol.tome.compiler.Token.WordToken;
import ch.njol.tome.ir.definitions.IRMemberRedefinition;
import ch.njol.tome.parser.Parser;

public class ASTInvariant extends AbstractASTElement implements ASTMember, NamedASTElement {
	public final ASTMemberModifiers modifiers;
	
	public boolean negated;
	public @Nullable LowercaseWordToken name;
	public @Nullable ASTExpression expression;
	
	public ASTInvariant(final ASTMemberModifiers modifiers) {
		this.modifiers = modifiers;
	}
	
	@Override
	public boolean isInherited() {
		return true;
	}
	
	@Override
	public @Nullable WordToken nameToken() {
		return name;
	}
	
	@Override
	public String toString() {
		return "invariant " + name;
	}
	
	public static ASTInvariant finishParsing(final Parser p, final ASTMemberModifiers modifiers) {
		final ASTInvariant ast = new ASTInvariant(modifiers);
		p.one("invariant");
		p.until(() -> {
			ast.negated = p.try_('!');
			ast.name = p.oneVariableIdentifierToken();
			p.one(':');
			ast.expression = ASTExpressions.parse(p); // TODO allow some statements?
		}, ';', false);
		return p.done(ast);
	}
	
	@Override
	public List<? extends IRMemberRedefinition> getIRMembers() {
		return Collections.EMPTY_LIST; // TODO
	}
}
