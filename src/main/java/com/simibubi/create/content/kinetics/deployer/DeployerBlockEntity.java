package com.simibubi.create.content.kinetics.deployer;

import static com.simibubi.create.content.kinetics.base.DirectionalKineticBlock.FACING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.equipment.sandPaper.SandPaperItem;
import com.simibubi.create.content.kinetics.base.IRotate.StressImpact;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.item.LegacyItemTransferAdapter;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.foundation.utility.LegacyItemStackNbtBridge;
import com.simibubi.create.foundation.utility.LegacyNbtUtilsBridge;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.api.animation.LerpedFloat;
import net.createmod.catnip.api.math.VecHelper;
import net.createmod.catnip.api.nbt.NBTHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Clearable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

public class DeployerBlockEntity extends KineticBlockEntity implements Clearable {
	protected State state;
	protected Mode mode;
	protected ItemStack heldItem;
	protected DeployerFakePlayer player;
	protected int timer;
	protected float reach;
	protected boolean fistBump = false;
	protected List<ItemStack> overflowItems = new ArrayList<>();
	protected FilteringBehaviour filtering;
	protected boolean redstoneLocked;
	protected UUID owner;
	private IItemHandlerModifiable invHandler;
	private CompoundTag deferredInventoryTag;

	private LerpedFloat animatedOffset;

	public BeltProcessingBehaviour processingBehaviour;

	enum State {
		WAITING, EXPANDING, RETRACTING, DUMPING;
	}

	enum Mode {
		PUNCH, USE
	}

