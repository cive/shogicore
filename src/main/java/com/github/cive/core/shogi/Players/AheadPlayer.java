package com.github.cive.core.shogi.Players;

import com.github.cive.core.shogi.Pieces.PieceBase;
import com.github.cive.core.shogi.Pieces.PieceFactory;

import java.awt.*;

/**
 * Created by yotuba on 16/03/12.
 * ahead player (先手)
 */
public class AheadPlayer extends PlayerBase {
    // for test
    public AheadPlayer(){}
    public AheadPlayer(GameRule rule) {
        setInitial(rule);
    }
    @Override
    protected void setFu() {
        for (int x = 1; x < 10; x++)
            this.addPiecesOnBoard(PieceFactory.create(PieceBase.FU, new Point(x, 7)).get());
    }
    @Override
    protected void setKaku() {
        this.addPiecesOnBoard(PieceFactory.create(PieceBase.KAKU, new Point(8,8)).get());
    }
    @Override
    protected void setHisha() {
        this.addPiecesOnBoard(PieceFactory.create(PieceBase.HISHA, new Point(2, 8)).get());
    }
    @Override
    protected void setKin() {
        this.addPiecesOnBoard(PieceFactory.create(PieceBase.KIN, new Point(4, 9)).get());
        this.addPiecesOnBoard(PieceFactory.create(PieceBase.KIN, new Point(6, 9)).get());
    }
    @Override
    protected void setGin() {
        this.addPiecesOnBoard(PieceFactory.create(PieceBase.GIN, new Point(3, 9)).get());
        this.addPiecesOnBoard(PieceFactory.create(PieceBase.GIN, new Point(7, 9)).get());
    }
    @Override
    protected void setKeima() {
        this.addPiecesOnBoard(PieceFactory.create(PieceBase.KEIMA, new Point(2, 9)).get());
        this.addPiecesOnBoard(PieceFactory.create(PieceBase.KEIMA, new Point(8, 9)).get());
    }
    @Override
    protected void setKyosha() {
        this.addPiecesOnBoard(PieceFactory.create(PieceBase.KYOSHA, new Point(1, 9)).get());
        this.addPiecesOnBoard(PieceFactory.create(PieceBase.KYOSHA, new Point(9, 9)).get());
    }
    @Override
    protected void setGyoku() {
        this.addPiecesOnBoard(PieceFactory.create(PieceBase.GYOKU, new Point(5, 9)).get());
    }
}
