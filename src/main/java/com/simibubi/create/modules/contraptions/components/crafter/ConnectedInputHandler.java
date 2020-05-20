package com.simibubi.create.modules.contraptions.components.crafter;

import static com.simibubi.create.modules.contraptions.base.HorizontalKineticBlock.HORIZONTAL_FACING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Predicates;
import com.simibubi.create.AllBlocksNew;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.utility.RaycastHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

@EventBusSubscriber
public class ConnectedInputHandler {

	@SubscribeEvent
	public static void onBlockActivated(PlayerInteractEvent.RightClickBlock event) {
		World world = event.getWorld();
		BlockPos pos = event.getPos();
		PlayerEntity player = event.getPlayer();
		Hand hand = event.getHand();
		ItemStack heldItem = player.getHeldItem(hand);

		if (player.isSneaking())
			return;
		if (!AllItems.WRENCH.typeOf(heldItem))
			return;
		BlockState blockState = world.getBlockState(pos);
		if (!AllBlocksNew.MECHANICAL_CRAFTER.has(blockState))
			return;

		BlockRayTraceResult ray = RaycastHelper.rayTraceRange(world, player, 10);
		if (ray == null)
			return;
		if (ray.getFace() == blockState.get(MechanicalCrafterBlock.HORIZONTAL_FACING))
			return;

		Direction activatedDirection = getActivatedDirection(world, pos, ray.getFace(), ray.getHitVec());
		if (activatedDirection != null) {
			if (event.getSide() != LogicalSide.CLIENT)
				toggleConnection(world, pos, pos.offset(activatedDirection));
			event.setCanceled(true);
			event.setCancellationResult(ActionResultType.SUCCESS);
			world.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, .25f, .1f);
		}
	}

	public static List<Pair<Direction, Vec3d>> getConnectiveSides(World world, BlockPos pos, Direction face) {
		List<Pair<Direction, Vec3d>> sides = new ArrayList<>(6);
		BlockState refState = world.getBlockState(pos);
		Direction refDirection = refState.get(HORIZONTAL_FACING);
		Vec3d faceOffset = new Vec3d(face.getDirectionVec()).scale(.5).add(VecHelper.getCenterOf(BlockPos.ZERO));

		if (Block.hasSolidSide(world.getBlockState(pos.offset(face)), world, pos.offset(face), face.getOpposite()))
			return sides;

		for (Direction direction : Direction.values()) {
			if (direction.getAxis() == face.getAxis())
				continue;
			if (direction.getAxis() == refDirection.getAxis())
				continue;
			BlockPos neighbourPos = pos.offset(direction);
			BlockState neighbour = world.getBlockState(neighbourPos);
			if (!AllBlocksNew.MECHANICAL_CRAFTER.has(neighbour))
				continue;
			if (refDirection != neighbour.get(HORIZONTAL_FACING))
				continue;
			if (Block.hasSolidSide(world.getBlockState(neighbourPos.offset(face)), world, neighbourPos.offset(face),
					face.getOpposite()))
				continue;

			Vec3d bbPos = new Vec3d(direction.getDirectionVec()).scale(.5).add(faceOffset);
			sides.add(Pair.of(direction, bbPos));
		}

		return sides;
	}

	public static Direction getActivatedDirection(World world, BlockPos pos, Direction face, Vec3d hit) {
		Vec3d localHit = hit.subtract(pos.getX(), pos.getY(), pos.getZ());
		for (Pair<Direction, Vec3d> pair : getConnectiveSides(world, pos, face)) {
			Vec3d bbPos = pair.getRight();
			AxisAlignedBB bb = new AxisAlignedBB(bbPos, bbPos).grow(1 / 6f);
			if (bb.contains(localHit))
				return pair.getKey();
		}
		return null;
	}

	public static void toggleConnection(World world, BlockPos pos, BlockPos pos2) {
		MechanicalCrafterTileEntity crafter1 = CrafterHelper.getCrafter(world, pos);
		MechanicalCrafterTileEntity crafter2 = CrafterHelper.getCrafter(world, pos2);

		if (crafter1 == null || crafter2 == null)
			return;

		BlockPos controllerPos1 = crafter1.getPos().add(crafter1.input.data.get(0));
		BlockPos controllerPos2 = crafter2.getPos().add(crafter2.input.data.get(0));

		if (controllerPos1.equals(controllerPos2)) {
			MechanicalCrafterTileEntity controller = CrafterHelper.getCrafter(world, controllerPos1);

			Set<BlockPos> positions =
				controller.input.data.stream().map(l -> controllerPos1.add(l)).collect(Collectors.toSet());
			List<BlockPos> frontier = new LinkedList<>();
			List<BlockPos> splitGroup = new ArrayList<>();

			frontier.add(pos2);
			positions.remove(pos2);
			positions.remove(pos);
			while (!frontier.isEmpty()) {
				BlockPos current = frontier.remove(0);
				for (Direction direction : Direction.values()) {
					BlockPos next = current.offset(direction);
					if (!positions.remove(next))
						continue;
					splitGroup.add(next);
					frontier.add(next);
				}
			}

			initAndAddAll(world, crafter1, positions);
			initAndAddAll(world, crafter2, splitGroup);

			crafter1.markDirty();
			crafter1.connectivityChanged();
			crafter2.markDirty();
			crafter2.connectivityChanged();
			return;
		}

		if (!crafter1.input.isController)
			crafter1 = CrafterHelper.getCrafter(world, controllerPos1);
		if (!crafter2.input.isController)
			crafter2 = CrafterHelper.getCrafter(world, controllerPos2);
		if (crafter1 == null || crafter2 == null)
			return;

		connectControllers(world, crafter1, crafter2);

		world.setBlockState(crafter1.getPos(), crafter1.getBlockState(), 3);

		crafter1.markDirty();
		crafter1.connectivityChanged();
		crafter2.markDirty();
		crafter2.connectivityChanged();
	}

	public static void initAndAddAll(World world, MechanicalCrafterTileEntity crafter, Collection<BlockPos> positions) {
		crafter.input = new ConnectedInput();
		positions.forEach(splitPos -> {
			modifyAndUpdate(world, splitPos, input -> {
				input.attachTo(crafter.getPos(), splitPos);
				crafter.input.data.add(splitPos.subtract(crafter.getPos()));
			});
		});
	}

	public static void connectControllers(World world, MechanicalCrafterTileEntity crafter1,
			MechanicalCrafterTileEntity crafter2) {

		crafter1.input.data.forEach(offset -> {
			BlockPos connectedPos = crafter1.getPos().add(offset);
			modifyAndUpdate(world, connectedPos, input -> {});
		});

		crafter2.input.data.forEach(offset -> {
			if (offset.equals(BlockPos.ZERO))
				return;
			BlockPos connectedPos = crafter2.getPos().add(offset);
			modifyAndUpdate(world, connectedPos, input -> {
				input.attachTo(crafter1.getPos(), connectedPos);
				crafter1.input.data.add(BlockPos.ZERO.subtract(input.data.get(0)));
			});
		});

		crafter2.input.attachTo(crafter1.getPos(), crafter2.getPos());
		crafter1.input.data.add(BlockPos.ZERO.subtract(crafter2.input.data.get(0)));
	}

	private static void modifyAndUpdate(World world, BlockPos pos, Consumer<ConnectedInput> callback) {
		TileEntity te = world.getTileEntity(pos);
		if (!(te instanceof MechanicalCrafterTileEntity))
			return;

		MechanicalCrafterTileEntity crafter = (MechanicalCrafterTileEntity) te;
		callback.accept(crafter.input);
		crafter.markDirty();
		crafter.connectivityChanged();
	}

	public static class ConnectedInput {
		boolean isController;
		List<BlockPos> data = Collections.synchronizedList(new ArrayList<>());

		public ConnectedInput() {
			isController = true;
			data.add(BlockPos.ZERO);
		}

		public void attachTo(BlockPos controllerPos, BlockPos myPos) {
			isController = false;
			data.clear();
			data.add(controllerPos.subtract(myPos));
		}

		public IItemHandler getItemHandler(World world, BlockPos pos) {
			if (!isController) {
				BlockPos controllerPos = pos.add(data.get(0));
				ConnectedInput input = CrafterHelper.getInput(world, controllerPos);
				if (input == this || input == null || !input.isController)
					return new ItemStackHandler();
				return input.getItemHandler(world, controllerPos);
			}

			List<IItemHandlerModifiable> list = data.stream().map(l -> CrafterHelper.getCrafter(world, pos.add(l)))
					.filter(Predicates.notNull()).map(crafter -> crafter.inventory).collect(Collectors.toList());
			return new CombinedInvWrapper(Arrays.copyOf(list.toArray(), list.size(), IItemHandlerModifiable[].class));
		}

		public void write(CompoundNBT nbt) {
			nbt.putBoolean("Controller", isController);
			ListNBT list = new ListNBT();
			data.forEach(pos -> list.add(NBTUtil.writeBlockPos(pos)));
			nbt.put("Data", list);
		}

		public void read(CompoundNBT nbt) {
			isController = nbt.getBoolean("Controller");
			data.clear();
			nbt.getList("Data", NBT.TAG_COMPOUND).forEach(inbt -> data.add(NBTUtil.readBlockPos((CompoundNBT) inbt)));
		}

	}

}
