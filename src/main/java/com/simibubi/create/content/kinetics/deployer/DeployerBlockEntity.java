package com.simibubi.create.content.kinetics.deployer;

import static com.simibubi.create.content.kinetics.base.DirectionalKineticBlock.FACING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.core.PartialModel;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.equipment.sandPaper.SandPaperItem;
import com.simibubi.create.content.equipment.sandPaper.SandPaperPolishingRecipe.SandPaperInv;
import com.simibubi.create.content.kinetics.base.IRotate.StressImpact;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class DeployerBlockEntity extends KineticBlockEntity {

	protected State state;
	protected Mode mode;
	protected ItemStack heldItem = ItemStack.EMPTY;
	protected DeployerFakePlayer player;
	protected int timer;
	protected float reach;
	protected boolean fistBump = false;
	protected List<ItemStack> overflowItems = new ArrayList<>();
	protected FilteringBehaviour filtering;
	protected boolean redstoneLocked;
	protected UUID owner;
	private LazyOptional<IItemHandlerModifiable> invHandler;
	private ListTag deferredInventoryList;

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
			if (deferredInventoryList != null) {
				player.getInventory()
					.load(deferredInventoryList);
				deferredInventoryList = null;
				heldItem = player.getMainHandItem();
				sendData();
			}
			Vec3 initialPos = VecHelper.getCenterOf(worldPosition.relative(getBlockState().getValue(FACING)));
			player.setPos(initialPos.x, initialPos.y, initialPos.z);
		}
		invHandler = LazyOptional.of(this::createHandler);
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
		if (!level.isClientSide && player != null && player.blockBreakingProgress != null) {
			if (level.isEmptyBlock(player.blockBreakingProgress.getKey())) {
				level.destroyBlockProgress(player.getId(), player.blockBreakingProgress.getKey(), -1);
				player.blockBreakingProgress = null;
			}
		}
		if (timer > 0) {
			timer -= getTimerSpeed();
			return;
		}
		if (level.isClientSide)
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

		if (player != null)
			heldItem = player.getMainHandItem();
	}

	protected Vec3 getMovementVector() {
		if (!AllBlocks.DEPLOYER.has(getBlockState()))
			return Vec3.ZERO;
		return Vec3.atLowerCornerOf(getBlockState().getValue(FACING)
			.getNormal());
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		state = NBTHelper.readEnum(compound, "State", State.class);
		mode = NBTHelper.readEnum(compound, "Mode", Mode.class);
		timer = compound.getInt("Timer");
		redstoneLocked = compound.getBoolean("Powered");
		if (compound.contains("Owner"))
			owner = compound.getUUID("Owner");

		deferredInventoryList = compound.getList("Inventory", Tag.TAG_COMPOUND);
		overflowItems = NBTHelper.readItemList(compound.getList("Overflow", Tag.TAG_COMPOUND));
		if (compound.contains("HeldItem"))
			heldItem = ItemStack.of(compound.getCompound("HeldItem"));
		super.read(compound, clientPacket);

		if (!clientPacket)
			return;
		fistBump = compound.getBoolean("Fistbump");
		reach = compound.getFloat("Reach");
		if (compound.contains("Particle")) {
			ItemStack particleStack = ItemStack.of(compound.getCompound("Particle"));
			SandPaperItem.spawnParticles(VecHelper.getCenterOf(worldPosition)
				.add(getMovementVector().scale(reach + 1)), particleStack, this.level);
		}
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		NBTHelper.writeEnum(compound, "Mode", mode);
		NBTHelper.writeEnum(compound, "State", state);
		compound.putInt("Timer", timer);
		compound.putBoolean("Powered", redstoneLocked);
		if (owner != null)
			compound.putUUID("Owner", owner);

		if (player != null) {
			ListTag invNBT = new ListTag();
			player.getInventory()
				.save(invNBT);
			compound.put("Inventory", invNBT);
			compound.put("HeldItem", player.getMainHandItem()
				.serializeNBT());
			compound.put("Overflow", NBTHelper.writeItemList(overflowItems));
		} else if (deferredInventoryList != null) {
			compound.put("Inventory", deferredInventoryList);
		}

		super.write(compound, clientPacket);

		if (!clientPacket)
			return;
		compound.putBoolean("Fistbump", fistBump);
		compound.putFloat("Reach", reach);
		if (player == null)
			return;
		compound.put("HeldItem", player.getMainHandItem()
			.serializeNBT());
		if (player.spawnedItemEffects != null) {
			compound.put("Particle", player.spawnedItemEffects.serializeNBT());
			player.spawnedItemEffects = null;
		}
	}

	@Override
	public void writeSafe(CompoundTag tag) {
		NBTHelper.writeEnum(tag, "Mode", mode);
		super.writeSafe(tag);
	}

	private IItemHandlerModifiable createHandler() {
		return new DeployerItemHandler(this);
	}

	public void redstoneUpdate() {
		if (level.isClientSide)
			return;
		boolean blockPowered = level.hasNeighborSignal(worldPosition);
		if (blockPowered == redstoneLocked)
			return;
		redstoneLocked = blockPowered;
		sendData();
	}

	@OnlyIn(Dist.CLIENT)
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
			invHandler.invalidate();
	}

	public void changeMode() {
		mode = mode == Mode.PUNCH ? Mode.USE : Mode.PUNCH;
		setChanged();
		sendData();
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (isItemHandlerCap(cap)) {
			if (invHandler == null)
				initHandler();
			return invHandler.cast();
		}
		return super.getCapability(cap, side);
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
		Lang.translate("tooltip.deployer.header")
			.forGoggles(tooltip);

		Lang.translate("tooltip.deployer." + (mode == Mode.USE ? "using" : "punching"))
			.style(ChatFormatting.YELLOW)
			.forGoggles(tooltip);

		if (!heldItem.isEmpty())
			Lang.translate("tooltip.deployer.contains", Components.translatable(heldItem.getDescriptionId())
				.getString(), heldItem.getCount())
				.style(ChatFormatting.GREEN)
				.forGoggles(tooltip);

		float stressAtBase = calculateStressApplied();
		if (StressImpact.isEnabled() && !Mth.equal(stressAtBase, 0)) {
			tooltip.add(Components.immutableEmpty());
			addStressImpactStats(tooltip, stressAtBase);
		}

		return true;
	}

	@OnlyIn(Dist.CLIENT)
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

	RecipeWrapper recipeInv = new RecipeWrapper(new ItemStackHandler(2));
	SandPaperInv sandpaperInv = new SandPaperInv(ItemStack.EMPTY);

	@Nullable
	public Recipe<? extends Container> getRecipe(ItemStack stack) {
		if (player == null || level == null)
			return null;

		ItemStack heldItemMainhand = player.getMainHandItem();
		if (heldItemMainhand.getItem() instanceof SandPaperItem) {
			sandpaperInv.setItem(0, stack);
			return checkRecipe(AllRecipeTypes.SANDPAPER_POLISHING, sandpaperInv, level).orElse(null);
		}

		recipeInv.setItem(0, stack);
		recipeInv.setItem(1, heldItemMainhand);

		DeployerRecipeSearchEvent event = new DeployerRecipeSearchEvent(this, recipeInv);

		event.addRecipe(() -> SequencedAssemblyRecipe.getRecipe(level, event.getInventory(),
			AllRecipeTypes.DEPLOYING.getType(), DeployerApplicationRecipe.class), 100);
		event.addRecipe(() -> checkRecipe(AllRecipeTypes.DEPLOYING, event.getInventory(), level), 50);
		event.addRecipe(() -> checkRecipe(AllRecipeTypes.ITEM_APPLICATION, event.getInventory(), level), 50);

		MinecraftForge.EVENT_BUS.post(event);
		return event.getRecipe();
	}

	private Optional<? extends Recipe<? extends Container>> checkRecipe(AllRecipeTypes type, RecipeWrapper inv, Level level) {
		return type.find(inv, level).filter(AllRecipeTypes.CAN_BE_AUTOMATED);
	}

	public DeployerFakePlayer getPlayer() {
		return player;
	}
}
