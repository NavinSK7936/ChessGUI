package Chess;

import javax.swing.*;

public class Queen extends Piece {
	Queen(boolean isWhite) {	super.rep = (super.isWhite = isWhite) ? '\u2655' : '\u265B';}
	
	@Override public boolean setOrCheck(Piece pieces[][], JLabel labels[][], King king, int y, int x, boolean ck){
		Piece curr = pieces[y-1][x-1], prev;
		pieces[y-1][x-1] = null;
		int i, j;
		for(byte dl[]: King.ds)
			for(i = y, j = x; super.inBoard(i += dl[0], j += dl[1]);) {
				if(pieces[i-1][j-1] != null)	if(pieces[i-1][j-1].isWhite == this.isWhite)	break;
				prev = pieces[i-1][j-1];
				pieces[i-1][j-1] = curr;
				if(!king.isCheck(pieces, labels, true)) {
					if(ck)	{
						pieces[i-1][j-1] = prev;
						pieces[y-1][x-1] = curr;
						return true;}
					labels[i][j].setBackground(prev == null ? ChessBoard.emptyColor : ChessBoard.killColor);}
				pieces[i-1][j-1] = prev;
				if(pieces[i-1][j-1] != null)	if(pieces[i-1][j-1].isWhite != this.isWhite)	break;}
		pieces[y-1][x-1] = curr;
		return false;
	}
}