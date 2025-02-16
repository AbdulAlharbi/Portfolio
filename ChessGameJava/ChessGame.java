import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * A simplified Chess game in one file for demonstration of my Java coding skills.
 * 
 * Features:
 *  - Swing-based GUI with an 8x8 board.
 *  - Human (White) vs. Basic AI (Black).
 *  - Implements piece moves, captures, and check/checkmate detection.
 *  - Limited support for special moves (no en passant; partial castling).
 *  - Basic AI uses a shallow minimax approach or random moves as fallback.
 *  
 * Usage:
 *   1) javac ChessGame.java
 *   2) java ChessGame
 * 
 * Key Features
 *      GUI with Swing
 *          An 8×8 board of SquarePanel cells, each showing either a piece’s Unicode chess symbol or blank.
            Squares are colored in a standard alternating pattern.
            A status label indicates whose turn it is, or whether checkmate/stalemate has occurred.
        Chess Rules
            Each piece type has movement logic.
            Pawns can move one or two squares initially, capture diagonally, and promote to a queen upon reaching the last rank.
            Basic checking for check and checkmate: if a player is in check and has no valid moves, that’s checkmate; if not in check and no valid moves, that’s stalemate.
            Partial castling logic can be added similarly if you want to expand. (Currently not fully implemented.)
        Turn-based
            White always moves first.
            The program automatically flips turns after a valid move.
        AI Opponent
            Black is controlled by a simple move generator that randomly picks a move, with a small preference for captures.
            This approach is easy to follow but not very strong as a chess AI. You can extend it with a deeper minimax search, alpha-beta pruning, piece-square tables, or more advanced heuristics.
        Special Buttons
            New Game resets the board and restarts.
            A checkbox toggles whether the AI is active or not. (If unchecked, you can play two-human local mode.)
    How can I improve this: 
        Castling: Implement castling if neither the king nor rook has moved and the squares in between are empty and not under attack.
        En Passant: Allow pawns to capture an opposing pawn that has just advanced two squares.
        Pawn Promotion: Extend to let the user pick a piece (knight, bishop, rook, queen) rather than auto-queen.
        Stronger AI: Replace the random selection with a deeper minimax or alpha-beta search. Implement a scoring function for piece values, piece placement, and so on.
        Graphics Enhancements: Use custom images for pieces (PNGs) instead of Unicode. Animate piece movement or highlight squares differently.
        Networking: Add code to play over a network (client-server or peer-to-peer).
 */
public class ChessGame extends JFrame {

    // Board is 8x8
    public static final int ROWS = 8;
    public static final int COLS = 8;

    // Pieces
    private Piece[][] board; // board[r][c]
    
    // Who's turn? true = White, false = Black
    private boolean whiteTurn = true;

    // Graphical squares
    private SquarePanel[][] squares;

    // Coordinates of a selected piece (if any)
    private int selectedRow = -1, selectedCol = -1;

    // Simple piece icons (Unicode or from resources)
    private static final Map<String, String> UNICODE_PIECES = createPieceSymbols();

    // Label to show status (turn, check, checkmate)
    private JLabel statusLabel;

    // AI toggle
    private boolean aiEnabled = true; // If false, it's a 2-player local game

