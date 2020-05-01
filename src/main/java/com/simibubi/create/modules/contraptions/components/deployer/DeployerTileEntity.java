package com.simibubi.create.modules.contraptions.components.deployer;

import static com.simibubi.create.modules.contraptions.base.DirectionalKineticBlock.FACING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.foundation.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.behaviour.inventory.ExtractingBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.curiosities.tools.SandPaperItem;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

public class DeployerTileEntity extends KineticTileEntity {

	private static final List<Pair<BlockPos, Direction>> EXTRACTING_LOCATIONS = Arrays.asList(Direction.values())
			.stream().map(d -> Pair.of(BlockPos.ZERO.offset(d), d.getOpposite())).collect(Collectors.toList());
	private FilteringBehaviour filtering;
	private ExtractingBehaviour extracting;

	protected State state;
	protected Mode mode;
	protected ItemStack heldItem = ItemStack.EMPTY;
	protected DeployerFakePlayer player;
	protected int timer;
	protected float reach;
	protected boolean boop = false;
	protected List<ItemStack> overflowItems = new ArrayList<>();
	private ListNBT deferredInventoryList;
	private LazyOptional<IItemHandlerModifiable> invHandler;

	enum State {
		WAITING, EXPANDING, RETRACTING, DUMPING;
	}

	enum Mode {
		PUNCH, USE
	}

	public DeployerTileEntity() {
		super(AllTileEntities.DEPLOYER.type);
		state = State.WAITING;
		mode = Mode.USE;
		heldItem = ItemStack.EMPTY;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		filtering = new FilteringBehaviour(this, new DeployerFilterSlot());
		extracting = new ExtractingBehaviour(this, this::getExtractingLocations, this::onExtract);

		behaviours.add(filtering);
		behaviours.add(extracting);
	}

	@Override
	public void initialize() {
		super.initialize();
		if (!world.isRemote) {
			player = new DeployerFakePlayer((ServerWorld) world);
			if (deferredInventoryList != null) {
				player.inventory.read(deferredInventoryList);
				deferredInventoryList = null;
				heldItem = player.getHeldItemMainhand();
				sendData();
			}
			Vec3d initialPos = VecHelper.getCenterOf(pos.offset(getBlockState().get(FACING)));
			player.setPosition(initialPos.x, initialPos.y, initialPos.z);
		}
		invHandler = LazyOptional.of(this::createHandler);
	}

	protected void onExtract(ItemStack stack) {
		player.setHeldItem(Hand.MAIN_HAND, stack.copy());
		sendData();
		markDirty();
	}

	protected List<Pair<BlockPos, Direction>> getExtractingLocations() {
		return EXTRACTING_LOCATIONS;
	}

	protected int getTimerSpeed() {
		return (int) (getSpeed() == 0 ? 0 : MathHelper.clamp(Math.abs(getSpeed() * 2), 8, 512));
	}

