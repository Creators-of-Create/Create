package com.simibubi.create.content.contraptions.actors.roller;

import java.util.List;

import com.jozufozu.flywheel.lib.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RollerBlockEntity extends SmartBlockEntity {

	// For simulations such as Ponder
	private float manuallyAnimatedSpeed;

	public FilteringBehaviour filtering;
	public ScrollOptionBehaviour<RollingMode> mode;

	private boolean dontPropagate;

	public RollerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		dontPropagate = false;
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(filtering = new FilteringBehaviour(this, new RollerValueBox(3)));
		behaviours.add(mode = new ScrollOptionBehaviour<RollingMode>(RollingMode.class,
			Lang.translateDirect("contraptions.roller_mode"), this, new RollerValueBox(-3)));

		filtering.setLabel(Lang.translateDirect("contraptions.mechanical_roller.pave_material"));
		filtering.withCallback(this::onFilterChanged);
		filtering.withPredicate(this::isValidMaterial);
		mode.withCallback(this::onModeChanged);
	}

	protected void onModeChanged(int mode) {
		shareValuesToAdjacent();
	}

	protected void onFilterChanged(ItemStack newFilter) {
		shareValuesToAdjacent();
	}

	protected boolean isValidMaterial(ItemStack newFilter) {
		if (newFilter.isEmpty())
			return true;
		BlockState appliedState = RollerMovementBehaviour.getStateToPaveWith(newFilter);
		if (appliedState.isAir())
			return false;
		if (appliedState.getBlock() instanceof EntityBlock)
			return false;
		if (appliedState.getBlock() instanceof StairBlock)
			return false;
		VoxelShape shape = appliedState.getShape(level, worldPosition);
		if (shape.isEmpty() || !shape.bounds()
			.equals(Shapes.block()
				.bounds()))
			return false;
		VoxelShape collisionShape = appliedState.getCollisionShape(level, worldPosition);
		if (collisionShape.isEmpty())
			return false;
		return true;
	}

	@Override
	protected AABB createRenderBoundingBox() {
		return new AABB(worldPosition).inflate(1);
	}

	public float getAnimatedSpeed() {
		return manuallyAnimatedSpeed;
	}

	public void setAnimatedSpeed(float speed) {
		manuallyAnimatedSpeed = speed;
	}

	public void searchForSharedValues() {
		BlockState blockState = getBlockState();
		Direction facing = blockState.getOptionalValue(RollerBlock.FACING)
			.orElse(Direction.SOUTH);

		for (int side : Iterate.positiveAndNegative) {
			BlockPos pos = worldPosition.relative(facing.getClockWise(), side);
			if (level.getBlockState(pos) != blockState)
				continue;
			if (!(level.getBlockEntity(pos) instanceof RollerBlockEntity otherRoller))
				continue;
			acceptSharedValues(otherRoller.mode.getValue(), otherRoller.filtering.getFilter());
			shareValuesToAdjacent();
			break;
		}
	}

	protected void acceptSharedValues(int mode, ItemStack filter) {
		dontPropagate = true;
		this.filtering.setFilter(filter.copy());
		this.mode.setValue(mode);
		dontPropagate = false;
		notifyUpdate();
	}

	public void shareValuesToAdjacent() {
		if (dontPropagate || level.isClientSide())
			return;
		BlockState blockState = getBlockState();
		Direction facing = blockState.getOptionalValue(RollerBlock.FACING)
			.orElse(Direction.SOUTH);

		for (int side : Iterate.positiveAndNegative) {
			for (int i = 1; i < 100; i++) {
				BlockPos pos = worldPosition.relative(facing.getClockWise(), side * i);
				if (level.getBlockState(pos) != blockState)
					break;
				if (!(level.getBlockEntity(pos) instanceof RollerBlockEntity otherRoller))
					break;
				otherRoller.acceptSharedValues(mode.getValue(), filtering.getFilter());
			}
		}
	}

	static enum RollingMode implements INamedIconOptions {

		TUNNEL_PAVE(AllIcons.I_ROLLER_PAVE),
		STRAIGHT_FILL(AllIcons.I_ROLLER_FILL),
		WIDE_FILL(AllIcons.I_ROLLER_WIDE_FILL),

		;

		private String translationKey;
		private AllIcons icon;

		private RollingMode(AllIcons icon) {
			this.icon = icon;
			translationKey = "contraptions.roller_mode." + Lang.asId(name());
		}

		@Override
		public AllIcons getIcon() {
			return icon;
		}

		@Override
		public String getTranslationKey() {
			return translationKey;
		}

	}

	private final class RollerValueBox extends ValueBoxTransform {

		private int hOffset;

		public RollerValueBox(int hOffset) {
			this.hOffset = hOffset;
		}

		@Override
		public void rotate(BlockState state, PoseStack ms) {
			Direction facing = state.getValue(RollerBlock.FACING);
			float yRot = AngleHelper.horizontalAngle(facing) + 180;
			TransformStack.of(ms)
				.rotateYDegrees(yRot)
				.rotateXDegrees(90);
		}

		@Override
		public boolean testHit(BlockState state, Vec3 localHit) {
			Vec3 offset = getLocalOffset(state);
			if (offset == null)
				return false;
			return localHit.distanceTo(offset) < scale / 3;
		}

		@Override
		public Vec3 getLocalOffset(BlockState state) {
			Direction facing = state.getValue(RollerBlock.FACING);
			float stateAngle = AngleHelper.horizontalAngle(facing) + 180;
			return VecHelper.rotateCentered(VecHelper.voxelSpace(8 + hOffset, 15.5f, 11), stateAngle, Axis.Y);
		}

	}

}
