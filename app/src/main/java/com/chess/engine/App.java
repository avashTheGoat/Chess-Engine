package com.chess.engine;

import java.util.Scanner;

import org.apache.commons.lang3.mutable.MutableInt;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;

public class App
{
    public static void main(String[] args)
    {
        PlayGame(Side.BLACK, 4);
    }
    
    private static void BenchmarkEngine(String _fen, int _maxPlies, boolean _shouldRunIntermediatePlies,
    boolean _shouldBenchmarkNoOptimizations, boolean _shouldBencharmkOnlyAlphaBeta,
    boolean _shouldBencharmkAlphaBetaAndMoveSorting, boolean _shouldBencharmkAlphaBetaMoveSortingAndQuiescence)
    {
        Board _board = new Board();
        _board.loadFromFen(_fen);
        System.out.println("Hash of position: " + _board.getZobristKey());
        ChessEngine _engine = new ChessEngine(_board);

        double _startTimeNanoseconds;
        double _timeTakenSeconds;
        MutableInt _numPositionsEvaluated;

        System.out.println(_board.toString());
        System.out.println();

        // System.out.println("Ply 0 (short-sighted evaluation): ");
        // System.out.println(_engine.Evaluate(true));

        System.out.println();

        int _startingPly = _shouldRunIntermediatePlies ? 1 : _maxPlies;
        
        if (_shouldBenchmarkNoOptimizations)
        {
            System.out.println("NO ALPHA-BETA PRUNING");
            System.out.println("_______________________________");

            System.out.println();

            for (int _numPlies = _startingPly; _numPlies <= _maxPlies; _numPlies++)
            {
                System.out.println("Ply " + _maxPlies + ": ");
    
                _numPositionsEvaluated = new MutableInt(0);
        
                _startTimeNanoseconds = System.nanoTime();
                System.out.println(_engine.FindBestMove(_numPlies, false, false, false, _numPositionsEvaluated));
                _timeTakenSeconds = (System.nanoTime() - _startTimeNanoseconds) / 1000000000;
                
                System.out.println("Num positions evaluated: " + _numPositionsEvaluated.intValue());
                System.out.println("Time taken (seconds): " + _timeTakenSeconds);
        
                System.out.println();
            }

            System.out.println();
            System.out.println();
            System.out.println();
        }

        if (_shouldBencharmkOnlyAlphaBeta)
        {
            System.out.println("ALPHA-BETA PRUNING: ");
            System.out.println("______________________________________");

            System.out.println();

            for (int _numPlies = _startingPly; _numPlies <= _maxPlies; _numPlies++)
            {
                
                System.out.printf("Ply %d: \n", _numPlies);
    
                _numPositionsEvaluated = new MutableInt(0);
        
                _startTimeNanoseconds = System.nanoTime();
                System.out.println(_engine.FindBestMove(_numPlies, true, false, false, _numPositionsEvaluated));
                _timeTakenSeconds = (System.nanoTime() - _startTimeNanoseconds) / 1000000000;
                
                System.out.println("Num positions evaluated: " + _numPositionsEvaluated.intValue());
                System.out.println("Time taken (seconds): " + _timeTakenSeconds);
        
                System.out.println();
            }
            
            System.out.println();
            System.out.println();
            System.out.println();
        }

        if (_shouldBencharmkAlphaBetaAndMoveSorting)
        {
            System.out.println("ALPHA-BETA PRUNING AND HEURISTIC MOVE ORDERING: ");
            System.out.println("______________________________________");

            System.out.println();

            for (int _numPlies = _startingPly; _numPlies <= _maxPlies; _numPlies++)
            {
                
                System.out.println("Ply " + _maxPlies + ": ");
    
                _numPositionsEvaluated = new MutableInt(0);
        
                _startTimeNanoseconds = System.nanoTime();
                System.out.println(_engine.FindBestMove(_numPlies, true, true, false, _numPositionsEvaluated));
                _timeTakenSeconds = (System.nanoTime() - _startTimeNanoseconds) / 1000000000;
                
                System.out.println("Num positions evaluated: " + _numPositionsEvaluated.intValue());
                System.out.println("Time taken (seconds): " + _timeTakenSeconds);
        
                System.out.println();
            }

            System.out.println();
            System.out.println();
            System.out.println();
        }
    
        if (_shouldBencharmkAlphaBetaMoveSortingAndQuiescence)
        {
            System.out.println("ALPHA-BETA PRUNING, HEURISTIC MOVE ORDERING, AND QUIESCENCE: ");
            System.out.println("______________________________________");

            System.out.println();

            for (int _numPlies = _startingPly; _numPlies <= _maxPlies; _numPlies++)
            {
                
                System.out.println("Ply " + _maxPlies + ": ");
    
                _numPositionsEvaluated = new MutableInt(0);
        
                _startTimeNanoseconds = System.nanoTime();
                System.out.println(_engine.FindBestMove(_numPlies, true, true, true, _numPositionsEvaluated));
                _timeTakenSeconds = (System.nanoTime() - _startTimeNanoseconds) / 1000000000;
                
                System.out.println("Num positions evaluated: " + _numPositionsEvaluated.intValue());
                System.out.println("Time taken (seconds): " + _timeTakenSeconds);
        
                System.out.println();
            }
        }
    }

