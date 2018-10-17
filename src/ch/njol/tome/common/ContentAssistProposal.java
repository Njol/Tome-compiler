package ch.njol.tome.common;

import ch.njol.tome.ir.IRElement;

public class ContentAssistProposal {
	
	private final IRElement elementToShow;
	private final String replacementString;
	
	public ContentAssistProposal(final IRElement elementToShow, final String replacementString) {
		this.elementToShow = elementToShow;
		this.replacementString = replacementString;
	}
	
	public IRElement getElementToShow() {
		return elementToShow;
	}
	
	public String getReplacementString() {
		return replacementString;
	}
	
}
