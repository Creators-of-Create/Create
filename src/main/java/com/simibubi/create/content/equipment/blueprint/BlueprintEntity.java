package com.simibubi.create.content.equipment.blueprint;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.commons.lang3.Validate;

import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.content.schematics.requirement.ISpecialEntityItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement.ItemUseType;
import com.simibubi.create.foundation.networking.ISyncPersistentData;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.IInteractionChecker;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
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
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.network.NetworkHooks;

public class BlueprintEntity extends HangingEntity
	implements IEntityAdditionalSpawnData, ISpecialEntityItemRequirement, ISyncPersistentData, IInteractionChecker {

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
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag p_213281_1_) {
		p_213281_1_.putByte("Facing", (byte) this.direction.get3DDataValue());
		p_213281_1_.putByte("Orientation", (byte) this.verticalOrientation.get3DDataValue());
		p_213281_1_.putInt("Size", size);
		super.addAdditionalSaveData(p_213281_1_);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag p_70037_1_) {
		if (p_70037_1_.contains("Facing", Tag.TAG_ANY_NUMERIC)) {
			this.direction = Direction.from3DDataValue(p_70037_1_.getByte("Facing"));
			this.verticalOrientation = Direction.from3DDataValue(p_70037_1_.getByte("Orientation"));
			this.size = p_70037_1_.getInt("Size");
		} else {
			this.direction = Direction.SOUTH;
			this.verticalOrientation = Direction.DOWN;
			this.size = 1;
		}
		super.readAdditionalSaveData(p_70037_1_);
		this.updateFacingWithBoundingBox(this.direction, this.verticalOrientation);
	}

	protected void updateFacingWithBoundingBox(Direction facing, Direction verticalOrientation) {
		Validate.notNull(facing);
		this.direction = facing;
		this.verticalOrientation = verticalOrientation;
		if (facing.getAxis()
			.isHorizontal()) {
			setXRot(0.0F);
			setYRot(this.direction.get2DDataValue() * 90);
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
	protected float getEyeHeight(Pose p_213316_1_, EntityDimensions p_213316_2_) {
		return 0;
	}

	@Override
	protected void recalculateBoundingBox() {
		if (this.direction == null)
			return;
		if (this.verticalOrientation == null)
			return;

		Vec3 pos = Vec3.atLowerCornerOf(getPos())
			.add(.5, .5, .5)
			.subtract(Vec3.atLowerCornerOf(direction.getNormal())
				.scale(0.46875));
		double d1 = pos.x;
		double d2 = pos.y;
		double d3 = pos.z;
		this.setPosRaw(d1, d2, d3);

		Axis axis = direction.getAxis();
		if (size == 2)
			pos = pos.add(Vec3.atLowerCornerOf(axis.isHorizontal() ? direction.getCounterClockWise()
				.getNormal()
				: verticalOrientation.getClockWise()
					.getNormal())
				.scale(0.5))
				.add(Vec3
					.atLowerCornerOf(axis.isHorizontal() ? Direction.UP.getNormal()
						: direction == Direction.UP ? verticalOrientation.getNormal()
							: verticalOrientation.getOpposite()
								.getNormal())
					.scale(0.5));

		d1 = pos.x;
		d2 = pos.y;
		d3 = pos.z;

		double d4 = (double) this.getWidth();
		double d5 = (double) this.getHeight();
		double d6 = (double) this.getWidth();
		Direction.Axis direction$axis = this.direction.getAxis();
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
		this.setBoundingBox(new AABB(d1 - d4, d2 - d5, d3 - d6, d1 + d4, d2 + d5, d3 + d6));
	}

	@Override
	public boolean survives() {
		if (!level.noCollision(this))
			return false;

		int i = Math.max(1, this.getWidth() / 16);
		int j = Math.max(1, this.getHeight() / 16);
		BlockPos blockpos = this.pos.relative(this.direction.getOpposite());
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
				BlockState blockstate = this.level.getBlockState(blockpos$mutable);
				if (Block.canSupportCenter(this.level, blockpos$mutable, this.direction))
					continue;
				if (!blockstate.getMaterial()
					.isSolid() && !DiodeBlock.isDiode(blockstate)) {
					return false;
				}
			}
		}

		return this.level.getEntities(this, this.getBoundingBox(), HANGING_ENTITY)
			.isEmpty();
	}

	@Override
	public int getWidth() {
		return 16 * size;
	}

	@Override
	public int getHeight() {
		return 16 * size;
	}

	@Override
	public boolean skipAttackInteraction(Entity source) {
		if (!(source instanceof Player) || level.isClientSide)
			return super.skipAttackInteraction(source);

		Player player = (Player) source;
		double attrib = player.getAttribute(ForgeMod.REACH_DISTANCE.get())
			.getValue() + (player.isCreative() ? 0 : -0.5F);

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
	public void dropItem(@Nullable Entity p_110128_1_) {
		if (!level.getGameRules()
			.getBoolean(GameRules.RULE_DOENTITYDROPS))
			return;

		playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);
		if (p_110128_1_ instanceof Player) {
			Player playerentity = (Player) p_110128_1_;
			if (playerentity.getAbilities().instabuild)
				return;
		}

		spawnAtLocation(AllItems.CRAFTING_BLUEPRINT.asStack());
	}

	@Override
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

	@Override
	public void moveTo(double p_70012_1_, double p_70012_3_, double p_70012_5_, float p_70012_7_, float p_70012_8_) {
		this.setPos(p_70012_1_, p_70012_3_, p_70012_5_);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void lerpTo(double p_180426_1_, double p_180426_3_, double p_180426_5_, float p_180426_7_, float p_180426_8_,
		int p_180426_9_, boolean p_180426_10_) {
		BlockPos blockpos =
			this.pos.offset(p_180426_1_ - this.getX(), p_180426_3_ - this.getY(), p_180426_5_ - this.getZ());
		this.setPos((double) blockpos.getX(), (double) blockpos.getY(), (double) blockpos.getZ());
	}

	@Override
	public void writeSpawnData(FriendlyByteBuf buffer) {
		CompoundTag compound = new CompoundTag();
		addAdditionalSaveData(compound);
		buffer.writeNbt(compound);
		buffer.writeNbt(getPersistentData());
	}

	@Override
	public void readSpawnData(FriendlyByteBuf additionalData) {
		readAdditionalSaveData(additionalData.readNbt());
		getPersistentData().merge(additionalData.readNbt());
	}

	@Override
	public InteractionResult interactAt(Player player, Vec3 vec, InteractionHand hand) {
		if (player instanceof FakePlayer)
			return InteractionResult.PASS;

		boolean holdingWrench = AllItems.WRENCH.isIn(player.getItemInHand(hand));
		BlueprintSection section = getSectionAt(vec);
		ItemStackHandler items = section.getItems();

		if (!holdingWrench && !level.isClientSide && !items.getStackInSlot(9)
			.isEmpty()) {

			IItemHandlerModifiable playerInv = new InvWrapper(player.getInventory());
			boolean firstPass = true;
			int amountCrafted = 0;
			ForgeHooks.setCraftingPlayer(player);
			Optional<CraftingRecipe> recipe = Optional.empty();

			do {
				Map<Integer, ItemStack> stacksTaken = new HashMap<>();
				Map<Integer, ItemStack> craftingGrid = new HashMap<>();
				boolean success = true;

				Search: for (int i = 0; i < 9; i++) {
					FilterItemStack requestedItem = FilterItemStack.of(items.getStackInSlot(i));
					if (requestedItem.isEmpty()) {
						craftingGrid.put(i, ItemStack.EMPTY);
						continue;
					}

					for (int slot = 0; slot < playerInv.getSlots(); slot++) {
						if (!requestedItem.test(level, playerInv.getStackInSlot(slot)))
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
						recipe = level.getRecipeManager()
							.getRecipeFor(RecipeType.CRAFTING, craftingInventory, level);
					ItemStack result = recipe.filter(r -> r.matches(craftingInventory, level))
						.map(r -> r.assemble(craftingInventory))
						.orElse(ItemStack.EMPTY);

					if (result.isEmpty()) {
						success = false;
					} else if (result.getCount() + amountCrafted > 64) {
						success = false;
					} else {
						amountCrafted += result.getCount();
						result.onCraftedBy(player.level, player, 1);
						ForgeEventFactory.firePlayerCraftingEvent(player, result, craftingInventory);
						NonNullList<ItemStack> nonnulllist = level.getRecipeManager()
							.getRemainingItemsFor(RecipeType.CRAFTING, craftingInventory, level);

						if (firstPass)
							level.playSound(null, player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS,
								.2f, 1f + Create.RANDOM.nextFloat());
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
			ForgeHooks.setCraftingPlayer(null);
			return InteractionResult.SUCCESS;
		}

		int i = section.index;
		if (!level.isClientSide && player instanceof ServerPlayer) {
			NetworkHooks.openGui((ServerPlayer) player, section, buf -> {
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

	static class BlueprintCraftingInventory extends CraftingContainer {

		private static final AbstractContainerMenu dummyContainer = new AbstractContainerMenu(null, -1) {
			public boolean stillValid(Player playerIn) {
				return false;
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
		return persistentData.getCompound("Recipes");
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
			CompoundTag invNBT = list.getCompound(index + "");
			inferredIcon = list.getBoolean("InferredIcon");
			if (!invNBT.isEmpty())
				newInv.deserializeNBT(invNBT);
			return newInv;
		}

		public void save(ItemStackHandler inventory) {
			CompoundTag list = getOrCreateRecipeCompound();
			list.put(index + "", inventory.serializeNBT());
			list.putBoolean("InferredIcon", inferredIcon);
			cachedDisplayItems = null;
			if (!level.isClientSide)
				syncPersistentDataWithTracking(BlueprintEntity.this);
		}

		public boolean isEntityAlive() {
			return isAlive();
		}

		public Level getBlueprintWorld() {
			return level;
		}

		@Override
		public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
			return BlueprintMenu.create(id, inv, this);
		}

		@Override
		public Component getDisplayName() {
			return AllItems.CRAFTING_BLUEPRINT.get()
				.getDescription();
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
		AABB box = getBoundingBox();

		double dx = 0;
		if (box.minX > player.getX()) {
			dx = box.minX - player.getX();
		} else if (player.getX() > box.maxX) {
			dx = player.getX() - box.maxX;
		}

		double dy = 0;
		if (box.minY > player.getY()) {
			dy = box.minY - player.getY();
		} else if (player.getY() > box.maxY) {
			dy = player.getY() - box.maxY;
		}

		double dz = 0;
		if (box.minZ > player.getZ()) {
			dz = box.minZ - player.getZ();
		} else if (player.getZ() > box.maxZ) {
			dz = player.getZ() - box.maxZ;
		}

		return (dx * dx + dy * dy + dz * dz) <= 64.0D;
	}

}
