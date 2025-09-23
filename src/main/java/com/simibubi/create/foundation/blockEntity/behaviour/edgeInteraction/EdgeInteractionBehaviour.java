package com.simibubi.create.foundation.blockEntity.behaviour.edgeInteraction;

import java.util.function.Predicate;

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
	Predicate<Item> requiredItem;

	public EdgeInteractionBehaviour(SmartBlockEntity be, ConnectionCallback callback) {
		super(be);
		this.connectionCallback = callback;
		requiredItem = item -> true;
		connectivityPredicate = (world, pos, face, face2) -> true;
	}

	public EdgeInteractionBehaviour connectivity(ConnectivityPredicate pred) {
		this.connectivityPredicate = pred;
		return this;
	}

	public EdgeInteractionBehaviour require(Item required) {
		return this.require(item -> item == required);
	}

	public EdgeInteractionBehaviour require(Predicate<Item> predicate) {
		this.requiredItem = predicate;
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
