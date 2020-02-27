package com.simibubi.create.modules.contraptions.components.contraptions.chassis;

import com.simibubi.create.AllSoundEvents;

import net.minecraft.block.BlockState;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;

public abstract class AbstractChassisBlock extends RotatedPillarBlock {

	public AbstractChassisBlock(Properties properties) {
		super(properties);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new ChassisTileEntity();
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {
		if (!player.isAllowEdit())
			return false;

		ItemStack heldItem = player.getHeldItem(handIn);
		boolean isSlimeBall = heldItem.getItem().isIn(Tags.Items.SLIMEBALLS);

		BooleanProperty affectedSide = getGlueableSide(state, hit.getFace());
		if (affectedSide == null)
			return false;

		if (isSlimeBall && state.get(affectedSide)) {
			for (Direction face : Direction.values()) {
				BooleanProperty glueableSide = getGlueableSide(state, face);
				if (glueableSide != null && !state.get(glueableSide)) {
					if (worldIn.isRemote) {
						Vec3d vec = hit.getHitVec();
						worldIn.addParticle(ParticleTypes.ITEM_SLIME, vec.x, vec.y, vec.z, 0, 0, 0);
						return true;
					}
					worldIn.playSound(null, pos, AllSoundEvents.SLIME_ADDED.get(), SoundCategory.BLOCKS, .5f, 1);
					if (!player.isCreative())
						heldItem.shrink(1);
					state = state.with(glueableSide, true);
				}
			}
			if (!worldIn.isRemote)
				worldIn.setBlockState(pos, state);
			return true;
		}

		if ((!heldItem.isEmpty() || !player.isSneaking()) && !isSlimeBall)
			return false;
		if (state.get(affectedSide) == isSlimeBall)
			return false;
		if (worldIn.isRemote) {
			Vec3d vec = hit.getHitVec();
			worldIn.addParticle(ParticleTypes.ITEM_SLIME, vec.x, vec.y, vec.z, 0, 0, 0);
			return true;
		}

		worldIn.playSound(null, pos, AllSoundEvents.SLIME_ADDED.get(), SoundCategory.BLOCKS, .5f, 1);
		worldIn.setBlockState(pos, state.with(affectedSide, isSlimeBall));
		return true;
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		if (rotation == Rotation.NONE)
			return state;

		@SuppressWarnings("deprecation")
		BlockState rotated = super.rotate(state, rotation);
		for (Direction face : Direction.values()) {
			BooleanProperty glueableSide = getGlueableSide(rotated, face);
			if (glueableSide != null)
				rotated = rotated.with(glueableSide, false);
		}

		for (Direction face : Direction.values()) {
			BooleanProperty glueableSide = getGlueableSide(state, face);
			if (glueableSide == null || !state.get(glueableSide))
				continue;
			Direction rotatedFacing = rotation.rotate(face);
			BooleanProperty rotatedGlueableSide = getGlueableSide(rotated, rotatedFacing);
			if (rotatedGlueableSide != null)
				rotated = rotated.with(rotatedGlueableSide, true);
		}

		return rotated;
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		if (mirrorIn == Mirror.NONE)
			return state;

		BlockState mirrored = state;
		for (Direction face : Direction.values()) {
			BooleanProperty glueableSide = getGlueableSide(mirrored, face);
			if (glueableSide != null)
				mirrored = mirrored.with(glueableSide, false);
		}

		for (Direction face : Direction.values()) {
			BooleanProperty glueableSide = getGlueableSide(state, face);
			if (glueableSide == null || !state.get(glueableSide))
				continue;
			Direction mirroredFacing = mirrorIn.mirror(face);
			BooleanProperty mirroredGlueableSide = getGlueableSide(mirrored, mirroredFacing);
			if (mirroredGlueableSide != null)
				mirrored = mirrored.with(mirroredGlueableSide, true);
		}

		return mirrored;
	}

	public abstract BooleanProperty getGlueableSide(BlockState state, Direction face);

}
