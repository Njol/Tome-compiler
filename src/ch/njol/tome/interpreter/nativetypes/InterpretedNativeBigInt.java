package ch.njol.tome.interpreter.nativetypes;

import java.math.BigInteger;

import ch.njol.tome.ir.IRContext;

public class InterpretedNativeBigInt extends AbstractInterpretedSimpleNativeObject {
	
	public static final String TOME_INTERFACE = "lang.Integer";
	
	public final BigInteger value;
	
	public InterpretedNativeBigInt(IRContext irContext, BigInteger value) {
		super(irContext);
		this.value = value;
	}
	
	// native methods
	
	public InterpretedNativeBigInt _add(InterpretedNativeBigInt other) {
		return new InterpretedNativeBigInt(irContext, value.add(other.value));
	}
	
	public InterpretedNativeBigInt _subtract(InterpretedNativeBigInt other) {
		return new InterpretedNativeBigInt(irContext, value.subtract(other.value));
	}
	
	public InterpretedNativeBigInt _multiply(InterpretedNativeBigInt other) {
		return new InterpretedNativeBigInt(irContext, value.multiply(other.value));
	}
	
}
