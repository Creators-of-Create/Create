package com.simibubi.create.content.curiosities.clipboard;

import java.util.List;
import java.util.UUID;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.blockEntity.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class ClipboardBlockEntity extends SmartBlockEntity {

	public ItemStack dataContainer;
	private UUID lastEdit;

	public ClipboardBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		dataContainer = AllBlocks.CLIPBOARD.asStack();
	}

	@Override
	public void initialize() {
		super.initialize();
		updateWrittenState();
	}

	public void onEditedBy(Player player) {
		lastEdit = player.getUUID();
		notifyUpdate();
		updateWrittenState();
	}

	public void updateWrittenState() {
		BlockState blockState = getBlockState();
		if (!AllBlocks.CLIPBOARD.has(blockState))
			return;
		if (level.isClientSide())
			return;
		boolean isWritten = blockState.getValue(ClipboardBlock.WRITTEN);
		boolean shouldBeWritten = dataContainer.getTag() != null;
		if (isWritten == shouldBeWritten)
			return;
		level.setBlockAndUpdate(worldPosition, blockState.setValue(ClipboardBlock.WRITTEN, shouldBeWritten));
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		tag.put("Item", dataContainer.serializeNBT());
		if (clientPacket && lastEdit != null)
			tag.putUUID("LastEdit", lastEdit);
	}

	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		dataContainer = ItemStack.of(tag.getCompound("Item"));

		if (clientPacket)
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> readClientSide(tag));
	}

	@OnlyIn(Dist.CLIENT)
	private void readClientSide(CompoundTag tag) {
		Minecraft mc = Minecraft.getInstance();
		if (!(mc.screen instanceof ClipboardScreen cs))
			return;
		if (tag.contains("LastEdit") && tag.getUUID("LastEdit")
			.equals(mc.player.getUUID()))
			return;
		if (!worldPosition.equals(cs.targetedBlock))
			return;
		cs.reopenWith(dataContainer);
	}

}
