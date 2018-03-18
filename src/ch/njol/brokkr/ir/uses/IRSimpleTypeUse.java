package ch.njol.brokkr.ir.uses;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTElementPart;
import ch.njol.brokkr.compiler.SourceCodeLinkable;
import ch.njol.brokkr.interpreter.InterpretedTypeUse;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.IRElement;
import ch.njol.brokkr.ir.definitions.IRGenericTypeDefinition;
import ch.njol.brokkr.ir.definitions.IRMemberDefinition;
import ch.njol.brokkr.ir.definitions.IRMemberRedefinition;
import ch.njol.brokkr.ir.definitions.IRTypeDefinition;

/**
 * A type object for "normal" types, i.e. types without special handling (like tuples and "and/or" types).
 */
public class IRSimpleTypeUse extends AbstractIRTypeUse implements SourceCodeLinkable {
	
	private final IRTypeDefinition type;
	private final Map<IRGenericTypeDefinition, IRTypeUse> genericArguments = new HashMap<>();
	
	/**
	 * Creates a simple type object without generic type information
	 * 
	 * @param type
	 */
	public IRSimpleTypeUse(final IRTypeDefinition type) {
		this.type = type;
	}
	
	public IRSimpleTypeUse(final IRTypeDefinition base, final Map<IRGenericTypeDefinition, IRTypeUse> genericArguments) {
		IRElement.assertSameIRContext(Arrays.asList(base), genericArguments.keySet(), genericArguments.values());
		type = base;
		this.genericArguments.putAll(genericArguments);
	}
	
	public IRTypeDefinition getBase() {
		return type;
	}
	
	@Override
	public IRContext getIRContext() {
		return type.getIRContext();
	}
	
	@Override
	public InterpretedTypeUse interpret(final InterpreterContext context) throws InterpreterException {
		throw new InterpreterException("not implemented"); // TODO
	}
	
	@Override
	public Set<? extends IRTypeUse> allInterfaces() {
		// TODO map generic arguments properly
		return type.allInterfaces();
	}
	
	@Override
	public boolean equalsType(final IRTypeUse other) {
		return other instanceof IRSimpleTypeUse && type.equalsType(((IRSimpleTypeUse) other).type) && genericArguments.equals(((IRSimpleTypeUse) other).genericArguments);
	}
	
	@Override
	public int compareTo(final IRTypeUse other) {
		if (other instanceof IRSimpleTypeUse) {
			final IRSimpleTypeUse o = (IRSimpleTypeUse) other;
			final int c = type.compareTo(o.type);
			if (c != 0)
				return c;
			final int c2 = genericArguments.size() - o.genericArguments.size();
			if (c2 != 0)
				return c2;
			final Iterator<Entry<IRGenericTypeDefinition, IRTypeUse>> iter1 = genericArguments.entrySet().iterator(), iter2 = o.genericArguments.entrySet().iterator();
			while (iter1.hasNext() && iter2.hasNext()) {
				final Entry<IRGenericTypeDefinition, IRTypeUse> e1 = iter1.next(), e2 = iter2.next();
				final int c3 = e1.getKey().compareTo(e2.getKey());
				if (c3 != 0)
					return c3;
				final int c4 = e1.getValue().compareTo(e2.getValue());
				if (c4 != 0)
					return c4;
			}
			return iter1.hasNext() ? 1 : iter2.hasNext() ? -1 : 0;
		}
		return IRTypeUse.compareTypeUseClasses(this.getClass(), other.getClass());
	}
	
	@Override
	public int typeHashCode() {
		return type.typeHashCode() * 31 + genericArguments.hashCode();
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
	public @Nullable ASTElementPart getLinked() {
		return type instanceof SourceCodeLinkable ? ((SourceCodeLinkable) type).getLinked() : null;
	}
	
	@Override
	public @Nullable IRMemberUse getMemberByName(final String name) {
		final IRMemberRedefinition memberRedefinition = type.getMemberByName(name);
		if (memberRedefinition == null)
			return null;
		return memberRedefinition.getUse(this, genericArguments);
	}
	
	@Override
	public @Nullable IRMemberUse getMember(final IRMemberDefinition definition) {
		final IRMemberRedefinition memberRedefinition = type.getMember(definition);
		if (memberRedefinition == null)
			return null;
		return memberRedefinition.getUse(this, genericArguments);
	}
	
	@SuppressWarnings("null")
	@Override
	public List<IRMemberUse> members() {
		return type.members().stream().map(m -> m.getUse(this, genericArguments)).filter(m -> m != null).collect(Collectors.toList());
	}
	
	@Override
	public String toString() {
		return type + (genericArguments.isEmpty() ? "" : "<" + String.join(", ", genericArguments.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.toList())) + ">");
	}
	
}
