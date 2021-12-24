package com.simibubi.create.content.contraptions.solver;

import net.minecraft.core.BlockPos;

import javax.annotation.Nonnull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class PropertyMap {
	public static sealed class WriteResult {
		public static final class Ok extends WriteResult {
			public static final Ok V = new Ok(Set.of());
			public final Set<RewriteRule.Tracker<?>> readyToRewrite;
			public Ok(Set<RewriteRule.Tracker<?>> readyToRewrite) { this.readyToRewrite = readyToRewrite; }
		}

		public static final class Contradiction extends WriteResult {
			public static final Contradiction V = new Contradiction();
		}
	}

	private static class Counter<T> {
		public final Property<T> key;
		private T value;
		private final Set<RewriteRule.Tracker<?>> readers = new HashSet<>();

		public Counter(Property<T> key) {
			this.key = key;
		}

		public boolean isEmpty() {
			return value == null;
		}

		public boolean canDrop() {
			return isEmpty() && readers.isEmpty();
		}

		public Optional<T> read() {
			return Optional.ofNullable(value);
		}

		public WriteResult write(@Nonnull T newValue) {
			WriteResult result = WriteResult.Ok.V;
			if (isEmpty()) {
				value = newValue;
				// notify readers
				readers.forEach(RewriteRule.Tracker::dependencyRemoved);
				result = new WriteResult.Ok(readers.stream()
						.filter(RewriteRule.Tracker::canRewrite)
						.collect(Collectors.toSet()));
			} else if (!value.equals(newValue)) {
				return WriteResult.Contradiction.V;
			}
			return result;
		}

		public void unwrite() {
			if (isEmpty()) throw new IllegalStateException();
			value = null;
			// notify readers
			readers.forEach(RewriteRule.Tracker::dependencyAdded);
		}

		public boolean trackReader(RewriteRule.Tracker<?> reader) {
			readers.add(reader);
			return isEmpty();
		}

		public void untrackReader(RewriteRule.Tracker<?> reader) {
			readers.remove(reader);
		}
	}

	private final Map<Property<?>, Counter<?>> properties = new HashMap<>();

	public <T> Optional<T> read(Property<T> property) {
		Counter<T> counter = (Counter<T>) properties.get(property);
		if (counter == null) return Optional.empty();
		return counter.read();
	}

	public <T> WriteResult write(Property<T> property, T value) {
		Counter<T> counter = (Counter<T>) properties.computeIfAbsent(property, $ -> new Counter<>(property));
		return counter.write(value);
	}

	public Set<RewriteRule.Tracker<?>> unwrite(Property<?> property) {
		Counter<?> init = properties.get(property);
		if (init == null) return Set.of();

		Set<Counter<?>> toVisit = new HashSet<>();
		toVisit.add(init);
		Set<Counter<?>> visited = new HashSet<>();

		while (!toVisit.isEmpty()) {
			Set<Counter<?>> next = new HashSet<>();

			for (Counter<?> c : toVisit) {
				if (c.isEmpty() || visited.contains(c)) continue;
				visited.add(c);

				c.unwrite();
				if (c.canDrop()) {
					properties.put(c.key, null);
				}

				c.readers.stream()
							.map(r -> properties.get(r.writes))
							.filter(Objects::nonNull)
							.forEachOrdered(toVisit::add);
			}

			toVisit = next;
		}

		return visited.stream().flatMap(c -> c.readers.stream()).collect(Collectors.toSet());
	}

	public int trackReader(RewriteRule.Tracker<?> reader) {
		int dependencies = 0;
		for (Property<?> p : reader.reads) {
			dependencies += properties.computeIfAbsent(p, $ -> new Counter<>(p)).trackReader(reader) ? 1 : 0;
		}
		return dependencies;
	}

	public void untrackReader(RewriteRule.Tracker<?> reader) {
		for (Property<?> property : reader.reads) {
			properties.get(property).untrackReader(reader);
		}
	}
}
