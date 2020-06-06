package com.simibubi.create.content.curiosities.tools;

import java.util.UUID;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.advancement.AllTriggers;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ExtendoGripItem extends Item {

	static Multimap<String, AttributeModifier> rangeModifier;
	static Multimap<String, AttributeModifier> doubleRangeModifier;

	static {
		// Holding an ExtendoGrip
		rangeModifier = HashMultimap.create();
		rangeModifier.put(PlayerEntity.REACH_DISTANCE.getName(),
			new AttributeModifier(UUID.fromString("7f7dbdb2-0d0d-458a-aa40-ac7633691f66"), "Range modifier", 3,
				AttributeModifier.Operation.ADDITION));

		// Holding two ExtendoGrips o.O
		doubleRangeModifier = HashMultimap.create();
		doubleRangeModifier.put(PlayerEntity.REACH_DISTANCE.getName(),
			new AttributeModifier(UUID.fromString("8f7dbdb2-0d0d-458a-aa40-ac7633691f66"), "Range modifier", 5,
				AttributeModifier.Operation.ADDITION));
	}

	public ExtendoGripItem(Properties properties) {
		super(properties.maxStackSize(1)
			.rarity(Rarity.UNCOMMON));
	}

	@SubscribeEvent
	public static void holdingExtendoGripIncreasesRange(LivingUpdateEvent event) {
		if (!(event.getEntity() instanceof PlayerEntity))
			return;
		if (event.isCanceled())
			return;

		PlayerEntity player = (PlayerEntity) event.getEntityLiving();
		String marker = "createExtendo";
		String dualMarker = "createDualExtendo";

		CompoundNBT persistentData = player.getPersistentData();
		boolean inOff = AllItems.EXTENDO_GRIP.isIn(player.getHeldItemOffhand());
		boolean inMain = AllItems.EXTENDO_GRIP.isIn(player.getHeldItemMainhand());
		boolean holdingDualExtendo = inOff && inMain;
		boolean holdingExtendo = inOff ^ inMain;
		holdingExtendo &= !holdingDualExtendo;
		boolean wasHoldingExtendo = persistentData.contains(marker);
		boolean wasHoldingDualExtendo = persistentData.contains(dualMarker);

		if (holdingExtendo != wasHoldingExtendo) {
			if (!holdingExtendo) {
				player.getAttributes()
					.removeAttributeModifiers(rangeModifier);
				persistentData.remove(marker);
			} else {
				if (player instanceof ServerPlayerEntity)
					AllTriggers.EXTENDO.trigger((ServerPlayerEntity) player);
				player.getAttributes()
					.applyAttributeModifiers(rangeModifier);
				persistentData.putBoolean(marker, true);
			}
		}

		if (holdingDualExtendo != wasHoldingDualExtendo) {
			if (!holdingDualExtendo) {
				player.getAttributes()
					.removeAttributeModifiers(doubleRangeModifier);
				persistentData.remove(dualMarker);
			} else {
				if (player instanceof ServerPlayerEntity)
					AllTriggers.GIGA_EXTENDO.trigger((ServerPlayerEntity) player);
				player.getAttributes()
					.applyAttributeModifiers(doubleRangeModifier);
				persistentData.putBoolean(dualMarker, true);
			}
		}

	}

}
