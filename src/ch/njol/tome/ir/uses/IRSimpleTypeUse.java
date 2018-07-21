package ch.njol.tome.ir.uses;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTElementPart;
import ch.njol.tome.compiler.SourceCodeLinkable;
import ch.njol.tome.interpreter.InterpretedTypeUse;
import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.InterpreterException;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.definitions.IRMemberDefinition;
import ch.njol.tome.ir.definitions.IRMemberRedefinition;
import ch.njol.tome.ir.definitions.IRTypeDefinition;

/**
 * A type object for "normal" types, i.e. types without special handling (like tuples and "and/or" types).
 */
public class IRSimpleTypeUse extends AbstractIRTypeUse implements SourceCodeLinkable {
	
	private final IRTypeDefinition typeDefinition;
	
	/**
	 * Creates a simple type object without generic type information
	 * 
	 * @param baseType
	 */
	public IRSimpleTypeUse(final IRTypeDefinition typeDefinition) {
		this.typeDefinition = typeDefinition;
	}
	
	public IRTypeDefinition getDefinition() {
		return typeDefinition;
	}
	
	@Override
	public IRContext getIRContext() {
		return typeDefinition.getIRContext();
	}
	
	@Override
	public InterpretedTypeUse interpret(final InterpreterContext context) throws InterpreterException {
		throw new InterpreterException("not implemented"); // TODO
	}
	
	@Override
	public Set<? extends IRTypeUse> allInterfaces() {
		return typeDefinition.allInterfaces();
	}
	
	@Override
	public boolean equalsType(final IRTypeUse other) {
		return other instanceof IRSimpleTypeUse && typeDefinition.equalsType(((IRSimpleTypeUse) other).typeDefinition);
	}
	
	@Override
	public int compareTo(final IRTypeUse other) {
		if (other instanceof IRSimpleTypeUse) {
			final IRSimpleTypeUse o = (IRSimpleTypeUse) other;
			return typeDefinition.compareTo(o.typeDefinition);
		}
		return IRTypeUse.compareTypeUseClasses(this.getClass(), other.getClass());
	}
	
	@Override
	public int typeHashCode() {
		return typeDefinition.typeHashCode();
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
		return typeDefinition instanceof SourceCodeLinkable ? ((SourceCodeLinkable) typeDefinition).getLinked() : null;
	}
	
	@Override
	public @Nullable IRMemberUse getMemberByName(final String name) {
		final IRMemberRedefinition memberRedefinition = typeDefinition.getMemberByName(name);
		if (memberRedefinition == null)
			return null;
		return memberRedefinition.getUse(this, Collections.emptyMap());
	}
	
	@Override
	public @Nullable IRMemberUse getMember(final IRMemberDefinition definition) {
		final IRMemberRedefinition memberRedefinition = typeDefinition.getMember(definition);
		if (memberRedefinition == null)
			return null;
		return memberRedefinition.getUse(this, Collections.emptyMap());
	}
	
	@Override
	public List<IRMemberUse> members() {
		return typeDefinition.members().stream().map(m -> m.getUse(this, Collections.emptyMap())).filter(m -> m != null).collect(Collectors.toList());
	}
	
	@Override
	public String toString() {
		return typeDefinition.toString();
	}
	
}
