package ch.njol.brokkr.compiler.ast;

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

import ch.njol.brokkr.compiler.Module;
import ch.njol.brokkr.compiler.ParseException;
import ch.njol.brokkr.compiler.Token;
import ch.njol.brokkr.compiler.Token.CommentToken;
import ch.njol.brokkr.compiler.Token.LowercaseWordToken;
import ch.njol.brokkr.compiler.Token.NumberToken;
import ch.njol.brokkr.compiler.Token.StringToken;
import ch.njol.brokkr.compiler.Token.SymbolToken;
import ch.njol.brokkr.compiler.Token.UppercaseWordToken;
import ch.njol.brokkr.compiler.Token.WhitespaceToken;
import ch.njol.brokkr.compiler.Token.WordToken;
import ch.njol.brokkr.compiler.ast.Interfaces.Expression;
import ch.njol.brokkr.compiler.ast.Interfaces.FormalAttribute;
import ch.njol.brokkr.compiler.ast.Interfaces.FormalError;
import ch.njol.brokkr.compiler.ast.Interfaces.FormalParameter;
import ch.njol.brokkr.compiler.ast.Interfaces.GenericParameter;
import ch.njol.brokkr.compiler.ast.Interfaces.HasVariables;
import ch.njol.brokkr.compiler.ast.Interfaces.TypeDeclaration;
import ch.njol.brokkr.compiler.ast.Interfaces.TypeExpression;
import ch.njol.brokkr.compiler.ast.Interfaces.TypeUse;
import ch.njol.brokkr.compiler.ast.Interfaces.TypedElement;
import ch.njol.brokkr.compiler.ast.Members.Member;
import ch.njol.brokkr.compiler.ast.Statements.Statement;
import ch.njol.brokkr.compiler.ast.Statements.VariableDeclarations;
import ch.njol.brokkr.compiler.ast.Statements.VariableDeclarationsVariable;
import ch.njol.brokkr.compiler.ast.TopLevelElements.BrokkrFile;
import ch.njol.brokkr.data.Exclusivity;
import ch.njol.brokkr.data.Kleenean;
import ch.njol.brokkr.data.Modifiability;
import ch.njol.brokkr.interpreter.InterpretedNormalObject;
import ch.njol.brokkr.interpreter.InterpretedNullConstant;
import ch.njol.brokkr.interpreter.InterpretedNullConstant.InterpretedNativeNullClass;
import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedBrokkrParameterDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedGenericTypeRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedNativeTypeDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedParameterDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedParameterRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedVariableOrAttributeRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedVariableRedefinition;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeBrokkrClass;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedTuple;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedTuple.InterpretedNativeTupleValueAndEntry;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedTuple.InterpretedTypeTuple;
import ch.njol.brokkr.interpreter.uses.InterpretedAndTypeUse;
import ch.njol.brokkr.interpreter.uses.InterpretedOrTypeUse;
import ch.njol.brokkr.interpreter.uses.InterpretedSimpleClassUse;
import ch.njol.brokkr.interpreter.uses.InterpretedSimpleTypeUse;
import ch.njol.brokkr.interpreter.uses.InterpretedTypeObject;
import ch.njol.brokkr.interpreter.uses.InterpretedTypeUse;
import ch.njol.util.StringUtils;

public class Expressions {
	
	public static Expression parse(final AbstractElement<?> parent) throws ParseException {
		return parse(parent, true);
	}
	
