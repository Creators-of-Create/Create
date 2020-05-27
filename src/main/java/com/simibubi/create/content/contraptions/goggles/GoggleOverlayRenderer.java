package com.simibubi.create.content.contraptions.goggles;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllItemsNew;
import com.simibubi.create.foundation.gui.GuiGameElement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class GoggleOverlayRenderer {


	@SubscribeEvent
	public static void lookingAtBlocksThroughGogglesShowsTooltip(RenderGameOverlayEvent.Post event) {
		if (event.getType() != ElementType.HOTBAR)
			return;

		RayTraceResult objectMouseOver = Minecraft.getInstance().objectMouseOver;
		if (!(objectMouseOver instanceof BlockRayTraceResult))
			return;

		BlockRayTraceResult result = (BlockRayTraceResult) objectMouseOver;
		Minecraft mc = Minecraft.getInstance();
		ClientWorld world = mc.world;
		BlockPos pos = result.getPos();
		ItemStack goggles = mc.player.getItemStackFromSlot(EquipmentSlotType.HEAD);
		TileEntity te = world.getTileEntity(pos);

		boolean goggleInformation = te instanceof IHaveGoggleInformation;
		boolean hoveringInformation = te instanceof IHaveHoveringInformation;

		if (!goggleInformation && !hoveringInformation)
			return;

		List<String> tooltip = new ArrayList<>();

		if (goggleInformation && AllItemsNew.typeOf(AllItemsNew.GOGGLES, goggles)) {
			IHaveGoggleInformation gte = (IHaveGoggleInformation) te;
			if (!gte.addToGoggleTooltip(tooltip, mc.player.isSneaking()))
				goggleInformation = false;
		}

		if (hoveringInformation) {
			boolean goggleAddedInformation = !tooltip.isEmpty();
			if (goggleAddedInformation)
				tooltip.add("");
			IHaveHoveringInformation hte = (IHaveHoveringInformation) te;
			if (!hte.addToTooltip(tooltip, mc.player.isSneaking()))
				hoveringInformation = false;
			if (goggleAddedInformation && !hoveringInformation)
				tooltip.remove(tooltip.size() - 1);
		}

		if (!goggleInformation && !hoveringInformation)
			return;
		if (tooltip.isEmpty())
			return;

		RenderSystem.pushMatrix();
		Screen tooltipScreen = new TooltipScreen(null);
		tooltipScreen.init(mc, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight());
		tooltipScreen.renderTooltip(tooltip, tooltipScreen.width / 2, tooltipScreen.height / 2);
		
		ItemStack item = AllItemsNew.GOGGLES.asStack();
		GuiGameElement.of(item).at(tooltipScreen.width / 2 + 10, tooltipScreen.height / 2 - 16).render();
		RenderSystem.popMatrix();
	}
	

	private static final class TooltipScreen extends Screen {
		private TooltipScreen(ITextComponent p_i51108_1_) {
			super(p_i51108_1_);
		}

		@Override
		public void init(Minecraft mc, int width, int height) {
			this.minecraft = mc;
			this.itemRenderer = mc.getItemRenderer();
			this.font = mc.fontRenderer;
			this.width = width;
			this.height = height;
		}
	}

}
