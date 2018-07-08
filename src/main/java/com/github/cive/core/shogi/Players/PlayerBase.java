package com.github.cive.core.shogi.Players;

import com.github.cive.core.shogi.Pieces.PieceBase;
import com.github.cive.core.shogi.Pieces.PieceFactory;

import java.awt.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by yotuba on 16/03/12.
 * Player class
 * have pieces on board and in hand.
 */
public abstract class PlayerBase implements Cloneable{
    public enum PlayerType
    {
        Ahead,
        Behind
    }
    public enum GameRule
    {
        Normal,
        NoneFu,
        NoneHishaKaku
    }
    public PlayerBase clone() throws CloneNotSupportedException {
        PlayerBase c = (PlayerBase)super.clone();
        c.piecesInHand = null;
        c.piecesInHand = new ArrayList<>();
        for (PieceBase piece : piecesInHand)
        {
            c.piecesInHand.add(PieceFactory.create(piece.getTypeOfPiece(), piece.getPosition()).get());
        }
        c.piecesOnBoard = null;
        c.piecesOnBoard = new ArrayList<>();
        for (PieceBase piece : piecesOnBoard)
        {
            c.piecesOnBoard.add(PieceFactory.create(piece.getTypeOfPiece(), piece.getPosition()).get());
        }
        return c;
    }
    private ArrayList<PieceBase> piecesOnBoard = new ArrayList<>();
    private ArrayList<PieceBase> piecesInHand = new ArrayList<>();
    public ArrayList<PieceBase> getPiecesOnBoard() {
        return piecesOnBoard;
    }
    public ArrayList<PieceBase> getPiecesInHand() {
        return piecesInHand;
    }
    public void addPiecesOnBoard(PieceBase p) {
        this.piecesOnBoard.add(p);
    }
    public void addPiecesInHand(PieceBase p) {
        this.piecesInHand.add(p);
    }
    // n <= 34
    // O(n)
    public Stream<PieceBase> getPiecesOnBoard(int type) {
        return piecesOnBoard.stream().filter(x -> x.getTypeOfPiece() == type);
    }
    // O(n)
    public Optional<PieceBase> getPieceOnBoardAt(Point position) {
        for (PieceBase piece : piecesOnBoard) {
            if (piece.getPosition().equals(position)) return Optional.of(piece);
        }
        return Optional.empty();
    }
    // O(n)
    public Optional<Integer> getPieceTypeOnBoardAt(Point position) {
        for (PieceBase piece : piecesOnBoard) {
            if (piece.getPosition().equals(position)) return Optional.of(piece.getTypeOfPiece());
        }
        return Optional.empty();
    }
    // O(n)
    public Boolean reducePieceInHandThatIs(PieceBase p) {
        for (PieceBase piece : piecesInHand) {
            if (java.util.Objects.equals(piece.getTypeOfPiece(), p.getTypeOfPiece())){
                piecesInHand.remove(piece);
                return true;
            }
        }
        return false;
    }
    // O(n)
    public Boolean reducePieceOnBoardAt(Point position) {
        for (PieceBase piece : piecesOnBoard) {
            if (piece.getPosition().equals(position)){
                piecesOnBoard.remove(piece);
                return true;
            }
        }
        return false;
    }
    // O(n)
    public Boolean matchTypeInHand(PieceBase search) {
        for (PieceBase piece : piecesInHand) {
            if (Objects.equals(piece.getTypeOfPiece(), search.getTypeOfPiece())) return true;
        }
        return false;
    }
    protected void setInitial(GameRule rule) {
        piecesOnBoard.clear();
        piecesInHand.clear();
        switch (rule) {
            case Normal:
                setDefault();
                break;
            case NoneFu:
                setKaku();
                setHisha();
                setGin();
                setKin();
                setKeima();
                setKyosha();
                setGyoku();
                break;
            case NoneHishaKaku:
                setFu();
                setGin();
                setKin();
                setKeima();
                setKyosha();
                setGyoku();
                break;
            default:
                setDefault();
                break;
        }
    }
    protected void setDefault() {
        setFu();
        setKaku();
        setHisha();
        setGin();
        setKin();
        setKeima();
        setKyosha();
        setGyoku();
    }
    public void update(PlayerBase player) {
        piecesInHand.clear();
        piecesOnBoard.clear();
        piecesInHand.addAll(player.getPiecesInHand());
        piecesOnBoard.addAll(player.getPiecesOnBoard());
    }

    protected Integer countAll() {
        Integer size = piecesInHand.size();
        size += piecesOnBoard.size();
        return size;
    }

    abstract protected void setFu();
    abstract protected void setKyosha();
    abstract protected void setKeima();
    abstract protected void setGin();
    abstract protected void setKin();
    abstract protected void setKaku();
    abstract protected void setHisha();
    abstract protected void setGyoku();
}
