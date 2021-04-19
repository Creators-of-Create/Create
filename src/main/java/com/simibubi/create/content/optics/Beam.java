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

public class Beam extends ArrayList<BeamSegment> {
	private final Set<ILightHandler<?>> lightEventListeners;
	private final Vector3d direction;
	@Nullable
	private final Beam parent;

	public Beam(@Nullable Beam parent, Vector3d direction) {
		super();
		this.parent = parent;
		this.direction = direction;
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
		return lightEventListeners.equals(that.lightEventListeners) && Objects.equals(direction, that.direction);
	}

	public boolean isRemoved() {
		return isEmpty() || get(0).getHandler()
				.getTile()
				.isRemoved() || !get(0).getHandler()
				.getOutBeams()
				.contains(this) || (parent != null && parent.isRemoved());
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
}
