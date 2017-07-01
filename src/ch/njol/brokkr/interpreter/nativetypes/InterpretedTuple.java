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
import ch.njol.brokkr.interpreter.uses.InterpretedClassObject;
import ch.njol.brokkr.interpreter.uses.InterpretedMemberUse;
import ch.njol.brokkr.interpreter.uses.InterpretedTypeObject;
import ch.njol.brokkr.interpreter.uses.InterpretedTypeUse;

public abstract class InterpretedTuple implements InterpretedNativeObject {
	
	List<InterpretedNativeTupleValueAndEntry> entries;
	
	protected InterpretedTuple(final List<InterpretedNativeTupleValueAndEntry> entries) {
		this.entries = entries;
	}
	
	@SuppressWarnings("null")
	public static InterpretedTuple newInstance(final Stream<InterpretedNativeTupleValueAndEntry> entries) {
		final List<InterpretedNativeTupleValueAndEntry> entriesList = entries.collect(Collectors.toList());
		if (entries.allMatch(e -> e.value instanceof InterpretedNativeTypeDefinition)) // TODO is this correct? is this how e.g. a [Int8] is interpreted?
			return new InterpretedTypeTuple(entriesList);
		else
			return new InterpretedNormalTuple(entriesList);
	}
	
	public static class InterpretedNativeTupleValueAndEntry {
		
		private final InterpretedTupleEntry entry;
		private final InterpretedObject value;
		
		public InterpretedNativeTupleValueAndEntry(final InterpretedTupleEntry entry, final InterpretedObject value) {
			this.entry = entry;
			this.value = value;
		}
		
		public InterpretedNativeTupleValueAndEntry(final int index, final InterpretedTypeObject type, final String name, final InterpretedObject value) {
			entry = new InterpretedTupleEntry(index, type, name);
			this.value = value;
		}
		
	}
	
	public static class InterpretedTupleEntry implements InterpretedAttributeDefinition, InterpretedAttributeImplementation {
		
		private final int index;
		private final InterpretedTypeObject type;
		private final String name;
		
		public InterpretedTupleEntry(final int index, final InterpretedTypeObject type, final String name) {
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
		public List<InterpretedParameterRedefinition> parameters() {
			return Collections.EMPTY_LIST;
		}
		
		@SuppressWarnings("null")
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
		
		@SuppressWarnings("null")
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
				if (e.entry.equalsAttribute(this))
					return e.value;
			}
			throw new InterpreterException("Invalid tuple entry");
		}
		
		@Override
		public boolean equalsAttribute(final InterpretedAttributeDefinition other) {
			if (!(other instanceof InterpretedTupleEntry))
				return false;
			final InterpretedTupleEntry e = (InterpretedTupleEntry) other;
			return e.name.equals(name) && e.index == index && e.type.equalsType(type);
		}
		
	}
	
	@Override
	public InterpretedClassObject nativeClass() {
		return new InterpretedTypeTuple(entries.stream().map(e -> new InterpretedNativeTupleValueAndEntry(e.entry.index, e.entry.type.nativeClass(), e.entry.name, e.entry.type)).collect(Collectors.toList()));
	}
	
	public static class InterpretedNormalTuple extends InterpretedTuple {
		
		public InterpretedNormalTuple(final List<InterpretedNativeTupleValueAndEntry> entries) {
			super(entries);
		}
		
	}
	
	/**
	 * The type of a type tuple
	 */
	public static class InterpretedTupleType implements InterpretedNativeTypeDefinition {
		
		private final InterpretedTypeTuple tuple;
		
		public InterpretedTupleType(final InterpretedTypeTuple tuple) {
			this.tuple = tuple;
		}
		
		@Override
		public List<? extends InterpretedMemberRedefinition> members() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public boolean equalsType(final InterpretedNativeTypeDefinition other) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean isSubtypeOfOrEqual(final InterpretedNativeTypeDefinition other) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean isSupertypeOfOrEqual(final InterpretedNativeTypeDefinition other) {
			// TODO Auto-generated method stub
			return false;
		}
		
	}
	
	/**
	 * A tuple whose entries are only types
	 */
	public static class InterpretedTypeTuple extends InterpretedTuple implements InterpretedClassObject {
		
		public InterpretedTypeTuple(final List<InterpretedNativeTupleValueAndEntry> entries) {
			super(entries);
		}
		
		@Override
		public @Nullable InterpretedAttributeImplementation getAttributeImplementation(final InterpretedAttributeDefinition definition) {
			for (final InterpretedNativeTupleValueAndEntry e : entries) {
				if (e.entry.equalsAttribute(definition))
					return e.entry;
			}
			return null;
		}
		
		@Override
		public @Nullable InterpretedMemberUse getMemberByName(final String name) {
			for (final InterpretedNativeTupleValueAndEntry e : entries) {
				if (e.entry.name.equals(name))
					return new InterpretedAttributeUse(e.entry, null, null);
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
		
		@SuppressWarnings("null")
		@Override
		public List<InterpretedMemberRedefinition> members() {
			return entries.stream().map(e -> e.entry).collect(Collectors.toList());
		}
		
		@Override
		public InterpretedTypeTuple typeType() {
			return new InterpretedTypeTuple(entries.stream().map(e -> new InterpretedNativeTupleValueAndEntry(e.entry.index, (@NonNull InterpretedTypeObject) e.entry.type.typeType(), e.entry.name, e.entry.type)).collect(Collectors.toList()));
		}

		@Override
		public @NonNull InterpretedNativeClassDefinition getBase() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
}
