package ch.njol.brokkr.interpreter;

import java.math.BigDecimal;
import java.util.Collections;

import ch.njol.brokkr.common.Kleenean;
import ch.njol.brokkr.compiler.Modules;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeInt16;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeInt32;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeInt64;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeInt8;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeUInt16;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeUInt32;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeUInt64;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeUInt8;
import ch.njol.brokkr.ir.definitions.IRBrokkrInterface;
import ch.njol.brokkr.ir.definitions.IRTypeDefinition;
import ch.njol.brokkr.ir.uses.IRSimpleTypeUse;
import ch.njol.brokkr.ir.uses.IRTypeUse;

public class Interpreter {
	
	private final Modules modules;
	
	public Interpreter(final Modules modules) {
		this.modules = modules;
	}
	
	// should only be used for standard types
	public IRTypeDefinition getType(final String module, final String name) {
		final IRTypeDefinition type = modules.getType(module, name);
		if (type == null)
			throw new InterpreterException("Missing type '" + name + "' from module '" + module + "'");
		return type;
	}
	
	public IRTypeUse getTypeUse(final String module, final String name) {
		return new IRSimpleTypeUse(getType(module, name));
	}
	
	public IRBrokkrInterface getInterface(final String module, final String name) {
		final IRTypeDefinition type = getType(module, name);
		if (!(type instanceof IRBrokkrInterface))
			throw new InterpreterException("Type '" + name + "' from module '" + module + "' is not an interface");
		return (IRBrokkrInterface) type;
	}
	
	public IRTypeDefinition getTypeType() {
		return getType("lang", "Type");
	}
	
	@SuppressWarnings("null")
	public InterpretedObject kleenean(final Kleenean value) {
		final IRBrokkrInterface kleeneanType = getInterface("lang", "Kleenean");
		return kleeneanType.getAttributeByName("" + value).interpretDispatched(null, Collections.EMPTY_MAP, false);
	}
	
	@SuppressWarnings("null")
	public InterpretedObject bool(final boolean value) {
		final IRBrokkrInterface booleanType = getInterface("lang", "Boolean");
		return booleanType.getAttributeByName("" + value).interpretDispatched(null, Collections.EMPTY_MAP, false);
	}
	
//	public IRObject newTuple(Stream<IRNativeType> types, Stream<String> names) {
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
