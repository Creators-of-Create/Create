package com.simibubi.create.content.contraptions.solver;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;

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

	public enum Types {
		SHAFT, LARGE_COG, SMALL_COG, SPEED_CONTROLLER_TOP
	}

	public static class Type {
		private final Types name;
		private final String value;

		private Type(Types name, String value) {
			this.name = name;
			this.value = value;
		}

		private final static Interner<Type> cachedTypes = Interners.newStrongInterner();

		public static Type of(Types name, String value) {
			return cachedTypes.intern(new Type(name, value));
		}

		public static Type of(Types name, StringRepresentable value) {
			return of(name, value.getSerializedName());
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Type type1 = (Type) o;
			return name == type1.name && Objects.equals(value, type1.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, value);
		}

		public CompoundTag save(CompoundTag tag) {
			NBTHelper.writeEnum(tag, "Name", name);
			tag.putString("Value", value);
			return tag;
		}

		public static Type load(CompoundTag tag) {
			return Type.of(NBTHelper.readEnum(tag, "Name", Types.class), tag.getString("Value"));
		}
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
		public Entry stressOnly() {
			return new Entry(offset, new Value(value.from, value.to, 0));
		}
	}

	private static record Value(Type from, Type to, float ratio) {
		public boolean isStressOnly() {
			return ratio == 0;
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

	public Optional<Float> checkConnection(KineticConnections to, Vec3i offset) {
		Value fromValue = connections.get(offset);
		if (fromValue == null) return Optional.empty();

		Value toValue = to.connections.get(offset.multiply(-1));
		if (toValue == null) return Optional.empty();

		if (fromValue.isStressOnly() || toValue.isStressOnly()) return Optional.empty();

		if (fromValue.from.equals(toValue.to) && fromValue.to.equals(toValue.from)
				&& (Mth.equal(fromValue.ratio, 1/toValue.ratio) || (Mth.equal(toValue.ratio, 1/fromValue.ratio))))
			return Optional.of(fromValue.ratio);
		return Optional.empty();
	}

	public boolean checkStressOnlyConnection(KineticConnections to, Vec3i offset) {
		Value fromValue = connections.get(offset);
		if (fromValue == null) return false;

		Value toValue = to.connections.get(offset.multiply(-1));
		if (toValue == null) return false;

		if (!fromValue.isStressOnly() || !toValue.isStressOnly()) return false;

		return fromValue.from.equals(toValue.to) && fromValue.to.equals(toValue.from);
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

	public boolean hasStressOnlyConnections() {
		return connections.values().stream().anyMatch(Value::isStressOnly);
	}

	public CompoundTag save(CompoundTag tag) {
		ListTag connectionsTags = new ListTag();
		for (Map.Entry<Vec3i, Value> entry : connections.entrySet()) {
			CompoundTag entryTag = new CompoundTag();
			entryTag.put("Off", NBTHelper.writeVec3i(entry.getKey()));
			entryTag.put("From", entry.getValue().from().save(new CompoundTag()));
			entryTag.put("To", entry.getValue().to().save(new CompoundTag()));
			entryTag.putFloat("Ratio", entry.getValue().ratio());
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
			Type from = Type.load(comp.getCompound("From"));
			Type to = Type.load(comp.getCompound("To"));
			float ratio = comp.getFloat("Ratio");
			connections.put(offset, new Value(from, to, ratio));
		});
		return new KineticConnections(connections);
	}

}

