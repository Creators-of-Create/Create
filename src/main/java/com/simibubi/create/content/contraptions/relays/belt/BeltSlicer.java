package com.simibubi.create.content.contraptions.relays.belt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity.CasingType;
import com.simibubi.create.content.contraptions.relays.belt.item.BeltConnectorItem;
import com.simibubi.create.content.contraptions.relays.belt.transport.BeltInventory;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.BlockFlags;

public class BeltSlicer {

	public static class Feedback {
		int color = 0xffffff;
		AxisAlignedBB bb;
		String langKey;
		TextFormatting formatting = TextFormatting.WHITE;
	}

	public static ActionResultType useWrench(BlockState state, World world, BlockPos pos, PlayerEntity player,
		Hand handIn, BlockRayTraceResult hit, Feedback feedBack) {
		BeltTileEntity controllerTE = BeltHelper.getControllerTE(world, pos);
		if (controllerTE == null)
			return ActionResultType.PASS;
		if (state.getValue(BeltBlock.CASING) && hit.getDirection() != Direction.UP)
			return ActionResultType.PASS;
		if (state.getValue(BeltBlock.PART) == BeltPart.PULLEY && hit.getDirection()
			.getAxis() != Axis.Y)
			return ActionResultType.PASS;

		int beltLength = controllerTE.beltLength;
		if (beltLength == 2)
			return ActionResultType.FAIL;

		BlockPos beltVector = new BlockPos(BeltHelper.getBeltVector(state));
		BeltPart part = state.getValue(BeltBlock.PART);
		List<BlockPos> beltChain = BeltBlock.getBeltChain(world, controllerTE.getBlockPos());
		boolean creative = player.isCreative();

		// Shorten from End
		if (hoveringEnd(state, hit)) {
			if (world.isClientSide)
				return ActionResultType.SUCCESS;

			for (BlockPos blockPos : beltChain) {
				BeltTileEntity belt = BeltHelper.getSegmentTE(world, blockPos);
				if (belt == null)
					continue;
				belt.detachKinetics();
				belt.invalidateItemHandler();
				belt.beltLength = 0;
			}

			BeltInventory inventory = controllerTE.inventory;
			BlockPos next = part == BeltPart.END ? pos.subtract(beltVector) : pos.offset(beltVector);
			BlockState replacedState = world.getBlockState(next);
			BeltTileEntity segmentTE = BeltHelper.getSegmentTE(world, next);
			KineticTileEntity.switchToBlockState(world, next,
				state.setValue(BeltBlock.CASING, segmentTE != null && segmentTE.casing != CasingType.NONE));
			world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3 | BlockFlags.IS_MOVING);
			world.removeBlockEntity(pos);
			world.levelEvent(2001, pos, Block.getId(state));

			if (!creative && AllBlocks.BELT.has(replacedState)
				&& replacedState.getValue(BeltBlock.PART) == BeltPart.PULLEY)
				player.inventory.placeItemBackInInventory(world, AllBlocks.SHAFT.asStack());

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
			if (part == BeltPart.START && segmentTE != null && inventory != null) {
				controllerTE.inventory = null;
				segmentTE.inventory = null;
				segmentTE.setController(next);
				for (TransportedItemStack transportedItemStack : inventory.getTransportedItems()) {
					transportedItemStack.beltPosition -= 1;
					if (transportedItemStack.beltPosition <= 0) {
						ItemEntity entity = new ItemEntity(world, pos.getX() + .5f, pos.getY() + 11 / 16f,
							pos.getZ() + .5f, transportedItemStack.stack);
						entity.setDeltaMovement(Vector3d.ZERO);
						entity.setDefaultPickUpDelay();
						entity.hurtMarked = true;
						world.addFreshEntity(entity);
					} else
						segmentTE.getInventory()
							.addItem(transportedItemStack);
				}
			}

			return ActionResultType.SUCCESS;
		}

		BeltTileEntity segmentTE = BeltHelper.getSegmentTE(world, pos);
		if (segmentTE == null)
			return ActionResultType.PASS;

		// Split in half
		int hitSegment = segmentTE.index;
		Vector3d centerOf = VecHelper.getCenterOf(hit.getBlockPos());
		Vector3d subtract = hit.getLocation()
			.subtract(centerOf);
		boolean towardPositive = subtract.dot(Vector3d.atLowerCornerOf(beltVector)) > 0;
		BlockPos next = !towardPositive ? pos.subtract(beltVector) : pos.offset(beltVector);

		if (hitSegment == 0 || hitSegment == 1 && !towardPositive)
			return ActionResultType.FAIL;
		if (hitSegment == controllerTE.beltLength - 1 || hitSegment == controllerTE.beltLength - 2 && towardPositive)
			return ActionResultType.FAIL;

