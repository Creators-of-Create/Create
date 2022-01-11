package com.simibubi.create.content.contraptions.relays.gearbox;

import java.util.Arrays;
import java.util.List;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.contraptions.solver.AllConnections;
import com.simibubi.create.content.contraptions.solver.ConnectionsBuilder;
import com.simibubi.create.content.contraptions.solver.KineticConnections;
import com.simibubi.create.foundation.block.ITE;

import com.simibubi.create.foundation.utility.DirectionHelper;

import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootContext.Builder;
import net.minecraft.world.phys.HitResult;

public class GearboxBlock extends RotatedPillarKineticBlock implements ITE<GearboxTileEntity> {

	public GearboxBlock(Properties properties) {
		super(properties);
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState state) {
		return PushReaction.PUSH_ONLY;
	}

	@Override
	public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
		super.fillItemCategory(group, items);
		items.add(AllItems.VERTICAL_GEARBOX.asStack());
	}

	@SuppressWarnings("deprecation")
	@Override
	public List<ItemStack> getDrops(BlockState state, Builder builder) {
		if (state.getValue(AXIS).isVertical())
			return super.getDrops(state, builder);
		return Arrays.asList(new ItemStack(AllItems.VERTICAL_GEARBOX.get()));
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos,
			Player player) {
		if (state.getValue(AXIS).isVertical())
			return super.getCloneItemStack(state, target, world, pos, player);
		return new ItemStack(AllItems.VERTICAL_GEARBOX.get());
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(AXIS, Axis.Y);
	}

	// IRotate:

	@Override
	public boolean hasShaftTowards(BlockState state, Direction face) {
		return face.getAxis() != state.getValue(AXIS);
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.getValue(AXIS);
	}

	private static float getModifier(Direction from, Direction to) {
		if (from == to) return 1;
		Axis fromAxis = from.getAxis(), toAxis = to.getAxis();
		if (fromAxis == toAxis) return -1;
		return AllConnections.perpendicularRatios(to.getNormal().subtract(from.getNormal()), fromAxis, toAxis);
	}

	@Override
	public ConnectionsBuilder buildInitialConnections(ConnectionsBuilder builder, BlockState state) {
		Axis axis = state.getValue(AXIS);
		Direction start = DirectionHelper.getPositivePerpendicular(axis);
		for (Direction cur : Iterate.directionsPerpendicularTo(axis)) {
			builder = builder.withHalfShaft(cur, getModifier(start, cur));
		}
		return builder;
	}

	@Override
	public Class<GearboxTileEntity> getTileEntityClass() {
		return GearboxTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends GearboxTileEntity> getTileEntityType() {
		return AllTileEntities.GEARBOX.get();
	}
}
