package ch.njol.tome.ast.expressions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTElement;
import ch.njol.tome.ast.ASTLink;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.parser.Parser;

public class ASTOperatorLink extends ASTLink<IRAttributeRedefinition> {
	
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
			
			// TODO should these exist?
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
	
	private final boolean isBinary;
	
	public ASTOperatorLink(final boolean isBinary) {
		this.isBinary = isBinary;
	}
	
	@Override
	protected @Nullable IRAttributeRedefinition tryLink(final String name) {
		final @NonNull String[] s = (isBinary ? binaryOperators : unaryPrefixOperators).get(name);
		final ASTElement parent = this.parent;
		if (s == null || parent == null)
			return null;
		return parent.getIRContext().getTypeDefinition("lang", s[0]).getAttributeByName(s[1]);
	}
	
	public static ASTOperatorLink parse(final Parser parent, final boolean isBinary, final char... symbols) {
		return ASTLink.parse(new ASTOperatorLink(isBinary), parent, q -> q.try2(symbols));
	}
	
	public static ASTOperatorLink parse(final Parser parent, final boolean isBinary, final String... symbols) {
		return ASTLink.parse(new ASTOperatorLink(isBinary), parent, q -> q.try2(symbols));
	}
	
	public static @Nullable ASTOperatorLink tryParse(final Parser parent, final boolean isBinary, final char... symbols) {
		return ASTLink.tryParse(new ASTOperatorLink(isBinary), parent, q -> q.try2(symbols));
	}
	
	public static @Nullable ASTOperatorLink tryParse(final Parser parent, final boolean isBinary, final String... symbols) {
		return ASTLink.tryParse(new ASTOperatorLink(isBinary), parent, q -> q.try2(symbols));
	}
	
}
