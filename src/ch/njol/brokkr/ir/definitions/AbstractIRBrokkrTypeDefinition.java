package ch.njol.brokkr.ir.definitions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTElementPart;
import ch.njol.brokkr.ast.ASTInterfaces.ASTMember;
import ch.njol.brokkr.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.brokkr.ast.ASTTopLevelElements.ASTBrokkrFile;
import ch.njol.brokkr.ast.ASTTopLevelElements.ASTModuleDeclaration;
import ch.njol.brokkr.ast.ASTTopLevelElements.ASTModuleIdentifier;
import ch.njol.brokkr.common.ModuleIdentifier;
import ch.njol.brokkr.compiler.SourceCodeLinkable;
import ch.njol.brokkr.ir.AbstractIRElement;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.uses.IRMemberUse;
import ch.njol.brokkr.ir.uses.IRTypeUse;
import ch.njol.brokkr.ir.uses.IRUnknownTypeUse;

public abstract class AbstractIRBrokkrTypeDefinition extends AbstractIRElement implements IRBrokkrTypeDefinition, SourceCodeLinkable {
	
	protected final ASTTypeDeclaration ast;
	
	public AbstractIRBrokkrTypeDefinition(final ASTTypeDeclaration ast) {
		this.ast = registerDependency(ast);
		final ASTBrokkrFile astBrokkrFile = ast.getParentOfType(ASTBrokkrFile.class);
		registerDependency(astBrokkrFile != null && astBrokkrFile.moduleDeclaration != null ? astBrokkrFile.moduleDeclaration : astBrokkrFile);
	}
	
	@Override
	public @Nullable ModuleIdentifier module() {
		final ASTBrokkrFile brokkrFile = ast.getParentOfType(ASTBrokkrFile.class);
		if (brokkrFile == null)
			return null;
		final ASTModuleDeclaration moduleDeclaration = brokkrFile.moduleDeclaration;
		if (moduleDeclaration == null)
			return null;
		final ASTModuleIdentifier moduleIdentifier = moduleDeclaration.module;
		if (moduleIdentifier == null)
			return null;
		return moduleIdentifier.identifier;
	}
	
	@Override
	public String name() {
		return "" + ast.name();
	}
	
	@Override
	public IRContext getIRContext() {
		return ast.getIRContext();
	}
	
//	@Override
//	public @Nullable IRTypeDefinition declaringType() {
//		final ASTTypeDeclaration declaringType = declaration.getParentOfType(ASTTypeDeclaration.class);
//		return declaringType != null ? declaringType.getIR() : null;
//	}
	
	@Override
	public String toString() {
		final ModuleIdentifier moduleIdentifier = module();
		return (moduleIdentifier != null ? moduleIdentifier : "<unknown module>") + "." + name();
	}
	
//	@Override
//	public boolean isStatic() {
//		return true;
//	}

//	@Override
//	public @Nullable IRMemberUse getUse(@Nullable final IRTypeUse targetType, final Map<IRGenericTypeDefinition, IRTypeUse> genericArguments) {
//		return null;
//	}
	
	@SuppressWarnings("null")
	@Override
	public ASTElementPart getLinked() {
		return ast.nameToken();
	}
	
	private @Nullable List<IRMemberRedefinition> members = null;
	
	@SuppressWarnings("null")
	@Override
	public List<IRMemberRedefinition> members() {
		if (members == null) {
			members = new ArrayList<>();
			// declared (and possibly overridden) members
			for (final ASTMember m : ast.declaredMembers()) {
				members.addAll(m.getIRMembers());
			}
			// inherited members
			for (final IRMemberUse mu : ast.parentTypes().members()) {
				final IRMemberRedefinition redefinition = mu.redefinition();
				if (members.stream().anyMatch(m -> m.isRedefinitionOf(redefinition)))
					continue; // don't add overridden members twice
				members.add(mu.getRedefinitionFor(this));
			}
		}
		return members;
	}
	
	@Override
	public Set<? extends IRTypeUse> allInterfaces() {
		if (new ModuleIdentifier("lang").equals(module()) && "Any".equals(ast.name())) // if this is Any, then return empty set
			return Collections.EMPTY_SET;
		final IRTypeUse parentTypes = ast.parentTypes();
		if (parentTypes == null) // no explicit parent types - return Any
			return Collections.singleton(getIRContext().getTypeUse("lang", "Any"));
		try {
			return parentTypes.allInterfaces();
		} catch (final StackOverflowError e) {
			// FIXME make a proper check for inheritance loops
			return Collections.singleton(new IRUnknownTypeUse(getIRContext()));
		}
	}
	
	@Override
	public int compareTo(final IRTypeDefinition other) {
		if (other instanceof AbstractIRBrokkrTypeDefinition) {
			final ModuleIdentifier id1 = module(), id2 = ((AbstractIRBrokkrTypeDefinition) other).module();
			if (id1 != null && id2 != null) {
				final int c = id1.compareTo(id2);
				if (c != 0)
					return c;
			} else if (id1 != null && id2 == null) {
				return 1;
			} else if (id1 == null && id2 != null) {
				return -1;
			}
			return name().compareTo(((AbstractIRBrokkrTypeDefinition) other).name());
		}
		return IRTypeDefinition.compareTypeDefinitionClasses(this.getClass(), other.getClass());
	}
	
}
