package ch.njol.brokkr.interpreter.definitions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.ast.Interfaces.FormalResult;
import ch.njol.brokkr.compiler.ast.Members.AttributeDeclaration;
import ch.njol.brokkr.compiler.ast.Members.Member;
import ch.njol.brokkr.compiler.ast.Members.SimpleParameter;
import ch.njol.brokkr.data.MethodModifiability;
import ch.njol.brokkr.interpreter.InterpretedObject;

public class InterpretedBrokkrAttributeImplementation extends AbstractInterpretedBrokkrAttribute implements InterpretedAttributeImplementation {

	public InterpretedBrokkrAttributeImplementation(AttributeDeclaration declaration) {
		super(declaration);
	}
	
	@Override
	public @Nullable InterpretedObject interpretImplementation(InterpretedObject thisObject, Map<InterpretedParameterDefinition, InterpretedObject> arguments, boolean allResults) {
		return super.interpretImplementation(thisObject, arguments, allResults);
	}
	
}
