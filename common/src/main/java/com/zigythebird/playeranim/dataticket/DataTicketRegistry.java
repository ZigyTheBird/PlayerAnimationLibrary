package com.zigythebird.playeranim.dataticket;

import com.zigythebird.playeranim.PlayerAnimLibMod;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores the default (builtin) {@link DataTicket DataTicketRegistry}
 * <p>
 * Additionally handles registration of SerializableDataTickets
 */
public final class DataTicketRegistry {
	private static final Map<String, SerializableDataTicket<?>> SERIALIZABLE_TICKETS = new ConcurrentHashMap<>();

	@Nullable
	public static SerializableDataTicket<?> byName(String id) {
		return SERIALIZABLE_TICKETS.getOrDefault(id, null);
	}

	/**
	 * Register a {@link SerializableDataTicket} with GeckoLib for handling custom data transmission
	 * <p>
	 * It is recommended you don't call this directly, and instead call it via {@link GeckoLibUtil#addDataTicket}
	 *
	 * @param ticket The SerializableDataTicket instance to register
	 * @return The registered instance
	 */
	public static <D> SerializableDataTicket<D> registerSerializable(SerializableDataTicket<D> ticket) {
		SerializableDataTicket<?> existingTicket = SERIALIZABLE_TICKETS.putIfAbsent(ticket.id(), ticket);

		if (existingTicket != null)
			PlayerAnimLibMod.LOGGER.error("Duplicate SerializableDataTicket registered! This will cause issues. Existing: {} , New: {}", existingTicket.id(), ticket.id());

		return ticket;
	}
}
