package com.simibubi.create.content.contraptions.mounted;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.ContraptionType;
import com.simibubi.create.content.contraptions.mounted.CartAssemblerBlockEntity.CartMovementMode;
import com.simibubi.create.content.contraptions.render.ContraptionLighter;
import com.simibubi.create.content.contraptions.render.NonStationaryLighter;
import net.createmod.catnip.utility.Iterate;
import net.createmod.catnip.utility.NBTHelper;
import net.createmod.catnip.utility.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Queue;

import static com.simibubi.create.content.contraptions.mounted.CartAssemblerBlock.RAIL_SHAPE;

public class MountedContraption extends Contraption {

	public CartMovementMode rotationMode;
	public AbstractMinecart connectedCart;

	public MountedContraption() {
		this(CartMovementMode.ROTATE);
	}

	public MountedContraption(CartMovementMode mode) {
		rotationMode = mode;
	}

	@Override
	public ContraptionType getType() {
		return ContraptionType.MOUNTED;
	}

	@Override
	public boolean assemble(Level world, BlockPos pos) throws AssemblyException {
		BlockState state = world.getBlockState(pos);
		if (!state.hasProperty(RAIL_SHAPE))
			return false;
		if (!searchMovedStructure(world, pos, null))
			return false;

		Axis axis = state.getValue(RAIL_SHAPE) == RailShape.EAST_WEST ? Axis.X : Axis.Z;
		addBlock(pos, Pair.of(new StructureBlockInfo(pos, AllBlocks.MINECART_ANCHOR.getDefaultState()
			.setValue(BlockStateProperties.HORIZONTAL_AXIS, axis), null), null));

		if (blocks.size() == 1)
			return false;

		return true;
	}

	@Override
	protected boolean addToInitialFrontier(Level world, BlockPos pos, Direction direction, Queue<BlockPos> frontier) {
		frontier.clear();
		frontier.add(pos.above());
		return true;
	}

	@Override
	protected Pair<StructureBlockInfo, BlockEntity> capture(Level world, BlockPos pos) {
		Pair<StructureBlockInfo, BlockEntity> pair = super.capture(world, pos);
		StructureBlockInfo capture = pair.getKey();
		if (!AllBlocks.CART_ASSEMBLER.has(capture.state()))
			return pair;

		Pair<StructureBlockInfo, BlockEntity> anchorSwap =
			Pair.of(new StructureBlockInfo(pos, CartAssemblerBlock.createAnchor(capture.state()), null), pair.getValue());
		if (pos.equals(anchor) || connectedCart != null)
			return anchorSwap;

		for (Axis axis : Iterate.axes) {
			if (axis.isVertical() || !VecHelper.onSameAxis(anchor, pos, axis))
				continue;
			for (AbstractMinecart abstractMinecartEntity : world.getEntitiesOfClass(AbstractMinecart.class,
				new AABB(pos))) {
				if (!CartAssemblerBlock.canAssembleTo(abstractMinecartEntity))
					break;
				connectedCart = abstractMinecartEntity;
				connectedCart.setPos(pos.getX() + .5, pos.getY(), pos.getZ() + .5f);
			}
		}

		return anchorSwap;
	}

	@Override
	protected boolean movementAllowed(BlockState state, Level world, BlockPos pos) {
		if (!pos.equals(anchor) && AllBlocks.CART_ASSEMBLER.has(state))
			return testSecondaryCartAssembler(world, state, pos);
		return super.movementAllowed(state, world, pos);
	}

	protected boolean testSecondaryCartAssembler(Level world, BlockState state, BlockPos pos) {
		for (Axis axis : Iterate.axes) {
			if (axis.isVertical() || !VecHelper.onSameAxis(anchor, pos, axis))
				continue;
			for (AbstractMinecart abstractMinecartEntity : world.getEntitiesOfClass(AbstractMinecart.class,
				new AABB(pos))) {
				if (!CartAssemblerBlock.canAssembleTo(abstractMinecartEntity))
					break;
				return true;
			}
		}
		return false;
	}

	@Override
	public CompoundTag writeNBT(boolean spawnPacket) {
		CompoundTag tag = super.writeNBT(spawnPacket);
		NBTHelper.writeEnum(tag, "RotationMode", rotationMode);
		return tag;
	}

	@Override
	public void readNBT(Level world, CompoundTag nbt, boolean spawnData) {
		rotationMode = NBTHelper.readEnum(nbt, "RotationMode", CartMovementMode.class);
		super.readNBT(world, nbt, spawnData);
	}

	@Override
	protected boolean customBlockPlacement(LevelAccessor world, BlockPos pos, BlockState state) {
		return AllBlocks.MINECART_ANCHOR.has(state);
	}

	@Override
	protected boolean customBlockRemoval(LevelAccessor world, BlockPos pos, BlockState state) {
		return AllBlocks.MINECART_ANCHOR.has(state);
	}

	@Override
	public boolean canBeStabilized(Direction facing, BlockPos localPos) {
		return true;
	}

	public void addExtraInventories(Entity cart) {
		if (cart instanceof Container container)
			storage.attachExternal(new ContraptionInvWrapper(true, new InvWrapper(container)));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public ContraptionLighter<?> makeLighter() {
		return new NonStationaryLighter<>(this);
	}
}
