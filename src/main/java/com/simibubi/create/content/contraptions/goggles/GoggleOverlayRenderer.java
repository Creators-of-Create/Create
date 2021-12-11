package com.simibubi.create.content.contraptions.goggles;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.components.structureMovement.IDisplayAssemblyExceptions;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.PistonExtensionPoleBlock;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.config.CClient;
import com.simibubi.create.foundation.gui.RemovedGuiUtils;
import com.simibubi.create.foundation.gui.Theme;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBox;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.outliner.Outline;
import com.simibubi.create.foundation.utility.outliner.Outliner.OutlineEntry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import com.simibubi.create.lib.utility.GuiUtils;

public class GoggleOverlayRenderer {

	private static final List<Supplier<Boolean>> customGogglePredicates = new LinkedList<>();
	private static final Map<Object, OutlineEntry> outlines = CreateClient.OUTLINER.getOutlines();

	public static int hoverTicks = 0;
	public static BlockPos lastHovered = null;

	public static void renderOverlay(PoseStack poseStack, float partialTicks, Window window) {
		HitResult objectMouseOver = Minecraft.getInstance().hitResult;

		if (!(objectMouseOver instanceof BlockHitResult)) {
			lastHovered = null;
			hoverTicks = 0;
			return;
		}

		for (OutlineEntry entry : outlines.values()) {
			if (!entry.isAlive())
				continue;
			Outline outline = entry.getOutline();
			if (outline instanceof ValueBox && !((ValueBox) outline).isPassive)
				return;
		}

		BlockHitResult result = (BlockHitResult) objectMouseOver;
		Minecraft mc = Minecraft.getInstance();
		ClientLevel world = mc.level;
		BlockPos pos = result.getBlockPos();
		ItemStack headSlot = mc.player.getItemBySlot(EquipmentSlot.HEAD);
		BlockEntity te = world.getBlockEntity(pos);

		if (lastHovered == null || lastHovered.equals(pos))
			hoverTicks++;
		else
			hoverTicks = 0;
		lastHovered = pos;

		boolean wearingGoggles = AllItems.GOGGLES.isIn(headSlot);
		for (Supplier<Boolean> supplier : customGogglePredicates)
			wearingGoggles |= supplier.get();

		boolean hasGoggleInformation = te instanceof IHaveGoggleInformation;
		boolean hasHoveringInformation = te instanceof IHaveHoveringInformation;

		boolean goggleAddedInformation = false;
		boolean hoverAddedInformation = false;

		List<Component> tooltip = new ArrayList<>();

		if (hasGoggleInformation && wearingGoggles) {
			IHaveGoggleInformation gte = (IHaveGoggleInformation) te;
			goggleAddedInformation = gte.addToGoggleTooltip(tooltip, mc.player.isShiftKeyDown());
		}

		if (hasHoveringInformation) {
			if (!tooltip.isEmpty())
				tooltip.add(TextComponent.EMPTY);
			IHaveHoveringInformation hte = (IHaveHoveringInformation) te;
			hoverAddedInformation = hte.addToTooltip(tooltip, mc.player.isShiftKeyDown());

			if (goggleAddedInformation && !hoverAddedInformation)
				tooltip.remove(tooltip.size() - 1);
		}

		if (te instanceof IDisplayAssemblyExceptions) {
			boolean exceptionAdded = ((IDisplayAssemblyExceptions) te).addExceptionToTooltip(tooltip);
			if (exceptionAdded) {
				hasHoveringInformation = true;
				hoverAddedInformation = true;
			}
		}

		// break early if goggle or hover returned false when present
		if ((hasGoggleInformation && !goggleAddedInformation) && (hasHoveringInformation && !hoverAddedInformation))
			return;

		// check for piston poles if goggles are worn
		BlockState state = world.getBlockState(pos);
		if (wearingGoggles && AllBlocks.PISTON_EXTENSION_POLE.has(state)) {
			Direction[] directions = Iterate.directionsInAxis(state.getValue(PistonExtensionPoleBlock.FACING)
				.getAxis());
			int poles = 1;
			boolean pistonFound = false;
			for (Direction dir : directions) {
				int attachedPoles = PistonExtensionPoleBlock.PlacementHelper.get()
					.attachedPoles(world, pos, dir);
				poles += attachedPoles;
				pistonFound |= world.getBlockState(pos.relative(dir, attachedPoles + 1))
					.getBlock() instanceof MechanicalPistonBlock;
			}

			if (!pistonFound)
				return;
			if (!tooltip.isEmpty())
				tooltip.add(TextComponent.EMPTY);

			tooltip.add(IHaveGoggleInformation.componentSpacing.plainCopy()
				.append(Lang.translate("gui.goggles.pole_length"))
				.append(new TextComponent(" " + poles)));
		}

		if (tooltip.isEmpty())
			return;

		poseStack.pushPose();

		int tooltipTextWidth = 0;
		for (FormattedText textLine : tooltip) {
			int textLineWidth = mc.font.width(textLine);
			if (textLineWidth > tooltipTextWidth)
				tooltipTextWidth = textLineWidth;
		}

		int tooltipHeight = 8;
		if (tooltip.size() > 1) {
			tooltipHeight += 2; // gap between title lines and next lines
			tooltipHeight += (tooltip.size() - 1) * 10;
		}

		CClient cfg = AllConfigs.CLIENT;
		int posX = mc.getWindow().getGuiScaledWidth() / 2 + cfg.overlayOffsetX.get();
		int posY = mc.getWindow().getGuiScaledHeight() / 2 + cfg.overlayOffsetY.get();

		posX = Math.min(posX, mc.getWindow().getGuiScaledWidth() - tooltipTextWidth - 20);
		posY = Math.min(posY, mc.getWindow().getGuiScaledHeight() - tooltipHeight - 20);

		float fade = Mth.clamp((hoverTicks + partialTicks) / 12f, 0, 1);
		Boolean useCustom = cfg.overlayCustomColor.get();
		Color colorBackground = useCustom ?
				new Color(cfg.overlayBackgroundColor.get()) :
				Theme.c(Theme.Key.VANILLA_TOOLTIP_BACKGROUND).scaleAlpha(.75f);
		Color colorBorderTop = useCustom ?
				new Color(cfg.overlayBorderColorTop.get()) :
				Theme.c(Theme.Key.VANILLA_TOOLTIP_BORDER, true).copy();
		Color colorBorderBot = useCustom ?
				new Color(cfg.overlayBorderColorBot.get()) :
				Theme.c(Theme.Key.VANILLA_TOOLTIP_BORDER, false).copy();

		if (fade < 1) {
			poseStack.translate((1 - fade) * Math.signum(cfg.overlayOffsetX.get() + .5f) * 4, 0, 0);
			colorBackground.scaleAlpha(fade);
			colorBorderTop.scaleAlpha(fade);
			colorBorderBot.scaleAlpha(fade);
		}

		GuiUtils.drawHoveringText(poseStack, tooltip, posX, posY, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight(), -1,
			colorBackground.getRGB(), colorBorderTop.getRGB(), colorBorderBot.getRGB(), mc.font);

		ItemStack item = AllItems.GOGGLES.asStack();
		GuiGameElement.of(item)
			.at(posX + 10, posY - 16, 450)
			.render(poseStack);
		poseStack.popPose();
	}

	/**
	 * Use this method to add custom entry points to the goggles overlay, e.g. custom
	 * armor, handheld alternatives, etc.
	 */
	public static void registerCustomGoggleCondition(Supplier<Boolean> condition) {
		customGogglePredicates.add(condition);
	}

}
