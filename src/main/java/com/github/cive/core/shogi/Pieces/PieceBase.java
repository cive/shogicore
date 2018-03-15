package com.github.cive.core.shogi.Pieces;

import com.github.cive.core.shogi.Players.AheadPlayer;
import com.github.cive.core.shogi.GameBoard;
import com.github.cive.core.shogi.Players.PlayerBase;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Optional;

public abstract class PieceBase implements Constant, Cloneable{
    private Point point_for_gui;
    public PieceBase(Point position) {
        this.setPosition(position);
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

    /**
     *
     * @param p GUI で表示するための位置
     */
    public void setPoint(Point p) {
        this.point_for_gui = p;
    }

    /**
     *
     * @return GUI で表示するための位置
     */
    public Point getPoint() {
        return point_for_gui;
    }
    /**
     * @return 盤台上の位置
     */
    public Point getPosition() {
        return new Point(9-point_for_gui.x, point_for_gui.y+1);
    }
    /**
     * @param position 盤台上の位置
     * */
    public void setPosition(Point position)
    {
        this.point_for_gui.x = position.x - 9;
        this.point_for_gui.y = position.y + 1;
    }

    @Override
    public PieceBase clone() throws CloneNotSupportedException {
        PieceBase c = (PieceBase)super.clone();
        c.point_for_gui = new Point(point_for_gui);
        return c;
    }
    public abstract Set<Point> getRuleOfPiece(int player_type);

    /**
     * この駒が移動できるポイントを返す
     * @param attacker 攻撃側のプレイヤー
     * @param defender 防御側のプレイヤー
     * @return おける位置の集合
     */
    public Set<Point> getCapablePutPoint(PlayerBase attacker, PlayerBase defender) {
        int player_type = attacker instanceof AheadPlayer ? PlayerBase.AHEAD : PlayerBase.BEHIND;
        Set<Point> set = new HashSet<>();
        for (Point rule_of_point : this.getRuleOfPiece(player_type)) {
            Point target = new Point(this.getPoint().x + rule_of_point.x, this.getPoint().y + rule_of_point.y);
            if (!attacker.getPieceTypeOnBoardAt(target).isPresent() && GameBoard.isInGrid(target)) set.add(target);
        }
        return set;
    }
    public abstract Integer getTypeOfPiece();

    /**
     * 成りができる場合に真を返す関数
     * @param dst 移動先の位置
     * @param isAheadsTurn 手前側のターンの際に真の値
     * @return TRUE ならば、成りが可能
     */
    public Boolean canPromote(Point dst, boolean isAheadsTurn) {
        //this : src
        boolean aPromote = (0 <= dst.y && dst.y <= 2) || (0 <= this.getPoint().y && this.getPoint().y <= 2);
        boolean bPromote = (6 <= dst.y && dst.y <= 8) || (6 <= this.getPoint().y && this.getPoint().y <= 8);
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
        PieceFactory factory = new PieceFactory();
        return factory.createPromoted(getTypeOfPiece(), getPosition());
    }

    /**
     * 駒台に乗せる際には、元に戻す必要があるため
     * @return 成り駒の元の駒
     */
    public final Optional<PieceBase> getDemotePiece() {
        PieceFactory factory = new PieceFactory();
        return factory.createDemoted(getTypeOfPiece(), getPoint());
    }

    /**
     * 桂馬や香車、歩兵などは、次のターンに動かせない場合ルール違反である
     * @param attacker 攻撃側のプレイヤー
     * @return TRUE ならば、次のターンに動かせる
     */
    public Boolean canMoveLater(PlayerBase attacker) {
        int player_type = attacker instanceof AheadPlayer ? PlayerBase.AHEAD : PlayerBase.BEHIND;
        for (Point rule_of_point : this.getRuleOfPiece(player_type)) {
            Point target = new Point(this.getPoint().x + rule_of_point.x, this.getPoint().y + rule_of_point.y);
            if (GameBoard.isInGrid(target)) return true;
        }
        return false;
    }

    /**
     *
     * @return 盤上にある場合 TRUE を返す
     */
    public Boolean isOnBoard() {
        return 0 <= point_for_gui.x && point_for_gui.x <= 8;
    }
    @Override
    public String toString() {
        return getName() + getPoint().toString();
    }
    public Boolean isEmpty() {
        return this.getTypeOfPiece() == PieceBase.NONE;
    }
}
