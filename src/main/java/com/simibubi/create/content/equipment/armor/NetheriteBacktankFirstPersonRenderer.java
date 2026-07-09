package com.simibubi.create.content.equipment.armor;

import com.simibubi.create.AllItems;
import com.simibubi.create.Create;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderArmEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class NetheriteBacktankFirstPersonRenderer {

	private static final Identifier BACKTANK_ARMOR_LOCATION =
		Create.asResource("textures/models/armor/netherite_diving_arm.png");

	private static boolean rendererActive = false;

	public static void clientTick() {
		Minecraft mc = Minecraft.getInstance();
		rendererActive =
			mc.player != null && AllItems.NETHERITE_BACKTANK.isIn(mc.player.getItemBySlot(EquipmentSlot.CHEST));
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onRenderPlayerHand(RenderArmEvent event) {
		if (!rendererActive)
			return;

		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null)
			return;
		// TODO 26.2: Rebuild first-person armor sleeve rendering against the new player render-state API.
	}

}
