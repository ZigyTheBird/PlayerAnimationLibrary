package com.zigythebird.playeranim.dataticket;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores the default (builtin) {@link DataTicket DataTickets}
 * <p>
 * Additionally handles registration of SerializableDataTickets
 */
public final class DataTickets {
//	private static final Map<String, SerializableDataTicket<?>> SERIALIZABLE_TICKETS = new ConcurrentHashMap<>();
	
	// Builtin tickets
	// These tickets are used by GeckoLib by default, usually added in by the GeoRenderer for use in animations
	public static final DataTicket<BlockEntity> BLOCK_ENTITY = new DataTicket<>("block_entity", BlockEntity.class);
	public static final DataTicket<ItemStack> ITEMSTACK = new DataTicket<>("itemstack", ItemStack.class);
	public static final DataTicket<Entity> ENTITY = new DataTicket<>("entity", Entity.class);
	public static final DataTicket<EquipmentSlot> EQUIPMENT_SLOT = new DataTicket<>("equipment_slot", EquipmentSlot.class);
	public static final DataTicket<Float> TICK = new DataTicket<>("tick", Float.class);
	public static final DataTicket<ItemDisplayContext> ITEM_RENDER_PERSPECTIVE = new DataTicket<>("item_render_perspective", ItemDisplayContext.class);

	//Todo: Implement data tickets and serializable data tickets in a way useful to mod developers.
	//Probably make data tickets use resource locations as their IDs so no conflicts

	// Builtin serializable tickets
	// These are not used anywhere by default, but are provided as examples and for ease of use
//	public static final SerializableDataTicket<Integer> ANIM_STATE = GeckoLibUtil.addDataTicket(SerializableDataTicket.ofInt(GeckoLibConstants.id("anim_state")));
//	public static final SerializableDataTicket<String> ANIM = GeckoLibUtil.addDataTicket(SerializableDataTicket.ofString(GeckoLibConstants.id("anim")));
//	public static final SerializableDataTicket<Integer> USE_TICKS = GeckoLibUtil.addDataTicket(SerializableDataTicket.ofInt(GeckoLibConstants.id("use_ticks")));
//	public static final SerializableDataTicket<Boolean> ACTIVE = GeckoLibUtil.addDataTicket(SerializableDataTicket.ofBoolean(GeckoLibConstants.id("active")));
//	public static final SerializableDataTicket<Boolean> OPEN = GeckoLibUtil.addDataTicket(SerializableDataTicket.ofBoolean(GeckoLibConstants.id("open")));
//	public static final SerializableDataTicket<Boolean> CLOSED = GeckoLibUtil.addDataTicket(SerializableDataTicket.ofBoolean(GeckoLibConstants.id("closed")));
//	public static final SerializableDataTicket<Direction> DIRECTION = GeckoLibUtil.addDataTicket(SerializableDataTicket.ofEnum(GeckoLibConstants.id("direction"), Direction.class));
//
//	@Nullable
//	public static SerializableDataTicket<?> byName(String id) {
//		return SERIALIZABLE_TICKETS.getOrDefault(id, null);
//	}
//
//	/**
//	 * Register a {@link SerializableDataTicket} with GeckoLib for handling custom data transmission
//	 * <p>
//	 * It is recommended you don't call this directly, and instead call it via {@link GeckoLibUtil#addDataTicket}
//	 *
//	 * @param ticket The SerializableDataTicket instance to register
//	 * @return The registered instance
//	 */
//	public static <D> SerializableDataTicket<D> registerSerializable(SerializableDataTicket<D> ticket) {
//		SerializableDataTicket<?> existingTicket = SERIALIZABLE_TICKETS.putIfAbsent(ticket.id(), ticket);
//
//		if (existingTicket != null)
//			GeckoLibConstants.LOGGER.error("Duplicate SerializableDataTicket registered! This will cause issues. Existing: " + existingTicket.id() + ", New: " + ticket.id());
//
//		return ticket;
//	}
}
