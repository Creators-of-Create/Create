package com.simibubi.create.content.kinetics.crank;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.Material;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.transmission.sequencer.SequencedGearshiftBlockEntity.SequenceContext;
import com.simibubi.create.content.kinetics.transmission.sequencer.SequencerInstructions;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.createmod.catnip.utility.VecHelper;
import net.createmod.catnip.utility.lang.Components;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ValveHandleBlockEntity extends HandCrankBlockEntity {

	public ScrollValueBehaviour angleInput;
	public int cooldown;

	protected int startAngle;
	protected int targetAngle;
	protected int totalUseTicks;

	public ValveHandleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		behaviours.add(angleInput = new ValveHandleScrollValueBehaviour(this).between(-180, 180));
		angleInput.onlyActiveWhen(this::showValue);
		angleInput.setValue(45);
	}

	@Override
	protected boolean clockwise() {
		return angleInput.getValue() < 0 ^ backwards;
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		compound.putInt("TotalUseTicks", totalUseTicks);
		compound.putInt("StartAngle", startAngle);
		compound.putInt("TargetAngle", targetAngle);
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		totalUseTicks = compound.getInt("TotalUseTicks");
		startAngle = compound.getInt("StartAngle");
		targetAngle = compound.getInt("TargetAngle");
	}

	@Override
	public void tick() {
		super.tick();
		if (inUse == 0 && cooldown > 0)
			cooldown--;
		independentAngle = level.isClientSide() ? getIndependentAngle(0) : 0;
	}

	@Override
	public float getIndependentAngle(float partialTicks) {
		if (inUse == 0 && source != null && getSpeed() != 0)
			return KineticBlockEntityRenderer.getAngleForTe(this, worldPosition,
				KineticBlockEntityRenderer.getRotationAxisOf(this));

		int step = getBlockState().getOptionalValue(ValveHandleBlock.FACING)
			.orElse(Direction.SOUTH)
			.getAxisDirection()
			.getStep();

		return (inUse > 0 && totalUseTicks > 0
			? Mth.lerp(Math.min(totalUseTicks, totalUseTicks - inUse + partialTicks) / (float) totalUseTicks,
				startAngle, targetAngle)
			: targetAngle) * Mth.DEG_TO_RAD * (backwards ? -1 : 1) * step;
	}

	public boolean showValue() {
		return inUse == 0;
	}

	public boolean activate(boolean sneak) {
		if (getTheoreticalSpeed() != 0)
			return false;
		if (inUse > 0 || cooldown > 0)
			return false;
		if (level.isClientSide)
			return true;

		// Always overshoot, target will stop early
		int value = angleInput.getValue();
		int target = Math.abs(value);
		int rotationSpeed = AllBlocks.COPPER_VALVE_HANDLE.get()
			.getRotationSpeed();
		double degreesPerTick = KineticBlockEntity.convertToAngular(rotationSpeed);
		inUse = (int) Math.ceil(target / degreesPerTick) + 2;

		startAngle = (int) ((independentAngle) % 90 + 360) % 90;
		targetAngle = Math.round((startAngle + (target > 135 ? 180 : 90) * Mth.sign(value)) / 90f) * 90;
		totalUseTicks = inUse;
		backwards = sneak;

		sequenceContext = SequenceContext.fromGearshift(SequencerInstructions.TURN_ANGLE, rotationSpeed, target);
		updateGeneratedRotation();
		cooldown = 4;

		return true;
	}

	@Override
	protected void copySequenceContextFrom(KineticBlockEntity sourceBE) {}

	@Override
	@OnlyIn(Dist.CLIENT)
	public SuperByteBuffer getRenderedHandle() {
		return CachedBuffers.block(getBlockState());
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public Instancer<ModelData> getRenderedHandleInstance(Material<ModelData> material) {
		return material.getModel(getBlockState());
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean shouldRenderShaft() {
		return false;
	}

	public static class ValveHandleScrollValueBehaviour extends ScrollValueBehaviour {

		public ValveHandleScrollValueBehaviour(SmartBlockEntity be) {
			super(CreateLang.translateDirect("kinetics.valve_handle.rotated_angle"), be, new ValveHandleValueBox());
			withFormatter(v -> String.valueOf(Math.abs(v)) + CreateLang.translateDirect("generic.unit.degrees")
				.getString());
		}

		@Override
		public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
			ImmutableList<Component> rows = ImmutableList.of(Components.literal("\u27f3")
				.withStyle(ChatFormatting.BOLD),
				Components.literal("\u27f2")
					.withStyle(ChatFormatting.BOLD));
			return new ValueSettingsBoard(label, 180, 45, rows, new ValueSettingsFormatter(this::formatValue));
		}

		@Override
		public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlHeld) {
			int value = Math.max(1, valueSetting.value());
			if (!valueSetting.equals(getValueSettings()))
				playFeedbackSound(this);
			setValue(valueSetting.row() == 0 ? -value : value);
		}

		@Override
		public ValueSettings getValueSettings() {
			return new ValueSettings(value < 0 ? 0 : 1, Math.abs(value));
		}

		public MutableComponent formatValue(ValueSettings settings) {
			return CreateLang.number(Math.max(1, Math.abs(settings.value())))
				.add(CreateLang.translateDirect("generic.unit.degrees"))
				.component();
		}

		@Override
		public void onShortInteract(Player player, InteractionHand hand, Direction side) {
			BlockState blockState = blockEntity.getBlockState();
			if (blockState.getBlock() instanceof ValveHandleBlock vhb)
				vhb.clicked(getWorld(), getPos(), blockState, player, hand);
		}

	}

	public static class ValveHandleValueBox extends ValueBoxTransform.Sided {

		@Override
		protected boolean isSideActive(BlockState state, Direction direction) {
			return direction == state.getValue(ValveHandleBlock.FACING);
		}

		@Override
		protected Vec3 getSouthLocation() {
			return VecHelper.voxelSpace(8, 8, 4.5);
		}

		@Override
		public boolean testHit(BlockState state, Vec3 localHit) {
			Vec3 offset = getLocalOffset(state);
			if (offset == null)
				return false;
			return localHit.distanceTo(offset) < scale / 1.5f;
		}

	}

}
