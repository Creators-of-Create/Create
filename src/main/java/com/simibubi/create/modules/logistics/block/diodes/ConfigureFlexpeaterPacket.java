package com.simibubi.create.modules.logistics.block.diodes;

import com.simibubi.create.foundation.packet.TileEntityConfigurationPacket;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class ConfigureFlexpeaterPacket extends TileEntityConfigurationPacket<FlexpeaterTileEntity> {

	private int maxState;

	public ConfigureFlexpeaterPacket(BlockPos pos, int newMaxState) {
		super(pos);
		this.maxState = newMaxState;
	}

	public ConfigureFlexpeaterPacket(PacketBuffer buffer) {
		super(buffer);
	}

	@Override
	protected void writeSettings(PacketBuffer buffer) {
		buffer.writeInt(maxState);
	}

	@Override
	protected void readSettings(PacketBuffer buffer) {
		maxState = buffer.readInt();
	}

	@Override
	protected void applySettings(FlexpeaterTileEntity te) {
		te.maxState = maxState;
		te.state = MathHelper.clamp(te.state, 0, maxState);
		te.forceClientState = true;
		te.sendData();
		te.forceClientState = false;
	}

}
