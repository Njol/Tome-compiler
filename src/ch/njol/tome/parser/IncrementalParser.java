package ch.njol.tome.parser;

import java.util.function.Function;

import ch.njol.tome.ast.ASTElement;
import ch.njol.tome.compiler.Token;
import ch.njol.tome.util.TokenListStream;

public class IncrementalParser extends Parser {
	
	public IncrementalParser(final TokenListStream in) {
		super(in);
	}
	
	private IncrementalParser(final IncrementalParser parent) {
		super(parent);
	}
	
	@Override
	public Parser start() {
		assert valid;
		assert currentChild == null;
		return currentChild = new IncrementalParser(this);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends ASTElement> T one(final Function<Parser, T> p) {
		assert valid;
		assert currentChild == null;
		Token next = in.peekNext(0, false);
		ASTElement e;
		if (next != null && (e = next.parent()) != null) {
			ASTElement e2;
			while ((e2 = e.parent()) != null)
				e = e2;
			addPart(e);
			do { // skip all token belonging to the re-used element
				in.moveForward();
			} while ((next = in.peekNext(0, false)) != null && next.isDescendantOf(e));
			return (T) e;
		}
		return super.one(p);
	}
	
}
