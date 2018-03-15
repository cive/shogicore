package com.github.cive.core.shogi.Exceptions;

/**
 * Created by rrqkd_000 on 2016/05/28.
 * プレイヤーの玉が設定されていないときに投げる
 */
public class PlayerNotDefinedGyokuException extends Exception {
    public PlayerNotDefinedGyokuException(String str) {
        super(str);
    }
    public PlayerNotDefinedGyokuException() {
        super("玉が設置されていません");
    }
}
