package com.simibubi.create.content.curiosities.projector;

import java.util.Vector;

import com.simibubi.create.foundation.render.backend.effects.FilterSphere;
import com.simibubi.create.foundation.render.backend.instancing.IInstanceRendered;
import com.simibubi.create.foundation.tileEntity.SyncedTileEntity;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

public class ChromaticProjectorTileEntity extends SyncedTileEntity implements IInstanceRendered {

	Vector<FilterStep> stages = FilterStep.createDefault();

	float radius = 3f;

	float feather = 1;
	float density = 1;
	float fade = 1;
	boolean blend = true;

	public boolean surface = true;
	public boolean field = true;
	public float strength = 1;

	public boolean rMask = true;
	public boolean gMask = true;
	public boolean bMask = true;

	public ChromaticProjectorTileEntity(TileEntityType<?> te) {
		super(te);
	}

	public FilterSphere getFilter() {

		BlockPos pos = getPos();
		FilterSphere sphere = new FilterSphere();

		sphere.x = (float) (pos.getX() + 0.5);
		sphere.y = (float) (pos.getY() + 0.5);
		sphere.z = (float) (pos.getZ() + 0.5);
		sphere.radius = radius;

		sphere.feather = feather;
		sphere.density = density;
		sphere.fade = fade;
		sphere.blend = blend;

		sphere.surface = surface;
		sphere.field = field;
		sphere.strength = strength;

		sphere.rMask = rMask;
		sphere.gMask = gMask;
		sphere.bMask = bMask;

		sphere.filter = FilterStep.fold(stages);
		return sphere;
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

	public ChromaticProjectorTileEntity setStrength(int strength) {
		this.strength = strength / 100f;
		return this;
	}

	@Override
	public CompoundNBT write(CompoundNBT tag) {
		super.write(tag);

		tag.put("filters", FilterStep.writeAll(stages));

		tag.putFloat("radius", radius);

		tag.putFloat("feather", feather);
		tag.putFloat("density", density);
		tag.putFloat("fade", fade);
		tag.putBoolean("blend", blend);

		tag.putBoolean("surface", surface);
		tag.putBoolean("field", field);
		tag.putFloat("strength", strength);

		tag.putBoolean("rMask", rMask);
		tag.putBoolean("gMask", gMask);
		tag.putBoolean("bMask", bMask);

		return tag;
	}

	@Override
	public void fromTag(BlockState state, CompoundNBT tag) {
		super.fromTag(state, tag);

		stages = FilterStep.readAll(tag.getList("filters", Constants.NBT.TAG_COMPOUND));

		radius = tag.getFloat("radius");

		feather = tag.getFloat("feather");
		density = tag.getFloat("density");
		fade = tag.getFloat("fade");
		blend = tag.getBoolean("blend");

		surface = tag.getBoolean("surface");
		field = tag.getBoolean("field");
		strength = tag.getFloat("strength");

		rMask = tag.getBoolean("rMask");
		gMask = tag.getBoolean("gMask");
		bMask = tag.getBoolean("bMask");
	}
}
