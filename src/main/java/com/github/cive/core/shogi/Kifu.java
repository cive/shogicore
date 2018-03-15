package com.github.cive.core.shogi;

import com.github.cive.core.shogi.Pieces.PieceBase;
import com.github.cive.core.shogi.Players.PlayerBase;
import org.w3c.dom.ranges.RangeException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by yotuba on 16/05/06.
 * 棋譜を記録
 * プレイヤーの初期の持ち駒 と 駒の移動を記録
 * undo, redoを使って手数(movedNum)を変更
 * もし、実際の手番(movementOfPieceList.size())よりも前の手番を参照していたらそれ以降の盤面を削除し、そこから新規に記録する
 */
public class Kifu {
    /**
     * Players 構造体
     */
    private class Players {
        private PlayerBase attacker;
        private PlayerBase defender;
        public Players(PlayerBase attacker, PlayerBase defender) {
            setAttacker(attacker);
            setDefender(defender);
        }
        protected void setAttacker(PlayerBase attacker) {
            try {
                this.attacker = attacker.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        protected void setDefender(PlayerBase defender) {
            try {
                this.defender = defender.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        protected PlayerBase getAttacker() throws CloneNotSupportedException{
            return attacker.clone();
        }
        protected PlayerBase getDefender() throws CloneNotSupportedException{
            return defender.clone();
        }
    }
    // 動かした回数
    private int movedNum;
    // 千日手を調べるために使用する変数
    // Kifu クラスには、盤面のハッシュ値を計算する機構がないために必要
    public Boolean hasSafeUpdated;
    // 初期盤面を登録するために必要な変数
    private Players initialPlayers;
    // 棋譜を記録するためのリスト
    // undo, redo をするために保持する
    private ArrayList<MovementOfPiece> movementOfPieceList;
    // 文字列の棋譜を記録するリスト
    // 棋譜をエクスポートするために使用する
    private ArrayList<String> kifuList;
    // 千日手を判定するために保持するリスト
    private List<String> hashList;
    public Kifu() {
        movementOfPieceList = new ArrayList<>();
        kifuList = new ArrayList<>();
        hashList = new ArrayList<>();
        hasSafeUpdated = true;
    }
    // 初期配置は、redo する際に必要
    public void setInitialPlayers(PlayerBase attacker, PlayerBase defender) {
        initialPlayers = new Players(attacker, defender);
    }
    public PlayerBase getIniAttacker() throws CloneNotSupportedException{
        return initialPlayers.getAttacker();
    }
    public PlayerBase getIniDefender() throws CloneNotSupportedException{
        return initialPlayers.getDefender();
    }

    /**
     * どちらの手番かを動かした回数から調べる
     * @param num 手目
     * @return TRUE ならば、手前のプレイヤーの手番
     */
    public Boolean hasAhead(int num) {
        return num > 0 && (movedNum > 0 && movedNum >= num);
    }

    /**
     * undo した際に、次の手を持っているか調べる
     * @param num 手目
     * @return TRUE ならば、次の手が記録されている
     */
    public Boolean hasNext(int num) {
        return num > 0 && movedNum + num <= movementOfPieceList.size();
    }
    public void undo(int num) {
        if (num <= 0) return;
        if (movedNum > 0 && movedNum >= num) movedNum -= num;
    }
    public void redo(int num) {
        if (num <= 0) return;
        if (movedNum + num <= movementOfPieceList.size()) movedNum += num;
    }

    /**
     * 駒の移動を記録したデータリストを返す
     * @return 駒の移動を記録したリスト
     */
    public ArrayList<MovementOfPiece> getMovementOfPieceList() {
        return movementOfPieceList;
    }

    /**
     * undo(n)でn手前にセットすることでn手前以降のデータを削除し、n+1手目のデータをセットする
     * num = list.size() - n となる
     * 実際に外から実行できるのは update(MovementOfPiece, Player, Player)のみ
     * @param mp 駒の移動
     * @param attacker 駒を移動する側
     * @param defender 防御側
     * @param num num手目にアップデート、それ以降の棋譜は削除
     * @throws RangeException 入力値numが不正
     */
    private void updateInternal(MovementOfPiece mp, PlayerBase attacker, PlayerBase defender, int num) throws RangeException {
        // movementOfPieceList.size() は初期値が 0
        // 2手差があるのに、更新しようとするのはおかしいので例外とする
        if (num - movementOfPieceList.size() >= 2 || num < 0) {
            throw new RangeException(RangeException.BAD_BOUNDARYPOINTS_ERR, "範囲外の値です");
        } else if (movementOfPieceList.size() > num) {
            while (movementOfPieceList.size() > num) {
                movementOfPieceList.remove(num);
                kifuList.remove(num);
                hashList.remove(num);
            }
        }
        try {
            if (movementOfPieceList.size() > 0) {
                kifuList.add(getKifu(
                        mp
                        , movementOfPieceList.get(movementOfPieceList.size() - 1)
                        , attacker.clone()
                        , defender.clone()
                ));
            }
            else
            {
                // 初手
                kifuList.add(getKifu(mp));
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        movementOfPieceList.add(mp);
        movedNum = num;
    }

    /**
     * このメソッドは、 {@code undo(n)} などを考慮しないで次の一手を指す際に使える
     * {@code undo(n)}でn手前にセットすることでn手前以降のデータを削除し、n+1手目のデータをセットする
     * {@code undo(n)}を使用していない場合は、棋譜を登録するのみ
     * 使用後は {@code updateHash}を使用すること
     * @param mp 駒の移動
     * @param attacker 駒を移動する側
     * @param defender 防御側
     */
    public void update(MovementOfPiece mp, PlayerBase attacker, PlayerBase defender) {
        updateInternal(mp, attacker, defender, movedNum +1);
    }

    /**
     * 初手の際にのみ使用する関数
     * @param now
     * @return
     */
    protected String getKifu(MovementOfPiece now)
    {
        String str = "▲";
        Optional<PieceBase> dst = now.getDst();
        if (dst.isPresent())
        {
            str += dst.get().getPosition().x + getChineseNum(dst.get().getPosition().y);
            str += dst.get().getName();
        }
        System.out.println("info: " + str); // TODO: Logger
        return str;
    }

    /**
     * 棋譜(String)を出力するメソッド
     * updateされる毎に実行される
     * @param now 現在の盤面
     * @param prev 一つ前の盤面
     * @param attacker 現在の盤面の攻撃側
     * @param defender 現在の盤面の防御側
     * @return 棋譜
     */
    private String getKifu(MovementOfPiece now, MovementOfPiece prev, PlayerBase attacker, PlayerBase defender)
    {
        final Optional<PieceBase> src = now.getSrc();
        final Optional<PieceBase> dst = now.getDst();
        String str = "";
        str += isAheadTurn() ? "▲" : "△";
        int trans = isAheadTurn() ? 1 : -1; /* 先手、後手座標変換用 */
        if (movedNum != 1 && isSamePosition(now, prev)) {
            str += "同";
        } else {
            str += dst.get().getPosition().x + getChineseNum(dst.get().getPosition().y);
        }
        str += dst.get().getName();
        int src_type = src.get().getTypeOfPiece();
        /**
         * おける場所が重ならない駒は
         * FU, KYOSHA or GYOKU
         * おける場所が重なる駒は
         * KEIMA, GIN, KIN(NARI*), KAKU, HISHA, UMA or RYU
         */
        if (src_type == PieceBase.KEIMA) {
            attacker.getPiecesOnBoard(src_type).forEach(x -> System.out.println(x.getPosition()));
            Optional<PieceBase> keima = Optional.ofNullable(attacker.getPiecesOnBoard(src_type)
                    .filter(x -> x.getCapablePutPoint(attacker, defender).contains(dst.get().getPoint()))
                    .filter(x -> !x.getPoint().equals(src.get().getPoint()))
                    .findFirst()
                    .orElse(null));
            if (keima.isPresent())
            {
                str += "";
            } else if ((src.get().getPoint().x - keima.get().getPoint().x) * trans > 0) {
                str += "右";
            } else {
                str += "左";
            }
        }
        if (src_type == PieceBase.GIN) {
            List<PieceBase> gins = new ArrayList<>(
                    Arrays.asList(
                            attacker.getPiecesOnBoard(src_type)
                                    .filter(x -> x.getCapablePutPoint(attacker, defender) == dst.get().getPoint())
                                    .filter(x -> !x.getPoint().equals(src.get().getPoint()))
                                    .toArray(PieceBase[]::new)
                    )
            );
            boolean there_were_gin_on_col = gins.stream().anyMatch(g -> g.getPoint().y - src.get().getPoint().y == 0);
            boolean top = dst.get().getPoint().x - src.get().getPoint().x == 0
                    && (dst.get().getPoint().y - src.get().getPoint().y) * trans == 1;
            boolean right_top = (dst.get().getPoint().x - src.get().getPoint().x) * trans == 1
                    && (dst.get().getPoint().y - src.get().getPoint().y) * trans == 1;
            boolean left_top = (dst.get().getPoint().x - src.get().getPoint().x) * trans == -1
                    && (dst.get().getPoint().y - src.get().getPoint().y) * trans == 1;
            boolean right_bottom = (dst.get().getPoint().x - src.get().getPoint().x) * trans == 1
                    && (dst.get().getPoint().y - src.get().getPoint().y) * trans == -1;
            boolean left_bottom = (dst.get().getPoint().x - src.get().getPoint().x) * trans == -1
                    && (dst.get().getPoint().y - src.get().getPoint().y) * trans == 1;
            if (gins.stream().count() == 0) {
                str += "";
            } else if (top) {
                if (there_were_gin_on_col) str += "直";
                else str += "上";
            } else if (right_top) {
                boolean there_were_gin_in_front_of =
                        gins.stream().anyMatch(g -> (g.getPoint().y - src.get().getPoint().y) * trans == 2);
                if (!there_were_gin_on_col) str += "上";
                else if (there_were_gin_in_front_of) str += "左上";
                else str += "左";
            } else if (left_top) {
                boolean there_were_gin_in_front_of =
                        gins.stream().anyMatch(g -> (g.getPoint().y - src.get().getPoint().y) * trans == 2);
                if (!there_were_gin_on_col) str += "上";
                else if (there_were_gin_in_front_of) str += "右上";
                else str += "右";
            } else if (right_bottom) {
                boolean there_were_gin_behind =
                        gins.stream().anyMatch(g -> (g.getPoint().y - src.get().getPoint().y) * trans == -2);
                if (!there_were_gin_on_col) str += "引";
                else if (there_were_gin_behind) str += "左引";
                else str += "左";
            } else if (left_bottom) {
                boolean there_were_gin_behind =
                        gins.stream().anyMatch(g -> (g.getPoint().y - src.get().getPoint().y) * trans == -2);
                if (!there_were_gin_on_col) str += "引";
                else if (there_were_gin_behind) str += "右引";
                else str += "右";
            }
            // TODO impl KIN(NARI*), KAKU, HISHA, UMA and RYU
        }
        // 成　または　不成
        // srcが盤上にあり、成駒できるとき
        if (src.get().canPromote(dst.get().getPoint(), isAheadTurn()) && src.get().isOnBoard()) {
            if (dst.get().getTypeOfPiece() == src_type) {
                str += "不成";
            } else {
                str += "成";
            }
        }
        System.out.println("info: " + str);
        return str;
    }
    protected boolean isSamePosition(MovementOfPiece now, MovementOfPiece prev)
    {
        // dst が null になることはない
        return prev != null && prev.getDst().get().getPoint().equals(now.getDst().get().getPoint());
    }
    public boolean isAheadTurn() {
        return movedNum % 2 == 0;
    }
    public Integer getMovedNum() {
        return movedNum;
    }
    private String getChineseNum(int num) {
        switch (num) {
            case 1: return "一";
            case 2: return "二";
            case 3: return "三";
            case 4: return "四";
            case 5: return "五";
            case 6: return "六";
            case 7: return "七";
            case 8: return "八";
            case 9: return "九";
            default: return "";
        }
    }
    public Boolean isSennnichite() {
        if (hashList.isEmpty()) return false;
        String hex = hashList.get(hashList.size()-1);
        return hashList.stream().filter(str -> str.equals(hex)).count() > 3;
    }
}
