package com.simibubi.create.content.contraptions.components.press;

import static com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour.ProcessingResult.HOLD;
import static com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour.ProcessingResult.PASS;

import java.util.List;
import java.util.Optional;

import com.simibubi.create.content.contraptions.components.press.MechanicalPressTileEntity.Mode;
import com.simibubi.create.content.contraptions.relays.belt.transport.BeltInventory;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour.ProcessingResult;

import net.minecraft.item.ItemStack;

public class BeltPressingCallbacks {

	static ProcessingResult onItemReceived(TransportedItemStack transported, BeltInventory beltInventory,
		MechanicalPressTileEntity press) {
		if (press.getSpeed() == 0 || press.running)
			return PASS;
		if (!press.getRecipe(transported.stack)
			.isPresent())
			return PASS;

		press.start(Mode.BELT);
		return HOLD;
	}

	static ProcessingResult whenItemHeld(TransportedItemStack transportedStack, BeltInventory beltInventory,
		MechanicalPressTileEntity pressTe) {
		
		if (pressTe.getSpeed() == 0)
			return PASS;
		if (!pressTe.running)
			return PASS;
		if (pressTe.runningTicks != 30)
			return HOLD;

		Optional<PressingRecipe> recipe = pressTe.getRecipe(transportedStack.stack);
		pressTe.pressedItems.clear();
		pressTe.pressedItems.add(transportedStack.stack);

		if (!recipe.isPresent())
			return PASS;

		ItemStack out = recipe.get()
			.getRecipeOutput()
			.copy();
		List<ItemStack> multipliedOutput = ItemHelper.multipliedOutput(transportedStack.stack, out);
		if (multipliedOutput.isEmpty())
			transportedStack.stack = ItemStack.EMPTY;
		transportedStack.stack = multipliedOutput.get(0);
		pressTe.sendData();
		return HOLD;
	}

}
