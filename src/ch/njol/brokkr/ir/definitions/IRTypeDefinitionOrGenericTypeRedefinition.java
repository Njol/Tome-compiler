package ch.njol.brokkr.ir.definitions;

import java.util.Map;

import ch.njol.brokkr.ir.IRElement;
import ch.njol.brokkr.ir.uses.IRTypeUse;

public interface IRTypeDefinitionOrGenericTypeRedefinition extends IRElement {
	
	IRTypeUse getUse(Map<IRGenericTypeDefinition, IRTypeUse> genericArguments);
	
}