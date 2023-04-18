package com.simibubi.create.compat.curios;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.contraptions.goggles.GogglesItem;

import com.simibubi.create.content.curiosities.armor.BackTankUtil;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
		// Enable if the backtank should remove back slots
		// forgeEventBus.addListener(Curios::onEquipmentChanged);

		GogglesItem.addIsWearingPredicate(player -> resolveCuriosMap(player)
			.map(curiosMap -> curiosMap.get("head"))
			.map(stacksHandler -> {
				// Check all the Head slots for Goggles existing
				int slots = stacksHandler.getSlots();
				for (int slot = 0; slot < slots; slot++)
					if (AllItems.GOGGLES.isIn(stacksHandler.getStacks().getStackInSlot(slot)))
						return true;

				return false;
			})
			.orElse(false));

		BackTankUtil.addBacktankSupplier(entity -> resolveCuriosMap(entity)
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
		InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.BACK.getMessageBuilder()
			.build());
	}

	private static void onClientSetup(final FMLClientSetupEvent event) {
		CuriosRenderers.register();
	}


	/**
	 * The AttributeModifier which removes all Back Slots from Curios (A 0 multiplier on the Back slot total)
	 */
	public static final AttributeModifier backtankRemovesBackSlotsModifier =
		new AttributeModifier(UUID.fromString("c1f7e0de-9e55-464f-a7ea-f1b535cc010e"), "Backtank Removes Back Slots", 0,
			AttributeModifier.Operation.MULTIPLY_TOTAL);

	/**
	 * A listener for the LivingEquipmentChangeEvent event, handling changes to Curio slots that should happen
	 * when particular equipment is equipped.
	 *
	 * @param event The event.
	 */
	private static void onEquipmentChanged(final LivingEquipmentChangeEvent event) {
		final Optional<ICurioStacksHandler> optStacksHandler = resolveCuriosMap(event.getEntityLiving()).map(curiosMap -> curiosMap.get("back"));
		if (optStacksHandler.isEmpty())
			return;
		final ICurioStacksHandler stacksHandler = optStacksHandler.get();

		// Test for if the backtank is being added to the chest slot -- if so, remove all back slots.
		if (event.getSlot() == EquipmentSlot.CHEST) {

			final boolean removing = AllItems.COPPER_BACKTANK.isIn(event.getFrom());
			final boolean adding = AllItems.COPPER_BACKTANK.isIn(event.getTo());

			if (removing == adding)
				return;

			final boolean containsModifier = stacksHandler.getModifiers().containsKey(backtankRemovesBackSlotsModifier.getId());

			if (removing && containsModifier)
				stacksHandler.removeModifier(backtankRemovesBackSlotsModifier.getId());
			else if (adding && !containsModifier)
				stacksHandler.addTransientModifier(backtankRemovesBackSlotsModifier);
		}
	}
}
