/**
 * @author Jeffrey Chan & Minyi Li, RMIT 2020
 */
package grid;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;


/**
 * Class implementing the grid for Killer Sudoku.
 * Extends SudokuGrid (hence implements all abstract methods in that abstract
 * class).
 * You will need to complete the implementation for this for task E and
 * subsequently use it to complete the other classes.
 * See the comments in SudokuGrid to understand what each overriden method is
 * aiming to do (and hence what you should aim for in your implementation).
 */
public class KillerSudokuGrid extends SudokuGrid
{
    //make a Cell class
    public static class Cell {
        public int row;
        public int col;
        //constructor for Cell class with row and column as parameters
        public Cell(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }
    //make a Cage class
    public static class Cage {
        public ArrayList<Cell> cells;
        public int total;
        //constructor for Cage class with grid, cell andd total value as parameters
        public Cage(KillerSudokuGrid grid, ArrayList<Cell> cells, int total) {
            //for each cell in the cells array list
            for (Cell cell : cells) {
                //"put" the cell key from the cell to the cage map
                grid.cellToCageMap.put(getCellKey(cell), this);
            }
            this.cells = cells;
            this.total = total;
        }
    }

    private int[][] grid;
    private int[] valid_symbols;
    private ArrayList<Cage> cages;
    private HashMap<String, Cage> cellToCageMap;

    public KillerSudokuGrid() {
        super();
        cages = new ArrayList<>();
        cellToCageMap = new HashMap<>();
    } // end of KillerSudokuGrid()


    /* ********************************************************* */
    //Get this symbols index in valid_symbols
    public int symbolIndex(int symbol) {
        for (int i = 0; i < valid_symbols.length; i++) {
            if (valid_symbols[i] == symbol)
                return i;
        }
        return -1;
    }

    //Get key from cell
    public static String getCellKey(int row, int col) {
        return row + " " + col;
    }
    public static String getCellKey(Cell cell) {
        return cell.row + " " + cell.col;
    }


    @Override
    public void initGrid(String filename)
        throws FileNotFoundException, IOException
    {
        File inFile = new File(filename);
        Scanner in = new Scanner(inFile);

        //Get dimension of grid
        int grid_dim = in.nextInt();

        //Get valid symbols, store in array
        valid_symbols = new int[grid_dim];
        for (int i = 0; i < grid_dim; i++) {
            valid_symbols[i] = in.nextInt();
        }

        //Fill empty grid
        grid = new int[grid_dim][grid_dim];
        for (int i = 0; i < grid_dim; i++) {
            for (int j = 0; j < grid_dim; j++) {
                grid[i][j] = -1;
            }
        }
        
        //Get number of cages
        int num_cages = in.nextInt();

        //Get cages
        int cage_total = -1;
        ArrayList<Cell> cells = new ArrayList<>();
        while (true) {
            boolean done = true;
            if (in.hasNextInt()) {
                if (cage_total != -1) {
                    cages.add(new Cage(this, cells, cage_total));
                }
                cage_total = in.nextInt();
                cells = new ArrayList<>();
                done = false;
            }
            if (in.hasNext()) {
                String tuple = in.next();
                int comma_loc = tuple.indexOf(',');
                String rowStr = tuple.substring(0, comma_loc);
                String colStr = tuple.substring(comma_loc + 1);

                int row = Integer.parseInt(rowStr);
                int col = Integer.parseInt(colStr);
                cells.add(new Cell(row, col));
                done = false;
            }
            if (done) {
                cages.add(new Cage(this, cells, cage_total));
                break;
            }
        }


        in.close();

    } // end of initBoard()


    @Override
    public void outputGrid(String filename)
        throws FileNotFoundException, IOException
    {
        FileOutputStream outFile = new FileOutputStream(filename);
        outFile.write(this.toString().getBytes());
        outFile.close();
    } // end of outputBoard()


    @Override
    public String toString() {
        StringBuilder outStr = new StringBuilder();
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid.length; j++) {
                if (grid[i][j] != -1)
                    outStr.append(grid[i][j]);
                else
                    outStr.append("x");
                if (j != grid.length - 1) {
                    outStr.append(",");
                }
            }
            if (i != grid.length - 1)
                outStr.append("\n");
        }
        return outStr.toString();
    } // end of toString()


    @Override
    public boolean validate() {

        ArrayList<Integer> remaining_symbols = new ArrayList<>();

        //Check rows
        for (int i = 0; i < grid.length; i++) {

            for (int e : valid_symbols)
                remaining_symbols.add(e);

            for (int j = 0; j < grid.length; j++) {
                if (grid[i][j] == -1)
                    return false;
                if (remaining_symbols.contains(grid[i][j]))
                    remaining_symbols.remove((Object)grid[i][j]);
                else
                    return false;
            }

            remaining_symbols.clear();
        }


        //Check cols
        for (int i = 0; i < grid.length; i++) {

            for (int e : valid_symbols)
                remaining_symbols.add(e);

            for (int j = 0; j < grid.length; j++) {
                if (remaining_symbols.contains(grid[j][i]))
                    remaining_symbols.remove((Object)grid[j][i]);
                else
                    return false;
            }

            remaining_symbols.clear();
        }


        //Check boxes
        int box_size = (int)Math.sqrt(grid.length);
        for (int i = 0; i < grid.length; i += box_size) {
            for (int j = 0; j < grid.length; j += box_size) {

                for (int e : valid_symbols)
                    remaining_symbols.add(e);

                for (int k = 0; k < box_size; k++) {
                    for (int m = 0; m < box_size; m++) {

                        if (remaining_symbols.contains(grid[i + k][j + m]))
                            remaining_symbols.remove((Object)grid[i + k][j + m]);
                        else
                            return false;

                    }
                }

                remaining_symbols.clear();
            }
        }

        //Check cages
        ArrayList<Integer> symbols_in_cage = new ArrayList<>();
        for (Cage cage : cages) {
            int total = 0;
            for (Cell cell : cage.cells) {
                total += grid[cell.row][cell.col];
                //unique symbols in cage
                if (symbols_in_cage.contains(grid[cell.row][cell.col]))
                    return false;
                symbols_in_cage.add(grid[cell.row][cell.col]);
            }
            if (total != cage.total)
                return false;
            symbols_in_cage.clear();
        }

        return true;
    } // end of validate()

    public int[][] getGrid() {
        return grid;
    }

    public int[] getValidSymbols() {
        return valid_symbols;
    }

    public ArrayList<Cage> getCages() {
        return cages;
    }

    public HashMap<String, Cage> getCellToCageMap() {
        return cellToCageMap;
    }

} // end of class KillerSudokuGrid
