package com.simibubi.create.content.contraptions.components.fan;

import java.util.List;

import javax.annotation.Nullable;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.logistics.block.chute.ChuteTileEntity;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

@MethodsReturnNonnullByDefault
public class EncasedFanTileEntity extends KineticTileEntity implements IAirCurrentSource {

	public AirCurrent airCurrent;
	protected int airCurrentUpdateCooldown;
	protected int entitySearchCooldown;
	protected boolean updateAirFlow;

	public EncasedFanTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		airCurrent = new AirCurrent(this);
		updateAirFlow = true;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		registerAwardables(behaviours, AllAdvancements.ENCASED_FAN, AllAdvancements.FAN_PROCESSING);
	}
	
	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		if (clientPacket)
			airCurrent.rebuild();
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		super.write(compound, clientPacket);
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
	public void remove() {
		super.remove();
		updateChute();
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
			if (airCurrent.maxDistance > 0)
				award(AllAdvancements.ENCASED_FAN);
			sendData();
		}

		if (getSpeed() == 0)
			return;

		if (entitySearchCooldown-- <= 0) {
			entitySearchCooldown = 5;
			airCurrent.findEntities();
		}

		airCurrent.tick();
	}

}
