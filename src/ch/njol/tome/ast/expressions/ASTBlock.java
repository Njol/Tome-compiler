package ch.njol.tome.ast.expressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTElement;
import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.AbstractASTElementWithIR;
import ch.njol.tome.ast.statements.ASTStatements.ASTStatement;
import ch.njol.tome.ir.expressions.IRBlock;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.ir.uses.IRUnknownTypeUse;
import ch.njol.tome.parser.Parser;

public class ASTBlock extends AbstractASTElementWithIR<IRBlock> implements ASTExpression<IRBlock> {
	
	public @Nullable ASTExpression<?> expression;
	public List<ASTStatement<?>> statements = new ArrayList<>();
	
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
	
	public ASTBlock(final @NonNull ASTStatement<?>... statements) {
		this.statements.addAll(Arrays.asList(statements));
	}
	
	@Override
	public String toString() {
		return "{...}";
	}
	
	public static ASTBlock parse(final Parser parent) {
		return parent.one(p -> {
			final ASTBlock ast = new ASTBlock();
			p.oneRepeatingGroup('{', () -> {
				if (ast.statements.isEmpty()) {
					final ASTElement e = ASTStatement.parseWithExpression(p);
					if (e instanceof ASTExpression)
						ast.expression = (ASTExpression<?>) e;
					else
						ast.statements.add((ASTStatement<?>) e);
				} else {
					ast.statements.add(ASTStatement.parse(p));
					assert ast.expression == null;
				}
			}, '}');
			return ast;
		});
	}
	
	@Override
	protected IRBlock calculateIR() {
//		if (expression != null)
//			return expression.getIR(); // FIXME actually returns a block like [[ {return expression;} ]]
		return new IRBlock(getIRContext(), statements.stream().map(s -> s.getIR()));
	}
	
}
