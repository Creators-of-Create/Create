package com.simibubi.create.content.contraptions.components.deployer;

import static com.simibubi.create.content.contraptions.base.DirectionalKineticBlock.FACING;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.core.PartialModel;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.itemAssembly.SequencedAssemblyRecipe;
import com.simibubi.create.content.curiosities.tools.SandPaperItem;
import com.simibubi.create.content.curiosities.tools.SandPaperPolishingRecipe.SandPaperInv;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;

import com.simibubi.create.lib.transfer.item.IItemHandler;
import com.simibubi.create.lib.transfer.item.ItemTransferable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import com.simibubi.create.lib.transfer.item.IItemHandlerModifiable;
import com.simibubi.create.lib.transfer.item.ItemStackHandler;
import com.simibubi.create.lib.transfer.item.RecipeWrapper;
import com.simibubi.create.lib.utility.NBT;
import com.simibubi.create.lib.utility.LazyOptional;
import com.simibubi.create.lib.utility.NBTSerializer;

public class DeployerTileEntity extends KineticTileEntity implements ItemTransferable {

	protected State state;
	protected Mode mode;
	protected ItemStack heldItem = ItemStack.EMPTY;
	protected DeployerFakePlayer player;
	protected int timer;
	protected float reach;
	protected boolean boop = false;
	protected List<ItemStack> overflowItems = new ArrayList<>();
	protected FilteringBehaviour filtering;
	protected boolean redstoneLocked;
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

