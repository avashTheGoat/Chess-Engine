package com.chess.engine;

import com.github.bhlangonijr.chesslib.move.Move;

import lombok.Data;

@Data
public class ScoredMove implements Cloneable, Comparable<ScoredMove>
{
    private final Move move;
    private final float score;

    /**
     * <STRONG>IMPORTANT NOTE:</STRONG> this function was built to optimize the
     * heuristic move sorting function in the com.chess.engine.ChessEngine class.
     * It will not give the expected results.
     * @param _comparingScoredMove the com.chess.engine.ScoredMove to compare to
     * @return the comparison result
     */
    @Override
    public int compareTo(ScoredMove _comparingScoredMove)
    {
        if (score > _comparingScoredMove.score)
            return -1;

        else if (score < _comparingScoredMove.score)
            return 1;

        return 0;
    }
}