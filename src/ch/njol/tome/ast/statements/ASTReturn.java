package ch.njol.tome.ast.statements;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTAttribute;
import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.ASTLink;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.ast.expressions.ASTExpressions;
import ch.njol.tome.ast.members.ASTAttributeDeclaration;
import ch.njol.tome.ast.statements.ASTStatements.ASTStatement;
import ch.njol.tome.compiler.Token.LowercaseWordToken;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.definitions.IRResultRedefinition;
import ch.njol.tome.ir.expressions.IRVariableAssignment;
import ch.njol.tome.ir.statements.IRExpressionStatement;
import ch.njol.tome.ir.statements.IRReturn;
import ch.njol.tome.ir.statements.IRStatement;
import ch.njol.tome.ir.statements.IRStatementList;
import ch.njol.tome.parser.Parser;
import ch.njol.util.StringUtils;

public class ASTReturn extends AbstractASTElement implements ASTStatement {
	private final List<ASTReturnResult> results = new ArrayList<>();
	private @Nullable ASTReturnErrorLink error;
	
	private static class ASTReturnErrorLink extends ASTLink<IRResultRedefinition> {
		@Override
		protected @Nullable IRResultRedefinition tryLink(String name) {
			final ASTAttributeDeclaration attribute = getParentOfType(ASTAttributeDeclaration.class);
			return attribute == null ? null : attribute.getResult(name);
		}
		
		private static ASTReturnErrorLink parse(Parser parent) {
			return parseAsVariableIdentifier(new ASTReturnErrorLink(), parent);
		}
	}
	
	@Override
	public String toString() {
		return "return " + StringUtils.join(results, ", ");
	}
	
	public static ASTReturn parse(final Parser parent, final boolean withReturn) {
		final Parser p = parent.start();
		final ASTReturn ast = new ASTReturn();
		if (withReturn)
			p.one("return");
		p.until(() -> {
			if (p.try_('#')) {
				ast.error = ASTReturnErrorLink.parse(p);
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
		if (error != null) {
			// TODO
		}
		final List<IRStatement> statements = new ArrayList<>();
		for (final ASTReturnResult r : results) {
			final IRResultRedefinition result = r.result != null ? r.result.get() : null;
			final ASTExpression value = r.value;
			if (result != null && value != null) {
				statements.add(new IRExpressionStatement(new IRVariableAssignment(result.definition(), value.getIR())));
			}
		}
		statements.add(new IRReturn(getIRContext()));
		return new IRStatementList(getIRContext(), statements);
	}
	
	public static class ASTReturnResult extends AbstractASTElement {
		private @Nullable ASTReturnResultLink result;
		public @Nullable ASTExpression value;
		
		private static class ASTReturnResultLink extends ASTLink<IRResultRedefinition> {
			@Override
			protected @Nullable IRResultRedefinition tryLink(String name) {
				final ASTAttribute fa = getParentOfType(ASTAttribute.class);
				if (fa == null)
					return null;
				final IRAttributeRedefinition attribute = fa.getIR();
				return attribute.getResultByName(name);
			}
			
			private static ASTReturnResultLink parse(Parser parent) {
				return parseAsVariableIdentifier(new ASTReturnResultLink(), parent);
			}
		}
		
		@Override
		public String toString() {
			return (result != null ? result + " " : "") + value;
		}
		
		public static ASTReturnResult parse(final Parser parent) {
			final Parser p = parent.start();
			final ASTReturnResult ast = new ASTReturnResult();
			if (p.peekNext() instanceof LowercaseWordToken && p.peekNext(':', 1, true)) {
				ast.result = ASTReturnResultLink.parse(p);
				p.next(); // skip ':'
			}
			// TODO what to link without a name?
			ast.value = ASTExpressions.parse(p);
			return p.done(ast);
		}
	}
	
}
