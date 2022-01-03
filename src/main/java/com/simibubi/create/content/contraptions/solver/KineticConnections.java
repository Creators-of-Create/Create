package com.simibubi.create.content.contraptions.solver;

import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KineticConnections {

	public static record Entry(Vec3i offset, Value value) {
		public Entry(Vec3i offset, String from, String to, float ratio) {
			this(offset, new Value(from, to, ratio));
		}
		public Entry(Vec3i offset, String from, String to) {
			this(offset, from, to, 1);
		}
		public Entry(Vec3i offset, String from) {
			this(offset, from, from);
		}
		public Entry stressOnly() {
			return new Entry(offset, new Value(value.from, value.to, 0));
		}
	}

	public static record Value(String from, String to, float ratio) {
		public boolean isStressOnly() {
			return ratio == 0;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Value value = (Value) o;
			return Float.compare(value.ratio, ratio) == 0 && from.equals(value.from) && to.equals(value.to);
		}

		@Override
		public int hashCode() {
			return Objects.hash(from, to, ratio);
		}
	}

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

	public Optional<Float> checkConnection(Vec3i offset) {
		Value value = connections.get(offset);
		if (value == null) return Optional.empty();
		return Optional.of(value.ratio);
	}

	private Optional<Float> checkConnection(KineticConnections to, Vec3i offset, boolean stressOnly) {
		Value fromValue = connections.get(offset);
		Value toValue = to.connections.get(offset.multiply(-1));
		if (fromValue == null || toValue == null) return Optional.empty();
		if (!fromValue.from.equals(toValue.to) || !fromValue.to.equals(toValue.from)) return Optional.empty();
		if (fromValue.isStressOnly() ^ toValue.isStressOnly()) return Optional.empty();
		if (fromValue.isStressOnly()) return stressOnly ? Optional.of(0f) : Optional.empty();
		return Optional.of(fromValue.ratio / toValue.ratio);
	}

	public Optional<Float> checkConnection(KineticConnections to, Vec3i offset) {
		return checkConnection(to, offset, false);
	}

	public boolean checkStressOnlyConnection(KineticConnections to, Vec3i offset) {
		return checkConnection(to, offset, true).isPresent();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		KineticConnections that = (KineticConnections) o;
		return connections.equals(that.connections);
	}

	@Override
	public int hashCode() {
		return Objects.hash(connections);
	}

	public KineticConnections merge(KineticConnections other) {
		Map<Vec3i, Value> connectionsNew = new HashMap<>();
		connectionsNew.putAll(other.connections);
		connectionsNew.putAll(connections);
		return new KineticConnections(connectionsNew);
	}

	public boolean hasStressOnlyConnections() {
		return connections.values().stream().anyMatch(Value::isStressOnly);
	}

	public CompoundTag save(CompoundTag tag) {
		ListTag connectionsTags = new ListTag();
		for (Map.Entry<Vec3i, Value> entry : connections.entrySet()) {
			CompoundTag entryTag = new CompoundTag();
			Value v = entry.getValue();
			entryTag.put("Off", NBTHelper.writeVec3i(entry.getKey()));
			entryTag.putString("From", v.from());
			if (!v.to().equals(v.from()))
				entryTag.putString("To", v.to());
			if (v.ratio() != 1)
				entryTag.putFloat("Ratio", v.ratio());
			connectionsTags.add(entryTag);
		}
		tag.put("Connections", connectionsTags);
		return tag;
	}

	public static KineticConnections load(CompoundTag tag) {
		Map<Vec3i, Value> connections = new HashMap<>();
		tag.getList("Connections", Tag.TAG_COMPOUND).forEach(c -> {
			CompoundTag comp = (CompoundTag) c;
			Vec3i offset = NBTHelper.readVec3i(comp.getList("Off", Tag.TAG_INT));
			String from = comp.getString("From");
			String to = comp.contains("To") ? comp.getString("To") : from;
			float ratio = comp.contains("Ratio") ? comp.getFloat("Ratio") : 1;
			connections.put(offset, new Value(from, to, ratio));
		});
		return new KineticConnections(connections);
	}

}

