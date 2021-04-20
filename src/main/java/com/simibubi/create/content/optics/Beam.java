package com.simibubi.create.content.optics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.item.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class Beam extends ArrayList<BeamSegment> {
	private final Set<ILightHandler> lightEventListeners;
	@Nullable
	private final Beam parent;
	private boolean removed = false;

	public Beam(@Nullable Beam parent) {
		super();
		this.parent = parent;
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
		Beam that = (Beam) o;
		return lightEventListeners.equals(that.lightEventListeners);
	}

	public boolean isRemoved() {
		// || !get(0).getHandler().getOutBeams().contains(this)
		ILightHandler handler = getHandler();
		removed = removed || isEmpty() || handler == null || handler.getTile()
				.isRemoved() || (parent != null && parent.isRemoved());
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
}
