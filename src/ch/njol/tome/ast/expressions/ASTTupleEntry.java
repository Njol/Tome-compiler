package ch.njol.tome.ast.expressions;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.ast.expressions.ASTExpressions.ASTTypeExpressions;
import ch.njol.tome.compiler.Token.WordToken;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.expressions.IRUnknownExpression;
import ch.njol.tome.ir.nativetypes.IRTuple;
import ch.njol.tome.ir.nativetypes.IRTuple.IRTupleBuilderEntry;
import ch.njol.tome.ir.nativetypes.IRTuple.IRTypeTuple;
import ch.njol.tome.ir.nativetypes.IRTuple.IRTypeTupleBuilder;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.ir.uses.IRUnknownTypeUse;
import ch.njol.tome.parser.Parser;

/**
 * An entry of a tuple, with an optional name.
 */
public class ASTTupleEntry extends AbstractASTElement {
	public @Nullable WordToken name;
	public @Nullable ASTExpression value;
	
	@Override
	public String toString() {
		return (name != null ? name + ": " : "") + value;
	}
	
	public static ASTTupleEntry parse(final Parser parent, final boolean onlyTypes) {
		return parent.one(p -> {
			final ASTTupleEntry ast = new ASTTupleEntry();
			if (p.peekNext() instanceof WordToken && p.peekNext(':', 1, true)) {
				ast.name = p.oneIdentifierToken();
				p.next(); // skip ':'
			}
			ast.value = onlyTypes ? ASTTypeExpressions.parse(p, true, true) : ASTExpressions.parse(p);
			return ast;
		});
	}
	
	public @Nullable String name() {
		final WordToken wordToken = name;
		return wordToken != null ? wordToken.word : null;
	}
	
	public static IRTuple makeIRNormalTuple(final IRContext irContext, final List<ASTTupleEntry> entries) {
		final IRTypeTupleBuilder builder = new IRTypeTupleBuilder(irContext);
		for (final ASTTupleEntry e : entries)
			builder.addEntry(e.getNormalIR());
		return builder.build();
	}
	
	public static IRTypeTuple makeIRTypeTuple(final IRContext irContext, final List<ASTTupleEntry> entries) {
		final IRTypeTupleBuilder builder = new IRTypeTupleBuilder(irContext);
		for (final ASTTupleEntry e : entries)
			builder.addEntry(e.getTypeIR());
		return builder.build();
	}
	
	public IRTupleBuilderEntry getNormalIR() {
		final ASTExpression expression = value;
		final WordToken nameToken = name;
		final IRTypeUse valueType = expression == null ? new IRUnknownTypeUse(getIRContext()) : expression.getIRType();
		return new IRTupleBuilderEntry(nameToken == null ? "<unknown>" : nameToken.word, valueType); // TODO make an UnknownString? maybe as a constant?
	}
	
	public IRTupleBuilderEntry getTypeIR() {
		final ASTExpression expression = value;
		final WordToken nameToken = name;
		return new IRTupleBuilderEntry(nameToken == null ? "<unknown>" : nameToken.word, expression == null ? new IRUnknownExpression("Syntax error, expected an expression", this) : expression.getIR());
	}
}
