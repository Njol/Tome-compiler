package ch.njol.tome.ast.members;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTElement;
import ch.njol.tome.ast.ASTInterfaces.ASTAttribute;
import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.ASTInterfaces.ASTParameter;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeUse;
import ch.njol.tome.ast.ASTLink;
import ch.njol.tome.ast.AbstractASTElementWithIR;
import ch.njol.tome.ast.expressions.ASTExpressions;
import ch.njol.tome.ast.expressions.ASTExpressions.ASTTypeExpressions;
import ch.njol.tome.common.Visibility;
import ch.njol.tome.compiler.Token;
import ch.njol.tome.compiler.Token.WordToken;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.definitions.IRBrokkrNormalParameterDefinition;
import ch.njol.tome.ir.definitions.IRBrokkrNormalParameterRedefinition;
import ch.njol.tome.ir.definitions.IRMemberRedefinition;
import ch.njol.tome.ir.definitions.IRParameterRedefinition;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.ir.uses.IRUnknownTypeUse;
import ch.njol.tome.parser.Parser;

public class ASTSimpleParameter extends AbstractASTElementWithIR<IRParameterRedefinition> implements ASTParameter {
	
	private final int index;
	public @Nullable Visibility visibility;
	public boolean override;
	private @Nullable ASTSimpleParameterLink overridden;
	public @Nullable ASTTypeUse<?> type;
	public @Nullable WordToken name;
	public @Nullable ASTExpression<?> defaultValue;
	
	private static class ASTSimpleParameterLink extends ASTLink<IRParameterRedefinition> {
		@Override
		protected @Nullable IRParameterRedefinition tryLink(final String name) {
			final ASTAttributeDeclaration attribute = getParentOfType(ASTAttributeDeclaration.class);
			if (attribute == null)
				return null;
			final IRMemberRedefinition parent = attribute.modifiers.overridden();
			if (parent == null || !(parent instanceof IRAttributeRedefinition))
				return null;
			return ((IRAttributeRedefinition) parent).getParameterByName(name);
		}
		
		public static @Nullable ASTSimpleParameterLink parse(final Parser parent) {
			return parseAsAnyIdentifier(new ASTSimpleParameterLink(), parent);
		}
	}
	
	private ASTSimpleParameter(final int index) {
		this.index = index;
	}
	
	@Override
	public @Nullable WordToken nameToken() {
		return name;
	}
	
	public @Nullable ASTAttribute attribute() {
		final ASTElement parent = this.parent;
		if (parent instanceof ASTAttribute)
			return (ASTAttribute) parent;
		return null;
	}
	
//		@Override
//		public @Nullable FormalParameter overridden() {
//			return overridden.get();
//		}
	
	@Override
	public String toString() {
		return type + " " + name;
	}
	
	@Override
	public @Nullable String hoverInfo(final Token token) {
		return getIR().hoverInfo();
	}
	
	public static ASTSimpleParameter parse(final Parser parent, final int index) {
		final Parser p = parent.start();
		final ASTSimpleParameter ast = new ASTSimpleParameter(index);
		ast.visibility = Visibility.parse(p);
		ast.override = p.try_("override");
		if (ast.override) {
			if (p.peekNext() instanceof WordToken && p.peekNext("as", 1, true)) {
				ast.overridden = ASTSimpleParameterLink.parse(p);
				p.next(); // skip 'as'
			}
			if (ast.overridden == null // if not renamed, the only thing left to change is the type, so require it
					|| !(p.peekNext() instanceof WordToken && (p.peekNext(',', 1, true) || p.peekNext(')', 1, true)))) // allows overriding the name without changing the type
				ast.type = ASTTypeExpressions.parse(p, true, true);
		} else {
			ast.type = ASTTypeExpressions.parse(p, true, true);
		}
		ast.name = p.oneIdentifierToken();
		if (p.try_('='))
			ast.defaultValue = ASTExpressions.parse(p);
		return p.done(ast);
	}
	
	private @Nullable IRParameterRedefinition overridden() {
		if (overridden != null) {
			return overridden.get();
		} else {
			// TODO
			return null;
		}
	}
	
	@Override
	public IRTypeUse getIRType() {
		if (type != null)
			return type.getIR();
		final IRParameterRedefinition parent = overridden();
		if (parent == null)
			return new IRUnknownTypeUse(getIRContext());
		return parent.type();
	}
	
	@Override
	protected IRParameterRedefinition calculateIR() {
		final IRParameterRedefinition parent = overridden();
		final ASTAttribute attribute = attribute();
		assert attribute != null;
		return (parent != null ? new IRBrokkrNormalParameterRedefinition(this, parent, attribute.getIR())
				: new IRBrokkrNormalParameterDefinition(this, attribute.getIR()));
	}
	
}
