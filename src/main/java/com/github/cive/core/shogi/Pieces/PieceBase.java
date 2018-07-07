package com.github.cive.core.shogi.Pieces;

import com.github.cive.core.shogi.Players.AheadPlayer;
import com.github.cive.core.shogi.GameBoard;
import com.github.cive.core.shogi.Players.PlayerBase;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Optional;

public abstract class PieceBase implements Constant, Cloneable{
    private Point position;
    public PieceBase(Point position) {
        // set_potision
        this.position = position;
    }
    public String getName() {
        return "　";
    }
    public String getName(Boolean in_English) {
        if (in_English) {
            return "  ";
        } else {
            return getName();
        }
    }

    public static int getTypeFromString(String str) throws IllegalArgumentException
    {
        if (str.length() != 2)
        {
            throw new IllegalArgumentException();
        }
        switch (str)
        {
            case "FU":
                return PieceBase.FU;
            case "KY":
                return PieceBase.KYOSHA;
            case "KE":
                return PieceBase.KEIMA;
            case "GI":
                return PieceBase.GIN;
            case "KI":
                return PieceBase.KIN;
            case "KA":
                return PieceBase.KAKU;
            case "HI":
                return PieceBase.HISHA;
            case "OU":
                return PieceBase.GYOKU;
            case "UM":
                return PieceBase.UMA;
            case "RY":
                return PieceBase.RYU;
            case "TO":
                return PieceBase.TOKIN;
            case "NY":
                return PieceBase.NARIKYO;
            case "NK":
                return PieceBase.NARIKEI;
            case "NG":
                return PieceBase.NARIGIN;
            default:
                return PieceBase.NONE;
        }
    }

    /**
     *
     * @param point_for_gui GUI で表示するための位置
     */
    public void setPoint(Point point_for_gui) {
        this.position.x = 9 - point_for_gui.x;
        this.position.y = point_for_gui.y + 1;
    }

    /**
     *
     * @return GUI で表示するための位置
     */
    public Point getPoint() {
        return new Point(9 - position.x, position.y - 1);
    }
    /**
     * @return 盤台上の位置
     */
    public Point getPosition() {
        return position;
    }
    /**
     * @param position 盤台上の位置
     * */
    public void setPosition(Point position)
    {
        this.position = position;
    }

    @Override
    public PieceBase clone() throws CloneNotSupportedException {
        PieceBase c = (PieceBase)super.clone();
        c.position = new Point(position);
        return c;
    }

    /**
     * この駒が移動できるポイントを返す
     * @param attacker 攻撃側のプレイヤー
     * @param defender 防御側のプレイヤー
     * @return おける位置の集合
     */
    public Set<Point> getCapablePutPosition(PlayerBase attacker, PlayerBase defender) {
        PlayerBase.PlayerType player_type = attacker instanceof AheadPlayer ? PlayerBase.PlayerType.Ahead : PlayerBase.PlayerType.Behind;
        Set<Point> set = new HashSet<>();
        for (Point rule_of_point : this.getRuleOfPiece(player_type)) {
            Point target_position = new Point(this.getPosition().x + rule_of_point.x, this.getPosition().y + rule_of_point.y);
            if (!attacker.getPieceTypeOnBoardAt(target_position).isPresent() && GameBoard.isInGrid(target_position)) set.add(target_position);
        }
        return set;
    }

    /**
     * 成りができる場合に真を返す関数
     * @param dst_position 移動先の位置
     * @param isAheadsTurn 手前側のターンの際に真の値
     * @return TRUE ならば、成りが可能
     */
    public Boolean canPromote(Point dst_position, boolean isAheadsTurn) {
        //this : src
        boolean aPromote = (1 <= dst_position.y && dst_position.y <= 3) || (1 <= this.getPosition().y && this.getPosition().y <= 3);
        boolean bPromote = (7 <= dst_position.y && dst_position.y <= 9) || (7 <= this.getPosition().y && this.getPosition().y <= 9);
        int type = this.getTypeOfPiece();
        boolean canPromoteForType = (type >= PieceBase.FU && type <= PieceBase.GIN) || type == PieceBase.KAKU || type == PieceBase.HISHA;
        if(isAheadsTurn && aPromote && canPromoteForType) {
            return true;
        } else if (!isAheadsTurn && bPromote && canPromoteForType) {
            return true;
        }
        return false;
    }

    /**
     * 成り駒があれば、成り駒を取得する関数
     * @return 成り駒を取得
     */
    public final Optional<PieceBase> getPromotePiece() {
        return PieceFactory.createPromoted(getTypeOfPiece(), getPosition());
    }

    /**
     * 駒台に乗せる際には、元に戻す必要があるため
     * @return 成り駒の元の駒
     */
    public final Optional<PieceBase> getDemotePiece() {
        return PieceFactory.createDemoted(getTypeOfPiece(), getPosition());
    }

    /**
     * 桂馬や香車、歩兵などは、次のターンに動かせない場合ルール違反である
     * @param attacker 攻撃側のプレイヤー
     * @return TRUE ならば、次のターンに動かせる
     */
    public Boolean canMoveLater(PlayerBase attacker) {
        PlayerBase.PlayerType player_type = attacker instanceof AheadPlayer ? PlayerBase.PlayerType.Ahead : PlayerBase.PlayerType.Behind;
        for (Point rule_of_point : this.getRuleOfPiece(player_type)) {
            Point target_position = new Point(this.getPosition().x + rule_of_point.x, this.getPosition().y + rule_of_point.y);
            if (GameBoard.isInGrid(target_position)) return true;
        }
        return false;
    }

    /**
     *
     * @return 盤上にある場合 TRUE を返す
     */
    public Boolean isOnBoard() {
        return 1 <= position.x && position.x <= 9;
    }
    @Override
    public String toString() {
        return getName() + getPosition().toString();
    }

    public abstract Set<Point> getRuleOfPiece(PlayerBase.PlayerType player_type);
    public abstract Integer getTypeOfPiece();
    public abstract Integer getBacksideType();
}
