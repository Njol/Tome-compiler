package ch.njol.brokkr.compiler.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.ParseException;
import ch.njol.brokkr.compiler.Token;
import ch.njol.brokkr.compiler.Token.CodeGenerationToken;
import ch.njol.brokkr.compiler.Token.LowercaseWordToken;
import ch.njol.brokkr.compiler.Token.WordToken;
import ch.njol.brokkr.compiler.ast.Expressions.Access;
import ch.njol.brokkr.compiler.ast.Expressions.AccessExpression;
import ch.njol.brokkr.compiler.ast.Expressions.Block;
import ch.njol.brokkr.compiler.ast.Expressions.DirectAttributeAccess;
import ch.njol.brokkr.compiler.ast.Expressions.TypeExpressions;
import ch.njol.brokkr.compiler.ast.Expressions.VariableOrUnqualifiedAttributeUse;
import ch.njol.brokkr.compiler.ast.Interfaces.Expression;
import ch.njol.brokkr.compiler.ast.Interfaces.FormalAttribute;
import ch.njol.brokkr.compiler.ast.Interfaces.FormalError;
import ch.njol.brokkr.compiler.ast.Interfaces.FormalParameter;
import ch.njol.brokkr.compiler.ast.Interfaces.FormalVariable;
import ch.njol.brokkr.compiler.ast.Interfaces.FormalVariableOrAttribute;
import ch.njol.brokkr.compiler.ast.Interfaces.HasVariables;
import ch.njol.brokkr.compiler.ast.Interfaces.TypeUse;
import ch.njol.brokkr.compiler.ast.Members.Member;
import ch.njol.brokkr.compiler.ast.Members.NormalResult;
import ch.njol.brokkr.compiler.ast.Members.Postcondition;
import ch.njol.brokkr.compiler.ast.Members.Precondition;
import ch.njol.brokkr.interpreter.InterpretedClosure;
import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedBrokkrLocalVariable;
import ch.njol.brokkr.interpreter.definitions.InterpretedBrokkrParameterDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedMemberRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedNativeTypeDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedParameterDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedParameterRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedVariableOrAttributeRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedVariableRedefinition;
import ch.njol.brokkr.interpreter.uses.InterpretedTypeUse;
import ch.njol.util.StringUtils;

public class Statements {
	
	public static interface Statement extends Element {
		
		static Element parse_(final AbstractElement<?> parent, final boolean allowExpressionAtEnd) throws ParseException {
//			if (parent.peek('{'))
//				throw new ParseException("A block is not allowed here", parent.in.getOffset(), parent.in.getOffset() + 1);
//			if (parent.peekNext('{'))
//			return parent.one(Block.class);
			if (parent.peekNext() instanceof CodeGenerationToken)
				return parent.one(CodeGenerationStatement.class);
			if (parent.peekNext("$="))
				return parent.one(CodeGenerationCall.class);
			if (parent.peekNext("return"))
				return parent.one(Return.class);
//			if (parent.peekNext("assert"))
//				return parent.one(Assert.class);
			if (parent.peekNext("requires"))
				return parent.one(Precondition.class);
			if (parent.peekNext("ensures"))
				return parent.one(Postcondition.class);
			if (parent.peekNext("var")) // <Type> var [= ...]; is handled below
				return parent.one(VariableDeclarations.class);
			
			// TODO ErrorHandlingStatement
			
			final Expression expr = Expressions.parse(parent);
			if (parent.peekNext(';'))
				return parent.one(new ExpressionStatement(expr));
			if (allowExpressionAtEnd && parent.peekNext() == null)
				return expr;
			if (expr instanceof TypeUse) {
				// variable declaration
				return parent.one(new VariableDeclarations((TypeUse) expr));
			}
			
			if (expr instanceof VariableOrUnqualifiedAttributeUse)
				return parent.one(new LambdaMethodCall(((VariableOrUnqualifiedAttributeUse) expr).varOrAttribute));
			final Access a;
			if (expr instanceof AccessExpression && !((AccessExpression) expr).meta && (a = ((AccessExpression) expr).access) instanceof DirectAttributeAccess)
				return parent.one(new LambdaMethodCall(expr, ((DirectAttributeAccess) a).attribute));
			
			return parent.one(new ExpressionStatement(expr));
		}
		
		public static Element parseWithExpression(final AbstractElement<?> parent) throws ParseException {
			return parse_(parent, true);
		}
		
		public static Statement parse(final AbstractElement<?> parent) throws ParseException {
			return (Statement) parse_(parent, false);
		}
		
		public void interpret(InterpreterContext context);
		
	}
	
