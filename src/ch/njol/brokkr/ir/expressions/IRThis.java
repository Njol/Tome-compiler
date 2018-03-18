package ch.njol.brokkr.ir.expressions;

import ch.njol.brokkr.ast.ASTElement;
import ch.njol.brokkr.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.uses.IRSelfTypeUse;
import ch.njol.brokkr.ir.uses.IRTypeUse;
import ch.njol.brokkr.ir.uses.IRUnknownTypeUse;

public class IRThis extends AbstractIRExpression {
	
	private final IRSelfTypeUse self;
	
	public IRThis(final IRSelfTypeUse self) {
		this.self = self;
	}
	
	/**
	 * Creates a new 'this' expression as if it were written where the given AST element is, i.e. will create a 'this' expression for the innermost enclosing type of the given AST
	 * element.
	 * If the given element is a type, a 'this' expression of that type will be created.
	 * 
	 * @param ast
	 * @return
	 */
	public static IRThis makeNew(final ASTElement ast) {
		final ASTTypeDeclaration type = ast.getParentOfType(ASTTypeDeclaration.class);
		return new IRThis(new IRSelfTypeUse(type == null ? new IRUnknownTypeUse(ast.getIRContext()) : type.getIR().getRawUse()));
	}
	
	@Override
	public IRTypeUse type() {
		return self;
	}
	
	@Override
	public IRContext getIRContext() {
		return self.getIRContext();
	}
	
	@Override
	public InterpretedObject interpret(final InterpreterContext context) throws InterpreterException {
		return context.getThisObject();
	}
	
}
