package com.github.cive.core.shogi.Pieces;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class Gyoku extends PieceBase {

    public Gyoku(Point p) {
        super(p);
    }

    @Override
    public String getName() {
        return "玉";
    }
    @Override
    public String getName(Boolean in_English) {
        if (in_English) {
            return "OU";
        } else {
            return getName();
        }
    }

    @Override
    public Set<Point> getRuleOfPiece(int player_type) {
        Set<Point> set = new HashSet<>();
        for(int i = -1; i < 2; i++) {
            for(int j = -1; j < 2; j++) {
                if(i == 0 && j == 0) continue;
                set.add(new Point(i, j));
            }
        }
        return set;
    }
    @Override
    public Integer getTypeOfPiece() {
        return PieceBase.GYOKU;
    }
}
