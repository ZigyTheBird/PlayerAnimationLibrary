package com.zigythebird.playeranimcore.enums;

public enum Axis {
    X,
    Y,
    Z;

    public static String toString(Axis axis) {
        switch (axis) {
            case X -> {
                return "X";
            }
            case Y -> {
                return "y";
            }
            case Z -> {
                return "Z";
            }
            case null, default -> {
                return "";
            }
        }
    }
}
