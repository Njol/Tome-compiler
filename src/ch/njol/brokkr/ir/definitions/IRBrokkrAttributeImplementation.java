package ch.njol.brokkr.ir.definitions;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTMembers.ASTAttributeDeclaration;
import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterException;

public class IRBrokkrAttributeImplementation extends AbstractIRBrokkrAttribute implements IRAttributeImplementation {
	
	public IRBrokkrAttributeImplementation(final ASTAttributeDeclaration declaration, final IRAttributeRedefinition overridden) {
		super(declaration, overridden);
	}
	
	@Override
	public @Nullable InterpretedObject interpretImplementation(final InterpretedObject thisObject, final Map<IRParameterDefinition, InterpretedObject> arguments, final boolean allResults) throws InterpreterException {
		return super.interpretImplementation(thisObject, arguments, allResults);
	}
	
}
