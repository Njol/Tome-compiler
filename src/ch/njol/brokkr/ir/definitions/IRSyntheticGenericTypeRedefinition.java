package ch.njol.brokkr.ir.definitions;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTElementPart;
import ch.njol.brokkr.ir.AbstractIRElement;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.IRElement;
import ch.njol.brokkr.ir.uses.IRTypeUse;

public class IRSyntheticGenericTypeRedefinition extends AbstractIRElement implements IRGenericTypeRedefinition {
	
	private final IRGenericTypeRedefinition parent;
	private final IRTypeDefinition forType;
	private final IRTypeUse knownExactType;
	
	public IRSyntheticGenericTypeRedefinition(final IRGenericTypeRedefinition parent, final IRTypeDefinition forType, final IRTypeUse knownExactType) {
		IRElement.assertSameIRContext(parent, knownExactType);
		this.parent = registerDependency(parent);
		this.forType = registerDependency(forType);
		this.knownExactType = registerDependency(knownExactType);
	}
	
	@Override
	public String name() {
		return parent.name();
	}
	
	@Override
	public IRTypeDefinition declaringType() {
		return forType;
	}
	
	@Override
	public @Nullable ASTElementPart getLinked() {
		return parent.getLinked(); // TODO maybe even link to the use in the parent type declaration
	}
	
	@Override
	public IRContext getIRContext() {
		return parent.getIRContext();
	}
	
	@Override
	public IRGenericTypeDefinition definition() {
		return parent.definition();
	}
	
	@Override
	public @NonNull IRGenericTypeRedefinition parentRedefinition() {
		return parent;
	}
	
	@Override
	public IRTypeUse upperBound() {
		return knownExactType;
	}
	
	@Override
	public String toString() {
		return name() + " [" + knownExactType + "]";
	}
	
}
