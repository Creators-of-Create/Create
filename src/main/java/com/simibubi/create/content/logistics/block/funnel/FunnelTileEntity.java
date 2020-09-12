package com.simibubi.create.content.logistics.block.funnel;

import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.block.chute.ChuteTileEntity;
import com.simibubi.create.content.logistics.block.funnel.BeltFunnelBlock.Shape;
import com.simibubi.create.foundation.gui.widgets.InterpolatedChasingValue;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.ExtractingBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InsertingBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InventoryManagementBehaviour.Attachments;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class FunnelTileEntity extends SmartTileEntity {

	private FilteringBehaviour filtering;
	private InsertingBehaviour inserting;
	private ExtractingBehaviour extracting;
	private DirectBeltInputBehaviour beltInputBehaviour;

	int sendFlap;
	InterpolatedChasingValue flap;

	static enum Mode {
		INVALID, PAUSED, COLLECT, BELT, CHUTE_SIDE, CHUTE_END
	}

	public FunnelTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		flap = new InterpolatedChasingValue().start(.25f)
			.target(0)
			.withSpeed(.05f);
	}

	public Mode determineCurrentMode() {
		BlockState state = getBlockState();
		if (!FunnelBlock.isFunnel(state))
			return Mode.INVALID;
		if (state.has(BlockStateProperties.POWERED) && state.get(BlockStateProperties.POWERED))
			return Mode.PAUSED;
		if (state.getBlock() instanceof BeltFunnelBlock)
			return Mode.BELT;
		if (state.getBlock() instanceof ChuteFunnelBlock)
			return Mode.CHUTE_SIDE;

		Direction facing = FunnelBlock.getFunnelFacing(state);
		BlockState input = world.getBlockState(pos.offset(facing));

		if (AllBlocks.CHUTE.has(input))
			return Mode.CHUTE_END;
		return Mode.COLLECT;
	}

	@Override
	public void tick() {
		super.tick();
		Mode mode = determineCurrentMode();
		if (mode == Mode.BELT)
			tickAsBeltFunnel();
		if (world.isRemote)
			return;
		if (mode == Mode.CHUTE_SIDE)
			tickAsHorizontalChuteFunnel();
		if (mode == Mode.CHUTE_END)
			tickAsVerticalChuteFunnel();
	}

	public void tickAsHorizontalChuteFunnel() {
		if (!getBlockState().get(ChuteFunnelBlock.PUSHING))
			return;
		BlockPos chutePos = pos.offset(FunnelBlock.getFunnelFacing(getBlockState()));
		TileEntity te = world.getTileEntity(chutePos);
		if (!(te instanceof ChuteTileEntity))
			return;
		ChuteTileEntity chute = (ChuteTileEntity) te;
		extracting.setCallback(stack -> chute.setItem(stack, .5f));
		extracting.withAdditionalFilter(stack -> chute.getItem()
			.isEmpty());
		extracting.extract();
	}

	public void tickAsVerticalChuteFunnel() {
		Direction funnelFacing = FunnelBlock.getFunnelFacing(getBlockState());
		BlockPos chutePos = pos.offset(funnelFacing);
		TileEntity te = world.getTileEntity(chutePos);
		if (!(te instanceof ChuteTileEntity))
			return;
		ChuteTileEntity chute = (ChuteTileEntity) te;
		if (chute.getItemMotion() > 0 != (funnelFacing == Direction.UP))
			return;
		extracting.setCallback(stack -> chute.setItem(stack));
		extracting.withAdditionalFilter(stack -> chute.getItem()
			.isEmpty());
		extracting.extract();
	}

	public void tickAsBeltFunnel() {
		BlockState blockState = getBlockState();
		Direction facing = blockState.get(BeltFunnelBlock.HORIZONTAL_FACING);
		flap.tick();
		if (world.isRemote)
			return;

		if (!blockState.get(BeltFunnelBlock.PUSHING)) {
			// Belts handle insertion from their side
			if (AllBlocks.BELT.has(world.getBlockState(pos.down())))
				return;
			TransportedItemStackHandlerBehaviour handler =
				TileEntityBehaviour.get(world, pos.down(), TransportedItemStackHandlerBehaviour.TYPE);
			if (handler == null)
				return;
			handler.handleCenteredProcessingOnAllItems(1 / 32f, this::collectFromHandler);
			return;
		}

		DirectBeltInputBehaviour inputBehaviour =
			TileEntityBehaviour.get(world, pos.down(), DirectBeltInputBehaviour.TYPE);
		if (inputBehaviour == null)
			return;
		if (!inputBehaviour.canInsertFromSide(facing))
			return;

		extracting.setCallback(stack -> {
			flap(false);
			inputBehaviour.handleInsertion(stack, facing, false);
		});

		extracting.withAdditionalFilter(stack -> inputBehaviour.handleInsertion(stack, facing, true)
			.isEmpty());
		extracting.extract();
	}

	private TransportedResult collectFromHandler(TransportedItemStack stack) {
		TransportedResult ignore = TransportedResult.doNothing();
		ItemStack toInsert = stack.stack.copy();
		if (!filtering.test(toInsert))
			return ignore;
		ItemStack remainder = inserting.insert(toInsert, false);
		if (remainder.equals(stack.stack, false))
			return ignore;
		
		flap(true);
		
		if (remainder.isEmpty())
			return TransportedResult.removeItem();
		TransportedItemStack changed = stack.copy();
		changed.stack = remainder;
		return TransportedResult.convertTo(changed);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		Supplier<List<Pair<BlockPos, Direction>>> direction =
			Attachments.toward(() -> FunnelBlock.getFunnelFacing(getBlockState())
				.getOpposite());

		inserting = new InsertingBehaviour(this, direction);
		extracting = new ExtractingBehaviour(this, direction);
		behaviours.add(inserting);
		behaviours.add(extracting);

		filtering = new FilteringBehaviour(this, new FunnelFilterSlotPositioning()).showCountWhen(() -> {
			BlockState blockState = getBlockState();
			return blockState.getBlock() instanceof HorizontalInteractionFunnelBlock
				&& blockState.get(HorizontalInteractionFunnelBlock.PUSHING) || determineCurrentMode() == Mode.CHUTE_END;
		});
		filtering.onlyActiveWhen(this::supportsFiltering);
		behaviours.add(filtering);

		beltInputBehaviour = new DirectBeltInputBehaviour(this).onlyInsertWhen(this::supportsDirectBeltInput)
			.setInsertionHandler(this::handleDirectBeltInput);
		behaviours.add(beltInputBehaviour);
	}

	private boolean supportsDirectBeltInput(Direction side) {
		BlockState blockState = getBlockState();
		return blockState != null && blockState.getBlock() instanceof FunnelBlock
			&& blockState.get(FunnelBlock.FACING) == Direction.UP;
	}

	private boolean supportsFiltering() {
		BlockState blockState = getBlockState();
		return blockState != null && blockState.has(BlockStateProperties.POWERED);
	}

	private ItemStack handleDirectBeltInput(TransportedItemStack stack, Direction side, boolean simulate) {
		ItemStack inserted = stack.stack;
		if (!filtering.test(inserted))
			return inserted;
		if (determineCurrentMode() == Mode.PAUSED)
			return inserted;
		return inserting.insert(inserted, simulate);
	}

	public void flap(boolean inward) {
		sendFlap = inward ? 1 : -1;
		sendData();
	}

	public boolean hasFlap() {
		return getBlockState().getBlock() instanceof BeltFunnelBlock
			&& getBlockState().get(BeltFunnelBlock.SHAPE) == Shape.RETRACTED;
	}
	
	@Override
	protected void write(CompoundNBT compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		if (clientPacket && sendFlap != 0) {
			compound.putInt("Flap", sendFlap);
			sendFlap = 0;
		}
	}
	
	@Override
	protected void read(CompoundNBT compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		if (clientPacket && compound.contains("Flap")) {
			int direction = compound.getInt("Flap");
			flap.set(direction);
		}
	}

	@Override
	public double getMaxRenderDistanceSquared() {
		return hasFlap() ? super.getMaxRenderDistanceSquared() : 64;
	}

}
