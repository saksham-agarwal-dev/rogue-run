package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;
    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */

    public static final int MIN_ROOM_SIZE = 4;
    Random random;


    public void interactWithKeyboard() throws IOException {
        mainMenuUI();
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        TETile[][] finalWorldFrame = initializeArray();
        String readInput = input.toLowerCase();
        long seed = 0;
        if (readInput.startsWith("n")) {
            readInput = readInput.substring(1);
            while (!readInput.startsWith("s")) {
                seed = seed * 10 + charNum(readInput.charAt(0));
                readInput = readInput.substring(1);
            }
            finalWorldFrame = makeNewMap(seed);
        } else if (readInput.startsWith("l")
                && (Paths.get("output.txt").toFile().isFile())) {
            try {
                finalWorldFrame = interactWithInputString(load());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (readInput.length() > 1) {
                readInput = readInput.substring(1);
            } else {
                readInput = "";
            }
            try {
                input = load() + readInput;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Coordinate playerPos = getPlayerPos(finalWorldFrame);
        for (int i = 0; i < readInput.length(); i++) {
            if (readInput.charAt(i) == 'w') {
                if (finalWorldFrame[playerPos.x][playerPos.y + 1] == Tileset.FLOOR
                        || finalWorldFrame[playerPos.x][playerPos.y + 1] == Tileset.TREASURE) {
                    finalWorldFrame[playerPos.x][playerPos.y + 1] = Tileset.AVATAR;
                    finalWorldFrame[playerPos.x][playerPos.y] = Tileset.FLOOR;
                    playerPos.y++;
                }
            }
            if (readInput.charAt(i) == 'a') {
                if (finalWorldFrame[playerPos.x - 1][playerPos.y] == Tileset.FLOOR
                        || finalWorldFrame[playerPos.x - 1][playerPos.y] == Tileset.TREASURE) {
                    finalWorldFrame[playerPos.x - 1][playerPos.y] = Tileset.AVATAR;
                    finalWorldFrame[playerPos.x][playerPos.y] = Tileset.FLOOR;
                    playerPos.x--;
                }
            }
            if (readInput.charAt(i) == 's') {
                if (finalWorldFrame[playerPos.x][playerPos.y - 1] == Tileset.FLOOR
                        || finalWorldFrame[playerPos.x][playerPos.y - 1] == Tileset.TREASURE) {
                    finalWorldFrame[playerPos.x][playerPos.y - 1] = Tileset.AVATAR;
                    finalWorldFrame[playerPos.x][playerPos.y] = Tileset.FLOOR;
                    playerPos.y--;
                }
            }
            if (readInput.charAt(i) == 'd') {
                if (finalWorldFrame[playerPos.x + 1][playerPos.y] == Tileset.FLOOR
                        || finalWorldFrame[playerPos.x + 1][playerPos.y] == Tileset.TREASURE) {
                    finalWorldFrame[playerPos.x + 1][playerPos.y] = Tileset.AVATAR;
                    finalWorldFrame[playerPos.x][playerPos.y] = Tileset.FLOOR;
                    playerPos.x++;
                }
            }
            if (readInput.charAt(i) == ':' && readInput.endsWith(":q")) {
                write(input.toLowerCase().replaceAll(":q", ""));
            }
            finalWorldFrame = treasureCheck(finalWorldFrame);
        }
        return finalWorldFrame;
    }

    private static String detailedDescription(String s) {
        if (s.equals("wall")) {
            return "A massive stone wall that is impossible to climb or go through.";
        } else if (s.equals("you")) {
            return "You, the heroic and adventurous explorer.";
        } else if (s.equals("floor")) {
            return "Ancient and creaky floor. However, it does seem safe to walk on.";
        } else if (s.equals("nothing")) {
            return "You are unable to see what's over there.";
        } else if (s.equals("unlocked door")) {
            return "Now that you have found all of the treasure, you must leave!";
        } else if (s.equals("treasure")) {
            return "You can see the glimmer of the gold! You have found some of the treasure! "
                    + "Now you must take it.";
        } else if (s.equals("trap")) {
            return "This looks like normal flooring, but something seems off. Better be careful.";
        } else if (s.equals("locked door")) {
            return "That is your exit after you find all of the treasure.";
        } else {
            return s;
        }
    }

    private void replayGame() {
        String replay = "";
        try {
            replay = load().toLowerCase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        TETile[][] world;
        int separator = 0;
        for (int i = 0; i < replay.length(); i++) {
            if (replay.charAt(i) == 's') {
                separator = i;
                break;
            }
        }
        ter.initialize(WIDTH, HEIGHT);
        while (separator <= replay.length() - 1) {
            world = interactWithInputString(replay.substring(0, separator + 1));
            separator++;
            ter.renderFrame(world);
            StdDraw.pause(100);
        }
        try {
            playGame(load());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static TETile[][] limitedRenderArray(TETile[][] world, Coordinate playerPos) {
        TETile[][] newArr = initializeArray();
        for (int x = Math.max(playerPos.x - 5, 0); x < Math.min(WIDTH, playerPos.x + 6); x++) {
            for (int y = Math.max(playerPos.y - 5, 0);
                 y < Math.min(HEIGHT, playerPos.y + 6); y++) {
                if (Math.abs(Math.sqrt(Math.pow(x - playerPos.x, 2)
                        + Math.pow(y - playerPos.y, 2))) <= 5) {
                    newArr[x][y] = world[x][y];
                }
            }
        }
        return newArr;
    }

    private static boolean isNum(char c) {
        return c == '1'
                || c == '2'
                || c == '3'
                || c == '4'
                || c == '5'
                || c == '6'
                || c == '7'
                || c == '8'
                || c == '9'
                || c == '0';
    }

    private static int charNum(char c) {
        if (c == '1') {
            return 1;
        }
        if (c == '2') {
            return 2;
        }
        if (c == '3') {
            return 3;
        }
        if (c == '4') {
            return 4;
        }
        if (c == '5') {
            return 5;
        }
        if (c == '6') {
            return 6;
        }
        if (c == '7') {
            return 7;
        }
        if (c == '8') {
            return 8;
        }
        if (c == '9') {
            return 9;
        }
        return 0;
    }

    private static void welcomeScreen() {
        StdDraw.clear();
        StdDraw.text(0.5D, 0.77D, "Rogue Run: A Random Story");
        StdDraw.text(0.5D, 0.6D, "New Game (N)");
        StdDraw.text(0.5D, 0.55D, "Load Game (L)");
        StdDraw.text(0.5D, 0.5D, "Replay Saved Game (R)");
        StdDraw.text(0.5D, 0.45D, "Quit (Q)");
        StdDraw.show();
    }

    private static long inputN(String s) {
        char[] chars = s.toCharArray();
        long i = 0;
        for (char c : chars) {
            if (isNum(c)) {
                i = i * 10 + charNum(c);
            } else {
                return i;
            }
        }
        return i;
    }

    private static long longLen(long l) {
        if (l == 0) {
            return 0;
        }
        return l % 10 + longLen(l / 10);
    }

    private static TETile[][] initializeArray() {
        TETile[][] world = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }
        return world;
    }

    private void mainMenuUI() {
        boolean nPressed = false;
        StdDraw.clear();
        StdDraw.setCanvasSize(WIDTH * 10, HEIGHT * 10);
        StdDraw.setPenColor(StdDraw.BLACK);
        welcomeScreen();
        long seed = 0;
        String prev = "";
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                if ((c == 'N' || c == 'n') && !nPressed) {
                    StdDraw.text(0.5D, 0.3D, "Enter seed.");
                    nPressed = true;
                    prev += c;
                } else if ((c == 'S' || c == 's')
                        && nPressed
                        && isNum(prev.charAt(prev.length() - 1))) {
                    prev += c;
                    StdDraw.clear();
                    playGame(prev.toLowerCase());
                } else if (isNum(c)
                        && nPressed
                        && prev.toLowerCase().endsWith("n")) {
                    seed = charNum(c);
                    StdDraw.setPenColor(StdDraw.WHITE);
                    StdDraw.filledRectangle(0.5D, 0.3D, 0.5D, 0.03D);
                    StdDraw.setPenColor(StdDraw.BLACK);
                    StdDraw.text(0.5D, 0.3D, "Enter seed. Press S to proceed.");
                    StdDraw.text(0.5D, 0.2D, String.valueOf(seed));
                    prev += c;
                } else if (isNum(c)
                        && nPressed
                        && isNum(prev.toLowerCase().charAt(prev.length() - 1))) {
                    seed = seed * 10 + charNum(c);
                    if (seed < 0) {
                        seed = 9223372036854775807L;
                    }
                    prev += c;
                    StdDraw.setPenColor(StdDraw.WHITE);
                    StdDraw.filledRectangle(0.5D, 0.2D, 0.5D, 0.03D);
                    StdDraw.setPenColor(StdDraw.BLACK);
                    StdDraw.text(0.5D, 0.2D, String.valueOf(seed));
                } else if (c == 'q' || c == 'Q') {
                    System.exit(0);
                } else if ((c == 'l' || c == 'L') && !nPressed) {
                    File output = Paths.get("output.txt").toFile();
                    if (output.isFile()) {
                        try {
                            playGame(load());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else if ((c == 'r' || c == 'R') && !nPressed) {
                    File output = Paths.get("output.txt").toFile();
                    if (output.isFile()) {
                        replayGame();
                    }
                }
                StdDraw.show();
            }
        }
    }

    private void playGame(String s) {
        TETile[][] world = interactWithInputString(s);
        ter.initialize(WIDTH, HEIGHT + 10, 0, 10);
        String instructions = s;
        Coordinate playerPos = getPlayerPos(world);
        world = treasureCheck(world);
        ter.renderFrame(world);
        boolean limitedRender = false;
        while (true) {
            if (!limitedRender) {
                ter.customRenderFrame(world);
            } else {
                ter.customRenderFrame(limitedRenderArray(world, playerPos));
            }
            StdDraw.setPenColor(Color.WHITE);
            int x = (int) StdDraw.mouseX();
            int y = (int) StdDraw.mouseY() - 10;
            if (0 <= x && x < WIDTH && 0 <= y && y < HEIGHT) {
                if (limitedRender) {
                    StdDraw.textLeft(0.1D, 1.5D,
                            detailedDescription(
                                    limitedRenderArray(world, playerPos)[x][y].description()));
                } else {
                    StdDraw.textLeft(0.1D, 1.5D, detailedDescription(world[x][y].description()));
                }
            }
            StdDraw.show();
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                if (c == 'w' || c == 'W') {
                    world = moveCheck(world, world[playerPos.x][playerPos.y + 1], 'w');
                    instructions += c;
                }
                if (c == 'a' || c == 'A') {
                    world = moveCheck(world, world[playerPos.x - 1][playerPos.y], 'a');
                    instructions += c;
                }
                if (c == 's' || c == 'S') {
                    world = moveCheck(world, world[playerPos.x][playerPos.y - 1], 's');
                    instructions += c;
                }
                if (c == 'd' || c == 'D') {
                    world = moveCheck(world, world[playerPos.x + 1][playerPos.y], 'd');
                    instructions += c;
                }
                if (c == 'q' || c == 'Q') {
                    if (instructions.endsWith(":")) {
                        write(instructions.replaceAll(":", ""));
                        System.exit(0);
                    }
                }
                if (c == ':') {
                    instructions += c;
                }
                if (c == 't' || c == 'T') {
                    if (limitedRender) {
                        limitedRender = false;
                    } else {
                        limitedRender = true;
                    }
                }
                world = treasureCheck(world);
                playerPos = getPlayerPos(world);
            }
        }
    }

    private void youWin() {
        StdDraw.setCanvasSize(WIDTH * 10, HEIGHT * 10);
        StdDraw.clear(Color.black);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(0.5D, 0.5D, "You collected all of the treasure. You win!");
        StdDraw.text(0.5D, 0.3D, "Main menu (M)");
        StdDraw.text(0.5D, 0.2D, "Quit (Q)");
        StdDraw.show();
        StdDraw.setPenColor(Color.BLACK);
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                if (c == 'm' || c == 'M') {
                    mainMenuUI();
                }
                if (c == 'q' || c == 'Q') {
                    System.exit(0);
                }
            }
        }
    }

    private void youLose() {
        StdDraw.setCanvasSize(WIDTH * 10, HEIGHT * 10);
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.setFont(new Font("SansSerif", Font.BOLD, 16));
        StdDraw.text(0.5D, 0.5D, "You stepped on a trap. You lose.");
        StdDraw.text(0.5D, 0.3D, "Main menu (M)");
        StdDraw.text(0.5D, 0.2D, "Quit (Q)");
        StdDraw.show();
        StdDraw.setPenColor(StdDraw.BLACK);
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                if (c == 'm' || c == 'M') {
                    mainMenuUI();
                }
                if (c == 'q' || c == 'Q') {
                    System.exit(0);
                }
            }
        }
    }

    private Coordinate getPlayerPos(TETile[][] world) {
        for (int x = 0; x < world.length; x++) {
            for (int y = 0; y < world[x].length; y++) {
                if (world[x][y] == Tileset.AVATAR) {
                    return new Coordinate(x, y);
                }
            }
        }
        return null;
    }

    //@Source https://stackabuse.com/java-save-write-string-into-a-file/
    private void write(String s) {
        Path path = Paths.get("output.txt");
        try {
            Files.writeString(path, s, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            // Handle exception
        }
    }

    private String load() throws IOException {
        Scanner scanner = new Scanner(Paths.get("output.txt"));
        return scanner.nextLine();
    }

    private TETile[][] makeNewMap(long seed) {
        random = new Random(seed);
        TETile[][] world = initializeArray();
        return makeMap(world);
    }

    private TETile[][] makeMap(TETile[][] world) {
        BSPTree bspTree = new BSPTree(random, WIDTH, HEIGHT);
        ArrayList<Box> rooms = BSPTree.leaves(bspTree);
        ArrayList<Box> roomsUsed = new ArrayList<>();
        ArrayList<Box> roomsToBeUsed = new ArrayList<>();
        ArrayList<Box> roomsUnUsed = new ArrayList<>(rooms);

        while (roomsToBeUsed.size() < 0.6 * rooms.size()) {
            for (Box room : roomsUnUsed) {
                if (random.nextDouble() < 0.3) {
                    roomsToBeUsed.add(room);
                }
            }
            for (Box room : roomsToBeUsed) {
                roomsUnUsed.remove(room);
            }
        }
        Box largestReject = null;
        for (Box room : roomsUnUsed) {
            if (largestReject == null
                    || room.getArea() > largestReject.getArea()) {
                largestReject = room;
            }
        }
        for (Box room : roomsToBeUsed) {
            room = room.shrink(random);
            addRoom(world, room);
            roomsUsed.add(room);
        }

        Box prev = roomsUsed.get(0);
        for (Box room : roomsUsed) {
            connect(world, room, prev);
            prev = room;
        }
        int nearestHorizontal = WIDTH;
        int nearestVertical = HEIGHT;
        Box horNeighbor = roomsUsed.get(0), verNeighbor = roomsUsed.get(roomsUsed.size() - 1);
        for (Box room : roomsUsed) {
            if (largestReject != null
                    && room.getCenterX() - largestReject.getCenterX() < nearestHorizontal) {
                verNeighbor = room;
            }
            if (largestReject != null
                    && room.getCenterY() - largestReject.getCenterY() < nearestHorizontal) {
                horNeighbor = room;
            }
        }
        world = connect(world, verNeighbor, horNeighbor);
        world = addAvatar(world);
        world = addTreasure(world);
        world = addDoor(world);
        return world;
    }

    private TETile[][] addAvatar(TETile[][] world) {
        ArrayList<Coordinate> floorTiles = new ArrayList<>();
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (world[x][y] == Tileset.FLOOR) {
                    floorTiles.add(new Coordinate(x, y));
                }
            }
        }
        Coordinate tileChosen = floorTiles.get(RandomUtils.uniform(random, floorTiles.size()));
        world[tileChosen.x][tileChosen.y] = Tileset.AVATAR;
        return world;
    }

    private TETile[][] addDoor(TETile[][] world) {
        ArrayList<Coordinate> suitableWalls = new ArrayList<>();
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (world[x][y] == Tileset.WALL) {
                    if ((x - 1 >= 0 && world[x - 1][y] == Tileset.FLOOR)
                            || (y - 1 >= 0 && world[x][y - 1] == Tileset.FLOOR)
                            || (x + 1 < WIDTH && world[x + 1][y] == Tileset.FLOOR)
                            || (y + 1 < HEIGHT && world[x][y + 1] == Tileset.FLOOR)) {
                        suitableWalls.add(new Coordinate(x, y));
                    }
                }
            }
        }
        Coordinate tileChosen = suitableWalls.get(
                RandomUtils.uniform(random, suitableWalls.size()));
        world[tileChosen.x][tileChosen.y] = Tileset.LOCKED_DOOR;
        return world;
    }

    private TETile[][] treasureCheck(TETile[][] world) {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (world[x][y] == Tileset.TREASURE) {
                    return world;
                }
            }
        }
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (world[x][y] == Tileset.LOCKED_DOOR) {
                    world[x][y] = Tileset.UNLOCKED_DOOR;
                }
            }
        }
        return world;
    }

    private TETile[][] moveCheck(TETile[][] world, TETile move, char ch) {
        if (move == Tileset.FLOOR || move == Tileset.TREASURE) {
            Coordinate c = getPlayerPos(world);
            world[c.x][c.y] = Tileset.FLOOR;
            if (ch == 'w') {
                world[c.x][c.y + 1] = Tileset.AVATAR;
            }
            if (ch == 'a') {
                world[c.x - 1][c.y] = Tileset.AVATAR;
            }
            if (ch == 's') {
                world[c.x][c.y - 1] = Tileset.AVATAR;
            }
            if (ch == 'd') {
                world[c.x + 1][c.y] = Tileset.AVATAR;
            }
        }
        if (move == Tileset.UNLOCKED_DOOR) {
            youWin();
        }
        if (move == Tileset.TRAP) {
            youLose();
        }
        return world;
    }

    private TETile[][] addTreasure(TETile[][] world) {
        ArrayList<Coordinate> treasureTiles = new ArrayList<>();
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (world[x][y] == Tileset.FLOOR) {
                    treasureTiles.add(new Coordinate(x, y));
                }
            }
        }
        int numTreasure = RandomUtils.uniform(random, 5, 10);
        for (int i = 0; i < numTreasure; i++) {
            Coordinate tileChosen = treasureTiles.remove(
                    RandomUtils.uniform(random, treasureTiles.size()));
            world[tileChosen.x][tileChosen.y] = Tileset.TREASURE;
        }
        return world;
    }

    private TETile[][] addTrap(TETile[][] world) {
        ArrayList<Coordinate> trapTiles = new ArrayList<>();
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (world[x][y] == Tileset.FLOOR) {
                    trapTiles.add(new Coordinate(x, y));
                }
            }
        }
        for (int i = 0; i < 10; i++) {
            Coordinate tileChosen = trapTiles.remove(RandomUtils.uniform(random, trapTiles.size()));
            world[tileChosen.x][tileChosen.y] = Tileset.TRAP;
        }
        return world;
    }

    private TETile[][] addRoom(TETile[][] world, Box box) {
        int x = box.bottomLeft.x;
        while (x <= box.topRight.x) {
            int y = box.bottomLeft.y;
            while (y <= box.topRight.y) {
                if (x == box.bottomLeft.x
                        || x == box.topRight.x
                        || y == box.bottomLeft.y
                        || y == box.topRight.y) {
                    world[x][y] = Tileset.WALL;
                } else {
                    world[x][y] = Tileset.FLOOR;
                }
                y++;
            }
            x++;
        }
        int numTraps = RandomUtils.uniform(random, 3, 6);
        for (int i = 0; i < numTraps; i++) {
            int xTrap = RandomUtils.uniform(random, box.bottomLeft.x + 1, box.topRight.x);
            int yTrap = RandomUtils.uniform(random, box.bottomLeft.y + 1, box.topRight.y);
            world[xTrap][yTrap] = Tileset.TRAP;
        }
        return world;
    }

    private TETile[][] connect(TETile[][] world, Box c1, Box c2) {
        int connectionX1 = RandomUtils.uniform(random, c1.bottomLeft.x + 1, c1.topRight.x);
        int connectionX2 = RandomUtils.uniform(random, c2.bottomLeft.x + 1, c2.topRight.x);
        int connectionY1 = RandomUtils.uniform(random, c1.bottomLeft.y + 1, c1.topRight.y);
        int connectionY2 = RandomUtils.uniform(random, c2.bottomLeft.y + 1, c2.topRight.y);
        Coordinate connection1 = new Coordinate(connectionX1, connectionY1);
        Coordinate connection2 = new Coordinate(connectionX2, connectionY2);
        Coordinate current, greater;
        if (connectionX1 <= connectionX2) {
            current = new Coordinate(connectionX1, connectionY1);
            greater = connection2;
        } else {
            current =  new Coordinate(connectionX2, connectionY2);
            greater = connection1;
        }
        while (current.x < greater.x) {
            world[current.x][current.y] = Tileset.FLOOR;
            if (world[current.x][current.y + 1] == Tileset.NOTHING) {
                world[current.x][current.y + 1] = Tileset.WALL;
            }
            if (world[current.x][current.y - 1] == Tileset.NOTHING) {
                world[current.x][current.y - 1] = Tileset.WALL;
            }
            current.x++;
        }
        if (world[current.x + 1][current.y] == Tileset.NOTHING) {
            world[current.x + 1][current.y] = Tileset.WALL;
        }
        if (world[current.x - 1][current.y] == Tileset.NOTHING) {
            world[current.x - 1][current.y] = Tileset.WALL;
        }
        if (world[current.x - 1][current.y - 1] == Tileset.NOTHING) {
            world[current.x - 1][current.y - 1] = Tileset.WALL;
        }
        if (world[current.x + 1][current.y - 1] == Tileset.NOTHING) {
            world[current.x + 1][current.y - 1] = Tileset.WALL;
        }
        if (world[current.x][current.y + 1] == Tileset.NOTHING) {
            world[current.x][current.y + 1] = Tileset.WALL;
        }
        if (world[current.x][current.y - 1] == Tileset.NOTHING) {
            world[current.x][current.y - 1] = Tileset.WALL;
        }
        if (world[current.x + 1][current.y + 1] == Tileset.NOTHING) {
            world[current.x + 1][current.y + 1] = Tileset.WALL;
        }
        if (world[current.x - 1][current.y + 1] == Tileset.NOTHING) {
            world[current.x - 1][current.y + 1] = Tileset.WALL;
        }
        while (current.y < greater.y) {
            world[current.x][current.y] = Tileset.FLOOR;
            if (world[current.x + 1][current.y] == Tileset.NOTHING) {
                world[current.x + 1][current.y] = Tileset.WALL;
            }
            if (world[current.x - 1][current.y] == Tileset.NOTHING
                    && world[current.x - 1][current.y] != Tileset.FLOOR) {
                world[current.x - 1][current.y] = Tileset.WALL;
            }
            current.y++;
        }
        while (current.y > greater.y) {
            world[current.x][current.y] = Tileset.FLOOR;
            if (world[current.x + 1][current.y] == Tileset.NOTHING) {
                world[current.x + 1][current.y] = Tileset.WALL;
            }
            if (world[current.x - 1][current.y] == Tileset.NOTHING
                    && world[current.x - 1][current.y] != Tileset.FLOOR) {
                world[current.x - 1][current.y] = Tileset.WALL;
            }
            current.y--;
        }
        return world;
    }

    private static class Box {
        Coordinate bottomLeft, topRight;

        Box(Coordinate bottomLeft, Coordinate topRight) {
            this.bottomLeft = new Coordinate(bottomLeft);
            this.topRight = new Coordinate(topRight);
        }

        Box(Box box) {
            this.bottomLeft = new Coordinate(box.bottomLeft);
            this.topRight = new Coordinate(box.topRight);
        }

        public int getHeight() {
            return topRight.y - bottomLeft.y;
        }

        public int getWidth() {
            return topRight.x - bottomLeft.x;
        }

        public int getArea() {
            return getHeight() * getWidth();
        }

        public int getCenterX() {
            return (bottomLeft.x + topRight.x) / 2;
        }

        public int getCenterY() {
            return (bottomLeft.y + topRight.y) / 2;
        }

        public int getSplit(Random random, boolean vertical) {
            int splitPoint;
            if (vertical) {
                if (getHeight() < 2 * MIN_ROOM_SIZE) {
                    splitPoint = bottomLeft.y;
                } else {
                    splitPoint = (int) RandomUtils.gaussian(random,
                            (int) (0.5 * (bottomLeft.y + topRight.y)),
                            (int) (0.15 * (topRight.y - bottomLeft.y)));
                }
            } else {
                if (getWidth() < 2 * MIN_ROOM_SIZE) {
                    splitPoint = bottomLeft.x;
                } else {
                    splitPoint = (int) RandomUtils.gaussian(random,
                            (int) (0.5 * (bottomLeft.x + topRight.x)),
                            (int) (0.15 * (topRight.x - bottomLeft.x)));
                }
            }
            return splitPoint;
        }

        public Box shrink(Random random) {
            Box shrunk = new Box(this);
            int shrinkWidthRange = (getWidth() - MIN_ROOM_SIZE) / 2;
            int shrinkHeightRange = (getHeight() - MIN_ROOM_SIZE) / 2;
            if (shrinkWidthRange > 1) {
                shrunk.bottomLeft.x += RandomUtils.uniform(random,
                        0,
                        (getWidth() - MIN_ROOM_SIZE) / 2);
                shrunk.topRight.x -= RandomUtils.uniform(random,
                        0,
                        (getWidth() - MIN_ROOM_SIZE) / 2);
            }
            if (shrinkHeightRange > 1) {
                shrunk.bottomLeft.y += RandomUtils.uniform(random,
                        0,
                        (getHeight() - MIN_ROOM_SIZE) / 2);
                shrunk.topRight.y -= RandomUtils.uniform(random,
                        0,
                        (getHeight() - MIN_ROOM_SIZE) / 2);
            }
            return shrunk;
        }
    }

    public static class Coordinate {
        int x, y;

        Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        Coordinate(Coordinate c) {
            x = c.x;
            y = c.y;
        }

        public boolean equals(Coordinate c) {
            return this.x == c.x && this.y == c.y;
        }
    }

    private static class BSPTree {
        int iterations;
        BSPTree left;
        BSPTree right;
        Box box;
        Random random;

        BSPTree(Random random, int width, int height) {
            this.iterations = 4;
            this.random = random;
            this.box = new Box(new Coordinate(0, 0), new Coordinate(width - 1, height - 1));
            newInit();
        }

        private BSPTree(int iterations, Random random, Box box) {
            this.iterations = iterations;
            this.random = random;
            this.box = box;
            newInit();
        }

        private void newInit() {
            if (box.getHeight() * box.getWidth() > 600
                    && iterations == 0) {
                this.iterations++;
            }
            if (iterations > 0) {
                //split in x direction
                int splitPoint = 0;
                Box b1 = null, b2 = null;
                if (random.nextDouble() > 0.5) {
                    splitPoint = box.getSplit(random, false);
                    b1 = new Box(
                            new Coordinate(this.box.bottomLeft),
                            new Coordinate(splitPoint, this.box.topRight.y));
                    b2 = new Box(
                            new Coordinate(splitPoint, box.bottomLeft.y),
                            new Coordinate(this.box.topRight));
                    if (b1.getWidth() <= MIN_ROOM_SIZE || b2.getWidth() <= MIN_ROOM_SIZE) {
                        this.left = null;
                        this.right = null;
                    } else {
                        this.left = new BSPTree(iterations - 1, random, b1);
                        this.right = new BSPTree(iterations - 1, random, b2);
                    }
                } else {
                    splitPoint = box.getSplit(random, true);
                    b1 = new Box(
                            new Coordinate(box.bottomLeft),
                            new Coordinate(box.topRight.x, splitPoint));
                    b2 = new Box(
                            new Coordinate(box.bottomLeft.x, splitPoint),
                            new Coordinate(box.topRight));
                    if (b1.getHeight() <= MIN_ROOM_SIZE || b2.getHeight() <= MIN_ROOM_SIZE) {
                        this.left = null;
                        this.right = null;
                    } else {
                        this.left = new BSPTree(iterations - 1, random, b1);
                        this.right = new BSPTree(iterations - 1, random, b2);
                    }
                }
            } else {
                left = null;
                right = null;
            }
        }

        public void reset() {
            left = null;
            right = null;
            newInit();
        }

        public static ArrayList<Box> leaves(BSPTree tree) {
            ArrayList<Box> boxes = new ArrayList<>();
            if (tree.left != null) {
                boxes.addAll(leaves(tree.left));
            }
            if (tree.right != null) {
                boxes.addAll(leaves(tree.right));
            }
            if (tree.left == null && tree.right == null) {
                boxes.add(tree.box);
            }
            return boxes;
        }
    }

}
