package com.simibubi.create.content.logistics.depot;

import java.util.List;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class DepotBlockEntity extends SmartBlockEntity {

	DepotBehaviour depotBehaviour;

	public DepotBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(
				Capabilities.ItemHandler.BLOCK,
				AllBlockEntityTypes.DEPOT.get(),
				(be, context) -> be.depotBehaviour.itemHandler
		);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(depotBehaviour = new DepotBehaviour(this));
		depotBehaviour.addSubBehaviours(behaviours);
	}

	public ItemStack getHeldItem() {
		return depotBehaviour.getHeldItemStack();
	}

	public void setHeldItem(ItemStack item) {
		TransportedItemStack newStack = new TransportedItemStack(item);
		if (depotBehaviour.heldItem != null)
			newStack.angle = depotBehaviour.heldItem.angle;
		depotBehaviour.setHeldItem(newStack);
	}

}
