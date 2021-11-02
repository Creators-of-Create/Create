package com.simibubi.create.content.contraptions.components.fan;

import javax.annotation.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.content.contraptions.base.GeneratingKineticTileEntity;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.logistics.block.chute.ChuteTileEntity;
import com.simibubi.create.foundation.config.AllConfigs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

@MethodsReturnNonnullByDefault
public class EncasedFanTileEntity extends GeneratingKineticTileEntity implements IAirCurrentSource {

	public AirCurrent airCurrent;
	protected int airCurrentUpdateCooldown;
	protected int entitySearchCooldown;
	protected boolean isGenerator;
	protected boolean updateAirFlow;
	protected boolean updateGenerator;

	public EncasedFanTileEntity(BlockEntityType<? extends EncasedFanTileEntity> type) {
		super(type);
		isGenerator = false;
		airCurrent = new AirCurrent(this);
		updateAirFlow = true;
		updateGenerator = false;
	}

	@Override
	protected void fromTag(BlockState state, CompoundTag compound, boolean clientPacket) {
		super.fromTag(state, compound, clientPacket);
		if (!wasMoved) 
			isGenerator = compound.getBoolean("Generating");
		if (clientPacket)
			airCurrent.rebuild();
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putBoolean("Generating", isGenerator);
		super.write(compound, clientPacket);
	}

	@Override
	public float calculateAddedStressCapacity() {
		return lastCapacityProvided = (isGenerator ? super.calculateAddedStressCapacity() : 0);
	}

	@Override
	public float calculateStressApplied() {
		return isGenerator ? 0 : super.calculateStressApplied();
	}

	@Override
	public float getGeneratedSpeed() {
		return isGenerator ? AllConfigs.SERVER.kinetics.generatingFanSpeed.get() : 0;
	}

	public void queueGeneratorUpdate() {
		updateGenerator = true;
	}

	public void updateGenerator() {
		BlockState blockState = getBlockState();
		boolean shouldGenerate = true;

		if (!AllBlocks.ENCASED_FAN.has(blockState))
			shouldGenerate = false;

		if (shouldGenerate && blockState.getValue(EncasedFanBlock.FACING) != Direction.DOWN)
			shouldGenerate = false;

		if (shouldGenerate)
			shouldGenerate = level != null && level.hasNeighborSignal(worldPosition) && level.isLoaded(worldPosition.below()) && blockBelowIsHot();

		if (shouldGenerate == isGenerator)
			return;
		isGenerator = shouldGenerate;
		updateGeneratedRotation();
	}

	public boolean blockBelowIsHot() {
		if (level == null)
			return false;
		BlockState checkState = level.getBlockState(worldPosition.below());

		if (!checkState.getBlock()
			.is(AllBlockTags.FAN_HEATERS.tag))
			return false;

		if (checkState.hasProperty(BlazeBurnerBlock.HEAT_LEVEL) && !checkState.getValue(BlazeBurnerBlock.HEAT_LEVEL)
			.isAtLeast(BlazeBurnerBlock.HeatLevel.FADING))
			return false;

		if (checkState.hasProperty(BlockStateProperties.LIT) && !checkState.getValue(BlockStateProperties.LIT))
			return false;

		return true;
	}

	@Override
	public AirCurrent getAirCurrent() {
		return airCurrent;
	}

	@Nullable
	@Override
	public Level getAirCurrentWorld() {
		return level;
	}

	@Override
	public BlockPos getAirCurrentPos() {
		return worldPosition;
	}

	@Override
	public Direction getAirflowOriginSide() {
		return this.getBlockState()
			.getValue(EncasedFanBlock.FACING);
	}

	@Override
	public Direction getAirFlowDirection() {
		float speed = getSpeed();
		if (speed == 0)
			return null;
		Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
		speed = convertToDirection(speed, facing);
		return speed > 0 ? facing : facing.getOpposite();
	}

	@Override
	public boolean isSourceRemoved() {
		return remove;
	}

	@Override
	public void onSpeedChanged(float prevSpeed) {
		super.onSpeedChanged(prevSpeed);
		updateAirFlow = true;
		updateChute();
	}

	public void updateChute() {
		Direction direction = getBlockState().getValue(EncasedFanBlock.FACING);
		if (!direction.getAxis()
			.isVertical())
			return;
		BlockEntity poweredChute = level.getBlockEntity(worldPosition.relative(direction));
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

		boolean server = !level.isClientSide || isVirtual();
		
		if (server && airCurrentUpdateCooldown-- <= 0) {
			airCurrentUpdateCooldown = AllConfigs.SERVER.kinetics.fanBlockCheckRate.get();
			updateAirFlow = true;
		}

		if (updateAirFlow) {
			updateAirFlow = false;
			airCurrent.rebuild();
			sendData();
		}
		
		if (updateGenerator) {
			updateGenerator = false;
			updateGenerator();
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
