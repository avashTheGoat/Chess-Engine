package com.chess.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.mutable.MutableInt;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.File;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.PieceType;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;

import lombok.Data;
import lombok.Setter;

public class ChessEngine
{
    @Setter
    private Board board;

    private static final float SCORE_TOLERANCE = 0.01f;

    private final float DOUBLED_PAWN_PENALTY = 0.095f;
    private final float ISOLATED_PAWN_PENALTY = 0.105f;
    private final float DOUBLED_AND_ISOLATED_PENALTY = 0.2f;
    private final float BACKWARDS_PAWN_PENALTY = 0.075f;

    private final float PAWN_VALUE = 1f;
    private final float KNIGHT_VALUE = 2.85f;
    private final float BISHOP_VALUE = 3f;
    private final float ROOK_VALUE = 5f;
    private final float QUEEN_VALUE = 10f;

    public ChessEngine(Board _board)
    {
        board = _board;
    }

    //#region Searching
    /**
     * Finds the best move for the engine's Chess board using the Minimax algorithm.
     * @param _initialPerspective the side whose turn it is
     * @param _curPerspective set as the same value as initial perspective
     * @param _maxDepth the depth (number of moves in the future) to calculate to
     * @param _depth set as 0
     * @param _rootMove set as null
     * @return the best move, along with the score of the move, for the side whose turn it is
     */
    private ScoredMove FindBestMove(Side _initialPerspective, Side _curPerspective,
    int _maxDepth, int _depth, ScoredMove _rootMove)
    {
        if (board.isDraw()) return new ScoredMove(_rootMove.getMove(), 0f);

        if (board.isMated())
        {
            Square _whiteKingLocation = board.getKingSquare(Side.WHITE);

            if (board.squareAttackedBy(_whiteKingLocation, Side.BLACK) != 0L)
                return new ScoredMove(_rootMove.getMove(), -Float.MAX_VALUE);

            else return new ScoredMove(_rootMove.getMove(), Float.MAX_VALUE);
        }

        if (_depth == _maxDepth)
            return new ScoredMove(_maxDepth == 0 ? null : _rootMove.getMove(), Evaluate());
        

        ScoredMove _bestMoveForSide = null;
        List<ScoredMove> _equalMoves = new ArrayList<>(0);
        for (Move _curMove : board.legalMoves())
        {
            if (_depth == 0) _rootMove = new ScoredMove(_curMove, 0f);

            board.doMove(_curMove);

            ScoredMove _scoredMove = FindBestMove
            (
                _initialPerspective, _curPerspective.flip(),
                _maxDepth, _depth + 1, _rootMove
            );

            board.undoMove();

            if (_bestMoveForSide == null)
            {
                _bestMoveForSide = _scoredMove;
                _equalMoves.add(_scoredMove);
            }

            if (_scoredMove.getScore() + SCORE_TOLERANCE > _bestMoveForSide.getScore()
                && _scoredMove.getScore() - SCORE_TOLERANCE < _bestMoveForSide.getScore())
            {
                _equalMoves.add(_scoredMove);
            }

            else if (_curPerspective == Side.WHITE)
            {
                if (_scoredMove.getScore() > _bestMoveForSide.getScore())
                {
                    _bestMoveForSide = _scoredMove;
                    _equalMoves.clear();
                    _equalMoves.add(_scoredMove);
                }
            }

            else
            {
                if (_scoredMove.getScore() < _bestMoveForSide.getScore())
                {
                    _bestMoveForSide = _scoredMove;
                    _equalMoves.clear();
                    _equalMoves.add(_scoredMove);
                }
            }
        }

        // only if depth is zero because this method
        // only returns the root move, so this won't
        // affect the outcome for other depths
        if (_depth == 0 && _equalMoves.size() > 1)
        {
            int _randIndex = new Random().nextInt(_equalMoves.size());

            _bestMoveForSide = _equalMoves.get(_randIndex);
            _rootMove = _equalMoves.get(_randIndex);
        }

        else if (_initialPerspective == Side.WHITE)
        {
            if (_bestMoveForSide.getScore() > _rootMove.getScore())
                _rootMove = new ScoredMove(_rootMove.getMove(), _bestMoveForSide.getScore());
        }

        else
        {
            if (_bestMoveForSide.getScore() < _rootMove.getScore())
                _rootMove = new ScoredMove(_rootMove.getMove(), _bestMoveForSide.getScore());
        }

        return _bestMoveForSide;
    }

