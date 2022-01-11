package com.simibubi.create.content.contraptions.solver;

import net.minecraft.core.Direction;

import java.util.HashSet;
import java.util.Set;

public class ConnectionsBuilder {
	private final Set<KineticConnection> connections = new HashSet<>();

	public static ConnectionsBuilder builder() {
		return new ConnectionsBuilder();
	}

	public KineticConnections build() {
		return new KineticConnections(connections);
	}

	public ConnectionsBuilder withHalfShaft(AllConnections.Shafts shaft, Direction dir) {
		connections.add(KineticConnectionsRegistry.getConnectionType(shaft.type(dir)).get());
		return this;
	}

	public ConnectionsBuilder withHalfShaft(Direction dir) {
		return withHalfShaft(AllConnections.Shafts.SHAFT, dir);
	}

	public ConnectionsBuilder withFullShaft(Direction.Axis axis) {
		return this.withHalfShaft(AllConnections.pos(axis)).withHalfShaft(AllConnections.neg(axis));
	}

	public ConnectionsBuilder withDirectional(AllConnections.Directional directional, Direction dir) {
		connections.add(KineticConnectionsRegistry.getConnectionType(directional.type(dir)).get());
		return this;
	}

	public ConnectionsBuilder withAxial(AllConnections.Axial axial, Direction.Axis axis) {
		connections.add(KineticConnectionsRegistry.getConnectionType(axial.type(axis)).get());
		return this;
	}

	public ConnectionsBuilder withDirAxial(AllConnections.DirAxial dirAxial, Direction dir, boolean first) {
		connections.add(KineticConnectionsRegistry.getConnectionType(dirAxial.type(dir, first)).get());
		return this;
	}

	public ConnectionsBuilder withLargeCog(Direction.Axis axis) {
		return withAxial(AllConnections.Axial.LARGE_COG, axis);
	}

	public ConnectionsBuilder withSmallCog(Direction.Axis axis) {
		return withAxial(AllConnections.Axial.SMALL_COG, axis);
	}
}
