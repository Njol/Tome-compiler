package ch.njol.brokkr.interpreter.nativetypes;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.InterpretedError;
import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeImplementation;
import ch.njol.brokkr.interpreter.definitions.InterpretedMemberRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedNativeTypeDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedParameterDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedParameterRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedResultDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedResultRedefinition;
import ch.njol.brokkr.interpreter.uses.InterpretedAttributeUse;
import ch.njol.brokkr.interpreter.uses.InterpretedClassUse;
import ch.njol.brokkr.interpreter.uses.InterpretedMemberUse;
import ch.njol.brokkr.interpreter.uses.InterpretedTypeUse;

public abstract class InterpretedTuple implements InterpretedNativeObject {
	
	public final List<InterpretedNativeTupleValueAndEntry> entries;
	
	protected InterpretedTuple(final List<InterpretedNativeTupleValueAndEntry> entries) {
		this.entries = entries;
		entries.forEach(e -> e.entry.tuple = this);
	}
	
	public static InterpretedTuple newInstance(final Stream<InterpretedNativeTupleValueAndEntry> entries) {
		final List<InterpretedNativeTupleValueAndEntry> entriesList = entries.collect(Collectors.toList());
		if (entries.allMatch(e -> e.value instanceof InterpretedNativeTypeDefinition)) // TODO is this correct? is this how e.g. a [Int8] is interpreted?
			return new InterpretedTypeTuple(entriesList);
		else
			return new InterpretedNormalTuple(entriesList);
	}
	
	public static class InterpretedNativeTupleValueAndEntry {
		
		public final InterpretedTupleEntry entry;
		public final InterpretedObject value;
		
		public InterpretedNativeTupleValueAndEntry(final InterpretedTupleEntry entry, final InterpretedObject value) {
			this.entry = entry;
			this.value = value;
		}
		
		public InterpretedNativeTupleValueAndEntry(final int index, final InterpretedTypeUse type, final String name, final InterpretedObject value) {
			entry = new InterpretedTupleEntry(index, type, name);
			this.value = value;
		}
		
	}
	
	public static class InterpretedTupleEntry implements InterpretedAttributeDefinition, InterpretedAttributeImplementation, InterpretedMemberUse {
		
		public @Nullable InterpretedTuple tuple;
		public final int index;
		public final InterpretedTypeUse type;
		public final String name;
		
		public InterpretedTupleEntry(final int index, final InterpretedTypeUse type, final String name) {
			this.index = index;
			this.type = type;
			this.name = name;
		}
		
		@Override
		public String name() {
			return name;
		}
		
		@SuppressWarnings("null")
		@Override
		public InterpretedTypeUse targetType() {
			return tuple.nativeClass();
		}
		
		@Override
		public List<InterpretedParameterRedefinition> parameters() {
			return Collections.EMPTY_LIST;
		}
		
		@Override
		public List<InterpretedResultRedefinition> results() {
			return Collections.singletonList(new TupleEntryResult());
		}
		
		// TODO make better, and think of how to make definitions and redefinitions for tuples
		private class TupleEntryResult implements InterpretedResultDefinition {
			@Override
			public String name() {
				return name;
			}
			
			@Override
			public InterpretedTypeUse type() {
				return type;
			}
		}
		
		@Override
		public List<InterpretedError> errors() {
			return Collections.EMPTY_LIST;
		}
		
		@Override
		public boolean isModifying() {
			return false;
		}
		
		@Override
		public boolean isVariable() {
			return true;
		}
		
		@Override
		public @Nullable InterpretedObject interpretImplementation(final InterpretedObject thisObject, final Map<InterpretedParameterDefinition, InterpretedObject> arguments, final boolean allResults) {
			if (!(thisObject instanceof InterpretedTuple))
				throw new InterpreterException("Not a tuple");
			for (final InterpretedNativeTupleValueAndEntry e : ((InterpretedTuple) thisObject).entries) {
				if (e.entry.equalsMember(this))
					return e.value;
			}
			throw new InterpreterException("Invalid tuple entry");
		}
		
		@Override
		public boolean equalsMember(final InterpretedMemberRedefinition other) {
			if (!(other instanceof InterpretedTupleEntry))
				return false;
			final InterpretedTupleEntry e = (InterpretedTupleEntry) other;
			return e.name.equals(name) && e.index == index && e.type.equalsType(type);
		}
		
		@Override
		public InterpretedMemberRedefinition redefinition() {
			return this;
		}
		
		@Override
		public @NonNull InterpretedAttributeDefinition definition() {
			return this;
		}
		
	}
	
	@Override
	public InterpretedClassUse nativeClass() {
		return new InterpretedTypeTuple(entries.stream().map(e -> new InterpretedNativeTupleValueAndEntry(e.entry.index, e.entry.type.nativeClass(), e.entry.name, e.entry.type)).collect(Collectors.toList()));
	}
	
	public static class InterpretedNormalTuple extends InterpretedTuple {
		
		public InterpretedNormalTuple(final List<InterpretedNativeTupleValueAndEntry> entries) {
			super(entries);
		}
		
	}
	
	/**
	 * A tuple whose entries are only types is also a type of tuples whose entries are instances of this one's type entries.
	 */
	public static class InterpretedTypeTuple extends InterpretedTuple implements InterpretedClassUse {
		
		public InterpretedTypeTuple(final List<InterpretedNativeTupleValueAndEntry> entries) {
			super(entries);
		}
		
		@Override
		public @Nullable InterpretedAttributeImplementation getAttributeImplementation(final InterpretedAttributeDefinition definition) {
			for (final InterpretedNativeTupleValueAndEntry e : entries) {
				if (e.entry.equalsMember(definition))
					return e.entry;
			}
			return null;
		}
		
		@Override
		public @Nullable InterpretedMemberUse getMemberByName(final String name) {
			for (final InterpretedNativeTupleValueAndEntry e : entries) {
				if (e.entry.name.equals(name))
					return new InterpretedAttributeUse(e.entry);
			}
			return null;
		}
		
		@Override
		public boolean equalsType(final InterpretedTypeUse other) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean isSubtypeOfOrEqual(final InterpretedTypeUse other) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean isSupertypeOfOrEqual(final InterpretedTypeUse other) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public List<InterpretedMemberUse> members() {
			return entries.stream().map(e -> e.entry).collect(Collectors.toList());
		}
		
	}
	
}
