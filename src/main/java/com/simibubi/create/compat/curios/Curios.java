package com.simibubi.create.compat.curios;

import java.util.concurrent.atomic.AtomicBoolean;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.goggles.GogglesItem;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

public class Curios {
	public static void init() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(Curios::onInterModEnqueue);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(Curios::onClientSetup);

		GogglesItem.addIsWearingPredicate(player -> {
			AtomicBoolean hasGoggles = new AtomicBoolean(false);
			player.getCapability(CuriosCapability.INVENTORY).ifPresent(handler -> {
				ICurioStacksHandler stacksHandler = handler.getCurios().get("head");
				if(stacksHandler != null) hasGoggles.set(stacksHandler.getStacks().getStackInSlot(0).getItem() == AllItems.GOGGLES.get());
			});
			return hasGoggles.get();
		});

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FMLJavaModLoadingContext.get().getModEventBus().addListener(CuriosRenderers::onLayerRegister));
	}

	private static void onInterModEnqueue(final InterModEnqueueEvent event) {
		InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.HEAD.getMessageBuilder().build());
	}

	private static void onClientSetup(final FMLClientSetupEvent event) {
		CuriosRenderers.register();
	}
}
