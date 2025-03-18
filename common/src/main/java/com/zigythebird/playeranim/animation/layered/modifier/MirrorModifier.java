/*
 * MIT License
 *
 * Copyright (c) 2022 KosmX
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

package com.zigythebird.playeranim.animation.layered.modifier;

import com.zigythebird.playeranim.api.firstPerson.FirstPersonConfiguration;
import com.zigythebird.playeranim.cache.PlayerAnimBone;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MirrorModifier extends AbstractModifier {
    public static final Map<String, String> mirrorMap;

    @Override
    public void get3DTransform(@NotNull PlayerAnimBone bone) {
        String modelName = bone.getName();
        if (mirrorMap.containsKey(modelName)) modelName = mirrorMap.get(modelName);
        transformBone(bone);

        PlayerAnimBone newBone = new PlayerAnimBone(modelName);
        newBone.copyOtherBone(bone);
        super.get3DTransform(newBone);
        transformBone(newBone);
        bone.copyOtherBone(newBone);
    }

    // Override candidate
    @Override
    public @NotNull FirstPersonConfiguration getFirstPersonConfiguration() {
        FirstPersonConfiguration configuration = super.getFirstPersonConfiguration();
        return new FirstPersonConfiguration()
                .setShowLeftArm(configuration.isShowRightArm())
                .setShowRightArm(configuration.isShowLeftArm())
                .setShowLeftItem(configuration.isShowRightItem())
                .setShowRightItem(configuration.isShowLeftItem());
    }

    protected void transformBone(PlayerAnimBone bone) {
        bone.setPosX(-bone.getPosX());
        bone.setRotY(-bone.getRotY());
        bone.setBend(-bone.getBend());
    }

    static {
        HashMap<String, String> partMap = new HashMap<>();
        partMap.put("left_arm", "right_arm");
        partMap.put("left_eg", "right_leg");
        partMap.put("left_item", "right_item");
        partMap.put("right_arm", "left_arm");
        partMap.put("right_leg", "left_leg");
        partMap.put("right_item", "left_item");
        mirrorMap = Collections.unmodifiableMap(partMap);
    }
}
