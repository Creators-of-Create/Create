package com.simibubi.create.foundation.blockEntity.behaviour.inventory;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.item.ItemHelper.ExtractionCountMode;

import net.createmod.catnip.math.BlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public abstract class CapManipulationBehaviourBase<T, S extends CapManipulationBehaviourBase<?, ?>>
	extends BlockEntityBehaviour {

	protected InterfaceProvider target;
	protected LazyOptional<T> targetCapability;
	protected Predicate<BlockEntity> filter;
	protected boolean simulateNext;
	protected boolean bypassSided;
	private boolean findNewNextTick;

	public CapManipulationBehaviourBase(SmartBlockEntity be, InterfaceProvider target) {
		super(be);
		setLazyTickRate(5);
		this.target = target;
		targetCapability = LazyOptional.empty();
		simulateNext = false;
		bypassSided = false;
		filter = Predicates.alwaysTrue();
	}

	protected abstract Capability<T> capability();

	@Override
	public void initialize() {
		super.initialize();
		findNewNextTick = true;
	}

	@Override
	public void onNeighborChanged(BlockPos neighborPos) {
		if (this.getTarget().getConnectedPos().equals(neighborPos))
			onHandlerInvalidated(targetCapability);
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
		return targetCapability.isPresent();
	}

	@Nullable
	public T getInventory() {
		return targetCapability.orElse(null);
	}

	/**
	 * Get the target of this is behavior, which is the face of the owner BlockEntity that acts as the interface.
	 * To get the BlockFace to use for capability lookup, call getOpposite on the result.
	 */
	public BlockFace getTarget() {
		return this.target.getTarget(this.getWorld(), this.blockEntity.getBlockPos(), this.blockEntity.getBlockState());
	}

	protected void onHandlerInvalidated(LazyOptional<T> handler) {
		findNewNextTick = true;
		targetCapability = LazyOptional.empty();
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		if (!targetCapability.isPresent())
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

		targetCapability = LazyOptional.empty();

		if (!world.isLoaded(pos))
			return;
		BlockEntity invBE = world.getBlockEntity(pos);
		if (invBE == null || !filter.test(invBE))
			return;
		Capability<T> capability = capability();
		targetCapability =
			bypassSided ? invBE.getCapability(capability) : invBE.getCapability(capability, targetBlockFace.getFace());
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
