package com.simibubi.create.content.logistics.trains.track;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.trains.ITrackBlock;
import com.simibubi.create.content.logistics.trains.track.TrackPlacement.PlacementInfo;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TrackBlockItem extends BlockItem {

	public TrackBlockItem(Block pBlock, Properties pProperties) {
		super(pBlock, pProperties);
	}

	@Override
	public InteractionResult useOn(UseOnContext pContext) {
		ItemStack stack = pContext.getItemInHand();
		BlockPos pos = pContext.getClickedPos();
		Level level = pContext.getLevel();
		BlockState state = level.getBlockState(pos);
		Player player = pContext.getPlayer();

		if (player == null)
			return super.useOn(pContext);

		Vec3 lookAngle = player.getLookAngle();

		if (!isFoil(stack)) {
			if (state.getBlock() instanceof TrackBlock track && track.getTrackAxes(level, pos, state)
				.size() > 1) {
				if (!level.isClientSide)
					player.displayClientMessage(Lang.translate("track.junction_start")
						.withStyle(ChatFormatting.RED), true);
				return InteractionResult.SUCCESS;
			}

			if (select(level, pos, lookAngle, stack))
				return InteractionResult.SUCCESS;
			return super.useOn(pContext);

		} else if (player.isSteppingCarefully()) {
			if (!level.isClientSide) {
				player.displayClientMessage(Lang.translate("track.selection_cleared"), true);
				stack.setTag(null);
			}
			return InteractionResult.SUCCESS;
		}

		boolean placing = !(state.getBlock() instanceof ITrackBlock);
		if (placing) {
			if (!state.getMaterial()
				.isReplaceable())
				pos = pos.relative(pContext.getClickedFace());
			state = getPlacementState(pContext);
			if (state == null)
				return InteractionResult.FAIL;
		}

		ItemStack offhandItem = player.getOffhandItem();
		PlacementInfo info =
			TrackPlacement.tryConnect(level, pos, state, lookAngle, stack, AllBlocks.METAL_GIRDER.isIn(offhandItem));

		if (info.message != null && !level.isClientSide)
			player.displayClientMessage(Lang.translate(info.message), true);
		if (!info.valid)
			return InteractionResult.FAIL;

		stack.setTag(null);

		if (level.isClientSide)
			return InteractionResult.SUCCESS;

		if (offhandItem.getItem() instanceof BlockItem blockItem) {
			Block block = blockItem.getBlock();
			if (block == null)
				return InteractionResult.SUCCESS;
			if (block instanceof EntityBlock)
				return InteractionResult.SUCCESS;

			for (boolean first : Iterate.trueAndFalse) {
				int extent = (first ? info.end1Extent : info.end2Extent) + (info.curve != null ? 1 : 0);
				Vec3 axis = first ? info.axis1 : info.axis2;
				BlockPos pavePos = first ? info.pos1 : info.pos2;
				TrackPaver.paveStraight(level, pavePos.below(), axis, extent, block);
			}

			if (info.curve != null)
				TrackPaver.paveCurve(level, info.curve, block);
		}

		return InteractionResult.SUCCESS;
	}

	public BlockState getPlacementState(UseOnContext pContext) {
		return getPlacementState(updatePlacementContext(new BlockPlaceContext(pContext)));
	}

	public static boolean select(LevelAccessor world, BlockPos pos, Vec3 lookVec, ItemStack heldItem) {
		BlockState blockState = world.getBlockState(pos);
		Block block = blockState.getBlock();
		if (!(block instanceof ITrackBlock))
			return false;

		ITrackBlock track = (ITrackBlock) block;
		Pair<Vec3, AxisDirection> nearestTrackAxis = track.getNearestTrackAxis(world, pos, blockState, lookVec);
		Vec3 axis = nearestTrackAxis.getFirst()
			.scale(nearestTrackAxis.getSecond() == AxisDirection.POSITIVE ? -1 : 1);
		Vec3 end = track.getCurveStart(world, pos, blockState, axis);
		Vec3 normal = track.getUpNormal(world, pos, blockState)
			.normalize();

		CompoundTag compoundTag = heldItem.getOrCreateTagElement("ConnectingFrom");
		compoundTag.put("Pos", NbtUtils.writeBlockPos(pos));
		compoundTag.put("Axis", VecHelper.writeNBT(axis));
		compoundTag.put("Normal", VecHelper.writeNBT(normal));
		compoundTag.put("End", VecHelper.writeNBT(end));
		return true;
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return stack.hasTag() && stack.getTag()
			.contains("ConnectingFrom");
	}

}
