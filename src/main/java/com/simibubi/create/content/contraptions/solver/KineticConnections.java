package com.simibubi.create.content.contraptions.solver;

import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class KineticConnections {
	private final Map<KineticConnection, Float> connections;

	public KineticConnections(Map<KineticConnection, Float> connections) {
		this.connections = connections;
	}

	public static KineticConnections empty() {
		return new KineticConnections(Map.of());
	}

	public Set<Map.Entry<KineticConnection, Float>> entries() {
		return connections.entrySet();
	}

	public boolean hasStressOnlyConnections() {
		return entries().stream()
				.flatMap(c -> c.getKey().getRatios().values().stream()
						.flatMap(m -> m.values().stream()))
				.anyMatch(r -> r == 0);
	}

	public float getShaftSpeedModifier(Direction face) {
		KineticConnection shaft = KineticConnectionsRegistry
				.getConnectionType(AllConnections.Directional.SHAFT.type(face)).get();

		return Optional.ofNullable(connections.get(shaft)).orElse(0f);
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

	public CompoundTag save(CompoundTag tag) {
		ListTag connectionsTags = new ListTag();
		for (Map.Entry<KineticConnection, Float> entry : connections.entrySet()) {
			CompoundTag entryTag = new CompoundTag();
			entryTag.putString("Name", entry.getKey().name());
			if (entry.getValue() != 1) {
				entryTag.putFloat("Mod", entry.getValue());
			}
			connectionsTags.add(entryTag);
		}
		tag.put("Connections", connectionsTags);
		return tag;
	}

	public static KineticConnections load(CompoundTag tag) {
		Map<KineticConnection, Float> connections = new HashMap<>();
		tag.getList("Connections", Tag.TAG_COMPOUND).forEach(t -> {
			CompoundTag ct = (CompoundTag) t;
			String name = ct.getString("Name");
			float mod = ct.contains("Mod") ? ct.getFloat("Mod") : 1;
			KineticConnectionsRegistry.getConnectionType(name).ifPresent(c -> connections.put(c, mod));
		});
		return new KineticConnections(connections);
	}

}

