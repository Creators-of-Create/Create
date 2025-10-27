package com.simibubi.create.foundation.blockEntity.behaviour.inventory;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.item.ItemHelper.ExtractionCountMode;

import net.createmod.catnip.math.BlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.BlockCapability;

public abstract class CapManipulationBehaviourBase<T, S extends CapManipulationBehaviourBase<?, ?>>
	extends BlockEntityBehaviour {

	protected InterfaceProvider target;
	protected T targetCapability;
	protected Predicate<BlockEntity> filter;
	protected boolean simulateNext;
	protected boolean bypassSided;
	private boolean findNewNextTick;

	public CapManipulationBehaviourBase(SmartBlockEntity be, InterfaceProvider target) {
		super(be);
		setLazyTickRate(5);
		this.target = target;
		targetCapability = null;
		simulateNext = false;
		bypassSided = false;
		filter = Predicates.alwaysTrue();
	}

	protected abstract BlockCapability<T, Direction> capability();

	@Override
	public void initialize() {
		super.initialize();
		findNewNextTick = true;
	}

	@Override
	public void onNeighborChanged(BlockPos neighborPos) {
		if (this.getTarget().getConnectedPos().equals(neighborPos))
			onHandlerInvalidated();
	}

	@SuppressWarnings("unchecked")
	public S bypassSidedness() {
		bypassSided = true;
		return (S) this;
	}

	/**
	 * Only simulate the upcoming operation
	 */
	@SuppressWarnings("unchecked")
	public S simulate() {
		simulateNext = true;
		return (S) this;
	}

	@SuppressWarnings("unchecked")
	public S withFilter(Predicate<BlockEntity> filter) {
		this.filter = filter;
		return (S) this;
	}

	public boolean hasInventory() {
		return targetCapability != null;
	}

	@Nullable
	public T getInventory() {
		return targetCapability;
	}

	/**
	 * Get the target of this is behavior, which is the face of the owner BlockEntity that acts as the interface.
	 * To get the BlockFace to use for capability lookup, call getOpposite on the result.
	 */
	public BlockFace getTarget() {
		return this.target.getTarget(this.getWorld(), this.blockEntity.getBlockPos(), this.blockEntity.getBlockState());
	}

	protected boolean onHandlerInvalidated() {
		if (targetCapability == null)
			return false;
		findNewNextTick = true;
		targetCapability = null;

		return true;
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		if (targetCapability == null)
			findNewCapability();
	}

	@Override
	public void tick() {
		super.tick();
		if (findNewNextTick || getWorld().getGameTime() % 64 == 0) {
			findNewNextTick = false;
			findNewCapability();
		}
	}

	public int getAmountFromFilter() {
		int amount = -1;
		FilteringBehaviour filter = blockEntity.getBehaviour(FilteringBehaviour.TYPE);
		if (filter != null && !filter.anyAmount())
			amount = filter.getAmount();
		return amount;
	}

	public ExtractionCountMode getModeFromFilter() {
		ExtractionCountMode mode = ExtractionCountMode.UPTO;
		FilteringBehaviour filter = blockEntity.getBehaviour(FilteringBehaviour.TYPE);
		if (filter != null && !filter.upTo)
			mode = ExtractionCountMode.EXACTLY;
		return mode;
	}

	public void findNewCapability() {
		Level world = getWorld();
		BlockFace targetBlockFace = this.getTarget().getOpposite();
		BlockPos pos = targetBlockFace.getPos();

		targetCapability = null;

		if (!world.isLoaded(pos))
			return;
		BlockEntity invBE = world.getBlockEntity(pos);
		if (!filter.test(invBE))
			return;
		BlockCapability<T, Direction> capability = capability();
		targetCapability = world.getCapability(capability, pos, bypassSided ? null : targetBlockFace.getFace());
	}

	@FunctionalInterface
	public interface InterfaceProvider {

		public static InterfaceProvider towardBlockFacing() {
			return (w, p, s) -> new BlockFace(p,
				s.hasProperty(BlockStateProperties.FACING) ? s.getValue(BlockStateProperties.FACING)
					: s.getValue(BlockStateProperties.HORIZONTAL_FACING));
		}

		public static InterfaceProvider oppositeOfBlockFacing() {
			return (w, p, s) -> new BlockFace(p,
				(s.hasProperty(BlockStateProperties.FACING) ? s.getValue(BlockStateProperties.FACING)
					: s.getValue(BlockStateProperties.HORIZONTAL_FACING)).getOpposite());
		}

		public BlockFace getTarget(Level world, BlockPos pos, BlockState blockState);
	}

}
