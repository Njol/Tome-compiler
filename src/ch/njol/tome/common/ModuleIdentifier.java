package ch.njol.tome.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

public class ModuleIdentifier implements Comparable<ModuleIdentifier> {
	public final List<String> parts = new ArrayList<>();
	
	public ModuleIdentifier(final @NonNull String... parts) {
		this.parts.addAll(Arrays.asList(parts));
	}
	
	public ModuleIdentifier(final String parts) {
		this.parts.addAll(Arrays.asList(parts.split("\\.")));
	}
	
	@Override
	public String toString() {
		return "" + String.join(".", parts);
	}
	
	@Override
	public int hashCode() {
		return parts.hashCode();
	}
	
	@Override
	public boolean equals(@Nullable final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ModuleIdentifier other = (ModuleIdentifier) obj;
		return parts.equals(other.parts);
	}
	
	@Override
	public int compareTo(final ModuleIdentifier other) {
		final int n = Math.min(parts.size(), other.parts.size());
		for (int i = 0; i < n; i++) {
			final int c = parts.get(i).compareTo(other.parts.get(i));
			if (c != 0)
				return c;
		}
		return parts.size() == other.parts.size() ? 0 : parts.size() > other.parts.size() ? 1 : -1;
	}
}
