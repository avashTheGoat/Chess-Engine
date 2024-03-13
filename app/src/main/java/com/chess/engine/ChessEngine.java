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
import com.github.bhlangonijr.chesslib.Rank;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;

import lombok.Getter;
import lombok.Setter;

public class ChessEngine
{
    @Setter
    @Getter
    private Board board;
    private Random rand;

    private static final float SCORE_TOLERANCE = 0.005f;

    //#region Evaluation Constants
    //#region Pawn structure penalties
    private static final float DOUBLED_PAWN_PENALTY = 0.095f;
    private static final float ISOLATED_PAWN_PENALTY = 0.105f;
    private static final float DOUBLED_AND_ISOLATED_PENALTY = 0.21f;
    //#endregion

    //#region Endgame weight calculation
    // pawns don't count that much to endgame weight
    // minor pieces have a decent influence on endgame weight
    // rooks have slightly multiplied difference
    // both queens on the board moves the game away from endgame by a lot
    private static final float PAWN_WEIGHT = 0.35f;
    private static final float MINOR_PIECE_WEIGHT = 1f;
    private static final float ROOK_WEIGHT = 1.095f;
    private static final float QUEEN_WEIGHT = 1.3f;
    //#endregion

    //#region Piece values
    private static final float PAWN_VALUE = 1f;
    private static final float KNIGHT_VALUE = 3f;
    private static final float BISHOP_VALUE = 3.15f;
    private static final float ROOK_VALUE = 5f;
    private static final float QUEEN_VALUE = 10f;
    //#endregion

    //#region Piece square tables
    // favors pushed pawns, but doesn't favor overextended pawns as strongly.
    // even pawns about to promote are not as highly regarded because
    // they could prove to be weaknesses that can be targeted by the opponent.
    // also, likes pawns not being pushed at the castling locations
    private static final float[][] PAWN_PLACEMENT_TABLE_MIDDLEGAME = new float[][]
    {
        new float[] { QUEEN_VALUE, QUEEN_VALUE, QUEEN_VALUE, QUEEN_VALUE, QUEEN_VALUE, QUEEN_VALUE, QUEEN_VALUE, QUEEN_VALUE },
        new float[] { 0.075f, 0.075f, 0.1f, 0.14f, 0.14f, 0.1f, 0.075f, 0.075f },
        new float[] { 0.02f, 0.045f, 0.11f, 0.15f, 0.15f, 0.11f, 0.045f, 0.02f },
        new float[] { 0.025f, 0.08f, 0.13f, 0.16f, 0.16f, 0.13f, 0.08f, 0.025f },
        new float[] { 0.03f, 0.065f, 0.085f, 0.145f, 0.145f, 0.085f, 0.065f, 0.03f },
        new float[] { 0.035f, 0f, 0.035f, 0.035f, 0.035f, 0f, 0f, 0.035f },
        new float[] { 0.085f, 0.085f, 0.085f, -0.1f, -0.1f, 0.085f, 0.085f, 0.085f },
        new float[] { -0.175f, -0.175f, -0.175f, -0.175f, -0.175f, -0.175f, -0.175f, -0.175f }
    };

    // favors pawns closer to promotion
    // favors flank pawns more strongly
    // huge jump in pawn value from 5th rank to 6th rank
    private static final float[][] PAWN_PLACEMENT_TABLE_ENDGAME = new float[][]
    {
        new float[] { QUEEN_VALUE, QUEEN_VALUE, QUEEN_VALUE, QUEEN_VALUE, QUEEN_VALUE, QUEEN_VALUE, QUEEN_VALUE, QUEEN_VALUE },
        new float[] { 2.65f, 2f, 1.85f, 1.85f, 1.85f, 1.85f, 2f, 2.65f },
        new float[] { 1.75f, 1.65f, 1.6f, 1.6f, 1.6f, 1.6f, 1.65f, 1.75f },
        new float[] { 0.4f, 0.3f, 0.225f, 0.225f, 0.225f, 0.225f, 0.3f, 0.4f },
        new float[] { 0.175f, 0.15f, 0.125f, 0.125f, 0.125f, 0.125f, 0.15f, 0.175f },
        new float[] { -0.075f, -0.075f, -0.075f, -0.075f, -0.075f, -0.075f, -0.075f, -0.075f },
        new float[] { -0.15f, -0.15f, -0.15f, -0.15f, -0.15f, -0.15f, -0.15f, -0.15f },
        new float[] { -0.25f, -0.25f, -0.25f, -0.25f, -0.25f, -0.25f, -0.25f, -0.25f }
    };

    // favors knights closer to the center
    // knights on opponent's edge are not punished as harshly
    // because they are likely applying some pressure to the opponent's position
    private static final float[][] KNIGHT_PLACEMENT_TABLE = new float[][]
    {
        new float[] { -0.155f, -0.075f, -0.075f, -0.075f, -0.075f, -0.075f, -0.075f, -0.155f },
        new float[] { -0.135f, 0.035f, 0.05f, 0.05f, 0.05f, 0.05f, 0.035f, -0.135f },
        new float[] { -0.11f, 0.1f, 0.125f, 0.1f, 0.1f, 0.125f, 0.1f, -0.11f },
        new float[] { -0.1f, 0.125f, 0.175f, 0.175f, 0.175f, 0.175f, 0.125f, -0.1f },
        new float[] { -0.1f, 0.125f, 0.175f, 0.175f, 0.175f, 0.175f, 0.125f, -0.1f },
        new float[] { -0.125f, 0.1f, 0.125f, 0.1f, 0.1f, 0.125f, 0.1f, -0.125f },
        new float[] { -0.175f, 0.035f, 0.05f, 0.05f, 0.05f, 0.05f, 0.035f, -0.175f },
        new float[] { -0.25f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.25f }
    };

