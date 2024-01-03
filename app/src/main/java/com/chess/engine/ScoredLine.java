package com.chess.engine;

import java.util.List;
import java.util.ArrayList;

import com.github.bhlangonijr.chesslib.move.Move;

import lombok.Data;

@Data
public class ScoredLine implements Cloneable, Comparable<ScoredLine>
{
    private final List<Move> line;
    private final float score;

    public ScoredLine clone()
    {
        return new ScoredLine(new ArrayList<>(line), score);
    }

    public List<Move> getLine()
    {
        return new ArrayList<>(line);
    }

    @Override
    public int compareTo(ScoredLine _comparingScoredLine)
    {
        if (score < _comparingScoredLine.score)
        {
            return -1;
        }

        else if (score > _comparingScoredLine.score)
        {
            return 1;
        }

        return 0;
    }
}