    /**
     * Finds the best move for the engine's Chess board using the Minimax algorithm.
     * In addition, the function "returns" the number of moves that were evaluated
     * using the org.apache.commons.lang3.mutable.MutableInt class.
     * @param _initialPerspective the side whose turn it is
     * @param _curPerspective set as the same value as initial perspective
     * @param _maxDepth the depth (number of moves in the future) to calculate to
     * @param _depth set as 0
     * @param _rootMove set as null
     * @param _numMovesEvaluated an object holding the number of positions that were evaluated
     * while finding the best move
     * @return the best move, along with the score of the move, for the side whose turn it is
     */
    private ScoredMove FindBestMove(Side _initialPerspective, Side _curPerspective,
    int _maxDepth, int _depth, ScoredMove _rootMove, MutableInt _numMovesEvaluated)
    {
        if (board.isDraw()) return new ScoredMove(_rootMove.getMove(), 0f);

        if (board.isMated())
        {
            Square _whiteKingLocation = board.getKingSquare(Side.WHITE);

            if (board.squareAttackedBy(_whiteKingLocation, Side.BLACK) != 0L)
                return new ScoredMove(_rootMove.getMove(), -Float.MAX_VALUE);

            else return new ScoredMove(_rootMove.getMove(), Float.MAX_VALUE);
        }

        if (_depth == _maxDepth)
        {
            if (_rootMove == null)
            {
                throw new IllegalArgumentException("Max depth cannot be zero.");
            }

            return new ScoredMove(_rootMove.getMove(), Evaluate());
        }
        

        ScoredMove _bestMoveForSide = null;
        List<ScoredMove> _equalMoves = new ArrayList<>(0);
        for (Move _curMove : board.legalMoves())
        {
            _numMovesEvaluated.increment();

            if (_depth == 0) _rootMove = new ScoredMove(_curMove, 0f);

            board.doMove(_curMove);

            ScoredMove _scoredMove = FindBestMove
            (
                _initialPerspective, _curPerspective.flip(),
                _maxDepth, _depth + 1, _rootMove, _numMovesEvaluated
            );

            board.undoMove();

            if (_bestMoveForSide == null)
            {
                _bestMoveForSide = _scoredMove;
                _equalMoves.add(_scoredMove);
            }

            if (_scoredMove.getScore() + SCORE_TOLERANCE > _bestMoveForSide.getScore()
                && _scoredMove.getScore() - SCORE_TOLERANCE < _bestMoveForSide.getScore())
            {
                _equalMoves.add(_scoredMove);
            }

            else if (_curPerspective == Side.WHITE)
            {
                if (_scoredMove.getScore() > _bestMoveForSide.getScore())
                {
                    _bestMoveForSide = _scoredMove;
                    _equalMoves.clear();
                    _equalMoves.add(_scoredMove);
                }
            }

            else
            {
                if (_scoredMove.getScore() < _bestMoveForSide.getScore())
                {
                    _bestMoveForSide = _scoredMove;
                    _equalMoves.clear();
                    _equalMoves.add(_scoredMove);
                }
            }
        }

        // only if depth is zero because this method
        // only returns the root move, so this won't
        // affect the outcome for other depths
        if (_depth == 0 && _equalMoves.size() > 1)
        {
            int _randIndex = new Random().nextInt(_equalMoves.size());

            _bestMoveForSide = _equalMoves.get(_randIndex);
            _rootMove = _equalMoves.get(_randIndex);
        }

        else if (_initialPerspective == Side.WHITE)
        {
            if (_bestMoveForSide.getScore() > _rootMove.getScore())
                _rootMove = new ScoredMove(_rootMove.getMove(), _bestMoveForSide.getScore());
        }

        else
        {
            if (_bestMoveForSide.getScore() < _rootMove.getScore())
                _rootMove = new ScoredMove(_rootMove.getMove(), _bestMoveForSide.getScore());
        }

        return _bestMoveForSide;
    }

