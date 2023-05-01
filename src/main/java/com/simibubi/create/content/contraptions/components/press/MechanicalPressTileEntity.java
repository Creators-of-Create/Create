package com.simibubi.create.content.contraptions.components.press;

import java.util.List;
import java.util.Optional;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllTags;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.crafter.MechanicalCraftingRecipe;
import com.simibubi.create.content.contraptions.components.press.PressingBehaviour.Mode;
import com.simibubi.create.content.contraptions.components.press.PressingBehaviour.PressingBehaviourSpecifics;
import com.simibubi.create.content.contraptions.itemAssembly.SequencedAssemblyRecipe;
import com.simibubi.create.content.contraptions.processing.BasinOperatingTileEntity;
import com.simibubi.create.content.contraptions.processing.BasinTileEntity;
import com.simibubi.create.content.contraptions.processing.InWorldProcessing;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.advancement.CreateAdvancement;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.item.SmartInventory;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class MechanicalPressTileEntity extends BasinOperatingTileEntity implements PressingBehaviourSpecifics {

	private static final Object compressingRecipesKey = new Object();

	public PressingBehaviour pressingBehaviour;
	private int tracksCreated;

	public MechanicalPressTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	protected AABB createRenderBoundingBox() {
		return new AABB(worldPosition).expandTowards(0, -1.5, 0)
			.expandTowards(0, 1, 0);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		pressingBehaviour = new PressingBehaviour(this);
		behaviours.add(pressingBehaviour);

		registerAwardables(behaviours, AllAdvancements.PRESS, AllAdvancements.COMPACTING,
			AllAdvancements.TRACK_CRAFTING);
	}

	public void onItemPressed(ItemStack result) {
		award(AllAdvancements.PRESS);
		if (AllTags.AllBlockTags.TRACKS.matches(result))
			tracksCreated += result.getCount();
		if (tracksCreated >= 1000) {
			award(AllAdvancements.TRACK_CRAFTING);
			tracksCreated = 0;
		}
	}

	public PressingBehaviour getPressingBehaviour() {
		return pressingBehaviour;
	}

	@Override
	public boolean tryProcessInBasin(boolean simulate) {
		applyBasinRecipe();

		Optional<BasinTileEntity> basin = getBasin();
		if (basin.isPresent()) {
			SmartInventory inputs = basin.get()
				.getInputInventory();
			for (int slot = 0; slot < inputs.getSlots(); slot++) {
				ItemStack stackInSlot = inputs.getItem(slot);
				if (stackInSlot.isEmpty())
					continue;
				pressingBehaviour.particleItems.add(stackInSlot);
			}
		}

		return true;
	}

	@Override
	protected void write(CompoundTag compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		if (getBehaviour(AdvancementBehaviour.TYPE).isOwnerPresent())
			compound.putInt("TracksCreated", tracksCreated);
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		tracksCreated = compound.getInt("TracksCreated");
	}

	@Override
	public boolean tryProcessInWorld(ItemEntity itemEntity, boolean simulate) {
		ItemStack item = itemEntity.getItem();
		Optional<PressingRecipe> recipe = getRecipe(item);
		if (!recipe.isPresent())
			return false;
		if (simulate)
			return true;

		ItemStack itemCreated = ItemStack.EMPTY;
		pressingBehaviour.particleItems.add(item);
		if (canProcessInBulk() || item.getCount() == 1) {
			InWorldProcessing.applyRecipeOn(itemEntity, recipe.get());
			itemCreated = itemEntity.getItem()
				.copy();
		} else {
			for (ItemStack result : InWorldProcessing.applyRecipeOn(ItemHandlerHelper.copyStackWithSize(item, 1),
				recipe.get())) {
				if (itemCreated.isEmpty())
					itemCreated = result.copy();
				ItemEntity created =
					new ItemEntity(level, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), result);
				created.setDefaultPickUpDelay();
				created.setDeltaMovement(VecHelper.offsetRandomly(Vec3.ZERO, Create.RANDOM, .05f));
				level.addFreshEntity(created);
			}
			item.shrink(1);
		}

		if (!itemCreated.isEmpty())
			onItemPressed(itemCreated);
		return true;
	}

	@Override
	public boolean tryProcessOnBelt(TransportedItemStack input, List<ItemStack> outputList, boolean simulate) {
		Optional<PressingRecipe> recipe = getRecipe(input.stack);
		if (!recipe.isPresent())
			return false;
		if (simulate)
			return true;
		pressingBehaviour.particleItems.add(input.stack);
		List<ItemStack> outputs = InWorldProcessing.applyRecipeOn(
			canProcessInBulk() ? input.stack : ItemHandlerHelper.copyStackWithSize(input.stack, 1), recipe.get());

		for (ItemStack created : outputs) {
			if (!created.isEmpty()) {
				onItemPressed(created);
				break;
			}
		}

		outputList.addAll(outputs);
		return true;
	}

	@Override
	public void onPressingCompleted() {
		if (pressingBehaviour.onBasin() && matchBasinRecipe(currentRecipe)
			&& getBasin().filter(BasinTileEntity::canContinueProcessing)
				.isPresent())
			startProcessingBasin();
		else
			basinChecker.scheduleUpdate();
	}

	private static final RecipeWrapper pressingInv = new RecipeWrapper(new ItemStackHandler(1));

	public Optional<PressingRecipe> getRecipe(ItemStack item) {
		Optional<PressingRecipe> assemblyRecipe =
			SequencedAssemblyRecipe.getRecipe(level, item, AllRecipeTypes.PRESSING.getType(), PressingRecipe.class);
		if (assemblyRecipe.isPresent())
			return assemblyRecipe;

		pressingInv.setItem(0, item);
		return AllRecipeTypes.PRESSING.find(pressingInv, level);
	}

	public static <C extends Container> boolean canCompress(Recipe<C> recipe) {
		if (!(recipe instanceof CraftingRecipe) || !AllConfigs.SERVER.recipes.allowShapedSquareInPress.get())
			return false;
		NonNullList<Ingredient> ingredients = recipe.getIngredients();
		return (ingredients.size() == 4 || ingredients.size() == 9) && ItemHelper.matchAllIngredients(ingredients);
	}

	@Override
	protected <C extends Container> boolean matchStaticFilters(Recipe<C> recipe) {
		return (recipe instanceof CraftingRecipe && !(recipe instanceof MechanicalCraftingRecipe) && canCompress(recipe)
			&& !AllRecipeTypes.shouldIgnoreInAutomation(recipe))
			|| recipe.getType() == AllRecipeTypes.COMPACTING.getType();
	}

	@Override
	public float getKineticSpeed() {
		return getSpeed();
	}

	@Override
	public boolean canProcessInBulk() {
		return AllConfigs.SERVER.recipes.bulkPressing.get();
	}

	@Override
	protected Object getRecipeCacheKey() {
		return compressingRecipesKey;
	}

	@Override
	public int getParticleAmount() {
		return 15;
	}

	@Override
	public void startProcessingBasin() {
		if (pressingBehaviour.running && pressingBehaviour.runningTicks <= PressingBehaviour.CYCLE / 2)
			return;
		super.startProcessingBasin();
		pressingBehaviour.start(Mode.BASIN);
	}

	@Override
	protected void onBasinRemoved() {
		pressingBehaviour.particleItems.clear();
		pressingBehaviour.running = false;
		pressingBehaviour.runningTicks = 0;
		sendData();
	}

	@Override
	protected boolean isRunning() {
		return pressingBehaviour.running;
	}

	@Override
	protected Optional<CreateAdvancement> getProcessedRecipeTrigger() {
		return Optional.of(AllAdvancements.COMPACTING);
	}

}
