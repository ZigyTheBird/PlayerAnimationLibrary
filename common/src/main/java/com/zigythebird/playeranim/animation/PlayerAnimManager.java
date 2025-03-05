package com.zigythebird.playeranim.animation;

import com.zigythebird.playeranim.animation.layered.IAnimation;
import com.zigythebird.playeranim.api.firstPerson.FirstPersonConfiguration;
import com.zigythebird.playeranim.api.firstPerson.FirstPersonMode;
import com.zigythebird.playeranim.cache.PlayerAnimBone;
import com.zigythebird.playeranim.dataticket.DataTicket;
import com.zigythebird.playeranim.math.Pair;
import com.zigythebird.playeranim.math.Vec3f;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
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

	private float lastUpdateTime;
	private boolean isFirstTick = true;
	private float firstTickTime = -1;
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

	public float getLastUpdateTime() {
		return this.lastUpdateTime;
	}

	public void updatedAt(float updateTime) {
		this.lastUpdateTime = updateTime;
	}

	public float getFirstTickTime() {
		return this.firstTickTime;
	}

	public void startedAt(float time) {
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
	public boolean isActive() {
		for (Pair<Integer, IAnimation> layer : layers) {
			if (layer.getRight().isActive()) return true;
		}
		return false;
	}

	@Override
	public void tick(AnimationState state) {
		for (Pair<Integer, IAnimation> layer : layers) {
			if (layer.getRight().isActive()) {
				layer.getRight().tick(state);
			}
		}
	}

	@Override
	public void get3DTransform(@NotNull PlayerAnimBone bone) {
		for (Pair<Integer, IAnimation> layer : layers) {
			if (layer.getRight().isActive() && (!FirstPersonMode.isFirstPersonPass() || layer.getRight().getFirstPersonMode().isEnabled())) {
				layer.getRight().get3DTransform(bone);
			}
		}
	}

	@Override
	public void setupAnim(AnimationState state) {
		for (Pair<Integer, IAnimation> layer : layers) {
			layer.getRight().setupAnim(state);
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

	public void updatePart(String partName, ModelPart part, AnimationProcessor processor) {
		PlayerAnimBone bone = processor.getBone(partName);
		PartPose initialPose = part.getInitialPose();

		part.x = bone.getPosX() + initialPose.x();
		part.y = bone.getPosY() + initialPose.y();
		part.z = bone.getPosZ() + initialPose.z();

		part.xRot = bone.getRotX();
		part.yRot = bone.getRotY();
		part.zRot = bone.getRotZ();

		part.xScale = bone.getScaleX();
		part.yScale = bone.getScaleY();
		part.zScale = bone.getScaleZ();
	}
}
