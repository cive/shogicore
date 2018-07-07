package com.github.cive.core.shogi.KifuParsers;

import com.github.cive.core.shogi.GameBoard;
import org.junit.Test;

import java.io.File;

public class CsaV21Test {
    @Test
    public void ReadTest1()
    {
        File resourcesDirectory = new File("src/test/resources" + "/csav21/sample1.csa");
        CsaV21 csav21 = new CsaV21();
        GameBoard gb = new GameBoard();
        csav21.read(gb, resourcesDirectory.getAbsolutePath());
    }
    @Test
    public void ReadTest2()
    {
        File resourcesDirectory = new File("src/test/resources" + "/csav21/sample2.csa");
        CsaV21 csav21 = new CsaV21();
        GameBoard gb = new GameBoard();
        csav21.read(gb, resourcesDirectory.getAbsolutePath());
    }
    @Test
    public void ReadTest3()
    {
        File resourcesDirectory = new File("src/test/resources" + "/csav21/sample3.csa");
        CsaV21 csav21 = new CsaV21();
        GameBoard gb = new GameBoard();
        csav21.read(gb, resourcesDirectory.getAbsolutePath());
    }
}
