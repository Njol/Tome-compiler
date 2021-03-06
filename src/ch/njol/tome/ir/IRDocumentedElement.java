package ch.njol.tome.ir;

import ch.njol.tome.common.HoverInfo;

public interface IRDocumentedElement extends IRElement, HoverInfo {
	
	/**
	 * TODO return something better than a plain String
	 * 
	 * @return A string documenting this element. Should always include the name/ID of this element, preferably at the start.
	 */
	public String documentation();
	
	@Override
	default String hoverInfo() {
		return documentation();
	}
	
}
