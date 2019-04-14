package ch.njol.tome.ast.expressions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.ASTInterfaces.ASTLocalVariable;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeUse;
import ch.njol.tome.ast.ASTLink;
import ch.njol.tome.ast.AbstractASTElementWithIR;
import ch.njol.tome.ast.expressions.ASTExpressions.ASTTypeExpressions;
import ch.njol.tome.compiler.Token.SymbolToken;
import ch.njol.tome.compiler.Token.WordOrSymbols;
import ch.njol.tome.ir.IRError;
import ch.njol.tome.ir.definitions.IRBrokkrLocalVariable;
import ch.njol.tome.ir.definitions.IRParameterRedefinition;
import ch.njol.tome.ir.definitions.IRVariableRedefinition;
import ch.njol.tome.ir.expressions.IRExpression;
import ch.njol.tome.ir.expressions.IRUnknownExpression;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.ir.uses.IRUnknownTypeUse;
import ch.njol.tome.parser.Parser;

// FIXME think about this some more
public class ASTErrorHandlingExpression extends AbstractASTElementWithIR<IRExpression> implements ASTExpression<IRExpression> {
	
	public ASTExpression<?> expression;
	public boolean negated;
	private @Nullable ASTErrorHandlingExpressionLink error;
	
	private static class ASTErrorHandlingExpressionLink extends ASTLink<IRError> {
		@Override
		protected @Nullable IRError tryLink(final String name) {
			// TODO Auto-generated method stub
			return null;
		}
		
		private static ASTErrorHandlingExpressionLink parse(final Parser parent) {
			return parseAsVariableIdentifier(new ASTErrorHandlingExpressionLink(), parent);
		}
	}
	
	public List<ASTErrorHandlingExpressionParameter> parameters = new ArrayList<>();
	public @Nullable ASTExpression<?> value;
	
	public ASTErrorHandlingExpression(final ASTExpression<?> expression, final SymbolToken errorSymbol) {
		this.expression = expression;
	}
	
	@Override
	public String toString() {
		return "";
	}
	
	public static ASTErrorHandlingExpression finishParsing(final Parser p, final ASTExpression<?> expression, final SymbolToken errorSymbol) {
		final ASTErrorHandlingExpression ast = new ASTErrorHandlingExpression(expression, errorSymbol);
		ast.negated = p.try_('!');
		ast.error = ASTErrorHandlingExpressionLink.parse(p);
		if (p.try_('(')) {
			do {
				ast.parameters.add(ASTErrorHandlingExpressionParameter.parse(p));
			} while (p.try_(','));
			p.one(')');
		}
		p.one(':');
		ast.value = ASTExpressions.parse(p);
		return p.done(ast);
	}
	
	@Override
	public IRTypeUse getIRType() {
		return value != null ? value.getIRType() : new IRUnknownTypeUse(getIRContext());
	}
	
	@Override
	public IRExpression calculateIR() {
		return new IRUnknownExpression("not implemented", this);
	}
	
	public static class ASTErrorHandlingExpressionParameter extends AbstractASTElementWithIR<IRVariableRedefinition> implements ASTLocalVariable {
		
		public @Nullable ASTTypeUse<?> type;
		private @Nullable ASTErrorHandlingExpressionParameterLink parameter;
		
		private static class ASTErrorHandlingExpressionParameterLink extends ASTLink<IRParameterRedefinition> {
			@Override
			protected @Nullable IRParameterRedefinition tryLink(final String name) {
				// TODO Auto-generated method stub
				return null;
			}
			
			private static ASTErrorHandlingExpressionParameterLink parse(final Parser parent) {
				return parseAsVariableIdentifier(new ASTErrorHandlingExpressionParameterLink(), parent);
			}
		}
		
		@Override
		public @Nullable WordOrSymbols nameToken() {
			return parameter != null ? parameter.getNameToken() : null;
		}
		
		@Override
		public String toString() {
			return "";
		}
		
		public static ASTErrorHandlingExpressionParameter parse(final Parser parent) {
			return parent.one(p -> {
				final ASTErrorHandlingExpressionParameter ast = new ASTErrorHandlingExpressionParameter();
				if (!p.try_("var"))
					ast.type = ASTTypeExpressions.parse(p, true, true);
				ast.parameter = ASTErrorHandlingExpressionParameterLink.parse(p);
				return ast;
			});
		}
		
		@Override
		public IRTypeUse getIRType() {
			if (type != null)
				return type.getIR();
			final IRParameterRedefinition param = parameter != null ? parameter.get() : null;
			if (param != null)
				return param.type();
			return new IRUnknownTypeUse(getIRContext());
		}
		
		@Override
		protected IRVariableRedefinition calculateIR() {
			return new IRBrokkrLocalVariable(this); // TODO correct?
		}
		
	}
	
}
