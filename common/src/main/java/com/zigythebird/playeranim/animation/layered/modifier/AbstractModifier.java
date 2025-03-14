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

import com.zigythebird.playeranim.animation.*;
import com.zigythebird.playeranim.animation.layered.AnimationContainer;
import com.zigythebird.playeranim.animation.layered.IAnimation;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("rawtypes")
public abstract class AbstractModifier extends AnimationContainer<IAnimation> {
    @Setter
    protected IAnimation host;

    /**
     * Could return null if someone makes their own custom way of playing animations.
     */
    @Nullable
    protected AnimationController getController() {
        return host instanceof AnimationController ? (AnimationController) host : null;
    }

    public AbstractModifier() {}

    /**
     * Return true if the modifier should be removed.
     */
    public boolean canRemove() {
        return false;
    }
}
