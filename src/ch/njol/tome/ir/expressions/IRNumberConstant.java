package ch.njol.tome.ir.expressions;

import java.math.BigDecimal;

import ch.njol.tome.ast.expressions.ASTNumberConstant;
import ch.njol.tome.interpreter.InterpretedObject;
import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.InterpreterException;
import ch.njol.tome.interpreter.nativetypes.InterpretedNativeBigInt;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.ir.uses.IRUnknownTypeUse;

public class IRNumberConstant extends AbstractIRExpression {
	
	private final IRContext irContext;
	private final BigDecimal value;
	
	public IRNumberConstant(final ASTNumberConstant ast) {
		irContext = ast.getIRContext();
		value = registerDependency(ast).value;
	}
	
	@Override
	public IRTypeUse type() {
		return type(irContext, value);
	}
	
	@Override
	public InterpretedObject interpret(final InterpreterContext context) throws InterpreterException {
		return interpreted(irContext, value);
	}
	
	public static IRTypeUse type(final IRContext irContext, final BigDecimal value) {
		return new IRUnknownTypeUse(irContext); // TODO
//		return interpreted(value).nativeClass();
	}
	
	@Override
	public IRContext getIRContext() {
		return irContext;
	}
	
	@Override
	public String toString() {
		return "" + value;
	}
	
	// FIXME should return a non-native object, e.g. need to call [Int8.fromNative(...)] in addition to the current code
	// TODO or allow native types to implement Brokkr interfaces (e.g. via extensions)
	public static InterpretedObject interpreted(final IRContext irContext, final BigDecimal value) throws InterpreterException {
		if (value.scale() <= 0) { // i.e. an integer
			return new InterpretedNativeBigInt(irContext, value.toBigIntegerExact());
//			try {
//				final long val = value.longValueExact();
//				if (val < 0) {
//					if ((val & ~0x7FL) == 0)
//						return new InterpretedNativeInt8(irContext, (byte) val);
//					else if ((val & ~0x7FFFL) == 0)
//						return new InterpretedNativeInt16(irContext, (short) val);
//					else if ((val & ~0x7FFF_FFFFL) == 0)
//						return new InterpretedNativeInt32(irContext, (int) val);
//					else
//						return new InterpretedNativeInt64(irContext, val);
//				} else {
//					if ((val & ~0xFFL) == 0)
//						return new InterpretedNativeUInt8(irContext, (byte) val);
//					else if ((val & ~0xFFFFL) == 0)
//						return new InterpretedNativeUInt16(irContext, (short) val);
//					else if ((val & ~0xFFFF_FFFFL) == 0)
//						return new InterpretedNativeUInt32(irContext, (int) val);
//					else
//						return new InterpretedNativeUInt64(irContext, val);
//				}
//			} catch (final ArithmeticException e) {
//				// BigInteger
//				return new InterpretedNativeBigInt(irContext, value.toBigIntegerExact());
//			}
		} else {
			// BigDecimal
			throw new InterpreterException("not implemented");
		}
	}
	
}
