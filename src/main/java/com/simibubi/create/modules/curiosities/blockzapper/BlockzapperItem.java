package com.simibubi.create.modules.curiosities.blockzapper;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.base.Predicates;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllPackets;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.ItemDescription.Palette;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Rarity;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.StairsShape;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.PacketDistributor;

public class BlockzapperItem extends Item {

	public static enum ComponentTier {
		None(TextFormatting.DARK_GRAY), Brass(TextFormatting.GOLD), Chromatic(TextFormatting.LIGHT_PURPLE),

		;

		public TextFormatting color;

		private ComponentTier(TextFormatting color) {
			this.color = color;
		}

	}

	public static enum Components {
		Body, Amplifier, Accelerator, Retriever, Scope
	}

	public BlockzapperItem(Properties properties) {
		super(properties.maxStackSize(1).rarity(Rarity.UNCOMMON));
	}

	@Override
	public boolean canPlayerBreakBlockWhileHolding(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
		return false;
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.NONE;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if (stack.hasTag() && stack.getTag().contains("BlockUsed")) {
			String usedblock = NBTUtil.readBlockState(stack.getTag().getCompound("BlockUsed")).getBlock()
					.getTranslationKey();
			ItemDescription.add(tooltip, TextFormatting.DARK_GRAY + Lang.translate("blockzapper.usingBlock",
					TextFormatting.GRAY + new TranslationTextComponent(usedblock).getFormattedText()));
		}
		Palette palette = Palette.Purple;
		if (Screen.hasShiftDown()) {
			ItemDescription.add(tooltip, palette.color + Lang.translate("blockzapper.componentUpgrades"));

			for (Components c : Components.values()) {
				ComponentTier tier = getTier(c, stack);
				ItemDescription.add(tooltip,
						"> " + TextFormatting.GRAY + Lang.translate("blockzapper.component." + Lang.asId(c.name()))
								+ ": " + tier.color
								+ Lang.translate("blockzapper.componentTier." + Lang.asId(tier.name())));
			}
		}
	}

	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		if (group == Create.creativeTab) {
			ItemStack gunWithoutStuff = new ItemStack(this);
			items.add(gunWithoutStuff);

			ItemStack gunWithGoldStuff = new ItemStack(this);
			for (Components c : Components.values())
				setTier(c, ComponentTier.Brass, gunWithGoldStuff);
			items.add(gunWithGoldStuff);

			ItemStack gunWithPurpurStuff = new ItemStack(this);
			for (Components c : Components.values())
				setTier(c, ComponentTier.Chromatic, gunWithPurpurStuff);
			items.add(gunWithPurpurStuff);
		}
		super.fillItemGroup(group, items);
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		// Shift -> open GUI
		if (context.isPlacerSneaking()) {
			if (context.getWorld().isRemote) {
				DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
					openHandgunGUI(context.getItem(), context.getHand() == Hand.OFF_HAND);
				});
				applyCooldown(context.getPlayer(), context.getItem(), false);
			}
			return ActionResultType.SUCCESS;
		}
		return super.onItemUse(context);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
		ItemStack item = player.getHeldItem(hand);
		CompoundNBT nbt = item.getOrCreateTag();

		// Shift -> Open GUI
		if (player.isSneaking()) {
			if (world.isRemote) {
				DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
					openHandgunGUI(item, hand == Hand.OFF_HAND);
				});
				applyCooldown(player, item, false);
			}
			return new ActionResult<ItemStack>(ActionResultType.SUCCESS, item);
		}

		boolean mainHand = hand == Hand.MAIN_HAND;
		boolean isSwap = item.getTag().contains("_Swap");
		boolean gunInOtherHand = AllItems.PLACEMENT_HANDGUN
				.typeOf(player.getHeldItem(mainHand ? Hand.OFF_HAND : Hand.MAIN_HAND));

		// Pass To Offhand
		if (mainHand && isSwap && gunInOtherHand)
			return new ActionResult<ItemStack>(ActionResultType.FAIL, item);
		if (mainHand && !isSwap && gunInOtherHand)
			item.getTag().putBoolean("_Swap", true);
		if (!mainHand && isSwap)
			item.getTag().remove("_Swap");
		if (!mainHand && gunInOtherHand)
			player.getHeldItem(Hand.MAIN_HAND).getTag().remove("_Swap");
		player.setActiveHand(hand);

		// Check if block setting is present
		BlockState stateToUse = Blocks.AIR.getDefaultState();
		if (nbt.contains("BlockUsed"))
			stateToUse = NBTUtil.readBlockState(nbt.getCompound("BlockUsed"));
		else {
			world.playSound(player, player.getPosition(), SoundEvents.BLOCK_NOTE_BLOCK_BASS, SoundCategory.BLOCKS, 1f,
					0.5f);
			player.sendStatusMessage(
					new StringTextComponent(TextFormatting.RED + Lang.translate("blockzapper.leftClickToSet")), true);
			return new ActionResult<ItemStack>(ActionResultType.FAIL, item);
		}

		// Raytrace - Find the target
		Vec3d start = player.getPositionVec().add(0, player.getEyeHeight(), 0);
		Vec3d range = player.getLookVec().scale(getReachDistance(item));
		BlockRayTraceResult raytrace = world.rayTraceBlocks(
				new RayTraceContext(start, start.add(range), BlockMode.OUTLINE, FluidMode.NONE, player));
		BlockPos pos = raytrace.getPos();
		BlockState stateReplaced = world.getBlockState(pos);

		// No target
		if (pos == null || stateReplaced.getBlock() == Blocks.AIR) {
			applyCooldown(player, item, gunInOtherHand);
			return new ActionResult<ItemStack>(ActionResultType.SUCCESS, item);
		}

		// Find exact position of gun barrel for VFX
		float yaw = (float) ((player.rotationYaw) / -180 * Math.PI);
		float pitch = (float) ((player.rotationPitch) / -180 * Math.PI);
		Vec3d barrelPosNoTransform = new Vec3d(mainHand == (player.getPrimaryHand() == HandSide.RIGHT) ? -.35f : .35f,
				-0.1f, 1);
		Vec3d barrelPos = start.add(barrelPosNoTransform.rotatePitch(pitch).rotateYaw(yaw));

		// Client side
		if (world.isRemote) {
			BlockzapperHandler.dontAnimateItem(hand);
			return new ActionResult<ItemStack>(ActionResultType.SUCCESS, item);
		}

		// Server side - Replace Blocks
		boolean replace = nbt.contains("Replace") && nbt.getBoolean("Replace");
		List<BlockPos> selectedBlocks = getSelectedBlocks(item, world, player);
		applyPattern(selectedBlocks, item);
		Direction face = raytrace.getFace();

		for (BlockPos placed : selectedBlocks) {
			if (world.getBlockState(placed) == stateToUse)
				continue;
			if (!stateToUse.isValidPosition(world, placed))
				continue;
			if (!player.isCreative() && !canBreak(item, world.getBlockState(placed), world, placed))
				continue;
			if (!player.isCreative() && BlockHelper.findAndRemoveInInventory(stateToUse, player, 1) == 0) {
				player.getCooldownTracker().setCooldown(item.getItem(), 20);
				player.sendStatusMessage(
						new StringTextComponent(TextFormatting.RED + Lang.translate("blockzapper.empty")), true);
				return new ActionResult<ItemStack>(ActionResultType.SUCCESS, item);
			}

			if (!player.isCreative() && replace)
				dropBlocks(world, player, item, face, placed);

			for (Direction updateDirection : Direction.values())
				stateToUse = stateToUse.updatePostPlacement(updateDirection,
						world.getBlockState(placed.offset(updateDirection)), world, placed,
						placed.offset(updateDirection));

			BlockSnapshot blocksnapshot = BlockSnapshot.getBlockSnapshot(world, placed);
			IFluidState ifluidstate = world.getFluidState(placed);
			world.setBlockState(placed, ifluidstate.getBlockState(), 18);
			world.setBlockState(placed, stateToUse);
			if (ForgeEventFactory.onBlockPlace(player, blocksnapshot, Direction.UP)) {
				blocksnapshot.restore(true, false);
				return new ActionResult<ItemStack>(ActionResultType.FAIL, item);
			}

			if (player instanceof ServerPlayerEntity)
				CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayerEntity) player, placed,
						new ItemStack(stateToUse.getBlock()));

		}

		applyCooldown(player, item, gunInOtherHand);
		AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> player),
				new BlockzapperBeamPacket(barrelPos, raytrace.getHitVec(), hand, false));
		AllPackets.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
				new BlockzapperBeamPacket(barrelPos, raytrace.getHitVec(), hand, true));

		return new ActionResult<ItemStack>(ActionResultType.SUCCESS, item);

	}

	@Override
	public boolean onBlockDestroyed(ItemStack stack, World worldIn, BlockState state, BlockPos pos,
			LivingEntity entityLiving) {
		if (entityLiving instanceof PlayerEntity && ((PlayerEntity) entityLiving).isCreative()) {
			worldIn.setBlockState(pos, state);
			return false;
		}
		return super.onBlockDestroyed(stack, worldIn, state, pos, entityLiving);
	}

	@Override
	public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
		if (!(entity instanceof PlayerEntity))
			return false;
		if (entity.isSneaking())
			return true;

		Vec3d start = entity.getPositionVec().add(0, entity.getEyeHeight(), 0);
		Vec3d range = entity.getLookVec().scale(getReachDistance(stack));
		BlockRayTraceResult raytrace = entity.world.rayTraceBlocks(
				new RayTraceContext(start, start.add(range), BlockMode.OUTLINE, FluidMode.NONE, entity));
		BlockPos pos = raytrace.getPos();
		if (pos == null)
			return true;

		entity.world.sendBlockBreakProgress(entity.getEntityId(), pos, -1);
		BlockState newState = entity.world.getBlockState(pos);

		if (BlockHelper.getRequiredItem(newState).isEmpty())
			return true;
		if (entity.world.getTileEntity(pos) != null)
			return true;
		if (newState.has(BlockStateProperties.DOUBLE_BLOCK_HALF))
			return true;
		if (newState.has(BlockStateProperties.ATTACHED))
			return true;
		if (newState.has(BlockStateProperties.HANGING))
			return true;
		if (newState.has(BlockStateProperties.BED_PART))
			return true;
		if (newState.has(BlockStateProperties.STAIRS_SHAPE))
			newState = newState.with(BlockStateProperties.STAIRS_SHAPE, StairsShape.STRAIGHT);
		if (newState.has(BlockStateProperties.PERSISTENT))
			newState = newState.with(BlockStateProperties.PERSISTENT, true);

		if (stack.getTag().contains("BlockUsed")
				&& NBTUtil.readBlockState(stack.getTag().getCompound("BlockUsed")) == newState)
			return true;

		stack.getTag().put("BlockUsed", NBTUtil.writeBlockState(newState));
		entity.world.playSound((PlayerEntity) entity, entity.getPosition(), SoundEvents.BLOCK_NOTE_BLOCK_BELL,
				SoundCategory.BLOCKS, 0.5f, 0.8f);

		return true;
	}

	@Override
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		if (AllItems.PLACEMENT_HANDGUN.typeOf(stack)) {
			CompoundNBT nbt = stack.getOrCreateTag();
			if (!nbt.contains("Replace"))
				nbt.putBoolean("Replace", false);
			if (!nbt.contains("Pattern"))
				nbt.putString("Pattern", PlacementPatterns.Solid.name());
			if (!nbt.contains("SearchDiagonal"))
				nbt.putBoolean("SearchDiagonal", false);
			if (!nbt.contains("SearchMaterial"))
				nbt.putBoolean("SearchMaterial", false);
			if (!nbt.contains("SearchDistance"))
				nbt.putInt("SearchDistance", 1);
		}
	}

	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, PlayerEntity player) {
		return true;
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 0;
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {
		super.onPlayerStoppedUsing(stack, worldIn, entityLiving, timeLeft);
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		boolean differentBlock = true;
		if (oldStack.hasTag() && newStack.hasTag() && oldStack.getTag().contains("BlockUsed")
				&& newStack.getTag().contains("BlockUsed"))
			differentBlock = NBTUtil.readBlockState(oldStack.getTag().getCompound("BlockUsed")) != NBTUtil
					.readBlockState(newStack.getTag().getCompound("BlockUsed"));
		return slotChanged || !AllItems.PLACEMENT_HANDGUN.typeOf(newStack) || differentBlock;
	}

	@OnlyIn(Dist.CLIENT)
	private void openHandgunGUI(ItemStack handgun, boolean offhand) {
		ScreenOpener.open(new BlockzapperScreen(handgun, offhand));
	}

	public static List<BlockPos> getSelectedBlocks(ItemStack stack, World worldIn, PlayerEntity player) {
		List<BlockPos> list = new LinkedList<>();
		CompoundNBT tag = stack.getTag();
		if (tag == null)
			return list;

		boolean searchDiagonals = tag.contains("SearchDiagonal") && tag.getBoolean("SearchDiagonal");
		boolean searchAcrossMaterials = tag.contains("SearchFuzzy") && tag.getBoolean("SearchFuzzy");
		boolean replace = tag.contains("Replace") && tag.getBoolean("Replace");
		int searchRange = tag.contains("SearchDistance") ? tag.getInt("SearchDistance") : 0;

		Set<BlockPos> visited = new HashSet<>();
		List<BlockPos> frontier = new LinkedList<>();

		Vec3d start = player.getPositionVec().add(0, player.getEyeHeight(), 0);
		Vec3d range = player.getLookVec().scale(getReachDistance(stack));
		BlockRayTraceResult raytrace = player.world.rayTraceBlocks(
				new RayTraceContext(start, start.add(range), BlockMode.COLLIDER, FluidMode.NONE, player));
		BlockPos pos = raytrace.getPos().toImmutable();

		if (pos == null)
			return list;

		BlockState state = worldIn.getBlockState(pos);
		Direction face = raytrace.getFace();
		List<BlockPos> offsets = new LinkedList<>();

		for (int x = -1; x <= 1; x++)
			for (int y = -1; y <= 1; y++)
				for (int z = -1; z <= 1; z++)
					if (Math.abs(x) + Math.abs(y) + Math.abs(z) < 2 || searchDiagonals)
						if (face.getAxis().getCoordinate(x, y, z) == 0)
							offsets.add(new BlockPos(x, y, z));

		BlockPos startPos = replace ? pos : pos.offset(face);
		frontier.add(startPos);

		while (!frontier.isEmpty()) {
			BlockPos currentPos = frontier.remove(0);
			if (visited.contains(currentPos))
				continue;
			visited.add(currentPos);
			if (!currentPos.withinDistance(startPos, searchRange))
				continue;

			// Replace Mode
			if (replace) {
				BlockState stateToReplace = worldIn.getBlockState(currentPos);
				BlockState stateAboveStateToReplace = worldIn.getBlockState(currentPos.offset(face));

				// Criteria
				if (stateToReplace.getBlockHardness(worldIn, currentPos) == -1)
					continue;
				if (stateToReplace.getBlock() != state.getBlock() && !searchAcrossMaterials)
					continue;
				if (stateToReplace.getMaterial().isReplaceable())
					continue;
				if (stateAboveStateToReplace.isSolid())
					continue;
				list.add(currentPos);

				// Search adjacent spaces
				for (BlockPos offset : offsets)
					frontier.add(currentPos.add(offset));
				continue;
			}

			// Place Mode
			BlockState stateToPlaceAt = worldIn.getBlockState(currentPos);
			BlockState stateToPlaceOn = worldIn.getBlockState(currentPos.offset(face.getOpposite()));

			// Criteria
			if (stateToPlaceOn.getMaterial().isReplaceable())
				continue;
			if (stateToPlaceOn.getBlock() != state.getBlock() && !searchAcrossMaterials)
				continue;
			if (!stateToPlaceAt.getMaterial().isReplaceable())
				continue;
			list.add(currentPos);

			// Search adjacent spaces
			for (BlockPos offset : offsets)
				frontier.add(currentPos.add(offset));
			continue;
		}

		return list;
	}

	public static boolean canBreak(ItemStack stack, BlockState state, World world, BlockPos pos) {
		ComponentTier tier = getTier(Components.Body, stack);
		float blockHardness = state.getBlockHardness(world, pos);

		if (blockHardness == -1)
			return false;
		if (tier == ComponentTier.None)
			return blockHardness < 3;
		if (tier == ComponentTier.Brass)
			return blockHardness < 6;
		if (tier == ComponentTier.Chromatic)
			return true;

		return false;
	}

	public static int getMaxAoe(ItemStack stack) {
		ComponentTier tier = getTier(Components.Amplifier, stack);
		if (tier == ComponentTier.None)
			return 2;
		if (tier == ComponentTier.Brass)
			return 4;
		if (tier == ComponentTier.Chromatic)
			return 8;

		return 0;
	}

	public static int getCooldownDelay(ItemStack stack) {
		ComponentTier tier = getTier(Components.Accelerator, stack);
		if (tier == ComponentTier.None)
			return 10;
		if (tier == ComponentTier.Brass)
			return 6;
		if (tier == ComponentTier.Chromatic)
			return 2;

		return 20;
	}

	public static int getReachDistance(ItemStack stack) {
		ComponentTier tier = getTier(Components.Scope, stack);
		if (tier == ComponentTier.None)
			return 15;
		if (tier == ComponentTier.Brass)
			return 30;
		if (tier == ComponentTier.Chromatic)
			return 100;

		return 0;
	}

	public static void applyPattern(List<BlockPos> blocksIn, ItemStack stack) {
		CompoundNBT tag = stack.getTag();
		PlacementPatterns pattern = !tag.contains("Pattern") ? PlacementPatterns.Solid
				: PlacementPatterns.valueOf(tag.getString("Pattern"));
		Random r = new Random();
		Predicate<BlockPos> filter = Predicates.alwaysFalse();

		switch (pattern) {
		case Chance25:
			filter = pos -> r.nextBoolean() || r.nextBoolean();
			break;
		case Chance50:
			filter = pos -> r.nextBoolean();
			break;
		case Chance75:
			filter = pos -> r.nextBoolean() && r.nextBoolean();
			break;
		case Checkered:
			filter = pos -> (pos.getX() + pos.getY() + pos.getZ()) % 2 == 0;
			break;
		case InverseCheckered:
			filter = pos -> (pos.getX() + pos.getY() + pos.getZ()) % 2 != 0;
			break;
		case Solid:
		default:
			break;
		}

		blocksIn.removeIf(filter);
	}

	protected static void dropBlocks(World worldIn, PlayerEntity playerIn, ItemStack item, Direction face,
			BlockPos placed) {
		TileEntity tileentity = worldIn.getBlockState(placed).hasTileEntity() ? worldIn.getTileEntity(placed) : null;

		if (getTier(Components.Retriever, item) == ComponentTier.None) {
			Block.spawnDrops(worldIn.getBlockState(placed), worldIn, placed.offset(face), tileentity);
		}

		if (getTier(Components.Retriever, item) == ComponentTier.Brass)
			Block.spawnDrops(worldIn.getBlockState(placed), worldIn, playerIn.getPosition(), tileentity);

		if (getTier(Components.Retriever, item) == ComponentTier.Chromatic)
			for (ItemStack stack : Block.getDrops(worldIn.getBlockState(placed), (ServerWorld) worldIn, placed,
					tileentity))
				if (!playerIn.inventory.addItemStackToInventory(stack))
					Block.spawnAsEntity(worldIn, placed, stack);
	}

	protected static void applyCooldown(PlayerEntity playerIn, ItemStack item, boolean dual) {
		playerIn.getCooldownTracker().setCooldown(item.getItem(),
				dual ? getCooldownDelay(item) * 2 / 3 : getCooldownDelay(item));
	}

	public static ComponentTier getTier(Components component, ItemStack stack) {
		if (!stack.hasTag() || !stack.getTag().contains(component.name()))
			stack.getOrCreateTag().putString(component.name(), ComponentTier.None.name());
		return NBTHelper.readEnum(stack.getTag().getString(component.name()), ComponentTier.class);
	}

	public static void setTier(Components component, ComponentTier tier, ItemStack stack) {
		stack.getOrCreateTag().putString(component.name(), NBTHelper.writeEnum(tier));
	}

}
