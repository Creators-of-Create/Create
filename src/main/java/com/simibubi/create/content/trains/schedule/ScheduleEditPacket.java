package com.simibubi.create.content.trains.schedule;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent.Context;

public class ScheduleEditPacket extends SimplePacketBase {

	private Schedule schedule;

	public ScheduleEditPacket(Schedule schedule) {
		this.schedule = schedule;
	}

	public ScheduleEditPacket(FriendlyByteBuf buffer) {
		schedule = Schedule.fromTag(buffer.readNbt());
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeNbt(schedule.write());
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			ServerPlayer sender = context.getSender();
			ItemStack mainHandItem = sender.getMainHandItem();
			if (!AllItems.SCHEDULE.isIn(mainHandItem))
				return;
			
			CompoundTag tag = mainHandItem.getOrCreateTag();
			if (schedule.entries.isEmpty()) {
				tag.remove("Schedule");
				if (tag.isEmpty())
					mainHandItem.setTag(null);
			} else
				tag.put("Schedule", schedule.write());
			
			sender.getCooldowns()
				.addCooldown(mainHandItem.getItem(), 5);
		});
		return true;
	}

}
