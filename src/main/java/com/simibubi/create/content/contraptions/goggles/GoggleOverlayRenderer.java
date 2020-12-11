package com.simibubi.create.content.contraptions.goggles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.PistonExtensionPoleBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.PistonPolePlacementHelper;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBox;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.outliner.Outline;
import com.simibubi.create.foundation.utility.outliner.Outliner.OutlineEntry;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class GoggleOverlayRenderer {

	private static final Map<Object, OutlineEntry> outlines = CreateClient.outliner.getOutlines();

	@SubscribeEvent
	public static void lookingAtBlocksThroughGogglesShowsTooltip(RenderGameOverlayEvent.Post event) {
		MatrixStack ms = event.getMatrixStack();
		if (event.getType() != ElementType.HOTBAR)
			return;

		RayTraceResult objectMouseOver = Minecraft.getInstance().objectMouseOver;
		if (!(objectMouseOver instanceof BlockRayTraceResult))
			return;

		for (OutlineEntry entry : outlines.values()) {
			if (!entry.isAlive())
				continue;
			Outline outline = entry.getOutline();
			if (outline instanceof ValueBox && !((ValueBox) outline).isPassive) {
				return;
			}
		}

		BlockRayTraceResult result = (BlockRayTraceResult) objectMouseOver;
		Minecraft mc = Minecraft.getInstance();
		ClientWorld world = mc.world;
		BlockPos pos = result.getPos();
		ItemStack headSlot = mc.player.getItemStackFromSlot(EquipmentSlotType.HEAD);
		TileEntity te = world.getTileEntity(pos);

		boolean wearingGoggles = AllItems.GOGGLES.isIn(headSlot);

		boolean hasGoggleInformation = te instanceof IHaveGoggleInformation;
		boolean hasHoveringInformation = te instanceof IHaveHoveringInformation;

		boolean goggleAddedInformation = false;
		boolean hoverAddedInformation = false;

		List<ITextComponent> tooltip = new ArrayList<>();

		if (hasGoggleInformation && wearingGoggles) {
			IHaveGoggleInformation gte = (IHaveGoggleInformation) te;
			goggleAddedInformation = gte.addToGoggleTooltip(tooltip, mc.player.isSneaking());
		}

		if (hasHoveringInformation) {
			if (!tooltip.isEmpty())
				tooltip.add(StringTextComponent.EMPTY);
			IHaveHoveringInformation hte = (IHaveHoveringInformation) te;
			hoverAddedInformation = hte.addToTooltip(tooltip, mc.player.isSneaking());

			if (goggleAddedInformation && !hoverAddedInformation)
				tooltip.remove(tooltip.size() - 1);
		}

		// break early if goggle or hover returned false when present
		if ((hasGoggleInformation && !goggleAddedInformation) && (hasHoveringInformation && !hoverAddedInformation))
			return;

		// check for piston poles if goggles are worn
		BlockState state = world.getBlockState(pos);
		if (wearingGoggles && AllBlocks.PISTON_EXTENSION_POLE.has(state)) {
			Direction[] directions = Iterate.directionsInAxis(state.get(PistonExtensionPoleBlock.FACING)
				.getAxis());
			int poles = 1;
			boolean pistonFound = false;
			for (Direction dir : directions) {
				int attachedPoles = PistonPolePlacementHelper.attachedPoles(world, pos, dir);
				poles += attachedPoles;
				pistonFound |= world.getBlockState(pos.offset(dir, attachedPoles + 1))
					.getBlock() instanceof MechanicalPistonBlock;
			}

			if (!pistonFound)
				return;
			if (!tooltip.isEmpty())
				tooltip.add(StringTextComponent.EMPTY);

			tooltip.add(IHaveGoggleInformation.componentSpacing.copy()
				.append(Lang.translate("gui.goggles.pole_length"))
				.append(new StringTextComponent(" " + poles)));
		}

		if (tooltip.isEmpty())
			return;

		ms.push();
		Screen tooltipScreen = new TooltipScreen(null);
		tooltipScreen.init(mc, mc.getWindow()
			.getScaledWidth(),
			mc.getWindow()
				.getScaledHeight());
		int posX = tooltipScreen.width / 2 + AllConfigs.CLIENT.overlayOffsetX.get();
		int posY = tooltipScreen.height / 2 + AllConfigs.CLIENT.overlayOffsetY.get();
		// tooltipScreen.renderTooltip(tooltip, tooltipScreen.width / 2,
		// tooltipScreen.height / 2);
		tooltipScreen.renderTooltip(ms, tooltip, posX, posY);

		ItemStack item = AllItems.GOGGLES.asStack();
		// GuiGameElement.of(item).at(tooltipScreen.width / 2 + 10, tooltipScreen.height
		// / 2 - 16).render();
		GuiGameElement.of(item)
			.atLocal(posX + 10, posY, 450)
			.render(ms);
		ms.pop();
	}

	private static final class TooltipScreen extends Screen {
		private TooltipScreen(ITextComponent p_i51108_1_) {
			super(p_i51108_1_);
		}

		@Override
		public void init(Minecraft mc, int width, int height) {
			this.client = mc;
			this.itemRenderer = mc.getItemRenderer();
			this.textRenderer = mc.fontRenderer;
			this.width = width;
			this.height = height;
		}
	}

}