    /**
     * Finds the best move for the engine's Chess board using the Minimax algorithm and alpha beta pruning.
     * @param _initialPerspective the side whose turn it is
     * @param _curPerspective set as the same value as initial perspective
     * @param _maxDepth the depth (number of moves in the future) to calculate to
     * @param _depth set as 0
     * @param _rootMove set as null
     * @param _alpha set as -Float.MAX_VALUE
     * @param _beta set as Float.MAX_VALUE
     * @return the best move, along with the score of the move, for the side whose turn it is
     */
    private ScoredMove FindBestMove(Side _initialPerspective, Side _curPerspective,
    int _maxDepth, int _depth, ScoredMove _rootMove, float _alpha, float _beta)
    {
        if (board.isDraw()) return new ScoredMove(_rootMove.getMove(), 0f);

        if (board.isMated())
        {
            Square _whiteKingLocation = board.getKingSquare(Side.WHITE);

            if (board.squareAttackedBy(_whiteKingLocation, Side.BLACK) != 0L)
                return new ScoredMove(_rootMove.getMove(), -Float.MAX_VALUE);

            else return new ScoredMove(_rootMove.getMove(), Float.MAX_VALUE);
        }

        if (_depth == _maxDepth)
            return new ScoredMove(_maxDepth == 0 ? null : _rootMove.getMove(), Evaluate());

        ScoredMove _bestMoveForSide = null;
        List<ScoredMove> _equalMoves = new ArrayList<>(0);
        for (Move _curMove : board.legalMoves())
        {
            if (_depth == 0) _rootMove = new ScoredMove(_curMove, 0f);

            board.doMove(_curMove);

            ScoredMove _scoredMove = FindBestMove
            (
                _initialPerspective, _curPerspective.flip(),
                _maxDepth, _depth + 1, _rootMove, _alpha, _beta
            );

            board.undoMove();

            if (_bestMoveForSide == null)
            {
                _bestMoveForSide = _scoredMove;
                _equalMoves.add(_scoredMove);
            }

            if (_scoredMove.getScore() + SCORE_TOLERANCE > _bestMoveForSide.getScore()
                && _scoredMove.getScore() - SCORE_TOLERANCE < _bestMoveForSide.getScore())
            {
                _equalMoves.add(_scoredMove);
            }

            else if (_curPerspective == Side.WHITE)
            {
                if (_scoredMove.getScore() > _bestMoveForSide.getScore())
                {
                    _bestMoveForSide = _scoredMove;
                    _equalMoves.clear();
                    _equalMoves.add(_scoredMove);
                }

                _alpha = Math.max(_alpha, _bestMoveForSide.getScore());
                if (_alpha >= _beta)
                {
                    System.out.println("Pruning because black has a better option");
                    System.out.println("Alpha: " + _alpha);
                    System.out.println("Beta: " + _beta);
                    System.out.println("Best move for white so far: " + _bestMoveForSide);
                    break;
                }
            }

            else
            {
                if (_scoredMove.getScore() < _bestMoveForSide.getScore())
                {
                    _bestMoveForSide = _scoredMove;
                    _equalMoves.clear();
                    _equalMoves.add(_scoredMove);
                }

                _beta = Math.min(_beta, _bestMoveForSide.getScore());
                if (_beta <= _alpha)
                {
                    System.out.println("Pruning because white has a better option");
                    System.out.println("Beta: " + _beta);
                    System.out.println("Alpha: " + _alpha);
                    System.out.println("Best move for black so far: " + _bestMoveForSide);
                    break;
                }
            }
        }

        // only if depth is zero because this method
        // only returns the root move, so this won't
        // affect the outcome for other depths
        if (_depth == 0 && _equalMoves.size() > 1)
        {
            int _randIndex = new Random().nextInt(_equalMoves.size());

            _bestMoveForSide = _equalMoves.get(_randIndex);
            _rootMove = _equalMoves.get(_randIndex);
        }

        else if (_initialPerspective == Side.WHITE)
        {
            if (_bestMoveForSide.getScore() > _rootMove.getScore())
                _rootMove = new ScoredMove(_rootMove.getMove(), _bestMoveForSide.getScore());
        }

        else
        {
            if (_bestMoveForSide.getScore() < _rootMove.getScore())
                _rootMove = new ScoredMove(_rootMove.getMove(), _bestMoveForSide.getScore());
        }

        return _bestMoveForSide;
    }

