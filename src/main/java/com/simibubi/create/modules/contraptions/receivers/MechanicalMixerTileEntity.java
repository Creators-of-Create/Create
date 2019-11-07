package com.simibubi.create.modules.contraptions.receivers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.simibubi.create.AllPackets;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.receivers.BasinTileEntity.BasinInventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

public class MechanicalMixerTileEntity extends KineticTileEntity {

	public int runningTicks;
	public int processingTicks;
	public boolean running;
	public boolean checkBasin;
	public boolean basinRemoved;

	public int minIngredients;
	public int currentValue;
	public int lastModified;

	private ShapelessRecipe lastRecipe;
	private LazyOptional<IItemHandler> basinInv = LazyOptional.empty();
	private List<ItemStack> inputs;

	public MechanicalMixerTileEntity() {
		super(AllTileEntities.MECHANICAL_MIXER.type);
		checkBasin = true;
		minIngredients = currentValue = 1;
		lastModified = -1;
		processingTicks = -1;
	}

	@Override
	public void onSpeedChanged() {
		super.onSpeedChanged();
		checkBasin = true;
	}

	public float getRenderedHeadOffset(float partialTicks) {
		int localTick = 0;
		if (running) {
			if (runningTicks < 20) {
				localTick = runningTicks;
				float num = (localTick + partialTicks) / 20f;
				num = ((2 - MathHelper.cos((float) (num * Math.PI))) / 2);
				return num - .5f;
			}
			if (runningTicks <= 20) {
				return 1;
			}
			if (runningTicks > 20) {
				localTick = 40 - runningTicks;
				float num = (localTick - partialTicks) / 20f;
				num = ((2 - MathHelper.cos((float) (num * Math.PI))) / 2);
				return num - .5f;
			}
		}
		return 0;
	}

	public float getRenderedHeadRotationSpeed(float partialTicks) {
		float speed = getSpeed();
		if (running) {
			if (runningTicks < 15) {
				return speed;
			}
			if (runningTicks <= 20) {
				return speed * 2;
			}
			if (runningTicks > 20) {
				return speed;
			}
		}
		return speed / 2;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(pos).expand(0, -1.5, 0);
	}

	@Override
	public void read(CompoundNBT compound) {
		running = compound.getBoolean("Running");
		runningTicks = compound.getInt("Ticks");
		currentValue = minIngredients = compound.getInt("MinIngredients");
		super.read(compound);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putBoolean("Running", running);
		compound.putInt("Ticks", runningTicks);
		compound.putInt("MinIngredients", minIngredients);
		return super.write(compound);
	}

	public void setMinIngredientsLazily(int minIngredients) {
		this.currentValue = MathHelper.clamp(minIngredients, 1, 9);
		if (currentValue == this.minIngredients)
			return;
		this.lastModified = 0;
	}

	@Override
	public void tick() {
		super.tick();

		if (world.isRemote && lastModified != -1) {
			if (lastModified++ > 10) {
				lastModified = -1;
				AllPackets.channel.sendToServer(new ConfigureMixerPacket(pos, currentValue));
			}
		}

		if (runningTicks == 40) {
			running = false;
			runningTicks = 0;
			return;
		}

		if (basinRemoved) {
			basinRemoved = false;
			if (running) {
				runningTicks = 40;
				return;
			}
		}

		float speed = Math.abs(getSpeed());
		if (running) {
			if (world.isRemote && runningTicks == 20)
				renderParticles();

			if (!world.isRemote && runningTicks == 20) {
				if (processingTicks < 0) {
					processingTicks = (MathHelper.log2((int) (8000 / speed))) * 15 + 1;
					return;
				}
				processingTicks--;
				if (processingTicks == 0) {
					runningTicks++;
					processingTicks = -1;
					applyRecipe();
					sendData();
				}
			}

			if (runningTicks != 20)
				runningTicks++;

			return;
		}

		if (speed < 32)
			return;
		if (!checkBasin)
			return;
		checkBasin = false;
		TileEntity basinTE = world.getTileEntity(pos.down(2));
		if (basinTE == null || !(basinTE instanceof BasinTileEntity))
			return;
		if (!basinInv.isPresent())
			basinInv = basinTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		if (!basinInv.isPresent())
			return;

		if (world.isRemote)
			return;

		gatherInputs();
		if (matchRecipe(lastRecipe)) {
			running = true;
			runningTicks = 0;
			sendData();
			return;
		}

		List<IRecipe<?>> shapelessRecipe = world.getRecipeManager().getRecipes().parallelStream()
				.filter(recipe -> recipe.getSerializer() == IRecipeSerializer.CRAFTING_SHAPELESS)
				.filter(this::matchRecipe).sorted((r1, r2) -> r1.getIngredients().size() - r2.getIngredients().size())
				.collect(Collectors.toList());
		if (shapelessRecipe.isEmpty())
			return;

		running = true;
		runningTicks = 0;
		lastRecipe = (ShapelessRecipe) shapelessRecipe.get(0);
		sendData();
	}

