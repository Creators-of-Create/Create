package com.simibubi.create.content.logistics.packagePort.frogport;

import java.util.List;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.api.equipment.goggles.IHaveHoveringInformation;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.compat.computercraft.ComputerCraftProxy;
import com.simibubi.create.compat.computercraft.events.PackageEvent;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.content.logistics.packagePort.PackagePortBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagerItemHandler;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.item.TooltipHelper;

import dan200.computercraft.api.peripheral.PeripheralCapability;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.animation.LerpedFloat.Chaser;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public class FrogportBlockEntity extends PackagePortBlockEntity implements IHaveHoveringInformation {

	public ItemStack animatedPackage;
	public LerpedFloat manualOpenAnimationProgress;
	public LerpedFloat animationProgress;
	public LerpedFloat anticipationProgress;
	public boolean currentlyDepositing;
	public boolean goggles;

	public boolean sendAnticipate;

	public float passiveYaw;

	private boolean failedLastExport;
	private FrogportSounds sounds;

	private ItemStack deferAnimationStart;
	private boolean deferAnimationInward;

	private AdvancementBehaviour advancements;

	public AbstractComputerBehaviour computerBehaviour;

	public FrogportBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		sounds = new FrogportSounds();
		animationProgress = LerpedFloat.linear();
		anticipationProgress = LerpedFloat.linear();
		manualOpenAnimationProgress = LerpedFloat.linear()
			.startWithValue(0)
			.chase(0, 0.35, Chaser.LINEAR);
		goggles = false;
	}

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(
			Capabilities.ItemHandler.BLOCK,
			AllBlockEntityTypes.PACKAGE_FROGPORT.get(),
			(be, context) -> be.itemHandler
		);

		if (Mods.COMPUTERCRAFT.isLoaded()) {
			event.registerBlockEntity(
				PeripheralCapability.get(),
				AllBlockEntityTypes.PACKAGE_FROGPORT.get(),
				(be, context) -> be.computerBehaviour.getPeripheralCapability()
			);
		}
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(advancements = new AdvancementBehaviour(this, AllAdvancements.FROGPORT));
		behaviours.add(computerBehaviour = ComputerCraftProxy.behaviour(this));
		super.addBehaviours(behaviours);
	}

	public boolean isAnimationInProgress() {
		return animationProgress.getChaseTarget() == 1;
	}

	@Override
	public AABB getRenderBoundingBox() {
		AABB bb = super.getRenderBoundingBox().expandTowards(0, 1, 0);
		if (target != null)
			bb = bb.minmax(new AABB(BlockPos.containing(target.getExactTargetLocation(this, level, worldPosition))))
				.inflate(0.5);
		return bb;
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		if (level.isClientSide() || isAnimationInProgress())
			return;

		boolean prevFail = failedLastExport;
		tryPushingToAdjacentInventories();
		tryPullingFromOwnAndAdjacentInventories();

		if (failedLastExport != prevFail)
			sendData();
	}

	public void sendAnticipate() {
		if (isAnimationInProgress())
			return;
		for (int i = 0; i < inventory.getSlots(); i++)
			if (inventory.getStackInSlot(i)
				.isEmpty()) {
				sendAnticipate = true;
				sendData();
				return;
			}
	}

	public void anticipate() {
		anticipationProgress.chase(1, 0.1, Chaser.LINEAR);
	}

	@Override
	public void tick() {
		super.tick();

		if (deferAnimationStart != null) {
			startAnimation(deferAnimationStart, deferAnimationInward);
			deferAnimationStart = null;
		}

		if (anticipationProgress.getValue() == 1)
			anticipationProgress.startWithValue(0);

		manualOpenAnimationProgress.updateChaseTarget(openTracker.openCount > 0 ? 1 : 0);
		boolean wasOpen = manualOpenAnimationProgress.getValue() > 0;

		anticipationProgress.tickChaser();
		manualOpenAnimationProgress.tickChaser();

		if (level.isClientSide() && wasOpen && manualOpenAnimationProgress.getValue() == 0)
			sounds.close(level, worldPosition);

		if (!isAnimationInProgress())
			return;

		animationProgress.tickChaser();

		float value = animationProgress.getValue();
		if (currentlyDepositing) {
			if (!level.isClientSide() || isVirtual()) {
				if (value > 0.5 && animatedPackage != null) {
					if (target == null
						|| !target.depositImmediately() && !target.export(level, worldPosition, animatedPackage, false))
						drop(animatedPackage);
					else
						computerBehaviour.prepareComputerEvent(new PackageEvent(animatedPackage, "package_sent"));
					animatedPackage = null;
				}
			} else {
				if (value > 0.7 && animatedPackage != null)
					animatedPackage = null;
				if (animationProgress.getValue(0) < 0.2 && value > 0.2) {
					Vec3 v = target.getExactTargetLocation(this, level, worldPosition);
					level.playLocalSound(v.x, v.y, v.z, SoundEvents.CHAIN_STEP, SoundSource.BLOCKS, 0.25f, 1.2f, false);
				}
			}
		}

		if (value < 1)
			return;

		anticipationProgress.startWithValue(0);
		animationProgress.startWithValue(0);
		if (level.isClientSide()) {
//			sounds.close(level, worldPosition);
			animatedPackage = null;
			return;
		}

		if (!currentlyDepositing) {
			if (!ItemHandlerHelper.insertItem(inventory, animatedPackage.copy(), false)
				.isEmpty())
				drop(animatedPackage);
			else
				computerBehaviour.prepareComputerEvent(new PackageEvent(animatedPackage, "package_received"));
		}

		animatedPackage = null;
	}

	public void startAnimation(ItemStack box, boolean deposit) {
		if (!PackageItem.isPackage(box))
			return;

		if (deposit && (target == null
			|| target.depositImmediately() && !target.export(level, worldPosition, box.copy(), false)))
			return;

		animationProgress.startWithValue(0);
		animationProgress.chase(1, 0.1, Chaser.LINEAR);
		animatedPackage = box;
		currentlyDepositing = deposit;

		if (level != null && !deposit && !level.isClientSide())
			advancements.awardPlayer(AllAdvancements.FROGPORT);

		if (level != null && level.isClientSide()) {
			sounds.open(level, worldPosition);

			if (currentlyDepositing) {
				sounds.depositPackage(level, worldPosition);

			} else {
				sounds.catchPackage(level, worldPosition);
				Vec3 vec = target.getExactTargetLocation(this, level, worldPosition);
				if (vec != null)
					for (int i = 0; i < 5; i++)
						level.addParticle(
							new BlockParticleOption(ParticleTypes.BLOCK, AllBlocks.ROPE.getDefaultState()), vec.x,
							vec.y - level.random.nextFloat() * 0.25, vec.z, 0, 0, 0);
			}
		}

		if (level != null && !level.isClientSide()) {
			level.blockEntityChanged(worldPosition);
			sendData();
		}
	}

	protected void tryPushingToAdjacentInventories() {
		failedLastExport = false;

		if (itemHandler == null)
			return;

		boolean empty = true;
		for (int i = 0; i < itemHandler.getSlots(); i++)
			if (!itemHandler.getStackInSlot(i)
				.isEmpty())
				empty = false;
		if (empty)
			return;
		IItemHandler handler = getAdjacentInventory(Direction.DOWN);
		if (handler == null)
			return;

		for (int i = 0; i < itemHandler.getSlots(); i++) {
			ItemStack stackInSlot = itemHandler.extractItem(i, 1, true);
			if (stackInSlot.isEmpty())
				continue;
			ItemStack remainder = ItemHandlerHelper.insertItemStacked(handler, stackInSlot, false);
			if (remainder.isEmpty()) {
				itemHandler.extractItem(i, 1, false);
				level.blockEntityChanged(worldPosition);
			} else
				failedLastExport = true;
		}
	}

	@Override
	protected void onOpenChange(boolean open) {
	}

	public void tryPullingFromOwnAndAdjacentInventories() {
		if (isAnimationInProgress())
			return;
		if (target == null || !target.export(level, worldPosition, PackageStyles.getDefaultBox(), true))
			return;
		if (tryPullingFrom(inventory))
			return;
		for (Direction side : Iterate.directions) {
			if (side != Direction.DOWN)
				continue;
			IItemHandler handler = getAdjacentInventory(side);
			if (handler == null)
				continue;
			if (tryPullingFrom(handler))
				return;
		}
	}

	public boolean tryPullingFrom(IItemHandler handler) {
		ItemStack extract = ItemHelper.extract(handler, stack -> {
			if (!PackageItem.isPackage(stack))
				return false;
			String filterString = getFilterString();
			return filterString == null || handler instanceof PackagerItemHandler
				|| !PackageItem.matchAddress(stack, filterString);
		}, false);
		if (extract.isEmpty())
			return false;
		startAnimation(extract, true);
		return true;

	}

	protected IItemHandler getAdjacentInventory(Direction side) {
		BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(side));
		if (blockEntity == null || blockEntity instanceof FrogportBlockEntity)
			return null;
		return level.getCapability(ItemHandler.BLOCK, blockEntity.getBlockPos(), side.getOpposite());
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(tag, registries, clientPacket);
		tag.putFloat("PlacedYaw", passiveYaw);
		if (animatedPackage != null && isAnimationInProgress()) {
			tag.put("AnimatedPackage", animatedPackage.saveOptional(registries));
			tag.putBoolean("Deposit", currentlyDepositing);
		}
		if (sendAnticipate) {
			sendAnticipate = false;
			tag.putBoolean("Anticipate", true);
		}
		if (failedLastExport)
			NBTHelper.putMarker(tag, "FailedLastExport");
		if (goggles)
			NBTHelper.putMarker(tag, "Goggles");
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(tag, registries, clientPacket);
		passiveYaw = tag.getFloat("PlacedYaw");
		failedLastExport = tag.getBoolean("FailedLastExport");
		goggles = tag.getBoolean("Goggles");
		if (!clientPacket)
			animatedPackage = null;
		if (tag.contains("AnimatedPackage")) {
			deferAnimationInward = tag.getBoolean("Deposit");
			deferAnimationStart = ItemStack.parseOptional(registries, tag.getCompound("AnimatedPackage"));
		}
		if (clientPacket && tag.contains("Anticipate"))
			anticipate();
	}

	public float getYaw() {
		if (target == null)
			return passiveYaw;
		Vec3 diff = target.getExactTargetLocation(this, level, worldPosition)
			.subtract(Vec3.atCenterOf(worldPosition));
		return (float) (Mth.atan2(diff.x, diff.z) * Mth.RAD_TO_DEG) + 180;
	}

	@Override
	public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		boolean superTip = IHaveHoveringInformation.super.addToTooltip(tooltip, isPlayerSneaking);
		if (!failedLastExport)
			return superTip;
		TooltipHelper.addHint(tooltip, "hint.blocked_frogport");
		return true;
	}

	@Override
	protected void onOpenedManually() {
		if (level.isClientSide())
			sounds.open(level, worldPosition);
	}

	@Override
	public ItemInteractionResult use(Player player) {
		if (player == null)
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		ItemStack mainHandItem = player.getMainHandItem();
		if (!goggles && AllItems.GOGGLES.isIn(mainHandItem)) {
			goggles = true;
			if (!level.isClientSide()) {
				notifyUpdate();
				level.playSound(null, worldPosition, SoundEvents.ARMOR_EQUIP_GOLD.value(), SoundSource.BLOCKS, 0.5f, 1.0f);
			}
			return ItemInteractionResult.SUCCESS;
		}

		return super.use(player);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		computerBehaviour.removePeripheral();
	}

}
