package byow.lab12;
import org.junit.Test;
import static org.junit.Assert.*;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {
    public HexWorld(int width, int height) {
        final int WIDTH = width;
        final int HEIGHT = height;
    }
    public HexWorld() {
        final int WIDTH = 60;
        final int HEIGHT = 30;
    }
    public static void addHexagon(int s, int pos, TETile type) {
        int len = lenCalcWide(s);
        StringBuilder str = new StringBuilder("a");
        for (int i = 0; i < len; i++) {
            str.append("a");
        }
        System.out.println(str);
        for (int i = 0; i < s; i++) {
            String st = " " + str.substring(i + 1, str.length() - i - 1) + " ";
            System.out.println(st);
        }
    }
    private static int lenCalcWide(int s) {
        /* Calculates the length of the widest part
        of the hexagon.
        side length 1:       side length 2:
         a                        aaa
        aaa --> length 3         aaaaa
        aaa                     aaaaaaa --> length 4
         a                      aaaaaaa
                                 aaaaa
                                  aaa
         */
        if (s == 1) {
            return 3;
        } else if (s == 2) {
            return 4;
        } else if (s > 2) {
            return 3 + lenCalcWide(s - 1);
        } else {
            return 0;
        }
    }
}
