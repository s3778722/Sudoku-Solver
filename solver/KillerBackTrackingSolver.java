/*
 * @author Jeffrey Chan & Minyi Li, RMIT 2020
 */

package solver;

import java.util.ArrayList;

import grid.KillerSudokuGrid;
import grid.SudokuGrid;


/**
 * Backtracking solver for Killer Sudoku.
 */
public class KillerBackTrackingSolver extends KillerSudokuSolver
{
    public KillerBackTrackingSolver() {
    } // end of KillerBackTrackingSolver()

    public boolean canPlace(KillerSudokuGrid grid, int r, int c, int v) {

        int[][] g = grid.getGrid();

        //Check row and column for this value
        for (int i = 0; i < g.length; i++) {
            if (g[r][i] == v)
                return false;
            if (g[i][c] == v)
                return false;
        }

        //Check box for this value
        int box_size = (int)Math.sqrt(g.length);
        int boxCornerR = r - r % box_size;
        int boxCornerC = c - c % box_size;

        for (int i = 0; i < box_size; i++) {
            for (int j = 0; j < box_size; j++) {
                if (g[boxCornerR + i][boxCornerC + j] == v)
                    return false;
            }
        }

        //Check cage
        KillerSudokuGrid.Cage cage = grid.getCellToCageMap().get(KillerSudokuGrid.getCellKey(r, c));
        ArrayList<Integer> symbols_in_cage = new ArrayList<>();
        if (cage != null) {
            int total = 0;
            for (KillerSudokuGrid.Cell cell : cage.cells) {
                if (g[cell.row][cell.col] != -1) {
                    total += g[cell.row][cell.col];
                    symbols_in_cage.add(g[cell.row][cell.col]);
                }
            }
            if (symbols_in_cage.size() == cage.cells.size() - 1)
                return total + v == cage.total;
            else
                return total + v < cage.total;

        }

        return true;
    }


    @Override
    public boolean solve(SudokuGrid grid) {
        KillerSudokuGrid killer_grid = (KillerSudokuGrid)grid;
        int[][] g = killer_grid.getGrid();

        //Try to find an empty spot in the grid
        int row = -1;
        int col = -1;
        boolean foundEmpty = false;
        for (int i = 0; i < g.length; i++) {
            for (int j = 0; j < g.length; j++) {
                if (g[i][j] == -1) {
                    row = i;
                    col = j;
                    foundEmpty = true;
                    break;
                }
            }
            if (foundEmpty)
                break;
        }

        //If there are no empty spots, the grid is filled properly, return true
        if (!foundEmpty)
            return true;

        //Try each possible valid symbol in this position, commence depth-first search
        int[] valid_symbols = killer_grid.getValidSymbols();
        for (int i = 0; i < g.length; i++) {

            int symbol = valid_symbols[i];

            //If symbol is allowed here, place it and recurse
            if (canPlace(killer_grid, row, col, symbol)) {
                g[row][col] = symbol;
                if (solve(grid))
                    return true;
                else
                    //Otherwise, revert change and move on to next symbol
                    g[row][col] = -1;
            }
        }

        //No symbol in this branch resulted in a proper grid, return false
        return false;
    } // end of solve()
} // end of class KillerBackTrackingSolver()
