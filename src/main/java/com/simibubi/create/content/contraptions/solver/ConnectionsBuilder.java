package com.simibubi.create.content.contraptions.solver;

import net.minecraft.core.Direction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConnectionsBuilder {
	private final Map<KineticConnection, Float> connections = new HashMap<>();

	public static ConnectionsBuilder builder() {
		return new ConnectionsBuilder();
	}

	public KineticConnections build() {
		return new KineticConnections(connections);
	}

	public ConnectionsBuilder withDirectional(AllConnections.Directional directional, Direction dir, float mod) {
		connections.put(KineticConnectionsRegistry.getConnectionType(directional.type(dir)).get(), mod);
		return this;
	}

	public ConnectionsBuilder withDirectional(AllConnections.Directional directional, Direction dir) {
		return withDirectional(directional, dir, 1);
	}

	public ConnectionsBuilder withAxial(AllConnections.Axial axial, Direction.Axis axis, float mod) {
		connections.put(KineticConnectionsRegistry.getConnectionType(axial.type(axis)).get(), mod);
		return this;
	}

	public ConnectionsBuilder withAxial(AllConnections.Axial axial, Direction.Axis axis) {
		return withAxial(axial, axis, 1);
	}

	public ConnectionsBuilder withDirAxial(AllConnections.DirAxial dirAxial, Direction dir, boolean first, float mod) {
		connections.put(KineticConnectionsRegistry.getConnectionType(dirAxial.type(dir, first)).get(), mod);
		return this;
	}

	public ConnectionsBuilder withDirAxial(AllConnections.DirAxial dirAxial, Direction dir, boolean first) {
		return withDirAxial(dirAxial, dir, first, 1);
	}

	public ConnectionsBuilder withLargeCog(Direction.Axis axis) {
		return withAxial(AllConnections.Axial.LARGE_COG, axis);
	}

	public ConnectionsBuilder withSmallCog(Direction.Axis axis) {
		return withAxial(AllConnections.Axial.SMALL_COG, axis);
	}

	public ConnectionsBuilder withHalfShaft(Direction dir, float mod) {
		return withDirectional(AllConnections.Directional.SHAFT, dir, mod);
	}

	public ConnectionsBuilder withHalfShaft(Direction dir) {
		return withHalfShaft(dir, 1);
	}

	public ConnectionsBuilder withFullShaft(Direction.Axis axis) {
		return this.withHalfShaft(AllConnections.pos(axis)).withHalfShaft(AllConnections.neg(axis));
	}
}
