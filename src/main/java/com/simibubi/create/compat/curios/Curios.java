package com.simibubi.create.compat.curios;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.goggles.GogglesItem;

import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;

import java.util.concurrent.atomic.AtomicBoolean;

public class Curios {
	public static void init() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(Curios::onInterModEnqueue);

		GogglesItem.addIsWearingPredicate(player -> {
			AtomicBoolean hasGoggles = new AtomicBoolean(false);
			player.getCapability(CuriosCapability.INVENTORY).ifPresent(handler -> {
				hasGoggles.set(handler.getCurios().get("head").getStacks().getStackInSlot(0).getItem() == AllItems.GOGGLES.get());
			});
			return hasGoggles.get();
		});
	}

	private static void onInterModEnqueue(final InterModEnqueueEvent event) {
		InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.HEAD.getMessageBuilder().build());
	}
}
