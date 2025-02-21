package com.zigythebird.playeranim.animation;

import com.zigythebird.playeranim.animation.layered.IAnimation;
import com.zigythebird.playeranim.api.firstPerson.FirstPersonConfiguration;
import com.zigythebird.playeranim.api.firstPerson.FirstPersonMode;
import com.zigythebird.playeranim.dataticket.DataTicket;
import com.zigythebird.playeranim.math.Pair;
import com.zigythebird.playeranim.math.Vec3f;
import com.zigythebird.playeranim.util.MathHelper;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;

/**
 * The animation data collection for a given player instance
 * <p>
 * Generally speaking, a single working-instance of a player will have a single instance of {@code PlayerAnimManager} associated with it
 */
public class PlayerAnimManager implements IAnimation {
	private final Map<String, BoneSnapshot> boneSnapshotCollection = new Object2ObjectOpenHashMap<>();
	private final ArrayList<Pair<Integer, IAnimation>> layers = new ArrayList<>();
	private Map<DataTicket<?>, Object> extraData;
	private AbstractClientPlayer player;

	private double lastUpdateTime;
	private boolean isFirstTick = true;
	private double firstTickTime = -1;
	private float tickDelta;

	public PlayerAnimManager(AbstractClientPlayer player) {
		this.player = player;
	}

	/**
	 * DON'T YOU DARE TOUCH THIS! - Zigy
	 */
	@ApiStatus.Internal
	protected ArrayList<Pair<Integer, IAnimation>> getLayers() {
		return this.layers;
	}

	public Map<String, BoneSnapshot> getBoneSnapshotCollection() {
		return this.boneSnapshotCollection;
	}

	public void clearSnapshotCache() {
		getBoneSnapshotCollection().clear();
	}

	public double getLastUpdateTime() {
		return this.lastUpdateTime;
	}

	public void updatedAt(double updateTime) {
		this.lastUpdateTime = updateTime;
	}

	public double getFirstTickTime() {
		return this.firstTickTime;
	}

	public void startedAt(double time) {
		this.firstTickTime = time;
	}

	public boolean isFirstTick() {
		return this.isFirstTick;
	}

	protected void finishFirstTick() {
		this.isFirstTick = false;
	}

	public float getTickDelta() {
		return this.tickDelta;
	}

	public void setTickDelta(float tickDelta) {
		this.tickDelta = tickDelta;
	}

	/**
	 * Set a custom data point to be used later
	 *
	 * @param dataTicket The DataTicket for the data point
	 * @param data The piece of data to store
	 */
	public <D> void setData(DataTicket<D> dataTicket, D data) {
		if (this.extraData == null)
			this.extraData = new Object2ObjectOpenHashMap<>();

		this.extraData.put(dataTicket, data);
	}

	/**
	 * Retrieve a custom data point that was stored earlier, or null if it hasn't been stored
	 */
	public <D> D getData(DataTicket<D> dataTicket) {
		return this.extraData != null ? dataTicket.getData(this.extraData) : null;
	}

	public AbstractClientPlayer getPlayer() {
		return player;
	}

	@Override
	public @NotNull State getAnimationState() {
		for (Pair<Integer, IAnimation> layer : layers) {
			if (layer.getRight().isActive()) return State.RUNNING;
		}
		return State.STOPPED;
	}

	@Override
	public void tick() {
		for (Pair<Integer, IAnimation> layer : layers) {
			if (layer.getRight().isActive()) {
				layer.getRight().tick();
			}
		}
	}

	@Override
	public @NotNull Vec3f get3DTransform(@NotNull String modelName, @NotNull TransformType type, float tickDelta, @NotNull Vec3f value0) {
		for (Pair<Integer, IAnimation> layer : layers) {
			if (layer.getRight().isActive() && (!FirstPersonMode.isFirstPersonPass() || layer.getRight().getFirstPersonMode().isEnabled())) {
				value0 = layer.getRight().get3DTransform(modelName, type, tickDelta, value0);
			}
		}
		return value0;
	}

