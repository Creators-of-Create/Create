package com.simibubi.create.content.contraptions.components.actors;

import java.util.List;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollvalue.INamedIconOptions;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollvalue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.utility.AngleHelper;
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

	public RollerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(filtering = new FilteringBehaviour(this, new RollerValueBox(4)).moveText(new Vec3(-30, 23, 10))
			.withCallback(this::onFilterChanged));
		behaviours.add(mode = new ScrollOptionBehaviour<RollingMode>(RollingMode.class,
			Lang.translateDirect("contraptions.roller_mode"), this, new RollerValueBox(-2)));
	}

	protected boolean onFilterChanged(ItemStack newFilter) {
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
		protected void rotate(BlockState state, PoseStack ms) {
			Direction facing = state.getValue(RollerBlock.FACING);
			float yRot = AngleHelper.horizontalAngle(facing) + 180;
			TransformStack.cast(ms)
				.rotateY(yRot)
				.rotateX(90);
		}

		@Override
		protected Vec3 getLocalOffset(BlockState state) {
			Direction facing = state.getValue(RollerBlock.FACING);
			float stateAngle = AngleHelper.horizontalAngle(facing) + 180;
			return VecHelper.rotateCentered(VecHelper.voxelSpace(7.75 + hOffset, 15.5f, 11), stateAngle, Axis.Y);
		}

	}

}
