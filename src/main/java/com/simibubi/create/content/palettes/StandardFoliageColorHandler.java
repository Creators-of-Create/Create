package com.simibubi.create.content.palettes;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GrassColors;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.biome.BiomeColors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StandardFoliageColorHandler implements IBlockColor {

	@Override
	public int getColor(BlockState state, IBlockDisplayReader light, BlockPos pos, int layer) {
		return pos != null && light != null ? BiomeColors.getAverageGrassColor(light, pos) : GrassColors.get(0.5D, 1.0D);
	}

}
