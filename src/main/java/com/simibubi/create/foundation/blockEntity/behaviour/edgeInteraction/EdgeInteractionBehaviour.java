package com.simibubi.create.foundation.blockEntity.behaviour.edgeInteraction;

import java.util.Optional;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class EdgeInteractionBehaviour extends BlockEntityBehaviour {

	public static final BehaviourType<EdgeInteractionBehaviour> TYPE = new BehaviourType<>();
	
	ConnectionCallback connectionCallback;
	ConnectivityPredicate connectivityPredicate;
	Optional<Item> requiredItem;

	public EdgeInteractionBehaviour(SmartBlockEntity be, ConnectionCallback callback) {
		super(be);
		this.connectionCallback = callback;
		requiredItem = Optional.empty();
		connectivityPredicate = (world, pos, face, face2) -> true;
	}
	
	public EdgeInteractionBehaviour connectivity(ConnectivityPredicate pred) {
		this.connectivityPredicate = pred;
		return this;
	}
	
	public EdgeInteractionBehaviour require(Item item) {
		this.requiredItem = Optional.of(item);
		return this;
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}
	
	@FunctionalInterface
	public interface ConnectionCallback {
		public void apply(Level world, BlockPos clicked, BlockPos neighbour);
	}
	
	@FunctionalInterface
	public interface ConnectivityPredicate {
		public boolean test(Level world, BlockPos pos, Direction selectedFace, Direction connectedFace);
	}

}
