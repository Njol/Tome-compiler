package ch.njol.brokkr.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTExpressions.ASTAccess;
import ch.njol.brokkr.ast.ASTExpressions.ASTAccessExpression;
import ch.njol.brokkr.ast.ASTExpressions.ASTBlock;
import ch.njol.brokkr.ast.ASTExpressions.ASTDirectAttributeAccess;
import ch.njol.brokkr.ast.ASTExpressions.ASTTypeExpressions;
import ch.njol.brokkr.ast.ASTExpressions.ASTVariableOrUnqualifiedAttributeUse;
import ch.njol.brokkr.ast.ASTInterfaces.ASTAttribute;
import ch.njol.brokkr.ast.ASTInterfaces.ASTElementWithVariables;
import ch.njol.brokkr.ast.ASTInterfaces.ASTExpression;
import ch.njol.brokkr.ast.ASTInterfaces.ASTLocalVariable;
import ch.njol.brokkr.ast.ASTInterfaces.ASTTypeUse;
import ch.njol.brokkr.ast.ASTMembers.ASTAttributeDeclaration;
import ch.njol.brokkr.ast.ASTMembers.ASTPostcondition;
import ch.njol.brokkr.ast.ASTMembers.ASTPrecondition;
import ch.njol.brokkr.compiler.ParseException;
import ch.njol.brokkr.compiler.Token;
import ch.njol.brokkr.compiler.Token.CodeGenerationToken;
import ch.njol.brokkr.compiler.Token.LowercaseWordToken;
import ch.njol.brokkr.compiler.Token.WordToken;
import ch.njol.brokkr.interpreter.InterpretedNormalObject;
import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeCodeGenerationResult;
import ch.njol.brokkr.ir.definitions.IRAttributeRedefinition;
import ch.njol.brokkr.ir.definitions.IRBrokkrLocalVariable;
import ch.njol.brokkr.ir.definitions.IRParameterDefinition;
import ch.njol.brokkr.ir.definitions.IRParameterRedefinition;
import ch.njol.brokkr.ir.definitions.IRResultRedefinition;
import ch.njol.brokkr.ir.definitions.IRVariableOrAttributeRedefinition;
import ch.njol.brokkr.ir.definitions.IRVariableRedefinition;
import ch.njol.brokkr.ir.expressions.IRAttributeAccess;
import ch.njol.brokkr.ir.expressions.IRClosure;
import ch.njol.brokkr.ir.expressions.IRExpression;
import ch.njol.brokkr.ir.expressions.IRThis;
import ch.njol.brokkr.ir.expressions.IRUnknownExpression;
import ch.njol.brokkr.ir.expressions.IRVariableAssignment;
import ch.njol.brokkr.ir.statements.IRCodeGenerationStatement;
import ch.njol.brokkr.ir.statements.IRExpressionStatement;
import ch.njol.brokkr.ir.statements.IRReturn;
import ch.njol.brokkr.ir.statements.IRStatement;
import ch.njol.brokkr.ir.statements.IRStatementList;
import ch.njol.brokkr.ir.statements.IRUnknownStatement;
import ch.njol.brokkr.ir.statements.IRVariableDeclaration;
import ch.njol.brokkr.ir.uses.IRTypeUse;
import ch.njol.brokkr.ir.uses.IRUnknownTypeUse;
import ch.njol.util.StringUtils;

public class ASTStatements {
	
	public static interface ASTStatement extends ASTElement {
		
