package ch.njol.tome.ir;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTElementPart;
import ch.njol.tome.ir.definitions.IRAttributeDefinition;
import ch.njol.tome.ir.definitions.IRParameterRedefinition;
import ch.njol.tome.ir.definitions.IRResultRedefinition;
import ch.njol.tome.ir.definitions.IRTypeDefinition;

public class IRUnknownAttributeDefinition extends AbstractIRUnknown implements IRAttributeDefinition {
	
	private IRTypeDefinition declaringType;
	private final String name;
	
	public IRUnknownAttributeDefinition(IRTypeDefinition declaringType, final String name, final String errorMessage, @Nullable final ASTElementPart location, final IRContext irContext) {
		super(errorMessage, location, irContext);
		this.declaringType = declaringType;
		this.name = name;
	}
	
	public IRUnknownAttributeDefinition(IRTypeDefinition declaringType, final String name, final String errorMessage, final ASTElementPart location) {
		super(errorMessage, location);
		this.declaringType = declaringType;
		this.name = name;
	}
	
	@Override
	public List<IRParameterRedefinition> parameters() {
		return Collections.EMPTY_LIST;
	}
	
	@Override
	public List<IRResultRedefinition> results() {
		return Collections.EMPTY_LIST;
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
		return false;
	}
	
	@Override
	public String name() {
		return name;
	}
	
	@Override
	public boolean isStatic() {
		return false;
	}
	
	@Override
	public IRTypeDefinition declaringType() {
		return declaringType;
	}
	
	@Override
	public @Nullable ASTElementPart getLinked() {
		return null;
	}
	
	@Override
	public String documentation() {
		return "";
	}
	
}
