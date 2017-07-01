package ch.njol.brokkr.interpreter.definitions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;

import ch.njol.brokkr.compiler.ast.Interfaces.FormalResult;
import ch.njol.brokkr.compiler.ast.Members.AttributeDeclaration;
import ch.njol.brokkr.compiler.ast.Members.Member;
import ch.njol.brokkr.compiler.ast.Members.SimpleParameter;
import ch.njol.brokkr.data.MethodModifiability;

public class InterpretedBrokkrAttributeRedefinition extends AbstractInterpretedBrokkrAttribute {

	public InterpretedBrokkrAttributeRedefinition(AttributeDeclaration declaration) {
		super(declaration);
	}
	
}
