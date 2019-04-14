package ch.njol.tome.util;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ir.IRGenericArguments;
import ch.njol.tome.ir.definitions.IRGenericTypeDefinition;
import ch.njol.tome.ir.definitions.IRTypeDefinition;
import ch.njol.tome.ir.expressions.IRExpression;
import ch.njol.tome.ir.uses.IRSimpleTypeUse;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.ir.uses.IRTypeUseWithGenerics;

public final class IRUtils {
	
	private IRUtils() {}
	
	/**
	 * Tries to extract the {@code X} from a type {@code Type<X>}
	 * 
	 * @param use A type use (may be anything)
	 * @return If the given type use is of the form {@code Type<X>}, returns {@code X}, otherwise {@code null}.
	 */
	public static @Nullable IRTypeUse extractTypeType(final IRTypeUse use) {
		if (use instanceof IRTypeUseWithGenerics) {
			// if 'Type<X>', return 'X'
			final IRTypeUse base = ((IRTypeUseWithGenerics) use).getBaseType();
			final IRTypeDefinition typeTypeDefinition = use.getIRContext().getTypeDefinition("lang", "Type");
			if (!base.equalsType(typeTypeDefinition.getUse()))
				return null;
			// Type<...> at this point
			final IRGenericArguments generics = ((IRTypeUseWithGenerics) use).getGenericArguments();
			IRGenericTypeDefinition genericType = typeTypeDefinition.getGenericTypeDefinitionByName("T");
			if (genericType == null)
				return null;
			final IRExpression value = generics.getValueArgument(genericType);
			if (value instanceof IRTypeUse)
				return (IRTypeUse) value;
			return use.getIRContext().getTypeUse("lang", "Any");
		} else if (use instanceof IRSimpleTypeUse) {
			// if just 'Type' with no parameters, return 'Any'
			final IRTypeDefinition typeDefinition = ((IRSimpleTypeUse) use).getDefinition();
			final IRTypeDefinition typeTypeDefinition = use.getIRContext().getTypeDefinition("lang", "Type");
			if (typeDefinition.equalsType(typeTypeDefinition))
				return use.getIRContext().getTypeUse("lang", "Any"); // TODO make constants for all the strings used here
		}
		return null;
	}
	
}