    /**
     * Finds the best move for the engine's Chess board using the Minimax algorithm and alpha beta pruning.
     * In addition, the function "returns" the number of moves that were evaluated
     * using the org.apache.commons.lang3.mutable.MutableInt class.
     * @param _initialPerspective the side whose turn it is
     * @param _curPerspective set as the same value as initial perspective
     * @param _maxDepth the depth (number of moves in the future) to calculate to
     * @param _depth set as 0
     * @param _rootMove set as null
     * @param _alpha set as -Float.MAX_VALUE
     * @param _beta set as Float.MAX_VALUE
     * @param _numMovesEvaluated an object holding the number of positions that were evaluated
     * while finding the best move
     * @return the best move, along with the score of the move, for the side whose turn it is
     */
    private ScoredMove FindBestMove(Side _initialPerspective, Side _curPerspective,
    int _maxDepth, int _depth, ScoredMove _rootMove, float _alpha, float _beta,
    MutableInt _numMovesEvaluated)
    {
        if (board.isDraw()) return new ScoredMove(_rootMove.getMove(), 0f);

        if (board.isMated())
        {
            Square _whiteKingLocation = board.getKingSquare(Side.WHITE);

            if (board.squareAttackedBy(_whiteKingLocation, Side.BLACK) != 0L)
                return new ScoredMove(_rootMove.getMove(), -Float.MAX_VALUE);

            else return new ScoredMove(_rootMove.getMove(), Float.MAX_VALUE);
        }

        if (_depth == _maxDepth)
        {
            _numMovesEvaluated.increment();
            return new ScoredMove(_maxDepth == 0 ? null : _rootMove.getMove(), Evaluate());
        }

        ScoredMove _bestMoveForSide = null;
        List<ScoredMove> _equalMoves = new ArrayList<>(0);
        for (Move _curMove : board.legalMoves())
        {
            if (_depth == 0) _rootMove = new ScoredMove(_curMove, 0f);

            board.doMove(_curMove);

            ScoredMove _scoredMove = FindBestMove
            (
                _initialPerspective, _curPerspective.flip(),
                _maxDepth, _depth + 1, _rootMove, _alpha, _beta, _numMovesEvaluated
            );

            board.undoMove();

            if (_bestMoveForSide == null)
            {
                _bestMoveForSide = _scoredMove;
                _equalMoves.add(_scoredMove);
            }

            if (_scoredMove.getScore() + SCORE_TOLERANCE > _bestMoveForSide.getScore()
                && _scoredMove.getScore() - SCORE_TOLERANCE < _bestMoveForSide.getScore())
            {
                _equalMoves.add(_scoredMove);
            }

            else if (_curPerspective == Side.WHITE)
            {
                if (_scoredMove.getScore() > _bestMoveForSide.getScore())
                {
                    _bestMoveForSide = _scoredMove;
                    _equalMoves.clear();
                    _equalMoves.add(_scoredMove);
                }

                _alpha = Math.max(_alpha, _bestMoveForSide.getScore());
                if (_alpha >= _beta)
                {
                    // System.out.println("Pruning because black has a better option");
                    // System.out.println("Alpha: " + _alpha);
                    // System.out.println("Beta: " + _beta);
                    // System.out.println("Best move for white so far: " + _bestMoveForSide);
                    break;
                }
            }

            else
            {
                if (_scoredMove.getScore() < _bestMoveForSide.getScore())
                {
                    _bestMoveForSide = _scoredMove;
                    _equalMoves.clear();
                    _equalMoves.add(_scoredMove);
                }

                _beta = Math.min(_beta, _bestMoveForSide.getScore());
                if (_beta <= _alpha)
                {
                    // System.out.println("Pruning because white has a better option");
                    // System.out.println("Beta: " + _beta);
                    // System.out.println("Alpha: " + _alpha);
                    // System.out.println("Best move for black so far: " + _bestMoveForSide);
                    break;
                }
            }
        }

        // only if depth is zero because this method
        // only returns the root move, so this won't
        // affect the outcome for other depths
        if (_depth == 0 && _equalMoves.size() > 1)
        {
            int _randIndex = new Random().nextInt(_equalMoves.size());

            _bestMoveForSide = _equalMoves.get(_randIndex);
            _rootMove = _equalMoves.get(_randIndex);
        }

        else if (_initialPerspective == Side.WHITE)
        {
            if (_bestMoveForSide.getScore() > _rootMove.getScore())
                _rootMove = new ScoredMove(_rootMove.getMove(), _bestMoveForSide.getScore());
        }

        else
        {
            if (_bestMoveForSide.getScore() < _rootMove.getScore())
                _rootMove = new ScoredMove(_rootMove.getMove(), _bestMoveForSide.getScore());
        }

        return _bestMoveForSide;
    }

    /**
     * Finds the best move for the side to move according to the engine's internal Chess board.
     * Various optimizations can be turned on and off using the SearchingArgs object that is passed in.
     * @param _args the arguments for the search
     * @return the best move for the side to move
     * @throws IllegalArgumentException when the number of plies in the SearchingArgs object is less than 0.
     */
    public ScoredMove FindBestMove(SearchingArgs _args) throws IllegalArgumentException
    {
        if (board.isDraw()) return new ScoredMove(new Move(Square.NONE, Square.NONE), 0f);

        if (board.isMated())
        {
            Square _whiteKingLocation = board.getKingSquare(Side.WHITE);

            if (board.squareAttackedBy(_whiteKingLocation, Side.BLACK) != 0L)
                return new ScoredMove(new Move(Square.NONE, Square.NONE), -Float.MAX_VALUE);

            else return new ScoredMove(new Move(Square.NONE, Square.NONE), Float.MAX_VALUE);
        }

        if (_args.getNumPlies() < 0)
        {
            throw new IllegalArgumentException("The \"maxDepth\" parameter for the given" +
            " SearchingArgs is " + _args.getNumPlies() + " when it should be greater than or equal to 0.");
        }

        List<Move> _sortedLegalMoves = board.legalMoves();

        if (_args.isShouldUseHeuristicMoveOrdering()) SortMovesHeuristically(_sortedLegalMoves);

        ScoredMove _bestMoveForSide = null;
        Side _initialPerspective = board.getSideToMove();
        float _alpha = -Float.MAX_VALUE;
        float _beta = Float.MAX_VALUE;

        List<ScoredMove> _equalMoves = new ArrayList<>(0);
        
        for (Move _curMove : _sortedLegalMoves)
        {
            ScoredMove _rootMove = new ScoredMove(_curMove, 0f);

            board.doMove(_curMove);

            ScoredMove _scoredMove = FindBestMove
            (
                _args, _initialPerspective, _rootMove,
                board.getSideToMove(), _alpha, _beta, 1
            );

            board.undoMove();

            if (_bestMoveForSide == null)
            {
                _bestMoveForSide = _scoredMove;
                _equalMoves.add(_scoredMove);
            }

            if (_scoredMove.getScore() + SCORE_TOLERANCE > _bestMoveForSide.getScore()
                && _scoredMove.getScore() - SCORE_TOLERANCE < _bestMoveForSide.getScore())
            {
                _equalMoves.add(_scoredMove);
            }

            else if (_initialPerspective == Side.WHITE)
            {
                if (_scoredMove.getScore() > _bestMoveForSide.getScore())
                {
                    _bestMoveForSide = _scoredMove;
                    _equalMoves.clear();
                    _equalMoves.add(_scoredMove);
                }

                _alpha = Math.max(_alpha, _bestMoveForSide.getScore());
            }

            else
            {
                if (_scoredMove.getScore() < _bestMoveForSide.getScore())
                {
                    _bestMoveForSide = _scoredMove;
                    _equalMoves.clear();
                    _equalMoves.add(_scoredMove);
                }

                _beta = Math.min(_beta, _bestMoveForSide.getScore());
            }

            if (_args.isShouldUseAlphaBetaPruning() && _beta <= _alpha)
                break;
        }

        // only with the initial call because this method
        // only returns the root move, so this won't
        // affect the outcome for other depths
        if (_equalMoves.size() > 1)
        {
            int _randIndex = new Random().nextInt(_equalMoves.size());
            _bestMoveForSide = _equalMoves.get(_randIndex);
        }

        return _bestMoveForSide;
    }


