package ch.njol.tome.ast.expressions;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.ast.expressions.ASTExpressions.ASTAtomicExpression;
import ch.njol.tome.common.ContentAssistProposal;
import ch.njol.tome.compiler.Token;
import ch.njol.tome.compiler.Token.WordOrSymbols;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.expressions.IRAttributeAccess;
import ch.njol.tome.ir.expressions.IRExpression;
import ch.njol.tome.ir.expressions.IRUnknownExpression;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.ir.uses.IRUnknownTypeUse;
import ch.njol.tome.parser.Parser;
import ch.njol.tome.util.StringMatcher;

public class ASTAccessExpression extends AbstractASTElement implements ASTExpression {
	public final ASTExpression target;
	public boolean nullSafe, meta;
	public @Nullable ASTDirectAttributeAccess access;
	
	public ASTAccessExpression(final ASTExpression target) {
		this.target = target;
	}
	
	@Override
	public String toString() {
		return target + (nullSafe ? "?" : "") + (meta ? "::" : ".") + access;
	}
	
	@Override
	public IRTypeUse getIRType() {
		return access != null ? access.getIRType() : new IRUnknownTypeUse(getIRContext());
	}
	
	public static @Nullable ASTExpression parse(final Parser parent) {
		final Parser p = parent.start();
		final ASTExpression first = ASTAtomicExpression.parse(p);
		if (first == null) {
			p.cancel();
			return null;
		}
		return finishParsing(p, first);
	}
	
	private static ASTExpression finishParsing(final Parser p, final ASTExpression target) {
		if (!p.peekNextOneOf(".", "?.", "::", "?::")) { // note: must try '?' together with '.' or '::', as it is also used by the ternary operator '? :'
			p.doneAsChildren();
			return target;
		}
		final ASTAccessExpression ast = new ASTAccessExpression(target);
		final String op = p.oneOf(".", "?.", "::", "?::");
		assert op != null; // TODO make a peekNext call that returns the value?
		ast.nullSafe = op.startsWith("?");
		ast.meta = op.endsWith("::");
		ast.access = ASTDirectAttributeAccess.parse(p);
		final Parser parent = p.startNewParent();
		p.done(ast);
		return finishParsing(parent, ast);
	}
	
	@Override
	public IRExpression getIR() {
		final IRExpression target = this.target.getIR();
		final ASTDirectAttributeAccess access = this.access;
		if (access == null)
			return new IRUnknownExpression("Syntax error, expected an attribute", this);
		final IRAttributeRedefinition attribute = access.attribute();
		if (attribute == null) {
			final WordOrSymbols a = access.attributeLink != null ? access.attributeLink.getNameToken() : null;
			return new IRUnknownExpression("Cannot find an attribute named " + a + " in the type " + target.type(), a == null ? this : a);
		}
		return new IRAttributeAccess(target, attribute, ASTArgument.makeIRArgumentMap(attribute.definition(), access.arguments), access.allResults, nullSafe, meta);
	}
	
	@Override
	public @Nullable Stream<ContentAssistProposal> getContentAssistProposals(final Token token, final StringMatcher matcher) {
		// can only get here from operator or ASTDirectAttributeAccess
		return target.getIR().type().members().stream().filter(m -> matcher.matches(m.redefinition().name())).map(ir -> new ContentAssistProposal(ir, ir.redefinition().name()));
	}
	
}
