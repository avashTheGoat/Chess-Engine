package com.chess.engine;

import java.util.Scanner;

import org.apache.commons.lang3.mutable.MutableInt;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;

public class App
{
    public static void main(String[] args)
    {
        // pawn endgame w/ pawn about to promote
        // BenchmarkEngine("6k1/5p2/6p1/8/8/8/p5PP/6K1 w - - 0 0", 5, false, true, true, true);

        // Board _board = new Board();
        // _board.loadFromFen("3k4/2R5/1Q5K/8/8/8/8/8 w - - 0 1");

        // _board.doMove(new Move("b6b8", Side.WHITE));

        // System.out.println(_board.toString());
    }
    
    private static void BenchmarkEngine(String _fen, int _maxPlies, boolean _shouldRunIntermediatePlies,
    boolean _shouldBenchmarkNoOptimizations, boolean _shouldBencharmkOnlyAlphaBeta,
    boolean _shouldBencharmkAlphaBetaAndMoveSorting)
    {
        Board _board = new Board();
        _board.loadFromFen(_fen);
        ChessEngine _engine = new ChessEngine(_board);

        double _startTimeNanoseconds;
        double _timeTakenSeconds;
        MutableInt _numPositionsEvaluated;

        System.out.println(_board.toString());
        System.out.println();

        System.out.println("Ply 0 (short-sighted evaluation): ");
        System.out.println(_engine.Evaluate(false));

        System.out.println();
        
        if (_shouldBenchmarkNoOptimizations)
        {
            //#region Without alpha beta pruning
            System.out.println("NO ALPHA-BETA PRUNING");
            System.out.println("_______________________________");

            System.out.println();

            if (_shouldRunIntermediatePlies)
            {
                for (int _numPlies = 1; _numPlies <= _maxPlies; _numPlies++)
                {
                    System.out.println("Ply " + _maxPlies + ": ");
        
                    _numPositionsEvaluated = new MutableInt(0);
            
                    _startTimeNanoseconds = System.nanoTime();
                    System.out.println(_engine.FindBestMove(_numPlies, false, false, _numPositionsEvaluated));
                    _timeTakenSeconds = (System.nanoTime() - _startTimeNanoseconds) / 1000000000;
                    
                    System.out.println("Num positions evaluated: " + _numPositionsEvaluated.intValue());
                    System.out.println("Time taken (seconds): " + _timeTakenSeconds);
            
                    System.out.println();
                }
            }

            else
            {
                System.out.println("Ply " + _maxPlies + ": ");
        
                _numPositionsEvaluated = new MutableInt(0);
        
                _startTimeNanoseconds = System.nanoTime();
                System.out.println(_engine.FindBestMove(_maxPlies, false, false, _numPositionsEvaluated));
                _timeTakenSeconds = (System.nanoTime() - _startTimeNanoseconds) / 1000000000;
                
                System.out.println("Num positions evaluated: " + _numPositionsEvaluated.intValue());
                System.out.println("Time taken (seconds): " + _timeTakenSeconds);
        
                System.out.println();
            }
            //#endregion

            System.out.println();
            System.out.println();
            System.out.println();
        }

        if (_shouldBencharmkOnlyAlphaBeta)
        {
            //#region With alpha beta pruning
            System.out.println("ALPHA-BETA PRUNING: ");
            System.out.println("______________________________________");

            System.out.println();

            if (_shouldRunIntermediatePlies)
            {
                for (int _numPlies = 1; _numPlies <= _maxPlies; _numPlies++)
                {
                    
                    System.out.printf("Ply %d: \n", _numPlies);
        
                    _numPositionsEvaluated = new MutableInt(0);
            
                    _startTimeNanoseconds = System.nanoTime();
                    System.out.println(_engine.FindBestMove(_numPlies, true, false, _numPositionsEvaluated));
                    _timeTakenSeconds = (System.nanoTime() - _startTimeNanoseconds) / 1000000000;
                    
                    System.out.println("Num positions evaluated: " + _numPositionsEvaluated.intValue());
                    System.out.println("Time taken (seconds): " + _timeTakenSeconds);
            
                    System.out.println();
                }
            }

            else
            {
                System.out.println("Ply " + _maxPlies + ": ");
        
                _numPositionsEvaluated = new MutableInt(0);
        
                _startTimeNanoseconds = System.nanoTime();
                System.out.println(_engine.FindBestMove(_maxPlies, true, false, _numPositionsEvaluated));
                _timeTakenSeconds = (System.nanoTime() - _startTimeNanoseconds) / 1000000000;
                
                System.out.println("Num positions evaluated: " + _numPositionsEvaluated.intValue());
                System.out.println("Time taken (seconds): " + _timeTakenSeconds);
        
                System.out.println();
            }
            //#endregion
            
            System.out.println();
            System.out.println();
            System.out.println();
        }

        if (_shouldBencharmkAlphaBetaAndMoveSorting)
        {
            //#region With alpha beta pruning and move ordering
            System.out.println("ALPHA-BETA PRUNING AND HEURISTIC MOVE ORDERING: ");
            System.out.println("______________________________________");

            System.out.println();

            if (_shouldRunIntermediatePlies)
            {
                for (int _numPlies = 1; _numPlies <= _maxPlies; _numPlies++)
                {
                    
                    System.out.println("Ply " + _maxPlies + ": ");
        
                    _numPositionsEvaluated = new MutableInt(0);
            
                    _startTimeNanoseconds = System.nanoTime();
                    System.out.println(_engine.FindBestMove(_numPlies, true, true, _numPositionsEvaluated));
                    _timeTakenSeconds = (System.nanoTime() - _startTimeNanoseconds) / 1000000000;
                    
                    System.out.println("Num positions evaluated: " + _numPositionsEvaluated.intValue());
                    System.out.println("Time taken (seconds): " + _timeTakenSeconds);
            
                    System.out.println();
                }
            }

            else
            {
                System.out.println("Ply " + _maxPlies + ": ");
        
                _numPositionsEvaluated = new MutableInt(0);
        
                _startTimeNanoseconds = System.nanoTime();
                System.out.println(_engine.FindBestMove(_maxPlies, true, true, _numPositionsEvaluated));
                _timeTakenSeconds = (System.nanoTime() - _startTimeNanoseconds) / 1000000000;
                
                System.out.println("Num positions evaluated: " + _numPositionsEvaluated.intValue());
                System.out.println("Time taken (seconds): " + _timeTakenSeconds);
        
                System.out.println();
            }
            //#endregion
        }
    }

