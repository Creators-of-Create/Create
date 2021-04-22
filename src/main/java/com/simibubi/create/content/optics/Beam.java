package com.simibubi.create.content.optics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.item.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class Beam extends ArrayList<BeamSegment> {
	private final transient Set<ILightHandler> lightEventListeners;
	@Nullable
	private final Beam parent;
	private final long createdAt;
	@Nullable
	private final transient World world;
	private boolean removed = false;

	public Beam(@Nullable Beam parent) {
		super();
		this.parent = parent;
		this.createdAt = 0;
		this.world = null;
		lightEventListeners = new HashSet<>();
	}

	public Beam(@Nullable Beam parent, @Nullable World world) {
		super();
		this.parent = parent;
		this.world = world;
		this.createdAt = world == null ? -1 : this.world.getGameTime();
		lightEventListeners = new HashSet<>();
	}

	public void onCreated() {
		lightEventListeners.stream()
				.flatMap(handler -> handler.constructSubBeams(this))
				.forEach(Beam::onCreated);
	}

	public void render(MatrixStack ms, IRenderTypeBuffer buffer, float partialTicks) {
		forEach(beamSegment -> beamSegment.renderSegment(ms, buffer, partialTicks));
	}

	public void addListener(@Nullable ILightHandler tile) {
		if (tile != null)
			lightEventListeners.add(tile);
	}

	@Nullable
	public Vector3d getDirection() {
		return isEmpty() ? null : get(0).getNormalized();
	}

	public void onRemoved() {
		removed = true;
		lightEventListeners.stream()
				.filter(handler -> handler != this.getHandler())
				.forEach(ILightHandler::updateBeams);
	}

	@Nullable
	public ILightHandler getHandler() {
		return size() == 0 ? null : get(0).getHandler();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		Beam beam = (Beam) o;
		return createdAt == beam.createdAt && removed == beam.removed && lightEventListeners.equals(beam.lightEventListeners);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), lightEventListeners, createdAt, removed);
	}

	public boolean isRemoved() {
		ILightHandler handler = getHandler();
		removed = removed || isEmpty() || handler == null || handler.getTile()
				.isRemoved() || (parent != null && parent.isRemovedSimple());
		return removed;
	}

	private boolean isRemovedSimple() {
		ILightHandler handler = getHandler();
		removed = removed || isEmpty() || handler == null || handler.getTile()
				.isRemoved();
		return removed;
	}

	public float[] getColorAt(BlockPos testBlockPos) {
		float[] out = DyeColor.WHITE.getColorComponentValues();
		for (BeamSegment segment : this) {
			if (VecHelper.getCenterOf(testBlockPos)
					.subtract(segment.getStart())
					.dotProduct(segment.getNormalized()) > 0)
				out = segment.getColors();
			else
				break;
		}

		return out;
	}

	public boolean isNew() {
		return world != null && world.getGameTime() == createdAt;
	}


	@Nullable
	public Beam getParent() {
		return parent;
	}
}
