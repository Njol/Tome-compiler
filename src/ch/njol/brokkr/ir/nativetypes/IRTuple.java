package ch.njol.brokkr.ir.nativetypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.InterpretedNormalObject;
import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpretedTuple;
import ch.njol.brokkr.interpreter.InterpretedTuple.InterpretedTypeTuple;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.ir.AbstractIRElement;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.IRError;
import ch.njol.brokkr.ir.definitions.IRAttributeDefinition;
import ch.njol.brokkr.ir.definitions.IRAttributeImplementation;
import ch.njol.brokkr.ir.definitions.IRAttributeRedefinition;
import ch.njol.brokkr.ir.definitions.IRClassDefinition;
import ch.njol.brokkr.ir.definitions.IRGenericTypeDefinition;
import ch.njol.brokkr.ir.definitions.IRMemberRedefinition;
import ch.njol.brokkr.ir.definitions.IRParameterDefinition;
import ch.njol.brokkr.ir.definitions.IRParameterRedefinition;
import ch.njol.brokkr.ir.definitions.IRResultDefinition;
import ch.njol.brokkr.ir.definitions.IRResultRedefinition;
import ch.njol.brokkr.ir.definitions.IRTypeDefinition;
import ch.njol.brokkr.ir.expressions.IRExpression;
import ch.njol.brokkr.ir.uses.IRAndTypeUse;
import ch.njol.brokkr.ir.uses.IRAttributeUse;
import ch.njol.brokkr.ir.uses.IRClassUse;
import ch.njol.brokkr.ir.uses.IRMemberUse;
import ch.njol.brokkr.ir.uses.IROrTypeUse;
import ch.njol.brokkr.ir.uses.IRTypeUse;

/*
 * TODO tuples must be immutable (TODO why?) (and a value type if possible, since their identity should not matter - e.g. the empty tuple may or may not be the same all the time)
 * immutable pros:
 *   - less misuse instead of using proper objects
 *   - can be used cross-thread
 *   - have no identity, so modifying one would actually only modify the local copy
 *   	- in particular, all non-reference types should be immutable
 *   - nobody owns a tuple (follows from immutability - having read access to a tuple allows one to make a perfect copy, so restricting use is somewhat useless)
 */
public abstract class IRTuple extends AbstractIRElement implements IRExpression {
	
	protected final IRContext irContext;
	
	public final List<IRTupleEntry> entries;
	
	protected IRTuple(final IRContext irContext, final List<IRTupleBuilderEntry> builderEntries) {
		this.irContext = irContext;
		entries = new ArrayList<>();
		for (int i = 0; i < builderEntries.size(); i++) {
			final IRTupleBuilderEntry builderEntry = builderEntries.get(i);
			entries.add(registerDependency(new IRTupleEntry(this, i, builderEntry.name, builderEntry.value)));
		}
	}
	
	public static IRTuple newInstance(final IRContext irContext, final Stream<IRTupleBuilderEntry> entries) {
		return newInstance(irContext, entries.collect(Collectors.toList()));
	}
	
	public static IRTuple newInstance(final IRContext irContext, final List<IRTupleBuilderEntry> entries) {
		if (entries.stream().allMatch(e -> e.value instanceof IRTypeUse)) // TODO is this correct? is this how e.g. a [Int8] is interpreted?
			return new IRTypeTuple(irContext, entries);
		else
			return new IRNormalTuple(irContext, entries);
	}
	
	@Override
	public IRContext getIRContext() {
		return irContext;
	}
	
	@Override
	public String toString() {
		return "[" + String.join(", ", entries.stream().map(e -> e.name + ": " + e.value).collect(Collectors.toList())) + "]";
	}
	
	/**
	 * Creates a new tuple consisting of the given tuple added to the end of this tuple, i.e. the new tuple will have all entries of both tuples.
	 * 
	 * @param other
	 * @return
	 */
	public IRTuple add(final IRTuple other) {
		final IRTupleBuilder builder = new IRTupleBuilder(irContext);
		for (final IRTupleEntry e : entries)
			builder.addEntry(e.name, e.value);
		for (final IRTupleEntry e : other.entries)
			builder.addEntry(e.name, e.value);
		return builder.build();
	}
	
