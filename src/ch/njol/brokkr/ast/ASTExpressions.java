package ch.njol.brokkr.ast;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTInterfaces.ASTAttribute;
import ch.njol.brokkr.ast.ASTInterfaces.ASTElementWithVariables;
import ch.njol.brokkr.ast.ASTInterfaces.ASTError;
import ch.njol.brokkr.ast.ASTInterfaces.ASTExpression;
import ch.njol.brokkr.ast.ASTInterfaces.ASTGenericParameter;
import ch.njol.brokkr.ast.ASTInterfaces.ASTLocalVariable;
import ch.njol.brokkr.ast.ASTInterfaces.ASTParameter;
import ch.njol.brokkr.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.brokkr.ast.ASTInterfaces.ASTTypeExpression;
import ch.njol.brokkr.ast.ASTInterfaces.ASTTypeUse;
import ch.njol.brokkr.ast.ASTInterfaces.TypedASTElement;
import ch.njol.brokkr.ast.ASTMembers.ASTGenericTypeDeclaration;
import ch.njol.brokkr.ast.ASTMembers.ASTMember;
import ch.njol.brokkr.ast.ASTStatements.ASTStatement;
import ch.njol.brokkr.ast.ASTStatements.ASTVariableDeclarations;
import ch.njol.brokkr.ast.ASTStatements.ASTVariableDeclarationsVariable;
import ch.njol.brokkr.ast.ASTTopLevelElements.ASTBrokkrFile;
import ch.njol.brokkr.common.DebugString;
import ch.njol.brokkr.common.Exclusivity;
import ch.njol.brokkr.common.Kleenean;
import ch.njol.brokkr.common.Modifiability;
import ch.njol.brokkr.common.Optional;
import ch.njol.brokkr.compiler.Module;
import ch.njol.brokkr.compiler.ParseException;
import ch.njol.brokkr.compiler.Token;
import ch.njol.brokkr.compiler.Token.CommentToken;
import ch.njol.brokkr.compiler.Token.LowercaseWordToken;
import ch.njol.brokkr.compiler.Token.NumberToken;
import ch.njol.brokkr.compiler.Token.StringToken;
import ch.njol.brokkr.compiler.Token.SymbolToken;
import ch.njol.brokkr.compiler.Token.SymbolsWordToken;
import ch.njol.brokkr.compiler.Token.UppercaseWordToken;
import ch.njol.brokkr.compiler.Token.WhitespaceToken;
import ch.njol.brokkr.compiler.Token.WordToken;
import ch.njol.brokkr.interpreter.InterpretedNormalObject;
import ch.njol.brokkr.interpreter.InterpretedNullConstant;
import ch.njol.brokkr.interpreter.InterpretedNullConstant.IRNativeNullClass;
import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.ir.definitions.IRAttributeDefinition;
import ch.njol.brokkr.ir.definitions.IRAttributeRedefinition;
import ch.njol.brokkr.ir.definitions.IRBrokkrClass;
import ch.njol.brokkr.ir.definitions.IRBrokkrLocalVariable;
import ch.njol.brokkr.ir.definitions.IRGenericTypeDefinition;
import ch.njol.brokkr.ir.definitions.IRGenericTypeRedefinition;
import ch.njol.brokkr.ir.definitions.IRParameterDefinition;
import ch.njol.brokkr.ir.definitions.IRParameterRedefinition;
import ch.njol.brokkr.ir.definitions.IRTypeDefinition;
import ch.njol.brokkr.ir.definitions.IRTypeDefinitionOrGenericTypeRedefinition;
import ch.njol.brokkr.ir.definitions.IRVariableDefinition;
import ch.njol.brokkr.ir.definitions.IRVariableOrAttributeRedefinition;
import ch.njol.brokkr.ir.definitions.IRVariableRedefinition;
import ch.njol.brokkr.ir.nativetypes.IRTuple;
import ch.njol.brokkr.ir.nativetypes.IRTuple.IRNativeTupleValueAndEntry;
import ch.njol.brokkr.ir.nativetypes.IRTuple.IRTypeTuple;
import ch.njol.brokkr.ir.uses.IRAndTypeUse;
import ch.njol.brokkr.ir.uses.IRAttributeUse;
import ch.njol.brokkr.ir.uses.IRClassUse;
import ch.njol.brokkr.ir.uses.IRGenericTypeUse;
import ch.njol.brokkr.ir.uses.IROrTypeUse;
import ch.njol.brokkr.ir.uses.IRSimpleClassUse;
import ch.njol.brokkr.ir.uses.IRSimpleTypeUse;
import ch.njol.brokkr.ir.uses.IRTypeUse;
import ch.njol.brokkr.ir.uses.IRUnknownTypeUse;
import ch.njol.util.StringUtils;

public class ASTExpressions {
	
	public static ASTExpression parse(final AbstractASTElement<?> parent) throws ParseException {
		return parse(parent, true);
	}
	
	public static ASTExpression parse(final AbstractASTElement<?> parent, final boolean allowComparisons) throws ParseException {
		
		if (parent.peekNext('{'))
			return parent.one(ASTBlock.class);
		if (parent.peekNext("create"))
			return parent.one(ASTAnonymousObject.class);
		
		// TODO "[params] ->" syntax
		if (parent.peekNext("var") && parent.peekNext(1, true) instanceof LowercaseWordToken && parent.peekNext("->", 2, true))
			return parent.one(ASTLambda.class);
		if (parent.peekNext() instanceof LowercaseWordToken && parent.peekNext("->", 1, true))
			return parent.one(new ASTLambda(parent.one(new ASTLambdaParameter(true))));
		if (parent.peekNext('[')) {
			Token t;
			int i = 0;
			boolean foundClosing = false;
			while (true) {
				t = parent.peekNext(i, false);
				if (t == null)
					break;
				if (t instanceof SymbolToken && ((SymbolToken) t).symbol == ']') {
					foundClosing = true;
				} else if (foundClosing && !(t instanceof WhitespaceToken || t instanceof CommentToken)) {
					if (parent.peekNext("->", i, false)) {
						return parent.one(ASTLambda.class);
					} else {
						break;
					}
				}
				i++;
			}
		}
		
		final ASTExpression expr = parent.one(new ASTOperatorExpression(allowComparisons));
		WordToken assignmentOp;
		SymbolToken sym;
		if ((assignmentOp = parent.try2("=", "+=", "-=", "*=", "/=", "&=", "|=")) != null) {
			// TODO this is way too complicated - make it simpler if possible (by changing the 'expr' line above/parsing assignment first)
			// TODO directly determine if a variable is local or an unqualified attribute?
			if (expr instanceof ASTVariableOrUnqualifiedAttributeUse) {
				final ASTVariableOrUnqualifiedAttributeUse varOrAttribute = (ASTVariableOrUnqualifiedAttributeUse) expr;
				if (!varOrAttribute.arguments.isEmpty()) {
					parent.errorFatal("Left-hand side of an assignment must be a variable or a field", expr.regionStart(), expr.regionLength());
					return expr;
				}
				expr.setParent(null);
				final ASTLocalVariableOrUnqualifiedAttributeAssignment a = new ASTLocalVariableOrUnqualifiedAttributeAssignment((SymbolsWordToken) assignmentOp, varOrAttribute);
				parent.one(a);
				return a;
			} else if (expr instanceof ASTAccessExpression && !((ASTAccessExpression) expr).meta) {// && ((ASTAccessExpression) expr).access instanceof ASTDirectAttributeAccess) {
				final ASTAccessExpression e = (ASTAccessExpression) expr;
				final ASTDirectAttributeAccess da = e.access;
				assert da != null;
				if (e.meta || e.nullSafe /*|| da.negated*/ || da.allResults || !da.arguments.isEmpty()) {
					parent.errorFatal("Left-hand side of an assignment must be a variable or a field", expr.regionStart(), expr.regionLength());
					return expr;
				}
				e.setParent(null);
				da.setParent(null);
				final ASTAttributeAssignment assignment = new ASTAttributeAssignment((SymbolsWordToken) assignmentOp, e.target, da.attribute);
				parent.one(assignment);
				return assignment;
			} else {
				if (expr.regionLength() > 0)
					parent.errorFatal("Left-hand side of an assignment must be a variable or a field", expr.regionStart(), expr.regionLength());
				return expr;
			}
		} else if ((sym = parent.try2('?')) != null) { // no need to check for '?.' and '?~' as those are parsed before in OperatorExpression
			final ASTTernaryIf i = new ASTTernaryIf(expr);
			sym.setParent(i);
			parent.one(i);
			return i;
		} else if ((sym = parent.try2('#')) != null) {
			final ASTErrorHandlingExpression eh = new ASTErrorHandlingExpression(expr);
			sym.setParent(eh);
			parent.one(eh);
			return eh;
		} else if (expr instanceof ASTTypeUse && parent.peekNext() instanceof LowercaseWordToken && parent.peekNext("->", 1, true)) {
			return parent.one(new ASTLambda(parent.one(new ASTLambdaParameter((ASTTypeUse) expr))));
		} else {
			return expr;
		}
	}
	
	public static class ASTBlock extends AbstractASTElement<ASTBlock> implements ASTExpression {
		public @Nullable ASTExpression expression;
		public List<ASTStatement> statements = new ArrayList<>();
		
		@Override
		public IRTypeUse getIRType() {
			// TODO gather all possible return paths and get most specific common supertype from all
			// note below: block doesn't actually return anything; TODO make lambdas better
//			statements.forEach(s -> s.forEach(ep -> {
//				if (ep instanceof Return)
//					((Return) ep).results;
//			}));
			return new IRUnknownTypeUse();
		}
		
