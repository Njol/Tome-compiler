package ch.njol.brokkr.ir.definitions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTElementPart;
import ch.njol.brokkr.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.brokkr.ast.ASTMembers.ASTMember;
import ch.njol.brokkr.ast.ASTTopLevelElements.ASTBrokkrFile;
import ch.njol.brokkr.common.ModuleIdentifier;
import ch.njol.brokkr.compiler.Module;
import ch.njol.brokkr.ir.uses.IRMemberUse;
import ch.njol.brokkr.ir.uses.IRTypeUse;

public abstract class AbstractIRBrokkrTypeDefinition implements IRTypeDefinition, IRMemberDefinition {
	
	protected final ASTTypeDeclaration declaration;
	
	public AbstractIRBrokkrTypeDefinition(final ASTTypeDeclaration declaration) {
		this.declaration = declaration;
	}
	
	@Override
	public String name() {
		return "" + declaration.name();
	}
	
	@Override
	public @Nullable IRTypeDefinition declaringType() {
		final ASTTypeDeclaration declaringType = declaration.getParentOfType(ASTTypeDeclaration.class);
		return declaringType != null ? declaringType.getIR() : null;
	}
	
	@Override
	public String toString() {
		return ASTBrokkrFile.getModule(declaration) + "." + name();
	}
	
	@Override
	public boolean isStatic() {
		return true;
	}
	
	@Override
	public @Nullable IRMemberUse getUse(@Nullable final IRTypeUse targetType, final Map<IRGenericTypeDefinition, IRTypeUse> genericArguments) {
		return null;
	}
	
	@SuppressWarnings("null")
	@Override
	public ASTElementPart getLinked() {
		return declaration.nameToken();
	}
	
	private @Nullable List<IRMemberRedefinition> members = null;
	
	@SuppressWarnings("null")
	@Override
	public List<IRMemberRedefinition> members() {
		if (members == null) {
			members = new ArrayList<>();
			// declared (and possibly overridden) members
			for (final ASTMember m : declaration.declaredMembers()) {
				members.add(m.getIR());
			}
			// inherited members
			for (final IRMemberUse mu : declaration.parentTypes().members()) {
				final IRMemberRedefinition redefinition = mu.redefinition();
				if (members.stream().anyMatch(m -> m.isRedefinitionOf(redefinition)))
					continue; // don't add overridden members twice
				members.add(redefinition);
			}
		}
		return members;
	}
	
	@Override
	public boolean equalsType(final IRTypeDefinition other) {
		return this.getClass() == other.getClass() && declaration == ((AbstractIRBrokkrTypeDefinition) other).declaration;
	}
	
	@Override
	public int typeHashCode() {
		final Module module = ASTBrokkrFile.getModule(declaration);
		final ModuleIdentifier moduleIdentifier = module == null ? null : module.id;
		return (moduleIdentifier == null ? 0 : moduleIdentifier.hashCode() * 31) + name().hashCode();
	}
	
	@Override
	public boolean isSubtypeOfOrEqual(final IRTypeDefinition other) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean isSupertypeOfOrEqual(final IRTypeDefinition other) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
