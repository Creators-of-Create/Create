package com.simibubi.create.content.logistics.block.display;

import com.simibubi.create.content.logistics.block.display.source.DisplaySource;
import com.simibubi.create.foundation.networking.TileEntityConfigurationPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class DisplayLinkConfigurationPacket extends TileEntityConfigurationPacket<DisplayLinkTileEntity> {

	private CompoundTag configData;
	private int targetLine;

	public DisplayLinkConfigurationPacket(BlockPos pos, CompoundTag configData, int targetLine) {
		super(pos);
		this.configData = configData;
		this.targetLine = targetLine;
	}

	public DisplayLinkConfigurationPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	protected void writeSettings(FriendlyByteBuf buffer) {
		buffer.writeNbt(configData);
		buffer.writeInt(targetLine);
	}

	@Override
	protected void readSettings(FriendlyByteBuf buffer) {
		configData = buffer.readNbt();
		targetLine = buffer.readInt();
	}

	@Override
	protected void applySettings(DisplayLinkTileEntity te) {
		te.targetLine = targetLine;

		if (!configData.contains("Id")) {
			te.notifyUpdate();
			return;
		}

		ResourceLocation id = new ResourceLocation(configData.getString("Id"));
		DisplaySource source = AllDisplayBehaviours.getSource(id);
		if (source == null) {
			te.notifyUpdate();
			return;
		}

		if (te.activeSource == null || te.activeSource != source) {
			te.activeSource = source;
			te.setSourceConfig(configData.copy());
		} else {
			te.getSourceConfig()
				.merge(configData);
		}

		te.updateGatheredData();
		te.notifyUpdate();
	}

}
