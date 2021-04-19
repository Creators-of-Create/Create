package com.simibubi.create.content.optics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.math.vector.Vector3d;

public class Beam extends ArrayList<BeamSegment> {
	public final Set<Beam> subBeams;
	private final Set<ILightHandler<?>> lightEventListeners;
	private final Vector3d direction;
	private boolean removed = false;

	public Beam(Vector3d direction) {
		super();
		this.direction = direction;
		lightEventListeners = new HashSet<>();
		subBeams = new HashSet<>();
	}

	public void onRemoved() {
		lightEventListeners.forEach(handler -> handler.onBeamRemoved(this));
		subBeams.forEach(Beam::onRemoved);
		subBeams.clear();
		removed = true;
		clear();
	}

	public void onCreated() {
		lightEventListeners.stream()
				.flatMap(handler -> handler.constructSubBeams(this))
				.forEach(subBeams::add);
	}

	public void registerSubBeam(Beam beam) {
		subBeams.add(beam);
	}

	public void render(MatrixStack ms, IRenderTypeBuffer buffer, float partialTicks) {
		if (removed)
			throw new IllegalStateException("tried to render removed beam");
		forEach(beamSegment -> beamSegment.renderSegment(ms, buffer, partialTicks));
	}

	public void addListener(@Nullable ILightHandler<?> tile) {
		if (tile != null)
			lightEventListeners.add(tile);
	}

	public Vector3d getDirection() {
		return direction;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		Beam that = (Beam) o;
		return removed == that.removed && subBeams.equals(that.subBeams) && lightEventListeners.equals(that.lightEventListeners) && Objects.equals(direction, that.direction);
	}

	public void removeSubBeam(Beam out) {
		if (subBeams.remove(out))
			out.onRemoved();
	}
}