    /**
     * Internal function for recursion so that outside callers do not need to pass in every parameter.
     * @param _args
     * @param _initialPerspective
     * @param _rootMove
     * @param _curPerspective
     * @param _alpha
     * @param _beta
     * @param _ply
     * @return the best move for the side to move
     */
    private ScoredMove FindBestMove(SearchingArgs _args, Side _initialPerspective, ScoredMove _rootMove, Side _curPerspective, float _alpha, float _beta, int _ply)
    {
        if (board.isDraw()) return new ScoredMove(_rootMove.getMove(), 0f);

        if (board.isMated())
        {
            Square _whiteKingLocation = board.getKingSquare(Side.WHITE);

            if (board.squareAttackedBy(_whiteKingLocation, Side.BLACK) != 0L)
                return new ScoredMove(_rootMove.getMove(), -Float.MAX_VALUE);

            else return new ScoredMove(_rootMove.getMove(), Float.MAX_VALUE);
        }

        if (_ply >= _args.getNumPlies())
        {
            return new ScoredMove
            (
                _args.getNumPlies() == 0 ? new Move(Square.NONE, Square.NONE)
                : _rootMove.getMove(),
                Evaluate()
            );
        }

        List<Move> _sortedLegalMoves = board.legalMoves();

        if (_args.isShouldUseHeuristicMoveOrdering()) SortMovesHeuristically(_sortedLegalMoves);

        ScoredMove _bestMoveForSide = null;
        for (Move _curMove : _sortedLegalMoves)
        {
            board.doMove(_curMove);

            ScoredMove _scoredMove = FindBestMove
            (
                _args, _initialPerspective, _rootMove,
                _curPerspective.flip(), _alpha, _beta, _ply + 1
            );

            board.undoMove();

            if (_bestMoveForSide == null)
                _bestMoveForSide = _scoredMove;

            else if (_curPerspective == Side.WHITE)
            {
                if (_scoredMove.getScore() > _bestMoveForSide.getScore())
                    _bestMoveForSide = _scoredMove;

                _alpha = Math.max(_alpha, _bestMoveForSide.getScore());
            }

            else
            {
                if (_scoredMove.getScore() < _bestMoveForSide.getScore())
                    _bestMoveForSide = _scoredMove;

                _beta = Math.min(_beta, _bestMoveForSide.getScore());
            }

            if (_args.isShouldUseAlphaBetaPruning() && _alpha >= _beta)
                break;
        }

        if (_initialPerspective == Side.WHITE)
        {
            if (_bestMoveForSide.getScore() > _rootMove.getScore())
                _rootMove = 
                new ScoredMove
                (
                    _rootMove.getMove(),
                    _bestMoveForSide.getScore()
                );
        }

        else
        {
            if (_bestMoveForSide.getScore() < _rootMove.getScore())
                _rootMove = 
                new ScoredMove
                (
                    _rootMove.getMove(),
                    _bestMoveForSide.getScore()
                );
        }

        return _bestMoveForSide;
    }

