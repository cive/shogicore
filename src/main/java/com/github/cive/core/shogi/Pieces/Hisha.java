package com.github.cive.core.shogi.Pieces;

import com.github.cive.core.shogi.GameBoard;
import com.github.cive.core.shogi.Players.PlayerBase;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class Hisha extends PieceBase {
    public Hisha(Point p) {
        super(p);
    }

    @Override
    public String getName() {
        return "飛";
    }
    @Override
    public String getName(Boolean in_English) {
        if (in_English) {
            return "HI";
        } else {
            return getName();
        }
    }

    @Override
    public Set<Point> getRuleOfPiece(PlayerBase.PlayerType player_type) {
        //dammy
        Set<Point> set = new HashSet<>();
        for(int i = -8; i < 9; i++) {
            if(i == 0) continue;
            set.add(new Point(i, 0));
            set.add(new Point(0, i));
        }

        return set;
    }
    @Override
    public Set<Point> getCapablePutPosition(PlayerBase attacker, PlayerBase defender){
        Set<Point> set = new HashSet<>();
        set.addAll(getSetToNeedToAdd(attacker, defender, true, 1, this.getPosition()));
        set.addAll(getSetToNeedToAdd(attacker, defender, false, 1, this.getPosition()));
        set.addAll(getSetToNeedToAdd(attacker, defender, true, -1, this.getPosition()));
        set.addAll(getSetToNeedToAdd(attacker, defender, false, -1, this.getPosition()));
        return set;
    }
    public Set<Point> getSetToNeedToAdd(PlayerBase attacker, PlayerBase defender, boolean axis, int ini, Point selected_position) {
        Set<Point> set_for_add = new HashSet<>();
        for(int i = ini; Math.abs(i) < 9; i += ini) {
            Point target_position = new Point(selected_position.x+(axis?i:0), selected_position.y+(axis?0:i));
            if(GameBoard.isInGrid(target_position)) {
                if (attacker.getPieceTypeOnBoardAt(target_position).isPresent()) {
                    break;
                }
                set_for_add.add(target_position);
                if (defender.getPieceTypeOnBoardAt(target_position).isPresent()) {
                    break;
                }
            }
        }
        return set_for_add;
    }

    @Override
    public Integer getTypeOfPiece() {
        return PieceBase.HISHA;
    }
    @Override
    public Integer getBacksideType()
    {
        return PieceBase.RYU;
    }
}
