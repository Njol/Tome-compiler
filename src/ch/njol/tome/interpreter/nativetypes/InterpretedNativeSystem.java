package ch.njol.tome.interpreter.nativetypes;

import ch.njol.tome.ir.IRContext;

public class InterpretedNativeSystem extends AbstractInterpretedSimpleNativeObject {
	
	public static final String TOME_INTERFACE = "system.System";
	
	public InterpretedNativeSystem(IRContext irContext) {
		super(irContext);
	}
	
	// native methods
	
	public void _printHelloWorld() {
		System.out.println("Hello World!");
	}
	
//	public void _println(final InterpretedNativeString line) {
//		System.out.println(line.value);
//	}
	
	public void _println(final InterpretedNativeBigInt value) {
		System.out.println(value.value);
	}
	
}
