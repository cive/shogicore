package com.github.cive.core.shogi.Pieces;

import com.github.cive.core.shogi.Players.PlayerBase;

import java.awt.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class Uma extends Kaku{
    public Uma(Point p) {
        super(p);
    }

    @Override
    public String getName() {
        return "馬";
    }
    @Override
    public String getName(Boolean in_English) {
        if (in_English) {
            return "UM";
        } else {
            return getName();
        }
    }

    @Override
    public Set<Point> getRuleOfPiece(int player_type) {
        // dammy
        Set<Point> set = new HashSet<>();
        PieceFactory factory = new PieceFactory();
        Optional<PieceBase> kaku = factory.create(PieceBase.KAKU, this.getPoint());
        Optional<PieceBase> gyoku = factory.create(PieceBase.GYOKU, this.getPoint());
        set.addAll(kaku.get().getRuleOfPiece(player_type));
        set.addAll(gyoku.get().getRuleOfPiece(player_type));
        return set;
    }
    @Override
    public Integer getTypeOfPiece() {
        return PieceBase.UMA;
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
