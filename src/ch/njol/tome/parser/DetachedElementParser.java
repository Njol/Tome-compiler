package ch.njol.tome.parser;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTElement;

public class DetachedElementParser<E extends ASTElement> extends ElementParser<E> {
	
	private Parser parent;

	public DetachedElementParser(final Parser parent, E ast) {
		super(parent.in, ast);
		this.parent = parent;
	}
	
	public AttachedElementParser<E> attach() {
		final @Nullable E ast = this.ast;
		assert ast != null;
		final AttachedElementParser<E> ep = parent.startChild(ast);
		ep.addWhitespace(bufferedWhitespace);
		invalidate();
		return ep;
	}
	
	public <P extends ASTElement> DetachedElementParser<P> attachToNewDetachedParent(final P parentElement) {
		final DetachedElementParser<P> parent = new DetachedElementParser<>(in, parentElement);
		toChildOf(parent);
		return parent;
	}
	
}
