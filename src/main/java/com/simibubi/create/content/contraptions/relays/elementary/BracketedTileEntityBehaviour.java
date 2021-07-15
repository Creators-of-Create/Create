package com.simibubi.create.content.contraptions.relays.elementary;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import com.simibubi.create.content.schematics.ItemRequirement;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.advancement.ITriggerable;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.world.World;

public class BracketedTileEntityBehaviour extends TileEntityBehaviour {

	public static BehaviourType<BracketedTileEntityBehaviour> TYPE = new BehaviourType<>();

	private Optional<BlockState> bracket;
	private boolean reRender;

	private Predicate<BlockState> pred;
	private Function<BlockState, ITriggerable> trigger;

	public BracketedTileEntityBehaviour(SmartTileEntity te) {
		this(te, state -> true);
	}

	public BracketedTileEntityBehaviour(SmartTileEntity te, Predicate<BlockState> pred) {
		super(te);
		this.pred = pred;
		bracket = Optional.empty();
	}

	public BracketedTileEntityBehaviour withTrigger(Function<BlockState, ITriggerable> trigger) {
		this.trigger = trigger;
		return this;
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

	public void triggerAdvancements(World world, PlayerEntity player, BlockState state) {
		if (trigger == null)
			return;
		AllTriggers.triggerFor(trigger.apply(state), player);
	}

	public void removeBracket(boolean inOnReplacedContext) {
		World world = getWorld();
		if (!world.isClientSide)
			world.levelEvent(2001, getPos(), Block.getId(getBracket()));
		this.bracket = Optional.empty();
		reRender = true;
		if (inOnReplacedContext)
			tileEntity.sendData();
		else
			tileEntity.notifyUpdate();
	}

	public boolean isBracketPresent() {
		return getBracket() != Blocks.AIR.defaultBlockState();
	}

	public BlockState getBracket() {
		return bracket.orElse(Blocks.AIR.defaultBlockState());
	}

	@Override
	public ItemRequirement getRequiredItems() {
		return ItemRequirement.of(getBracket(), null);
	}

	@Override
	public boolean isSafeNBT() {
		return true;
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
			getWorld().sendBlockUpdated(getPos(), tileEntity.getBlockState(), tileEntity.getBlockState(), 16);
		super.read(nbt, clientPacket);
	}

	public boolean canHaveBracket() {
		return pred.test(tileEntity.getBlockState());
	}

}
