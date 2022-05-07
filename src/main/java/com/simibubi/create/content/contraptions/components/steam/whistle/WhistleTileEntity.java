package com.simibubi.create.content.contraptions.components.steam.whistle;

import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.steam.whistle.WhistleExtenderBlock.WhistleExtenderShape;
import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class WhistleTileEntity extends SmartTileEntity implements IHaveGoggleInformation {

	protected int pitch = 0;

	public WhistleTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {}

	public void updatePitch() {
		BlockPos currentPos = worldPosition.above();
		int prevPitch = pitch;
		for (pitch = 0; pitch <= 24; pitch += 2) {
			BlockState blockState = level.getBlockState(currentPos);
			if (!AllBlocks.STEAM_WHISTLE_EXTENSION.has(blockState))
				break;
			if (blockState.getValue(WhistleExtenderBlock.SHAPE) == WhistleExtenderShape.SINGLE) {
				pitch++;
				break;
			}
			currentPos = currentPos.above();
		}
		if (prevPitch == pitch)
			return;
		notifyUpdate();
	}

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		tag.putInt("Pitch", pitch);
		super.write(tag, clientPacket);
	}

	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		pitch = tag.getInt("Pitch");
		super.read(tag, clientPacket);
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		tooltip.add(new TextComponent(spacing + "Pitch: " + pitch));
		return true;
	}

}
