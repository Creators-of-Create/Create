package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import java.util.List;

import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.INamedIconOptions;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class WindmillBearingTileEntity extends MechanicalBearingTileEntity {

	protected ScrollOptionBehaviour<RotationDirection> movementDirection;
	protected float generated;

	public WindmillBearingTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void initialize() {
		updateGeneratedSpeed();
		super.initialize();
	}

	@Override
	public void assemble() {
		super.assemble();
		updateGeneratedSpeed();
	}

	@Override
	public void disassemble() {
		super.disassemble();
		updateGeneratedSpeed();
	}

	public void updateGeneratedSpeed() {
		if (!running) {
			generated = 0;
		} else if (movedContraption != null) {
			int sails = ((BearingContraption) movedContraption.getContraption()).getSailBlocks()
					/ AllConfigs.SERVER.kinetics.windmillSailsPerRPM.get();
			generated = Mth.clamp(sails, 1, 16) * getAngleSpeedDirection();
		}
	}

	@Override
	public void onSpeedChanged(float prevSpeed) {
		boolean cancelAssembly = assembleNextTick;
		super.onSpeedChanged(prevSpeed);
		assembleNextTick = cancelAssembly;
	}

	@Override
	public float getGeneratedSpeed() {
		return generated;
	}

	@Override
	protected boolean isWindmill() {
		return true;
	}

	protected float getAngleSpeedDirection() {
		RotationDirection rotationDirection = RotationDirection.values()[movementDirection.getValue()];
		return (rotationDirection == RotationDirection.CLOCKWISE ? 1 : -1);
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putFloat("LastGenerated", generated);
		super.write(compound, clientPacket);
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		if (!wasMoved)
			generated = compound.getFloat("LastGenerated");
		super.read(compound, clientPacket);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		behaviours.remove(movementMode);
		movementDirection = new ScrollOptionBehaviour<>(RotationDirection.class,
			Lang.translate("contraptions.windmill.rotation_direction"), this, getMovementModeSlot());
		movementDirection.requiresWrench();
		movementDirection.withCallback($ -> onDirectionChanged());
		behaviours.add(movementDirection);
	}

	private void onDirectionChanged() {
		if (!running)
			return;
		if (!level.isClientSide)
			updateGeneratedSpeed();
	}

	@Override
	public boolean isWoodenTop() {
		return true;
	}

	enum RotationDirection implements INamedIconOptions {

		CLOCKWISE(AllIcons.I_REFRESH), COUNTER_CLOCKWISE(AllIcons.I_ROTATE_CCW),;

		private final String translationKey;
		private final AllIcons icon;

		RotationDirection(AllIcons icon) {
			this.icon = icon;
			translationKey = "generic." + Lang.asId(name());
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

}
