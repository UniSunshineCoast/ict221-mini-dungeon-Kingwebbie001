package dungeon.engine;

import java.util.ArrayList;
import java.util.Collections; // For shuffling directions in maze generation
import java.util.List;
import java.util.Random;

import dungeon.engine.things.Bomb;
import dungeon.engine.things.Entry;
import dungeon.engine.things.Gold;
import dungeon.engine.things.HealthPotion;
import dungeon.engine.things.Ladder;
import dungeon.engine.things.MeleeMutant;
import dungeon.engine.things.RangedMutant;
import dungeon.engine.things.Trap;
import dungeon.engine.things.Wall;

/**
 * Represents a single level of the MiniDungeon game map.
 * This class is responsible for initializing the grid, generating corridor-like
 * walls using a recursive backtracker algorithm, placing various "things"
 * (items, mutants, etc.) on the map, and providing access to cells.
 */
public class DungeonMap {
    private final Cell[][] grid;
    private final int rows = 20; // UPDATED: Map size to 20x20
    private final int cols = 20; // UPDATED: Map size to 20x20
    private final Random random;

    // Store positions of ranged mutants for easier access during attacks
    private List<Position> rangedMutantPositions;

    /**
     * Constructs a new DungeonMap and initializes it based on the given difficulty.
     * The map is populated with walls, an entry point, a ladder, and various
     * items (gold, traps, mutants, health potions) placed randomly.
     *
     * @param difficulty The game difficulty level, affecting the number of Ranged Mutants.
     * @param isLevel1   True if this is Level 1, false for Level 2. Affects entry point.
     * @param level1LadderPosition If isLevel1 is false, this is the position of the ladder from Level 1,
     * which becomes the entry point for Level 2.
     */
    public DungeonMap(int difficulty, boolean isLevel1, Position level1LadderPosition) {
        this.grid = new Cell[rows][cols];
        this.random = new Random();
        this.rangedMutantPositions = new ArrayList<>();
        initializeGridWithWalls(); // CHANGED: Initialize with walls instead of empty
        
        // Determine the starting point for maze carving.
        // It should be an odd-indexed cell to ensure 2-unit steps work within bounds,
        // and ideally near where the player will spawn.
        int mazeStartX = 1;
        int mazeStartY = 1;
        if (isLevel1) {
            // For Level 1, ensure player spawn area (rows-2, 0) is clear.
            // Start carving from a cell near the bottom-left corner to ensure accessibility.
            // The maze generation works best with odd coordinates if we are carving 2 units at a time.
            mazeStartX = rows - 2; // Can be rows-2 (even) or rows-1 (odd)
            mazeStartY = 1; // Needs to be odd for 2-step carving
            // Adjust to ensure mazeStart is odd-indexed for carving if map dimensions are even.
            // If rows is 20, rows-2 = 18 (even). We need to carve from odd cell.
            // Let's force it to be (rows - 2, 1) to be consistent with player spawn being near (rows - 2, 0)
            // and maze generation starting from an odd-indexed cell for correct carving.
            if (rows % 2 == 0) mazeStartX = rows - 3; // Ensure odd if rows is even
            if (cols % 2 == 0) mazeStartY = 1; // Ensure odd if cols is even
        } else {
            // For Level 2, maze generation starts from the previous ladder's position
            // (which becomes the entry point).
            mazeStartX = level1LadderPosition.getRow();
            mazeStartY = level1LadderPosition.getCol();
        }

        // Make sure maze start point is odd-indexed for carving
        if (mazeStartX % 2 == 0) mazeStartX++;
        if (mazeStartY % 2 == 0) mazeStartY++;
        
        // If the adjusted maze start is out of bounds, adjust it back.
        if (mazeStartX >= rows) mazeStartX = rows - (rows % 2 == 0 ? 3 : 2); // ensures it's within bounds and odd
        if (mazeStartY >= cols) mazeStartY = cols - (cols % 2 == 0 ? 3 : 2); // ensures it's within bounds and odd
        if (mazeStartX < 1) mazeStartX = 1;
        if (mazeStartY < 1) mazeStartY = 1;


        carveMaze(mazeStartX, mazeStartY); // NEW: Carve paths after filling with walls

        // Place Entry and Ladder after maze generation, overriding any carved paths if necessary
        Position entryPos;
        if (isLevel1) {
            // Level 1: bottom left cell (19,0 for 20x20)
            entryPos = new Position(rows - 1, 0);
            // Ensure this specific cell is a floor (carved out) if it's a wall.
            grid[entryPos.getRow()][entryPos.getCol()].setThing(new Entry());
            // Also ensure the player spawn position (rows-2,0) is clear.
            grid[rows - 2][0].setThing(null); // Ensure player spawn is always clear path

            placeLadder(new Ladder()); // Ladder placed randomly on Level 1 on a floor cell
        } else {
            // Level 2: same as Ladder from Level 1
            entryPos = level1LadderPosition;
            if (entryPos != null) {
                grid[entryPos.getRow()][entryPos.getCol()].setThing(new Entry());
            } else {
                // Fallback if ladder position from Level 1 is not provided for Level 2 (shouldn't happen)
                entryPos = new Position(rows - 1, 0);
                grid[entryPos.getRow()][entryPos.getCol()].setThing(new Entry());
            }
            // Ladder for Level 2 is placed randomly for exiting the game
            placeLadder(new Ladder());
        }

        // Place other items based on counts on available floor cells
        placeRandomThings(ThingType.GOLD, 10); // Increased counts for larger map
        placeRandomThings(ThingType.TRAP, 8);
        placeRandomThings(ThingType.MELEE_MUTANT, 6);
        placeRandomThings(ThingType.HEALTH_POTION, 4);
        placeRandomThings(ThingType.BOMB, 3); // Increased bombs for larger map

        // Ranged Mutants count depends on difficulty
        placeRandomThings(ThingType.RANGED_MUTANT, difficulty * 2); // Scale with map size
    }

