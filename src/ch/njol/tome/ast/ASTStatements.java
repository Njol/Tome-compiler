package ch.njol.tome.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTExpressions.ASTAccess;
import ch.njol.tome.ast.ASTExpressions.ASTAccessExpression;
import ch.njol.tome.ast.ASTExpressions.ASTBlock;
import ch.njol.tome.ast.ASTExpressions.ASTDirectAttributeAccess;
import ch.njol.tome.ast.ASTExpressions.ASTTypeExpressions;
import ch.njol.tome.ast.ASTExpressions.ASTVariableOrUnqualifiedAttributeUse;
import ch.njol.tome.ast.ASTInterfaces.ASTAttribute;
import ch.njol.tome.ast.ASTInterfaces.ASTElementWithVariables;
import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.ASTInterfaces.ASTLocalVariable;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeUse;
import ch.njol.tome.ast.ASTMembers.ASTAttributeDeclaration;
import ch.njol.tome.ast.ASTMembers.ASTPostcondition;
import ch.njol.tome.ast.ASTMembers.ASTPrecondition;
import ch.njol.tome.compiler.Token;
import ch.njol.tome.compiler.Token.CodeGenerationToken;
import ch.njol.tome.compiler.Token.LowercaseWordToken;
import ch.njol.tome.compiler.Token.WordOrSymbols;
import ch.njol.tome.compiler.Token.WordToken;
import ch.njol.tome.interpreter.InterpretedNormalObject;
import ch.njol.tome.interpreter.InterpretedObject;
import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.InterpreterException;
import ch.njol.tome.interpreter.nativetypes.InterpretedNativeCodeGenerationResult;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.definitions.IRBrokkrLocalVariable;
import ch.njol.tome.ir.definitions.IRParameterDefinition;
import ch.njol.tome.ir.definitions.IRParameterRedefinition;
import ch.njol.tome.ir.definitions.IRResultRedefinition;
import ch.njol.tome.ir.definitions.IRVariableOrAttributeRedefinition;
import ch.njol.tome.ir.definitions.IRVariableRedefinition;
import ch.njol.tome.ir.expressions.IRAttributeAccess;
import ch.njol.tome.ir.expressions.IRClosure;
import ch.njol.tome.ir.expressions.IRExpression;
import ch.njol.tome.ir.expressions.IRThis;
import ch.njol.tome.ir.expressions.IRUnknownExpression;
import ch.njol.tome.ir.expressions.IRVariableAssignment;
import ch.njol.tome.ir.statements.IRCodeGenerationStatement;
import ch.njol.tome.ir.statements.IRExpressionStatement;
import ch.njol.tome.ir.statements.IRReturn;
import ch.njol.tome.ir.statements.IRStatement;
import ch.njol.tome.ir.statements.IRStatementList;
import ch.njol.tome.ir.statements.IRUnknownStatement;
import ch.njol.tome.ir.statements.IRVariableDeclaration;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.ir.uses.IRUnknownTypeUse;
import ch.njol.tome.parser.Parser;
import ch.njol.util.StringUtils;

public class ASTStatements {
	
	public static interface ASTStatement extends ASTElement {
		
		static ASTElement parse_(final Parser parent, final boolean allowExpressionAtEnd) {
//			if (parent.peek('{'))
//				throw new ParseException("A block is not allowed here", parent.in.getOffset(), parent.in.getOffset() + 1);
//			if (parent.peekNext('{'))
//			return parent.one(Block.class);
			if (parent.peekNext() instanceof CodeGenerationToken)
				return ASTCodeGenerationStatement.parse(parent);
			if (parent.peekNext("$="))
				return ASTCodeGenerationCallStatement.parse(parent);
			if (parent.peekNext("return"))
				return ASTReturn.parse(parent, true);
//			if (parent.peekNext("assert"))
//				return parent.one(Assert.parse(parent);
			if (parent.peekNext("requires"))
				return ASTPrecondition.parse(parent);
			if (parent.peekNext("ensures"))
				return ASTPostcondition.parse(parent);
			if (parent.peekNext("var")) // <Type> var [= ...]; is handled below
				return ASTVariableDeclarations.parse(parent);
			
			// TODO ErrorHandlingStatement
			
			// TODO tuple variable assignment, e.g. [[ [a,b] = method(); ]] or [[ [a,b] = [b,a]; ]]
			
			Parser p = parent.start();
			final ASTExpression expr = ASTExpressions.parse(p);
			if (p.peekNext(';'))
				return ASTExpressionStatement.finishParsing(p, expr);
			if (allowExpressionAtEnd && p.peekNext() == null)
				return expr;
			if (expr instanceof ASTTypeUse) {
				// variable declaration
				return ASTVariableDeclarations.finishParsing(p, (ASTTypeUse) expr);
			}
			
			if (expr instanceof ASTVariableOrUnqualifiedAttributeUse)
				return ASTLambdaMethodCall.finishParsing(p, (ASTVariableOrUnqualifiedAttributeUse) expr, true);
			final ASTAccess a;
			if (expr instanceof ASTAccessExpression && !((ASTAccessExpression) expr).meta && (a = ((ASTAccessExpression) expr).access) instanceof ASTDirectAttributeAccess)
				return ASTLambdaMethodCall.finishParsing(p, (ASTAccessExpression) expr, (ASTDirectAttributeAccess) a, true);
			
			// this also complains about a missing semicolon
			return ASTExpressionStatement.finishParsing(p, expr);
		}
		
