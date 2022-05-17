package com.simibubi.create.foundation.tileEntity.behaviour.edgeInteraction;

import java.util.function.Predicate;

import com.simibubi.create.AllTags;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class EdgeInteractionBehaviour extends TileEntityBehaviour {

	public static final BehaviourType<EdgeInteractionBehaviour> TYPE = new BehaviourType<>();

	ConnectionCallback connectionCallback;
	ConnectivityPredicate connectivityPredicate;
	Predicate<Item> requiredPredicate;

	public EdgeInteractionBehaviour(SmartTileEntity te, ConnectionCallback callback) {
		super(te);
		this.connectionCallback = callback;
		requiredPredicate = item -> false;
		connectivityPredicate = (world, pos, face, face2) -> true;
	}

	public EdgeInteractionBehaviour connectivity(ConnectivityPredicate pred) {
		this.connectivityPredicate = pred;
		return this;
	}

	public EdgeInteractionBehaviour require(Item item) {
		this.requiredPredicate = item1 -> item1 == item;
		return this;
	}

	public EdgeInteractionBehaviour require(AllTags.AllItemTags tag) {
		this.requiredPredicate = tag::matches;
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
