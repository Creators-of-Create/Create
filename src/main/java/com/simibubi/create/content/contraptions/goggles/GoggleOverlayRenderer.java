package com.simibubi.create.content.contraptions.goggles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.components.structureMovement.IDisplayAssemblyExceptions;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.PistonExtensionPoleBlock;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.gui.Theme;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBox;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.outliner.Outline;
import com.simibubi.create.foundation.utility.outliner.Outliner.OutlineEntry;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class GoggleOverlayRenderer {

	private static final Map<Object, OutlineEntry> outlines = CreateClient.OUTLINER.getOutlines();

	public static void renderOverlay(MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay,
		float partialTicks) {
		RayTraceResult objectMouseOver = Minecraft.getInstance().hitResult;

		if (!(objectMouseOver instanceof BlockRayTraceResult))
			return;

		for (OutlineEntry entry : outlines.values()) {
			if (!entry.isAlive())
				continue;
			Outline outline = entry.getOutline();
			if (outline instanceof ValueBox && !((ValueBox) outline).isPassive)
				return;
		}

		BlockRayTraceResult result = (BlockRayTraceResult) objectMouseOver;
		Minecraft mc = Minecraft.getInstance();
		ClientWorld world = mc.level;
		BlockPos pos = result.getBlockPos();
		ItemStack headSlot = mc.player.getItemBySlot(EquipmentSlotType.HEAD);
		TileEntity te = world.getBlockEntity(pos);

		boolean wearingGoggles = AllItems.GOGGLES.isIn(headSlot);

		boolean hasGoggleInformation = te instanceof IHaveGoggleInformation;
		boolean hasHoveringInformation = te instanceof IHaveHoveringInformation;

		boolean goggleAddedInformation = false;
		boolean hoverAddedInformation = false;

		List<ITextComponent> tooltip = new ArrayList<>();

		if (hasGoggleInformation && wearingGoggles) {
			IHaveGoggleInformation gte = (IHaveGoggleInformation) te;
			goggleAddedInformation = gte.addToGoggleTooltip(tooltip, mc.player.isShiftKeyDown());
		}

		if (hasHoveringInformation) {
			if (!tooltip.isEmpty())
				tooltip.add(StringTextComponent.EMPTY);
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
				tooltip.add(StringTextComponent.EMPTY);

			tooltip.add(IHaveGoggleInformation.componentSpacing.plainCopy()
				.append(Lang.translate("gui.goggles.pole_length"))
				.append(new StringTextComponent(" " + poles)));
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
		for (ITextProperties textLine : tooltip) {
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

		int posX = tooltipScreen.width / 2 + AllConfigs.CLIENT.overlayOffsetX.get();
		int posY = tooltipScreen.height / 2 + AllConfigs.CLIENT.overlayOffsetY.get();

		posX = Math.min(posX, tooltipScreen.width - tooltipTextWidth - 20);
		posY = Math.min(posY, tooltipScreen.height - tooltipHeight - 20);

		Boolean useCustom = AllConfigs.CLIENT.overlayCustomColor.get();
		int colorBackground = useCustom ? AllConfigs.CLIENT.overlayBackgroundColor.get() : Theme.i(Theme.Key.VANILLA_TOOLTIP_BACKGROUND);
		int colorBorderTop = useCustom ? AllConfigs.CLIENT.overlayBorderColorTop.get() : Theme.i(Theme.Key.VANILLA_TOOLTIP_BORDER, true);
		int colorBorderBot = useCustom ? AllConfigs.CLIENT.overlayBorderColorBot.get() : Theme.i(Theme.Key.VANILLA_TOOLTIP_BORDER, false);
		GuiUtils.drawHoveringText(ms, tooltip, posX, posY, tooltipScreen.width, tooltipScreen.height, -1,  colorBackground, colorBorderTop, colorBorderBot, mc.font);


		ItemStack item = AllItems.GOGGLES.asStack();
		GuiGameElement.of(item)
			.at(posX + 10, posY - 16, 450)
			.render(ms);
		ms.popPose();
	}

	public static final class TooltipScreen extends Screen {
		public TooltipScreen(ITextComponent p_i51108_1_) {
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
