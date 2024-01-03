package com.chess.engine;

import com.github.bhlangonijr.chesslib.move.Move;

import lombok.Data;

@Data
public class ScoredMove implements Cloneable, Comparable<ScoredMove>
{
    private final Move move;
    private final float score;

    public ScoredMove clone()
    {
        return new ScoredMove(move, score);
    }

    @Override
    public int compareTo(ScoredMove _comparingScoredMove)
    {
        if (score < _comparingScoredMove.score)
        {
            return -1;
        }

        else if (score > _comparingScoredMove.score)
        {
            return 1;
        }

        return 0;
    }
}