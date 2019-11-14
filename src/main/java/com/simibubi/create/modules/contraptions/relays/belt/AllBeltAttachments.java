package com.simibubi.create.modules.contraptions.relays.belt;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.modules.contraptions.relays.belt.BeltInventory.TransportedItemStack;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public enum AllBeltAttachments {

	BELT_FUNNEL(AllBlocks.BELT_FUNNEL),
	BELT_OBSERVER(AllBlocks.ENTITY_DETECTOR),
	MECHANICAL_PRESS(AllBlocks.MECHANICAL_PRESS),

	;

	IBeltAttachment attachment;

	private AllBeltAttachments(AllBlocks attachment) {
		this.attachment = (IBeltAttachment) attachment.get();
	}

	public interface IBeltAttachment {

		public List<BlockPos> getPotentialAttachmentPositions(IWorld world, BlockPos pos, BlockState beltState);

		public BlockPos getBeltPositionForAttachment(IWorld world, BlockPos pos, BlockState state);

		default boolean isAttachedCorrectly(IWorld world, BlockPos attachmentPos, BlockPos beltPos, BlockState attachmentState,
				BlockState beltState) {
			return true;
		}

		default boolean processEntity(BeltTileEntity te, Entity entity, BeltAttachmentState state) {
			return false;
		}

		default boolean startProcessingItem(BeltTileEntity te, TransportedItemStack transported, BeltAttachmentState state) {
			return false;
		}

		default boolean processItem(BeltTileEntity te, TransportedItemStack transported, BeltAttachmentState state) {
			return false;
		}

		default void onAttachmentPlaced(IWorld world, BlockPos pos, BlockState state) {
			BlockPos beltPos = getBeltPositionForAttachment(world, pos, state);
			TileEntity te = world.getTileEntity(beltPos);
			if (te == null || !(te instanceof BeltTileEntity))
				return;
			BeltTileEntity belt = (BeltTileEntity) te;
			if (!isAttachedCorrectly(world, pos, belt.getPos(), state, belt.getBlockState()))
				return;
			belt.attachmentTracker.addAttachment(world, pos);
			belt.markDirty();
			belt.sendData();
		}

		default void onAttachmentRemoved(IWorld world, BlockPos pos, BlockState state) {
			BlockPos beltPos = getBeltPositionForAttachment(world, pos, state);
			TileEntity te = world.getTileEntity(beltPos);
			if (te == null || !(te instanceof BeltTileEntity))
				return;
			BeltTileEntity belt = (BeltTileEntity) te;
			if (!isAttachedCorrectly(world, pos, belt.getPos(), state, belt.getBlockState()))
				return;
			belt.attachmentTracker.removeAttachment(pos);
			belt.markDirty();
			belt.sendData();
		}
	}

	public static class BeltAttachmentState {
		public IBeltAttachment attachment;
		public BlockPos attachmentPos;
		public int processingDuration;
		public Entity processingEntity;
		public TransportedItemStack processingStack;

		public BeltAttachmentState(IBeltAttachment attachment, BlockPos attachmentPos) {
			this.attachment = attachment;
			this.attachmentPos = attachmentPos;
		}

	}

	public static class Tracker {
		public List<BeltAttachmentState> attachments;
		private BeltTileEntity te;

		public Tracker(BeltTileEntity te) {
			attachments = new ArrayList<>(0);
			this.te = te;
		}

		public void findAttachments(BeltTileEntity belt) {
			for (AllBeltAttachments ba : AllBeltAttachments.values()) {
				World world = belt.getWorld();
				BlockPos beltPos = belt.getPos();
				BlockState beltState = belt.getBlockState();
				List<BlockPos> attachmentPositions = ba.attachment.getPotentialAttachmentPositions(world, beltPos,
						beltState);

				for (BlockPos potentialPos : attachmentPositions) {
					if (!world.isBlockPresent(potentialPos))
						continue;
					BlockState state = world.getBlockState(potentialPos);
					if (!(state.getBlock() instanceof IBeltAttachment))
						continue;
					IBeltAttachment attachment = (IBeltAttachment) state.getBlock();
					if (!attachment.getBeltPositionForAttachment(world, potentialPos, state).equals(beltPos))
						continue;
					if (!attachment.isAttachedCorrectly(world, potentialPos, beltPos, state, beltState))
						continue;

					addAttachment(world, potentialPos);
				}
			}
		}

		public BeltAttachmentState addAttachment(IWorld world, BlockPos pos) {
			BlockState state = world.getBlockState(pos);
			removeAttachment(pos);
			if (!(state.getBlock() instanceof IBeltAttachment)) {
				Create.logger.warn("Missing belt attachment for Belt at " + pos.toString());
				return null;
			}
			BeltAttachmentState newAttachmentState = new BeltAttachmentState((IBeltAttachment) state.getBlock(), pos);
			attachments.add(newAttachmentState);
			te.markDirty();
			return newAttachmentState;
		}

		public void removeAttachment(BlockPos pos) {
			BeltAttachmentState toRemove = null;
			for (BeltAttachmentState atState : attachments)
				if (atState.attachmentPos.equals(pos))
					toRemove = atState;
			if (toRemove != null)
				attachments.remove(toRemove);
			te.markDirty();
		}

		public void forEachAttachment(Consumer<BeltAttachmentState> consumer) {
			attachments.forEach(consumer::accept);
		}

		public void readAndSearch(CompoundNBT nbt, BeltTileEntity belt) {
			attachments.clear();
			if (!nbt.contains("HasAttachments"))
				return;
			if (nbt.contains("AttachmentData")) {
				ListNBT list = (ListNBT) nbt.get("AttachmentData");
				for (INBT data : list) {
					CompoundNBT stateNBT = (CompoundNBT) data;
					BlockPos attachmentPos = NBTUtil.readBlockPos(stateNBT.getCompound("Position"));
					BeltAttachmentState atState = addAttachment(belt.getWorld(), attachmentPos);
					if (atState == null)
						continue;
					atState.processingDuration = stateNBT.getInt("Duration");
				}
			}
		}

		public void write(CompoundNBT nbt) {
			if (!attachments.isEmpty()) {
				nbt.putBoolean("HasAttachments", true);
				ListNBT list = new ListNBT();
				forEachAttachment(atState -> {
					CompoundNBT stateNBT = new CompoundNBT();
					stateNBT.put("Position", NBTUtil.writeBlockPos(atState.attachmentPos));
					stateNBT.putInt("Duration", atState.processingDuration);
					list.add(stateNBT);
				});
				nbt.put("AttachmentData", list);
			}

		}

	}

}
