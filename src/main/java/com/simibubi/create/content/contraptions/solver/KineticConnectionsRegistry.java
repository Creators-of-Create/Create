package com.simibubi.create.content.contraptions.solver;

import net.minecraft.core.Vec3i;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class KineticConnectionsRegistry {
	private static final Map<String, KineticConnection> connectionTypes = new HashMap<>();
	private static final Map<KineticConnection, Map<Vec3i, Map<KineticConnection, Float>>> connectionRatios
			= new HashMap<>();

	public static KineticConnection registerConnectionType(String name) {
		if (connectionTypes.containsKey(name))
			throw new IllegalStateException("connection type named \"" + name + "\" already exists");
		KineticConnection connection = new KineticConnection(name);
		connectionTypes.put(name, connection);
		return connection;
	}

	public static Optional<KineticConnection> getConnectionType(String name) {
		return Optional.ofNullable(connectionTypes.get(name));
	}

	public static void registerConnectionRatio(KineticConnection from, KineticConnection to, Vec3i offset, float ratio) {
		Map<KineticConnection, Float> fromMap = connectionRatios
				.computeIfAbsent(from, $ -> new HashMap<>())
				.computeIfAbsent(offset, $ -> new HashMap<>());
		if (fromMap.containsKey(to)) return;

		Map<KineticConnection, Float> toMap = connectionRatios
				.computeIfAbsent(to, $ -> new HashMap<>())
				.computeIfAbsent(offset.multiply(-1), $ -> new HashMap<>());

		fromMap.put(to, ratio);
		toMap.put(from, ratio == 0 ? 0 : 1/ratio);
	}

	public static Map<Vec3i, Map<KineticConnection, Float>> getConnections(KineticConnection from) {
		return Optional.ofNullable(connectionRatios.get(from))
				.map(Collections::unmodifiableMap)
				.orElseGet(Map::of);
	}
}
