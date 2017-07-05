package ch.njol.brokkr.interpreter.uses;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeImplementation;
import ch.njol.brokkr.interpreter.definitions.InterpretedGenericTypeDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedNativeTypeDefinition;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeClassDefinition;

public class InterpretedSimpleClassUse extends InterpretedSimpleTypeUse implements InterpretedClassUse {
	
	public InterpretedSimpleClassUse(final InterpretedNativeClassDefinition type) {
		super(type);
	}
	
	public InterpretedSimpleClassUse(final InterpretedNativeClassDefinition base, final Map<InterpretedGenericTypeDefinition, InterpretedTypeUse> genericArguments) {
		super(base, genericArguments);
	}
	
	@Override
	public InterpretedNativeClassDefinition getBase() {
		return (InterpretedNativeClassDefinition) super.getBase();
	}

	@Override
	public @Nullable InterpretedAttributeImplementation getAttributeImplementation(InterpretedAttributeDefinition definition) {
		return getBase().getAttributeImplementation(definition);
	}

}