    // favors bishops closer to the center
    // slight buff given to bishops in fianchetto position
    // compared to other squares in the same rank
    private static final float[][] BISHOP_PLACEMENT_TABLE = new float[][]
    {
        new float[] { -0.2f, -0.25f, -0.25f, -0.25f, -0.25f, -0.25f, -0.25f, -0.2f },
        new float[] { -0.175f, 0.125f, 0.12f, 0.12f, 0.12f, 0.12f, 0.125f, -0.175f },
        new float[] { 0.15f, 0.14f, 0.15f, 0.15f, 0.15f, 0.15f, 0.14f, 0.15f },
        new float[] { -0.125f, 0.165f, 0.175f, 0.18f, 0.18f, 0.175f, 0.165f, -0.125f },
        new float[] { -0.12f, 0.155f, 0.1675f, 0.175f, 0.175f, 0.1675f, 0.155f, -0.12f },
        new float[] { -0.15f, 0.14f, 0.15f, 0.15f, 0.15f, 0.15f, 0.14f, -0.15f },
        new float[] { -0.175f, 0.125f, 0.12f, 0.12f, 0.12f, 0.12f, 0.125f, -0.175f },
        new float[] { -0.2f, -0.25f, -0.25f, -0.25f, -0.25f, -0.25f, -0.25f, -0.2f }
    };

    // favors rooks closer to the center
    // also rooks near the enemy's side
    // and rooks in the middle two files
    private static final float[][] ROOK_PLACEMENT_TABLE = new float[][]
    {
        new float[] { 0.125f, 0.145f, 0.16f, 0.185f, 0.185f, 0.16f, 0.145f, 0.125f },
        new float[] { 0.125f, 0.145f, 0.16f, 0.185f, 0.185f, 0.16f, 0.145f, 0.125f },
        new float[] { 0.1f, 0.135f, 0.15f, 0.15f, 0.15f, 0.15f, 0.135f, 0.1f },
        new float[] { 0.085f, 0.145f, 0.16f, 0.19f, 0.19f, 0.16f, 0.145f, 0.085f },
        new float[] { 0.05f, 0.145f, 0.16f, 0.19f, 0.19f, 0.16f, 0.145f, 0.05f },
        new float[] { 0f, 0.12f, 0.12f, 0.135f, 0.135f, 0.12f, 0.12f, 0f },
        new float[] { -0.1f, 0.065f, 0.065f, 0.085f, 0.085f, 0.065f, 0.065f, -0.1f },
        new float[] { -0.1f, -0.075f, 0.025f, 0.075f, 0.075f, 0.025f, -0.075f, -0.1f }
    };

    // favors queens closer to the center. harshly peanlizes queens at the edges
    // slightly buffs queens on the opponent's edge of the board
    private static final float[][] QUEEN_PLACEMENT_TABLE = new float[][]
    {
        new float[] { -0.125f, 0.035f, 0.045f, 0.045f, 0.045f, 0.045f, 0.035f, -0.125f },
        new float[] { -0.075f, 0.07f, 0.095f, 0.095f, 0.095f, 0.095f, 0.07f, -0.075f },
        new float[] { -0.035f, 0.085f, 0.1f, 0.125f, 0.125f, 0.1f, 0.085f, -0.035f },
        new float[] { 0f, 0.085f, 0.1f, 0.185f, 0.185f, 0.1f, 0.085f, 0f },
        new float[] { 0f, 0.085f, 0.1f, 0.185f, 0.185f, 0.1f, 0.085f, 0f },
        new float[] { -0.035f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, -0.035f },
        new float[] { -0.075f, 0.065f, 0.065f, 0.07f, 0.07f, 0.065f, 0.065f, -0.075f },
        new float[] { -0.125f, -0.025f, 0f, 0.045f, 0.045f, 0f, -0.025f, -0.125f }
    };

    // buffs hiding in the corners on the king's side
    // heavily punishes being in the opponent's side or in the middle
    private static final float[][] KING_PLACEMENT_TABLE_MIDDLEGAME = new float[][]
    {
        new float[] { -QUEEN_VALUE, -QUEEN_VALUE, -QUEEN_VALUE, -QUEEN_VALUE, -QUEEN_VALUE, -QUEEN_VALUE, -QUEEN_VALUE, -QUEEN_VALUE },
        new float[] { -4f, -5f, -6f, -8f, -8f, -6f, -5f, -4f },
        new float[] { -4f, -5f, -6f, -8f, -8f, -6f, -5f, -4f },
        new float[] { -4f, -5f, -6f, -8f, -8f, -6f, -5f, -4f },
        new float[] { -1f, -1.75f, -2.5f, -4f, -4f, -2.5f, -1.75f, -1f },
        new float[] { -0.185f, -0.3f, -0.45f, -0.75f, -0.75f, -0.45f, -0.3f, -0.185f },
        new float[] { 0.035f, 0.035f, 0.035f, -0.115f, -0.115f, 0.035f, 0.035f, 0.035f },
        new float[] { 0.175f, 0.15f, 0.1f, -0.1f, -0.1f, 0.1f, 0.15f, 0.175f }
    };

