package com.chess.engine;

import com.github.bhlangonijr.chesslib.move.Move;

import lombok.Data;

@Data
public class ScoredMove implements Cloneable
{
    private final Move move;
    private final float score;

    public ScoredMove clone()
    {
        return new ScoredMove(move, score);
    }
}