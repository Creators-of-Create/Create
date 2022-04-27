package com.simibubi.create.content.contraptions.components.structureMovement.glue;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.network.NetworkEvent.Context;

public class SuperGlueSelectionPacket extends SimplePacketBase {

	private BlockPos from;
	private BlockPos to;

	public SuperGlueSelectionPacket(BlockPos from, BlockPos to) {
		this.from = from;
		this.to = to;
	}

	public SuperGlueSelectionPacket(FriendlyByteBuf buffer) {
		from = buffer.readBlockPos();
		to = buffer.readBlockPos();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(from);
		buffer.writeBlockPos(to);
	}

	@Override
	public void handle(Supplier<Context> context) {
		Context ctx = context.get();
		ctx.enqueueWork(() -> {
			ServerPlayer player = ctx.getSender();

			double range = player.getAttribute(ForgeMod.REACH_DISTANCE.get())
				.getValue() + 2;
			if (player.distanceToSqr(Vec3.atCenterOf(to)) > range * range)
				return;
			if (!to.closerThan(from, 25))
				return;

			Pair<Set<BlockPos>, List<BlockFace>> group =
				SuperGlueSelectionHelper.searchGlueGroup(player.level, from, to);
			if (group == null)
				return;
			if (!group.getFirst()
				.contains(to))
				return;
			List<BlockFace> glue = group.getSecond();
			if (!SuperGlueSelectionHelper.collectGlueFromInventory(player, glue.size(), true))
				return;
			
			SuperGlueSelectionHelper.collectGlueFromInventory(player, glue.size(), false);
			Create.GLUE_QUEUE.add(player.level, glue);
		});
		ctx.setPacketHandled(true);
	}

}
