package ch.njol.tome.ir.expressions;

import ch.njol.tome.ast.ASTExpressions.ASTString;
import ch.njol.tome.interpreter.InterpretedObject;
import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.nativetypes.InterpretedNativeString;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.uses.IRTypeUse;

public class IRString extends AbstractIRExpression {
	
	private final IRContext irContext;
	private final String value;
	
	public IRString(final ASTString ast) {
		irContext = ast.getIRContext();
		value = registerDependency(ast).value;
	}
	
	@Override
	public IRTypeUse type() {
		return irContext.getTypeUse("lang", "String");
	}
	
	@Override
	public IRContext getIRContext() {
		return irContext;
	}
	
	@Override
	public InterpretedObject interpret(final InterpreterContext context) {
		return new InterpretedNativeString(irContext, value); // FIXME return a non-native object that actually implements lang.String
	}
	
}
