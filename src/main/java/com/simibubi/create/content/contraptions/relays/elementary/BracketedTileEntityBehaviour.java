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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

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

	public void triggerAdvancements(Level world, Player player, BlockState state) {
		if (trigger == null)
			return;
		AllTriggers.triggerFor(trigger.apply(state), player);
	}

	public void removeBracket(boolean inOnReplacedContext) {
		Level world = getWorld();
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
	public void write(CompoundTag nbt, boolean clientPacket) {
		bracket.ifPresent(p -> nbt.put("Bracket", NbtUtils.writeBlockState(p)));
		if (clientPacket && reRender) {
			NBTHelper.putMarker(nbt, "Redraw");
			reRender = false;
		}
		super.write(nbt, clientPacket);
	}

	@Override
	public void read(CompoundTag nbt, boolean clientPacket) {
		bracket = Optional.empty();
		if (nbt.contains("Bracket"))
			bracket = Optional.of(NbtUtils.readBlockState(nbt.getCompound("Bracket")));
		if (clientPacket && nbt.contains("Redraw"))
			getWorld().sendBlockUpdated(getPos(), tileEntity.getBlockState(), tileEntity.getBlockState(), 16);
		super.read(nbt, clientPacket);
	}

	public boolean canHaveBracket() {
		return pred.test(tileEntity.getBlockState());
	}

}
