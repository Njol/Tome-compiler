package ch.njol.brokkr.ir;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTElementPart;
import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.ir.definitions.IRAttributeRedefinition;
import ch.njol.brokkr.ir.definitions.IRParameterDefinition;
import ch.njol.brokkr.ir.uses.IRTypeUse;

public class IRUnknownParameterDefinition extends AbstractIRUnknown implements IRParameterDefinition {
	
	private final String name;
	private final IRTypeUse type;
	private final IRAttributeRedefinition attribute;
	
	public IRUnknownParameterDefinition(final String name, final IRTypeUse type, final IRAttributeRedefinition attribute, final String errorMessage, final ASTElementPart location) {
		super(errorMessage, location);
		IRElement.assertSameIRContext(type, attribute);
		this.name = name;
		this.type = type;
		this.attribute = attribute;
	}
	
	@Override
	public String name() {
		return name;
	}
	
	@Override
	public IRTypeUse type() {
		return type;
	}
	
	@Override
	public IRContext getIRContext() {
		return type.getIRContext();
	}
	
	@Override
	public @Nullable InterpretedObject defaultValue(final InterpreterContext context) throws InterpreterException {
		return null;
	}
	
	@Override
	public IRAttributeRedefinition attribute() {
		return attribute;
	}
	
	@Override
	public String hoverInfo() {
		return errorMessage;
	}
	
}
