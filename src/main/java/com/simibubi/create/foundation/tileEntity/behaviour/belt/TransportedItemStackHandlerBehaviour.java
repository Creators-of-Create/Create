package com.simibubi.create.foundation.tileEntity.behaviour.belt;

import java.util.List;
import java.util.function.Function;

import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.util.math.Vec3d;

public class TransportedItemStackHandlerBehaviour extends TileEntityBehaviour {

	public static BehaviourType<TransportedItemStackHandlerBehaviour> TYPE = new BehaviourType<>();
	private ProcessingCallback processingCallback;
	private PositionGetter positionGetter;

	public TransportedItemStackHandlerBehaviour(SmartTileEntity te, ProcessingCallback processingCallback) {
		super(te);
		this.processingCallback = processingCallback;
		positionGetter = t -> VecHelper.getCenterOf(te.getPos());
	}
	
	public TransportedItemStackHandlerBehaviour withStackPlacement(PositionGetter function) {
		this.positionGetter = function;
		return this;
	}

	public void handleProcessingOnAllItems(Function<TransportedItemStack, List<TransportedItemStack>> processFunction) {
		this.processingCallback.applyToAllItems(processFunction);
	}

	public Vec3d getWorldPositionOf(TransportedItemStack transported) {
		return positionGetter.getWorldPositionVector(transported);
	}
	
	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	@FunctionalInterface
	public interface ProcessingCallback {
		public void applyToAllItems(Function<TransportedItemStack, List<TransportedItemStack>> processFunction);
	}

	@FunctionalInterface
	public interface PositionGetter {
		public Vec3d getWorldPositionVector(TransportedItemStack transported);
	}

}
