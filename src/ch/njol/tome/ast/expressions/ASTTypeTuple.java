package ch.njol.tome.ast.expressions;

import ch.njol.tome.ast.ASTInterfaces.ASTTypeExpression;
import ch.njol.tome.ir.nativetypes.IRTuple.IRTypeTuple;
import ch.njol.tome.parser.Parser;

/**
 * A subclass of Tuple that implements TypeExpression, and entries are only parsed as types.
 * <p>
 * Exclusively used for parsing a tuple as a type only; normal tuples that contain only types must be handled equal to such a type tuple.
 */
public class ASTTypeTuple extends AbstractASTTuple<IRTypeTuple> implements ASTTypeExpression<IRTypeTuple> {
	
	@Override
	protected IRTypeTuple calculateIR() {
		return (IRTypeTuple) ASTTupleEntry.makeIRNormalTuple(getIRContext(), entries);
	}
	
	public static ASTTypeTuple parse(final Parser parent) {
		return parse(parent, new ASTTypeTuple());
	}
	
}
