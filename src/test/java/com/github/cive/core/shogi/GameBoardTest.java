package com.github.cive.core.shogi;
import org.junit.Test;

public class GameBoardTest {
    @Test
    public void CanLaunchGameBoard()
    {
        GameBoard gameBoard = new GameBoard();
        assert gameBoard != null : gameBoard.getBoardSurface();
    }
}
