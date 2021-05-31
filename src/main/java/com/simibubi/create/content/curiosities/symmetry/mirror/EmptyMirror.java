package com.simibubi.create.content.curiosities.symmetry.mirror;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.core.PartialModel;

import net.minecraft.block.BlockState;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;

public class EmptyMirror extends SymmetryMirror {

	public static enum Align implements IStringSerializable {
		None("none");

		private final String name;
		private Align(String name) { this.name = name; }
		@Override public String getString() { return name; }
		@Override public String toString() { return name; }
	}

	public EmptyMirror(Vector3d pos) {
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
	public PartialModel getModel() {
		return null;
	}

	@Override
	public List<ITextComponent> getAlignToolTips() {
		return ImmutableList.of();
	}

}
