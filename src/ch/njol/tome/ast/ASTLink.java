package ch.njol.tome.ast;

import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTElementWithIR;
import ch.njol.tome.compiler.SemanticError;
import ch.njol.tome.compiler.SourceCodeLinkable;
import ch.njol.tome.compiler.Token;
import ch.njol.tome.compiler.Token.WordOrSymbols;
import ch.njol.tome.ir.IRElement;
import ch.njol.tome.parser.Parser;
import ch.njol.tome.util.Cache;

public abstract class ASTLink<T extends IRElement> extends AbstractASTElement implements ASTElementWithIR {
	
	protected @Nullable WordOrSymbols name;
	
	private boolean isLinking = false;
	private final Cache<@Nullable T> cache = new Cache<>(() -> {
		if (isLinking) {// recursion - abort
//				assert false : this;
			return null;
		}
		final WordOrSymbols token = name;
		if (token == null)
			return null;
		isLinking = true;
		final @Nullable T value = tryLink(token.wordOrSymbols());
		isLinking = false;
		return value;
	});
	
	protected ASTLink() {}
	
	protected abstract @Nullable T tryLink(String name);
	
	protected String errorMessage(String name) {
		return "Cannot find [" + name + "]";
	}
	
//	protected abstract List<T> contentAssist(StringMatcher matcher);
	
	protected static <T extends IRElement, L extends ASTLink<T>> L parseAsTypeIdentifier(L link, final Parser parent) {
		return parse(link, parent, Parser::oneTypeIdentifierToken);
	}
	
	protected static <T extends IRElement, L extends ASTLink<T>> L parseAsVariableIdentifier(L link, final Parser parent) {
		return parse(link, parent, Parser::oneVariableIdentifierToken);
	}
	
	protected static <T extends IRElement, L extends ASTLink<T>> L parseAsAnyIdentifier(L link, final Parser parent) {
		return parse(link, parent, Parser::oneIdentifierToken);
	}
	
	protected static <T extends IRElement, L extends ASTLink<T>> L parse(L link, final Parser parent, final Function<Parser, @Nullable WordOrSymbols> tokenParserFunction) {
		return parent.one(p -> {
			link.name = tokenParserFunction.apply(p);
			return link;
		});
	}
	
	protected static <T extends IRElement, L extends ASTLink<T>> @Nullable L tryParse(L link, final Parser parent, final Function<Parser, @Nullable WordOrSymbols> tokenParserFunction) {
		final Parser p = parent.start();
		final WordOrSymbols name = tokenParserFunction.apply(p);
		if (name == null) {
			p.cancel();
			return null;
		}
		link.name = name;
		return p.done(link);
	}
	
	public final @Nullable String getName() {
		final WordOrSymbols token = name;
		return token != null ? token.wordOrSymbols() : null;
	}
	
	public final @Nullable WordOrSymbols getNameToken() {
		return name;
	}
	
	public final @Nullable T get() {
		return cache.get();
	}
	
	@Override
	public @Nullable IRElement getIR() {
		return get();
	}
	
	@Override
	public void getSemanticErrors(Consumer<SemanticError> consumer) {
		WordOrSymbols name = this.name;
		if (name != null) { // if name is null there's already a syntax error
			final @Nullable T value = get();
			if (value == null)
				consumer.accept(new SemanticError(errorMessage(name.toString()), absoluteRegionStart(), regionLength()));
		}
		super.getSemanticErrors(consumer);
	}
	
	@Override
	public String toString() {
		return "" + name;
	}
	
	@Override
	public @Nullable SourceCodeLinkable getLinked(final Token t) {
		final @Nullable T linked = get();
		if (linked instanceof SourceCodeLinkable)
			return (SourceCodeLinkable) linked;
		return super.getLinked(t);
	}
	
}
