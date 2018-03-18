package ch.njol.brokkr.common;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDerived extends AbstractInvalidatable implements Derived {
	
	private final List<Invalidatable> dependencies = new ArrayList<>();
	
	protected final void registerDependency(final Invalidatable dep) {
		assert isValid();
		dependencies.add(dep);
		dep.registerInvalidateListener(this);
	}
	
	protected final void registerDependencies(final Invalidatable... deps) {
		for (final Invalidatable dep : deps)
			registerDependency(dep);
	}
	
	@Override
	public final void onInvalidate(final Invalidatable source) {
		invalidate();
	}
	
	@Override
	public final void invalidateInternal() {
		super.invalidate();
		for (final Invalidatable dep : dependencies)
			dep.removeInvalidateListener(this);
		// clear references
		dependencies.clear();
	}
	
}
