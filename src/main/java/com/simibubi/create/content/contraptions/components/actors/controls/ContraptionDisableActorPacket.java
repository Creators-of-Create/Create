package com.simibubi.create.content.contraptions.components.actors.controls;

import java.util.Iterator;
import java.util.List;

import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent.Context;

public class ContraptionDisableActorPacket extends SimplePacketBase {

	private int entityID;
	private ItemStack filter;
	private boolean enable;
	
	public ContraptionDisableActorPacket(int entityID, ItemStack filter, boolean enable) {
		this.entityID = entityID;
		this.filter = filter;
		this.enable = enable;
	}

	public ContraptionDisableActorPacket(FriendlyByteBuf buffer) {
		entityID = buffer.readInt();
		enable = buffer.readBoolean();
		filter = buffer.readItem();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(entityID);
		buffer.writeBoolean(enable);
		buffer.writeItem(filter);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			Entity entityByID = Minecraft.getInstance().level.getEntity(entityID);
			if (!(entityByID instanceof AbstractContraptionEntity ace))
				return;
			
			Contraption contraption = ace.getContraption();
			List<ItemStack> disabledActors = contraption.getDisabledActors();
			if (filter.isEmpty())
				disabledActors.clear();
			
			if (!enable) {
				disabledActors.add(filter);
				contraption.setActorsActive(filter, false);
				return;
			}

			for (Iterator<ItemStack> iterator = disabledActors.iterator(); iterator.hasNext();) {
				ItemStack next = iterator.next();
				if (ContraptionControlsMovement.isSameFilter(next, filter) || next.isEmpty())
					iterator.remove();
			}
			
			contraption.setActorsActive(filter, true);
		});
		return true;
	}
	
}
