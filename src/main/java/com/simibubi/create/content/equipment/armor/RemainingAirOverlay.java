package com.simibubi.create.content.equipment.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllItems;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.utility.lang.Components;
import net.createmod.catnip.utility.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.List;

public class RemainingAirOverlay implements IGuiOverlay {
	public static final RemainingAirOverlay INSTANCE = new RemainingAirOverlay();

	@Override
	public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.options.hideGui || mc.gameMode.getPlayerMode() == GameType.SPECTATOR)
			return;

		LocalPlayer player = mc.player;
		if (player == null)
			return;
		if (player.isCreative())
			return;
		if (!player.getPersistentData()
			.contains("VisualBacktankAir"))
			return;
		if (!player.isEyeInFluid(FluidTags.WATER) && !player.isInLava())
			return;

		int timeLeft = player.getPersistentData()
			.getInt("VisualBacktankAir");

		PoseStack poseStack = graphics.pose();
		poseStack.pushPose();

		ItemStack backtank = getDisplayedBacktank(player);
		poseStack.translate(width / 2 + 90, height - 53 + (backtank.getItem()
			.isFireResistant() ? 9 : 0), 0);

		Component text = Components.literal(StringUtil.formatTickDuration(Math.max(0, timeLeft - 1) * 20));
		GuiGameElement.of(backtank)
			.at(0, 0)
			.render(graphics);
		int color = 0xFF_FFFFFF;
		if (timeLeft < 60 && timeLeft % 2 == 0) {
			color = Color.mixColors(0xFF_FF0000, color, Math.max(timeLeft / 60f, .25f));
		}
		graphics.drawString(mc.font, text, 16, 5, color);

		poseStack.popPose();
	}

	public static ItemStack getDisplayedBacktank(LocalPlayer player) {
		List<ItemStack> backtanks = BacktankUtil.getAllWithAir(player);
		if (!backtanks.isEmpty()) {
			return backtanks.get(0);
		}
		return AllItems.COPPER_BACKTANK.asStack();
	}
}
