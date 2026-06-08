package com.ankanalytic.snake;

public final class NativeGameLib {
    static {
        System.loadLibrary("snake-native");
    }

    private NativeGameLib() {
    }

    public static native int[] nextHead(int x, int y, int directionOrdinal);

    public static native boolean containsPoint(int[] flattenedPoints, int x, int y);
}
