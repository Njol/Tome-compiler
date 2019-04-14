package ch.njol.tome.ast.statements;

import java.util.ArrayList;
import java.util.List;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.AbstractASTElementWithIR;
import ch.njol.tome.ast.expressions.ASTExpressions;
import ch.njol.tome.ast.statements.ASTStatements.ASTStatement;
import ch.njol.tome.compiler.Token;
import ch.njol.tome.compiler.Token.CodeGenerationToken;
import ch.njol.tome.ir.statements.IRCodeGenerationStatement;
import ch.njol.tome.ir.statements.IRStatement;
import ch.njol.tome.parser.Parser;

/**
 * A line of code that will generate a line of code when called (i.e. must always be in a code generation method)
 * TODO allow this outside of code generation methods? i.e. just execute it statically in that case?
 * -> actually, should use different syntax for 'code here' (currently [$=]) and 'code for later' (currently [$]), so that this statement is still only valid in code generation
 * methods.
 */
public class ASTCodeGenerationStatement extends AbstractASTElementWithIR<IRStatement> implements ASTStatement<IRStatement> {
	
	// TODO make these a single list?
	public final List<CodeGenerationToken> code = new ArrayList<>();
	public final List<ASTExpression<?>> expressions = new ArrayList<>();
	
	@Override
	public String toString() {
		return "$...$";
	}
	
	public static ASTCodeGenerationStatement parse(final Parser parent) {
		final Parser p = parent.start();
		final ASTCodeGenerationStatement ast = new ASTCodeGenerationStatement();
		CodeGenerationToken t = (CodeGenerationToken) p.next();
		assert t != null;
		ast.code.add(t);
		while (true) {
			if (t.ended)
				break;
			ast.expressions.add(ASTExpressions.parse(p));
			final Token x = p.next();
			if (x == null || !(x instanceof CodeGenerationToken)) {
				p.expectedFatal("'$'");
				return p.done(ast);
			}
			t = (CodeGenerationToken) x;
			ast.code.add(t);
		}
		return p.done(ast);
	}
	
	@Override
	protected IRStatement calculateIR() {
		return new IRCodeGenerationStatement(this);
	}
	
}
