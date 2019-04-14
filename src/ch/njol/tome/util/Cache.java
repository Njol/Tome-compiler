package ch.njol.tome.util;

import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A cached value that may be invalidated at any time.
 * <p>
 * This class is thread-safe.
 */
public class Cache<T extends @Nullable Derived> {
	
	private final Supplier<T> calculation;
	private volatile @Nullable T value = null;
	
	public <S extends Watchable> Cache(S source, final Function<S, T> calculation) {
		this(source, () -> calculation.apply(source));
	}
	
	public Cache(Watchable source, final Supplier<T> calculation) {
		this.calculation = calculation;
		registerDependency(source);
	}
	
	public void registerDependency(Watchable dependency) {
		dependency.addModificationListener((s) -> {
			if (value != null) {
				synchronized (this) {
					value = null;
				}
			}
		});
	}
	
	public T get() {
		@Nullable
		T cachedValue = this.value;
		if (cachedValue != null)
			return cachedValue;
		synchronized (this) {
			cachedValue = this.value;
			if (cachedValue != null)
				return cachedValue;
			final T value = calculation.get();
			if (value != null) {
				registerDependency(value);
				this.value = value;
			}
			return value;
		}
	}
	
}