    // TODO: fix this method (returns correct score, but it doesn't return the actual line)
    private ScoredLine FindBestLine(Side _initialPerspective, Side _curPerspective,
    int _maxDepth, int _depth, ScoredLine _curLine)
    {
        if (board.isDraw())
        {
            _curLine = new ScoredLine(_curLine.getLine(), 0f);
            return _curLine;
        }

        if (board.isMated())
        {
            Square _whiteKingLocation = board.getKingSquare(Side.WHITE);

            // white is mated
            if (board.squareAttackedBy(_whiteKingLocation, Side.BLACK) != 0L)
            {
                _curLine = new ScoredLine(_curLine.getLine(), -Float.MAX_VALUE);
                return _curLine;
            }

            // black is mated
            else
            {
                _curLine = new ScoredLine(_curLine.getLine(), Float.MAX_VALUE);
                return _curLine;
            }
        }

        if (_depth == _maxDepth)
        {
            if (_curLine == null)
            {
                throw new IllegalArgumentException("Max depth cannot be zero.");
            }

            return new ScoredLine(_curLine.getLine(), Evaluate());
        }
        

        ScoredLine _bestLineForSide = null;
        List<ScoredLine> _equalLines = new ArrayList<>(0);
        for (Move _curMove : board.legalMoves())
        {
            board.doMove(_curMove);

            if (_curLine == null) _curLine = new ScoredLine(new ArrayList<>(), 0f);

            List<Move> _newLine = _curLine.getLine();
            _newLine.add(_curMove);

            _curLine = new ScoredLine(_newLine, _curLine.getScore());

            ScoredLine _scoredLine = FindBestLine
            (
                _initialPerspective, _curPerspective.flip(),
                _maxDepth, _depth + 1, _curLine
            );

            board.undoMove();

            if (_bestLineForSide == null)
            {
                _bestLineForSide = _scoredLine;
                _equalLines.add(_scoredLine);
            }

            if (_scoredLine.getScore() + SCORE_TOLERANCE > _bestLineForSide.getScore()
                && _scoredLine.getScore() - SCORE_TOLERANCE < _bestLineForSide.getScore())
            {
                _equalLines.add(_scoredLine);
            }

            else if (_curPerspective == Side.WHITE)
            {
                if (_scoredLine.getScore() > _bestLineForSide.getScore())
                {
                    _bestLineForSide = _scoredLine;
                    _equalLines.clear();
                    _equalLines.add(_scoredLine);
                }
            }

            else
            {
                if (_scoredLine.getScore() < _bestLineForSide.getScore())
                {
                    _bestLineForSide = _scoredLine;
                    _equalLines.clear();
                    _equalLines.add(_scoredLine);
                }
            }
        }

        if (_equalLines.size() > 1)
        {
            int _randIndex = new Random().nextInt(_equalLines.size());
            return _equalLines.get(_randIndex);
        }
        
        return _bestLineForSide;
    }
    //#endregion

    //#region Evaluating
    /**
     * Evaluates the position of the engine's Chess board.
     * @return the evaluation of the position. A negative number indicates a position
     * that favors the black side, while a positive number indicates a position that favors the white side.
     */
    public float Evaluate()
    {
        List<Square> _whitePawnLocations = board.getPieceLocation(Piece.WHITE_PAWN);
        List<Square> _whiteKnightLocations = board.getPieceLocation(Piece.WHITE_KNIGHT);
        List<Square> _whiteBishopLocations = board.getPieceLocation(Piece.WHITE_BISHOP);
        List<Square> _whiteRookLocations = board.getPieceLocation(Piece.WHITE_ROOK);
        Square _whiteQueenLocation = board.getFistPieceLocation(Piece.WHITE_QUEEN);
        Square _whiteKingLocation = board.getFistPieceLocation(Piece.WHITE_KING);
        boolean _isThereWhiteQueen = _whiteQueenLocation != Square.NONE;

        float _whiteMaterial = EvaluateMaterial
        (
            _whitePawnLocations.size(), _whiteKnightLocations.size(),
            _whiteBishopLocations.size(), _whiteRookLocations.size(),
            _isThereWhiteQueen ? 1 : 0
        );

        float _whitePosition = EvaluatePosition
        (
            _whitePawnLocations, _whiteKnightLocations, _whiteBishopLocations,
            _whiteRookLocations, _whiteQueenLocation, _whiteKingLocation
        );

        List<Square> _blackPawnLocations = board.getPieceLocation(Piece.BLACK_PAWN);
        List<Square> _blackKnightLocations = board.getPieceLocation(Piece.BLACK_KNIGHT);
        List<Square> _blackBishopLocations = board.getPieceLocation(Piece.BLACK_BISHOP);
        List<Square> _blackRookLocations = board.getPieceLocation(Piece.BLACK_ROOK);
        Square _blackQueenLocation = board.getFistPieceLocation(Piece.BLACK_QUEEN);
        Square _blackKingLocation = board.getFistPieceLocation(Piece.BLACK_KING);
        boolean _isThereBlackQueen = _blackQueenLocation != Square.NONE;

        float _blackMaterial = EvaluateMaterial
        (
            _blackPawnLocations.size(), _blackKnightLocations.size(),
            _blackBishopLocations.size(), _blackRookLocations.size(),
            _isThereBlackQueen ? 1 : 0
        );

        float _blackPosition = EvaluatePosition
        (
            _blackPawnLocations, _blackKnightLocations, _blackBishopLocations,
            _blackRookLocations, _blackQueenLocation, _blackKingLocation
        );

        // System.out.println();
        // System.out.println("White position, material: " + _whitePosition + ", " + _whiteMaterial);
        // System.out.println("Black position: " + _blackPosition + ", " + _blackMaterial);
        // System.out.println();
        // _whitePosition = 0f;
        // _blackPosition = 0f;

        return (_whiteMaterial + _whitePosition) - (_blackMaterial + _blackPosition);
    }