		public ASTBlock() {}
		
		public ASTBlock(final @NonNull ASTStatement... statements) {
			this.statements.addAll(Arrays.asList(statements));
		}
		
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			for (final ASTStatement s : statements) {
				s.interpret(context);
				if (context.isReturning)
					return null; // a block doesn't return anything - the result is stored in whatever result variable(s) is/are used
				// TODO different 'return's for returning from the outermost function, and only returning from a single block?
			}
			return null;
		}
		
		@Override
		protected ASTBlock parse() throws ParseException {
			oneRepeatingGroup('{', () -> {
				if (statements.isEmpty()) {
					final ASTElement e = ASTStatement.parseWithExpression(this);
					if (e instanceof ASTExpression)
						expression = (ASTExpression) e;
					else
						statements.add((ASTStatement) e);
				} else {
					statements.add(ASTStatement.parse(this));
					assert expression == null;
				}
			}, '}');
			return this;
		}
	}
	
	public static class ASTAnonymousObject extends AbstractASTElement<ASTAnonymousObject> implements ASTExpression, ASTTypeDeclaration {
		public @Nullable ASTTypeUse type;
		public List<ASTMember> members = new ArrayList<>();
		
		@Override
		public @Nullable String name() {
			return null;
		}
		
		@Override
		public @Nullable WordToken nameToken() {
			return null;
		}
		
		@Override
		public int linkStart() {
			return type != null ? type.regionStart() : regionStart();
		}
		
		@Override
		public int linkEnd() {
			return type != null ? type.regionEnd() : regionEnd();
		}
		
		@Override
		public List<? extends ASTMember> declaredMembers() {
			return members;
		}
		
		// always has a parent type
		@Override
		public @NonNull IRTypeUse parentTypes() {
			return type != null ? type.staticallyKnownType() : new IRUnknownTypeUse();
		}
		
		@Override
		public List<? extends ASTGenericParameter> genericParameters() {
			return Collections.EMPTY_LIST;
		}
		
		@Override
		protected ASTAnonymousObject parse() throws ParseException {
			one("create");
			type = ASTTypeExpressions.parse(this, true, false);
			oneRepeatingGroup('{', () -> {
				members.add(ASTMember.parse(this));
			}, '}');
			return this;
		}
		
		@Override
		public IRTypeUse getIRType() {
			return new IRSimpleTypeUse(getIR()); // a new anonymous object (which this represents) is an instance of its anonymous type (which this also represents)
		}
		
		@Override
		public IRBrokkrClass getIR() {
			return new IRBrokkrClass(this);
		}
		
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			return new InterpretedNormalObject(new IRSimpleClassUse(getIR()));
		}
	}
	
	public static class ASTLambda extends AbstractASTElement<ASTLambda> implements ASTExpression, ASTElementWithVariables {
		public final List<ASTLambdaParameter> parameters = new ArrayList<>();
		public @Nullable ASTExpression code;
		
		public ASTLambda() {}
		
		public ASTLambda(final ASTLambdaParameter param) {
			parameters.add(param);
			param.setParent(this);
		}
		
		@Override
		protected ASTLambda parse() throws ParseException {
			if (parameters.isEmpty()) {
				if (!tryGroup('[', () -> {
					do {
						parameters.add(one(ASTLambdaParameter.class));
					} while (try_(','));
				}, ']')) {
					parameters.add(one(ASTLambdaParameter.class));
				}
			}
			one("->");
			code = ASTExpressions.parse(this);
			return this;
		}
		
		@Override
		public List<? extends IRVariableRedefinition> allVariables() {
			return parameters.stream().map(p -> p.interpreted()).collect(Collectors.toList());
		}
		
		@Override
		public IRTypeUse getIRType() {
			// TODO wrong, is actually a function type
			return code != null ? code.getIRType() : new IRUnknownTypeUse();
		}
		
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			throw new InterpreterException("not implemented");
//			return new IRClosure() {
//				@Override
//				IRObject interpret(final Map<IRParameter, IRObject> arguments) {
//					// TODO Auto-generated method stub
//					return null;
//				}
//			};
		}
	}
	
	public static class ASTLambdaParameter extends AbstractASTElement<ASTLambdaParameter> implements ASTLocalVariable {
		public @Nullable ASTTypeUse type;
		public @Nullable LowercaseWordToken name;
		
		@Override
		public @Nullable WordToken nameToken() {
			return name;
		}
		
		public ASTLambdaParameter() {}
		
		public ASTLambdaParameter(final ASTTypeUse type) {
			this.type = type;
			type.setParent(this);
		}
		
		private boolean withoutType;
		
		public ASTLambdaParameter(final boolean withoutType) {
			this.withoutType = withoutType;
		}
		
		@Override
		protected @NonNull ASTLambdaParameter parse() throws ParseException {
			if (type == null && !withoutType && !try_("var"))
				type = ASTTypeExpressions.parse(this, false, false);
			name = oneVariableIdentifierToken();
			return this;
		}
		
		@Override
		public IRVariableRedefinition interpreted() {
			return new IRBrokkrLocalVariable(this);
		}
		
		@Override
		public IRTypeUse getIRType() {
			if (type != null)
				return type.staticallyKnownType();
			throw new InterpreterException("not implemented"); // TODO infer type
		}
	}
	
	public static abstract class AbstractASTAssignment<T extends AbstractASTElement<T>> extends AbstractASTElement<T> implements ASTExpression {
		public final SymbolsWordToken assignmentOp;
		public final @Nullable ASTOperatorLink assignmentOpLink;
		public @Nullable ASTExpression value;
		
		protected AbstractASTAssignment(final SymbolsWordToken assignmentOp) {
			this.assignmentOp = assignmentOp;
			assignmentOp.setParent(this);
			assignmentOpLink = assignmentOp.symbols.size() > 1 ? new ASTOperatorLink(this, assignmentOp, true) : null;
		}
		
		@Override
		public IRTypeUse getIRType() {
			return value != null ? value.getIRType() : new IRUnknownTypeUse();
		}
		
		protected abstract @Nullable InterpretedObject target(InterpreterContext context);
		
		protected abstract @Nullable IRVariableOrAttributeRedefinition varOrAttribute();
		
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			final ASTExpression expression = value;
			if (expression == null)
				return null;
			InterpretedObject value = expression.interpret(context);
			if (value == null)
				return null;
			final IRVariableOrAttributeRedefinition varOrAttribute = varOrAttribute();
			if (varOrAttribute == null)
				return null;
			final IRAttributeRedefinition operator = assignmentOpLink != null ? assignmentOpLink.get() : null;
			if (operator == null && assignmentOpLink != null)
				return null;
			if (varOrAttribute instanceof IRVariableRedefinition) {
				final IRVariableDefinition variableDefinition = ((IRVariableRedefinition) varOrAttribute).definition();
				if (operator != null)
					value = operator.interpretDispatched(context.getLocalVariableValue(variableDefinition), Collections.singletonMap(operator.parameters().get(0).definition(), value), false);
				context.setLocalVariableValue(variableDefinition, value);
			} else {
				final InterpretedObject target = target(context);
				if (target == null)
					return null;
				final IRAttributeDefinition attributeDefinition = ((IRAttributeRedefinition) varOrAttribute).definition();
				if (target instanceof InterpretedNormalObject) {
					if (operator != null)
						value = operator.interpretDispatched(((InterpretedNormalObject) target).getAttributeValue(attributeDefinition), Collections.singletonMap(operator.parameters().get(0).definition(), value), false);
					((InterpretedNormalObject) target).setAttributeValue(attributeDefinition, value);
				} else {// TODO tuples
					throw new InterpreterException("Tried to set an attribute on a native object");
				}
			}
			return value;
		}
	}
	
	// TODO think about whether assignment should really be an expression - this can be handy, but can also hide state changes.
	public static class ASTLocalVariableOrUnqualifiedAttributeAssignment extends AbstractASTAssignment<ASTLocalVariableOrUnqualifiedAttributeAssignment> {
		public final ASTVariableOrUnqualifiedAttributeUse varOrAttribute;
		
		public ASTLocalVariableOrUnqualifiedAttributeAssignment(final SymbolsWordToken assignmentOp, final ASTVariableOrUnqualifiedAttributeUse varOrAttribute) {
			super(assignmentOp);
			this.varOrAttribute = varOrAttribute;
			varOrAttribute.setParent(this);
		}
		
		@Override
		protected ASTLocalVariableOrUnqualifiedAttributeAssignment parse() throws ParseException {
			value = ASTExpressions.parse(this);
			return this;
		}
		
		@Override
		protected @Nullable InterpretedObject target(final InterpreterContext context) {
			return context.getThisObject();
		}
		
		@Override
		protected @Nullable IRVariableOrAttributeRedefinition varOrAttribute() {
			return varOrAttribute.link.get();
		}
	}
	
	public static class ASTAttributeAssignment extends AbstractASTAssignment<ASTAttributeAssignment> {
		public final ASTExpression target;
		public final ASTLink<? extends IRAttributeRedefinition> attribute;
		
		public ASTAttributeAssignment(final SymbolsWordToken assignmentOp, final ASTExpression target, final ASTLink<? extends IRAttributeRedefinition> attribute) {
			super(assignmentOp);
			this.target = target;
			this.attribute = attribute;
			target.setParent(this);
			addLink(attribute);
		}
		
		@Override
		protected ASTAttributeAssignment parse() throws ParseException {
			value = ASTExpressions.parse(this);
			return this;
		}
		
		@Override
		protected @Nullable InterpretedObject target(final InterpreterContext context) {
			return target.interpret(context);
		}
		
		@Override
		protected @Nullable IRVariableOrAttributeRedefinition varOrAttribute() {
			return attribute.get();
		}
	}
	
	public static class ASTTernaryIf extends AbstractASTElement<ASTTernaryIf> implements ASTExpression {
		public ASTExpression condition;
		public @Nullable ASTExpression then, otherwise;
		
		public ASTTernaryIf(final ASTExpression condition) {
			this.condition = condition;
			condition.setParent(this);
		}
		
		@Override
		protected ASTTernaryIf parse() throws ParseException {
			then = ASTExpressions.parse(this);
			one(':');
			otherwise = ASTExpressions.parse(this);
			return this;
		}
		
		@Override
		public IRTypeUse getIRType() {
			return new IROrTypeUse(then != null ? then.getIRType() : new IRUnknownTypeUse(), otherwise != null ? otherwise.getIRType() : new IRUnknownTypeUse());
		}
		
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			final InterpretedObject cond = condition.interpret(context);
			if (cond == null)
				return null;
			return cond.equals(getInterpreter().bool(true)) ? (then != null ? then.interpret(context) : null) : (otherwise != null ? otherwise.interpret(context) : null);
		}
	}
	
	public static class ASTErrorHandlingExpression extends AbstractASTElement<ASTElement> implements ASTExpression {
		public ASTExpression expression;
		public boolean negated;
		public ASTLink<ASTError> error = new ASTLink<ASTError>(this) {
			@Override
			protected @Nullable ASTError tryLink(final String name) {
				//expression.type();
				// TODO Auto-generated method stub
				return null;
			}
		};
		public List<ASTErrorHandlingExpressionParameter> parameters = new ArrayList<>();
		public @Nullable ASTExpression value;
		
		public ASTErrorHandlingExpression(final ASTExpression expression) {
			this.expression = expression;
			expression.setParent(this);
		}
		
		@Override
		protected ASTElement parse() throws ParseException {
			negated = try_('!');
			error.setName(oneVariableIdentifierToken());
			if (try_('(')) {
				do {
					parameters.add(one(ASTErrorHandlingExpressionParameter.class));
				} while (try_(','));
				one(')');
			}
			one(':');
			value = ASTExpressions.parse(this);
			return this;
		}
		
		@Override
		public IRTypeUse getIRType() {
			return value != null ? value.getIRType() : new IRUnknownTypeUse();
		}
		
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			throw new InterpreterException("not implemented");
		}
	}
	
	public static class ASTErrorHandlingExpressionParameter extends AbstractASTElement<ASTErrorHandlingExpressionParameter> implements ASTLocalVariable {
		public final ASTLink<ASTParameter> parameter = new ASTLink<ASTParameter>(this) {
			@Override
			protected @Nullable ASTParameter tryLink(final String name) {
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
		protected ASTErrorHandlingExpressionParameter parse() throws ParseException {
			if (!try_("var"))
				type = ASTTypeExpressions.parse(this, true, true);
			parameter.setName(oneVariableIdentifierToken());
			return this;
		}
		
		@Override
		public IRTypeUse getIRType() {
			if (type != null)
				return type.staticallyKnownType();
			final ASTParameter param = parameter.get();
			if (param != null)
				return param.getIRType();
			return new IRUnknownTypeUse();
		}
		
		@Override
		public IRVariableRedefinition interpreted() {
			return new IRBrokkrLocalVariable(this); // TODO correct?
		}
	}
	
	public static class ASTOperatorExpression extends AbstractASTElement<ASTExpression> implements ASTExpression {
		private final boolean allowComparisons;
		
		public List<ASTExpression> expressions = new ArrayList<>();
		public List<ASTLink<IRAttributeRedefinition>> operators = new ArrayList<>();
		
		public ASTOperatorExpression() {
			this(true);
		}
		
		public ASTOperatorExpression(final boolean allowComparisons) {
			this.allowComparisons = allowComparisons;
		}
		
		@Override
		public String toString() {
			final StringBuilder b = new StringBuilder();
			b.append('(');
			b.append(expressions.get(0));
			for (int i = 1; i < expressions.size(); i++) {
				b.append(operators.get(i - 1));
				b.append(expressions.get(i));
			}
			b.append(')');
			return "" + b;
		}
		
		private final static String[] opsWithoutComp = {//
				"&", "|", "+", "-", "*", "/", "^", //
				"implies", "extends", "super", "is"};
		private final static String[] opsWithComp = {//
				"&", "|", "+", "-", "*", "/", "^", // copy of above
				"&=", "|=", "+=", "-=", "*=", "/=", // TODO should these exist or not? could possibly be confused with modifying an object instead of changing a variable (the most common use, numbers, has no ambiguity though)
				">=", ">", "<=", "<", //
				"===", "==", "!==", "!=", //
				"implies", "extends", "super", "is"}; // copy of above
		
		@Override
		protected ASTExpression parse() throws ParseException {
			final ASTExpression first = one(ASTOperatorExpressionPart.class);
			expressions.add(first);
			WordToken op;
			while ((op = try2(allowComparisons ? opsWithComp : opsWithoutComp)) != null) {
				operators.add(new ASTOperatorLink(this, op, true));
				expressions.add(one(ASTOperatorExpressionPart.class));
			}
			if (expressions.size() == 1)
				return first;
			else
				return this;
		}
		
		// TODO use proper operator order
		
		@Override
		public IRTypeUse getIRType() {
			final IRAttributeRedefinition attributeRedefinition = operators.get(operators.size() - 1).get();
			return attributeRedefinition == null ? new IRUnknownTypeUse() : attributeRedefinition.mainResultType();
		}
		
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			InterpretedObject o = expressions.get(0).interpret(context);
			for (int i = 0; i < operators.size(); i++) {
				final InterpretedObject o2 = expressions.get(i + 1).interpret(context);
				if (o == null || o2 == null)
					return null;
				final IRAttributeRedefinition operatorAttribute = operators.get(i).get();
				if (operatorAttribute == null)
					return null;
				final IRAttributeDefinition attr = operatorAttribute.definition();
				o = attr.interpretDispatched(o, Collections.singletonMap(attr.parameters().get(0).definition(), o2), false);
			}
			return o;
		}
	}
	
	public static class ASTOperatorExpressionPart extends AbstractASTElement<ASTExpression> implements ASTExpression {
		public @Nullable ASTExpression expression;
		public ASTLink<IRAttributeRedefinition> prefixOperator = new ASTOperatorLink(this, null, false);
		
		@Override
		protected ASTExpression parse() throws ParseException {
			final ASTModifierTypeUseModifier mod = ASTModifierTypeUseModifier.tryParse(this);
			if (mod != null) {
				final ASTTypeExpression e = one(new ASTModifierTypeUse(mod));
				if (peekNext('<'))
					return ASTTypeWithGenericArguments.withModifiers(e, this);
				return e;
			}
			
			prefixOperator.setName(try2("!", "-"));
			
			ASTExpression expr;
			expression = expr = ASTAccessExpression.parse(this);
			if (expr == null) {
				expectedFatal("an expression");
				return this;
			}
			if (prefixOperator.getNameToken() == null)
				return expr;
			else
				return this;
		}
		
		@Override
		public IRTypeUse getIRType() {
			final IRAttributeRedefinition attributeRedefinition = prefixOperator.get();
			return attributeRedefinition == null ? new IRUnknownTypeUse() : attributeRedefinition.mainResultType();
		}
		
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			final InterpretedObject object = expression != null ? expression.interpret(context) : null;
			if (object == null)
				return null;
			final IRAttributeRedefinition attributeRedefinition = prefixOperator.get();
			return attributeRedefinition == null ? null : attributeRedefinition.interpretDispatched(object, Collections.EMPTY_MAP, false);
		}
	}
	
	public static class ASTAccessExpression extends AbstractASTElement<ASTExpression> implements ASTExpression {
		public final ASTExpression target;
		public boolean nullSafe, meta;
		public @Nullable ASTDirectAttributeAccess access;
		
		@Override
		public IRTypeUse getIRType() {
			return access != null ? access.getIRType() : new IRUnknownTypeUse();
		}
		
		public ASTAccessExpression(final ASTExpression target) {
			this.target = target;
			target.setParent(this);
		}
		
		public final static @Nullable ASTExpression parse(final AbstractASTElement<?> parent) throws ParseException {
			final ASTExpression first = ASTAtomicExpression.parse(parent);
			if (first == null)
				return null;
			ASTExpression e = first;
			while (true) {
				final ASTExpression next = parent.one(new ASTAccessExpression(e));
				if (next == e) {
					e.setParent(parent);
					return e;
				}
				e = next;
			}
		}
		
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			final InterpretedObject target = this.target.interpret(context);
			if (target == null)
				return null;
			if (nullSafe && target instanceof InterpretedNullConstant)
				return new InterpretedNullConstant();
			final ASTDirectAttributeAccess daa = access;
			if (daa == null)
				return null;
			final IRAttributeRedefinition attributeRedefinition = daa.attribute.get();
			if (attributeRedefinition == null)
				return null;
			final IRAttributeDefinition attributeDefinition = attributeRedefinition.definition();
			final InterpretedObject obj = attributeDefinition.interpretDispatched(target, ASTArgument.makeIRArgumentMap(attributeDefinition, daa.arguments, context), daa.allResults);
//			if (daa.negated)
//				return getInterpreter().getInterface("lang", "Boolean").getAttributeByName("negated").interpretDispatched(obj, Collections.EMPTY_MAP, false);
			return obj;
		}
		
		@Override
		protected ASTExpression parse() throws ParseException {
			final String op = try_(".", "?.", "~", "?~"); // note: must try '?' together with '.' or '~', as it is also used by the ternary operator '? :'
			if (op == null)
				return target;
			nullSafe = op.startsWith("?");
			meta = op.endsWith("~");
			access = one(ASTDirectAttributeAccess.class);
			return this;
		}
	}
	
	public static interface ASTAccess extends TypedASTElement {}
	
	public static class ASTDirectAttributeAccess extends AbstractASTElement<ASTDirectAttributeAccess> implements ASTAccess, ASTMethodCall {
//		public boolean negated;
		public final ASTLink<IRAttributeRedefinition> attribute = new ASTLink<IRAttributeRedefinition>(this) {
			@Override
			protected @Nullable IRAttributeRedefinition tryLink(final String name) {
				final ASTAccessExpression accessExpression = (ASTAccessExpression) parent;
				if (accessExpression == null)
					return null;
				final IRTypeUse type = accessExpression.target.getIRType();
				final IRAttributeUse a = type.getAttributeByName(name);
//				if (a == null && name.endsWith("_"))
//					a = type.getAttributeByName("" + name.substring(0, name.length() - 1));
				return a == null ? null : a.redefinition();
			}
		};
		public boolean allResults;
		
		public final List<ASTArgument> arguments = new ArrayList<>();
		
		@Override
		public @Nullable IRAttributeRedefinition attribute() {
			return attribute.get();
		}
		
		@Override
		public IRTypeUse getIRType() {
			final IRAttributeRedefinition attributeRedefinition = attribute.get();
			if (attributeRedefinition == null)
				return new IRUnknownTypeUse();
			return allResults ? attributeRedefinition.allResultTypes() : attributeRedefinition.mainResultType();
		}
		
		@Override
		protected ASTDirectAttributeAccess parse() throws ParseException {
//			negated = try_('!');
			final WordToken name = oneVariableIdentifierToken();
			attribute.setName(name);
			allResults = name.word.endsWith("_"); //try_('!'); // FIXME other symbol - possible: !´`'¬@¦§°%_$£\     // maybe a combination of symbols? e.g. []
			tryGroup('(', () -> {
				do {
					arguments.add(one(ASTArgument.class));
				} while (try_(','));
			}, ')');
			return this;
		}
		
		@Override
		public String toString() {
			return /*(negated ? "!" : "") +*/ attribute.getName() + (arguments.size() == 0 ? "" : "(...)");
		}
	}
	
	public static interface ASTAtomicExpression extends ASTExpression {
		public static @Nullable ASTExpression parse(final AbstractASTElement<?> parent) throws ParseException {
			// peek is acceptable here, as nobody needs content assist for expression *syntax*.
			if (parent.try_('(')) {
				final ASTExpression[] e = new ASTExpression[1];
				parent.until(() -> {
					e[0] = ASTExpressions.parse(parent);
				}, ')', false);
				if (e[0] != null)
					return e[0];
			}
			final Token next = parent.peekNext();
			if (next instanceof StringToken) {
				parent.next();
				return parent.one(new ASTString((StringToken) next));
			}
			if (next instanceof NumberToken) {
				parent.next();
				return parent.one(new ASTNumberConstant((NumberToken) next));
			}
			if (next instanceof UppercaseWordToken)
				return ASTTypeExpressions.parse(parent, false, false);
			if (parent.peekNext('['))
				return parent.one(ASTTuple.class);
			if (parent.peekNext('~'))
				return parent.one(ASTUnqualifiedMetaAccess.class);
//			if (parent.peekNext('?'))
//				return parent.one(ImplicitLambdaArgument.class);
			if (parent.peekNext("this"))
				return parent.one(ASTThis.class);
			if (parent.peekNext("null"))
				return parent.one(ASTNull.class);
			if (parent.peekNext("arguments"))
				return parent.one(ASTArgumentsKeyword.class);
			if (parent.peekNext("exists") || parent.peekNext("forall"))
				return parent.one(ASTQuantifier.class);
			if (parent.peekNext("recurse"))
				return parent.one(ASTRecurse.class);
			if (parent.peekNext("old"))
				return parent.one(ASTOld.class);
			final ASTKleeneanConstant kleeneanConstant = ASTKleeneanConstant.tryParse(parent);
			if (kleeneanConstant != null)
				return kleeneanConstant;
			// must be here after all keywords
			if (next instanceof LowercaseWordToken)
				return parent.one(ASTVariableOrUnqualifiedAttributeUse.class);
			return null;
		}
	}
	
	// TODO is this good or bad?
