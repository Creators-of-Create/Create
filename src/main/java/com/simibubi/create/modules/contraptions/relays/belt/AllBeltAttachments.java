package com.simibubi.create.modules.contraptions.relays.belt;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.simibubi.create.AllBlocks;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public enum AllBeltAttachments {

	BELT_FUNNEL(AllBlocks.BELT_FUNNEL),
	BELT_OBSERVER(AllBlocks.ENTITY_DETECTOR),

	;

	IBeltAttachment attachment;

	private AllBeltAttachments(AllBlocks attachment) {
		this.attachment = (IBeltAttachment) attachment.get();
	}

	public interface IBeltAttachment {
		public List<BlockPos> getPotentialAttachmentLocations(BeltTileEntity te);

		public Optional<BlockPos> getValidBeltPositionFor(IWorld world, BlockPos pos, BlockState state);

		public boolean handleEntity(BeltTileEntity te, Entity entity, BeltAttachmentState state);

		default void onAttachmentPlaced(IWorld world, BlockPos pos, BlockState state) {
			Optional<BlockPos> beltPos = getValidBeltPositionFor(world, pos, state);
			if (!beltPos.isPresent())
				return;
			BeltTileEntity te = (BeltTileEntity) world.getTileEntity(beltPos.get());
			if (te == null)
				return;
			te.attachmentTracker.addAttachment(world, pos);
			te.sendData();
		}

		default void onAttachmentRemoved(IWorld world, BlockPos pos, BlockState state) {
			Optional<BlockPos> beltPos = getValidBeltPositionFor(world, pos, state);
			if (!beltPos.isPresent())
				return;
			BeltTileEntity te = (BeltTileEntity) world.getTileEntity(beltPos.get());
			if (te == null)
				return;
			te.attachmentTracker.removeAttachment(pos);
			te.sendData();
		}
	}

	public static class BeltAttachmentState {
		public IBeltAttachment attachment;
		public BlockPos attachmentPos;
		public int processingDuration;
		public Entity processingEntity;

		public BeltAttachmentState(IBeltAttachment attachment, BlockPos attachmentPos) {
			this.attachment = attachment;
			this.attachmentPos = attachmentPos;
		}

	}

	public static class Tracker {
		public List<BeltAttachmentState> attachments;

		public Tracker() {
			attachments = new ArrayList<>(0);
		}

		public void findAttachments(BeltTileEntity belt) {
			for (AllBeltAttachments ba : AllBeltAttachments.values()) {
				List<BlockPos> attachmentPositions = ba.attachment.getPotentialAttachmentLocations(belt);
				World world = belt.getWorld();
				for (BlockPos potentialPos : attachmentPositions) {
					if (!world.isBlockPresent(potentialPos))
						continue;
					BlockState state = world.getBlockState(potentialPos);
					if (!(state.getBlock() instanceof IBeltAttachment)) 
						continue;
					Optional<BlockPos> validBeltPos = ((IBeltAttachment) state.getBlock()).getValidBeltPositionFor(world, potentialPos, state);
					if (!validBeltPos.isPresent())
						continue;
					if (validBeltPos.get().equals(belt.getPos()))
						addAttachment(world, potentialPos);
				}
			}
		}

		public BeltAttachmentState addAttachment(IWorld world, BlockPos pos) {
			BlockState state = world.getBlockState(pos);
			removeAttachment(pos);
			BeltAttachmentState newAttachmentState = new BeltAttachmentState((IBeltAttachment) state.getBlock(), pos);
			attachments.add(newAttachmentState);
			return newAttachmentState;
		}

		public void removeAttachment(BlockPos pos) {
			BeltAttachmentState toRemove = null;
			for (BeltAttachmentState atState : attachments)
				if (atState.attachmentPos.equals(pos))
					toRemove = atState;
			if (toRemove != null)
				attachments.remove(toRemove);
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
