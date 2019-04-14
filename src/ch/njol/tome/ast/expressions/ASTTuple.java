package ch.njol.tome.ast.expressions;

import ch.njol.tome.ir.nativetypes.IRTuple;
import ch.njol.tome.parser.Parser;

/**
 * A tuple, like '[a, b: c]' or '[A, B]'. If all entries are types, this is also a type.
 */
public class ASTTuple extends AbstractASTTuple<IRTuple> {
	
	public static ASTTuple parse(final Parser parent) {
		return parse(parent, new ASTTuple());
	}
	
	@Override
	protected IRTuple calculateIR() {
		return ASTTupleEntry.makeIRNormalTuple(getIRContext(), entries);
	}
	
}
