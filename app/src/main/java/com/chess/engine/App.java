package com.chess.engine;

import com.github.bhlangonijr.chesslib.Board;

import org.apache.commons.lang3.mutable.MutableInt;

public class App
{
    public static void main(String[] args)
    {
        Board _testingBoard = new Board();
        ChessEngine _engine = new ChessEngine(_testingBoard);

        double _startTimeNanoseconds;
        double _timeTakenSeconds;

        //#region Board 1 (hanging queen protected by pawn)
        _testingBoard.loadFromFen("rnb1kbnr/pppp1pp1/4p2p/6q1/4P3/5N1P/PPPP1PP1/RNBQKB1R w KQkq - 0 1");
        System.out.println(_testingBoard.toString());
        System.out.println();

        System.out.println("Ply 0 (short-sighted evaluation): ");
        System.out.println(_engine.Evaluate());
        
        //#region Without alpha beta pruning
        System.out.println("NO ALPHA-BETA PRUNING");
        System.out.println("_______________________________");

        MutableInt _numMovesEvaluated = new MutableInt(0);
        System.out.println("Ply 1: ");

        _startTimeNanoseconds = System.nanoTime();
        System.out.println(_engine.FindBestMove(new SearchingArgs(_testingBoard, 1, false, false, _numMovesEvaluated)));
        _timeTakenSeconds = (System.nanoTime() - _startTimeNanoseconds) / 1000000000;
        
        System.out.println("Num moves evaluated: " + _numMovesEvaluated.intValue());
        System.out.println("Time taken (seconds): " + _timeTakenSeconds);

        _numMovesEvaluated = new MutableInt(0);
        System.out.println("Ply 2: ");

        _startTimeNanoseconds = System.nanoTime();
        System.out.println(_engine.FindBestMove(new SearchingArgs(_testingBoard, 2, false, false, _numMovesEvaluated)));
        _timeTakenSeconds = (System.nanoTime() - _startTimeNanoseconds) / 1000000000;

        System.out.println("Num moves evaluated: " + _numMovesEvaluated.intValue());
        System.out.println("Time taken (seconds): " + _timeTakenSeconds);

        _numMovesEvaluated = new MutableInt(0);
        System.out.println("Ply 3: ");

        _startTimeNanoseconds = System.nanoTime();
        System.out.println(_engine.FindBestMove(new SearchingArgs(_testingBoard, 3, false, false, _numMovesEvaluated)));
        _timeTakenSeconds = (System.nanoTime() - _startTimeNanoseconds) / 1000000000;
        
        System.out.println("Num moves evaluated: " + _numMovesEvaluated.intValue());
        System.out.println("Time taken (seconds): " + _timeTakenSeconds);
        //#endregion

        System.out.println();
        System.out.println();
        System.out.println();

        //#region With alpha beta pruning
        System.out.println("ALPHA-BETA PRUNING: ");
        System.out.println("______________________________________");

        _numMovesEvaluated = new MutableInt(0);
        System.out.println("Ply 1: ");

        _startTimeNanoseconds = System.nanoTime();
        System.out.println(_engine.FindBestMove(new SearchingArgs(_testingBoard, 1, true, false, _numMovesEvaluated)));
        _timeTakenSeconds = (System.nanoTime() - _startTimeNanoseconds) / 1000000000;
        
        System.out.println("Num moves evaluated: " + _numMovesEvaluated.intValue());
        System.out.println("Time taken: " + _timeTakenSeconds);

        _numMovesEvaluated = new MutableInt(0);
        System.out.println("Ply 2: ");

        _startTimeNanoseconds = System.nanoTime();
        System.out.println(_engine.FindBestMove(new SearchingArgs(_testingBoard, 2, true, false, _numMovesEvaluated)));
        _timeTakenSeconds = (System.nanoTime() - _startTimeNanoseconds) / 1000000000;
        
        System.out.println("Num moves evaluated: " + _numMovesEvaluated.intValue());
        System.out.println("Time taken: " + _timeTakenSeconds);

        _numMovesEvaluated = new MutableInt(0);
        System.out.println("Ply 3: ");

        _startTimeNanoseconds = System.nanoTime();
        System.out.println(_engine.FindBestMove(new SearchingArgs(_testingBoard, 3, true, false, _numMovesEvaluated)));
        _timeTakenSeconds = (System.nanoTime() - _startTimeNanoseconds) / 1000000000;
        
        System.out.println("Num moves evaluated: " + _numMovesEvaluated.intValue());
        System.out.println("Time taken: " + _timeTakenSeconds);
        //#endregion
        
        System.out.println();
        System.out.println();
        System.out.println();

        //#region With alpha beta pruning and move ordering
        System.out.println("ALPHA-BETA PRUNING AND HEURISTIC MOVE ORDERING: ");
        System.out.println("______________________________________");

        _numMovesEvaluated = new MutableInt(0);
        System.out.println("Ply 1: ");

        _startTimeNanoseconds = System.nanoTime();
        System.out.println(_engine.FindBestMove(new SearchingArgs(_testingBoard, 1, true, true, _numMovesEvaluated)));
        _timeTakenSeconds = (System.nanoTime() - _startTimeNanoseconds) / 1000000000;
        
        System.out.println("Num moves evaluated: " + _numMovesEvaluated.intValue());
        System.out.println("Time taken: " + _timeTakenSeconds);

        _numMovesEvaluated = new MutableInt(0);
        System.out.println("Ply 2: ");

        _startTimeNanoseconds = System.nanoTime();
        System.out.println(_engine.FindBestMove(new SearchingArgs(_testingBoard, 2, true, true, _numMovesEvaluated)));
        _timeTakenSeconds = (System.nanoTime() - _startTimeNanoseconds) / 1000000000;
        
        System.out.println("Num moves evaluated: " + _numMovesEvaluated.intValue());
        System.out.println("Time taken: " + _timeTakenSeconds);

        _numMovesEvaluated = new MutableInt(0);
        System.out.println("Ply 3: ");

        _startTimeNanoseconds = System.nanoTime();
        System.out.println(_engine.FindBestMove(new SearchingArgs(_testingBoard, 3, true, true, _numMovesEvaluated)));
        _timeTakenSeconds = (System.nanoTime() - _startTimeNanoseconds) / 1000000000;
        
        System.out.println("Num moves evaluated: " + _numMovesEvaluated.intValue());
        System.out.println("Time taken: " + _timeTakenSeconds);
        //#endregion
        //#endregion
    
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();

        //#region Board 2 (mate in two for black)
        _testingBoard.loadFromFen("r1bq2r1/b4pk1/p1pp1p2/1p2pP2/1P2P1PB/3P4/1PPQ2P1/R3K2R w");
        System.out.println(_testingBoard.toString());
        System.out.println();

        System.out.println("Ply 0 (short-sighted evaluation): ");
        System.out.println(_engine.Evaluate());
        
        //#region Without alpha beta pruning
        System.out.println("NO ALPHA-BETA PRUNING");
        System.out.println("_______________________________");

        _numMovesEvaluated = new MutableInt(0);
        System.out.println("Ply 1: ");

        _startTimeNanoseconds = System.nanoTime();
        System.out.println(_engine.FindBestMove(new SearchingArgs(_testingBoard, 1, false, false, _numMovesEvaluated)));
        _timeTakenSeconds = (System.nanoTime() - _startTimeNanoseconds) / 1000000000;
        
        System.out.println("Num moves evaluated: " + _numMovesEvaluated.intValue());
        System.out.println("Time taken (seconds): " + _timeTakenSeconds);

        _numMovesEvaluated = new MutableInt(0);
        System.out.println("Ply 2: ");

        _startTimeNanoseconds = System.nanoTime();
        System.out.println(_engine.FindBestMove(new SearchingArgs(_testingBoard, 2, false, false, _numMovesEvaluated)));
        _timeTakenSeconds = (System.nanoTime() - _startTimeNanoseconds) / 1000000000;

        System.out.println("Num moves evaluated: " + _numMovesEvaluated.intValue());
        System.out.println("Time taken (seconds): " + _timeTakenSeconds);

        _numMovesEvaluated = new MutableInt(0);
        System.out.println("Ply 3: ");

        _startTimeNanoseconds = System.nanoTime();
        System.out.println(_engine.FindBestMove(new SearchingArgs(_testingBoard, 3, false, false, _numMovesEvaluated)));
        _timeTakenSeconds = (System.nanoTime() - _startTimeNanoseconds) / 1000000000;
        
        System.out.println("Num moves evaluated: " + _numMovesEvaluated.intValue());
        System.out.println("Time taken (seconds): " + _timeTakenSeconds);
        //#endregion

        //#region With alpha beta pruning
        System.out.println("ALPHA-BETA PRUNING: ");
        System.out.println("______________________________________");

        _numMovesEvaluated = new MutableInt(0);
        System.out.println("Ply 1: ");

        _startTimeNanoseconds = System.nanoTime();
        System.out.println(_engine.FindBestMove(new SearchingArgs(_testingBoard, 1, true, false, _numMovesEvaluated)));
        _timeTakenSeconds = (System.nanoTime() - _startTimeNanoseconds) / 1000000000;
        
        System.out.println("Num moves evaluated: " + _numMovesEvaluated.intValue());
        System.out.println("Time taken: " + _timeTakenSeconds);

        _numMovesEvaluated = new MutableInt(0);
        System.out.println("Ply 2: ");

        _startTimeNanoseconds = System.nanoTime();
        System.out.println(_engine.FindBestMove(new SearchingArgs(_testingBoard, 2, true, false, _numMovesEvaluated)));
        _timeTakenSeconds = (System.nanoTime() - _startTimeNanoseconds) / 1000000000;
        
        System.out.println("Num moves evaluated: " + _numMovesEvaluated.intValue());
        System.out.println("Time taken: " + _timeTakenSeconds);

        _numMovesEvaluated = new MutableInt(0);
        System.out.println("Ply 3: ");

        _startTimeNanoseconds = System.nanoTime();
        System.out.println(_engine.FindBestMove(new SearchingArgs(_testingBoard, 3, true, false, _numMovesEvaluated)));
        _timeTakenSeconds = (System.nanoTime() - _startTimeNanoseconds) / 1000000000;
        
        System.out.println("Num moves evaluated: " + _numMovesEvaluated.intValue());
        System.out.println("Time taken: " + _timeTakenSeconds);
        //#endregion
        
        //#region With alpha beta pruning and move ordering
        System.out.println("ALPHA-BETA PRUNING AND HEURISTIC MOVE ORDERING: ");
        System.out.println("______________________________________");

        _numMovesEvaluated = new MutableInt(0);
        System.out.println("Ply 1: ");

        _startTimeNanoseconds = System.nanoTime();
        System.out.println(_engine.FindBestMove(new SearchingArgs(_testingBoard, 1, true, true, _numMovesEvaluated)));
        _timeTakenSeconds = (System.nanoTime() - _startTimeNanoseconds) / 1000000000;
        
        System.out.println("Num moves evaluated: " + _numMovesEvaluated.intValue());
        System.out.println("Time taken: " + _timeTakenSeconds);

        _numMovesEvaluated = new MutableInt(0);
        System.out.println("Ply 2: ");

        _startTimeNanoseconds = System.nanoTime();
        System.out.println(_engine.FindBestMove(new SearchingArgs(_testingBoard, 2, true, true, _numMovesEvaluated)));
        _timeTakenSeconds = (System.nanoTime() - _startTimeNanoseconds) / 1000000000;
        
        System.out.println("Num moves evaluated: " + _numMovesEvaluated.intValue());
        System.out.println("Time taken: " + _timeTakenSeconds);

        _numMovesEvaluated = new MutableInt(0);
        System.out.println("Ply 3: ");

        _startTimeNanoseconds = System.nanoTime();
        System.out.println(_engine.FindBestMove(new SearchingArgs(_testingBoard, 3, true, true, _numMovesEvaluated)));
        _timeTakenSeconds = (System.nanoTime() - _startTimeNanoseconds) / 1000000000;
        
        System.out.println("Num moves evaluated: " + _numMovesEvaluated.intValue());
        System.out.println("Time taken: " + _timeTakenSeconds);
        //#endregion
        //#endregion
    }
}