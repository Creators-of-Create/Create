package com.simibubi.create.foundation.metadoc;

import com.simibubi.create.content.schematics.SchematicWorld;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public class MetaDocWorld extends SchematicWorld {

	int overrideLight;
	WorldSectionElement mask;

	public MetaDocWorld(BlockPos anchor, World original) {
		super(anchor, original);
	}

	public void pushFakeLight(int light) {
		this.overrideLight = light;
	}

	public void popLight() {
		this.overrideLight = -1;
	}

	@Override
	public int getLightLevel(LightType p_226658_1_, BlockPos p_226658_2_) {
		return overrideLight == -1 ? 15 : overrideLight;
	}

	public void setMask(WorldSectionElement mask) {
		this.mask = mask;
	}

	public void clearMask() {
		this.mask = null;
	}

	@Override
	public BlockState getBlockState(BlockPos globalPos) {
		if (mask != null && !mask.test(globalPos.subtract(anchor)))
			return Blocks.AIR.getDefaultState();
		return super.getBlockState(globalPos);
	}

}
