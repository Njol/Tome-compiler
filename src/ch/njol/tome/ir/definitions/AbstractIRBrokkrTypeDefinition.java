package ch.njol.tome.ir.definitions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTElementPart;
import ch.njol.tome.ast.ASTInterfaces.ASTGenericParameter;
import ch.njol.tome.ast.ASTInterfaces.ASTMember;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.tome.ast.ASTTopLevelElements.ASTSourceFile;
import ch.njol.tome.ast.ASTTopLevelElements.ASTModuleDeclaration;
import ch.njol.tome.ast.ASTTopLevelElements.ASTModuleIdentifier;
import ch.njol.tome.common.ModuleIdentifier;
import ch.njol.tome.compiler.SourceCodeLinkable;
import ch.njol.tome.ir.AbstractIRElement;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.uses.IRMemberUse;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.ir.uses.IRUnknownTypeUse;

public abstract class AbstractIRBrokkrTypeDefinition extends AbstractIRElement implements IRBrokkrTypeDefinition, SourceCodeLinkable {
	
	protected final ASTTypeDeclaration ast;
	
	public AbstractIRBrokkrTypeDefinition(final ASTTypeDeclaration ast) {
		this.ast = registerDependency(ast);
		final ASTSourceFile astBrokkrFile = ast.getParentOfType(ASTSourceFile.class);
		registerDependency(astBrokkrFile != null && astBrokkrFile.moduleDeclaration != null ? astBrokkrFile.moduleDeclaration : astBrokkrFile);
	}
	
	@Override
	public @Nullable ModuleIdentifier module() {
		final ASTSourceFile brokkrFile = ast.getParentOfType(ASTSourceFile.class);
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
		List<IRMemberRedefinition> members = this.members;
		if (members == null) {
			this.members = members = new ArrayList<>();
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

	private @Nullable List<IRAttributeRedefinition> positionalGenericParameters = null;
	
	@Override
	public List<IRAttributeRedefinition> positionalGenericParameters() {
		 List<IRAttributeRedefinition> positionalGenericParameters = this.positionalGenericParameters;
		if (positionalGenericParameters == null) {
			this.positionalGenericParameters = positionalGenericParameters = new ArrayList<>();
			for (final ASTGenericParameter gp : ast.genericParameters()) {
				IRAttributeRedefinition attribute = gp.declaration();
				if (attribute != null)
					positionalGenericParameters.add(attribute);
			}
		}
		return positionalGenericParameters;
	
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
