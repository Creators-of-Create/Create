package com.simibubi.create.compat.curios;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.goggles.GogglesItem;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

public class Curios {
	public static void init(IEventBus modEventBus, IEventBus forgeEventBus) {
		modEventBus.addListener(Curios::onInterModEnqueue);
		modEventBus.addListener(Curios::onClientSetup);

		GogglesItem.addIsWearingPredicate(player -> player.getCapability(CuriosCapability.INVENTORY)
			.map(handler -> {
				ICurioStacksHandler stacksHandler = handler.getCurios()
					.get("head");
				if (stacksHandler == null)
					return false;
				int slots = stacksHandler.getSlots();
				for (int slot = 0; slot < slots; slot++)
					if (AllItems.GOGGLES.isIn(stacksHandler.getStacks()
						.getStackInSlot(slot)))
						return true;

				return false;
			})
			.orElse(false));

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
			() -> () -> modEventBus.addListener(CuriosRenderers::onLayerRegister));
	}

	private static void onInterModEnqueue(final InterModEnqueueEvent event) {
		InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.HEAD.getMessageBuilder()
			.build());
	}

	private static void onClientSetup(final FMLClientSetupEvent event) {
		CuriosRenderers.register();
	}
}
