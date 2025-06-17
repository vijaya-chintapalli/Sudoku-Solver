import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SudokuGUI extends JFrame {
    private static final int SIZE = 9;
    private JTextField[][] cells = new JTextField[SIZE][SIZE];
    private int selectedRow = 0, selectedCol = 0;

    public SudokuGUI() {
        setTitle("Sudoku Solver");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 650);
        setLayout(new BorderLayout());

        // 🎉 Set font for JOptionPane that supports emoji
        UIManager.put("OptionPane.messageFont", new Font("Segoe UI Emoji", Font.PLAIN, 16));
        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI Emoji", Font.PLAIN, 14));

        JPanel grid = new JPanel(new GridLayout(SIZE, SIZE));

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                JTextField cell = new JTextField();
                cell.setHorizontalAlignment(JTextField.CENTER);
                cell.setFont(new Font("SansSerif", Font.BOLD, 20));

                int r = row, c = col;

                cell.addFocusListener(new FocusAdapter() {
                    public void focusGained(FocusEvent e) {
                        selectedRow = r;
                        selectedCol = c;
                    }
                });

                cell.addKeyListener(new KeyAdapter() {
                    public void keyPressed(KeyEvent e) {
                        switch (e.getKeyCode()) {
                            case KeyEvent.VK_LEFT:
                                moveCell(selectedRow, selectedCol - 1);
                                e.consume();
                                break;
                            case KeyEvent.VK_RIGHT:
                                moveCell(selectedRow, selectedCol + 1);
                                e.consume();
                                break;
                            case KeyEvent.VK_UP:
                                moveCell(selectedRow - 1, selectedCol);
                                e.consume();
                                break;
                            case KeyEvent.VK_DOWN:
                                moveCell(selectedRow + 1, selectedCol);
                                e.consume();
                                break;
                        }
                    }

                    public void keyReleased(KeyEvent e) {
                        validateCurrentCell(cell, r, c);
                    }
                });

                cells[row][col] = cell;
                grid.add(cell);
            }
        }

        JButton solveButton = new JButton("Solve");
        solveButton.addActionListener(e -> solveSudoku());

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearSelected());

        JButton hintButton = new JButton("Hint");
        hintButton.addActionListener(e -> giveHint());

        JPanel buttons = new JPanel();
        buttons.add(solveButton);
        buttons.add(hintButton);
        buttons.add(clearButton);

        add(grid, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void moveCell(int newRow, int newCol) {
        if (newRow < 0) newRow = SIZE - 1;
        else if (newRow >= SIZE) newRow = 0;
        if (newCol < 0) newCol = SIZE - 1;
        else if (newCol >= SIZE) newCol = 0;

        selectedRow = newRow;
        selectedCol = newCol;
        cells[selectedRow][selectedCol].requestFocus();
    }

    private void validateCurrentCell(JTextField cell, int row, int col) {
        String text = cell.getText();
        if (text.isEmpty()) {
            cell.setBackground(Color.WHITE);
            return;
        }
        try {
            int val = Integer.parseInt(text);
            if (val < 1 || val > 9) {
                cell.setBackground(Color.PINK);
                return;
            }

            int[][] board = getCurrentBoard();
            board[row][col] = 0;  // avoid self-conflict

            if (!SudokuSolver.isSafe(board, row, col, val)) {
                cell.setBackground(Color.PINK);
            } else {
                cell.setBackground(Color.WHITE);
            }
        } catch (NumberFormatException ex) {
            cell.setBackground(Color.PINK);
        }
    }

    private void clearSelected() {
        cells[selectedRow][selectedCol].setText("");
        cells[selectedRow][selectedCol].setBackground(Color.WHITE);
    }

    private void solveSudoku() {
        int[][] board = getCurrentBoard();
        boolean valid = true;

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                String text = cells[r][c].getText();
                cells[r][c].setBackground(Color.WHITE);
                try {
                    int val = text.isEmpty() ? 0 : Integer.parseInt(text);
                    if (!text.isEmpty()) {
                        board[r][c] = 0; // avoid self-check
                        if (!SudokuSolver.isSafe(board, r, c, val)) {
                            cells[r][c].setBackground(Color.PINK);
                            valid = false;
                        }
                        board[r][c] = val;
                    }
                } catch (NumberFormatException ex) {
                    cells[r][c].setBackground(Color.PINK);
                    valid = false;
                }
            }
        }

        if (!valid) {
            JOptionPane.showMessageDialog(this, "There are invalid entries in the puzzle.");
            return;
        }

        // check if already solved
        int[][] solved = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++)
            System.arraycopy(board[i], 0, solved[i], 0, SIZE);

        if (SudokuSolver.solve(solved)) {
            boolean alreadySolved = true;
            for (int r = 0; r < SIZE && alreadySolved; r++) {
                for (int c = 0; c < SIZE && alreadySolved; c++) {
                    String text = cells[r][c].getText();
                    int val = text.isEmpty() ? 0 : Integer.parseInt(text);
                    if (val != solved[r][c]) {
                        alreadySolved = false;
                    }
                }
            }

            if (alreadySolved) {
                JOptionPane.showMessageDialog(this, "Congratulations! You solved the puzzle correctly!");
                return;
            }

            for (int r = 0; r < SIZE; r++) {
                for (int c = 0; c < SIZE; c++) {
                    cells[r][c].setText(Integer.toString(solved[r][c]));
                    cells[r][c].setBackground(Color.WHITE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "No solution exists!");
        }
    }

    private void giveHint() {
        int[][] board = getCurrentBoard();

        int[][] solved = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++)
            System.arraycopy(board[i], 0, solved[i], 0, SIZE);

        if (!SudokuSolver.solve(solved)) {
            JOptionPane.showMessageDialog(this, "No solution exists!");
            return;
        }

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (board[r][c] == 0) {
                    cells[r][c].setText(Integer.toString(solved[r][c]));
                    cells[r][c].setBackground(Color.CYAN);
                    selectedRow = r;
                    selectedCol = c;
                    cells[r][c].requestFocus();
                    return;
                }
            }
        }

        JOptionPane.showMessageDialog(this, "No empty cells to hint!");
    }

    private int[][] getCurrentBoard() {
        int[][] board = new int[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                String text = cells[r][c].getText();
                board[r][c] = text.isEmpty() ? 0 : Integer.parseInt(text);
            }
        }
        return board;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SudokuGUI::new);
    }
}
