package ch.njol.tome.util;

import static org.eclipse.jdt.annotation.DefaultLocation.*;

import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A cached value that may be invalidated at any time.
 * <p>
 * This class is thread-safe.
 */
@NonNullByDefault({PARAMETER, RETURN_TYPE, FIELD, ARRAY_CONTENTS})
public class Cache<T extends Derived> implements ModificationListener {
	
	private final Supplier<T> calculation;
	private volatile @Nullable T value = null;
	
	public Cache(final Supplier<T> calculation) {
		this.calculation = calculation;
	}
	
	@Override
	public void onModification(final Modifiable source) {
		if (value != null) {
			synchronized (this) {
				value = null;
			}
		}
	}
	
	@SuppressWarnings("null")
	public T get() {
		@Nullable
		T value = this.value;
		if (value != null)
			return value;
		synchronized (this) {
			value = this.value;
			if (value != null)
				return value;
			value = calculation.get();
			if (value != null) {
				value.addModificationListener(this);
				this.value = value;
			}
			return value;
		}
	}
	
}
