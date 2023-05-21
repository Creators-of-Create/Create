package com.simibubi.create.content.equipment.symmetryWand;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public class SymmetryEffectPacket extends SimplePacketBase {

	private BlockPos mirror;
	private List<BlockPos> positions;

	public SymmetryEffectPacket(BlockPos mirror, List<BlockPos> positions) {
		this.mirror = mirror;
		this.positions = positions;
	}

	public SymmetryEffectPacket(FriendlyByteBuf buffer) {
		mirror = buffer.readBlockPos();
		int amt = buffer.readInt();
		positions = new ArrayList<>(amt);
		for (int i = 0; i < amt; i++) {
			positions.add(buffer.readBlockPos());
		}
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(mirror);
		buffer.writeInt(positions.size());
		for (BlockPos blockPos : positions) {
			buffer.writeBlockPos(blockPos);
		}
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			if (Minecraft.getInstance().player.position().distanceTo(Vec3.atLowerCornerOf(mirror)) > 100)
				return;
			for (BlockPos to : positions)
				SymmetryHandler.drawEffect(mirror, to);
		}));
		return true;
	}

}
