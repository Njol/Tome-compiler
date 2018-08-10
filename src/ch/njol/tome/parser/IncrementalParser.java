package ch.njol.tome.parser;

import java.util.function.Function;

import ch.njol.tome.ast.ASTElement;
import ch.njol.tome.compiler.Token;
import ch.njol.tome.util.TokenListStream;

public class IncrementalParser extends Parser {

	public IncrementalParser(TokenListStream in) {
		super(in);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends ASTElement> T one(Function<Parser, T> p) {
		Token next = in.peekNext(0, false);
		ASTElement e;
		if (next != null && (e = next.parent()) != null) {
			ASTElement e2;
			while ((e2 = e.parent()) != null)
				e = e2;
			addPart(e);
			do { // skip all token belonging to the re-used element
				in.moveForward();
			} while((next = in.peekNext(0, false)) != null && next.isDescendantOf(e));
			return (T) e;
		}
		return super.one(p);
	}
	
}
