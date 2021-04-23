package com.simibubi.create.content.curiosities.projector;

import java.util.Vector;

import com.simibubi.create.foundation.render.backend.effects.SphereFilterProgram;
import com.simibubi.create.foundation.render.backend.instancing.IInstanceRendered;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;

public class ChromaticProjectorTileEntity extends TileEntity implements IInstanceRendered {

	Vector<FilterStep> stages = FilterStep.createDefault();

	float radius = 10f;
	float density = 1f;
	float feather = 3;
	float fade = 1.3f;
	boolean blend = true;

	public ChromaticProjectorTileEntity(TileEntityType<?> te) {
		super(te);
	}

	public SphereFilterProgram.FilterSphere makeFilter() {
		Matrix4f filter = FilterStep.fold(stages);

		BlockPos pos = getPos();
		return new SphereFilterProgram.FilterSphere()
				.setFilter(filter)
				.setCenter(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)
				.setRadius(radius)
				.setDensity(density)
				.setFeather(feather)
				.setBlendOver(false)
				.setFade(fade);
	}

	public ChromaticProjectorTileEntity setRadius(int radius) {
		this.radius = radius / 2f;
		return this;
	}

	public ChromaticProjectorTileEntity setDensity(int density) {
		this.density = density / 100f;
		return this;
	}

	public ChromaticProjectorTileEntity setFeather(int feather) {
		this.feather = feather / 4f;
		return this;
	}

	public ChromaticProjectorTileEntity setFade(int fade) {
		this.fade = feather / 10f;
		return this;
	}

	public ChromaticProjectorTileEntity setBlend(boolean blend) {
		this.blend = blend;
		return this;
	}
}
