package ch.njol.tome.ir.definitions;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTMembers.ASTAttributeDeclaration;
import ch.njol.tome.interpreter.InterpretedObject;
import ch.njol.tome.interpreter.InterpreterException;

public class IRBrokkrAttributeImplementation extends AbstractIRBrokkrAttribute implements IRAttributeImplementation {
	
	public IRBrokkrAttributeImplementation(final ASTAttributeDeclaration declaration, final IRAttributeRedefinition overridden) {
		super(declaration, overridden);
	}
	
	@Override
	public @Nullable InterpretedObject interpretImplementation(final InterpretedObject thisObject, final Map<IRParameterDefinition, InterpretedObject> arguments, final boolean allResults) throws InterpreterException {
		return super.interpretImplementation(thisObject, arguments, allResults);
	}
	
}
