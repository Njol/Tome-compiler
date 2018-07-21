package ch.njol.tome.interpreter;

import java.util.Collections;
import java.util.List;

import ch.njol.tome.interpreter.nativetypes.InterpretedNativeObject;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.nativetypes.IRTuple.IRTypeTuple;
import ch.njol.tome.ir.uses.IRClassUse;
import ch.njol.tome.ir.uses.IRTypeUse;

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
		
	}
	
}
