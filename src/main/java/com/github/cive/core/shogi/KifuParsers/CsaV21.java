package com.github.cive.core.shogi.KifuParsers;

import com.github.cive.core.shogi.GameBoard;
import com.github.cive.core.shogi.Pieces.PieceBase;
import com.github.cive.core.shogi.Pieces.PieceFactory;
import com.github.cive.core.shogi.Players.PlayerBase;
import com.github.cive.core.shogi.Utils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.github.cive.core.shogi.Pieces.Constant.*;

public class CsaV21 {
    private final int ALL_TYPE = 100;
    private boolean isVersion2 = false;
    private String black_player;
    private String white_player;
    private GameBoard game_board;
    private int row = 0;
    private boolean isPreInitialized = false;
    private boolean isInitialized = false;
    private Logger logger;
    private Integer[] piece_max = new Integer[8];

    private void accept(String line) {
        if (line.isEmpty()) return;
        switch(line.charAt(0))
        {
            case '\'':
                break;
            case 'V':
                if (line.length() == 2 && line.charAt(1) == '2')
                {
                    isVersion2 = true;
                }
                break;
            case 'N':
                if (line.length() > 2 && line.charAt(1) == '+')
                {
                    black_player = line.substring(2);
                }
                else
                {
                    white_player = line.substring(2);
                }
                break;
            case 'P':
                function_P(line);
                break;
            case '+':
                if (isVersion2) game_board.MoveByStringForCsaV21(line.substring(1));
                break;
            case '-':
                if (isVersion2) game_board.MoveByStringForCsaV21(line.substring(1));
                break;
            case 'T':
                // TODO: 時刻の追加
                break;
            case '$':
                break;
            case '%':
                break;
            default:
                break;
        }
    }

    private void function_P(String line)
    {
        if (!isVersion2) return;
        if (line.length() == 29 && !isPreInitialized)
        {
            ++row;
            int line_num = Integer.parseInt(line.charAt(1) + "");
            String sub = line.substring(2);
            Matcher m = Pattern.compile("[\\s\\S]{3}").matcher(sub);
            int col = 9;
            while (m.find())
            {
                String piece_str = m.group();
                String piece_type = piece_str.substring(1);
                int type = PieceBase.getTypeFromString(piece_type);
                if (type == PieceBase.NONE)
                {
                    --col;
                    continue;
                }
                Optional<PieceBase> piece = PieceFactory.create(type, new Point(col, line_num));
                if (piece_str.charAt(0) == '+')
                {
                    game_board.getAttacker().addPiecesOnBoard(piece.get());
                }
                else if (piece_str.charAt(0) == '-')
                {
                    game_board.getDefender().addPiecesOnBoard(piece.get());
                }
                --col;
            }
            // バリエーションチェックは行わないため、 PI や P+ などが走った場合でもこのルートを通る場合がある
            // 間違えたファイルを入力したときはバグるかもしれない
            if (row == 9) isPreInitialized = true;
            return;
        }
        if (row != 0)
        {
            switch (line.charAt(1))
            {
                case 'I':
                    function_PI(line);
                case '+':
                    function_Pplus(line);
                case '-':
                    function_Pminus(line);
            }
        }
    }

    private void function_PI(String line)
    {
        if (isPreInitialized) return;
        game_board.initGame(PlayerBase.GameRule.Normal, PlayerBase.GameRule.Normal);
        // ex. PI13FU23FU22KA82HI
        String sub = line.substring(2);
        Matcher m = Pattern.compile("[\\s\\S]{4}").matcher(sub);
        while(m.find())
        {
            String piece_info_str = m.group();
            Pair<Point, Integer> pair;
            pair = getPieceInfoFromString(piece_info_str);
            Optional<PieceBase> piece = game_board.getPieceOf(pair.getKey());
            piece.ifPresent((PieceBase p) -> {
                if (p.getTypeOfPiece().equals(pair.getValue()))
                {
                    // どちらかにはある
                    if (!game_board.getAttacker().reducePieceInHandThatIs(p)) game_board.getDefender().reducePieceInHandThatIs(p);
                }
            });
        }
        isPreInitialized = true;
    }

