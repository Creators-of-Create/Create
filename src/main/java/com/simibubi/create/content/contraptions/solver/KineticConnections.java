package com.simibubi.create.content.contraptions.solver;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class KineticConnections {
	private final Set<KineticConnection> connections;

	public KineticConnections(Set<KineticConnection> connections) {
		this.connections = connections;
	}

	public static KineticConnections empty() {
		return new KineticConnections(Set.of());
	}

	public Stream<KineticConnection> stream() {
		return connections.stream();
	}

	public boolean hasStressOnlyConnections() {
		return connections.stream()
				.flatMap(c -> c.getRatios().values().stream()
						.flatMap(m -> m.values().stream()))
				.anyMatch(r -> r == 0);
	}

	public float getShaftSpeedModifier(Direction face) {
		Vec3i offset = face.getNormal();

		KineticConnection shaft = KineticConnectionsRegistry
				.getConnectionType(AllConnections.Shafts.SHAFT.type(face.getOpposite())).get();

		return stream()
				.flatMap(c -> Optional.ofNullable(c.getRatios().get(offset))
						.flatMap(r -> Optional.ofNullable(r.get(shaft)))
						.stream())
				.findFirst()
				.orElse(0f);
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
		for (KineticConnection connection : connections) {
			connectionsTags.add(StringTag.valueOf(connection.name()));
		}
		tag.put("Connections", connectionsTags);
		return tag;
	}

	public static KineticConnections load(CompoundTag tag) {
		Set<KineticConnection> connections = new HashSet<>();
		tag.getList("Connections", Tag.TAG_STRING).forEach(t -> {
			KineticConnectionsRegistry.getConnectionType(t.getAsString())
					.ifPresent(connections::add);
		});
		return new KineticConnections(connections);
	}

}

