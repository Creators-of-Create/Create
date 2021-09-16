package com.simibubi.create.content.logistics.block.mechanicalArm;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.ITransformableTE;
import com.simibubi.create.content.contraptions.components.structureMovement.StructureTransform;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmInteractionPoint.Jukebox;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmInteractionPoint.Mode;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widgets.InterpolatedAngle;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.INamedIconOptions;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;

public class ArmTileEntity extends KineticTileEntity implements ITransformableTE {

	// Server
	List<ArmInteractionPoint> inputs;
	List<ArmInteractionPoint> outputs;
	ListTag interactionPointTag;

	// Both
	float chasedPointProgress;
	int chasedPointIndex;
	ItemStack heldItem;
	Phase phase;

	// Client
	ArmAngleTarget previousTarget;
	InterpolatedAngle lowerArmAngle;
	InterpolatedAngle upperArmAngle;
	InterpolatedAngle baseAngle;
	InterpolatedAngle headAngle;
	InterpolatedAngle clawAngle;
	float previousBaseAngle;
	boolean updateInteractionPoints;

	//
	protected ScrollOptionBehaviour<SelectionMode> selectionMode;
	protected int lastInputIndex = -1;
	protected int lastOutputIndex = -1;
	protected boolean redstoneLocked;

	public enum Phase {
		SEARCH_INPUTS, MOVE_TO_INPUT, SEARCH_OUTPUTS, MOVE_TO_OUTPUT, DANCING
	}

	public ArmTileEntity(BlockEntityType<?> typeIn) {
		super(typeIn);
		inputs = new ArrayList<>();
		outputs = new ArrayList<>();
		interactionPointTag = new ListTag();
		heldItem = ItemStack.EMPTY;
		phase = Phase.SEARCH_INPUTS;
		previousTarget = ArmAngleTarget.NO_TARGET;
		baseAngle = new InterpolatedAngle();
		baseAngle.init(previousTarget.baseAngle);
		lowerArmAngle = new InterpolatedAngle();
		lowerArmAngle.init(previousTarget.lowerArmAngle);
		upperArmAngle = new InterpolatedAngle();
		upperArmAngle.init(previousTarget.upperArmAngle);
		headAngle = new InterpolatedAngle();
		headAngle.init(previousTarget.headAngle);
		clawAngle = new InterpolatedAngle();
		previousBaseAngle = previousTarget.baseAngle;
		updateInteractionPoints = true;
		redstoneLocked = false;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);