    private static void PlayGame(Side _playerSide)
    {
        Scanner _input = new Scanner(System.in);
        ChessEngine _engine = new ChessEngine(new Board());

        while (!_engine.getBoard().isDraw() || !_engine.getBoard().isMated())
        {
            if (_playerSide == Side.WHITE)
            {
                System.out.println(_engine.getBoard().toString());

                System.out.println("Enter move: ");

                Move _playerMove = new Move(_input.nextLine(), Side.WHITE);
                _engine.getBoard().doMove(_playerMove);

                Move _engineMove = _engine.FindBestMove
                (
                    4, true, true,
                    null
                ).getMove();
                _engine.getBoard().doMove(_engineMove);

                System.out.println("Engine move: " + _engineMove);
                System.out.println();
            }

            else
            {
                Move _engineMove = _engine.FindBestMove
                (
                    4, true, true,
                    null
                ).getMove();
                _engine.getBoard().doMove(_engineMove);

                System.out.println("Engine move: " + _engineMove);
                System.out.println();

                System.out.println(_engine.getBoard().toString());

                System.out.println();
                
                System.out.println("Enter move: ");

                Move _playerMove = new Move(_input.nextLine(), Side.WHITE);
                _engine.getBoard().doMove(_playerMove);
            }
        }
    }

    private static void PlayGame(Side _playerSide, String _initialFen)
    {
        Scanner _input = new Scanner(System.in);
        Board _board = new Board();
        _board.loadFromFen(_initialFen);
        ChessEngine _engine = new ChessEngine(_board);

        while (!_engine.getBoard().isDraw() || !_engine.getBoard().isMated())
        {
            if (_playerSide == Side.WHITE)
            {
                if (_board.getSideToMove() == Side.WHITE)
                {
                    System.out.println(_engine.getBoard().toString());
    
                    System.out.println("Enter move: ");
    
                    Move _playerMove = new Move(_input.nextLine(), Side.WHITE);

                    _engine.getBoard().doMove(_playerMove);
    
                    ScoredMove _engineMove = _engine.FindBestMove
                    (
                        4, true, true,
                        null
                    );

                    // if engine (black) is mated
                    if (_engineMove.getScore() == Float.MAX_VALUE) break;

                    _engine.getBoard().doMove(_engineMove.getMove());
    
                    System.out.println("Engine move: " + _engineMove);
                    System.out.println();
                }

                else
                {
                    ScoredMove _engineMove = _engine.FindBestMove
                    (
                        4, true, true,
                        null
                    );

                    // if black (white) is mated
                    if (_engineMove.getScore() == -Float.MAX_VALUE) break;

                    _engine.getBoard().doMove(_engineMove.getMove());
    
                    System.out.println("Engine move: " + _engineMove);
                    System.out.println();

                    System.out.println(_engine.getBoard().toString());
    
                    System.out.println("Enter move: ");
    
                    Move _playerMove = new Move(_input.nextLine(), Side.WHITE);
                    _engine.getBoard().doMove(_playerMove);
                }
            }

            else
            {
                if (_board.getSideToMove() == Side.BLACK)
                {
                    System.out.println("Enter move: ");
    
                    Move _playerMove = new Move(_input.nextLine(), Side.WHITE);
                    _engine.getBoard().doMove(_playerMove);

                    Move _engineMove = _engine.FindBestMove
                    (
                        4, true, true,
                        null
                    ).getMove();
                    _engine.getBoard().doMove(_engineMove);
    
                    System.out.println("Engine move: " + _engineMove);
                    System.out.println();
    
                    System.out.println(_engine.getBoard().toString());
    
                    System.out.println();
                }

                else
                {
                    Move _engineMove = _engine.FindBestMove
                    (
                        4, true, true,
                        null
                    ).getMove();
                    _engine.getBoard().doMove(_engineMove);
    
                    System.out.println("Engine move: " + _engineMove);
                    System.out.println();
    
                    System.out.println(_engine.getBoard().toString());
                    System.out.println();

                    System.out.println("Enter move: ");
    
                    Move _playerMove = new Move(_input.nextLine(), Side.WHITE);
                    _engine.getBoard().doMove(_playerMove);

                    System.out.println();
                }
            }
        }
    
        System.out.println();
        System.out.println(_engine.getBoard().toString());
        System.out.println();
        System.out.println("The game ended");
    }
}