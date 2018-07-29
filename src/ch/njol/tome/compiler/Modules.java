package ch.njol.tome.compiler;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.common.Modifiable;
import ch.njol.tome.common.ModificationListener;
import ch.njol.tome.common.ModuleIdentifier;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.definitions.IRTypeDefinition;
import ch.njol.tome.moduleast.ASTModule;

public class Modules implements ModificationListener {
	
	private final Map<ModuleIdentifier, ASTModule> modules = new HashMap<>();
	
//	public final Interpreter interpreter = new Interpreter(this);
	public final IRContext irContext = new IRContext(this);
	
	private final String description;
	
	public Modules(String description) {
		this.description = description;
	}
	
	@Override
	public String toString() {
		return description;
	}
	
	public void register(final ASTModule mod) {
		final ModuleIdentifier id = mod.id;
		if (id == null)
			return;
		ASTModule oldMod = modules.put(id, mod);
		mod.addModificationListener(this);
		if (oldMod != null) {
			oldMod.removeModificationListener(this);
			oldMod.invalidateSubtree();
		}
	}
	
	public @Nullable ASTModule get(final ModuleIdentifier id) {
		return modules.get(id);
	}
	
	public void unregister(final ASTModule module) {
		if (modules.get(module.id) == module) {
			modules.remove(module.id);
			module.removeModificationListener(this);
			module.invalidateSubtree();
		}
	}
	
	@Override
	public void onModification(final Modifiable source) {
		if (source instanceof ASTModule) {
			unregister((ASTModule) source);
		}
	}
	
	public @Nullable IRTypeDefinition getType(final String module, final String name) {
		final ASTModule m = get(new ModuleIdentifier(module));
		return m == null ? null : m.getType(name);
	}
	
}