//	public static class ImplicitLambdaArgument extends AbstractElement<ImplicitLambdaArgument> implements Expression {
//		@Override
//		public IRNativeType nativeType() {
//			return null; // TODO
//		}
//
//		@Override
//		protected @NonNull ImplicitLambdaArgument parse() throws ParseException {
//			one('?');
//			return this;
//		}
//	}
	
	public static class ASTNumberConstant extends AbstractASTElement<ASTNumberConstant> implements ASTExpression {
		public final BigDecimal value;
		
		public ASTNumberConstant(final NumberToken token) {
			token.setParent(this);
			value = token.value;
		}
		
		@Override
		public String toString() {
			return "" + value;
		}
		
		@Override
		public IRTypeUse getIRType() {
			return getInterpreter().numberConstant(value).nativeClass();
		}
		
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			return getInterpreter().numberConstant(value);
		}
		
		@Override
		protected ASTNumberConstant parse() throws ParseException {
			return this;
		}
	}
	
	public static class ASTKleeneanConstant extends AbstractASTElement<ASTKleeneanConstant> implements ASTExpression {
		public final Kleenean value;
		
		private ASTKleeneanConstant(final Kleenean value) {
			this.value = value;
		}
		
		public static @Nullable ASTKleeneanConstant tryParse(final AbstractASTElement<?> parent) {
			final WordToken token = parent.try2("true", "false", "unknown");
			if (token == null)
				return null;
			return new ASTKleeneanConstant(Kleenean.valueOf(token.word.toUpperCase(Locale.ENGLISH)));
		}
		
		@Override
		public String toString() {
			assert value != null;
			return "" + value.name().toLowerCase(Locale.ENGLISH);
		}
		
		@Override
		protected ASTKleeneanConstant parse() throws ParseException {
			return this;
		}
		
		@Override
		public IRTypeUse getIRType() {
			return getInterpreter().getTypeUse("lang", value == Kleenean.UNKNOWN ? "Kleenean" : "Boolean");
		}
		
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			return getInterpreter().kleenean(value);
		}
	}
	
	/**
	 * The keyword 'this', representing 'the current object'.
	 */
	public static class ASTThis extends AbstractASTElement<ASTThis> implements ASTExpression {
		@Override
		public IRTypeUse getIRType() {
			final ASTTypeDeclaration type = getParentOfType(ASTTypeDeclaration.class);
			return type == null ? new IRUnknownTypeUse() : new IRSimpleTypeUse(type.getIR());
		}
		
		@Override
		public String toString() {
			return "this";
		}
		
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			return context.getThisObject();
		}
		
		@Override
		protected ASTThis parse() throws ParseException {
			one("this");
			return this;
		}
	}
	
	/**
	 * The keyword 'null', representing 'no value'.
	 */
	public static class ASTNull extends AbstractASTElement<ASTNull> implements ASTExpression {
		@Override
		public String toString() {
			return "null";
		}
		
		@Override
		protected ASTNull parse() throws ParseException {
			one("null");
			return this;
		}
		
		@Override
		public IRTypeUse getIRType() {
			return new IRSimpleTypeUse(new IRNativeNullClass());
		}
		
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			return new InterpretedNullConstant();
		}
	}
	
	/**
	 * The keyword 'arguments', representing a tuple of all arguments to the current method.
	 */
	public static class ASTArgumentsKeyword extends AbstractASTElement<ASTArgumentsKeyword> implements ASTExpression {
		@Override
		public String toString() {
			return "arguments";
		}
		
		@Override
		protected ASTArgumentsKeyword parse() throws ParseException {
			one("arguments");
			return this;
		}
		
		@Override
		public IRTypeUse getIRType() {
			final ASTAttribute attribute = getParentOfType(ASTAttribute.class);
			return attribute == null ? new IRUnknownTypeUse() : attribute.getIR().allParameterTypes();
		}
		
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			final ASTAttribute attribute = getParentOfType(ASTAttribute.class);
			if (attribute == null)
				return null;
			final List<IRParameterRedefinition> parameters = attribute.getIR().parameters();
			final List<IRNativeTupleValueAndEntry> entries = new ArrayList<>();
			for (int i = 0; i < parameters.size(); i++) {
				final IRParameterRedefinition p = parameters.get(i);
				entries.add(new IRNativeTupleValueAndEntry(i, p.type(), p.name(), context.getLocalVariableValue(p.definition())));
			}
			return IRTuple.newInstance(entries.stream());
		}
	}
	
	/**
	 * A string literal.
	 */
	public static class ASTString extends AbstractASTElement<ASTString> implements ASTExpression {
		public final String value;
		
		public ASTString(final StringToken value) {
			value.setParent(this);
			this.value = value.value;
		}
		
		@Override
		public String toString() {
			assert value != null;
			return "'" + value.replaceAll("'", "\\'") + "'";
		}
		
		@Override
		protected ASTString parse() throws ParseException {
			return this;
		}
		
		@Override
		public IRTypeUse getIRType() {
			return getInterpreter().getTypeUse("lang", "String");
		}
		
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			return getInterpreter().stringConstant(value);
		}
	}
	
	/**
	 * A first-order logic quantifier for contracts, i.e. 'for all' or 'there exists'.
	 */
	public static class ASTQuantifier extends AbstractASTElement<ASTQuantifier> implements ASTExpression/*, ASTElementWithVariables*/ {
		boolean forall;
		public final List<ASTQuantifierVars> vars = new ArrayList<>();
		public @Nullable ASTExpression condition;
		public @Nullable ASTExpression expression;
		
		@Override
		public String toString() {
			return (forall ? "forall" : "exists") + "(...)";
		}
		
		@Override
		protected ASTQuantifier parse() throws ParseException {
			forall = oneOf("forall", "exists").equals("forall");
			oneGroup('(', () -> {
				until(() -> {
					do {
						vars.add(one(ASTQuantifierVars.class));
					} while (try_(';'));
					if (try_('|'))
						condition = ASTExpressions.parse(this);
				}, ':', false);
				expression = ASTExpressions.parse(this);
			}, ')');
			return this;
		}
		
//		@Override
//		public List<? extends IRVariableRedefinition> allVariables() {
//			return vars.stream().flatMap(vars -> vars.vars.stream()).map(v -> v.interpreted(null)).collect(Collectors.toList());
//		}
		
		@Override
		public IRTypeUse getIRType() {
			return getInterpreter().getTypeUse("lang", "Boolean");
		}
		
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			throw new InterpreterException("not implemented");
		}
	}
	
	public static class ASTQuantifierVars extends AbstractASTElement<ASTQuantifierVars> {
		public @Nullable ASTTypeUse type;
		public final List<ASTQuantifierVar> vars = new ArrayList<>();
		
		@Override
		protected ASTQuantifierVars parse() throws ParseException {
			type = ASTTypeExpressions.parse(this, true, true);
			do {
				vars.add(one(ASTQuantifierVar.class));
			} while (try_(','));
			return this;
		}
	}
	
	public static class ASTQuantifierVar extends AbstractASTElement<ASTQuantifierVar> implements ASTParameter {
		public @Nullable LowercaseWordToken name;
		
		@Override
		public @Nullable WordToken nameToken() {
			return name;
		}
		
		@Override
		protected ASTQuantifierVar parse() throws ParseException {
			name = oneVariableIdentifierToken();
			return this;
		}
		
		@Override
		public IRTypeUse getIRType() {
			final ASTQuantifierVars vars = (ASTQuantifierVars) parent;
			if (vars == null)
				return new IRUnknownTypeUse();
			final ASTTypeUse type = vars.type;
			if (type == null)
				return new IRUnknownTypeUse();
			return type.staticallyKnownType();
		}
		
		@Override
		public IRParameterRedefinition interpreted(final IRAttributeRedefinition attribute) {
			throw new InterpreterException("not implemented");
		}
	}
	
	/**
	 * A tuple, like '[a, b: c]' or '[A, B]'. If all entries are types, this is also a type.
	 */
	public static class ASTTuple extends AbstractASTElement<ASTTuple> implements ASTExpression {
		public List<ASTTupleEntry> entries = new ArrayList<>();
		
		@Override
		public IRTypeUse getIRType() {
			final List<IRNativeTupleValueAndEntry> entries = new ArrayList<>();
			for (int i = 0; i < this.entries.size(); i++) {
				entries.add(this.entries.get(i).nativeType(i));
			}
			return new IRTypeTuple(entries);
		}
		
		@Override
		protected ASTTuple parse() throws ParseException {
			oneGroup('[', () -> {
				do {
					entries.add(one(new ASTTupleEntry(this instanceof ASTTypeTuple)));
				} while (try_(','));
			}, ']');
			return this;
		}
		
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			final List<IRNativeTupleValueAndEntry> entries = new ArrayList<>();
			for (int i = 0; i < this.entries.size(); i++) {
				final IRNativeTupleValueAndEntry entry = this.entries.get(i).interpret(context, i);
				if (entry == null)
					return null;
				entries.add(entry);
			}
			return IRTuple.newInstance(entries.stream());
		}
	}
	
	/**
	 * A subclass of Tuple that implements TypeExpression, and entries are only parsed as types.
	 * <p>
	 * Exclusively used for parsing a tuple as a type only; normal tuples that contain only types must be handled equal to such a type tuple.
	 */
	public static class ASTTypeTuple extends ASTTuple implements ASTTypeExpression {
		@Override
		public IRTypeTuple staticallyKnownType() {
			final List<IRNativeTupleValueAndEntry> entries = new ArrayList<>();
			for (int i = 0; i < this.entries.size(); i++) {
				entries.add(this.entries.get(i).nativeType(i));
			}
			return new IRTypeTuple(entries.stream().collect(Collectors.toList()));
		}
		
		@Override
		public @NonNull IRTypeUse interpret(final InterpreterContext context) {
			final InterpretedObject t = super.interpret(context);
			return t == null ? new IRUnknownTypeUse() : (IRTypeTuple) t;
		}
	}
	
	/**
	 * An entry of a tuple, with an optional name.
	 */
	public static class ASTTupleEntry extends AbstractASTElement<ASTTupleEntry> {
		public @Nullable WordToken name;
		public @Nullable ASTExpression value;
		
		private final boolean onlyTypes;
		
		public ASTTupleEntry(final boolean onlyTypes) {
			this.onlyTypes = onlyTypes;
		}
		
		@Override
		protected ASTTupleEntry parse() throws ParseException {
			if (peekNext() instanceof WordToken && peekNext(':', 1, true)) {
				name = oneIdentifierToken();
				next(); // skip ':'
			}
			value = onlyTypes ? ASTTypeExpressions.parse(this, true, true) : ASTExpressions.parse(this);
			return this;
		}
		
		public @Nullable String name() {
			final WordToken wordToken = name;
			return wordToken != null ? wordToken.word : null;
		}
		
		public @Nullable IRNativeTupleValueAndEntry interpret(final InterpreterContext context, final int index) {
			final ASTExpression expression = value;
			final WordToken nameToken = name;
			if (expression == null || nameToken == null)
				return null;
			final InterpretedObject value = expression.interpret(context);
			if (value == null)
				return null;
			return new IRNativeTupleValueAndEntry(index, expression.getIRType(), nameToken.word, value);
		}
		
		public IRNativeTupleValueAndEntry nativeType(final int index) {
			final ASTExpression expression = value;
			final WordToken nameToken = name;
			final IRTypeUse valueType = expression == null ? new IRUnknownTypeUse() : expression.getIRType();
			return new IRNativeTupleValueAndEntry(index, valueType.nativeClass(), nameToken == null ? "<unknown>" : nameToken.word, valueType); // TODO make an UnknownString? maybe as a constant?
		}
	}
	
	/**
	 * A method call with arguments. Used by {@link ASTArgument} to find its linked parameter.
	 */
	public static interface ASTMethodCall extends ASTElement {
		public @Nullable IRAttributeRedefinition attribute();
	}
	
	/**
	 * A variable of unqualified attribute. Since both are just a lowercase word, these cases cannot be distinguished before linking.
	 * Also handles unqualified attribute calls.
	 */
	public static class ASTVariableOrUnqualifiedAttributeUse extends AbstractASTElement<ASTVariableOrUnqualifiedAttributeUse> implements ASTExpression, ASTMethodCall, DebugString {
		public ASTLink<IRVariableOrAttributeRedefinition> link = new ASTLink<IRVariableOrAttributeRedefinition>(this) {
			@Override
			protected @Nullable IRVariableOrAttributeRedefinition tryLink(final String name) {
				for (ASTElement p = parent(); p != null; p = p.parent()) {
					if (p instanceof ASTBlock) {
						// note: does not care about order of variable use and declaration - TODO either check this here or just let the semantic checker handle it
						for (final ASTVariableDeclarations vars : ((ASTBlock) p).getDirectChildrenOfType(ASTVariableDeclarations.class)) {
							for (final ASTVariableDeclarationsVariable var : vars.variables) {
								final LowercaseWordToken nameToken = var.nameToken;
								if (nameToken != null && name.equals(nameToken.word))
									return var.interpreted();
							}
						}
					}
					if (p instanceof ASTElementWithVariables) {
						final IRVariableRedefinition var = ((ASTElementWithVariables) p).getVariableByName(name);
						if (var != null)
							return var;
					}
					if (p instanceof ASTTypeDeclaration) {
						final IRAttributeRedefinition attribute = ((ASTTypeDeclaration) p).getIR().getAttributeByName(name);
						if (attribute != null)
							return attribute;
					}
				}
				// TODO semantic error; maybe set directly in Link: (copied from old code, so needs modification)
				//if (arguments.size() > 0)
				//error(m + " is not a method");
				return null;
			}
		};
		public List<ASTArgument> arguments = new ArrayList<>();
		
		@Override
		protected ASTVariableOrUnqualifiedAttributeUse parse() throws ParseException {
			link.setName(oneVariableIdentifierToken());
			tryGroup('(', () -> {
				do {
					arguments.add(one(ASTArgument.class));
				} while (try_(','));
			}, ')');
			return this;
		}
		
		@Override
		public String debug() {
			return "" + link.get();
		}
		
		@Override
		public IRTypeUse getIRType() {
			final IRVariableOrAttributeRedefinition variableOrAttributeRedefinition = link.get();
			return variableOrAttributeRedefinition == null ? new IRUnknownTypeUse() : variableOrAttributeRedefinition.mainResultType();
		}
		
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			final IRVariableOrAttributeRedefinition varOrAttr = link.get();
			if (varOrAttr == null)
				return null;
			if (varOrAttr instanceof IRVariableRedefinition) {
				return context.getLocalVariableValue(((IRVariableRedefinition) varOrAttr).definition());
			} else {
				return ((IRAttributeRedefinition) varOrAttr).interpretDispatched(context.getThisObject(), Collections.EMPTY_MAP, false);
			}
		}
		
		@Override
		public @Nullable IRAttributeRedefinition attribute() {
			final IRVariableOrAttributeRedefinition varOrAttr = link.get();
			return varOrAttr instanceof IRAttributeRedefinition ? (IRAttributeRedefinition) varOrAttr : null;
		}
	}
	
	/**
	 * An argument to a function call.
	 */
	public static class ASTArgument extends AbstractASTElement<ASTArgument> {
		public boolean isDots;
		public ASTLink<IRParameterRedefinition> parameter = new ASTLink<IRParameterRedefinition>(this) {
			@Override
			protected @Nullable IRParameterRedefinition tryLink(final String name) {
				// find parameter by name if set, otherwise use position // TODO what to sent the link to in that case? just nothing?
				final ASTMethodCall parent = (ASTMethodCall) parent();
				assert parent != null;
				final IRAttributeRedefinition method = parent.attribute();
				if (method == null)
					return null;
				for (final IRParameterRedefinition p : method.parameters()) {
					if (name.equals(p.name()))
						return p;
				}
				return null;
			}
		};
		public @Nullable ASTExpression value;
		
		@Override
		protected ASTArgument parse() throws ParseException {
			isDots = try_("...");
			if (!isDots) {
				if (peekNext() instanceof LowercaseWordToken && peekNext(':', 1, true)) {
					parameter.setName(oneVariableIdentifierToken());
					next(); // skip ':'
				}
				value = ASTExpressions.parse(this);
			}
			return this;
		}
		
		public static Map<IRParameterDefinition, InterpretedObject> makeIRArgumentMap(final IRAttributeDefinition method, final List<ASTArgument> args, final InterpreterContext context) {
			final List<IRParameterRedefinition> parameters = method.parameters();
			final Map<IRParameterDefinition, InterpretedObject> r = new HashMap<>();
			for (int i = 0; i < args.size(); i++) {
				final ASTArgument arg = args.get(i);
				final ASTExpression expression = arg.value;
				if (expression == null)
					continue;
				final InterpretedObject value = expression.interpret(context);
				if (value == null)
					continue;
				if (arg.parameter.getNameToken() == null) {
					r.put(parameters.get(i).definition(), value);
				} else {
					final IRParameterRedefinition parameter = arg.parameter.get();
					if (parameter == null)
						continue;
					r.put(parameter.definition(), value);
				}
			}
			return r;
		}
	}
	
	/**
	 * A meta access without a target (like '~a'), i.e. targets 'this'.
	 */
	public static class ASTUnqualifiedMetaAccess extends AbstractASTElement<ASTUnqualifiedMetaAccess> implements ASTExpression {
		public ASTLink<IRAttributeRedefinition> attribute = new ASTLink<IRAttributeRedefinition>(this) {
			@Override
			protected @Nullable IRAttributeRedefinition tryLink(@NonNull final String name) {
				final ASTTypeDeclaration mc = getParentOfType(ASTTypeDeclaration.class);
				if (mc == null)
					return null;
				return mc.getIR().getAttributeByName(name);
			}
		};
		
		@Override
		protected ASTUnqualifiedMetaAccess parse() throws ParseException {
			one('~');
			attribute.setName(oneVariableIdentifierToken());
			return this;
		}
		
		@Override
		public IRTypeUse getIRType() {
			final IRAttributeRedefinition attr = attribute.get();
			if (attr == null)
				return new IRUnknownTypeUse();
			final boolean isPure = !attr.isModifying();
			return getInterpreter().getTypeUse("lang", isPure ? "Function" : "Procedure");
		}
		
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			throw new InterpreterException("not implemented");
//			return context.getThisObject().getAttributeClosure(attribute.get().definition());
		}
	}
	
	/**
	 * The keyword 'recurse' which calls the current method, with optionally new arguments (and all unspecified arguments left the same).
	 */
	public static class ASTRecurse extends AbstractASTElement<ASTRecurse> implements ASTExpression, ASTMethodCall {
		public List<ASTArgument> arguments = new ArrayList<>();
		
		@Override
		public @Nullable IRAttributeRedefinition attribute() {
			final ASTAttribute attr = getParentOfType(ASTAttribute.class);
			return attr == null ? null : attr.getIR();
		}
		
		@Override
		public IRTypeUse getIRType() {
			final IRAttributeRedefinition attribute = attribute();
			return attribute == null ? new IRUnknownTypeUse() : attribute.mainResultType();
		}
		
		@Override
		protected ASTRecurse parse() throws ParseException {
			one("recurse");
			oneGroup('(', () -> {
				do {
					arguments.add(one(ASTArgument.class));
				} while (try_(','));
			}, ')');
			return this;
		}
		
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			final IRAttributeRedefinition attribute = attribute();
			if (attribute == null)
				return null;
			// this is dispatched as 'recurse' may be in a result's default value
			// (A 'recurse' in a body could just call that same body immediately without a dispatch, since if that body is executed it has to be the last one)
			return attribute.definition().interpretDispatched(context.getThisObject(), ASTArgument.makeIRArgumentMap(attribute.definition(), arguments, context), false);
		}
	}
	
	/**
	 * The keyword/"function" 'old' which evaluates an expression as if it were evaluated at the beginning of its parent function.
	 */
	public static class ASTOld extends AbstractASTElement<ASTOld> implements ASTExpression {
		public @Nullable ASTExpression expression;
		
		@Override
		protected ASTOld parse() throws ParseException {
			one("old");
			oneGroup('(', () -> {
				expression = ASTExpressions.parse(this);
			}, ')');
			return this;
		}
		
		@Override
		public IRTypeUse getIRType() {
			return expression != null ? expression.getIRType() : new IRUnknownTypeUse();
		}
		
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			throw new InterpreterException("not implemented");
		}
	}
	
	// ================================ Types ================================
	
	public static class ASTTypeExpressions {
		
		public static ASTTypeExpression parse(final AbstractASTElement<?> parent, final boolean allowOps, final boolean allowTuple) throws ParseException {
			return parse(parent, allowOps, allowTuple, allowOps);
		}
		
		static ASTTypeExpression parse(final AbstractASTElement<?> parent, final boolean allowOps, final boolean allowTuple, final boolean allowDotGeneric) throws ParseException {
			assert !(!allowDotGeneric && allowOps) : "generics are automatically allowed when operators are";
			if (allowDotGeneric && !allowOps) { // if allowing ops, ops are done first
				final ASTTypeExpression target = ASTTypeExpressions.parse(parent, false, false, false); // TODO do tuples have generics? or should this just be allowed to then produce a better error message?
				if (parent.peekNext('.'))
					return parent.one(new ASTGenericTypeAccess(target));
				else
					return target;
			}
			if (allowTuple && parent.peekNext('[')) {
//				if (!allowTuple)
//					throw new ParseException("Expected a type that is not a tuple", parent.in.getOffset(), parent.in.getOffset() + 1);
				// not null, since it starts with '['
				return (ASTTypeTuple) parent.one(new ASTTypeTuple());
			}
			if (!allowOps) { // i.e. only a single type (possibly with modifiers and generics)
				if (parent.peekNext("Self"))
					return parent.one(ASTSelf.class);
				final ASTModifierTypeUseModifier modifier = ASTModifierTypeUseModifier.tryParse(parent);
				final ASTTypeExpression e;
				if (modifier != null)
					e = parent.one(new ASTModifierTypeUse(modifier));
				else
					e = parent.one(ASTSimpleTypeUse.class);
				if (parent.peekNext('<'))
					return ASTTypeWithGenericArguments.withModifiers(e, parent);
				return e;
			}
			return parent.one(ASTTypeUseWithOperators.class);
			
		}
	}
	
	/**
	 * The keyword 'Self', representing the class of the current object (i.e. equal to this.class, but can be used in more contexts like interfaces and generics)
	 */
	public static class ASTSelf extends AbstractASTElement<ASTSelf> implements ASTTypeExpression {
		ASTLink<ASTTypeDeclaration> link = new ASTLink<ASTTypeDeclaration>(this) {
			@Override
			protected @Nullable ASTTypeDeclaration tryLink(final String name) {
				return ASTSelf.this.getParentOfType(ASTTypeDeclaration.class);
			}
		};
		
		@Override
		public String toString() {
			return "Self";
		}
		
		@Override
		protected ASTSelf parse() throws ParseException {
			one("Self");
			return this;
		}
		
		@Override
		public @NonNull IRClassUse interpret(final InterpreterContext context) {
			return context.getThisObject().nativeClass();
		}
		
		@Override
		public IRTypeUse staticallyKnownType() {
			final ASTTypeDeclaration typeDeclaration = getParentOfType(ASTTypeDeclaration.class);
			return typeDeclaration == null ? new IRUnknownTypeUse() : new IRSimpleTypeUse(typeDeclaration.getIR());
		}
	}
	
	public final static class ASTOperatorLink extends ASTLink<IRAttributeRedefinition> {
		private final boolean isBinary;
		
		public ASTOperatorLink(final ASTElement parent, @Nullable final WordToken symbols, final boolean isBinary) {
			super(parent, symbols);
			this.isBinary = isBinary;
		}
		
		@SuppressWarnings("null")
		private final static Map<String, @NonNull String[]> binaryOperators = new HashMap<String, @NonNull String[]>() {
			private static final long serialVersionUID = 1L;
			
			{
				put("+", new String[] {"Addable", "add"});
				put("+=", new String[] {"Addable", "add"});
				put("-", new String[] {"Subtractable", "subtract"});
				put("-=", new String[] {"Subtractable", "subtract"});
				put("*", new String[] {"Multipliable", "multiply"});
				put("*=", new String[] {"Multipliable", "multiply"});
				put("/", new String[] {"Divisible", "divide"});
				put("/=", new String[] {"Divisible", "divide"});
				put("^", new String[] {"Exponentiable", "exponentiate"});
				put("|", new String[] {"Orable", "or"});
				put("|=", new String[] {"Orable", "or"});
				put("&", new String[] {"Andable", "and"});
				put("&=", new String[] {"Andable", "and"});
				put("==", new String[] {"Comparable", "equals"});
				put("!=", new String[] {"Comparable", "notEquals"}); // TODO easiest way, but requires a weird notEquals method
				put("===", new String[] {"Any", "referenceEquals"});
				put("!==", new String[] {"Any", "referenceNotEquals"}); // TODO same
				put("<=", new String[] {"Orderable", "isLessThanOrEqualTo"});
				put("<", new String[] {"Orderable", "isLessThan"});
				put(">=", new String[] {"Orderable", "isGreaterThanOrEqualTo"});
				put(">", new String[] {"Orderable", "isGreaterThan"});
				put("implies", new String[] {"Boolean", "implies"});
				put("extends", new String[] {"Type", "isSubtypeOfOrEqual"});
				put("super", new String[] {"Interface", "isSupertypeOfOrEqual"});
				put("is", new String[] {"Any", "isInstanceOf"});
			}
		};
		
		@SuppressWarnings("null")
		private final static Map<String, @NonNull String[]> unaryPrefixOperators = new HashMap<String, @NonNull String[]>() {
			private static final long serialVersionUID = 1L;
			
			{
				put("!", new String[] {"Negatable", "negated"});
				put("-", new String[] {"Subtractable", "negated"});
			}
		};
		
		@Override
		protected @Nullable IRAttributeRedefinition tryLink(@NonNull final String name) {
			final @NonNull String[] s = (isBinary ? binaryOperators : unaryPrefixOperators).get(name);
			if (s == null)
				return null;
			return ASTBrokkrFile.getInterpreter(parentElement).getType("lang", s[0]).getAttributeByName(s[1]);
		}
	}
	
	/**
	 * An expression with types and operators, currently only & and |.
	 */
	public static class ASTTypeUseWithOperators extends AbstractASTElement<ASTTypeExpression> implements ASTTypeExpression {
		public List<ASTTypeExpression> types = new ArrayList<>();
		public List<ASTLink<IRAttributeRedefinition>> operators = new ArrayList<>();
		
		@Override
		public String toString() {
			String r = "" + types.get(0);
			for (int i = 0; i < operators.size(); i++) {
				r += " " + operators.get(i) + " " + types.get(i + 1);
			}
			return r;
		}
		
		@Override
		protected ASTTypeExpression parse() throws ParseException {
			final ASTTypeExpression first = ASTTypeExpressions.parse(this, false, true, true);
			types.add(first);
			WordToken op;
			while ((op = try2("&", "|")) != null) {
				operators.add(new ASTOperatorLink(this, op, true));
				types.add(ASTTypeExpressions.parse(this, false, true, true));
			}
			if (types.size() == 1)
				return first;
			else
				return this;
		}
		
		// TODO use proper operator order
		
		@Override
		public @NonNull IRTypeUse staticallyKnownType() {
			IRTypeUse o = types.get(0).staticallyKnownType();
			for (int i = 0; i < operators.size(); i++) {
				final IRTypeUse o2 = types.get(i + 1).staticallyKnownType();
				if ("&".equals(operators.get(i).getName()))
					o = new IRAndTypeUse(o, o2);
				else
					o = new IROrTypeUse(o, o2);
			}
			return o;
		}
		
		@Override
		public @NonNull IRTypeUse interpret(final InterpreterContext context) {
			IRTypeUse o = types.get(0).interpret(context);
			for (int i = 0; i < operators.size(); i++) {
				final IRTypeUse o2 = types.get(i + 1).interpret(context);
				if ("&".equals(operators.get(i).getName()))
					o = new IRAndTypeUse(o, o2);
				else
					o = new IROrTypeUse(o, o2);
			}
			return o;
		}
	}
	
	/**
	 * A type use that is a single word, which can be either a normal type or a generic parameter.
	 */
	public static class ASTSimpleTypeUse extends AbstractASTElement<ASTSimpleTypeUse> implements ASTTypeExpression {
		
		public final ASTLink<IRTypeDefinitionOrGenericTypeRedefinition> typeDeclaration = new ASTLink<IRTypeDefinitionOrGenericTypeRedefinition>(this) {
			@Override
			protected @Nullable IRTypeDefinitionOrGenericTypeRedefinition tryLink(final String name) {
				// A type is either defined in the same file, or imported.
				// From types in the same file only siblings and siblings of parents are valid candidates.
				ASTElement start = parent();
				if (start instanceof ASTTypeDeclaration) { // i.e. this type is in the declaration of a type as a parent or such // TODO improve this (documentation + implementation - currently depends on no elements between the type decl and this type)
					if (name.equals(((ASTTypeDeclaration) start).name()))
						return ((ASTTypeDeclaration) start).getIR();
					start = start.parent(); // prevent looking at members of the implementing/extending type (also prevents an infinite loop)
				}
				for (ASTElement p = start; p != null; p = p.parent()) {
					if (p instanceof ASTTypeDeclaration) {
						final IRTypeDefinition type = ((ASTTypeDeclaration) p).getIR();
						// generic types
						final IRGenericTypeRedefinition genericType = type.getGenericTypeByName(name);
						if (genericType != null)
							return genericType;
						// inner classes and interfaces // TODO make part of interpreted or not?
						final List<ASTTypeDeclaration> declarations = ((ASTTypeDeclaration) p).getDirectChildrenOfType(ASTTypeDeclaration.class);
						for (final ASTTypeDeclaration d : declarations) {
							if (name.equals(d.name())) {
								return d.getIR();
							}
						}
					} else if (p instanceof ASTBrokkrFile) {
						final Module m = ((ASTBrokkrFile) p).module;
						if (m == null)
							return null;
						return m.getType(name);
					} else if (p instanceof ASTAttribute) {
						for (final ASTGenericTypeDeclaration gp : ((ASTAttribute) p).modifiers().genericParameters) {
							if (name.equals(gp.name()))
								return gp.getIR();
						}
					}
				}
				return null;
			}
		};
		
		@Override
		public String toString() {
			return "" + typeDeclaration.getName();
		}
		
		@Override
		protected ASTSimpleTypeUse parse() throws ParseException {
			typeDeclaration.setName(oneTypeIdentifierToken());
			return this;
		}
		
		@Override
		public IRTypeUse interpret(final InterpreterContext context) {
			final IRTypeDefinitionOrGenericTypeRedefinition type = typeDeclaration.get();
			return type == null ? new IRUnknownTypeUse() : type.getUse(Collections.EMPTY_MAP);
		}
		
		@Override
		public IRTypeUse staticallyKnownType() {
			final IRTypeDefinitionOrGenericTypeRedefinition type = typeDeclaration.get();
			return type == null ? new IRUnknownTypeUse() : type.getUse(Collections.EMPTY_MAP);
		}
		
		public @Nullable IRTypeDefinitionOrGenericTypeRedefinition definition() {
			final IRTypeDefinitionOrGenericTypeRedefinition type = typeDeclaration.get();
			return type == null ? null : type;
		}
	}
	
	/**
	 * A type use with modifiers, e.g. 'mod exclusive C'.
	 */
	public static class ASTModifierTypeUse extends AbstractASTElement<ASTTypeExpression> implements ASTTypeExpression {
		
		public final List<ASTModifierTypeUseModifier> modifiers = new ArrayList<>();
		/**
		 * Either a {@link ASTSimpleTypeUse} or a {@link ASTTypeWithGenericArguments}
		 */
		public @Nullable ASTTypeExpression type;
		
		public ASTModifierTypeUse(final ASTModifierTypeUseModifier firstModifier) {
			modifiers.add(firstModifier);
			firstModifier.setParent(this);
		}
		
		@Override
		public String toString() {
			return (modifiers.size() == 0 ? "" : StringUtils.join(modifiers, " ") + " ") + type;
		}
		
		@Override
		protected ASTTypeExpression parse() throws ParseException {
			do {
				final ASTModifierTypeUseModifier e = ASTModifierTypeUseModifier.tryParse(this);
				if (e != null) {
					modifiers.add(e);
					continue;
				}
			} while (false);
			
			// TODO is 'modifier Self' possible?
			type = one(ASTSimpleTypeUse.class);
			
			return this;
		}
		
		@Override
		public IRTypeUse interpret(final InterpreterContext context) {
			final IRTypeUse result = type != null ? type.interpret(context) : new IRUnknownTypeUse();
//			for (final ModifierTypeUseModifierElement mod : modifiers) {
//				if (mod.modifiability != null) {
//					result.setModifiability(mod.modifiability);
//				} else if (mod.exclusivity != null) {
//					result.setExclusivity(mod.exclusivity);
//				}
//			}
			return result;
		}
		
		@Override
		public IRTypeUse staticallyKnownType() {
			final IRTypeUse result = type != null ? type.staticallyKnownType() : new IRUnknownTypeUse();
//			for (final ModifierTypeUseModifierElement mod : modifiers) {
//				if (mod.modifiability != null) {
//					result.setModifiability(mod.modifiability);
//				} else if (mod.exclusivity != null) {
//					result.setExclusivity(mod.exclusivity);
//				}
//			}
			return result;
		}
	}
	
	/**
	 * a type use modifier like modifiability or exclusivity, optionally copied from an expression
	 */
	public static class ASTModifierTypeUseModifier extends AbstractASTElement<ASTModifierTypeUseModifier> {
		
		public final @Nullable Modifiability modifiability;
		public final @Nullable Exclusivity exclusivity;
		public final @Nullable Optional optional;
		
		public @Nullable ASTExpression from;
		
		public static @Nullable ASTModifierTypeUseModifier tryParse(final AbstractASTElement<?> parent) throws ParseException {
			final Modifiability modifiability = Modifiability.parse(parent);
			if (modifiability != null)
				return parent.one(new ASTModifierTypeUseModifier(modifiability));
			final Exclusivity exclusivity = Exclusivity.parse(parent);
			if (exclusivity != null)
				return parent.one(new ASTModifierTypeUseModifier(exclusivity));
			final Optional optional = Optional.parse(parent);
			if (optional != null)
				return parent.one(new ASTModifierTypeUseModifier(optional));
			return null;
		}
		
		private ASTModifierTypeUseModifier(final Modifiability modifiability) {
			this.modifiability = modifiability;
			exclusivity = null;
			optional = null;
		}
		
		private ASTModifierTypeUseModifier(final Exclusivity exclusivity) {
			modifiability = null;
			this.exclusivity = exclusivity;
			optional = null;
		}
		
		private ASTModifierTypeUseModifier(final Optional optional) {
			modifiability = null;
			exclusivity = null;
			this.optional = optional;
		}
		
		@Override
		public String toString() {
			return (modifiability != null ? modifiability : exclusivity != null ? exclusivity : optional)
					+ (from == null ? "" : "@" + (from instanceof ASTVariableOrUnqualifiedAttributeUse || from instanceof ASTThis ? from : "(" + from + ")"));
		}
		
		@Override
		protected ASTModifierTypeUseModifier parse() throws ParseException {
			if (try_('@')) {
				if (!tryGroup('(', () -> {
					from = ASTExpressions.parse(this);
				}, ')')) {
					if (peekNext("this"))
						from = one(ASTThis.class);
					else
						from = one(ASTVariableOrUnqualifiedAttributeUse.class);
				}
			}
			return this;
		}
	}
	
	/**
	 * A type use with generic arguments, e.g. 'A<B, C: D>'
	 */
	public static class ASTTypeWithGenericArguments extends AbstractASTElement<ASTTypeWithGenericArguments> implements ASTTypeExpression {
		
		public final ASTSimpleTypeUse baseType;
		
		public ASTTypeWithGenericArguments(final ASTSimpleTypeUse baseType) {
			this.baseType = baseType;
			baseType.setParent(this);
		}
		
		public static ASTTypeExpression withModifiers(final ASTTypeExpression withModifiers, final AbstractASTElement<?> parent) throws ParseException {
			if (withModifiers instanceof ASTSimpleTypeUse)
				return parent.one(new ASTTypeWithGenericArguments((ASTSimpleTypeUse) withModifiers));
			final ASTSimpleTypeUse typeUseElement = (ASTSimpleTypeUse) ((ASTModifierTypeUse) withModifiers).type; // cast is valid, as the other case is constructed here
			assert typeUseElement != null; // shouldn't get here if this is null
			final ASTTypeWithGenericArguments generic = parent.one(new ASTTypeWithGenericArguments(typeUseElement));
			((ASTModifierTypeUse) withModifiers).type = generic;
			generic.setParent(withModifiers);
			return withModifiers;
		}
		
		public final List<ASTGenericArgument> genericArguments = new ArrayList<>();
		
		@Override
		public String toString() {
			return baseType + "<" + StringUtils.join(genericArguments, ",") + ">";
		}
		
		@Override
		protected ASTTypeWithGenericArguments parse() throws ParseException {
			oneGroup('<', () -> {
				do {
					genericArguments.add(one(ASTGenericArgument.class));
				} while (try_(','));
			}, '>');
			return this;
		}
		
		private IRTypeUse _interpret(final @Nullable InterpreterContext context) {
			final Map<IRGenericTypeDefinition, IRTypeUse> genericArguments = new HashMap<>();
			for (final ASTGenericArgument ga : this.genericArguments) {
				final IRGenericTypeRedefinition param = ga.parameter.get();
				final ASTTypeExpression value = ga.value;
				if (param == null || value == null)
					continue;
				genericArguments.put(param.definition(), context == null ? value.staticallyKnownType() : value.interpret(context));
			}
			final IRTypeDefinitionOrGenericTypeRedefinition baseDefinition = baseType.definition();
			return baseDefinition == null ? new IRUnknownTypeUse() : baseDefinition.getUse(genericArguments);
		}
		
		@Override
		public IRTypeUse staticallyKnownType() {
			return _interpret(null);
		}
		
		@Override
		public @NonNull IRTypeUse interpret(final InterpreterContext context) {
			return _interpret(context);
		}
	}
	
	/**
	 * A generic argument to a type use.
	 */
	public static class ASTGenericArgument extends AbstractASTElement<ASTGenericArgument> {
		public ASTLink<IRGenericTypeRedefinition> parameter = new ASTLink<IRGenericTypeRedefinition>(this) {
			@Override
			protected @Nullable IRGenericTypeRedefinition tryLink(final String name) {
				assert parent != null;
				final IRGenericTypeUse genericTypeUse = ((ASTTypeWithGenericArguments) parent).baseType.staticallyKnownType().getGenericTypeByName(name);
				if (genericTypeUse == null)
					return null;
				return genericTypeUse.redefinition();
			}
		};
		public boolean wildcard;
		public @Nullable ASTTypeExpression value;
		
		@Override
		public String toString() {
			return (parameter.getName() == null ? "" : parameter.getName() + ": ") + value;//(wildcard ? "?" + (extends_ == null ? "" : " extends " + extends_) + (super_ == null ? "" : " super " + super_) : value);
		}
		
		@Override
		protected ASTGenericArgument parse() throws ParseException {
			if (peekNext() instanceof WordToken && peekNext(':', 1, true)) {
				parameter.setName(oneIdentifierToken());
				next(); // skip ':'
			} else {
				parameter.setName(null); // FIXME what to link?
			}
//			if (try_('?')) {
//				wildcard = true;
//				unordered(() -> {
//					if (try_("extends"))
//						extends_ = ActualType.parse(this, true, true);
//				}, () -> {
//					if (try_("super"))
//						super_ = ActualType.parse(this, true, true);
//				});
//			} else {
//				value = ActualType.parse(this, true, true);
//			}
			value = ASTTypeExpressions.parse(this, true, true, true); // Expressions.parse(this, false);
			return this;
		}
	}
	
	/**
	 * Access to a generic type of a type, e.g. 'A.B'.
	 * TODO is this the same as a static attribute access?
	 */
	public static class ASTGenericTypeAccess extends AbstractASTElement<ASTGenericTypeAccess> implements ASTTypeExpression {
		public final ASTTypeUse target;
		private final ASTLink<IRGenericTypeUse> genericType = new ASTLink<IRGenericTypeUse>(this) {
			@Override
			protected @Nullable IRGenericTypeUse tryLink(final String name) {
				return target.staticallyKnownType().getGenericTypeByName(name);
			}
		};
		
		public ASTGenericTypeAccess(final ASTTypeUse target) {
			this.target = target;
			target.setParent(this);
		}
		
		@Override
		public String toString() {
			return target + "." + genericType.getName();
		}
		
		@Override
		protected ASTGenericTypeAccess parse() throws ParseException {
			one('.');
			genericType.setName(oneTypeIdentifierToken());
			ASTGenericTypeAccess result = this;
			while (try_('.')) {
				result = new ASTGenericTypeAccess(this);
				result.genericType.setName(oneTypeIdentifierToken());
			}
			return result;
		}
		
		@Override
		public IRTypeUse getIRType() {
			return staticallyKnownType();
		}
		
		@Override
		public IRTypeUse staticallyKnownType() {
			final IRGenericTypeUse gtu = genericType.get();
			return gtu == null ? new IRUnknownTypeUse() : gtu;
		}
		
		@Override
		public @NonNull IRTypeUse interpret(final InterpreterContext context) {
			final IRGenericTypeUse genericTypeUse = genericType.get();
			if (genericTypeUse == null)
				return new IRUnknownTypeUse();
			return target.interpret(context).nativeClass().getGenericType(genericTypeUse.definition());
		}
	}
	
}
