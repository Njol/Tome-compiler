package ch.njol.brokkr.ir.nativetypes;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeObject;
import ch.njol.brokkr.ir.IRError;
import ch.njol.brokkr.ir.definitions.IRAttributeDefinition;
import ch.njol.brokkr.ir.definitions.IRAttributeImplementation;
import ch.njol.brokkr.ir.definitions.IRAttributeRedefinition;
import ch.njol.brokkr.ir.definitions.IRClassDefinition;
import ch.njol.brokkr.ir.definitions.IRMemberRedefinition;
import ch.njol.brokkr.ir.definitions.IRParameterDefinition;
import ch.njol.brokkr.ir.definitions.IRParameterRedefinition;
import ch.njol.brokkr.ir.definitions.IRResultDefinition;
import ch.njol.brokkr.ir.definitions.IRResultRedefinition;
import ch.njol.brokkr.ir.definitions.IRTypeDefinition;
import ch.njol.brokkr.ir.uses.IRAttributeUse;
import ch.njol.brokkr.ir.uses.IRClassUse;
import ch.njol.brokkr.ir.uses.IRMemberUse;
import ch.njol.brokkr.ir.uses.IRTypeUse;

// TODO tuples must be immutable (and a value type if possible, since their identity should not matter - e.g. the empty tuple may or may not be the same all the time)
public abstract class IRTuple implements InterpretedNativeObject {
	
	public final List<IRNativeTupleValueAndEntry> entries;
	
	protected IRTuple(final List<IRNativeTupleValueAndEntry> entries) {
		this.entries = entries;
		entries.forEach(e -> e.entry.tuple = this);
	}
	
	public static IRTuple newInstance(final Stream<IRNativeTupleValueAndEntry> entries) {
		final List<IRNativeTupleValueAndEntry> entriesList = entries.collect(Collectors.toList());
		if (entries.allMatch(e -> e.value instanceof IRTypeDefinition)) // TODO is this correct? is this how e.g. a [Int8] is interpreted?
			return new IRTypeTuple(entriesList);
		else
			return new IRNormalTuple(entriesList);
	}
	
	@Override
	public String toString() {
		return "[" + String.join(", ", entries.stream().map(e -> e.entry.name + ": " + e.value).collect(Collectors.toList())) + "]";
	}
	
	public static class IRNativeTupleValueAndEntry {
		
		public final IRTupleEntry entry;
		public final InterpretedObject value;
		
		public IRNativeTupleValueAndEntry(final IRTupleEntry entry, final InterpretedObject value) {
			this.entry = entry;
			this.value = value;
		}
		
		public IRNativeTupleValueAndEntry(final int index, final IRTypeUse type, final String name, final InterpretedObject value) {
			entry = new IRTupleEntry(index, type, name);
			this.value = value;
		}
		
	}
	
	public static class IRTupleEntry implements IRAttributeDefinition, IRAttributeImplementation, IRMemberUse {
		
		public @Nullable IRTuple tuple;
		public final int index;
		public final IRTypeUse type;
		public final String name;
		
		/**
		 * @param index
		 * @param type
		 * @param name The name as given in the source code, or a synthetic name based on the index // TODO define that and maybe make a static util emthod for it
		 */
		public IRTupleEntry(final int index, final IRTypeUse type, final String name) {
			this.index = index;
			this.type = type;
			this.name = name;
		}
		
		@Override
		public String name() {
			return name;
		}
		
		@Override
		public List<IRParameterRedefinition> parameters() {
			return Collections.EMPTY_LIST;
		}
		
		@Override
		public List<IRResultRedefinition> results() {
			return Collections.singletonList(new TupleEntryResult());
		}
		
		// TODO make better, and think of how to make definitions and redefinitions for tuples
		private class TupleEntryResult implements IRResultDefinition {
			@Override
			public String name() {
				return name;
			}
			
			@Override
			public IRTypeUse type() {
				return type;
			}
			
			@Override
			public IRAttributeRedefinition attribute() {
				return IRTupleEntry.this;
			}
		}
		
