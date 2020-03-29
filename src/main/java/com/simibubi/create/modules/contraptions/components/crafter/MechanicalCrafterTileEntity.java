package com.simibubi.create.modules.contraptions.components.crafter;

import static com.simibubi.create.modules.contraptions.base.HorizontalKineticBlock.HORIZONTAL_FACING;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.foundation.behaviour.inventory.InsertingBehaviour;
import com.simibubi.create.foundation.behaviour.inventory.InventoryManagementBehaviour.Attachments;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.components.crafter.ConnectedInputHandler.ConnectedInput;
import com.simibubi.create.modules.contraptions.components.crafter.MechanicalCrafterBlock.Pointing;
import com.simibubi.create.modules.contraptions.components.crafter.RecipeGridHandler.GroupedItems;
import com.simibubi.create.modules.contraptions.relays.belt.BeltTileEntity;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class MechanicalCrafterTileEntity extends KineticTileEntity {

	enum Phase {
		IDLE, ACCEPTING, ASSEMBLING, EXPORTING, WAITING, CRAFTING, INSERTING;
	}

	protected ItemStackHandler inventory = new ItemStackHandler(1) {

		@Override
		public int getSlotLimit(int slot) {
			return 1;
		}

		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			return ItemStack.EMPTY;
		};

		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			if (phase != Phase.IDLE)
				return stack;
			if (covered)
				return stack;
			return super.insertItem(slot, stack, simulate);
		};

		protected void onContentsChanged(int slot) {
			if (!getStackInSlot(slot).isEmpty() && phase == Phase.IDLE)
				checkCompletedRecipe();
			markDirty();
			sendData();
		};

	};

	protected GroupedItems groupedItems = new GroupedItems();
	protected ConnectedInput input = new ConnectedInput();
	protected LazyOptional<IItemHandler> invSupplier = LazyOptional.of(() -> input.getItemHandler(world, pos));
	protected boolean reRender;
	protected Phase phase;
	protected int countDown;
	protected boolean covered;

	protected GroupedItems groupedItemsBeforeCraft; // for rendering on client
	private InsertingBehaviour inserting;

	public MechanicalCrafterTileEntity() {
		super(AllTileEntities.MECHANICAL_CRAFTER.type);
		setLazyTickRate(20);
		phase = Phase.IDLE;
		groupedItemsBeforeCraft = new GroupedItems();
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		inserting = new InsertingBehaviour(this, Attachments.toward(this::getTargetFacing));
		behaviours.add(inserting);
	}

	public void blockChanged() {
		removeBehaviour(InsertingBehaviour.TYPE);
		inserting = new InsertingBehaviour(this, Attachments.toward(this::getTargetFacing));
		putBehaviour(inserting);
	}

	public Direction getTargetFacing() {
		return MechanicalCrafterBlock.getTargetDirection(world.getBlockState(pos));
	}

	@Override
	public boolean hasFastRenderer() {
		return false;
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.put("Inventory", inventory.serializeNBT());

		CompoundNBT inputNBT = new CompoundNBT();
		input.write(inputNBT);
		compound.put("ConnectedInput", inputNBT);

		CompoundNBT groupedItemsNBT = new CompoundNBT();
		groupedItems.write(groupedItemsNBT);
		compound.put("GroupedItems", groupedItemsNBT);

		compound.putString("Phase", phase.name());
		compound.putInt("CountDown", countDown);
		compound.putBoolean("Cover", covered);

		return super.write(compound);
	}

	@Override
	public CompoundNBT writeToClient(CompoundNBT tag) {
		if (reRender) {
			tag.putBoolean("Redraw", true);
			reRender = false;
		}
		return super.writeToClient(tag);
	}

	@Override
	public void readClientUpdate(CompoundNBT tag) {
		if (tag.contains("Redraw"))
			world.notifyBlockUpdate(getPos(), getBlockState(), getBlockState(), 16);

		Phase phaseBefore = phase;
		GroupedItems before = this.groupedItems;

		super.readClientUpdate(tag);

		if (phaseBefore != phase && phase == Phase.CRAFTING)
			groupedItemsBeforeCraft = before;
		if (phaseBefore == Phase.EXPORTING && phase == Phase.WAITING) {
			Direction facing = getBlockState().get(MechanicalCrafterBlock.HORIZONTAL_FACING);
			Vec3d vec = new Vec3d(facing.getDirectionVec()).scale(.75).add(VecHelper.getCenterOf(pos));
			Direction targetDirection = MechanicalCrafterBlock.getTargetDirection(getBlockState());
			vec = vec.add(new Vec3d(targetDirection.getDirectionVec()).scale(1));
			world.addParticle(ParticleTypes.CRIT, vec.x, vec.y, vec.z, 0, 0, 0);
		}

	}

	@Override
	public void read(CompoundNBT compound) {
		inventory.deserializeNBT(compound.getCompound("Inventory"));
		input.read(compound.getCompound("ConnectedInput"));
		groupedItems = GroupedItems.read(compound.getCompound("GroupedItems"));
		phase = Phase.IDLE;
		String name = compound.getString("Phase");
		for (Phase phase : Phase.values())
			if (phase.name().equals(name))
				this.phase = phase;
		countDown = compound.getInt("CountDown");
		covered = compound.getBoolean("Cover");
		super.read(compound);
	}

	@Override
	public void remove() {
		invSupplier.invalidate();
		super.remove();
	}

	public int getCountDownSpeed() {
		if (getSpeed() == 0)
			return 0;
		return MathHelper.clamp((int) Math.abs(getSpeed()), 4, 250);
	}

	@Override
	public void tick() {
		super.tick();

		if (phase == Phase.ACCEPTING)
			return;

		if (phase == Phase.ASSEMBLING) {
			countDown -= getCountDownSpeed();
			if (countDown < 0) {
				countDown = 0;
				if (world.isRemote)
					return;
				if (RecipeGridHandler.getTargetingCrafter(this) != null) {
					phase = Phase.EXPORTING;
					countDown = 1000;
					sendData();
					return;
				}
				ItemStack result = RecipeGridHandler.tryToApplyRecipe(world, groupedItems);
				if (result != null) {

					List<ItemStack> containers = new ArrayList<>();
					groupedItems.grid.values().forEach(stack -> {
						if (stack.hasContainerItem())
							containers.add(stack.getContainerItem().copy());
					});

					groupedItems = new GroupedItems(result);
					containers.forEach(stack -> {
						GroupedItems container = new GroupedItems(stack);
						container.mergeOnto(groupedItems, Pointing.LEFT);
					});

					phase = Phase.CRAFTING;
					countDown = 2000;
					sendData();
					return;
				}
				ejectWholeGrid();
				return;
			}
		}

		if (phase == Phase.EXPORTING) {
			countDown -= getCountDownSpeed();

			if (countDown < 0) {
				countDown = 0;
				if (world.isRemote)
					return;

				MechanicalCrafterTileEntity targetingCrafter = RecipeGridHandler.getTargetingCrafter(this);
				if (targetingCrafter == null) {
					ejectWholeGrid();
					return;
				}

				Pointing pointing = getBlockState().get(MechanicalCrafterBlock.POINTING);
				groupedItems.mergeOnto(targetingCrafter.groupedItems, pointing);
				groupedItems = new GroupedItems();
				phase = Phase.WAITING;
				countDown = 0;
				sendData();
				targetingCrafter.continueIfAllPrecedingFinished();
				targetingCrafter.sendData();
				return;
			}
		}

		if (phase == Phase.CRAFTING) {

			if (world.isRemote) {
				Direction facing = getBlockState().get(MechanicalCrafterBlock.HORIZONTAL_FACING);
				float progress = countDown / 2000f;
				Vec3d facingVec = new Vec3d(facing.getDirectionVec());
				Vec3d vec = facingVec.scale(.65).add(VecHelper.getCenterOf(pos));
				Vec3d offset = VecHelper.offsetRandomly(Vec3d.ZERO, world.rand, .125f)
						.mul(VecHelper.planeByNormal(facingVec)).normalize().scale(progress * .5f).add(vec);
				if (progress > .5f)
					world.addParticle(ParticleTypes.CRIT, offset.x, offset.y, offset.z, 0, 0, 0);

				if (!groupedItemsBeforeCraft.grid.isEmpty() && progress < .5f) {
					if (groupedItems.grid.containsKey(Pair.of(0, 0))) {
						ItemStack stack = groupedItems.grid.get(Pair.of(0, 0));
						groupedItemsBeforeCraft = new GroupedItems();

						for (int i = 0; i < 10; i++) {
							Vec3d randVec = VecHelper.offsetRandomly(Vec3d.ZERO, world.rand, .125f)
									.mul(VecHelper.planeByNormal(facingVec)).normalize().scale(.25f);
							Vec3d offset2 = randVec.add(vec);
							randVec = randVec.scale(.35f);
							world.addParticle(new ItemParticleData(ParticleTypes.ITEM, stack), offset2.x, offset2.y,
									offset2.z, randVec.x, randVec.y, randVec.z);
						}
					}
				}
			}

			countDown -= getCountDownSpeed();
			if (countDown < 0) {
				countDown = 0;
				if (world.isRemote)
					return;
				tryInsert();
				return;
			}
		}

		if (phase == Phase.INSERTING) {
			if (!world.isRemote && isTargetingBelt())
				tryInsert();
			return;
		}

	}

	protected boolean isTargetingBelt() {
		BlockPos targetPos = pos.offset(getTargetFacing());
		if (!AllBlocks.BELT.typeOf(world.getBlockState(targetPos)))
			return false;
		TileEntity te = world.getTileEntity(targetPos);
		if (te == null || !(te instanceof BeltTileEntity))
			return false;
		return ((KineticTileEntity) te).getSpeed() != 0;
	}

	public void tryInsert() {
		if (inserting.getInventory() == null && !isTargetingBelt()) {
			ejectWholeGrid();
			return;
		}

		boolean chagedPhase = phase != Phase.INSERTING;
		final List<Pair<Integer, Integer>> inserted = new LinkedList<>();

		groupedItems.grid.forEach((pair, stack) -> {
			if (isTargetingBelt()) {
				Direction facing = getTargetFacing();
				BlockPos targetPos = pos.offset(facing);
				BeltTileEntity te = (BeltTileEntity) world.getTileEntity(targetPos);
				if (te.tryInsertingFromSide(facing, stack, false))
					inserted.add(pair);
				return;
			}

			ItemStack remainder = inserting.insert(stack.copy(), false);
			if (!remainder.isEmpty())
				stack.setCount(remainder.getCount());
			else
				inserted.add(pair);
		});

		inserted.forEach(groupedItems.grid::remove);
		if (groupedItems.grid.isEmpty())
			ejectWholeGrid();
		else
			phase = Phase.INSERTING;
		if (!inserted.isEmpty() || chagedPhase)
			sendData();
	}

	public void ejectWholeGrid() {
		List<MechanicalCrafterTileEntity> chain = RecipeGridHandler.getAllCraftersOfChain(this);
		if (chain == null)
			return;
		chain.forEach(MechanicalCrafterTileEntity::eject);
	}

	public void eject() {
		Vec3d ejectPos = VecHelper.getCenterOf(pos)
				.add(new Vec3d(getBlockState().get(HORIZONTAL_FACING).getDirectionVec()).scale(.75f));
		groupedItems.grid.forEach((pair, stack) -> dropItem(ejectPos, stack));
		if (!inventory.getStackInSlot(0).isEmpty())
			dropItem(ejectPos, inventory.getStackInSlot(0));
		phase = Phase.IDLE;
		groupedItems = new GroupedItems();
		inventory.setStackInSlot(0, ItemStack.EMPTY);
		sendData();
	}

	public void dropItem(Vec3d ejectPos, ItemStack stack) {
		ItemEntity itemEntity = new ItemEntity(world, ejectPos.x, ejectPos.y, ejectPos.z, stack);
		itemEntity.setDefaultPickupDelay();
		world.addEntity(itemEntity);
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		if (world.isRemote)
			return;
		if (phase == Phase.IDLE && craftingItemPresent())
			checkCompletedRecipe();
		if (phase == Phase.INSERTING)
			tryInsert();
	}

	public boolean craftingItemPresent() {
		return !inventory.getStackInSlot(0).isEmpty() || covered;
	}

	protected void checkCompletedRecipe() {
		if (getSpeed() == 0)
			return;
		if (world.isRemote)
			return;
		List<MechanicalCrafterTileEntity> chain =
			RecipeGridHandler.getAllCraftersOfChainIf(this, MechanicalCrafterTileEntity::craftingItemPresent);
		if (chain == null)
			return;
		chain.forEach(MechanicalCrafterTileEntity::begin);
	}

	protected void begin() {
		phase = Phase.ACCEPTING;
		groupedItems = new GroupedItems(inventory.getStackInSlot(0));
		inventory.setStackInSlot(0, ItemStack.EMPTY);
		if (RecipeGridHandler.getPrecedingCrafters(this).isEmpty()) {
			phase = Phase.ASSEMBLING;
			countDown = 500;
		}
		sendData();
	}

	protected void continueIfAllPrecedingFinished() {
		List<MechanicalCrafterTileEntity> preceding = RecipeGridHandler.getPrecedingCrafters(this);
		if (preceding == null) {
			ejectWholeGrid();
			return;
		}

		for (MechanicalCrafterTileEntity mechanicalCrafterTileEntity : preceding)
			if (mechanicalCrafterTileEntity.phase != Phase.WAITING)
				return;

		phase = Phase.ASSEMBLING;
		countDown = Math.max(100, getCountDownSpeed() + 1);
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			if (getBlockState().get(HORIZONTAL_FACING) == side)
				return LazyOptional.empty();
			return invSupplier.cast();
		}
		return super.getCapability(cap, side);
	}

	public void connectivityChanged() {
		reRender = true;
		sendData();
		invSupplier.invalidate();
		invSupplier = LazyOptional.of(() -> input.getItemHandler(world, pos));
	}

}
