package com.simibubi.create.content.logistics.block.chute;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.fan.EncasedFanBlock;
import com.simibubi.create.content.contraptions.components.fan.EncasedFanTileEntity;
import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.logistics.block.chute.ChuteBlock.Shape;
import com.simibubi.create.content.logistics.block.funnel.BrassFunnelBlock;
import com.simibubi.create.content.logistics.block.funnel.ChuteFunnelBlock;
import com.simibubi.create.content.logistics.block.funnel.FunnelBlock;
import com.simibubi.create.foundation.gui.widgets.InterpolatedValue;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ChuteTileEntity extends SmartTileEntity implements IHaveGoggleInformation {

	float pull;
	float push;

	ItemStack item;
	InterpolatedValue itemPosition;
	ChuteItemHandler itemHandler;
	LazyOptional<IItemHandler> lazyHandler;
	boolean canPickUpItems;

	public ChuteTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		item = ItemStack.EMPTY;
		itemPosition = new InterpolatedValue();
		itemHandler = new ChuteItemHandler(this);
		lazyHandler = LazyOptional.of(() -> itemHandler);
		canPickUpItems = false;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(new DirectBeltInputBehaviour(this).onlyInsertWhen((d) -> canDirectlyInsertCached()));
	}

	// Cached per-tick, useful when a lot of items are waiting on top of it
	public boolean canDirectlyInsertCached() {
		return canPickUpItems;
	}

	private boolean canDirectlyInsert() {
		BlockState blockState = getBlockState();
		BlockState blockStateAbove = world.getBlockState(pos.up());
		if (!AllBlocks.CHUTE.has(blockState))
			return false;
		if (AllBlocks.CHUTE.has(blockStateAbove) && blockStateAbove.get(ChuteBlock.FACING) == Direction.DOWN)
			return false;
		if (getItemMotion() > 0 && getInputChutes().isEmpty())
			return false;
		return blockState.get(ChuteBlock.FACING) == Direction.DOWN || blockState.get(ChuteBlock.SHAPE) == Shape.START;
	}

	@Override
	public void initialize() {
		super.initialize();
	}

	@Override
	public void tick() {
		super.tick();
		canPickUpItems = canDirectlyInsert();
		if (item.isEmpty())
			return;
		float itemMotion = getItemMotion();
		float nextOffset = itemPosition.value + itemMotion;

		if (itemMotion < 0) {
			if (nextOffset < .5f) {
				if (handleSideOutput())
					return;
				boolean success = handleDownwardOutput(true);
				if (!success)
					nextOffset = .5f;
				else if (nextOffset < 0) {
					handleDownwardOutput(world.isRemote);
					return;
				}
			}
		}

		if (itemMotion > 0) {
			if (nextOffset > .5f) {
				if (handleSideOutput())
					return;
				boolean success = handleUpwardOutput(true);
				if (!success)
					nextOffset = .5f;
				else if (nextOffset > 1) {
					handleUpwardOutput(world.isRemote);
					return;
				}
			}
		}

		itemPosition.set(nextOffset);
	}

	private boolean handleDownwardOutput(boolean simulate) {
		BlockState blockState = getBlockState();
		ChuteTileEntity targetChute = getTargetChute(blockState);

		if (targetChute != null) {
			boolean canInsert = targetChute.item.isEmpty();
			if (!simulate && canInsert) {
				targetChute.setItem(item, 1);
				setItem(ItemStack.EMPTY);
			}
			return canInsert;
		}

		// Diagonal chutes can only insert into other chutes
		if (blockState.get(ChuteBlock.FACING)
			.getAxis()
			.isHorizontal())
			return false;

		BlockState stateBelow = world.getBlockState(pos.down());
		if (stateBelow.getBlock() instanceof FunnelBlock) {
			if (stateBelow.has(BrassFunnelBlock.POWERED) && stateBelow.get(BrassFunnelBlock.POWERED))
				return false;
			if (stateBelow.get(BrassFunnelBlock.FACING) != Direction.UP)
				return false;
			ItemStack remainder = FunnelBlock.tryInsert(world, pos.down(), item, simulate);
			if (!simulate)
				setItem(remainder);
			return remainder.isEmpty();
		}

		DirectBeltInputBehaviour directInput =
			TileEntityBehaviour.get(world, pos.down(), DirectBeltInputBehaviour.TYPE);
		if (directInput != null) {
			if (!directInput.canInsertFromSide(Direction.UP))
				return false;
			ItemStack remainder = directInput.handleInsertion(item, Direction.UP, simulate);
			if (!simulate)
				setItem(remainder);
			return remainder.isEmpty();
		}

		if (Block.hasSolidSideOnTop(world, pos.down()))
			return false;

		if (!simulate) {
			Vector3d dropVec = VecHelper.getCenterOf(pos)
				.add(0, -12 / 16f, 0);
			ItemEntity dropped = new ItemEntity(world, dropVec.x, dropVec.y, dropVec.z, item.copy());
			dropped.setDefaultPickupDelay();
			dropped.setMotion(0, -.25f, 0);
			world.addEntity(dropped);
			setItem(ItemStack.EMPTY);
		}

		return true;
	}

	private boolean handleUpwardOutput(boolean simulate) {
		BlockState stateAbove = world.getBlockState(pos.up());
		if (stateAbove.getBlock() instanceof FunnelBlock) {
			boolean powered = stateAbove.has(BrassFunnelBlock.POWERED) && stateAbove.get(BrassFunnelBlock.POWERED);
			if (!powered && stateAbove.get(BrassFunnelBlock.FACING) == Direction.DOWN) {
				ItemStack remainder = FunnelBlock.tryInsert(world, pos.up(), item, simulate);
				if (remainder.isEmpty()) {
					if (!simulate)
						setItem(remainder);
					return true;
				}
			}
		}

		ChuteTileEntity bestOutput = null;
		List<ChuteTileEntity> inputChutes = getInputChutes();
		for (ChuteTileEntity targetChute : inputChutes) {
			if (!targetChute.item.isEmpty())
				continue;
			float itemMotion = targetChute.getItemMotion();
			if (itemMotion < 0)
				continue;
			if (bestOutput == null || bestOutput.getItemMotion() < itemMotion) {
				bestOutput = targetChute;
			}
		}

		if (bestOutput != null) {
			if (!simulate) {
				bestOutput.setItem(item, 0);
				setItem(ItemStack.EMPTY);
			}
			return true;
		}

		if (Block.hasSolidSide(stateAbove, world, pos.up(), Direction.DOWN))
			return false;
		if (!inputChutes.isEmpty())
			return false;

		if (!simulate) {
			Vector3d dropVec = VecHelper.getCenterOf(pos)
				.add(0, 8 / 16f, 0);
			ItemEntity dropped = new ItemEntity(world, dropVec.x, dropVec.y, dropVec.z, item.copy());
			dropped.setDefaultPickupDelay();
			dropped.setMotion(0, getItemMotion() * 2, 0);
			world.addEntity(dropped);
			setItem(ItemStack.EMPTY);
		}
		return true;
	}

	private boolean handleSideOutput() {
		if (world.isRemote)
			return false;
		for (Direction direction : Iterate.horizontalDirections) {
			BlockPos funnelPos = pos.offset(direction);
			BlockState funnelState = world.getBlockState(funnelPos);
			if (AllBlocks.BRASS_CHUTE_FUNNEL.has(funnelState)) {
				if (funnelState.get(ChuteFunnelBlock.POWERED))
					continue;
				if (funnelState.get(ChuteFunnelBlock.HORIZONTAL_FACING) != direction.getOpposite())
					continue;
				if (funnelState.get(ChuteFunnelBlock.PUSHING))
					continue;
				ItemStack remainder = FunnelBlock.tryInsert(world, funnelPos, item.copy(), world.isRemote);
				if (remainder.getCount() != item.getCount() && !world.isRemote)
					setItem(remainder);
			}
		}
		return item.isEmpty();
	}

	public void setItem(ItemStack stack) {
		setItem(stack, getItemMotion() < 0 ? 1 : 0);
	}

	public void setItem(ItemStack stack, float insertionPos) {
		item = stack;
		itemPosition.lastValue = itemPosition.value = insertionPos;
		markDirty();
		sendData();
	}

	@Override
	public void remove() {
		super.remove();
		if (lazyHandler != null)
			lazyHandler.invalidate();
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.put("Item", item.serializeNBT());
		compound.putFloat("ItemPosition", itemPosition.value);
		compound.putFloat("Pull", pull);
		compound.putFloat("Push", push);
		return super.write(compound);
	}

	@Override
	public void read(CompoundNBT compound) {
		ItemStack previousItem = item;
		item = ItemStack.read(compound.getCompound("Item"));
		itemPosition.lastValue = itemPosition.value = compound.getFloat("ItemPosition");
		pull = compound.getFloat("Pull");
		push = compound.getFloat("Push");
		super.read(compound);

		if (hasWorld() && world.isRemote && !previousItem.equals(item, false) && !item.isEmpty()) {
			if (world.rand.nextInt(3) != 0)
				return;
			Vector3d p = VecHelper.getCenterOf(pos);
			p = VecHelper.offsetRandomly(p, world.rand, .5f);
			Vector3d m = Vector3d.ZERO;
			world.addParticle(new ItemParticleData(ParticleTypes.ITEM, item), p.x, p.y, p.z, m.x, m.y, m.z);
		}
	}

	public float getItemMotion() {
		// Chutes per second
		final float fanSpeedModifier = 1 / 64f;
		final float maxUpwardItemSpeed = 20f;
		final float gravity = 4f;

		float upwardMotion = (push + pull) * fanSpeedModifier;
		return (upwardMotion == 0 ? -gravity : MathHelper.clamp(upwardMotion, 0, maxUpwardItemSpeed)) / 20f;
	}

	public void onRemoved(BlockState chuteState) {
		ChuteTileEntity targetChute = getTargetChute(chuteState);
		List<ChuteTileEntity> inputChutes = getInputChutes();
		if (!item.isEmpty())
			InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), item);
		super.remove();
		if (targetChute != null) {
			targetChute.updatePull();
			targetChute.propagatePush();
		}
		inputChutes.forEach(c -> c.updatePush(inputChutes.size()));
	}

	public void onAdded() {
		updateContainingBlockInfo();
		updatePull();
		ChuteTileEntity targetChute = getTargetChute(getBlockState());
		if (targetChute != null)
			targetChute.propagatePush();
		else
			updatePush(1);
	}

	public void updatePull() {
		float totalPull = calculatePull();
		if (pull == totalPull)
			return;
		pull = totalPull;
		sendData();
		ChuteTileEntity targetChute = getTargetChute(getBlockState());
		if (targetChute != null)
			targetChute.updatePull();
	}

	public void updatePush(int branchCount) {
		float totalPush = calculatePush(branchCount);
		if (push == totalPush)
			return;
		push = totalPush;
		sendData();
		propagatePush();
	}

	public void propagatePush() {
		List<ChuteTileEntity> inputs = getInputChutes();
		inputs.forEach(c -> c.updatePush(inputs.size()));
	}

	protected float calculatePull() {
		BlockState blockStateAbove = world.getBlockState(pos.up());
		if (AllBlocks.ENCASED_FAN.has(blockStateAbove)
			&& blockStateAbove.get(EncasedFanBlock.FACING) == Direction.DOWN) {
			TileEntity te = world.getTileEntity(pos.up());
			if (te instanceof EncasedFanTileEntity && !te.isRemoved()) {
				EncasedFanTileEntity fan = (EncasedFanTileEntity) te;
				return Math.abs(fan.getSpeed());
			}
		}

		float totalPull = 0;
		for (Direction d : Iterate.directions) {
			ChuteTileEntity inputChute = getInputChute(d);
			if (inputChute == null)
				continue;
			totalPull += inputChute.pull;
		}
		return totalPull;
	}

	protected float calculatePush(int branchCount) {
		BlockState blockStateBelow = world.getBlockState(pos.down());
		if (AllBlocks.ENCASED_FAN.has(blockStateBelow) && blockStateBelow.get(EncasedFanBlock.FACING) == Direction.UP) {
			TileEntity te = world.getTileEntity(pos.down());
			if (te instanceof EncasedFanTileEntity && !te.isRemoved()) {
				EncasedFanTileEntity fan = (EncasedFanTileEntity) te;
				return Math.abs(fan.getSpeed());
			}
		}

		ChuteTileEntity targetChute = getTargetChute(getBlockState());
		if (targetChute == null)
			return 0;
		return targetChute.push / branchCount;
	}

	@Nullable
	private ChuteTileEntity getTargetChute(BlockState state) {
		Direction targetDirection = state.get(ChuteBlock.FACING);
		BlockPos chutePos = pos.down();
		if (targetDirection.getAxis()
			.isHorizontal())
			chutePos = chutePos.offset(targetDirection.getOpposite());
		BlockState chuteState = world.getBlockState(chutePos);
		if (!AllBlocks.CHUTE.has(chuteState))
			return null;
		TileEntity te = world.getTileEntity(chutePos);
		if (te instanceof ChuteTileEntity)
			return (ChuteTileEntity) te;
		return null;
	}

	private List<ChuteTileEntity> getInputChutes() {
		List<ChuteTileEntity> inputs = new LinkedList<>();
		for (Direction d : Iterate.directions) {
			ChuteTileEntity inputChute = getInputChute(d);
			if (inputChute == null)
				continue;
			inputs.add(inputChute);
		}
		return inputs;
	}

	@Nullable
	private ChuteTileEntity getInputChute(Direction direction) {
		if (direction == Direction.DOWN)
			return null;
		direction = direction.getOpposite();
		BlockPos chutePos = pos.up();
		if (direction.getAxis()
			.isHorizontal())
			chutePos = chutePos.offset(direction);
		BlockState chuteState = world.getBlockState(chutePos);
		if (!AllBlocks.CHUTE.has(chuteState) || chuteState.get(ChuteBlock.FACING) != direction)
			return null;
		TileEntity te = world.getTileEntity(chutePos);
		if (te instanceof ChuteTileEntity && !te.isRemoved())
			return (ChuteTileEntity) te;
		return null;
	}

	@Override
	public boolean addToGoggleTooltip(List<String> tooltip, boolean isPlayerSneaking) {
		tooltip.add(spacing + TextFormatting.GOLD + "Pull: " + TextFormatting.WHITE + pull);
		tooltip.add(spacing + TextFormatting.GOLD + "Push: " + TextFormatting.WHITE + push);
		tooltip.add(TextFormatting.YELLOW + "-> Item Motion: " + TextFormatting.WHITE + getItemMotion());
		return true;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return lazyHandler.cast();
		return super.getCapability(cap, side);
	}

	public ItemStack getItem() {
		return item;
	}

}
