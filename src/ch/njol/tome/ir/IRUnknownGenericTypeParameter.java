package ch.njol.tome.ir;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTElementPart;
import ch.njol.tome.ir.definitions.IRGenericTypeDefinition;
import ch.njol.tome.ir.definitions.IRGenericTypeParameter;
import ch.njol.tome.ir.definitions.IRTypeDefinition;

public class IRUnknownGenericTypeParameter extends AbstractIRUnknown implements IRGenericTypeParameter {
	
	private final IRTypeDefinition declaringType;
	private final String name;
	
	public IRUnknownGenericTypeParameter(final IRTypeDefinition declaringType, final String name, final String errorMessage, final @Nullable ASTElementPart location, final IRContext irContext) {
		super(errorMessage, location, irContext);
		this.declaringType = declaringType;
		this.name = name;
	}
	
	@Override
	public IRTypeDefinition declaringType() {
		return declaringType;
	}
	
	@Override
	public String name() {
		return name;
	}
	
	@Override
	public IRGenericTypeDefinition definition() {
		return new IRUnknownGenericTypeDefinition("", getLocation(), name, declaringType);
	}
	
}
