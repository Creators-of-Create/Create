package com.simibubi.create.content.curiosities.symmetry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.tterrag.registrate.fabric.EnvExecutor;

import net.fabricmc.api.EnvType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;


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

	public void encode(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(mirror);
		buffer.writeInt(positions.size());
		for (BlockPos blockPos : positions) {
			buffer.writeBlockPos(blockPos);
		}
	}

	public void handle(Supplier<Context> ctx) {
		ctx.get().enqueueWork(() -> EnvExecutor.runWhenOn(EnvType.CLIENT, () -> () -> {
			if (Minecraft.getInstance().player.position().distanceTo(Vec3.atLowerCornerOf(mirror)) > 100)
				return;
			for (BlockPos to : positions)
				SymmetryHandler.drawEffect(mirror, to);
		}));
		ctx.get().setPacketHandled(true);
	}

}
