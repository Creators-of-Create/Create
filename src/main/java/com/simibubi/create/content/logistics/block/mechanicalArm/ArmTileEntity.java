package com.simibubi.create.content.logistics.block.mechanicalArm;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmInteractionPoint.Jukebox;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmInteractionPoint.Mode;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widgets.InterpolatedAngle;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.INamedIconOptions;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ArmTileEntity extends KineticTileEntity {

	// Server
	List<ArmInteractionPoint> inputs;
	List<ArmInteractionPoint> outputs;
	ListNBT interactionPointTag;

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

	enum Phase {
		SEARCH_INPUTS, MOVE_TO_INPUT, SEARCH_OUTPUTS, MOVE_TO_OUTPUT, DANCING
	}

	public ArmTileEntity(TileEntityType<?> typeIn) {
		super(typeIn);
		inputs = new ArrayList<>();
		outputs = new ArrayList<>();
		heldItem = ItemStack.EMPTY;
		phase = Phase.SEARCH_INPUTS;
		baseAngle = new InterpolatedAngle();
		lowerArmAngle = new InterpolatedAngle();
		upperArmAngle = new InterpolatedAngle();
		headAngle = new InterpolatedAngle();
		clawAngle = new InterpolatedAngle();
		previousTarget = ArmAngleTarget.NO_TARGET;
		previousBaseAngle = previousTarget.baseAngle;
		updateInteractionPoints = true;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);

		selectionMode = new ScrollOptionBehaviour<>(SelectionMode.class, Lang.translate("mechanical_arm.selection_mode"), this,
				new CenteredSideValueBoxTransform((blockState, direction) -> direction != Direction.DOWN && direction != Direction.UP) {
					@Override
					protected Vec3d getLocalOffset(BlockState state) {
						int yPos = state.get(ArmBlock.CEILING) ? 16 - 3 : 3;
						Vec3d location = VecHelper.voxelSpace(8, yPos, 14.5);
						location = VecHelper.rotateCentered(location, AngleHelper.horizontalAngle(getSide()), Direction.Axis.Y);
						return location;
					}

					@Override
					protected float getScale() {
						return .3f;
					}
				});
		selectionMode.requiresWrench();
		behaviours.add(selectionMode);
	}

	@Override
	public void tick() {
		super.tick();
		initInteractionPoints();
		tickMovementProgress();

		if (world.isRemote)
			return;
		if (chasedPointProgress < 1)
			return;
		if (phase == Phase.MOVE_TO_INPUT)
			collectItem();
		if (phase == Phase.MOVE_TO_OUTPUT)
			depositItem();
	}

	@Override
	public void lazyTick() {
		super.lazyTick();

		if (world.isRemote)
			return;
		if (chasedPointProgress < .5f)
			return;
		if (phase == Phase.SEARCH_INPUTS || phase == Phase.DANCING) {
			checkForMusic();
			searchForItem();
		}
		if (phase == Phase.SEARCH_OUTPUTS)
			searchForDestination();
	}

	private void checkForMusic() {
		boolean hasMusic = checkForMusicAmong(inputs) || checkForMusicAmong(outputs);
		if (hasMusic != (phase == Phase.DANCING)) {
			phase = hasMusic ? Phase.DANCING : Phase.SEARCH_INPUTS;
			markDirty();
			sendData();
		}
	}

	private boolean checkForMusicAmong(List<ArmInteractionPoint> list) {
		for (ArmInteractionPoint armInteractionPoint : list) {
			if (!(armInteractionPoint instanceof Jukebox))
				continue;
			BlockState state = world.getBlockState(armInteractionPoint.pos);
			if (state.has(JukeboxBlock.HAS_RECORD) && state.get(JukeboxBlock.HAS_RECORD))
				return true;
		}
		return false;
	}

	private void tickMovementProgress() {
		chasedPointProgress += Math.min(256, Math.abs(getSpeed())) / 1024f;
		if (chasedPointProgress > 1)
			chasedPointProgress = 1;
		if (!world.isRemote)
			return;

		ArmInteractionPoint targetedInteractionPoint = getTargetedInteractionPoint();
		ArmAngleTarget previousTarget = this.previousTarget;
		ArmAngleTarget target = targetedInteractionPoint == null ? ArmAngleTarget.NO_TARGET
			: targetedInteractionPoint.getTargetAngles(pos, isOnCeiling());

		baseAngle.set(AngleHelper.angleLerp(chasedPointProgress, previousBaseAngle,
			target == ArmAngleTarget.NO_TARGET ? previousBaseAngle : target.baseAngle));

		// Arm's angles first backup to resting position and then continue
		if (chasedPointProgress < .5f)
			target = ArmAngleTarget.NO_TARGET;
		else
			previousTarget = ArmAngleTarget.NO_TARGET;
		float progress = chasedPointProgress == 1 ? 1 : (chasedPointProgress % .5f) * 2;

		lowerArmAngle.set(MathHelper.lerp(progress, previousTarget.lowerArmAngle, target.lowerArmAngle));
		upperArmAngle.set(MathHelper.lerp(progress, previousTarget.upperArmAngle, target.upperArmAngle));
		
		headAngle.set(AngleHelper.angleLerp(progress, previousTarget.headAngle % 360, target.headAngle % 360));
	}

	protected boolean isOnCeiling() {
		BlockState state = getBlockState();
		return hasWorld() && state != null && state.has(ArmBlock.CEILING) && state.get(ArmBlock.CEILING);
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
		boolean foundInput = false;
		//for round robin, we start looking after the last used index, for default we start at 0;
		int startIndex = selectionMode.get() == SelectionMode.DEFAULT ? 0 : lastInputIndex + 1;

		//if we enforce round robin, only look at the next input in the list, otherwise, look at all inputs
		int scanRange = selectionMode.get() == SelectionMode.ROUND_ROBIN_HARD ? lastInputIndex + 2 : inputs.size();
		if (scanRange > inputs.size())
			scanRange = inputs.size();

		InteractionPoints: for (int i = startIndex; i < scanRange; i++) {
			ArmInteractionPoint armInteractionPoint = inputs.get(i);
			for (int j = 0; j < armInteractionPoint.getSlotCount(world); j++) {
				if (getDistributableAmount(armInteractionPoint, j) == 0)
					continue;

				selectIndex(true, i);
				foundInput = true;
				break InteractionPoints;
			}
		}
		if (!foundInput && selectionMode.get() == SelectionMode.ROUND_ROBIN_SOFT) {
			//if we didn't find an input, but don't want to enforce round robin, reset the last index
			lastInputIndex = -1;
		}
		if (lastInputIndex == inputs.size() - 1) {
			//if we reached the last input in the list, reset the last index
			lastInputIndex = -1;
		}
	}

	protected void searchForDestination() {
		ItemStack held = heldItem.copy();

		boolean foundOutput = false;
		//for round robin, we start looking after the last used index, for default we start at 0;
		int startIndex = selectionMode.get() == SelectionMode.DEFAULT ? 0 : lastOutputIndex + 1;

		//if we enforce round robin, only look at the next index in the list, otherwise, look at all
		int scanRange = selectionMode.get() == SelectionMode.ROUND_ROBIN_HARD ? lastOutputIndex + 2 : outputs.size();
		if (scanRange > outputs.size())
			scanRange = outputs.size();

		for (int i = startIndex; i < scanRange; i++) {
			ArmInteractionPoint armInteractionPoint = outputs.get(i);
			ItemStack remainder = armInteractionPoint.insert(world, held, true);
			if (remainder.equals(heldItem, false))
				continue;

			selectIndex(false, i);
			foundOutput = true;
			break;
		}

		if (!foundOutput && selectionMode.get() == SelectionMode.ROUND_ROBIN_SOFT) {
			//if we didn't find an input, but don't want to enforce round robin, reset the last index
			lastOutputIndex = -1;
		}
		if (lastOutputIndex == outputs.size() - 1) {
			//if we reached the last input in the list, reset the last index
			lastOutputIndex = -1;
		}
	}

	//input == true => select input, false => select output
	private void selectIndex(boolean input, int index) {
		phase = input ? Phase.MOVE_TO_INPUT : Phase.MOVE_TO_OUTPUT;
		chasedPointIndex = index;
		chasedPointProgress = 0;
		if (input)
			lastInputIndex = index;
		else
			lastOutputIndex = index;
		sendData();
		markDirty();
	}

	protected int getDistributableAmount(ArmInteractionPoint armInteractionPoint, int i) {
		ItemStack stack = armInteractionPoint.extract(world, i, true);
		ItemStack remainder = simulateInsertion(stack);
		return stack.getCount() - remainder.getCount();
	}

	protected void depositItem() {
		ArmInteractionPoint armInteractionPoint = getTargetedInteractionPoint();
		if (armInteractionPoint != null) {
			ItemStack toInsert = heldItem.copy();
			ItemStack remainder = armInteractionPoint.insert(world, toInsert, false);
			heldItem = remainder;
		}
		phase = heldItem.isEmpty() ? Phase.SEARCH_INPUTS : Phase.SEARCH_OUTPUTS;
		chasedPointProgress = 0;
		chasedPointIndex = -1;
		sendData();
		markDirty();

		if (!world.isRemote)
			AllTriggers.triggerForNearbyPlayers(AllTriggers.MECHANICAL_ARM, world, pos, 10);
	}

	protected void collectItem() {
		ArmInteractionPoint armInteractionPoint = getTargetedInteractionPoint();
		if (armInteractionPoint != null)
			for (int i = 0; i < armInteractionPoint.getSlotCount(world); i++) {
				int amountExtracted = getDistributableAmount(armInteractionPoint, i);
				if (amountExtracted == 0)
					continue;

				heldItem = armInteractionPoint.extract(world, i, amountExtracted, false);
				phase = Phase.SEARCH_OUTPUTS;
				chasedPointProgress = 0;
				chasedPointIndex = -1;
				sendData();
				markDirty();
				return;
			}

		phase = Phase.SEARCH_INPUTS;
		chasedPointProgress = 0;
		chasedPointIndex = -1;
		sendData();
		markDirty();
	}

	private ItemStack simulateInsertion(ItemStack stack) {
		for (ArmInteractionPoint armInteractionPoint : outputs) {
			stack = armInteractionPoint.insert(world, stack, true);
			if (stack.isEmpty())
				break;
		}
		return stack;
	}

	protected void initInteractionPoints() {
		if (!updateInteractionPoints || interactionPointTag == null)
			return;
		inputs.clear();
		outputs.clear();
		for (INBT inbt : interactionPointTag) {
			ArmInteractionPoint point = ArmInteractionPoint.deserialize(world, (CompoundNBT) inbt);
			if (point == null)
				continue;
			if (point.mode == Mode.DEPOSIT)
				outputs.add(point);
			if (point.mode == Mode.TAKE)
				inputs.add(point);
		}
		updateInteractionPoints = false;
		sendData();
		markDirty();
	}

	@Override
	public void write(CompoundNBT compound, boolean clientPacket) {
		super.write(compound, clientPacket);

		ListNBT pointsNBT = new ListNBT();
		inputs.stream()
			.map(ArmInteractionPoint::serialize)
			.forEach(pointsNBT::add);
		outputs.stream()
			.map(ArmInteractionPoint::serialize)
			.forEach(pointsNBT::add);

		NBTHelper.writeEnum(compound, "Phase", phase);
		compound.put("InteractionPoints", pointsNBT);
		compound.put("HeldItem", heldItem.serializeNBT());
		compound.putInt("TargetPointIndex", chasedPointIndex);
		compound.putFloat("MovementProgress", chasedPointProgress);
	}

	@Override
	protected void read(CompoundNBT compound, boolean clientPacket) {
		int previousIndex = chasedPointIndex;
		Phase previousPhase = phase;
		ListNBT interactionPointTagBefore = interactionPointTag;
		
		super.read(compound, clientPacket);
		heldItem = ItemStack.read(compound.getCompound("HeldItem"));
		phase = NBTHelper.readEnum(compound, "Phase", Phase.class);
		chasedPointIndex = compound.getInt("TargetPointIndex");
		chasedPointProgress = compound.getFloat("MovementProgress");
		interactionPointTag = compound.getList("InteractionPoints", NBT.TAG_COMPOUND);
		
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
				previousPoint == null ? ArmAngleTarget.NO_TARGET : previousPoint.getTargetAngles(pos, ceiling);
			if (previousPoint != null)
				previousBaseAngle = previousPoint.getTargetAngles(pos, ceiling).baseAngle;
		}
	}

	public enum SelectionMode implements INamedIconOptions {
		DEFAULT(AllIcons.I_TOOL_MIRROR),//first valid interaction points gets used
		ROUND_ROBIN_SOFT(AllIcons.I_TOOL_ROTATE),//attempt round robin, but skip invalid points
		ROUND_ROBIN_HARD(AllIcons.I_TOOL_ROTATE),//enforce round robin, wait for invalid points to be ready again

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
