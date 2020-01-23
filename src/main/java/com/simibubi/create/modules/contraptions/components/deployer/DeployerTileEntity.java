package com.simibubi.create.modules.contraptions.components.deployer;

import static com.simibubi.create.modules.contraptions.base.DirectionalKineticBlock.FACING;
import static net.minecraftforge.eventbus.api.Event.Result.DENY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.foundation.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.behaviour.filtering.FilteringBehaviour.SlotPositioning;
import com.simibubi.create.foundation.behaviour.inventory.ExtractingBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
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
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.items.IItemHandler;
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

	private List<ItemStack> overflowItems = new ArrayList<>();

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
		filtering = new FilteringBehaviour(this).withSlotPositioning(
				new SlotPositioning(DeployerBlock::getFilterSlotPosition, DeployerBlock::getFilterSlotOrientation)
						.scale(.4f));
		extracting = new ExtractingBehaviour(this, this::getExtractingLocations, this::onExtract);

		behaviours.add(filtering);
		behaviours.add(extracting);
	}

	@Override
	public void initialize() {
		super.initialize();
		if (!world.isRemote)
			player = new DeployerFakePlayer((ServerWorld) world);
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
		return (int) (getSpeed() == 0 ? 0 : MathHelper.clamp(Math.abs(getSpeed()) / 4, 1, 512));
	}

	@Override
	public void tick() {
		super.tick();

		if (getSpeed() == 0)
			return;
		if (timer > 0) {
			timer -= getTimerSpeed();
			return;
		}
		if (world.isRemote)
			return;

		if (state == State.WAITING) {
			if (!overflowItems.isEmpty()) {
				tryDisposeOfItems();
				if (!overflowItems.isEmpty())
					timer = getTimerSpeed() * 10;
				return;
			}

			if (!filtering.test(player.getHeldItemMainhand())) {
				if (!player.getHeldItemMainhand().isEmpty()) {
					overflowItems.add(player.getHeldItemMainhand());
					player.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
					sendData();
					return;
				}
				extracting.extract(1);
				if (!filtering.test(player.getHeldItemMainhand()))
					timer = getTimerSpeed() * 10;
				return;
			}

			if (filtering.getFilter().isEmpty() && player.getHeldItemMainhand().isEmpty())
				extracting.extract(1);

			if (player.getHeldItemMainhand().getItem() instanceof BlockItem) {
				if (!world.getBlockState(pos.offset(getBlockState().get(FACING), 2)).getMaterial().isReplaceable()) {
					timer = getTimerSpeed() * 10;
					return;
				}
			}

			state = State.EXPANDING;
			Vec3d movementVector = getMovementVector();
			Vec3d rayOrigin = VecHelper.getCenterOf(pos).add(movementVector.scale(3 / 2f));
			Vec3d rayTarget = VecHelper.getCenterOf(pos).add(movementVector.scale(5 / 2f));
			RayTraceContext rayTraceContext = new RayTraceContext(rayOrigin, rayTarget, BlockMode.OUTLINE,
					FluidMode.SOURCE_ONLY, player);
			BlockRayTraceResult result = world.rayTraceBlocks(rayTraceContext);
			reach = (float) (.5f + Math.min(result.getHitVec().subtract(rayOrigin).length(), .75f));

			timer = 1000;
			sendData();
			return;
		}

		if (state == State.EXPANDING) {
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

	protected void activate() {
		Vec3d movementVector = getMovementVector();
		Direction direction = getBlockState().get(FACING);

		player.rotationYaw = AngleHelper.horizontalAngle(direction);
		player.rotationPitch = direction == Direction.UP ? -90 : direction == Direction.DOWN ? 90 : 0;

		BlockPos clicked = pos.offset(direction, 2);
		ItemStack stack = player.getHeldItemMainhand();

		List<LivingEntity> entities = world.getEntitiesWithinAABB(LivingEntity.class, new AxisAlignedBB(clicked));
		if (!entities.isEmpty()) {
			stack.interactWithEntity(player, entities.get(world.rand.nextInt(entities.size())), Hand.MAIN_HAND);
			return;
		}

		Vec3d rayOrigin = VecHelper.getCenterOf(pos).add(movementVector.scale(3 / 2f + 1 / 64f));
		Vec3d rayTarget = VecHelper.getCenterOf(pos).add(movementVector.scale(5 / 2f - 1 / 64f));
		RayTraceContext rayTraceContext = new RayTraceContext(rayOrigin, rayTarget, BlockMode.OUTLINE,
				FluidMode.SOURCE_ONLY, player);
		BlockRayTraceResult result = world.rayTraceBlocks(rayTraceContext);
		ItemUseContext itemusecontext = new ItemUseContext(player, Hand.MAIN_HAND, result);

		RightClickBlock event = ForgeHooks.onRightClickBlock(player, Hand.MAIN_HAND, clicked, direction.getOpposite());

		if (event.getUseItem() != DENY) {
			ActionResultType actionresult = stack.onItemUseFirst(itemusecontext);
			if (actionresult != ActionResultType.PASS)
				return;
			player.setHeldItem(Hand.MAIN_HAND, stack.onItemUseFinish(world, player));
		}

		BlockState clickedState = world.getBlockState(clicked);
		boolean holdingSomething = !player.getHeldItemMainhand().isEmpty();
		boolean flag1 = !(player.isSneaking() && holdingSomething)
				|| (stack.doesSneakBypassUse(world, clicked, player));

		if (event.getUseBlock() != DENY && flag1
				&& clickedState.onBlockActivated(world, player, Hand.MAIN_HAND, result))
			return;
		if (stack.isEmpty())
			return;
		if (event.getUseItem() == DENY)
			return;
		if (stack.getItem() instanceof BlockItem
				&& !clickedState.isReplaceable(new BlockItemUseContext(itemusecontext)))
			return;

		ActionResultType onItemUse = stack.onItemUse(itemusecontext);
		if (onItemUse == ActionResultType.SUCCESS)
			return;
		stack.getItem().onItemRightClick(world, player, Hand.MAIN_HAND);
	}

	protected void returnAndDeposit() {
		PlayerInventory inv = player.inventory;
		for (List<ItemStack> list : Arrays.asList(inv.armorInventory, inv.offHandInventory, inv.mainInventory)) {
			for (int i = 0; i < list.size(); ++i) {
				ItemStack itemstack = list.get(i);
				if (itemstack.isEmpty())
					continue;

				if (list == inv.mainInventory && i == inv.currentItem && filtering.test(itemstack))
					if (itemstack.getCount() == 1)
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
		for (Iterator<ItemStack> iterator = overflowItems.iterator(); iterator.hasNext();) {
			ItemStack itemStack = iterator.next();
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
		super.read(compound);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putString("Mode", NBTHelper.writeEnum(mode));
		compound.putString("State", NBTHelper.writeEnum(state));
		compound.putInt("Timer", timer);
		return super.write(compound);
	}

	@Override
	public CompoundNBT writeToClient(CompoundNBT compound) {
		compound.putFloat("Reach", reach);
		if (player != null)
			compound.put("HeldItem", player.getHeldItemMainhand().serializeNBT());
		return super.writeToClient(compound);
	}

	@Override
	public void readClientUpdate(CompoundNBT tag) {
		reach = tag.getFloat("Reach");
		if (tag.contains("HeldItem"))
			heldItem = ItemStack.read(tag.getCompound("HeldItem"));
		super.readClientUpdate(tag);
	}

	@Override
	public boolean hasFastRenderer() {
		return false;
	}

	@Override
	public void remove() {
		super.remove();
		player = null;
	}

	public AllBlocks getHandPose() {
		return mode == Mode.PUNCH ? AllBlocks.DEPLOYER_HAND_PUNCHING
				: heldItem.isEmpty() ? AllBlocks.DEPLOYER_HAND_POINTING : AllBlocks.DEPLOYER_HAND_HOLDING;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return super.getRenderBoundingBox().grow(3);
	}

	public void changeMode() {
		eject();
		mode = mode == Mode.PUNCH ? Mode.USE : Mode.PUNCH;
		markDirty();
		sendData();
	}

	protected void eject() {

	}

}