	public DeployerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		this.state = State.WAITING;
		mode = Mode.USE;
		heldItem = ItemStack.EMPTY;
		redstoneLocked = false;
		animatedOffset = LerpedFloat.linear()
			.startWithValue(0);
	}

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(
				Capabilities.Item.BLOCK,
				AllBlockEntityTypes.DEPLOYER.get(),
				(be, context) ->  {
					if (be.invHandler == null)
						be.initHandler();
					return new LegacyItemTransferAdapter(be.invHandler);
				}
		);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		filtering = new FilteringBehaviour(this, new DeployerFilterSlot());
		behaviours.add(filtering);
		processingBehaviour =
			new BeltProcessingBehaviour(this).whenItemEnters((s, i) -> BeltDeployerCallbacks.onItemReceived(s, i, this))
				.whileItemHeld((s, i) -> BeltDeployerCallbacks.whenItemHeld(s, i, this));
		behaviours.add(processingBehaviour);

		registerAwardables(behaviours, AllAdvancements.TRAIN_CASING, AllAdvancements.ANDESITE_CASING,
			AllAdvancements.BRASS_CASING, AllAdvancements.COPPER_CASING, AllAdvancements.FIST_BUMP,
			AllAdvancements.DEPLOYER, AllAdvancements.SELF_DEPLOYING);
	}

	@Override
	public void initialize() {
		super.initialize();
		initHandler();
	}

	private void initHandler() {
		if (invHandler != null)
			return;
		if (level instanceof ServerLevel sLevel) {
			player = new DeployerFakePlayer(sLevel, owner);
			if (deferredInventoryTag != null) {
				player.getInventory()
					.load(TagValueInput.create(ProblemReporter.DISCARDING, sLevel.registryAccess(), deferredInventoryTag)
						.listOrEmpty("Inventory", ItemStackWithSlot.CODEC));
				deferredInventoryTag = null;
				heldItem = player.getMainHandItem();
				sendData();
			}
			Vec3 initialPos = VecHelper.getCenterOf(worldPosition.relative(getBlockState().getValue(FACING)));
			player.setPos(initialPos.x, initialPos.y, initialPos.z);
		}
		invHandler = createHandler();
	}

	protected void onExtract(ItemStack stack) {
		player.setItemInHand(InteractionHand.MAIN_HAND, stack.copy());
		sendData();
		setChanged();
	}

	protected int getTimerSpeed() {
		return (int) (getSpeed() == 0 ? 0 : Mth.clamp(Math.abs(getSpeed() * 2), 8, 512));
	}

	@Override
	public void tick() {
		super.tick();

		if (getSpeed() == 0)
			return;
		if (!level.isClientSide() && player != null && player.blockBreakingProgress != null) {
			if (level.isEmptyBlock(player.blockBreakingProgress.getKey())) {
				level.destroyBlockProgress(player.getId(), player.blockBreakingProgress.getKey(), -1);
				player.blockBreakingProgress = null;
			}
		}
		if (timer > 0) {
			timer -= getTimerSpeed();
			return;
		}
		if (level.isClientSide())
			return;
		if (player == null)
			return;

		ItemStack stack = player.getMainHandItem();
		if (state == State.WAITING) {
			if (!overflowItems.isEmpty()) {
				timer = getTimerSpeed() * 10;
				return;
			}

			boolean changed = false;
			Inventory inventory = player.getInventory();
			for (int i = 0; i < inventory.getContainerSize(); i++) {
				if (overflowItems.size() > 10)
					break;
				ItemStack item = inventory.getItem(i);
				if (item.isEmpty())
					continue;
				if (item != stack || !filtering.test(item)) {
					overflowItems.add(item);
					inventory.setItem(i, ItemStack.EMPTY);
					changed = true;
				}
			}

			if (changed) {
				sendData();
				timer = getTimerSpeed() * 10;
				return;
			}

			Direction facing = getBlockState().getValue(FACING);
			if (mode == Mode.USE
				&& !DeployerHandler.shouldActivate(stack, level, worldPosition.relative(facing, 2), facing)) {
				timer = getTimerSpeed() * 10;
				return;
			}

			// Check for advancement conditions
			if (mode == Mode.PUNCH && !fistBump && startFistBump(facing))
				return;
			if (redstoneLocked)
				return;

			start();
			return;
		}

		if (state == State.EXPANDING) {
			if (fistBump)
				triggerFistBump();
			activate();

			state = State.RETRACTING;
			timer = 1000;
			sendData();
			return;
		}

		if (state == State.RETRACTING) {
			state = State.WAITING;
			timer = 500;
			sendData();
			return;
		}

	}

	protected void start() {
		state = State.EXPANDING;
		Vec3 movementVector = getMovementVector();
		Vec3 rayOrigin = VecHelper.getCenterOf(worldPosition)
			.add(movementVector.scale(3 / 2f));
		Vec3 rayTarget = VecHelper.getCenterOf(worldPosition)
			.add(movementVector.scale(5 / 2f));
		ClipContext rayTraceContext = new ClipContext(rayOrigin, rayTarget, Block.OUTLINE, Fluid.NONE, player);
		BlockHitResult result = level.clip(rayTraceContext);
		reach = (float) (.5f + Math.min(result.getLocation()
			.subtract(rayOrigin)
			.length(), .75f));
		timer = 1000;
		sendData();
	}

	public boolean startFistBump(Direction facing) {
		int i = 0;
		DeployerBlockEntity partner = null;

		for (i = 2; i < 5; i++) {
			BlockPos otherDeployer = worldPosition.relative(facing, i);
			if (!level.isLoaded(otherDeployer))
				return false;
			BlockEntity other = level.getBlockEntity(otherDeployer);
			if (other instanceof DeployerBlockEntity dpe) {
				partner = dpe;
				break;
			}
		}

		if (partner == null)
			return false;

		if (level.getBlockState(partner.getBlockPos())
			.getValue(FACING)
			.getOpposite() != facing || partner.mode != Mode.PUNCH)
			return false;
		if (partner.getSpeed() == 0)
			return false;

		for (DeployerBlockEntity be : Arrays.asList(this, partner)) {
			be.fistBump = true;
			be.reach = ((i - 2)) * .5f;
			be.timer = 1000;
			be.state = State.EXPANDING;
			be.sendData();
		}

		return true;
	}

	public void triggerFistBump() {
		int i = 0;
		DeployerBlockEntity deployerBlockEntity = null;
		for (i = 2; i < 5; i++) {
			BlockPos pos = worldPosition.relative(getBlockState().getValue(FACING), i);
			if (!level.isLoaded(pos))
				return;
			if (level.getBlockEntity(pos) instanceof DeployerBlockEntity dpe) {
				deployerBlockEntity = dpe;
				break;
			}
		}

		if (deployerBlockEntity == null)
			return;
		if (!deployerBlockEntity.fistBump || deployerBlockEntity.state != State.EXPANDING)
			return;
		if (deployerBlockEntity.timer > 0)
			return;

		fistBump = false;
		deployerBlockEntity.fistBump = false;
		deployerBlockEntity.state = State.RETRACTING;
		deployerBlockEntity.timer = 1000;
		deployerBlockEntity.sendData();
		award(AllAdvancements.FIST_BUMP);

		BlockPos soundLocation = BlockPos.containing(Vec3.atCenterOf(worldPosition)
			.add(Vec3.atCenterOf(deployerBlockEntity.getBlockPos()))
			.scale(.5f));
		level.playSound(null, soundLocation, SoundEvents.PLAYER_ATTACK_NODAMAGE, SoundSource.BLOCKS, .75f, .75f);
	}

	protected void activate() {
		Vec3 movementVector = getMovementVector();
		Direction direction = getBlockState().getValue(FACING);
		Vec3 center = VecHelper.getCenterOf(worldPosition);
		BlockPos clickedPos = worldPosition.relative(direction, 2);
		player.setXRot(direction == Direction.UP ? -90 : direction == Direction.DOWN ? 90 : 0);
		player.setYRot(direction.toYRot());

		if (direction == Direction.DOWN
			&& BlockEntityBehaviour.get(level, clickedPos, TransportedItemStackHandlerBehaviour.TYPE) != null)
			return; // Belt processing handled in BeltDeployerCallbacks

		DeployerHandler.activate(player, center, clickedPos, movementVector, mode);
		award(AllAdvancements.DEPLOYER);

		if (player != null) {
			int count = heldItem.getCount();
			heldItem = player.getMainHandItem();
			if (count != heldItem.getCount())
				setChanged();
		}
	}

	protected Vec3 getMovementVector() {
		if (!AllBlocks.DEPLOYER.has(getBlockState()))
			return Vec3.ZERO;
		return Vec3.atLowerCornerOf(getBlockState().getValue(FACING)
			.getUnitVec3i());
	}

	@Override
	protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
		state = NBTHelper.readEnum(compound, "State", State.class);
		mode = NBTHelper.readEnum(compound, "Mode", Mode.class);
		timer = compound.getIntOr("Timer", 0);
		redstoneLocked = compound.getBooleanOr("Powered", false);
		if (compound.contains("Owner"))
			owner = LegacyNbtUtilsBridge.loadUUID(compound.get("Owner"));

		deferredInventoryTag = new CompoundTag();
		deferredInventoryTag.put("Inventory", compound.getListOrEmpty("Inventory"));
		overflowItems = NBTHelper.readItemList(compound.getListOrEmpty("Overflow"), registries);
		if (compound.contains("HeldItem"))
			heldItem = LegacyItemStackNbtBridge.parseOptional(registries, compound.getCompoundOrEmpty("HeldItem"));
		super.read(compound, registries, clientPacket);

		if (!clientPacket)
			return;
		fistBump = compound.getBooleanOr("Fistbump", false);
		reach = compound.getFloatOr("Reach", 0);
		if (compound.contains("Particle")) {
			ItemStack particleStack =
				LegacyItemStackNbtBridge.parseOptional(registries, compound.getCompoundOrEmpty("Particle"));
			SandPaperItem.spawnParticles(VecHelper.getCenterOf(worldPosition)
				.add(getMovementVector().scale(reach + 1)), particleStack, this.level);
		}
	}

	@Override
	public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
		NBTHelper.writeEnum(compound, "Mode", mode);
		NBTHelper.writeEnum(compound, "State", state);
		compound.putInt("Timer", timer);
		compound.putBoolean("Powered", redstoneLocked);
		if (owner != null)
			compound.put("Owner", LegacyNbtUtilsBridge.createUUID(owner));

		if (player != null) {
			compound.put("Inventory", savePlayerInventory(registries).getListOrEmpty("Inventory"));
			compound.put("HeldItem", LegacyItemStackNbtBridge.saveOptional(player.getMainHandItem(), registries));
			compound.put("Overflow", NBTHelper.writeItemList(overflowItems, registries));
		} else if (deferredInventoryTag != null) {
			compound.put("Inventory", deferredInventoryTag.getListOrEmpty("Inventory"));
		}

		super.write(compound, registries, clientPacket);

		if (!clientPacket)
			return;
		compound.putBoolean("Fistbump", fistBump);
		compound.putFloat("Reach", reach);
		if (player == null)
			return;
		compound.put("HeldItem", LegacyItemStackNbtBridge.saveOptional(player.getMainHandItem(), registries));
		if (player.spawnedItemEffects != null) {
			compound.put("Particle", LegacyItemStackNbtBridge.saveOptional(player.spawnedItemEffects, registries));
			player.spawnedItemEffects = null;
		}
	}

	private CompoundTag savePlayerInventory(HolderLookup.Provider registries) {
		TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
		player.getInventory()
			.save(output.list("Inventory", ItemStackWithSlot.CODEC));
		return output.buildResult();
	}

	@Override
	public void writeSafe(CompoundTag tag, HolderLookup.Provider registries) {
		NBTHelper.writeEnum(tag, "Mode", mode);
		super.writeSafe(tag, registries);
	}

	private IItemHandlerModifiable createHandler() {
		return new DeployerItemHandler(this);
	}

	public void redstoneUpdate() {
		if (level.isClientSide())
			return;
		boolean blockPowered = level.hasNeighborSignal(worldPosition);
		if (blockPowered == redstoneLocked)
			return;
		redstoneLocked = blockPowered;
		sendData();
	}

	public PartialModel getHandPose() {
		return mode == Mode.PUNCH ? AllPartialModels.DEPLOYER_HAND_PUNCHING
			: heldItem.isEmpty() ? AllPartialModels.DEPLOYER_HAND_POINTING : AllPartialModels.DEPLOYER_HAND_HOLDING;
	}

	@Override
	protected AABB createRenderBoundingBox() {
		return super.createRenderBoundingBox().inflate(3);
	}

	public void discardPlayer() {
		if (player == null)
			return;
		player.getInventory()
			.dropAll();
		overflowItems.forEach(itemstack -> player.drop(itemstack, true, false));
		player.discard();
		player = null;
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (invHandler != null)
			invalidateCapabilities();
	}

	@Override
	public void clearContent() {
		filtering.setFilter(ItemStack.EMPTY);
	}

	public void changeMode() {
		mode = mode == Mode.PUNCH ? Mode.USE : Mode.PUNCH;
		setChanged();
		sendData();
	}

	@Override
	public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		if (super.addToTooltip(tooltip, isPlayerSneaking))
			return true;
		if (getSpeed() == 0)
			return false;
		if (overflowItems.isEmpty())
			return false;
		TooltipHelper.addHint(tooltip, "hint.full_deployer");
		return true;
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		CreateLang.translate("tooltip.deployer.header")
			.forGoggles(tooltip);

		CreateLang.translate("tooltip.deployer." + (mode == Mode.USE ? "using" : "punching"))
			.style(ChatFormatting.YELLOW)
			.forGoggles(tooltip);

		if (!heldItem.isEmpty())
			CreateLang.translate("tooltip.deployer.contains", heldItem.getHoverName()
					.getString(), heldItem.getCount())
				.style(ChatFormatting.GREEN)
				.forGoggles(tooltip);

		float stressAtBase = calculateStressApplied();
		if (StressImpact.isEnabled() && !Mth.equal(stressAtBase, 0)) {
			tooltip.add(CommonComponents.EMPTY);
			addStressImpactStats(tooltip, stressAtBase);
		}

		return true;
	}

	public float getHandOffset(float partialTicks) {
		if (isVirtual())
			return animatedOffset.getValue(partialTicks);

		float progress = 0;
		int timerSpeed = getTimerSpeed();
		PartialModel handPose = getHandPose();

		if (state == State.EXPANDING) {
			progress = 1 - (timer - partialTicks * timerSpeed) / 1000f;
			if (fistBump)
				progress *= progress;
		}
		if (state == State.RETRACTING)
			progress = (timer - partialTicks * timerSpeed) / 1000f;
		float handLength = handPose == AllPartialModels.DEPLOYER_HAND_POINTING ? 0
			: handPose == AllPartialModels.DEPLOYER_HAND_HOLDING ? 4 / 16f : 3 / 16f;
		float distance = Math.min(Mth.clamp(progress, 0, 1) * (reach + handLength), 21 / 16f);

		return distance;
	}

	public void setAnimatedOffset(float offset) {
		animatedOffset.setValue(offset);
	}

	ItemStackHandler recipeInv = new ItemStackHandler(2);

	@Nullable
	public RecipeHolder<? extends Recipe<? extends RecipeInput>> getRecipe(ItemStack stack) {
		if (player == null || level == null)
			return null;

		ItemStack heldItemMainhand = player.getMainHandItem();
		if (heldItemMainhand.getItem() instanceof SandPaperItem) {
			Optional<RecipeHolder<Recipe<RecipeInput>>> polishingRecipe = checkRecipe(AllRecipeTypes.SANDPAPER_POLISHING, new SingleRecipeInput(stack), level);
			if (polishingRecipe.isPresent()) {
				return polishingRecipe.get();
			}
		}

		recipeInv.setStackInSlot(0, stack);
		recipeInv.setStackInSlot(1, heldItemMainhand);

		DeployerRecipeSearchEvent event = new DeployerRecipeSearchEvent(this, new RecipeWrapper(recipeInv));

		event.addRecipe(() -> SequencedAssemblyRecipe.getRecipe(level, event.getInventory(),
			AllRecipeTypes.DEPLOYING.getType(), DeployerApplicationRecipe.class), 100);
		event.addRecipe(() -> checkRecipe(AllRecipeTypes.DEPLOYING, event.getInventory(), level), 50);
		event.addRecipe(() -> checkRecipe(AllRecipeTypes.ITEM_APPLICATION, event.getInventory(), level), 50);

		NeoForge.EVENT_BUS.post(event);
		return event.getRecipe();
	}

	private Optional<RecipeHolder<Recipe<RecipeInput>>> checkRecipe(AllRecipeTypes type, RecipeInput inv, Level level) {
		return type.find(inv, level).filter(AllRecipeTypes.CAN_BE_AUTOMATED);
	}

	public DeployerFakePlayer getPlayer() {
		return player;
	}
}
