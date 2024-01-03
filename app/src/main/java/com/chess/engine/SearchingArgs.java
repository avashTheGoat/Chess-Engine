package com.chess.engine;

import org.apache.commons.lang3.mutable.MutableInt;

import com.github.bhlangonijr.chesslib.Board;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SearchingArgs 
{
    private Board board;
    private int numPlies;
    private boolean shouldUseAlphaBetaPruning;
    private boolean shouldUseHeuristicMoveOrdering;
    private MutableInt numMovesEvaluatedReciever;
}