    private void function_Pplus(String line)
    {
        String sub = line.substring(2);
        Matcher m = Pattern.compile("[\\s\\S]{4}").matcher(sub);
        while(m.find())
        {
            String piece_info_str = m.group();
            Pair<Point, Integer> pair = getPieceInfoFromString(piece_info_str);
            if (pair.getValue() == ALL_TYPE)
            {
                function_P_addAll(true);
                return;
            }
            Optional<PieceBase> piece = game_board.getPieceOf(pair.getKey());
            // type = AL の場合も考慮しなければならない
            if (!piece.isPresent() && !pair.getKey().equals(new Point(0, 0)) && game_board.canPlaceAtInit(pair.getValue()))
            {
                game_board.getAttacker().addPiecesOnBoard(PieceFactory.create(pair.getValue(), pair.getKey()).get());
            }
            else
            {
                game_board.getAttacker().addPiecesInHand(PieceFactory.create(pair.getValue(), new Point(-1, -1)).get());
            }
        }
    }

    private void function_Pminus(String line)
    {
        String sub = line.substring(2);
        Matcher m = Pattern.compile("[\\s\\S]{4}").matcher(sub);
        while(m.find())
        {
            String piece_info_str = m.group();
            Pair<Point, Integer> pair = getPieceInfoFromString(piece_info_str);
            if (pair.getValue() == ALL_TYPE)
            {
                function_P_addAll(false);
                return;
            }
            Optional<PieceBase> piece = game_board.getPieceOf(pair.getKey());
            // type = AL の場合も考慮しなければならない
            if (!piece.isPresent() && !pair.getKey().equals(new Point(0, 0)) && game_board.canPlaceAtInit(pair.getValue()))
            {
                game_board.getAttacker().addPiecesOnBoard(PieceFactory.create(pair.getValue(), pair.getKey()).get());
            }
        }
    }

    private void function_P_addAll(boolean isBlack)
    {
        PlayerBase player;
        if (isBlack)
        {
            player = game_board.getAttacker();
        }
        else
        {
            player = game_board.getDefender();
        }
        for (int type = FU; type <= GYOKU; ++type)
        {
            long count = game_board.countPieces(type);
            for (;count < piece_max[type-1];++count)
            {
                player.addPiecesInHand(PieceFactory.create(type, new Point(-1, -1)).get());
            }
        }
    }

    private Pair<Point, Integer> getPieceInfoFromString(String piece_info_str)
    {
        String place_str = piece_info_str.substring(0, 2);
        String type_str = piece_info_str.substring(2, 4);
        Point place = Utils.LocationStringToPosition(place_str);
        int type;
        if (type_str.equals("AL"))
        {
            type = ALL_TYPE;
        }
        else
        {
            type = PieceBase.getTypeFromString(type_str);
        }
        return new MutablePair<>(place, type);
    }

    public void read(GameBoard gb, String filename)
    {
        this.game_board = gb;
        readPropaties();
        try
        {
            Stream<String> stream = Files.lines(Paths.get(filename), StandardCharsets.UTF_8);
            stream.forEach(this::accept);
        }
        catch (IOException | NumberFormatException e)
        {
            System.err.println(e.getMessage());
        }
    }

    private void readPropaties()
    {
        Optional<ResourceBundle> res = Optional.empty();

        try
        {
            res = Optional.ofNullable(ResourceBundle.getBundle("csa_v21"));
        }
        catch (MissingResourceException e)
        {
            e.printStackTrace();
        }
        piece_max[FU-1]     = readAndValidateNumber(res, "fu_max", 18);
        piece_max[KYOSHA-1] = readAndValidateNumber(res, "kyosha_max", 4);
        piece_max[KEIMA-1]  = readAndValidateNumber(res, "keima_max", 4);
        piece_max[GIN-1]    = readAndValidateNumber(res, "gin_max", 4);
        piece_max[KIN-1]    = readAndValidateNumber(res, "kin_max", 4);
        piece_max[KAKU-1]   = readAndValidateNumber(res, "kaku_max", 2);
        piece_max[HISHA-1]  = readAndValidateNumber(res, "hisha_max", 2);
        piece_max[GYOKU-1]  = readAndValidateNumber(res, "gyoku_max", 2);

    }

    /**
     * @brief プロパティから key の値を読み取り、存在しない場合・0以上の整数でない場合は、デフォルト値を返す
     * @param res プロパティのリソース (存在しない場合は、empty)
     * @param key プロパティのキー
     * @param default_value デフォルト値
     * @return 読み取った値
     */
    private Integer readAndValidateNumber(Optional<ResourceBundle> res, String key, Integer default_value)
    {
        if (res.isPresent())
        {
            String strval;
            try
            {
                strval = res.get().getString(key);
            }
            catch (MissingResourceException e)
            {
                e.printStackTrace();
                return default_value;
            }
            String regex = "\\A[0-9]+\\z";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(strval);
            if (m.find())
            {
                return Integer.parseInt(strval);
            }
            else
            {
                return default_value;
            }
        }
        else
        {
            return default_value;
        }

    }
}