		// Look for shafts
		if (!creative) {
			int requiredShafts = 0;
			if (!segmentTE.hasPulley())
				requiredShafts++;
			BlockState other = world.getBlockState(next);
			if (AllBlocks.BELT.has(other) && other.getValue(BeltBlock.PART) == BeltPart.MIDDLE)
				requiredShafts++;

			int amountRetrieved = 0;
			boolean beltFound = false;
			Search: while (true) {
				for (int i = 0; i < player.inventory.getContainerSize(); ++i) {
					if (amountRetrieved == requiredShafts && beltFound)
						break Search;

					ItemStack itemstack = player.inventory.getItem(i);
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
								player.inventory.setItem(i, ItemStack.EMPTY);
							else
								itemstack.shrink(taken);
						amountRetrieved += taken;
					}
				}

				if (!world.isClientSide)
					player.inventory.placeItemBackInInventory(world, AllBlocks.SHAFT.asStack(amountRetrieved));
				return ActionResultType.FAIL;
			}
		}

		if (!world.isClientSide) {
			for (BlockPos blockPos : beltChain) {
				BeltTileEntity belt = BeltHelper.getSegmentTE(world, blockPos);
				if (belt == null)
					continue;
				belt.detachKinetics();
				belt.invalidateItemHandler();
				belt.beltLength = 0;
			}

			BeltInventory inventory = controllerTE.inventory;
			KineticTileEntity.switchToBlockState(world, pos,
				state.setValue(BeltBlock.PART, towardPositive ? BeltPart.END : BeltPart.START));
			KineticTileEntity.switchToBlockState(world, next, world.getBlockState(next)
				.setValue(BeltBlock.PART, towardPositive ? BeltPart.START : BeltPart.END));
			world.playSound(null, pos, SoundEvents.WOOL_HIT,
				player == null ? SoundCategory.BLOCKS : SoundCategory.PLAYERS, 0.5F, 2.3F);

			// Transfer items to new controller
			BeltTileEntity newController = towardPositive ? BeltHelper.getSegmentTE(world, next) : segmentTE;
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

		return ActionResultType.SUCCESS;
	}

	public static ActionResultType useConnector(BlockState state, World world, BlockPos pos, PlayerEntity player,
		Hand handIn, BlockRayTraceResult hit, Feedback feedBack) {
		BeltTileEntity controllerTE = BeltHelper.getControllerTE(world, pos);
		if (controllerTE == null)
			return ActionResultType.PASS;

		int beltLength = controllerTE.beltLength;
		if (beltLength == BeltConnectorItem.maxLength())
			return ActionResultType.FAIL;

		BlockPos beltVector = new BlockPos(BeltHelper.getBeltVector(state));
		BeltPart part = state.getValue(BeltBlock.PART);
		Direction facing = state.getValue(BeltBlock.HORIZONTAL_FACING);
		List<BlockPos> beltChain = BeltBlock.getBeltChain(world, controllerTE.getBlockPos());
		boolean creative = player.isCreative();

		if (!hoveringEnd(state, hit))
			return ActionResultType.PASS;

		BlockPos next = part == BeltPart.START ? pos.subtract(beltVector) : pos.offset(beltVector);
		BeltTileEntity mergedController = null;
		int mergedBeltLength = 0;

		// Merge Belts / Extend at End
		BlockState nextState = world.getBlockState(next);
		if (!nextState.getMaterial()
			.isReplaceable()) {
			if (!AllBlocks.BELT.has(nextState))
				return ActionResultType.FAIL;
			if (!beltStatesCompatible(state, nextState))
				return ActionResultType.FAIL;

			mergedController = BeltHelper.getControllerTE(world, next);
			if (mergedController == null)
				return ActionResultType.FAIL;
			if (mergedController.beltLength + beltLength > BeltConnectorItem.maxLength())
				return ActionResultType.FAIL;

			mergedBeltLength = mergedController.beltLength;

			if (!world.isClientSide) {
				boolean flipBelt = facing != nextState.getValue(BeltBlock.HORIZONTAL_FACING);
				Optional<DyeColor> color = controllerTE.color;
				for (BlockPos blockPos : BeltBlock.getBeltChain(world, mergedController.getBlockPos())) {
					BeltTileEntity belt = BeltHelper.getSegmentTE(world, blockPos);
					if (belt == null)
						continue;
					belt.detachKinetics();
					belt.invalidateItemHandler();
					belt.beltLength = 0;
					belt.color = color;
					if (flipBelt)
						world.setBlock(blockPos, flipBelt(world.getBlockState(blockPos)), 3 | BlockFlags.IS_MOVING);
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
				BeltTileEntity belt = BeltHelper.getSegmentTE(world, blockPos);
				if (belt == null)
					continue;
				belt.detachKinetics();
				belt.invalidateItemHandler();
				belt.beltLength = 0;
			}

			BeltInventory inventory = controllerTE.inventory;
			KineticTileEntity.switchToBlockState(world, pos, state.setValue(BeltBlock.PART, BeltPart.MIDDLE));

			if (mergedController == null) {
				// Attach at end
				world.setBlock(next, state.setValue(BeltBlock.CASING, false), 3 | BlockFlags.IS_MOVING);
				BeltTileEntity segmentTE = BeltHelper.getSegmentTE(world, next);
				if (segmentTE != null)
					segmentTE.color = controllerTE.color;
				world.playSound(null, pos, SoundEvents.WOOL_PLACE,
					player == null ? SoundCategory.BLOCKS : SoundCategory.PLAYERS, 0.5F, 1F);

				// Transfer items to new controller
				if (part == BeltPart.START && segmentTE != null && inventory != null) {
					segmentTE.setController(next);
					for (TransportedItemStack transportedItemStack : inventory.getTransportedItems()) {
						transportedItemStack.beltPosition += 1;
						segmentTE.getInventory()
							.addItem(transportedItemStack);
					}
				}

			} else {
				// Merge with other
				BeltInventory mergedInventory = mergedController.inventory;
				world.playSound(null, pos, SoundEvents.WOOL_HIT,
					player == null ? SoundCategory.BLOCKS : SoundCategory.PLAYERS, 0.5F, 1.3F);
				BeltTileEntity segmentTE = BeltHelper.getSegmentTE(world, next);
				KineticTileEntity.switchToBlockState(world, next,
					state.setValue(BeltBlock.CASING, segmentTE != null && segmentTE.casing != CasingType.NONE)
						.setValue(BeltBlock.PART, BeltPart.MIDDLE));

				if (!creative) {
					player.inventory.placeItemBackInInventory(world, AllBlocks.SHAFT.asStack(2));
					player.inventory.placeItemBackInInventory(world, AllItems.BELT_CONNECTOR.asStack());
				}

				// Transfer items to other controller
				BlockPos search = controllerTE.getBlockPos();
				for (int i = 0; i < 10000; i++) {
					BlockState blockState = world.getBlockState(search);
					if (!AllBlocks.BELT.has(blockState))
						break;
					if (blockState.getValue(BeltBlock.PART) != BeltPart.START) {
						search = search.subtract(beltVector);
						continue;
					}

					BeltTileEntity newController = BeltHelper.getSegmentTE(world, search);

					if (newController != controllerTE && inventory != null) {
						newController.setController(search);
						controllerTE.inventory = null;
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
							if (newController == controllerTE)
								transportedItemStack.beltPosition += beltLength;
							newController.getInventory()
								.addItem(transportedItemStack);
						}
					}

					break;
				}
			}
		}
		return ActionResultType.SUCCESS;
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

	static boolean hoveringEnd(BlockState state, BlockRayTraceResult hit) {
		BeltPart part = state.getValue(BeltBlock.PART);
		if (part == BeltPart.MIDDLE || part == BeltPart.PULLEY)
			return false;

		Vector3d beltVector = BeltHelper.getBeltVector(state);
		Vector3d centerOf = VecHelper.getCenterOf(hit.getBlockPos());
		Vector3d subtract = hit.getLocation()
			.subtract(centerOf);

		return subtract.dot(beltVector) > 0 == (part == BeltPart.END);
	}

	@OnlyIn(Dist.CLIENT)
	public static void tickHoveringInformation() {
		Minecraft mc = Minecraft.getInstance();
		RayTraceResult target = mc.hitResult;
		if (target == null || !(target instanceof BlockRayTraceResult))
			return;

		BlockRayTraceResult result = (BlockRayTraceResult) target;
		ClientWorld world = mc.level;
		BlockPos pos = result.getBlockPos();
		BlockState state = world.getBlockState(pos);
		ItemStack held = mc.player.getItemInHand(Hand.MAIN_HAND);
		ItemStack heldOffHand = mc.player.getItemInHand(Hand.OFF_HAND);

		if (mc.player.isShiftKeyDown())
			return;
		if (!AllBlocks.BELT.has(state))
			return;

		Feedback feedback = new Feedback();

		// TODO: Populate feedback in the methods for clientside
		if (AllItems.WRENCH.isIn(held) || AllItems.WRENCH.isIn(heldOffHand))
			useWrench(state, world, pos, mc.player, Hand.MAIN_HAND, result, feedback);
		else if (AllItems.BELT_CONNECTOR.isIn(held) || AllItems.BELT_CONNECTOR.isIn(heldOffHand))
			useConnector(state, world, pos, mc.player, Hand.MAIN_HAND, result, feedback);
		else
			return;

		if (feedback.langKey != null)
			mc.player.displayClientMessage(Lang.translate(feedback.langKey)
				.withStyle(feedback.formatting), true);
		else
			mc.player.displayClientMessage(new StringTextComponent(""), true);

		if (feedback.bb != null)
			CreateClient.OUTLINER.chaseAABB("BeltSlicer", feedback.bb)
				.lineWidth(1 / 16f)
				.colored(feedback.color);
	}

}
