package ch.njol.tome.ir;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTElementPart;
import ch.njol.tome.compiler.Token.WordToken;
import ch.njol.tome.ir.definitions.IRAttributeDefinition;
import ch.njol.tome.ir.definitions.IRGenericTypeDefinition;
import ch.njol.tome.ir.definitions.IRTypeDefinition;
import ch.njol.tome.ir.uses.IRMemberUse;
import ch.njol.tome.ir.uses.IRTypeUse;

public class IRUnknownGenericTypeDefinition extends AbstractIRUnknown implements IRGenericTypeDefinition {
	
	private final String name;
	private IRTypeDefinition declaringType;
	
	public IRUnknownGenericTypeDefinition(final String errorMessage, final WordToken name, IRTypeDefinition declaringType) {
		super(errorMessage, name);
		this.name = name.word;
		this.declaringType = declaringType;
	}
	
	public IRUnknownGenericTypeDefinition(final String errorMessage, final @Nullable ASTElementPart location, String name, IRTypeDefinition declaringType) {
		super(errorMessage, location, declaringType.getIRContext());
		this.name = name;
		this.declaringType = declaringType;
	}
	
	@Override
	public @Nullable IRTypeUse extends_() {
		return null;
	}
	
	@Override
	public @Nullable IRTypeUse super_() {
		return null;
	}
	
	@Override
	public String name() {
		return name;
	}
	
	@Override
	public boolean isStatic() {
		return true;
	}
	
	@Override
	public IRTypeDefinition declaringType() {
		return declaringType;
	}
	
	@Override
	public @Nullable IRMemberUse getUse(@Nullable final IRTypeUse targetType, final Map<IRAttributeDefinition, IRTypeUse> genericArguments) {
		return null;
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
