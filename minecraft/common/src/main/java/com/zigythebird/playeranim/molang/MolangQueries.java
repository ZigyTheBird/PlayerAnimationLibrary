/*
 * MIT License
 *
 * Copyright (c) 2024 GeckoLib
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.zigythebird.playeranim.molang;

import com.zigythebird.playeranim.animation.AvatarAnimationController;
import com.zigythebird.playeranimcore.animation.AnimationController;
import com.zigythebird.playeranimcore.molang.MolangLoader;
import com.zigythebird.playeranimcore.molang.QueryBinding;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.ClientMannequin;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;

import java.util.Arrays;
import java.util.Optional;

public final class MolangQueries {
    public static final String ACTOR_COUNT = "actor_count";
    public static final String BLOCKING = "blocking";
    public static final String BODY_X_ROTATION = "body_x_rotation";
    public static final String BODY_Y_ROTATION = "body_y_rotation";
    public static final String CARDINAL_FACING = "cardinal_facing";
    public static final String CARDINAL_FACING_2D = "cardinal_facing_2d";
    public static final String CARDINAL_PLAYER_FACING = "cardinal_player_facing";
    public static final String DAY = "day";
    public static final String DEATH_TICKS = "death_ticks";
    public static final String DISTANCE_FROM_CAMERA = "distance_from_camera";
    public static final String EQUIPMENT_COUNT = "equipment_count";
    public static final String FRAME_ALPHA = "frame_alpha";
    public static final String GET_ACTOR_INFO_ID = "get_actor_info_id";
    public static final String GROUND_SPEED = "ground_speed";
    public static final String HAS_CAPE = "has_cape";
    public static final String HAS_COLLISION = "has_collision";
    public static final String HAS_GRAVITY = "has_gravity";
    public static final String HAS_HEAD_GEAR = "has_head_gear";
    public static final String HAS_OWNER = "has_owner";
    public static final String HAS_PLAYER_RIDER = "has_player_rider";
    public static final String HAS_RIDER = "has_rider";
    public static final String HEAD_X_ROTATION = "head_x_rotation";
    public static final String HEAD_Y_ROTATION = "head_y_rotation";
    public static final String HEALTH = "health";
    public static final String HURT_TIME = "hurt_time";
    public static final String INVULNERABLE_TICKS = "invulnerable_ticks";
    public static final String IS_ALIVE = "is_alive";
    public static final String IS_ANGRY = "is_angry";
    public static final String IS_BABY = "is_baby";
    public static final String IS_BREATHING = "is_breathing";
    public static final String IS_FIRE_IMMUNE = "is_fire_immune";
    public static final String IS_FIRST_PERSON = "is_first_person";
    public static final String IS_IN_CONTACT_WITH_WATER = "is_in_contact_with_water";
    public static final String IS_IN_LAVA = "is_in_lava";
    public static final String IS_IN_WATER = "is_in_water";
    public static final String IS_IN_WATER_OR_RAIN = "is_in_water_or_rain";
    public static final String IS_INVISIBLE = "is_invisible";
    public static final String IS_LEASHED = "is_leashed";
    public static final String IS_MOVING = "is_moving";
    public static final String IS_ON_FIRE = "is_on_fire";
    public static final String IS_ON_GROUND = "is_on_ground";
    public static final String IS_RIDING = "is_riding";
    public static final String IS_SADDLED = "is_saddled";
    public static final String IS_SILENT = "is_silent";
    public static final String IS_SLEEPING = "is_sleeping";
    public static final String IS_SNEAKING = "is_sneaking";
    public static final String IS_SPRINTING = "is_sprinting";
    public static final String IS_SWIMMING = "is_swimming";
    public static final String IS_USING_ITEM = "is_using_item";
    public static final String IS_WALL_CLIMBING = "is_wall_climbing";
    public static final String LIFE_TIME = "life_time";
    public static final String LIMB_SWING = "limb_swing";
    public static final String LIMB_SWING_AMOUNT = "limb_swing_amount";
    public static final String MAIN_HAND_ITEM_MAX_DURATION = "main_hand_item_max_duration";
    public static final String MAIN_HAND_ITEM_USE_DURATION = "main_hand_item_use_duration";
    public static final String MAX_HEALTH = "max_health";
    public static final String MOON_BRIGHTNESS = "moon_brightness";
    public static final String MOON_PHASE = "moon_phase";
    public static final String MOVEMENT_DIRECTION = "movement_direction";
    public static final String PLAYER_LEVEL = "player_level";
    public static final String RIDER_BODY_X_ROTATION = "rider_body_x_rotation";
    public static final String RIDER_BODY_Y_ROTATION = "rider_body_y_rotation";
    public static final String RIDER_HEAD_X_ROTATION = "rider_head_x_rotation";
    public static final String RIDER_HEAD_Y_ROTATION = "rider_head_y_rotation";
    public static final String SCALE = "scale";
    public static final String SLEEP_ROTATION = "sleep_rotation";
    public static final String TIME_OF_DAY = "time_of_day";
    public static final String TIME_STAMP = "time_stamp";
    public static final String VERTICAL_SPEED = "vertical_speed";
    public static final String YAW_SPEED = "yaw_speed";

    public static void setDefaultQueryValues(QueryBinding<AnimationController> binding) {
        MolangLoader.setDoubleQuery(binding, ACTOR_COUNT, actor -> Minecraft.getInstance().levelRenderer.levelRenderState.entityRenderStates.size());
        MolangLoader.setDoubleQuery(binding, CARDINAL_PLAYER_FACING, actor -> ((AvatarAnimationController) actor).getAvatar().getDirection().ordinal());
        MolangLoader.setDoubleQuery(binding, DAY, actor -> ((AvatarAnimationController) actor).getAvatar().level().getGameTime() / 24000d);
        MolangLoader.setDoubleQuery(binding, FRAME_ALPHA, actor -> actor.getAnimationData().getPartialTick());
        MolangLoader.setBoolQuery(binding, HAS_CAPE, actor -> {
            Avatar avatar = ((AvatarAnimationController) actor).getAvatar();
            if (avatar instanceof AbstractClientPlayer player) {
                return player.getSkin().cape() != null;
            } else if (avatar instanceof ClientMannequin mannequin) {
                return mannequin.getSkin().cape() != null;
            } else {
                return false;
            }
        });
        MolangLoader.setBoolQuery(binding, IS_FIRST_PERSON, actor -> {
            Avatar avatar = ((AvatarAnimationController) actor).getAvatar();
            if (avatar instanceof AbstractClientPlayer player && player.isLocalPlayer()) {
                return Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON;
            } else {
                return false;
            }
        });
        MolangLoader.setDoubleQuery(binding, LIFE_TIME, actor -> actor.isActive() ? actor.getAnimationTime() : 0);
        MolangLoader.setDoubleQuery(binding, MOON_BRIGHTNESS, actor -> ((AvatarAnimationController) actor).getAvatar().level().getMoonBrightness());
        MolangLoader.setDoubleQuery(binding, MOON_PHASE, actor -> ((AvatarAnimationController) actor).getAvatar().level().getMoonPhase());
        MolangLoader.setDoubleQuery(binding, PLAYER_LEVEL, actor -> {
            Avatar avatar = ((AvatarAnimationController) actor).getAvatar();
            if (avatar instanceof AbstractClientPlayer player) {
                return player.experienceLevel;
            } else {
                return 0.0D;
            }
        });
        MolangLoader.setDoubleQuery(binding, TIME_OF_DAY, actor -> ((AvatarAnimationController) actor).getAvatar().level().getDayTime() / 24000d);
        MolangLoader.setDoubleQuery(binding, TIME_STAMP, actor -> ((AvatarAnimationController) actor).getAvatar().level().getGameTime());

        setDefaultEntityQueryValues(binding);
        setDefaultLivingEntityQueryValues(binding);
    }

    private static void setDefaultEntityQueryValues(QueryBinding<AnimationController> binding) {
        MolangLoader.setDoubleQuery(binding, BODY_X_ROTATION, actor -> ((AvatarAnimationController) actor).getAvatar().getViewXRot(actor.getAnimationData().getPartialTick()));
        MolangLoader.setDoubleQuery(binding, BODY_Y_ROTATION, actor -> ((AvatarAnimationController) actor).getAvatar() instanceof LivingEntity living ? Mth.lerp(actor.getAnimationData().getPartialTick(), living.yBodyRotO, living.yBodyRot) : ((AvatarAnimationController) actor).getAvatar().getViewYRot(actor.getAnimationData().getPartialTick()));
        MolangLoader.setDoubleQuery(binding, CARDINAL_FACING, actor -> ((AvatarAnimationController) actor).getAvatar().getDirection().get3DDataValue());
        MolangLoader.setDoubleQuery(binding, CARDINAL_FACING_2D, actor -> {
            int directionId = ((AvatarAnimationController) actor).getAvatar().getDirection().get3DDataValue();

            return directionId < 2 ? 6 : directionId;
        });
        MolangLoader.setDoubleQuery(binding, DISTANCE_FROM_CAMERA, actor -> Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().distanceTo(((AvatarAnimationController) actor).getAvatar().position()));
        MolangLoader.setDoubleQuery(binding, GET_ACTOR_INFO_ID, actor -> ((AvatarAnimationController) actor).getAvatar().getId());
        MolangLoader.setDoubleQuery(binding, EQUIPMENT_COUNT, actor -> ((AvatarAnimationController) actor).getAvatar() instanceof EquipmentUser armorable ? Arrays.stream(EquipmentSlot.values()).filter(EquipmentSlot::isArmor).filter(slot -> !armorable.getItemBySlot(slot).isEmpty()).count() : 0);
        MolangLoader.setBoolQuery(binding, HAS_COLLISION, actor -> !((AvatarAnimationController) actor).getAvatar().noPhysics);
        MolangLoader.setBoolQuery(binding, HAS_GRAVITY, actor -> !((AvatarAnimationController) actor).getAvatar().isNoGravity());
        MolangLoader.setBoolQuery(binding, HAS_OWNER, actor -> ((AvatarAnimationController) actor).getAvatar() instanceof OwnableEntity ownable && ownable.getOwnerReference() != null);
        MolangLoader.setBoolQuery(binding, HAS_PLAYER_RIDER, actor -> ((AvatarAnimationController) actor).getAvatar().hasPassenger(Player.class::isInstance));
        MolangLoader.setBoolQuery(binding, HAS_RIDER, actor -> ((AvatarAnimationController) actor).getAvatar().isVehicle());
        MolangLoader.setBoolQuery(binding, IS_ALIVE, actor -> ((AvatarAnimationController) actor).getAvatar().isAlive());
        MolangLoader.setBoolQuery(binding, IS_ANGRY, actor -> ((AvatarAnimationController) actor).getAvatar() instanceof NeutralMob neutralMob && neutralMob.isAngry());
        MolangLoader.setBoolQuery(binding, IS_BREATHING, actor -> ((AvatarAnimationController) actor).getAvatar().getAirSupply() >= ((AvatarAnimationController) actor).getAvatar().getMaxAirSupply());
        MolangLoader.setBoolQuery(binding, IS_FIRE_IMMUNE, actor -> ((AvatarAnimationController) actor).getAvatar().getType().fireImmune());
        MolangLoader.setBoolQuery(binding, IS_INVISIBLE, actor -> ((AvatarAnimationController) actor).getAvatar().isInvisible());
        MolangLoader.setBoolQuery(binding, IS_IN_CONTACT_WITH_WATER, actor -> ((AvatarAnimationController) actor).getAvatar().isInWaterOrRain());
        MolangLoader.setBoolQuery(binding, IS_IN_LAVA, actor -> ((AvatarAnimationController) actor).getAvatar().isInLava());
        MolangLoader.setBoolQuery(binding, IS_IN_WATER, actor -> ((AvatarAnimationController) actor).getAvatar().isInWater());
        MolangLoader.setBoolQuery(binding, IS_IN_WATER_OR_RAIN, actor -> ((AvatarAnimationController) actor).getAvatar().isInWaterOrRain());
        MolangLoader.setBoolQuery(binding, IS_LEASHED, actor -> ((AvatarAnimationController) actor).getAvatar() instanceof Leashable leashable && leashable.isLeashed());
        MolangLoader.setBoolQuery(binding, IS_MOVING, actor -> actor.getAnimationData().isMoving());
        MolangLoader.setBoolQuery(binding, IS_ON_FIRE, actor -> ((AvatarAnimationController) actor).getAvatar().isOnFire());
        MolangLoader.setBoolQuery(binding, IS_ON_GROUND, actor -> ((AvatarAnimationController) actor).getAvatar().onGround());
        MolangLoader.setBoolQuery(binding, IS_RIDING, actor -> ((AvatarAnimationController) actor).getAvatar().isPassenger());
        MolangLoader.setBoolQuery(binding, IS_SADDLED, actor -> ((AvatarAnimationController) actor).getAvatar() instanceof EquipmentUser saddleable && !saddleable.getItemBySlot(EquipmentSlot.SADDLE).isEmpty());
        MolangLoader.setBoolQuery(binding, IS_SILENT, actor -> ((AvatarAnimationController) actor).getAvatar().isSilent());
        MolangLoader.setBoolQuery(binding, IS_SNEAKING, actor -> ((AvatarAnimationController) actor).getAvatar().isCrouching());
        MolangLoader.setBoolQuery(binding, IS_SPRINTING, actor -> ((AvatarAnimationController) actor).getAvatar().isSprinting());
        MolangLoader.setBoolQuery(binding, IS_SWIMMING, actor -> ((AvatarAnimationController) actor).getAvatar().isSwimming());
        MolangLoader.setDoubleQuery(binding, MOVEMENT_DIRECTION, actor -> actor.getAnimationData().isMoving() ? Direction.getApproximateNearest(((AvatarAnimationController) actor).getAvatar().getDeltaMovement()).get3DDataValue() : 6);
        MolangLoader.setDoubleQuery(binding, RIDER_BODY_X_ROTATION, actor -> ((AvatarAnimationController) actor).getAvatar().isVehicle() ? ((AvatarAnimationController) actor).getAvatar().getFirstPassenger() instanceof LivingEntity ? 0 : ((AvatarAnimationController) actor).getAvatar().getFirstPassenger().getViewXRot(actor.getAnimationData().getPartialTick()) : 0);
        MolangLoader.setDoubleQuery(binding, RIDER_BODY_Y_ROTATION, actor -> ((AvatarAnimationController) actor).getAvatar().isVehicle() ? ((AvatarAnimationController) actor).getAvatar().getFirstPassenger() instanceof LivingEntity living ? Mth.lerp(actor.getAnimationData().getPartialTick(), living.yBodyRotO, living.yBodyRot) : ((AvatarAnimationController) actor).getAvatar().getFirstPassenger().getViewYRot(actor.getAnimationData().getPartialTick()) : 0);
        MolangLoader.setDoubleQuery(binding, RIDER_HEAD_X_ROTATION, actor -> ((AvatarAnimationController) actor).getAvatar().getFirstPassenger() instanceof LivingEntity living ? living.getViewXRot(actor.getAnimationData().getPartialTick()) : 0);
        MolangLoader.setDoubleQuery(binding, RIDER_HEAD_Y_ROTATION, actor -> ((AvatarAnimationController) actor).getAvatar().getFirstPassenger() instanceof LivingEntity living ? living.getViewYRot(actor.getAnimationData().getPartialTick()) : 0);
        MolangLoader.setDoubleQuery(binding, VERTICAL_SPEED, actor -> ((AvatarAnimationController) actor).getAvatar().getDeltaMovement().y);
        MolangLoader.setDoubleQuery(binding, YAW_SPEED, actor -> ((AvatarAnimationController) actor).getAvatar().getYRot() - ((AvatarAnimationController) actor).getAvatar().yRotO);
    }

    private static void setDefaultLivingEntityQueryValues(QueryBinding<AnimationController> binding) {
        MolangLoader.setBoolQuery(binding, BLOCKING, actor -> ((AvatarAnimationController) actor).getAvatar().isBlocking());
        MolangLoader.setDoubleQuery(binding, DEATH_TICKS, actor -> ((AvatarAnimationController) actor).getAvatar().deathTime == 0 ? 0 : ((AvatarAnimationController) actor).getAvatar().deathTime + actor.getAnimationData().getPartialTick());
        MolangLoader.setDoubleQuery(binding, GROUND_SPEED, actor -> ((AvatarAnimationController) actor).getAvatar().getDeltaMovement().horizontalDistance());
        MolangLoader.setBoolQuery(binding, HAS_HEAD_GEAR, actor -> !((AvatarAnimationController) actor).getAvatar().getItemBySlot(EquipmentSlot.HEAD).isEmpty());
        MolangLoader.setDoubleQuery(binding, HEAD_X_ROTATION, actor -> ((AvatarAnimationController) actor).getAvatar().getViewXRot(actor.getAnimationData().getPartialTick()));
        MolangLoader.setDoubleQuery(binding, HEAD_Y_ROTATION, actor -> ((AvatarAnimationController) actor).getAvatar().getViewYRot(actor.getAnimationData().getPartialTick()));
        MolangLoader.setDoubleQuery(binding, HEALTH, actor -> ((AvatarAnimationController) actor).getAvatar().getHealth());
        MolangLoader.setDoubleQuery(binding, HURT_TIME, actor -> ((AvatarAnimationController) actor).getAvatar().hurtTime == 0 ? 0 : ((AvatarAnimationController) actor).getAvatar().hurtTime - actor.getAnimationData().getPartialTick());
        MolangLoader.setDoubleQuery(binding, INVULNERABLE_TICKS, actor -> ((AvatarAnimationController) actor).getAvatar().invulnerableTime == 0 ? 0 : ((AvatarAnimationController) actor).getAvatar().invulnerableTime - actor.getAnimationData().getPartialTick());
        MolangLoader.setBoolQuery(binding, IS_BABY, actor -> ((AvatarAnimationController) actor).getAvatar().isBaby());
        MolangLoader.setBoolQuery(binding, IS_SLEEPING, actor -> ((AvatarAnimationController) actor).getAvatar().isSleeping());
        MolangLoader.setBoolQuery(binding, IS_USING_ITEM, actor -> ((AvatarAnimationController) actor).getAvatar().isUsingItem());
        MolangLoader.setBoolQuery(binding, IS_WALL_CLIMBING, actor -> ((AvatarAnimationController) actor).getAvatar().onClimbable());
        MolangLoader.setDoubleQuery(binding, LIMB_SWING, actor -> ((AvatarAnimationController) actor).getAvatar().walkAnimation.position());
        MolangLoader.setDoubleQuery(binding, LIMB_SWING_AMOUNT, actor -> ((AvatarAnimationController) actor).getAvatar().walkAnimation.speed(actor.getAnimationData().getPartialTick()));
        MolangLoader.setDoubleQuery(binding, MAIN_HAND_ITEM_MAX_DURATION, actor -> ((AvatarAnimationController) actor).getAvatar().getMainHandItem().getUseDuration(((AvatarAnimationController) actor).getAvatar()));
        MolangLoader.setDoubleQuery(binding, MAIN_HAND_ITEM_USE_DURATION, actor -> ((AvatarAnimationController) actor).getAvatar().getUsedItemHand() == InteractionHand.MAIN_HAND ? ((AvatarAnimationController) actor).getAvatar().getTicksUsingItem() / 20d + actor.getAnimationData().getPartialTick() : 0);
        MolangLoader.setDoubleQuery(binding, MAX_HEALTH, actor -> ((AvatarAnimationController) actor).getAvatar().getMaxHealth());
        MolangLoader.setDoubleQuery(binding, SCALE, actor -> ((AvatarAnimationController) actor).getAvatar().getScale());
        MolangLoader.setDoubleQuery(binding, SLEEP_ROTATION, actor -> Optional.ofNullable(((AvatarAnimationController) actor).getAvatar().getBedOrientation()).map(Direction::toYRot).orElse(0f));
    }
}
