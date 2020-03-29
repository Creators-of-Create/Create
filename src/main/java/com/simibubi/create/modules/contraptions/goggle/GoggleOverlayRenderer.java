package com.simibubi.create.modules.contraptions.goggle;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.gui.ScreenElementRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
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

		if (!AllItems.GOGGLES.typeOf(goggles))
			return;

		if (!(te instanceof IHaveGoggleInformation))
			return;

		IHaveGoggleInformation gte = (IHaveGoggleInformation) te;

		List<String> tooltip = new ArrayList<>();

		if (!gte.addToGoggleTooltip(tooltip, mc.player.isSneaking()))
			return;

		GlStateManager.pushMatrix();
		Screen tooltipScreen = new Screen(null) {

			@Override
			public void init(Minecraft mc, int width, int height) {
				this.minecraft = mc;
				this.itemRenderer = mc.getItemRenderer();
				this.font = mc.fontRenderer;
				this.width = width;
				this.height = height;
			}

		};

		tooltipScreen.init(mc, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight());
		tooltipScreen.renderTooltip(tooltip, tooltipScreen.width / 2, tooltipScreen.height / 2);
		ItemStack item = goggles;
		ScreenElementRenderer.render3DItem(() -> {
			GlStateManager.translated(tooltipScreen.width / 2 + 10, tooltipScreen.height / 2 - 16, 0);
			return item;
		});
		GlStateManager.popMatrix();

	}

}
