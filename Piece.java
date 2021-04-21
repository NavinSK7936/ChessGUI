package Chess;

import java.io.*;

import javax.swing.*;

public abstract class Piece implements Serializable {
	public char rep;
	public boolean isWhite;
	public static boolean inBoard(int y, int x) {return (y > 0 && y < 9) && (x > 0 && x < 9);}
	abstract public boolean setOrCheck(Piece pieces[][], JLabel labels[][], King king, int y, int x, boolean ck);
}