package com.simibubi.create.content.contraptions.actors.contraptionControls;

import java.util.List;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.content.contraptions.actors.trainControls.ControlsBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.DyeHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ContraptionControlsBlockEntity extends SmartBlockEntity {

	public FilteringBehaviour filtering;
	public boolean disabled;
	public boolean powered;

	public LerpedFloat indicator;
	public LerpedFloat button;

	public ContraptionControlsBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		indicator = LerpedFloat.angular()
			.startWithValue(0);
		button = LerpedFloat.linear()
			.startWithValue(0)
			.chase(0, 0.125f, Chaser.EXP);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(filtering = new FilteringBehaviour(this, new ControlsSlot()));
		filtering.setLabel(Lang.translateDirect("contraptions.contoller.target"));
		filtering.withPredicate(AllItemTags.CONTRAPTION_CONTROLLED::matches);
	}

	public void pressButton() {
		button.setValue(1);
	}

	public void updatePoweredState() {
		if (level.isClientSide())
			return;
		boolean powered = level.hasNeighborSignal(worldPosition);
		if (this.powered == powered)
			return;
		this.powered = powered;
		this.disabled = powered;
		notifyUpdate();
	}

	@Override
	public void initialize() {
		super.initialize();
		updatePoweredState();
	}

	@Override
	public void tick() {
		super.tick();
		if (!level.isClientSide())
			return;
		tickAnimations();
		int value = disabled ? 4 * 45 : 0;
		indicator.setValue(value);
		indicator.updateChaseTarget(value);
	}

	public void tickAnimations() {
		button.tickChaser();
		indicator.tickChaser();
	}

	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		disabled = tag.getBoolean("Disabled");
		powered = tag.getBoolean("Powered");
	}

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		tag.putBoolean("Disabled", disabled);
		tag.putBoolean("Powered", powered);
	}

	public static void sendStatus(Player player, ItemStack filter, boolean enabled) {
		MutableComponent state = Lang.translate("contraption.controls.actor_toggle." + (enabled ? "on" : "off"))
			.color(DyeHelper.DYE_TABLE.get(enabled ? DyeColor.LIME : DyeColor.ORANGE)
				.getFirst())
			.component();

		if (filter.isEmpty()) {
			Lang.translate("contraption.controls.all_actor_toggle", state)
				.sendStatus(player);
			return;
		}

		Lang.translate("contraption.controls.specific_actor_toggle", filter.getHoverName()
			.getString(), state)
			.sendStatus(player);
	}

	public static class ControlsSlot extends ValueBoxTransform.Sided {

		@Override
		public Vec3 getLocalOffset(BlockState state) {
			Direction facing = state.getValue(ControlsBlock.FACING);
			float yRot = AngleHelper.horizontalAngle(facing);
			return VecHelper.rotateCentered(VecHelper.voxelSpace(8, 12f, 5.5f), yRot, Axis.Y);
		}

		@Override
		public void rotate(BlockState state, PoseStack ms) {
			Direction facing = state.getValue(ControlsBlock.FACING);
			float yRot = AngleHelper.horizontalAngle(facing);
			TransformStack.cast(ms)
				.rotateY(yRot + 180)
				.rotateX(67.5f);
		}

		@Override
		public float getScale() {
			return .508f;
		}

		@Override
		protected Vec3 getSouthLocation() {
			return Vec3.ZERO;
		}

	}

}
