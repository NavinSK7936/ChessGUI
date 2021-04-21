package Chess;

import javax.swing.*;

public class Knight extends Piece {
	public static final byte ds[][] = {{-1, -2}, {-1, 2}, {1, -2}, {1, 2}};
	
	Knight(boolean isWhite) {	super.rep = (super.isWhite = isWhite) ? '\u2658' : '\u265E';}
	@Override public boolean setOrCheck(Piece pieces[][], JLabel labels[][], King king, int y, int x, boolean ck){
		Piece curr = pieces[y-1][x-1], prev;
		pieces[y-1][x-1] = null;
		int i, j;
		for(byte dl[]: this.ds)
			for(byte c = 0; c < 2; c++)
				if(super.inBoard(i = y + dl[c], j = x + dl[1-c])) {
					if(pieces[i-1][j-1] != null)	if(pieces[i-1][j-1].isWhite == isWhite)	continue;
					prev = pieces[i-1][j-1];
					pieces[i-1][j-1] = curr;
					if(!king.isCheck(pieces, labels, true)) {
						if(ck)	{
							pieces[i-1][j-1] = prev;
							pieces[y-1][x-1] = curr;
							return true;}
						labels[i][j].setBackground(prev == null ? ChessBoard.emptyColor : ChessBoard.killColor);}
					pieces[i-1][j-1] = prev;}
		pieces[y-1][x-1] = curr;
		return false;
	}
}