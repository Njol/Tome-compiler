package ch.njol.tome.compiler;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.common.Invalidatable;
import ch.njol.tome.common.InvalidateListener;
import ch.njol.tome.common.ModuleIdentifier;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.definitions.IRTypeDefinition;
import ch.njol.tome.moduleast.ASTModule;

public class Modules implements InvalidateListener {
	
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
		mod.registerInvalidateListener(this);
		if (oldMod != null) {
			oldMod.removeInvalidateListener(this);
			oldMod.invalidateSubtree();
		}
	}
	
	public @Nullable ASTModule get(final ModuleIdentifier id) {
		return modules.get(id);
	}
	
	public void unregister(final ASTModule module) {
		if (modules.get(module.id) == module) {
			modules.remove(module.id);
			module.removeInvalidateListener(this);
			module.invalidateSubtree();
		}
	}
	
	@Override
	public void onInvalidate(final Invalidatable source) {
		if (source instanceof ASTModule) {
			unregister((ASTModule) source);
		}
	}
	
	public @Nullable IRTypeDefinition getType(final String module, final String name) {
		final ASTModule m = get(new ModuleIdentifier(module));
		return m == null ? null : m.getType(name);
	}
	
}
