package ch.njol.brokkr.interpreter;

import java.math.BigDecimal;
import java.util.Collections;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.Modules;
import ch.njol.brokkr.compiler.ast.Interfaces.TypeDeclaration;
import ch.njol.brokkr.data.Kleenean;
import ch.njol.brokkr.interpreter.definitions.InterpretedNativeTypeDefinition;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeBrokkrInterface;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeInt16;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeInt32;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeInt64;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeInt8;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeUInt16;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeUInt32;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeUInt64;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeUInt8;
import ch.njol.brokkr.interpreter.uses.InterpretedSimpleTypeUse;
import ch.njol.brokkr.interpreter.uses.InterpretedTypeUse;

public class Interpreter {
	
	private final Modules modules;
	
	public Interpreter(final Modules modules) {
		this.modules = modules;
	}
	
	// should only be used for standard types
	public InterpretedNativeTypeDefinition getType(final String module, final String name) {
		final InterpretedNativeTypeDefinition type = modules.getType(module, name);
		if (type == null)
			throw new InterpreterException("Missing type '" + name + "' from module '" + module + "'");
		return type;
	}

	public InterpretedTypeUse getTypeUse(final String module, final String name) {
		return new InterpretedSimpleTypeUse(getType(module, name));
	}
	
	public InterpretedNativeBrokkrInterface getInterface(final String module, final String name) {
		final InterpretedNativeTypeDefinition type = getType(module, name);
		if (!(type instanceof InterpretedNativeBrokkrInterface))
			throw new InterpreterException("Type '" + name + "' from module '" + module + "' is not an interface");
		return (InterpretedNativeBrokkrInterface) type;
	}
	
	public InterpretedNativeTypeDefinition getTypeType() {
		return getType("lang", "Type");
	}
	
	@SuppressWarnings("null")
	public InterpretedObject kleenean(final Kleenean value) {
		final InterpretedNativeBrokkrInterface kleeneanType = getInterface("lang", "Kleenean");
		return kleeneanType.getAttributeByName("" + value).interpretDispatched(null, Collections.EMPTY_MAP, false);
	}
	
	@SuppressWarnings("null")
	public InterpretedObject bool(final boolean value) {
		final InterpretedNativeBrokkrInterface booleanType = getInterface("lang", "Boolean");
		return booleanType.getAttributeByName("" + value).interpretDispatched(null, Collections.EMPTY_MAP, false);
	}
	
//	public InterpretedObject newTuple(Stream<InterpretedNativeType> types, Stream<String> names) {
//		// TODO Auto-generated method stub
//		return null;
//	}
	
	public InterpretedObject numberConstant(final BigDecimal value) {
		if (value.scale() <= 0) { // i.e. an integer
			try {
				final long val = value.longValueExact();
				if (val < 0) {
					if ((val & ~0x7Fl) == 0)
						return new InterpretedNativeInt8((byte) val);
					else if ((val & ~0x7FFFl) == 0)
						return new InterpretedNativeInt16((short) val);
					else if ((val & ~0x7FFF_FFFFl) == 0)
						return new InterpretedNativeInt32((int) val);
					else
						return new InterpretedNativeInt64(val);
				} else {
					if ((val & ~0xFFl) == 0)
						return new InterpretedNativeUInt8((byte) val);
					else if ((val & ~0xFFFFl) == 0)
						return new InterpretedNativeUInt16((short) val);
					else if ((val & ~0xFFFF_FFFFl) == 0)
						return new InterpretedNativeUInt32((int) val);
					else
						return new InterpretedNativeUInt64(val);
				}
			} catch (final ArithmeticException e) {
				// BigInteger
				throw new InterpreterException("not implemented");
			}
		} else {
			// BigDecimal
			throw new InterpreterException("not implemented");
		}
	}
	
	public InterpretedObject stringConstant(final String value) {
		throw new InterpreterException("not implemented");
	}

}
