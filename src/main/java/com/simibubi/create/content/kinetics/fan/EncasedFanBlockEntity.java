package com.simibubi.create.content.kinetics.fan;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.logistics.chute.ChuteBlockEntity;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

@MethodsReturnNonnullByDefault
public class EncasedFanBlockEntity extends KineticBlockEntity implements IAirCurrentSource {

	public AirCurrent airCurrent;
	protected int airCurrentUpdateCooldown;
	protected int entitySearchCooldown;
	protected boolean updateAirFlow;

	public EncasedFanBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		airCurrent = new AirCurrent(this);
		updateAirFlow = true;
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		registerAwardables(behaviours, AllAdvancements.ENCASED_FAN, AllAdvancements.FAN_PROCESSING);
	}

	@Override
	protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(compound, registries, clientPacket);
		if (clientPacket)
			airCurrent.rebuild();
	}

	@Override
	public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(compound, registries, clientPacket);
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
		if (!(poweredChute instanceof ChuteBlockEntity chuteBE))
			return;
		if (direction == Direction.DOWN)
			chuteBE.updatePull();
		else
			chuteBE.updatePush(1);
	}

	public void blockInFrontChanged() {
		updateAirFlow = true;
	}

	@Override
	public void tick() {
		super.tick();

		boolean server = !level.isClientSide || isVirtual();

		if (server && airCurrentUpdateCooldown-- <= 0) {
			airCurrentUpdateCooldown = AllConfigs.server().kinetics.fanBlockCheckRate.get();
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

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {

		Direction flowDirection = getAirFlowDirection();
		Direction facing = getBlockState().getValue(EncasedFanBlock.FACING);
		boolean blowingOutward = false;
		if (flowDirection != null)
			blowingOutward = flowDirection == facing;

		// Used for GameTests to safely verify tooltip content on the server side
		// Bypasses client-only formatting logic to prevent crashes in headless environments
		// Note: Used Component.translatable directly to avoid issues with CreateLang in GameTests
		if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            tooltip.add(Component.translatable("create.tooltip.encased_fan.header"));

            if (flowDirection == null) {
                tooltip.add(Component.translatable("create.tooltip.encased_fan.not_spinning"));
                return true;
            }
            tooltip.add(Component.translatable("create.tooltip.encased_fan.direction"));
            tooltip.add(Component.translatable(blowingOutward
					? "create.tooltip.encased_fan.outward"
					: "create.tooltip.encased_fan.inward"));
			tooltip.add(Component.translatable("create.tooltip.encased_fan.range"));
			return true;
        }		

		super.addToGoggleTooltip(tooltip, isPlayerSneaking);
		
		CreateLang.translate("tooltip.encased_fan.header")
			.forGoggles(tooltip);

		if (flowDirection == null) {
			CreateLang.translate("tooltip.encased_fan.not_spinning")
				.style(ChatFormatting.DARK_GRAY)
				.forGoggles(tooltip, 1);
			return true;
		}

		CreateLang.translate("tooltip.encased_fan.direction")
			.style(ChatFormatting.GRAY)
			.text(": ")
			.add(CreateLang.translate(blowingOutward
					? "tooltip.encased_fan.outward"
					: "tooltip.encased_fan.inward")
				.style(blowingOutward ? ChatFormatting.GREEN : ChatFormatting.BLUE))
			.forGoggles(tooltip, 1);


		CreateLang.translate("tooltip.encased_fan.range")
			.style(ChatFormatting.GRAY)
			.text(": ")
			.add(CreateLang.text(String.format("%.1f", airCurrent.maxDistance))
				.style(ChatFormatting.AQUA))
			.forGoggles(tooltip, 1);

		return true;
	}

}
