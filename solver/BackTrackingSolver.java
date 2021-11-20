/*
 * @author Jeffrey Chan & Minyi Li, RMIT 2020
 */

package solver;

import grid.StdSudokuGrid;
import grid.SudokuGrid;


/**
 * Backtracking solver for standard Sudoku.
 */
public class BackTrackingSolver extends StdSudokuSolver
{
    public BackTrackingSolver() {
    } // end of BackTrackingSolver()
    
    //Check if placing this value in this position is allowable
    //takes grid, row, column and value as parameter
    public boolean canPlace(int[][] grid, int r, int c, int v) {

        //Check row and column for this value
        for (int i = 0; i < grid.length; i++) {
            //check the position by iterating through the column, if equals the value, return false
            if (grid[r][i] == v){
                return false;
            }
            //check the position by iterating through the row, if equals the value, return false
            if (grid[i][c] == v){
                return false;
            }
        }

        //Check box for this value
        int box_size = (int)Math.sqrt(grid.length); //square root the gird length to get the box size
        //get int of box corner of the row by having row minus the remainder from the division oof row and box size
        int boxCornerR = r - r % box_size;
        //get int of box corner of the column by having column minus the remainder from the division of column and box size
        int boxCornerC = c - c % box_size;

        //iterate through the box
        for (int i = 0; i < box_size; i++) {
            for (int j = 0; j < box_size; j++) {
                //if the box contains the value, return false
                if (grid[boxCornerR + i][boxCornerC + j] == v)
                    return false;
            }
        }
        //return true if the value can be placed
        return true;
    }

    @Override
    public boolean solve(SudokuGrid grid) {
        StdSudokuGrid std_grid = (StdSudokuGrid)grid;
        int[][] g = std_grid.getGrid();

        //Try to find an empty spot in the grid
        int row = -1;
        int col = -1;
        boolean foundEmpty = false;

        //iterate through the grid
        for (int i = 0; i < g.length; i++) {
            for (int j = 0; j < g.length; j++) {
                // find empty location
                if (g[i][j] == -1) {
                    row = i;
                    col = j;
                    foundEmpty = true;
                    //break if found empty locatioon
                    break;
                }
            }
            //break if found empty location
            if (foundEmpty)
                break;
        }

        //If there are no empty locations, the grid is filled properly, return true
        if (!foundEmpty)
            return true;

        //Try each possible valid symbol in this position, commence depth-first search
        int[] valid_symbols = std_grid.getValidSymbols();
        for (int i = 0; i < g.length; i++) {

            int symbol = valid_symbols[i];

            //If symbol(e.g. integer) is allowed here, place it and make a recursion
            if (canPlace(g, row, col, symbol)) {
                g[row][col] = symbol;
                //recursion
                if (solve(grid))
                    return true;
                else
                    //Otherwise, revert change and move on to next symbol
                    g[row][col] = -1;
            }
        }

        return false;
    } // end of solve()
} // end of class BackTrackingSolver()
