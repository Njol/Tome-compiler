package ch.njol.tome.parser;

import java.util.ArrayList;
import java.util.List;

import ch.njol.tome.ast.ASTElement;
import ch.njol.tome.ast.ASTElementPart;
import ch.njol.tome.compiler.Token;
import ch.njol.tome.compiler.Token.WhitespaceOrCommentToken;

/**
 * A parser fort an as-of-yet unknown element type. It will simply collect any parsed tokens and element into a list and will correctly add them to the element
 */
public class UnknownParser extends Parser implements AttachedParser {
	
	private Parser parent;
	private boolean valid = true;
	
	private final List<ASTElementPart> parts = new ArrayList<>();
	
	public UnknownParser(final Parser parent) {
		super(parent.in);
		this.parent = parent;
	}
	
	/**
	 * Starts parsing an unknown element as a parent of this unknown element
	 * 
	 * @return
	 */
	public UnknownParser startUnknownParent() {
		final UnknownParser newParent = new UnknownParser(parent);
		parent = newParent;
		return newParent;
	}
	
	private void toParser(final ElementParser<?> p, final ASTElement ast) {
		// add ending whitespace to new parser
		for (int i = parts.size() - 1; i >= 0; i--) {
			final ASTElementPart part = parts.get(i);
			if (!(part instanceof WhitespaceOrCommentToken))
				break;
			p.addWhitespace((WhitespaceOrCommentToken) part);
			parts.remove(i);
		}
		
		// add all other tokens to element that replaces this parser (in the correct order)
		final List<? extends ASTElementPart> astParts = ast.parts();
		int j = 0;
		for (final ASTElementPart part : parts) {
			final ASTElementPart astPart = astParts.get(j);
			if (part != astPart) {
				assert part instanceof Token;
				ast.insertChild(part, j);
			}
			j++;
		}
		assert j == astParts.size();
		
		valid = false;
	}
	
	public <T extends ASTElement> AttachedElementParser<T> toElement(final T ast) {
		assert valid;
		final AttachedElementParser<T> p = parent.startChild(ast);
		toParser(p, ast);
		return p;
	}
	
	public <T extends ASTElement> DetachedElementParser<T> toDetached(final T ast) {
		assert valid;
		final DetachedElementParser<T> p = parent.startDetached(ast);
		toParser(p, ast);
		return p;
	}

	/**
	 * @return
	 * @throws ClassCastException if you call this incorrectly
	 * @throws IndexOutOfBoundsException if you call this incorrectly
	 */
	public AttachedElementParser<?> toOnlyChildElement() {
		assert valid;
		final ASTElement element = (ASTElement) parts.get(0);
		final AttachedElementParser<?> p = parent.startChild(element);
		for (int i = 1; i < parts.size(); i++) {
			p.addWhitespace((WhitespaceOrCommentToken) parts.get(i));
		}
		valid = false;
		return p;
	}

	/**
	 * @return
	 * @throws ClassCastException if you call this incorrectly
	 * @throws IndexOutOfBoundsException if you call this incorrectly
	 */
	public <T extends ASTElement> AttachedElementParser<T> toOnlyChildElement(final T expectedOnlyChild) {
		assert valid;
		assert expectedOnlyChild == parts.get(0);
		final AttachedElementParser<T> p = parent.startChild(expectedOnlyChild);
		for (int i = 1; i < parts.size(); i++) {
			p.addWhitespace((WhitespaceOrCommentToken) parts.get(i));
		}
		valid = false;
		return p;
	}
	
	/**
	 * @return
	 * @throws ClassCastException if you call this incorrectly
	 * @throws IndexOutOfBoundsException if you call this incorrectly
	 */
	public DetachedElementParser<?> toOnlyChildElementDetached() {
		assert valid;
		final ASTElement element = (ASTElement) parts.get(0);
		final DetachedElementParser<?> p = parent.startDetached(element);
		for (int i = 1; i < parts.size(); i++) {
			p.addWhitespace((WhitespaceOrCommentToken) parts.get(i));
		}
		valid = false;
		return p;
	}
	
	/**
	 * @return
	 * @throws ClassCastException if you call this incorrectly
	 * @throws IndexOutOfBoundsException if you call this incorrectly
	 */
	public <T extends ASTElement> DetachedElementParser<T> toOnlyChildElementDetached(final T expectedOnlyChild) {
		assert valid;
		assert expectedOnlyChild == parts.get(0);
		final DetachedElementParser<T> p = parent.startDetached(expectedOnlyChild);
		for (int i = 1; i < parts.size(); i++) {
			p.addWhitespace((WhitespaceOrCommentToken) parts.get(i));
		}
		valid = false;
		return p;
	}
	
	@Override
	public void addChildToAST(final ASTElementPart child) {
		assert valid;
		parts.add(child);
	}
	
	@Override
	public ASTParser parent() {
		assert valid;
		return parent;
	}
	
	@Override
	protected void addWhitespace(final WhitespaceOrCommentToken t) {
		assert valid;
		parts.add(t);
	}
	
	@Override
	protected void addWhitespace(final List<WhitespaceOrCommentToken> tokens) {
		assert valid;
		parts.addAll(tokens);
	}
	
	@Override
	protected void finalize() throws Throwable {
		assert !valid || parts.isEmpty(); // either used and replaced, or not used at all
		super.finalize();
	}
	
}
