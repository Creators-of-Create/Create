package com.simibubi.create.content.kinetics.belt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity.CasingType;
import com.simibubi.create.content.kinetics.belt.item.BeltConnectorItem;
import com.simibubi.create.content.kinetics.belt.transport.BeltInventory;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.CatnipClient;
import net.createmod.catnip.utility.VecHelper;
import net.createmod.catnip.utility.lang.Components;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BeltSlicer {

	public static class Feedback {
		int color = 0xffffff;
		AABB bb;
		String langKey;
		ChatFormatting formatting = ChatFormatting.WHITE;
	}

	public static InteractionResult useWrench(BlockState state, Level world, BlockPos pos, Player player,
		InteractionHand handIn, BlockHitResult hit, Feedback feedBack) {
		BeltBlockEntity controllerBE = BeltHelper.getControllerBE(world, pos);
		if (controllerBE == null)
			return InteractionResult.PASS;
		if (state.getValue(BeltBlock.CASING) && hit.getDirection() != Direction.UP)
			return InteractionResult.PASS;
		if (state.getValue(BeltBlock.PART) == BeltPart.PULLEY && hit.getDirection()
			.getAxis() != Axis.Y)
			return InteractionResult.PASS;

		int beltLength = controllerBE.beltLength;
		if (beltLength == 2)
			return InteractionResult.FAIL;

		BlockPos beltVector = new BlockPos(BeltHelper.getBeltVector(state));
		BeltPart part = state.getValue(BeltBlock.PART);
		List<BlockPos> beltChain = BeltBlock.getBeltChain(world, controllerBE.getBlockPos());
		boolean creative = player.isCreative();

		// Shorten from End
		if (hoveringEnd(state, hit)) {
			if (world.isClientSide)
				return InteractionResult.SUCCESS;

			for (BlockPos blockPos : beltChain) {
				BeltBlockEntity belt = BeltHelper.getSegmentBE(world, blockPos);
				if (belt == null)
					continue;
				belt.detachKinetics();
				belt.invalidateItemHandler();
				belt.beltLength = 0;
			}

			BeltInventory inventory = controllerBE.inventory;
			BlockPos next = part == BeltPart.END ? pos.subtract(beltVector) : pos.offset(beltVector);
			BlockState replacedState = world.getBlockState(next);
			BeltBlockEntity segmentBE = BeltHelper.getSegmentBE(world, next);
			KineticBlockEntity.switchToBlockState(world, next, ProperWaterloggedBlock.withWater(world,
				state.setValue(BeltBlock.CASING, segmentBE != null && segmentBE.casing != CasingType.NONE), next));
			world.setBlock(pos, ProperWaterloggedBlock.withWater(world, Blocks.AIR.defaultBlockState(), pos),
				Block.UPDATE_ALL | Block.UPDATE_MOVE_BY_PISTON);
			world.removeBlockEntity(pos);
			world.levelEvent(2001, pos, Block.getId(state));

			if (!creative && AllBlocks.BELT.has(replacedState)
				&& replacedState.getValue(BeltBlock.PART) == BeltPart.PULLEY)
				player.getInventory().placeItemBackInInventory(AllBlocks.SHAFT.asStack());

			// Eject overshooting items
			if (part == BeltPart.END && inventory != null) {
				List<TransportedItemStack> toEject = new ArrayList<>();
				for (TransportedItemStack transportedItemStack : inventory.getTransportedItems())
					if (transportedItemStack.beltPosition > beltLength - 1)
						toEject.add(transportedItemStack);
				toEject.forEach(inventory::eject);
				toEject.forEach(inventory.getTransportedItems()::remove);
			}

			// Transfer items to new controller
			if (part == BeltPart.START && segmentBE != null && inventory != null) {
				controllerBE.inventory = null;
				segmentBE.inventory = null;
				segmentBE.setController(next);
				for (TransportedItemStack transportedItemStack : inventory.getTransportedItems()) {
					transportedItemStack.beltPosition -= 1;
					if (transportedItemStack.beltPosition <= 0) {
						ItemEntity entity = new ItemEntity(world, pos.getX() + .5f, pos.getY() + 11 / 16f,
							pos.getZ() + .5f, transportedItemStack.stack);
						entity.setDeltaMovement(Vec3.ZERO);
						entity.setDefaultPickUpDelay();
						entity.hurtMarked = true;
						world.addFreshEntity(entity);
					} else
						segmentBE.getInventory()
							.addItem(transportedItemStack);
				}
			}

			return InteractionResult.SUCCESS;
		}

		BeltBlockEntity segmentBE = BeltHelper.getSegmentBE(world, pos);
		if (segmentBE == null)
			return InteractionResult.PASS;

		// Split in half
		int hitSegment = segmentBE.index;
		Vec3 centerOf = VecHelper.getCenterOf(hit.getBlockPos());
		Vec3 subtract = hit.getLocation()
			.subtract(centerOf);
		boolean towardPositive = subtract.dot(Vec3.atLowerCornerOf(beltVector)) > 0;
		BlockPos next = !towardPositive ? pos.subtract(beltVector) : pos.offset(beltVector);

		if (hitSegment == 0 || hitSegment == 1 && !towardPositive)
			return InteractionResult.FAIL;
		if (hitSegment == controllerBE.beltLength - 1 || hitSegment == controllerBE.beltLength - 2 && towardPositive)
			return InteractionResult.FAIL;

		// Look for shafts
		if (!creative) {
			int requiredShafts = 0;
			if (!segmentBE.hasPulley())
				requiredShafts++;
			BlockState other = world.getBlockState(next);
			if (AllBlocks.BELT.has(other) && other.getValue(BeltBlock.PART) == BeltPart.MIDDLE)
				requiredShafts++;

			int amountRetrieved = 0;
			boolean beltFound = false;
			Search: while (true) {
				for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
					if (amountRetrieved == requiredShafts && beltFound)
						break Search;

					ItemStack itemstack = player.getInventory().getItem(i);
					if (itemstack.isEmpty())
						continue;
					int count = itemstack.getCount();

					if (AllItems.BELT_CONNECTOR.isIn(itemstack)) {
						if (!world.isClientSide)
							itemstack.shrink(1);
						beltFound = true;
						continue;
					}

					if (AllBlocks.SHAFT.isIn(itemstack)) {
						int taken = Math.min(count, requiredShafts - amountRetrieved);
						if (!world.isClientSide)
							if (taken == count)
								player.getInventory().setItem(i, ItemStack.EMPTY);
							else
								itemstack.shrink(taken);
						amountRetrieved += taken;
					}
				}

				if (!world.isClientSide)
					player.getInventory().placeItemBackInInventory(AllBlocks.SHAFT.asStack(amountRetrieved));
				return InteractionResult.FAIL;
			}
		}

		if (!world.isClientSide) {
			for (BlockPos blockPos : beltChain) {
				BeltBlockEntity belt = BeltHelper.getSegmentBE(world, blockPos);
				if (belt == null)
					continue;
				belt.detachKinetics();
				belt.invalidateItemHandler();
				belt.beltLength = 0;
			}

			BeltInventory inventory = controllerBE.inventory;
			KineticBlockEntity.switchToBlockState(world, pos,
				state.setValue(BeltBlock.PART, towardPositive ? BeltPart.END : BeltPart.START));
			KineticBlockEntity.switchToBlockState(world, next, world.getBlockState(next)
				.setValue(BeltBlock.PART, towardPositive ? BeltPart.START : BeltPart.END));
			world.playSound(null, pos, SoundEvents.WOOL_HIT,
				player == null ? SoundSource.BLOCKS : SoundSource.PLAYERS, 0.5F, 2.3F);

			// Transfer items to new controller
			BeltBlockEntity newController = towardPositive ? BeltHelper.getSegmentBE(world, next) : segmentBE;
			if (newController != null && inventory != null) {
				newController.inventory = null;
				newController.setController(newController.getBlockPos());
				for (Iterator<TransportedItemStack> iterator = inventory.getTransportedItems()
					.iterator(); iterator.hasNext();) {
					TransportedItemStack transportedItemStack = iterator.next();
					float newPosition = transportedItemStack.beltPosition - hitSegment - (towardPositive ? 1 : 0);
					if (newPosition <= 0)
						continue;
					transportedItemStack.beltPosition = newPosition;
					iterator.remove();
					newController.getInventory()
						.addItem(transportedItemStack);
				}
			}
		}

		return InteractionResult.SUCCESS;
	}

	public static InteractionResult useConnector(BlockState state, Level world, BlockPos pos, Player player,
		InteractionHand handIn, BlockHitResult hit, Feedback feedBack) {
		BeltBlockEntity controllerBE = BeltHelper.getControllerBE(world, pos);
		if (controllerBE == null)
			return InteractionResult.PASS;

		int beltLength = controllerBE.beltLength;
		if (beltLength == BeltConnectorItem.maxLength())
			return InteractionResult.FAIL;

		BlockPos beltVector = new BlockPos(BeltHelper.getBeltVector(state));
		BeltPart part = state.getValue(BeltBlock.PART);
		Direction facing = state.getValue(BeltBlock.HORIZONTAL_FACING);
		List<BlockPos> beltChain = BeltBlock.getBeltChain(world, controllerBE.getBlockPos());
		boolean creative = player.isCreative();

		if (!hoveringEnd(state, hit))
			return InteractionResult.PASS;

		BlockPos next = part == BeltPart.START ? pos.subtract(beltVector) : pos.offset(beltVector);
		BeltBlockEntity mergedController = null;
		int mergedBeltLength = 0;

		// Merge Belts / Extend at End
		BlockState nextState = world.getBlockState(next);
		if (!nextState.getMaterial()
			.isReplaceable()) {
			if (!AllBlocks.BELT.has(nextState))
				return InteractionResult.FAIL;
			if (!beltStatesCompatible(state, nextState))
				return InteractionResult.FAIL;

			mergedController = BeltHelper.getControllerBE(world, next);
			if (mergedController == null)
				return InteractionResult.FAIL;
			if (mergedController.beltLength + beltLength > BeltConnectorItem.maxLength())
				return InteractionResult.FAIL;

			mergedBeltLength = mergedController.beltLength;

			if (!world.isClientSide) {
				boolean flipBelt = facing != nextState.getValue(BeltBlock.HORIZONTAL_FACING);
				Optional<DyeColor> color = controllerBE.color;
				for (BlockPos blockPos : BeltBlock.getBeltChain(world, mergedController.getBlockPos())) {
					BeltBlockEntity belt = BeltHelper.getSegmentBE(world, blockPos);
					if (belt == null)
						continue;
					belt.detachKinetics();
					belt.invalidateItemHandler();
					belt.beltLength = 0;
					belt.color = color;
					if (flipBelt)
						world.setBlock(blockPos, flipBelt(world.getBlockState(blockPos)), Block.UPDATE_ALL | Block.UPDATE_MOVE_BY_PISTON);
				}

				// Reverse items
				if (flipBelt && mergedController.inventory != null) {
					List<TransportedItemStack> transportedItems = mergedController.inventory.getTransportedItems();
					for (TransportedItemStack transportedItemStack : transportedItems) {
						transportedItemStack.beltPosition = mergedBeltLength - transportedItemStack.beltPosition;
						transportedItemStack.prevBeltPosition =
							mergedBeltLength - transportedItemStack.prevBeltPosition;
					}
				}
			}
		}

		if (!world.isClientSide) {
			for (BlockPos blockPos : beltChain) {
				BeltBlockEntity belt = BeltHelper.getSegmentBE(world, blockPos);
				if (belt == null)
					continue;
				belt.detachKinetics();
				belt.invalidateItemHandler();
				belt.beltLength = 0;
			}

			BeltInventory inventory = controllerBE.inventory;
			KineticBlockEntity.switchToBlockState(world, pos, state.setValue(BeltBlock.PART, BeltPart.MIDDLE));

			if (mergedController == null) {
				// Attach at end
				world.setBlock(next,
					ProperWaterloggedBlock.withWater(world, state.setValue(BeltBlock.CASING, false), next),
					Block.UPDATE_ALL | Block.UPDATE_MOVE_BY_PISTON);
				BeltBlockEntity segmentBE = BeltHelper.getSegmentBE(world, next);
				if (segmentBE != null)
					segmentBE.color = controllerBE.color;
				world.playSound(null, pos, SoundEvents.WOOL_PLACE,
					player == null ? SoundSource.BLOCKS : SoundSource.PLAYERS, 0.5F, 1F);

				// Transfer items to new controller
				if (part == BeltPart.START && segmentBE != null && inventory != null) {
					segmentBE.setController(next);
					for (TransportedItemStack transportedItemStack : inventory.getTransportedItems()) {
						transportedItemStack.beltPosition += 1;
						segmentBE.getInventory()
							.addItem(transportedItemStack);
					}
				}

			} else {
				// Merge with other
				BeltInventory mergedInventory = mergedController.inventory;
				world.playSound(null, pos, SoundEvents.WOOL_HIT,
					player == null ? SoundSource.BLOCKS : SoundSource.PLAYERS, 0.5F, 1.3F);
				BeltBlockEntity segmentBE = BeltHelper.getSegmentBE(world, next);
				KineticBlockEntity.switchToBlockState(world, next,
					state.setValue(BeltBlock.CASING, segmentBE != null && segmentBE.casing != CasingType.NONE)
						.setValue(BeltBlock.PART, BeltPart.MIDDLE));

				if (!creative) {
					player.getInventory().placeItemBackInInventory(AllBlocks.SHAFT.asStack(2));
					player.getInventory().placeItemBackInInventory(AllItems.BELT_CONNECTOR.asStack());
				}

				// Transfer items to other controller
				BlockPos search = controllerBE.getBlockPos();
				for (int i = 0; i < 10000; i++) {
					BlockState blockState = world.getBlockState(search);
					if (!AllBlocks.BELT.has(blockState))
						break;
					if (blockState.getValue(BeltBlock.PART) != BeltPart.START) {
						search = search.subtract(beltVector);
						continue;
					}

					BeltBlockEntity newController = BeltHelper.getSegmentBE(world, search);

					if (newController != controllerBE && inventory != null) {
						newController.setController(search);
						controllerBE.inventory = null;
						for (TransportedItemStack transportedItemStack : inventory.getTransportedItems()) {
							transportedItemStack.beltPosition += mergedBeltLength;
							newController.getInventory()
								.addItem(transportedItemStack);
						}
					}

					if (newController != mergedController && mergedInventory != null) {
						newController.setController(search);
						mergedController.inventory = null;
						for (TransportedItemStack transportedItemStack : mergedInventory.getTransportedItems()) {
							if (newController == controllerBE)
								transportedItemStack.beltPosition += beltLength;
							newController.getInventory()
								.addItem(transportedItemStack);
						}
					}

					break;
				}
			}
		}
		return InteractionResult.SUCCESS;
	}

	static boolean beltStatesCompatible(BlockState state, BlockState nextState) {
		Direction facing1 = state.getValue(BeltBlock.HORIZONTAL_FACING);
		BeltSlope slope1 = state.getValue(BeltBlock.SLOPE);
		Direction facing2 = nextState.getValue(BeltBlock.HORIZONTAL_FACING);
		BeltSlope slope2 = nextState.getValue(BeltBlock.SLOPE);

		switch (slope1) {
		case UPWARD:
			if (slope2 == BeltSlope.DOWNWARD)
				return facing1 == facing2.getOpposite();
			return slope2 == slope1 && facing1 == facing2;
		case DOWNWARD:
			if (slope2 == BeltSlope.UPWARD)
				return facing1 == facing2.getOpposite();
			return slope2 == slope1 && facing1 == facing2;
		default:
			return slope2 == slope1 && facing2.getAxis() == facing1.getAxis();
		}
	}

	static BlockState flipBelt(BlockState state) {
		Direction facing = state.getValue(BeltBlock.HORIZONTAL_FACING);
		BeltSlope slope = state.getValue(BeltBlock.SLOPE);
		BeltPart part = state.getValue(BeltBlock.PART);

		if (slope == BeltSlope.UPWARD)
			state = state.setValue(BeltBlock.SLOPE, BeltSlope.DOWNWARD);
		else if (slope == BeltSlope.DOWNWARD)
			state = state.setValue(BeltBlock.SLOPE, BeltSlope.UPWARD);

		if (part == BeltPart.END)
			state = state.setValue(BeltBlock.PART, BeltPart.START);
		else if (part == BeltPart.START)
			state = state.setValue(BeltBlock.PART, BeltPart.END);

		return state.setValue(BeltBlock.HORIZONTAL_FACING, facing.getOpposite());
	}

	static boolean hoveringEnd(BlockState state, BlockHitResult hit) {
		BeltPart part = state.getValue(BeltBlock.PART);
		if (part == BeltPart.MIDDLE || part == BeltPart.PULLEY)
			return false;

		Vec3 beltVector = BeltHelper.getBeltVector(state);
		Vec3 centerOf = VecHelper.getCenterOf(hit.getBlockPos());
		Vec3 subtract = hit.getLocation()
			.subtract(centerOf);

		return subtract.dot(beltVector) > 0 == (part == BeltPart.END);
	}

	@OnlyIn(Dist.CLIENT)
	public static void tickHoveringInformation() {
		Minecraft mc = Minecraft.getInstance();
		HitResult target = mc.hitResult;
		if (target == null || !(target instanceof BlockHitResult))
			return;

		BlockHitResult result = (BlockHitResult) target;
		ClientLevel world = mc.level;
		BlockPos pos = result.getBlockPos();
		BlockState state = world.getBlockState(pos);
		ItemStack held = mc.player.getItemInHand(InteractionHand.MAIN_HAND);
		ItemStack heldOffHand = mc.player.getItemInHand(InteractionHand.OFF_HAND);

		if (mc.player.isShiftKeyDown())
			return;
		if (!AllBlocks.BELT.has(state))
			return;

		Feedback feedback = new Feedback();

		// TODO: Populate feedback in the methods for clientside
		if (AllItems.WRENCH.isIn(held) || AllItems.WRENCH.isIn(heldOffHand))
			useWrench(state, world, pos, mc.player, InteractionHand.MAIN_HAND, result, feedback);
		else if (AllItems.BELT_CONNECTOR.isIn(held) || AllItems.BELT_CONNECTOR.isIn(heldOffHand))
			useConnector(state, world, pos, mc.player, InteractionHand.MAIN_HAND, result, feedback);
		else
			return;

		if (feedback.langKey != null)
			mc.player.displayClientMessage(CreateLang.translateDirect(feedback.langKey)
				.withStyle(feedback.formatting), true);
		else
			mc.player.displayClientMessage(Components.immutableEmpty(), true);

		if (feedback.bb != null)
			CatnipClient.OUTLINER.chaseAABB("BeltSlicer", feedback.bb)
				.lineWidth(1 / 16f)
				.colored(feedback.color);
	}

}
