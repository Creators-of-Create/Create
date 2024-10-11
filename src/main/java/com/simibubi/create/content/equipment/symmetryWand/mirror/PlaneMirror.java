package com.simibubi.create.content.equipment.symmetryWand.mirror;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.utility.Lang;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PlaneMirror extends SymmetryMirror {

	public static enum Align implements StringRepresentable {
		XY("xy"), YZ("yz");

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

	public PlaneMirror(Vec3 pos) {
		super(pos);
		orientation = Align.XY;
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

		case XY:
			result.put(flipZ(position), flipZ(block));
			break;
		case YZ:
			result.put(flipX(position), flipX(block));
			break;
		default:
			break;

		}
		return result;
	}

	@Override
	public String typeName() {
		return PLANE;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public PartialModel getModel() {
		return AllPartialModels.SYMMETRY_PLANE;
	}

	@Override
	public void applyModelTransform(PoseStack ms) {
		super.applyModelTransform(ms);
		TransformStack.of(ms)
			.center()
			.rotateYDegrees(((Align) orientation) == Align.XY ? 0 : 90)
			.uncenter();
	}

	@Override
	public List<Component> getAlignToolTips() {
		return ImmutableList.of(Lang.translateDirect("orientation.alongZ"), Lang.translateDirect("orientation.alongX"));
	}

}
