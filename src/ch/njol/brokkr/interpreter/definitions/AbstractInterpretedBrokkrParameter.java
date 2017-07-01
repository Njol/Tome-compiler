package ch.njol.brokkr.interpreter.definitions;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.ast.Interfaces.FormalAttribute;
import ch.njol.brokkr.compiler.ast.Interfaces.FormalParameter;

public abstract class AbstractInterpretedBrokkrParameter extends AbstractInterpretedBrokkrVariable implements InterpretedParameterRedefinition {

	protected final FormalParameter param;
	protected final InterpretedAttributeDefinition attribute;
	
	@SuppressWarnings("null")
	public AbstractInterpretedBrokkrParameter(FormalParameter param) {
		super(param);
		this.param = param;
		this.attribute = param.getParentOfType(FormalAttribute.class).interpreted().definition();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + attribute.hashCode();
		result = prime * result + name.hashCode();
		return result;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractInterpretedBrokkrParameter other = (AbstractInterpretedBrokkrParameter) obj;
		// TODO make sure params and error params are different
		if (!attribute.equals(other.attribute))
			return false;
		if (!name.equals(other.name))
			return false;
		return true;
	}

}
