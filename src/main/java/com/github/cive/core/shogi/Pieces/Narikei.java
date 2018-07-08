package com.github.cive.core.shogi.Pieces;

import com.github.cive.core.shogi.Players.PlayerBase;

import java.awt.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Created by yotuba on 16/05/11.
 * Promoted Keima
 */
public class Narikei extends PieceBase {
    public Narikei(Point p) {
        super(p);
    }

    @Override
    public String getName() {
        return "圭";
    }
    @Override
    public String getName(Boolean in_English) {
        if (in_English) {
            return "NK";
        } else {
            return getName();
        }
    }

    @Override
    public Set<Point> getRuleOfPiece(PlayerBase.PlayerType player_type) {
        Set<Point> set = new HashSet<>();
        PieceFactory factory = new PieceFactory();
        Optional<PieceBase> kin = factory.create(PieceBase.KIN, this.getPosition());
        set.addAll(kin.get().getRuleOfPiece(player_type));
        return set;
    }
    @Override
    public Integer getTypeOfPiece() {
        return PieceBase.NARIKEI;
    }

    @Override
    public Integer getBacksideType() {
        return PieceBase.KEIMA;
    }
}
