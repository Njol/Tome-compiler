package ch.njol.brokkr.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

public class ModuleIdentifier {
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
}
