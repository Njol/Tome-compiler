package ch.njol.tome.common;

import java.util.Locale;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.parser.Parser;

public enum Borrowing {
	/**
	 * A borrowed value cannot be stored for more than the current function (lambda, attribute, etc.), and is returned to the caller after the method returns.
	 * <p>
	 * For shared values this makes little difference, but for exclusive values the value remains exclusive after the method returns in the context of the caller.
	 * <p>
	 * This is particularly useful for functions, as they can assign local variables of the caller function if and only if those variables are borrowed
	 * (as the lifespan of the functions then does not exceed the lifespan of the variables).
	 */
	BORROWED, // other possible names: loaned, lended (but those would be as if viewed from the caller)
	
	/**
	 * A captured value is not released after a method returns, but is either discarded or stored in the object graph of one or more arguments (incl. the current object).
	 */
	CAPTURED;
	
	// TODO should borrowing or capturing be the default? both are used very often, but not adding 'borrowed' can be forgotten (or could result in a warning?), while not adding 'capturing' results in an error.
	// also, a way to temporarily store borrowed objects in fields could be useful. Proving correctness can be done by ensuring the objects are owned by the method, and thus are destroyed/inaccessible at or before the method returns.
	// TODO think about object graph exclusivity
	// examples:
	//	- tree data structure where nodes have references to parent nodes
	//		- currently, all nodes must be shared, while the actual tree object can be exclusive
	//		- data can be stored exclusively in the tree nodes
	// usage example:
	//	- method that uses a temporary list:
	//		what(borrowed exclusive T smth) {
	//			var list = List<exclusive T>.new([smth, smthelse]); //
	//			... // list used
	//			// list still exclusive at end of method
	//		}
	
	public static @Nullable Borrowing parse(final Parser parent) {
		final String val = parent.try_("borrowed", "captured");
		if (val == null)
			return null;
		if (val.startsWith("b"))
			return BORROWED;
		else
			return CAPTURED;
	}
	
	@Override
	public String toString() {
		return name().toLowerCase(Locale.ENGLISH);
	}
	
}
