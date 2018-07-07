package com.github.cive.core.shogi;

import java.awt.*;

public class Utils {

    // 場所を表わす文字列を座標値に変換（「5二」を表わす「52」は(4, 1)に変換）
    public static Point LocationStringToPoint(String str) {
        int x = Integer.parseInt(str.substring(0, 1));
        int y = Integer.parseInt(str.substring(1, 2));
        return new Point(9 - x, y - 1);
    }

    public static Point LocationStringToPosition(String str) {
        int x = Integer.parseInt(str.substring(0, 1));
        int y = Integer.parseInt(str.substring(1, 2));
        return new Point(x, y);
    }
}
