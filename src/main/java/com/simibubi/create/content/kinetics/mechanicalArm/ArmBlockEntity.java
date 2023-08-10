package com.simibubi.create.content.kinetics.mechanicalArm;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.ITransformableBlockEntity;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.AllArmInteractionPointTypes.JukeboxPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint.Mode;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.createmod.catnip.utility.NBTHelper;
import net.createmod.catnip.utility.VecHelper;
import net.createmod.catnip.utility.animation.LerpedFloat;
import net.createmod.catnip.utility.lang.Lang;
import net.createmod.catnip.utility.math.AngleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class ArmBlockEntity extends KineticBlockEntity implements ITransformableBlockEntity {

	// Server
	List<ArmInteractionPoint> inputs;
	List<ArmInteractionPoint> outputs;
	ListTag interactionPointTag;

	// Both
	float chasedPointProgress;
	int chasedPointIndex;
	ItemStack heldItem;
	Phase phase;
	boolean goggles;

	// Client
	ArmAngleTarget previousTarget;
	LerpedFloat lowerArmAngle;
	LerpedFloat upperArmAngle;
	LerpedFloat baseAngle;
	LerpedFloat headAngle;
	LerpedFloat clawAngle;
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

	public ArmBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
		inputs = new ArrayList<>();
		outputs = new ArrayList<>();
		interactionPointTag = new ListTag();
		heldItem = ItemStack.EMPTY;
		phase = Phase.SEARCH_INPUTS;
		previousTarget = ArmAngleTarget.NO_TARGET;
		baseAngle = LerpedFloat.angular();
		baseAngle.startWithValue(previousTarget.baseAngle);
		lowerArmAngle = LerpedFloat.angular();
		lowerArmAngle.startWithValue(previousTarget.lowerArmAngle);
		upperArmAngle = LerpedFloat.angular();
		upperArmAngle.startWithValue(previousTarget.upperArmAngle);
		headAngle = LerpedFloat.angular();
		headAngle.startWithValue(previousTarget.headAngle);
		clawAngle = LerpedFloat.angular();
		previousBaseAngle = previousTarget.baseAngle;
		updateInteractionPoints = true;
		redstoneLocked = false;
		goggles = false;
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);

		selectionMode = new ScrollOptionBehaviour<SelectionMode>(SelectionMode.class,
			CreateLang.translateDirect("logistics.when_multiple_outputs_available"), this, new SelectionModeValueBox());
		behaviours.add(selectionMode);

		registerAwardables(behaviours, AllAdvancements.ARM_BLAZE_BURNER, AllAdvancements.ARM_MANY_TARGETS,
			AllAdvancements.MECHANICAL_ARM, AllAdvancements.MUSICAL_ARM);
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
					point.keepAlive();
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
	protected AABB createRenderBoundingBox() {
		return super.createRenderBoundingBox().inflate(3);
	}

	private boolean checkForMusicAmong(List<ArmInteractionPoint> list) {
		for (ArmInteractionPoint armInteractionPoint : list) {
			if (!(armInteractionPoint instanceof AllArmInteractionPointTypes.JukeboxPoint))
				continue;
			BlockState state = level.getBlockState(armInteractionPoint.getPos());
			if (state.getOptionalValue(JukeboxBlock.HAS_RECORD)
				.orElse(false))
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

		baseAngle.setValue(AngleHelper.angleLerp(chasedPointProgress, previousBaseAngle,
			target == ArmAngleTarget.NO_TARGET ? previousBaseAngle : target.baseAngle));

		// Arm's angles first backup to resting position and then continue
		if (chasedPointProgress < .5f)
			target = ArmAngleTarget.NO_TARGET;
		else
			previousTarget = ArmAngleTarget.NO_TARGET;
		float progress = chasedPointProgress == 1 ? 1 : (chasedPointProgress % .5f) * 2;

		lowerArmAngle.setValue(Mth.lerp(progress, previousTarget.lowerArmAngle, target.lowerArmAngle));
		upperArmAngle.setValue(Mth.lerp(progress, previousTarget.upperArmAngle, target.upperArmAngle));
		headAngle.setValue(AngleHelper.angleLerp(progress, previousTarget.headAngle % 360, target.headAngle % 360));

		return false;
	}

	protected boolean isOnCeiling() {
		BlockState state = getBlockState();
		return hasLevel() && state.getOptionalValue(ArmBlock.CEILING)
			.orElse(false);
	}

	@Override
	public void destroy() {
		super.destroy();
		if (!heldItem.isEmpty())
			Block.popResource(level, worldPosition, heldItem);
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
			if (!armInteractionPoint.isValid())
				continue;
			for (int j = 0; j < armInteractionPoint.getSlotCount(); j++) {
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
			if (!armInteractionPoint.isValid())
				continue;

			ItemStack remainder = armInteractionPoint.insert(held, true);
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
		ItemStack stack = armInteractionPoint.extract(i, true);
		ItemStack remainder = simulateInsertion(stack);
		if (stack.sameItem(remainder)) {
			return stack.getCount() - remainder.getCount();
		} else {
			return stack.getCount();
		}
	}

	private ItemStack simulateInsertion(ItemStack stack) {
		for (ArmInteractionPoint armInteractionPoint : outputs) {
			if (armInteractionPoint.isValid())
				stack = armInteractionPoint.insert(stack, true);
			if (stack.isEmpty())
				break;
		}
		return stack;
	}

	protected void depositItem() {
		ArmInteractionPoint armInteractionPoint = getTargetedInteractionPoint();
		if (armInteractionPoint != null && armInteractionPoint.isValid()) {
			ItemStack toInsert = heldItem.copy();
			ItemStack remainder = armInteractionPoint.insert(toInsert, false);
			heldItem = remainder;

			if (armInteractionPoint instanceof JukeboxPoint && remainder.isEmpty())
				award(AllAdvancements.MUSICAL_ARM);
		}

		phase = heldItem.isEmpty() ? Phase.SEARCH_INPUTS : Phase.SEARCH_OUTPUTS;
		chasedPointProgress = 0;
		chasedPointIndex = -1;
		sendData();
		setChanged();

		if (!level.isClientSide)
			award(AllAdvancements.MECHANICAL_ARM);
	}

	protected void collectItem() {
		ArmInteractionPoint armInteractionPoint = getTargetedInteractionPoint();
		if (armInteractionPoint != null && armInteractionPoint.isValid())
			for (int i = 0; i < armInteractionPoint.getSlotCount(); i++) {
				int amountExtracted = getDistributableAmount(armInteractionPoint, i);
				if (amountExtracted == 0)
					continue;

				ItemStack prevHeld = heldItem;
				heldItem = armInteractionPoint.extract(i, amountExtracted, false);
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

		for (Tag tag : interactionPointTag) {
			ArmInteractionPoint.transformPos((CompoundTag) tag, transform);
		}

		notifyUpdate();
	}

	// ClientLevel#hasChunk (and consequently #isAreaLoaded) always returns true,
	// so manually check the ChunkSource to avoid weird behavior on the client side
	protected boolean isAreaActuallyLoaded(BlockPos center, int range) {
		if (!level.isAreaLoaded(center, range)) {
			return false;
		}
		if (level.isClientSide) {
			int minY = center.getY() - range;
			int maxY = center.getY() + range;
			if (maxY < level.getMinBuildHeight() || minY >= level.getMaxBuildHeight()) {
				return false;
			}

			int minX = center.getX() - range;
			int minZ = center.getZ() - range;
			int maxX = center.getX() + range;
			int maxZ = center.getZ() + range;

			int minChunkX = SectionPos.blockToSectionCoord(minX);
			int maxChunkX = SectionPos.blockToSectionCoord(maxX);
			int minChunkZ = SectionPos.blockToSectionCoord(minZ);
			int maxChunkZ = SectionPos.blockToSectionCoord(maxZ);

			ChunkSource chunkSource = level.getChunkSource();
			for (int chunkX = minChunkX; chunkX <= maxChunkX; ++chunkX) {
				for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; ++chunkZ) {
					if (!chunkSource.hasChunk(chunkX, chunkZ)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	protected void initInteractionPoints() {
		if (!updateInteractionPoints || interactionPointTag == null)
			return;
		if (!isAreaActuallyLoaded(worldPosition, getRange() + 1))
			return;
		inputs.clear();
		outputs.clear();

		boolean hasBlazeBurner = false;
		for (Tag tag : interactionPointTag) {
			ArmInteractionPoint point = ArmInteractionPoint.deserialize((CompoundTag) tag, level, worldPosition);
			if (point == null)
				continue;
			if (point.getMode() == Mode.DEPOSIT)
				outputs.add(point);
			else if (point.getMode() == Mode.TAKE)
				inputs.add(point);
			hasBlazeBurner |= point instanceof AllArmInteractionPointTypes.BlazeBurnerPoint;
		}

		if (!level.isClientSide) {
			if (outputs.size() >= 10)
				award(AllAdvancements.ARM_MANY_TARGETS);
			if (hasBlazeBurner)
				award(AllAdvancements.ARM_BLAZE_BURNER);
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
		compound.putBoolean("Goggles", goggles);
		compound.put("HeldItem", heldItem.serializeNBT());
		compound.putInt("TargetPointIndex", chasedPointIndex);
		compound.putFloat("MovementProgress", chasedPointProgress);
	}

	@Override
	public void writeSafe(CompoundTag compound) {
		super.writeSafe(compound);

		writeInteractionPoints(compound);
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		int previousIndex = chasedPointIndex;
		Phase previousPhase = phase;
		ListTag interactionPointTagBefore = interactionPointTag;

		super.read(compound, clientPacket);
		heldItem = ItemStack.of(compound.getCompound("HeldItem"));
		phase = NBTHelper.readEnum(compound, "Phase", Phase.class);
		chasedPointIndex = compound.getInt("TargetPointIndex");
		chasedPointProgress = compound.getFloat("MovementProgress");
		interactionPointTag = compound.getList("InteractionPoints", Tag.TAG_COMPOUND);
		redstoneLocked = compound.getBoolean("Powered");

		boolean hadGoggles = goggles;
		goggles = compound.getBoolean("Goggles");

		if (!clientPacket)
			return;

		if (hadGoggles != goggles)
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> InstancedRenderDispatcher.enqueueUpdate(this));

		boolean ceiling = isOnCeiling();
		if (interactionPointTagBefore == null || interactionPointTagBefore.size() != interactionPointTag.size())
			updateInteractionPoints = true;
		if (previousIndex != chasedPointIndex || (previousPhase != phase)) {
			ArmInteractionPoint previousPoint = null;
			if (previousPhase == Phase.MOVE_TO_INPUT && previousIndex < inputs.size())
				previousPoint = inputs.get(previousIndex);
			if (previousPhase == Phase.MOVE_TO_OUTPUT && previousIndex < outputs.size())
				previousPoint = outputs.get(previousIndex);
			previousTarget = previousPoint == null ? ArmAngleTarget.NO_TARGET
				: previousPoint.getTargetAngles(worldPosition, ceiling);
			if (previousPoint != null)
				previousBaseAngle = previousTarget.baseAngle;

			ArmInteractionPoint targetedPoint = getTargetedInteractionPoint();
			if (targetedPoint != null)
				targetedPoint.updateCachedState();
		}
	}

	public static int getRange() {
		return AllConfigs.server().logistics.mechanicalArmRange.get();
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

	public void setLevel(Level level) {
		super.setLevel(level);
		for (ArmInteractionPoint input : inputs) {
			input.setLevel(level);
		}
		for (ArmInteractionPoint output : outputs) {
			output.setLevel(level);
		}
	}

	private class SelectionModeValueBox extends CenteredSideValueBoxTransform {

		public SelectionModeValueBox() {
			super((blockState, direction) -> !direction.getAxis()
				.isVertical());
		}

		@Override
		public Vec3 getLocalOffset(BlockState state) {
			int yPos = state.getValue(ArmBlock.CEILING) ? 16 - 3 : 3;
			Vec3 location = VecHelper.voxelSpace(8, yPos, 15.5);
			location = VecHelper.rotateCentered(location, AngleHelper.horizontalAngle(getSide()), Direction.Axis.Y);
			return location;
		}

		@Override
		public float getScale() {
			return super.getScale();
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