	public static class ExpressionStatement extends AbstractElement<ExpressionStatement> implements Statement {
		public final Expression expression;
		
		public ExpressionStatement(final Expression expression) {
			this.expression = expression;
			expression.setParent(this);
		}
		
		@Override
		public String toString() {
			return expression + ";";
		}
		
		@Override
		protected ExpressionStatement parse() throws ParseException {
			one(';');
//			expectedFatal("';' to complete statement"); // TODO better error message
			return this;
		}
		
		@Override
		public void interpret(final InterpreterContext context) {
			expression.interpret(context);
		}
	}
	
	public static class CodeGenerationCall extends AbstractElement<CodeGenerationCall> implements Member, Statement {
		public @Nullable Expression code;
		
		@Override
		public boolean isInherited() {
			return false; // only generates code at the current location
		}
		
		@Override
		public String toString() {
			return "$= " + code + ";";
		}
		
		@Override
		protected CodeGenerationCall parse() throws ParseException {
			one("$=");
			until(() -> {
				code = Expressions.parse(this);
			}, ';', false);
			return this;
		}
		
		@Override
		public void interpret(final InterpreterContext context) {
			throw new InterpreterException("not implemented");
		}
		
		@Override
		public InterpretedMemberRedefinition interpreted() {
			throw new InterpreterException("not implemented");
		}
	}
	
	public static class Return extends AbstractElement<Return> implements Statement {
		private final boolean withReturn;
		
		public Return() {
			this(true);
		}
		
		public Return(final boolean withReturn) {
			this.withReturn = withReturn;
		}
		
		public Link<FormalError> error = new Link<FormalError>(this) {
			@Override
			protected @Nullable FormalError tryLink(final String name) {
				// TODO Auto-generated method stub
				return null;
			}
		};
		List<ReturnResult> results = new ArrayList<>();
		
		@Override
		public String toString() {
			return "return " + StringUtils.join(results, ", ");
		}
		
		@Override
		protected Return parse() throws ParseException {
			if (withReturn)
				one("return");
			until(() -> {
				if (try_('#')) {
					error.setName(oneVariableIdentifierToken());
					tryGroup('(', () -> {
						do {
							results.add(one(ReturnResult.class)); // TODO are these also ReturnResults?
						} while (try_(','));
					}, ')');
				} else {
					do {
						results.add(one(ReturnResult.class));
					} while (try_(','));
				}
			}, ';', true);
			return this;
		}
		
		@SuppressWarnings("null")
		@Override
		public void interpret(final InterpreterContext context) {
			for (final ReturnResult r : results) {
				context.setLocalVariableValue(r.result.get().interpreted().definition(), r.value.interpret(context));
			}
			context.isReturning = true;
		}
	}
	
	public static class ReturnResult extends AbstractElement<ReturnResult> {
		public Link<NormalResult> result = new Link<NormalResult>(this) {
			@Override
			protected @Nullable NormalResult tryLink(final String name) {
				// TODO link by name or position
				return null;
			}
		};
		public @Nullable Expression value;
		
		@Override
		public String toString() {
			return (result.getName() != null ? result.getName() + " " : "") + value;
		}
		
		@Override
		protected ReturnResult parse() throws ParseException {
			if (peekNext() instanceof LowercaseWordToken && peekNext(':', 1, true)) {
				result.setName(oneVariableIdentifierToken());
				next(); // skip ':'
			}
			value = Expressions.parse(this);
			return this;
		}
	}
	
	/*
	public static class Assert extends AbstractElement<Assert> implements Statement {
		public @Nullable Expression expression;
		
		@Override
		public String toString() {
			return "assert " + expression + ";";
		}
		
		@Override
		protected Assert parse() throws ParseException {
			one("assert");
			until(() -> {
				expression = Expressions.parse(this);
			}, ';', false);
			return this;
		}
	
		@SuppressWarnings("null")
		@Override
		public @Nullable InterpretedObject interpret(InterpreterContext context) {
			InterpretedObject object = expression.interpret(context);
			return null;
		}
	}*/
	
	public static class VariableDeclarations extends AbstractElement<VariableDeclarations> implements Statement {
		public @Nullable TypeUse type;
		public List<VariableDeclarationsVariable> variables = new ArrayList<>();
		
		public VariableDeclarations() {}
		
		public VariableDeclarations(final TypeUse type) {
			this.type = type;
			type.setParent(this);
		}
		
		@Override
		protected VariableDeclarations parse() throws ParseException {
			until(() -> {
				if (type == null)
					one("var");
				do {
					variables.add(one(VariableDeclarationsVariable.class));
				} while (try_(','));
			}, ';', false);
			return this;
		}
		
