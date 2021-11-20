/*
 * @author Jeffrey Chan & Minyi Li, RMIT 2020
 */

package solver;

import java.util.ArrayList;

import grid.StdSudokuGrid;
import grid.SudokuGrid;


/**
 * Dancing links solver for standard Sudoku.
 */
public class DancingLinksSolver extends StdSudokuSolver
{
    public DancingLinksSolver() {
    } // end of DancingLinksSolver()

    //Holds (r, c, v) and points to head of list of nodes in this row
    private static class Row {
        public int r;
        public int c;
        public int v;
        public MatrixNode head;
        public boolean deleted;
        public Row(int r, int c, int v) {
            this.r = r;
            this.c = c;
            this.v = v;
            deleted = false;
        }
    }

    //Holds pointer to head of list of nodes in this column and length of the list
    private static class Column {
        public MatrixNode head;
        public int length = 0;
        public boolean deleted = false;
    }

    //Represents a 1 in the matrix, 2D doubly linked, hold pointers to Row and Column representation
    private static class MatrixNode {
        private MatrixNode left;
        public MatrixNode right;
        public MatrixNode up;
        public MatrixNode down;
        public Row row;
        public Column column;

		public void setLeft(MatrixNode left) {
			this.left = left;
		}
    }

    //Utility for the list of columns
    private static class ColumnList {
        Column[][] row_col;
        Column[][] row_val;
        Column[][] col_val;
        Column[][] box_val;
        Column[] all_columns;
        //constructor for column list with a grid dimension / length parameter
        public ColumnList(int grid_dim) {

            //Initialize all fields
            row_col = new Column[grid_dim][grid_dim];
            row_val = new Column[grid_dim][grid_dim];
            col_val = new Column[grid_dim][grid_dim];
            box_val = new Column[grid_dim][grid_dim];
            all_columns = new Column[grid_dim * grid_dim * 4];

            //iterate through the grid's dimension and increment of current index for the initialization of the variables
            int current_index = 0;
            for (int i = 0; i < grid_dim; i++) {
                for (int j = 0; j < grid_dim; j++) {
                    row_col[i][j] = new Column();
                    row_val[i][j] = new Column();
                    col_val[i][j] = new Column();
                    box_val[i][j] = new Column();

                    all_columns[current_index++] = row_col[i][j];
                    all_columns[current_index++] = row_val[i][j];
                    all_columns[current_index++] = col_val[i][j];
                    all_columns[current_index++] = box_val[i][j];
                }
            }

        }
    }

    //Represents 2D doubly linked list binary matrix
    private static class Matrix {
        Row[] rows;
        ColumnList columns;
        int grid_dim;

        public Matrix(int grid_dim) {

            //Initialize fields
            rows = new Row[grid_dim * grid_dim * grid_dim];
            columns = new ColumnList(grid_dim);
            this.grid_dim = grid_dim;
            
            //Iterate through the grid's column, row, value and increment of current index
            int current_index = 0;
            for (int r = 0; r < grid_dim; r++) {
                for (int c = 0; c < grid_dim; c++) {
                    for (int v = 0; v < grid_dim; v++) {

                        //Add this row to rows
                        Row row = new Row(r, c, v);
                        rows[current_index++] = row;

                        //Create this row's nodes (1s in columns)
                        MatrixNode n_row_col = new MatrixNode();
                        MatrixNode n_row_val = new MatrixNode();
                        MatrixNode n_col_val = new MatrixNode();
                        MatrixNode n_box_val = new MatrixNode();

                        //Link row's nodes horizontally
                        appendToRow(n_row_col, row);
                        appendToRow(n_row_val, row);
                        appendToRow(n_col_val, row);
                        appendToRow(n_box_val, row);

                        //Link nodes to their columns (vertically)
                        appendToColumn(n_row_col, columns.row_col[r][c]);
                        appendToColumn(n_row_val, columns.row_val[r][v]);
                        appendToColumn(n_col_val, columns.col_val[c][v]);

                        //square root the gird length to get the box size
                        int box_size = (int)Math.sqrt(grid_dim);
                        //get int of box corner of the row by having row minus the remainder of row and box size
                        int boxCornerR = r - r % box_size;
                        //get int of box corner of the column by having column minus the remainder of row and box size
                        int boxCornerC = c - c % box_size;
                        //get the box number with the following formula
                        int box_num = (boxCornerR * box_size + boxCornerC) / box_size;
                        
                        //link node to the column (vertically)
                        appendToColumn(n_box_val, columns.box_val[box_num][v]);
                    }
                }
            }

        }

        //Places node at end of the linked list pointed to by this row's head
        public void appendToRow(MatrixNode n, Row row) {

            //If no nodes in list, make this node the head, left and right point to itself
            if (row.head == null) {
                row.head = n;
                n.setLeft(n);
                n.right = n;
            }
            else {
                //Find node whose right, points back to head (last node in list), append this node
                MatrixNode curr = row.head;
                while (curr.right != row.head) {
                    curr = curr.right;
                }
                curr.right = n;
                n.setLeft(curr);
                n.right = row.head;
                row.head.setLeft(n);
            }

            //Point this node to this row
            n.row = row;
        }

