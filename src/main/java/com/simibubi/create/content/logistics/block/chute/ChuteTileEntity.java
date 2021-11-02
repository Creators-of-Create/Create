package com.simibubi.create.content.logistics.block.chute;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.fan.AirCurrent;
import com.simibubi.create.content.contraptions.components.fan.EncasedFanBlock;
import com.simibubi.create.content.contraptions.components.fan.EncasedFanTileEntity;
import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.contraptions.particle.AirParticleData;
import com.simibubi.create.content.logistics.block.funnel.FunnelBlock;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.gui.widgets.InterpolatedValue;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.item.ItemHelper.ExtractionCountMode;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;

import mcp.MethodsReturnNonnullByDefault;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
/*
 * Commented Code: Chutes create air streams and act similarly to encased fans
 * (Unfinished)
 */
public class ChuteTileEntity extends SmartTileEntity implements IHaveGoggleInformation { // , IAirCurrentSource {

	//	public AirCurrent airCurrent;

	float pull;
	float push;

	ItemStack item;
	InterpolatedValue itemPosition;
	ChuteItemHandler itemHandler;
	LazyOptional<IItemHandler> lazyHandler;
	boolean canPickUpItems;

	float bottomPullDistance;
	float beltBelowOffset;
	TransportedItemStackHandlerBehaviour beltBelow;
	boolean updateAirFlow;
	int airCurrentUpdateCooldown;
	int entitySearchCooldown;

	LazyOptional<IItemHandler> capAbove;
	LazyOptional<IItemHandler> capBelow;

