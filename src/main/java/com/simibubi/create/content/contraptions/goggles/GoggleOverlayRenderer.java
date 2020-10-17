package com.simibubi.create.content.contraptions.goggles;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.config.AllConfigs;
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
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(value = Dist.CLIENT)
public class GoggleOverlayRenderer {


	@SubscribeEvent
	public static void lookingAtBlocksThroughGogglesShowsTooltip(RenderGameOverlayEvent.Post event) {
		MatrixStack ms = event.getMatrixStack();
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

		List<ITextComponent> tooltip = new ArrayList<>();

		if (goggleInformation && AllItems.GOGGLES.isIn(goggles)) {
			IHaveGoggleInformation gte = (IHaveGoggleInformation) te;
			if (!gte.addToGoggleTooltip(tooltip, mc.player.isSneaking()))
				goggleInformation = false;
		}

		if (hoveringInformation) {
			boolean goggleAddedInformation = !tooltip.isEmpty();
			if (goggleAddedInformation)
				tooltip.add(StringTextComponent.EMPTY);
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

		ms.push();
		Screen tooltipScreen = new TooltipScreen(null);
		tooltipScreen.init(mc, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight());
		int posX = tooltipScreen.width / 2 + AllConfigs.CLIENT.overlayOffsetX.get();
		int posY = tooltipScreen.height / 2 + AllConfigs.CLIENT.overlayOffsetY.get();
		tooltipScreen.renderTooltip(ms, tooltip, posX, posY);

		ItemStack item = AllItems.GOGGLES.asStack();
		GuiGameElement.of(item).atLocal(posX + 10, posY, 450).render(ms);
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
