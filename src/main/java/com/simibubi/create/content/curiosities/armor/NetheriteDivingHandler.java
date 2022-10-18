package com.simibubi.create.content.curiosities.armor;

import java.util.function.Supplier;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.PacketDistributor;

@EventBusSubscriber
public final class NetheriteDivingHandler {
	public static final String NETHERITE_DIVING_BITS_KEY = "CreateNetheriteDivingBits";
	public static final String FIRE_IMMUNE_KEY = "CreateFireImmune";

	@SubscribeEvent
	public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
		EquipmentSlot slot = event.getSlot();
		if (slot.getType() != EquipmentSlot.Type.ARMOR) {
			return;
		}

		LivingEntity entity = event.getEntityLiving();
		ItemStack to = event.getTo();

		if (slot == EquipmentSlot.HEAD) {
			if (AllItems.NETHERITE_DIVING_HELMET.isIn(to)) {
				setBit(entity, 0);
			} else {
				clearBit(entity, 0);
			}
		} else if (slot == EquipmentSlot.CHEST) {
			if (AllItems.NETHERITE_BACKTANK.isIn(to)) {
				setBit(entity, 1);
			} else {
				clearBit(entity, 1);
			}
		} else if (slot == EquipmentSlot.LEGS) {
			if (to.getItem() instanceof ArmorItem armorItem && armorItem.getMaterial() == ArmorMaterials.NETHERITE) {
				setBit(entity, 2);
			} else {
				clearBit(entity, 2);
			}
		} else if (slot == EquipmentSlot.FEET) {
			if (to.getItem() instanceof ArmorItem armorItem && armorItem.getMaterial() == ArmorMaterials.NETHERITE) {
				setBit(entity, 3);
			} else {
				clearBit(entity, 3);
			}
		}
	}

	public static void setBit(LivingEntity entity, int i) {
		CompoundTag nbt = entity.getPersistentData();
		byte bits = nbt.getByte(NETHERITE_DIVING_BITS_KEY);
		bits |= 1 << i;
		nbt.putByte(NETHERITE_DIVING_BITS_KEY, bits);

		if ((bits & 0xF) == 0xF) {
			setFireImmune(entity, true);
		}
	}

	public static void clearBit(LivingEntity entity, int i) {
		CompoundTag nbt = entity.getPersistentData();
		if (!nbt.contains(NETHERITE_DIVING_BITS_KEY)) {
			return;
		}

		byte bits = nbt.getByte(NETHERITE_DIVING_BITS_KEY);
		boolean prevFullSet = (bits & 0xF) == 0xF;
		bits &= ~(1 << i);
		nbt.putByte(NETHERITE_DIVING_BITS_KEY, bits);

		if (prevFullSet) {
			setFireImmune(entity, false);
		}
	}

	public static void setFireImmune(LivingEntity entity, boolean fireImmune) {
		entity.getPersistentData().putBoolean(FIRE_IMMUNE_KEY, fireImmune);
		AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), SetFireImmunePacket.create(entity));
	}

	@SubscribeEvent
	public static void onStartTrackingEntity(PlayerEvent.StartTracking event) {
		if (!(event.getPlayer() instanceof ServerPlayer player)) {
			return;
		}

		if (!(event.getTarget() instanceof LivingEntity entity)) {
			return;
		}

		AllPackets.channel.send(PacketDistributor.PLAYER.with(() -> player), SetFireImmunePacket.create(entity));
	}

	public static class SetFireImmunePacket extends SimplePacketBase {
		private final int entityId;
		private final boolean fireImmune;

		public SetFireImmunePacket(int entityId, boolean fireImmune) {
			this.entityId = entityId;
			this.fireImmune = fireImmune;
		}

		public static SetFireImmunePacket create(Entity entity) {
			int entityId = entity.getId();
			boolean fireImmune = entity.getPersistentData().getBoolean(FIRE_IMMUNE_KEY);
			return new SetFireImmunePacket(entityId, fireImmune);
		}

		public SetFireImmunePacket(FriendlyByteBuf buffer) {
			entityId = buffer.readVarInt();
			fireImmune = buffer.readBoolean();
		}

		@Override
		public void write(FriendlyByteBuf buffer) {
			buffer.writeVarInt(entityId);
			buffer.writeBoolean(fireImmune);
		}

		@Override
		public void handle(Supplier<Context> context) {
			context.get().enqueueWork(() -> {
				Entity entity = Minecraft.getInstance().level.getEntity(entityId);
				if (entity != null) {
					entity.getPersistentData().putBoolean(FIRE_IMMUNE_KEY, fireImmune);
				}
			});
			context.get().setPacketHandled(true);
		}
	}
}
