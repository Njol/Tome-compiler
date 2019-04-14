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
import ch.njol.tome.util.Watchable;

public abstract class ASTLink<T extends @Nullable IRElement> extends AbstractASTElement implements ASTElementWithIR<@Nullable T> {
	
	protected @Nullable WordOrSymbols name;
	
	private boolean isLinking = false;
	private final Cache<@Nullable T> cache = new Cache<>(this, () -> {
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
	
	protected void registerDependency(Watchable dependency) {
		// TODO remove dependencies before tryLink (as that will add them again, or even add less if some became obsolete)
		cache.registerDependency(dependency);
	}
	
	protected @Nullable String errorMessage(final String name) {
		return "Cannot find [" + name + "]";
	}
	
//	protected abstract List<T> contentAssist(StringMatcher matcher);
	
	protected static <T extends @Nullable IRElement, L extends ASTLink<T>> L parseAsTypeIdentifier(final L link, final Parser parent) {
		return parse(link, parent, Parser::oneTypeIdentifierToken);
	}
	
	protected static <T extends @Nullable IRElement, L extends ASTLink<T>> L parseAsVariableIdentifier(final L link, final Parser parent) {
		return parse(link, parent, Parser::oneVariableIdentifierToken);
	}
	
	protected static <T extends @Nullable IRElement, L extends ASTLink<T>> L parseAsAnyIdentifier(final L link, final Parser parent) {
		return parse(link, parent, Parser::oneIdentifierToken);
	}
	
	protected static <T extends @Nullable IRElement, L extends ASTLink<T>> L parse(final L link, final Parser parent, final Function<Parser, @Nullable WordOrSymbols> tokenParserFunction) {
		return parent.one(p -> {
			link.name = tokenParserFunction.apply(p);
			return link;
		});
	}
	
	protected static <T extends @Nullable IRElement, L extends ASTLink<T>> @Nullable L tryParse(final L link, final Parser parent, final Function<Parser, @Nullable WordOrSymbols> tokenParserFunction) {
		final Parser p = parent.start();
		final WordOrSymbols name = tokenParserFunction.apply(p);
		if (name == null) {
			p.cancel();
			return null;
		}
		link.name = name;
		return p.done(link);
	}
	
	protected static <T extends @Nullable IRElement, L extends ASTLink<T>> L finishParsing(final L link, final Parser p, final WordOrSymbols name) {
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
	public Cache<@Nullable T> irChache() {
		return cache;
	}
	
	@Override
	public @Nullable T getIR() {
		return get();
	}
	
	@Override
	public void getSemanticErrors(final Consumer<SemanticError> consumer) {
		final WordOrSymbols name = this.name;
		if (name != null) { // if name is null there's already a syntax error
			final @Nullable T value = get();
			if (value == null) {
				final String errorMessage = errorMessage(name.toString());
				if (errorMessage != null)
					consumer.accept(new SemanticError(errorMessage, absoluteRegionStart(), regionLength()));
			}
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
