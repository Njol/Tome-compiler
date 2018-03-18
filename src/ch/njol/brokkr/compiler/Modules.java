package ch.njol.brokkr.compiler;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.common.ModuleIdentifier;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.definitions.IRTypeDefinition;

public class Modules {
	
	private final Map<ModuleIdentifier, Module> modules = new HashMap<>();
	
//	public final Interpreter interpreter = new Interpreter(this);
	public final IRContext irContext = new IRContext(this);
	
	@SuppressWarnings({"null", "unused"})
	public void register(final Module mod) {
		if (mod.id == null)
			return;
		modules.put(mod.id, mod);
	}
	
	public @Nullable Module get(final ModuleIdentifier id) {
		return modules.get(id);
	}
	
	public void unregister(final Module module) {
		if (modules.get(module.id) == module)
			modules.remove(module.id);
	}
	
	public @Nullable IRTypeDefinition getType(final String module, final String name) {
		final Module m = get(new ModuleIdentifier(module));
		return m == null ? null : m.getType(name);
	}
	
}
