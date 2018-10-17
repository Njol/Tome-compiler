package ch.njol.tome.ast.statements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTElementWithVariables;
import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.ASTInterfaces.ASTLocalVariable;
import ch.njol.tome.ast.ASTInterfaces.ASTTargettedExpression;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeUse;
import ch.njol.tome.ast.ASTLink;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.ast.expressions.ASTAccessExpression;
import ch.njol.tome.ast.expressions.ASTBlock;
import ch.njol.tome.ast.expressions.ASTDirectAttributeAccess;
import ch.njol.tome.ast.expressions.ASTExpressions.ASTTypeExpressions;
import ch.njol.tome.ast.expressions.ASTVariableOrUnqualifiedAttributeUse;
import ch.njol.tome.ast.statements.ASTStatements.ASTStatement;
import ch.njol.tome.compiler.Token.LowercaseWordToken;
import ch.njol.tome.compiler.Token.WordOrSymbols;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.definitions.IRBrokkrLocalVariable;
import ch.njol.tome.ir.definitions.IRParameterDefinition;
import ch.njol.tome.ir.definitions.IRParameterRedefinition;
import ch.njol.tome.ir.definitions.IRVariableOrAttributeRedefinition;
import ch.njol.tome.ir.definitions.IRVariableRedefinition;
import ch.njol.tome.ir.expressions.IRAttributeAccess;
import ch.njol.tome.ir.expressions.IRClosure;
import ch.njol.tome.ir.expressions.IRExpression;
import ch.njol.tome.ir.expressions.IRThis;
import ch.njol.tome.ir.expressions.IRUnknownExpression;
import ch.njol.tome.ir.statements.IRExpressionStatement;
import ch.njol.tome.ir.statements.IRStatement;
import ch.njol.tome.ir.statements.IRUnknownStatement;
import ch.njol.tome.ir.uses.IRSelfTypeUse;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.ir.uses.IRUnknownTypeUse;
import ch.njol.tome.parser.Parser;

public class ASTLambdaMethodCall extends AbstractASTElement implements ASTStatement, ASTTargettedExpression {
	public @Nullable ASTExpression target;
	private @Nullable ASTLink<? extends IRVariableOrAttributeRedefinition> method;
	public List<ASTLambdaMethodCallPart> parts = new ArrayList<>();
	
	public ASTLambdaMethodCall(final ASTVariableOrUnqualifiedAttributeUse method) {
		target = null;
		this.method = method.varOrAttributeLink;
	}
	
	public ASTLambdaMethodCall(final ASTAccessExpression target, final ASTDirectAttributeAccess methodAccess) {
		assert methodAccess.parent() == target;
		this.target = target;
		method = methodAccess.attributeLink;
	}
	
	@Override
	public String toString() {
		return "<lambda method call>"; // TODO
	}
	
	@Override
	public @Nullable IRTypeUse targetType() {
		return this.target != null ? this.target.getIRType() : IRSelfTypeUse.makeNew(this);
	}
	
	public static ASTLambdaMethodCall finishParsing(final Parser p, final ASTVariableOrUnqualifiedAttributeUse method, final boolean withSemicolon) {
		return finishParsing(p, new ASTLambdaMethodCall(method), withSemicolon);
	}
	
	public static ASTLambdaMethodCall finishParsing(final Parser p, final ASTAccessExpression target, final ASTDirectAttributeAccess methodAccess, final boolean withSemicolon) {
		return finishParsing(p, new ASTLambdaMethodCall(target, methodAccess), withSemicolon);
	}
	
	private static ASTLambdaMethodCall finishParsing(final Parser p, final ASTLambdaMethodCall ast, final boolean withSemicolon) {
		final boolean[] first = {true};
		p.repeatUntil(() -> {
			ast.parts.add(ASTLambdaMethodCallPart.parse(p, !first[0]));
			first[0] = false;
		}, ';', false, withSemicolon);
		return p.done(ast);
	}
	
	@Override
	public IRStatement getIR() {
		final IRExpression target = this.target != null ? this.target.getIR() : IRThis.makeNew(this);
		final IRVariableOrAttributeRedefinition attribute = method != null ? method.get() : null;
		if (!(attribute instanceof IRAttributeRedefinition)) {
			assert method != null;
			final WordOrSymbols m = method.getNameToken();
			return new IRUnknownStatement("Must be a method", m == null ? this : m);
		}
		final Map<IRParameterDefinition, IRExpression> arguments = new HashMap<>();
		for (final ASTLambdaMethodCallPart part : parts) {
			final List<IRVariableRedefinition> parameters = new ArrayList<>();
			part.parameters.forEach(p -> parameters.add(p.getIR()));
			final IRParameterRedefinition param = part.parameter != null ? part.parameter.get() : null;
			if (param != null)
				arguments.put(param.definition(), new IRClosure(parameters,
						part.expression != null ? part.expression.getIR() : new IRUnknownExpression("Syntax error. Proper syntax: [[name [optional, params] {...}]] or [[name [optional, params] nextMethod {...}]]", this)));
		}
		return new IRExpressionStatement(new IRAttributeAccess(target, (IRAttributeRedefinition) attribute, arguments, false, false, false));
	}
	
