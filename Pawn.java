package Chess;

import javax.swing.*;

public class Pawn extends Piece {
	public static final byte ds[] = {-1, 1};
	public boolean canEnPass = true, notMoved = true;
	
	Pawn(boolean isWhite) {	super.rep = (super.isWhite = isWhite) ? '\u2659' : '\u265F';}
	
	@Override public boolean setOrCheck(Piece pieces[][], JLabel labels[][], King king, int y, int x, boolean ck){
		Piece curr = pieces[y-1][x-1], prev;
		pieces[y-1][x-1] = null;
		int i = y + (isWhite ? -1 : 1), j = x;
		for(byte c = 0; Math.abs(c) < (this.notMoved ? 2 : 1); c += isWhite ? -1 : 1)
			if(pieces[i+c-1][j-1] == null) {
				pieces[i+c-1][j-1] = curr;
				if(!king.isCheck(pieces, labels, true)) {
					if(ck)	{
						pieces[i+c-1][j-1] = null;
						pieces[y-1][x-1] = curr;
						return true;}
					labels[i+c][j].setBackground(i == 1 || i == 8 ? ChessBoard.promColor : ChessBoard.emptyColor);}
				pieces[i+c-1][j-1] = null;}
			else	break;
		for(byte dl: this.ds)
			if(super.inBoard(i, j = x + dl))
				if(pieces[i-1][j-1] != null) {
					if(pieces[i-1][j-1].isWhite != this.isWhite) {
						prev = pieces[i-1][j-1];
						pieces[i-1][j-1] = curr;
						if(!king.isCheck(pieces, labels, true)) {
							if(ck)	{
								pieces[i-1][j-1] = prev;
								pieces[y-1][x-1] = curr;
								return true;}
							labels[i][j].setBackground(i == 1 || i == 8 ? ChessBoard.promColor : ChessBoard.killColor);}
						pieces[i-1][j-1] = prev;}}
				else if(y == (this.isWhite ? 4 : 5) && pieces[y-1][j-1] != null)
					if(pieces[y-1][j-1].isWhite != this.isWhite && pieces[y-1][j-1] instanceof Pawn)
						if(((Pawn)pieces[y-1][j-1]).canEnPass) {
							prev = pieces[y-1][j-1];	pieces[y-1][j-1] = null;
							pieces[i-1][j-1] = curr;
							if(!king.isCheck(pieces, labels, true)) {
								if(ck)	{
									pieces[i-1][j-1] = null;
									pieces[y-1][j-1] = prev;
									pieces[y-1][x-1] = curr;
									return true;}
								labels[i][j].setBackground(ChessBoard.killColor);}
							pieces[i-1][j-1] = null;
							pieces[y-1][j-1] = prev;}
		pieces[y-1][x-1] = curr;
		return false;
	}
}