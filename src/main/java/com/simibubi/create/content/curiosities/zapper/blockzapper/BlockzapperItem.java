package com.simibubi.create.content.curiosities.zapper.blockzapper;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.simibubi.create.AllItemsNew;
import com.simibubi.create.Create;
import com.simibubi.create.content.curiosities.zapper.PlacementPatterns;
import com.simibubi.create.content.curiosities.zapper.ZapperItem;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.ItemDescription.Palette;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;

public class BlockzapperItem extends ZapperItem {

	public BlockzapperItem(Properties properties) {
		super(properties);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		Palette palette = Palette.Purple;
		if (Screen.hasShiftDown()) {
			ItemDescription.add(tooltip, palette.color + Lang.translate("blockzapper.componentUpgrades"));

			for (Components c : Components.values()) {
				ComponentTier tier = getTier(c, stack);
				String componentName =
					TextFormatting.GRAY + Lang.translate("blockzapper.component." + Lang.asId(c.name()));
				String tierName = tier.color + Lang.translate("blockzapper.componentTier." + Lang.asId(tier.name()));
				ItemDescription.add(tooltip, "> " + componentName + ": " + tierName);
			}
		}
	}

	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		if (group == Create.baseCreativeTab) {
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
	}

	protected boolean activate(World world, PlayerEntity player, ItemStack stack, BlockState selectedState,
		BlockRayTraceResult raytrace) {
		CompoundNBT nbt = stack.getOrCreateTag();
		boolean replace = nbt.contains("Replace") && nbt.getBoolean("Replace");

		List<BlockPos> selectedBlocks = getSelectedBlocks(stack, world, player);
		PlacementPatterns.applyPattern(selectedBlocks, stack);
		Direction face = raytrace.getFace();

		for (BlockPos placed : selectedBlocks) {
			if (world.getBlockState(placed) == selectedState)
				continue;
			if (!selectedState.isValidPosition(world, placed))
				continue;
			if (!player.isCreative() && !canBreak(stack, world.getBlockState(placed), world, placed))
				continue;
			if (!player.isCreative() && BlockHelper.findAndRemoveInInventory(selectedState, player, 1) == 0) {
				player.getCooldownTracker()
					.setCooldown(stack.getItem(), 20);
				player.sendStatusMessage(
					new StringTextComponent(TextFormatting.RED + Lang.translate("blockzapper.empty")), true);
				return false;
			}

			if (!player.isCreative() && replace)
				dropBlocks(world, player, stack, face, placed);

			for (Direction updateDirection : Direction.values())
				selectedState = selectedState.updatePostPlacement(updateDirection,
					world.getBlockState(placed.offset(updateDirection)), world, placed, placed.offset(updateDirection));

			BlockSnapshot blocksnapshot = BlockSnapshot.getBlockSnapshot(world, placed);
			IFluidState ifluidstate = world.getFluidState(placed);
			world.setBlockState(placed, ifluidstate.getBlockState(), 18);
			world.setBlockState(placed, selectedState);
			if (ForgeEventFactory.onBlockPlace(player, blocksnapshot, Direction.UP)) {
				blocksnapshot.restore(true, false);
				return false;
			}

			if (player instanceof ServerPlayerEntity && world instanceof ServerWorld) {
				ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
				CriteriaTriggers.PLACED_BLOCK.trigger(serverPlayer, placed, new ItemStack(selectedState.getBlock()));

				boolean fullyUpgraded = true;
				for (Components c : Components.values()) {
					if (getTier(c, stack) != ComponentTier.Chromatic) {
						fullyUpgraded = false;
						break;
					}
				}
				if (fullyUpgraded)
					AllTriggers.UPGRADED_ZAPPER.trigger(serverPlayer);
			}
		}

		return true;
	}

	@Override
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		if (AllItemsNew.typeOf(AllItemsNew.BLOCKZAPPER, stack)) {
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
	@OnlyIn(Dist.CLIENT)
	protected void openHandgunGUI(ItemStack handgun, boolean offhand) {
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

		Vec3d start = player.getPositionVec()
			.add(0, player.getEyeHeight(), 0);
		Vec3d range = player.getLookVec()
			.scale(getRange(stack));
		BlockRayTraceResult raytrace = player.world
			.rayTraceBlocks(new RayTraceContext(start, start.add(range), BlockMode.COLLIDER, FluidMode.NONE, player));
		BlockPos pos = raytrace.getPos()
			.toImmutable();

		if (pos == null)
			return list;

		BlockState state = worldIn.getBlockState(pos);
		Direction face = raytrace.getFace();
		List<BlockPos> offsets = new LinkedList<>();

		for (int x = -1; x <= 1; x++)
			for (int y = -1; y <= 1; y++)
				for (int z = -1; z <= 1; z++)
					if (Math.abs(x) + Math.abs(y) + Math.abs(z) < 2 || searchDiagonals)
						if (face.getAxis()
							.getCoordinate(x, y, z) == 0)
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
				if (stateToReplace.getMaterial()
					.isReplaceable())
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
			if (stateToPlaceOn.getMaterial()
				.isReplaceable())
				continue;
			if (stateToPlaceOn.getBlock() != state.getBlock() && !searchAcrossMaterials)
				continue;
			if (!stateToPlaceAt.getMaterial()
				.isReplaceable())
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

	@Override
	protected int getCooldownDelay(ItemStack stack) {
		return getCooldown(stack);
	}

	public static int getCooldown(ItemStack stack) {
		ComponentTier tier = getTier(Components.Accelerator, stack);
		if (tier == ComponentTier.None)
			return 10;
		if (tier == ComponentTier.Brass)
			return 6;
		if (tier == ComponentTier.Chromatic)
			return 2;

		return 20;
	}

	@Override
	protected int getZappingRange(ItemStack stack) {
		return getRange(stack);
	}

	public static int getRange(ItemStack stack) {
		ComponentTier tier = getTier(Components.Scope, stack);
		if (tier == ComponentTier.None)
			return 15;
		if (tier == ComponentTier.Brass)
			return 30;
		if (tier == ComponentTier.Chromatic)
			return 100;

		return 0;
	}

	protected static void dropBlocks(World worldIn, PlayerEntity playerIn, ItemStack item, Direction face,
		BlockPos placed) {
		TileEntity tileentity = worldIn.getBlockState(placed)
			.hasTileEntity() ? worldIn.getTileEntity(placed) : null;

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

	public static ComponentTier getTier(Components component, ItemStack stack) {
		if (!stack.hasTag() || !stack.getTag()
			.contains(component.name()))
			stack.getOrCreateTag()
				.putString(component.name(), ComponentTier.None.name());
		return NBTHelper.readEnum(stack.getTag()
			.getString(component.name()), ComponentTier.class);
	}

	public static void setTier(Components component, ComponentTier tier, ItemStack stack) {
		stack.getOrCreateTag()
			.putString(component.name(), NBTHelper.writeEnum(tier));
	}

	public static enum ComponentTier {
		None(TextFormatting.DARK_GRAY), Brass(TextFormatting.GOLD), Chromatic(TextFormatting.LIGHT_PURPLE);
		public TextFormatting color;

		private ComponentTier(TextFormatting color) {
			this.color = color;
		}

	}

	public static enum Components {
		Body, Amplifier, Accelerator, Retriever, Scope
	}

}
