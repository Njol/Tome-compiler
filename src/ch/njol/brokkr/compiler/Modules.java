package ch.njol.brokkr.compiler;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.ast.Interfaces.TypeDeclaration;
import ch.njol.brokkr.compiler.ast.TopLevelElements.ModuleIdentifier;
import ch.njol.brokkr.interpreter.Interpreter;
import ch.njol.brokkr.interpreter.definitions.InterpretedNativeTypeDefinition;

public class Modules {
	
	private final Map<ModuleIdentifier, Module> modules = new HashMap<>();
	
	public final Interpreter interpreter = new Interpreter(this);
	
	@SuppressWarnings("null")
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
	
	public @Nullable InterpretedNativeTypeDefinition getType(String module, String name) {
		Module m = get(new ModuleIdentifier(module));
		return m == null ? null : m.getType(name);
	}
	
}
