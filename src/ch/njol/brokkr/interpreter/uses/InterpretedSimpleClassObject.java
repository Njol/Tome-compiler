package ch.njol.brokkr.interpreter.uses;

import org.eclipse.jdt.annotation.NonNull;

import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeClassDefinition;

public class InterpretedSimpleClassObject extends InterpretedSimpleClassUse implements InterpretedClassObject {
	
	public InterpretedSimpleClassObject(final InterpretedNativeClassDefinition type) {
		super(type);
	}
	
	@Override
	public InterpretedClassObject nativeClass() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
