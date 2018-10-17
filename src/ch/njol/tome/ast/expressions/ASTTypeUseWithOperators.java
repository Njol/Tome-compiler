package ch.njol.tome.ast.expressions;

import java.util.ArrayList;
import java.util.List;

import ch.njol.tome.ast.ASTInterfaces.ASTTypeExpression;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.ast.expressions.ASTExpressions.ASTTypeExpressions;
import ch.njol.tome.ir.uses.IRAndTypeUse;
import ch.njol.tome.ir.uses.IROrTypeUse;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.parser.Parser;

/**
 * An expression with types and operators, currently only & and |.
 */
public class ASTTypeUseWithOperators extends AbstractASTElement implements ASTTypeExpression {
	
	public List<ASTTypeExpression> types = new ArrayList<>();
	
	public List<ASTOperatorLink> operators = new ArrayList<>();
	
	@Override
	public String toString() {
		String r = "" + types.get(0);
		for (int i = 0; i < operators.size(); i++) {
			r += " " + operators.get(i) + " " + types.get(i + 1);
		}
		return r;
	}
	
	public static ASTTypeExpression parse(final Parser parent) {
		final Parser p = parent.start();
		final ASTTypeUseWithOperators ast = new ASTTypeUseWithOperators();
		final ASTTypeExpression first = ASTTypeExpressions.parse(p, false, true, true);
		ast.types.add(first);
		ASTOperatorLink op;
		while ((op = ASTOperatorLink.tryParse(p, true, '&', '|')) != null) {
			ast.operators.add(op);
			ast.types.add(ASTTypeExpressions.parse(p, false, true, true));
		}
		if (ast.types.size() == 1) {
			p.doneAsChildren();
			return first;
		}
		return p.done(ast);
	}
	
	// TODO use proper operator order
	
	@Override
	public IRTypeUse getIR() {
		IRTypeUse o = types.get(0).getIR();
		for (int i = 0; i < operators.size(); i++) {
			final IRTypeUse o2 = types.get(i + 1).getIR();
			if ("&".equals(operators.get(i).getName()))
				o = IRAndTypeUse.makeNew(o, o2);
			else
				o = IROrTypeUse.makeNew(o, o2);
		}
		return o;
	}
	
}
