package com.github.cive.core.shogi;

import com.github.cive.core.shogi.Pieces.PieceBase;
import com.github.cive.core.shogi.Pieces.PieceFactory;
import org.apache.commons.codec.binary.Hex;

import java.util.Optional;

/**
 * Created by yotuba on 16/05/20.
 * 盤面のデータ構造
 */
public class MovementOfPiece {
    public MovementOfPiece(Optional<PieceBase> src, Optional<PieceBase> dst, String HexHash) {
        this.dst = dst;
        this.src = src;
        this.HexHash = HexHash;
    }
    private Optional<PieceBase> src;
    private Optional<PieceBase> dst;
    private String HexHash;

    /**
     * set the destination piece
     * @param dst destination piece
     */
    public void setDst(Optional<PieceBase> dst)
    {
        this.dst = dst.isPresent() ?
                PieceFactory.create(dst.get().getTypeOfPiece(), dst.get().getPosition()) :
                Optional.empty();
    }
    /**
     * set the source piece
     * @param src source piece
     */
    public void setSrc(Optional<PieceBase> src)
    {
        this.src = src.isPresent() ?
                PieceFactory.create(src.get().getTypeOfPiece(), src.get().getPosition()) :
                Optional.empty();
    }

    /**
     * set the screen hash
     * @param HexHash screen hash
     */
    public void setHexHash(String HexHash)
    {
        this.HexHash = HexHash;
    }

    /**
     * get the destination piece.
     * @return destination piece
     */
    public Optional<PieceBase> getDst()
    {
        Optional<PieceBase> dst = this.dst.isPresent() ?
                PieceFactory.create(this.dst.get().getTypeOfPiece(), this.dst.get().getPosition()) :
                Optional.empty();
        return dst;
    }
    /**
     * get the source piece.
     * @return source piece
     */
    public Optional<PieceBase> getSrc()
    {
        Optional<PieceBase> src = this.src.isPresent() ?
                PieceFactory.create(this.src.get().getTypeOfPiece(), this.src.get().getPosition()) :
                Optional.empty();
        return src;
    }

    /**
     * get the screen hash
     * @return screen hash
     */
    public String getHexHash() {
        return HexHash;
    }

    @Override
    public String toString() {
        return src.toString() + "->" + dst.toString();
    }
}