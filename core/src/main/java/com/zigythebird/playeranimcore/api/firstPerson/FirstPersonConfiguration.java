package com.zigythebird.playeranimcore.api.firstPerson;

public class FirstPersonConfiguration {
    boolean showRightArm = false;
    boolean showLeftArm = false;

    boolean showRightItem = true;
    boolean showLeftItem = true;
    boolean showArmor = false;

    public FirstPersonConfiguration() {}

    public FirstPersonConfiguration(boolean showRightArm, boolean showLeftArm, boolean showRightItem, boolean showLeftItem) {
        this(showRightArm, showLeftArm, showRightItem, showLeftItem, false);
    }

    public FirstPersonConfiguration(boolean showRightArm, boolean showLeftArm, boolean showRightItem, boolean showLeftItem, boolean showArmor) {
        this.showRightArm = showRightArm;
        this.showLeftArm = showLeftArm;
        this.showRightItem = showRightItem;
        this.showLeftItem = showLeftItem;
        this.showArmor = showArmor;
    }

    public boolean isShowArmor() {
        return showArmor;
    }

    public boolean isShowLeftItem() {
        return showLeftItem;
    }

    public boolean isShowRightItem() {
        return showRightItem;
    }

    public boolean isShowLeftArm() {
        return showLeftArm;
    }

    public boolean isShowRightArm() {
        return showRightArm;
    }

    public FirstPersonConfiguration setShowRightItem(boolean showRightItem) {
        this.showRightItem = showRightItem;
        return this;
    }

    public FirstPersonConfiguration setShowArmor(boolean showArmor) {
        this.showArmor = showArmor;
        return this;
    }

    public FirstPersonConfiguration setShowLeftItem(boolean showLeftItem) {
        this.showLeftItem = showLeftItem;
        return this;
    }

    public FirstPersonConfiguration setShowLeftArm(boolean showLeftArm) {
        this.showLeftArm = showLeftArm;
        return this;
    }

    public FirstPersonConfiguration setShowRightArm(boolean showRightArm) {
        this.showRightArm = showRightArm;
        return this;
    }
}
