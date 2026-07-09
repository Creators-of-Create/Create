package com.simibubi.create.content.equipment.armor;

import com.simibubi.create.Create;

import net.createmod.catnip.api.animation.LerpedFloat;
import net.createmod.catnip.api.animation.LerpedFloat.Chaser;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class CardboardArmorStealthOverlay implements IClientItemExtensions {

	public CardboardArmorStealthOverlay() {
	}

	private static final Identifier PACKAGE_BLUR_LOCATION = Create.asResource("textures/misc/package_blur.png");

	private static LerpedFloat opacity = LerpedFloat.linear()
		.startWithValue(0)
		.chase(0, 0.25f, Chaser.EXP);

	public static void clientTick() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null)
			return;

		opacity.tickChaser();
		opacity.updateChaseTarget(CardboardArmorHandler.testForStealth(player) ? 1 : 0);
	}

	@Override
	public void renderFirstPersonOverlay(ItemStack stack, EquipmentSlot slot, Player player, GuiGraphicsExtractor graphics,
		DeltaTracker deltaTracker) {
		// TODO 26.2: Rebuild package blur using GuiGraphicsExtractor's submit pipeline.
	}

}