	@Override
	public void tick() {
		super.tick();

		if (getSpeed() == 0)
			return;
		if (!world.isRemote && player != null && player.blockBreakingProgress != null) {
			if (world.isAirBlock(player.blockBreakingProgress.getKey())) {
				world.sendBlockBreakProgress(player.getEntityId(), player.blockBreakingProgress.getKey(), -1);
				player.blockBreakingProgress = null;
			}
		}
		if (timer > 0) {
			timer -= getTimerSpeed();
			return;
		}
		if (world.isRemote)
			return;

		ItemStack stack = player.getHeldItemMainhand();
		if (state == State.WAITING) {
			if (!overflowItems.isEmpty()) {
				tryDisposeOfItems();
				if (!overflowItems.isEmpty())
					timer = getTimerSpeed() * 10;
				return;
			}

			if (!filtering.test(stack)) {
				if (!stack.isEmpty()) {
					overflowItems.add(stack);
					player.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
					sendData();
					return;
				}
				extracting.extract(1);
				if (!filtering.test(stack))
					timer = getTimerSpeed() * 10;
				return;
			}

			if (filtering.getFilter().isEmpty() && stack.isEmpty())
				extracting.extract(1);

			Direction facing = getBlockState().get(FACING);
			if (mode == Mode.USE && !DeployerHandler.shouldActivate(stack, world, pos.offset(facing, 2))) {
				timer = getTimerSpeed() * 10;
				return;
			}

			// Check for advancement conditions
			if (mode == Mode.PUNCH && !boop && startBoop(facing))
				return;

			state = State.EXPANDING;
			Vec3d movementVector = getMovementVector();
			Vec3d rayOrigin = VecHelper.getCenterOf(pos).add(movementVector.scale(3 / 2f));
			Vec3d rayTarget = VecHelper.getCenterOf(pos).add(movementVector.scale(5 / 2f));
			RayTraceContext rayTraceContext =
				new RayTraceContext(rayOrigin, rayTarget, BlockMode.OUTLINE, FluidMode.NONE, player);
			BlockRayTraceResult result = world.rayTraceBlocks(rayTraceContext);
			reach = (float) (.5f + Math.min(result.getHitVec().subtract(rayOrigin).length(), .75f));

			timer = 1000;
			sendData();
			return;
		}

		if (state == State.EXPANDING) {
			if (boop)
				triggerBoop();
			else
				activate();

			state = State.RETRACTING;
			timer = 1000;
			sendData();
			return;
		}

		if (state == State.RETRACTING) {
			state = State.WAITING;
			timer = 500;
			returnAndDeposit();
			sendData();
			return;
		}

	}

	public boolean startBoop(Direction facing) {
		if (!world.isAirBlock(pos.offset(facing, 1)) || !world.isAirBlock(pos.offset(facing, 2)))
			return false;
		BlockPos otherDeployer = pos.offset(facing, 4);
		if (!world.isBlockPresent(otherDeployer))
			return false;
		TileEntity otherTile = world.getTileEntity(otherDeployer);
		if (!(otherTile instanceof DeployerTileEntity))
			return false;
		DeployerTileEntity deployerTile = (DeployerTileEntity) otherTile;
		if (world.getBlockState(otherDeployer).get(FACING).getOpposite() != facing || deployerTile.mode != Mode.PUNCH)
			return false;

		boop = true;
		reach = 1f;
		timer = 1000;
		state = State.EXPANDING;
		sendData();
		return true;
	}

	public void triggerBoop() {
		TileEntity otherTile = world.getTileEntity(pos.offset(getBlockState().get(FACING), 4));
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
		List<ServerPlayerEntity> players =
			world.getEntitiesWithinAABB(ServerPlayerEntity.class, new AxisAlignedBB(pos).grow(9));
		players.forEach(AllTriggers.DEPLOYER_BOOP::trigger);
	}

	protected void activate() {
		Vec3d movementVector = getMovementVector();
		Direction direction = getBlockState().get(FACING);
		Vec3d center = VecHelper.getCenterOf(pos);
		BlockPos clickedPos = pos.offset(direction, 2);
		player.rotationYaw = direction.getHorizontalAngle();
		player.rotationPitch = direction == Direction.UP ? -90 : direction == Direction.DOWN ? 90 : 0;

		DeployerHandler.activate(player, center, clickedPos, movementVector, mode);
		if (player != null)
			heldItem = player.getHeldItemMainhand();
	}

	protected void returnAndDeposit() {
		PlayerInventory inv = player.inventory;
		for (List<ItemStack> list : Arrays.asList(inv.armorInventory, inv.offHandInventory, inv.mainInventory)) {
			for (int i = 0; i < list.size(); ++i) {
				ItemStack itemstack = list.get(i);
				if (itemstack.isEmpty())
					continue;

				if (list == inv.mainInventory && i == inv.currentItem && filtering.test(itemstack))
					continue;

				itemstack = insert(itemstack, false);
				if (!itemstack.isEmpty())
					ItemHelper.addToList(itemstack, overflowItems);
				list.set(i, ItemStack.EMPTY);
			}
		}
		heldItem = player.getHeldItemMainhand();
	}

