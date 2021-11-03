package com.simibubi.create.foundation.block.render;

import org.apache.commons.lang3.mutable.MutableInt;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.IBlockRenderProperties;

public class ReducedDestroyEffects implements IBlockRenderProperties {
	
	@Override
	public boolean addDestroyEffects(BlockState state, Level worldIn, BlockPos pos, ParticleEngine manager) {
		if (!(worldIn instanceof ClientLevel))
			return true;
		ClientLevel world = (ClientLevel) worldIn;
		VoxelShape voxelshape = state.getShape(world, pos);
		MutableInt amtBoxes = new MutableInt(0);
		voxelshape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> amtBoxes.increment());
		double chance = 1d / amtBoxes.getValue();

		if (state.isAir())
			return true;

		voxelshape.forAllBoxes((p_172273_, p_172274_, p_172275_, p_172276_, p_172277_, p_172278_) -> {
			double d1 = Math.min(1.0D, p_172276_ - p_172273_);
			double d2 = Math.min(1.0D, p_172277_ - p_172274_);
			double d3 = Math.min(1.0D, p_172278_ - p_172275_);
			int i = Math.max(2, Mth.ceil(d1 / 0.25D));
			int j = Math.max(2, Mth.ceil(d2 / 0.25D));
			int k = Math.max(2, Mth.ceil(d3 / 0.25D));

			for (int l = 0; l < i; ++l) {
				for (int i1 = 0; i1 < j; ++i1) {
					for (int j1 = 0; j1 < k; ++j1) {
						if (world.random.nextDouble() > chance)
							continue;

						double d4 = ((double) l + 0.5D) / (double) i;
						double d5 = ((double) i1 + 0.5D) / (double) j;
						double d6 = ((double) j1 + 0.5D) / (double) k;
						double d7 = d4 * d1 + p_172273_;
						double d8 = d5 * d2 + p_172274_;
						double d9 = d6 * d3 + p_172275_;
						manager.add(new TerrainParticle(world, pos.getX() + d7, pos.getY() + d8, pos.getZ() + d9,
							d4 - 0.5D, d5 - 0.5D, d6 - 0.5D, state, pos).updateSprite(state, pos));
					}
				}
			}
		});
		
		return true;
	}
	
}
