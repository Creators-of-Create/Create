package com.simibubi.create.content.logistics.packet;

import com.simibubi.create.content.logistics.block.funnel.FunnelTileEntity;
import com.simibubi.create.foundation.networking.TileEntityDataPacket;

import net.minecraft.network.PacketBuffer;

public class FunnelFlapPacket extends TileEntityDataPacket<FunnelTileEntity> {

    private final boolean inwards;

    public FunnelFlapPacket(PacketBuffer buffer) {
        super(buffer);

        inwards = buffer.readBoolean();
    }

    public FunnelFlapPacket(FunnelTileEntity tile, boolean inwards) {
        super(tile.getBlockPos());
        this.inwards = inwards;
    }

    @Override
    protected void writeData(PacketBuffer buffer) {
        buffer.writeBoolean(inwards);
    }

    @Override
    protected void handlePacket(FunnelTileEntity tile) {
        tile.flap(inwards);
    }
}