		static ASTElement parse_(final AbstractASTElement<?> parent, final boolean allowExpressionAtEnd) throws ParseException {
//			if (parent.peek('{'))
//				throw new ParseException("A block is not allowed here", parent.in.getOffset(), parent.in.getOffset() + 1);
//			if (parent.peekNext('{'))
//			return parent.one(Block.class);
			if (parent.peekNext() instanceof CodeGenerationToken)
				return parent.one(ASTCodeGenerationStatement.class);
			if (parent.peekNext("$="))
				return parent.one(ASTCodeGenerationCallStatement.class);
			if (parent.peekNext("return"))
				return parent.one(ASTReturn.class);
//			if (parent.peekNext("assert"))
//				return parent.one(Assert.class);
			if (parent.peekNext("requires"))
				return parent.one(ASTPrecondition.class);
			if (parent.peekNext("ensures"))
				return parent.one(ASTPostcondition.class);
			if (parent.peekNext("var")) // <Type> var [= ...]; is handled below
				return parent.one(ASTVariableDeclarations.class);
			
			// TODO ErrorHandlingStatement
			
			// TODO tuple variable assignment, e.g. [[ [a,b] = method(); ]] or [[ [a,b] = [b,a]; ]]
			
			final ASTExpression expr = ASTExpressions.parse(parent);
			if (parent.peekNext(';'))
				return parent.one(new ASTExpressionStatement(expr));
			if (allowExpressionAtEnd && parent.peekNext() == null)
				return expr;
			if (expr instanceof ASTTypeUse) {
				// variable declaration
				return parent.one(new ASTVariableDeclarations((ASTTypeUse) expr));
			}
			
			if (expr instanceof ASTVariableOrUnqualifiedAttributeUse)
				return parent.one(new ASTLambdaMethodCall((ASTVariableOrUnqualifiedAttributeUse) expr));
			final ASTAccess a;
			if (expr instanceof ASTAccessExpression && !((ASTAccessExpression) expr).meta && (a = ((ASTAccessExpression) expr).access) instanceof ASTDirectAttributeAccess)
				return parent.one(new ASTLambdaMethodCall((ASTAccessExpression) expr, (ASTDirectAttributeAccess) a));
			
			return parent.one(new ASTExpressionStatement(expr));
		}
		
		public static ASTElement parseWithExpression(final AbstractASTElement<?> parent) throws ParseException {
			return parse_(parent, true);
		}
		
		public static ASTStatement parse(final AbstractASTElement<?> parent) throws ParseException {
			return (ASTStatement) parse_(parent, false);
		}
		
		// TODO remove
//		public default void interpret(final InterpreterContext context) {
//			getIR().interpret(context);
//		}
		
		public IRStatement getIR();
		
	}
	
	public static class ASTExpressionStatement extends AbstractASTElement<ASTExpressionStatement> implements ASTStatement {
		public final ASTExpression expression;
		
		public ASTExpressionStatement(final ASTExpression expression) {
			this.expression = expression;
			expression.setParent(this);
		}
		
		@Override
		public String toString() {
			return expression + ";";
		}
		
		@Override
		protected ASTExpressionStatement parse() throws ParseException {
			one(';');
//			expectedFatal("';' to complete statement"); // TODO better error message
			return this;
		}
		
		@Override
		public IRStatement getIR() {
			return new IRExpressionStatement(expression.getIR());
		}
	}
	
	public static class ASTCodeGenerationCallStatement extends AbstractASTElement<ASTCodeGenerationCallStatement> implements ASTStatement {
		public @Nullable ASTExpression code;
		
		@Override
		public String toString() {
			return "$= " + code + ";";
		}
		
		@Override
		protected ASTCodeGenerationCallStatement parse() throws ParseException {
			one("$=");
			until(() -> {
				code = ASTExpressions.parse(this);
			}, ';', false);
			return this;
		}
		
		@Override
		public IRStatement getIR() {
			final ASTExpression code = this.code;
			if (code == null)
				return new IRUnknownStatement("Snytax error. proper sytnax: [$= some_expression;]", this);
			try {
				final InterpretedObject result = code.getIR().interpret(new InterpreterContext(getIRContext(), (InterpretedNormalObject) null));
				if (!(result instanceof InterpretedNativeCodeGenerationResult))
					return new IRUnknownStatement("Must call a code generation template", this);
				return ((InterpretedNativeCodeGenerationResult) result).parseStatements(this);
			} catch (final InterpreterException e) {
				return new IRUnknownStatement("" + e.getMessage(), this);
			}
		}
	}
	
	public static class ASTReturn extends AbstractASTElement<ASTReturn> implements ASTStatement {
		private final boolean withReturn;
		
		public ASTReturn() {
			this(true);
		}
		
		public ASTReturn(final boolean withReturn) {
			this.withReturn = withReturn;
		}
		