	@Override
	public IRTypeTuple type() {
		return new IRTypeTuple(irContext, entries.stream().map(e -> new IRTupleBuilderEntry(e.name, e.value.type())).collect(Collectors.toList()));
	}
	
	@Override
	public InterpretedTuple interpret(final InterpreterContext context) throws InterpreterException {
		final List<InterpretedObject> values = new ArrayList<>();
		for (final IRTupleEntry e : entries)
			values.add(e.value.interpret(context));
		return new InterpretedTuple(type(), values);
	}
	
	public final static class IRTupleBuilder {
		
		private final IRContext irContext;
		private final List<IRTupleBuilderEntry> entries = new ArrayList<>();
		
		public IRTupleBuilder(final IRContext irContext) {
			this.irContext = irContext;
		}
		
		public IRTupleBuilder addEntry(final String name, final IRExpression value) {
			entries.add(new IRTupleBuilderEntry(name, value));
			return this;
		}
		
		public IRTupleBuilder addEntry(final IRTupleBuilderEntry entry) {
			entries.add(entry);
			return this;
		}
		
		public IRTuple build() {
			return IRTuple.newInstance(irContext, entries);
		}
	}
	
	public final static class IRTypeTupleBuilder {
		
		private final IRContext irContext;
		private final List<IRTupleBuilderEntry> entries = new ArrayList<>();
		
		public IRTypeTupleBuilder(final IRContext irContext) {
			this.irContext = irContext;
		}
		
		public IRTypeTupleBuilder addEntry(final String name, final IRTypeUse value) {
			entries.add(new IRTupleBuilderEntry(name, value));
			return this;
		}
		
		public IRTypeTupleBuilder addEntry(final IRTupleBuilderEntry entry) {
			entries.add(entry);
			return this;
		}
		
		public IRTypeTuple build() {
			return new IRTypeTuple(irContext, entries);
		}
	}
	
	public final static class IRTupleBuilderEntry {
		
		public final String name;
		public final IRExpression value;
		
		/**
		 * @param name The name as given in the source code, or a synthetic name based on the index // TODO define that and maybe make a static utility method for it (or make it
		 *            null?)
		 * @param value
		 */
		public IRTupleBuilderEntry(final String name, final IRExpression value) {
			this.name = name;
			this.value = value;
		}
		
	}
	
	public final static class IRTupleEntry extends AbstractIRElement implements IRAttributeDefinition, IRAttributeImplementation, IRMemberUse {
		
		public final IRTuple tuple;
		public final int index;
		public final String name;
		public final IRExpression value;
		
		public IRTupleEntry(final IRTuple tuple, final int index, final String name, final IRExpression value) {
			this.tuple = tuple; // dependency relation is the other way around
			this.index = index;
			this.name = name;
			this.value = registerDependency(value);
		}
		
		@Override
		public String name() {
			return name;
		}
		
