package com.github.cive.core.shogi.Pieces;

import com.github.cive.core.shogi.Players.PlayerBase;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class Ryu extends Hisha {
    public Ryu(Point p) {
        super(p);
    }

    @Override
    public String getName() {
        return "龍";
    }
    @Override
    public String getName(Boolean in_English) {
        if (in_English) {
            return "RY";
        } else {
            return getName();
        }
    }

    @Override
    public Set<Point> getRuleOfPiece(int player_type) {
        // dammy
        Set<Point> set = new HashSet<>();
        PieceBase hisha = new Hisha(this.getPoint());
        PieceBase gyoku = new Gyoku(this.getPoint());
        set.addAll(hisha.getRuleOfPiece(player_type));
        set.addAll(gyoku.getRuleOfPiece(player_type));

        return set;
    }
    @Override
    public Integer getTypeOfPiece() {
        return PieceBase.RYU;
    }
    @Override
    public Set<Point> getCapablePutPoint(PlayerBase attacker, PlayerBase defender) {
        Set<Point> set = new HashSet<>();
        set.addAll(getSetToNeedToAdd(attacker, defender, true, 1, this.getPoint()));
        set.addAll(getSetToNeedToAdd(attacker, defender, false, 1, this.getPoint()));
        set.addAll(getSetToNeedToAdd(attacker, defender, true, -1, this.getPoint()));
        set.addAll(getSetToNeedToAdd(attacker, defender, false, -1, this.getPoint()));
        set.addAll((new Gyoku(this.getPoint()).getCapablePutPoint(attacker, defender)));
        return set;
    }

}
