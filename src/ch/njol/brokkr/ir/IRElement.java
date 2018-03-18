package ch.njol.brokkr.ir;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.common.Derived;

/**
 * Implementation note: each IR element must register its dependencies using {@link AbstractIRElement#registerDependency}.
 */
public interface IRElement extends Derived {
	
	IRContext getIRContext();
	
//	/**
//	 * Returns A collection of {@link ASTElement}s that are the sources of this {@link IRElement}. This may return the lowest (in the AST) {@link ASTElement} from which all source
//	 * data is reachable, or list each source element separately.
//	 * <p>
//	 * The returned collection my be empty if this IR element is not generated from an AST, e.g. if it represents native code.
//	 */
//	Collection<ASTElement> sources();

//	@Override
//	default void linked(final ASTLink<?> link) {
//		for (final ASTElement source : sources())
//			source.linked(link);
//	}
//
//	@Override
//	default void unlinked(ASTLink<?> link) {
//		for (final ASTElement source : sources())
//			source.unlinked(link);
//	}
	
	public static void assertSameIRContext(final @NonNull IRElement... irElements) {
		if (irElements.length <= 1 || !IRElement.class.desiredAssertionStatus())
			return;
		final IRContext irContext = irElements[0].getIRContext();
		for (int i = 1; i < irElements.length; i++) {
			assert irContext == irElements[i].getIRContext() : "Differing IR contexts for elements " + irElements[0] + " and " + irElements[i];
		}
	}
	
	public static void assertSameIRContext(final Collection<? extends IRElement> irElements) {
		if (irElements.size() <= 1 || !IRElement.class.desiredAssertionStatus())
			return;
		final Iterator<? extends IRElement> iter = irElements.iterator();
		final IRElement e0 = iter.next();
		final IRContext irContext = e0.getIRContext();
		while (iter.hasNext()) {
			final IRElement e = iter.next();
			assert irContext == e.getIRContext() : "Differing IR contexts for elements " + e0 + " and " + e;
		}
	}
	
	public static void assertSameIRContext(final Collection<? extends IRElement> irElements, final @NonNull IRElement... moreIRElements) {
		assertSameIRContext(irElements);
		assertSameIRContext(moreIRElements);
		if (irElements.size() > 0 && moreIRElements.length > 0)
			assertSameIRContext(irElements.iterator().next(), moreIRElements[0]);
	}
	
	@SafeVarargs
	public static void assertSameIRContext(final @NonNull Collection<? extends IRElement>... irElements) {
		@Nullable
		IRElement someElement = null;
		for (final Collection<? extends IRElement> elems : irElements) {
			if (elems.size() > 0) {
				if (someElement == null)
					someElement = elems.iterator().next();
				else
					assertSameIRContext(someElement, elems.iterator().next());
				assertSameIRContext(elems);
			}
		}
	}
	
}
