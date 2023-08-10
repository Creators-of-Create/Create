package com.simibubi.create.content.equipment.symmetryWand.mirror;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CrossPlaneMirror extends SymmetryMirror {

	public static enum Align implements StringRepresentable {
		Y("y"), D("d");

		private final String name;

		private Align(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public CrossPlaneMirror(Vec3 pos) {
		super(pos);
		orientation = Align.Y;
	}

	@Override
	protected void setOrientation() {
		if (orientationIndex < 0)
			orientationIndex += Align.values().length;
		if (orientationIndex >= Align.values().length)
			orientationIndex -= Align.values().length;
		orientation = Align.values()[orientationIndex];
	}

	@Override
	public void setOrientation(int index) {
		this.orientation = Align.values()[index];
		orientationIndex = index;
	}

	@Override
	public Map<BlockPos, BlockState> process(BlockPos position, BlockState block) {
		Map<BlockPos, BlockState> result = new HashMap<>();

		switch ((Align) orientation) {
		case D:
			result.put(flipD1(position), flipD1(block));
			result.put(flipD2(position), flipD2(block));
			result.put(flipD1(flipD2(position)), flipD1(flipD2(block)));
			break;
		case Y:
			result.put(flipX(position), flipX(block));
			result.put(flipZ(position), flipZ(block));
			result.put(flipX(flipZ(position)), flipX(flipZ(block)));
			break;
		default:
			break;
		}

		return result;
	}

	@Override
	public String typeName() {
		return CROSS_PLANE;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public PartialModel getModel() {
		return AllPartialModels.SYMMETRY_CROSSPLANE;
	}

	@Override
	public void applyModelTransform(PoseStack ms) {
		super.applyModelTransform(ms);
		TransformStack.cast(ms)
			.centre()
			.rotateY(((Align) orientation) == Align.Y ? 0 : 45)
			.unCentre();
	}

	@Override
	public List<Component> getAlignToolTips() {
		return ImmutableList.of(CreateLang.translateDirect("orientation.orthogonal"), CreateLang.translateDirect("orientation.diagonal"));
	}

}
