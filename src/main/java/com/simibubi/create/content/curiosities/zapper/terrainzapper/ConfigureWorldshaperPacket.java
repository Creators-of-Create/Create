package com.simibubi.create.content.curiosities.zapper.terrainzapper;

import com.simibubi.create.content.curiosities.zapper.ConfigureZapperPacket;
import com.simibubi.create.content.curiosities.zapper.PlacementPatterns;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;

public class ConfigureWorldshaperPacket extends ConfigureZapperPacket {

	protected TerrainBrushes brush;
	protected int brushParamX;
	protected int brushParamY;
	protected int brushParamZ;
	protected TerrainTools tool;
	protected PlacementOptions placement;

	public ConfigureWorldshaperPacket(Hand hand, PlacementPatterns pattern, TerrainBrushes brush, int brushParamX, int brushParamY, int brushParamZ, TerrainTools tool, PlacementOptions placement) {
		super(hand, pattern);
		this.brush = brush;
		this.brushParamX = brushParamX;
		this.brushParamY = brushParamY;
		this.brushParamZ = brushParamZ;
		this.tool = tool;
		this.placement = placement;
	}

	public ConfigureWorldshaperPacket(PacketBuffer buffer) {
		super(buffer);
		brush = buffer.readEnum(TerrainBrushes.class);
		brushParamX = buffer.readVarInt();
		brushParamY = buffer.readVarInt();
		brushParamZ = buffer.readVarInt();
		tool = buffer.readEnum(TerrainTools.class);
		placement = buffer.readEnum(PlacementOptions.class);
	}

	@Override
	public void write(PacketBuffer buffer) {
		super.write(buffer);
		buffer.writeEnum(brush);
		buffer.writeVarInt(brushParamX);
		buffer.writeVarInt(brushParamY);
		buffer.writeVarInt(brushParamZ);
		buffer.writeEnum(tool);
		buffer.writeEnum(placement);
	}

	@Override
	public void configureZapper(ItemStack stack) {
		WorldshaperItem.configureSettings(stack, pattern, brush, brushParamX, brushParamY, brushParamZ, tool, placement);
	}

}
