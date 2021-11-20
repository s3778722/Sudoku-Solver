/*
 * @author Jeffrey Chan & Minyi Li, RMIT 2020
 */
package solver;

import java.util.HashMap;

import grid.StdSudokuGrid;
import grid.SudokuGrid;


/**
 * Algorithm X solver for standard Sudoku.
 */
public class AlgorXSolver extends StdSudokuSolver
{
    //A row in the binary matrix, holds parameter of (row, column and value)
    private static class Row {
        int r;
        int c;
        int v;
        public Row(int r, int c, int v) {
            this.r = r;
            this.c = c;
            this.v = v;
        }
    }

    //Class for binary matrix
    private static class Matrix {

        boolean[][] values;     //the matrix itself
        Row[] rows;             //used to easily access the (r, c, v) of a row in the matrix
        int grid_dim;
        int col_amt;
        int row_amt;
        HashMap<Integer, Boolean> rowDeleted;       //maps a row's index to if it is deleted
        HashMap<Integer, Boolean> columnDeleted;    //maps a column's index to if it is deleted

        //constructor of Matrix class wiith grid dimension/length parameter
        public Matrix(int grid_dim) {

            //Initialization of fields
            values = new boolean[grid_dim * grid_dim * grid_dim][grid_dim * grid_dim * 4];  //matrix size
            this.grid_dim = grid_dim;
            this.row_amt = grid_dim * grid_dim * grid_dim; //9 rows x 9 columns x 9 values = possible candidate values
            this.col_amt = grid_dim * grid_dim * 4; //9 rows x 9 columns x 4 constrains = constraints to be met.
            /*
            The 4 constraints that must be met for every cell are:
                A single value in each cell
                Values 1 through n must exist only once in each row
                Values 1 through n must exist only once in each column
                Values 1 through n must exist only once in each box
            */
            rows = new Row[row_amt];
            rowDeleted = new HashMap<>();
            columnDeleted = new HashMap<>();

            //For each row, fill the necessary columns
            int current_index = 0;
            for (int r = 0; r < grid_dim; r++) {
                for (int c = 0; c < grid_dim; c++) {
                    for (int v = 0; v < grid_dim; v++) {

                        //Add this row to rows[] utility
                        rows[current_index] = new Row(r, c, v);

                        //Place a 1 in each of this row's satisfied columns
                        values[current_index][getColumnIndex_RowCol(r, c)] = true;
                        values[current_index][getColumnIndex_RowVal(r, v)] = true;
                        values[current_index][getColumnIndex_ColVal(c, v)] = true;
                        
                        //square root the gird length to get the box size
                        int box_size = (int)Math.sqrt(grid_dim);
                        //get int of box corner of the row by having row minus the remainder from the divison of row and box size
                        int boxCornerR = r - r % box_size;
                        //get int of box corner of the column by having column minus the remainder from the division of column and box size
                        int boxCornerC = c - c % box_size;
                        //get the box number with the following formula
                        int box_num = (boxCornerR * box_size + boxCornerC) / box_size;

                        values[current_index++][getColumnIndex_BoxVal(box_num, v)] = true;
                    }
                }
            }
        }

        //Calculate index of row using (r, c, v)
        public int getRowIndex(int r, int c, int v) {
            return v + c * grid_dim + r * grid_dim * grid_dim;
        }

        //Calculate index of columns using (i, j) constraints
        public int getColumnIndex_RowCol(int r, int c) {
            return c + grid_dim * r;
        }
        public int getColumnIndex_RowVal(int r, int v) {
            return grid_dim * grid_dim + v + grid_dim * r;
        }
        public int getColumnIndex_ColVal(int c, int v) {
            return grid_dim * grid_dim * 2 + v + grid_dim * c;
        }
        public int getColumnIndex_BoxVal(int b, int v) {
            return grid_dim * grid_dim * 3 + v + grid_dim * b;
        }

        //Get column with least amount of 1s
        public int getLeastColumnIndex() {
            int leastColumnIndex = -1;
            int leastVal = row_amt;

            //Loop through every column that is not deleted
            for (int j = 0; j < col_amt; j++) {
                if (columnDeleted.get(j) == null || !columnDeleted.get(j)) {

                    //Calc column's size
                    int size = 0;
                    for (int i = 0; i < row_amt; i++) {
                        if (values[i][j] && (rowDeleted.get(i) == null || !rowDeleted.get(i)))
                            size++;
                    }

                    //Compare column to current least, and set if necessary
                    if (size < leastVal) {
                        leastVal = size;
                        leastColumnIndex = j;
                    }
                }
            }
            
            return leastColumnIndex;
        }

        //Include (delete = true) or Revert (delete = false) row from choices
        public void chooseRow(int r, int c, int v, boolean delete) {

            int row = getRowIndex(r, c, v);

            //For each column that this row has a 1 in
            for (int j = 0; j < col_amt; j++) {
                if (values[row][j]) {

                    //For each row that has a 1 in this column
                    for (int i = 0; i < row_amt; i++) {
                        if (values[i][j])

                            //Delete or Undelete row
                            rowDeleted.put(i, delete);
                    }

                    //Delete or Undelete column
                    columnDeleted.put(j, delete);
                }
            }

        }


    }

    public AlgorXSolver() {
    } // end of AlgorXSolver()

    //Recursive Algo X solve method
    public boolean solve(int[][] g, Matrix matrix) {

        //Get least column
        int leastColumnIndex = matrix.getLeastColumnIndex();

        //If every column is deleted, the grid is complete
        if (leastColumnIndex == -1)
            return true;

        //For each row that has a 1 in this column
        for (int i = 0; i < matrix.row_amt; i++) {
            if (matrix.values[i][leastColumnIndex] && (matrix.rowDeleted.get(i) == null || !matrix.rowDeleted.get(i))) {

                //Include row
                g[matrix.rows[i].r][matrix.rows[i].c] = matrix.rows[i].v;   //Uses rows[] utility to get (r, c, v) at this index
                matrix.chooseRow(matrix.rows[i].r, matrix.rows[i].c, matrix.rows[i].v,true);

                //If recursion is successful, grid is complete
                if (solve(g, matrix))
                    return true;
                else {
                    //Otherwise, revert row back to not included and move to the next row to try
                    g[matrix.rows[i].r][matrix.rows[i].c] = -1;
                    matrix.chooseRow(matrix.rows[i].r, matrix.rows[i].c, matrix.rows[i].v,false);
                }

            }
        }

        //No row in this branch worked, return false
        return false;

    }


    @Override
    public boolean solve(SudokuGrid grid) {
        StdSudokuGrid std_grid = (StdSudokuGrid)grid;
        int[][] g = std_grid.getGrid();

        //Make matrix from grid
        Matrix matrix = new Matrix(g.length);

        //Include initial value rows
        for (int r = 0; r < g.length; r++) {
            for (int c = 0; c < g.length; c++) {
                if (g[r][c] != -1) {

                    //Set grid values to the index of their symbol
                    g[r][c] = std_grid.symbolIndex(g[r][c]);

                    matrix.chooseRow(r, c, g[r][c], true);
                }
            }
        }


        //Call recursive solver
        boolean result = solve(g, matrix);

        if (!result) {
            System.out.println("Initial Values Error");
        }
        else {
            for (int i = 0; i < g.length; i++) {
                for (int j = 0; j < g.length; j++) {
                    //Reset grid values from their indexes to their proper symbols
                    g[i][j] = std_grid.getValidSymbols()[g[i][j]];
                }
            }
        }

        return result;

    } // end of solve()
} // end of class AlgorXSolver
