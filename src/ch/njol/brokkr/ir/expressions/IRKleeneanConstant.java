package ch.njol.brokkr.ir.expressions;

import ch.njol.brokkr.ast.ASTExpressions.ASTKleeneanConstant;
import ch.njol.brokkr.common.Kleenean;
import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeBoolean;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeKleenean;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.uses.IRTypeUse;

public class IRKleeneanConstant extends AbstractIRExpression {
	
	private final IRContext irContext;
	private final Kleenean value;
	
	public IRKleeneanConstant(ASTKleeneanConstant ast) {
		this.irContext = ast.getIRContext();
		this.value = registerDependency(ast).value;
	}
	
	@Override
	public IRTypeUse type() {
		return irContext.getTypeUse("lang", value == Kleenean.UNKNOWN ? "Kleenean" : "Boolean");
	}
	
	@Override
	public IRContext getIRContext() {
		return irContext;
	}
	
	@Override
	public InterpretedObject interpret(final InterpreterContext context) {
		if (value == Kleenean.UNKNOWN)
			return new InterpretedNativeKleenean(irContext, Kleenean.UNKNOWN);
		else
			return new InterpretedNativeBoolean(irContext, value == Kleenean.TRUE);
	}
	
}
