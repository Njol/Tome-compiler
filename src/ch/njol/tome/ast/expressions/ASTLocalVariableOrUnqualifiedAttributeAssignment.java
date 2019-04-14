package ch.njol.tome.ast.expressions;

import java.util.Collections;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.compiler.Token.WordOrSymbols;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.definitions.IRVariableOrAttributeRedefinition;
import ch.njol.tome.ir.definitions.IRVariableRedefinition;
import ch.njol.tome.ir.expressions.IRAttributeAccess;
import ch.njol.tome.ir.expressions.IRAttributeAssignment;
import ch.njol.tome.ir.expressions.IRExpression;
import ch.njol.tome.ir.expressions.IRThis;
import ch.njol.tome.ir.expressions.IRUnknownExpression;
import ch.njol.tome.ir.expressions.IRVariableAssignment;
import ch.njol.tome.ir.expressions.IRVariableExpression;
import ch.njol.tome.parser.Parser;

// TODO think about whether assignment should really be an expression - this can be handy, but can also hide state changes.
// pro: [while ((toSleep = ...) > 0) Thread.sleep(toSleep);]
// remember to issue a warning when used like [if (var = some bool)]
public class ASTLocalVariableOrUnqualifiedAttributeAssignment extends AbstractASTAssignment<ASTLocalVariableOrUnqualifiedAttributeAssignment> {
	
	public final ASTVariableOrUnqualifiedAttributeUse varOrAttribute;
	
	public ASTLocalVariableOrUnqualifiedAttributeAssignment(final ASTVariableOrUnqualifiedAttributeUse varOrAttribute, final @Nullable ASTOperatorLink assignmentOpLink, final WordOrSymbols assignmentOpToken) {
		super(assignmentOpLink, assignmentOpToken);
		this.varOrAttribute = varOrAttribute;
	}
	
	@Override
	public String toString() {
		return varOrAttribute + " " + assignmentOpToken + " " + value;
	}
	
	public static ASTLocalVariableOrUnqualifiedAttributeAssignment finishParsing(final Parser p, final ASTVariableOrUnqualifiedAttributeUse varOrAttribute,
			final @Nullable ASTOperatorLink assignmentOpLink, final WordOrSymbols assignmentOpToken) {
		final ASTLocalVariableOrUnqualifiedAttributeAssignment ast = new ASTLocalVariableOrUnqualifiedAttributeAssignment(varOrAttribute, assignmentOpLink, assignmentOpToken);
		ast.value = ASTExpressions.parse(p);
		return p.done(ast);
	}
	
	@Override
	protected IRExpression makeAssignmentIR(final IRExpression value) {
		final IRVariableOrAttributeRedefinition varOrAttr = varOrAttribute.varOrAttribute();
		if (varOrAttr == null)
			return new IRUnknownExpression("Cannot find the local variable or attribute " + varOrAttribute, varOrAttribute);
		if (varOrAttr instanceof IRAttributeRedefinition) {
			return new IRAttributeAssignment(IRThis.makeNew(this), ((IRAttributeRedefinition) varOrAttr).definition(), value);
		} else {
			return new IRVariableAssignment(((IRVariableRedefinition) varOrAttr).definition(), value);
		}
	}
	
	@Override
	protected IRExpression makeAccessIR() {
		final IRVariableOrAttributeRedefinition varOrAttr = varOrAttribute.varOrAttribute();
		if (varOrAttr == null)
			return new IRUnknownExpression("Cannot find the local variable or attribute " + varOrAttribute, varOrAttribute);
		if (varOrAttr instanceof IRAttributeRedefinition) {
			return new IRAttributeAccess(IRThis.makeNew(this), ((IRAttributeRedefinition) varOrAttr).definition(), Collections.EMPTY_MAP, false, false, false);
		} else {
			return new IRVariableExpression((IRVariableRedefinition) varOrAttr);
		}
	}
	
}