		public ASTLink<IRResultRedefinition> error = new ASTLink<IRResultRedefinition>(this) {
			@Override
			protected @Nullable IRResultRedefinition tryLink(final String name) {
				final ASTAttributeDeclaration attribute = getParentOfType(ASTAttributeDeclaration.class);
				return attribute == null ? null : attribute.getResult(name);
			}
		};
		List<ASTReturnResult> results = new ArrayList<>();
		
		@Override
		public String toString() {
			return "return " + StringUtils.join(results, ", ");
		}
		
		@Override
		protected ASTReturn parse() throws ParseException {
			if (withReturn)
				one("return");
			until(() -> {
				if (try_('#')) {
					error.setName(oneVariableIdentifierToken());
					tryGroup('(', () -> {
						do {
							results.add(one(ASTReturnResult.class)); // TODO are these also ReturnResults?
						} while (try_(','));
					}, ')');
				} else {
					do {
						results.add(one(ASTReturnResult.class));
					} while (try_(','));
				}
			}, ';', true);
			return this;
		}
		
		@Override
		public IRStatement getIR() {
			if (error.getName() != null) {
				// TODO
			}
			final List<IRStatement> statements = new ArrayList<>();
			for (final ASTReturnResult r : results) {
				final IRResultRedefinition result = r.result.get();
				final ASTExpression value = r.value;
				if (result != null && value != null) {
					statements.add(new IRExpressionStatement(new IRVariableAssignment(result.definition(), value.getIR())));
				}
			}
			statements.add(new IRReturn(getIRContext()));
			return new IRStatementList(getIRContext(), statements);
		}
	}
	
	public static class ASTReturnResult extends AbstractASTElement<ASTReturnResult> {
		public ASTLink<IRResultRedefinition> result = new ASTLink<IRResultRedefinition>(this) {
			@Override
			protected @Nullable IRResultRedefinition tryLink(final String name) {
				final ASTAttribute fa = getParentOfType(ASTAttribute.class);
				if (fa == null)
					return null;
				final IRAttributeRedefinition attribute = fa.getIR();
				return attribute.getResultByName(name);
			}
		};
		public @Nullable ASTExpression value;
		
		@Override
		public String toString() {
			return (result.getName() != null ? result.getName() + " " : "") + value;
		}
		
		@Override
		protected ASTReturnResult parse() throws ParseException {
			if (peekNext() instanceof LowercaseWordToken && peekNext(':', 1, true)) {
				result.setName(oneVariableIdentifierToken());
				next(); // skip ':'
			}
			// TODO what to link without a name?
			value = ASTExpressions.parse(this);
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
		public @Nullable IRObject interpret(InterpreterContext context) {
			IRObject object = expression.interpret(context);
			return null;
		}
	}*/
	
	public static class ASTVariableDeclarations extends AbstractASTElement<ASTVariableDeclarations> implements ASTStatement {
		public @Nullable ASTTypeUse type;
		public List<ASTVariableDeclarationsVariable> variables = new ArrayList<>();
		
		public ASTVariableDeclarations() {}
		
		public ASTVariableDeclarations(final ASTTypeUse type) {
			this.type = type;
			type.setParent(this);
		}
		
		@Override
		public String toString() {
			return (type == null ? "var" : type) + " " + StringUtils.join(variables, ", ") + ";";
		}
		
		@Override
		protected ASTVariableDeclarations parse() throws ParseException {
			until(() -> {
				if (type == null)
					one("var");
				do {
					variables.add(one(ASTVariableDeclarationsVariable.class));
				} while (try_(','));
			}, ';', false);
			return this;
		}
		
		@Override
		public IRStatement getIR() {
			return new IRStatementList(getIRContext(), variables.stream().map(v -> v.getIRDeclaration()).collect(Collectors.toList()));
		}
	}
	
	public static class ASTVariableDeclarationsVariable extends AbstractASTElement<ASTVariableDeclarationsVariable> implements ASTLocalVariable {
		public @Nullable LowercaseWordToken nameToken;
		public @Nullable ASTExpression initialValue;
		
		@Override
		public @Nullable WordToken nameToken() {
			return nameToken;
		}
		
		@Override
		public String toString() {
			return nameToken + (initialValue == null ? "" : " = " + initialValue);
		}
		
