package ch.njol.tome.ast.expressions;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeExpression;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeUse;
import ch.njol.tome.ast.ASTInterfaces.TypedASTElement;
import ch.njol.tome.ast.expressions.ASTLambda.ASTLambdaParameter;
import ch.njol.tome.compiler.Token;
import ch.njol.tome.compiler.Token.CommentToken;
import ch.njol.tome.compiler.Token.LowercaseWordToken;
import ch.njol.tome.compiler.Token.NumberToken;
import ch.njol.tome.compiler.Token.StringToken;
import ch.njol.tome.compiler.Token.SymbolToken;
import ch.njol.tome.compiler.Token.UppercaseWordToken;
import ch.njol.tome.compiler.Token.WhitespaceToken;
import ch.njol.tome.compiler.Token.WordOrSymbols;
import ch.njol.tome.parser.Parser;

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
			return ASTLambda.parse(parent);
		if (parent.peekNext() instanceof LowercaseWordToken && parent.peekNext("->", 1, true))
			return ASTLambda.parse(parent);
		if (parent.peekNext('[')) {
			int i = 0;
			boolean foundClosing = false;
			for (Token t; (t = parent.peekNext(i, false)) != null; i++) {
				if (t instanceof SymbolToken && ((SymbolToken) t).symbol == ']') {
					foundClosing = true;
					break;
				}
			}
			if (foundClosing) {
				i++;
				for (Token t; (t = parent.peekNext(i, false)) != null; i++) {
					if (!(t instanceof WhitespaceToken || t instanceof CommentToken)) {
						if (parent.peekNext("->", i, false)) {
							return ASTLambda.parse(parent);
						} else {
							break;
						}
					}
					i++;
				}
			}
		}
		
		final Parser p = parent.start();
		final ASTExpression expr = ASTOperatorExpression.parse(p, allowComparisons);
		SymbolToken sym;
		SymbolToken plainAssignmentToken = p.try2('=');
		final ASTOperatorLink assignmentOp = plainAssignmentToken != null ? null : ASTOperatorLink.tryParse(p, true, "+=", "-=", "*=", "/=", "&=", "|=");
		if (plainAssignmentToken != null || assignmentOp != null) {
			WordOrSymbols assignmentOpToken = assignmentOp != null ? assignmentOp.getNameToken() : plainAssignmentToken;
			assert assignmentOpToken != null;
			if (expr instanceof ASTVariableOrUnqualifiedAttributeUse) {
				final ASTVariableOrUnqualifiedAttributeUse varOrAttribute = (ASTVariableOrUnqualifiedAttributeUse) expr;
				if (!varOrAttribute.arguments.isEmpty()) {
					p.errorFatal("Left-hand side of an assignment must be a variable or a field", expr.absoluteRegionStart(), expr.regionLength());
					p.doneAsChildren();
					return expr;
				}
				return ASTLocalVariableOrUnqualifiedAttributeAssignment.finishParsing(p, varOrAttribute, assignmentOp, assignmentOpToken);
			} else if (expr instanceof ASTAccessExpression) {// && ((ASTAccessExpression) expr).access instanceof ASTDirectAttributeAccess) {
				final ASTAccessExpression e = (ASTAccessExpression) expr;
				final ASTDirectAttributeAccess da = e.access;
				assert da != null;
				if (e.meta || e.nullSafe /*|| da.negated*/ || da.allResults || !da.arguments.isEmpty()) {
					p.errorFatal("Left-hand side of an assignment must be a variable or a field", expr.absoluteRegionStart(), expr.regionLength());
					p.doneAsChildren();
					return expr;
				}
				p.unparse(e);
				p.unparse(da);
				return ASTAttributeAssignment.finishParsing(p, e.target, da.attributeLink, assignmentOp, assignmentOpToken);
			} else {
				if (expr.regionLength() > 0)
					p.errorFatal("Left-hand side of an assignment must be a variable or a field", expr.absoluteRegionStart(), expr.regionLength());
				p.doneAsChildren();
				return expr;
			}
		} else if ((sym = p.try2('?')) != null) { // no need to check for '?.' and '?~' as those are parsed before in OperatorExpression
			return ASTTernaryIf.finishParsing(p, expr, sym);
		} else if ((sym = p.try2('#')) != null) {
			return ASTErrorHandlingExpression.finishParsing(p, expr, sym);
		} else if (expr instanceof ASTTypeUse && p.peekNext() instanceof LowercaseWordToken && p.peekNext("->", 1, true)) {
			final Parser paramParser = p.start();
			final ASTLambdaParameter param = ASTLambdaParameter.finishParsing(paramParser, (ASTTypeUse) expr);
			return ASTLambda.finishParsing(p, param);
		} else {
			p.doneAsChildren();
			return expr;
		}
	}
	
	public static interface ASTAccess extends TypedASTElement {}
	
	public static interface ASTAtomicExpression extends ASTExpression {
		public static @Nullable ASTExpression parse(final Parser parent) {
			// peek is acceptable here, as nobody needs content assist for expression *syntax*.
			final Token next = parent.peekNext();
			if (next instanceof StringToken) {
				parent.next();
				return new ASTString((StringToken) next);
			}
			if (next instanceof NumberToken)
				return ASTNumberConstant.parse(parent);
			if (next instanceof UppercaseWordToken)
				return ASTTypeExpressions.parse(parent, false, false);
			if (parent.peekNext('('))
				return ASTParenthesesExpression.parse(parent);
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
	
	// ================================ Types ================================
	
	public static class ASTTypeExpressions {
		
		public static ASTTypeExpression parse(final Parser parent, final boolean allowOps, final boolean allowTuple) {
			return parse(parent, allowOps, allowTuple, allowOps);
		}
		
		public static ASTTypeExpression parse(final Parser parent, final boolean allowOps, final boolean allowTuple, final boolean allowDotGeneric) {
			assert !(!allowDotGeneric && allowOps) : "generics are automatically allowed when operators are";
			if (allowDotGeneric && !allowOps) { // if allowing ops, ops are done first
				final Parser p = parent.start();
				final ASTTypeExpression target = ASTTypeExpressions.parse(p, false, false, false); // TODO do tuples have generics? or should this just be allowed to then produce a better error message?
				if (p.peekNext('.'))
					return ASTGenericTypeAccess.finishParsing(p, target);
				p.doneAsChildren();
				return target;
			}
			if (allowTuple && parent.peekNext('[')) {
//				if (!allowTuple)
//					throw new ParseException("Expected a type that is not a tuple", parent.in.getOffset(), parent.in.getOffset() + 1);
				// not null, since it starts with '['
				return ASTTypeTuple.parse(parent);
			}
			if (!allowOps) { // i.e. only a single type (possibly with modifiers and generics)
				if (parent.peekNext("Self"))
					return ASTSelf.parse(parent);
				return ASTModifierTypeUse.parse(parent);
			}
			return ASTTypeUseWithOperators.parse(parent);
		}
	}
	
}
