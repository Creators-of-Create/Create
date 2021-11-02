package com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllKeys;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform.Sided;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueBehaviour.StepContext;
import com.simibubi.create.foundation.utility.animation.PhysicalFloat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ScrollValueHandler {

	private static float lastPassiveScroll = 0.0f;
	private static float passiveScroll = 0.0f;
	private static float passiveScrollDirection = 1f;
	private static final PhysicalFloat wrenchCog = PhysicalFloat.create()
		.withDrag(0.3);

	@OnlyIn(Dist.CLIENT)
	public static boolean onScroll(double delta) {
		HitResult objectMouseOver = Minecraft.getInstance().hitResult;
		if (!(objectMouseOver instanceof BlockHitResult))
			return false;

		BlockHitResult result = (BlockHitResult) objectMouseOver;
		Minecraft mc = Minecraft.getInstance();
		ClientLevel world = mc.level;
		BlockPos blockPos = result.getBlockPos();

		ScrollValueBehaviour scrolling = TileEntityBehaviour.get(world, blockPos, ScrollValueBehaviour.TYPE);
		if (scrolling == null)
			return false;
		if (!scrolling.isActive())
			return false;
		if (!mc.player.mayBuild())
			return false;

		passiveScrollDirection = (float) -delta;
		wrenchCog.bump(3, -delta * 10);
		int prev = scrolling.scrollableValue;

		if (scrolling.needsWrench && !AllItems.WRENCH.isIn(mc.player.getMainHandItem()))
			return false;
		if (scrolling.slotPositioning instanceof Sided)
			((Sided) scrolling.slotPositioning).fromSide(result.getDirection());
		if (!scrolling.testHit(objectMouseOver.getLocation()))
			return false;

		if (scrolling instanceof BulkScrollValueBehaviour && AllKeys.ctrlDown()) {
			BulkScrollValueBehaviour bulkScrolling = (BulkScrollValueBehaviour) scrolling;
			for (SmartTileEntity te : bulkScrolling.getBulk()) {
				ScrollValueBehaviour other = te.getBehaviour(ScrollValueBehaviour.TYPE);
				if (other != null)
					applyTo(delta, other);
			}

		} else
			applyTo(delta, scrolling);

		if (prev != scrolling.scrollableValue) {
			float pitch = (scrolling.scrollableValue - scrolling.min) / (float) (scrolling.max - scrolling.min);
			pitch = Mth.lerp(pitch, 1.5f, 2f);
			AllSoundEvents.SCROLL_VALUE.play(world, mc.player, blockPos, 1, pitch);
		}
		return true;
	}

	public static float getScroll(float partialTicks) {
		return wrenchCog.getValue(partialTicks) + Mth.lerp(partialTicks, lastPassiveScroll, passiveScroll);
	}

	@OnlyIn(Dist.CLIENT)
	public static void tick() {
		if (!Minecraft.getInstance()
			.isPaused()) {
			lastPassiveScroll = passiveScroll;
			wrenchCog.tick();
			passiveScroll += passiveScrollDirection * 0.5;
		}
	}

	protected static void applyTo(double delta, ScrollValueBehaviour scrolling) {
		scrolling.ticksUntilScrollPacket = 10;
		int valueBefore = scrolling.scrollableValue;

		StepContext context = new StepContext();
		context.control = AllKeys.ctrlDown();
		context.shift = AllKeys.shiftDown();
		context.currentValue = scrolling.scrollableValue;
		context.forward = delta > 0;

		double newValue = scrolling.scrollableValue + Math.signum(delta) * scrolling.step.apply(context);
		scrolling.scrollableValue = (int) Mth.clamp(newValue, scrolling.min, scrolling.max);

		if (valueBefore != scrolling.scrollableValue)
			scrolling.clientCallback.accept(scrolling.scrollableValue);
	}

}
