package com.zigythebird.playeranimcore.util;

import com.zigythebird.playeranimcore.animation.AnimationController;
import com.zigythebird.playeranimcore.animation.layered.modifier.AbstractModifier;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ModifierList extends AbstractList<AbstractModifier> {
    private final List<AbstractModifier> modifiers = new ArrayList<>();
    private final AnimationController.InternalAnimationAccessor internalAnimationAccessor;
    
    public ModifierList(AnimationController controller) {
        this.internalAnimationAccessor = new AnimationController.InternalAnimationAccessor(controller);
    }

    @Override
    public AbstractModifier get(int index) {
        return this.modifiers.get(index);
    }

    @Override
    public int size() {
        return this.modifiers.size();
    }

    @Override
    public AbstractModifier set(int index, AbstractModifier element) {
        AbstractModifier modifier = this.modifiers.set(index, element);
        linkModifiers();
        return modifier;
    }

    @Override
    public void add(int index, AbstractModifier element) {
        this.modifiers.add(index, element);
        linkModifiers();
    }

    @Override
    public AbstractModifier remove(int index) {
        AbstractModifier modifier = this.modifiers.remove(index);
        linkModifiers();
        return modifier;
    }

    protected void linkModifiers() {
        Iterator<AbstractModifier> modifierIterator = modifiers.iterator();
        if (modifierIterator.hasNext()) {
            AbstractModifier tmp = modifierIterator.next();
            while (modifierIterator.hasNext()) {
                AbstractModifier tmp2 = modifierIterator.next();
                tmp.setAnim(tmp2);
                tmp = tmp2;
            }
            tmp.setAnim(internalAnimationAccessor);
        }
    }
}
