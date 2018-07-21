package ch.njol.tome.interpreter;

import java.util.Collections;

import ch.njol.tome.common.Kleenean;
import ch.njol.tome.compiler.Modules;
import ch.njol.tome.ir.definitions.IRBrokkrInterfaceDefinition;
import ch.njol.tome.ir.definitions.IRTypeDefinition;
import ch.njol.tome.ir.uses.IRTypeUse;

public class Interpreter {
	
	private final Modules modules;
	
	public Interpreter(final Modules modules) {
		this.modules = modules;
	}
	
	// should only be used for standard types
	public IRTypeDefinition getType(final String module, final String name) throws InterpreterException {
		final IRTypeDefinition type = modules.getType(module, name);
		if (type == null)
			throw new InterpreterException("Missing type '" + name + "' from module '" + module + "'");
		return type;
	}
	
	public IRTypeUse getTypeUse(final String module, final String name) throws InterpreterException {
		return getType(module, name).getGenericUse(Collections.EMPTY_MAP);
	}
	
	public IRBrokkrInterfaceDefinition getInterface(final String module, final String name) throws InterpreterException {
		final IRTypeDefinition type = getType(module, name);
		if (!(type instanceof IRBrokkrInterfaceDefinition))
			throw new InterpreterException("Type '" + name + "' from module '" + module + "' is not an interface");
		return (IRBrokkrInterfaceDefinition) type;
	}
	
	public IRTypeDefinition getTypeType() throws InterpreterException {
		return getType("lang", "Type");
	}
	
	@SuppressWarnings("null")
	public InterpretedObject kleenean(final Kleenean value) throws InterpreterException {
		final IRBrokkrInterfaceDefinition kleeneanType = getInterface("lang", "Kleenean");
		return kleeneanType.getAttributeByName("" + value).interpretDispatched(null, Collections.EMPTY_MAP, false);
	}
	
	@SuppressWarnings("null")
	public InterpretedObject bool(final boolean value) throws InterpreterException {
		final IRBrokkrInterfaceDefinition booleanType = getInterface("lang", "Boolean");
		return booleanType.getAttributeByName("" + value).interpretDispatched(null, Collections.EMPTY_MAP, false);
	}
	
//	public IRObject newTuple(Stream<IRNativeType> types, Stream<String> names) {
//		// TODO Auto-generated method stub
//		return null;
//	}
	
	public InterpretedObject stringConstant(final String value) throws InterpreterException {
		throw new InterpreterException("not implemented");
	}
	
}
