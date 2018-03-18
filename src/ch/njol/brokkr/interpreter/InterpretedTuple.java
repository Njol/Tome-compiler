package ch.njol.brokkr.interpreter;

import java.util.Collections;
import java.util.List;

import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeObject;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.definitions.IRGenericTypeDefinition;
import ch.njol.brokkr.ir.nativetypes.IRTuple.IRTypeTuple;
import ch.njol.brokkr.ir.uses.IRClassUse;
import ch.njol.brokkr.ir.uses.IRTypeUse;

public class InterpretedTuple implements InterpretedNativeObject {
	
	public final static InterpretedTuple emptyTuple(final IRContext irContext) {
		return new InterpretedTuple(IRTypeTuple.emptyTuple(irContext), Collections.EMPTY_LIST);
	}
	
	public final IRTypeTuple type;
	public final List<InterpretedObject> values;
	
	public InterpretedTuple(final IRTypeTuple type, final List<InterpretedObject> values) {
		assert type.entries.size() == values.size();
		this.type = type;
		this.values = values;
	}
	
	@Override
	public IRClassUse nativeClass() {
		return type; // TODO make tuple interfaces and classes
	}
	
	public static class InterpretedTypeTuple extends InterpretedTuple implements InterpretedTypeUse {
		
		public InterpretedTypeTuple(final IRTypeTuple type, final List<InterpretedObject> values) {
			super(type, values);
		}
		
		@Override
		public IRTypeUse irType() {
			return type;
		}
		
		@Override
		public InterpretedTypeUse getGenericType(final IRGenericTypeDefinition definition) throws InterpreterException {
			throw new InterpreterException("Trying to get a generic type of a tuple"); // tuples have no generic types - or do they?
		}
		
	}
	
}
