package ch.njol.brokkr.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class is thread-safe.
 */
public abstract class AbstractDerived extends AbstractInvalidatable implements Derived {
	
	private volatile @Nullable List<Invalidatable> dependencies = null;
	
	@NonNullByDefault({})
	protected final synchronized <T extends Invalidatable> T registerDependency(final T dep) {
		if (!isValid())
			return dep;
		if (dep != null) {
			if (!dep.isValid()) {
				onInvalidate(dep);
				return dep;
			}
			List<Invalidatable> dependencies = this.dependencies;
			if (dependencies == null)
				dependencies = this.dependencies = new ArrayList<>();
			dependencies.add(dep);
			dep.registerInvalidateListener(this);
		}
		return dep;
	}
	
	protected final synchronized void registerDependencies(final Invalidatable... deps) {
		for (final Invalidatable dep : deps)
			registerDependency(dep);
	}
	
	protected final synchronized <T extends Collection<? extends Invalidatable>> T registerDependencies(final T deps) {
		for (final Invalidatable dep : deps)
			registerDependency(dep);
		return deps;
	}
	
	@Override
	public final void onInvalidate(final Invalidatable source) {
		invalidate();
	}
	
	@Override
	protected final synchronized void invalidate() {
		super.invalidate();
		final List<Invalidatable> dependencies = this.dependencies;
		if (dependencies != null) {
			this.dependencies = null; // clear references
			for (final Invalidatable dep : dependencies)
				dep.removeInvalidateListener(this);
		}
	}
	
}
