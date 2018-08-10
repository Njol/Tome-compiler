package ch.njol.tome.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class is thread-safe.
 */
public abstract class AbstractDerived extends AbstractModifiable implements Derived {
	
	private volatile @Nullable List<Modifiable> dependencies = null;
	
	/**
	 * @param dep A dependency to register
	 * @return The argument
	 */
	@NonNullByDefault({})
	protected final synchronized <T extends Modifiable> T registerDependency(final T dep) {
		if (dep != null) {
			List<Modifiable> dependencies = this.dependencies;
			if (dependencies == null)
				dependencies = this.dependencies = new ArrayList<>();
			dependencies.add(dep);
			dep.addModificationListener(this);
		}
		return dep;
	}
	
	protected final synchronized void registerDependencies(final Modifiable... deps) {
		for (final Modifiable dep : deps)
			registerDependency(dep);
	}
	
	protected final synchronized <T extends Collection<? extends Modifiable>> T registerDependencies(final T deps) {
		for (final Modifiable dep : deps)
			registerDependency(dep);
		return deps;
	}
	
	@Override
	public final void onModification(final Modifiable source) {
//		onDependencyModification();
		modified();
	}
	
//	protected abstract void onDependencyModification();
	
	@Override
	protected final synchronized void modified() {
		super.modified();
	}
	
}
