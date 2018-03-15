package com.github.cive.core.shogi;

import com.github.cive.core.shogi.Pieces.PieceBase;
import com.github.cive.core.shogi.Players.AheadPlayer;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * Created by rrqkd_000 on 2016/06/04.
 */
public class ShogiBoardController {
    final static int VIEW_MODE = 0;
    final static int BATTLE_MODE = 1;
    private PieceBase selected_piece_Base_in_hand = new EmptyPieceBase(new Point(-1, -1));
    private Point selected_point = new Point(-1,-1);
    private static final Point OFFSET = new Point(151 + 26, 0 + 26);
    private static final Point AHEAD_OFFSET = new Point(650,300);
    private static final Point BEHIND_OFFSET = new Point(0, 0);
    GameBoard gameBoard;
    public Integer mode(){
        if (gameBoard.isConclusion()) {
            return VIEW_MODE;
        } else {
            return BATTLE_MODE;
        }
    }
    public PieceBase getSelected_piece_Base_in_hand() throws CloneNotSupportedException{
        return selected_piece_Base_in_hand.clone();
    }
    public Point getSelected_point() {
        return selected_point;
    }
    public ShogiBoardController() {

    }
    public void setGameBoard(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
    }

    public void placeFromTweet(String tweet)
    {
        List<String> strs = Arrays.asList(tweet.split("\\s␣"));
        for (String str : strs)
        {
            if (gameBoard.MoveByString(str))
            {
                break;
            }
        }
    }

    public void selectPieceAt(Point clicked) {
        // 盤上の選択
        boolean isClickedOnBoard = clicked.x >= OFFSET.x && clicked.x <= (OFFSET.x+50*9) && clicked.y >= OFFSET.y && clicked.y <= (OFFSET.y+50*9);
        // 盤上の位置
        Point clicked_position_on_board = new Point((int) ((clicked.x - OFFSET.x) / 50), (int) ((clicked.y - OFFSET.y) / 50));

        // 盤上の駒を選択中
        boolean isSelectingOnBoard = selected_point.x != -1;
        // 持ち駒を選択中
        boolean isSelectingInHand;
        if (selected_piece_Base_in_hand != null) {
            isSelectingInHand = selected_piece_Base_in_hand.getTypeOfPiece() != PieceBase.NONE;
        } else {
            isSelectingInHand = false;
        }
        // 将棋盤上は選択されていて、攻撃側の駒を選択しているならば
        if (isClickedOnBoard && !gameBoard.getAttacker().getPieceOnBoardAt(clicked_position_on_board).isEmpty()) {
            selected_point = clicked_position_on_board;
        }
        // 将棋盤上をクリックしていて持ち駒を選択しているならば
        if (isClickedOnBoard && isSelectingInHand) {
            // 持ち駒を置く．
            gameBoard.placePieceInHand(selected_piece_Base_in_hand, clicked_position_on_board);
            // 選択解除
            selected_piece_Base_in_hand = unselectPiece();
        }
        // 将棋盤上をクリックしていて、将棋盤場の駒を選択していてかつ
        // 駒移動が可能ならば
        if (isClickedOnBoard && isSelectingOnBoard && gameBoard.canPlaceInside(selected_point, clicked_position_on_board)) {
            // なり駒するかの判定
            if(gameBoard.getAttacker().getPieceOnBoardAt(selected_point).canPromote(clicked_position_on_board, gameBoard.isAheadsTurn())) {
                System.out.println("can promote");
                Object[] options = {"はい", "いいえ", "キャンセル"};
                int reply = JOptionPane.showOptionDialog(null, "成りますか？", "成駒", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
                if(reply == JOptionPane.YES_OPTION) {
                    gameBoard.replacePieceWithPromote(selected_point, clicked_position_on_board);
                    selected_point = unselectPoint();
                } else if(reply == JOptionPane.NO_OPTION) {
                    gameBoard.replacePiece(selected_point, clicked_position_on_board);
                    selected_point = unselectPoint();
                } else {
                    selected_point = unselectPoint();
                }
            } else {
                gameBoard.replacePiece(selected_point, clicked_position_on_board);
                selected_point = unselectPoint();
            }
        }
        if (!isClickedOnBoard) {
            // 選択解除
            selected_piece_Base_in_hand = unselectPiece();
            selected_point = unselectPoint();
            int type_of_selected_in_hand;
            if (gameBoard.getAttacker() instanceof AheadPlayer)
                type_of_selected_in_hand = getTypeOfPieceInHand(clicked, AHEAD_OFFSET);
            else
                type_of_selected_in_hand = getTypeOfPieceInHand(clicked, BEHIND_OFFSET);
            for (PieceBase pieceBase : gameBoard.getAttacker().getPiecesInHand()) {
                if(pieceBase.getTypeOfPiece() == type_of_selected_in_hand) {
                    selected_piece_Base_in_hand = pieceBase;
                    break;
                }
            }
        }
    }
    @NotNull
    @Contract(pure = true)
    private Integer getTypeOfPieceInHand(Point clicked, Point offset) {
        boolean judge;
        judge = clicked.x >= offset.x && clicked.x <= (offset.x+150) &&
                clicked.y >= offset.y && clicked.y <= (offset.y+50);
        if(judge)return PieceBase.FU;
        for(int type = 2; type < 8; type+=2) {
            judge = clicked.x >= offset.x    && clicked.x <= (offset.x+75) &&
                    clicked.y >= offset.y+type*25 && clicked.y <= (offset.y+50+type*25);
            if(judge)return type;
            judge=clicked.x >= (offset.x+75)    && clicked.x <= (offset.x+150) &&
                    clicked.y >= offset.y+type*25 && clicked.y <= (offset.y+50+type*25);
            if(judge)return type+1;
        }
        return PieceBase.NONE;
    }
    @Contract(" -> !null")
    private Point unselectPoint() {
        return new Point(-1, -1);
    }
    @Contract(" -> !null")
    private PieceBase unselectPiece() {
        return new EmptyPieceBase();
    }
}
