package com.simibubi.create.modules.contraptions.components.deployer;

import static com.simibubi.create.modules.contraptions.base.DirectionalKineticBlock.FACING;
import static net.minecraftforge.eventbus.api.Event.Result.DENY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Multimap;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.foundation.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.behaviour.filtering.FilteringBehaviour.SlotPositioning;
import com.simibubi.create.foundation.behaviour.inventory.ExtractingBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.WrappedWorld;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.curiosities.tools.SandPaperItem;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.BucketItem;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ActionResult;
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
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.Event.Result;
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
	protected List<ItemStack> overflowItems = new ArrayList<>();
	protected Pair<BlockPos, Float> blockBreakingProgress;

	private ListNBT deferredInventoryList;
	private ItemStack spawnItemEffects;

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
		if (!world.isRemote) {
			player = new DeployerFakePlayer((ServerWorld) world);
			if (deferredInventoryList != null) {
				player.inventory.read(deferredInventoryList);
				deferredInventoryList = null;
				heldItem = player.getHeldItemMainhand();
				sendData();
			}
		}
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
		if (!world.isRemote && blockBreakingProgress != null) {
			if (world.isAirBlock(blockBreakingProgress.getKey())) {
				world.sendBlockBreakProgress(player.getEntityId(), blockBreakingProgress.getKey(), -1);
				blockBreakingProgress = null;
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
			if (stack.getItem() instanceof BlockItem) {
				if (!world.getBlockState(pos.offset(facing, 2)).getMaterial().isReplaceable()) {
					timer = getTimerSpeed() * 10;
					return;
				}
			}

			if (stack.getItem() instanceof BucketItem) {
				BucketItem bucketItem = (BucketItem) stack.getItem();
				Fluid fluid = bucketItem.getFluid();
				if (fluid != Fluids.EMPTY && world.getFluidState(pos.offset(facing, 2)).getFluid() == fluid) {
					timer = getTimerSpeed() * 10;
					return;
				}
			}

			state = State.EXPANDING;
			Vec3d movementVector = getMovementVector();
			Vec3d rayOrigin = VecHelper.getCenterOf(pos).add(movementVector.scale(3 / 2f));
			Vec3d rayTarget = VecHelper.getCenterOf(pos).add(movementVector.scale(5 / 2f));
			RayTraceContext rayTraceContext = new RayTraceContext(rayOrigin, rayTarget, BlockMode.OUTLINE,
					FluidMode.NONE, player);
			BlockRayTraceResult result = world.rayTraceBlocks(rayTraceContext);
			reach = (float) (.5f + Math.min(result.getHitVec().subtract(rayOrigin).length(), .75f));

			timer = 1000;
			sendData();
			return;
		}

		if (state == State.EXPANDING) {
			Multimap<String, AttributeModifier> attributeModifiers = stack
					.getAttributeModifiers(EquipmentSlotType.MAINHAND);
			player.getAttributes().applyAttributeModifiers(attributeModifiers);
			activate();
			player.getAttributes().removeAttributeModifiers(attributeModifiers);
			heldItem = player.getHeldItemMainhand();

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
		// Update player position and angle
		Vec3d movementVector = getMovementVector();
		Direction direction = getBlockState().get(FACING);
		Vec3d center = VecHelper.getCenterOf(pos);
		Vec3d rayOrigin = center.add(movementVector.scale(3 / 2f + 1 / 64f));
		Vec3d rayTarget = center.add(movementVector.scale(5 / 2f - 1 / 64f));
		BlockPos clickedPos = pos.offset(direction, 2);

		player.rotationYaw = direction.getHorizontalAngle();
		player.rotationPitch = direction == Direction.UP ? -90 : direction == Direction.DOWN ? 90 : 0;
		player.setPosition(rayOrigin.x, rayOrigin.y, rayOrigin.z);

		ItemStack stack = player.getHeldItemMainhand();
		Item item = stack.getItem();

		// Check for entities
		World world = this.world;
		List<LivingEntity> entities = world.getEntitiesWithinAABB(LivingEntity.class, new AxisAlignedBB(clickedPos));
		Hand hand = Hand.MAIN_HAND;
		if (!entities.isEmpty()) {
			LivingEntity entity = entities.get(world.rand.nextInt(entities.size()));
			List<ItemEntity> capturedDrops = new ArrayList<>();
			boolean success = false;
			entity.captureDrops(capturedDrops);

			// Use on entity
			if (mode == Mode.USE) {
				ActionResultType cancelResult = ForgeHooks.onInteractEntity(player, entity, hand);
				if (cancelResult == ActionResultType.FAIL) {
					entity.captureDrops(null);
					return;
				}
				if (cancelResult == null) {
					if (entity.processInitialInteract(player, hand))
						success = true;
					else if (stack.interactWithEntity(player, entity, hand))
						success = true;
				}
			}

			// Punch entity
			if (mode == Mode.PUNCH) {
				player.resetCooldown();
				player.attackTargetEntityWithCurrentItem(entity);
				success = true;
			}

			entity.captureDrops(null);
			capturedDrops.forEach(e -> player.inventory.placeItemBackInInventory(this.world, e.getItem()));
			if (success)
				return;
		}

		// Shoot ray
		RayTraceContext rayTraceContext = new RayTraceContext(rayOrigin, rayTarget, BlockMode.OUTLINE, FluidMode.NONE,
				player);
		BlockRayTraceResult result = world.rayTraceBlocks(rayTraceContext);
		BlockState clickedState = world.getBlockState(clickedPos);

		// Left click
		if (mode == Mode.PUNCH) {
			LeftClickBlock event = ForgeHooks.onLeftClickBlock(player, clickedPos, direction.getOpposite());
			if (event.isCanceled())
				return;
			if (!world.isBlockModifiable(player, clickedPos))
				return;
			if (world.extinguishFire(player, clickedPos, direction.getOpposite()))
				return;
			if (clickedState.isAir(world, clickedPos))
				return;
			if (event.getUseBlock() != Result.DENY)
				clickedState.onBlockClicked(world, clickedPos, player);
			if (stack.isEmpty())
				return;

			float progress = clickedState.getPlayerRelativeBlockHardness(player, world, clickedPos) * 16;
			float before = 0;
			if (blockBreakingProgress != null)
				before = blockBreakingProgress.getValue();
			progress += before;

			if (progress >= 1) {
				player.interactionManager.tryHarvestBlock(clickedPos);
				world.sendBlockBreakProgress(player.getEntityId(), clickedPos, -1);
				blockBreakingProgress = null;
				return;
			}

			if ((int) (before * 10) != (int) (progress * 10))
				world.sendBlockBreakProgress(player.getEntityId(), clickedPos, (int) (progress * 10));
			blockBreakingProgress = Pair.of(clickedPos, progress);
			return;
		}

		// Right click
		ItemUseContext itemusecontext = new ItemUseContext(player, hand, result);
		RightClickBlock event = ForgeHooks.onRightClickBlock(player, hand, clickedPos, direction.getOpposite());

		// Item has custom active use
		if (event.getUseItem() != DENY) {
			ActionResultType actionresult = stack.onItemUseFirst(itemusecontext);
			if (actionresult != ActionResultType.PASS)
				return;
		}

		boolean holdingSomething = !player.getHeldItemMainhand().isEmpty();
		boolean flag1 = !(player.isSneaking() && holdingSomething)
				|| (stack.doesSneakBypassUse(world, clickedPos, player));

		// Use on block
		if (event.getUseBlock() != DENY && flag1 && clickedState.onBlockActivated(world, player, hand, result))
			return;
		if (stack.isEmpty())
			return;
		if (event.getUseItem() == DENY)
			return;
		if (item instanceof BlockItem && !clickedState.isReplaceable(new BlockItemUseContext(itemusecontext)))
			return;

		// Reposition fire placement for convenience
		if (item == Items.FLINT_AND_STEEL) {
			Direction newFace = result.getFace();
			BlockPos newPos = result.getPos();
			if (!FlintAndSteelItem.canSetFire(clickedState, world, clickedPos))
				newFace = Direction.UP;
			if (clickedState.getMaterial() == Material.AIR)
				newPos = newPos.offset(direction);
			result = new BlockRayTraceResult(result.getHitVec(), newFace, newPos, result.isInside());
			itemusecontext = new ItemUseContext(player, hand, result);
		}

		// 'Inert' item use behaviour & block placement
		ActionResultType onItemUse = stack.onItemUse(itemusecontext);
		if (onItemUse == ActionResultType.SUCCESS)
			return;
		if (item == Items.ENDER_PEARL)
			return;

		// buckets create their own ray, We use a fake wall to contain the active area
		if (item instanceof BucketItem || item instanceof SandPaperItem) {
			world = new WrappedWorld(world) {

				boolean rayMode = false;

				@Override
				public BlockRayTraceResult rayTraceBlocks(RayTraceContext context) {
					rayMode = true;
					BlockRayTraceResult rayTraceBlocks = super.rayTraceBlocks(context);
					rayMode = false;
					return rayTraceBlocks;
				};

				@Override
				public BlockState getBlockState(BlockPos position) {
					if (rayMode
							&& (pos.offset(direction, 3).equals(position) || pos.offset(direction, 1).equals(position)))
						return Blocks.BEDROCK.getDefaultState();
					return world.getBlockState(position);
				}

			};
		}

		ActionResult<ItemStack> onItemRightClick = item.onItemRightClick(world, player, hand);
		player.setHeldItem(hand, onItemRightClick.getResult());

		CompoundNBT tag = stack.getOrCreateTag();
		if (stack.getItem() instanceof SandPaperItem && tag.contains("Polishing"))
			spawnItemEffects = ItemStack.read(tag.getCompound("Polishing"));
		if (stack.isFood())
			spawnItemEffects = stack.copy();

		if (!player.getActiveItemStack().isEmpty())
			player.setHeldItem(hand, stack.onItemUseFinish(world, player));

		player.resetActiveHand();
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
		super.read(compound);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putString("Mode", NBTHelper.writeEnum(mode));
		compound.putString("State", NBTHelper.writeEnum(state));
		compound.putInt("Timer", timer);
		if (player != null) {
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
		if (player != null)
			compound.put("HeldItem", player.getHeldItemMainhand().serializeNBT());
		if (spawnItemEffects != null) {
			compound.put("Particle", spawnItemEffects.serializeNBT());
			spawnItemEffects = null;
		}
		return super.writeToClient(compound);
	}

	@Override
	public void readClientUpdate(CompoundNBT tag) {
		reach = tag.getFloat("Reach");
		if (tag.contains("HeldItem"))
			heldItem = ItemStack.read(tag.getCompound("HeldItem"));
		if (tag.contains("Particle")) {
			ItemStack particleStack = ItemStack.read(tag.getCompound("Particle"));
			SandPaperItem.spawnParticles(VecHelper.getCenterOf(pos).add(getMovementVector().scale(2f)), particleStack,
					this.world);
		}

		super.readClientUpdate(tag);
	}

	@Override
	public boolean hasFastRenderer() {
		return false;
	}

	@Override
	public void remove() {
		if (!world.isRemote && blockBreakingProgress != null)
			world.sendBlockBreakProgress(player.getEntityId(), blockBreakingProgress.getKey(), -1);
		super.remove();
		player = null;
	}

	public AllBlockPartials getHandPose() {
		return mode == Mode.PUNCH ? AllBlockPartials.DEPLOYER_HAND_PUNCHING
				: heldItem.isEmpty() ? AllBlockPartials.DEPLOYER_HAND_POINTING : AllBlockPartials.DEPLOYER_HAND_HOLDING;
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
