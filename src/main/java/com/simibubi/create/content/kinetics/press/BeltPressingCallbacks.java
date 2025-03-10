package com.simibubi.create.content.kinetics.press;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.simibubi.create.Create;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.processing.ProcessingMode;
import com.simibubi.create.content.processing.ProcessingResult;
import com.simibubi.create.content.processing.callbacks.IBeltCallbacks;

import net.minecraft.world.item.ItemStack;

public class BeltPressingCallbacks implements IBeltCallbacks<PressingBehaviour> {


	@Override
	public ProcessingResult onItemReceived(TransportedItemStack itemStack, TransportedItemStackHandlerBehaviour handler, PressingBehaviour behaviour) {
		if (behaviour.getSpecifics().getKineticSpeed() == 0)
			return ProcessingResult.PASS;
		if (behaviour.isProcessing())
			return ProcessingResult.HOLD;
		if (!behaviour.getSpecifics().tryProcessOnBelt(itemStack, null, true))
			return ProcessingResult.PASS;

		behaviour.startProcessing(ProcessingMode.BELT);
		return ProcessingResult.HOLD;
	}

	@Override
	public ProcessingResult whenItemHeld(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler, PressingBehaviour behaviour) {

		if (behaviour.getSpecifics().getKineticSpeed() == 0)
			return ProcessingResult.PASS;
		if (!behaviour.isProcessing())
			return ProcessingResult.PASS;
		if (behaviour.getFinishedTicks() != PressingBehaviour.CYCLE / 2)
			return ProcessingResult.HOLD;

		behaviour.clearParticles();
		ArrayList<ItemStack> results = new ArrayList<>();
		if (!behaviour.getSpecifics().tryProcessOnBelt(transported, results, false))
			return ProcessingResult.PASS;

		boolean bulk = behaviour.getSpecifics().canProcessInBulk() || transported.stack.getCount() == 1;

		transported.clearFanProcessingData();

		List<TransportedItemStack> collect = results.stream()
			.map(stack -> {
				TransportedItemStack copy = transported.copy();
				boolean centered = BeltHelper.isItemUpright(stack);
				copy.stack = stack;
				copy.locked = true;
				copy.angle = centered ? 180 : Create.RANDOM.nextInt(360);
				return copy;
			})
			.collect(Collectors.toList());

		if (bulk) {
			if (collect.isEmpty())
				handler.handleProcessingOnItem(transported, TransportedResult.removeItem());
			else
				handler.handleProcessingOnItem(transported, TransportedResult.convertTo(collect));

		} else {
			TransportedItemStack left = transported.copy();
			left.stack.shrink(1);

			if (collect.isEmpty())
				handler.handleProcessingOnItem(transported, TransportedResult.convertTo(left));
			else
				handler.handleProcessingOnItem(transported, TransportedResult.convertToAndLeaveHeld(collect, left));
		}

		behaviour.blockEntity.sendData();
		return ProcessingResult.HOLD;

	}
}
