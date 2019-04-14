package ch.njol.tome.ir.uses;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTElementPart;
import ch.njol.tome.compiler.SourceCodeLinkable;
import ch.njol.tome.interpreter.InterpretedTypeUse;
import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.InterpreterException;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.IRElement;
import ch.njol.tome.ir.definitions.IRGenericTypeDefinition;

public class IRGenericTypeAccess extends AbstractIRTypeUse implements SourceCodeLinkable {
	
	private final IRTypeUse target;
	private final IRGenericTypeDefinition genericType;
	
	public IRGenericTypeAccess(final IRTypeUse target, final IRGenericTypeDefinition genericType) {
		IRElement.assertSameIRContext(target, genericType);
		this.target = target;
		this.genericType = genericType;
	}
	
	@Override
	public IRContext getIRContext() {
		return target.getIRContext();
	}
	
	@Override
	public int typeHashCode() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public boolean equalsType(final IRTypeUse other) {
		return compareTo(other) == 0;
	}
	
	@Override
	public int compareTo(final IRTypeUse other) {
		if (other instanceof IRGenericTypeAccess) {
			final IRGenericTypeAccess o = (IRGenericTypeAccess) other;
			final int c = target.compareTo(o.target);
			if (c != 0)
				return c;
			return genericType.compareTo(o.genericType);
		}
		return IRTypeUse.compareTypeUseClasses(this.getClass(), other.getClass());
	}
	
	@Override
	public boolean isSubtypeOfOrEqual(final IRTypeUse other) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean isSupertypeOfOrEqual(final IRTypeUse other) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public Set<? extends IRTypeUse> allInterfaces() {
		IRTypeUse extended = genericType.extends_();
		return extended != null ? extended.allInterfaces() : Collections.EMPTY_SET;
	}
	
	@Override
	public List<? extends IRMemberUse> members() {
		IRTypeUse extended = genericType.extends_();
		return extended != null ? extended.members() : Collections.EMPTY_LIST;
	}
	
	@Override
	public InterpretedTypeUse interpret(final InterpreterContext context) throws InterpreterException {
//		if (typeUse == null)
//			throw new InterpreterException("Invalid generic type access");
//		final InterpretedTypeUse interpretedTarget = target.interpret(context);
//		if (interpretedTarget instanceof InterpretedNormalObject) {
//			final InterpretedObject attributeValue = ((InterpretedNormalObject) interpretedTarget).getAttributeValue(attribute.definition());
//			if (attributeValue instanceof InterpretedTypeUse)
//				return (InterpretedTypeUse) attributeValue;
//			throw new InterpreterException("Type attribute did not return a Type");
//		}
//		throw new InterpreterException("base type of generic type access is not a normal type");
		throw new InterpreterException("not implemented");
	}
	
	@Override
	public String toString() {
		if (target instanceof IRSelfTypeUse)
			return genericType.name();
		return target + "." + genericType.name();
	}
	
	@Override
	public @Nullable ASTElementPart getLinked() {
		return genericType.getLinked();
	}
	
}
