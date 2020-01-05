package com.simibubi.create.modules.logistics.block.belts;

import java.util.List;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.behaviour.base.SmartTileEntity;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.foundation.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.behaviour.filtering.FilteringBehaviour.SlotPositioning;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.HorizontalBlock;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.Vec3d;

public class BeltObserverTileEntity extends SmartTileEntity {

	private static FilteringBehaviour.SlotPositioning slots;
	private FilteringBehaviour filtering;

	public BeltObserverTileEntity() {
		super(AllTileEntities.ENTITY_DETECTOR.type);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		if (slots == null)
			createSlotPositioning();
		filtering = new FilteringBehaviour(this).withSlotPositioning(slots).moveText(new Vec3d(0, 5, 0));
		behaviours.add(filtering);
	}

	protected void createSlotPositioning() {
		slots = new SlotPositioning(state -> {
			float yRot = AngleHelper.horizontalAngle(state.get(HorizontalBlock.HORIZONTAL_FACING));
			Vec3d position = VecHelper.voxelSpace(8f, 14.5f, 16f);
			return VecHelper.rotateCentered(position, yRot, Axis.Y);
		}, state -> {
			float yRot = AngleHelper.horizontalAngle(state.get(HorizontalBlock.HORIZONTAL_FACING));
			return new Vec3d(0, 180 + yRot, 90);
		}).scale(.4f);
	}

}
