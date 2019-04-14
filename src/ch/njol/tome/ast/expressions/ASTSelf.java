package ch.njol.tome.ast.expressions;

import ch.njol.tome.ast.ASTInterfaces.ASTTypeExpression;
import ch.njol.tome.ast.AbstractASTElementWithIR;
import ch.njol.tome.ir.uses.IRSelfTypeUse;
import ch.njol.tome.parser.Parser;

/**
 * The keyword 'Self', representing the class of the current object (i.e. equal to this.class, but can be used in more contexts like interfaces and generics)
 */
public class ASTSelf extends AbstractASTElementWithIR<IRSelfTypeUse> implements ASTTypeExpression<IRSelfTypeUse> {
	
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
		return parent.one(p -> {
			final ASTSelf ast = new ASTSelf();
			p.one("Self");
			return ast;
		});
	}
	
	@Override
	protected IRSelfTypeUse calculateIR() {
		return IRSelfTypeUse.makeNew(this);
	}
	
}
