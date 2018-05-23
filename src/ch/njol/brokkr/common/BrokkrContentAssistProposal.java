package ch.njol.brokkr.common;

import ch.njol.brokkr.ir.IRElement;

public class BrokkrContentAssistProposal {
	
	private final IRElement elementToShow;
	private final String replacementString;
	
	public BrokkrContentAssistProposal(final IRElement elementToShow, final String replacementString) {
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
