package com.simibubi.create.foundation.utility;

import java.util.Optional;

import javax.annotation.Nullable;

import com.simibubi.create.content.optics.ILightHandlerProvider;
import com.simibubi.create.foundation.block.ITE;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IBeaconBeamColorProvider;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;

public class BeaconHelper {
	private BeaconHelper() {
	}

	public static Optional<BeaconTileEntity> getBeaconTE(@Nullable BlockPos testPos, @Nullable IBlockReader world) {
		if (testPos == null || world == null)
			return Optional.empty();
		while (testPos.getY() > 0) {
			testPos = testPos.down();
			BlockState state = world.getBlockState(testPos);
			if (getCorrectedOpacity(state, world, testPos) >= 15 && state.getBlock() != Blocks.BEDROCK)
				break;
			if (state.getBlock() == Blocks.BEACON) {
				TileEntity te = world.getTileEntity(testPos);

				if (!(te instanceof BeaconTileEntity))
					break;
				return Optional.of((BeaconTileEntity) te);
			}
		}
		return Optional.empty();
	}

	public static Optional<BeaconTileEntity> getBeaconTE(Vector3d pos, World world) {
		int testX = MathHelper.floor(pos.getX());
		int testZ = MathHelper.floor(pos.getZ());
		int localWorldHeight = world.getHeight(Heightmap.Type.WORLD_SURFACE, testX, testZ);
		return getBeaconTE(new BlockPos(testX, Math.min(MathHelper.floor(pos.getY()), localWorldHeight), testZ), world);
	}

	public static boolean isAboveActiveBeacon(Vector3d pos, World world) {
		return getBeaconTE(pos, world).filter(bte -> bte.getLevels() != 0 && !bte.beamSegments.isEmpty())
				.isPresent();
	}

	@Nullable
	public static float[] getBeaconColorFor(Block block) {
		if (!(block instanceof IBeaconBeamColorProvider))
			return null;
		return ((IBeaconBeamColorProvider) block).getColor()
				.getColorComponentValues();
	}

	public static int getCorrectedOpacity(BlockState state, IBlockReader world, BlockPos pos) {
		try {
			if (state.getBlock() instanceof ITE) {
				TileEntity te = ((ITE<?>) state.getBlock()).getTileEntity(world, pos);
				if (te instanceof ILightHandlerProvider && ((ILightHandlerProvider) te).getHandler()
						.absorbsLight())
					return 15;
			}
		} catch (ITE.TileEntityException ignored) {
		}
		return state.getOpacity(world, pos);
	}
}