	// TODO allow to combine multiple parts? (e.g. x {} y, z {}') (useful for switch statements)
	public static class ASTLambdaMethodCallPart extends AbstractASTElement implements ASTElementWithVariables {
		@Nullable
		public ASTLink<IRParameterRedefinition> parameter;
		public List<ASTLambdaMethodCallPartParameter> parameters = new ArrayList<>();
		public @Nullable ASTExpression expression;
		
		public static class ASTLambdaMethodCallPartLink extends ASTLink<IRParameterRedefinition> {
			@Override
			protected @Nullable IRParameterRedefinition tryLink(String name) {
				ASTLambdaMethodCall call = getParentOfType(ASTLambdaMethodCall.class);
				if (call == null)
					return null;
				final IRVariableOrAttributeRedefinition method = call.method != null ? call.method.get() : null;
				return method == null || !(method instanceof IRAttributeRedefinition) ? null : ((IRAttributeRedefinition) method).getParameterByName(name);
			}
			
			private static ASTLambdaMethodCallPartLink parse(Parser parent) {
				return parseAsVariableIdentifier(new ASTLambdaMethodCallPartLink(), parent);
			}
		}
		
		@Override
		public List<? extends IRVariableRedefinition> allVariables() {
			return parameters.stream().map(p -> p.getIR()).collect(Collectors.toList());
		}
		
		@Override
		public String toString() {
			return "<lambda method call part>"; // TODO
		}
		
		public static ASTLambdaMethodCallPart parse(final Parser parent, final boolean withName) {
			final Parser p = parent.start();
			final ASTLambdaMethodCallPart ast = new ASTLambdaMethodCallPart();
			if (withName)
				ast.parameter = ASTLambdaMethodCallPartLink.parse(p);
			p.tryGroup('[', () -> {
				do {
					ast.parameters.add(ASTLambdaMethodCallPartParameter.parse(p));
				} while (p.try_(','));
			}, ']');
			if (p.peekNext('{')) {
				ast.expression = ASTBlock.parse(p);
			} else if (withName) {
				// TODO limit this some more? this only exists for [else if]
				final Parser blockParser = p.start();
				final Parser callParser = blockParser.start();
				final ASTVariableOrUnqualifiedAttributeUse variableOrUnqualifiedAttributeUse = ASTVariableOrUnqualifiedAttributeUse.parse(callParser);
				final ASTLambdaMethodCall call = ASTLambdaMethodCall.finishParsing(callParser, variableOrUnqualifiedAttributeUse, false);
				ast.expression = blockParser.done(new ASTBlock(call));
			}
			return p.done(ast);
		}
	}
	
	/**
	 * TODO what exactly is this? and is it a parameter? or just a link to one (in which case the interface might need to be removed - still should be a linkable local variable
	 * though, as it is now a variable in scope (and maybe has a different name too))
	 */
	public static class ASTLambdaMethodCallPartParameter extends AbstractASTElement implements ASTLocalVariable {
		private @Nullable ASTLink<IRParameterRedefinition> parameter;
		public @Nullable ASTTypeUse type;
		
		private static class ASTLambdaMethodCallPartParameterLink extends ASTLink<IRParameterRedefinition> {
			@Override
			protected @Nullable IRParameterRedefinition tryLink(String name) {
				// TODO parameter named like this link, or parameter with same position as this parameter (either from left or right, depending on where the dots are (if any)).
				return null;
			}
			
			private static ASTLambdaMethodCallPartParameterLink parse(Parser parent) {
				return parseAsVariableIdentifier(new ASTLambdaMethodCallPartParameterLink(), parent);
			}
		}
		
		@Override
		public @Nullable WordOrSymbols nameToken() {
			return parameter != null ? parameter.getNameToken() : null;
		}
		
		@Override
		public String toString() {
			return "<lambda method call part parameter>"; // TODO
		}
		
		public static ASTLambdaMethodCallPartParameter parse(final Parser parent) {
			final Parser p = parent.start();
			final ASTLambdaMethodCallPartParameter ast = new ASTLambdaMethodCallPartParameter();
			if (!p.try_("var") && !(p.peekNext() instanceof LowercaseWordToken && (p.peekNext(',', 1, true) || p.peekNext(']', 1, true))))
				ast.type = ASTTypeExpressions.parse(p, true, true);
			ast.parameter = ASTLambdaMethodCallPartParameterLink.parse(p);
			return p.done(ast);
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
		public IRVariableRedefinition getIR() {
			return new IRBrokkrLocalVariable(this);
		}
		
	}
	
}
