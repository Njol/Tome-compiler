package ch.njol.brokkr.ir.definitions;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTMembers.ASTAttributeDeclaration;
import ch.njol.brokkr.interpreter.InterpretedObject;

public class IRBrokkrAttributeImplementation extends AbstractIRBrokkrAttribute implements IRAttributeImplementation {
	
	public IRBrokkrAttributeImplementation(final ASTAttributeDeclaration declaration) {
		super(declaration);
	}
	
	@Override
	public @Nullable InterpretedObject interpretImplementation(final InterpretedObject thisObject, final Map<IRParameterDefinition, InterpretedObject> arguments, final boolean allResults) {
		return super.interpretImplementation(thisObject, arguments, allResults);
	}
	
}
