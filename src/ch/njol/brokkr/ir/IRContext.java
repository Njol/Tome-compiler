package ch.njol.brokkr.ir;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.common.ModuleIdentifier;
import ch.njol.brokkr.compiler.Modules;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeObject;
import ch.njol.brokkr.ir.definitions.IRBrokkrTypeDefinition;
import ch.njol.brokkr.ir.definitions.IRMemberRedefinition;
import ch.njol.brokkr.ir.definitions.IRTypeDefinition;
import ch.njol.brokkr.ir.nativetypes.internal.IRNativeTypeClassDefinition;
import ch.njol.brokkr.ir.uses.IRTypeUse;

public class IRContext {
	
	public final Modules modules;
	
	/**
	 * Creates a new, empty IR context. Useful only for cases where the context just cannot be found, e.g. if a file has no module declaration.
	 */
	public IRContext() {
		this(new Modules());
	}
	
	public IRContext(final Modules modules) {
		this.modules = modules;
	}
	
	public IRTypeUse getTypeUse(final String module, final String name) {
		return getTypeDefinition(module, name).getRawUse();
	}
	
	public IRTypeDefinition getTypeDefinition(final String module, final String name) {
		final IRTypeDefinition type = modules.getType(module, name);
		if (type != null)
			return type;
		return new IRUnresolvedTypeDefinition(this, module, name);
	}
	
	public Map<Class<? extends InterpretedNativeObject>, IRNativeTypeClassDefinition> nativeTypeClassCache = new HashMap<>();
	
	public final static class IRUnresolvedTypeDefinition extends AbstractIRElement implements IRBrokkrTypeDefinition {
		private final IRContext irContext;
		private final ModuleIdentifier module;
		private final String name;
		
		private IRUnresolvedTypeDefinition(final IRContext irContext, final ModuleIdentifier module, final String name) {
			this.irContext = irContext;
			this.module = module;
			this.name = name;
		}
		
		private IRUnresolvedTypeDefinition(final IRContext irContext, final String module, final String name) {
			this(irContext, new ModuleIdentifier(module), name);
		}
		
		@Override
		public @Nullable ModuleIdentifier module() {
			return module;
		}
		
		@Override
		public String name() {
			return name;
		}
		
		@Override
		public IRContext getIRContext() {
			return irContext;
		}
		
		@Override
		public @NonNull String toString() {
			return "unresolvable type [" + module + "." + name + "]";
		}
		
		@Override
		public List<? extends IRMemberRedefinition> members() {
			return Collections.EMPTY_LIST;
		}
		
		@Override
		public Set<? extends IRTypeUse> allInterfaces() {
			return module.equals(new ModuleIdentifier("lang")) && name.equals("Any")
					? Collections.EMPTY_SET
					: Collections.singleton(irContext.getTypeUse("lang", "Any"));
		}
		
		@Override
		public int compareTo(final IRTypeDefinition other) {
			if (other instanceof IRUnresolvedTypeDefinition) {
				final int c = module.compareTo(((IRUnresolvedTypeDefinition) other).module);
				if (c != 0)
					return c;
				return name.compareTo(((IRUnresolvedTypeDefinition) other).name);
			}
			return IRTypeDefinition.compareTypeDefinitionClasses(this.getClass(), other.getClass());
		}
	}
	
}
