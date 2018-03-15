package com.github.cive.core.shogi.Pieces;

import java.util.Optional;
import java.awt.*;

/**
 * Created by yotuba on 16/05/05.
 * PieceBase Factory class
 */
public class PieceFactory implements Constant {
    public static Optional<PieceBase> create(int pieceId, Point place) {
        switch(pieceId) {
            case FU: return Optional.of(new Fu(place));
            case KYOSHA: return Optional.of(new Kyosha(place));
            case KEIMA: return Optional.of(new Keima(place));
            case GIN: return Optional.of(new Gin(place));
            case KIN: return Optional.of(new Kin(place));
            case KAKU: return Optional.of(new Kaku(place));
            case HISHA: return Optional.of(new Hisha(place));
            case GYOKU: return Optional.of(new Gyoku(place));
            case UMA: return Optional.of(new Uma(place));
            case RYU: return Optional.of(new Ryu(place));
            default: return Optional.empty();
        }
    }
    public static Optional<PieceBase> createPromoted(int pieceId, Point place) {
        if (pieceId == FU) {
            return Optional.of(new Tokin(place));
        } else if (pieceId == KYOSHA) {
            return Optional.of(new Narikyo(place));
        } else if (pieceId == KEIMA) {
            return Optional.of(new Narikei(place));
        } else if (pieceId == GIN) {
            return Optional.of(new Narigin(place));
        } else if (pieceId == KAKU) {
            return Optional.of(new Uma(place));
        } else if (pieceId == HISHA) {
            return Optional.of(new Ryu(place));
        } else {
            return Optional.empty();
        }
    }
    public static Optional<PieceBase> createDemoted(int pieceId, Point place) {
        if (pieceId == TOKIN) {
            return Optional.of(new Fu(place));
        } else if (pieceId == NARIKYO) {
            return Optional.of(new Kyosha(place));
        } else if (pieceId == NARIKEI) {
            return Optional.of(new Keima(place));
        } else if (pieceId == NARIGIN) {
            return Optional.of(new Gin(place));
        } else if (pieceId == UMA) {
            return Optional.of(new Kaku(place));
        } else if (pieceId == RYU) {
            return Optional.of(new Hisha(place));
        } else {
            return Optional.empty();
        }
    }
}
