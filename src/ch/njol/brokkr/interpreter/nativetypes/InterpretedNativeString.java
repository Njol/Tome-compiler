package ch.njol.brokkr.interpreter.nativetypes;

import ch.njol.brokkr.ir.IRContext;

public class InterpretedNativeString extends AbstractInterpretedSimpleNativeObject {
	
	public final String value;
	
	public InterpretedNativeString(final IRContext irContext, final String value) {
		super(irContext);
		this.value = value;
	}
	
	// native methods
	
	// TODO
	
}
