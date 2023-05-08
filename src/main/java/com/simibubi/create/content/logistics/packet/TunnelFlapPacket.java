package com.simibubi.create.content.logistics.packet;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelBlockEntity;
import com.simibubi.create.foundation.networking.BlockEntityDataPacket;

import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;

public class TunnelFlapPacket extends BlockEntityDataPacket<BeltTunnelBlockEntity> {

    private List<Pair<Direction, Boolean>> flaps;

    public TunnelFlapPacket(FriendlyByteBuf buffer) {
        super(buffer);

        byte size = buffer.readByte();

        this.flaps = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            Direction direction = Direction.from3DDataValue(buffer.readByte());
            boolean inwards = buffer.readBoolean();

            flaps.add(Pair.of(direction, inwards));
        }
    }

    public TunnelFlapPacket(BeltTunnelBlockEntity blockEntity, List<Pair<Direction, Boolean>> flaps) {
        super(blockEntity.getBlockPos());

        this.flaps = new ArrayList<>(flaps);
    }

    @Override
    protected void writeData(FriendlyByteBuf buffer) {
        buffer.writeByte(flaps.size());

        for (Pair<Direction, Boolean> flap : flaps) {
            buffer.writeByte(flap.getLeft().get3DDataValue());
            buffer.writeBoolean(flap.getRight());
        }
    }

    @Override
    protected void handlePacket(BeltTunnelBlockEntity blockEntity) {
        for (Pair<Direction, Boolean> flap : flaps) {
            blockEntity.flap(flap.getLeft(), flap.getRight());
        }
    }
}
