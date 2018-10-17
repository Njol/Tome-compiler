package ch.njol.tome.ast.expressions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTElementWithVariables;
import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeUse;
import ch.njol.tome.ast.ASTInterfaces.ASTVariable;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.ast.expressions.ASTExpressions.ASTTypeExpressions;
import ch.njol.tome.compiler.Token.LowercaseWordToken;
import ch.njol.tome.compiler.Token.WordToken;
import ch.njol.tome.ir.definitions.IRQuantifierVariable;
import ch.njol.tome.ir.definitions.IRVariableDefinition;
import ch.njol.tome.ir.definitions.IRVariableRedefinition;
import ch.njol.tome.ir.expressions.IRExpression;
import ch.njol.tome.ir.expressions.IRUnknownExpression;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.ir.uses.IRUnknownTypeUse;
import ch.njol.tome.parser.Parser;
import ch.njol.util.StringUtils;

/**
 * A first-order logic quantifier for contracts, i.e. a 'for all' or 'there exists'.
 */
public class ASTQuantifier extends AbstractASTElement implements ASTExpression, ASTElementWithVariables {
	boolean forall;
	public final List<ASTQuantifierVars> vars = new ArrayList<>();
	public @Nullable ASTExpression condition;
	public @Nullable ASTExpression expression;
	
	@Override
	public String toString() {
		return (forall ? "forall" : "exists") + "(...)";
	}
	
	public static ASTQuantifier parse(final Parser parent) {
		return parent.one(p -> {
			final ASTQuantifier ast = new ASTQuantifier();
			ast.forall = Objects.equals(p.oneOf("forall", "exists"), "forall");
			p.oneGroup('(', () -> {
				p.until(() -> {
					do {
						ast.vars.add(ASTQuantifierVars.parse(p));
					} while (p.try_(';'));
					if (p.try_('|'))
						ast.condition = ASTExpressions.parse(p);
				}, ':', false);
				ast.expression = ASTExpressions.parse(p);
			}, ')');
			return ast;
		});
	}
	
	@Override
	public List<? extends IRVariableRedefinition> allVariables() {
		return vars.stream().flatMap(vars -> vars.vars.stream()).map(v -> v.getIR()).collect(Collectors.toList());
	}
	
	@Override
	public IRTypeUse getIRType() {
		return getIRContext().getTypeUse("lang", "Boolean");
	}
	
	@Override
	public IRExpression getIR() {
		return new IRUnknownExpression("not implemented", this);
	}
	
	public static class ASTQuantifierVars extends AbstractASTElement {
		public @Nullable ASTTypeUse type;
		public final List<ASTQuantifierVar> vars = new ArrayList<>();
		
		@Override
		public String toString() {
			return type + " " + StringUtils.join(vars, ", ");
		}
		
		public static ASTQuantifierVars parse(final Parser parent) {
			return parent.one(p -> {
				final ASTQuantifierVars ast = new ASTQuantifierVars();
				ast.type = ASTTypeExpressions.parse(p, true, true);
				do {
					ast.vars.add(ASTQuantifierVar.parse(p));
				} while (p.try_(','));
				return ast;
			});
		}
	}
	
	public static class ASTQuantifierVar extends AbstractASTElement implements ASTVariable /*implements ASTParameter*/ {
		public @Nullable LowercaseWordToken name;
		
//		@Override
//		public @Nullable WordToken nameToken() {
//			return name;
//		}
		
		@Override
		public String toString() {
			return "" + name;
		}
		
		public static ASTQuantifierVar parse(final Parser parent) {
			return parent.one(p -> {
				final ASTQuantifierVar ast = new ASTQuantifierVar();
				ast.name = p.oneVariableIdentifierToken();
				return ast;
			});
		}
		
		@Override
		public @Nullable WordToken nameToken() {
			return name;
		}
		
		@Override
		public IRTypeUse getIRType() {
			final ASTQuantifierVars vars = (ASTQuantifierVars) parent;
			if (vars == null)
				return new IRUnknownTypeUse(getIRContext());
			final ASTTypeUse type = vars.type;
			if (type == null)
				return new IRUnknownTypeUse(getIRContext());
			return type.getIR();
		}
		
		public IRVariableDefinition getIR() {
			return new IRQuantifierVariable(this);
		}
	}
	
}
