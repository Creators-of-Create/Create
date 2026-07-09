package com.simibubi.create.content.equipment.blueprint;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.AllItems;
import com.simibubi.create.api.schematic.requirement.SpecialEntityItemRequirement;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement.ItemUseType;
import com.simibubi.create.foundation.networking.ISyncPersistentData;
import com.simibubi.create.foundation.utility.IInteractionChecker;
import com.simibubi.create.foundation.utility.LegacyBlockEntityTagBridge;

import net.createmod.catnip.api.data.Couple;
import net.createmod.catnip.api.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

public class BlueprintEntity extends HangingEntity
	implements IEntityWithComplexSpawn, SpecialEntityItemRequirement, ISyncPersistentData, IInteractionChecker {

	protected int size;
	protected Direction verticalOrientation;

	@SuppressWarnings("unchecked")
	public BlueprintEntity(EntityType<?> p_i50221_1_, Level p_i50221_2_) {
		super((EntityType<? extends HangingEntity>) p_i50221_1_, p_i50221_2_);
		size = 1;
	}

	public BlueprintEntity(Level world, BlockPos pos, Direction facing, Direction verticalOrientation) {
		super(AllEntityTypes.CRAFTING_BLUEPRINT.get(), world, pos);

		for (int size = 3; size > 0; size--) {
			this.size = size;
			this.updateFacingWithBoundingBox(facing, verticalOrientation);
			if (this.survives())
				break;
		}
	}

	public static EntityType.Builder<?> build(EntityType.Builder<?> builder) {
		@SuppressWarnings("unchecked")
		EntityType.Builder<BlueprintEntity> entityBuilder = (EntityType.Builder<BlueprintEntity>) builder;
		return entityBuilder;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		output.putByte("Facing", (byte) this.getDirection().get3DDataValue());
		output.putByte("Orientation", (byte) this.verticalOrientation.get3DDataValue());
		output.putInt("Size", size);
		super.addAdditionalSaveData(output);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		setDirectionRaw(Direction.from3DDataValue(input.getByteOr("Facing", (byte) Direction.SOUTH.get3DDataValue())));
		this.verticalOrientation = Direction.from3DDataValue(input.getByteOr("Orientation", (byte) Direction.DOWN.get3DDataValue()));
		this.size = input.getIntOr("Size", 1);
		super.readAdditionalSaveData(input);
		this.updateFacingWithBoundingBox(this.getDirection(), this.verticalOrientation);
	}

	protected void updateFacingWithBoundingBox(Direction facing, Direction verticalOrientation) {
		Objects.requireNonNull(facing);
		this.setDirectionRaw(facing);
		this.verticalOrientation = verticalOrientation;
		if (facing.getAxis()
			.isHorizontal()) {
			setXRot(0.0F);
			setYRot(this.getDirection().get2DDataValue() * 90);
		} else {
			setXRot(-90 * facing.getAxisDirection()
				.getStep());
			setYRot(verticalOrientation.getAxis()
				.isHorizontal() ? 180 + verticalOrientation.toYRot() : 0);
		}

		this.xRotO = getXRot();
		this.yRotO = getYRot();
		this.recalculateBoundingBox();
	}

	@Override
	public EntityDimensions getDimensions(Pose pose) {
		return super.getDimensions(pose).withEyeHeight(0);
	}

	@Override
	protected AABB calculateBoundingBox(BlockPos blockPos, Direction direction) {
		Vec3 pos = Vec3.atLowerCornerOf(getPos())
				.add(.5, .5, .5)
				.subtract(Vec3.atLowerCornerOf(direction.getUnitVec3i())
						.scale(0.46875));
		double d1 = pos.x;
		double d2 = pos.y;
		double d3 = pos.z;
		this.setPosRaw(d1, d2, d3);

		Axis axis = direction.getAxis();
		if (size == 2)
			pos = pos.add(Vec3.atLowerCornerOf(axis.isHorizontal() ? direction.getCounterClockWise()
									.getUnitVec3i()
									: verticalOrientation.getClockWise()
									.getUnitVec3i())
							.scale(0.5))
					.add(Vec3
							.atLowerCornerOf(axis.isHorizontal() ? Direction.UP.getUnitVec3i()
									: direction == Direction.UP ? verticalOrientation.getUnitVec3i()
									: verticalOrientation.getOpposite()
									.getUnitVec3i())
							.scale(0.5));

		d1 = pos.x;
		d2 = pos.y;
		d3 = pos.z;

		double d4 = (double) this.getWidth();
		double d5 = (double) this.getHeight();
		double d6 = (double) this.getWidth();
		Direction.Axis direction$axis = this.getDirection().getAxis();
		switch (direction$axis) {
			case X:
				d4 = 1.0D;
				break;
			case Y:
				d5 = 1.0D;
				break;
			case Z:
				d6 = 1.0D;
		}

		d4 = d4 / 32.0D;
		d5 = d5 / 32.0D;
		d6 = d6 / 32.0D;

		return new AABB(d1 - d4, d2 - d5, d3 - d6, d1 + d4, d2 + d5, d3 + d6);
	}

	@Override
	protected void recalculateBoundingBox() {
		if (this.getDirection() != null && this.verticalOrientation != null) {
			setBoundingBox(calculateBoundingBox(pos, this.getDirection()));
		}
	}
	@Override
	public void setPos(double pX, double pY, double pZ) {
		setPosRaw(pX, pY, pZ);
		super.setPos(pX, pY, pZ);
	}

	@Override
	public boolean survives() {
		if (!level().noCollision(this))
			return false;

		int i = Math.max(1, this.getWidth() / 16);
		int j = Math.max(1, this.getHeight() / 16);
		Direction direction = this.getDirection();
		BlockPos blockpos = this.pos.relative(direction.getOpposite());
		Direction upDirection = direction.getAxis()
			.isHorizontal() ? Direction.UP
			: direction == Direction.UP ? verticalOrientation : verticalOrientation.getOpposite();
		Direction newDirection = direction.getAxis()
			.isVertical() ? verticalOrientation.getClockWise() : direction.getCounterClockWise();
		BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();

		for (int k = 0; k < i; ++k) {
			for (int l = 0; l < j; ++l) {
				int i1 = (i - 1) / -2;
				int j1 = (j - 1) / -2;
				blockpos$mutable.set(blockpos)
					.move(newDirection, k + i1)
					.move(upDirection, l + j1);
				BlockState blockstate = this.level().getBlockState(blockpos$mutable);
				if (Block.canSupportCenter(this.level(), blockpos$mutable, direction))
					continue;
				if (!blockstate.isSolid() && !DiodeBlock.isDiode(blockstate)) {
					return false;
				}
			}
		}

		return this.level()
			.getEntitiesOfClass(HangingEntity.class, this.getBoundingBox(), entity -> entity != this)
			.isEmpty();
	}

	public int getWidth() {
		return 16 * size;
	}

	public int getHeight() {
		return 16 * size;
	}

	@Override
	public boolean skipAttackInteraction(Entity source) {
		if (!(source instanceof Player player) || level().isClientSide())
			return super.skipAttackInteraction(source);

		double attrib = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) + (player.isCreative() ? 0 : -0.5F);

		Vec3 eyePos = source.getEyePosition(1);
		Vec3 look = source.getViewVector(1);
		Vec3 target = eyePos.add(look.scale(attrib));

		Optional<Vec3> rayTrace = getBoundingBox().clip(eyePos, target);
		if (!rayTrace.isPresent())
			return super.skipAttackInteraction(source);

		Vec3 hitVec = rayTrace.get();
		BlueprintSection sectionAt = getSectionAt(hitVec.subtract(position()));
		ItemStackHandler items = sectionAt.getItems();

		if (items.getStackInSlot(9)
			.isEmpty())
			return super.skipAttackInteraction(source);
		for (int i = 0; i < items.getSlots(); i++)
			items.setStackInSlot(i, ItemStack.EMPTY);
		sectionAt.save(items);
		return true;
	}

	@Override
	public void dropItem(ServerLevel level, @Nullable Entity p_110128_1_) {
		if (!level.getGameRules()
			.get(GameRules.ENTITY_DROPS))
			return;

		playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);
		if (p_110128_1_ instanceof Player playerentity) {
			if (playerentity.getAbilities().instabuild)
				return;
		}

		spawnAtLocation(level, AllItems.CRAFTING_BLUEPRINT.asStack(), 0);
	}

	public ItemStack getPickedResult(HitResult target) {
		return AllItems.CRAFTING_BLUEPRINT.asStack();
	}

	@Override
	public ItemRequirement getRequiredItems() {
		return new ItemRequirement(ItemUseType.CONSUME, AllItems.CRAFTING_BLUEPRINT.get());
	}

	@Override
	public void playPlacementSound() {
		this.playSound(SoundEvents.PAINTING_PLACE, 1.0F, 1.0F);
	}

	public void moveTo(double p_70012_1_, double p_70012_3_, double p_70012_5_, float p_70012_7_, float p_70012_8_) {
		this.setPos(p_70012_1_, p_70012_3_, p_70012_5_);
	}

	public void lerpTo(double pX, double pY, double pZ, float pYRot, float pXRot, int pSteps) {
		BlockPos blockpos =
				this.pos.offset(BlockPos.containing(pX - this.getX(), pY - this.getY(), pZ - this.getZ()));
		this.setPos(blockpos.getX(), blockpos.getY(), blockpos.getZ());
	}

	@Override
	public void writeSpawnData(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		CompoundTag compound = LegacyBlockEntityTagBridge.output(registryAccess(), this::addAdditionalSaveData);
		registryFriendlyByteBuf.writeNbt(compound);
		registryFriendlyByteBuf.writeNbt(getPersistentData());
	}

	@Override
	public void readSpawnData(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		CompoundTag entityData = registryFriendlyByteBuf.readNbt();
		if (entityData != null)
			readAdditionalSaveData(LegacyBlockEntityTagBridge.input(entityData, registryAccess()));
		CompoundTag persistentData = registryFriendlyByteBuf.readNbt();
		if (persistentData != null)
			getPersistentData().merge(persistentData);
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand, Vec3 vec) {
		if (player instanceof FakePlayer)
			return InteractionResult.PASS;

		boolean holdingWrench = AllItems.WRENCH.isIn(player.getItemInHand(hand));
		BlueprintSection section = getSectionAt(vec);
		ItemStackHandler items = section.getItems();

		if (!holdingWrench && !level().isClientSide() && !items.getStackInSlot(9)
			.isEmpty()) {
			ServerLevel serverLevel = (ServerLevel) level();

			IItemHandlerModifiable playerInv = new InvWrapper(player.getInventory());
			boolean firstPass = true;
			int amountCrafted = 0;
			CommonHooks.setCraftingPlayer(player);
			Optional<RecipeHolder<CraftingRecipe>> recipe = Optional.empty();

			do {
				Map<Integer, ItemStack> stacksTaken = new HashMap<>();
				Map<Integer, ItemStack> craftingGrid = new HashMap<>();
				boolean success = true;

				Search:
				for (int i = 0; i < 9; i++) {
					FilterItemStack requestedItem = FilterItemStack.of(items.getStackInSlot(i));
					if (requestedItem.isEmpty()) {
						craftingGrid.put(i, ItemStack.EMPTY);
						continue;
					}

					for (int slot = 0; slot < playerInv.getSlots(); slot++) {
						if (!requestedItem.test(level(), playerInv.getStackInSlot(slot)))
							continue;
						ItemStack currentItem = playerInv.extractItem(slot, 1, false);
						if (stacksTaken.containsKey(slot))
							stacksTaken.get(slot)
								.grow(1);
						else
							stacksTaken.put(slot, currentItem.copy());
						craftingGrid.put(i, currentItem);
						continue Search;
					}

					success = false;
					break;
				}

				if (success) {
					CraftingContainer craftingInventory = new BlueprintCraftingInventory(craftingGrid);

					if (!recipe.isPresent())
						recipe = serverLevel.recipeAccess()
							.getRecipeFor(RecipeType.CRAFTING, craftingInventory.asCraftInput(), serverLevel);
					ItemStack result = recipe.filter(r -> r.value().matches(craftingInventory.asCraftInput(), level()))
						.map(r -> r.value().assemble(craftingInventory.asCraftInput()))
						.orElse(ItemStack.EMPTY);

					if (result.isEmpty()) {
						success = false;
					} else if (result.getCount() + amountCrafted > 64) {
						success = false;
					} else {
						amountCrafted += result.getCount();
						result.onCraftedBy(player, 1);
						EventHooks.firePlayerCraftingEvent(player, result, craftingInventory);
						NonNullList<ItemStack> nonnulllist = recipe.map(r -> r.value()
							.getRemainingItems(craftingInventory.asCraftInput()))
							.orElseGet(NonNullList::create);

						if (firstPass)
							level().playSound(null, player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS,
								.2f, 1f + level().getRandom().nextFloat());
						player.getInventory()
							.placeItemBackInInventory(result);
						for (ItemStack itemStack : nonnulllist)
							player.getInventory()
								.placeItemBackInInventory(itemStack);
						firstPass = false;
					}
				}

				if (!success) {
					for (Entry<Integer, ItemStack> entry : stacksTaken.entrySet())
						playerInv.insertItem(entry.getKey(), entry.getValue(), false);
					break;
				}

			} while (player.isShiftKeyDown());
			CommonHooks.setCraftingPlayer(null);
			return InteractionResult.SUCCESS;
		}

		int i = section.index;
		if (!level().isClientSide() && player instanceof ServerPlayer) {
			player.openMenu(section, buf -> {
				buf.writeVarInt(getId());
				buf.writeVarInt(i);
			});
		}

		return InteractionResult.SUCCESS;
	}

	public BlueprintSection getSectionAt(Vec3 vec) {
		int index = 0;
		if (size > 1) {
			vec = VecHelper.rotate(vec, getYRot(), Axis.Y);
			vec = VecHelper.rotate(vec, -getXRot(), Axis.X);
			vec = vec.add(0.5, 0.5, 0);
			if (size == 3)
				vec = vec.add(1, 1, 0);
			int x = Mth.clamp(Mth.floor(vec.x), 0, size - 1);
			int y = Mth.clamp(Mth.floor(vec.y), 0, size - 1);
			index = x + y * size;
		}

		BlueprintSection section = getSection(index);
		return section;
	}

	static class BlueprintCraftingInventory extends TransientCraftingContainer {

		private static final AbstractContainerMenu dummyContainer = new AbstractContainerMenu(null, -1) {
			public boolean stillValid(Player playerIn) {
				return false;
			}

			@Override
			public ItemStack quickMoveStack(Player p_38941_, int p_38942_) {
				return ItemStack.EMPTY;
			}
		};

		public BlueprintCraftingInventory(Map<Integer, ItemStack> items) {
			super(dummyContainer, 3, 3);
			for (int y = 0; y < 3; y++) {
				for (int x = 0; x < 3; x++) {
					ItemStack stack = items.get(y * 3 + x);
					setItem(y * 3 + x, stack == null ? ItemStack.EMPTY : stack.copy());
				}
			}
		}

	}

	public CompoundTag getOrCreateRecipeCompound() {
		CompoundTag persistentData = getPersistentData();
		if (!persistentData.contains("Recipes"))
			persistentData.put("Recipes", new CompoundTag());
		return persistentData.getCompoundOrEmpty("Recipes");
	}

	private Map<Integer, BlueprintSection> sectionCache = new HashMap<>();

	public BlueprintSection getSection(int index) {
		return sectionCache.computeIfAbsent(index, i -> new BlueprintSection(i));
	}

	class BlueprintSection implements MenuProvider, IInteractionChecker {
		int index;
		Couple<ItemStack> cachedDisplayItems;
		public boolean inferredIcon = false;

		public BlueprintSection(int index) {
			this.index = index;
		}

		public Couple<ItemStack> getDisplayItems() {
			if (cachedDisplayItems != null)
				return cachedDisplayItems;
			ItemStackHandler items = getItems();
			return cachedDisplayItems = Couple.create(items.getStackInSlot(9), items.getStackInSlot(10));
		}

		public ItemStackHandler getItems() {
			ItemStackHandler newInv = new ItemStackHandler(11);
			CompoundTag list = getOrCreateRecipeCompound();
			CompoundTag invNBT = list.getCompoundOrEmpty(index + "");
			inferredIcon = list.getBooleanOr("InferredIcon", false);
			if (!invNBT.isEmpty())
				newInv.deserialize(LegacyBlockEntityTagBridge.input(invNBT, registryAccess()));
			return newInv;
		}

		public void save(ItemStackHandler inventory) {
			CompoundTag list = getOrCreateRecipeCompound();
			list.put(index + "", LegacyBlockEntityTagBridge.output(registryAccess(), inventory::serialize));
			list.putBoolean("InferredIcon", inferredIcon);
			cachedDisplayItems = null;
			if (!level().isClientSide())
				syncPersistentDataWithTracking(BlueprintEntity.this);
		}

		public boolean isEntityAlive() {
			return isAlive();
		}

		public Level getBlueprintWorld() {
			return level();
		}

		@Override
		public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
			return BlueprintMenu.create(id, inv, this);
		}

		@Override
		public Component getDisplayName() {
			return AllItems.CRAFTING_BLUEPRINT.get()
				.getName(AllItems.CRAFTING_BLUEPRINT.asStack());
		}

		@Override
		public boolean canPlayerUse(Player player) {
			return BlueprintEntity.this.canPlayerUse(player);
		}

	}

	@Override
	public void onPersistentDataUpdated() {
		sectionCache.clear();
	}

	@Override
	public boolean canPlayerUse(Player player) {
		return player.isWithinEntityInteractionRange(this, 8);
	}

}
