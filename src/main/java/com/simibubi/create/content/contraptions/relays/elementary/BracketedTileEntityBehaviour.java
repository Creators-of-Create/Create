package com.simibubi.create.content.contraptions.relays.elementary;

import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.contraptions.components.structureMovement.StructureTransform;
import com.simibubi.create.content.schematics.ItemRequirement;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BracketedTileEntityBehaviour extends TileEntityBehaviour {

	public static final BehaviourType<BracketedTileEntityBehaviour> TYPE = new BehaviourType<>();

	private BlockState bracket;
	private boolean reRender;

	private Predicate<BlockState> pred;

	public BracketedTileEntityBehaviour(SmartTileEntity te) {
		this(te, state -> true);
	}

	public BracketedTileEntityBehaviour(SmartTileEntity te, Predicate<BlockState> pred) {
		super(te);
		this.pred = pred;
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	public void applyBracket(BlockState state) {
		this.bracket = state;
		reRender = true;
		tileEntity.notifyUpdate();
		Level world = getWorld();
		if (world.isClientSide)
			return;
		tileEntity.getBlockState()
			.updateNeighbourShapes(world, getPos(), 3);
	}

	public void transformBracket(StructureTransform transform) {
		if (isBracketPresent()) {
			BlockState transformedBracket = transform.apply(bracket);
			applyBracket(transformedBracket);
		}
	}

	@Nullable
	public BlockState removeBracket(boolean inOnReplacedContext) {
		if (bracket == null) {
			return null;
		}

		BlockState removed = this.bracket;
		Level world = getWorld();
		if (!world.isClientSide)
			world.levelEvent(2001, getPos(), Block.getId(bracket));
		this.bracket = null;
		reRender = true;
		if (inOnReplacedContext) {
			tileEntity.sendData();
			return removed;
		}
		tileEntity.notifyUpdate();
		if (world.isClientSide)
			return removed;
		tileEntity.getBlockState()
			.updateNeighbourShapes(world, getPos(), 3);
		return removed;
	}

	public boolean isBracketPresent() {
		return bracket != null;
	}

	@Nullable
	public BlockState getBracket() {
		return bracket;
	}

	public boolean canHaveBracket() {
		return pred.test(tileEntity.getBlockState());
	}

	@Override
	public ItemRequirement getRequiredItems() {
		if (!isBracketPresent()) {
			return ItemRequirement.NONE;
		}
		return ItemRequirement.of(bracket, null);
	}

	@Override
	public boolean isSafeNBT() {
		return true;
	}

	@Override
	public void write(CompoundTag nbt, boolean clientPacket) {
		if (isBracketPresent()) {
			nbt.put("Bracket", NbtUtils.writeBlockState(bracket));
		}
		if (clientPacket && reRender) {
			NBTHelper.putMarker(nbt, "Redraw");
			reRender = false;
		}
		super.write(nbt, clientPacket);
	}

	@Override
	public void read(CompoundTag nbt, boolean clientPacket) {
		if (nbt.contains("Bracket"))
			bracket = NbtUtils.readBlockState(nbt.getCompound("Bracket"));
		if (clientPacket && nbt.contains("Redraw"))
			getWorld().sendBlockUpdated(getPos(), tileEntity.getBlockState(), tileEntity.getBlockState(), 16);
		super.read(nbt, clientPacket);
	}

}
