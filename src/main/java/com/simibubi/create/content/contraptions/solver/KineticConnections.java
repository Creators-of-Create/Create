package com.simibubi.create.content.contraptions.solver;

import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KineticConnections {

	public interface Type {
		boolean compatible(Type other);
	}

	public static record Entry(Vec3i offset, Value value) {
		public Entry(Vec3i offset, Type from, Type to, float ratio) {
			this(offset, new Value(from, to, ratio));
		}
		public Entry(Vec3i offset, Type from, Type to) {
			this(offset, from, to, 1);
		}
		public Entry(Vec3i offset, Type type, float ratio) {
			this(offset, type, type, ratio);
		}
		public Entry(Vec3i offset, Type type) {
			this(offset, type, type, 1);
		}
	}

	private static record Value(Type from, Type to, float ratio) { }

	private final Map<Vec3i, Value> connections;

	private KineticConnections(Map<Vec3i, Value> connections) {
		this.connections = connections;
	}

	public KineticConnections(Stream<Entry> entries) {
		this(entries.collect(Collectors.toMap(Entry::offset, Entry::value)));
	}

	public KineticConnections(Collection<Entry> entries) {
		this(entries.stream());
	}

	public KineticConnections(Entry... entries) {
		this(Arrays.stream(entries));
	}

	public Set<Vec3i> getDirections() {
		return connections.keySet();
	}

	public Optional<Float> checkConnection(KineticConnections to, Vec3i offset) {
		Value fromValue = connections.get(offset);
		if (fromValue == null) return Optional.empty();

		Value toValue = to.connections.get(offset.multiply(-1));
		if (toValue == null) return Optional.empty();

		if (fromValue.from.compatible(toValue.to) && fromValue.to.compatible(toValue.from)
				&& (Mth.equal(fromValue.ratio, 1/toValue.ratio) || (Mth.equal(toValue.ratio, 1/fromValue.ratio))))
			return Optional.of(fromValue.ratio);
		return Optional.empty();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		KineticConnections that = (KineticConnections) o;
		return Objects.equals(connections, that.connections);
	}

	@Override
	public int hashCode() {
		return Objects.hash(connections);
	}

	public KineticConnections merge(KineticConnections other) {
		Map<Vec3i, Value> out = new HashMap<>();
		out.putAll(other.connections);
		out.putAll(connections);
		return new KineticConnections(out);
	}

}

