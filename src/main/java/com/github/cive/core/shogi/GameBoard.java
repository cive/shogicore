/**
 * GameBoard.java
 * 将棋盤の盤上の処理はここで行う
 */

package com.github.cive.core.shogi;

import com.github.cive.core.shogi.Exceptions.PlayerNotDefinedGyokuException;
import com.github.cive.core.shogi.Pieces.PieceBase;
import com.github.cive.core.shogi.Players.AheadPlayer;
import com.github.cive.core.shogi.Players.BehindPlayer;
import com.github.cive.core.shogi.Players.PlayerBase;

import java.awt.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

//import jdk.internal.util.xml.impl.Pair;
import static org.apache.commons.codec.binary.Hex.encodeHexString;

public class GameBoard {
    private Kifu kifu;
    private PlayerBase attacker;
    private PlayerBase defender;
    private PlayerBase playerA;
    private PlayerBase playerB;
    public GameBoard() {
        initGame();
        //printBoard();
    }
    public GameBoard(int rule) {
        initGame(rule);
        //printBoard();
    }
    public Boolean isAheadsTurn() {
        return attacker instanceof AheadPlayer;
    }
    public void initGame() {
        playerA = new AheadPlayer();
        playerB = new BehindPlayer();
        kifu = new Kifu();
        kifu.setInitialPlayers(playerA, playerB);
        setTurn(true);
    }
    private void initGame(int rule) {
        playerA = new AheadPlayer(rule);
        playerB = new BehindPlayer();
        kifu = new Kifu();
        kifu.setInitialPlayers(playerA, playerB);
        setTurn(true);
    }
    public Optional<PieceBase> getPieceOf(Point p) {
        if(GameBoard.isInGrid(p))
        {
            if( attacker.getPieceOnBoardAt(p).isPresent() )
            {
                return attacker.getPieceOnBoardAt(p);
            }
            else if ( defender.getPieceOnBoardAt(p).isPresent() )
            {
                return defender.getPieceOnBoardAt(p);
            }
            return Optional.empty();
        }
        else return Optional.empty();
    }
    public Optional<PieceBase> getPieceOf(int x, int y) {
        return this.getPieceOf(new Point(x, y));
    }
    public static boolean isInGrid(Point point) {
        return point.x >= 0 && point.x < 9
                && point.y >= 0 && point.y < 9;
    }
    private void setTurn(boolean aheadsTurn) {
        if(aheadsTurn) {
            this.attacker = playerA;
            this.defender = playerB;
        } else {
            this.attacker = playerB;
            this.defender = playerA;
        }
    }
    public void nextTurn() {
        System.out.println("attOnBoa: " + attacker.getPiecesOnBoard());
        System.out.println("attInHan: " + attacker.getPiecesInHand());
        System.out.println("defOnBoa: " + defender.getPiecesOnBoard());
        System.out.println("defInHan: " + defender.getPiecesInHand());
        if(this.attacker instanceof BehindPlayer) {
            this.attacker = playerA;
            this.defender = playerB;
        } else {
            this.attacker = playerB;
            this.defender = playerA;
        }
        isConclusion();
    }
    public PlayerBase getAttacker() {
        return attacker;
    }
    public PlayerBase getDefender() {
        return defender;
    }
    public Boolean canPlaceInside(Point src, Point dst) {
        Optional<PieceBase> p = attacker.getPieceOnBoardAt(src);
        if (!  p.isPresent()) return false;
        Set<Point> s = p.get().getCapablePutPoint(attacker, defender);
        return s.size() != 0 && s.contains(dst);
    }
    public Boolean isTherePieceAt(Point p) {
        return attacker.getPieceTypeOnBoardAt(p).orElse(0) + defender.getPieceTypeOnBoardAt(p).orElse(0) > 0;
    }

    /**
     * 持ち駒を置く
     * @param piece このオブジェクトは削除されない
     * @param dst 持ち駒を置く位置
     * @param opt 棋譜に登録するか？ undo, redo で盤面を復元する際は必要ないので false を使う
     */
    private void placePieceInHand(PieceBase piece, Point dst, Boolean opt){
        // 持ち駒を置けるなら置いて，交代
        if(wouldMoveNextLater(piece,dst) && !selected_will_be_niFu(piece, dst.x) && !isTherePieceAt(dst)){

            // 棋譜の登録
            if (opt) {
                Optional<PieceBase> src_piece = Optional.empty();
                Optional<PieceBase> dst_piece = Optional.of(piece);
                dst_piece.get().setPoint(dst);
                // piece オブジェクト自体は削除しないで addPiecesOnBoard で置く
                attacker.reducePieceInHandThatIs(piece);
                // 持ち駒を置く
                piece.setPoint(dst);
                attacker.addPiecesOnBoard(piece);
                kifu.update(new MovementOfPiece(src_piece, dst_piece, getHash()), attacker, defender);
            }
            else
            {
                attacker.reducePieceInHandThatIs(piece);
                // 持ち駒を置く
                piece.setPoint(dst);
                attacker.addPiecesOnBoard(piece);
            }
            this.nextTurn();
        }
    }

