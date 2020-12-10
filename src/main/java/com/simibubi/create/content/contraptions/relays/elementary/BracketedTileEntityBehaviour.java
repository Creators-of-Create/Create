package com.simibubi.create.content.contraptions.relays.elementary;

import java.util.Optional;
import java.util.function.Predicate;

import com.google.common.base.Predicates;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.world.World;

public class BracketedTileEntityBehaviour extends TileEntityBehaviour {

	public static BehaviourType<BracketedTileEntityBehaviour> TYPE = new BehaviourType<>();

	private Optional<BlockState> bracket;
	private boolean reRender;

	private Predicate<BlockState> pred;

	public BracketedTileEntityBehaviour(SmartTileEntity te) {
		this(te, Predicates.alwaysTrue());
	}

	public BracketedTileEntityBehaviour(SmartTileEntity te, Predicate<BlockState> pred) {
		super(te);
		this.pred = pred;
		bracket = Optional.empty();
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	public void applyBracket(BlockState state) {
		this.bracket = Optional.of(state);
		reRender = true;
		tileEntity.notifyUpdate();
	}

	public void removeBracket() {
		World world = getWorld();
		if (!world.isRemote)
			world.playEvent(2001, getPos(), Block.getStateId(getBracket()));
		this.bracket = Optional.empty();
		reRender = true;
		tileEntity.notifyUpdate();
	}

	public boolean isBacketPresent() {
		return getBracket() != Blocks.AIR.getDefaultState();
	}

	public BlockState getBracket() {
		return bracket.orElse(Blocks.AIR.getDefaultState());
	}

	@Override
	public void write(CompoundNBT nbt, boolean clientPacket) {
		bracket.ifPresent(p -> nbt.put("Bracket", NBTUtil.writeBlockState(p)));
		if (clientPacket && reRender) {
			NBTHelper.putMarker(nbt, "Redraw");
			reRender = false;
		}
		super.write(nbt, clientPacket);
	}

	@Override
	public void read(CompoundNBT nbt, boolean clientPacket) {
		bracket = Optional.empty();
		if (nbt.contains("Bracket"))
			bracket = Optional.of(NBTUtil.readBlockState(nbt.getCompound("Bracket")));
		if (clientPacket && nbt.contains("Redraw"))
			getWorld().notifyBlockUpdate(getPos(), tileEntity.getBlockState(), tileEntity.getBlockState(), 16);
		super.read(nbt, clientPacket);
	}

	public boolean canHaveBracket() {
		return pred.test(tileEntity.getBlockState());
	}

}
