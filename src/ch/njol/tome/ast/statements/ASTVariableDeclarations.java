package ch.njol.tome.ast.statements;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.ASTInterfaces.ASTLocalVariable;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeUse;
import ch.njol.tome.ast.AbstractASTElementWithIR;
import ch.njol.tome.ast.expressions.ASTExpressions;
import ch.njol.tome.ast.statements.ASTStatements.ASTStatement;
import ch.njol.tome.compiler.Token.LowercaseWordToken;
import ch.njol.tome.compiler.Token.WordToken;
import ch.njol.tome.ir.definitions.IRBrokkrLocalVariable;
import ch.njol.tome.ir.definitions.IRVariableRedefinition;
import ch.njol.tome.ir.statements.IRStatement;
import ch.njol.tome.ir.statements.IRStatementList;
import ch.njol.tome.ir.statements.IRVariableDeclaration;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.ir.uses.IRUnknownTypeUse;
import ch.njol.tome.parser.Parser;
import ch.njol.util.StringUtils;

public class ASTVariableDeclarations extends AbstractASTElementWithIR<IRStatement> implements ASTStatement<IRStatement> {
	
	public @Nullable ASTTypeUse<?> type;
	public List<ASTVariableDeclarationsVariable> variables = new ArrayList<>();
	
	public ASTVariableDeclarations() {}
	
	public ASTVariableDeclarations(final ASTTypeUse<?> type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return (type == null ? "var" : type) + " " + StringUtils.join(variables, ", ") + ";";
	}
	
	public static ASTVariableDeclarations parse(final Parser parent) {
		return finishParsing(parent.start(), null);
	}
	
	public static ASTVariableDeclarations finishParsing(final Parser p, @Nullable final ASTTypeUse<?> type) {
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
	protected IRStatement calculateIR() {
		return new IRStatementList(getIRContext(), variables.stream().map(v -> v.getIRDeclaration()).collect(Collectors.toList()));
	}
	
	public static class ASTVariableDeclarationsVariable extends AbstractASTElementWithIR<IRVariableRedefinition> implements ASTLocalVariable {
		
		public @Nullable LowercaseWordToken nameToken;
		public @Nullable ASTExpression<?> initialValue;
		
		@Override
		public @Nullable WordToken nameToken() {
			return nameToken;
		}
		
		@Override
		public String toString() {
			return nameToken + (initialValue == null ? "" : " = " + initialValue);
		}
		
		public static ASTVariableDeclarationsVariable parse(final Parser parent) {
			final Parser p = parent.start();
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
			final ASTTypeUse<?> typeUse = variableDeclarations.type;
			if (typeUse == null) {
				if (initialValue != null)
					return initialValue.getIRType();
				return new IRUnknownTypeUse(getIRContext()); // FIXME semantics of inferred types? just use the supertype of any assignment?
			}
			return typeUse.getIR();
		}
		
		@Override
		protected IRVariableRedefinition calculateIR() {
			return new IRBrokkrLocalVariable(this);
		}
		
		public IRVariableDeclaration getIRDeclaration() {
			return new IRVariableDeclaration(getIR());
		}
	}
	
}