	public @NotNull Vec3f get3DTransform(@NotNull String modelName, @NotNull TransformType type, @NotNull Vec3f value0) {
		return get3DTransform(modelName, type, tickDelta, value0);
	}

	@Override
	public void setupAnim(float tickDelta) {
		for (Pair<Integer, IAnimation> layer : layers) {
			layer.getRight().setupAnim(tickDelta);
		}
	}
	
	/**
	 * Add an animation layer.
	 * If there are multiple layers with the same priority, the one added first will have more priority
	 * @param priority priority
	 * @param layer    animation layer
	 */
	public void addAnimLayer(int priority, IAnimation layer) {
		int search = 0;
		//Insert the layer into the correct slot
		while (layers.size() > search && layers.get(search).getLeft() < priority) {
			search++;
		}
		layers.add(search, new Pair<>(priority, layer));
	}

	/**
	 * Remove an animation layer
	 * @param layer needle
	 * @return true if any elements were removed.
	 */
	public boolean removeLayer(IAnimation layer) {
		return layers.removeIf(integerIAnimationPair -> integerIAnimationPair.getRight() == layer);
	}

	/**
	 * Remove EVERY layer with priority
	 * @param layerLevel search and destroy
	 * @return true if any elements were removed.
	 */
	public boolean removeLayer(int layerLevel) {
		return layers.removeIf(integerIAnimationPair -> integerIAnimationPair.getLeft() == layerLevel);
	}

	@Override
	public @NotNull FirstPersonMode getFirstPersonMode() {
		for (int i = layers.size(); i > 0;) {
			Pair<Integer, IAnimation> layer = layers.get(--i);
			if (layer.getRight().isActive()) { // layer.right.requestFirstPersonMode(tickDelta).takeIf{ it != NONE }?.let{ return@requestFirstPersonMode it }
				FirstPersonMode mode = layer.getRight().getFirstPersonMode();
				if (mode != FirstPersonMode.NONE) return mode;
			}
		}
		return FirstPersonMode.NONE;
	}

	@Override
	public @NotNull FirstPersonConfiguration getFirstPersonConfiguration() {
		for (int i = layers.size(); i > 0;) {
			Pair<Integer, IAnimation> layer = layers.get(--i);
			if (layer.getRight().isActive()) { // layer.right.requestFirstPersonMode(tickDelta).takeIf{ it != NONE }?.let{ return@requestFirstPersonMode it }
				FirstPersonMode mode = layer.getRight().getFirstPersonMode();
				if (mode != FirstPersonMode.NONE) return layer.getRight().getFirstPersonConfiguration();
			}
		}
		return IAnimation.super.getFirstPersonConfiguration();
	}

	public int getPriority() {
		int priority = 0;
		for (int i=layers.size()-1; i>=0; i--) {
			Pair<Integer, IAnimation> layer = layers.get(i);
			if (layer.getRight().isActive()) {
				priority = layer.getLeft();
				break;
			}
		}
		return priority;
	}

	public void updatePart(String partName, ModelPart part) {
		Vec3f pos = this.get3DTransform(partName, TransformType.POSITION, tickDelta, new Vec3f(part.x, part.y, part.z));
		part.x = pos.getX();
		part.y = pos.getY();
		part.z = pos.getZ();
		Vec3f rot = this.get3DTransform(partName, TransformType.ROTATION, tickDelta, new Vec3f( // clamp guards
				MathHelper.clampToRadian(part.xRot),
				MathHelper.clampToRadian(part.yRot),
				MathHelper.clampToRadian(part.zRot)));
		part.setRotation(rot.getX(), rot.getY(), rot.getZ());
		Vec3f scale = this.get3DTransform(partName, TransformType.SCALE, tickDelta,
				new Vec3f(part.xScale, part.yScale, part.zScale)
		);
		part.xScale = scale.getX();
		part.yScale = scale.getY();
		part.zScale = scale.getZ();
	}
}