    // same as queen's placement table
    private static final float[][] KING_PLACEMENT_TABLE_ENDGAME = new float[][]
    {
        new float[] { -0.125f, 0.035f, 0.045f, 0.045f, 0.045f, 0.045f, 0.035f, -0.125f },
        new float[] { -0.075f, 0.07f, 0.095f, 0.095f, 0.095f, 0.095f, 0.07f, -0.075f },
        new float[] { -0.035f, 0.085f, 0.1f, 0.125f, 0.125f, 0.1f, 0.085f, -0.035f },
        new float[] { 0f, 0.085f, 0.1f, 0.185f, 0.185f, 0.1f, 0.085f, 0f },
        new float[] { 0f, 0.085f, 0.1f, 0.185f, 0.185f, 0.1f, 0.085f, 0f },
        new float[] { -0.035f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, -0.035f },
        new float[] { -0.075f, 0.065f, 0.065f, 0.07f, 0.07f, 0.065f, 0.065f, -0.075f },
        new float[] { -0.125f, -0.025f, 0f, 0.045f, 0.045f, 0f, -0.025f, -0.125f }
    };
    //#endregion
    
    //#region King safety movement penalty
    private static final float KING_AS_QUEEN_MOVEMENT_PENALTY = 0.075f;
    //#endregion
    //#endregion

    public ChessEngine(Board _board)
    {
        board = _board;
        rand = new Random();
    }

    //#region Searching
    /**
     * Finds the best move for the side to move according to the engine's internal Chess board.
     * This function uses the Negamax algorithm.
     * Various settings can be turned on and off using the boolean parameters.
     * @param _numPlies the max number of plies to search into
     * @param _shouldUseAlphaBetaPruning a boolean flag that determines if alpha beta pruning is used
     * @param _shouldUseHeuristicMoveOrdering a boolean flag that determines if heuristic move ordering is used
     * @param _numMovesEvaluatedReciever an object to count the number of moves evaluated.
     * If move counting is not required, set this parameter to null.
     * @return the best move for the side to move in the engine's internal board.
     * If the score of the move's score is positive, the position was evaluated as better for white,
     * but if the move's score is negative, the position is better for black.
     * @throws IllegalArgumentException if _numPlies is less than 0
     */
    public ScoredMove FindBestMove(int _numPlies, boolean _shouldUseAlphaBetaPruning,
    boolean _shouldUseHeuristicMoveOrdering, boolean _shouldUseQuiescence,
    MutableInt _numMovesEvaluatedReciever) throws IllegalArgumentException
    {
        //#region Initial exit conditions
        if (board.isDraw())
            return new ScoredMove(new Move(Square.NONE, Square.NONE), 0f);

        if (board.isMated())
        {
            Square _whiteKingLocation = board.getKingSquare(Side.WHITE);

            if (board.squareAttackedBy(_whiteKingLocation, Side.BLACK) != 0L)
                return new ScoredMove(new Move(Square.NONE, Square.NONE), Float.MAX_VALUE * GetSideMultiplier());

            else
                return new ScoredMove(new Move(Square.NONE, Square.NONE), Float.MAX_VALUE * GetSideMultiplier());
        }
        //#endregion

        //#region Argument checking
        if (_numPlies < 0)
        {
            throw new IllegalArgumentException("The \"maxDepth\" parameter for the given" +
            " SearchingArgs is " + _numPlies + " when it should be greater than or equal to 0.");
        }
        //#endregion

        List<Move> _legalMoves = board.legalMoves();
        if (_shouldUseHeuristicMoveOrdering)
            SortMovesHeuristically(_legalMoves, false, false);

        ScoredMove _bestMoveForSide = null;

        float _alpha = -Float.MAX_VALUE;
        float _beta = Float.MAX_VALUE;

        List<ScoredMove> _equalMoves = new ArrayList<>();

        System.out.println(_legalMoves);

        for (Move _curMove : _legalMoves)
        {
            board.doMove(_curMove);

            float _eval = -FindBestMove
            (
                _numPlies, _shouldUseAlphaBetaPruning, _shouldUseHeuristicMoveOrdering,
                _shouldUseQuiescence, _numMovesEvaluatedReciever, -_beta, -_alpha, 2
            );

            board.undoMove();

            if (_bestMoveForSide == null)
            {
                _bestMoveForSide = new ScoredMove(_curMove, _eval);
                _equalMoves.add(_bestMoveForSide);
            }

            else if (_eval + SCORE_TOLERANCE > _bestMoveForSide.getScore()
                && _eval - SCORE_TOLERANCE < _bestMoveForSide.getScore())
            {
                _equalMoves.add(_bestMoveForSide);
            }

            else
            {
                if (_eval > _bestMoveForSide.getScore())
                {
                    _bestMoveForSide = new ScoredMove(_curMove, _eval);
                    _equalMoves.clear();
                    _equalMoves.add(_bestMoveForSide);
                }
                
                _alpha = Math.max(_alpha, _eval);
                
                if (_shouldUseAlphaBetaPruning && _alpha >= _beta) break;
            }
        }

        // only with the initial call because this method
        // only returns the root move, not the line, so this won't
        // affect the outcome for other depths
        if (_equalMoves.size() != 1)
        {
            int _randIndex = rand.nextInt(_equalMoves.size());
            _bestMoveForSide = _equalMoves.get(_randIndex);
        }

        // changing score variable to be negative when
        // black is better, positive when white is better
        _bestMoveForSide = new ScoredMove
        (
            _bestMoveForSide.getMove(),
            GetSideMultiplier() * _bestMoveForSide.getScore()
        );

        return _bestMoveForSide;
    }

