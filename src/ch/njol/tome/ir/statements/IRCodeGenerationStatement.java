package ch.njol.tome.ir.statements;

import java.util.List;
import java.util.stream.Collectors;

import ch.njol.tome.ast.ASTStatements.ASTCodeGenerationStatement;
import ch.njol.tome.compiler.Token.CodeGenerationToken;
import ch.njol.tome.interpreter.InterpretedObject;
import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.InterpreterException;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.IRElement;
import ch.njol.tome.ir.expressions.IRExpression;

public class IRCodeGenerationStatement extends AbstractIRStatement {
	
	private final IRContext irContext;
	private final List<CodeGenerationToken> code;
	private final List<IRExpression> expressions;
	
	public IRCodeGenerationStatement(ASTCodeGenerationStatement ast) {
		registerDependency(ast);
		this.irContext = ast.getIRContext();
		this.code = ast.code;
		this.expressions = ast.expressions.stream().map(e -> e.getIR()).collect(Collectors.toList());
	}
	
	@Override
	public IRContext getIRContext() {
		return irContext;
	}
	
	@Override
	public void interpret(final InterpreterContext context) throws InterpreterException {
		// TODO where to store the resulting string?
		final StringBuilder result = new StringBuilder(code.get(0).code);
		for (int i = 1; i < code.size(); i++) {
			final InterpretedObject val = expressions.get(i).interpret(context);
			result.append(val); // TODO how to make a String out of this?
			result.append(code.get(i).code);
		}
	}
	
}
