package ch.njol.tome.ast.expressions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.ASTInterfaces.ASTMethodCall;
import ch.njol.tome.ast.ASTLink;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.compiler.Token.LowercaseWordToken;
import ch.njol.tome.ir.definitions.IRAttributeDefinition;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.definitions.IRParameterDefinition;
import ch.njol.tome.ir.definitions.IRParameterRedefinition;
import ch.njol.tome.ir.expressions.IRExpression;
import ch.njol.tome.parser.Parser;

/**
 * An argument to a function call.
 */
public class ASTArgument extends AbstractASTElement {
	
	private final int index;
	public boolean isDots;
	public @Nullable ASTExpression<?> value;
	public @Nullable ParameterLink parameterLink;
	
	public static class ParameterLink extends ASTLink<IRParameterRedefinition> {
		@Override
		protected @Nullable IRParameterRedefinition tryLink(final String name) {
			final ASTMethodCall parent = getParentOfType(ASTMethodCall.class);
			if (parent == null)
				return null;
			final IRAttributeRedefinition method = parent.attribute();
			if (method == null)
				return null;
			for (final IRParameterRedefinition p : method.parameters()) {
				if (name.equals(p.name()))
					return p;
			}
			return null;
		}
		
		private static ParameterLink parse(final Parser parent) {
			return parseAsVariableIdentifier(new ParameterLink(), parent);
		}
	}
	
	public ASTArgument(final int index) {
		this.index = index;
	}
	
	@Override
	public String toString() {
		return "" + value;
	}
	
	public static ASTArgument parse(final Parser parent, final int index) {
		return parent.one(p -> {
			final ASTArgument ast = new ASTArgument(index);
			ast.isDots = p.try_("...");
			if (!ast.isDots) {
				if (p.peekNext() instanceof LowercaseWordToken && p.peekNext(':', 1, true)) {
					ast.parameterLink = ParameterLink.parse(p);
					p.next(); // skip ':'
				}
				ast.value = ASTExpressions.parse(p);
			}
			return ast;
		});
	}
	
	public @Nullable IRParameterRedefinition getParameter() {
		if (parameterLink != null) // if the name is defined, find the parameter by name
			return parameterLink.get();
		// otherwise find the parameter by index
		final ASTMethodCall parent = getParentOfType(ASTMethodCall.class);
		if (parent == null)
			return null;
		final IRAttributeRedefinition method = parent.attribute();
		if (method == null)
			return null;
		final List<IRParameterRedefinition> parameters = method.parameters();
		return index < parameters.size() ? parameters.get(index) : null;
	}
	
//		public static Map<IRParameterDefinition, InterpretedObject> makeInterpretedArgumentMap(final IRAttributeDefinition method, final List<ASTArgument> args, final InterpreterContext context) {
//			final List<IRParameterRedefinition> parameters = method.parameters();
//			final Map<IRParameterDefinition, InterpretedObject> r = new HashMap<>();
//			for (int i = 0; i < args.size(); i++) {
//				final ASTArgument arg = args.get(i);
//				final ASTExpression expression = arg.value;
//				if (expression == null)
//					continue;
//				final InterpretedObject value = expression.interpret(context);
//				if (value == null)
//					continue;
//				if (arg.parameter.getNameToken() == null) {
//					r.put(parameters.get(i).definition(), value);
//				} else {
//					final IRParameterRedefinition parameter = arg.parameter.get();
//					if (parameter == null)
//						continue;
//					r.put(parameter.definition(), value);
//				}
//			}
//			return r;
//		}
	
	public static Map<IRParameterDefinition, IRExpression> makeIRArgumentMap(final IRAttributeDefinition method, final List<ASTArgument> args) {
		final Map<IRParameterDefinition, IRExpression> r = new HashMap<>();
		for (final ASTArgument arg : args) {
			final ASTExpression<?> expression = arg.value;
			if (expression == null)
				continue;
			final IRExpression value = expression.getIR();
			final IRParameterRedefinition parameter = arg.getParameter();
			if (parameter == null)
				continue;
			r.put(parameter.definition(), value);
		}
		return r;
	}
}
