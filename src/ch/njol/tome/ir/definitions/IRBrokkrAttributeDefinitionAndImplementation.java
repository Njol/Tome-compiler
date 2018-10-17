package ch.njol.tome.ir.definitions;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.members.ASTAttributeDeclaration;
import ch.njol.tome.interpreter.InterpretedObject;
import ch.njol.tome.interpreter.InterpreterException;

public class IRBrokkrAttributeDefinitionAndImplementation extends AbstractIRBrokkrAttribute implements IRAttributeDefinition, IRAttributeImplementation {
	
	public IRBrokkrAttributeDefinitionAndImplementation(final ASTAttributeDeclaration declaration) {
		super(declaration);
	}
	
	@Override
	public IRAttributeDefinition definition() {
		return this;
	}
	
	@Override
	public @Nullable InterpretedObject interpretImplementation(final InterpretedObject thisObject, final Map<IRParameterDefinition, InterpretedObject> arguments, final boolean allResults) throws InterpreterException {
		return super.interpretImplementation(thisObject, arguments, allResults);
	}
	
}
