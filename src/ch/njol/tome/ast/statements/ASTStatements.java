package ch.njol.tome.ast.statements;

import ch.njol.tome.ast.ASTElement;
import ch.njol.tome.ast.ASTInterfaces.ASTElementWithIR;
import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeUse;
import ch.njol.tome.ast.expressions.ASTAccessExpression;
import ch.njol.tome.ast.expressions.ASTDirectAttributeAccess;
import ch.njol.tome.ast.expressions.ASTExpressions;
import ch.njol.tome.ast.expressions.ASTExpressions.ASTAccess;
import ch.njol.tome.ast.expressions.ASTVariableOrUnqualifiedAttributeUse;
import ch.njol.tome.ast.members.ASTPostcondition;
import ch.njol.tome.ast.members.ASTPrecondition;
import ch.njol.tome.compiler.Token.CodeGenerationToken;
import ch.njol.tome.ir.statements.IRStatement;
import ch.njol.tome.parser.Parser;

public class ASTStatements {
	
	public static interface ASTStatement<IR extends IRStatement> extends ASTElementWithIR<IR> {
		
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
			
			final Parser p = parent.start();
			final ASTExpression<?> expr = ASTExpressions.parse(p);
			if (p.peekNext(';'))
				return ASTExpressionStatement.finishParsing(p, expr);
			if (allowExpressionAtEnd && p.peekNext() == null) {
				p.doneAsChildren();
				return expr;
			}
			if (expr instanceof ASTTypeUse) {
				// variable declaration
				return ASTVariableDeclarations.finishParsing(p, (ASTTypeUse<?>) expr);
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
		
		public static ASTStatement<?> parse(final Parser parent) {
			return (ASTStatement<?>) parse_(parent, false);
		}
		
		// TODO remove
//		public default void interpret(final InterpreterContext context) {
//			getIR().interpret(context);
//		}
		
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
	
}