	public static Expression parse(final AbstractElement<?> parent, final boolean allowComparisons) throws ParseException {
		
		if (parent.peekNext('{'))
			return parent.one(Block.class);
		if (parent.peekNext("create"))
			return parent.one(AnonymousObject.class);
		
		// TODO "[params] ->" syntax
		if (parent.peekNext("var") && parent.peekNext(1, true) instanceof LowercaseWordToken && parent.peekNext("->", 2, true))
			return parent.one(Lambda.class);
		if (parent.peekNext() instanceof LowercaseWordToken && parent.peekNext("->", 1, true))
			return parent.one(new Lambda(parent.one(new LambdaParameter(true))));
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
						return parent.one(Lambda.class);
					} else {
						break;
					}
				}
				i++;
			}
		}
		
		final Expression expr = parent.one(new OperatorExpression(allowComparisons));
		SymbolToken sym;
		if ((sym = parent.try2('=')) != null) {
			// TODO this is way too complicated - make it simpler if possible (by changing the 'expr' line above/parsing assignment first)
			// TODO directly determine if a variable is local or an unqualified attribute?
			if (expr instanceof VariableOrUnqualifiedAttributeUse) {
				final Link<InterpretedVariableOrAttributeRedefinition> varOrAttribute = ((VariableOrUnqualifiedAttributeUse) expr).varOrAttribute;
				if (!((VariableOrUnqualifiedAttributeUse) expr).arguments.isEmpty()) {
					parent.errorFatal("Left-hand side of an assignment must be a variable or a field", expr.regionStart(), expr.regionLength());
					return expr;
				}
				expr.setParent(null);
				final LocalVariableOrUnqualifiedAttributeAssignment a = new LocalVariableOrUnqualifiedAttributeAssignment(varOrAttribute);
				sym.setParent(a);
				parent.one(a);
				return a;
			} else if (expr instanceof AccessExpression && !((AccessExpression) expr).meta && ((AccessExpression) expr).access instanceof DirectAttributeAccess) {
				final AccessExpression e = (AccessExpression) expr;
				final DirectAttributeAccess da = (DirectAttributeAccess) e.access;
				assert da != null;
				if (e.meta || e.nullSafe || da.negated || da.allResults || !da.arguments.isEmpty()) {
					parent.errorFatal("Left-hand side of an assignment must be a variable or a field", expr.regionStart(), expr.regionLength());
					return expr;
				}
				e.setParent(null);
				da.setParent(null);
				final AttributeAssignment assignment = new AttributeAssignment(e.target, da.attribute);
				sym.setParent(assignment);
				parent.one(assignment);
				return assignment;
			} else {
				if (expr.regionLength() > 0)
					parent.errorFatal("Left-hand side of an assignment must be a variable or a field", expr.regionStart(), expr.regionLength());
				return expr;
			}
		} else if ((sym = parent.try2('?')) != null) { // no need to check for '?.' and '?~' as those are parsed before in OperatorExpression
			final TernaryIf i = new TernaryIf(expr);
			sym.setParent(i);
			parent.one(i);
			return i;
		} else if ((sym = parent.try2('#')) != null) {
			final ErrorHandlingExpression eh = new ErrorHandlingExpression(expr);
			sym.setParent(eh);
			parent.one(eh);
			return eh;
		} else if (expr instanceof TypeUse && parent.peekNext() instanceof LowercaseWordToken && parent.peekNext("->", 1, true)) {
			return parent.one(new Lambda(parent.one(new LambdaParameter((TypeUse) expr))));
		} else {
			return expr;
		}
	}
	
	public static class Block extends AbstractElement<Block> implements Expression {
		public @Nullable Expression expression;
		public List<Statement> statements = new ArrayList<>();
		
		@Override
		public InterpretedTypeUse interpretedType() {
			// TODO gather all possible return paths and get most specific common supertype from all
			// note below: block doesn't actually return anything; TODO make lambdas better
//			statements.forEach(s -> s.forEach(ep -> {
//				if (ep instanceof Return)
//					((Return) ep).results;
//			}));
			throw new InterpreterException("not implemented");
		}
		
		public Block() {}
		
		@SuppressWarnings("null")
		public Block(final Statement... statements) {
			this.statements.addAll(Arrays.asList(statements));
		}
		
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			for (final Statement s : statements) {
				s.interpret(context);
				if (context.isReturning)
					return null; // a block doesn't return anything - the result is stored in whatever result variable(s) is/are used
				// TODO different 'return's for returning from the outermost function, and only returning from a single block?
			}
			return null;
		}
		
		@Override
		protected Block parse() throws ParseException {
			oneRepeatingGroup('{', () -> {
				if (statements.isEmpty()) {
					final Element e = Statement.parseWithExpression(this);
					if (e instanceof Expression)
						expression = (Expression) e;
					else
						statements.add((Statement) e);
				} else {
					statements.add(Statement.parse(this));
					assert expression == null;
				}
			}, '}');
			return this;
		}
	}
	
	public static class AnonymousObject extends AbstractElement<AnonymousObject> implements Expression, TypeDeclaration {
		public @Nullable TypeUse type;
		public List<Member> members = new ArrayList<>();
		
		@Override
		public @Nullable String name() {
			assert false;
			return "Anonymous type";
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
		public List<? extends Member> declaredMembers() {
			return members;
		}
		
		@SuppressWarnings("null")
		@Override
		public InterpretedTypeUse parentTypes() {
			return type.staticallyKnownType();
		}
		
		@Override
		public List<? extends GenericParameter> genericParameters() {
			return Collections.EMPTY_LIST;
		}
		
		@Override
		protected AnonymousObject parse() throws ParseException {
			one("create");
			type = TypeExpressions.parse(this, true, false);
			oneRepeatingGroup('{', () -> {
				members.add(Member.parse(this));
			}, '}');
			return this;
		}
		
		@Override
		public InterpretedTypeUse interpretedType() {
			return new InterpretedSimpleTypeUse(interpreted()); // a new anonymous object (which this represents) is an instance of its anonymous type (which this also represents)
		}
		
		@Override
		public InterpretedNativeBrokkrClass interpreted() {
			return new InterpretedNativeBrokkrClass(this);
		}
		
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			return new InterpretedNormalObject(new InterpretedSimpleClassUse(interpreted()));
		}
	}
	
	public static class Lambda extends AbstractElement<Lambda> implements Expression, HasVariables {
		public final List<LambdaParameter> parameters = new ArrayList<>();
		public @Nullable Expression code;
		
		public Lambda() {}
		
		public Lambda(final LambdaParameter param) {
			parameters.add(param);
			param.setParent(this);
		}
		
		@Override
		protected Lambda parse() throws ParseException {
			if (parameters.isEmpty()) {
				if (!tryGroup('[', () -> {
					do {
						parameters.add(one(LambdaParameter.class));
					} while (try_(','));
				}, ']')) {
					parameters.add(one(LambdaParameter.class));
				}
			}
			one("->");
			code = Expressions.parse(this);
			return this;
			/*
			Lambda returns Lambda:
				=> ((parameters+=LambdaParameterAtomicType | '[' (parameters+=LambdaParameter (',' parameters+=LambdaParameter)*)?
				']') '->')
				(=> '(' results=ValueTupleNoBracesOneRequired ')' | result=Expression | block=Block);
			
			LambdaParameter returns Parameter:
				('var' | type=ActualType)? name=VARIABLE_IDENTIFIER;
			
			LambdaParameterAtomicType returns Parameter:
				('var' | type=ActualTypeWithoutModifiers<false>)? name=VARIABLE_IDENTIFIER;
			*/
		}
		
		@SuppressWarnings("null")
		@Override
		public List<? extends InterpretedVariableRedefinition> allVariables() {
			return parameters.stream().map(p -> p.interpreted()).collect(Collectors.toList());
		}
		
		@SuppressWarnings("null")
		@Override
		public InterpretedTypeUse interpretedType() {
			return code.interpretedType();
		}
		
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			throw new InterpreterException("not implemented");
//			return new InterpretedClosure() {
//				@Override
//				InterpretedObject interpret(final Map<InterpretedParameter, InterpretedObject> arguments) {
//					// TODO Auto-generated method stub
//					return null;
//				}
//			};
		}
	}
	
	public static class LambdaParameter extends AbstractElement<LambdaParameter> implements FormalParameter {
		public @Nullable TypeUse type;
		public @Nullable LowercaseWordToken name;
		
		@Override
		public @Nullable WordToken nameToken() {
			return name;
		}
		
		public LambdaParameter() {}
		
		public LambdaParameter(final TypeUse type) {
			this.type = type;
			type.setParent(this);
		}
		
		private boolean withoutType;
		
		public LambdaParameter(final boolean withoutType) {
			this.withoutType = withoutType;
		}
		
		@Override
		protected @NonNull LambdaParameter parse() throws ParseException {
			if (type == null && !withoutType && !try_("var"))
				type = TypeExpressions.parse(this, false, false);
			name = oneVariableIdentifierToken();
			return this;
		}
		
		@Override
		public InterpretedParameterRedefinition interpreted() {
			return new InterpretedBrokkrParameterDefinition(this);
		}
		
		@Override
		public InterpretedTypeUse interpretedType() {
			if (type != null)
				return type.staticallyKnownType();
			throw new InterpreterException("not implemented"); // TODO infer type
		}
	}
	
	// TODO think about whether assignment should really be an expression - this can be handy, but can also hide state changes.
	public static class LocalVariableOrUnqualifiedAttributeAssignment extends AbstractElement<LocalVariableOrUnqualifiedAttributeAssignment> implements Expression {
		public final Link<? extends InterpretedVariableOrAttributeRedefinition> varOrAttribute;
		public @Nullable Expression value;
		
		@SuppressWarnings("null")
		@Override
		public InterpretedTypeUse interpretedType() {
			return value.interpretedType();
		}
		
		public LocalVariableOrUnqualifiedAttributeAssignment(final Link<? extends InterpretedVariableOrAttributeRedefinition> varOrAttribute) {
			this.varOrAttribute = varOrAttribute;
			addLink(varOrAttribute);
		}
		
		@SuppressWarnings("null")
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			final InterpretedObject value = this.value.interpret(context);
			final InterpretedVariableOrAttributeRedefinition varOrAttribute = this.varOrAttribute.get();
			if (varOrAttribute instanceof InterpretedVariableRedefinition) {
				context.setLocalVariableValue(((InterpretedVariableRedefinition) varOrAttribute).definition(), value);
			} else {
				((InterpretedNormalObject) context.getThisObject()).setAttributeValue(((InterpretedAttributeRedefinition) varOrAttribute).definition(), value);
			}
			return value;
		}
		
		@Override
		protected LocalVariableOrUnqualifiedAttributeAssignment parse() throws ParseException {
			value = Expressions.parse(this);
			return this;
		}
	}
	
	public static class AttributeAssignment extends AbstractElement<AttributeAssignment> implements Expression {
		public final Expression target;
		public final Link<? extends InterpretedAttributeRedefinition> attribute;
		public @Nullable Expression value;
		
		public AttributeAssignment(final Expression target, final Link<? extends InterpretedAttributeRedefinition> attribute) {
			this.target = target;
			this.attribute = attribute;
			target.setParent(this);
			addLink(attribute);
		}
		
		@Override
		protected AttributeAssignment parse() throws ParseException {
			value = Expressions.parse(this);
			return this;
		}
		
		@SuppressWarnings("null")
		@Override
		public InterpretedTypeUse interpretedType() {
			return value.interpretedType();
		}
		
		@SuppressWarnings("null")
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			final InterpretedObject value = this.value.interpret(context);
			final InterpretedObject target = this.target.interpret(context);
			if (target instanceof InterpretedNormalObject)
				((InterpretedNormalObject) target).setAttributeValue(attribute.get().definition(), value);
			else
				throw new InterpreterException("Tried to set an attribute on a native object");
			return value;
		}
	}
	
	public static class TernaryIf extends AbstractElement<TernaryIf> implements Expression {
		public Expression condition;
		public @Nullable Expression then, otherwise;
		
		public TernaryIf(final Expression condition) {
			this.condition = condition;
			condition.setParent(this);
		}
		
		@Override
		protected TernaryIf parse() throws ParseException {
			then = Expressions.parse(this);
			one(':');
			otherwise = Expressions.parse(this);
			return this;
		}
		
		@SuppressWarnings("null")
		@Override
		public InterpretedTypeUse interpretedType() {
			return new InterpretedOrTypeUse(then.interpretedType(), otherwise.interpretedType());
		}
		
		@SuppressWarnings("null")
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			final InterpretedObject cond = condition.interpret(context);
			return cond.equals(getInterpreter().bool(true)) ? then.interpret(context) : otherwise.interpret(context);
		}
	}
	
	public static class ErrorHandlingExpression extends AbstractElement<Element> implements Expression {
		public Expression expression;
		public boolean negated;
		public Link<FormalError> error = new Link<FormalError>(this) {
			@Override
			protected @Nullable FormalError tryLink(final String name) {
				//expression.type();
				// TODO Auto-generated method stub
				return null;
			}
		};
		public List<ErrorHandlingExpressionParameter> parameters = new ArrayList<>();
		public @Nullable Expression value;
		
		public ErrorHandlingExpression(final Expression expression) {
			this.expression = expression;
			expression.setParent(this);
		}
		
		@Override
		protected Element parse() throws ParseException {
			negated = try_('!');
			error.setName(oneVariableIdentifierToken());
			if (try_('(')) {
				do {
					parameters.add(one(ErrorHandlingExpressionParameter.class));
				} while (try_(','));
				one(')');
			}
			one(':');
			value = Expressions.parse(this);
			return this;
		}
		
		@SuppressWarnings("null")
		@Override
		public InterpretedTypeUse interpretedType() {
			return value == null ? null : value.interpretedType();
		}
		
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			throw new InterpreterException("not implemented");
		}
	}
	
	public static class ErrorHandlingExpressionParameter extends AbstractElement<ErrorHandlingExpressionParameter> implements FormalParameter {
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
		protected ErrorHandlingExpressionParameter parse() throws ParseException {
			if (!try_("var"))
				type = TypeExpressions.parse(this, true, true);
			parameter.setName(oneVariableIdentifierToken());
			return this;
		}
		
		@SuppressWarnings("null")
		@Override
		public InterpretedTypeUse interpretedType() {
			if (type != null)
				return type.staticallyKnownType();
			final FormalParameter param = parameter.get();
			if (param != null)
				return param.interpretedType();
			return null;
		}
		
		@Override
		public InterpretedParameterRedefinition interpreted() {
			return new InterpretedBrokkrParameterDefinition(this); // TODO correct? is this even a parameter?
		}
	}
	
	public static class OperatorExpression extends AbstractElement<Expression> implements Expression {
		private final boolean allowComparisons;
		
		public List<Expression> expressions = new ArrayList<>();
		public List<Link<InterpretedAttributeRedefinition>> operators = new ArrayList<>();
		
		public OperatorExpression() {
			this(true);
		}
		
		public OperatorExpression(final boolean allowComparisons) {
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
		protected Expression parse() throws ParseException {
			final Expression first = one(OperatorExpressionPart.class);
			expressions.add(first);
			WordToken op;
			while ((op = try2(allowComparisons ? opsWithComp : opsWithoutComp)) != null) {
				operators.add(new OperatorLink(this, op, true));
				expressions.add(one(OperatorExpressionPart.class));
			}
			if (expressions.size() == 1)
				return first;
			else
				return this;
		}
		
		// TODO use proper operator order
		
		@SuppressWarnings("null")
		@Override
		public InterpretedTypeUse interpretedType() {
			return operators.get(operators.size() - 1).get().mainResultType();
		}
		
		@SuppressWarnings("null")
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			InterpretedObject o = expressions.get(0).interpret(context);
			for (int i = 0; i < operators.size(); i++) {
				final InterpretedObject o2 = expressions.get(i + 1).interpret(context);
				final InterpretedAttributeDefinition attr = operators.get(i).get().definition();
				o = attr.interpretDispatched(o, Collections.singletonMap(attr.parameters().get(0).definition(), o2), false);
			}
			return o;
		}
	}
	
	public static class OperatorExpressionPart extends AbstractElement<Expression> implements Expression {
		public @Nullable Expression expression;
		public Link<InterpretedAttributeRedefinition> prefixOperator = new OperatorLink(this, null, false);
		
		@Override
		protected Expression parse() throws ParseException {
			prefixOperator.setName(try2("!", "-"));
			Expression expr;
			expression = expr = AccessExpression.parse(this);
			if (expr == null) {
				expectedFatal("an expression");
				return this;
			}
			if (prefixOperator.getNameToken() == null)
				return expr;
			else
				return this;
		}
		
		@SuppressWarnings("null")
		@Override
		public InterpretedTypeUse interpretedType() {
			return prefixOperator.get().mainResultType();
		}
		
		@SuppressWarnings("null")
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			final InterpretedObject object = expression.interpret(context);
			return prefixOperator.get().interpretDispatched(object, Collections.EMPTY_MAP, false);
		}
	}
	
	public static class AccessExpression extends AbstractElement<Expression> implements Expression {
		public final Expression target;
		public boolean nullSafe, meta;
		public @Nullable Access access;
		
		@SuppressWarnings("null")
		@Override
		public InterpretedTypeUse interpretedType() {
			return access == null ? null : access.interpretedType();
		}
		
		public AccessExpression(final Expression target) {
			this.target = target;
			target.setParent(this);
		}
		
		public final static @Nullable Expression parse(final AbstractElement<?> parent) throws ParseException {
			final Token t1 = parent.peekNext(), t2 = parent.peekNext(1, true);
			if (t1 instanceof LowercaseWordToken && (t2 instanceof WordToken && //
					!Arrays.asList(OperatorExpression.opsWithComp).contains(((WordToken) t2).word) || parent.peekNext('@', 1, true))) {// 'modifier[@x] ... Type'
				final TypeExpression e = parent.one(ModifierTypeUseElement.class);
				if (parent.peekNext('<'))
					return GenericTypeUseElement.withModifiers(e, parent);
				return e;
			}
			
			final Expression first = AtomicExpression.parse(parent);
			if (first == null)
				return null;
			Expression e = first;
			while (true) {
				final Expression next = parent.one(new AccessExpression(e));
				if (next == e) {
					e.setParent(parent);
					return e;
				}
				e = next;
			}
		}
		
		@SuppressWarnings("null")
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			final InterpretedObject target = this.target.interpret(context);
			if (nullSafe && target instanceof InterpretedNullConstant)
				return new InterpretedNullConstant();
			if (access instanceof DirectAttributeAccess) {
				final DirectAttributeAccess daa = ((DirectAttributeAccess) access);
				final InterpretedAttributeDefinition attribute = daa.attribute.get().definition();
				final InterpretedObject obj = attribute.interpretDispatched(target, Argument.makeInterpretedArgumentMap(attribute, daa.arguments, context), daa.allResults);
				if (daa.negated)
					return getInterpreter().getInterface("lang", "Boolean").getAttributeByName("negated").interpretDispatched(obj, Collections.EMPTY_MAP, false);
				return obj;
			} else {
				((Cascade) access).block.interpret(context);
				return target;
			}
		}
		
		@Override
		protected Expression parse() throws ParseException {
			final String op = try_(".", "?.", "~", "?~"); // note: must try '?' together with '.' or '~', as it is also used by the ternary operator '? :'
			if (op == null)
				return target;
			nullSafe = op.startsWith("?");
			meta = op.endsWith("~");
			if (!meta && peekNext('{')) {
				access = one(Cascade.class);
			} else {
				access = one(DirectAttributeAccess.class);
			}
			return this;
		}
	}
	
	public static interface Access extends TypedElement {}
	
	public static class DirectAttributeAccess extends AbstractElement<DirectAttributeAccess> implements Access, MethodCall {
		public boolean negated;
		public final Link<InterpretedAttributeRedefinition> attribute = new Link<InterpretedAttributeRedefinition>(this) {
			@Override
			protected @Nullable InterpretedAttributeRedefinition tryLink(final String name) {
				@SuppressWarnings("null")
				final InterpretedTypeUse type = ((AccessExpression) parent).target.interpretedType();
				final InterpretedAttributeRedefinition a = type.getAttributeByName(name);
				if (a == null && name.endsWith("_"))
					return type.getAttributeByName("" + name.substring(0, name.length() - 1));
				return a;
			}
		};
		public boolean allResults;
		
		public final List<Argument> arguments = new ArrayList<>();
		
		@Override
		public @Nullable InterpretedAttributeRedefinition attribute() {
			return attribute.get();
		}
		
		@SuppressWarnings("null")
		@Override
		public InterpretedTypeUse interpretedType() {
			return allResults ? attribute.get().allResultTypes() : attribute.get().mainResultType();
		}
		
		@Override
		protected DirectAttributeAccess parse() throws ParseException {
			negated = try_('!');
			final WordToken name = oneVariableIdentifierToken();
			attribute.setName(name);
			allResults = name.word.endsWith("_"); //try_('!'); // FIXME other symbol - possible: !´`'¬@¦§°%_$£\     // maybe a combination of symbols? e.g. []
			tryGroup('(', () -> {
				do {
					arguments.add(one(Argument.class));
				} while (try_(','));
			}, ')');
			return this;
		}
		
		@Override
		public String toString() {
			return (negated ? "!" : "") + attribute.getName() + (arguments.size() == 0 ? "" : "(...)");
		}
	}
	
	public static class Cascade extends AbstractElement<Cascade> implements Access {
		public @Nullable Block block;
		
		@SuppressWarnings("null")
		@Override
		public InterpretedTypeUse interpretedType() {
			return ((AccessExpression) parent).target.interpretedType();
		}
		
		@Override
		protected Cascade parse() throws ParseException {
			block = one(Block.class);
			return this;
		}
		
		@Override
		public String toString() {
			return "{...}";
		}
	}
	
	public static interface AtomicExpression extends Expression {
		public static @Nullable Expression parse(final AbstractElement<?> parent) throws ParseException {
			// peek is acceptable here, as nobody needs content assist for expression *syntax*.
			if (parent.try_('(')) {
				final Expression[] e = new Expression[1];
				parent.until(() -> {
					e[0] = Expressions.parse(parent);
				}, ')', false);
				if (e[0] != null)
					return e[0];
			}
			final Token next = parent.peekNext();
			if (next instanceof StringToken)
				return parent.one(StringDeclaration.class);
			if (next instanceof NumberToken)
				return parent.one(NumberConstant.class);
			if (next instanceof UppercaseWordToken)
				return TypeExpressions.parse(parent, false, false);
			if (parent.peekNext('['))
				return parent.one(Tuple.class);
			if (parent.peekNext('~'))
				return parent.one(UnqualifiedMetaAccess.class);
//			if (parent.peekNext('?'))
//				return parent.one(ImplicitLambdaArgument.class);
			if (parent.peekNext("this"))
				return parent.one(This.class);
			if (parent.peekNext("null"))
				return parent.one(Null.class);
			if (parent.peekNext("arguments"))
				return parent.one(ArgumentsKeyword.class);
			if (parent.peekNext("exists") || parent.peekNext("forall"))
				return parent.one(Quantifier.class);
			if (parent.peekNext("recurse"))
				return parent.one(Recurse.class);
			if (parent.peekNext("old"))
				return parent.one(Old.class);
			if (parent.peekNext("true") || parent.peekNext("false") || parent.peekNext("unknown"))
				return parent.one(KleeneanConstant.class);
			// must be here after all keywords
			if (next instanceof LowercaseWordToken)
				return parent.one(VariableOrUnqualifiedAttributeUse.class);
			return null;
		}
	}
	
	// TODO is this good or bad?