    public void placePieceInHand(PieceBase piece, Point dst) {
        placePieceInHand(piece, dst, true);
    }

    private void replacePiece(Point src, Point dst, Boolean opt) {
        // 棋譜の登録
        if (opt) {
            Optional<PieceBase> src_for_kifu = attacker.getPieceOnBoardAt(src);
            Optional<PieceBase> dst_for_kifu = attacker.getPieceOnBoardAt(src);
            dst_for_kifu.get().setPoint(dst);
            replace(src, dst, attacker, defender, false);
            kifu.update(new MovementOfPiece(src_for_kifu, dst_for_kifu, getHash()), attacker, defender);
        }
        else
        {
            replace(src, dst, attacker, defender, false);
        }

        this.nextTurn();
    }

    public void replacePiece(Point src, Point dst) {
        replacePiece(src, dst, true);
    }

    /**
     *
     * @param src 移動元
     * @param dst 移動先
     * @param opt 棋譜に登録するなら{@code true}
     */
    private void replacePieceWithPromote(Point src, Point dst, Boolean opt) {
            // 棋譜の登録
            if (opt) {
                Optional<PieceBase> src_for_kifu = attacker.getPieceOnBoardAt(src);
                Optional<PieceBase> dst_for_kifu = attacker.getPieceOnBoardAt(src);
                dst_for_kifu.get().setPoint(dst);
                // 駒の移動
                replace(src, dst, attacker, defender, true);
                kifu.update(new MovementOfPiece(src_for_kifu, dst_for_kifu.get().getPromotePiece(), getHash()), attacker, defender);
            }
            else
            {
                // 駒の移動
                replace(src, dst, attacker, defender, true);
            }

            this.nextTurn();
    }

    public void replacePieceWithPromote(Point src, Point dst) {
        replacePieceWithPromote(src, dst, true);
    }

