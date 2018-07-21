package ch.njol.tome.parser;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTElement;
import ch.njol.tome.compiler.Token.WhitespaceOrCommentToken;

public class AttachedElementParser<E extends ASTElement> extends ElementParser<E> implements AttachedParser {
	
	protected final ASTParser parent;
	
	public AttachedElementParser(final DocumentParser<E> parent, E document) {
		super(parent.in, document);
		this.parent = parent;
	}
	
	public AttachedElementParser(final Parser parent, E ast) {
		super(parent.in, ast);
		this.parent = parent;
	}
	
	@Override
	public ASTParser parent() {
		assert isValid();
		return parent;
	}
	
	public void done(@Nullable final AttachedParser newParser) {
		assert isValid();
		final AttachedElementParser<?> oldChild = currentChild;
		if (oldChild != null)
			oldChild.done(newParser);
		parent.fatalParseErrors().addAll(fatalParseErrors());
		if (newParser != null) { // new element started, add remaining whitespace to parent of that element
			// parent of new must be a normal parser, as the only parser with another parent, the "root" parser, newer gets any siblings.
			((Parser) newParser.parent()).addWhitespace(bufferedWhitespace);
			bufferedWhitespace.clear();
		} else { // top-level element done, add remaining whitespace to that element
			for (final WhitespaceOrCommentToken t : bufferedWhitespace)
				addChildToAST(t);
			bufferedWhitespace.clear();
		}
		invalidate();
	}
	
}