		@Override
		public void interpret(final InterpreterContext context) {
			for (final VariableDeclarationsVariable v : variables) {
				context.defineLocalVariable(v.interpreted().definition(), v.initialValue != null ? v.initialValue.interpret(context) : null);
			}
		}
	}
	
	public static class VariableDeclarationsVariable extends AbstractElement<VariableDeclarationsVariable> implements FormalVariable {
		public @Nullable LowercaseWordToken nameToken;
		public @Nullable Expression initialValue;
		
		@Override
		public @Nullable WordToken nameToken() {
			return nameToken;
		}
		
		@Override
		protected VariableDeclarationsVariable parse() throws ParseException {
			nameToken = oneVariableIdentifierToken();
			if (try_('='))
				initialValue = Expressions.parse(this);
			return this;
		}
		
		@SuppressWarnings("null")
		@Override
		public InterpretedTypeUse interpretedType() {
			return ((VariableDeclarations) parent).type.staticallyKnownType();
		}
		
		@Override
		public InterpretedVariableRedefinition interpreted() {
			return new InterpretedBrokkrLocalVariable(this);
		}
	}
	
//	public static class ErrorHandlingStatement extends AbstractElement implements Statement {
//
//		@Override
//		protected void parse()  {
//			// TODO Auto-generated method stub
//
//		}
//	}

//	public static class CodeGenerationStatement extends AbstractElement<CodeGenerationStatement> implements Statement {
//		public final List<String> parts = new ArrayList<>();
//		public final List<Expression> expressions = new ArrayList<>();
//
//		@Override
//		protected CodeGenerationStatement parse() {
//			final int start = in.getOffset();
//			final int first = in.next();
//			final int next = in.next();
//			if (first != '$' || next == -1 || next == '=') {
//				expectedFatal("a code generation line or block", start, next == '=' ? 2 : 1);
//				return this;
//			}
//			Literal<CodeGenerationPart> literal;
//			if (next == '{') {
//				literal = Literals.codeGenerationBlockPart;
//			} else {
//				in.setOffset(in.getOffset() - 1);
//				literal = Literals.codeGenerationLinePart;
//			}
//			while (true) {
//				final CodeGenerationPart part = one(literal);
//				parts.add(part.code);
//				if (part.followedBySingleVariable) {
//					final int varStart = in.getOffset();
//					expressions.add(one(ActualVariableOrUnqualifiedField.class));
//					{ // prevent 'one(...)' from skipping whitespace after the variable
//						in.setOffset(varStart);
//						variableIdentifier.parse(in);
//					}
//					continue;
//				} else if (part.followedByExpression) {
//					expressions.add(Expression.parse(this));
//					if (in.peekNext() != ')') {
//						expectedFatal("Missing ')' to complete expression", in.getOffset(), 1);
//						return this;
//					}
//					continue;
//				}
//				return this;
//			}
//		}
//	}
	
	public static class LambdaMethodCall extends AbstractElement<Statement> implements Statement {
		public @Nullable Expression target;
		public Link<? extends InterpretedVariableOrAttributeRedefinition> method;
		public List<LambdaMethodCallPart> parts = new ArrayList<>();
		
		public LambdaMethodCall(final Link<? extends InterpretedVariableOrAttributeRedefinition> method) {
			target = null;
			this.method = method;
		}
		
		public LambdaMethodCall(final Expression target, final Link<? extends InterpretedVariableOrAttributeRedefinition> method) {
			this.target = target;
			target.setParent(this);
			this.method = method;
		}
		
		@Override
		protected Statement parse() throws ParseException {
			final boolean[] first = {true};
			repeatUntil(() -> {
				parts.add(one(new LambdaMethodCallPart(!first[0])));
				first[0] = false;
			}, ';', false);
			return this;
		}
		
		@SuppressWarnings("null")
		@Override
		public void interpret(final InterpreterContext context) {
			final InterpretedObject object = target != null ? target.interpret(context) : context.getThisObject();
			final InterpretedVariableOrAttributeRedefinition m = method.get();
			if (!(m instanceof InterpretedAttributeRedefinition))
				throw new InterpreterException("not an attribute");
			final Map<InterpretedParameterDefinition, InterpretedObject> arguments = new HashMap<>();
			for (final LambdaMethodCallPart part : parts) {
				// TODO calculate parameter and return types
				arguments.put(part.parameter.get().definition(), new InterpretedClosure(null, null, false) {
					@Override
					public InterpretedObject interpret(final Map<InterpretedParameter, InterpretedObject> arguments) {
						part.block.interpret(context);
						return null;
					}
				});
			}
			((FormalAttribute) m).interpreted().interpretDispatched(object, arguments, false);
		}
	}
	