    /**
     * undo, redo の実装のための関数
     * 引数の手番を復元する
     * @param num 0番目の手番が初期盤面となる
     */
    public void replaceAt(int num) {
        // プレイヤーの初期手ごまをディープコピーしてくる
        try {
            attacker.update(kifu.getIniAttacker());
            defender.update(kifu.getIniDefender());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        // 記録した棋譜から、並べなおす
        ArrayList<MovementOfPiece> list = kifu.getMovementOfPieceList();
        if (num > list.size()) return;
        for (int i = 0; i < num; i++) {
            MovementOfPiece bs = list.get(i);
            if (!bs.getSrc().get().isOnBoard()) {
                placePieceInHand(bs.getDst().get(), bs.getDst().get().getPoint(), false);
            } else if (!Objects.equals(bs.getDst().get().getTypeOfPiece(), bs.getSrc().get().getTypeOfPiece())) {
               replacePieceWithPromote(bs.getSrc().get().getPoint(), bs.getDst().get().getPoint(), false);
            } else {
                replacePiece(bs.getSrc().get().getPoint(), bs.getDst().get().getPoint(), false);
            }
        }
        kifu.undo(list.size() - num);
    }

    private void replace(Point src, Point dst, PlayerBase attacker, PlayerBase defender, Boolean willPromote)
    {
        // オブジェクトを削除して再配置する
        Optional<PieceBase> src_piece = attacker.getPieceOnBoardAt(src);
        attacker.reducePieceOnBoardAt(src);
        src_piece.get().setPoint(dst);
        if(defender.getPieceTypeOnBoardAt(dst).isPresent()) {
            Optional<PieceBase> dst_piece = defender.getPieceOnBoardAt(dst);
            defender.reducePieceOnBoardAt(dst);
            dst_piece.get().setPoint(new Point(-1,-1));
            if(dst_piece.get().getTypeOfPiece() > PieceBase.GYOKU)
                attacker.addPiecesInHand(dst_piece.get().getDemotePiece().get());
            else
                attacker.addPiecesInHand(dst_piece.get());
        }
        if (willPromote)
            attacker.addPiecesOnBoard(src_piece.get().getPromotePiece().get());
        else
            attacker.addPiecesOnBoard(src_piece.get());
    }

    // 与えられた位置が含まれる列に，既に歩があればtrue
    public boolean selected_will_be_niFu(PieceBase selected_pieceBase, int x){
        boolean ret = false;
        // 置こうとしている駒が歩であるか
        if(selected_pieceBase.getTypeOfPiece() == PieceBase.FU){
            for(int y = 0; y < 9; y++){
                Optional<PieceBase> piece = attacker.getPieceOnBoardAt(new Point(x, y));
                // 自分の駒でかつそれが歩であれば
                if(piece.isPresent() && piece.get().getTypeOfPiece() == PieceBase.FU){
                    ret = true;
                    break;
                }
            }
        }
        return ret;
    }
    // 与えられた位置が動かせる位置ならtrue
    public Boolean wouldMoveNextLater(PieceBase selected_pieceBase, Point dst) {
        PieceBase p = null;
        try {
            p = selected_pieceBase.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        if(p == null) return false;
        p.setPoint(dst);
        return p.canMoveLater(attacker);
    }

    // 場所を表わす文字列を座標値に変換（「5二」を表わす「52」は(4, 1)に変換）
    private Point LocationStringToPoint(String str) {
        int x = Integer.parseInt(str.substring(0, 1));
        int y = Integer.parseInt(str.substring(1, 2));
        return new Point(9 - x, y - 1);
    }

    // 文字列で駒を移動する
    public boolean MoveByString(String str) {
        // 文字列のパターンで場合分け
        if (Pattern.compile("[1-9]{4}").matcher(str).matches()) {
            // 移動
            Point src = LocationStringToPoint(str.substring(0, 2));
            Point dst = LocationStringToPoint(str.substring(2, 4));
            if (canPlaceInside(src, dst)) {
                replacePiece(src, dst);
                return true;
            }
        }else if (Pattern.compile("[1-9]{4}\\+").matcher(str).matches()) {
            // 移動して成る
            Point src = LocationStringToPoint(str.substring(0, 2));
            Point dst = LocationStringToPoint(str.substring(2, 4));
            if (canPlaceInside(src, dst)) {
                replacePieceWithPromote(src, dst);
                return true;
            }
        }else if (Pattern.compile(".[1-9]{2}").matcher(str).matches()) {
            // 持ち駒を指す
            Map<String, Integer> nameIdPairs = new HashMap<String, Integer>() {
                {
                    String[] keys = new String[] {"歩", "香", "桂", "銀", "金", "角", "飛"};
                    int[] values = new int[] {PieceBase.FU, PieceBase.KYOSHA, PieceBase.KEIMA, PieceBase.GIN, PieceBase.KIN, PieceBase.KAKU, PieceBase.HISHA};
                    for(int i = 0; i < 7; i++){
                        put(keys[i], values[i]);
                    }
                }
            };
            String nameOfHand = str.substring(0, 1);	// これから置く駒
            if(nameIdPairs.containsKey(nameOfHand)){
                int pieceId = nameIdPairs.get(nameOfHand);
                Point dst = LocationStringToPoint(str.substring(1, 3));
                Optional<PieceBase> src = attacker.getPiecesInHand().stream().filter(x -> x.getTypeOfPiece() == pieceId).findFirst();
                attacker.reducePieceInHandThatIs(src.get());
                placePieceInHand(src.get(), dst);
                return true;
            }
        }
        return false;
    }

    public void printBoard() {
        for(int y = 0; y < 9; y++) {
            for(int x = 0; x < 9; x++) {
                Point point = new Point(x, y);
                String str = "";
                if (attacker.getPieceTypeOnBoardAt(point).isPresent())
                {
                    str += "+";
                    str += attacker.getPieceOnBoardAt(point).get().getName();
                }
                else if (defender.getPieceOnBoardAt(point).isPresent())
                {
                    str += "-";
                    str += defender.getPieceOnBoardAt(point).get().getName();
                }
                else
                {
                    str += "_ ";
                }
                System.out.print(str);
            }
            System.out.println();
        }
    }
    public ArrayList<MovementOfPiece> getKifuList() {
        return kifu.getMovementOfPieceList();
    }
    private String getHash() {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        if (md != null) {
            md.update(this.getBoardSurface().getBytes());
        } else {
            System.err.println("ハッシュ値が取得できません");
            throw new NullPointerException();
        }
        byte[] data = md.digest();
        return encodeHexString(data);
    }
    public String getBoardSurface() {
        StringBuilder ret = new StringBuilder();
        for(int y = 0; y < 9; y++) {
            ret.append("P").append(y + 1);
            for(int x = 0; x < 9; x++) {
                PlayerBase player;
                if (attacker.getPieceTypeOnBoardAt(new Point(x, y)).isPresent()) player = attacker;
                else player = defender;
                Boolean existPiece = player.getPieceTypeOnBoardAt(new Point(x,y)).isPresent();
                if (player instanceof AheadPlayer && existPiece) ret.append("+");
                else if (player instanceof BehindPlayer && existPiece) ret.append("-");
                if (!existPiece) ret.append(" * ");
                else ret.append(player.getPieceOnBoardAt(new Point(x, y)).get().getName(true));
            }
            if (y != 8) ret.append("\n");
        }
        return ret.toString();
    }

    /**
     * 敵が王手を仕掛けて来ていたら王手されていると判断する
     * @return 王手されているならば{@code true}
     */
    private Boolean isMated(PlayerBase attacker, PlayerBase defender) throws PlayerNotDefinedGyokuException {
        // とりあえずの実装
        // より早く判定するには、玉の周りの駒と飛車角行を調べればよい
        Point ptGyoku = attacker.getPiecesOnBoard(PieceBase.GYOKU)
                .findFirst()
                .orElseThrow(PlayerNotDefinedGyokuException::new)
                .getPoint();
        Set<Point> set = new HashSet<>();
        defender.getPiecesOnBoard().stream().forEach(x -> set.addAll(x.getCapablePutPoint(defender,attacker)));
        return set.stream().anyMatch(x -> x.equals(ptGyoku));
    }

    public Boolean isMated() throws PlayerNotDefinedGyokuException {
        return isMated(this.attacker, this.defender);
    }

    /**
     * 投了判定
     * 王手されているときに判定する
     * 計算量は多め
     * @return これ以上指す手がない場合{@code true}
     */
    public Boolean isCheckmated() throws PlayerNotDefinedGyokuException {
        // 王手されている前提
        // 攻撃側の玉を動かしたとしてもisMatedだった場合、投了の可能性があるので
        // 何かはれる可能性がないか調べる
        PlayerBase cAtt = null, cDef = null;
        try {
            cAtt = attacker.clone();
            cDef = defender.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        final PlayerBase att = cAtt;
        final PlayerBase def = cDef;
        List<PieceBase> pieces_in_hand = new ArrayList<>();
        pieces_in_hand.addAll(attacker.getPiecesInHand());
        Set<Point> points_of_capable_moving_of_gyoku = new HashSet<>();
        Point ptGyoku;
        if (att != null && def != null) {
            ptGyoku = att.getPiecesOnBoard(PieceBase.GYOKU)
                    .findAny()
                    .orElseThrow(PlayerNotDefinedGyokuException::new)
                    .getPoint();
            points_of_capable_moving_of_gyoku.addAll(att.getPieceOnBoardAt(ptGyoku).get().getCapablePutPoint(att,def));
        } else {
            System.err.println("読み込みエラー");
            throw new NullPointerException();
        }
        // まず、玉を他の位置に移動して王手が防げるか判定する
        for (Point movable_gyoku : points_of_capable_moving_of_gyoku) {
            // replace したときにどうなるかを確かめるため、ディープコピーしなければならない
            PlayerBase a = null, d = null;
            try {
                a = att.clone();
                d = def.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }

            if (a != null && d != null)
            {
                replace(ptGyoku, movable_gyoku, a, d, false);
            }
            if (!isMated(a, d)) return false;
        }

        // もし、駒を持っていて指すことで王手を防げるのなら詰みではない
        if(!def.getPiecesInHand().isEmpty()) for (Point movable_gyoku : points_of_capable_moving_of_gyoku)
        {
            PlayerBase a = null, d = null;
            try {
                a = att.clone();
                d = def.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }

            if (a != null && d != null) {
                for (PieceBase piece : pieces_in_hand) {
                    if ( !selected_will_be_niFu(piece, movable_gyoku.x)) {
                        piece.setPoint(movable_gyoku);
                        a.addPiecesOnBoard(piece);
                        if (!isMated(a, d)) return false;
                    }
                }
            }
        }
        // もし、玉の周りの駒を移動させることで王手を防げるのなら詰みではない
        for (PieceBase pieceBase : att.getPiecesOnBoard()) {
            Point src = pieceBase.getPoint();
            Set<Point> points = new HashSet<>();
            points.addAll(pieceBase.getCapablePutPoint(att, def));
            for (Point dest : points) {
                PlayerBase a = null, d = null;
                try {
                    a = att.clone();
                    d = def.clone();
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }

                if (a != null && d != null) {
                    replace(src, dest, a, d, false);
                }
                if (!isMated(a, d)) return false;
            }
        }
        return true;
    }

    /**
     * 終局判定
     * @return 終局であれば{@code true}
     */
    public Boolean isConclusion() {
        if (kifu.isSennnichite()) return true;
        try {
            if (isMated()) {
                System.out.println("Mate");
                if (isCheckmated()) {
                    System.out.println("Checkmate");
                    return true;
                }
            }
        } catch (PlayerNotDefinedGyokuException e) {
            e.printStackTrace();
        }
        return false;
    }
}