    /**
     * Internal function for recursion so that outside callers do not need to pass in every parameter.
     * @param _numPlies
     * @param _shouldUseAlphaBetaPruning
     * @param _shouldUseHeuristicMoveOrdering
     * @param _numPositionsEvaluatedReciever
     * @param _rootMove
     * @param _curPerspective
     * @param _alpha
     * @param _beta
     * @param _ply
     * @return the best move for the side to move, which is determined by the engine's Chess board.
     */
    private float FindBestMove(int _numPlies, boolean _shouldUseAlphaBetaPruning,
    boolean _shouldUseHeuristicMoveOrdering, boolean _shouldUseQuiescence,
    MutableInt _numPositionsEvaluatedReciever, float _alpha, float _beta, int _ply)
    {
        //#region Exit conditions
        if (board.isDraw()) return 0f;

        if (board.isMated())
        {
            Square _whiteKingLocation = board.getKingSquare(Side.WHITE);

            if (board.squareAttackedBy(_whiteKingLocation, Side.BLACK) != 0L)
                return Float.MAX_VALUE * GetSideMultiplier();

            // if white king is not attacked, black king must be mated
            else return Float.MAX_VALUE * GetSideMultiplier();
        }

        if (_ply > _numPlies)
        {
            if (_numPositionsEvaluatedReciever != null)
                _numPositionsEvaluatedReciever.increment();

            if (_shouldUseQuiescence)
                return Quiescence(_alpha, _beta) * GetSideMultiplier();

            return Evaluate(false) * GetSideMultiplier();
        }
        //#endregion

        List<Move> _sortedLegalMoves = board.legalMoves();

        if (_shouldUseHeuristicMoveOrdering) SortMovesHeuristically(_sortedLegalMoves, false, false);

        for (Move _curMove : _sortedLegalMoves)
        {
            board.doMove(_curMove);
            float _eval = -FindBestMove
            (
                _numPlies, _shouldUseAlphaBetaPruning, _shouldUseHeuristicMoveOrdering,
                _shouldUseQuiescence, _numPositionsEvaluatedReciever, -_beta, -_alpha, _ply + 1
            );
            board.undoMove();

            _alpha = Math.max(_alpha, _eval);

            if (_shouldUseAlphaBetaPruning && _alpha >= _beta) return _beta;
        }

        return _alpha;
    }
    
    public float Quiescence(float _alpha, float _beta)
    {
        float _standPat = Evaluate(false) * GetSideMultiplier();

        if (_standPat >= _beta)
        {
            // if (board.getHistory().contains(4232603434365899186L))
            //     System.out.println("Doing a cutoff. Stand pat of " + board.getFen() + " is " + _standPat);
            return _standPat;
        }
        _alpha = Math.max(_alpha, _standPat);

        List<Move> _captures = MoveGenerator.generatePseudoLegalCaptures(board);        
        SortMovesHeuristically(_captures, true, false);
        for (Move _capture : _captures)
        {
            board.doMove(_capture);
            float _eval = -Quiescence(-_beta, -_alpha);
            board.undoMove();

            // if (board.getHistory().contains(4232603434365899186L))
            // {
            //     System.out.println("Evaluation of move " + _capture
            //     + " for " + board.getSideToMove() + " is " + _eval);
            //     System.out.println();
            // }

            if (_eval >= _beta)
                return _eval;

            _alpha = Math.max(_alpha, _eval);
        }

        return _alpha;
    }
    //#endregion

