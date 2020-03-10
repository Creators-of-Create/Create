package com.simibubi.create.foundation.item;

import static com.simibubi.create.foundation.item.AllToolTypes.AXE;
import static com.simibubi.create.foundation.item.AllToolTypes.HOE;
import static com.simibubi.create.foundation.item.AllToolTypes.PICKAXE;
import static com.simibubi.create.foundation.item.AllToolTypes.SHOVEL;
import static com.simibubi.create.foundation.item.AllToolTypes.SWORD;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import com.simibubi.create.foundation.packet.SimplePacketBase;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.item.ToolItem;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public abstract class AbstractToolItem extends ToolItem {

	protected AllToolTypes[] toolTypes;

	public AbstractToolItem(float attackDamageIn, float attackSpeedIn, IItemTier tier, Properties builder,
			AllToolTypes... types) {
		super(attackDamageIn, attackSpeedIn, tier, Collections.emptySet(), setToolTypes(builder, tier, types));
		toolTypes = types;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		boolean canEnchant = super.canApplyAtEnchantingTable(stack, enchantment);
		for (AllToolTypes type : toolTypes) {
			switch (type) {
			case AXE:
				canEnchant |= enchantment.canApply(new ItemStack(Items.DIAMOND_AXE));
				break;
			case HOE:
				canEnchant |= enchantment.canApply(new ItemStack(Items.DIAMOND_HOE));
				break;
			case PICKAXE:
				canEnchant |= enchantment.canApply(new ItemStack(Items.DIAMOND_PICKAXE));
				break;
			case SHEARS:
				canEnchant |= enchantment.canApply(new ItemStack(Items.SHEARS));
				break;
			case SHOVEL:
				canEnchant |= enchantment.canApply(new ItemStack(Items.DIAMOND_SHOVEL));
				break;
			case SWORD:
				canEnchant |= enchantment.canApply(new ItemStack(Items.DIAMOND_SWORD));
				break;
			default:
				break;
			}
		}

		return canEnchant;
	}

	private static Properties setToolTypes(Properties builder, IItemTier tier, AllToolTypes... types) {
		for (AllToolTypes type : types) {
			if (type == PICKAXE)
				builder.addToolType(ToolType.PICKAXE, tier.getHarvestLevel());
			if (type == SHOVEL)
				builder.addToolType(ToolType.SHOVEL, tier.getHarvestLevel());
			if (type == AXE)
				builder.addToolType(ToolType.AXE, tier.getHarvestLevel());
		}
		return builder;
	}

	protected boolean hasType(AllToolTypes typeIn) {
		for (AllToolTypes type : toolTypes)
			if (type == typeIn)
				return true;
		return false;
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		if (hasType(HOE) && !context.isPlacerSneaking())
			return Items.WOODEN_HOE.onItemUse(context);
		if (hasType(SHOVEL))
			return Items.WOODEN_SHOVEL.onItemUse(context);
		return super.onItemUse(context);
	}

	@Override
	public boolean canHarvestBlock(ItemStack stack, BlockState state) {
		return super.canHarvestBlock(stack, state) || getToolTypes(stack).contains(state.getHarvestTool())
				|| hasType(SWORD) && Items.WOODEN_SWORD.canHarvestBlock(stack, state);
	}

	@Override
	public boolean canPlayerBreakBlockWhileHolding(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
		return !(hasType(SWORD) && !player.isCreative());
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state) {
		if (hasType(SWORD))
			return Items.WOODEN_SWORD.getDestroySpeed(stack, state);
		return super.getDestroySpeed(stack, state);
	}

	public boolean modifiesDrops() {
		return false;
	}

	public void modifyDrops(final Collection<ItemStack> drops, IWorld world, BlockPos pos, ItemStack tool, BlockState state) {
	}

	public void spawnParticles(IWorld world, BlockPos pos, ItemStack tool, BlockState state) {
	}

	public static class HarvestPacket extends SimplePacketBase {

		private BlockState state;
		private ItemStack stack;
		private BlockPos pos;
		private boolean self;

		public HarvestPacket(BlockState state, ItemStack stack, BlockPos pos, boolean self) {
			this.state = state;
			this.stack = stack;
			this.pos = pos;
			this.self = self;
		}

		public HarvestPacket(PacketBuffer buffer) {
			state = NBTUtil.readBlockState(buffer.readCompoundTag());
			stack = buffer.readItemStack();
			pos = buffer.readBlockPos();
			self = buffer.readBoolean();
		}

		@Override
		public void write(PacketBuffer buffer) {
			buffer.writeCompoundTag(NBTUtil.writeBlockState(state));
			buffer.writeItemStack(stack);
			buffer.writeBlockPos(pos);
			buffer.writeBoolean(self);
		}

		@Override
		public void handle(Supplier<Context> context) {
			context.get().enqueueWork(() -> DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> this.spawnParticles(self)));
			context.get().setPacketHandled(true);
		}

		@OnlyIn(Dist.CLIENT)
		void spawnParticles(boolean self) {
			if (!(stack.getItem() instanceof AbstractToolItem))
				return;
			ClientWorld world = Minecraft.getInstance().world;
			if (!self)
				world.playEvent(2001, pos, Block.getStateId(state));
			((AbstractToolItem) stack.getItem()).spawnParticles(world, pos, stack, state);
		}
	}

}
