package com.simibubi.create.content.curiosities.tools;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.commons.lang3.Validate;

import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.item.filter.FilterItem;
import com.simibubi.create.content.schematics.ISpecialEntityItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement.ItemUseType;
import com.simibubi.create.foundation.networking.ISyncPersistentData;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneDiodeBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.hooks.BasicEventHooks;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public class BlueprintEntity extends HangingEntity
	implements IEntityAdditionalSpawnData, ISpecialEntityItemRequirement, ISyncPersistentData {

	protected int size;
	protected Direction verticalOrientation;

	@SuppressWarnings("unchecked")
	public BlueprintEntity(EntityType<?> p_i50221_1_, World p_i50221_2_) {
		super((EntityType<? extends HangingEntity>) p_i50221_1_, p_i50221_2_);
		size = 1;
	}

	public BlueprintEntity(World world, BlockPos pos, Direction facing, Direction verticalOrientation) {
		super(AllEntityTypes.CRAFTING_BLUEPRINT.get(), world, pos);

		for (int size = 3; size > 0; size--) {
			this.size = size;
			this.updateFacingWithBoundingBox(facing, verticalOrientation);
			if (this.onValidSurface())
				break;
		}
	}

	public static EntityType.Builder<?> build(EntityType.Builder<?> builder) {
		@SuppressWarnings("unchecked")
		EntityType.Builder<BlueprintEntity> entityBuilder = (EntityType.Builder<BlueprintEntity>) builder;
		return entityBuilder;
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void writeAdditional(CompoundNBT p_213281_1_) {
		p_213281_1_.putByte("Facing", (byte) this.facingDirection.getIndex());
		p_213281_1_.putByte("Orientation", (byte) this.verticalOrientation.getIndex());
		p_213281_1_.putInt("Size", size);
		super.writeAdditional(p_213281_1_);
	}

	@Override
	public void readAdditional(CompoundNBT p_70037_1_) {
		this.facingDirection = Direction.byIndex(p_70037_1_.getByte("Facing"));
		this.verticalOrientation = Direction.byIndex(p_70037_1_.getByte("Orientation"));
		this.size = p_70037_1_.getInt("Size");
		super.readAdditional(p_70037_1_);
		this.updateFacingWithBoundingBox(this.facingDirection, this.verticalOrientation);
	}

	protected void updateFacingWithBoundingBox(Direction facing, Direction verticalOrientation) {
		Validate.notNull(facing);
		this.facingDirection = facing;
		this.verticalOrientation = verticalOrientation;
		if (facing.getAxis()
			.isHorizontal()) {
			this.rotationPitch = 0.0F;
			this.rotationYaw = (float) (this.facingDirection.getHorizontalIndex() * 90);
		} else {
			this.rotationPitch = (float) (-90 * facing.getAxisDirection()
				.getOffset());
			this.rotationYaw = verticalOrientation.getAxis()
				.isHorizontal() ? 180 + verticalOrientation.getHorizontalAngle() : 0;
		}

		this.prevRotationPitch = this.rotationPitch;
		this.prevRotationYaw = this.rotationYaw;
		this.updateBoundingBox();
	}

	@Override
	protected float getEyeHeight(Pose p_213316_1_, EntitySize p_213316_2_) {
		return 0;
	}

	@Override
	protected void updateBoundingBox() {
		if (this.facingDirection == null)
			return;
		if (this.verticalOrientation == null)
			return;

		Vector3d pos = Vector3d.of(hangingPosition)
			.add(.5, .5, .5)
			.subtract(Vector3d.of(facingDirection.getDirectionVec())
				.scale(0.46875));
		double d1 = pos.x;
		double d2 = pos.y;
		double d3 = pos.z;
		this.setPos(d1, d2, d3);

		Axis axis = facingDirection.getAxis();
		if (size == 2)
			pos = pos.add(Vector3d.of(axis.isHorizontal() ? facingDirection.rotateYCCW()
				.getDirectionVec()
				: verticalOrientation.rotateY()
					.getDirectionVec())
				.scale(0.5))
				.add(Vector3d
					.of(axis.isHorizontal() ? Direction.UP.getDirectionVec()
						: facingDirection == Direction.UP ? verticalOrientation.getDirectionVec()
							: verticalOrientation.getOpposite()
								.getDirectionVec())
					.scale(0.5));

		d1 = pos.x;
		d2 = pos.y;
		d3 = pos.z;

		double d4 = (double) this.getWidthPixels();
		double d5 = (double) this.getHeightPixels();
		double d6 = (double) this.getWidthPixels();
		Direction.Axis direction$axis = this.facingDirection.getAxis();
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
		this.setBoundingBox(new AxisAlignedBB(d1 - d4, d2 - d5, d3 - d6, d1 + d4, d2 + d5, d3 + d6));
	}

	public boolean onValidSurface() {
		if (!world.isSpaceEmpty(this))
			return false;

		int i = Math.max(1, this.getWidthPixels() / 16);
		int j = Math.max(1, this.getHeightPixels() / 16);
		BlockPos blockpos = this.hangingPosition.offset(this.facingDirection.getOpposite());
		Direction upDirection = facingDirection.getAxis()
			.isHorizontal() ? Direction.UP
				: facingDirection == Direction.UP ? verticalOrientation : verticalOrientation.getOpposite();
		Direction direction = facingDirection.getAxis()
			.isVertical() ? verticalOrientation.rotateY() : facingDirection.rotateYCCW();
		BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

		for (int k = 0; k < i; ++k) {
			for (int l = 0; l < j; ++l) {
				int i1 = (i - 1) / -2;
				int j1 = (j - 1) / -2;
				blockpos$mutable.setPos(blockpos)
					.move(direction, k + i1)
					.move(upDirection, l + j1);
				BlockState blockstate = this.world.getBlockState(blockpos$mutable);
				if (Block.hasEnoughSolidSide(this.world, blockpos$mutable, this.facingDirection))
					continue;
				if (!blockstate.getMaterial()
					.isSolid() && !RedstoneDiodeBlock.isDiode(blockstate)) {
					return false;
				}
			}
		}

		return this.world.getEntitiesInAABBexcluding(this, this.getBoundingBox(), IS_HANGING_ENTITY)
			.isEmpty();
	}

	@Override
	public int getWidthPixels() {
		return 16 * size;
	}

	@Override
	public int getHeightPixels() {
		return 16 * size;
	}

	@Override
	public void onBroken(@Nullable Entity p_110128_1_) {
		if (!world.getGameRules()
			.getBoolean(GameRules.DO_ENTITY_DROPS))
			return;

		playSound(SoundEvents.ENTITY_PAINTING_BREAK, 1.0F, 1.0F);
		if (p_110128_1_ instanceof PlayerEntity) {
			PlayerEntity playerentity = (PlayerEntity) p_110128_1_;
			if (playerentity.abilities.isCreativeMode) {
				return;
			}
		}

		entityDropItem(AllItems.CRAFTING_BLUEPRINT.asStack());
	}

	@Override
	public ItemStack getPickedResult(RayTraceResult target) {
		return AllItems.CRAFTING_BLUEPRINT.asStack();
	}

	@Override
	public ItemRequirement getRequiredItems() {
		return new ItemRequirement(ItemUseType.CONSUME, AllItems.CRAFTING_BLUEPRINT.get());
	}

	@Override
	public void playPlaceSound() {
		this.playSound(SoundEvents.ENTITY_PAINTING_PLACE, 1.0F, 1.0F);
	}

	@Override
	public void setLocationAndAngles(double p_70012_1_, double p_70012_3_, double p_70012_5_, float p_70012_7_,
		float p_70012_8_) {
		this.setPosition(p_70012_1_, p_70012_3_, p_70012_5_);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void setPositionAndRotationDirect(double p_180426_1_, double p_180426_3_, double p_180426_5_,
		float p_180426_7_, float p_180426_8_, int p_180426_9_, boolean p_180426_10_) {
		BlockPos blockpos =
			this.hangingPosition.add(p_180426_1_ - this.getX(), p_180426_3_ - this.getY(), p_180426_5_ - this.getZ());
		this.setPosition((double) blockpos.getX(), (double) blockpos.getY(), (double) blockpos.getZ());
	}

	@Override
	public void writeSpawnData(PacketBuffer buffer) {
		CompoundNBT compound = new CompoundNBT();
		writeAdditional(compound);
		buffer.writeCompoundTag(compound);
		buffer.writeCompoundTag(getPersistentData());
	}

	@Override
	public void readSpawnData(PacketBuffer additionalData) {
		readAdditional(additionalData.readCompoundTag());
		getPersistentData().merge(additionalData.readCompoundTag());
	}

	@Override
	public ActionResultType applyPlayerInteraction(PlayerEntity player, Vector3d vec, Hand hand) {
		if (player instanceof FakePlayer)
			return ActionResultType.PASS;

		BlueprintSection section = getSectionAt(vec);

		if (!AllItems.WRENCH.isIn(player.getHeldItem(hand)) && !world.isRemote) {
			boolean empty = true;
			ItemStackHandler items = section.getItems();
			for (int i = 0; i < 9; i++) {
				if (!items.getStackInSlot(i)
					.isEmpty()) {
					empty = false;
					break;
				}
			}

			if (!empty) {
				IItemHandlerModifiable playerInv = new InvWrapper(player.inventory);
				boolean firstPass = true;
				int amountCrafted = 0;
				ForgeHooks.setCraftingPlayer(player);
				Optional<ICraftingRecipe> recipe = Optional.empty();

				do {
					Map<Integer, ItemStack> stacksTaken = new HashMap<>();
					Map<Integer, ItemStack> craftingGrid = new HashMap<>();
					boolean success = true;

					Search: for (int i = 0; i < 9; i++) {
						ItemStack requestedItem = items.getStackInSlot(i);
						if (requestedItem.isEmpty()) {
							craftingGrid.put(i, ItemStack.EMPTY);
							continue;
						}

						for (int slot = 0; slot < playerInv.getSlots(); slot++) {
							if (!FilterItem.test(world, playerInv.getStackInSlot(slot), requestedItem))
								continue;
							ItemStack currentItem = playerInv.extractItem(slot, 1, false);
							if (stacksTaken.containsKey(slot)) {
								stacksTaken.get(slot)
									.grow(1);
							} else {
								stacksTaken.put(slot, currentItem.copy());
							}
							craftingGrid.put(i, currentItem);
							continue Search;
						}

						success = false;
						break;
					}

					if (success) {
						CraftingInventory craftingInventory = new BlueprintCraftingInventory(craftingGrid);

						if (!recipe.isPresent())
							recipe = world.getRecipeManager()
								.getRecipe(IRecipeType.CRAFTING, craftingInventory, world);
						ItemStack result = recipe.filter(r -> r.matches(craftingInventory, world))
							.map(r -> r.getCraftingResult(craftingInventory))
							.orElse(ItemStack.EMPTY);

						if (result.isEmpty()) {
							success = false;
						} else if (result.getCount() + amountCrafted > 64) {
							success = false;
						} else {
							amountCrafted += result.getCount();
							result.onCrafting(player.world, player, 1);
							BasicEventHooks.firePlayerCraftingEvent(player, result, craftingInventory);
							NonNullList<ItemStack> nonnulllist = world.getRecipeManager()
								.getRecipeNonNull(IRecipeType.CRAFTING, craftingInventory, world);

							if (firstPass)
								world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ITEM_PICKUP,
									SoundCategory.PLAYERS, .2f, 1f + Create.RANDOM.nextFloat());
							player.inventory.placeItemBackInInventory(world, result);
							for (ItemStack itemStack : nonnulllist)
								player.inventory.placeItemBackInInventory(world, itemStack);
							firstPass = false;
						}
					}

					if (!success) {
						for (Entry<Integer, ItemStack> entry : stacksTaken.entrySet())
							playerInv.insertItem(entry.getKey(), entry.getValue(), false);
						break;
					}

				} while (player.isSneaking());
				ForgeHooks.setCraftingPlayer(null);

				return ActionResultType.SUCCESS;
			}
		}

		int i = section.index;
		if (!world.isRemote && player instanceof ServerPlayerEntity) {
			NetworkHooks.openGui((ServerPlayerEntity) player, section, buf -> {
				buf.writeVarInt(getEntityId());
				buf.writeVarInt(i);
			});
		}

		return ActionResultType.SUCCESS;
	}

	public BlueprintSection getSectionAt(Vector3d vec) {
		int index = 0;
		if (size > 1) {
			vec = VecHelper.rotate(vec, rotationYaw, Axis.Y);
			vec = VecHelper.rotate(vec, -rotationPitch, Axis.X);
			vec = vec.add(0.5, 0.5, 0);
			if (size == 3)
				vec = vec.add(1, 1, 0);
			int x = MathHelper.clamp(MathHelper.floor(vec.x), 0, size - 1);
			int y = MathHelper.clamp(MathHelper.floor(vec.y), 0, size - 1);
			index = x + y * size;
		}

		BlueprintSection section = getSection(index);
		return section;
	}

	static class BlueprintCraftingInventory extends CraftingInventory {

		private static Container dummyContainer = new Container(null, -1) {
			public boolean canInteractWith(PlayerEntity playerIn) {
				return false;
			}
		};

		public BlueprintCraftingInventory(Map<Integer, ItemStack> items) {
			super(dummyContainer, 3, 3);
			for (int y = 0; y < 3; y++) {
				for (int x = 0; x < 3; x++) {
					ItemStack stack = items.get(y * 3 + x);
					setInventorySlotContents(y * 3 + x, stack == null ? ItemStack.EMPTY : stack.copy());
				}
			}
		}

	}

	public CompoundNBT getOrCreateRecipeCompound() {
		CompoundNBT persistentData = getPersistentData();
		if (!persistentData.contains("Recipes"))
			persistentData.put("Recipes", new CompoundNBT());
		return persistentData.getCompound("Recipes");
	}

	private Map<Integer, BlueprintSection> sectionCache = new HashMap<>();

	public BlueprintSection getSection(int index) {
		return sectionCache.computeIfAbsent(index, i -> new BlueprintSection(i));
	}

	class BlueprintSection implements INamedContainerProvider {
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
			CompoundNBT list = getOrCreateRecipeCompound();
			CompoundNBT invNBT = list.getCompound(index + "");
			inferredIcon = list.getBoolean("InferredIcon");
			if (!invNBT.isEmpty())
				newInv.deserializeNBT(invNBT);
			return newInv;
		}

		public void save(ItemStackHandler inventory) {
			CompoundNBT list = getOrCreateRecipeCompound();
			list.put(index + "", inventory.serializeNBT());
			list.putBoolean("InferredIcon", inferredIcon);
			cachedDisplayItems = null;
			if (!world.isRemote)
				syncPersistentDataWithTracking(BlueprintEntity.this);
		}

		public boolean isEntityAlive() {
			return isAlive();
		}

		public World getBlueprintWorld() {
			return world;
		}

		@Override
		public Container createMenu(int id, PlayerInventory inv, PlayerEntity player) {
			return BlueprintContainer.create(id, inv, this);
		}

		@Override
		public ITextComponent getDisplayName() {
			return new TranslationTextComponent(AllItems.CRAFTING_BLUEPRINT.get()
				.getTranslationKey());
		}

	}

	@Override
	public void onPersistentDataUpdated() {
		sectionCache.clear();
	}

}