package ch.njol.brokkr.interpreter.definitions;

import org.eclipse.jdt.annotation.NonNull;

import ch.njol.brokkr.compiler.ast.Members.GenericTypeDeclaration;
import ch.njol.brokkr.compiler.ast.TopLevelElements.GenericParameterDeclaration;

public abstract class AbstractInterpretedBrokkrGenericType implements InterpretedGenericTypeRedefinition {

	private final GenericTypeDeclaration declaration;

	public AbstractInterpretedBrokkrGenericType(GenericTypeDeclaration declaration) {
		this.declaration = declaration;
	}
	
	@Override
	public String name() {
		return ""+declaration.name;
	}

}
