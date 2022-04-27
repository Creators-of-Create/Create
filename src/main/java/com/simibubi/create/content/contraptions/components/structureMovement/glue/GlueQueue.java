package com.simibubi.create.content.contraptions.components.structureMovement.glue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.foundation.utility.WorldAttached;

import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

public class GlueQueue {

	private WorldAttached<List<BlockFace>> QUEUED_GLUE = new WorldAttached<>(level -> new LinkedList<>());

	public void tick(Level level) {
		List<BlockFace> list = QUEUED_GLUE.get(level);
		if (list.isEmpty())
			return;
		BlockFace next = list.remove(0);
		if (!level.isLoaded(next.getPos()))
			return;

		SuperGlueEntity entity = new SuperGlueEntity(level, next.getPos(), next.getFace());
		level.addFreshEntity(entity);
		AllSoundEvents.SLIME_ADDED.playFrom(entity, 0.125F, Mth.clamp(8f / (list.size() + 1), 0.75f, 1f));

		AllPackets.channel.send(PacketDistributor.ALL.noArg(),
			new GlueEffectPacket(entity.getHangingPosition(), entity.getFacingDirection()
				.getOpposite(), false));
	}

	public void add(Level level, Collection<BlockFace> entries) {
		QUEUED_GLUE.get(level)
			.addAll(entries);
	}

}
