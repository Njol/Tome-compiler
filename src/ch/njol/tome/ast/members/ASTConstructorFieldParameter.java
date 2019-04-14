package ch.njol.tome.ast.members;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTElement;
import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.ASTInterfaces.ASTParameter;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.tome.ast.ASTLink;
import ch.njol.tome.ast.AbstractASTElementWithIR;
import ch.njol.tome.ast.expressions.ASTExpressions;
import ch.njol.tome.compiler.Token;
import ch.njol.tome.compiler.Token.WordOrSymbols;
import ch.njol.tome.ir.IRUnknownParameterDefinition;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.definitions.IRBrokkrConstructorFieldParameter;
import ch.njol.tome.ir.definitions.IRParameterDefinition;
import ch.njol.tome.ir.definitions.IRParameterRedefinition;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.ir.uses.IRUnknownTypeUse;
import ch.njol.tome.parser.Parser;

// TODO make more specific, e.g. AbstractASTElementWithIR<IRParameterRedefinition, IRParameterDefinition>
public class ASTConstructorFieldParameter extends AbstractASTElementWithIR<IRParameterRedefinition> implements ASTParameter {
	
	private @Nullable ASTConstructorFieldParameterLink attribute;
	public @Nullable ASTExpression<?> defaultValue;
	
	private static class ASTConstructorFieldParameterLink extends ASTLink<IRAttributeRedefinition> {
		@Override
		protected @Nullable IRAttributeRedefinition tryLink(final String name) {
			final ASTTypeDeclaration<?> mc = getParentOfType(ASTTypeDeclaration.class);
			if (mc == null)
				return null;
			return mc.getIR().getAttributeByName(name);
		}
		
		private static ASTConstructorFieldParameterLink parse(final Parser parent) {
			return parseAsVariableIdentifier(new ASTConstructorFieldParameterLink(), parent);
		}
	}
	
	@Override
	public @Nullable WordOrSymbols nameToken() {
		return attribute != null ? attribute.getNameToken() : null;
	}
	
	public static ASTConstructorFieldParameter parse(final Parser parent) {
		final Parser p = parent.start();
		final ASTConstructorFieldParameter ast = new ASTConstructorFieldParameter();
		ast.attribute = ASTConstructorFieldParameterLink.parse(p);
		if (p.try_('='))
			ast.defaultValue = ASTExpressions.parse(p);
		return p.done(ast);
	}
	
	@Override
	public String toString() {
		return "" + attribute;
	}
	
	@Override
	public @Nullable String hoverInfo(final Token token) {
		return getIR().hoverInfo();
	}
	
	@SuppressWarnings("null")
	@Override
	public IRTypeUse getIRType() {
		final IRAttributeRedefinition f = attribute.get();
		return f == null ? null : f.mainResultType();
	}
	
	public @Nullable ASTConstructor constructor() {
		final ASTElement parent = this.parent;
		if (parent instanceof ASTConstructor)
			return (ASTConstructor) parent;
		return null;
	}
	
	@Override
	protected IRParameterDefinition calculateIR() {
		final IRAttributeRedefinition attr = attribute != null ? attribute.get() : null;
		final ASTConstructor constructor = constructor();
		assert constructor != null;
		if (attr == null || attr.results().size() != 1 || !attr.results().get(0).name().equals("result") || !attr.isVariable())
			return new IRUnknownParameterDefinition(attr != null ? attr.name() : "<unknown name>", new IRUnknownTypeUse(getIRContext()), constructor.getIR(), "Constructor field parameter '" + (attr != null ? attr.name() : "<unknown name>") + "' does not reference a field", this);
		return new IRBrokkrConstructorFieldParameter(this, attr, constructor.getIR());
	}
	
}
