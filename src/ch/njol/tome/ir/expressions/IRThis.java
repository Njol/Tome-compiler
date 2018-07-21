package ch.njol.tome.ir.expressions;

import ch.njol.tome.ast.ASTElement;
import ch.njol.tome.interpreter.InterpretedObject;
import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.InterpreterException;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.uses.IRSelfTypeUse;
import ch.njol.tome.ir.uses.IRTypeUse;

public class IRThis extends AbstractIRExpression {
	
	private final IRSelfTypeUse self;
	
	public IRThis(final IRSelfTypeUse self, final ASTElement ast) {
		this.self = self;
		registerDependency(ast);
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
		return new IRThis(IRSelfTypeUse.makeNew(ast), ast);
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
