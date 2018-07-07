package com.github.cive.core.shogi;
import com.github.cive.core.shogi.Players.PlayerBase;
import org.junit.Test;
import static org.junit.Assert.*;
import java.awt.Point;

import com.github.cive.core.shogi.Pieces.*;
import com.github.cive.core.shogi.Exceptions.PlayerNotDefinedGyokuException;

public class GameBoardTest {
    @Test
    public void CanLaunchGameBoard()
    {
        GameBoard gameBoard = new GameBoard();
        assert gameBoard != null : gameBoard.getBoardSurface();
    }
    @Test
    public void IsOkDefaultGameBoard()
    {
        GameBoard gb = new GameBoard(PlayerBase.GameRule.Normal, PlayerBase.GameRule.Normal);
        String str = "P1-KY-KE-GI-KI-OU-KI-GI-KE-KY\n" +
                "P2 * -HI *  *  *  *  * -KA * \n" +
                "P3-FU-FU-FU-FU-FU-FU-FU-FU-FU\n" +
                "P4 *  *  *  *  *  *  *  *  * \n" +
                "P5 *  *  *  *  *  *  *  *  * \n" +
                "P6 *  *  *  *  *  *  *  *  * \n" +
                "P7+FU+FU+FU+FU+FU+FU+FU+FU+FU\n" +
                "P8 * +KA *  *  *  *  * +HI * \n" +
                "P9+KY+KE+GI+KI+OU+KI+GI+KE+KY";
        assertEquals(gb.getBoardSurface(), str);
    }

    @Test
    public void CanMovePiece() {
        GameBoard gameBoard = new GameBoard(PlayerBase.GameRule.Normal, PlayerBase.GameRule.Normal);
        assertTrue(gameBoard.getAttacker().getPieceOnBoardAt(new Point(7,7)).get().getCapablePutPosition(
                gameBoard.getAttacker(), gameBoard.getDefender()
        ).size() > 0);
        assertTrue(gameBoard.canPlaceInside(new Point(7, 7), new Point(7, 6)));
        assertFalse(gameBoard.canPlaceInside(new Point(7, 7), new Point(6, 7)));
        assertFalse(gameBoard.canPlaceInside(new Point(7, 7), new Point(7, 5)));
        assertFalse(gameBoard.canPlaceInside(new Point(7, 3), new Point(7, 4)));
        gameBoard.replacePiece(new Point(7,7), new Point(7,6));
        assertTrue(gameBoard.canPlaceInside(new Point(7, 3), new Point(7, 4)));
    }

    @Test
    public void IsOutOfRange() {
        GameBoard gameBoard = new GameBoard(PlayerBase.GameRule.Normal, PlayerBase.GameRule.Normal);
        assertFalse(gameBoard.getPieceOf(new Point(-1, 0)).isPresent());
        assertFalse(gameBoard.getPieceOf(new Point(13124,5151)).isPresent());
    }
    @Test
    public void IsMated() {
        GameBoard gb = new GameBoard(PlayerBase.GameRule.Normal, PlayerBase.GameRule.Normal);
        gb.replacePiece(new Point(7, 7), new Point(7, 6));
        String str = "P1-KY-KE-GI-KI-OU-KI-GI-KE-KY\n" +
                "P2 * -HI *  *  *  *  * -KA * \n" +
                "P3-FU-FU-FU-FU-FU-FU-FU-FU-FU\n" +
                "P4 *  *  *  *  *  *  *  *  * \n" +
                "P5 *  *  *  *  *  *  *  *  * \n" +
                "P6 *  * +FU *  *  *  *  *  * \n" +
                "P7+FU+FU * +FU+FU+FU+FU+FU+FU\n" +
                "P8 * +KA *  *  *  *  * +HI * \n" +
                "P9+KY+KE+GI+KI+OU+KI+GI+KE+KY";
        assertEquals(str, gb.getBoardSurface());
        gb.replacePiece(new Point(3, 3), new Point(3, 4));
        gb.replacePieceWithPromote(new Point(8, 8), new Point(2, 2));
        gb.replacePiece(new Point(3, 1), new Point(2, 2));
        gb.placePieceInHand(gb.getAttacker().getPiecesInHand().stream()
                        .filter(x->x.getTypeOfPiece() == PieceBase.KAKU)
                        .findFirst()
                        .get()
                , new Point(1, 5));
        try {
            assertTrue(gb.isMated());
        } catch (PlayerNotDefinedGyokuException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void MovePieceFromKifu() {
        GameBoard gameBoard = new GameBoard(PlayerBase.GameRule.Normal, PlayerBase.GameRule.Normal);
        gameBoard.replacePiece(new Point(7,7), new Point(7,6));
        assertTrue(gameBoard.getDefender().getPieceTypeOnBoardAt(new Point(7, 6)).orElse(PieceBase.NONE) == PieceBase.FU);
        assertTrue(gameBoard.getDefender().getPieceTypeOnBoardAt(new Point(7, 7)).orElse(PieceBase.NONE) == PieceBase.NONE);
        gameBoard.replacePiece(new Point(3,3), new Point(3,4));
        gameBoard.replacePiece(new Point(7,6), new Point(7,5));
        // 一回目の駒移動を再現
        gameBoard.replaceAt(1);
        assertTrue(gameBoard.getDefender().getPieceTypeOnBoardAt(new Point(7, 6)).orElse(PieceBase.NONE) == PieceBase.FU);
        assertTrue(gameBoard.getDefender().getPieceTypeOnBoardAt(new Point(7, 7)).orElse(PieceBase.NONE) == PieceBase.NONE);
        // 二回目の駒移動を再現
        gameBoard.replaceAt(2);
        assertTrue(gameBoard.getAttacker().getPieceTypeOnBoardAt(new Point(7, 6)).orElse(PieceBase.NONE) == PieceBase.FU);
        assertTrue(gameBoard.getAttacker().getPieceTypeOnBoardAt(new Point(7, 7)).orElse(PieceBase.NONE) == PieceBase.NONE);
        assertTrue(gameBoard.getDefender().getPieceTypeOnBoardAt(new Point(3, 3)).orElse(PieceBase.NONE) == PieceBase.NONE);
        assertTrue(gameBoard.getDefender().getPieceTypeOnBoardAt(new Point(3, 4)).orElse(PieceBase.NONE) == PieceBase.FU);
    }

    @Test
    public void StartFromMiddle() {
        GameBoard gameBoard = new GameBoard(PlayerBase.GameRule.Normal, PlayerBase.GameRule.Normal);
        gameBoard.replacePiece(new Point(7,7), new Point(7,6));
        gameBoard.replacePiece(new Point(3,3), new Point(3,4));
        gameBoard.replacePiece(new Point(7,6), new Point(7,5));
        // 一回目の駒移動を再現
        gameBoard.replaceAt(1);
        gameBoard.replacePiece(new Point(3,3), new Point(3,4));
        assertFalse(gameBoard.getKifuList().size() == 2);
    }
}
