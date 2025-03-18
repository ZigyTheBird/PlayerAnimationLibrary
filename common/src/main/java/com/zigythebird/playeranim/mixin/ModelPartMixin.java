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

package com.zigythebird.playeranim.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zigythebird.playeranim.accessors.IModelPart;
import com.zigythebird.playeranim.accessors.IUpperPartHelper;
import com.zigythebird.playeranim.cache.PlayerAnimBone;
import com.zigythebird.playeranim.util.RenderUtil;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ModelPart.class)
public class ModelPartMixin implements IUpperPartHelper, IModelPart {
    @Unique
    private boolean playerAnimLib$isUpper = false;

    @Unique
    private PlayerAnimBone playerAnimLib$parent = null;

    @Override
    public boolean playerAnimLib$isUpperPart() {
        return playerAnimLib$isUpper;
    }

    @Override
    public void playerAnimLib$setUpperPart(boolean bl) {
        playerAnimLib$isUpper = bl;
    }

    @Override
    public void playerAnimLib$setParent(PlayerAnimBone parent) {
        this.playerAnimLib$parent = parent;
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;translateAndRotate(Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private void translateToParent(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, int k, CallbackInfo ci) {
        if (playerAnimLib$parent != null) {
            List<PlayerAnimBone> parents = new ArrayList<>();
            PlayerAnimBone currentParent = playerAnimLib$parent;
            parents.add(playerAnimLib$parent);
            while (currentParent.getParent() != null && currentParent.getParent() != currentParent) {
                currentParent = currentParent.getParent();
                parents.addFirst(currentParent.getParent());
            }

            for (PlayerAnimBone bone : parents) {
                RenderUtil.prepMatrixForBone(poseStack, bone);
            }
        }
    }
}
