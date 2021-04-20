package com.simibubi.create.content.contraptions.components.fan;

import javax.annotation.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.content.contraptions.base.GeneratingKineticTileEntity;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.logistics.block.chute.ChuteTileEntity;
import com.simibubi.create.foundation.config.AllConfigs;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@MethodsReturnNonnullByDefault
public class EncasedFanTileEntity extends GeneratingKineticTileEntity implements IAirCurrentSource {

	public AirCurrent airCurrent;
	protected int airCurrentUpdateCooldown;
	protected int entitySearchCooldown;
	protected boolean isGenerator;
	protected boolean updateAirFlow;
	protected boolean updateGenerator;

	public EncasedFanTileEntity(TileEntityType<? extends EncasedFanTileEntity> type) {
		super(type);
		isGenerator = false;
		airCurrent = new AirCurrent(this);
		updateAirFlow = true;
		updateGenerator = false;
	}

	@Override
	protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
		super.fromTag(state, compound, clientPacket);
		if (!wasMoved) 
			isGenerator = compound.getBoolean("Generating");
		if (clientPacket)
			airCurrent.rebuild();
	}

	@Override
	public void write(CompoundNBT compound, boolean clientPacket) {
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

		if (shouldGenerate && blockState.get(EncasedFanBlock.FACING) != Direction.DOWN)
			shouldGenerate = false;

		if (shouldGenerate)
			shouldGenerate = world != null && world.isBlockPowered(pos) && world.isBlockPresent(pos.down()) && blockBelowIsHot();

		if (shouldGenerate == isGenerator)
			return;
		isGenerator = shouldGenerate;
		updateGeneratedRotation();
	}

	public boolean blockBelowIsHot() {
		if (world == null)
			return false;
		BlockState checkState = world.getBlockState(pos.down());

		if (!checkState.getBlock()
			.isIn(AllBlockTags.FAN_HEATERS.tag))
			return false;

		if (checkState.contains(BlazeBurnerBlock.HEAT_LEVEL) && !checkState.get(BlazeBurnerBlock.HEAT_LEVEL)
			.isAtLeast(BlazeBurnerBlock.HeatLevel.FADING))
			return false;

		if (checkState.contains(BlockStateProperties.LIT) && !checkState.get(BlockStateProperties.LIT))
			return false;

		return true;
	}

	@Override
	public AirCurrent getAirCurrent() {
		return airCurrent;
	}

	@Nullable
	@Override
	public World getAirCurrentWorld() {
		return world;
	}

	@Override
	public BlockPos getAirCurrentPos() {
		return pos;
	}

	@Override
	public Direction getAirflowOriginSide() {
		return this.getBlockState()
			.get(EncasedFanBlock.FACING);
	}

	@Override
	public Direction getAirFlowDirection() {
		float speed = getSpeed();
		if (speed == 0)
			return null;
		Direction facing = getBlockState().get(BlockStateProperties.FACING);
		speed = convertToDirection(speed, facing);
		return speed > 0 ? facing : facing.getOpposite();
	}

	@Override
	public boolean isSourceRemoved() {
		return removed;
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

		boolean server = !world.isRemote || isVirtual();
		
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
