package com.simibubi.create.compat.curios;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.equipment.armor.BacktankUtil;
import com.simibubi.create.content.equipment.goggles.GogglesItem;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

public class Curios {

	/**
	 * Resolves the Stacks Handler Map given an Entity.
	 * It is recommended to then use a `.map(curiosMap -> curiosMap.get({key})`,
	 * which can be null and would therefore be caught by the Optional::map function.
	 *
	 * @param entity The entity which possibly has a Curio Inventory capability
	 * @return An optional of the Stacks Handler Map
	 */
	private static Optional<Map<String, ICurioStacksHandler>> resolveCuriosMap(LivingEntity entity) {
		return entity.getCapability(CuriosCapability.INVENTORY).map(ICuriosItemHandler::getCurios);
	}

	public static void init(IEventBus modEventBus, IEventBus forgeEventBus) {
		modEventBus.addListener(Curios::onInterModEnqueue);
		modEventBus.addListener(Curios::onClientSetup);

		GogglesItem.addIsWearingPredicate(player -> resolveCuriosMap(player)
			.map(curiosMap -> {
				for (ICurioStacksHandler stacksHandler : curiosMap.values()) {
					// Search all the curio slots for Goggles existing
					int slots = stacksHandler.getSlots();
					for (int slot = 0; slot < slots; slot++) {
						if (AllItems.GOGGLES.isIn(stacksHandler.getStacks().getStackInSlot(slot))) {
							return true;
						}
					}
				}

				return false;
			})
			.orElse(false));

		BacktankUtil.addBacktankSupplier(entity -> resolveCuriosMap(entity)
			.map(curiosMap -> {
				List<ItemStack> stacks = new ArrayList<>();
				for (ICurioStacksHandler stacksHandler : curiosMap.values()) {
					// Search all the curio slots for pressurized air sources, and add them to the list
					int slots = stacksHandler.getSlots();
					for (int slot = 0; slot < slots; slot++) {
						final ItemStack itemStack = stacksHandler.getStacks().getStackInSlot(slot);
						if (AllTags.AllItemTags.PRESSURIZED_AIR_SOURCES.matches(itemStack))
							stacks.add(itemStack);
					}
				}

				return stacks;
			}).orElse(new ArrayList<>()));

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
