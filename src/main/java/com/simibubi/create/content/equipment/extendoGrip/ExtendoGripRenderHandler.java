package com.simibubi.create.content.equipment.extendoGrip;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllPartialModels;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class ExtendoGripRenderHandler {

	public static float mainHandAnimation;
	public static float lastMainHandAnimation;
	public static PartialModel pose = AllPartialModels.DEPLOYER_HAND_PUNCHING;

	public static void tick() {
		lastMainHandAnimation = mainHandAnimation;
		mainHandAnimation *= Mth.clamp(mainHandAnimation, 0.8f, 0.99f);

		pose = AllPartialModels.DEPLOYER_HAND_PUNCHING;
		if (!AllItems.EXTENDO_GRIP.isIn(getRenderedOffHandStack()))
			return;
		ItemStack main = getRenderedMainHandStack();
		if (main.isEmpty())
			return;
		if (!(main.getItem() instanceof BlockItem))
			return;
		if (!com.simibubi.create.foundation.render.LegacyItemRendererBridge.getItemRenderer()
			.getModel(main, null, null, 0)
			.isGui3d())
			return;
		pose = AllPartialModels.DEPLOYER_HAND_HOLDING;
	}

	@SubscribeEvent
	public static void onRenderPlayerHand(RenderHandEvent event) {
		// TODO 26.2: Rebuild custom first-person arm rendering against the new player render-state API.
	}

	private static ItemStack getRenderedMainHandStack() {
		return Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer().mainHandItem;
	}

	private static ItemStack getRenderedOffHandStack() {
		return Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer().offHandItem;
	}

}
