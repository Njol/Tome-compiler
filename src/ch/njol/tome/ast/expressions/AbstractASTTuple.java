package ch.njol.tome.ast.expressions;

import java.util.ArrayList;
import java.util.List;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.AbstractASTElementWithIR;
import ch.njol.tome.ir.nativetypes.IRTuple;
import ch.njol.tome.ir.nativetypes.IRTuple.IRTypeTuple;
import ch.njol.tome.parser.Parser;

public abstract class AbstractASTTuple<IR extends IRTuple> extends AbstractASTElementWithIR<IR> implements ASTExpression<IR> {
	
	public List<ASTTupleEntry> entries = new ArrayList<>();
	
	@Override
	public String toString() {
		return "" + entries;
	}
	
	protected static <T extends AbstractASTTuple<?>> T parse(final Parser parent, final T ast) {
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
	public IRTypeTuple getIRType() {
		return ASTTupleEntry.makeIRTypeTuple(getIRContext(), entries);
	}
	
}
