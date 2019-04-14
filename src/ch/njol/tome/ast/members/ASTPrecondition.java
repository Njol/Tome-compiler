package ch.njol.tome.ast.members;

import java.util.Collections;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTError;
import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.AbstractASTElementWithIR;
import ch.njol.tome.ast.expressions.ASTExpressions;
import ch.njol.tome.ast.statements.ASTStatements.ASTStatement;
import ch.njol.tome.compiler.Token;
import ch.njol.tome.compiler.Token.LowercaseWordToken;
import ch.njol.tome.compiler.Token.WordToken;
import ch.njol.tome.ir.IRPrecondition;
import ch.njol.tome.ir.IRUnknownPrecondition;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.expressions.IRAttributeAccess;
import ch.njol.tome.ir.statements.IRPreconditionDeclaration;
import ch.njol.tome.parser.Parser;

public class ASTPrecondition extends AbstractASTElementWithIR<IRPrecondition> implements ASTStatement<IRPrecondition>, ASTError<IRPrecondition> {
	
	public boolean negated;
	public @Nullable LowercaseWordToken name;
	public @Nullable ASTExpression<?> expression;
	
	@Override
	public @Nullable WordToken nameToken() {
		return name;
	}
	
	@Override
	public String toString() {
		return "requires " + name;
	}
	
//		@SuppressWarnings("null")
//		@Override
//		public List<? extends IRVariableRedefinition> declaredVariables() {
//			return Collections.EMPTY_LIST; // preconditions have no parameters
//		}
//
//		@Override
//		public @Nullable HasVariables parentHasVariables() {
//			return null; // TODO return overridden precondition?
//		}
	
	public static ASTPrecondition parse(final Parser parent) {
		final Parser p = parent.start();
		final ASTPrecondition ast = new ASTPrecondition();
		p.until(() -> {
			p.one("requires");
			//genericRestrictions=GenericParameters? // FIXME wrong
			ast.negated = p.peekNext('!');
			Token t;
			if ((t = p.peekNext(ast.negated ? 1 : 0, true)) instanceof LowercaseWordToken && p.peekNext(';', ast.negated ? 2 : 1, true)) {
				ast.name = (LowercaseWordToken) t;
				ast.expression = ASTExpressions.parse(p);
			} else {
				ast.negated = p.try_('!');
				ast.name = p.oneVariableIdentifierToken();
				if (p.try_(':'))
					ast.expression = ASTExpressions.parse(p); // TODO allow some statements?
			}
		}, ';', false);
		return p.done(ast);
	}
	
	@Override
	protected IRPrecondition calculateIR() {
		final String name = this.name != null ? this.name.word : "<unknown name>";
		final ASTAttributeDeclaration attribute = getParentOfType(ASTAttributeDeclaration.class);
		if (attribute == null)
			return new IRUnknownPrecondition(name, "Internal compiler error (precondition not in attribute)", this);
		final ASTExpression<?> expression = this.expression;
		if (expression == null)
			return new IRUnknownPrecondition(name, "Syntax error. Proper syntax: [ensures some_expression;] or [ensures name: some_expression;] or [ensures !name: some_expression;]", this);
		final IRAttributeRedefinition negatedAttribute = getIRContext().getTypeDefinition("lang", "Boolean").getAttributeByName("negated");
		if (negatedAttribute == null)
			return new IRUnknownPrecondition(name, "Cannot find attribute lang.Boolean.negated", this);
		return new IRPreconditionDeclaration(attribute.getIR(), name,
				negated ? new IRAttributeAccess(expression.getIR(), negatedAttribute, Collections.EMPTY_MAP, false, false, false) : expression.getIR());
	}
	
}
