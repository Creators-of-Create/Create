package com.simibubi.create.content.trains.schedule.condition;

import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class IdleCargoCondition extends TimedWaitCondition {
	
	@Override
	public Pair<ItemStack, Component> getSummary() {
		return Pair.of(ItemStack.EMPTY, Lang.translateDirect("schedule.condition.idle_short", formatTime(true)));
	}

	@Override
	public ResourceLocation getId() {
		return Create.asResource("idle");
	}
	
	@Override
	public boolean tickCompletion(Level level, Train train, CompoundTag context) {
		int idleTime = Integer.MAX_VALUE;
		for (Carriage carriage : train.carriages) 
			idleTime = Math.min(idleTime, carriage.storage.getTicksSinceLastExchange());
		context.putInt("Time", idleTime);
		requestDisplayIfNecessary(context, idleTime);
		return idleTime > totalWaitTicks();
	}
	
}