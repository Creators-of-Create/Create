package com.simibubi.create.content.contraptions.solver;

import net.minecraft.core.Vec3i;

import java.util.Set;

public class AllPropertyTypes {
	public static final Property.Type<Float> SPEED = new Property.Type<>();
	public static final Property.Type<Set<Vec3i>> SHAFT_CONNECTIONS = new Property.Type<>();
}
