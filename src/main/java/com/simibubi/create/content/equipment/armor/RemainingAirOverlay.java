package com.simibubi.create.content.equipment.armor;

import java.util.List;

import org.joml.Matrix3x2fStack;

import com.simibubi.create.AllItems;

import net.createmod.catnip.api.client.gui.element.GuiGameElement;
import net.createmod.catnip.api.theme.Color;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.neoforged.neoforge.client.gui.GuiLayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;

public class RemainingAirOverlay implements GuiLayer {
	public static final RemainingAirOverlay INSTANCE = new RemainingAirOverlay();

	@Override
	public void render(GuiGraphicsExtractor GuiGraphicsExtractor, DeltaTracker deltaTracker) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.gui.hud.isHidden() || mc.gameMode.getPlayerMode() == GameType.SPECTATOR)
			return;

		LocalPlayer player = mc.player;
		if (player == null)
			return;
		if (player.isCreative())
			return;
		if (!player.getPersistentData()
			.contains("VisualBacktankAir"))
			return;
		BlockPos eyePos = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
		boolean isAir = !player.isEyeInFluid(FluidTags.WATER) && !player.isInLava() || player.level().getBlockState(eyePos).is(Blocks.BUBBLE_COLUMN);
		boolean canBreathe = MobEffectUtil.hasWaterBreathing(player) || player.getAbilities().invulnerable;
		if ((isAir || canBreathe) && !player.isInLava())
			return;

		int timeLeft = player.getPersistentData()
			.getIntOr("VisualBacktankAir", 0);

		Matrix3x2fStack poseStack = GuiGraphicsExtractor.pose();
		poseStack.pushMatrix();

		ItemStack backtank = getDisplayedBacktank(player);
		poseStack.translate(GuiGraphicsExtractor.guiWidth() / 2 + 90, GuiGraphicsExtractor.guiHeight() - 53 + (backtank
				.has(DataComponents.DAMAGE_RESISTANT) ? 9 : 0));

		Component text = Component.literal(StringUtil.formatTickDuration(Math.max(0, timeLeft - 1) * 20, mc.level.tickRateManager().tickrate()));
		GuiGameElement.of(backtank)
			.at(0, 0)
			.render(GuiGraphicsExtractor, 0, 0);
		int color = 0xFF_FFFFFF;
		if (timeLeft < 60 && timeLeft % 2 == 0) {
			color = Color.mixColors(0xFF_FF0000, color, Math.max(timeLeft / 60f, .25f));
		}
		GuiGraphicsExtractor.text(mc.font, text, 16, 5, color);

		poseStack.popMatrix();
	}

	public static ItemStack getDisplayedBacktank(LocalPlayer player) {
		List<ItemStack> backtanks = BacktankUtil.getAllWithAir(player);
		if (!backtanks.isEmpty()) {
			return backtanks.getFirst();
		}
		return AllItems.COPPER_BACKTANK.asStack();
	}
}
