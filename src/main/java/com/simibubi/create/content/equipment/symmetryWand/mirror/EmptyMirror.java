package com.simibubi.create.content.equipment.symmetryWand.mirror;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EmptyMirror extends SymmetryMirror {

	public static enum Align implements StringRepresentable {
		None("none");

		private final String name;
		private Align(String name) { this.name = name; }
		@Override public String getSerializedName() { return name; }
		@Override public String toString() { return name; }
	}

	public EmptyMirror(Vec3 pos) {
		super(pos);
		orientation = Align.None;
	}

	@Override
	protected void setOrientation() {
	}

	@Override
	public void setOrientation(int index) {
		this.orientation = Align.values()[index];
		orientationIndex = index;
	}

	@Override
	public Map<BlockPos, BlockState> process(BlockPos position, BlockState block) {
		return new HashMap<>();
	}

	@Override
	public String typeName() {
		return EMPTY;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public PartialModel getModel() {
		return null;
	}

	@Override
	public List<Component> getAlignToolTips() {
		return ImmutableList.of();
	}

}
