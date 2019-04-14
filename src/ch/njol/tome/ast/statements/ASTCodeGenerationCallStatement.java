package ch.njol.tome.ast.statements;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.AbstractASTElementWithIR;
import ch.njol.tome.ast.expressions.ASTExpressions;
import ch.njol.tome.ast.statements.ASTStatements.ASTStatement;
import ch.njol.tome.interpreter.InterpretedNormalObject;
import ch.njol.tome.interpreter.InterpretedObject;
import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.InterpreterException;
import ch.njol.tome.interpreter.nativetypes.InterpretedNativeCodeGenerationResult;
import ch.njol.tome.ir.statements.IRStatement;
import ch.njol.tome.ir.statements.IRUnknownStatement;
import ch.njol.tome.parser.Parser;

public class ASTCodeGenerationCallStatement extends AbstractASTElementWithIR<IRStatement> implements ASTStatement<IRStatement> {
	
	public @Nullable ASTExpression<?> code;
	
	@Override
	public String toString() {
		return "$= " + code + ";";
	}
	
	public static ASTCodeGenerationCallStatement parse(final Parser parent) {
		final Parser p = parent.start();
		final ASTCodeGenerationCallStatement ast = new ASTCodeGenerationCallStatement();
		p.one("$=");
		p.until(() -> {
			ast.code = ASTExpressions.parse(p);
		}, ';', false);
		return p.done(ast);
	}
	
	@Override
	protected IRStatement calculateIR() {
		final ASTExpression<?> code = this.code;
		if (code == null)
			return new IRUnknownStatement("Syntax error. proper syntax: [$= some_expression;]", this);
		try {
			final InterpretedObject result = code.getIR().interpret(new InterpreterContext(getIRContext(), (InterpretedNormalObject) null));
			if (!(result instanceof InterpretedNativeCodeGenerationResult))
				return new IRUnknownStatement("Must call a code generation template", this);
			return ((InterpretedNativeCodeGenerationResult) result).parseStatements(this);
		} catch (final InterpreterException e) {
			return new IRUnknownStatement("" + e.getMessage(), this);
		}
	}
	
}
