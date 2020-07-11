package com.simibubi.create.content.contraptions.components.press;

import static com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour.ProcessingResult.HOLD;
import static com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour.ProcessingResult.PASS;

import java.util.List;
import java.util.Optional;

import com.simibubi.create.content.contraptions.components.press.MechanicalPressTileEntity.Mode;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour.ProcessingResult;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;

import net.minecraft.item.ItemStack;

public class BeltPressingCallbacks {

	static ProcessingResult onItemReceived(TransportedItemStack transported,
		TransportedItemStackHandlerBehaviour handler, MechanicalPressTileEntity press) {
		if (press.getSpeed() == 0 || press.running)
			return PASS;
		if (!press.getRecipe(transported.stack)
			.isPresent())
			return PASS;

		press.start(Mode.BELT);
		return HOLD;
	}

	static ProcessingResult whenItemHeld(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler,
		MechanicalPressTileEntity pressTe) {

		if (pressTe.getSpeed() == 0)
			return PASS;
		if (!pressTe.running)
			return PASS;
		if (pressTe.runningTicks != 30)
			return HOLD;

		Optional<PressingRecipe> recipe = pressTe.getRecipe(transported.stack);
		pressTe.pressedItems.clear();
		pressTe.pressedItems.add(transported.stack);

		if (!recipe.isPresent())
			return PASS;

		ItemStack out = recipe.get()
			.getRecipeOutput()
			.copy();
		List<ItemStack> multipliedOutput = ItemHelper.multipliedOutput(transported.stack, out);
		if (multipliedOutput.isEmpty())
			transported.stack = ItemStack.EMPTY;
		transported.stack = multipliedOutput.get(0);
		pressTe.sendData();
		return HOLD;
	}

}
