package ch.njol.tome.ast.expressions;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTElement;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeExpression;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeUse;
import ch.njol.tome.ast.ASTLink;
import ch.njol.tome.ast.AbstractASTElementWithIR;
import ch.njol.tome.ir.uses.IRGenericTypeUse;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.ir.uses.IRUnknownTypeUse;
import ch.njol.tome.parser.Parser;

/**
 * Access to a generic type of a type, e.g. 'A.B'.
 * TODO is this the same as a static attribute access? and is this allowed on objects (and not only types) too?
 */
public class ASTGenericTypeAccess extends AbstractASTElementWithIR<IRTypeUse> implements ASTTypeExpression<IRTypeUse> {
	
	public final ASTTypeUse<?> target;
	
	private @Nullable ASTGenericTypeAccessLink genericType;
	
	private static class ASTGenericTypeAccessLink extends ASTLink<IRGenericTypeUse> {
		
		@Override
		protected @Nullable IRGenericTypeUse tryLink(final String name) {
			final ASTElement parent = this.parent;
			if (parent == null || !(parent instanceof ASTGenericTypeAccess))
				return null;
			return ((ASTGenericTypeAccess) parent).target.getIR().getGenericTypeByName(name);
		}
		
		private static ASTGenericTypeAccessLink parse(final Parser parent) {
			return parseAsTypeIdentifier(new ASTGenericTypeAccessLink(), parent);
		}
		
	}
	
	public ASTGenericTypeAccess(final ASTTypeUse<?> target) {
		this.target = target;
	}
	
	@Override
	public String toString() {
		return target + "." + genericType;
	}
	
	static ASTGenericTypeAccess finishParsing(final Parser p, final ASTTypeUse<?> target) {
		final ASTGenericTypeAccess ast = new ASTGenericTypeAccess(target);
		p.one('.'); // TODO create a "one" that asserts that it succeeds? (only to be used if it should have been peek()ed before)
		ast.genericType = ASTGenericTypeAccessLink.parse(p);
		if (p.peekNext('.')) {
			final Parser newParent = p.startNewParent();
			p.done(ast);
			return finishParsing(newParent, ast);
		}
		return p.done(ast);
	}
	
	@Override
	protected IRTypeUse calculateIR() {
		final IRGenericTypeUse gt = genericType != null ? genericType.get() : null;
		if (gt == null)
			return new IRUnknownTypeUse(getIRContext());
		return gt;
	}
	
}
