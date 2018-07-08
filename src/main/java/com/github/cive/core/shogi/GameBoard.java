/**
 * GameBoard.java
 * 将棋盤の盤上の処理はここで行う
 */

package com.github.cive.core.shogi;

import com.github.cive.core.shogi.Exceptions.PlayerNotDefinedGyokuException;
import com.github.cive.core.shogi.Pieces.PieceBase;
import com.github.cive.core.shogi.Pieces.PieceFactory;
import com.github.cive.core.shogi.Players.AheadPlayer;
import com.github.cive.core.shogi.Players.BehindPlayer;
import com.github.cive.core.shogi.Players.PlayerBase;

import java.awt.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

//import jdk.internal.util.xml.impl.Pair;
import static com.github.cive.core.shogi.Utils.LocationStringToPoint;
import static com.github.cive.core.shogi.Utils.LocationStringToPosition;
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
    public GameBoard(PlayerBase.GameRule ahead_rule, PlayerBase.GameRule behind_rule) {
        initGame(ahead_rule, behind_rule);
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
    public void initGame(PlayerBase.GameRule ahead_rule, PlayerBase.GameRule behind_rule) {
        playerA = new AheadPlayer(ahead_rule);
        playerB = new BehindPlayer(behind_rule);
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
    public static boolean isInGrid(Point position) {
        return position.x >= 1 && position.x <= 9
                && position.y >= 1 && position.y <= 9;
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

    /**
     *
     * @param src_position
     * @param dst_position
     * @return
     */
    public Boolean canPlaceInside(Point src_position, Point dst_position) {
        Optional<PieceBase> p = attacker.getPieceOnBoardAt(src_position);
        if (!  p.isPresent()) return false;
        Set<Point> s = p.get().getCapablePutPosition(attacker, defender);
        return s.size() != 0 && s.contains(dst_position);
    }

    /**
     *
     * @param position
     * @return
     */
    public Boolean isTherePieceAt(Point position) {
        return attacker.getPieceTypeOnBoardAt(position).orElse(0) + defender.getPieceTypeOnBoardAt(position).orElse(0) > 0;
    }

    /**
     * 持ち駒を置く
     * @param piece このオブジェクトは削除されない
     * @param dst_position 持ち駒を置く位置
     * @param opt 棋譜に登録するか？ undo, redo で盤面を復元する際は必要ないので false を使う
     */
    private void placePieceInHand(PieceBase piece, Point dst_position, Boolean opt){
        // 持ち駒を置けるなら置いて，交代
        if(wouldMoveNextLater(piece, dst_position) && !selected_will_be_niFu(piece, dst_position.x) && !isTherePieceAt(dst_position)){

            // 棋譜の登録
            if (opt) {
                Optional<PieceBase> src_piece = Optional.empty();
                Optional<PieceBase> dst_piece = PieceFactory.create(piece.getTypeOfPiece(), dst_position);
                // piece オブジェクト自体は削除しないで addPiecesOnBoard で置く
                attacker.reducePieceInHandThatIs(piece);
                // 持ち駒を置く
                piece.setPosition(dst_position);
                attacker.addPiecesOnBoard(piece);
                kifu.update(new MovementOfPiece(src_piece, dst_piece, getHash()), attacker, defender);
            }
            else
            {
                attacker.reducePieceInHandThatIs(piece);
                // 持ち駒を置く
                piece.setPosition(dst_position);
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
            Optional<PieceBase> src_for_kifu = PieceFactory.create(attacker.getPieceOnBoardAt(src).get().getTypeOfPiece(), src);
            Optional<PieceBase> dst_for_kifu = PieceFactory.create(src_for_kifu.get().getTypeOfPiece(), dst); // gui のポイントを入れなくてはならない
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
                Optional<PieceBase> src_for_kifu = PieceFactory.create(attacker.getPieceOnBoardAt(src).get().getTypeOfPiece(), src);
                Optional<PieceBase> dst_for_kifu = PieceFactory.createPromoted(src_for_kifu.get().getTypeOfPiece(), dst);
                // 駒の移動
                replace(src, dst, attacker, defender, true);
                // 駒の移動をしてからハッシュ値を取得する必要がある
                kifu.update(new MovementOfPiece(src_for_kifu, dst_for_kifu, getHash()), attacker, defender);
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
                placePieceInHand(bs.getDst().get(), bs.getDst().get().getPosition(), false);
            } else if (!Objects.equals(bs.getDst().get().getTypeOfPiece(), bs.getSrc().get().getTypeOfPiece())) {
               replacePieceWithPromote(bs.getSrc().get().getPosition(), bs.getDst().get().getPosition(), false);
            } else {
                replacePiece(bs.getSrc().get().getPosition(), bs.getDst().get().getPosition(), false);
            }
        }
        kifu.undo(list.size() - num);
    }

    /**
     * replace internal func
     * @param src_position
     * @param dst_position
     * @param attacker
     * @param defender
     * @param willPromote
     */
    private void replace(Point src_position, Point dst_position, PlayerBase attacker, PlayerBase defender, Boolean willPromote)
    {
        // オブジェクトを削除して再配置する
        Optional<PieceBase> src_piece = attacker.getPieceOnBoardAt(src_position);
        attacker.reducePieceOnBoardAt(src_position);
        src_piece.get().setPosition(dst_position);
        if(defender.getPieceTypeOnBoardAt(dst_position).isPresent()) {
            Optional<PieceBase> dst_piece = defender.getPieceOnBoardAt(dst_position);
            defender.reducePieceOnBoardAt(dst_position);
            dst_piece.get().setPosition(new Point(-1,-1));
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
    public boolean selected_will_be_niFu(final PieceBase selected_piece, int x){
        boolean ret = false;
        // 置こうとしている駒が歩であるか
        if(selected_piece.getTypeOfPiece() == PieceBase.FU){
            for(int y = 1; y <= 9; y++){
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
    public Boolean wouldMoveNextLater(final PieceBase selected_piece, Point dst_position) {
        // selected_piece が deep copy でないために、一度コピーしてから setPosition している
        PieceBase p = null;
        try {
            p = selected_piece.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        if(p == null) return false;
        p.setPosition(dst_position);
        return p.canMoveLater(attacker);
    }


    // 文字列で駒を移動する
    public boolean MoveByString(String str) {
        // 文字列のパターンで場合分け
        if (Pattern.compile("[1-9]{4}").matcher(str).matches()) {
            // 移動
            Point src = LocationStringToPosition(str.substring(0, 2));
            Point dst = LocationStringToPosition(str.substring(2, 4));
            if (canPlaceInside(src, dst)) {
                replacePiece(src, dst);
                return true;
            }
        }else if (Pattern.compile("[1-9]{4}\\+").matcher(str).matches()) {
            // 移動して成る
            Point src = LocationStringToPosition(str.substring(0, 2));
            Point dst = LocationStringToPosition(str.substring(2, 4));
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

    public boolean MoveByStringForCsaV21(String str)
    {
        AtomicBoolean ret = new AtomicBoolean(false);
        // 文字列のパターンで場合分け
        if (Pattern.compile("[1-9]{4}.*").matcher(str).matches()) {
            // 移動
            Point src = LocationStringToPosition(str.substring(0, 2));
            Point dst = LocationStringToPosition(str.substring(2, 4));
            String piece_type_str = str.substring(4, 6);
            int piece_type = PieceBase.getTypeFromString(piece_type_str);
            Optional<PieceBase> src_piece = getPieceOf(src);
            if (src_piece.isPresent() && canPlaceInside(src, dst)) {
                if (src_piece.get().getTypeOfPiece() == piece_type) {
                    replacePiece(src, dst);
                    ret.set(true);
                }
                src_piece.get().getPromotePiece().ifPresent(piece -> {
                    if (piece.getTypeOfPiece() == piece_type)
                    {
                        replacePieceWithPromote(src, dst);
                        ret.set(true);
                    }
                });
            }
        }
        else if (Pattern.compile("00[1-9]{2}.*").matcher(str).matches()) {
            // 持ち駒を指す
            Point dst = LocationStringToPosition(str.substring(2, 4));
            String piece_type_str = str.substring(4, 6);
            int piece_type = PieceBase.getTypeFromString(piece_type_str);
            Optional<PieceBase> src = attacker.getPiecesInHand().stream().filter(x -> x.getTypeOfPiece() == piece_type).findFirst();
            src.ifPresent(pieceBase -> {
                placePieceInHand(src.get(), dst);
                ret.set(true);
            });
        }
        return ret.get();
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
                Point position = new Point(9-x, y+1);
                PlayerBase player;
                if (attacker.getPieceTypeOnBoardAt(position).isPresent()) player = attacker;
                else player = defender;
                Boolean existPiece = player.getPieceTypeOnBoardAt(position).isPresent();
                if (player instanceof AheadPlayer && existPiece) ret.append("+");
                else if (player instanceof BehindPlayer && existPiece) ret.append("-");
                if (!existPiece) ret.append(" * ");
                else ret.append(player.getPieceOnBoardAt(position).get().getName(true));
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
                .getPosition();
        Set<Point> set = new HashSet<>();
        defender.getPiecesOnBoard().stream().forEach(x -> set.addAll(x.getCapablePutPosition(defender,attacker)));
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
                    .getPosition();
            points_of_capable_moving_of_gyoku.addAll(att.getPieceOnBoardAt(ptGyoku).get().getCapablePutPosition(att,def));
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
        for (PieceBase piece : att.getPiecesOnBoard()) {
            Point src = piece.getPosition();
            Set<Point> positions = new HashSet<>();
            positions.addAll(piece.getCapablePutPosition(att, def));
            for (Point dest : positions) {
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

    public long countPieces(int type)
    {
        long count = this.attacker.getPiecesOnBoard().stream().filter(piece -> piece.getTypeOfPiece() == type || piece.getBacksideType() == type).count();
        count += this.attacker.getPiecesOnBoard().stream().filter(piece -> piece.getTypeOfPiece() == type || piece.getBacksideType() == type).count();
        count += this.defender.getPiecesOnBoard().stream().filter(piece -> piece.getTypeOfPiece() == type || piece.getBacksideType() == type).count();
        count += this.defender.getPiecesOnBoard().stream().filter(piece -> piece.getTypeOfPiece() == type || piece.getBacksideType() == type).count();
        return count;
    }

    public boolean canPlaceAtInit(int type)
    {
        return numberOfPiece(type) > countPieces(type);
    }

    public Integer numberOfPiece(int type)
    {
        switch (type) {
            case PieceBase.FU:
            case PieceBase.TOKIN:
                return 18;
            case PieceBase.KYOSHA:
            case PieceBase.NARIKYO:
            case PieceBase.KEIMA:
            case PieceBase.NARIKEI:
            case PieceBase.GIN:
            case PieceBase.NARIGIN:
            case PieceBase.KIN:
                return 4;
            case PieceBase.KAKU:
            case PieceBase.UMA:
            case PieceBase.HISHA:
            case PieceBase.RYU:
            case PieceBase.GYOKU:
                return 2;
            default:
                return 0;
        }
    }
}