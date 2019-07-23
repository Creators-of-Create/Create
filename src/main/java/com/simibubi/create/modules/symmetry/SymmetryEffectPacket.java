package com.simibubi.create.modules.symmetry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class SymmetryEffectPacket {

	private BlockPos mirror;
	private List<BlockPos> positions;

	public SymmetryEffectPacket(BlockPos mirror, List<BlockPos> positions) {
		this.mirror = mirror;
		this.positions = positions;
	}

	public SymmetryEffectPacket(PacketBuffer buffer) {
		mirror = buffer.readBlockPos();
		int amt = buffer.readInt();
		positions = new ArrayList<>(amt);
		for (int i = 0; i < amt; i++) {
			positions.add(buffer.readBlockPos());
		}
	}

	public void toBytes(PacketBuffer buffer) {
		buffer.writeBlockPos(mirror);
		buffer.writeInt(positions.size());
		for (BlockPos blockPos : positions) {
			buffer.writeBlockPos(blockPos);
		}
	}

	public void handle(Supplier<Context> context) {
		if (Minecraft.getInstance().player.getPositionVector().distanceTo(new Vec3d(mirror)) > 100)
			return;

		for (BlockPos to : positions)
			SymmetryHandler.drawEffect(mirror, to);
	}

}
