package com.simibubi.create.content.contraptions.components.structureMovement.chassis;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.block.BlockState;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;

import net.minecraft.block.AbstractBlock.Properties;

public abstract class AbstractChassisBlock extends RotatedPillarBlock implements IWrenchable {

	public AbstractChassisBlock(Properties properties) {
		super(properties);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.CHASSIS.create();
	}

	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
		BlockRayTraceResult hit) {
		if (!player.mayBuild())
			return ActionResultType.PASS;

		ItemStack heldItem = player.getItemInHand(handIn);
		boolean isSlimeBall = heldItem.getItem()
			.is(Tags.Items.SLIMEBALLS) || AllItems.SUPER_GLUE.isIn(heldItem);

		BooleanProperty affectedSide = getGlueableSide(state, hit.getDirection());
		if (affectedSide == null)
			return ActionResultType.PASS;

		if (isSlimeBall && state.getValue(affectedSide)) {
			for (Direction face : Iterate.directions) {
				BooleanProperty glueableSide = getGlueableSide(state, face);
				if (glueableSide != null && !state.getValue(glueableSide) && glueAllowedOnSide(worldIn, pos, state, face)) {
					if (worldIn.isClientSide) {
						Vector3d vec = hit.getLocation();
						worldIn.addParticle(ParticleTypes.ITEM_SLIME, vec.x, vec.y, vec.z, 0, 0, 0);
						return ActionResultType.SUCCESS;
					}
					AllSoundEvents.SLIME_ADDED.playOnServer(worldIn, pos, .5f, 1);
					state = state.setValue(glueableSide, true);
				}
			}
			if (!worldIn.isClientSide)
				worldIn.setBlockAndUpdate(pos, state);
			return ActionResultType.SUCCESS;
		}

		if ((!heldItem.isEmpty() || !player.isShiftKeyDown()) && !isSlimeBall)
			return ActionResultType.PASS;
		if (state.getValue(affectedSide) == isSlimeBall)
			return ActionResultType.PASS;
		if (!glueAllowedOnSide(worldIn, pos, state, hit.getDirection()))
			return ActionResultType.PASS;
		if (worldIn.isClientSide) {
			Vector3d vec = hit.getLocation();
			worldIn.addParticle(ParticleTypes.ITEM_SLIME, vec.x, vec.y, vec.z, 0, 0, 0);
			return ActionResultType.SUCCESS;
		}

		AllSoundEvents.SLIME_ADDED.playOnServer(worldIn, pos, .5f, 1);
		worldIn.setBlockAndUpdate(pos, state.setValue(affectedSide, isSlimeBall));
		return ActionResultType.SUCCESS;
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		if (rotation == Rotation.NONE)
			return state;

		@SuppressWarnings("deprecation")
		BlockState rotated = super.rotate(state, rotation);
		for (Direction face : Iterate.directions) {
			BooleanProperty glueableSide = getGlueableSide(rotated, face);
			if (glueableSide != null)
				rotated = rotated.setValue(glueableSide, false);
		}

		for (Direction face : Iterate.directions) {
			BooleanProperty glueableSide = getGlueableSide(state, face);
			if (glueableSide == null || !state.getValue(glueableSide))
				continue;
			Direction rotatedFacing = rotation.rotate(face);
			BooleanProperty rotatedGlueableSide = getGlueableSide(rotated, rotatedFacing);
			if (rotatedGlueableSide != null)
				rotated = rotated.setValue(rotatedGlueableSide, true);
		}

		return rotated;
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		if (mirrorIn == Mirror.NONE)
			return state;

		BlockState mirrored = state;
		for (Direction face : Iterate.directions) {
			BooleanProperty glueableSide = getGlueableSide(mirrored, face);
			if (glueableSide != null)
				mirrored = mirrored.setValue(glueableSide, false);
		}

		for (Direction face : Iterate.directions) {
			BooleanProperty glueableSide = getGlueableSide(state, face);
			if (glueableSide == null || !state.getValue(glueableSide))
				continue;
			Direction mirroredFacing = mirrorIn.mirror(face);
			BooleanProperty mirroredGlueableSide = getGlueableSide(mirrored, mirroredFacing);
			if (mirroredGlueableSide != null)
				mirrored = mirrored.setValue(mirroredGlueableSide, true);
		}

		return mirrored;
	}

	public abstract BooleanProperty getGlueableSide(BlockState state, Direction face);

	protected boolean glueAllowedOnSide(IBlockReader world, BlockPos pos, BlockState state, Direction side) {
		return true;
	}

}
