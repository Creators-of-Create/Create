package com.simibubi.create.content.curiosities.weapons;

public record SplitProperty(
		PotatoCannonProjectileType type,
		float chance
) {
	@Override
	public String toString() {
		return "SplitProperty{" +
				"type=" + type +
				", chance=" + chance +
				'}';
	}
}
