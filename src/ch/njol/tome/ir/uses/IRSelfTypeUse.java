package ch.njol.tome.ir.uses;

import java.util.List;
import java.util.Set;

import ch.njol.tome.ast.ASTElement;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.tome.interpreter.InterpretedTypeUse;
import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.InterpreterException;
import ch.njol.tome.ir.IRContext;

/**
 * The <code>Self</code> type, which has the following properties:
 * <ul>
 * <li>It is the class of the <code>this</code> object
 * <li>It is a class (follows from above)
 * <li>It is a subtype of or equal to the type it is used in (follows from above)
 * </ul>
 * Which class it actually represents is (usually) only known at runtime.
 */
public class IRSelfTypeUse extends AbstractIRTypeUse {
	
	private final IRTypeUse parent;
	
	/**
	 * @param parent The type where this Self type is used in. This also defines the supertype of this type.
	 */
	public IRSelfTypeUse(final IRTypeUse parent) {
		this.parent = parent;
	}

	/**
	 * Creates a new 'Self' expression as if it were written where the given AST element is, i.e. will create a 'Self' expression for the innermost enclosing type of the given AST
	 * element.
	 * If the given element is a type, a 'Self' expression of that type will be created.
	 * 
	 * @param ast
	 * @return
	 */
	public static IRSelfTypeUse makeNew(final ASTElement ast) {
		final ASTTypeDeclaration type = ast instanceof ASTTypeDeclaration ? (ASTTypeDeclaration) ast : ast.getParentOfType(ASTTypeDeclaration.class);
		return new IRSelfTypeUse(type == null ? new IRUnknownTypeUse(ast.getIRContext()) : type.getIR().getUse());
	}
	
	@Override
	public IRContext getIRContext() {
		return parent.getIRContext();
	}
	
	@Override
	public Set<? extends IRTypeUse> allInterfaces() {
		return parent.allInterfaces();
	}
	
	@Override
	public int typeHashCode() {
		return parent.typeHashCode();
	}
	
	@Override
	public boolean equalsType(final IRTypeUse other) {
		return other instanceof IRSelfTypeUse && parent.equalsType(((IRSelfTypeUse) other).parent);
	}
	
	@Override
	public int compareTo(final IRTypeUse other) {
		if (other instanceof IRSelfTypeUse) {
			return parent.compareTo(((IRSelfTypeUse) other).parent);
		}
		return IRTypeUse.compareTypeUseClasses(this.getClass(), other.getClass());
	}
	
	@Override
	public boolean isSubtypeOfOrEqual(final IRTypeUse other) {
		return parent.isSubtypeOfOrEqual(other);
	}
	
	@Override
	public boolean isSupertypeOfOrEqual(final IRTypeUse other) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public List<? extends IRMemberUse> members() {
		return parent.members();
	}
	
	@Override
	public InterpretedTypeUse interpret(final InterpreterContext context) throws InterpreterException {
		return context.getThisObject().nativeClass().interpret(context);
	}
	
	@Override
	public String toString() {
		return "Self (" + parent + ")";
	}
	
}
