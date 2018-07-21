package ch.njol.tome.ir;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.common.Derived;

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

	public static void assertSameIRContext(final IRElement e1, final IRElement e2) {
		assertSameIRContext(e1, e1.getIRContext(), e2, e2.getIRContext());
	}

	public static void assertSameIRContext(final IRElement e1, final IRContext irc1, final IRElement e2, final IRContext irc2) {
		assert irc1 == irc2 : "Differing IR contexts for elements " + e1 + " (" + irc1 + ") and " + e2 + " (" + irc2 + ")";
	}
	
	public static void assertSameIRContext(final @NonNull IRElement... irElements) {
		if (irElements.length <= 1 || !IRElement.class.desiredAssertionStatus())
			return;
		for (int i = 1; i < irElements.length; i++) {
			assertSameIRContext(irElements[0], irElements[i]);
		}
	}
	
	public static void assertSameIRContext(final Collection<? extends IRElement> irElements) {
		if (irElements.size() <= 1 || !IRElement.class.desiredAssertionStatus())
			return;
		final Iterator<? extends IRElement> iter = irElements.iterator();
		final IRElement e0 = iter.next();
		while (iter.hasNext()) {
			final IRElement e = iter.next();
			assertSameIRContext(e0, e);
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
