package ch.njol.tome.ast;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTAttribute;
import ch.njol.tome.ast.ASTInterfaces.ASTElementWithVariables;
import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.ASTInterfaces.ASTGenericParameter;
import ch.njol.tome.ast.ASTInterfaces.ASTLocalVariable;
import ch.njol.tome.ast.ASTInterfaces.ASTMember;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeExpression;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeUse;
import ch.njol.tome.ast.ASTInterfaces.ASTVariable;
import ch.njol.tome.ast.ASTInterfaces.TypedASTElement;
import ch.njol.tome.ast.ASTMembers.ASTAttributeDeclaration;
import ch.njol.tome.ast.ASTStatements.ASTStatement;
import ch.njol.tome.ast.ASTStatements.ASTVariableDeclarations;
import ch.njol.tome.ast.ASTStatements.ASTVariableDeclarationsVariable;
import ch.njol.tome.ast.ASTTopLevelElements.ASTSourceFile;
import ch.njol.tome.common.Borrowing;
import ch.njol.tome.common.Cache;
import ch.njol.tome.common.ContentAssistProposal;
import ch.njol.tome.common.DebugString;
import ch.njol.tome.common.Exclusiveness;
import ch.njol.tome.common.Kleenean;
import ch.njol.tome.common.Modifiability;
import ch.njol.tome.common.Optionality;
import ch.njol.tome.common.StringMatcher;
import ch.njol.tome.compiler.Token;
import ch.njol.tome.compiler.Token.CommentToken;
import ch.njol.tome.compiler.Token.LowercaseWordToken;
import ch.njol.tome.compiler.Token.NumberToken;
import ch.njol.tome.compiler.Token.StringToken;
import ch.njol.tome.compiler.Token.SymbolToken;
import ch.njol.tome.compiler.Token.UppercaseWordToken;
import ch.njol.tome.compiler.Token.WhitespaceToken;
import ch.njol.tome.compiler.Token.WordOrSymbols;
import ch.njol.tome.compiler.Token.WordToken;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.IRError;
import ch.njol.tome.ir.IRGenericArgument;
import ch.njol.tome.ir.IRUnknownGenericArgument;
import ch.njol.tome.ir.IRValueGenericArgument;
import ch.njol.tome.ir.definitions.IRAttributeDefinition;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.definitions.IRBrokkrClassDefinition;
import ch.njol.tome.ir.definitions.IRBrokkrLocalVariable;
import ch.njol.tome.ir.definitions.IRMemberRedefinition;
import ch.njol.tome.ir.definitions.IRParameterDefinition;
import ch.njol.tome.ir.definitions.IRParameterRedefinition;
import ch.njol.tome.ir.definitions.IRQuantifierVariable;
import ch.njol.tome.ir.definitions.IRTypeDefinition;
import ch.njol.tome.ir.definitions.IRVariableDefinition;
import ch.njol.tome.ir.definitions.IRVariableOrAttributeRedefinition;
import ch.njol.tome.ir.definitions.IRVariableRedefinition;
import ch.njol.tome.ir.expressions.IRAnonymousObjectCreation;
import ch.njol.tome.ir.expressions.IRArgumentsKeyword;
import ch.njol.tome.ir.expressions.IRAttributeAccess;
import ch.njol.tome.ir.expressions.IRAttributeAssignment;
import ch.njol.tome.ir.expressions.IRBlock;
import ch.njol.tome.ir.expressions.IRClosure;
import ch.njol.tome.ir.expressions.IRExpression;
import ch.njol.tome.ir.expressions.IRIf;
import ch.njol.tome.ir.expressions.IRKleeneanConstant;
import ch.njol.tome.ir.expressions.IRNull;
import ch.njol.tome.ir.expressions.IRNumberConstant;
import ch.njol.tome.ir.expressions.IROld;
import ch.njol.tome.ir.expressions.IRString;
import ch.njol.tome.ir.expressions.IRThis;
import ch.njol.tome.ir.expressions.IRUnknownExpression;
import ch.njol.tome.ir.expressions.IRVariableAssignment;
import ch.njol.tome.ir.expressions.IRVariableExpression;
import ch.njol.tome.ir.nativetypes.IRTuple;
import ch.njol.tome.ir.nativetypes.IRTuple.IRTupleBuilderEntry;
import ch.njol.tome.ir.nativetypes.IRTuple.IRTypeTuple;
import ch.njol.tome.ir.nativetypes.IRTuple.IRTypeTupleBuilder;
import ch.njol.tome.ir.uses.IRAndTypeUse;
import ch.njol.tome.ir.uses.IRAttributeUse;
import ch.njol.tome.ir.uses.IRGenericTypeAccess;
import ch.njol.tome.ir.uses.IROrTypeUse;
import ch.njol.tome.ir.uses.IRSelfTypeUse;
import ch.njol.tome.ir.uses.IRSimpleTypeUse;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.ir.uses.IRUnknownTypeUse;
import ch.njol.tome.moduleast.ASTModule;
import ch.njol.tome.parser.AttachedElementParser;
import ch.njol.tome.parser.DetachedElementParser;
import ch.njol.tome.parser.ElementParser;
import ch.njol.tome.parser.ElementPlaceholder;
import ch.njol.tome.parser.Parser;
import ch.njol.tome.parser.SingleElementParser;
import ch.njol.tome.parser.UnknownParser;
import ch.njol.util.CollectionUtils;
import ch.njol.util.PartialComparator;
import ch.njol.util.PartialRelation;
import ch.njol.util.StringUtils;

public class ASTExpressions {
	
	public static ASTExpression parse(final Parser parent) {
		return parse(parent, true);
	}
	
