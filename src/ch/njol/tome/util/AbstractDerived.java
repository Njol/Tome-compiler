package ch.njol.tome.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class is thread-safe.
 */
public abstract class AbstractDerived extends AbstractWatchable implements Derived {
	
	private volatile @Nullable List<Watchable> dependencies = null;
	
	/**
	 * @param dep A dependency to register
	 * @return The argument
	 */
	@NonNullByDefault({})
	protected final synchronized <T extends Watchable> T registerDependency(final T dep) {
		if (dep != null) {
			List<Watchable> dependencies = this.dependencies;
			if (dependencies == null)
				dependencies = this.dependencies = new ArrayList<>();
			dependencies.add(dep);
			dep.addModificationListener(this);
		}
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