	public ChuteTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		item = ItemStack.EMPTY;
		itemPosition = new InterpolatedValue();
		itemHandler = new ChuteItemHandler(this);
		lazyHandler = LazyOptional.of(() -> itemHandler);
		canPickUpItems = false;
		capAbove = LazyOptional.empty();
		capBelow = LazyOptional.empty();
		bottomPullDistance = 0;
		//		airCurrent = new AirCurrent(this);
		updateAirFlow = true;
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
		BlockState blockStateAbove = level.getBlockState(worldPosition.above());
		if (!AbstractChuteBlock.isChute(blockState))
			return false;
		if (AbstractChuteBlock.getChuteFacing(blockStateAbove) == Direction.DOWN)
			return false;
		if (getItemMotion() > 0 && getInputChutes().isEmpty())
			return false;
		return AbstractChuteBlock.isOpenChute(blockState);
	}

	@Override
	public void initialize() {
		super.initialize();
		onAdded();
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(worldPosition).expandTowards(0, -3, 0);
	}

	@Override
	public void tick() {
		super.tick();

		if (!level.isClientSide)
			canPickUpItems = canDirectlyInsert();

		boolean clientSide = level != null && level.isClientSide && !isVirtual();
		float itemMotion = getItemMotion();
		if (itemMotion != 0 && level != null && level.isClientSide)
			spawnParticles(itemMotion);
		tickAirStreams(itemMotion);

		if (item.isEmpty() && !clientSide) {
			if (itemMotion < 0)
				handleInputFromAbove();
			if (itemMotion > 0)
				handleInputFromBelow();
			return;
		}

		float nextOffset = itemPosition.value + itemMotion;
		
		if (itemMotion < 0) {
			if (nextOffset < .5f) {
				if (!handleDownwardOutput(true))
					nextOffset = .5f;
				else if (nextOffset < 0) {
					handleDownwardOutput(clientSide);
					nextOffset = itemPosition.value;
				}
			}
		} else if (itemMotion > 0) {
			if (nextOffset > .5f) {
				if (!handleUpwardOutput(true))
					nextOffset = .5f;
				else if (nextOffset > 1) {
					handleUpwardOutput(clientSide);
					nextOffset = itemPosition.value;
				}
			}
		}

		itemPosition.set(nextOffset);
	}

	private void updateAirFlow(float itemSpeed) {
		updateAirFlow = false;
		//		airCurrent.rebuild();
		if (itemSpeed > 0 && level != null && !level.isClientSide) {
			float speed = pull - push;
			beltBelow = null;

			float maxPullDistance;
			if (speed >= 128)
				maxPullDistance = 3;
			else if (speed >= 64)
				maxPullDistance = 2;
			else if (speed >= 32)
				maxPullDistance = 1;
			else
				maxPullDistance = MathHelper.lerp(speed / 32, 0, 1);

			if (AbstractChuteBlock.isChute(level.getBlockState(worldPosition.below())))
				maxPullDistance = 0;
			float flowLimit = maxPullDistance;
			if (flowLimit > 0)
				flowLimit = AirCurrent.getFlowLimit(level, worldPosition, maxPullDistance, Direction.DOWN);

			for (int i = 1; i <= flowLimit + 1; i++) {
				TransportedItemStackHandlerBehaviour behaviour =
					TileEntityBehaviour.get(level, worldPosition.below(i), TransportedItemStackHandlerBehaviour.TYPE);
				if (behaviour == null)
					continue;
				beltBelow = behaviour;
				beltBelowOffset = i - 1;
				break;
			}
			this.bottomPullDistance = Math.max(0, flowLimit);
		}
		sendData();
	}

	private void findEntities(float itemSpeed) {
		//		if (getSpeed() != 0)
		//			airCurrent.findEntities();
		if (bottomPullDistance <= 0 && !getItem().isEmpty() || itemSpeed <= 0 || level == null || level.isClientSide)
			return;
		if (!canCollectItemsFromBelow())
			return;
		Vector3d center = VecHelper.getCenterOf(worldPosition);
		AxisAlignedBB searchArea =
			new AxisAlignedBB(center.add(0, -bottomPullDistance - 0.5, 0), center.add(0, -0.5, 0)).inflate(.45f);
		for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, searchArea)) {
			if (!itemEntity.isAlive())
				continue;
			ItemStack entityItem = itemEntity.getItem();
			if (!canAcceptItem(entityItem))
				continue;
			setItem(entityItem.copy(), (float) (itemEntity.getBoundingBox()
				.getCenter().y - worldPosition.getY()));
			itemEntity.remove();
			AllTriggers.triggerForNearbyPlayers(AllTriggers.UPWARD_CHUTE, level, worldPosition, 5);
			break;
		}
	}

	private void extractFromBelt(float itemSpeed) {
		if (itemSpeed <= 0 || level == null || level.isClientSide)
			return;
		if (getItem().isEmpty() && beltBelow != null) {
			beltBelow.handleCenteredProcessingOnAllItems(.5f, ts -> {
				if (canAcceptItem(ts.stack)) {
					setItem(ts.stack.copy(), -beltBelowOffset);
					return TransportedResult.removeItem();
				}
				return TransportedResult.doNothing();
			});
		}
	}

	private void tickAirStreams(float itemSpeed) {
		if (!level.isClientSide && airCurrentUpdateCooldown-- <= 0) {
			airCurrentUpdateCooldown = AllConfigs.SERVER.kinetics.fanBlockCheckRate.get();
			updateAirFlow = true;
		}

		if (updateAirFlow) {
			updateAirFlow(itemSpeed);
		}

		if (entitySearchCooldown-- <= 0 && item.isEmpty()) {
			entitySearchCooldown = 5;
			findEntities(itemSpeed);
		}

		extractFromBelt(itemSpeed);
		//		if (getSpeed() != 0)
		//			airCurrent.tick();
	}

	public void blockBelowChanged() {
		updateAirFlow = true;
		capBelow = LazyOptional.empty();
	}

	private void spawnParticles(float itemMotion) {
		// todo: reduce the amount of particles
		if (level == null)
			return;
		BlockState blockState = getBlockState();
		boolean up = itemMotion > 0;
		float absMotion = up ? itemMotion : -itemMotion;
		if (blockState == null || !AbstractChuteBlock.isChute(blockState))
			return;
		if (push == 0 && pull == 0)
			return;

		if (up && AbstractChuteBlock.isOpenChute(blockState) && BlockHelper.noCollisionInSpace(level, worldPosition.above()))
			spawnAirFlow(1, 2, absMotion, .5f);

		if (AbstractChuteBlock.getChuteFacing(blockState) != Direction.DOWN)
			return;

		if (AbstractChuteBlock.isTransparentChute(blockState))
			spawnAirFlow(up ? 0 : 1, up ? 1 : 0, absMotion, 1);

		if (!up && BlockHelper.noCollisionInSpace(level, worldPosition.below()))
			spawnAirFlow(0, -1, absMotion, .5f);

		if (up && canCollectItemsFromBelow() && bottomPullDistance > 0) {
			spawnAirFlow(-bottomPullDistance, 0, absMotion, 2);
			spawnAirFlow(-bottomPullDistance, 0, absMotion, 2);
		}
	}

	private void spawnAirFlow(float verticalStart, float verticalEnd, float motion, float drag) {
		if (level == null)
			return;
		AirParticleData airParticleData = new AirParticleData(drag, motion);
		Vector3d origin = Vector3d.atLowerCornerOf(worldPosition);
		float xOff = Create.RANDOM.nextFloat() * .5f + .25f;
		float zOff = Create.RANDOM.nextFloat() * .5f + .25f;
		Vector3d v = origin.add(xOff, verticalStart, zOff);
		Vector3d d = origin.add(xOff, verticalEnd, zOff)
			.subtract(v);
		if (Create.RANDOM.nextFloat() < 2 * motion)
			level.addAlwaysVisibleParticle(airParticleData, v.x, v.y, v.z, d.x, d.y, d.z);
	}

	private void handleInputFromAbove() {
		if (!capAbove.isPresent())
			capAbove = grabCapability(Direction.UP);
		handleInput(capAbove.orElse(null), 1);
	}

	private void handleInputFromBelow() {
		if (!capBelow.isPresent())
			capBelow = grabCapability(Direction.DOWN);
		handleInput(capBelow.orElse(null), 0);
	}

	private void handleInput(IItemHandler inv, float startLocation) {
		if (inv == null)
			return;
		Predicate<ItemStack> canAccept = this::canAcceptItem;
		int count = getExtractionAmount();
		ExtractionCountMode mode = getExtractionMode();
		if (mode == ExtractionCountMode.UPTO || !ItemHelper.extract(inv, canAccept, mode, count, true)
			.isEmpty()) {
			ItemStack extracted = ItemHelper.extract(inv, canAccept, mode, count, false);
			if (!extracted.isEmpty())
				setItem(extracted, startLocation);
		}
	}

	private boolean handleDownwardOutput(boolean simulate) {
		BlockState blockState = getBlockState();
		ChuteTileEntity targetChute = getTargetChute(blockState);
		Direction direction = AbstractChuteBlock.getChuteFacing(blockState);

		if (level == null)
			return false;
		if (!capBelow.isPresent())
			capBelow = grabCapability(Direction.DOWN);
		if (capBelow.isPresent()) {
			if (level.isClientSide && !isVirtual())
				return false;
			ItemStack remainder = ItemHandlerHelper.insertItemStacked(capBelow.orElse(null), item, simulate);
			ItemStack held = getItem();
			if (!simulate)
				setItem(remainder, itemPosition.get(0));
			if (remainder.getCount() != held.getCount())
				return true;
			if (direction == Direction.DOWN)
				return false;
		}

		if (targetChute != null) {
			boolean canInsert = targetChute.canAcceptItem(item);
			if (!simulate && canInsert) {
				targetChute.setItem(item, direction == Direction.DOWN ? 1 : .51f);
				setItem(ItemStack.EMPTY);
			}
			return canInsert;
		}

		// Diagonal chutes cannot drop items
		if (direction.getAxis()
			.isHorizontal())
			return false;

		if (FunnelBlock.getFunnelFacing(level.getBlockState(worldPosition.below())) == Direction.DOWN)
			return false;
		if (Block.canSupportRigidBlock(level, worldPosition.below()))
			return false;

		if (!simulate) {
			Vector3d dropVec = VecHelper.getCenterOf(worldPosition)
				.add(0, -12 / 16f, 0);
			ItemEntity dropped = new ItemEntity(level, dropVec.x, dropVec.y, dropVec.z, item.copy());
			dropped.setDefaultPickUpDelay();
			dropped.setDeltaMovement(0, -.25f, 0);
			level.addFreshEntity(dropped);
			setItem(ItemStack.EMPTY);
		}

		return true;
	}

	private boolean handleUpwardOutput(boolean simulate) {
		BlockState stateAbove = level.getBlockState(worldPosition.above());

		if (level == null)
			return false;

		if (AbstractChuteBlock.isOpenChute(getBlockState())) {
			if (!capAbove.isPresent())
				capAbove = grabCapability(Direction.UP);
			if (capAbove.isPresent()) {
				if (level.isClientSide && !isVirtual() && !ChuteBlock.isChute(stateAbove))
					return false;
				int countBefore = item.getCount();
				ItemStack remainder = ItemHandlerHelper.insertItemStacked(capAbove.orElse(null), item, simulate);
				if (!simulate)
					item = remainder;
				return countBefore != remainder.getCount();
			}
		}

		ChuteTileEntity bestOutput = null;
		List<ChuteTileEntity> inputChutes = getInputChutes();
		for (ChuteTileEntity targetChute : inputChutes) {
			if (!targetChute.canAcceptItem(item))
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

		if (FunnelBlock.getFunnelFacing(level.getBlockState(worldPosition.above())) == Direction.UP)
			return false;
		if (BlockHelper.hasBlockSolidSide(stateAbove, level, worldPosition.above(), Direction.DOWN))
			return false;
		if (!inputChutes.isEmpty())
			return false;

		if (!simulate) {
			Vector3d dropVec = VecHelper.getCenterOf(worldPosition)
				.add(0, 8 / 16f, 0);
			ItemEntity dropped = new ItemEntity(level, dropVec.x, dropVec.y, dropVec.z, item.copy());
			dropped.setDefaultPickUpDelay();
			dropped.setDeltaMovement(0, getItemMotion() * 2, 0);
			level.addFreshEntity(dropped);
			setItem(ItemStack.EMPTY);
		}
		return true;
	}

	protected boolean canAcceptItem(ItemStack stack) {
		return item.isEmpty();
	}

	protected int getExtractionAmount() {
		return 16;
	}

	protected ExtractionCountMode getExtractionMode() {
		return ExtractionCountMode.UPTO;
	}

	protected boolean canCollectItemsFromBelow() {
		return true;
	}

	private LazyOptional<IItemHandler> grabCapability(Direction side) {
		BlockPos pos = this.worldPosition.relative(side);
		if (level == null)
			return LazyOptional.empty();
		TileEntity te = level.getBlockEntity(pos);
		if (te == null)
			return LazyOptional.empty();
		if (te instanceof ChuteTileEntity) {
			if (side != Direction.DOWN || !(te instanceof SmartChuteTileEntity) || getItemMotion() > 0)
				return LazyOptional.empty();
		}
		return te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite());
	}

	public void setItem(ItemStack stack) {
		setItem(stack, getItemMotion() < 0 ? 1 : 0);
	}

	public void setItem(ItemStack stack, float insertionPos) {
		item = stack;
		itemPosition.lastValue = itemPosition.value = insertionPos;
		if (!level.isClientSide)
			notifyUpdate();
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		if (lazyHandler != null)
			lazyHandler.invalidate();
	}

	@Override
	public void write(CompoundNBT compound, boolean clientPacket) {
		compound.put("Item", item.serializeNBT());
		compound.putFloat("ItemPosition", itemPosition.value);
		compound.putFloat("Pull", pull);
		compound.putFloat("Push", push);
		compound.putFloat("BottomAirFlowDistance", bottomPullDistance);
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
		ItemStack previousItem = item;
		item = ItemStack.of(compound.getCompound("Item"));
		itemPosition.lastValue = itemPosition.value = compound.getFloat("ItemPosition");
		pull = compound.getFloat("Pull");
		push = compound.getFloat("Push");
		bottomPullDistance = compound.getFloat("BottomAirFlowDistance");
		super.fromTag(state, compound, clientPacket);
//		if (clientPacket)
//			airCurrent.rebuild();

		if (hasLevel() && level != null && level.isClientSide && !previousItem.equals(item, false) && !item.isEmpty()) {
			if (level.random.nextInt(3) != 0)
				return;
			Vector3d p = VecHelper.getCenterOf(worldPosition);
			p = VecHelper.offsetRandomly(p, level.random, .5f);
			Vector3d m = Vector3d.ZERO;
			level.addParticle(new ItemParticleData(ParticleTypes.ITEM, item), p.x, p.y, p.z, m.x, m.y, m.z);
		}
	}

	public float getItemMotion() {
		// Chutes per second
		final float fanSpeedModifier = 1 / 64f;
		final float maxItemSpeed = 20f;
		final float gravity = 4f;

		float motion = (push + pull) * fanSpeedModifier;
		return (MathHelper.clamp(motion, -maxItemSpeed, maxItemSpeed) + (motion <= 0 ? -gravity : 0)) / 20f;
	}

	public void onRemoved(BlockState chuteState) {
		ChuteTileEntity targetChute = getTargetChute(chuteState);
		List<ChuteTileEntity> inputChutes = getInputChutes();
		if (!item.isEmpty() && level != null)
			InventoryHelper.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), item);
		setRemoved();
		if (targetChute != null) {
			targetChute.updatePull();
			targetChute.propagatePush();
		}
		inputChutes.forEach(c -> c.updatePush(inputChutes.size()));
	}

	public void onAdded() {
		clearCache();
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
		updateAirFlow = true;
		sendData();
		ChuteTileEntity targetChute = getTargetChute(getBlockState());
		if (targetChute != null)
			targetChute.updatePull();
	}

	public void updatePush(int branchCount) {
		float totalPush = calculatePush(branchCount);
		if (push == totalPush)
			return;
		updateAirFlow = true;
		push = totalPush;
		sendData();
		propagatePush();
	}

	public void propagatePush() {
		List<ChuteTileEntity> inputs = getInputChutes();
		inputs.forEach(c -> c.updatePush(inputs.size()));
	}

	protected float calculatePull() {
		BlockState blockStateAbove = level.getBlockState(worldPosition.above());
		if (AllBlocks.ENCASED_FAN.has(blockStateAbove)
			&& blockStateAbove.getValue(EncasedFanBlock.FACING) == Direction.DOWN) {
			TileEntity te = level.getBlockEntity(worldPosition.above());
			if (te instanceof EncasedFanTileEntity && !te.isRemoved()) {
				EncasedFanTileEntity fan = (EncasedFanTileEntity) te;
				return fan.getSpeed();
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
		if (level == null)
			return 0;
		BlockState blockStateBelow = level.getBlockState(worldPosition.below());
		if (AllBlocks.ENCASED_FAN.has(blockStateBelow) && blockStateBelow.getValue(EncasedFanBlock.FACING) == Direction.UP) {
			TileEntity te = level.getBlockEntity(worldPosition.below());
			if (te instanceof EncasedFanTileEntity && !te.isRemoved()) {
				EncasedFanTileEntity fan = (EncasedFanTileEntity) te;
				return fan.getSpeed();
			}
		}

		ChuteTileEntity targetChute = getTargetChute(getBlockState());
		if (targetChute == null)
			return 0;
		return targetChute.push / branchCount;
	}

	@Nullable
	private ChuteTileEntity getTargetChute(BlockState state) {
		if (level == null)
			return null;
		Direction targetDirection = AbstractChuteBlock.getChuteFacing(state);
		if (targetDirection == null)
			return null;
		BlockPos chutePos = worldPosition.below();
		if (targetDirection.getAxis()
			.isHorizontal())
			chutePos = chutePos.relative(targetDirection.getOpposite());
		BlockState chuteState = level.getBlockState(chutePos);
		if (!AbstractChuteBlock.isChute(chuteState))
			return null;
		TileEntity te = level.getBlockEntity(chutePos);
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
		if (level == null || direction == Direction.DOWN)
			return null;
		direction = direction.getOpposite();
		BlockPos chutePos = worldPosition.above();
		if (direction.getAxis()
			.isHorizontal())
			chutePos = chutePos.relative(direction);
		BlockState chuteState = level.getBlockState(chutePos);
		Direction chuteFacing = AbstractChuteBlock.getChuteFacing(chuteState);
		if (chuteFacing != direction)
			return null;
		TileEntity te = level.getBlockEntity(chutePos);
		if (te instanceof ChuteTileEntity && !te.isRemoved())
			return (ChuteTileEntity) te;
		return null;
	}

	public boolean addToGoggleTooltip(List<ITextComponent> tooltip, boolean isPlayerSneaking) {
		boolean downward = getItemMotion() < 0;
		tooltip.add(componentSpacing.plainCopy()
			.append(Lang.translate("tooltip.chute.header")));
		if (pull == 0 && push == 0)
			tooltip.add(componentSpacing.plainCopy()
				.append(Lang.translate("tooltip.chute.no_fans_attached"))
				.withStyle(TextFormatting.GRAY));
		if (pull != 0)
			tooltip.add(componentSpacing.plainCopy()
				.append(Lang.translate("tooltip.chute.fans_" + (pull > 0 ? "pull_up" : "push_down"))
					.withStyle(TextFormatting.GRAY)));
		if (push != 0)
			tooltip.add(componentSpacing.plainCopy()
				.append(Lang.translate("tooltip.chute.fans_" + (push > 0 ? "push_up" : "pull_down"))
					.withStyle(TextFormatting.GRAY)));
		tooltip.add(componentSpacing.plainCopy()
			.append("-> ")
			.append(Lang.translate("tooltip.chute.items_move_" + (downward ? "down" : "up"))
				.withStyle(TextFormatting.YELLOW)));
		if (!item.isEmpty()) {
			tooltip.add(componentSpacing.plainCopy()
				.append(Lang.translate("tooltip.chute.contains", new TranslationTextComponent(item.getItem()
					.getDescriptionId(item)).getString(), item.getCount()))
				.withStyle(TextFormatting.GREEN));
		}
		return true;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return lazyHandler.cast();
		return super.getCapability(cap, side);
	}

	public ItemStack getItem() {
		return item;
	}

	//	@Override
	//	@Nullable
	//	public AirCurrent getAirCurrent() {
	//		return airCurrent;
	//	}
	//
	//	@Nullable
	//	@Override
	//	public World getAirCurrentWorld() {
	//		return world;
	//	}
	//
	//	@Override
	//	public BlockPos getAirCurrentPos() {
	//		return pos;
	//	}
	//
	//	@Override
	//	public float getSpeed() {
	//		if (getBlockState().get(ChuteBlock.SHAPE) == Shape.NORMAL && getBlockState().get(ChuteBlock.FACING) != Direction.DOWN)
	//			return 0;
	//		return pull + push;
	//	}
	//
	//	@Override
	//	@Nullable
	//	public Direction getAirFlowDirection() {
	//		float speed = getSpeed();
	//		if (speed == 0)
	//			return null;
	//		return speed > 0 ? Direction.UP : Direction.DOWN;
	//	}
	//
	//	@Override
	//	public boolean isSourceRemoved() {
	//		return removed;
	//	}
	//
	//	@Override
	//	public Direction getAirflowOriginSide() {
	//		return world != null && !(world.getTileEntity(pos.down()) instanceof IAirCurrentSource)
	//			&& getBlockState().get(ChuteBlock.FACING) == Direction.DOWN ? Direction.DOWN : Direction.UP;
	//	}
}
