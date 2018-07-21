package ch.njol.tome.util;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.interpreter.InterpretedObject;
import ch.njol.tome.interpreter.InterpretedTypeUse;
import ch.njol.tome.ir.IRGenericArgument;
import ch.njol.tome.ir.IRValueGenericArgument;
import ch.njol.tome.ir.definitions.IRAttributeDefinition;
import ch.njol.tome.ir.definitions.IRTypeDefinition;
import ch.njol.tome.ir.uses.IRGenericTypeUse;
import ch.njol.tome.ir.uses.IRSimpleTypeUse;
import ch.njol.tome.ir.uses.IRTypeUse;

public final class IRUtils {
	private IRUtils() {}
	
	/**
	 * Tries to extract the {@code X} from a type {@code Type<X>}
	 * 
	 * @param use A type use (may be anything)
	 * @return If the given type use is of the form {@code Type<X>}, returns {@code X}, otherwise {@code null}.
	 */
	public static @Nullable IRTypeUse extractTypeType(final IRTypeUse use) {
		if (use instanceof IRGenericTypeUse) {
			final IRTypeUse base = ((IRGenericTypeUse) use).getBaseType();
			final IRTypeDefinition typeTypeDefinition = use.getIRContext().getTypeDefinition("lang", "Type");
			if (!base.equalsType(typeTypeDefinition.getUse()))
				return null;
			final Map<IRAttributeDefinition, IRGenericArgument> generics = ((IRGenericTypeUse) use).getGenericArguments();
			final IRGenericArgument argument = generics.get(typeTypeDefinition.getAttributeByName("T"));
			if (argument instanceof IRValueGenericArgument) {
				final InterpretedObject value = ((IRValueGenericArgument) argument).getValue();
				if (value instanceof InterpretedTypeUse)
					return ((InterpretedTypeUse) value).irType();
			}
			return use.getIRContext().getTypeUse("lang", "Any");
		} else if (use instanceof IRSimpleTypeUse) {
			final IRTypeDefinition typeDefinition = ((IRSimpleTypeUse) use).getDefinition();
			final IRTypeDefinition typeTypeDefinition = use.getIRContext().getTypeDefinition("lang", "Type");
			if (typeDefinition.equalsType(typeTypeDefinition))
				return use.getIRContext().getTypeUse("lang", "Any");
		}
		return null;
	}
}
