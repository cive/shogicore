package com.github.cive.core.shogi.Pieces;

import com.github.cive.core.shogi.Players.PlayerBase;

import java.awt.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Created by yotuba on 16/05/12.
 * Promoted Fu
 */
public class Tokin extends PieceBase {
    public Tokin(Point p) {
        super(p);
    }

    @Override
    public String getName() {
        return "と";
    }
    @Override
    public String getName(Boolean in_English) {
        if (in_English) {
            return "TO";
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
        return PieceBase.TOKIN;
    }
    @Override
    public Integer getBacksideType()
    {
        return PieceBase.FU;
    }
}
