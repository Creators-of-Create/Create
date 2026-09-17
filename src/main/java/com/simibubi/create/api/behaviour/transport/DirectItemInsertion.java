package com.simibubi.create.api.behaviour.transport;

import java.util.Objects;

import com.simibubi.create.api.behaviour.transport.DirectItemReceiver.Context;
import com.simibubi.create.api.behaviour.transport.DirectItemReceiver.Result;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.world.item.ItemStack;

public final class DirectItemInsertion {

	private DirectItemInsertion() {}

	/**
	 * Attempt to deliver a stack directly to the target in the given context.
	 * Existing {@link DirectBeltInputBehaviour} instances take priority over
	 * {@link DirectItemReceiver#CAPABILITY}.
	 */
	public static Result tryInsert(Context context, ItemStack stack, boolean simulate) {
		Objects.requireNonNull(context, "context");
		Objects.requireNonNull(stack, "stack");

		DirectBeltInputBehaviour behaviour =
			BlockEntityBehaviour.get(context.level(), context.targetPos(), DirectBeltInputBehaviour.TYPE);
		if (behaviour != null) {
			ItemStack remainder = behaviour.handleInsertion(stack, context.side(), simulate);
			if (!simulate)
				return Result.accept(remainder);
			return remainder.getCount() == stack.getCount() ? Result.reject(stack) : Result.accept(remainder);
		}

		DirectItemReceiver receiver = context.level()
			.getCapability(DirectItemReceiver.CAPABILITY, context.targetPos(), context.side());
		if (receiver == null)
			return Result.pass(stack);

		return Objects.requireNonNull(receiver.insert(stack.copy(), context, simulate),
			"DirectItemReceiver returned null");
	}
}
