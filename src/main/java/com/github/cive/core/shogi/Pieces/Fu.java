package com.github.cive.core.shogi.Pieces;

import com.github.cive.core.shogi.Players.PlayerBase;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class Fu extends PieceBase {
    public Fu(Point p) {
        super(p);
    }
    @Override
    public String getName() {
        return "歩";
    }
    @Override
    public String getName(Boolean in_English) {
        if (in_English) {
            return "FU";
        } else {
            return getName();
        }
    }

    @Override
    public Set<Point> getRuleOfPiece(PlayerBase.PlayerType player_type) {
        Set<Point> set = new HashSet<>();
        if(player_type == PlayerBase.PlayerType.Ahead) {
            set.add(new Point(0, -1));
        } else {
            set.add(new Point(0, 1));
        }
        return set;
    }
    @Override
    public Integer getTypeOfPiece() {
        return PieceBase.FU;
    }
    @Override
    public Integer getBacksideType()
    {
        return PieceBase.TOKIN;
    }
}