		selectionMode = new ScrollOptionBehaviour<SelectionMode>(SelectionMode.class,
			Lang.translate("logistics.when_multiple_outputs_available"), this, new SelectionModeValueBox());
		selectionMode.requiresWrench();
		behaviours.add(selectionMode);
	}

	@Override
	public void tick() {
		super.tick();
		initInteractionPoints();
		boolean targetReached = tickMovementProgress();

		if (chasedPointProgress < 1) {
			if (phase == Phase.MOVE_TO_INPUT) {
				ArmInteractionPoint point = getTargetedInteractionPoint();
				if (point != null)
					point.keepAlive(level);
			}
			return;
		}
		if (level.isClientSide)
			return;

		if (phase == Phase.MOVE_TO_INPUT)
			collectItem();
		else if (phase == Phase.MOVE_TO_OUTPUT)
			depositItem();
		else if (phase == Phase.SEARCH_INPUTS || phase == Phase.DANCING)
			searchForItem();

		if (targetReached)
			lazyTick();
	}

	@Override
	public void lazyTick() {
		super.lazyTick();

		if (level.isClientSide)
			return;
		if (chasedPointProgress < .5f)
			return;
		if (phase == Phase.SEARCH_INPUTS || phase == Phase.DANCING)
			checkForMusic();
		if (phase == Phase.SEARCH_OUTPUTS)
			searchForDestination();
	}

	private void checkForMusic() {
		boolean hasMusic = checkForMusicAmong(inputs) || checkForMusicAmong(outputs);
		if (hasMusic != (phase == Phase.DANCING)) {
			phase = hasMusic ? Phase.DANCING : Phase.SEARCH_INPUTS;
			setChanged();
			sendData();
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public AABB makeRenderBoundingBox() {
		return super.makeRenderBoundingBox().inflate(3);
	}

	private boolean checkForMusicAmong(List<ArmInteractionPoint> list) {
		for (ArmInteractionPoint armInteractionPoint : list) {
			if (!(armInteractionPoint instanceof Jukebox))
				continue;
			BlockState state = level.getBlockState(armInteractionPoint.pos);
			if (state.getOptionalValue(JukeboxBlock.HAS_RECORD).orElse(false))
				return true;
		}
		return false;
	}

	private boolean tickMovementProgress() {
		boolean targetReachedPreviously = chasedPointProgress >= 1;
		chasedPointProgress += Math.min(256, Math.abs(getSpeed())) / 1024f;
		if (chasedPointProgress > 1)
			chasedPointProgress = 1;
		if (!level.isClientSide)
			return !targetReachedPreviously && chasedPointProgress >= 1;

		ArmInteractionPoint targetedInteractionPoint = getTargetedInteractionPoint();
		ArmAngleTarget previousTarget = this.previousTarget;
		ArmAngleTarget target = targetedInteractionPoint == null ? ArmAngleTarget.NO_TARGET
			: targetedInteractionPoint.getTargetAngles(worldPosition, isOnCeiling());

		baseAngle.set(AngleHelper.angleLerp(chasedPointProgress, previousBaseAngle,
			target == ArmAngleTarget.NO_TARGET ? previousBaseAngle : target.baseAngle));

		// Arm's angles first backup to resting position and then continue
		if (chasedPointProgress < .5f)
			target = ArmAngleTarget.NO_TARGET;
		else
			previousTarget = ArmAngleTarget.NO_TARGET;
		float progress = chasedPointProgress == 1 ? 1 : (chasedPointProgress % .5f) * 2;

		lowerArmAngle.set(Mth.lerp(progress, previousTarget.lowerArmAngle, target.lowerArmAngle));
		upperArmAngle.set(Mth.lerp(progress, previousTarget.upperArmAngle, target.upperArmAngle));

		headAngle.set(AngleHelper.angleLerp(progress, previousTarget.headAngle % 360, target.headAngle % 360));
		return false;
	}

	protected boolean isOnCeiling() {
		BlockState state = getBlockState();
		return hasLevel() && state.getOptionalValue(ArmBlock.CEILING).orElse(false);
	}

	@Nullable
	private ArmInteractionPoint getTargetedInteractionPoint() {
		if (chasedPointIndex == -1)
			return null;
		if (phase == Phase.MOVE_TO_INPUT && chasedPointIndex < inputs.size())
			return inputs.get(chasedPointIndex);
		if (phase == Phase.MOVE_TO_OUTPUT && chasedPointIndex < outputs.size())
			return outputs.get(chasedPointIndex);
		return null;
	}

	protected void searchForItem() {
		if (redstoneLocked)
			return;

		boolean foundInput = false;
		// for round robin, we start looking after the last used index, for default we
		// start at 0;
		int startIndex = selectionMode.get() == SelectionMode.PREFER_FIRST ? 0 : lastInputIndex + 1;

		// if we enforce round robin, only look at the next input in the list,
		// otherwise, look at all inputs
		int scanRange = selectionMode.get() == SelectionMode.FORCED_ROUND_ROBIN ? lastInputIndex + 2 : inputs.size();
		if (scanRange > inputs.size())
			scanRange = inputs.size();

		InteractionPoints: for (int i = startIndex; i < scanRange; i++) {
			ArmInteractionPoint armInteractionPoint = inputs.get(i);
			if (!armInteractionPoint.isStillValid(level))
				continue;
			for (int j = 0; j < armInteractionPoint.getSlotCount(level); j++) {
				if (getDistributableAmount(armInteractionPoint, j) == 0)
					continue;

				selectIndex(true, i);
				foundInput = true;
				break InteractionPoints;
			}
		}
		if (!foundInput && selectionMode.get() == SelectionMode.ROUND_ROBIN) {
			// if we didn't find an input, but don't want to enforce round robin, reset the
			// last index
			lastInputIndex = -1;
		}
		if (lastInputIndex == inputs.size() - 1) {
			// if we reached the last input in the list, reset the last index
			lastInputIndex = -1;
		}
	}

	protected void searchForDestination() {
		ItemStack held = heldItem.copy();

		boolean foundOutput = false;
		// for round robin, we start looking after the last used index, for default we
		// start at 0;
		int startIndex = selectionMode.get() == SelectionMode.PREFER_FIRST ? 0 : lastOutputIndex + 1;

		// if we enforce round robin, only look at the next index in the list,
		// otherwise, look at all
		int scanRange = selectionMode.get() == SelectionMode.FORCED_ROUND_ROBIN ? lastOutputIndex + 2 : outputs.size();
		if (scanRange > outputs.size())
			scanRange = outputs.size();

		for (int i = startIndex; i < scanRange; i++) {
			ArmInteractionPoint armInteractionPoint = outputs.get(i);
			if (!armInteractionPoint.isStillValid(level))
				continue;

			ItemStack remainder = armInteractionPoint.insert(level, held, true);
			if (remainder.equals(heldItem, false))
				continue;

			selectIndex(false, i);
			foundOutput = true;
			break;
		}

		if (!foundOutput && selectionMode.get() == SelectionMode.ROUND_ROBIN) {
			// if we didn't find an input, but don't want to enforce round robin, reset the
			// last index
			lastOutputIndex = -1;
		}
		if (lastOutputIndex == outputs.size() - 1) {
			// if we reached the last input in the list, reset the last index
			lastOutputIndex = -1;
		}
	}

	// input == true => select input, false => select output
	private void selectIndex(boolean input, int index) {
		phase = input ? Phase.MOVE_TO_INPUT : Phase.MOVE_TO_OUTPUT;
		chasedPointIndex = index;
		chasedPointProgress = 0;
		if (input)
			lastInputIndex = index;
		else
			lastOutputIndex = index;
		sendData();
		setChanged();
	}

	protected int getDistributableAmount(ArmInteractionPoint armInteractionPoint, int i) {
		ItemStack stack = armInteractionPoint.extract(level, i, true);
		ItemStack remainder = simulateInsertion(stack);
		return stack.getCount() - remainder.getCount();
	}

	protected void depositItem() {
		ArmInteractionPoint armInteractionPoint = getTargetedInteractionPoint();
		if (armInteractionPoint != null) {
			ItemStack toInsert = heldItem.copy();
			ItemStack remainder = armInteractionPoint.insert(level, toInsert, false);
			heldItem = remainder;
		}
		phase = heldItem.isEmpty() ? Phase.SEARCH_INPUTS : Phase.SEARCH_OUTPUTS;
		chasedPointProgress = 0;
		chasedPointIndex = -1;
		sendData();
		setChanged();

		if (!level.isClientSide)
			AllTriggers.triggerForNearbyPlayers(AllTriggers.MECHANICAL_ARM, level, worldPosition, 10);
	}

	protected void collectItem() {
		ArmInteractionPoint armInteractionPoint = getTargetedInteractionPoint();
		if (armInteractionPoint != null)
			for (int i = 0; i < armInteractionPoint.getSlotCount(level); i++) {
				int amountExtracted = getDistributableAmount(armInteractionPoint, i);
				if (amountExtracted == 0)
					continue;

				ItemStack prevHeld = heldItem;
				heldItem = armInteractionPoint.extract(level, i, amountExtracted, false);
				phase = Phase.SEARCH_OUTPUTS;
				chasedPointProgress = 0;
				chasedPointIndex = -1;
				sendData();
				setChanged();

				if (!prevHeld.sameItem(heldItem))
					level.playSound(null, worldPosition, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, .125f,
							.5f + Create.RANDOM.nextFloat() * .25f);
				return;
			}

		phase = Phase.SEARCH_INPUTS;
		chasedPointProgress = 0;
		chasedPointIndex = -1;
		sendData();
		setChanged();
	}

	private ItemStack simulateInsertion(ItemStack stack) {
		for (ArmInteractionPoint armInteractionPoint : outputs) {
			stack = armInteractionPoint.insert(level, stack, true);
			if (stack.isEmpty())
				break;
		}
		return stack;
	}

	public void redstoneUpdate() {
		if (level.isClientSide)
			return;
		boolean blockPowered = level.hasNeighborSignal(worldPosition);
		if (blockPowered == redstoneLocked)
			return;
		redstoneLocked = blockPowered;
		sendData();
		if (!redstoneLocked)
			searchForItem();
	}

	@Override
	public void transform(StructureTransform transform) {
		if (interactionPointTag == null)
			return;

		for (Tag inbt : interactionPointTag) {
			ArmInteractionPoint.transformPos(transform, (CompoundTag) inbt);
		}

		sendData();
		setChanged();
	}

	protected void initInteractionPoints() {
		if (!updateInteractionPoints || interactionPointTag == null)
			return;
		if (!level.isAreaLoaded(worldPosition, getRange() + 1))
			return;
		inputs.clear();
		outputs.clear();

		boolean hasBlazeBurner = false;
		for (Tag inbt : interactionPointTag) {
			ArmInteractionPoint point = ArmInteractionPoint.deserialize(level, worldPosition, (CompoundTag) inbt);
			if (point == null)
				continue;
			if (point.mode == Mode.DEPOSIT)
				outputs.add(point);
			if (point.mode == Mode.TAKE)
				inputs.add(point);
			hasBlazeBurner |= point instanceof ArmInteractionPoint.BlazeBurner;
		}

		if (!level.isClientSide) {
			if (outputs.size() >= 10)
				AllTriggers.triggerForNearbyPlayers(AllTriggers.ARM_MANY_TARGETS, level, worldPosition, 5);
			if (hasBlazeBurner)
				AllTriggers.triggerForNearbyPlayers(AllTriggers.ARM_BLAZE_BURNER, level, worldPosition, 5);
		}

		updateInteractionPoints = false;
		sendData();
		setChanged();
	}

	public void writeInteractionPoints(CompoundTag compound) {
		if (updateInteractionPoints) {
			compound.put("InteractionPoints", interactionPointTag);
		} else {
			ListTag pointsNBT = new ListTag();
			inputs.stream()
					.map(aip -> aip.serialize(worldPosition))
					.forEach(pointsNBT::add);
			outputs.stream()
					.map(aip -> aip.serialize(worldPosition))
					.forEach(pointsNBT::add);
			compound.put("InteractionPoints", pointsNBT);
		}
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		super.write(compound, clientPacket);

		writeInteractionPoints(compound);

		NBTHelper.writeEnum(compound, "Phase", phase);
		compound.putBoolean("Powered", redstoneLocked);
		compound.put("HeldItem", heldItem.serializeNBT());
		compound.putInt("TargetPointIndex", chasedPointIndex);
		compound.putFloat("MovementProgress", chasedPointProgress);
	}

	@Override
	public void writeSafe(CompoundTag compound, boolean clientPacket) {
		super.writeSafe(compound, clientPacket);

		writeInteractionPoints(compound);
	}

	@Override
	protected void fromTag(BlockState state, CompoundTag compound, boolean clientPacket) {
		int previousIndex = chasedPointIndex;
		Phase previousPhase = phase;
		ListTag interactionPointTagBefore = interactionPointTag;

		super.fromTag(state, compound, clientPacket);
		heldItem = ItemStack.of(compound.getCompound("HeldItem"));
		phase = NBTHelper.readEnum(compound, "Phase", Phase.class);
		chasedPointIndex = compound.getInt("TargetPointIndex");
		chasedPointProgress = compound.getFloat("MovementProgress");
		interactionPointTag = compound.getList("InteractionPoints", NBT.TAG_COMPOUND);
		redstoneLocked = compound.getBoolean("Powered");

		if (!clientPacket)
			return;

		boolean ceiling = isOnCeiling();
		if (interactionPointTagBefore == null || interactionPointTagBefore.size() != interactionPointTag.size())
			updateInteractionPoints = true;
		if (previousIndex != chasedPointIndex || (previousPhase != phase)) {
			ArmInteractionPoint previousPoint = null;
			if (previousPhase == Phase.MOVE_TO_INPUT && previousIndex < inputs.size())
				previousPoint = inputs.get(previousIndex);
			if (previousPhase == Phase.MOVE_TO_OUTPUT && previousIndex < outputs.size())
				previousPoint = outputs.get(previousIndex);
			previousTarget =
				previousPoint == null ? ArmAngleTarget.NO_TARGET : previousPoint.getTargetAngles(worldPosition, ceiling);
			if (previousPoint != null)
				previousBaseAngle = previousPoint.getTargetAngles(worldPosition, ceiling).baseAngle;
		}
	}

	public static int getRange() {
		return AllConfigs.SERVER.logistics.mechanicalArmRange.get();
	}

	@Override
	public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		if (super.addToTooltip(tooltip, isPlayerSneaking))
			return true;
		if (isPlayerSneaking)
			return false;
		if (!inputs.isEmpty())
			return false;
		if (!outputs.isEmpty())
			return false;

		TooltipHelper.addHint(tooltip, "hint.mechanical_arm_no_targets");
		return true;
	}

	@Override
	public boolean shouldRenderNormally() {
		return true;
	}

	private class SelectionModeValueBox extends CenteredSideValueBoxTransform {

		public SelectionModeValueBox() {
			super((blockState, direction) -> direction != Direction.DOWN && direction != Direction.UP);
		}

		@Override
		protected Vec3 getLocalOffset(BlockState state) {
			int yPos = state.getValue(ArmBlock.CEILING) ? 16 - 3 : 3;
			Vec3 location = VecHelper.voxelSpace(8, yPos, 15.95);
			location = VecHelper.rotateCentered(location, AngleHelper.horizontalAngle(getSide()), Direction.Axis.Y);
			return location;
		}

		@Override
		protected float getScale() {
			return .3f;
		}

	}

	public enum SelectionMode implements INamedIconOptions {
		ROUND_ROBIN(AllIcons.I_ARM_ROUND_ROBIN),
		FORCED_ROUND_ROBIN(AllIcons.I_ARM_FORCED_ROUND_ROBIN),
		PREFER_FIRST(AllIcons.I_ARM_PREFER_FIRST),

		;

		private final String translationKey;
		private final AllIcons icon;

		SelectionMode(AllIcons icon) {
			this.icon = icon;
			this.translationKey = "mechanical_arm.selection_mode." + Lang.asId(name());
		}

		@Override
		public AllIcons getIcon() {
			return icon;
		}

		@Override
		public String getTranslationKey() {
			return translationKey;
		}
	}

}
