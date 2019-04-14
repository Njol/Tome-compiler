package ch.njol.tome.util;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class is thread-safe.
 */
public abstract class AbstractDerived extends AbstractWatchable implements Derived {
	
	/**
	 * <b>When using this constructor, make sure to call {@link #registerDependency(Watchable)} manually!</b>
	 */
	protected AbstractDerived() {}
	
	public AbstractDerived(Watchable mainSource, Watchable... otherDependencies) {
		registerDependency(mainSource);
		registerDependencies(otherDependencies);
	}
	
	/**
	 * @param dep A dependency to register
	 * @return The argument
	 */
	@NonNullByDefault({})
	protected final synchronized <T extends Watchable> T registerDependency(final T dep) {
		if (dep != null)
			dep.addModificationListener(this);
		return dep;
	}
	
	protected final synchronized void registerDependencies(final Watchable... deps) {
		for (final Watchable dep : deps)
			registerDependency(dep);
	}
	
	protected final synchronized <T extends Collection<? extends Watchable>> T registerDependencies(final T deps) {
		for (final Watchable dep : deps)
			registerDependency(dep);
		return deps;
	}
	
	@Override
	public final void onModification(final Watchable source) {
//		onDependencyModification();
		modified();
	}
	
//	protected abstract void onDependencyModification();
	
	@Override
	protected final synchronized void modified() {
		super.modified();
	}
	
}
