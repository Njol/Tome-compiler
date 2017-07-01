package ch.njol.brokkr.interpreter.definitions;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.ast.Members.AttributeDeclaration;
import ch.njol.brokkr.interpreter.InterpretedObject;

public class InterpretedBrokkrAttributeDefinitionAndImplementation extends AbstractInterpretedBrokkrAttribute implements InterpretedAttributeDefinition, InterpretedAttributeImplementation {
	
	public InterpretedBrokkrAttributeDefinitionAndImplementation(final AttributeDeclaration declaration) {
		super(declaration);
	}
	
	@Override
	public InterpretedAttributeDefinition definition() {
		return this;
	}
	
	@Override
	public @Nullable InterpretedObject interpretImplementation(final InterpretedObject thisObject, final Map<InterpretedParameterDefinition, InterpretedObject> arguments, final boolean allResults) {
		return super.interpretImplementation(thisObject, arguments, allResults);
	}
	
}
