package ch.njol.tome.ast.expressions;

import java.util.ArrayList;
import java.util.List;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.ir.expressions.IRExpression;
import ch.njol.tome.ir.nativetypes.IRTuple.IRTypeTuple;
import ch.njol.tome.parser.Parser;

/**
 * A tuple, like '[a, b: c]' or '[A, B]'. If all entries are types, this is also a type.
 */
public class ASTTuple extends AbstractASTElement implements ASTExpression {
	
	public List<ASTTupleEntry> entries = new ArrayList<>();
	
	@Override
	public String toString() {
		return "" + entries;
	}
	
	public static ASTTuple parse(final Parser parent) {
		return parse(parent, new ASTTuple());
	}
	
	protected static <T extends ASTTuple> T parse(final Parser parent, final T ast) {
		return parent.one(p -> {
			p.oneGroup('[', () -> {
				do {
					ast.entries.add(ASTTupleEntry.parse(p, ast instanceof ASTTypeTuple));
				} while (p.try_(','));
			}, ']');
			return ast;
		});
	}
	
	@Override
	public IRExpression getIR() {
		return ASTTupleEntry.makeIRNormalTuple(getIRContext(), entries);
	}
	
	@Override
	public IRTypeTuple getIRType() {
		return ASTTupleEntry.makeIRTypeTuple(getIRContext(), entries);
	}
	
}
