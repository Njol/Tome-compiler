package ch.njol.tome.ast.expressions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTElementWithVariables;
import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.ASTInterfaces.ASTLocalVariable;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeUse;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.ast.expressions.ASTExpressions.ASTTypeExpressions;
import ch.njol.tome.compiler.Token;
import ch.njol.tome.compiler.Token.LowercaseWordToken;
import ch.njol.tome.compiler.Token.WordToken;
import ch.njol.tome.ir.definitions.IRBrokkrLocalVariable;
import ch.njol.tome.ir.definitions.IRVariableRedefinition;
import ch.njol.tome.ir.expressions.IRClosure;
import ch.njol.tome.ir.expressions.IRExpression;
import ch.njol.tome.ir.expressions.IRUnknownExpression;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.ir.uses.IRUnknownTypeUse;
import ch.njol.tome.parser.Parser;

public class ASTLambda extends AbstractASTElement implements ASTExpression, ASTElementWithVariables {
	public final List<ASTLambdaParameter> parameters = new ArrayList<>();
	public @Nullable ASTExpression code;
	
	public ASTLambda(final @Nullable ASTLambdaParameter param) {
		if (param != null)
			parameters.add(param);
	}
	
	@Override
	public String toString() {
		return parameters + " -> " + code;
	}
	
	@Override
	public @Nullable String hoverInfo(final Token token) {
		return null; // TODO return description of this function
	}
	
	public static ASTLambda parse(final Parser parent) {
		final Parser p = parent.start();
		@SuppressWarnings("null")
		final ASTLambdaParameter[] param = new ASTLambdaParameter[1];
		if (!p.tryGroup('[', () -> {
			do {
				param[0] = ASTLambdaParameter.parse(p, true);
			} while (p.try_(','));
		}, ']')) {
			param[0] = ASTLambdaParameter.parse(p, true);
		}
		return finishParsing(p, param[0]);
	}
	
	public static ASTLambda finishParsing(final Parser p, final ASTLambdaParameter param) {
		final ASTLambda ast = new ASTLambda(param);
		p.one("->");
		ast.code = ASTExpressions.parse(p);
		return p.done(ast);
	}
	
	@Override
	public List<? extends IRVariableRedefinition> allVariables() {
		return parameters.stream().map(p -> p.getIR()).collect(Collectors.toList());
	}
	
	@Override
	public IRExpression getIR() {
		final ASTExpression code = this.code;
		return new IRClosure(parameters.stream().map(p -> p.getIR()).collect(Collectors.toList()), code == null ? new IRUnknownExpression("missing expression for lambda function", this) : code.getIR());
	}
	
	public static class ASTLambdaParameter extends AbstractASTElement implements ASTLocalVariable {
		public @Nullable ASTTypeUse type;
		public @Nullable LowercaseWordToken name;
		
		@Override
		public @Nullable WordToken nameToken() {
			return name;
		}
		
		public ASTLambdaParameter(final @Nullable ASTTypeUse type) {
			this.type = type;
		}
		
		@Override
		public String toString() {
			return (type != null ? type + " " : "") + name;
		}
		
		public static ASTLambdaParameter parse(final Parser parent, final boolean withType) {
			final Parser p = parent.start();
			ASTTypeUse type = null;
			if (withType && !p.try_("var"))
				type = ASTTypeExpressions.parse(p, false, false);
			return finishParsing(p, type);
		}
		
		public static ASTLambdaParameter finishParsing(final Parser p, final @Nullable ASTTypeUse type) {
			final ASTLambdaParameter ast = new ASTLambdaParameter(type);
			ast.name = p.oneVariableIdentifierToken();
			return p.done(ast);
		}
		
		@Override
		public IRVariableRedefinition getIR() {
			return new IRBrokkrLocalVariable(this);
		}
		
		@Override
		public IRTypeUse getIRType() {
			if (type != null)
				return type.getIR();
			return new IRUnknownTypeUse(getIRContext()); // TODO infer type
		}
	}
	
}