        //Places node at end of the linked list pointed to by this column's head
        public void appendToColumn(MatrixNode n, Column column) {

            //If no nodes in list, make this node the head, up and down point to itself
            if (column.head == null) {
                column.head = n;
                n.up = n;
                n.down = n;
            } else {
                //Find node whose down, points back to head (last node in list), append this node
                MatrixNode curr = column.head;
                while (curr.down != column.head) {
                    curr = curr.down;
                }
                curr.down = n;
                n.up = curr;
                n.down = column.head;
                column.head.up = n;
            }
            column.length++;
            n.column = column;
        }

        //Set row's deleted to true and update length of columns affected
        public void removeRow(Row row) {
            row.deleted = true;
            MatrixNode curr = row.head;
            do {
                curr.column.length--;
                curr = curr.right;
            }
            while (curr != row.head);
        }

        public void removeColumn(Column column) {
            column.deleted = true;
        }

        //Undo deletion of this row, update length of columns affected
        public void reAddRow(Row row) {
            row.deleted = false;
            MatrixNode curr = row.head;
            do {
                curr.column.length++;
                curr = curr.right;
            }
            while (curr != row.head);
        }

        public void reAddColumn(Column column) {
            column.deleted = false;
        }

        //Get column with least length (least amount of 1s or least amount of nodes)
        public Column getLeastColumn() {
            Column leastColumn = null;
            int leastVal = grid_dim * grid_dim * grid_dim;
            for (Column column : columns.all_columns) {
                if (!column.deleted && column.length < leastVal) {
                    leastColumn = column;
                    leastVal = column.length;
                }
            }
            return leastColumn;
        }

        //Get row from rows[] using (r, c, v)
        public Row getRow(int r, int c, int v) {
            return rows[v + grid_dim * c + grid_dim * grid_dim * r];
        }

    }

    //Recursive Dancing Links solver
    public boolean solve(int[][] g, Matrix matrix) {

        //Get least column
        Column leastColumn = matrix.getLeastColumn();
        //If all columns deleted, grid is complete
        if (leastColumn == null)
            return true;

        //Try each node in this column
        MatrixNode rowNode = leastColumn.head;
        do {

            //Keep track of removed rows and columns so they may be reverted if branch fails
           ArrayList<Row> removedRows = new ArrayList<>();
           ArrayList<Column> removedColumns = new ArrayList<>();

           //If this row is not deleted
           if (!rowNode.row.deleted) {

               //Loop through nodes in this row
               MatrixNode currRow = rowNode;
               do {

                   //If this column is not deleted
                   if (!currRow.column.deleted) {

                       //Loop through nodes in this column
                       MatrixNode currCol = currRow;
                       do {

                           //Delete the row of each node in this column's linked list (delete each row with a 1 in this column)
                           if (currCol != currRow && !currCol.row.deleted) {
                               matrix.removeRow(currCol.row);
                               removedRows.add(currCol.row);
                           }

                           currCol = currCol.down;
                       }
                       while (currCol != currRow);

                       //Delete this column
                       matrix.removeColumn(currRow.column);
                       removedColumns.add(currRow.column);

                   }

                   currRow = currRow.right;
               }
               while (currRow != rowNode);

               //Delete this chosen row
               matrix.removeRow(currRow.row);
               removedRows.add(currRow.row);

               //Include row in grid
               g[rowNode.row.r][rowNode.row.c] = rowNode.row.v;

               //If recursion is successful, grid is complete
               if (solve(g, matrix))
                   return true;
               else {
                   //Otherwise revert back and move to next row
                   g[rowNode.row.r][rowNode.row.c] = -1;
                   for (Row row : removedRows)
                       matrix.reAddRow(row);
                   for (Column col : removedColumns)
                       matrix.reAddColumn(col);
               }

           }

           rowNode = rowNode.down;
       }
       while (rowNode != leastColumn.head);

       //No row in this branch worked, return false
       return false;

    }


    @Override
    public boolean solve(SudokuGrid grid) {

        StdSudokuGrid std_grid = (StdSudokuGrid)grid;
        int[][] g = std_grid.getGrid();

        //Make matrix from grid
        Matrix matrix = new Matrix(g.length);

        //Choose initial values using same method as recursive solver
        for (int r = 0; r < g.length; r++) {
            for (int c = 0; c < g.length; c++) {

                if (g[r][c] != -1) {

                    //Set grid values to the index of their symbol
                    g[r][c] = std_grid.symbolIndex(g[r][c]);

                    MatrixNode currRow = matrix.getRow(r, c, g[r][c]).head;

                    do {
                        if (!currRow.column.deleted) {

                            MatrixNode currCol = currRow;

                            do {
                                if (currCol != currRow && !currCol.row.deleted) {
                                    matrix.removeRow(currCol.row);
                                }

                                currCol = currCol.down;
                            }
                            while (currCol != currRow);

                            matrix.removeColumn(currRow.column);

                        }

                        currRow = currRow.right;
                    }
                    while (currRow != matrix.getRow(r, c, g[r][c]).head);

                    matrix.removeRow(currRow.row);
                    matrix.removeColumn(currRow.column);

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
} // end of class DancingLinksSolver
