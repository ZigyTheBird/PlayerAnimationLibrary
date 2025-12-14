package com.zigythebird.playeranimcore.api.firstPerson;

public enum FirstPersonMode {

    /**
     * The animation does not decide first-person mode, this way, the animation will be transparent in first-person mode.
     */
    NONE(false),
    /**
     * Use the vanilla renderer, most of the time broken, if you use this, please check your animation
     */
    VANILLA(true),

    /**
     * Use the 3rd person player model (only arms/items/shoulder armor) to render accurate first-person perspective.
     * Note that armor rendering is disabled in the default FirstPersonConfiguration. {@link FirstPersonConfiguration#showArmor}
     */
    THIRD_PERSON_MODEL(true),

    /**
     * First person animation is DISABLED, vanilla idle will be active.
     */
    DISABLED(false);


    public boolean isEnabled() {
        return enabled;
    }

    private final boolean enabled;


    FirstPersonMode(boolean enabled) {
        this.enabled = enabled;
    }
}
