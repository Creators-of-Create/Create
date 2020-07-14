package com.simibubi.create.content.contraptions.components.fan;

import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.content.contraptions.base.GeneratingKineticTileEntity;
import com.simibubi.create.content.contraptions.processing.HeaterBlock;
import com.simibubi.create.content.contraptions.processing.HeaterTileEntity;
import com.simibubi.create.content.logistics.block.chute.ChuteTileEntity;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.config.CKinetics;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;

public class EncasedFanTileEntity extends GeneratingKineticTileEntity {

	public AirCurrent airCurrent;
	protected int airCurrentUpdateCooldown;
	protected int entitySearchCooldown;
	protected boolean isGenerator;
	protected boolean updateAirFlow;

	public EncasedFanTileEntity(TileEntityType<? extends EncasedFanTileEntity> type) {
		super(type);
		isGenerator = false;
		airCurrent = new AirCurrent(this);
		updateAirFlow = true;
	}

	@Override
	public void readClientUpdate(CompoundNBT tag) {
		super.readClientUpdate(tag);
		airCurrent.rebuild();
	}

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		isGenerator = compound.getBoolean("Generating");
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putBoolean("Generating", isGenerator);
		return super.write(compound);
	}

	@Override
	public float calculateAddedStressCapacity() {
		return isGenerator ? super.calculateAddedStressCapacity() : 0;
	}

	@Override
	public float calculateStressApplied() {
		return isGenerator ? 0 : super.calculateStressApplied();
	}

	@Override
	public float getGeneratedSpeed() {
		return isGenerator ? AllConfigs.SERVER.kinetics.generatingFanSpeed.get() : 0;
	}

	public void updateGenerator(Direction facing) {
		boolean shouldGenerate = world.isBlockPowered(pos) && facing == Direction.DOWN
			&& world.isBlockPresent(pos.down()) && blockBelowIsHot();
		if (shouldGenerate == isGenerator)
			return;

		isGenerator = shouldGenerate;
		updateGeneratedRotation();
	}

	public boolean blockBelowIsHot() {
		if (world == null)
			return false;
		BlockState checkState = world.getBlockState(pos.down());
		return checkState.getBlock()
			.isIn(AllBlockTags.FAN_HEATERS.tag)
			|| (checkState.has(HeaterBlock.BLAZE_LEVEL) && checkState.get(HeaterBlock.BLAZE_LEVEL) >= 1);
	}

	public float getMaxDistance() {
		float speed = Math.abs(this.getSpeed());
		CKinetics config = AllConfigs.SERVER.kinetics;
		float distanceFactor = Math.min(speed / config.fanRotationArgmax.get(), 1);
		float pushDistance = MathHelper.lerp(distanceFactor, 3, config.fanPushDistance.get());
		float pullDistance = MathHelper.lerp(distanceFactor, 3f, config.fanPullDistance.get());
		return this.getSpeed() > 0 ? pushDistance : pullDistance;
	}

	public Direction getAirFlowDirection() {
		float speed = getSpeed();
		if (speed == 0)
			return null;
		Direction facing = getBlockState().get(BlockStateProperties.FACING);
		speed = convertToDirection(speed, facing);
		return speed > 0 ? facing : facing.getOpposite();
	}

	@Override
	public void onSpeedChanged(float prevSpeed) {
		super.onSpeedChanged(prevSpeed);
		updateAirFlow = true;
		updateChute();
	}

	public void updateChute() {
		Direction direction = getBlockState().get(EncasedFanBlock.FACING);
		if (!direction.getAxis()
			.isVertical())
			return;
		TileEntity poweredChute = world.getTileEntity(pos.offset(direction));
		if (!(poweredChute instanceof ChuteTileEntity))
			return;
		ChuteTileEntity chuteTE = (ChuteTileEntity) poweredChute;
		if (direction == Direction.DOWN)
			chuteTE.updatePull();
		else
			chuteTE.updatePush(1);
	}

	public void blockInFrontChanged() {
		updateAirFlow = true;
	}

	@Override
	public void tick() {
		super.tick();

		if (!world.isRemote && airCurrentUpdateCooldown-- <= 0) {
			airCurrentUpdateCooldown = AllConfigs.SERVER.kinetics.fanBlockCheckRate.get();
			updateAirFlow = true;
		}

		if (updateAirFlow) {
			updateAirFlow = false;
			airCurrent.rebuild();
			sendData();
		}

		if (getSpeed() == 0 || isGenerator)
			return;

		if (entitySearchCooldown-- <= 0) {
			entitySearchCooldown = 5;
			airCurrent.findEntities();
		}

		airCurrent.tick();
	}

}
