package com.zigythebird.playeranim.animation;

import com.zigythebird.playeranim.accessors.IModelPart;
import com.zigythebird.playeranim.animation.layered.AnimationStack;
import com.zigythebird.playeranim.animation.layered.IAnimation;
import com.zigythebird.playeranim.bones.BoneSnapshot;
import com.zigythebird.playeranim.bones.PlayerAnimBone;
import com.zigythebird.playeranim.dataticket.DataTicket;
import com.zigythebird.playeranim.math.Pair;
import com.zigythebird.playeranim.util.RenderUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.player.AbstractClientPlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;

/**
 * The animation data collection for a given player instance
 * <p>
 * Generally speaking, a single working-instance of a player will have a single instance of {@code PlayerAnimManager} associated with it
 */
public class PlayerAnimManager extends AnimationStack {
	private final Map<String, BoneSnapshot> boneSnapshotCollection = new Object2ObjectOpenHashMap<>();
	private Map<DataTicket<?>, Object> extraData;
	private final AbstractClientPlayer player;

	private float lastUpdateTime;
	private boolean isFirstTick = true;
	private float firstTickTime = -1;
	private float tickDelta;

	public PlayerAnimManager(AbstractClientPlayer player) {
		this.player = player;
	}

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

	/**
	 * If you touch this, you're a horrible person.
	 */
	@ApiStatus.Internal
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
	public @Nullable <D> D getData(DataTicket<D> dataTicket) {
		return this.extraData != null ? dataTicket.getData(this.extraData) : null;
	}

	public AbstractClientPlayer getPlayer() {
		return player;
	}

	public void updatePart(String partName, ModelPart part, AnimationProcessor processor) {
		PlayerAnimBone bone = processor.getBone(partName);
		PartPose initialPose = part.getInitialPose();
		this.get3DTransform(bone);
		RenderUtil.translatePartToBone(part, bone, initialPose);

		if (bone.getParent() != null && bone.getParent() != bone)
			((IModelPart)part).playerAnimLib$setParent(bone.getParent());
		else ((IModelPart)part).playerAnimLib$setParent(null);
	}
}