		public static ASTElement parseWithExpression(final Parser parent) {
			return parse_(parent, true);
		}
		
		public static ASTStatement parse(final Parser parent) {
			return (ASTStatement) parse_(parent, false);
		}
		
		// TODO remove
//		public default void interpret(final InterpreterContext context) {
//			getIR().interpret(context);
//		}
		
		public IRStatement getIR();
		
	}
	
	public static class ASTExpressionStatement extends AbstractASTElement implements ASTStatement {
		public final ASTExpression expression;
		
		public ASTExpressionStatement(final ASTExpression expression) {
			this.expression = expression;
		}
		
		@Override
		public String toString() {
			return expression + ";";
		}
		
		public static ASTExpressionStatement finishParsing(final Parser p, final ASTExpression expression) {
			final ASTExpressionStatement ast = new ASTExpressionStatement(expression);
			p.one(';');
//			expectedFatal("';' to complete statement"); // TODO better error message
			return p.done(ast);
		}
		
		@Override
		public IRStatement getIR() {
			return new IRExpressionStatement(expression.getIR());
		}
	}
	
	public static class ASTCodeGenerationCallStatement extends AbstractASTElement implements ASTStatement {
		public @Nullable ASTExpression code;
		
		@Override
		public String toString() {
			return "$= " + code + ";";
		}
		