    //#region Evaluating
    /**
     * Evaluates the position of the engine's Chess board.
     * @return the evaluation of the position. A positive number indicates a favorable position for
     * white, and a negative number favors black.
     */
    public float Evaluate(boolean _debug)
    {
        List<Square> _whitePawnLocations = board.getPieceLocation(Piece.WHITE_PAWN);
        List<Square> _whiteKnightLocations = board.getPieceLocation(Piece.WHITE_KNIGHT);
        List<Square> _whiteBishopLocations = board.getPieceLocation(Piece.WHITE_BISHOP);
        List<Square> _whiteRookLocations = board.getPieceLocation(Piece.WHITE_ROOK);
        Square _whiteQueenLocation = board.getFistPieceLocation(Piece.WHITE_QUEEN);
        Square _whiteKingLocation = board.getFistPieceLocation(Piece.WHITE_KING);
        boolean _isThereWhiteQueen = _whiteQueenLocation != Square.NONE;

        List<Square> _blackPawnLocations = board.getPieceLocation(Piece.BLACK_PAWN);
        List<Square> _blackKnightLocations = board.getPieceLocation(Piece.BLACK_KNIGHT);
        List<Square> _blackBishopLocations = board.getPieceLocation(Piece.BLACK_BISHOP);
        List<Square> _blackRookLocations = board.getPieceLocation(Piece.BLACK_ROOK);
        Square _blackQueenLocation = board.getFistPieceLocation(Piece.BLACK_QUEEN);
        Square _blackKingLocation = board.getFistPieceLocation(Piece.BLACK_KING);
        boolean _isThereBlackQueen = _blackQueenLocation != Square.NONE;

        final float _ENDGAME_WEIGHT = CalculateEndgameWeight
        (
            _whitePawnLocations, _whiteKnightLocations, _whiteBishopLocations,
            _whiteRookLocations, _whiteQueenLocation, _whiteKingLocation,
            _blackPawnLocations, _blackKnightLocations, _blackBishopLocations,
            _blackRookLocations, _blackQueenLocation, _blackKingLocation
        );

        float _whiteMaterial = EvaluateMaterial
        (
            _whitePawnLocations.size(), _whiteKnightLocations.size(),
            _whiteBishopLocations.size(), _whiteRookLocations.size(),
            _isThereWhiteQueen ? 1 : 0, _debug
        );

        float _blackMaterial = EvaluateMaterial
        (
            _blackPawnLocations.size(), _blackKnightLocations.size(),
            _blackBishopLocations.size(), _blackRookLocations.size(),
            _isThereBlackQueen ? 1 : 0, _debug
        );

        float _whitePosition = EvaluatePosition
        (
            Side.WHITE, _ENDGAME_WEIGHT, _whitePawnLocations, _whiteKnightLocations,
            _whiteBishopLocations, _whiteRookLocations, _whiteQueenLocation,
            _whiteKingLocation
        );


        float _blackPosition = EvaluatePosition
        (
            Side.BLACK, _ENDGAME_WEIGHT, _blackPawnLocations, _blackKnightLocations, _blackBishopLocations,
            _blackRookLocations, _blackQueenLocation, _blackKingLocation
        );

        if (_debug)
        {
            System.out.println("White mat: " + _whiteMaterial);
            System.out.println("Black mat: " + _blackMaterial);
            System.out.println("White pos: " + _whitePosition);
            System.out.println("Black pos: " + _blackPosition);
            System.out.println("Endgame weight: " + _ENDGAME_WEIGHT);
        }

        return (_whiteMaterial + _whitePosition) - (_blackMaterial + _blackPosition);
    }

    /**
     * <STRONG>IMPORTANT NOTE:</STRONG> A negative number does not mean
     * an evaluation that favors black and a positive number does
     * not mean an evaluation favors white necessarily.
     * That is applied in the "Evaluate" function.
     * <p>
     * Evaluates the material value of pawns and pieces.
     * This function does not accept a board and a side to evaluate
     * to optimize the "Evaluate" function. The number of pawns and pieces should
     * be counted from the same side, position, and board.
     * @param _numPawns the number of pawns for a side. Should be 8 to 0.
     * @param _numKnights the number of knights for a side. Should be 2 to 0.
     * @param _numBishops the number of bishops for a side. Should be 2 to 0.
     * @param _numRooks the number of rooks for a side. Should be 2 to 0.
     * @param _queenNum the number of queens. Should be 1 or 0.
     * @return the counted-up material. A more positive number indicates a better material count.
     */
    public float EvaluateMaterial(int _numPawns, int _numKnights, int _numBishops,
    int _numRooks, int _queenNum, boolean _debug)
    {
        if (_debug)
        {
            System.out.println("Num pawns: " + _numPawns);
            System.out.println("Num knights: " + _numKnights);
            System.out.println("Num bishops: " + _numBishops);
            System.out.println("Num rooks: " + _numRooks);
            System.out.println("Num queens: " + _queenNum);
        }
        float _totalMaterialValue = PAWN_VALUE * _numPawns + KNIGHT_VALUE * _numKnights
        + BISHOP_VALUE * _numBishops + ROOK_VALUE * _numRooks + QUEEN_VALUE * _queenNum;

        return _totalMaterialValue;
    }

    /**
     * <STRONG>IMPORTANT NOTE:</STRONG> A negative number does not mean
     * an evaluation that favors black and a positive number does
     * not mean an evaluation favors white necessarily.
     * That is applied in the "Evaluate" function.
     * <p>
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
     * @return the evaluation of the board for a side based on its positional aspects.
     * A more positive number indicates that the position is better for the side being evaluated.
     */
    private float EvaluatePosition(Side _side, float _endgameWeight,
    List<Square> _pawnLocations, List<Square> _knightLocations, List<Square> _bishopLocations,
    List<Square> _rookLocations, Square _queenLocation, Square _kingLocation)
    {
        return EvaluatePiecePlacement
        (
            _side, _endgameWeight, _pawnLocations, _knightLocations,
            _bishopLocations, _rookLocations, _queenLocation, _kingLocation
        )
        + EvaluatePieceMobility
        (
            _knightLocations, _bishopLocations,
            _rookLocations, _queenLocation
        )
        + EvaluatePawnStructure(_pawnLocations)
        // clamping so that king safety does not have an affect in the endgame
        + EvaluateKingSafety(_side, _kingLocation, _queenLocation) * Clamp(0.735f - _endgameWeight, 0f, 1f);
    }

