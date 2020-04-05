package com.simibubi.create.modules.logistics.block.belts;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.foundation.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.behaviour.inventory.SingleTargetAutoExtractingBehaviour;
import com.simibubi.create.modules.contraptions.relays.belt.AllBeltAttachments.BeltAttachmentState;
import com.simibubi.create.modules.contraptions.relays.belt.AllBeltAttachments.IBeltAttachment;
import com.simibubi.create.modules.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.modules.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.modules.logistics.block.AttachedLogisticalBlock;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public abstract class BeltAttachableLogisticalBlock extends AttachedLogisticalBlock implements IBeltAttachment {

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		onAttachmentPlaced(worldIn, pos, state);
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		onAttachmentRemoved(worldIn, pos, state);
		if (state.hasTileEntity() && state.getBlock() != newState.getBlock()) {
			TileEntityBehaviour.destroy(worldIn, pos, FilteringBehaviour.TYPE);
			worldIn.removeTileEntity(pos);
		}
	}

	@Override
	public BlockPos getBeltPositionForAttachment(IWorld world, BlockPos pos, BlockState state) {
		return pos.offset(getBlockFacing(state));
	}

	@Override
	public List<BlockPos> getPotentialAttachmentPositions(IWorld world, BlockPos pos, BlockState beltState) {
		return Arrays.asList(Direction.values()).stream().filter(d -> d != Direction.UP).map(pos::offset)
				.collect(Collectors.toList());
	}

	public boolean startProcessingItem(BeltTileEntity te, TransportedItemStack transported, BeltAttachmentState state) {
		BlockPos pos = state.attachmentPos;
		World world = te.getWorld();
		ItemStack stack = transported.stack;

		FilteringBehaviour filtering = TileEntityBehaviour.get(world, pos, FilteringBehaviour.TYPE);
		SingleTargetAutoExtractingBehaviour extracting = TileEntityBehaviour.get(world, pos,
				SingleTargetAutoExtractingBehaviour.TYPE);

		if (extracting == null)
			return false;
		if (filtering != null && (!filtering.test(stack) || stack.getCount() < filtering.getAmount()))
			return false;

		return true;
	}

	public boolean processItem(BeltTileEntity te, TransportedItemStack transported, BeltAttachmentState state) {
		BlockPos pos = state.attachmentPos;
		World world = te.getWorld();
		ItemStack stack = transported.stack;

		SingleTargetAutoExtractingBehaviour extracting = TileEntityBehaviour.get(world, pos,
				SingleTargetAutoExtractingBehaviour.TYPE);

		if (extracting == null)
			return false;
		if (extracting.getShouldPause().get())
			return false;

		FilteringBehaviour filtering = TileEntityBehaviour.get(world, pos, FilteringBehaviour.TYPE);
		if (filtering != null && (!filtering.test(stack) || stack.getCount() < filtering.getAmount()))
			return false;
		if (!extracting.getShouldExtract().get())
			return true;

		return !extracting.extractFromInventory();
	}

}
