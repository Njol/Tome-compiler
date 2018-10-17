package ch.njol.tome.ast.expressions;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTElement;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeExpression;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeUse;
import ch.njol.tome.ast.ASTLink;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.ir.uses.IRAttributeUse;
import ch.njol.tome.ir.uses.IRGenericTypeAccess;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.ir.uses.IRUnknownTypeUse;
import ch.njol.tome.parser.Parser;

/**
 * Access to a generic type of a type, e.g. 'A.B'.
 * TODO is this the same as a static attribute access? and is this allowed on objects (and not only types) too?
 */
public class ASTGenericTypeAccess extends AbstractASTElement implements ASTTypeExpression {
	
	public final ASTTypeUse target;
	
	private @Nullable ASTGenericTypeAccessLink genericType;
	
	private static class ASTGenericTypeAccessLink extends ASTLink<IRAttributeUse> {
		
		@Override
		protected @Nullable IRAttributeUse tryLink(String name) {
			ASTElement parent = this.parent;
			if (parent == null || !(parent instanceof ASTGenericTypeAccess))
				return null;
			return ((ASTGenericTypeAccess) parent).target.getIR().getAttributeByName(name);
		}
		
		private static ASTGenericTypeAccessLink parse(Parser parent) {
			return parseAsTypeIdentifier(new ASTGenericTypeAccessLink(), parent);
		}
		
	}
	
	public ASTGenericTypeAccess(final ASTTypeUse target) {
		this.target = target;
	}
	
	@Override
	public String toString() {
		return target + "." + genericType;
	}
	
	static ASTGenericTypeAccess finishParsing(final Parser p, final ASTTypeUse target) {
		final ASTGenericTypeAccess ast = new ASTGenericTypeAccess(target);
		p.one('.'); // TODO create a "one" that asserts that it succeeds? (only to be used if it should have been peek()ed before)
		ast.genericType = ASTGenericTypeAccessLink.parse(p);
		if (p.peekNext('.')) {
			Parser newParent = p.startNewParent();
			p.done(ast);
			return finishParsing(newParent, ast);
		}
		return p.done(ast);
	}
	
	@Override
	public IRTypeUse getIR() {
		final IRAttributeUse attr = genericType != null ? genericType.get() : null;
		if (attr == null)
			return new IRUnknownTypeUse(getIRContext());
		final IRTypeUse t = target.getIR();
		return new IRGenericTypeAccess(t, attr);
	}
	
}