    /**
     * Evaluates the material value of pawns and pieces.
     * This function does not accept a board and a side to evaluate
     * to optimize the "Evaluate" function. The number of pawns and pieces should
     * be counted from the same side, position, and board.
     * @param _numPawns the number of pawns for a side. Should be 8 to 0.
     * @param _numKnights the number of knights for a side. Should be 2 to 0.
     * @param _numBishops the number of bishops for a side. Should be 2 to 0.
     * @param _numRooks the number of rooks for a side. Should be 2 to 0.
     * @param _queenNum the number of queens. Should be 1 or 0.
     * @return the counted-up material. A negative number indicates a position
     * that favors the black side, while a positive number indicates a position that favors the white side.
     */
    private float EvaluateMaterial(int _numPawns, int _numKnights, int _numBishops,
    int _numRooks, int _queenNum)
    {
        float _totalMaterialValue = PAWN_VALUE * _numPawns + KNIGHT_VALUE * _numKnights
        + BISHOP_VALUE * _numBishops + ROOK_VALUE * _numRooks + QUEEN_VALUE * _queenNum;

        return _totalMaterialValue;
    }

    /**
     * Evaluates the positional aspect of a Chess board by calling functions such as
     * "EvaluatePiecePlacement", "EvaluatePawnStructure", and more internally.
     * The function takes in the pawn and piece location parameters to optimize
     * the "Evaluate" function.
     * <p>
     * These parameters should be counted from the same side, position, and board.
     * If there are no pawns, knights, bishops, or rooks, pass in an empty list.
     * If there is no queen or king, pass in "Square.NONE" as the value.
     * @param _pawnLocations the locations of every pawn of a side
     * @param _knightLocations the locations of every knight of a side
     * @param _bishopLocations the locations of every bishop of a side
     * @param _rookLocations the locations of every rook of a side
     * @param _queenLocation the location of the queen of a side
     * @param _kingLocation the locations of every pawn of a side
     * @return the evaluation of the board for a side based on its positional aspects. A negative number indicates a position
     * that favors the black side, while a positive number indicates a position that favors the white side.
     */
    private float EvaluatePosition(List<Square> _pawnLocations,
    List<Square> _knightLocations, List<Square> _bishopLocations,
    List<Square> _rookLocations, Square _queenLocation, Square _kingLocation)
    {
        return EvaluatePiecePlacement
        (
            _pawnLocations, _knightLocations,
            _bishopLocations, _rookLocations, _queenLocation, _kingLocation
        )
        + EvaluatePieceMobility
        (
            _knightLocations, _bishopLocations,
            _rookLocations, _queenLocation
        )
        + EvaluatePawnStructure(_pawnLocations) + EvaluateKingSafety(_kingLocation);
    }

    // TODO
    private static float EvaluatePiecePlacement(List<Square> _pawnLocations,
    List<Square> _knightLocations, List<Square> _bishopLocations,
    List<Square> _rookLocations, Square _queenLocation, Square _kingLocation)
    {
        return 0f;
    }

    // TODO
    private float EvaluatePieceMobility(List<Square> _knightLocations,
    List<Square> _bishopLocations, List<Square> _rookLocations, Square _queenLocation)
    {
        return 0f;
    }

    /**
     * Evaluates the pawn structure of a side.
     * <p>
     * The pawn locations should be counted from the same side, position, and board.
     * The function takes in the pawn locations to optimize the "Evaluation" function.
     * @param _pawnLocations the locations of every pawn of a side
     * @return the evaluation of the pawn structure of a side. A negative number indicates a position
     * that favors the black side, while a positive number indicates a position that favors the white side.
     */
    private float EvaluatePawnStructure(List<Square> _pawnLocations)
    {
        float _pawnStructurePenalty = 0f;

        // so that n doubled pawns in the same
        // file only get points deducted for each
        // of them rather than applying penalties
        // n * (n - 1) times
        boolean[] _doubledPawnFiles = new boolean[8];

        for (int i = 0; i < _pawnLocations.size(); i++)
        {
            int _numDoubledPawns = 0;
            boolean _isIsolated = true;

            File _pawnFile = _pawnLocations.get(i).getFile();

            for (int j = 0; j < _pawnLocations.size(); j++)
            {
                if (j == i) continue;

                File _comparingPawnFile = _pawnLocations.get(j).getFile();

                if (_pawnFile.equals(_comparingPawnFile) && !_doubledPawnFiles[_pawnFile.ordinal()])
                {
                    _numDoubledPawns++;
                    _doubledPawnFiles[_pawnFile.ordinal()] = true;
                }

                else if (_pawnFile.ordinal() + 1 == _comparingPawnFile.ordinal()
                         || _pawnFile.ordinal() - 1 == _comparingPawnFile.ordinal())
                    _isIsolated = false;
            }

            // +1 to count for itself
            _pawnStructurePenalty -= (_numDoubledPawns + 1) * DOUBLED_PAWN_PENALTY;
            _pawnStructurePenalty -= _isIsolated ? ISOLATED_PAWN_PENALTY : 0f;

            if (_numDoubledPawns > 0 && _isIsolated)
                _pawnStructurePenalty -= DOUBLED_AND_ISOLATED_PENALTY;
        }

        return _pawnStructurePenalty;
    }