		@Override
		protected ASTVariableDeclarationsVariable parse() throws ParseException {
			nameToken = oneVariableIdentifierToken();
			if (try_('='))
				initialValue = ASTExpressions.parse(this);
			return this;
		}
		
		@Override
		public IRTypeUse getIRType() {
			final ASTVariableDeclarations variableDeclarations = (ASTVariableDeclarations) parent;
			if (variableDeclarations == null)
				return new IRUnknownTypeUse(getIRContext());
			final ASTTypeUse typeUse = variableDeclarations.type;
			if (typeUse == null) {
				if (initialValue != null)
					return initialValue.getIRType();
				return new IRUnknownTypeUse(getIRContext()); // FIXME semantics of inferred types? just use the supertype of any assignment?
			}
			return typeUse.getIR();
		}
		
		@Override
		public IRVariableRedefinition getIR() {
			return new IRBrokkrLocalVariable(this);
		}
		
		public IRVariableDeclaration getIRDeclaration() {
			return new IRVariableDeclaration(getIR());
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
	
	public static class ASTLambdaMethodCall extends AbstractASTElement<ASTStatement> implements ASTStatement {
		public @Nullable ASTExpression target;
		public ASTLink<? extends IRVariableOrAttributeRedefinition> method;
		public List<ASTLambdaMethodCallPart> parts = new ArrayList<>();
		private final boolean withSemicolon;
		
		public ASTLambdaMethodCall(final ASTVariableOrUnqualifiedAttributeUse method) {
			this(method, true);
		}
		
		public ASTLambdaMethodCall(final ASTVariableOrUnqualifiedAttributeUse method, final boolean withSemicolon) {
			target = null;
			method.setParent(this);
			this.method = method.link;
			this.withSemicolon = withSemicolon;
		}
		
		public ASTLambdaMethodCall(final ASTAccessExpression target, final ASTDirectAttributeAccess methodAccess) {
			assert methodAccess.parent() == target;
			this.target = target;
			target.setParent(this);
			method = methodAccess.attribute;
			withSemicolon = true;
		}
		
		@Override
		public String toString() {
			return "<lambda method call>"; // TODO
		}
		
		@Override
		protected ASTStatement parse() throws ParseException {
			final boolean[] first = {true};
			repeatUntil(() -> {
				parts.add(one(new ASTLambdaMethodCallPart(!first[0])));
				first[0] = false;
			}, ';', false, withSemicolon);
			return this;
		}
		
		@Override
		public IRStatement getIR() {
			final IRExpression target = this.target != null ? this.target.getIR() : IRThis.makeNew(this);
			final IRVariableOrAttributeRedefinition attribute = method.get();
			if (!(attribute instanceof IRAttributeRedefinition)) {
				final WordToken m = method.getNameToken();
				return new IRUnknownStatement("Must be a method", m == null ? this : m);
			}
			final Map<IRParameterDefinition, IRExpression> arguments = new HashMap<>();
			for (final ASTLambdaMethodCallPart part : parts) {
				final List<IRVariableRedefinition> parameters = new ArrayList<>();
				part.parameters.forEach(p -> parameters.add(p.getIR()));
				final IRParameterRedefinition param = part.parameter.get();
				if (param != null)
					arguments.put(param.definition(), new IRClosure(parameters,
							part.expression != null ? part.expression.getIR() : new IRUnknownExpression("Syntax error. Proper syntax: [[name [optional, params] {...}]] or [[name [optional, params] nextMethod {...}]]", this)));
			}
			return new IRExpressionStatement(new IRAttributeAccess(target, (IRAttributeRedefinition) attribute, arguments, false, false, false));
		}
	}
	
	// TODO allow to combine multiple parts? (e.g. x {} y, z {}')
	public static class ASTLambdaMethodCallPart extends AbstractASTElement<ASTLambdaMethodCallPart> implements ASTElementWithVariables {
		public ASTLink<IRParameterRedefinition> parameter = new ASTLink<IRParameterRedefinition>(this) {
			@Override
			protected @Nullable IRParameterRedefinition tryLink(final String name) {
				@SuppressWarnings("null")
				final IRVariableOrAttributeRedefinition method = ((ASTLambdaMethodCall) parent).method.get();
				return method == null || !(method instanceof IRAttributeRedefinition) ? null : ((IRAttributeRedefinition) method).getParameterByName(name);
			}
		};
		public List<ASTLambdaMethodCallPartParameter> parameters = new ArrayList<>();
		public @Nullable ASTExpression expression;
		
		@Override
		public List<? extends IRVariableRedefinition> allVariables() {
			return parameters.stream().map(p -> p.getIR()).collect(Collectors.toList());
		}
		
		private final boolean withName;
		
		public ASTLambdaMethodCallPart(final boolean withName) {
			this.withName = withName;
		}
		
		@Override
		public String toString() {
			return "<lambda method call part>"; // TODO
		}
		
		@Override
		protected ASTLambdaMethodCallPart parse() throws ParseException {
			if (withName)
				parameter.setName(oneVariableIdentifierToken());
			tryGroup('[', () -> {
				do {
					parameters.add(one(ASTLambdaMethodCallPartParameter.class));
				} while (try_(','));
			}, ']');
			if (peekNext('{')) {
				expression = one(ASTBlock.class);
			} else if (withName) {
				// TODO limit this some more? this only exists for [else if]
				expression = new ASTBlock(one(new ASTLambdaMethodCall(one(ASTVariableOrUnqualifiedAttributeUse.class), false)));
				expression.setParent(this);
			}
			return this;
		}
	}
	
	/**
	 * TODO what exactly is this? and is it a parameter? or just a link to one (in which case the interface might need to be removed - still should be a linkable local variable
	 * though, as it is now a variable in scope (and maybe has a different name too))
	 */
	public static class ASTLambdaMethodCallPartParameter extends AbstractASTElement<ASTLambdaMethodCallPartParameter> implements ASTLocalVariable {
		public final ASTLink<IRParameterRedefinition> parameter = new ASTLink<IRParameterRedefinition>(this) {
			@Override
			protected @Nullable IRParameterRedefinition tryLink(final String name) {
				// TODO parameter named like this link, or parameter with same position as this parameter (either from left or right, depending on where the dots are (if any)).
				return null;
			}
		};
		public @Nullable ASTTypeUse type;
		
		@Override
		public @Nullable WordToken nameToken() {
			return parameter.getNameToken();
		}
		
		@Override
		public String toString() {
			return "<lambda method call part parameter>"; // TODO
		}
		
		@Override
		protected ASTLambdaMethodCallPartParameter parse() throws ParseException {
			if (!try_("var") && !(peekNext() instanceof LowercaseWordToken && (peekNext(',', 1, true) || peekNext(']', 1, true))))
				type = ASTTypeExpressions.parse(this, true, true);
			parameter.setName(oneVariableIdentifierToken());
			return this;
		}
		
		@Override
		public IRTypeUse getIRType() {
			if (type != null)
				return type.getIR();
			final IRParameterRedefinition param = parameter.get();
			if (param != null)
				return param.type();
			return new IRUnknownTypeUse(getIRContext());
		}
		
		@Override
		public IRVariableRedefinition getIR() {
			return new IRBrokkrLocalVariable(this);
		}
		
	}
	
	/**
	 * A line of code that will generate a line of code when called (i.e. must always be in a code generation method)
	 * TODO allow this outside of code generation methods? i.e. just execute it statically in that case?
	 * -> actually, should use different syntax for 'code here' (currently [$=]) and 'code for later' (currently [$]), so that this statement is still only valid in code generation
	 * methods.
	 */
	public static class ASTCodeGenerationStatement extends AbstractASTElement<ASTCodeGenerationStatement> implements ASTStatement {
		// TODO make these a single list?
		public final List<CodeGenerationToken> code = new ArrayList<>();
		public final List<ASTExpression> expressions = new ArrayList<>();
		
		@Override
		public String toString() {
			return "$...$";
		}
		
		@Override
		protected ASTCodeGenerationStatement parse() throws ParseException {
			CodeGenerationToken t = (CodeGenerationToken) next();
			assert t != null;
			code.add(t);
			while (true) {
				if (t.ended)
					break;
				expressions.add(ASTExpressions.parse(this));
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
		public IRStatement getIR() {
			return new IRCodeGenerationStatement(getIRContext(), code, expressions.stream().map(e -> e.getIR()).collect(Collectors.toList()));
		}
	}
}
