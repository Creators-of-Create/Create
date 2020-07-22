package com.simibubi.create.content.curiosities.symmetry.mirror;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.Property;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public abstract class SymmetryMirror {

	public static final String EMPTY = "empty";
	public static final String PLANE = "plane";
	public static final String CROSS_PLANE = "cross_plane";
	public static final String TRIPLE_PLANE = "triple_plane";

	protected Vector3d position;
	protected IStringSerializable orientation;
	protected int orientationIndex;
	public boolean enable;

	public SymmetryMirror(Vector3d pos) {
		position = pos;
		enable = true;
		orientationIndex = 0;
	}

	public static List<String> getMirrors() {
		return ImmutableList.of(Lang.translate("symmetry.mirror.plane"), Lang.translate("symmetry.mirror.doublePlane"),
			Lang.translate("symmetry.mirror.triplePlane"));
	}

	public IStringSerializable getOrientation() {
		return orientation;
	}

	public Vector3d getPosition() {
		return position;
	}

	public int getOrientationIndex() {
		return orientationIndex;
	}

	public void rotate(boolean forward) {
		orientationIndex += forward ? 1 : -1;
		setOrientation();
	}

	public void process(Map<BlockPos, BlockState> blocks) {
		Map<BlockPos, BlockState> result = new HashMap<>();
		for (BlockPos pos : blocks.keySet()) {
			result.putAll(process(pos, blocks.get(pos)));
		}
		blocks.putAll(result);
	}

	public abstract Map<BlockPos, BlockState> process(BlockPos position, BlockState block);

	protected abstract void setOrientation();

	public abstract void setOrientation(int index);

	public abstract String typeName();

	public abstract AllBlockPartials getModel();

	public void applyModelTransform(MatrixStack ms) {}

	private static final String $ORIENTATION = "direction";
	private static final String $POSITION = "pos";
	private static final String $TYPE = "type";
	private static final String $ENABLE = "enable";

	public CompoundNBT writeToNbt() {
		CompoundNBT nbt = new CompoundNBT();
		nbt.putInt($ORIENTATION, orientationIndex);

		ListNBT floatList = new ListNBT();
		floatList.add(FloatNBT.of((float) position.x));
		floatList.add(FloatNBT.of((float) position.y));
		floatList.add(FloatNBT.of((float) position.z));
		nbt.put($POSITION, floatList);
		nbt.putString($TYPE, typeName());
		nbt.putBoolean($ENABLE, enable);

		return nbt;
	}

	public static SymmetryMirror fromNBT(CompoundNBT nbt) {
		ListNBT floatList = nbt.getList($POSITION, 5);
		Vector3d pos = new Vector3d(floatList.getFloat(0), floatList.getFloat(1), floatList.getFloat(2));
		SymmetryMirror element;

		switch (nbt.getString($TYPE)) {
		case PLANE:
			element = new PlaneMirror(pos);
			break;
		case CROSS_PLANE:
			element = new CrossPlaneMirror(pos);
			break;
		case TRIPLE_PLANE:
			element = new TriplePlaneMirror(pos);
			break;
		default:
			element = new EmptyMirror(pos);
			break;
		}

		element.setOrientation(nbt.getInt($ORIENTATION));
		element.enable = nbt.getBoolean($ENABLE);

		return element;
	}

	protected Vector3d getDiff(BlockPos position) {
		return this.position.scale(-1)
			.add(position.getX(), position.getY(), position.getZ());
	}

	protected BlockPos getIDiff(BlockPos position) {
		Vector3d diff = getDiff(position);
		return new BlockPos((int) diff.x, (int) diff.y, (int) diff.z);
	}

	protected BlockState flipX(BlockState in) {
		return in.mirror(Mirror.FRONT_BACK);
	}

	protected BlockState flipY(BlockState in) {
		for (Property<?> property : in.getProperties()) {

			if (property == BlockStateProperties.HALF)
				return in.cycle(property);
			// Directional Blocks
			if (property instanceof DirectionProperty) {
				if (in.get(property) == Direction.DOWN) {
					return in.with((DirectionProperty) property, Direction.UP);
				} else if (in.get(property) == Direction.UP) {
					return in.with((DirectionProperty) property, Direction.DOWN);
				}
			}
		}
		return in;
	}

	protected BlockState flipZ(BlockState in) {
		return in.mirror(Mirror.LEFT_RIGHT);
	}

	protected BlockState flipD1(BlockState in) {
		return in.rotate(Rotation.COUNTERCLOCKWISE_90)
			.mirror(Mirror.FRONT_BACK);
	}

	protected BlockState flipD2(BlockState in) {
		return in.rotate(Rotation.COUNTERCLOCKWISE_90)
			.mirror(Mirror.LEFT_RIGHT);
	}

	protected BlockPos flipX(BlockPos position) {
		BlockPos diff = getIDiff(position);
		return new BlockPos(position.getX() - 2 * diff.getX(), position.getY(), position.getZ());
	}

	protected BlockPos flipY(BlockPos position) {
		BlockPos diff = getIDiff(position);
		return new BlockPos(position.getX(), position.getY() - 2 * diff.getY(), position.getZ());
	}

	protected BlockPos flipZ(BlockPos position) {
		BlockPos diff = getIDiff(position);
		return new BlockPos(position.getX(), position.getY(), position.getZ() - 2 * diff.getZ());
	}

	protected BlockPos flipD2(BlockPos position) {
		BlockPos diff = getIDiff(position);
		return new BlockPos(position.getX() - diff.getX() + diff.getZ(), position.getY(),
			position.getZ() - diff.getZ() + diff.getX());
	}

	protected BlockPos flipD1(BlockPos position) {
		BlockPos diff = getIDiff(position);
		return new BlockPos(position.getX() - diff.getX() - diff.getZ(), position.getY(),
			position.getZ() - diff.getZ() - diff.getX());
	}

	public void setPosition(Vector3d pos3d) {
		this.position = pos3d;
	}

	public abstract List<String> getAlignToolTips();

}