		@Override
		public List<IRError> errors() {
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
		public @Nullable InterpretedObject interpretImplementation(final InterpretedObject thisObject, final Map<IRParameterDefinition, InterpretedObject> arguments, final boolean allResults) {
			if (!(thisObject instanceof IRTuple))
				throw new InterpreterException("Not a tuple");
			for (final IRNativeTupleValueAndEntry e : ((IRTuple) thisObject).entries) {
				if (e.entry.equalsMember(this))
					return e.value;
			}
			throw new InterpreterException("Invalid tuple entry");
		}
		
		@Override
		public IRMemberRedefinition redefinition() {
			return this;
		}
		
		@Override
		public @NonNull IRAttributeDefinition definition() {
			return this;
		}
		
		@Override
		public boolean isStatic() {
			return false;
		}
		
		@Override
		public @NonNull IRTypeDefinition declaringType() {
			if (tuple != null)
				return tuple.nativeClass().definition;
			throw new IllegalStateException(); // tuple entries are invalid if not in a tuple
		}
		
		@Override
		public int hashCode() {
			return memberHashCode();
		}
		
		@Override
		public boolean equals(@Nullable final Object other) {
			return other instanceof IRMemberRedefinition ? equalsMember((IRMemberRedefinition) other) : false;
		}
		
	}
	
	@Override
	public IRTypeTuple nativeClass() {
		return new IRTypeTuple(entries.stream().map(e -> new IRNativeTupleValueAndEntry(e.entry.index, e.entry.type.nativeClass(), e.entry.name, e.entry.type)).collect(Collectors.toList()));
	}
	
	public static class IRNormalTuple extends IRTuple {
		
		public IRNormalTuple(final List<IRNativeTupleValueAndEntry> entries) {
			super(entries);
		}
		
	}
	
	/**
	 * The definition of a tuple type. This is a synthetic descriptor that is equal for all tuple of the same type.
	 * <p>
	 * TODO define tuple equality and subtyping behaviour
	 */
	public static class IRTypeTupleDefinition implements IRClassDefinition {
		
		IRTypeTuple typeTuple;
		
		public IRTypeTupleDefinition(final IRTypeTuple typeTuple) {
			this.typeTuple = typeTuple;
		}
		
		@Override
		public List<? extends IRMemberRedefinition> members() {
			return typeTuple.entries.stream().map(e -> e.entry).collect(Collectors.toList());
		}
		
		@Override
		public boolean equalsType(final IRTypeDefinition other) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public int typeHashCode() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public boolean isSubtypeOfOrEqual(final IRTypeDefinition other) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public @Nullable IRAttributeImplementation getAttributeImplementation(final IRAttributeDefinition definition) {
			for (final IRNativeTupleValueAndEntry e : typeTuple.entries) {
				if (e.entry.equalsMember(definition))
					return e.entry;
			}
			return null;
		}
		
		@Override
		public IRClassDefinition nativeClass() {
			return new IRTypeTupleDefinition(typeTuple.nativeClass());
		}
	}
	
	/**
	 * A tuple whose entries are only types is also a type of tuples whose entries are instances of this one's type entries.
	 * <p>
	 * This is a type use, and its definition is a synthetic {@link IRTypeTupleDefinition} that is shared among equal tuples.
	 */
	public static class IRTypeTuple extends IRTuple implements IRClassUse {
		
		IRTypeTupleDefinition definition;
		
		public IRTypeTuple(final List<IRNativeTupleValueAndEntry> entries) {
			super(entries);
			definition = new IRTypeTupleDefinition(this);
		}
		
		@Override
		public @Nullable IRAttributeImplementation getAttributeImplementation(final IRAttributeDefinition definition) {
			for (final IRNativeTupleValueAndEntry e : entries) {
				if (e.entry.equalsMember(definition))
					return e.entry;
			}
			return null;
		}
		
		@Override
		public @Nullable IRMemberUse getMemberByName(final String name) {
			for (final IRNativeTupleValueAndEntry e : entries) {
				if (e.entry.name.equals(name))
					return new IRAttributeUse(e.entry);
			}
			return null;
		}
		
		@Override
		public boolean equalsType(final IRTypeUse other) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean isSubtypeOfOrEqual(final IRTypeUse other) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean isSupertypeOfOrEqual(final IRTypeUse other) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public List<IRMemberUse> members() {
			return entries.stream().map(e -> e.entry).collect(Collectors.toList());
		}
		
	}
	
}