    /**
     * Calculates the endgame weight according to the engine's Chess board
     * for use in other functions.
     * @param _pawnLocations
     * @param _knightLocations
     * @param _bishopLocations
     * @param _rookLocations
     * @param _queenLocation
     * @param _kingLocation
     * @param _enemyPawnLocations
     * @param _enemyKnightLocations
     * @param _enemyBishopLocations
     * @param _enemyRookLocations
     * @param _enemyQueenLocation
     * @param _enemyKingLocation
     * @return the endgame weight, a float between 0 and 1 that indicates
     * how close the game is to the endgame. Numbers closer to 1 are near the endgame,
     * while numbers closer to 0 are nearer to the opening or middle game.
     * The endgame weight is calculated based on the number of pieces on the board,
     * excluding the kings.
     */
    private float CalculateEndgameWeight(List<Square> _pawnLocations, List<Square> _knightLocations,
    List<Square> _bishopLocations, List<Square> _rookLocations, Square _queenLocation,
    Square _kingLocation, List<Square> _enemyPawnLocations, List<Square> _enemyKnightLocations,
    List<Square> _enemyBishopLocations, List<Square> _enemyRookLocations, Square _enemyQueenLocation,
    Square _enemyKingLocation)
    {
        // if all pawns & pieces are present, endgame weight is 0

        // at most, 16
        int _numPawns = _pawnLocations.size() + _enemyPawnLocations.size();
        // at most, 8
        int _numMinorPieces = _knightLocations.size() + _bishopLocations.size()
        + _enemyKnightLocations.size() + _enemyBishopLocations.size();
        // at most, 4
        int _numRooks = _rookLocations.size() + _enemyRookLocations.size();
        // at most, 2
        int _numQueens = (_queenLocation == Square.NONE ? 0 : 1)
        + (_enemyQueenLocation == Square.NONE ? 0 : 1);

        float _endgameWeight = 1f;

        final float _PIECE_WEIGHT_SUM = (_numPawns * PAWN_VALUE * PAWN_WEIGHT) +
        (_numMinorPieces * BISHOP_VALUE * MINOR_PIECE_WEIGHT) + (_numRooks * ROOK_VALUE * ROOK_WEIGHT)
        + (_numQueens * QUEEN_VALUE * QUEEN_WEIGHT);
        final float _MAX_WEIGHT_SUM = 16 * PAWN_VALUE * PAWN_WEIGHT + 8 * BISHOP_VALUE * MINOR_PIECE_WEIGHT
        + 4 * ROOK_VALUE * ROOK_WEIGHT + 2 * QUEEN_VALUE * QUEEN_WEIGHT;

        // dividing by max result to "normalize" the subtraction
        // and keep it in the range 0 to 1
        _endgameWeight -= _PIECE_WEIGHT_SUM / _MAX_WEIGHT_SUM;

        return _endgameWeight;
    }