	public static ASTExpression parse(final Parser parent, final boolean allowComparisons) {
		
		if (parent.peekNext('{'))
			return ASTBlock.parse(parent);
		if (parent.peekNext("create"))
			return ASTAnonymousObject.parse(parent);
		
		// TODO fix / change "[params] ->" syntax (or make content assist smart enough to work with both this syntax and tuples)
		if (parent.peekNext("var") && parent.peekNext(1, true) instanceof LowercaseWordToken && parent.peekNext("->", 2, true))
			return ASTLambda.parse(parent, null);
		if (parent.peekNext() instanceof LowercaseWordToken && parent.peekNext("->", 1, true))
			return ASTLambda.parse(parent, ASTLambdaParameter.parse(parent, true));
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
						return ASTLambda.parse(parent, null);
					} else {
						break;
					}
				}
				i++;
			}
		}
		
		UnknownParser up = parent.startUnknownChild();
		final ASTExpression expr = ASTOperatorExpression.parse(up, allowComparisons);
		SymbolToken sym;
		WordOrSymbols assignmentOp = parent.try2("=", "+=", "-=", "*=", "/=", "&=", "|=");
		if (assignmentOp != null) {
			// TODO directly determine if a variable is local or an unqualified attribute?
			if (expr instanceof ASTVariableOrUnqualifiedAttributeUse) {
				final ASTVariableOrUnqualifiedAttributeUse varOrAttribute = (ASTVariableOrUnqualifiedAttributeUse) expr;
				if (!varOrAttribute.arguments.isEmpty()) {
					parent.errorFatal("Left-hand side of an assignment must be a variable or a field", expr.absoluteRegionStart(), expr.regionLength());
					return expr;
				}
				return ASTLocalVariableOrUnqualifiedAttributeAssignment.parse(up, varOrAttribute, assignmentOp);
			} else if (expr instanceof ASTAccessExpression && !((ASTAccessExpression) expr).meta) {// && ((ASTAccessExpression) expr).access instanceof ASTDirectAttributeAccess) {
				final ASTAccessExpression e = (ASTAccessExpression) expr;
				final ASTDirectAttributeAccess da = e.access;
				assert da != null;
				if (e.meta || e.nullSafe /*|| da.negated*/ || da.allResults || !da.arguments.isEmpty()) {
					parent.errorFatal("Left-hand side of an assignment must be a variable or a field", expr.absoluteRegionStart(), expr.regionLength());
					return expr;
				}
				e.removeFromParent();
				da.removeFromParent();
				return ASTAttributeAssignment.parse(parent, e.target, da.attribute, assignmentOp);
			} else {
				if (expr.regionLength() > 0)
					parent.errorFatal("Left-hand side of an assignment must be a variable or a field", expr.absoluteRegionStart(), expr.regionLength());
				return expr;
			}
		} else if ((sym = parent.try2('?')) != null) { // no need to check for '?.' and '?~' as those are parsed before in OperatorExpression
			return ASTTernaryIf.parse(parent, expr, sym);
		} else if ((sym = parent.try2('#')) != null) {
			return ASTErrorHandlingExpression.parse(parent, expr, sym);
		} else if (expr instanceof ASTTypeUse && parent.peekNext() instanceof LowercaseWordToken && parent.peekNext("->", 1, true)) {
			return ASTLambda.parse(parent, ASTLambdaParameter.parse(parent, (ASTTypeUse) expr));
		} else {
			return expr;
		}
	}
	
	public static class ASTBlock extends AbstractASTElement implements ASTExpression {
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
			return new IRUnknownTypeUse(getIRContext());
		}
		
		public ASTBlock() {}
		
		public ASTBlock(final @NonNull ASTStatement... statements) {
			this.statements.addAll(Arrays.asList(statements));
		}
		
		@Override
		public String toString() {
			return "{...}";
		}
		
		public static ASTBlock parse(final Parser parent) {
			final AttachedElementParser<ASTBlock> p = parent.startChild(new ASTBlock());
			p.oneRepeatingGroup('{', () -> {
				if (p.ast.statements.isEmpty()) {
					final ASTElement e = ASTStatement.parseWithExpression(p);
					if (e instanceof ASTExpression)
						p.ast.expression = (ASTExpression) e;
					else
						p.ast.statements.add((ASTStatement) e);
				} else {
					p.ast.statements.add(ASTStatement.parse(p));
					assert p.ast.expression == null;
				}
			}, '}');
			return p.ast;
		}
		
		@Override
		public IRExpression getIR() {
			if (expression != null)
				return expression.getIR(); // FIXME actually returns a block like [[ {return expression;} ]]
			return new IRBlock(getIRContext(), statements.stream().map(s -> s.getIR()));
		}
	}
	
	public static class ASTAnonymousObject extends AbstractASTElement implements ASTExpression {
		public @Nullable ASTAnonymousType type;
		
		@Override
		public int linkStart() {
			return type != null ? type.linkStart() : absoluteRegionStart();
		}
		
		@Override
		public int linkEnd() {
			return type != null ? type.linkEnd() : absoluteRegionEnd();
		}
		
		@Override
		public String toString() {
			return "create " + type;
		}
		
		public static ASTAnonymousObject parse(final Parser parent) {
			final AttachedElementParser<ASTAnonymousObject> p = parent.startChild(new ASTAnonymousObject());
			p.one("create");
			p.ast.type = ASTAnonymousType.parse(p);
			return p.ast;
		}
		
		@Override
		public IRTypeUse getIRType() {
			final ASTAnonymousType type = this.type;
			if (type == null)
				return new IRUnknownTypeUse(getIRContext());
			return new IRSimpleTypeUse(type.getIR());
		}
		
		@Override
		public IRExpression getIR() {
			if (type != null)
				return new IRAnonymousObjectCreation(type.getIR());
			else
				return new IRUnknownExpression("Syntax error. Proper syntax: [create SomeType { ... }]", this);
		}
	}
	
	public static class ASTAnonymousType extends AbstractASTElement implements ASTTypeDeclaration {
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
			return type != null ? type.absoluteRegionStart() : absoluteRegionStart();
		}
		
		@Override
		public int linkEnd() {
			return type != null ? type.absoluteRegionEnd() : absoluteRegionEnd();
		}
		
		@Override
		public List<? extends ASTMember> declaredMembers() {
			return members;
		}
		
		// always has a parent type
		@Override
		public @NonNull IRTypeUse parentTypes() {
			return type != null ? type.getIR() : new IRUnknownTypeUse(getIRContext());
		}
		
		@Override
		public List<? extends ASTGenericParameter> genericParameters() {
			return Collections.EMPTY_LIST;
		}
		
		@Override
		public String toString() {
			return type + " {...}";
		}
		
		public static ASTAnonymousType parse(final Parser parent) {
			final AttachedElementParser<ASTAnonymousType> p = parent.startChild(new ASTAnonymousType());
			p.ast.type = ASTTypeExpressions.parse(p, true, false);
			p.oneRepeatingGroup('{', () -> {
				p.ast.members.add(ASTMembers.parse(p));
			}, '}');
			return p.ast;
		}
		
		private final Cache<IRBrokkrClassDefinition> ir = new Cache<>(() -> new IRBrokkrClassDefinition(this));
		
		@Override
		public IRBrokkrClassDefinition getIR() {
			return ir.get();
		}
	}
	
	public static class ASTLambda extends AbstractASTElement implements ASTExpression, ASTElementWithVariables {
		public final List<ASTLambdaParameter> parameters = new ArrayList<>();
		public @Nullable ASTExpression code;
		
		public ASTLambda(final @Nullable ASTLambdaParameter param) {
			if (param != null) {
				parameters.add(param);
				addChild(param);
			}
		}
		
		@Override
		public String toString() {
			return parameters + " -> " + code;
		}
		
		@Override
		public @Nullable String hoverInfo(final Token token) {
			return null; // TODO return description of this function
		}
		
		public static ASTLambda parse(final Parser parent, @Nullable final ASTLambdaParameter param) {
			final AttachedElementParser<ASTLambda> p = parent.startChild(new ASTLambda(param));
			if (param == null) {
				if (!p.tryGroup('[', () -> {
					do {
						p.ast.parameters.add(ASTLambdaParameter.parse(p, true));
					} while (p.try_(','));
				}, ']')) {
					p.ast.parameters.add(ASTLambdaParameter.parse(p, true));
				}
			}
			p.one("->");
			p.ast.code = ASTExpressions.parse(p);
			return p.ast;
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
	}
	
	public static class ASTLambdaParameter extends AbstractASTElement implements ASTLocalVariable {
		public @Nullable ASTTypeUse type;
		public @Nullable LowercaseWordToken name;
		
		@Override
		public @Nullable WordToken nameToken() {
			return name;
		}
		
		public ASTLambdaParameter(final @Nullable ASTTypeUse type) {
			if (type != null) {
				this.type = type;
				addChild(type);
			}
		}
		
		@Override
		public String toString() {
			return (type != null ? type + " " : "") + name;
		}

		public static ASTLambdaParameter parse(final Parser parent, final ASTTypeUse type) {
			return parse(parent, type, false);
		}

		public static ASTLambdaParameter parse(final Parser parent, final boolean withType) {
			return parse(parent, null, withType);
		}

		public static ASTLambdaParameter parse(final Parser parent, @Nullable final ASTTypeUse type, final boolean withType) {
			final AttachedElementParser<ASTLambdaParameter> p = parent.startChild(new ASTLambdaParameter(type));
			if (type == null && withType && !p.try_("var"))
				p.ast.type = ASTTypeExpressions.parse(p, false, false);
			p.ast.name = p.oneVariableIdentifierToken();
			return p.ast;
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
	
	public static abstract class AbstractASTAssignment<T extends AbstractASTAssignment<T>> extends AbstractASTElement implements ASTExpression {
		public final WordOrSymbols assignmentOp;
		public final @Nullable ASTOperatorLink assignmentOpLink;
		public @Nullable ASTExpression value;
		
		protected AbstractASTAssignment(final WordOrSymbols assignmentOp) {
			this.assignmentOp = assignmentOp;
			assignmentOpLink = assignmentOp.wordOrSymbols().length() > 1 ? new ASTOperatorLink(this, assignmentOp, true) : null;
		}
		
		@Override
		public IRTypeUse getIRType() {
			return value != null ? value.getIRType() : new IRUnknownTypeUse(getIRContext());
		}
		
//		protected abstract @Nullable InterpretedObject target(InterpreterContext context);
//
//		protected abstract @Nullable IRVariableOrAttributeRedefinition varOrAttribute();
//
//		@Override
//		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
//			final ASTExpression expression = value;
//			if (expression == null)
//				return null;
//			InterpretedObject value = expression.interpret(context);
//			if (value == null)
//				return null;
//			final IRVariableOrAttributeRedefinition varOrAttribute = varOrAttribute();
//			if (varOrAttribute == null)
//				return null;
//			final IRAttributeRedefinition operator = assignmentOpLink != null ? assignmentOpLink.get() : null;
//			if (operator == null && assignmentOpLink != null)
//				return null;
//			if (varOrAttribute instanceof IRVariableRedefinition) {
//				final IRVariableDefinition variableDefinition = ((IRVariableRedefinition) varOrAttribute).definition();
//				if (operator != null)
//					value = operator.interpretDispatched(context.getLocalVariableValue(variableDefinition), Collections.singletonMap(operator.parameters().get(0).definition(), value), false);
//				context.setLocalVariableValue(variableDefinition, value);
//			} else {
//				final InterpretedObject target = target(context);
//				if (target == null)
//					return null;
//				final IRAttributeDefinition attributeDefinition = ((IRAttributeRedefinition) varOrAttribute).definition();
//				if (target instanceof InterpretedNormalObject) {
//					if (operator != null)
//						value = operator.interpretDispatched(((InterpretedNormalObject) target).getAttributeValue(attributeDefinition), Collections.singletonMap(operator.parameters().get(0).definition(), value), false);
//					((InterpretedNormalObject) target).setAttributeValue(attributeDefinition, value);
//				} else {// TODO tuples
//					throw new InterpreterException("Tried to set an attribute on a native object");
//				}
//			}
//			return value;
//		}
		
		protected abstract IRExpression makeAssignmentIR(IRExpression value);
		
		protected abstract IRExpression makeAccessIR();
		
		@Override
		public final IRExpression getIR() {
			final ASTExpression expression = value;
			if (expression == null)
				return new IRUnknownExpression("Missing right-hand side of assignment", this);
			final IRExpression val = expression.getIR();
			final ASTOperatorLink assignmentOpLink = this.assignmentOpLink;
			if (assignmentOpLink == null) // simple assignment (without operator)
				return makeAssignmentIR(val);
			final IRAttributeRedefinition operator = assignmentOpLink.get();
			if (operator == null)
				return new IRUnknownExpression("Operator with invalid parameter name: " + operator, this);
			final IRParameterRedefinition param = operator.getParameterByName("other");
			if (param == null) {
				final WordOrSymbols nameToken = assignmentOpLink.getNameToken();
				assert nameToken != null;
				return new IRUnknownExpression("Operator with invalid parameter name: " + operator, nameToken);
			}
			return makeAssignmentIR(new IRAttributeAccess(makeAccessIR(), operator, Collections.singletonMap(param.definition(), val), false, false, false));
		}
	}
	
	// TODO think about whether assignment should really be an expression - this can be handy, but can also hide state changes.
	// pro: [while ((toSleep = ...) > 0) Thread.sleep(toSleep);]
	// remember to issue a warning when used like [if (var = some bool)]
	public static class ASTLocalVariableOrUnqualifiedAttributeAssignment extends AbstractASTAssignment<ASTLocalVariableOrUnqualifiedAttributeAssignment> {
		public final ASTVariableOrUnqualifiedAttributeUse varOrAttribute;
		
		public ASTLocalVariableOrUnqualifiedAttributeAssignment(final ASTVariableOrUnqualifiedAttributeUse varOrAttribute, final WordOrSymbols assignmentOp) {
			super(assignmentOp);
			this.varOrAttribute = varOrAttribute;
			addChild(varOrAttribute);
			addChild(assignmentOp);
		}
		
		@Override
		public String toString() {
			return varOrAttribute + " " + assignmentOp + " " + value;
		}

		public static ASTLocalVariableOrUnqualifiedAttributeAssignment parse(final UnknownParser up, final ASTVariableOrUnqualifiedAttributeUse varOrAttribute, final WordOrSymbols assignmentOp) {
			final AttachedElementParser<ASTLocalVariableOrUnqualifiedAttributeAssignment> p = up.toElement(new ASTLocalVariableOrUnqualifiedAttributeAssignment(varOrAttribute, assignmentOp));
			p.ast.value = ASTExpressions.parse(p);
			return p.ast;
		}
		
		@Override
		protected IRExpression makeAssignmentIR(final IRExpression value) {
			final IRVariableOrAttributeRedefinition varOrAttr = varOrAttribute.link.get();
			if (varOrAttr == null)
				return new IRUnknownExpression("Cannot find the local variable or attribute " + varOrAttribute.link.getName(), varOrAttribute);
			if (varOrAttr instanceof IRAttributeRedefinition) {
				return new IRAttributeAssignment(IRThis.makeNew(this), ((IRAttributeRedefinition) varOrAttr).definition(), value);
			} else {
				return new IRVariableAssignment(((IRVariableRedefinition) varOrAttr).definition(), value);
			}
		}
		
		@Override
		protected IRExpression makeAccessIR() {
			final IRVariableOrAttributeRedefinition varOrAttr = varOrAttribute.link.get();
			if (varOrAttr == null)
				return new IRUnknownExpression("Cannot find the local variable or attribute " + varOrAttribute.link.getName(), varOrAttribute);
			if (varOrAttr instanceof IRAttributeRedefinition) {
				return new IRAttributeAccess(IRThis.makeNew(this), ((IRAttributeRedefinition) varOrAttr).definition(), Collections.EMPTY_MAP, false, false, false);
			} else {
				return new IRVariableExpression((IRVariableRedefinition) varOrAttr);
			}
		}
	}
	
	public static class ASTAttributeAssignment extends AbstractASTAssignment<ASTAttributeAssignment> {
		public final ASTExpression target;
		public final ASTLink<? extends IRAttributeRedefinition> attribute;
		
		public ASTAttributeAssignment(final ASTExpression target, final ASTLink<? extends IRAttributeRedefinition> attribute, final WordOrSymbols assignmentOp) {
			super(assignmentOp);
			this.target = target;
			this.attribute = attribute;
			addChild(target);
			addChild(assignmentOp);
			addLink(attribute);
		}
		
		@Override
		public String toString() {
			return target + "." + attribute + " = " + value;
		}
		
		public static ASTAttributeAssignment parse(final UnknownParser up, final ASTExpression target, final ASTLink<? extends IRAttributeRedefinition> attribute, final WordOrSymbols assignmentOp) {
			final AttachedElementParser<ASTAttributeAssignment> p = up.toElement(new ASTAttributeAssignment(target, attribute, assignmentOp));
			p.ast.value = ASTExpressions.parse(p);
			return p.ast;
		}
		
		@Override
		protected IRExpression makeAssignmentIR(final IRExpression value) {
			final IRAttributeRedefinition attr = attribute.get();
			if (attr == null) {
				final WordOrSymbols a = attribute.getNameToken();
				return new IRUnknownExpression("Cannot find an attribute named " + attribute.getName() + " in the type " + target.getIRType(), a == null ? this : a);
			}
			return new IRAttributeAssignment(target.getIR(), attr.definition(), value);
		}
		
		@Override
		protected IRExpression makeAccessIR() {
			final IRAttributeRedefinition attr = attribute.get();
			if (attr == null) {
				final WordOrSymbols a = attribute.getNameToken();
				return new IRUnknownExpression("Cannot find an attribute named " + attribute.getName() + " in the type " + target.getIRType(), a == null ? this : a);
			}
			return new IRAttributeAccess(target.getIR(), attr.definition(), Collections.EMPTY_MAP, false, false, false);
		}
	}
	
	public static class ASTTernaryIf extends AbstractASTElement implements ASTExpression {
		public ASTExpression condition;
		public @Nullable ASTExpression then, otherwise;
		
		public ASTTernaryIf(final ASTExpression condition, final SymbolToken questionMark) {
			this.condition = condition;
			addChild(condition);
			addChild(questionMark);
		}
		
		@Override
		public String toString() {
			return condition + " ? " + then + " : " + otherwise;
		}
		
		public static ASTTernaryIf parse(final UnknownParser up, final ASTExpression condition, final SymbolToken questionMark) {
			final AttachedElementParser<ASTTernaryIf> p = up.toElement(new ASTTernaryIf(condition, questionMark));
			p.ast.then = ASTExpressions.parse(p);
			p.one(':');
			p.ast.otherwise = ASTExpressions.parse(p);
			return p.ast;
		}
		
		@Override
		public IRTypeUse getIRType() {
			return IROrTypeUse.makeNew(then != null ? then.getIRType() : new IRUnknownTypeUse(getIRContext()), otherwise != null ? otherwise.getIRType() : new IRUnknownTypeUse(getIRContext()));
		}
		
		@Override
		public IRExpression getIR() {
			return new IRIf(condition.getIR(), then != null ? then.getIR() : new IRUnknownExpression("Syntax error. Correct syntax: [test ? then : otherwise]", this), otherwise != null ? otherwise.getIR() : null);
		}
	}
	
	// FIXME think about this some more
	public static class ASTErrorHandlingExpression extends AbstractASTElement implements ASTExpression {
		public ASTExpression expression;
		public boolean negated;
		public ASTLink<IRError> error = new ASTLink<IRError>(this) {
			@Override
			protected @Nullable IRError tryLink(final String name) {
				//expression.type();
				// TODO Auto-generated method stub
				return null;
			}
		};
		public List<ASTErrorHandlingExpressionParameter> parameters = new ArrayList<>();
		public @Nullable ASTExpression value;
		
		public ASTErrorHandlingExpression(final ASTExpression expression, final SymbolToken errorSymbol) {
			this.expression = expression;
			addChild(errorSymbol);
			addChild(expression);
		}
		
		@Override
		public String toString() {
			return "";
		}
		
		public static ASTErrorHandlingExpression parse(final UnknownParser up, final ASTExpression expression, final SymbolToken errorSymbol) {
			final AttachedElementParser<ASTErrorHandlingExpression> p = up.toElement(new ASTErrorHandlingExpression(expression, errorSymbol));
			p.ast.negated = p.try_('!');
			p.ast.error.setName(p.oneVariableIdentifierToken());
			if (p.try_('(')) {
				do {
					p.ast.parameters.add(ASTErrorHandlingExpressionParameter.parse(p));
				} while (p.try_(','));
				p.one(')');
			}
			p.one(':');
			p.ast.value = ASTExpressions.parse(p);
			return p.ast;
		}
		
		@Override
		public IRTypeUse getIRType() {
			return value != null ? value.getIRType() : new IRUnknownTypeUse(getIRContext());
		}
		
		@Override
		public IRExpression getIR() {
			return new IRUnknownExpression("not implemented", this);
		}
	}
	
	public static class ASTErrorHandlingExpressionParameter extends AbstractASTElement implements ASTLocalVariable {
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
			return "";
		}
		
		public static ASTErrorHandlingExpressionParameter parse(final Parser parent) {
			final AttachedElementParser<ASTErrorHandlingExpressionParameter> p = parent.startChild(new ASTErrorHandlingExpressionParameter());
			if (!p.try_("var"))
				p.ast.type = ASTTypeExpressions.parse(p, true, true);
			p.ast.parameter.setName(p.oneVariableIdentifierToken());
			return p.ast;
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
			return new IRBrokkrLocalVariable(this); // TODO correct?
		}
	}
	
	public static class ASTOperatorExpression extends AbstractASTElement implements ASTExpression {
		public List<ASTExpression> expressions = new ArrayList<>();
		public List<ASTLink<IRAttributeRedefinition>> operators = new ArrayList<>();
		
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
		
		@Override
		public @Nullable String hoverInfo(final Token token) {
			for (final ASTLink<IRAttributeRedefinition> opLink : operators) {
				final WordOrSymbols linkToken = opLink.getNameToken();
				if (linkToken != null && (linkToken.equals(token) || linkToken.tokens().contains(token))) {
					final IRAttributeRedefinition attr = opLink.get();
					return attr == null ? null : attr.hoverInfo();
				}
			}
			return null;
		}
		
		private final static String[] opsWithoutComp = {//
				"&", "|", "+", "-", "*", "/", "^", //
				"implies"};
		//, "extends", "super", "is"}; // FIXME extends, super, and is are problematic, as extensions (which can make a class/interface implement a new interface) may or may not be loaded at runtime
		// (e.g. they may not be included, but another loaded library loads them), making these operations quite volatile.
		// LANG better: allow check only for subinterfaces of interfaces marked in a specific way (this wouldn't help with extensions though)
		private final static String[] opsWithComp = {//
				"&", "|", "+", "-", "*", "/", "^", // copy of above
				">=", ">", "<=", "<", //
				"===", "==", "!==", "!=",
				"implies"};//, "extends", "super", "is"}; // copy of above
		private final static Set<String> assingmentOps = new HashSet<>(Arrays.asList("&", "|", "+", "-", "*", "/"));
		
		public static ASTExpression parse(final Parser parent, final boolean allowComparisons) {
			final AttachedElementParser<ASTOperatorExpression> p = parent.startChild(new ASTOperatorExpression());
			final ASTExpression first = ASTOperatorExpressionPart.parse(p);
			if (first == null) {
				p.expectedFatal("an expression");
				return p.ast;
			}
			p.ast.expressions.add(first);
			WordOrSymbols op;
			Token next;
			while (!((next = p.peekNext()) instanceof SymbolToken && assingmentOps.contains("" + ((SymbolToken) next).symbol) && p.peekNext('=', 1, true)) // +=/*=/etc.
					&& (op = p.try2(allowComparisons ? opsWithComp : opsWithoutComp)) != null) {
				p.ast.operators.add(new ASTOperatorLink(p.ast, op, true));
				final ASTExpression expression = ASTOperatorExpressionPart.parse(p);
				if (expression == null) {
					p.expectedFatal("an expression");
					return p.ast;
				}
				p.ast.expressions.add(expression);
			}
			if (p.ast.expressions.size() == 1)
				return first;
			else
				return p.ast;
		}
		
		/**
		 * Sets of operators of equal precedence, ordered by set precedence (higher index = higher precedence)
		 */
		@SuppressWarnings("null")
		final static String @NonNull [] @NonNull [] precedenceSets = {
				{">=", ">", "<=", "<", "===", "==", "!==", "!="},
				{"|"},
				{"&"},
				{"+", "-"},
				{"*", "/"},
				{"^"},
		};
		final static Set<String> allOrderableOperators = new HashSet<>();
		static {
			for (final String[] set : precedenceSets)
				allOrderableOperators.addAll(Arrays.asList(set));
		}
		
		private static int getPrecedence(final String o) {
			for (int i = 0; i < precedenceSets.length; i++) {
				final String[] set = precedenceSets[i];
				if (CollectionUtils.contains(set, o))
					return i;
			}
			return -1;
		}
		
		/**
		 * Defines operator precedence as a a partial order. Incomparable operators result in a semantic error, equal operators have the same precedence (e.g. <code>*</code> and
		 * <code>/</code>), and for other operators the relation is as follows: if o1 &lt; o2, then o1 has lower precedence than o2 (e.g. <code>a o1 b o2 c == a o1 (b o2 c)</code>)
		 */
		public final static PartialComparator<String> binaryOperatorComparator = new PartialComparator<String>() {
			@Override
			public PartialRelation compare(final String o1, final String o2) {
				if (o1.equals(o2))
					return PartialRelation.EQUAL;
				final int p1 = getPrecedence(o1), p2 = getPrecedence(o2);
				if (p1 < 0 || p2 < 0)
					return PartialRelation.INCOMPARABLE;
				return p1 == p2 ? PartialRelation.EQUAL : p1 < p2 ? PartialRelation.LESS : PartialRelation.GREATER;
			}
		};
		
		// TODO -x^2 should be -(x^2), not (-x)^2 == x^2 (or could also make this an error)
		
		private IRExpression build(final int fromExpressionIndex, final int toExpressionIndex) {
			if (fromExpressionIndex == toExpressionIndex)
				return expressions.get(fromExpressionIndex).getIR();
			int maxPrec = -1;
			for (int i = fromExpressionIndex; i < toExpressionIndex; i++) {
				@SuppressWarnings("null")
				final int p = getPrecedence(operators.get(i).getName());
				if (p > maxPrec)
					maxPrec = p;
			}
			for (int i = fromExpressionIndex; i < toExpressionIndex; i++) {
				final ASTLink<IRAttributeRedefinition> op = operators.get(i);
				final WordOrSymbols w = op.getNameToken();
				assert w != null;
				final int p = getPrecedence(w.wordOrSymbols());
				if (p == maxPrec) {
					final IRAttributeRedefinition attr = op.get();
					if (attr == null)
						return new IRUnknownExpression("Cannot find operator [" + w.wordOrSymbols() + "]", w);
					return new IRAttributeAccess(build(fromExpressionIndex, i), attr, Collections.singletonMap(attr.definition().parameters().get(0).definition(), build(i + 1, toExpressionIndex)), false, false, false);
				}
			}
//			assert false; // can happen on parse error
			return new IRUnknownExpression("Unexpected compiler error", this); // FIXME
		}
		
		@Override
		public IRExpression getIR() {
			if (operators.size() > 1) {
				for (final ASTLink<IRAttributeRedefinition> op : operators) {
					final WordOrSymbols w = op.getNameToken();
					assert w != null; // operators are only added to the list if they can be parsed
					if (!allOrderableOperators.contains(w.wordOrSymbols()))
						return new IRUnknownExpression("The operator [" + op.getName() + "] must not be used together with other operators, as the ordering of operators is not obvious.", w);
				}
			}
			return build(0, expressions.size() - 1);
		}
	}
	
	public static class ASTOperatorExpressionPart extends AbstractASTElement implements ASTExpression {
		public ASTLink<IRAttributeRedefinition> prefixOperator = new ASTOperatorLink(this, null, false);
		public @Nullable ASTExpression expression;
		
		@Override
		public String toString() {
			return (prefixOperator.getName() != null ? prefixOperator.getName() : "") + expression;
		}
		
		public static @Nullable ASTExpression parse(final Parser parent) {
			UnknownParser up = parent.startUnknownChild();
			final ASTModifierTypeUseModifier mod = ASTModifierTypeUseModifier.tryParse(up);
			if (mod != null) {
				DetachedElementParser<ASTModifierTypeUseModifier> modParser = up.toOnlyChildElementDetached(mod);
				final ASTTypeExpression e = ASTModifierTypeUse.parse(up, modParser);
				if (parent.peekNext('<'))
					return ASTTypeWithGenericArguments.parseWithModifiers(up, e);
				up.toOnlyChildElement();
				return e;
			}
			
			if (!parent.peekNextOneOf('!', '-'))
				return ASTAccessExpression.parse(parent);
			
			return parse(parent);
		}
		
		public static ASTOperatorExpressionPart parse(final UnknownParser up) {
			final AttachedElementParser<ASTOperatorExpressionPart> p = up.toElement(new ASTOperatorExpressionPart());
			p.ast.prefixOperator.setName(p.try2('!', '-'));
			ASTExpression expr;
			p.ast.expression = expr = ASTAccessExpression.parse(p);
			if (expr == null)
				p.expectedFatal("an expression");
			return p.ast;
		}
		
		@Override
		public IRTypeUse getIRType() {
			final IRAttributeRedefinition attributeRedefinition = prefixOperator.get();
			return attributeRedefinition == null ? new IRUnknownTypeUse(getIRContext()) : attributeRedefinition.mainResultType();
		}
		
		@Override
		public IRExpression getIR() {
			final IRAttributeRedefinition attribute = prefixOperator.get();
			if (attribute == null) {
				final WordOrSymbols op = prefixOperator.getNameToken();
				return new IRUnknownExpression("Cannot find the attribute for the prefix operator " + op, op == null ? this : op);
			}
			final ASTExpression expression = this.expression;
			if (expression == null)
				return new IRUnknownExpression("Syntax error, expected an expression", this);
			final IRExpression target = expression.getIR();
			return new IRAttributeAccess(target, attribute, Collections.EMPTY_MAP, false, false, false);
		}
	}
	
	public static class ASTAccessExpression extends AbstractASTElement implements ASTExpression {
		public final ASTExpression target;
		public boolean nullSafe, meta;
		public @Nullable ASTDirectAttributeAccess access;
		
		public ASTAccessExpression(final ASTExpression target) {
			this.target = target;
			addChild(target);
		}
		
		@Override
		public String toString() {
			return target + (nullSafe ? "?" : "") + (meta ? "::" : ".") + access;
		}
		
		@Override
		public IRTypeUse getIRType() {
			return access != null ? access.getIRType() : new IRUnknownTypeUse(getIRContext());
		}

		public static @Nullable ASTExpression parse(final ElementPlaceholder<ASTExpression> parser) {
			parser.startUnknown
			UnknownParser up = parent.startUnknownChild();
			final ASTExpression first = ASTAtomicExpression.parse(up);
			if (first == null)
				return null;
			DetachedElementParser<ASTExpression> firstParser = up.toOnlyChildElementDetached(first);
			DetachedElementParser<? extends ASTExpression> resultParser = parse(firstParser);
			return resultParser.toChildOf(parent).ast;
		}
		
		private static DetachedElementParser<? extends ASTExpression> parse(final DetachedElementParser<? extends ASTExpression> target) {
			if (!target.peekNextOneOf(".", "?.", "::", "?::")) // note: must try '?' together with '.' or '::', as it is also used by the ternary operator '? :'
				return target;
			DetachedElementParser<ASTAccessExpression> p = target.attachToNewDetachedParent(new ASTAccessExpression(target.ast));
			String op = p.oneOf(".", "?.", "::", "?::");
			assert op != null; // TODO make a peekNext call that returns the value?
			p.ast.nullSafe = op.startsWith("?");
			p.ast.meta = op.endsWith("::");
			p.ast.access = ASTDirectAttributeAccess.parse(p);
			return parse(p);
		}
		
		@Override
		public IRExpression getIR() {
			final IRExpression target = this.target.getIR();
			final ASTDirectAttributeAccess access = this.access;
			if (access == null)
				return new IRUnknownExpression("Syntax error, expected an attribute", this);
			final IRAttributeRedefinition attribute = access.attribute.get();
			if (attribute == null) {
				final WordOrSymbols a = access.attribute.getNameToken();
				return new IRUnknownExpression("Cannot find an attribute named " + a + " in the type " + target.type(), a == null ? this : a);
			}
			return new IRAttributeAccess(target, attribute, ASTArgument.makeIRArgumentMap(attribute.definition(), access.arguments), access.allResults, nullSafe, meta);
		}
		
		@Override
		public @Nullable Stream<ContentAssistProposal> getContentAssistProposals(final Token token, final StringMatcher matcher) {
			// can only get here from operator or ASTDirectAttributeAccess
			return target.getIR().type().members().stream().filter(m -> matcher.matches(m.redefinition().name())).map(ir -> new ContentAssistProposal(ir, ir.redefinition().name()));
		}
		
	}
	
	public static interface ASTAccess extends TypedASTElement {}
	
	public static class ASTDirectAttributeAccess extends AbstractASTElement implements ASTAccess, ASTMethodCall {
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
		public String toString() {
			return /*(negated ? "!" : "") +*/ attribute.getName() + (arguments.size() == 0 ? "" : "(...)");
		}
		
		public static ASTDirectAttributeAccess parse(final Parser parent) {
			final AttachedElementParser<ASTDirectAttributeAccess> p = parent.startChild(new ASTDirectAttributeAccess());
//			negated = try_('!');
			final WordToken name = p.oneVariableIdentifierToken();
			p.ast.attribute.setName(name);
			p.ast.allResults = name != null && name.word.endsWith("_"); //try_('!'); // FIXME other symbol - possible: !´`'¬@¦§°%_$£\     // maybe a combination of symbols? e.g. []
			p.tryGroup('(', () -> {
				do {
					p.ast.arguments.add(ASTArgument.parse(p));
				} while (p.try_(','));
			}, ')');
			return p.ast;
		}
		
		@Override
		public @Nullable IRAttributeRedefinition attribute() {
			return attribute.get();
		}
		
		@Override
		public IRTypeUse getIRType() {
			final IRAttributeRedefinition attributeRedefinition = attribute.get();
			if (attributeRedefinition == null)
				return new IRUnknownTypeUse(getIRContext());
			return allResults ? attributeRedefinition.allResultTypes() : attributeRedefinition.mainResultType();
		}
		
		@Override
		public @Nullable Stream<ContentAssistProposal> getContentAssistProposals(final Token token,
				final StringMatcher matcher) {
			return parent != null ? parent.getContentAssistProposals(token, matcher) : null;
		}
	}
	
	public static interface ASTAtomicExpression extends ASTExpression {
		public static @Nullable ASTExpression parse(final Parser parent) {
			// peek is acceptable here, as nobody needs content assist for expression *syntax*.
			final Token next = parent.peekNext();
			if (next instanceof StringToken) {
				parent.next();
				return new ASTString((StringToken) next);
			}
			if (next instanceof NumberToken) {
				parent.next();
				return new ASTNumberConstant((NumberToken) next);
			}
			if (next instanceof UppercaseWordToken)
				return ASTTypeExpressions.parse(parent, false, false);
			if (parent.peekNext('('))
				return ASTParanthesesExpression.parse(parent);
			if (parent.peekNext('['))
				return ASTTuple.parse(parent);
			if (parent.peekNext('~'))
				return ASTUnqualifiedMetaAccess.parse(parent);
//			if (parent.peekNext('?'))
//				return ImplicitLambdaArgument.parse(parent);
			if (parent.peekNext("this"))
				return ASTThis.parse(parent);
			if (parent.peekNext("null"))
				return ASTNull.parse(parent);
			if (parent.peekNext("arguments"))
				return ASTArgumentsKeyword.parse(parent);
			if (parent.peekNext("exists") || parent.peekNext("forall"))
				return ASTQuantifier.parse(parent);
			if (parent.peekNext("recurse"))
				return ASTRecurse.parse(parent);
			if (parent.peekNext("old"))
				return ASTOld.parse(parent);
			final ASTKleeneanConstant kleeneanConstant = ASTKleeneanConstant.tryParse(parent);
			if (kleeneanConstant != null)
				return kleeneanConstant;
			// must be here after all keywords
			if (next instanceof LowercaseWordToken)
				return ASTVariableOrUnqualifiedAttributeUse.parse(parent);
			return null;
		}
	}
	
	public static class ASTParanthesesExpression extends AbstractASTElement implements ASTExpression {
		
		private @Nullable ASTExpression expression;
		
		@Override
		public IRExpression getIR() {
			return expression != null ? expression.getIR() : new IRUnknownExpression("Missing expression after opening bracket '('", this);
		}
		
		@Override
		public String toString() {
			return "(" + expression + ")";
		}
		
		public static ASTParanthesesExpression parse(final Parser parent) {
			final AttachedElementParser<ASTParanthesesExpression> p = parent.startChild(new ASTParanthesesExpression());
			p.one('(');
			p.until(() -> {
				p.ast.expression = ASTExpressions.parse(p);
			}, ')', false);
			return p.ast;
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
//		protected @NonNull ImplicitLambdaArgument parse() {
//			one('?');
//			return this;
//		}
//	}
	
	public static class ASTNumberConstant extends AbstractASTElement implements ASTExpression {
		public final BigDecimal value;
		
		public ASTNumberConstant(final NumberToken token) {
			addChild(token);
			value = token.value;
		}
		
		@Override
		public String toString() {
			return "" + value;
		}
		
		@Override
		public @Nullable String hoverInfo(final Token token) {
			try {
				final long l = value.longValueExact();
				return l + " (0x" + Long.toHexString(l).toUpperCase(Locale.ENGLISH) + ")";
			} catch (final NumberFormatException e) {
				return "" + value;
			}
		}
		
		@Override
		public IRTypeUse getIRType() {
			return IRNumberConstant.type(getIRContext(), value);
		}
		
		@Override
		public IRExpression getIR() {
			return new IRNumberConstant(this);
		}
	}
	
	public static class ASTKleeneanConstant extends AbstractASTElement implements ASTExpression {
		public final Kleenean value;
		
		private ASTKleeneanConstant(final Kleenean value) {
			this.value = value;
		}
		
		public static @Nullable ASTKleeneanConstant tryParse(final Parser parent) {
			final WordOrSymbols token = parent.try2("true", "false", "unknown");
			if (token == null)
				return null;
			return new ASTKleeneanConstant(Kleenean.valueOf(token.wordOrSymbols().toUpperCase(Locale.ENGLISH)));
		}
		
		@Override
		public String toString() {
			assert value != null;
			return "" + value.name().toLowerCase(Locale.ENGLISH);
		}
		
		@Override
		public IRExpression getIR() {
			return new IRKleeneanConstant(this);
		}
		
	}
	
	/**
	 * The keyword 'this', representing 'the current object'.
	 */
	public static class ASTThis extends AbstractASTElement implements ASTExpression {
		@Override
		public String toString() {
			return "this";
		}
		
		@Override
		public @Nullable String hoverInfo(final Token token) {
			return null; // TODO current type, and maybe more
		}
		
		public static ASTThis parse(final Parser parent) {
			final AttachedElementParser<ASTThis> p = parent.startChild(new ASTThis());
			p.one("this");
			return p.ast;
		}
		
		@Override
		public IRTypeUse getIRType() {
			return IRSelfTypeUse.makeNew(this);
		}
		
		@Override
		public IRExpression getIR() {
			return IRThis.makeNew(this);
		}
	}
	
	/**
	 * The keyword 'null', representing 'no value' for 'optional' variables.
	 */
	public static class ASTNull extends AbstractASTElement implements ASTExpression {
		@Override
		public String toString() {
			return "null";
		}
		
		public static ASTNull parse(final Parser parent) {
			final AttachedElementParser<ASTNull> p = parent.startChild(new ASTNull());
			p.one("null");
			return p.ast;
		}
		
		@Override
		public IRExpression getIR() {
			return new IRNull(this);
		}
	}
	
	/**
	 * The keyword 'arguments', representing a tuple of all arguments to the current method.
	 */
	public static class ASTArgumentsKeyword extends AbstractASTElement implements ASTExpression {
		@Override
		public String toString() {
			return "arguments";
		}
		
		public static ASTArgumentsKeyword parse(final Parser parent) {
			final AttachedElementParser<ASTArgumentsKeyword> p = parent.startChild(new ASTArgumentsKeyword());
			p.one("arguments");
			return p.ast;
		}
		
		@Override
		public IRTypeUse getIRType() {
			final ASTAttribute attribute = getParentOfType(ASTAttribute.class);
			return attribute == null ? new IRUnknownTypeUse(getIRContext()) : attribute.getIR().allParameterTypes();
		}
		
		@Override
		public IRExpression getIR() {
			final ASTAttribute attribute = getParentOfType(ASTAttribute.class);
			if (attribute == null)
				return new IRUnknownExpression("Internal compiler error", this);
			return new IRArgumentsKeyword(attribute.getIR());
		}
	}
	
	/**
	 * A string literal.
	 */
	public static class ASTString extends AbstractASTElement implements ASTExpression {
		public final String value;
		
		public ASTString(final StringToken value) {
			addChild(value);
			this.value = value.value;
		}
		
		@Override
		public String toString() {
			assert value != null;
			return "'" + value.replaceAll("'", "\\'") + "'";
		}
		
		@Override
		public IRExpression getIR() {
			return new IRString(this);
		}
	}
	
	/**
	 * A first-order logic quantifier for contracts, i.e. a 'for all' or 'there exists'.
	 */
	public static class ASTQuantifier extends AbstractASTElement implements ASTExpression, ASTElementWithVariables {
		boolean forall;
		public final List<ASTQuantifierVars> vars = new ArrayList<>();
		public @Nullable ASTExpression condition;
		public @Nullable ASTExpression expression;
		
		@Override
		public String toString() {
			return (forall ? "forall" : "exists") + "(...)";
		}
		
		public static ASTQuantifier parse(final Parser parent) {
			final AttachedElementParser<ASTQuantifier> p = parent.startChild(new ASTQuantifier());
			p.ast.forall = Objects.equals(p.oneOf("forall", "exists"), "forall");
			p.oneGroup('(', () -> {
				p.until(() -> {
					do {
						p.ast.vars.add(ASTQuantifierVars.parse(p));
					} while (p.try_(';'));
					if (p.try_('|'))
						p.ast.condition = ASTExpressions.parse(p);
				}, ':', false);
				p.ast.expression = ASTExpressions.parse(p);
			}, ')');
			return p.ast;
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
	}
	
	public static class ASTQuantifierVars extends AbstractASTElement {
		public @Nullable ASTTypeUse type;
		public final List<ASTQuantifierVar> vars = new ArrayList<>();
		
		@Override
		public String toString() {
			return type + " " + StringUtils.join(vars, ", ");
		}
		
		public static ASTQuantifierVars parse(final Parser parent) {
			final AttachedElementParser<ASTQuantifierVars> p = parent.startChild(new ASTQuantifierVars());
			p.ast.type = ASTTypeExpressions.parse(p, true, true);
			do {
				p.ast.vars.add(ASTQuantifierVar.parse(p));
			} while (p.try_(','));
			return p.ast;
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
			final AttachedElementParser<ASTQuantifierVar> p = parent.startChild(new ASTQuantifierVar());
			p.ast.name = p.oneVariableIdentifierToken();
			return p.ast;
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
	
	/**
	 * A tuple, like '[a, b: c]' or '[A, B]'. If all entries are types, this is also a type.
	 */
	public static class ASTTuple extends AbstractASTElement implements ASTExpression {
		public List<ASTTupleEntry> entries = new ArrayList<>();
		
		@Override
		public String toString() {
			return "" + entries;
		}
		
		public static ASTTuple parse(final Parser parent) {
			return parse(parent, new ASTTuple());
		}
		
		protected static <T extends ASTTuple> T parse(final Parser parent, final T instance) {
			final AttachedElementParser<T> p = parent.startChild(instance);
			p.oneGroup('[', () -> {
				do {
					p.ast.entries.add(ASTTupleEntry.parse(parent, p.ast instanceof ASTTypeTuple));
				} while (p.try_(','));
			}, ']');
			return p.ast;
		}
		
		@Override
		public IRExpression getIR() {
			return ASTTupleEntry.makeIRNormalTuple(getIRContext(), entries);
		}
		
		@Override
		public IRTypeTuple getIRType() {
			return ASTTupleEntry.makeIRTypeTuple(getIRContext(), entries);
		}
		
	}
	
	/**
	 * A subclass of Tuple that implements TypeExpression, and entries are only parsed as types.
	 * <p>
	 * Exclusively used for parsing a tuple as a type only; normal tuples that contain only types must be handled equal to such a type tuple.
	 */
	public static class ASTTypeTuple extends ASTTuple implements ASTTypeExpression {
		@Override
		public IRTypeTuple getIR() {
			return (IRTypeTuple) ASTTupleEntry.makeIRNormalTuple(getIRContext(), entries);
		}
		
		public static ASTTypeTuple parse(final Parser parent) {
			return parse(parent, new ASTTypeTuple());
		}
	}
	
	/**
	 * An entry of a tuple, with an optional name.
	 */
	public static class ASTTupleEntry extends AbstractASTElement {
		public @Nullable WordToken name;
		public @Nullable ASTExpression value;
		
		@Override
		public String toString() {
			return (name != null ? name + ": " : "") + value;
		}
		
		public static ASTTupleEntry parse(final Parser parent, final boolean onlyTypes) {
			final AttachedElementParser<ASTTupleEntry> p = parent.startChild(new ASTTupleEntry());
			if (p.peekNext() instanceof WordToken && p.peekNext(':', 1, true)) {
				p.ast.name = p.oneIdentifierToken();
				p.next(); // skip ':'
			}
			p.ast.value = onlyTypes ? ASTTypeExpressions.parse(p, true, true) : ASTExpressions.parse(p);
			return p.ast;
		}
		
		public @Nullable String name() {
			final WordToken wordToken = name;
			return wordToken != null ? wordToken.word : null;
		}
		
		public static IRTuple makeIRNormalTuple(final IRContext irContext, final List<ASTTupleEntry> entries) {
			final IRTypeTupleBuilder builder = new IRTypeTupleBuilder(irContext);
			for (final ASTTupleEntry e : entries)
				builder.addEntry(e.getNormalIR());
			return builder.build();
		}
		
		public static IRTypeTuple makeIRTypeTuple(final IRContext irContext, final List<ASTTupleEntry> entries) {
			final IRTypeTupleBuilder builder = new IRTypeTupleBuilder(irContext);
			for (final ASTTupleEntry e : entries)
				builder.addEntry(e.getTypeIR());
			return builder.build();
		}
		
		public IRTupleBuilderEntry getNormalIR() {
			final ASTExpression expression = value;
			final WordToken nameToken = name;
			final IRTypeUse valueType = expression == null ? new IRUnknownTypeUse(getIRContext()) : expression.getIRType();
			return new IRTupleBuilderEntry(nameToken == null ? "<unknown>" : nameToken.word, valueType); // TODO make an UnknownString? maybe as a constant?
		}
		
		public IRTupleBuilderEntry getTypeIR() {
			final ASTExpression expression = value;
			final WordToken nameToken = name;
			return new IRTupleBuilderEntry(nameToken == null ? "<unknown>" : nameToken.word, expression == null ? new IRUnknownExpression("Syntax error, expected an expression", this) : expression.getIR());
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
	 * <p>
	 * Also handles unqualified attribute calls, but not unqualified meta accesses (see {@link ASTUnqualifiedMetaAccess})
	 */
	public static class ASTVariableOrUnqualifiedAttributeUse extends AbstractASTElement implements ASTExpression, ASTMethodCall, DebugString {
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
									return var.getIR();
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
//						else
//							System.out.println(((ASTTypeDeclaration) p).getIR() + ": " + ((ASTTypeDeclaration) p).getIR().allInterfaces() + " :: " + ((ASTTypeDeclaration) p).getIR().members()); // FIXME debug
					}
				}
				// TODO semantic error; maybe set directly in Link: (copied from old code, so needs modification)
				//if (arguments.size() > 0)
				//error(m + " is not a method");
				return null;
			}
		};
		public List<ASTArgument> arguments = new ArrayList<>();
		
		public static ASTVariableOrUnqualifiedAttributeUse parse(final Parser parent) {
			final AttachedElementParser<ASTVariableOrUnqualifiedAttributeUse> p = parent.startChild(new ASTVariableOrUnqualifiedAttributeUse());
			p.ast.link.setName(p.oneVariableIdentifierToken());
			p.tryGroup('(', () -> {
				do {
					p.ast.arguments.add(ASTArgument.parse(p));
				} while (p.try_(','));
			}, ')');
			return p.ast;
		}
		
		@Override
		public String debug() {
			return "" + link.get();
		}
		
		@Override
		public String toString() {
			return "" + link.getName();
		}
		
		@Override
		public @Nullable String hoverInfo(final Token token) {
			final IRVariableOrAttributeRedefinition varOrAttrib = link.get();
			if (varOrAttrib == null)
				return null;
			return varOrAttrib.hoverInfo();
		}
		
		@Override
		public IRTypeUse getIRType() {
			final IRVariableOrAttributeRedefinition variableOrAttributeRedefinition = link.get();
			return variableOrAttributeRedefinition == null ? new IRUnknownTypeUse(getIRContext()) : variableOrAttributeRedefinition.mainResultType();
		}
		
		@Override
		public @Nullable IRAttributeRedefinition attribute() {
			final IRVariableOrAttributeRedefinition varOrAttr = link.get();
			return varOrAttr instanceof IRAttributeRedefinition ? (IRAttributeRedefinition) varOrAttr : null;
		}
		
		@SuppressWarnings("null")
		@Override
		public IRExpression getIR() {
			final IRVariableOrAttributeRedefinition varOrAttribute = link.get();
			if (varOrAttribute == null) {
				final ASTTypeDeclaration selfAST = getParentOfType(ASTTypeDeclaration.class);
				return new IRUnknownExpression("Cannot find an attribute with the name " + link.getName() + " in the type " + (selfAST == null ? "<unknown type>" : selfAST.name()), link.getNameToken());
			}
			if (varOrAttribute instanceof IRVariableRedefinition)
				return new IRVariableExpression((IRVariableRedefinition) varOrAttribute);
			else
				return new IRAttributeAccess(IRThis.makeNew(this), (IRAttributeRedefinition) varOrAttribute, ASTArgument.makeIRArgumentMap(((IRAttributeRedefinition) varOrAttribute).definition(), arguments),
						false, false, false);
		}
		
		@Override
		public @Nullable Stream<ContentAssistProposal> getContentAssistProposals(final Token token, final StringMatcher matcher) {
			final List<ContentAssistProposal> result = new ArrayList<>();
			for (ASTElement p = parent(); p != null; p = p.parent()) {
				if (p instanceof ASTBlock) {
					// note: does not care about order of variable use and declaration - TODO either check this here or just let the semantic checker handle it
					for (final ASTVariableDeclarations vars : ((ASTBlock) p).getDirectChildrenOfType(ASTVariableDeclarations.class)) {
						for (final ASTVariableDeclarationsVariable var : vars.variables) {
							final LowercaseWordToken nameToken = var.nameToken;
							if (nameToken != null && matcher.matches(nameToken.word))
								result.add(new ContentAssistProposal(var.getIR(), nameToken.word));
						}
					}
				}
				if (p instanceof ASTElementWithVariables) {
					for (final IRVariableRedefinition var : ((ASTElementWithVariables) p).allVariables()) {
						if (matcher.matches(var.name()))
							result.add(new ContentAssistProposal(var, var.name()));
					}
				}
				if (p instanceof ASTTypeDeclaration) {
					for (final IRMemberRedefinition member : ((ASTTypeDeclaration) p).getIR().members()) {
						if (member instanceof IRAttributeRedefinition && matcher.matches(member.name())) {
							if (result.stream().anyMatch(cap -> cap.getElementToShow() instanceof IRVariableRedefinition && ((IRVariableRedefinition) cap.getElementToShow()).name().equals(member.name())))
								result.add(new ContentAssistProposal(member, "this." + member.name()));
							else
								result.add(new ContentAssistProposal(member, member.name()));
						}
					}
				}
			}
			return result.stream();
		}
	}
	
	/**
	 * An argument to a function call.
	 */
	public static class ASTArgument extends AbstractASTElement {
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
		public String toString() {
			return "" + value;
		}
		
		public static ASTArgument parse(final Parser parent) {
			final AttachedElementParser<ASTArgument> p = parent.startChild(new ASTArgument());
			p.ast.isDots = p.try_("...");
			if (!p.ast.isDots) {
				if (p.peekNext() instanceof LowercaseWordToken && p.peekNext(':', 1, true)) {
					p.ast.parameter.setName(p.oneVariableIdentifierToken());
					p.next(); // skip ':'
				}
				p.ast.value = ASTExpressions.parse(p);
			}
			return p.ast;
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
			final List<IRParameterRedefinition> parameters = method.parameters();
			final Map<IRParameterDefinition, IRExpression> r = new HashMap<>();
			for (int i = 0; i < args.size(); i++) {
				final ASTArgument arg = args.get(i);
				final ASTExpression expression = arg.value;
				if (expression == null)
					continue;
				final IRExpression value = expression.getIR();
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
	 * A meta access without a target (like '~a'), i.e. targets 'this'. Can thus also not be null-safe ('this' is never null).
	 * <p>
	 * TODO different symbol? maybe '::' like in Java?
	 * <p>
	 * TODO how to handle allResults here?
	 */
	public static class ASTUnqualifiedMetaAccess extends AbstractASTElement implements ASTExpression {
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
		public String toString() {
			return "~" + attribute.getName();
		}
		
		public static ASTUnqualifiedMetaAccess parse(final Parser parent) {
			final AttachedElementParser<ASTUnqualifiedMetaAccess> p = parent.startChild(new ASTUnqualifiedMetaAccess());
			p.one('~');
			p.ast.attribute.setName(p.oneVariableIdentifierToken());
			return p.ast;
		}
		
		@SuppressWarnings("null")
		@Override
		public IRExpression getIR() {
			final IRAttributeRedefinition attributeRedefinition = attribute.get();
			if (attributeRedefinition == null) {
				final ASTTypeDeclaration selfAST = getParentOfType(ASTTypeDeclaration.class);
				return new IRUnknownExpression("Cannot find an attribute with the name " + attribute.getName() + " in the type " + (selfAST == null ? "<unknown type>" : selfAST.name()), attribute.getNameToken());
			}
			return new IRAttributeAccess(IRThis.makeNew(this), attributeRedefinition, Collections.EMPTY_MAP, false, false, true);
		}
	}
	
	/**
	 * The keyword 'recurse' which calls the current method, with optionally new arguments (and all unspecified arguments left the same).
	 */
	public static class ASTRecurse extends AbstractASTElement implements ASTExpression, ASTMethodCall {
		public List<ASTArgument> arguments = new ArrayList<>();
		
		@Override
		public @Nullable IRAttributeRedefinition attribute() {
			final ASTAttribute attr = getParentOfType(ASTAttribute.class);
			return attr == null ? null : attr.getIR();
		}
		
		@Override
		public IRTypeUse getIRType() {
			final IRAttributeRedefinition attribute = attribute();
			return attribute == null ? new IRUnknownTypeUse(getIRContext()) : attribute.mainResultType();
		}
		
		@Override
		public String toString() {
			return "recurse(...)";
		}
		
		@Override
		public @Nullable String hoverInfo(final Token token) {
			final ASTAttributeDeclaration attribute = getParentOfType(ASTAttributeDeclaration.class);
			return attribute == null ? null : attribute.hoverInfo(token);
		}
		
		public static ASTRecurse parse(final ElementPlaceholder<ASTRecurse> parser) {
			final AttachedElementParser<ASTRecurse> p = parser.startAttached(new ASTRecurse());
			p.one("recurse");
			p.oneGroup('(', () -> {
				do {
					p.ast.arguments.add(ASTArgument.parse(p));
				} while (p.try_(','));
			}, ')');
			return p.ast;
		}
		
		@Override
		public IRExpression getIR() {
			final IRAttributeRedefinition attribute = attribute();
			if (attribute == null)
				return new IRUnknownExpression("Internal compiler error", this);
			return new IRAttributeAccess(attribute.isStatic() ? null : IRThis.makeNew(this),
					attribute, ASTArgument.makeIRArgumentMap(attribute.definition(), arguments), false, false, false);
		}
	}
	
	/**
	 * The keyword/"function" 'old' which evaluates an expression as if it were evaluated at the beginning of its parent function.
	 */
	public static class ASTOld extends AbstractASTElement implements ASTExpression {
		public @Nullable ASTExpression expression;
		
		public static ASTOld parse(final Parser parent) {
			final AttachedElementParser<ASTOld> p = parent.startChild(new ASTOld());
			p.one("old");
			p.oneGroup('(', () -> {
				p.ast.expression = ASTExpressions.parse(p);
			}, ')');
			return p.ast;
		}
		
		@Override
		public String toString() {
			return "old(" + expression + ")";
		}
		
		@Override
		public IRTypeUse getIRType() {
			return expression != null ? expression.getIRType() : new IRUnknownTypeUse(getIRContext());
		}
		
		@Override
		public IRExpression getIR() {
			// TODO make sure to register this to the attribute so that it can be calculated when the attribute is called (and the IROld just returns that value later)
			final ASTAttributeDeclaration attribute = getParentOfType(ASTAttributeDeclaration.class);
			final ASTExpression expression = this.expression;
			if (attribute == null)
				return new IRUnknownExpression("Internal compiler error", this);
			if (expression == null)
				return new IRUnknownExpression("Syntax error. Proper syntax: [old(some expression)]", this);
			return new IROld(attribute.getIR(), expression.getIR());
		}
	}
	
	// ================================ Types ================================
	
	public static class ASTTypeExpressions {
		
		public static ASTTypeExpression parse(final Parser parent, final boolean allowOps, final boolean allowTuple) {
			return parse(parent, allowOps, allowTuple, allowOps);
		}
		
		static ASTTypeExpression parse(final Parser parser, final boolean allowOps, final boolean allowTuple, final boolean allowDotGeneric) {
			assert !(!allowDotGeneric && allowOps) : "generics are automatically allowed when operators are";
			if (allowDotGeneric && !allowOps) { // if allowing ops, ops are done first
				DetachedElementParser<ASTTypeExpression> dp = parser.startDetached();
				final ASTTypeExpression target = ASTTypeExpressions.parse(dp, false, false, false); // TODO do tuples have generics? or should this just be allowed to then produce a better error message?
				if (parser.peekNext('.'))
					return ASTGenericTypeAccess.parse(dp);
				else
					return dp.toChildOf(parser).ast();
			}
			if (allowTuple && parser.peekNext('[')) {
//				if (!allowTuple)
//					throw new ParseException("Expected a type that is not a tuple", parent.in.getOffset(), parent.in.getOffset() + 1);
				// not null, since it starts with '['
				return (ASTTypeTuple) ASTTuple.parse(parser);
			}
			if (!allowOps) { // i.e. only a single type (possibly with modifiers and generics)
				if (parser.peekNext("Self"))
					return ASTSelf.parse(parser);
				final ASTModifierTypeUseModifier modifier = ASTModifierTypeUseModifier.tryParse(parser);
				final ASTTypeExpression e;
				if (modifier != null)
					e = ASTModifierTypeUse.parse(parser, modifier);
				else
					e = ASTSimpleTypeUse.parse(parser);
				if (parser.peekNext('<'))
					return ASTTypeWithGenericArguments.parseWithModifiers(parser, e);
				return e;
			}
			return ASTTypeUseWithOperators.parse(parser);
		}
	}
	
	/**
	 * The keyword 'Self', representing the class of the current object (i.e. equal to this.class, but can be used in more contexts like interfaces and generics)
	 */
	public static class ASTSelf extends AbstractASTElement implements ASTTypeExpression {
//		ASTLink<IRTypeDefinition> link = new ASTLink<IRTypeDefinition>(this) {
//			@Override
//			protected @Nullable IRTypeDefinition tryLink(final String name) {
//				final ASTTypeDeclaration astTypeDeclaration = ASTSelf.this.getParentOfType(ASTTypeDeclaration.class);
//				return astTypeDeclaration == null ? null : astTypeDeclaration.getIR();
//			}
//		};
		
		@Override
		public String toString() {
			return "Self";
		}
		
		public static ASTSelf parse(final Parser parent) {
			final AttachedElementParser<ASTSelf> p = parent.startChild(new ASTSelf());
			p.one("Self");
			return p.ast;
		}
		
		@Override
		public IRTypeUse getIR() {
			return IRSelfTypeUse.makeNew(this);
		}
	}
	
	public final static class ASTOperatorLink extends ASTLink<IRAttributeRedefinition> {
		private final boolean isBinary;
		
		public ASTOperatorLink(final ASTElement parent, @Nullable final WordOrSymbols symbols, final boolean isBinary) {
			super(parent, symbols);
			this.isBinary = isBinary;
		}
		
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
			return parentElement.getIRContext().getTypeDefinition("lang", s[0]).getAttributeByName(s[1]);
		}
	}
	
	/**
	 * An expression with types and operators, currently only & and |.
	 */
	public static class ASTTypeUseWithOperators extends AbstractASTElement implements ASTTypeExpression {
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
		
		public static ASTTypeExpression parse(final Parser parent) {
			final AttachedElementParser<ASTTypeUseWithOperators> p = parent.startChild(new ASTTypeUseWithOperators());
			final ASTTypeExpression first = ASTTypeExpressions.parse(p, false, true, true);
			p.ast.types.add(first);
			SymbolToken op;
			while ((op = p.try2('&', '|')) != null) {
				p.ast.operators.add(new ASTOperatorLink(p.ast, op, true));
				p.ast.types.add(ASTTypeExpressions.parse(p, false, true, true));
			}
			if (p.ast.types.size() == 1)
				return first;
			else
				return p.ast;
		}
		
		// TODO use proper operator order
		
		@Override
		public IRTypeUse getIR() {
			IRTypeUse o = types.get(0).getIR();
			for (int i = 0; i < operators.size(); i++) {
				final IRTypeUse o2 = types.get(i + 1).getIR();
				if ("&".equals(operators.get(i).getName()))
					o = IRAndTypeUse.makeNew(o, o2);
				else
					o = IROrTypeUse.makeNew(o, o2);
			}
			return o;
		}
	}
	
	/**
	 * A type use that is a single word, which can be either a normal type or a generic parameter.
	 */
	public static class ASTSimpleTypeUse extends AbstractASTElement implements ASTTypeExpression {
		
		public final ASTLink<IRTypeUse> link = new ASTLink<IRTypeUse>(this) {
			@Override
			protected @Nullable IRTypeUse tryLink(final String name) {
				// A type is either defined in the same file, or imported.
				// From types in the same file only siblings and siblings of parents are valid candidates.
				ASTElement start = parent();
				if (start instanceof ASTTypeDeclaration) { // i.e. this type is in the declaration of a type as a parent or such // TODO improve this (documentation + implementation - currently depends on no elements between the type decl and this type)
					if (name.equals(((ASTTypeDeclaration) start).name()))
						return ((ASTTypeDeclaration) start).getIR().getUse();
					start = start.parent(); // prevent looking at members of the implementing/extending type (also prevents an infinite loop)
				}
				for (ASTElement p = start; p != null; p = p.parent()) {
					if (p instanceof ASTTypeDeclaration) {
						final IRTypeDefinition type = ((ASTTypeDeclaration) p).getIR();
						// generic types
						final IRAttributeRedefinition attribute = type.getAttributeByName(name);
						if (attribute != null) {
							final IRSelfTypeUse self = IRSelfTypeUse.makeNew(p);
							return new IRGenericTypeAccess(self, attribute.getUse(self));
						}
						// inner classes and interfaces // TODO make part of interpreted or not?
						final List<ASTTypeDeclaration> declarations = ((ASTTypeDeclaration) p).getDirectChildrenOfType(ASTTypeDeclaration.class);
						for (final ASTTypeDeclaration d : declarations) {
							if (name.equals(d.name())) {
								return d.getIR().getUse();
							}
						}
					} else if (p instanceof ASTSourceFile) {
						final ASTModule m = ((ASTSourceFile) p).module;
						if (m == null)
							return null;
						final IRTypeDefinition typeDefinition = m.getType(name);
						return typeDefinition == null ? null : typeDefinition.getUse();
					} else if (p instanceof ASTAttribute) {
						if (name.equals(((ASTAttribute) p).name())) {
							final IRSelfTypeUse self = IRSelfTypeUse.makeNew(p);
							return new IRGenericTypeAccess(self, ((ASTAttribute) p).getIR().getUse(self));
						}
//						for (final ASTGenericTypeDeclaration gp : ((ASTAttribute) p).modifiers().genericParameters) {
//							if (name.equals(gp.name()))
//								return gp.getIR();
//						}
					}
				}
				return null;
			}
		};
		
		@Override
		public String toString() {
			return "" + link.getName();
		}
		
		public static ASTSimpleTypeUse parse(final Parser parent) {
			final AttachedElementParser<ASTSimpleTypeUse> p = parent.startChild(new ASTSimpleTypeUse());
			p.ast.link.setName(p.oneTypeIdentifierToken());
			return p.ast;
		}
		
		@Override
		public IRTypeUse getIR() {
			final IRTypeUse type = link.get();
			return type == null ? new IRUnknownTypeUse(getIRContext()) : type;
		}
		
	}
	
	/**
	 * A type use with modifiers, e.g. 'mod exclusive C'.
	 */
	public static class ASTModifierTypeUse extends AbstractASTElement implements ASTTypeExpression {
		
		public final List<ASTModifierTypeUseModifier> modifiers = new ArrayList<>();
		/**
		 * Either a {@link ASTSimpleTypeUse} or a {@link ASTTypeWithGenericArguments}
		 */
		public @Nullable ASTTypeExpression type;
		
		public ASTModifierTypeUse(final ASTModifierTypeUseModifier firstModifier) {
			modifiers.add(firstModifier);
			addChild(firstModifier);
		}
		
		@Override
		public String toString() {
			return (modifiers.size() == 0 ? "" : StringUtils.join(modifiers, " ") + " ") + type;
		}
		
		public static ASTTypeExpression parse(SingleElementParser<ASTTypeExpression> parent, final DetachedElementParser<ASTModifierTypeUseModifier> firstModifier) {
			final ElementParser<ASTModifierTypeUse> p = parent.startParsing(new ASTModifierTypeUse(firstModifier.ast));
			firstModifier.toChildOf(p);
			do {
				final ASTModifierTypeUseModifier e = ASTModifierTypeUseModifier.tryParse(p.parseChild());
				if (e != null) {
					p.ast.modifiers.add(e);
					continue;
				}
			} while (false);
			
			// TODO is 'modifier Self' possible?
			p.ast.type = ASTSimpleTypeUse.parse(p);
			
			return p.ast;
		}
		
		@Override
		public IRTypeUse getIR() {
			final IRTypeUse result = type != null ? type.getIR() : new IRUnknownTypeUse(getIRContext());
			// TODO
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
	public static class ASTModifierTypeUseModifier extends AbstractASTElement {
		
		public @Nullable Modifiability modifiability;
		public @Nullable Exclusiveness exclusivity;
		public @Nullable Optionality optional;
		public @Nullable Borrowing borrowing;
		
		public @Nullable ASTExpression from;
		
		@Override
		public String toString() {
			return (modifiability != null ? modifiability : exclusivity != null ? exclusivity : optional)
					+ (from == null ? "" : "@" + (from instanceof ASTVariableOrUnqualifiedAttributeUse || from instanceof ASTThis ? from : "(" + from + ")"));
		}
		
		public static @Nullable ASTModifierTypeUseModifier tryParse(final SingleElementParser<ASTModifierTypeUseModifier> parent) {
			final ElementParser<ASTModifierTypeUseModifier> p = parent.startParsing(new ASTModifierTypeUseModifier());
			final Modifiability modifiability = Modifiability.parse(p);
			if (modifiability != null) {
				p.ast.modifiability = modifiability;
				return parseFrom(p);
			}
			final Exclusiveness exclusivity = Exclusiveness.parse(p);
			if (exclusivity != null) {
				p.ast.exclusivity = exclusivity;
				return parseFrom(p);
			}
			final Optionality optional = Optionality.parse(p);
			if (optional != null) {
				p.ast.optional = optional;
				return parseFrom(p);
			}
			final Borrowing borrowing = Borrowing.parse(p);
			if (borrowing != null) {
				p.ast.borrowing = borrowing;
				return parseFrom(p);
			}
			return null;
		}
		
		private static ASTModifierTypeUseModifier parseFrom(final ElementParser<ASTModifierTypeUseModifier> p) {
			if (p.try_('@')) {
				if (!p.tryGroup('(', () -> {
					p.ast.from = ASTExpressions.parse(p);
				}, ')')) {
					if (p.peekNext("this"))
						p.ast.from = ASTThis.parse(p);
					else
						p.ast.from = ASTVariableOrUnqualifiedAttributeUse.parse(p);
				}
			}
			return p.ast;
		}
	}
	
	/**
	 * A type use with generic arguments, e.g. 'A<B, C: D>'
	 */
	public static class ASTTypeWithGenericArguments extends AbstractASTElement implements ASTTypeExpression {
		
		public final ASTSimpleTypeUse baseType;
		
		public ASTTypeWithGenericArguments(final ASTSimpleTypeUse baseType) {
			this.baseType = baseType;
			addChild(baseType);
		}
		
		public final List<ASTGenericArgument> genericArguments = new ArrayList<>();
		
		@Override
		public String toString() {
			return baseType + "<" + StringUtils.join(genericArguments, ",") + ">";
		}
		
		public static ASTTypeExpression parseWithModifiers(final UnknownParser parent, final ASTTypeExpression withModifiers) {
			if (withModifiers instanceof ASTSimpleTypeUse)
				return parse(parent, (ASTSimpleTypeUse) withModifiers);
			final ASTModifierTypeUse modifierTypeUse = (ASTModifierTypeUse) withModifiers;
			final ASTSimpleTypeUse typeUseElement = (ASTSimpleTypeUse) modifierTypeUse.type; // cast is valid, as the other case is constructed here
			assert typeUseElement != null; // shouldn't get here if this is null
			final ASTTypeWithGenericArguments generic = parse(parent, typeUseElement);
			modifierTypeUse.type = generic;
			modifierTypeUse.addChild(generic);
			return modifierTypeUse;
		}
		
		public static ASTTypeWithGenericArguments parse(final Parser parent, final ASTSimpleTypeUse baseType) {
			final AttachedElementParser<ASTTypeWithGenericArguments> p = parent.startChild(new ASTTypeWithGenericArguments(baseType));
			p.oneGroup('<', () -> {
				do {
					p.ast.genericArguments.add(ASTGenericArgument.parse(p));
				} while (p.try_(','));
			}, '>');
			return p.ast;
		}
		
		@Override
		public IRTypeUse getIR() {
			final Map<IRAttributeDefinition, IRGenericArgument> genericArguments = new HashMap<>();
			final IRTypeUse baseTypeIR = baseType.getIR();
			for (int i = 0; i < this.genericArguments.size(); i++) {
				final ASTGenericArgument ga = this.genericArguments.get(i);
				final IRAttributeRedefinition attrib = ga.attribute(baseTypeIR, i);
				final ASTGenericArgumentValue value = ga.value;
				if (attrib == null || value == null)
					continue;
				genericArguments.put(attrib.definition(), value.getIR());
			}
			return baseTypeIR.getGenericUse(genericArguments);
		}
	}
	
	/**
	 * A generic argument to a type use.
	 */
	public static class ASTGenericArgument extends AbstractASTElement {
		private final ASTLink<IRAttributeRedefinition> attribute = new ASTLink<IRAttributeRedefinition>(this) {
			@Override
			protected @Nullable IRAttributeRedefinition tryLink(final String name) {
				assert parent != null;
				final IRAttributeUse attributeUse = ((ASTTypeWithGenericArguments) parent).baseType.getIR().getAttributeByName(name);
				if (attributeUse == null)
					return null;
				return attributeUse.redefinition();
			}
		};
		public boolean wildcard;
		public @Nullable ASTGenericArgumentValue value;
		
		@Override
		public String toString() {
			return (attribute.getName() == null ? "" : attribute.getName() + ": ") + value;//(wildcard ? "?" + (extends_ == null ? "" : " extends " + extends_) + (super_ == null ? "" : " super " + super_) : value);
		}
		
		public @Nullable IRAttributeRedefinition attribute(final IRTypeUse baseType, final int index) {
			if (attribute.getName() != null) {
				final IRAttributeRedefinition attributeRedefinition = attribute.get();
				return attributeRedefinition == null ? null : attributeRedefinition;
			}
			if (baseType instanceof IRSimpleTypeUse) {
				final List<IRAttributeRedefinition> positionalGenericParameters = ((IRSimpleTypeUse) baseType).getDefinition().positionalGenericParameters();
				if (index < positionalGenericParameters.size())
					return positionalGenericParameters.get(index);
			}
			return null;
		}
		
		public static ASTGenericArgument parse(final Parser parent) {
			final AttachedElementParser<ASTGenericArgument> p = parent.startChild(new ASTGenericArgument());
			if (p.peekNext() instanceof WordToken && p.peekNext(':', 1, true)) {
				p.ast.attribute.setName(p.oneIdentifierToken());
				p.next(); // skip ':'
			} else {
				p.ast.attribute.setName(null); // TODO what to link? nothing? (there are no characters that could be linked, as everything is the value)
			}
			p.ast.value = ASTGenericArgumentValue.parse(p);
			return p.ast;
		}
	}
	
	public static class ASTGenericArgumentValue extends AbstractASTElement {
		
		private @Nullable ASTExpression expression;
		
		@Override
		public String toString() {
			return "" + expression;
		}
		
		public static ASTGenericArgumentValue parse(final Parser parent) {
			final AttachedElementParser<ASTGenericArgumentValue> p = parent.startChild(new ASTGenericArgumentValue());
//		if (try_('?')) {
//			wildcard = true;
//			unordered(() -> {
//				if (try_("extends"))
//					extends_ = ActualType.parse(this, true, true);
//			}, () -> {
//				if (try_("super"))
//					super_ = ActualType.parse(this, true, true);
//			});
//		} else {
//			value = ActualType.parse(this, true, true);
//		}
			p.ast.expression = ASTExpressions.parse(p, false);
			return p.ast;
		}
		
		public IRGenericArgument getIR() {
			return expression != null ? IRValueGenericArgument.fromExpression(expression) : new IRUnknownGenericArgument("missing value", this);
		}
		
	}
	
	/**
	 * Access to a generic type of a type, e.g. 'A.B'.
	 * TODO is this the same as a static attribute access? and is this allowed on objects (and not only types) too?
	 */
	public static class ASTGenericTypeAccess extends AbstractASTElement implements ASTTypeExpression {
		public final ASTTypeUse target;
		private final ASTLink<IRAttributeUse> genericType = new ASTLink<IRAttributeUse>(this) {
			@Override
			protected @Nullable IRAttributeUse tryLink(final String name) {
				return target.getIR().getAttributeByName(name);
			}
		};
		
		public ASTGenericTypeAccess(final ASTTypeUse target) {
			this.target = target;
			addChild(target);
		}
		
		@Override
		public String toString() {
			return target + "." + genericType.getName();
		}
		
		public static ASTGenericTypeAccess parse(final DetachedElementParser<? extends ASTTypeUse> target) {
			DetachedElementParser<ASTGenericTypeAccess> p = target.attachToNewDetachedParent(new ASTGenericTypeAccess(target.ast));
			p.one('.'); // TODO create a "one" that asserts that it succeeds?
			p.ast.genericType.setName(p.oneTypeIdentifierToken());
			if (p.peekNext('.'))
				return parse(p);
			return p.ast;
		}
		
		@Override
		public IRTypeUse getIR() {
			final IRAttributeUse attr = genericType.get();
			final IRTypeUse t = target.getIR();
			if (attr == null)
				return new IRUnknownTypeUse(getIRContext());
			return new IRGenericTypeAccess(t, attr);
		}
	}
	
}
