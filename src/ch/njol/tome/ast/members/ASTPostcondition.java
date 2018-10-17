package ch.njol.tome.ast.members;

import java.util.Collections;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.ASTInterfaces.NamedASTElement;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.ast.expressions.ASTExpressions;
import ch.njol.tome.ast.statements.ASTStatements.ASTStatement;
import ch.njol.tome.compiler.Token;
import ch.njol.tome.compiler.Token.LowercaseWordToken;
import ch.njol.tome.compiler.Token.WordToken;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.expressions.IRAttributeAccess;
import ch.njol.tome.ir.statements.IRPostcondition;
import ch.njol.tome.ir.statements.IRStatement;
import ch.njol.tome.ir.statements.IRUnknownStatement;
import ch.njol.tome.parser.Parser;

public class ASTPostcondition extends AbstractASTElement implements ASTStatement, NamedASTElement {
	public boolean negated;
	public @Nullable LowercaseWordToken name;
	public @Nullable ASTExpression expression;
	
	@Override
	public @Nullable WordToken nameToken() {
		return name;
	}
	
	@Override
	public String toString() {
		return "ensures " + (name == null ? "..." : name);
	}
	
	@Override
	public @Nullable String hoverInfo(final Token token) {
		return null;
	}
	
	public static ASTPostcondition parse(final Parser parent) {
		final Parser p = parent.start();
		final ASTPostcondition ast = new ASTPostcondition();
		p.until(() -> {
			p.one("ensures");
			//genericRestrictions=GenericParameters? // FIXME wrong
			final boolean negated = p.peekNext('!');
			if (p.peekNext(negated ? 1 : 0, true) instanceof LowercaseWordToken && p.peekNext(':', negated ? 2 : 1, true)) {
				ast.negated = negated;
				if (negated)
					p.next(); // skip '!';
				ast.name = p.oneVariableIdentifierToken();
				p.next(); // skip ':'
			}
			ast.expression = ASTExpressions.parse(p); // TODO allow some statements?
		}, ';', false);
		return p.done(ast);
	}
	
	@Override
	public IRStatement getIR() {
		final ASTAttributeDeclaration attribute = getParentOfType(ASTAttributeDeclaration.class);
		if (attribute == null)
			return new IRUnknownStatement("Internal compiler error", this);
		final WordToken name = this.name;
		final ASTExpression expression = this.expression;
		if (expression == null)
			return new IRUnknownStatement("Syntax error. Proper syntax: [ensures some_expression;] or [ensures name: some_expression;] or [ensures !name: some_expression;]", this);
		final IRAttributeRedefinition negatedAttribute = getIRContext().getTypeDefinition("lang", "Boolean").getAttributeByName("negated");
		if (negatedAttribute == null)
			return new IRUnknownStatement("Cannot find attribute lang.Boolean.negated", this);
		return new IRPostcondition(attribute.getIR(), name != null ? name.word : null,
				negated ? new IRAttributeAccess(expression.getIR(), negatedAttribute, Collections.EMPTY_MAP, false, false, false) : expression.getIR());
	}
}
