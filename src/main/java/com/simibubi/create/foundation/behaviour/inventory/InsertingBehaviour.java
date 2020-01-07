package com.simibubi.create.foundation.behaviour.inventory;

import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.foundation.behaviour.base.IBehaviourType;
import com.simibubi.create.foundation.behaviour.base.SmartTileEntity;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class InsertingBehaviour extends InventoryManagementBehaviour {

	public static IBehaviourType<InsertingBehaviour> TYPE = new IBehaviourType<InsertingBehaviour>() {
	};

	public InsertingBehaviour(SmartTileEntity te, Supplier<List<Pair<BlockPos, Direction>>> attachments) {
		super(te, attachments);
	}

	@Override
	public IBehaviourType<?> getType() {
		return TYPE;
	}

}
