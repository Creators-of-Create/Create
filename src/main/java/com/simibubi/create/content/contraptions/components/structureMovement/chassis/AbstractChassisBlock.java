package com.simibubi.create.content.contraptions.components.structureMovement.chassis;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.Iterate;

import me.alphamode.forgetags.Tags;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractChassisBlock extends RotatedPillarBlock implements IWrenchable, ITE<ChassisTileEntity> {

	public AbstractChassisBlock(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
		BlockHitResult hit) {
		if (!player.mayBuild())
			return InteractionResult.PASS;

		ItemStack heldItem = player.getItemInHand(handIn);
		boolean isSlimeBall = heldItem.is(Tags.Items.SLIMEBALLS) || AllItems.SUPER_GLUE.isIn(heldItem);

		BooleanProperty affectedSide = getGlueableSide(state, hit.getDirection());
		if (affectedSide == null)
			return InteractionResult.PASS;

		if (isSlimeBall && state.getValue(affectedSide)) {
			for (Direction face : Iterate.directions) {
				BooleanProperty glueableSide = getGlueableSide(state, face);
				if (glueableSide != null && !state.getValue(glueableSide)
					&& glueAllowedOnSide(worldIn, pos, state, face)) {
					if (worldIn.isClientSide) {
						Vec3 vec = hit.getLocation();
						worldIn.addParticle(ParticleTypes.ITEM_SLIME, vec.x, vec.y, vec.z, 0, 0, 0);
						return InteractionResult.SUCCESS;
					}
					AllSoundEvents.SLIME_ADDED.playOnServer(worldIn, pos, .5f, 1);
					state = state.setValue(glueableSide, true);
				}
			}
			if (!worldIn.isClientSide)
				worldIn.setBlockAndUpdate(pos, state);
			return InteractionResult.SUCCESS;
		}

		if ((!heldItem.isEmpty() || !player.isShiftKeyDown()) && !isSlimeBall)
			return InteractionResult.PASS;
		if (state.getValue(affectedSide) == isSlimeBall)
			return InteractionResult.PASS;
		if (!glueAllowedOnSide(worldIn, pos, state, hit.getDirection()))
			return InteractionResult.PASS;
		if (worldIn.isClientSide) {
			Vec3 vec = hit.getLocation();
			worldIn.addParticle(ParticleTypes.ITEM_SLIME, vec.x, vec.y, vec.z, 0, 0, 0);
			return InteractionResult.SUCCESS;
		}

		AllSoundEvents.SLIME_ADDED.playOnServer(worldIn, pos, .5f, 1);
		worldIn.setBlockAndUpdate(pos, state.setValue(affectedSide, isSlimeBall));
		return InteractionResult.SUCCESS;
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		if (rotation == Rotation.NONE)
			return state;

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

	protected boolean glueAllowedOnSide(BlockGetter world, BlockPos pos, BlockState state, Direction side) {
		return true;
	}

	@Override
	public Class<ChassisTileEntity> getTileEntityClass() {
		return ChassisTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends ChassisTileEntity> getTileEntityType() {
		return AllTileEntities.CHASSIS.get();
	}

}
