package com.simibubi.create.content.contraptions.goggles;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.components.structureMovement.IDisplayAssemblyExceptions;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.PistonExtensionPoleBlock;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.config.CClient;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.gui.Theme;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBox;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.outliner.Outline;
import com.simibubi.create.foundation.utility.outliner.Outliner.OutlineEntry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
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
import net.minecraftforge.fmlclient.gui.GuiUtils;

public class GoggleOverlayRenderer {

	private static final List<Supplier<Boolean>> customGogglePredicates = new LinkedList<>();
	private static final Map<Object, OutlineEntry> outlines = CreateClient.OUTLINER.getOutlines();

	public static int hoverTicks = 0;
	public static BlockPos lastHovered = null;

	public static void renderOverlay(PoseStack ms, MultiBufferSource buffer, int light, int overlay,
		float partialTicks) {
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

		ms.pushPose();
		Screen tooltipScreen = new TooltipScreen(null);
		tooltipScreen.init(mc, mc.getWindow()
			.getGuiScaledWidth(),
			mc.getWindow()
				.getGuiScaledHeight());

		int titleLinesCount = 1;
		int tooltipTextWidth = 0;
		for (FormattedText textLine : tooltip) {
			int textLineWidth = mc.font.width(textLine);
			if (textLineWidth > tooltipTextWidth)
				tooltipTextWidth = textLineWidth;
		}

		int tooltipHeight = 8;
		if (tooltip.size() > 1) {
			tooltipHeight += (tooltip.size() - 1) * 10;
			if (tooltip.size() > titleLinesCount)
				tooltipHeight += 2; // gap between title lines and next lines
		}

		CClient cfg = AllConfigs.CLIENT;
		int posX = tooltipScreen.width / 2 + cfg.overlayOffsetX.get();
		int posY = tooltipScreen.height / 2 + cfg.overlayOffsetY.get();

		posX = Math.min(posX, tooltipScreen.width - tooltipTextWidth - 20);
		posY = Math.min(posY, tooltipScreen.height - tooltipHeight - 20);

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
			ms.translate((1 - fade) * Math.signum(cfg.overlayOffsetX.get() + .5f) * 4, 0, 0);
			colorBackground.scaleAlpha(fade);
			colorBorderTop.scaleAlpha(fade);
			colorBorderBot.scaleAlpha(fade);
		}

		GuiUtils.drawHoveringText(ms, tooltip, posX, posY, tooltipScreen.width, tooltipScreen.height, -1,
			colorBackground.getRGB(), colorBorderTop.getRGB(), colorBorderBot.getRGB(), mc.font);

		ItemStack item = AllItems.GOGGLES.asStack();
		GuiGameElement.of(item)
			.at(posX + 10, posY - 16, 450)
			.render(ms);
		ms.popPose();
	}

	/**
	 * Use this method to add custom entry points to the goggle overay, e.g. custom
	 * armor, handheld alternatives, etc.
	 */
	public static void registerCustomGoggleCondition(Supplier<Boolean> condition) {
		customGogglePredicates.add(condition);
	}

	public static final class TooltipScreen extends Screen {
		public TooltipScreen(Component p_i51108_1_) {
			super(p_i51108_1_);
		}

		@Override
		public void init(Minecraft mc, int width, int height) {
			this.minecraft = mc;
			this.itemRenderer = mc.getItemRenderer();
			this.font = mc.font;
			this.width = width;
			this.height = height;
		}
	}

}
