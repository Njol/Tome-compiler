package ch.njol.brokkr.interpreter.uses;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

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
	
}
