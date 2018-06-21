package ch.njol.brokkr.ir.definitions;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTElementPart;
import ch.njol.brokkr.ir.AbstractIRUnknown;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.uses.IRTypeUse;
import ch.njol.brokkr.ir.uses.IRUnknownTypeUse;

public class IRUnknownGenericTypeDefinition extends AbstractIRUnknown implements IRGenericTypeDefinition {
	
	private final String name;
	
	public IRUnknownGenericTypeDefinition(final IRContext irContext, final String name, final String errorMessage, final @Nullable ASTElementPart location) {
		super(errorMessage, location, irContext);
		this.name = name;
	}
	
	@Override
	public IRTypeUse upperBound() {
		return new IRUnknownTypeUse(getIRContext());
	}
	
	@Override
	public String name() {
		return name;
	}
	
	@Override
	public IRTypeDefinition declaringType() {
		return new IRUnknownTypeDefinition(getIRContext(), errorMessage, location);
	}
	
	@Override
	public @Nullable ASTElementPart getLinked() {
		return location;
	}
	
	@Override
	public String documentation() {
		return "Unknown generic type "+name;
	}
	
}
