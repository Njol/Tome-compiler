package ch.njol.tome.ast.expressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.ASTLink;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.compiler.Token;
import ch.njol.tome.compiler.Token.SymbolToken;
import ch.njol.tome.compiler.Token.WordOrSymbols;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.expressions.IRAttributeAccess;
import ch.njol.tome.ir.expressions.IRExpression;
import ch.njol.tome.ir.expressions.IRUnknownExpression;
import ch.njol.tome.parser.Parser;
import ch.njol.util.CollectionUtils;
import ch.njol.util.PartialComparator;
import ch.njol.util.PartialRelation;

public class ASTOperatorExpression extends AbstractASTElement implements ASTExpression {
	public List<ASTExpression> expressions = new ArrayList<>();
	public List<ASTLink<IRAttributeRedefinition>> operators = new ArrayList<>();
	
	@Override
	public String toString() {
		if (expressions.isEmpty())
			return "<incomplete operator expression>";
		final StringBuilder b = new StringBuilder();
		b.append('(');
		b.append(expressions.get(0));
		for (int i = 1; i < expressions.size(); i++) {
			b.append(' ');
			b.append(operators.get(i - 1));
			b.append(' ');
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
	
	private final static String[] ops = {//
			"&", "|", "+", "-", "*", "/", "^", // copy of above
			">=", ">", "<=", "<", //
			"===", "==", "!==", "!=",
			"implies"};//, "extends", "super", "is"}; // FIXME extends, super, and is are problematic, as extensions (which can make a class/interface implement a new interface) may or may not be loaded at runtime
	// (e.g. they may not be included, but another loaded library loads them), making these operations quite volatile.
	// LANG better: allow check only for subinterfaces of interfaces marked in a specific way (this wouldn't help with extensions though)
	private final static Set<String> assingmentOps = new HashSet<>(Arrays.asList("&", "|", "+", "-", "*", "/"));
	
	public static ASTExpression parse(final Parser parent) {
		final Parser p = parent.start();
		final ASTOperatorExpression ast = new ASTOperatorExpression();
		final ASTExpression first = ASTOperatorExpressionPart.parse(p);
		if (first == null) {
			p.expectedFatal("an expression");
			return p.done(ast);
		}
		ast.expressions.add(first);
		ASTLink<IRAttributeRedefinition> op;
		Token next;
		while (!((next = p.peekNext()) instanceof SymbolToken && assingmentOps.contains("" + ((SymbolToken) next).symbol) && p.peekNext('=', 1, true)) // +=/*=/etc.
				&& (op = ASTOperatorLink.tryParse(p, true, ops)) != null) {
			ast.operators.add(op);
			final ASTExpression expression = ASTOperatorExpressionPart.parse(p);
			if (expression == null) {
				p.expectedFatal("an expression");
				return p.done(ast);
			}
			ast.expressions.add(expression);
		}
		if (ast.expressions.size() == 1) {
			p.doneAsChildren();
			return first;
		}
		return p.done(ast);
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