	// TODO allow to combine multiple parts? (e.g. x {} y, z {}')
	public static class LambdaMethodCallPart extends AbstractElement<LambdaMethodCallPart> implements HasVariables {
		public Link<InterpretedParameterRedefinition> parameter = new Link<InterpretedParameterRedefinition>(this) {
			@Override
			protected @Nullable InterpretedParameterRedefinition tryLink(final String name) {
				@SuppressWarnings("null")
				final InterpretedVariableOrAttributeRedefinition method = ((LambdaMethodCall) parent).method.get();
				return method == null || !(method instanceof InterpretedAttributeRedefinition) ? null : ((InterpretedAttributeRedefinition) method).getParameterByName(name);
			}
		};
		public List<LambdaMethodCallPartParameter> parameters = new ArrayList<>();
//		public @Nullable Statement statement;
//		public @Nullable Expression expression;
		public @Nullable Block block;
		
		@SuppressWarnings("null")
		@Override
		public List<? extends InterpretedVariableRedefinition> allVariables() {
			return parameters.stream().map(p -> p.interpreted()).collect(Collectors.toList());
		}
		
		private final boolean withName;
		
		public LambdaMethodCallPart(final boolean withName) {
			this.withName = withName;
		}
		
		@Override
		protected LambdaMethodCallPart parse() throws ParseException {
			if (withName)
				parameter.setName(oneVariableIdentifierToken());
			tryGroup('[', () -> {
				do {
					parameters.add(one(LambdaMethodCallPartParameter.class));
				} while (try_(','));
			}, ']');
			// this syntax is extremely special just to save a '}' and to make 'else if's nicer
//			if (try_(':')) {
//				statement = Statement.parseNoBlock(this);
//			} else {
//				final List<Object> l = tryAll("{", Expression.class, "}");
//				if (l != null)
//					expression = (Expression) l.get(1);
//				else
			block = one(Block.class);
//			}
			return this;
		}
	}
	
	/**
	 * TODO what exactly is this? and is it a parameter? or just a link to one (in which case the interface might need to be removed - still should be a linkable local variable
	 * though)
	 */
	public static class LambdaMethodCallPartParameter extends AbstractElement<LambdaMethodCallPartParameter> implements FormalParameter {
		public final Link<FormalParameter> parameter = new Link<FormalParameter>(this) {
			@Override
			protected @Nullable FormalParameter tryLink(final String name) {
				// TODO parameter named like this link, or parameter with same position as this parameter (either from left or right, depending on where the dots are (if any)).
				return null;
			}
		};
		public @Nullable TypeUse type;
		
		@Override
		public @Nullable WordToken nameToken() {
			return parameter.getNameToken();
		}
		
		@Override
		protected LambdaMethodCallPartParameter parse() throws ParseException {
			if (!try_("var") && !(peekNext() instanceof LowercaseWordToken && (peekNext(',', 1, true) || peekNext(']', 1, true))))
				type = TypeExpressions.parse(this, true, true);
			parameter.setName(oneVariableIdentifierToken());
			return this;
		}
		
		@Override
		public InterpretedTypeUse interpretedType() {
			if (type != null)
				return type.staticallyKnownType();
			final FormalParameter param = parameter.get();
			if (param != null)
				return param.interpretedType();
			throw new InterpreterException("");
		}
		
		@Override
		public InterpretedParameterRedefinition interpreted() {
			return new InterpretedBrokkrParameterDefinition(this);
		}
		
	}
	
	public static class CodeGenerationStatement extends AbstractElement<CodeGenerationStatement> implements Statement {
		public final List<CodeGenerationToken> code = new ArrayList<>();
		public final List<Expression> expressions = new ArrayList<>();
		
		@Override
		protected CodeGenerationStatement parse() throws ParseException {
			CodeGenerationToken t = (CodeGenerationToken) next();
			assert t != null;
			code.add(t);
			while (true) {
				if (t.ended)
					break;
				expressions.add(Expressions.parse(this));
				final Token x = next();
				if (x == null || !(x instanceof CodeGenerationToken)) {
					expectedFatal("'$'");
					return this;
				}
				t = (CodeGenerationToken) x;
				code.add(t);
			}
			return this;
		}
		
		@Override
		public void interpret(final InterpreterContext context) {
			throw new InterpreterException("not implemented");
		}
	}
}
