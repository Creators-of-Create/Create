package com.simibubi.create.content.processing.burner;

import org.joml.Quaternionf;

import dev.engine_room.flywheel.api.instance.InstanceHandle;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.lib.instance.ColoredLitInstance;
import net.minecraft.core.Vec3i;

public class ScrollInstance extends ColoredLitInstance {
	public float x;
	public float y;
	public float z;
	public final Quaternionf rotation = new Quaternionf();

	public float speedU;
	public float speedV;

	public float diffU;
	public float diffV;

	public float scaleU;
	public float scaleV;

	public ScrollInstance(InstanceType<? extends ColoredLitInstance> type, InstanceHandle handle) {
		super(type, handle);
	}

	public ScrollInstance position(Vec3i position) {
		this.x = position.getX();
		this.y = position.getY();
		this.z = position.getZ();
		return this;
	}
}