	public DeployerTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		this.state = State.WAITING;
		mode = Mode.USE;
		heldItem = ItemStack.EMPTY;
		redstoneLocked = false;
		animatedOffset = LerpedFloat.linear()
			.startWithValue(0);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		filtering = new FilteringBehaviour(this, new DeployerFilterSlot());
		behaviours.add(filtering);
		processingBehaviour =
			new BeltProcessingBehaviour(this).whenItemEnters((s, i) -> BeltDeployerCallbacks.onItemReceived(s, i, this))
				.whileItemHeld((s, i) -> BeltDeployerCallbacks.whenItemHeld(s, i, this));
		behaviours.add(processingBehaviour);
	}

	@Override
	public void initialize() {
		super.initialize();
		initHandler();
	}

	private void initHandler() {
		if (invHandler != null)
			return;
		if (!level.isClientSide) {
			player = new DeployerFakePlayer((ServerLevel) level);
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
			if (mode == Mode.PUNCH && !boop && startBoop(facing))
				return;
			if (redstoneLocked)
				return;

			start();
			return;
		}

		if (state == State.EXPANDING) {
			if (boop)
				triggerBoop();
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

	public boolean startBoop(Direction facing) {
		if (!level.isEmptyBlock(worldPosition.relative(facing, 1))
			|| !level.isEmptyBlock(worldPosition.relative(facing, 2)))
			return false;
		BlockPos otherDeployer = worldPosition.relative(facing, 4);
		if (!level.isLoaded(otherDeployer))
			return false;
		BlockEntity otherTile = level.getBlockEntity(otherDeployer);
		if (!(otherTile instanceof DeployerTileEntity))
			return false;
		DeployerTileEntity deployerTile = (DeployerTileEntity) otherTile;
		if (level.getBlockState(otherDeployer)
			.getValue(FACING)
			.getOpposite() != facing || deployerTile.mode != Mode.PUNCH)
			return false;

		boop = true;
		reach = 1f;
		timer = 1000;
		state = State.EXPANDING;
		sendData();
		return true;
	}

	public void triggerBoop() {
		BlockEntity otherTile = level.getBlockEntity(worldPosition.relative(getBlockState().getValue(FACING), 4));
		if (!(otherTile instanceof DeployerTileEntity))
			return;

		DeployerTileEntity deployerTile = (DeployerTileEntity) otherTile;
		if (!deployerTile.boop || deployerTile.state != State.EXPANDING)
			return;
		if (deployerTile.timer > 0)
			return;

		// everything should be met
		boop = false;
		deployerTile.boop = false;
		deployerTile.state = State.RETRACTING;
		deployerTile.timer = 1000;
		deployerTile.sendData();

		// award nearby players
		List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class, new AABB(worldPosition).inflate(9));
		players.forEach(AllTriggers.DEPLOYER_BOOP::trigger);
	}

	protected void activate() {
		Vec3 movementVector = getMovementVector();
		Direction direction = getBlockState().getValue(FACING);
		Vec3 center = VecHelper.getCenterOf(worldPosition);
		BlockPos clickedPos = worldPosition.relative(direction, 2);
		player.setXRot(direction == Direction.UP ? -90 : direction == Direction.DOWN ? 90 : 0);
		player.setYRot(direction.toYRot());

		if (direction == Direction.DOWN
			&& TileEntityBehaviour.get(level, clickedPos, TransportedItemStackHandlerBehaviour.TYPE) != null)
			return; // Belt processing handled in BeltDeployerCallbacks

		DeployerHandler.activate(player, center, clickedPos, movementVector, mode);
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
	protected void fromTag(CompoundTag compound, boolean clientPacket) {
		state = NBTHelper.readEnum(compound, "State", State.class);
		mode = NBTHelper.readEnum(compound, "Mode", Mode.class);
		timer = compound.getInt("Timer");
		redstoneLocked = compound.getBoolean("Powered");

		deferredInventoryList = compound.getList("Inventory", Tag.TAG_COMPOUND);
		overflowItems = NBTHelper.readItemList(compound.getList("Overflow", Tag.TAG_COMPOUND));
		if (compound.contains("HeldItem"))
			heldItem = ItemStack.of(compound.getCompound("HeldItem"));
		super.fromTag(compound, clientPacket);

		if (!clientPacket)
			return;
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

		if (player != null) {
			ListTag invNBT = new ListTag();
			player.getInventory()
				.save(invNBT);
			compound.put("Inventory", invNBT);
			compound.put("HeldItem", NBTSerializer.serializeNBT(player.getMainHandItem()));
			compound.put("Overflow", NBTHelper.writeItemList(overflowItems));
		} else if (deferredInventoryList != null) {
			compound.put("Inventory", deferredInventoryList);
		}

		super.write(compound, clientPacket);

		if (!clientPacket)
			return;
		compound.putFloat("Reach", reach);
		if (player == null)
			return;
		compound.put("HeldItem", NBTSerializer.serializeNBT(player.getMainHandItem()));
		if (player.spawnedItemEffects != null) {
			compound.put("Particle", NBTSerializer.serializeNBT(player.spawnedItemEffects));
			player.spawnedItemEffects = null;
		}
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

	public PartialModel getHandPose() {
		return mode == Mode.PUNCH ? AllBlockPartials.DEPLOYER_HAND_PUNCHING
			: heldItem.isEmpty() ? AllBlockPartials.DEPLOYER_HAND_POINTING : AllBlockPartials.DEPLOYER_HAND_HOLDING;
	}

	@Override
	public AABB makeRenderBoundingBox() {
		return super.makeRenderBoundingBox().inflate(3);
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		if (invHandler != null)
			invHandler.invalidate();
	}

	public void changeMode() {
		mode = mode == Mode.PUNCH ? Mode.USE : Mode.PUNCH;
		setChanged();
		sendData();
	}

	@Nullable
	@Override
	public IItemHandler getItemHandler(@Nullable Direction direction) {
		if(invHandler == null)
			initHandler();
		return invHandler == null ? null : invHandler.orElse(null);
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
	public boolean shouldRenderNormally() {
		return true;
	}

	public float getHandOffset(float partialTicks) {
		if (isVirtual())
			return animatedOffset.getValue(partialTicks);

		float progress = 0;
		int timerSpeed = getTimerSpeed();
		PartialModel handPose = getHandPose();

		if (state == State.EXPANDING)
			progress = 1 - (timer - partialTicks * timerSpeed) / 1000f;
		if (state == State.RETRACTING)
			progress = (timer - partialTicks * timerSpeed) / 1000f;
		float handLength = handPose == AllBlockPartials.DEPLOYER_HAND_POINTING ? 0
			: handPose == AllBlockPartials.DEPLOYER_HAND_HOLDING ? 4 / 16f : 3 / 16f;
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
		// safety checks
		if (player == null || level == null)
			return null;

		// sandpaper = op
		ItemStack heldItemMainhand = player.getMainHandItem();
		if (heldItemMainhand.getItem() instanceof SandPaperItem) {
			sandpaperInv.setItem(0, stack);
			return AllRecipeTypes.SANDPAPER_POLISHING.find(sandpaperInv, level)
				.orElse(null);
		}

		// inventory
		recipeInv.setItem(0, stack);
		recipeInv.setItem(1, heldItemMainhand);

		// event nonsense
		DeployerRecipeSearchEvent event = new DeployerRecipeSearchEvent(this, recipeInv);

		// creates deployer recipes
		event.addRecipe(() -> SequencedAssemblyRecipe.getRecipe(level, event.getInventory(),
			AllRecipeTypes.DEPLOYING.getType(), DeployerApplicationRecipe.class), 100);
		event.addRecipe(() -> AllRecipeTypes.DEPLOYING.find(event.getInventory(), level), 50);

		// post the event, get result
		DeployerRecipeSearchEvent.EVENT.invoker().handle(event);
		return event.getRecipe();
	}

	public DeployerFakePlayer getPlayer() {
		return player;
	}
}
