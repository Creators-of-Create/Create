package com.simibubi.create.compat.curios;

import com.simibubi.create.AllItems;
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
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Curios {

	/**
	 * Resolves the Stacks Handler given an Entity and the slot key
	 * @param entity The entity which possibly has a Curio Inventory capability
	 * @param key The key of the Curio slot
	 * @return An optional of the Stacks Handler
	 */
	private static Optional<ICurioStacksHandler> resolveCuriosStacksHandler(LivingEntity entity, String key) {
		return entity.getCapability(CuriosCapability.INVENTORY).map(handler -> handler.getCurios().get(key));
	}

	public static void init(IEventBus modEventBus, IEventBus forgeEventBus) {
		modEventBus.addListener(Curios::onInterModEnqueue);
		modEventBus.addListener(Curios::onClientSetup);
		// Enable if the backtank should remove back slots
		// forgeEventBus.addListener(Curios::onEquipmentChanged);

		GogglesItem.addIsWearingPredicate(player -> resolveCuriosStacksHandler(player, "head")
			.map(stacksHandler -> {
				// Check all the Head slots for Goggles existing
				int slots = stacksHandler.getSlots();
				for (int slot = 0; slot < slots; slot++)
					if (AllItems.GOGGLES.isIn(stacksHandler.getStacks().getStackInSlot(slot)))
						return true;

				return false;
			})
			.orElse(false));

		BackTankUtil.addBacktankSupplier(entity -> resolveCuriosStacksHandler(entity, "back")
			.map(stacksHandler -> {
				// Check all the Back Slots for a backtank existing in one of them
				List<ItemStack> stacks = new ArrayList<>();
				int slots = stacksHandler.getSlots();
				for (int slot = 0; slot < slots; slot++) {
					final ItemStack stack = stacksHandler.getStacks().getStackInSlot(slot);
					if (AllItems.COPPER_BACKTANK.isIn(stack))
						stacks.add(stack);
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
	 * @param event The event.
	 */
	private static void onEquipmentChanged(final LivingEquipmentChangeEvent event) {
		final Optional<ICurioStacksHandler> optStacksHandler = resolveCuriosStacksHandler(event.getEntityLiving(), "back");
		if (optStacksHandler.isEmpty())
			return;
		final ICurioStacksHandler stacksHandler = optStacksHandler.get();

		// Test for if the backtank is being added to the chest slot -- if so, remove all back slots.
		if(event.getSlot() == EquipmentSlot.CHEST) {

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
