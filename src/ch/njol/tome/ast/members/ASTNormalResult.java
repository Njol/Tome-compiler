package ch.njol.tome.ast.members;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTElement;
import ch.njol.tome.ast.ASTInterfaces.ASTAttribute;
import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.ASTInterfaces.ASTResult;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeUse;
import ch.njol.tome.ast.ASTInterfaces.ASTVariable;
import ch.njol.tome.ast.ASTLink;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.ast.expressions.ASTExpressions;
import ch.njol.tome.ast.expressions.ASTExpressions.ASTTypeExpressions;
import ch.njol.tome.compiler.Token;
import ch.njol.tome.compiler.Token.LowercaseWordToken;
import ch.njol.tome.compiler.Token.SymbolToken;
import ch.njol.tome.compiler.Token.WordToken;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.definitions.IRBrokkrResultDefinition;
import ch.njol.tome.ir.definitions.IRBrokkrResultRedefinition;
import ch.njol.tome.ir.definitions.IRResultRedefinition;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.ir.uses.IRUnknownTypeUse;
import ch.njol.tome.parser.Parser;

public class ASTNormalResult extends AbstractASTElement implements ASTVariable, ASTResult {
	public @Nullable ASTTypeUse type;
	public @Nullable LowercaseWordToken name;
	public @Nullable ASTExpression defaultValue;
	private @Nullable ASTNormalResultLink overridden;
	
	private static class ASTNormalResultLink extends ASTLink<IRResultRedefinition> {
		@Override
		protected @Nullable IRResultRedefinition tryLink(String name) {
			@SuppressWarnings("null")
			final IRAttributeRedefinition parentAttr = getParentOfType(ASTAttribute.class).getIR().parentRedefinition();
			if (parentAttr == null)
				return null;
			return parentAttr.getResultByName(name);
		}
		
		private static ASTNormalResultLink parse(Parser parent) {
			return parseAsVariableIdentifier(new ASTNormalResultLink(), parent);
		}
	}
	
	@Override
	public @Nullable WordToken nameToken() {
		return name;
	}
	
	@Override
	public @NonNull String name() {
		final WordToken wordToken = name;
		return wordToken != null ? wordToken.word : "result";
	}
	
	public @Nullable ASTAttribute attribute() {
		final ASTElement parent = this.parent;
		if (parent instanceof ASTAttribute)
			return (ASTAttribute) parent;
		return null;
	}
	
	@Override
	public String toString() {
		return (type == null ? "<unresolvable type>" : type) + " " + (name == null ? "result" : "" + name);
	}
	
	@Override
	public @Nullable String hoverInfo(final Token token) {
		return getIR().hoverInfo();
	}
	
	public static ASTNormalResult parse(final Parser parent) {
		final Parser p = parent.start();
		final ASTNormalResult ast = new ASTNormalResult();
		if (p.try_("override")) {
			ast.overridden = ASTNormalResultLink.parse(p);
			p.one("as");
		}
		boolean parseType = true;
		if (p.peekNext() instanceof LowercaseWordToken) {
			final Token nextNext = p.peekNext(1, true);
			if (nextNext instanceof SymbolToken && "=,#{".indexOf(((SymbolToken) nextNext).symbol) >= 0)
				parseType = false;
		}
		if (parseType)
			ast.type = ASTTypeExpressions.parse(p, true, true);
		ast.name = p.tryVariableIdentifierToken();
//			if (name == null)
//				name = "result";
//			if (peekNext("=>")) // method '=> results' syntax
//				return this;
		if (p.try_('='))
			ast.defaultValue = ASTExpressions.parse(p);
		return p.done(ast);
	}
	
	@Override
	public IRTypeUse getIRType() {
		final ASTTypeUse type = this.type;
		if (type != null)
			return type.getIR();
		final IRResultRedefinition parent = overridden != null ? overridden.get() : null;
		if (parent == null)
			return new IRUnknownTypeUse(getIRContext());
		return parent.type();
	}
	
	private @Nullable IRResultRedefinition ir;
	
	@Override
	public IRResultRedefinition getIR() {
		if (ir != null)
			return ir;
		final IRResultRedefinition parent = overridden != null ? overridden.get() : null;
		final ASTAttribute attribute = attribute();
		assert attribute != null;
		return ir = parent == null ? new IRBrokkrResultDefinition(this, attribute.getIR()) : new IRBrokkrResultRedefinition(this, parent, attribute.getIR());
	}
}