//	public static class ImplicitLambdaArgument extends AbstractElement<ImplicitLambdaArgument> implements Expression {
//		@Override
//		public InterpretedNativeType nativeType() {
//			return null; // TODO
//		}
//
//		@Override
//		protected @NonNull ImplicitLambdaArgument parse() throws ParseException {
//			one('?');
//			return this;
//		}
//	}
	
	public static class NumberConstant extends AbstractElement<NumberConstant> implements Expression {
		public @Nullable BigDecimal value;
		
		@Override
		public String toString() {
			return "" + value;
		}
		
		@SuppressWarnings("null")
		@Override
		public InterpretedTypeUse interpretedType() {
			return getInterpreter().numberConstant(value).nativeClass();
		}
		
		@SuppressWarnings("null")
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			return getInterpreter().numberConstant(value);
		}
		
		@Override
		protected NumberConstant parse() throws ParseException {
			final NumberToken t = next(NumberToken.class);
			if (t != null)
				value = t.value;
			return this;
		}
	}
	
	public static class KleeneanConstant extends AbstractElement<KleeneanConstant> implements Expression {
		public @Nullable Kleenean value;
		
		@Override
		public String toString() {
			assert value != null;
			return "" + value.name().toLowerCase(Locale.ENGLISH);
		}
		
		@Override
		protected KleeneanConstant parse() throws ParseException {
			value = Kleenean.valueOf("" + oneOf("true", "false", "unknown").toUpperCase(Locale.ENGLISH));
			return this;
		}
		
		@Override
		public InterpretedTypeUse interpretedType() {
			return getInterpreter().getTypeUse("lang", value == Kleenean.UNKNOWN ? "Kleenean" : "Boolean");
		}
		
		@SuppressWarnings("null")
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			return getInterpreter().kleenean(value);
		}
	}
	
	/**
	 * The keyword 'this', representing 'the current object'.
	 */
	public static class This extends AbstractElement<This> implements Expression {
		@SuppressWarnings("null")
		@Override
		public InterpretedTypeUse interpretedType() {
			return new InterpretedSimpleTypeUse(getParentOfType(TypeDeclaration.class).interpreted());
		}
		
		@Override
		public String toString() {
			return "this";
		}
		
		@SuppressWarnings("null")
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			return context.getThisObject();
		}
		
		@Override
		protected This parse() throws ParseException {
			one("this");
			return this;
		}
	}
	
	/**
	 * The keyword 'null', representing 'no value'.
	 */
	public static class Null extends AbstractElement<Null> implements Expression {
		@Override
		public String toString() {
			return "null";
		}
		
		@Override
		protected Null parse() throws ParseException {
			one("null");
			return this;
		}
		
		@Override
		public InterpretedTypeUse interpretedType() {
			return new InterpretedSimpleTypeUse(new InterpretedNativeNullClass());
		}
		
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			return new InterpretedNullConstant();
		}
	}
	
	/**
	 * The keyword 'arguments', representing a tuple of all arguments to the current method.
	 */
	public static class ArgumentsKeyword extends AbstractElement<ArgumentsKeyword> implements Expression {
		@Override
		public String toString() {
			return "arguments";
		}
		
		@Override
		protected ArgumentsKeyword parse() throws ParseException {
			one("arguments");
			return this;
		}
		
		@SuppressWarnings("null")
		@Override
		public InterpretedTypeUse interpretedType() {
			return getParentOfType(FormalAttribute.class).interpreted().allParameterTypes();
		}
		
		@SuppressWarnings("null")
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			final List<InterpretedParameterRedefinition> parameters = getParentOfType(FormalAttribute.class).interpreted().parameters();
			final List<InterpretedNativeTupleValueAndEntry> entries = new ArrayList<>();
			for (int i = 0; i < parameters.size(); i++) {
				final InterpretedParameterRedefinition p = parameters.get(i);
				entries.add(new InterpretedNativeTupleValueAndEntry(i, p.type(), p.name(), context.getLocalVariableValue(p.definition())));
			}
			return InterpretedTuple.newInstance(entries.stream());
		}
	}
	
	/**
	 * A string literal.
	 */
	public static class StringDeclaration extends AbstractElement<StringDeclaration> implements Expression {
		public @Nullable String value;
		
		@Override
		public String toString() {
			assert value != null;
			return "'" + value.replaceAll("'", "\\'") + "'";
		}
		
		@Override
		protected StringDeclaration parse() throws ParseException {
			final StringToken t = next(StringToken.class);
			if (t != null)
				value = t.value;
			return this;
		}
		
		@Override
		public InterpretedTypeUse interpretedType() {
			return getInterpreter().getTypeUse("lang", "String");
		}
		
		@SuppressWarnings("null")
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			return getInterpreter().stringConstant(value);
		}
	}
	
	/**
	 * A first-order logic quantifier for contracts, i.e. 'for all' or 'there exists'.
	 */
	public static class Quantifier extends AbstractElement<Quantifier> implements Expression, HasVariables {
		boolean forall;
		public final List<QuantifierVars> vars = new ArrayList<>();
		public @Nullable Expression condition;
		public @Nullable Expression expression;
		
		@Override
		public String toString() {
			return (forall ? "forall" : "exists") + "(...)";
		}
		
		@Override
		protected Quantifier parse() throws ParseException {
			forall = oneOf("forall", "exists").equals("forall");
			oneGroup('(', () -> {
				until(() -> {
					do {
						vars.add(one(QuantifierVars.class));
					} while (try_(';'));
					if (try_('|'))
						condition = Expressions.parse(this);
				}, ':', false);
				expression = Expressions.parse(this);
			}, ')');
			return this;
		}
		
		@SuppressWarnings("null")
		@Override
		public List<? extends InterpretedVariableRedefinition> allVariables() {
			return vars.stream().flatMap(vars -> vars.vars.stream()).map(v -> v.interpreted()).collect(Collectors.toList());
		}
		
		@Override
		public InterpretedTypeUse interpretedType() {
			return getInterpreter().getTypeUse("lang", "Boolean");
		}
		
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			throw new InterpreterException("not implemented");
		}
	}
	
	public static class QuantifierVars extends AbstractElement<QuantifierVars> {
		public @Nullable TypeUse type;
		public final List<QuantifierVar> vars = new ArrayList<>();
		
		@Override
		protected QuantifierVars parse() throws ParseException {
			type = TypeExpressions.parse(this, true, true);
			do {
				vars.add(one(QuantifierVar.class));
			} while (try_(','));
			return this;
		}
	}
	
	public static class QuantifierVar extends AbstractElement<QuantifierVar> implements FormalParameter {
		public @Nullable LowercaseWordToken name;
		
		@Override
		public @Nullable WordToken nameToken() {
			return name;
		}
		
		@Override
		protected QuantifierVar parse() throws ParseException {
			name = oneVariableIdentifierToken();
			return this;
		}
		
		@SuppressWarnings("null")
		@Override
		public InterpretedTypeUse interpretedType() {
			return ((QuantifierVars) parent).type.staticallyKnownType();
		}
		
		@Override
		public InterpretedParameterRedefinition interpreted() {
			throw new InterpreterException("not implemented");
		}
	}
	
	/**
	 * A tuple, like '[a, b: c]' or '[A, B]'. If all entries are types, this is also a type.
	 */
	public static class Tuple extends AbstractElement<Tuple> implements Expression {
		public List<TupleEntry> entries = new ArrayList<>();
		
		@SuppressWarnings("null")
		@Override
		public InterpretedTypeUse interpretedType() {
			final List<InterpretedNativeTupleValueAndEntry> entries = new ArrayList<>();
			for (int i = 0; i < this.entries.size(); i++) {
				entries.add(this.entries.get(i).nativeType(i));
			}
			return new InterpretedTypeTuple(entries);
		}
		
		@Override
		protected Tuple parse() throws ParseException {
			oneGroup('[', () -> {
				do {
					entries.add(one(new TupleEntry(this instanceof TypeTuple)));
				} while (try_(','));
			}, ']');
			return this;
		}
		
		@SuppressWarnings("null")
		@Override
		public @NonNull InterpretedTuple interpret(final InterpreterContext context) {
			final List<InterpretedNativeTupleValueAndEntry> entries = new ArrayList<>();
			for (int i = 0; i < this.entries.size(); i++) {
				entries.add(this.entries.get(i).interpret(context, i));
			}
			return InterpretedTuple.newInstance(entries.stream());
		}
	}
	
	/**
	 * A subclass of Tuple that implements TypeExpression, and entries are only parsed as types.
	 */
	public static class TypeTuple extends Tuple implements TypeExpression {
		@Override
		public InterpretedTypeTuple staticallyKnownType() {
			final List<InterpretedNativeTupleValueAndEntry> entries = new ArrayList<>();
			for (int i = 0; i < this.entries.size(); i++) {
				entries.add(this.entries.get(i).nativeType(i));
			}
			return new InterpretedTypeTuple(entries.stream().collect(Collectors.toList()));
		}
		
		@Override
		public @NonNull InterpretedTypeTuple interpret(@NonNull final InterpreterContext context) {
			return (InterpretedTypeTuple) super.interpret(context);
		}
	}
	
	/**
	 * An entry of a tuple, with an optional name.
	 */
	public static class TupleEntry extends AbstractElement<TupleEntry> {
		public @Nullable WordToken name;
		public @Nullable Expression value;
		
		private final boolean onlyTypes;
		
		public TupleEntry(final boolean onlyTypes) {
			this.onlyTypes = onlyTypes;
		}
		
		@Override
		protected TupleEntry parse() throws ParseException {
			if (peekNext() instanceof WordToken && peekNext(':', 1, true)) {
				name = oneIdentifierToken();
				next(); // skip ':'
			}
			value = onlyTypes ? TypeExpressions.parse(this, true, true) : Expressions.parse(this);
			return this;
		}
		
		@SuppressWarnings("null")
		public @Nullable String name() {
			return name != null ? name.word : null;
		}
		
		@SuppressWarnings("null")
		public InterpretedNativeTupleValueAndEntry interpret(final InterpreterContext context, final int index) {
			return new InterpretedNativeTupleValueAndEntry(index, value.interpretedType(), name.word, value.interpret(context));
		}
		
		@SuppressWarnings("null")
		public InterpretedNativeTupleValueAndEntry nativeType(final int index) {
			final InterpretedTypeUse valueType = value.interpretedType();
			return new InterpretedNativeTupleValueAndEntry(index, valueType.nativeClass(), name.word, valueType);
		}
	}
	
	/**
	 * A method call with arguments. Used by {@link Argument} to find its linked parameter.
	 */
	public static interface MethodCall extends Element {
		public @Nullable InterpretedAttributeRedefinition attribute();
	}
	
	/**
	 * A variable of unqualified attribute. Since both are just a lowercase word, these cases cannot be distinguished before linking.
	 * Also handles unqualified attribute calls.
	 */
	public static class VariableOrUnqualifiedAttributeUse extends AbstractElement<VariableOrUnqualifiedAttributeUse> implements Expression {
		public Link<InterpretedVariableOrAttributeRedefinition> varOrAttribute = new Link<InterpretedVariableOrAttributeRedefinition>(this) {
			@SuppressWarnings("null")
			@Override
			protected @Nullable InterpretedVariableOrAttributeRedefinition tryLink(final String name) {
				for (Element p = parent(); p != null; p = p.parent()) {
					if (p instanceof Block) {
						for (final VariableDeclarations vars : ((Block) p).getDirectChildrenOfType(VariableDeclarations.class)) {
							for (final VariableDeclarationsVariable var : vars.variables) {
								if (var.nameToken != null && name.equals(var.nameToken.word))
									return var.interpreted();
							}
						}
					}
					if (p instanceof HasVariables) {
						final InterpretedVariableRedefinition var = ((HasVariables) p).getVariableByName(name);
						if (var != null)
							return var;
					}
					if (p instanceof TypeDeclaration) {
						final InterpretedAttributeRedefinition attribute = ((TypeDeclaration) p).interpreted().getAttributeByName(name);
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
		public List<Argument> arguments = new ArrayList<>();
		
		@Override
		protected VariableOrUnqualifiedAttributeUse parse() throws ParseException {
			varOrAttribute.setName(oneVariableIdentifierToken());
			tryGroup('(', () -> {
				do {
					arguments.add(one(Argument.class));
				} while (try_(','));
			}, ')');
			return this;
		}
		
		@SuppressWarnings("null")
		@Override
		public InterpretedTypeUse interpretedType() {
			return varOrAttribute.get().mainResultType();
		}
		
		@SuppressWarnings("null")
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			final InterpretedVariableOrAttributeRedefinition varOrAttr = varOrAttribute.get();
			if (varOrAttr instanceof InterpretedVariableRedefinition) {
				return context.getLocalVariableValue(((InterpretedVariableRedefinition) varOrAttr).definition());
			} else {
				return ((InterpretedAttributeRedefinition) varOrAttr).interpretDispatched(context.getThisObject(), Collections.EMPTY_MAP, false);
			}
		}
	}
	
	/**
	 * An argument to a function call.
	 */
	public static class Argument extends AbstractElement<Argument> {
		public boolean isDots;
		public Link<InterpretedParameterRedefinition> parameter = new Link<InterpretedParameterRedefinition>(this) {
			@Override
			protected @Nullable InterpretedParameterRedefinition tryLink(final String name) {
				// find parameter by name if set, otherwise use position // TODO what to sent the link to in that case? just nothing?
				final MethodCall parent = (MethodCall) parent();
				assert parent != null;
				final InterpretedAttributeRedefinition method = parent.attribute();
				if (method == null)
					return null;
				for (final InterpretedParameterRedefinition p : method.parameters()) {
					if (name.equals(p.name()))
						return p;
				}
				return null;
			}
		};
		public @Nullable Expression value;
		
		@Override
		protected Argument parse() throws ParseException {
			isDots = try_("...");
			if (!isDots) {
				if (peekNext() instanceof LowercaseWordToken && peekNext(':', 1, true)) {
					parameter.setName(oneVariableIdentifierToken());
					next(); // skip ':'
				}
				value = Expressions.parse(this);
			}
			return this;
		}
		
		@SuppressWarnings("null")
		public static Map<InterpretedParameterDefinition, InterpretedObject> makeInterpretedArgumentMap(final InterpretedAttributeDefinition method, final List<Argument> args, final InterpreterContext context) {
			final List<InterpretedParameterRedefinition> parameters = method.parameters();
			final Map<InterpretedParameterDefinition, InterpretedObject> r = new HashMap<>();
			for (int i = 0; i < args.size(); i++) {
				final Argument arg = args.get(i);
				final InterpretedObject value = arg.value.interpret(context);
				if (arg.parameter.getNameToken() == null)
					r.put(parameters.get(i).definition(), value);
				else
					r.put(arg.parameter.get().definition(), value);
			}
			return r;
		}
	}
	
	/**
	 * A meta access without a target (like '~a'), i.e. targets 'this'.
	 */
	public static class UnqualifiedMetaAccess extends AbstractElement<UnqualifiedMetaAccess> implements Expression {
		public Link<InterpretedAttributeRedefinition> attribute = new Link<InterpretedAttributeRedefinition>(this) {
			@Override
			protected @Nullable InterpretedAttributeRedefinition tryLink(@NonNull final String name) {
				final TypeDeclaration mc = getParentOfType(TypeDeclaration.class);
				if (mc == null)
					return null;
				return mc.interpreted().getAttributeByName(name);
			}
		};
		
		@Override
		protected UnqualifiedMetaAccess parse() throws ParseException {
			one('~');
			attribute.setName(oneVariableIdentifierToken());
			return this;
		}
		
		@SuppressWarnings("null")
		@Override
		public InterpretedTypeUse interpretedType() {
			final InterpretedAttributeRedefinition attr = attribute.get();
			final boolean isPure = !attr.isModifying();
			return getInterpreter().getTypeUse("lang", isPure ? "Function" : "Procedure");
		}
		
		@Override
		@SuppressWarnings("null")
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			throw new InterpreterException("not implemented");
//			return context.getThisObject().getAttributeClosure(attribute.get().definition());
		}
	}
	
	/**
	 * The keyword 'recurse' which calls the current method, with optionally new arguments (and all unspecified arguments left the same).
	 */
	public static class Recurse extends AbstractElement<Recurse> implements Expression, MethodCall {
		public List<Argument> arguments = new ArrayList<>();
		
		@Override
		public @Nullable InterpretedAttributeRedefinition attribute() {
			final FormalAttribute attr = getParentOfType(FormalAttribute.class);
			return attr == null ? null : attr.interpreted();
		}
		
		@SuppressWarnings("null")
		@Override
		public InterpretedTypeUse interpretedType() {
			return attribute().mainResultType();
		}
		
		@Override
		protected Recurse parse() throws ParseException {
			one("recurse");
			oneGroup('(', () -> {
				do {
					arguments.add(one(Argument.class));
				} while (try_(','));
			}, ')');
			return this;
		}
		
		@SuppressWarnings("null")
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			final InterpretedAttributeRedefinition attribute = attribute();
			// this is dispatched as 'recurse' may be in a result's default value
			// (A 'recurse' in a body could just call that same body immediately without a dispatch, since if that body is executed it has to be the last one)
			return attribute.interpretDispatched(context.getThisObject(), Argument.makeInterpretedArgumentMap(attribute.definition(), arguments, context), false);
		}
	}
	
	/**
	 * The keyword/"function" 'old' which evaluates an expression as if it were evaluated at the beginning of its parent function.
	 */
	public static class Old extends AbstractElement<Old> implements Expression {
		public @Nullable Expression expression;
		
		@Override
		protected Old parse() throws ParseException {
			one("old");
			oneGroup('(', () -> {
				expression = Expressions.parse(this);
			}, ')');
			return this;
		}
		
		@SuppressWarnings("null")
		@Override
		public InterpretedTypeUse interpretedType() {
			return expression.interpretedType();
		}
		
		@Override
		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
			throw new InterpreterException("not implemented");
		}
	}
	
	// ================================ Types ================================
	
	public static class TypeExpressions {
		
		public static TypeExpression parse(final AbstractElement<?> parent, final boolean allowOps, final boolean allowTuple) throws ParseException {
			return parse(parent, allowOps, allowTuple, allowOps);
		}
		
		static TypeExpression parse(final AbstractElement<?> parent, final boolean allowOps, final boolean allowTuple, final boolean allowDotGeneric) throws ParseException {
			assert !(!allowDotGeneric && allowOps) : "generics are automatically allowed when operators are";
			if (allowDotGeneric && !allowOps) { // if allowing ops, ops are done first
				final TypeExpression target = TypeExpressions.parse(parent, false, false, false); // TODO do tuples have generics? or should this just be allowed to then produce a better error message?
				if (parent.peekNext('.'))
					return parent.one(new GenericTypeAccess(target));
				else
					return target;
			}
			if (allowTuple && parent.peekNext('[')) {
//				if (!allowTuple)
//					throw new ParseException("Expected a type that is not a tuple", parent.in.getOffset(), parent.in.getOffset() + 1);
				// not null, since it starts with '['
				return (TypeTuple) parent.one(new TypeTuple());
			}
			if (!allowOps) { // i.e. only a single type (possibly with modifiers and generics)
				if (parent.peekNext("Self"))
					return parent.one(Self.class);
				final TypeExpression e = parent.one(ModifierTypeUseElement.class);
				if (parent.peekNext('<'))
					return GenericTypeUseElement.withModifiers(e, parent);
				return e;
			}
			return parent.one(TypeUseWithOperators.class);
			
		}
	}
	
	/**
	 * The keyword 'Self', representing the class of the current object (i.e. equal to this.class, but can be used in more contexts like interfaces and generics)
	 */
	public static class Self extends AbstractElement<Self> implements TypeExpression {
		Link<TypeDeclaration> link = new Link<TypeDeclaration>(this) {
			@Override
			protected @Nullable TypeDeclaration tryLink(final String name) {
				return Self.this.getParentOfType(TypeDeclaration.class);
			}
		};
		
		@Override
		public String toString() {
			return "Self";
		}
		
		@Override
		protected Self parse() throws ParseException {
			one("Self");
			return this;
		}
		
		@Override
		public @NonNull InterpretedTypeObject interpret(final InterpreterContext context) {
			return context.getThisObject().nativeClass();
		}
		
		@SuppressWarnings("null")
		@Override
		public InterpretedTypeUse staticallyKnownType() {
			return new InterpretedSimpleTypeUse(getParentOfType(TypeDeclaration.class).interpreted());
		}
	}
	
	public final static class OperatorLink extends Link<InterpretedAttributeRedefinition> {
		private final boolean isBinary;
		
		public OperatorLink(final Element parent, @Nullable final WordToken symbols, final boolean isBinary) {
			super(parent, symbols);
			this.isBinary = isBinary;
		}
		
		private final static Map<String, String[]> binaryOperators = new HashMap<String, String[]>() {
			private static final long serialVersionUID = 1L;
			
			{
				put("+", new String[] {"Addable", "add"});
				put("-", new String[] {"Subtractable", "subtract"});
				put("*", new String[] {"Multipliable", "multiply"});
				put("/", new String[] {"Divisible", "divide"});
				put("^", new String[] {"Exponentiable", "exponentiate"});
				put("|", new String[] {"Orable", "or"});
				put("&", new String[] {"Andable", "and"});
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
		
		private final static Map<String, String[]> unaryPrefixOperators = new HashMap<String, String[]>() {
			private static final long serialVersionUID = 1L;
			
			{
				put("!", new String[] {"Negatable", "negated"});
				put("-", new String[] {"Subtractable", "negated"});
			}
		};
		
		@SuppressWarnings({"null", "unused"})
		@Override
		protected @Nullable InterpretedAttributeRedefinition tryLink(@NonNull final String name) {
			final String[] s = (isBinary ? binaryOperators : unaryPrefixOperators).get(name);
			if (s == null)
				return null;
			return BrokkrFile.getInterpreter(parentElement).getType("lang", s[0]).getAttributeByName(s[1]);
		}
	}
	
	/**
	 * An expression with types and operators, currently only & and |.
	 */
	public static class TypeUseWithOperators extends AbstractElement<TypeExpression> implements TypeExpression {
		public List<TypeExpression> types = new ArrayList<>();
		public List<Link<InterpretedAttributeRedefinition>> operators = new ArrayList<>();
		
		@Override
		public String toString() {
			String r = "" + types.get(0);
			for (int i = 0; i < operators.size(); i++) {
				r += " " + operators.get(i) + " " + types.get(i + 1);
			}
			return r;
		}
		
		@Override
		protected TypeExpression parse() throws ParseException {
			final TypeExpression first = TypeExpressions.parse(this, false, true, true);
			types.add(first);
			WordToken op;
			while ((op = try2("&", "|")) != null) {
				operators.add(new OperatorLink(this, op, true));
				types.add(TypeExpressions.parse(this, false, true, true));
			}
			if (types.size() == 1)
				return first;
			else
				return this;
		}
		
		// TODO use proper operator order
		
		@Override
		public @NonNull InterpretedTypeUse staticallyKnownType() {
			InterpretedTypeUse o = types.get(0).staticallyKnownType();
			for (int i = 0; i < operators.size(); i++) {
				final InterpretedTypeUse o2 = types.get(i + 1).staticallyKnownType();
				if ("&".equals(operators.get(i).getName()))
					o = new InterpretedAndTypeUse(o, o2);
				else
					o = new InterpretedOrTypeUse(o, o2);
			}
			return o;
		}
		
		@Override
		public @NonNull InterpretedTypeObject interpret(final InterpreterContext context) {
			InterpretedTypeUse o = types.get(0).interpret(context);
			for (int i = 0; i < operators.size(); i++) {
				final InterpretedTypeUse o2 = types.get(i + 1).interpret(context);
				if ("&".equals(operators.get(i).getName()))
					o = new InterpretedAndTypeUse(o, o2);
				else
					o = new InterpretedOrTypeUse(o, o2);
			}
			return o;
		}
	}
	
	/**
	 * A type use that is a single word, which can be either a normal type or a generic parameter.
	 */
	public static class SimpleTypeUseElement extends AbstractElement<SimpleTypeUseElement> implements TypeExpression {
		
		public final Link<InterpretedNativeTypeDefinition> typeDeclaration = new Link<InterpretedNativeTypeDefinition>(this) {
			@Override
			protected @Nullable InterpretedNativeTypeDefinition tryLink(final String name) {
				// A type is either defined in the same file, or imported.
				// From types in the same file only siblings and siblings of parents are valid candidates.
				Element start = parent();
				if (start instanceof TypeDeclaration) { // i.e. this type is in the declaration of a type as a parent or such // TODO improve this (documentation + implementation - currently depends on no elements between the type decl and this type)
					if (name.equals(((TypeDeclaration) start).name()))
						return ((TypeDeclaration) start).interpreted();
					start = start.parent(); // prevent looking at members of the implementing/extending type (also prevents an infinite loop)
				}
				for (Element p = start; p != null; p = p.parent()) {
					if (p instanceof TypeDeclaration) {
						((TypeDeclaration) p).getDirectChildrenOfType(TypeDeclaration.class);
						// FIXME
					} else if (p instanceof BrokkrFile) {
						final Module m = ((BrokkrFile) p).module;
						return m == null ? null : m.getType(name);
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
		protected SimpleTypeUseElement parse() throws ParseException {
			typeDeclaration.setName(oneTypeIdentifierToken());
			return this;
		}
		
		@SuppressWarnings("null")
		@Override
		public @Nullable InterpretedTypeObject interpret(final InterpreterContext context) {
			return typeDeclaration.get(); // TODO?
		}
		
		@SuppressWarnings("null")
		@Override
		public InterpretedTypeUse staticallyKnownType() {
			return new InterpretedSimpleTypeUse(typeDeclaration.get());
		}
		
		@SuppressWarnings("null")
		public InterpretedNativeTypeDefinition definition() {
			return typeDeclaration.get();
		}
	}
	
	/**
	 * A type use with modifiers, e.g. 'mod a b C', equal to 'mod (a X & b Y & C)' for the appropriate X and Y types.
	 */
	public static class ModifierTypeUseElement extends AbstractElement<TypeExpression> implements TypeExpression {
		
		public final List<ModifierTypeUseModifierElement> modifiers = new ArrayList<>();
		/**
		 * Either a {@link SimpleTypeUseElement} or a {@link GenericTypeUseElement}
		 */
		public @Nullable TypeExpression type;
		
		@Override
		public String toString() {
			return (modifiers.size() == 0 ? "" : StringUtils.join(modifiers, " ") + " ") + type;
		}
		
		@Override
		protected TypeExpression parse() throws ParseException {
			while (peekNext() instanceof LowercaseWordToken) {
				modifiers.add(one(ModifierTypeUseModifierElement.class));
			}
			
			// TODO is 'modifier Self' possible?
			type = one(SimpleTypeUseElement.class);
			
			if (modifiers.isEmpty()) {
				assert type != null;
				return type;
			}
			return this;
		}
		
		@SuppressWarnings("null")
		@Override
		public @Nullable InterpretedTypeObject interpret(final InterpreterContext context) {
			InterpretedTypeObject result = type.interpret(context);
			for (final ModifierTypeUseModifierElement mod : modifiers) {
				final InterpretedTypeUse m = new InterpretedSimpleTypeUse(mod.modifier.get());
				if (m != null)
					result = new InterpretedAndTypeUse(result, m);
			}
			return result;
		}
		
		@SuppressWarnings("null")
		@Override
		public InterpretedTypeUse staticallyKnownType() {
			InterpretedTypeUse r = type.staticallyKnownType();
			for (int i = modifiers.size() - 1; i >= 0; i--) {
				r = new InterpretedAndTypeUse(modifiers.get(i).staticallyKnownType(), r);
			}
			return r;
		}
	}
	
	/**
	 * a type use modifier, either a modifiability, exclusivity, etc. or a user-defined modifier.
	 */
	public static class ModifierTypeUseModifierElement extends AbstractElement<ModifierTypeUseModifierElement> implements TypeUse {
		public @Nullable Modifiability modifiability;
		public @Nullable Exclusivity exclusivity;
		public final Link<InterpretedNativeTypeDefinition> modifier = new Link<InterpretedNativeTypeDefinition>(this) {
			@SuppressWarnings("null")
			@Override
			protected @Nullable InterpretedNativeTypeDefinition tryLink(final String name) {
				final ModifierTypeUseElement base = getParentOfType(ModifierTypeUseElement.class);
				if (base == null)
					return null;
				final InterpretedTypeUse baseType = base.type.staticallyKnownType();
				if (baseType == null)
					return null;
				final BrokkrFile file = getParentOfType(BrokkrFile.class);
				if (file == null || file.module == null)
					return null;
				return file.module.getModifierType(name, baseType);
			}
		};
		public @Nullable Expression from;
		
		@Override
		public String toString() {
			return (modifiability != null ? modifiability : modifier.getName()) + (from == null ? "" : "@" + (from instanceof VariableOrUnqualifiedAttributeUse || from instanceof This ? from : "(" + from + ")"));
		}
		
		@Override
		protected ModifierTypeUseModifierElement parse() throws ParseException {
			modifiability = Modifiability.parse(this);
			if (modifiability == null) {
				exclusivity = Exclusivity.parse(this);
				if (exclusivity == null)
					modifier.setName(oneVariableIdentifierToken());
			}
			if (try_('@')) {
				if (!tryGroup('(', () -> {
					from = Expressions.parse(this);
				}, ')')) {
					if (peekNext("this"))
						from = one(This.class);
					else
						from = one(VariableOrUnqualifiedAttributeUse.class);
				}
			}
			return this;
		}
		
		@SuppressWarnings("null")
		@Override
		public InterpretedTypeUse staticallyKnownType() {
			return new InterpretedSimpleTypeUse(modifier.get());
		}
		
		@SuppressWarnings("null")
		@Override
		public @NonNull InterpretedTypeUse interpret(final InterpreterContext context) {
			return staticallyKnownType();
		}
	}
	
	/**
	 * A type use with generic arguments, e.g. 'A<B, C: D>'
	 */
	public static class GenericTypeUseElement extends AbstractElement<GenericTypeUseElement> implements TypeExpression {
		
		public final SimpleTypeUseElement baseType;
		
		public GenericTypeUseElement(final SimpleTypeUseElement baseType) {
			this.baseType = baseType;
			baseType.setParent(this);
		}
		
		@SuppressWarnings("null")
		public static TypeExpression withModifiers(final TypeExpression withModifiers, final AbstractElement<?> parent) throws ParseException {
			if (withModifiers instanceof SimpleTypeUseElement)
				return parent.one(new GenericTypeUseElement((SimpleTypeUseElement) withModifiers));
			final GenericTypeUseElement generic = parent.one(new GenericTypeUseElement((SimpleTypeUseElement) ((ModifierTypeUseElement) withModifiers).type));
			((ModifierTypeUseElement) withModifiers).type = generic;
			generic.setParent(withModifiers);
			return withModifiers;
		}
		
		public final List<GenericArgument> genericArguments = new ArrayList<>();
		
		@Override
		public String toString() {
			return baseType + "<" + StringUtils.join(genericArguments, ",") + ">";
		}
		
		@Override
		protected GenericTypeUseElement parse() throws ParseException {
			oneGroup('<', () -> {
				do {
					genericArguments.add(one(GenericArgument.class));
				} while (try_(','));
			}, '>');
			return this;
		}
		
		@SuppressWarnings("null")
		@Override
		public InterpretedTypeUse staticallyKnownType() {
			return new InterpretedSimpleTypeUse(baseType.definition(), genericArguments.stream().collect(Collectors.toMap(a -> a.parameter.get().definition(), a -> a.value.staticallyKnownType())));
		}
		
		@Override
		public @NonNull InterpretedTypeObject interpret(final InterpreterContext context) {
			return new InterpretedSimpleTypeUse(baseType.definition(), genericArguments.stream().collect(Collectors.toMap(a -> a.parameter.get().definition(), a -> a.value.interpret(context))));
		}
	}
	
	/**
	 * A generic argument to a type use.
	 */
	public static class GenericArgument extends AbstractElement<GenericArgument> {
		public Link<InterpretedGenericTypeRedefinition> parameter = new Link<InterpretedGenericTypeRedefinition>(this) {
			@Override
			protected @Nullable InterpretedGenericTypeRedefinition tryLink(final String name) {
				assert parent != null;
				final TypeUse type = ((GenericTypeUseElement) parent).baseType;
				if (type != null)
					return type.staticallyKnownType().getGenericTypeByName(name);
				return null;
			}
		};
		public boolean wildcard;
		public @Nullable TypeExpression value;
		
		@Override
		public String toString() {
			return (parameter.getName() == null ? "" : parameter.getName() + ": ") + value;//(wildcard ? "?" + (extends_ == null ? "" : " extends " + extends_) + (super_ == null ? "" : " super " + super_) : value);
		}
		
		@Override
		protected GenericArgument parse() throws ParseException {
			if (peekNext() instanceof WordToken && peekNext(':', 1, true)) {
				parameter.setName(oneIdentifierToken());
				next(); // skip ':'
			} else {
				parameter.setName(null); // TODO what to link?
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
			value = TypeExpressions.parse(this, true, true, true); // Expressions.parse(this, false);
			return this;
		}
	}
	
	/**
	 * Access to a generic type of a type, e.g. 'A.B'.
	 * TODO is this the same as a static attribute access?
	 */
	public static class GenericTypeAccess extends AbstractElement<GenericTypeAccess> implements TypeExpression {
		public final TypeUse target;
		private final Link<InterpretedGenericTypeRedefinition> genericType = new Link<InterpretedGenericTypeRedefinition>(this) {
			@Override
			protected @Nullable InterpretedGenericTypeRedefinition tryLink(final String name) {
				return target.staticallyKnownType().getGenericTypeByName(name);
			}
		};
		
		public GenericTypeAccess(final TypeUse target) {
			this.target = target;
			target.setParent(this);
		}
		
		@Override
		public String toString() {
			return target + "." + genericType.getName();
		}
		
		@Override
		protected GenericTypeAccess parse() throws ParseException {
			one('.');
			genericType.setName(oneTypeIdentifierToken());
			GenericTypeAccess result = this;
			while (try_('.')) {
				result = new GenericTypeAccess(this);
				result.genericType.setName(oneTypeIdentifierToken());
			}
			return result;
		}
		
		@Override
		public InterpretedTypeUse interpretedType() {
			return staticallyKnownType();
		}
		
		@SuppressWarnings("null")
		@Override
		public InterpretedTypeUse staticallyKnownType() {
			return genericType.get();
		}
		
		@Override
		public @NonNull InterpretedTypeObject interpret(final InterpreterContext context) {
			return target.interpret(context).nativeClass().getGenericType(genericType.get().definition());
		}
	}
	
}
