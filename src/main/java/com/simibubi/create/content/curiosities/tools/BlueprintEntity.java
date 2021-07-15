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
import com.simibubi.create.foundation.gui.IInteractionChecker;
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
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.hooks.BasicEventHooks;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public class BlueprintEntity extends HangingEntity
	implements IEntityAdditionalSpawnData, ISpecialEntityItemRequirement, ISyncPersistentData, IInteractionChecker {

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
	public IPacket<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void addAdditionalSaveData(CompoundNBT p_213281_1_) {
		p_213281_1_.putByte("Facing", (byte) this.direction.get3DDataValue());
		p_213281_1_.putByte("Orientation", (byte) this.verticalOrientation.get3DDataValue());
		p_213281_1_.putInt("Size", size);
		super.addAdditionalSaveData(p_213281_1_);
	}

	@Override
	public void readAdditionalSaveData(CompoundNBT p_70037_1_) {
		this.direction = Direction.from3DDataValue(p_70037_1_.getByte("Facing"));
		this.verticalOrientation = Direction.from3DDataValue(p_70037_1_.getByte("Orientation"));
		this.size = p_70037_1_.getInt("Size");
		super.readAdditionalSaveData(p_70037_1_);
		this.updateFacingWithBoundingBox(this.direction, this.verticalOrientation);
	}

	protected void updateFacingWithBoundingBox(Direction facing, Direction verticalOrientation) {
		Validate.notNull(facing);
		this.direction = facing;
		this.verticalOrientation = verticalOrientation;
		if (facing.getAxis()
			.isHorizontal()) {
			this.xRot = 0.0F;
			this.yRot = (float) (this.direction.get2DDataValue() * 90);
		} else {
			this.xRot = (float) (-90 * facing.getAxisDirection()
				.getStep());
			this.yRot = verticalOrientation.getAxis()
				.isHorizontal() ? 180 + verticalOrientation.toYRot() : 0;
		}

		this.xRotO = this.xRot;
		this.yRotO = this.yRot;
		this.recalculateBoundingBox();
	}

	@Override
	protected float getEyeHeight(Pose p_213316_1_, EntitySize p_213316_2_) {
		return 0;
	}

	@Override
	protected void recalculateBoundingBox() {
		if (this.direction == null)
			return;
		if (this.verticalOrientation == null)
			return;

		Vector3d pos = Vector3d.atLowerCornerOf(blockPosition())
			.add(.5, .5, .5)
			.subtract(Vector3d.atLowerCornerOf(direction.getNormal())
				.scale(0.46875));
		double d1 = pos.x;
		double d2 = pos.y;
		double d3 = pos.z;
		this.setPosRaw(d1, d2, d3);

		Axis axis = direction.getAxis();
		if (size == 2)
			pos = pos.add(Vector3d.atLowerCornerOf(axis.isHorizontal() ? direction.getCounterClockWise()
				.getNormal()
				: verticalOrientation.getClockWise()
					.getNormal())
				.scale(0.5))
				.add(Vector3d
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
		this.setBoundingBox(new AxisAlignedBB(d1 - d4, d2 - d5, d3 - d6, d1 + d4, d2 + d5, d3 + d6));
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
		BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

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
					.isSolid() && !RedstoneDiodeBlock.isDiode(blockstate)) {
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
		if (!(source instanceof PlayerEntity) || level.isClientSide)
			return super.skipAttackInteraction(source);

		PlayerEntity player = (PlayerEntity) source;
		double attrib = player.getAttribute(ForgeMod.REACH_DISTANCE.get())
			.getValue() + (player.isCreative() ? 0 : -0.5F);

		Vector3d eyePos = source.getEyePosition(1);
		Vector3d look = source.getViewVector(1);
		Vector3d target = eyePos.add(look.scale(attrib));

		Optional<Vector3d> rayTrace = getBoundingBox().clip(eyePos, target);
		if (!rayTrace.isPresent())
			return super.skipAttackInteraction(source);

		Vector3d hitVec = rayTrace.get();
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
		if (p_110128_1_ instanceof PlayerEntity) {
			PlayerEntity playerentity = (PlayerEntity) p_110128_1_;
			if (playerentity.abilities.instabuild) {
				return;
			}
		}

		spawnAtLocation(AllItems.CRAFTING_BLUEPRINT.asStack());
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
	public void playPlacementSound() {
		this.playSound(SoundEvents.PAINTING_PLACE, 1.0F, 1.0F);
	}

	@Override
	public void moveTo(double p_70012_1_, double p_70012_3_, double p_70012_5_, float p_70012_7_,
		float p_70012_8_) {
		this.setPos(p_70012_1_, p_70012_3_, p_70012_5_);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void lerpTo(double p_180426_1_, double p_180426_3_, double p_180426_5_,
		float p_180426_7_, float p_180426_8_, int p_180426_9_, boolean p_180426_10_) {
		BlockPos blockpos =
			this.pos.offset(p_180426_1_ - this.getX(), p_180426_3_ - this.getY(), p_180426_5_ - this.getZ());
		this.setPos((double) blockpos.getX(), (double) blockpos.getY(), (double) blockpos.getZ());
	}

	@Override
	public void writeSpawnData(PacketBuffer buffer) {
		CompoundNBT compound = new CompoundNBT();
		addAdditionalSaveData(compound);
		buffer.writeNbt(compound);
		buffer.writeNbt(getPersistentData());
	}

	@Override
	public void readSpawnData(PacketBuffer additionalData) {
		readAdditionalSaveData(additionalData.readNbt());
		getPersistentData().merge(additionalData.readNbt());
	}

	@Override
	public ActionResultType interactAt(PlayerEntity player, Vector3d vec, Hand hand) {
		if (player instanceof FakePlayer)
			return ActionResultType.PASS;

		boolean holdingWrench = AllItems.WRENCH.isIn(player.getItemInHand(hand));
		BlueprintSection section = getSectionAt(vec);
		ItemStackHandler items = section.getItems();

		if (!holdingWrench && !level.isClientSide && !items.getStackInSlot(9)
			.isEmpty()) {

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
						if (!FilterItem.test(level, playerInv.getStackInSlot(slot), requestedItem))
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
					CraftingInventory craftingInventory = new BlueprintCraftingInventory(craftingGrid);

					if (!recipe.isPresent())
						recipe = level.getRecipeManager()
							.getRecipeFor(IRecipeType.CRAFTING, craftingInventory, level);
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
						BasicEventHooks.firePlayerCraftingEvent(player, result, craftingInventory);
						NonNullList<ItemStack> nonnulllist = level.getRecipeManager()
							.getRemainingItemsFor(IRecipeType.CRAFTING, craftingInventory, level);

						if (firstPass)
							level.playSound(null, player.blockPosition(), SoundEvents.ITEM_PICKUP,
								SoundCategory.PLAYERS, .2f, 1f + Create.RANDOM.nextFloat());
						player.inventory.placeItemBackInInventory(level, result);
						for (ItemStack itemStack : nonnulllist)
							player.inventory.placeItemBackInInventory(level, itemStack);
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
			return ActionResultType.SUCCESS;
		}

		int i = section.index;
		if (!level.isClientSide && player instanceof ServerPlayerEntity) {
			NetworkHooks.openGui((ServerPlayerEntity) player, section, buf -> {
				buf.writeVarInt(getId());
				buf.writeVarInt(i);
			});
		}

		return ActionResultType.SUCCESS;
	}

	public BlueprintSection getSectionAt(Vector3d vec) {
		int index = 0;
		if (size > 1) {
			vec = VecHelper.rotate(vec, yRot, Axis.Y);
			vec = VecHelper.rotate(vec, -xRot, Axis.X);
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
			public boolean stillValid(PlayerEntity playerIn) {
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

	class BlueprintSection implements INamedContainerProvider, IInteractionChecker {
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
			if (!level.isClientSide)
				syncPersistentDataWithTracking(BlueprintEntity.this);
		}

		public boolean isEntityAlive() {
			return isAlive();
		}

		public World getBlueprintWorld() {
			return level;
		}

		@Override
		public Container createMenu(int id, PlayerInventory inv, PlayerEntity player) {
			return BlueprintContainer.create(id, inv, this);
		}

		@Override
		public ITextComponent getDisplayName() {
			return new TranslationTextComponent(AllItems.CRAFTING_BLUEPRINT.get()
				.getDescriptionId());
		}

		@Override
		public boolean canPlayerUse(PlayerEntity player) {
			return BlueprintEntity.this.canPlayerUse(player);
		}

	}

	@Override
	public void onPersistentDataUpdated() {
		sectionCache.clear();
	}

	@Override
	public boolean canPlayerUse(PlayerEntity player) {
		AxisAlignedBB box = getBoundingBox();

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
