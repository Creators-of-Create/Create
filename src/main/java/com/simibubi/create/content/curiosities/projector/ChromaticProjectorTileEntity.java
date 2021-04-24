package com.simibubi.create.content.curiosities.projector;

import java.util.Vector;

import com.simibubi.create.foundation.render.backend.effects.FilterSphere;
import com.simibubi.create.foundation.render.backend.instancing.IInstanceRendered;
import com.simibubi.create.foundation.tileEntity.SyncedTileEntity;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.common.util.Constants;

public class ChromaticProjectorTileEntity extends SyncedTileEntity implements IInstanceRendered {

	Vector<FilterStep> stages = FilterStep.createDefault();

	float radius = 3f;
	float density = 1;
	float feather = 1;
	float fade = 1;
	boolean blend = true;

	public ChromaticProjectorTileEntity(TileEntityType<?> te) {
		super(te);
	}

	public FilterSphere makeFilter() {
		Matrix4f filter = FilterStep.fold(stages);

		BlockPos pos = getPos();
		return new FilterSphere()
				.setFilter(filter)
				.setCenter(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)
				.setRadius(radius)
				.setDensity(density)
				.setFeather(feather)
				.setBlendOver(true)
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
		this.feather = feather / 10f;
		return this;
	}

	public ChromaticProjectorTileEntity setFade(int fade) {
		this.fade = fade / 10f;
		return this;
	}

	public ChromaticProjectorTileEntity setBlend(boolean blend) {
		this.blend = blend;
		return this;
	}

	@Override
	public CompoundNBT write(CompoundNBT tag) {
		super.write(tag);

		tag.put("filters", FilterStep.writeAll(stages));

		tag.putFloat("radius", radius);
		tag.putFloat("density", density);
		tag.putFloat("feather", feather);
		tag.putFloat("fade", fade);

		return tag;
	}

	@Override
	public void fromTag(BlockState state, CompoundNBT tag) {
		super.fromTag(state, tag);

		stages = FilterStep.readAll(tag.getList("filters", Constants.NBT.TAG_COMPOUND));

		radius = tag.getFloat("radius");
		density = tag.getFloat("density");
		feather = tag.getFloat("feather");
		fade = tag.getFloat("fade");
	}
}