    /**
     * Fills the entire grid with Wall objects. This is the initial state for maze generation.
     */
    private void initializeGridWithWalls() { // RENAMED and CHANGED logic
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = new Cell(new Wall()); // Fill with walls
            }
        }
    }

    /**
     * Recursively carves paths (removes walls) to create a maze-like structure.
     * Implements a simplified recursive backtracker algorithm.
     * Ensures paths are at least 1 unit wide.
     *
     * @param r Current row for carving.
     * @param c Current column for carving.
     */
    private void carveMaze(int r, int c) {
        grid[r][c].setThing(null); // Carve current cell (make it floor)

        // Define directions for carving (2 units step to ensure walls between cells)
        int[][] directions = {
            {0, 2},   // Right
            {0, -2},  // Left
            {2, 0},   // Down
            {-2, 0}   // Up
        };

        // Shuffle directions to ensure randomness
        List<int[]> shuffledDirections = new ArrayList<>();
        for (int[] dir : directions) {
            shuffledDirections.add(dir);
        }
        Collections.shuffle(shuffledDirections, random);

        for (int[] dir : shuffledDirections) {
            int dr = dir[0];
            int dc = dir[1];

            int nextR = r + dr;
            int nextC = c + dc;
            int wallR = r + dr / 2;
            int wallC = c + dc / 2;

            // Check bounds for the next cell and the wall cell in between
            if (isValidPosition(nextR, nextC) && grid[nextR][nextC].containsThingType(ThingType.WALL)) {
                // Carve the wall in between
                grid[wallR][wallC].setThing(null);
                // Recursively carve from the next cell
                carveMaze(nextR, nextC);
            }
        }
    }

    /**
     * Places the Ladder randomly on an empty, non-wall cell, and not on the entry point.
     *
     * @param ladder The Ladder instance to place.
     * @return The position where the ladder was placed.
     */
    private Position placeLadder(Thing ladder) {
        Position pos = placeRandomThings(ThingType.LADDER, 1);
        if (pos != null) {
            grid[pos.getRow()][pos.getCol()].setThing(ladder);
        }
        return pos;
    }

    /**
     * Places a specified number of a given ThingType randomly on the map.
     * Ensures items do not overlap with existing non-empty cells (except for walls,
     * which are placed first and are not considered for random placement).
     *
     * @param type  The ThingType to place (e.g., GOLD, TRAP).
     * @param count The number of instances of this thing to place.
     * @return The position of the last placed thing, or null if count is 0.
     */
    private Position placeRandomThings(ThingType type, int count) {
        int placedCount = 0;
        Position lastPlacedPos = null;
        // Define points that should always be clear for random placement
        // Player spawn is now (rows-2, 0)
        Position playerSpawnPos = new Position(rows - 2, 0);
        Position entryPointPos = new Position(rows - 1, 0);

        while (placedCount < count) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);

            Position currentAttemptPos = new Position(r, c);

            // Ensure the cell is within bounds, is currently empty (contains null thing - i.e., it's a floor),
            // and is not the entry point or the player spawn point.
            if (isValidPosition(r, c) && grid[r][c].isEmpty() && // Check if it's a carved-out path
                !currentAttemptPos.equals(playerSpawnPos) &&
                !currentAttemptPos.equals(entryPointPos)
            ) {
                // Create a new instance of the thing for each placement
                Thing newThing = createThingInstance(type);
                if (newThing != null) {
                    grid[r][c].setThing(newThing);
                    lastPlacedPos = currentAttemptPos;
                    placedCount++;
                    if (type == ThingType.RANGED_MUTANT) {
                        rangedMutantPositions.add(lastPlacedPos);
                    }
                }
            }
        }
        return lastPlacedPos;
    }

    /**
     * Creates a new instance of a Thing based on its ThingType.
     *
     * @param type The ThingType to instantiate.
     * @return A new Thing instance, or null if the type is not recognized.
     */
    private Thing createThingInstance(ThingType type) {
        switch (type) {
            case ENTRY: return new Entry();
            case LADDER: return new Ladder();
            case WALL: return new Wall();
            case TRAP: return new Trap();
            case GOLD: return new Gold();
            case MELEE_MUTANT: return new MeleeMutant();
            case RANGED_MUTANT: return new RangedMutant();
            case HEALTH_POTION: return new HealthPotion();
            case BOMB: return new Bomb();
            case PLAYER: // Player is handled separately, not randomly placed
            default: return null;
        }
    }

    /**
     * Returns the Cell object at the specified row and column.
     *
     * @param row The row index (0 to rows-1).
     * @param col The column index (0 to cols-1).
     * @return The Cell object at the given coordinates.
     * @throws IndexOutOfBoundsException if the row or column is out of bounds.
     */
    public Cell getCell(int row, int col) {
        if (!isValidPosition(row, col)) {
            throw new IndexOutOfBoundsException("Coordinates (" + row + ", " + col + ") are out of map bounds.");
        }
        return grid[row][col];
    }

    /**
     * Checks if the given row and column coordinates are within the map boundaries.
     *
     * @param row The row index.
     * @param col The column index.
     * @return true if the coordinates are valid, false otherwise.
     */
    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    /**
     * Returns the number of rows in the dungeon map.
     *
     * @return The number of rows.
     */
    public int getRows() {
        return rows;
    }

    /**
     * Returns the number of columns in the dungeon map.
     *
     * @return The number of columns.
     */
    public int getCols() {
        return cols;
    }

    /**
     * Returns a list of current positions of all ranged mutants on the map.
     *
     * @return A list of Position objects for ranged mutants.
     */
    public List<Position> getRangedMutantPositions() {
        // Rebuild the list to ensure it's up-to-date with current map state
        // (in case mutants are removed from cells)
        List<Position> currentRangedMutants = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c].containsThingType(ThingType.RANGED_MUTANT)) {
                    currentRangedMutants.add(new Position(r, c));
                }
            }
        }
        this.rangedMutantPositions = currentRangedMutants; // Update internal list
        return this.rangedMutantPositions;
    }

    /**
     * Prints the current state of the dungeon map to the console,
     * using the display symbols of the things in each cell.
     * This is useful for text-based UI and debugging.
     *
     * @param playerPosition The current position of the player, to display 'P'.
     */
    public void printMap(Position playerPosition) {
        System.out.println("----------------------------------------"); // Adjusted for 20x20
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (playerPosition != null && r == playerPosition.getRow() && c == playerPosition.getCol()) {
                    System.out.print(ThingType.PLAYER.getSymbol() + " "); // Always show player as 'P'
                } else {
                    System.out.print(grid[r][c].getDisplaySymbol() + " ");
                }
            }
            System.out.println(); // New line after each row
        }
        System.out.println("----------------------------------------"); // Adjusted for 20x20
    }

    /**
     * Returns a list of positions of adjacent cells that contain any of the specified ThingTypes.
     * This includes cells directly above, below, left, right, and diagonally.
     *
     * @param centerPosition The position around which to check for adjacent things.
     * @param typesToCheck   An array of ThingType enums to look for (e.g., {ThingType.WALL, ThingType.TRAP}).
     * @return A list of positions where the specified types of things are found.
     */
    public List<Position> getAdjacentThingPositions(Position centerPosition, ThingType... typesToCheck) {
        List<Position> adjacentThings = new ArrayList<>();
        int[] dr = {-1, -1, -1, 0, 0, 1, 1, 1}; // Row offsets for 8 directions (including diagonals)
        int[] dc = {-1, 0, 1, -1, 1, -1, 0, 1}; // Column offsets for 8 directions (including diagonals)

        for (int i = 0; i < dr.length; i++) {
            int newRow = centerPosition.getRow() + dr[i];
            int newCol = centerPosition.getCol() + dc[i];

            if (isValidPosition(newRow, newCol)) {
                Cell adjacentCell = grid[newRow][newCol];
                if (!adjacentCell.isEmpty()) {
                    for (ThingType type : typesToCheck) {
                        if (adjacentCell.containsThingType(type)) {
                            adjacentThings.add(new Position(newRow, newCol));
                            break; // Found one of the types, move to next adjacent cell
                        }
                    }
                }
            }
        }
        return adjacentThings;
    }
}