		public IRTypeUse type() {
			return value.type();
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
		private class TupleEntryResult extends AbstractIRElement implements IRResultDefinition {
			@Override
			public String name() {
				return name;
			}
			
			@Override
			public IRContext getIRContext() {
				return IRTupleEntry.this.getIRContext();
			}
			
			@Override
			public IRTypeUse type() {
				return IRTupleEntry.this.type();
			}
			
			@Override
			public IRAttributeRedefinition attribute() {
				return IRTupleEntry.this;
			}
			
			@Override
			public String hoverInfo() {
				return IRTupleEntry.this.hoverInfo();
			}
			
			@Override
			public @Nullable IRExpression defaultValue() {
				return null;
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
		public String documentation() {
			return "Tuple entry #" + index + ": " + type() + " " + name;
		}
		
		@Override
		public @Nullable InterpretedObject interpretImplementation(final InterpretedObject thisObject, final Map<IRParameterDefinition, InterpretedObject> arguments, final boolean allResults) throws InterpreterException {
			if (!(thisObject instanceof IRTuple))
				throw new InterpreterException("Not a tuple");
			for (final IRTupleEntry e : ((IRTuple) thisObject).entries) {
				if (e.equalsMember(this))
					return e.value.interpret(new InterpreterContext(tuple.irContext, (InterpretedNormalObject) null));
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
		public IRTypeDefinition declaringType() {
			return tuple.type().definition;
		}
		
		@Override
		public int hashCode() {
			return memberHashCode();
		}
		
		@Override
		public boolean equals(@Nullable final Object other) {
			return other instanceof IRMemberRedefinition ? equalsMember((IRMemberRedefinition) other) : false;
		}
		
		@Override
		public IRContext getIRContext() {
			return tuple.irContext;
		}
		
	}
	
//	@Override
//	public IRTypeTuple nativeClass() {
//		return new IRTypeTuple(entries.stream().map(e -> new IRNativeTupleValueAndEntry(e.entry.index, e.entry.type.nativeClass(), e.entry.name, e.entry.type)).collect(Collectors.toList()));
//	}
	
	public static class IRNormalTuple extends IRTuple {
		
		public IRNormalTuple(final IRContext irContext, final List<IRTupleBuilderEntry> entries) {
			super(irContext, entries);
		}
		
	}
	
	/**
	 * The definition of a tuple type. This is a synthetic descriptor that is equal for all tuples of the same type.
	 * <p>
	 * TODO define tuple equality and subtyping behaviour (right now no subtyping exists, and tuple types are equal when they types are equal (i.e. names don't matter))
	 */
	public static class IRTypeTupleDefinition extends AbstractIRElement implements IRClassDefinition {
		
		IRTypeTuple typeTuple;
		
		public IRTypeTupleDefinition(final IRTypeTuple typeTuple) {
			this.typeTuple = typeTuple;
		}
		
		@Override
		public List<? extends IRMemberRedefinition> members() {
			return typeTuple.entries;
		}
		
		@Override
		public IRContext getIRContext() {
			return typeTuple.getIRContext();
		}
		
		@Override
		public boolean equalsType(final IRTypeDefinition other) {
			return other instanceof IRTypeTupleDefinition && typeTuple.equalsType(((IRTypeTupleDefinition) other).typeTuple);
		}
		
		@Override
		public int compareTo(final IRTypeDefinition other) {
			if (other instanceof IRTypeTupleDefinition)
				return typeTuple.compareTo(((IRTypeTupleDefinition) other).typeTuple);
			return IRTypeDefinition.compareTypeDefinitionClasses(this.getClass(), other.getClass());
		}
		
		@Override
		public int typeHashCode() {
			return typeTuple.typeHashCode();
		}
		
//		@Override
//		public boolean isSubtypeOfOrEqual(final IRTypeDefinition other) {
//			return other.equalsType(typeTuple.irContext.getTypeDefinition("lang", "Any")) || other.equalsType(typeTuple.irContext.getTypeDefinition("lang", "Tuple"));
//		}
		
		@Override
		public @Nullable IRAttributeImplementation getAttributeImplementation(final IRAttributeDefinition definition) {
			for (final IRTupleEntry e : typeTuple.entries) {
				if (e.equalsMember(definition))
					return e;
			}
			return null;
		}
		
//		@Override
		public IRClassDefinition nativeClass() {
			return new IRTypeTupleDefinition(typeTuple.type());
		}
		
		@Override
		public IRTypeUse getUse(final Map<IRGenericTypeDefinition, IRTypeUse> genericArguments) {
			assert genericArguments.isEmpty();
			return typeTuple;
		}
		
		@Override
		public Set<? extends IRTypeUse> allInterfaces() {
			return new HashSet<>(Arrays.asList(typeTuple.irContext.getTypeUse("lang", "Tuple")));
		}
	}
	
	/**
	 * A tuple whose entries are only types is also a type of tuples whose entries are instances of this one's type entries.
	 * <p>
	 * This is a type use, and its definition is a synthetic {@link IRTypeTupleDefinition} that is shared among equal tuples.
	 */
	public static class IRTypeTuple extends IRTuple implements IRClassUse {
		
		IRTypeTupleDefinition definition;
		
		public IRTypeTuple(final IRContext irContext, final List<IRTupleBuilderEntry> entries) {
			super(irContext, entries);
			definition = new IRTypeTupleDefinition(this);
		}
		
		public IRTypeTuple(final IRContext irContext, final Stream<IRTupleBuilderEntry> entries) {
			this(irContext, entries.collect(Collectors.toList()));
		}
		
		public final static IRTypeTuple emptyTuple(final IRContext irContext) {
			return new IRTypeTuple(irContext, Collections.EMPTY_LIST);
		}
		
		@Override
		public @Nullable IRAttributeImplementation getAttributeImplementation(final IRAttributeDefinition definition) {
			for (final IRTupleEntry e : entries) {
				if (e.equalsMember(definition))
					return e;
			}
			return null;
		}
		
		@Override
		public @Nullable IRMemberUse getMemberByName(final String name) {
			for (final IRTupleEntry e : entries) {
				if (e.name.equals(name))
					return new IRAttributeUse(e);
			}
			return null;
		}
		
		@Override
		public Set<? extends IRTypeUse> allInterfaces() {
			return new HashSet<>(Arrays.asList(irContext.getTypeUse("lang", "Any"), irContext.getTypeUse("lang", "Tuple")));
		}
		
		@Override
		public boolean equalsType(final IRTypeUse other) {
			if (!(other instanceof IRTypeTuple))
				return false;
			final IRTypeTuple otherTuple = (IRTypeTuple) other;
			if (entries.size() != otherTuple.entries.size())
				return false;
			for (int i = 0; i < entries.size(); i++) {
				if (!((IRTypeUse) entries.get(i).value).equalsType((IRTypeUse) otherTuple.entries.get(i).value))
					return false;
			}
			return true;
		}
		
		@Override
		public int compareTo(final IRTypeUse other) {
			if (other instanceof IRAndTypeUse || other instanceof IROrTypeUse)
				return 1;
			if (other instanceof IRTypeTuple) {
				final IRTypeTuple otherTuple = (IRTypeTuple) other;
				for (int i = 0; i < entries.size(); i++) {
					final int c = ((IRTypeUse) entries.get(i).value).compareTo((IRTypeUse) otherTuple.entries.get(i).value);
					if (c != 0)
						return c;
				}
				return entries.size() == otherTuple.entries.size() ? 0 : entries.size() < otherTuple.entries.size() ? 1 : -1;
			}
			return -1;
		}
		
		@Override
		public int typeHashCode() {
			int r = 0;
			for (final IRTupleEntry e : entries) {
				r = r * 31 + ((IRTypeUse) e.value).typeHashCode();
			}
			return r;
		}
		
		@Override
		public boolean isSubtypeOfOrEqual(final IRTypeUse other) {
			// TODO define tuple sub/supertyping
			return other.equalsType(irContext.getTypeUse("lang", "Any")) || other.equalsType(irContext.getTypeUse("lang", "Tuple"));
		}
		
		@Override
		public boolean isSupertypeOfOrEqual(final IRTypeUse other) {
			return false;
		}
		
		@Override
		public List<? extends IRMemberUse> members() {
			return entries;
		}
		
		public IRTypeTuple add(final IRTypeTuple other) {
			return (IRTypeTuple) add((IRTuple) other);
		}
		
		@Override
		public InterpretedTypeTuple interpret(final InterpreterContext context) throws InterpreterException {
			final List<InterpretedObject> values = new ArrayList<>();
			for (final IRTupleEntry e : entries)
				values.add(e.value.interpret(context));
			return new InterpretedTypeTuple(type(), values);
		}
		
	}
	
}
