package com.github.cive.core.shogi.Pieces;

import com.github.cive.core.shogi.Players.PlayerBase;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class Keima extends PieceBase {
    public Keima(Point p) {
        super(p);
    }

    @Override
    public String getName() {
        return "桂";
    }
    @Override
    public String getName(Boolean in_English) {
        if (in_English) {
            return "KE";
        } else {
            return getName();
        }
    }

    @Override
    public Set<Point> getRuleOfPiece(int player_type) {
        Set<Point> set = new HashSet<>();
        if(player_type == PlayerBase.AHEAD) {
            set.add(new Point(-1, -2));
            set.add(new Point(1, -2));
        } else {
            set.add(new Point(-1, 2));
            set.add(new Point(1, 2));
        }
        return set;
    }
    @Override
    public Integer getTypeOfPiece() {
        return PieceBase.KEIMA;
    }
}
