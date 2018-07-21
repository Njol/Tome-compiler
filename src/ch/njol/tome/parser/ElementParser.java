package ch.njol.tome.parser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTElement;
import ch.njol.tome.ast.ASTElementPart;
import ch.njol.tome.compiler.Token.WhitespaceOrCommentToken;
import ch.njol.tome.util.TokenListStream;

public abstract class ElementParser<E extends ASTElement> extends Parser {
	
	public final E ast;
	private boolean valid = true;
	
	public ElementParser(final TokenListStream in, E ast) {
		super(in);
		this.ast = ast;
	}
	
	public boolean isValid() {
		return valid;
	}
	
	protected void invalidate() {
		valid = false;
	}
	
	/**
	 * Whitespace following the most recently used non-whitespace token. Will be added to the correct ASTElement once it is known which one that is.
	 */
	protected final List<WhitespaceOrCommentToken> bufferedWhitespace = new ArrayList<>();
	
	@Override
	protected void addWhitespace(final WhitespaceOrCommentToken t) {
		assert isValid();
		bufferedWhitespace.add(t);
	}
	
	@Override
	protected void addWhitespace(final List<WhitespaceOrCommentToken> tokens) {
		assert isValid();
		bufferedWhitespace.addAll(tokens);
	}
	
	/**
	 * Adds a child to the AST as well as any buffered whitespace.
	 * 
	 * @param child
	 */
	@Override
	public void addChildToAST(final ASTElementPart child) {
		final @Nullable E ast = this.ast;
		assert ast != null;
		for (final WhitespaceOrCommentToken t : bufferedWhitespace)
			ast.addChild(t);
		bufferedWhitespace.clear();
		ast.addChild(child);
	}
	
}
