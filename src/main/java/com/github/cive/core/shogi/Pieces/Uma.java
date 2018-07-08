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
    public Set<Point> getRuleOfPiece(PlayerBase.PlayerType player_type) {
        // dammy
        Set<Point> set = new HashSet<>();
        PieceFactory factory = new PieceFactory();
        Optional<PieceBase> kaku = factory.create(PieceBase.KAKU, this.getPosition());
        Optional<PieceBase> gyoku = factory.create(PieceBase.GYOKU, this.getPosition());
        set.addAll(kaku.get().getRuleOfPiece(player_type));
        set.addAll(gyoku.get().getRuleOfPiece(player_type));
        return set;
    }
    @Override
    public Integer getTypeOfPiece() {
        return PieceBase.UMA;
    }
    @Override
    public Set<Point> getCapablePutPosition(PlayerBase attacker, PlayerBase defender) {
        Set<Point> set = new HashSet<>();
        set.addAll(getSetToNeedToAdd(attacker, defender, true, 1, this.getPosition()));
        set.addAll(getSetToNeedToAdd(attacker, defender, false, 1, this.getPosition()));
        set.addAll(getSetToNeedToAdd(attacker, defender, true, -1, this.getPosition()));
        set.addAll(getSetToNeedToAdd(attacker, defender, false, -1, this.getPosition()));
        set.addAll((new Gyoku(this.getPosition()).getCapablePutPosition(attacker, defender)));
        return set;
    }

    @Override
    public Integer getBacksideType() {
        return PieceBase.KAKU;
    }
}