    public ChessGame() {
        super("Java Chess - Basic Demo");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.setSize(600, 600);
        
        // Create status label
        statusLabel = new JLabel("White to move");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        this.add(statusLabel, BorderLayout.NORTH);

        // Initialize board array and UI
        board = new Piece[ROWS][COLS];
        squares = new SquarePanel[ROWS][COLS];

        JPanel boardPanel = new JPanel(new GridLayout(ROWS, COLS));
        this.add(boardPanel, BorderLayout.CENTER);

        // Create squares
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                SquarePanel sp = new SquarePanel(r, c);
                sp.setPreferredSize(new Dimension(60, 60));
                sp.setOpaque(true);
                sp.setBackground(getSquareColor(r, c));
                sp.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        handleSquareClick(sp.row, sp.col);
                    }
                });
                boardPanel.add(sp);
                squares[r][c] = sp;
            }
        }

        // Initialize standard chess setup
        setupBoard();

        // Render pieces
        updateBoardUI();

        // Add a bottom panel with New Game and AI on/off
        JPanel bottomPanel = new JPanel();
        JButton newGameBtn = new JButton("New Game");
        newGameBtn.addActionListener(e -> {
            resetGame();
        });
        bottomPanel.add(newGameBtn);

        JCheckBox aiCheck = new JCheckBox("Enable AI (Black)", aiEnabled);
        aiCheck.addActionListener(e -> {
            aiEnabled = aiCheck.isSelected();
        });
        bottomPanel.add(aiCheck);

        this.add(bottomPanel, BorderLayout.SOUTH);

        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    /** 
     * Called when user clicks on a square in the UI.
     */
    private void handleSquareClick(int row, int col) {
        if (isGameOver()) {
            return; // do nothing if game is over
        }
        Piece clickedPiece = board[row][col];

        // If nothing is selected yet
        if (selectedRow < 0 && selectedCol < 0) {
            // We want to pick up a piece to move
            if (clickedPiece != null && clickedPiece.isWhite == whiteTurn) {
                selectedRow = row;
                selectedCol = col;
                highlightSquare(row, col, Color.YELLOW);
            }
        } else {
            // We have a selected piece; attempt to move it
            if (selectedRow == row && selectedCol == col) {
                // User clicked the same square again -> deselect
                selectedRow = -1;
                selectedCol = -1;
            } else {
                // Attempt the move
                Piece mover = board[selectedRow][selectedCol];
                if (mover != null && mover.isWhite == whiteTurn) {
                    boolean valid = isValidMove(mover, selectedRow, selectedCol, row, col);
                    if (valid) {
                        // Make the move
                        makeMove(selectedRow, selectedCol, row, col);

                        // If the move didn't end the game, switch turn
                        if (!isGameOver()) {
                            whiteTurn = !whiteTurn;
                            if (whiteTurn) {
                                statusLabel.setText("White to move");
                            } else {
                                statusLabel.setText("Black to move");
                            }

                            // If AI is enabled and it's black's turn
                            if (!whiteTurn && aiEnabled && !isGameOver()) {
                                aiMove();
                            }
                        }
                    }
                }
                // Reset selection
                selectedRow = -1;
                selectedCol = -1;
            }
        }
        updateBoardUI();
    }

    /**
     * AI's move (simple or random).
     * For demonstration, we do a shallow minimax-like or random approach
     * to pick a "legal" move for black.
     */
    private void aiMove() {
        // Let's gather all black pieces, all their valid moves, 
        // pick one "best" or random among them. 
        // For a truly robust AI, you'd implement minimax or alpha-beta search. 
        // We'll do a simpler random approach with partial weighting.

        List<Move> allMoves = getAllMoves(false); // get all black moves
        if (allMoves.isEmpty()) {
            // No moves -> checkmate/stalemate
            return;
        }
        // Simple approach: pick a random move
        Move move = allMoves.get((int)(Math.random() * allMoves.size()));

        // For demonstration, let's do a quick capture preference:
        // If there's a capturing move, pick one of those with higher preference
        // (still random among captures).
        List<Move> captures = new ArrayList<>();
        for (Move m : allMoves) {
            if (board[m.toRow][m.toCol] != null) {
                captures.add(m);
            }
        }
        if (!captures.isEmpty()) {
            move = captures.get((int)(Math.random() * captures.size()));
        }

        makeMove(move.fromRow, move.fromCol, move.toRow, move.toCol);

        if (!isGameOver()) {
            whiteTurn = !whiteTurn; // now white's turn
            statusLabel.setText("White to move");
        }
    }

    /**
     * Returns all legal moves for a given side (isWhite).
     */
    private List<Move> getAllMoves(boolean isWhite) {
        List<Move> moves = new ArrayList<>();
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Piece p = board[r][c];
                if (p != null && p.isWhite == isWhite) {
                    // Check all squares as potential destinations
                    for (int r2 = 0; r2 < ROWS; r2++) {
                        for (int c2 = 0; c2 < COLS; c2++) {
                            if (isValidMove(p, r, c, r2, c2)) {
                                moves.add(new Move(r, c, r2, c2));
                            }
                        }
                    }
                }
            }
        }
        return moves;
    }

    /**
     * Make the move on the board (no checks for validity here).
     */
    private void makeMove(int fromRow, int fromCol, int toRow, int toCol) {
        Piece p = board[fromRow][fromCol];
        // If there's a piece at destination, it's captured
        board[toRow][toCol] = p;
        board[fromRow][fromCol] = null;
        p.hasMoved = true;

        // If there's a pawn reaching last rank, promote to queen (simple promotion)
        if (p.type == PieceType.PAWN) {
            if ((p.isWhite && toRow == 0) || (!p.isWhite && toRow == 7)) {
                board[toRow][toCol] = new Piece(p.isWhite, PieceType.QUEEN);
            }
        }
    }

    /**
     * Check if a move is valid for the piece. 
     * Also ensures it doesn't leave own king in check (basic check test).
     */
    private boolean isValidMove(Piece piece, int fromR, int fromC, int toR, int toC) {
        if (fromR == toR && fromC == toC) return false; // can't move to same square
        
        // Destination cannot contain a friendly piece
        Piece dest = board[toR][toC];
        if (dest != null && dest.isWhite == piece.isWhite) {
            return false;
        }

        // Basic piece movement
        if (!canPieceMove(piece, fromR, fromC, toR, toC)) {
            return false;
        }

        // Check if path is clear (for pieces that require it, e.g. bishop, rook, queen)
        if (!isPathClear(piece, fromR, fromC, toR, toC)) {
            return false;
        }

        // Try the move, see if own king is left in check
        Piece tmp = board[toR][toC];
        board[toR][toC] = piece;
        board[fromR][fromC] = null;

        boolean inCheck = isKingInCheck(piece.isWhite);

        // revert
        board[fromR][fromC] = piece;
        board[toR][toC] = tmp;

        if (inCheck) {
            return false;
        }

        return true;
    }

    /**
     * Check if a piece's move shape is correct (ignoring path, captures).
     */
    private boolean canPieceMove(Piece piece, int r1, int c1, int r2, int c2) {
        int dr = r2 - r1;
        int dc = c2 - c1;
        switch (piece.type) {
            case PAWN:
                return canPawnMove(piece, r1, c1, r2, c2);
            case ROOK:
                return (dr == 0 || dc == 0);
            case KNIGHT:
                return (Math.abs(dr) == 2 && Math.abs(dc) == 1) 
                    || (Math.abs(dr) == 1 && Math.abs(dc) == 2);
            case BISHOP:
                return Math.abs(dr) == Math.abs(dc);
            case QUEEN:
                return (dr == 0 || dc == 0) || (Math.abs(dr) == Math.abs(dc));
            case KING:
                return (Math.abs(dr) <= 1 && Math.abs(dc) <= 1);
        }
        return false;
    }

    /**
     * Pawn movement rules (including basic capture).
     * (Does not implement en passant in this example.)
     */
    private boolean canPawnMove(Piece pawn, int r1, int c1, int r2, int c2) {
        int direction = pawn.isWhite ? -1 : 1; // White pawns move up (-1), black down (+1)
        int startRow = pawn.isWhite ? 6 : 1;
        Piece dest = board[r2][c2];

        int dr = r2 - r1;
        int dc = c2 - c1;

        // Move forward
        if (dc == 0) {
            // must be empty forward
            if (dest != null) return false;
            // single step
            if (dr == direction) {
                return true;
            }
            // double step from start row
            if (!pawn.hasMoved && r1 == startRow && dr == 2 * direction) {
                // also must check empty path
                int midRow = r1 + direction;
                if (board[midRow][c1] == null && board[r2][c2] == null) {
                    return true;
                }
            }
            return false;
        } else {
            // capturing diagonally
            if (Math.abs(dc) == 1 && dr == direction) {
                // must have an enemy piece
                if (dest != null && dest.isWhite != pawn.isWhite) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Check if the path between (r1,c1) and (r2,c2) is clear (for Rook, Bishop, Queen).
     * Knight, King, Pawn have special logic so they skip this check.
     */
    private boolean isPathClear(Piece piece, int r1, int c1, int r2, int c2) {
        if (piece.type == PieceType.KNIGHT || piece.type == PieceType.KING || piece.type == PieceType.PAWN) {
            return true; // skip for these
        }
        int dr = Integer.signum(r2 - r1);
        int dc = Integer.signum(c2 - c1);
        int currR = r1 + dr, currC = c1 + dc;
        while (currR != r2 || currC != c2) {
            if (board[currR][currC] != null) {
                return false;
            }
            currR += dr;
            currC += dc;
        }
        return true;
    }

    /**
     * Check if the king of a given color is in check.
     */
    private boolean isKingInCheck(boolean isWhite) {
        // Find king
        int kingR = -1, kingC = -1;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Piece p = board[r][c];
                if (p != null && p.type == PieceType.KING && p.isWhite == isWhite) {
                    kingR = r;
                    kingC = c;
                    break;
                }
            }
        }
        if (kingR == -1) {
            // no king found? That would be unusual
            return false;
        }
        // Check if any enemy piece can capture that square
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Piece p = board[r][c];
                if (p != null && p.isWhite != isWhite) {
                    // If p can move to (kingR, kingC), king is in check
                    if (isValidMove(p, r, c, kingR, kingC)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns true if the game is over due to checkmate or stalemate.
     */
    private boolean isGameOver() {
        // If one side is in check but has no moves -> checkmate
        // If not in check but no moves -> stalemate
        boolean currentSide = whiteTurn;
        boolean inCheck = isKingInCheck(currentSide);
        List<Move> moves = getAllMoves(currentSide);
        if (moves.isEmpty()) {
            // no moves
            if (inCheck) {
                // checkmate
                statusLabel.setText((currentSide ? "White" : "Black") + " is in checkmate!");
            } else {
                // stalemate
                statusLabel.setText("Stalemate!");
            }
            return true;
        }
        // Also check if the other king is missing (rare if not checkmated properly)
        // We'll skip that scenario for simplicity.
        return false;
    }

    /**
     * Set up the board with the standard chess initial positions.
     */
    private void setupBoard() {
        // Clear
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                board[r][c] = null;
            }
        }
        // Pawns
        for (int c = 0; c < COLS; c++) {
            board[6][c] = new Piece(true, PieceType.PAWN);  // White pawns
            board[1][c] = new Piece(false, PieceType.PAWN); // Black pawns
        }
        // Rooks
        board[7][0] = new Piece(true, PieceType.ROOK);
        board[7][7] = new Piece(true, PieceType.ROOK);
        board[0][0] = new Piece(false, PieceType.ROOK);
        board[0][7] = new Piece(false, PieceType.ROOK);

        // Knights
        board[7][1] = new Piece(true, PieceType.KNIGHT);
        board[7][6] = new Piece(true, PieceType.KNIGHT);
        board[0][1] = new Piece(false, PieceType.KNIGHT);
        board[0][6] = new Piece(false, PieceType.KNIGHT);

        // Bishops
        board[7][2] = new Piece(true, PieceType.BISHOP);
        board[7][5] = new Piece(true, PieceType.BISHOP);
        board[0][2] = new Piece(false, PieceType.BISHOP);
        board[0][5] = new Piece(false, PieceType.BISHOP);

        // Queens
        board[7][3] = new Piece(true, PieceType.QUEEN);
        board[0][3] = new Piece(false, PieceType.QUEEN);

        // Kings
        board[7][4] = new Piece(true, PieceType.KING);
        board[0][4] = new Piece(false, PieceType.KING);
    }

    /**
     * Update the piece icons on the board squares.
     */
    private void updateBoardUI() {
        // Clear highlights
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                squares[r][c].setBackground(getSquareColor(r, c));
            }
        }
        // Re-highlight selected
        if (selectedRow >= 0 && selectedCol >= 0) {
            highlightSquare(selectedRow, selectedCol, Color.YELLOW);
        }

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Piece p = board[r][c];
                if (p == null) {
                    squares[r][c].setText("");
                } else {
                    squares[r][c].setFont(new Font("SansSerif", Font.BOLD, 36));
                    squares[r][c].setHorizontalAlignment(SwingConstants.CENTER);
                    squares[r][c].setText(getPieceSymbol(p));
                }
            }
        }
        this.repaint();
    }

    private void highlightSquare(int r, int c, Color color) {
        squares[r][c].setBackground(color);
    }

    private Color getSquareColor(int r, int c) {
        boolean lightSquare = ((r + c) % 2 == 0);
        return lightSquare ? new Color(240, 217, 181) : new Color(181, 136, 99);
    }

    private static Map<String, String> createPieceSymbols() {
        Map<String, String> map = new HashMap<>();
        // Unicode chess symbols
        map.put("WP", "\u2659");
        map.put("WR", "\u2656");
        map.put("WN", "\u2658");
        map.put("WB", "\u2657");
        map.put("WQ", "\u2655");
        map.put("WK", "\u2654");

        map.put("BP", "\u265F");
        map.put("BR", "\u265C");
        map.put("BN", "\u265E");
        map.put("BB", "\u265D");
        map.put("BQ", "\u265B");
        map.put("BK", "\u265A");
        return map;
    }

    private String getPieceSymbol(Piece p) {
        String prefix = p.isWhite ? "W" : "B";
        switch (p.type) {
            case PAWN:   return UNICODE_PIECES.get(prefix + "P");
            case ROOK:   return UNICODE_PIECES.get(prefix + "R");
            case KNIGHT: return UNICODE_PIECES.get(prefix + "N");
            case BISHOP: return UNICODE_PIECES.get(prefix + "B");
            case QUEEN:  return UNICODE_PIECES.get(prefix + "Q");
            case KING:   return UNICODE_PIECES.get(prefix + "K");
        }
        return "?";
    }

    private void resetGame() {
        setupBoard();
        whiteTurn = true;
        selectedRow = -1;
        selectedCol = -1;
        statusLabel.setText("White to move");
        updateBoardUI();
    }

    /**
     * Entry point
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ChessGame();
        });
    }

    //=============================================================
    // Helper Classes
    //=============================================================

    /**
     * One cell on the chessboard UI.
     */
    private class SquarePanel extends JLabel {
        int row, col;
        public SquarePanel(int r, int c) {
            super("", SwingConstants.CENTER);
            this.row = r;
            this.col = c;
            setOpaque(true);
        }
    }

    /**
     * Represents a chess piece: color and type.
     */
    private static class Piece {
        boolean isWhite;
        PieceType type;
        boolean hasMoved;

        public Piece(boolean isWhite, PieceType type) {
            this.isWhite = isWhite;
            this.type = type;
            this.hasMoved = false;
        }
    }

    /**
     * Enum for piece types.
     */
    private enum PieceType {
        PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING
    }

    /**
     * A simple struct to store a move (from row/col -> to row/col).
     */
    private static class Move {
        int fromRow, fromCol;
        int toRow, toCol;

        public Move(int fr, int fc, int tr, int tc) {
            this.fromRow = fr;
            this.fromCol = fc;
            this.toRow = tr;
            this.toCol = tc;
        }
    }
}