		public static ASTCodeGenerationCallStatement parse(final Parser parent) {
			Parser p = parent.start();
			final ASTCodeGenerationCallStatement ast = new ASTCodeGenerationCallStatement();
			p.one("$=");
			p.until(() -> {
				ast.code = ASTExpressions.parse(p);
			}, ';', false);
			return p.done(ast);
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
	
	public static class ASTReturn extends AbstractASTElement implements ASTStatement {
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
		
		public static ASTReturn parse(final Parser parent, final boolean withReturn) {
			Parser p = parent.start();
			final ASTReturn ast = new ASTReturn();
			if (withReturn)
				p.one("return");
			p.until(() -> {
				if (p.try_('#')) {
					ast.error.setName(p.oneVariableIdentifierToken());
					p.tryGroup('(', () -> {
						do {
							ast.results.add(ASTReturnResult.parse(p)); // TODO are these also ReturnResults?
						} while (p.try_(','));
					}, ')');
				} else {
					do {
						ast.results.add(ASTReturnResult.parse(p));
					} while (p.try_(','));
				}
			}, ';', true);
			return p.done(ast);
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
	
	public static class ASTReturnResult extends AbstractASTElement {
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
		
		public static ASTReturnResult parse(final Parser parent) {
			Parser p = parent.start();
			final ASTReturnResult ast = new ASTReturnResult();
			if (p.peekNext() instanceof LowercaseWordToken && p.peekNext(':', 1, true)) {
				ast.result.setName(p.oneVariableIdentifierToken());
				p.next(); // skip ':'
			}
			// TODO what to link without a name?
			ast.value = ASTExpressions.parse(p);
			return p.done(ast);
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
		protected Assert parse() {
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
	
	public static class ASTVariableDeclarations extends AbstractASTElement implements ASTStatement {
		public @Nullable ASTTypeUse type;
		public List<ASTVariableDeclarationsVariable> variables = new ArrayList<>();
		
		public ASTVariableDeclarations() {}
		
		public ASTVariableDeclarations(final ASTTypeUse type) {
			this.type = type;
		}
		
		@Override
		public String toString() {
			return (type == null ? "var" : type) + " " + StringUtils.join(variables, ", ") + ";";
		}

		public static ASTVariableDeclarations parse(final Parser parent) {
			return finishParsing(parent.start(), null);
		}
		public static ASTVariableDeclarations finishParsing(final Parser p, @Nullable final ASTTypeUse type) {
			final ASTVariableDeclarations ast = new ASTVariableDeclarations();
			if (type != null)
				ast.type = type;
			p.until(() -> {
				if (type == null)
					p.one("var");
				do {
					ast.variables.add(ASTVariableDeclarationsVariable.parse(p));
				} while (p.try_(','));
			}, ';', false);
			return p.done(ast);
		}
		
		@Override
		public IRStatement getIR() {
			return new IRStatementList(getIRContext(), variables.stream().map(v -> v.getIRDeclaration()).collect(Collectors.toList()));
		}
	}
	
	public static class ASTVariableDeclarationsVariable extends AbstractASTElement implements ASTLocalVariable {
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
		
		public static ASTVariableDeclarationsVariable parse(final Parser parent) {
			Parser p = parent.start();
			final ASTVariableDeclarationsVariable ast = new ASTVariableDeclarationsVariable();
			ast.nameToken = p.oneVariableIdentifierToken();
			if (p.try_('='))
				ast.initialValue = ASTExpressions.parse(p);
			return p.done(ast);
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
	
	public static class ASTLambdaMethodCall extends AbstractASTElement implements ASTStatement {
		public @Nullable ASTExpression target;
		public ASTLink<? extends IRVariableOrAttributeRedefinition> method;
		public List<ASTLambdaMethodCallPart> parts = new ArrayList<>();
		
		public ASTLambdaMethodCall(final ASTVariableOrUnqualifiedAttributeUse method) {
			target = null;
			this.method = method.link;
		}
		
		public ASTLambdaMethodCall(final ASTAccessExpression target, final ASTDirectAttributeAccess methodAccess) {
			assert methodAccess.parent() == target;
			this.target = target;
			method = methodAccess.attribute;
		}
		
		@Override
		public String toString() {
			return "<lambda method call>"; // TODO
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
			final IRVariableOrAttributeRedefinition attribute = method.get();
			if (!(attribute instanceof IRAttributeRedefinition)) {
				final WordOrSymbols m = method.getNameToken();
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
	public static class ASTLambdaMethodCallPart extends AbstractASTElement implements ASTElementWithVariables {
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
		
		@Override
		public String toString() {
			return "<lambda method call part>"; // TODO
		}
		
		public static ASTLambdaMethodCallPart parse(final Parser parent, final boolean withName) {
			Parser p = parent.start();
			final ASTLambdaMethodCallPart ast = new ASTLambdaMethodCallPart();
			if (withName)
				ast.parameter.setName(p.oneVariableIdentifierToken());
			p.tryGroup('[', () -> {
				do {
					ast.parameters.add(ASTLambdaMethodCallPartParameter.parse(p));
				} while (p.try_(','));
			}, ']');
			if (p.peekNext('{')) {
				ast.expression = ASTBlock.parse(p);
			} else if (withName) {
				// TODO limit this some more? this only exists for [else if]
				Parser blockParser = parent.start();
				Parser callParser = blockParser.start();
				ASTVariableOrUnqualifiedAttributeUse variableOrUnqualifiedAttributeUse = ASTVariableOrUnqualifiedAttributeUse.parse(callParser);
				ASTLambdaMethodCall call = ASTLambdaMethodCall.finishParsing(callParser, variableOrUnqualifiedAttributeUse, false);
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
		public final ASTLink<IRParameterRedefinition> parameter = new ASTLink<IRParameterRedefinition>(this) {
			@Override
			protected @Nullable IRParameterRedefinition tryLink(final String name) {
				// TODO parameter named like this link, or parameter with same position as this parameter (either from left or right, depending on where the dots are (if any)).
				return null;
			}
		};
		public @Nullable ASTTypeUse type;
		
		@Override
		public @Nullable WordOrSymbols nameToken() {
			return parameter.getNameToken();
		}
		
		@Override
		public String toString() {
			return "<lambda method call part parameter>"; // TODO
		}
		
		public static ASTLambdaMethodCallPartParameter parse(final Parser parent) {
			Parser p = parent.start();
			final ASTLambdaMethodCallPartParameter ast = new ASTLambdaMethodCallPartParameter();
			if (!p.try_("var") && !(p.peekNext() instanceof LowercaseWordToken && (p.peekNext(',', 1, true) || p.peekNext(']', 1, true))))
				ast.type = ASTTypeExpressions.parse(p, true, true);
			ast.parameter.setName(p.oneVariableIdentifierToken());
			return p.done(ast);
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
	public static class ASTCodeGenerationStatement extends AbstractASTElement implements ASTStatement {
		// TODO make these a single list?
		public final List<CodeGenerationToken> code = new ArrayList<>();
		public final List<ASTExpression> expressions = new ArrayList<>();
		
		@Override
		public String toString() {
			return "$...$";
		}
		
		public static ASTCodeGenerationStatement parse(final Parser parent) {
			Parser p = parent.start();
			final ASTCodeGenerationStatement ast = new ASTCodeGenerationStatement();
			CodeGenerationToken t = (CodeGenerationToken) p.next();
			assert t != null;
			ast.code.add(t);
			while (true) {
				if (t.ended)
					break;
				ast.expressions.add(ASTExpressions.parse(p));
				final Token x = p.next();
				if (x == null || !(x instanceof CodeGenerationToken)) {
					p.expectedFatal("'$'");
					return p.done(ast);
				}
				t = (CodeGenerationToken) x;
				ast.code.add(t);
			}
			return p.done(ast);
		}
		
		@Override
		public IRStatement getIR() {
			return new IRCodeGenerationStatement(this);
		}
	}
}
