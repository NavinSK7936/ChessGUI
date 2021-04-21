package Chess;

import javax.swing.*;

public class King extends Piece {
	int y, x;
	public static final byte ds[][] = {{-1, -1}, {-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}};
	public boolean notMoved = true, onCheck = false;
	
	King(boolean isWhite, int y, int x) {
		super.rep = (super.isWhite = isWhite) ? '\u2654' : '\u265A';
		this.setPos(y, x);}
	
	public void setPos(int y, int x) {this.y = y;	this.x = x;}
	
	public boolean isCheck(Piece pieces[][], JLabel labels[][], boolean ck) {
		int i, j;
		for(byte dl[]: Knight.ds)
			for(byte c = 0; c < 2; c++)
				if(super.inBoard(i = y + dl[c], j = x + dl[1-c]))
					if(pieces[i-1][j-1] != null)
						if(pieces[i-1][j-1].isWhite != this.isWhite && pieces[i-1][j-1] instanceof Knight)
							if(ck)	return true;
							else	labels[i][j].setBackground(ChessBoard.wonColor);
		for(byte dl[]: Rook.ds)
			for(i = y, j = x; super.inBoard(i += dl[0], j += dl[1]);)
				if(pieces[i-1][j-1] != null) {
					if(pieces[i-1][j-1].isWhite != this.isWhite)
						if(pieces[i-1][j-1] instanceof Queen || pieces[i-1][j-1] instanceof Rook)
							if(ck)	return true;
							else	labels[i][j].setBackground(ChessBoard.wonColor);
					break;}
		for(byte dl[]: Bishop.ds)
			for(i = y, j = x; super.inBoard(i += dl[0], j += dl[1]);)
				if(pieces[i-1][j-1] != null) {
					if(pieces[i-1][j-1].isWhite != this.isWhite)
						if(pieces[i-1][j-1] instanceof Queen || pieces[i-1][j-1] instanceof Bishop)
							if(ck)	return true;
							else	labels[i][j].setBackground(ChessBoard.wonColor);
					break;}
		for(byte dl: Pawn.ds)
			if(super.inBoard(i = y + (this.isWhite ? -1 : 1), j = x + dl))
				if(pieces[i-1][j-1] != null)
					if(pieces[i-1][j-1].isWhite != this.isWhite && pieces[i-1][j-1] instanceof Pawn)
						if(ck)	return true;
						else	labels[i][j].setBackground(ChessBoard.wonColor);
		for(byte dl[]: King.ds)
			if(super.inBoard(i = y + dl[0], j = x + dl[1]))
				if(pieces[i-1][j-1] != null)
					if(pieces[i-1][j-1].isWhite != this.isWhite && pieces[i-1][j-1] instanceof King)
						if(ck)	return true;
						else	labels[i][j].setBackground(ChessBoard.wonColor);
		return false;
	}
	
	public boolean isStaleMate(Piece pieces[][], JLabel labels[][]) {
		for(int i = 0; i < 8; i++)
			for(int j = 0; j < 8; j++)
				if(pieces[i][j] != null)
					if(pieces[i][j].isWhite == this.isWhite)
						if(pieces[i][j].setOrCheck(pieces, labels, this, i+1, j+1, true))
							return false;
		return true;
	}
	
	@Override public boolean setOrCheck(Piece pieces[][], JLabel labels[][], King king, int y, int x, boolean ck){
		Piece curr = pieces[y-1][x-1], prev;
		pieces[y-1][x-1] = null;
		int i, j;
		for(byte dl[]: this.ds)
			if(super.inBoard(i = y + dl[0], j = x + dl[1])) {
				if(pieces[i-1][j-1] != null)	if(pieces[i-1][j-1].isWhite == isWhite)	continue;
				prev = pieces[i-1][j-1];
				pieces[i-1][j-1] = curr;
				king.setPos(i, j);
				if(!king.isCheck(pieces, labels, true)) {
					if(ck)	{
						pieces[i-1][j-1] = prev;
						pieces[y-1][x-1] = curr;
						king.setPos(y, x);
						return true;}
					labels[i][j].setBackground(prev == null ? ChessBoard.emptyColor : ChessBoard.killColor);}
				pieces[i-1][j-1] = prev;}
		
		Rook rook;
		if(king.notMoved && !king.onCheck)
			for(byte dx: Pawn.ds)
				if(pieces[y-1][x+2*dx-1] == null && labels[y][x+dx].getBackground().equals(ChessBoard.emptyColor))
					if(pieces[y-1][dx == -1 ? 0 : 7] instanceof Rook)
						if((rook = (Rook)pieces[y-1][dx == -1 ? 0 : 7]).notMoved) {
							if(dx == -1 && pieces[y-1][1] != null)	continue;
							pieces[y-1][x+2*dx-1] = curr;
							pieces[y-1][x+dx-1] = pieces[y-1][dx == -1 ? 0 : 7];
							pieces[y-1][dx == -1 ? 0 : 7] = null;
							king.setPos(y, x+2*dx);
							if(!king.isCheck(pieces, labels, true)) {
								if(ck)	{
									pieces[y-1][x+2*dx-1] = null;
									pieces[y-1][dx == -1 ? 0 : 7] = pieces[y-1][x+dx-1];
									pieces[y-1][x+dx-1] = null;
									pieces[y-1][x-1] = curr;
									king.setPos(y, x);
									return true;}
								labels[y][x+2*dx].setBackground(ChessBoard.castleColor);}
							pieces[y-1][x+2*dx-1] = null;
							pieces[y-1][dx == -1 ? 0 : 7] = pieces[y-1][x+dx-1];
							pieces[y-1][x+dx-1] = null;}
		pieces[y-1][x-1] = curr;
		king.setPos(y, x);
		
		return false;
	}
	public void setWonColor(Piece pieces[][], JLabel labels[][]) {
		Piece prev;
		int i, j, yPrev = y, xPrev = x;
		this.isCheck(pieces, labels, false);
		for(byte dl[]: this.ds)
			if(super.inBoard(i = yPrev + dl[0], j = xPrev + dl[1])) {
				if(pieces[i-1][j-1] != null)	if(pieces[i-1][j-1].isWhite == this.isWhite)	continue;
				prev = pieces[i-1][j-1];
				pieces[i-1][j-1] = this;
				this.setPos(i, j);
				this.isCheck(pieces, labels, false);
				pieces[i-1][j-1] = prev;}
		this.setPos(yPrev, xPrev);
		pieces[this.y-1][this.x-1] = this;
	}
}