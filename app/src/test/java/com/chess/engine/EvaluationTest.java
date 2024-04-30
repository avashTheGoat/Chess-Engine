package com.chess.engine;

import org.junit.jupiter.api.Test;

import com.github.bhlangonijr.chesslib.Board;

import static org.junit.jupiter.api.Assertions.*;

class EvaluationTest
{
    @Test
    public void TestCorrectEvaluationSign()
    {
        ChessEngine _engine = new ChessEngine(new Board());
        // white has only king
        _engine.getBoard().loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/8/4K3 w kq - 0 1");
        assertTrue(_engine.Evaluate(false) < 0);

        // black has only king
        _engine.getBoard().loadFromFen("4k3/8/8/8/8/8/PPPPPPPP/RNBQKBNR w KQ - 0 1");
        assertTrue(_engine.Evaluate(false) > 0);
    }

    @Test
    public void TestMaterialEvaluation()
    {
        ChessEngine _engine = new ChessEngine(new Board());
        float _startingMaterial = _engine.EvaluateMaterial(8, 2, 2, 2, 1, false);
        
        // one pawn less
        float _newMaterial = _engine.EvaluateMaterial(7, 2, 2, 2, 1, false);
        assertTrue(_startingMaterial > _newMaterial);

        // one knight less
        _newMaterial = _engine.EvaluateMaterial(8, 1, 2, 2, 1, false);
        assertTrue(_startingMaterial > _newMaterial);

        // one bishop less
        _newMaterial = _engine.EvaluateMaterial(7, 2, 1, 2, 1, false);
        assertTrue(_startingMaterial > _newMaterial);

        // one bishop and knight less
        assertTrue(_newMaterial > _engine.EvaluateMaterial(7, 1, 1, 2, 1, false));

        // one rook less
        _newMaterial = _engine.EvaluateMaterial(7, 2, 2, 1, 1, false);
        assertTrue(_startingMaterial > _newMaterial);

        // no queen
        _newMaterial = _engine.EvaluateMaterial(8, 2, 2, 2, 0, false);
        assertTrue(_startingMaterial > _newMaterial);
    }
}