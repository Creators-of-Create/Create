package com.simibubi.create.content.contraptions.components.structureMovement.train;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.simibubi.create.content.contraptions.components.structureMovement.train.MinecartCouplingSerializer.CouplingData;
import com.simibubi.create.foundation.utility.Couple;

import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.world.World;

public class MinecartCoupling {

	WeakReference<AbstractMinecartEntity> mainCart;
	WeakReference<AbstractMinecartEntity> connectedCart;
	double length;

	private MinecartCoupling(AbstractMinecartEntity mainCart, AbstractMinecartEntity connectedCart, double length) {
		this.mainCart = new WeakReference<>(mainCart);
		this.connectedCart = new WeakReference<>(connectedCart);
		this.length = length;
	}

	public static MinecartCoupling create(AbstractMinecartEntity mainCart, AbstractMinecartEntity connectedCart) {
		return new MinecartCoupling(mainCart, connectedCart,
			Math.max(2, getDistanceBetweenCarts(mainCart, connectedCart)));
	}

	@Nullable
	public static List<MinecartCoupling> loadAllAttached(World world, AbstractMinecartEntity minecart) {
		List<MinecartCoupling> loaded = new ArrayList<>(2);
		List<AbstractMinecartEntity> otherCartsInRange =
			world.getEntitiesWithinAABB(AbstractMinecartEntity.class, minecart.getBoundingBox()
				.grow(MinecartCouplingHandler.maxDistance()), c -> c != minecart);

		List<CouplingData> couplingData = MinecartCouplingSerializer.getCouplingData(minecart);
		Connections: for (CouplingData connection : couplingData) {
			boolean cartIsMain = connection.main;
			UUID cartCouplingUUID = connection.id;

			for (AbstractMinecartEntity otherCart : otherCartsInRange) {
				List<CouplingData> otherCouplingData = MinecartCouplingSerializer.getCouplingData(otherCart);
				for (CouplingData otherConnection : otherCouplingData) {
					boolean otherIsMain = otherConnection.main;
					UUID otherCouplingUUID = otherConnection.id;

					if (cartIsMain == otherIsMain)
						continue;
					if (!otherCouplingUUID.equals(cartCouplingUUID))
						continue;

					AbstractMinecartEntity mainCart = cartIsMain ? minecart : otherCart;
					AbstractMinecartEntity connectedCart = cartIsMain ? otherCart : minecart;
					loaded.add(new MinecartCoupling(mainCart, connectedCart, connection.length));
					continue Connections;
				}
			}
		}

		return loaded;
	}

	public void writeToCarts() {
		MinecartCouplingSerializer.removeCouplingFromCart(mainCart.get(), this);
		MinecartCouplingSerializer.removeCouplingFromCart(connectedCart.get(), this);

		MinecartCouplingSerializer.addCouplingToCart(mainCart.get(), this);
		MinecartCouplingSerializer.addCouplingToCart(connectedCart.get(), this);
	}

	/**
	 * Swap main and connected cart for aliging couplings of a train.<br>
	 * Changes are written to the carts' nbt data!<br>
	 * Id of this coupling will change!
	 */
	public void flip() {
		MinecartCouplingSerializer.removeCouplingFromCart(mainCart.get(), this);
		MinecartCouplingSerializer.removeCouplingFromCart(connectedCart.get(), this);

		WeakReference<AbstractMinecartEntity> oldMain = mainCart;
		mainCart = connectedCart;
		connectedCart = oldMain;

		MinecartCouplingSerializer.addCouplingToCart(mainCart.get(), this);
		MinecartCouplingSerializer.addCouplingToCart(connectedCart.get(), this);
	}

	public static double getDistanceBetweenCarts(AbstractMinecartEntity mainCart,
		AbstractMinecartEntity connectedCart) {
		return mainCart.getBoundingBox()
			.getCenter()
			.subtract(connectedCart.getBoundingBox()
				.getCenter())
			.length();
	}

	public boolean areBothEndsPresent() {
		return (mainCart.get() != null && mainCart.get()
			.isAlive()) && (connectedCart.get() != null
				&& connectedCart.get()
					.isAlive());
	}

	public UUID getId() {
		return mainCart.get()
			.getUniqueID();
	}

	public Couple<AbstractMinecartEntity> asCouple() {
		return Couple.create(mainCart.get(), connectedCart.get());
	}

}