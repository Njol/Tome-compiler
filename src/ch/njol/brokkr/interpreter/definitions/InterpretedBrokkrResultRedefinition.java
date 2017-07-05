package ch.njol.brokkr.interpreter.definitions;

import ch.njol.brokkr.compiler.ast.Members.NormalResult;

public class InterpretedBrokkrResultRedefinition extends AbstractInterpretedBrokkrResult {
	
	private final InterpretedResultRedefinition overridden;
	
	public InterpretedBrokkrResultRedefinition(final NormalResult result, final InterpretedResultRedefinition overridden, final InterpretedAttributeRedefinition attribute) {
		super(result, attribute);
		this.overridden = overridden;
	}
	
	@Override
	public InterpretedResultDefinition definition() {
		return overridden.definition();
	}
	
}
