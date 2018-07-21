package ch.njol.tome.ir.definitions;

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.common.ModuleIdentifier;

/**
 * The definition of a Brokkr type (a type with a module and a name)
 * TODO make native types like this or not?
 */
public interface IRBrokkrTypeDefinition extends IRTypeDefinition {
	
	public @Nullable ModuleIdentifier module();
	
	String name();
	
//	@Override
//	default IRClassDefinition nativeClass() {
//		return new IRBrokkrTypeClass(this);
//	}
	
	@Override
	public default boolean equalsType(final IRTypeDefinition other) {
		return other instanceof IRBrokkrTypeDefinition && Objects.equals(module(), ((IRBrokkrTypeDefinition) other).module()) && name().equals(((IRBrokkrTypeDefinition) other).name());
	}
	
//	@Override
//	public default int compareTo(final IRTypeDefinition other) {
//		if (other instanceof IRNativeNullClass || other instanceof IRNativeTypeClass)
//			return 1;
//		if (other instanceof IRBrokkrTypeDefinition) {
//			final ModuleIdentifier module = module(), otherModule = ((IRBrokkrTypeDefinition) other).module();
//			final String name = name(), otherName = ((AbstractIRBrokkrTypeDefinition) other).name();
//			if (module == null)
//				return otherModule == null ? name.compareTo(otherName) : -1;
//			if (otherModule == null)
//				return 1;
//			final int c = module.compareTo(otherModule);
//			if (c != 0)
//				return c;
//			return name.compareTo(otherName);
//		}
//		return -1;
//	}
	
	@Override
	public default int typeHashCode() {
		final ModuleIdentifier module = module();
		return (module == null ? 0 : module.hashCode() * 31) + name().hashCode();
	}
	
}