	public void renderParticles() {
		IItemHandler itemHandler = basinInv.orElse(null);
		if (itemHandler != null) {
			BasinInventory inv = (BasinInventory) itemHandler;

			for (int slot = 0; slot < inv.getInputHandler().getSlots(); slot++) {
				ItemStack stackInSlot = itemHandler.getStackInSlot(slot);
				if (stackInSlot.isEmpty())
					continue;

				ItemParticleData data = new ItemParticleData(ParticleTypes.ITEM, stackInSlot);
				float angle = world.rand.nextFloat() * 360;
				Vec3d offset = new Vec3d(0, 0, 0.25f);
				offset = VecHelper.rotate(offset, angle, Axis.Y);
				Vec3d target = VecHelper.rotate(offset, getSpeed() > 0 ? 25 : -25, Axis.Y).add(0, .25f, 0);

				Vec3d center = offset.add(VecHelper.getCenterOf(pos));
				target = VecHelper.offsetRandomly(target.subtract(offset), world.rand, 1 / 128f);
				world.addParticle(data, center.x, center.y - 2, center.z, target.x, target.y, target.z);
			}
		}
	}

	public void gatherInputs() {
		BasinInventory inv = (BasinInventory) basinInv.orElse(null);
		inputs = new ArrayList<>();
		IItemHandlerModifiable inputHandler = inv.getInputHandler();
		for (int slot = 0; slot < inputHandler.getSlots(); ++slot) {
			ItemStack itemstack = inputHandler.extractItem(slot, inputHandler.getSlotLimit(slot), true);
			if (!itemstack.isEmpty()) {
				inputs.add(itemstack);
			}
		}
	}

	public void applyRecipe() {
		if (lastRecipe == null)
			return;
		if (!basinInv.isPresent())
			return;

		BasinInventory inv = (BasinInventory) basinInv.orElse(null);
		if (inv == null)
			return;

		IItemHandlerModifiable inputs = inv.getInputHandler();
		IItemHandlerModifiable outputs = inv.getOutputHandler();
		int buckets = 0;
		Ingredients: for (Ingredient ingredient : lastRecipe.getIngredients()) {
			for (int slot = 0; slot < inputs.getSlots(); slot++) {
				if (!ingredient.test(inputs.extractItem(slot, 1, true)))
					continue;
				ItemStack extracted = inputs.extractItem(slot, 1, false);
				if (extracted.getItem() instanceof BucketItem)
					buckets++;
				continue Ingredients;
			}
			// something wasn't found
			return;
		}

		ItemHandlerHelper.insertItemStacked(outputs, lastRecipe.getRecipeOutput().copy(), false);
		if (buckets > 0)
			ItemHandlerHelper.insertItemStacked(outputs, new ItemStack(Items.BUCKET, buckets), false);

		// Continue mixing
		gatherInputs();
		if (matchRecipe(lastRecipe)) {
			runningTicks = 20;
			sendData();
		}
	}

	public <C extends IInventory> boolean matchRecipe(IRecipe<C> recipe) {
		if (!(recipe instanceof ShapelessRecipe))
			return false;
		if (recipe.getIngredients().size() < minIngredients)
			return false;

		ShapelessRecipe shapelessRecipe = (ShapelessRecipe) recipe;
		NonNullList<Ingredient> ingredients = shapelessRecipe.getIngredients();
		if (!ingredients.stream().allMatch(Ingredient::isSimple))
			return false;

		List<ItemStack> remaining = new ArrayList<>();
		inputs.forEach(stack -> remaining.add(stack.copy()));

		// sort by leniency
		List<Ingredient> sortedIngredients = new LinkedList<>(ingredients);
		sortedIngredients.sort((i1, i2) -> i1.getMatchingStacks().length - i2.getMatchingStacks().length);
		Ingredients: for (Ingredient ingredient : sortedIngredients) {
			for (ItemStack stack : remaining) {
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

}