    private static void PlayGame(Side _playerSide, int _ply)
    {
        PlayGame(_playerSide, "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", _ply);
    }

    private static void PlayGame(Side _playerSide, String _initialFen, int _ply)
    {
        Scanner _input = new Scanner(System.in);
        Board _board = new Board();
        _board.loadFromFen(_initialFen);
        ChessEngine _engine = new ChessEngine(_board);

        while (!_engine.getBoard().isDraw() || !_engine.getBoard().isMated())
        {
            System.out.println("Immediate eval: " + _engine.Evaluate(false));
            if (_playerSide == Side.WHITE)
            {
                if (_board.getSideToMove() == Side.WHITE)
                {
                    System.out.println(_engine.getBoard().toString());
    
                    System.out.println("Enter move: ");
    
                    Move _playerMove = new Move(_input.nextLine(), Side.WHITE);
                    _engine.getBoard().doMove(_playerMove);
    
                    ScoredMove _engineMove = _engine.FindBestMove(_ply, true, true, true, null);

                    if (_engineMove.getMove().equals(new Move(Square.NONE, Square.NONE))) break;

                    if (!_engine.getBoard().isMoveLegal(_engineMove.getMove(), true)) break;

                    _engine.getBoard().doMove(_engineMove.getMove());
    
                    System.out.println("Engine move: " + _engineMove.getMove());
                    System.out.println();
                }

                else
                {
                    ScoredMove _engineMove = _engine.FindBestMove(_ply, true, true, true, null);

                    // if black (white) is mated
                    if (_engineMove.getScore() == -Float.MAX_VALUE) break;

                    _engine.getBoard().doMove(_engineMove.getMove());

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

                    ScoredMove _engineMove = _engine.FindBestMove(_ply, true, true, true, null);

                    if (_engineMove.getMove().equals(new Move(Square.NONE, Square.NONE))) break;

                    if (!_engine.getBoard().isMoveLegal(_engineMove.getMove(), true)) break;

                    _engine.getBoard().doMove(_engineMove.getMove());
    
                    System.out.println("Engine move: " + _engineMove.getMove());
                    System.out.println();
    
                    System.out.println(_engine.getBoard().toString());
    
                    System.out.println();
                }

                else
                {
                    ScoredMove _engineMove = _engine.FindBestMove(_ply, true, true, true, null);
                    _engine.getBoard().doMove(_engineMove.getMove());
    
                    System.out.println("Engine move: " + _engineMove.getMove());
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