	protected void tryDisposeOfItems() {
		boolean noInv = extracting.getInventories().isEmpty();
		for (Iterator<ItemStack> iterator = overflowItems.iterator(); iterator.hasNext();) {
			ItemStack itemStack = iterator.next();

			if (noInv) {
				Vec3d offset = getMovementVector();
				Vec3d outPos = VecHelper.getCenterOf(pos).add(offset.scale(-.65f));
				Vec3d motion = offset.scale(-.25f);
				ItemEntity e = new ItemEntity(world, outPos.x, outPos.y, outPos.z, itemStack.copy());
				e.setMotion(motion);
				world.addEntity(e);
				iterator.remove();
				continue;
			}

			itemStack = insert(itemStack, false);
			if (itemStack.isEmpty())
				iterator.remove();
		}
	}

	protected ItemStack insert(ItemStack stack, boolean simulate) {
		for (IItemHandler inv : extracting.getInventories()) {
			stack = ItemHandlerHelper.insertItemStacked(inv, stack, simulate);
			if (stack.isEmpty())
				break;
		}
		return stack;
	}

	protected Vec3d getMovementVector() {
		if (!AllBlocks.DEPLOYER.typeOf(getBlockState()))
			return Vec3d.ZERO;
		return new Vec3d(getBlockState().get(FACING).getDirectionVec());
	}

	@Override
	public void read(CompoundNBT compound) {
		state = NBTHelper.readEnum(compound.getString("State"), State.class);
		mode = NBTHelper.readEnum(compound.getString("Mode"), Mode.class);
		timer = compound.getInt("Timer");
		deferredInventoryList = compound.getList("Inventory", NBT.TAG_COMPOUND);
		overflowItems = NBTHelper.readItemList(compound.getList("Overflow", NBT.TAG_COMPOUND));
		if (compound.contains("HeldItem"))
			heldItem = ItemStack.read(compound.getCompound("HeldItem"));
		super.read(compound);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putString("Mode", NBTHelper.writeEnum(mode));
		compound.putString("State", NBTHelper.writeEnum(state));
		compound.putInt("Timer", timer);
		if (player != null) {
			compound.put("HeldItem", player.getHeldItemMainhand().serializeNBT());
			ListNBT invNBT = new ListNBT();
			player.inventory.write(invNBT);
			compound.put("Inventory", invNBT);
			compound.put("Overflow", NBTHelper.writeItemList(overflowItems));
		}
		return super.write(compound);
	}

	@Override
	public CompoundNBT writeToClient(CompoundNBT compound) {
		compound.putFloat("Reach", reach);
		if (player != null) {
			compound.put("HeldItem", player.getHeldItemMainhand().serializeNBT());
			if (player.spawnedItemEffects != null) {
				compound.put("Particle", player.spawnedItemEffects.serializeNBT());
				player.spawnedItemEffects = null;
			}
		}
		return super.writeToClient(compound);
	}

	@Override
	public void readClientUpdate(CompoundNBT tag) {
		reach = tag.getFloat("Reach");
		if (tag.contains("Particle")) {
			ItemStack particleStack = ItemStack.read(tag.getCompound("Particle"));
			SandPaperItem.spawnParticles(VecHelper.getCenterOf(pos).add(getMovementVector().scale(2f)), particleStack,
					this.world);
		}

		super.readClientUpdate(tag);
	}

	private IItemHandlerModifiable createHandler() {
		return new DeployerItemHandler(this);
	}

	@Override
	public boolean hasFastRenderer() {
		return false;
	}

	public AllBlockPartials getHandPose() {
		return mode == Mode.PUNCH ? AllBlockPartials.DEPLOYER_HAND_PUNCHING
				: heldItem.isEmpty() ? AllBlockPartials.DEPLOYER_HAND_POINTING : AllBlockPartials.DEPLOYER_HAND_HOLDING;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return super.getRenderBoundingBox().grow(3);
	}

	@Override
	public void remove() {
		super.remove();
		invHandler.invalidate();
	}

	public void changeMode() {
		mode = mode == Mode.PUNCH ? Mode.USE : Mode.PUNCH;
		markDirty();
		sendData();
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && invHandler != null) {
			return invHandler.cast();
		}
		return super.getCapability(cap, side);
	}

}