    /**
     * <STRONG>IMPORTANT NOTE:</STRONG> A negative number does not mean
     * an evaluation that favors black and a positive number does
     * not mean an evaluation favors white necessarily.
     * That is applied in the "Evaluate" function.
     * <p>
     * Evaluates the placement of each piece based on the private
     * piece square tables in the engine. All given pawn and piece locations should be from the same
     * side and board.
     * @param _perspective the side whose pieces are being evaluated
     * @param _endgameWeight a float from 0-1 representing how close the game is to the endgame.
     * A value closer to 1 represents a game closer to the endgame.
     * @param _pawnLocations the squares of each pawn
     * @param _knightLocations the squares of each knight
     * @param _bishopLocations the squares of each bishop
     * @param _rookLocations the squares of each rook
     * @param _queenLocation the square of the queen
     * @param _kingLocation the square of the king
     * @return the evaluation of the piece locations. A negative value indicates bad piece placement,
     * while a positive value means the opposite.
     */
    private float EvaluatePiecePlacement(Side _perspective, float _endgameWeight,
    List<Square> _pawnLocations, List<Square> _knightLocations, List<Square> _bishopLocations,
    List<Square> _rookLocations, Square _queenLocation, Square _kingLocation)
    {
        if (_perspective.equals(Side.WHITE))
        {
            float _piecePlacementEvaluation = 0f;

            for (Square _pawnLocation : _pawnLocations)
            {
                int _row = 7 - _pawnLocation.getRank().ordinal();
                int _column = _pawnLocation.getFile().ordinal();

                _piecePlacementEvaluation += Lerp
                (
                    PAWN_PLACEMENT_TABLE_MIDDLEGAME[_row][_column],
                    PAWN_PLACEMENT_TABLE_ENDGAME[_row][_column], _endgameWeight
                );
            }

            for (Square _knightLocation : _knightLocations)
            {
                int _row = 7 - _knightLocation.getRank().ordinal();
                int _column = _knightLocation.getFile().ordinal();

                _piecePlacementEvaluation += KNIGHT_PLACEMENT_TABLE[_row][_column];
            }

            for (Square _bishopLocation : _bishopLocations)
            {
                int _row = 7 - _bishopLocation.getRank().ordinal();
                int _column = _bishopLocation.getFile().ordinal();

                _piecePlacementEvaluation += BISHOP_PLACEMENT_TABLE[_row][_column];
            }

            for (Square _rookLocation : _rookLocations)
            {
                int _row = 7 - _rookLocation.getRank().ordinal();
                int _column = _rookLocation.getFile().ordinal();

                _piecePlacementEvaluation += ROOK_PLACEMENT_TABLE[_row][_column];
            }

            if (!_queenLocation.equals(Square.NONE))
            {
                int _queenRow = 7 - _queenLocation.getRank().ordinal();
                int _queenColumn = _queenLocation.getFile().ordinal();

                _piecePlacementEvaluation += QUEEN_PLACEMENT_TABLE[_queenRow][_queenColumn];
            }

            int _kingRow = 7 - _kingLocation.getRank().ordinal();
            int _kingColumn = _kingLocation.getFile().ordinal();

            // doing this if it is most likely the endgame instead of lerping
            // because of how strong the punishments are for being in center
            // in the king middlegame table
            if (_endgameWeight > 0.7) _piecePlacementEvaluation +=
            KING_PLACEMENT_TABLE_ENDGAME[_kingRow][_kingColumn];

            else _piecePlacementEvaluation +=
            Lerp
            (
                KING_PLACEMENT_TABLE_MIDDLEGAME[_kingRow][_kingColumn],
                KING_PLACEMENT_TABLE_ENDGAME[_kingRow][_kingColumn], _endgameWeight
            );

            return _piecePlacementEvaluation;
        }

        else
        {
            float _piecePlacementEvaluation = 0f;

            for (Square _pawnLocation : _pawnLocations)
            {
                int _row = _pawnLocation.getRank().ordinal();
                int _column = 7 - _pawnLocation.getFile().ordinal();

                _piecePlacementEvaluation += Lerp
                (
                    PAWN_PLACEMENT_TABLE_MIDDLEGAME[_row][_column],
                    PAWN_PLACEMENT_TABLE_ENDGAME[_row][_column], _endgameWeight
                );
            }

            for (Square _knightLocation : _knightLocations)
            {
                int _row = _knightLocation.getRank().ordinal();
                int _column = 7 - _knightLocation.getFile().ordinal();

                _piecePlacementEvaluation += KNIGHT_PLACEMENT_TABLE[_row][_column];
            }

            for (Square _bishopLocation : _bishopLocations)
            {
                int _row = _bishopLocation.getRank().ordinal();
                int _column = 7 - _bishopLocation.getFile().ordinal();

                _piecePlacementEvaluation += BISHOP_PLACEMENT_TABLE[_row][_column];
            }

            for (Square _rookLocation : _rookLocations)
            {
                int _row = _rookLocation.getRank().ordinal();
                int _column = 7 - _rookLocation.getFile().ordinal();

                _piecePlacementEvaluation += ROOK_PLACEMENT_TABLE[_row][_column];
            }

            if (!_queenLocation.equals(Square.NONE))
            {
                int _queenRow = _queenLocation.getRank().ordinal();
                int _queenColumn = 7 - _queenLocation.getFile().ordinal();
    
                _piecePlacementEvaluation += QUEEN_PLACEMENT_TABLE[_queenRow][_queenColumn];
            }

            int _kingRow = _kingLocation.getRank().ordinal();
            int _kingColumn = 7 - _kingLocation.getFile().ordinal();

            // doing this if it is most likely the endgame instead of lerping
            // because of how strong the punishments are for being in center
            // in the king middlegame table
            if (_endgameWeight > 0.7) _piecePlacementEvaluation +=
            KING_PLACEMENT_TABLE_ENDGAME[_kingRow][_kingColumn];

            else  _piecePlacementEvaluation +=
            Lerp
            (
                KING_PLACEMENT_TABLE_MIDDLEGAME[_kingRow][_kingColumn],
                KING_PLACEMENT_TABLE_ENDGAME[_kingRow][_kingColumn], _endgameWeight
            );

            return _piecePlacementEvaluation;
        }
    }

    // TODO
    private float EvaluatePieceMobility(List<Square> _knightLocations,
    List<Square> _bishopLocations, List<Square> _rookLocations, Square _queenLocation)
    {
        // knights should be able to make more than three moves
        // List<Move> _knightMoves = new ArrayList<>();
        // MoveGenerator.generateKnightMoves(board, _knightMoves);

        // bishops should be able to make more than 4 moves

        // rooks should be able to make more than x moves

        // queens should be able to make more than x moves

        return 0f;
    }