    // TODO
    private float EvaluateKingSafety(Square _kingLocation)
    {
        return 0f;
    }
    //#endregion

    //#region Other
    /**
     * Orders the given moves to prioritize moves that were
     * heuristically evaluated as "better" to optimize alpha beta pruning.
     * This function assumes that all moves given are legal.
     * @param _movesToOrder the moves to order
     */
    private void SortMovesHeuristically(List<Move> _movesToOrder)
    {
        List<ScoredMove> _movesToOrderScored = new ArrayList<>();
        
        for (Move _moveToOrder : _movesToOrder)
        {
            float _moveEvaluationGuess = 0f;
            Piece _movingPiece = board.getPiece(_moveToOrder.getFrom());
            Piece _attackedPiece = board.getPiece(_moveToOrder.getTo());

            if (_attackedPiece != Piece.NONE)
            {
                // bonus for lower-valued piece taking higher valued piece
                if (GetPieceValue(_movingPiece) < GetPieceValue(_attackedPiece))
                {
                    _moveEvaluationGuess += GetPieceValue(_attackedPiece) - GetPieceValue(_movingPiece);
                }
            }

            // if the other side attacks the to square
            if (board.squareAttackedBy(_moveToOrder.getTo(), board.getSideToMove().flip()) != 0L)
            {
                // penalty if the piece's side doesn't defend the to square
                if (board.squareAttackedBy(_moveToOrder.getTo(), _movingPiece.getPieceSide()) == 0L)
                {
                    _moveEvaluationGuess -= GetPieceValue(_movingPiece);
                }

                else
                {
                    // penalty if a lower-valued piece attacks the to square
                    board.setSideToMove(board.getSideToMove().flip());
                    float _minAttackingOpponentPieceValue = 0f;
                    for (Move _legalMove : board.legalMoves())
                    {
                        if (_legalMove.getTo().equals(_moveToOrder.getTo()))
                        {
                            _minAttackingOpponentPieceValue = Math.min
                            (
                                GetPieceValue(board.getPiece(_legalMove.getFrom())),
                                _minAttackingOpponentPieceValue
                            );
                        }
                    }
                    board.setSideToMove(board.getSideToMove().flip());
    
                    if (_minAttackingOpponentPieceValue < GetPieceValue(_movingPiece))
                    {
                        _moveEvaluationGuess += _minAttackingOpponentPieceValue - GetPieceValue(_movingPiece);
                    }
                }

            }

            // bonus for giving check
            board.doMove(_moveToOrder);
            if (board.isKingAttacked())
            {
                _moveEvaluationGuess += PAWN_VALUE;
            }
            board.undoMove();

            _movesToOrderScored.add(new ScoredMove(_moveToOrder, _moveEvaluationGuess));
        }

        // sorting _movesToOrder based on _movesToOrderScored
        
        Collections.sort(_movesToOrderScored);

        _movesToOrder.clear();
        for (ScoredMove _moveToOrderScored : _movesToOrderScored)
        {
            _movesToOrder.add(_moveToOrderScored.getMove());
        }
    }

    private float GetPieceValue(Piece _piece)
    {
        PieceType _typeOfPiece = _piece.getPieceType();
        
        switch (_typeOfPiece)
        {
            case PAWN:
                return PAWN_VALUE;

            case KNIGHT:
                return KNIGHT_VALUE;
            
            case BISHOP:
                return BISHOP_VALUE;
                
            case ROOK:
                return ROOK_VALUE;

            case QUEEN:
                return QUEEN_VALUE;

            case KING:
                return Float.MAX_VALUE;

            case NONE:
            default:
                return 0f;
        }
    }
    //#endregion

    @Data
    private class RecursiveMoveSearchingParameters
    {
        private ScoredMove rootMove;
        private Side initialPerspective;
        private Side curPerspective;
        private int depth;
        private float alpha;
        private float beta;

        

        public RecursiveMoveSearchingParameters(ScoredMove _rootMove, Side _initialPerspective,
        Side _curPerspective, int _depth)
        {
            rootMove = _rootMove;
            initialPerspective = _initialPerspective;
            curPerspective = _curPerspective;
            depth = _depth;

            alpha = -Float.MAX_VALUE;
            beta = Float.MAX_VALUE;
        }

        public RecursiveMoveSearchingParameters(RecursiveMoveSearchingParameters _paramsToCopy)
        {
            this
            (
                new ScoredMove(_paramsToCopy.rootMove.getMove(), _paramsToCopy.rootMove.getScore()),
                Side.valueOf(_paramsToCopy.initialPerspective.toString()),
                _paramsToCopy.curPerspective, _paramsToCopy.depth
            );

            alpha = _paramsToCopy.alpha;
            beta = _paramsToCopy.beta;
        }

        public void ResetAlpha()
        {
            alpha = -Float.MAX_VALUE;
        }

        public void ResetBeta()
        {
            beta = Float.MAX_VALUE;
        }
    }
}