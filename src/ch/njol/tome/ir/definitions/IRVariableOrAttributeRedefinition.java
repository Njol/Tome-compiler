package ch.njol.tome.ir.definitions;

import ch.njol.tome.common.HoverInfo;
import ch.njol.tome.ir.IRElement;
import ch.njol.tome.ir.uses.IRTypeUse;

public interface IRVariableOrAttributeRedefinition extends HoverInfo, IRElement {
	
	IRTypeUse mainResultType();
	
}