    /**
     * <STRONG>IMPORTANT NOTE:</STRONG> A negative number does not mean
     * an evaluation that favors black and a positive number does
     * not mean an evaluation favors white necessarily.
     * That is applied in the "Evaluate" function.
     * <p>
     * Evaluates the pawn structure of a side.
     * <p>
     * The pawn locations should be counted from the same side, position, and board.
     * The function takes in the pawn locations to optimize the "Evaluation" function.
     * @param _pawnLocations the locations of every pawn of a side
     * @return the evaluation of the pawn structure of a side.
     * A more positive number means that the pawn structure is better for the side being evaluted.
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

    // TODO: fix error that occurs when this function is uncommented and playing a game
    private float EvaluateKingSafety(Side _side, Square _kingLocation, Square _queenLocation)
    {
        // board.setPiece(Piece.make(_side, PieceType.QUEEN), _kingLocation);
        // board.setPiece(Piece.make(_side, PieceType.PAWN), _queenLocation);

        // List<Move> _movesIfKingWasQueen = new ArrayList<>();
        // MoveGenerator.generateQueenMoves(board, _movesIfKingWasQueen);

        // board.setPiece(Piece.make(_side, PieceType.KING), _kingLocation);
        // board.setPiece(Piece.make(_side, PieceType.QUEEN), _queenLocation);

        // return _movesIfKingWasQueen.size() * KING_AS_QUEEN_MOVEMENT_PENALTY;
        return 0f;
    }
    //#endregion

    //#region Other
    // TODO
    // make mvv-lva moves first always
    /**
     * Orders the given moves to prioritize moves that were
     * heuristically evaluated as "better" to optimize alpha beta pruning.
     * <p>
     * If illegal moves are given and the <code>_isQuiescence</code> flag is true,
     * the function will additionally remove those moves from the list.
     * @param _movesToOrder the moves to order
     */
    private void SortMovesHeuristically(List<Move> _movesToOrder, boolean _isQuiescence, boolean _shouldDebug)
    {
        if (_shouldDebug)
        {
            System.out.println(board.toString());
            System.out.println();
        }

        List<ScoredMove> _movesToOrderScored = new ArrayList<>();
        
        for (Move _moveToOrder : _movesToOrder)
        {
            if (_isQuiescence)
            {
                try {
                    if (!board.isMoveLegal(_moveToOrder, false))
                        continue;
                }
                
                catch (Exception e) {
                    System.out.println("Board:\n" + board);
                    System.out.println("Fen: " + board.getFen());
                    System.out.println("Move: " + _moveToOrder);
                    throw new IllegalAccessError();
                }
            }

            float _moveEvaluationGuess = 0f;
            Piece _movingPiece = board.getPiece(_moveToOrder.getFrom());
            Piece _attackedPiece = board.getPiece(_moveToOrder.getTo());

            // if the other side attacks the to square
            if (board.squareAttackedBy(_moveToOrder.getTo(), _movingPiece.getPieceSide().flip()) != 0L)
            {
                // penalty if the piece's side doesn't defend the to square
                if (board.squareAttackedBy(_moveToOrder.getTo(), _movingPiece.getPieceSide()) == 0L)
                {
                    _moveEvaluationGuess -= GetPieceValue(_movingPiece);
                }

                // bonus for lower-valued piece taking higher valued piece.
                // mvv, lva
                if (_attackedPiece != Piece.NONE
                    && GetPieceValue(_movingPiece) < GetPieceValue(_attackedPiece))
                {
                    _moveEvaluationGuess += GetPieceValue(_attackedPiece) - GetPieceValue(_movingPiece);
                }
            }

            // enemy doesn't attack square
            else
            {
                // bonus for promoting
                if (_movingPiece.getPieceType().equals(PieceType.PAWN))
                {
                    if (_movingPiece.getPieceSide().equals(Side.WHITE))
                    {
                        if (_moveToOrder.getTo().getRank().equals(Rank.RANK_8))
                        {
                            _moveEvaluationGuess += QUEEN_VALUE;
                        }
                    }

                    else
                    {
                        if (_moveToOrder.getTo().getRank().equals(Rank.RANK_1))
                        {
                            _moveEvaluationGuess += QUEEN_VALUE;
                        }
                    }
                }

                // bonus for taking free piece
                if (_attackedPiece != Piece.NONE)
                    _moveEvaluationGuess += GetPieceValue(_attackedPiece);
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

        // should find a better way to sort
        // 1 N*log(N) sort and 2 N operations
        // One N operation is clearing
        // the other is adding to _movesToOrder list
        
        // maybe remove this and opt for
        // manually finding best move (O(N^2) worst case, but one of
        // the first moves likely cause a cutoff)
        Collections.sort(_movesToOrderScored);

        _movesToOrder.clear();
        for (ScoredMove _moveToOrderScored : _movesToOrderScored)
        {
            _movesToOrder.add(_moveToOrderScored.getMove());
        }

        if (_shouldDebug)
        {
            System.out.println(_movesToOrderScored);
    
            System.out.println();
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
    
    private float Lerp(float _valOne, float _valTwo, float _time)
    {
        return _valOne + _time * (_valTwo - _valOne);
    }
    
    private float Clamp(float _clampTarget, float _min, float _max)
    {
        if (_clampTarget > _max)
            _clampTarget = _max;

        if (_clampTarget < _min)
            _clampTarget = _min;

        return _clampTarget;
    }
    
    private int GetSideMultiplier()
    {
        return board.getSideToMove() == Side.WHITE ? 1 : -1;
    }
    //#endregion
}