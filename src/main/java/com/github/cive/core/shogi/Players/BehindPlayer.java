package com.github.cive.core.shogi.Players;

import java.awt.*;
import com.github.cive.core.shogi.Pieces.PieceBase;
import com.github.cive.core.shogi.Pieces.PieceFactory;

/**
 * Created by yotuba on 16/03/12.
 * behind player (後手)
 */
public class BehindPlayer extends PlayerBase {
    public BehindPlayer() {};
    public BehindPlayer(GameRule rule) {
        setInitial(rule);
    }
    @Override
    protected void setFu() {
        for (int x = 1; x < 10; x++)
            this.addPiecesOnBoard(PieceFactory.create(PieceBase.FU, new Point(x, 3)).get());
    }
    @Override
    protected void setKaku() {
        this.addPiecesOnBoard(PieceFactory.create(PieceBase.KAKU, new Point(2, 2)).get());
    }
    @Override
    protected void setHisha() {
        this.addPiecesOnBoard(PieceFactory.create(PieceBase.HISHA, new Point(8, 2)).get());
    }
    @Override
    protected void setKin() {
        this.addPiecesOnBoard(PieceFactory.create(PieceBase.KIN, new Point(4, 1)).get());
        this.addPiecesOnBoard(PieceFactory.create(PieceBase.KIN, new Point(6, 1)).get());
    }
    @Override
    protected void setGin() {
        this.addPiecesOnBoard(PieceFactory.create(PieceBase.GIN, new Point(3, 1)).get());
        this.addPiecesOnBoard(PieceFactory.create(PieceBase.GIN, new Point(7, 1)).get());
    }
    @Override
    protected void setKeima() {
        this.addPiecesOnBoard(PieceFactory.create(PieceBase.KEIMA, new Point(2, 1)).get());
        this.addPiecesOnBoard(PieceFactory.create(PieceBase.KEIMA, new Point(8, 1)).get());
    }
    @Override
    protected void setKyosha() {
        this.addPiecesOnBoard(PieceFactory.create(PieceBase.KYOSHA, new Point(1, 1)).get());
        this.addPiecesOnBoard(PieceFactory.create(PieceBase.KYOSHA, new Point(9, 1)).get());
    }
    @Override
    protected void setGyoku() {
        this.addPiecesOnBoard(PieceFactory.create(PieceBase.GYOKU, new Point(5, 1)).get());
    }
}
