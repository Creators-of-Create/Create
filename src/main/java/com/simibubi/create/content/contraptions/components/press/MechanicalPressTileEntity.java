package com.simibubi.create.content.contraptions.components.press;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.processing.BasinOperatingTileEntity;
import com.simibubi.create.content.contraptions.processing.BasinTileEntity.BasinInventory;
import com.simibubi.create.content.logistics.InWorldProcessing;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class MechanicalPressTileEntity extends BasinOperatingTileEntity {

	private static final Object compressingRecipesKey = new Object();
	public List<ItemStack> pressedItems = new ArrayList<>();

	public static class PressingInv extends RecipeWrapper {
		public PressingInv() {
			super(new ItemStackHandler(1));
		}
	}

	enum Mode {
		WORLD(1), BELT(19f / 16f), BASIN(22f / 16f)

		;

		float headOffset;

		Mode(float headOffset) {
			this.headOffset = headOffset;
		}
	}

	private static final PressingInv pressingInv = new PressingInv();
	public BeltProcessingBehaviour processingBehaviour;

	public int runningTicks;
	public boolean running;
	public Mode mode;
	public boolean finished;

	public MechanicalPressTileEntity(TileEntityType<? extends MechanicalPressTileEntity> type) {
		super(type);
		mode = Mode.WORLD;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		processingBehaviour =
			new BeltProcessingBehaviour(this).whenItemEnters((s, i) -> BeltPressingCallbacks.onItemReceived(s, i, this))
				.whileItemHeld((s, i) -> BeltPressingCallbacks.whenItemHeld(s, i, this));
		behaviours.add(processingBehaviour);
	}

	@Override
	protected void read(CompoundNBT compound, boolean clientPacket) {
		running = compound.getBoolean("Running");
		mode = Mode.values()[compound.getInt("Mode")];
		finished = compound.getBoolean("Finished");
		runningTicks = compound.getInt("Ticks");
		super.read(compound, clientPacket);

		if (clientPacket) {
			NBTHelper.iterateCompoundList(compound.getList("ParticleItems", NBT.TAG_COMPOUND),
				c -> pressedItems.add(ItemStack.read(c)));
			spawnParticles();
		}
	}

	@Override
	public void write(CompoundNBT compound, boolean clientPacket) {
		compound.putBoolean("Running", running);
		compound.putInt("Mode", mode.ordinal());
		compound.putBoolean("Finished", finished);
		compound.putInt("Ticks", runningTicks);
		super.write(compound, clientPacket);

		if (clientPacket)
			compound.put("ParticleItems", NBTHelper.writeCompoundList(pressedItems, ItemStack::serializeNBT));
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(pos).expand(0, -1.5, 0)
			.expand(0, 1, 0);
	}

	public float getRenderedHeadOffset(float partialTicks) {
		if (running) {
			if (runningTicks < 40) {
				float num = (runningTicks - 1 + partialTicks) / 30f;
				return MathHelper.clamp(num * num * num, 0, mode.headOffset);
			}
			return MathHelper.clamp(((60 - runningTicks) + 1 - partialTicks) / 20f * mode.headOffset, 0,
				mode.headOffset);
		}
		return 0;
	}

	public void start(Mode mode) {
		this.mode = mode;
		running = true;
		runningTicks = 0;
		pressedItems.clear();
		sendData();
	}

	public boolean inWorld() {
		return mode == Mode.WORLD;
	}

	public boolean onBasin() {
		return mode == Mode.BASIN;
	}

	@Override
	public void tick() {
		super.tick();

		if (!running || world == null)
			return;

		if (runningTicks == 30) {

			if (inWorld()) {
				AxisAlignedBB bb = new AxisAlignedBB(pos.down(1));
				pressedItems.clear();
				for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(null, bb)) {
					if (!(entity instanceof ItemEntity))
						continue;

					ItemEntity itemEntity = (ItemEntity) entity;

					if (!world.isRemote) {
						pressedItems.add(itemEntity.getItem());
						sendData();
						Optional<PressingRecipe> recipe = getRecipe(itemEntity.getItem());
						if (recipe.isPresent()) {
							InWorldProcessing.applyRecipeOn(itemEntity, recipe.get());
							AllTriggers.triggerForNearbyPlayers(AllTriggers.BONK, world, pos, 4);
						}
					}
				}
			}

			if (onBasin()) {
				if (!world.isRemote) {
					pressedItems.clear();
					applyBasinRecipe();
					IItemHandler orElse = basinItemInv.orElse(null);
					if (basinItemInv.isPresent() && orElse instanceof BasinInventory) {
						BasinInventory inv = (BasinInventory) orElse;

						for (int slot = 0; slot < inv.getInputHandler()
							.getSlots(); slot++) {
							ItemStack stackInSlot = inv.getStackInSlot(slot);
							if (stackInSlot.isEmpty())
								continue;
							pressedItems.add(stackInSlot);
						}
					}
					sendData();
				}

			}

			if (!world.isRemote) {
				world.playSound(null, getPos(), AllSoundEvents.MECHANICAL_PRESS_ITEM_BREAK.get(), SoundCategory.BLOCKS,
					.5f, 1f);
				world.playSound(null, getPos(), AllSoundEvents.MECHANICAL_PRESS_ACTIVATION.get(), SoundCategory.BLOCKS,
					.125f, 1f);
			}
		}

		if (!world.isRemote && runningTicks > 60) {
			finished = true;
			if (inWorld())
				finished = world.isBlockPowered(pos);
			running = false;

			if (onBasin()) {
				gatherInputs();
				if (matchBasinRecipe(lastRecipe)) {
					startProcessingBasin();
				}
			}

			pressedItems.clear();
			sendData();
			return;
		}

		runningTicks++;
	}

	protected void spawnParticles() {
		if (pressedItems.isEmpty())
			return;

		if (mode == Mode.BASIN) {
			pressedItems.forEach(stack -> makeCompactingParticleEffect(VecHelper.getCenterOf(pos.down(2)), stack));
		}
		if (mode == Mode.BELT) {
			pressedItems.forEach(stack -> makePressingParticleEffect(VecHelper.getCenterOf(pos.down(2))
				.add(0, 8 / 16f, 0), stack));
		}
		if (mode == Mode.WORLD) {
			pressedItems.forEach(stack -> makePressingParticleEffect(VecHelper.getCenterOf(pos.down(1))
				.add(0, -1 / 4f, 0), stack));
		}

		pressedItems.clear();
	}

	public void makePressingParticleEffect(Vec3d pos, ItemStack stack) {
		if (world != null && world.isRemote) {
			for (int i = 0; i < 20; i++) {
				Vec3d motion = VecHelper.offsetRandomly(Vec3d.ZERO, world.rand, .125f)
					.mul(1, 0, 1);
				world.addParticle(new ItemParticleData(ParticleTypes.ITEM, stack), pos.x, pos.y - .25f, pos.z, motion.x,
					motion.y + .125f, motion.z);
			}
		}
	}

	public void makeCompactingParticleEffect(Vec3d pos, ItemStack stack) {
		if (world != null && world.isRemote) {
			for (int i = 0; i < 20; i++) {
				Vec3d motion = VecHelper.offsetRandomly(Vec3d.ZERO, world.rand, .175f)
					.mul(1, 0, 1);
				world.addParticle(new ItemParticleData(ParticleTypes.ITEM, stack), pos.x, pos.y, pos.z, motion.x,
					motion.y + .25f, motion.z);
			}
		}
	}

	public Optional<PressingRecipe> getRecipe(ItemStack item) {
		pressingInv.setInventorySlotContents(0, item);
		return world.getRecipeManager()
			.getRecipe(AllRecipeTypes.PRESSING.getType(), pressingInv, world);
	}

	public static boolean canCompress(NonNullList<Ingredient> ingredients) {
		return (ingredients.size() == 4 || ingredients.size() == 9) && ItemHelper.condenseIngredients(ingredients)
			.size() == 1;
	}

	@Override
	protected <C extends IInventory> boolean matchStaticFilters(IRecipe<C> recipe) {
		return recipe instanceof ICraftingRecipe && canCompress(recipe.getIngredients());
	}

	@Override
	protected <C extends IInventory> boolean matchBasinRecipe(IRecipe<C> recipe) {
		if (!super.matchBasinRecipe(recipe))
			return false;

		NonNullList<Ingredient> ingredients = recipe.getIngredients();
		List<ItemStack> remainingItems = new ArrayList<>();
		itemInputs.forEach(stack -> remainingItems.add(stack.copy()));
		
		Ingredients: for (Ingredient ingredient : ingredients) {
			for (ItemStack stack : remainingItems) {
				if (stack.isEmpty())
					continue;
				if (ingredient.test(stack)) {
					stack.shrink(1);
					continue Ingredients;
				}
			}
			return false;
		}
		return true;
	}

	@Override
	protected Object getRecipeCacheKey() {
		return compressingRecipesKey;
	}

	@Override
	public void startProcessingBasin() {
		if (running && runningTicks <= 30)
			return;
		super.startProcessingBasin();
		start(Mode.BASIN);
	}

	@Override
	protected void onBasinRemoved() {
		pressedItems.clear();
		running = false;
		runningTicks = 0;
		sendData();
	}

	@Override
	protected boolean isRunning() {
		return running;
	}

}
