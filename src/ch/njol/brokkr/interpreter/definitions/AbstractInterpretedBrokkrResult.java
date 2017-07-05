package ch.njol.brokkr.interpreter.definitions;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.ast.Interfaces.FormalAttribute;
import ch.njol.brokkr.compiler.ast.Interfaces.FormalParameter;
import ch.njol.brokkr.compiler.ast.Interfaces.FormalResult;
import ch.njol.brokkr.compiler.ast.Interfaces.FormalVariable;
import ch.njol.brokkr.compiler.ast.Members.NormalResult;

public abstract class AbstractInterpretedBrokkrResult extends AbstractInterpretedBrokkrVariable implements InterpretedResultRedefinition {

	private final InterpretedAttributeRedefinition attribute;

	public AbstractInterpretedBrokkrResult(NormalResult result, InterpretedAttributeRedefinition attribute) {
		super(result);
		this.attribute = attribute;
	}
	

}
