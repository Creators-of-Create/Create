package com.simibubi.create.content.equipment.symmetryWand.mirror;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class SymmetryMirror {

	public static final String EMPTY = "empty";
	public static final String PLANE = "plane";
	public static final String CROSS_PLANE = "cross_plane";
	public static final String TRIPLE_PLANE = "triple_plane";

	protected Vec3 position;
	protected StringRepresentable orientation;
	protected int orientationIndex;
	public boolean enable;

	public SymmetryMirror(Vec3 pos) {
		position = pos;
		enable = true;
		orientationIndex = 0;
	}

	public static List<Component> getMirrors() {
		return ImmutableList.of(Lang.translateDirect("symmetry.mirror.plane"), Lang.translateDirect("symmetry.mirror.doublePlane"),
			Lang.translateDirect("symmetry.mirror.triplePlane"));
	}

	public StringRepresentable getOrientation() {
		return orientation;
	}

	public Vec3 getPosition() {
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

	@OnlyIn(Dist.CLIENT)
	public abstract PartialModel getModel();

	public void applyModelTransform(PoseStack ms) {}

	private static final String $ORIENTATION = "direction";
	private static final String $POSITION = "pos";
	private static final String $TYPE = "type";
	private static final String $ENABLE = "enable";

	public CompoundTag writeToNbt() {
		CompoundTag nbt = new CompoundTag();
		nbt.putInt($ORIENTATION, orientationIndex);

		ListTag floatList = new ListTag();
		floatList.add(FloatTag.valueOf((float) position.x));
		floatList.add(FloatTag.valueOf((float) position.y));
		floatList.add(FloatTag.valueOf((float) position.z));
		nbt.put($POSITION, floatList);
		nbt.putString($TYPE, typeName());
		nbt.putBoolean($ENABLE, enable);

		return nbt;
	}

	public static SymmetryMirror fromNBT(CompoundTag nbt) {
		ListTag floatList = nbt.getList($POSITION, 5);
		Vec3 pos = new Vec3(floatList.getFloat(0), floatList.getFloat(1), floatList.getFloat(2));
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

	protected Vec3 getDiff(BlockPos position) {
		return this.position.scale(-1)
			.add(position.getX(), position.getY(), position.getZ());
	}

	protected BlockPos getIDiff(BlockPos position) {
		Vec3 diff = getDiff(position);
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
				if (in.getValue(property) == Direction.DOWN) {
					return in.setValue((DirectionProperty) property, Direction.UP);
				} else if (in.getValue(property) == Direction.UP) {
					return in.setValue((DirectionProperty) property, Direction.DOWN);
				}
			}
		}
		return in;
	}

	protected BlockState flipZ(BlockState in) {
		return in.mirror(Mirror.LEFT_RIGHT);
	}

	@SuppressWarnings("deprecation")
	protected BlockState flipD1(BlockState in) {
		return in.rotate(Rotation.COUNTERCLOCKWISE_90)
			.mirror(Mirror.FRONT_BACK);
	}

	@SuppressWarnings("deprecation")
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

	public void setPosition(Vec3 pos3d) {
		this.position = pos3d;
	}

	public abstract List<Component> getAlignToolTips();

}
