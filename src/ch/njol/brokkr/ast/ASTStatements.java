package ch.njol.brokkr.ast;

import java.util.ArrayList;
import java.util.Collections;
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
import ch.njol.brokkr.ast.ASTInterfaces.ASTError;
import ch.njol.brokkr.ast.ASTInterfaces.ASTExpression;
import ch.njol.brokkr.ast.ASTInterfaces.ASTLocalVariable;
import ch.njol.brokkr.ast.ASTInterfaces.ASTTypeUse;
import ch.njol.brokkr.ast.ASTMembers.ASTMember;
import ch.njol.brokkr.ast.ASTMembers.ASTPostcondition;
import ch.njol.brokkr.ast.ASTMembers.ASTPrecondition;
import ch.njol.brokkr.compiler.ParseException;
import ch.njol.brokkr.compiler.Token;
import ch.njol.brokkr.compiler.Token.CodeGenerationToken;
import ch.njol.brokkr.compiler.Token.LowercaseWordToken;
import ch.njol.brokkr.compiler.Token.WordToken;
import ch.njol.brokkr.interpreter.InterpretedNativeClosure;
import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.ir.definitions.IRAttributeRedefinition;
import ch.njol.brokkr.ir.definitions.IRBrokkrLocalVariable;
import ch.njol.brokkr.ir.definitions.IRMemberRedefinition;
import ch.njol.brokkr.ir.definitions.IRParameterDefinition;
import ch.njol.brokkr.ir.definitions.IRParameterRedefinition;
import ch.njol.brokkr.ir.definitions.IRResultRedefinition;
import ch.njol.brokkr.ir.definitions.IRVariableOrAttributeRedefinition;
import ch.njol.brokkr.ir.definitions.IRVariableRedefinition;
import ch.njol.brokkr.ir.nativetypes.IRTuple;
import ch.njol.brokkr.ir.nativetypes.IRTuple.IRNativeTupleValueAndEntry;
import ch.njol.brokkr.ir.nativetypes.IRTuple.IRNormalTuple;
import ch.njol.brokkr.ir.nativetypes.IRTuple.IRTypeTuple;
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
				return parent.one(ASTCodeGenerationCall.class);
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
		
		public void interpret(InterpreterContext context);
		
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
		public void interpret(final InterpreterContext context) {
			expression.interpret(context);
		}
	}
	
	public static class ASTCodeGenerationCall extends AbstractASTElement<ASTCodeGenerationCall> implements ASTMember, ASTStatement {
		public @Nullable ASTExpression code;
		
		@Override
		public boolean isInherited() {
			return false; // only generates code at the current location
		}
		
		@Override
		public String toString() {
			return "$= " + code + ";";
		}
		
		@Override
		protected ASTCodeGenerationCall parse() throws ParseException {
			one("$=");
			until(() -> {
				code = ASTExpressions.parse(this);
			}, ';', false);
			return this;
		}
		
		@Override
		public void interpret(final InterpreterContext context) {
			throw new InterpreterException("not implemented");
		}
		
		@Override
		public IRMemberRedefinition getIR() {
			throw new InterpreterException("not implemented");
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
		
		public ASTLink<ASTError> error = new ASTLink<ASTError>(this) {
			@Override
			protected @Nullable ASTError tryLink(final String name) {
				// TODO Auto-generated method stub
				return null;
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
		
		@SuppressWarnings("null")
		@Override
		public void interpret(final InterpreterContext context) {
			for (final ASTReturnResult r : results) {
				context.setLocalVariableValue(r.result.get().definition(), r.value.interpret(context));
			}
			context.isReturning = true;
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
		public void interpret(final InterpreterContext context) {
			for (final ASTVariableDeclarationsVariable v : variables) {
				context.defineLocalVariable(v.interpreted().definition(), v.initialValue != null ? v.initialValue.interpret(context) : null);
			}
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
				return new IRUnknownTypeUse();
			final ASTTypeUse typeUse = variableDeclarations.type;
			if (typeUse == null)
				return new IRUnknownTypeUse();
			return typeUse.staticallyKnownType();
		}
		
		@Override
		public IRVariableRedefinition interpreted() {
			return new IRBrokkrLocalVariable(this);
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
		
		public ASTLambdaMethodCall(final ASTVariableOrUnqualifiedAttributeUse method) {
			target = null;
			method.setParent(this);
			this.method = method.link;
		}
		
		public ASTLambdaMethodCall(final ASTAccessExpression target, final ASTDirectAttributeAccess methodAccess) {
			assert methodAccess.parent() == target;
			this.target = target;
			target.setParent(this);
			method = methodAccess.attribute;
		}
		
		@Override
		protected ASTStatement parse() throws ParseException {
			final boolean[] first = {true};
			repeatUntil(() -> {
				parts.add(one(new ASTLambdaMethodCallPart(!first[0])));
				first[0] = false;
			}, ';', false);
			return this;
		}
		
		@SuppressWarnings("null")
		@Override
		public void interpret(final InterpreterContext context) {
			final InterpretedObject object = target != null ? target.interpret(context) : context.getThisObject();
			final IRVariableOrAttributeRedefinition m = method.get();
			if (!(m instanceof IRAttributeRedefinition))
				throw new InterpreterException("not an attribute");
			final Map<IRParameterDefinition, InterpretedObject> arguments = new HashMap<>();
			for (final ASTLambdaMethodCallPart part : parts) {
				final List<IRParameterRedefinition> parameters = new ArrayList<>();
				part.parameters.forEach(p -> parameters.add(p.parameter.get()));
				final List<IRNativeTupleValueAndEntry> parameterTupleEntries = new ArrayList<>();
				for (int i = 0; i < parameters.size(); i++) {
					final IRParameterRedefinition p = parameters.get(i);
					parameterTupleEntries.add(new IRNativeTupleValueAndEntry(i, p.type().nativeClass(), p.name(), p.type()));
				}
				arguments.put(part.parameter.get().definition(), new InterpretedNativeClosure(new IRTypeTuple(parameterTupleEntries), new IRTypeTuple(Collections.EMPTY_LIST), false) {
					@Override
					protected IRTuple interpret(final IRTuple arguments) {
						arguments.entries.forEach(e -> context.defineLocalVariable(parameters.get(e.entry.index).definition(), e.value));
						part.block.interpret(context);
						return new IRNormalTuple(Collections.EMPTY_LIST);
					}
				});
			}
			((ASTAttribute) m).getIR().interpretDispatched(object, arguments, false);
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
		public @Nullable ASTBlock block;
		
		@Override
		public List<? extends IRVariableRedefinition> allVariables() {
			return parameters.stream().map(p -> p.interpreted()).collect(Collectors.toList());
		}
		
		private final boolean withName;
		
		public ASTLambdaMethodCallPart(final boolean withName) {
			this.withName = withName;
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
			block = one(ASTBlock.class);
			return this;
		}
	}
	
	/**
	 * TODO what exactly is this? and is it a parameter? or just a link to one (in which case the interface might need to be removed - still should be a linkable local variable
	 * though)
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
		protected ASTLambdaMethodCallPartParameter parse() throws ParseException {
			if (!try_("var") && !(peekNext() instanceof LowercaseWordToken && (peekNext(',', 1, true) || peekNext(']', 1, true))))
				type = ASTTypeExpressions.parse(this, true, true);
			parameter.setName(oneVariableIdentifierToken());
			return this;
		}
		
		@Override
		public IRTypeUse getIRType() {
			if (type != null)
				return type.staticallyKnownType();
			final IRParameterRedefinition param = parameter.get();
			if (param != null)
				return param.type();
			return new IRUnknownTypeUse();
		}
		
		@Override
		public IRVariableRedefinition interpreted() {
			return new IRBrokkrLocalVariable(this);
		}
		
	}
	
	public static class ASTCodeGenerationStatement extends AbstractASTElement<ASTCodeGenerationStatement> implements ASTStatement {
		public final List<CodeGenerationToken> code = new ArrayList<>();
		public final List<ASTExpression> expressions = new ArrayList<>();
		
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
		public void interpret(final InterpreterContext context) {
			throw new InterpreterException("not implemented");
		}
	}
}
