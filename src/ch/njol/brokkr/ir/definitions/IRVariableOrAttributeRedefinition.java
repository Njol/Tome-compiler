package ch.njol.brokkr.ir.definitions;

import ch.njol.brokkr.common.HoverInfo;
import ch.njol.brokkr.ir.IRElement;
import ch.njol.brokkr.ir.uses.IRTypeUse;

public interface IRVariableOrAttributeRedefinition extends HoverInfo, IRElement {
	
	IRTypeUse mainResultType();
	
}
