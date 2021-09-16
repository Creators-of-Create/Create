package com.simibubi.create.content.palettes;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StandardFoliageColorHandler implements BlockColor {

	@Override
	public int getColor(BlockState state, BlockAndTintGetter light, BlockPos pos, int layer) {
		return pos != null && light != null ? BiomeColors.getAverageGrassColor(light, pos) : GrassColor.get(0.5D, 1.0D);
	}

}
