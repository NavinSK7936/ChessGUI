package Chess;

import user.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import java.net.*;

import java.io.*;

/*from outside of Chess directory
javac Chess/ChessBoard.java
java Chess.ChessBoard
*/

class SerializationUtil {

	// deserialize to Object from given file
	public static Object deserialize(String fileName) throws IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream(fileName);
		ObjectInputStream ois = new ObjectInputStream(fis);
		Object obj = ois.readObject();
		ois.close();
		return obj;
	}

	// serialize the given object and save it to file
	public static void serialize(Object obj, String fileName)
			throws IOException {
		FileOutputStream fos = new FileOutputStream(fileName);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(obj);

		fos.close();
	}

}

class ChessBoard extends JFrame implements MouseListener, ActionListener {
	
	private static final String col = "abcdefgh";
	private String pos;
	private boolean isWhite = true, isClicked, onCheck, isStaleMate, gameOver;

	private ATMTable scoreSheet;

	private String cacheText;
	private JTextField mouseField;
    
    private ImageIcon bgImg = new ImageIcon("Chess\\bg.jpg"), iconImg = new ImageIcon("Chess\\icon.jpg");
	
	private Piece pieces[][];
	private JLabel labels[][], label;
	
	private King wKing, bKing;
	private Pawn prev;

	private static final String promPiece[] = {"Queen", "Rook", "Bishop", "Knight"};

	public static final Color clickColor = new Color(130,144,255);
	public static final Color emptyColor = new Color(127,255,212);
	public static final Color killColor = new Color(255, 20, 90);
	public static final Color promColor = new Color(102, 203, 0);
	public static final Color castleColor = new Color(255,105,180);
	public static final Color checkColor = new Color(250,20,60);
	public static final Color wonColor = new Color(255, 50, 255);
	
	private static final HashMap<String, BoardColor> colors = new HashMap<String, BoardColor>() {{
		put("Old Blue", new BoardColor(new Color(51, 153, 255), new Color(204, 229, 255)));
		put("Salmon Theme", new BoardColor(new Color(250,128,114), new Color(255,248,220)));
		put("Wood Board", new BoardColor(new Color(205,133,63), new Color(255,235,205)));
		put("Lime Theme", new BoardColor(new Color(0, 153, 0), new Color(255, 255, 204)));
		put("Standard", new BoardColor(Color.LIGHT_GRAY, Color.WHITE));}};
	
	private static class BoardColor implements Serializable {
		Color bSquare, wSquare;
		BoardColor(Color bSquare, Color wSquare) {
			this.bSquare = bSquare;
			this.wSquare = wSquare;}}
	
	private Color bSquare;
	private Color wSquare;
	
	private BevelBorder lwrBorder = new BevelBorder(BevelBorder.LOWERED, Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK), rsdBorder = new BevelBorder(BevelBorder.RAISED, Color.BLACK, Color.BLACK);
	private Runnable runSetOrCheck;
	
	private JMenu game, view, customizeBoard, help;
	
	private final ArrayList<JMenuItem> gameMenuItems = new ArrayList<JMenuItem>() {{
    	add(new JMenuItem("New Game"));
	   	add(new JMenuItem("Restart Game"));
	    add(new JMenuItem("Draw Game"));
	    add(new JMenuItem("Resign Game"));
    	add(new JMenuItem("Quit Game"));}};
	
	private final ArrayList<JRadioButtonMenuItem> choices = new ArrayList<JRadioButtonMenuItem>() {{
		colors.forEach((name, color) -> add(new JRadioButtonMenuItem(name)));}};
	
	private final ButtonGroup colorGroup = new ButtonGroup() {{
		for(JRadioButtonMenuItem choice: choices)
			add(choice);}};
	
	private JMenuItem about;
	
	private int y0, x0, y, x, yPrev, xPrev, dy, dx;

	/********************************************************************************/
	ChessBoard() {
		this.initMenu();
		this.initPieces();
		super.add(this.initFrame(this.initBoard(), this.initAid(this.initScoreSheet(), this.initMousePosition())));
		super.addWindowListener(new WindowAdapter() {
			@Override public void windowClosing(WindowEvent e) {
				Toolkit.getDefaultToolkit().beep();
				if(JOptionPane.showConfirmDialog(null, "Are you Sure? Do you want to quit the game?", "Exit Game", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
					dispose();
				setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);}});
		super.setBounds(500, 100, 940, 900);
		super.setIconImage(iconImg.getImage());
		this.repaintBoard("Standard");
		super.setResizable(false);
		super.setTitle("Chess");
		super.setVisible(true);
		
		runSetOrCheck = new Runnable() {
			@Override public void run() {
				pieces[y-1][x-1].setOrCheck(pieces, labels, isWhite ? wKing : bKing, yPrev = y, xPrev = x, false);}};}
	
	public static void main(String args[]) {
        try {
			//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			JFrame.setDefaultLookAndFeelDecorated(true);}
		catch(Exception e) {System.err.println(e.getMessage());}

        String fileName = "name.ser";
		File file = new File(fileName);
		ChessBoard board = null;

		System.out.println(file.exists() + " " + file.getAbsolutePath());

        if(file.exists()) {
        	try {
        	    board = (ChessBoard) SerializationUtil.deserialize(fileName);
            }
            catch (ClassNotFoundException | IOException exception) {
			    exception.printStackTrace();
            }

			board.setVisible(true);
			ChessBoard frame0 = board;
			board.addWindowListener(new WindowAdapter() {
				@Override public void windowClosing(WindowEvent event) {
					try {
                        SerializationUtil.serialize(frame0, fileName);
					}
					catch(IOException exception) {
						exception.printStackTrace();
					}
				}
			});
			board.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }

        else {
        	board = new ChessBoard();
        	ChessBoard frame0 = board;
        	board.addWindowListener(new WindowAdapter() {
				@Override public void windowClosing(WindowEvent event) {
					try {
                        SerializationUtil.serialize(frame0, fileName);
					}
					catch(IOException exception) {
						exception.printStackTrace();
					}
				}
			});
        }
    }
	
	/********************************************************************************/
    private void initMenu() {
    	this.initGameMenu();
		this.initViewMenu();
		this.initHelpMenu();

		this.setJMenuBar(new JMenuBar() {{
			add(game);
		    add(view);
			add(help);}});}

	private void initGameMenu() {
		game = new JMenu("Game");
    	
    	int i = 0;
		for(JMenuItem menuItem: this.gameMenuItems) {
			menuItem.addActionListener(this);
			game.add(menuItem);
			if(++i == 1 || i == 4)	game.addSeparator();}}
	
	private void initViewMenu() {
		view = new JMenu("View");
		customizeBoard = new JMenu("Customize Board");

		for(JRadioButtonMenuItem choice: choices) {
			customizeBoard.add(choice);
			choice.addActionListener(this);}
		view.add(customizeBoard);}
	
	private void initHelpMenu() {
		help = new JMenu("Help");
        about = new JMenuItem("About Chess");

        about.addActionListener(this);
		help.add(about);}
	
	private void initPieces() {
		this.pieces = new Piece[8][8];
		
		pieces[0][0] = new Rook(false);
		pieces[0][1] = new Knight(false);
		pieces[0][2] = new Bishop(false);
		pieces[0][3] = new Queen(false);
		pieces[0][4] = bKing = new King(false, 1, 5);
		pieces[0][5] = new Bishop(false);
		pieces[0][6] = new Knight(false);
		pieces[0][7] = new Rook(false);
		
		for(int j = 0; j < 8; pieces[1][j++] = new Pawn(false));
		
		for(int j = 0; j < 8; pieces[6][j++] = new Pawn(true));
		
		pieces[7][0] = new Rook(true);
		pieces[7][1] = new Knight(true);
		pieces[7][2] = new Bishop(true);
		pieces[7][3] = new Queen(true);
		pieces[7][4] = wKing = new King(true, 8, 5);
		pieces[7][5] = new Bishop(true);
		pieces[7][6] = new Knight(true);
		pieces[7][7] = new Rook(true);}
	
	private JPanel initBoard() {
		JPanel board = new JPanel(new GridLayout(9, 9));
		board.setBorder(new BevelBorder(BevelBorder.RAISED, Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK));
		board.setBackground(Color.BLACK);
		
		this.labels = new JLabel[9][9];
		
		board.add(labels[0][0] = getLabel("   \\\\\\ "));
		labels[0][0].setBackground(Color.BLACK);
		labels[0][0].setForeground(bSquare);
		
		for(int j = 1; j < 9; j++) {
			board.add(labels[0][j] = getLabel("  " + Character.toUpperCase(col.charAt(j-1))));
			labels[0][j].setBackground(Color.BLACK);
			labels[0][j].setForeground(bSquare);}
		
		for(int i = 1; i < 9; i++) {
			board.add(labels[i][0] = getLabel("  " + Integer.toString(9-i)));
			labels[i][0].setBackground(Color.BLACK);
			labels[i][0].setForeground(bSquare);
			for(int j = 1; j < 9; j++) {
				board.add(labels[i][j] = this.getLabel(""));
				labels[i][j].addMouseListener(this);
				labels[i][j].setBorder(j % 2 == 0 ? lwrBorder : rsdBorder);
				labels[i][j].setBackground(j % 2 == 0 ? new Color(255,248,220) : new Color(218,165,32));}}
		
		return board;}
	
	private JSplitPane initFrame(JPanel board, JSplitPane aid) {
		return new JSplitPane(SwingConstants.VERTICAL, board, aid) {{
		    this.setDividerLocation(735);
		    this.setOneTouchExpandable(true);}};}
	
	private JSplitPane initAid(JPanel cachePanel, JPanel mousePanel) {
		return new JSplitPane(SwingConstants.HORIZONTAL, cachePanel, mousePanel) {{
		    this.setDividerLocation(600);
			this.setResizeWeight(0.0);}};}
	
	private JPanel initScoreSheet() {
		return new JPanel() {{
		    this.add(new JLabel("ScoreSheet") {{
		    	this.setForeground(Color.WHITE);}});
			this.add(scoreSheet = new ATMTable(new String[] {"WHITE", "BLACK"}, new Dimension(150, 500)));}
			@Override protected void paintComponent(Graphics g) {
				super.paintComponent(g);
        		g.drawImage(bgImg.getImage(), 0, 0, this.getWidth(), this.getHeight(), null);}};}
	
	private JPanel initMousePosition() {
		return new JPanel() {{
			this.add(new JLabel("Mouse Position") {{
			    this.setForeground(Color.WHITE);}});
			this.add(mouseField = new JTextField(10) {{
				this.setEditable(false);
		        this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 5));
				this.setHorizontalAlignment(JTextField.CENTER);}});}
			@Override protected void paintComponent(Graphics g) {
				super.paintComponent(g);
        		g.drawImage(bgImg.getImage(), 0, 0, this.getWidth(), this.getHeight(), null);}};}
		    
	
	/********************************************************************************/
	private void resetLabel() {
		for(int i = 0; i < 8; i++)
			for(int j = 0; j < 8; j++) {
				labels[i+1][j+1].setBackground((i + j) % 2 == 0 ? wSquare : bSquare);
				labels[i+1][j+1].setText(" " + (pieces[i][j] == null ? "" : pieces[i][j].rep));}}
	
	private void repaintBoard(String colorName) {
		this.isClicked = false;
		this.bSquare = colors.get(colorName).bSquare;
		this.wSquare = colors.get(colorName).wSquare;
		
        for(JRadioButtonMenuItem choice: this.choices)
            if(choice.getText() == colorName)
                choice.setSelected(true);

		for(int c = 1; c < 9; c++) {
			this.labels[0][c].setForeground(bSquare);
			this.labels[c][0].setForeground(bSquare);}
		this.resetLabel();

		if(this.onCheck)
		    labels[(isWhite ? wKing : bKing).y][(isWhite ? wKing : bKing).x].setBackground(checkColor);
		if(this.gameOver)
		    (isWhite ? wKing : bKing).setWonColor(pieces, labels);}
	
	private void setScore(boolean isResign) {
		if(isResign)
			if(!this.isWhite)
				this.scoreSheet.setValueAt("    resigns!", scoreSheet.getRowCount() - 1, 1);
			else
				this.scoreSheet.appendRow(new Object[] {"    resigns!", ""});
		else
			if(this.isWhite)
				this.scoreSheet.setValueAt(new String(new char[6 - this.cacheText.length() / 2]).replace("\0", "  ") + this.cacheText, scoreSheet.getRowCount() - 1, 1);
			else
				this.scoreSheet.appendRow(new Object[] {new String(new char[6 - this.cacheText.length() / 2]).replace("\0", "  ") + this.cacheText, ""});}
	
	private JLabel getLabel(String text) {
		return new JLabel(text) {{
		    this.setFont(new Font("Serif", Font.BOLD, 50));
			this.setOpaque(true);}};}
	
	private Piece getPromPiece() {
		int n;
		while((n = JOptionPane.showOptionDialog(this, "Choose any piece to promote your Pawn!", "Promote your Pawn!", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, this.promPiece, this.promPiece[0])) == -1)
			Toolkit.getDefaultToolkit().beep();
		return n == 0 ? new Queen(this.isWhite) : n == 1 ? new Rook(this.isWhite) : n == 2 ? new Bishop(this.isWhite) : new Knight(this.isWhite);}
	
	private char getPrefix(Piece piece) {
		return piece instanceof Queen ? 'Q' : piece instanceof Rook ? 'R' : piece instanceof Bishop ? 'B' : piece instanceof Knight ? 'N' : piece instanceof King ? 'K' : '\0';}
	
	private String getThemeName() {
        for(JRadioButtonMenuItem choice: this.choices)
            if(choice.isSelected())
                return choice.getText();
        return null;}
	
	/********************************************************************************/
	@Override public void mouseClicked(MouseEvent e) {
		if(this.gameOver) {Toolkit.getDefaultToolkit().beep();return;}
		
		label = (JLabel) e.getSource();
		if(this.isClicked) {
			if(label.getBackground().equals(this.clickColor)) {
				this.resetLabel();
				if(this.onCheck)
					labels[(isWhite ? wKing : bKing).y][(isWhite ? wKing : bKing).x].setBackground(checkColor);
				this.isClicked = false;
				return;}
			else if(label.getBackground().equals(this.castleColor)) {
				cacheText = "O-O" + (x == 7 ? "" : "-O");
				
				pieces[y-1][x-1] = pieces[yPrev-1][xPrev-1];
				pieces[yPrev-1][xPrev-1] = null;
				pieces[yPrev-1][(x == 7 ? 6 : 4)-1] = pieces[y-1][(x == 7 ? 8 : 1)-1];
				pieces[y-1][(x == 7 ? 8 : 1)-1] = null;
				(isWhite ? wKing : bKing).setPos(y, x);
				(isWhite ? wKing : bKing).notMoved = false;}
			else if(label.getBackground().equals(this.emptyColor) || label.getBackground().equals(this.killColor)) {
				cacheText = (pieces[yPrev-1][xPrev-1] instanceof Pawn && Math.abs(xPrev-x) == 1 ? col.charAt(xPrev-1) : "") + "" + this.getPrefix(pieces[yPrev-1][xPrev-1]) + (label.getBackground().equals(this.killColor) ? "x" : "") + this.pos;
				
				pieces[y-1][x-1] = pieces[yPrev-1][xPrev-1];
				pieces[yPrev-1][xPrev-1] = null;
				
	/* Pawn */		if(pieces[y-1][x-1] instanceof Pawn && Math.abs(xPrev - x) == 1 && pieces[yPrev-1][x-1] == prev && prev != null)
					if(prev.canEnPass) {
						cacheText += "(e.p.)";
						pieces[yPrev-1][x-1] = null;}
				if(prev != null)
					prev.canEnPass = false;
				prev = null;
				if(pieces[y-1][x-1] instanceof Pawn) {
					prev = (Pawn) pieces[y-1][x-1];
					if(prev.notMoved) {
						prev.notMoved = false;
						if(Math.abs(yPrev - y) == 1) {
							prev.canEnPass = false;
							prev = null;}}}
				
	/* Rook */		if(pieces[y-1][x-1] instanceof Rook)
					((Rook) pieces[y-1][x-1]).notMoved = false;
				
	/* King */		if(pieces[y-1][x-1] instanceof King) {
					(isWhite ? wKing : bKing).setPos(y, x);
					(isWhite ? wKing : bKing).notMoved = false;}}
			else if(label.getBackground().equals(this.promColor)) {
				cacheText = this.pos + "=" + this.getPrefix(pieces[y-1][x-1] = this.getPromPiece());
				pieces[yPrev-1][xPrev-1] = null;}
			else {
				Toolkit.getDefaultToolkit().beep();
				return;}
			this.resetLabel();
			this.isWhite ^= true;
			this.isClicked = false;
			(isWhite ? wKing : bKing).onCheck = false;
			
			this.isStaleMate = (this.isWhite ? wKing : bKing).isStaleMate(pieces, labels);
			this.onCheck = (this.isWhite ? wKing : bKing).isCheck(pieces, labels, true);
			
			if(this.onCheck) {
				cacheText += "+";
				(isWhite ? wKing : bKing).onCheck = true;
				labels[(isWhite ? wKing : bKing).y][(isWhite ? wKing : bKing).x].setBackground(checkColor);
				if(this.isStaleMate) {
					cacheText += "+";
					this.gameOver = true;
					(isWhite ? wKing : bKing).setWonColor(pieces, labels);
					System.out.println("GameOver! " + (!this.isWhite ? "White" : "Black") + " won the Game!");
					Toolkit.getDefaultToolkit().beep();
					JOptionPane.showConfirmDialog(this, "           " + (!this.isWhite ? "White" : "Black") + " won the Game!", "GameOver!", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);}}
			
			this.setScore(false);
			if(this.isStaleMate && !this.onCheck) {
				this.gameOver = true;
				labels[(isWhite ? wKing : bKing).y][(isWhite ? wKing : bKing).x].setBackground(this.checkColor);
				(isWhite ? wKing : bKing).setWonColor(pieces, labels);
				System.out.println("StaleMate! Match is Draw!");
				this.scoreSheet.appendRow(new Object[] {"         1/2", "         1/2"});
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showConfirmDialog(this, "           Match is Draw!", "Stalemate!", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);}
			return;}
		else if(pieces[y-1][x-1] == null) {
			Toolkit.getDefaultToolkit().beep();
			return;}
		else if(pieces[y-1][x-1].isWhite != isWhite) {
			Toolkit.getDefaultToolkit().beep();
			return;}
		
		this.isClicked = true;
		label.setBackground(this.clickColor);
		new Thread(this.runSetOrCheck).start();}
	
	@Override public void mousePressed(MouseEvent e) {}

    @Override public void mouseReleased(MouseEvent e) {}

    @Override public void mouseEntered(MouseEvent e) {
		label = (JLabel) e.getSource();
		y0 = labels[0][0].getLocation().y;	x0 = labels[0][0].getLocation().x;
		dy = labels[1][0].getLocation().y - y0; dx = labels[0][1].getLocation().x - x0;
		y = (label.getLocation().y - y0) / dy;	x = (label.getLocation().x - x0) / dx;
		mouseField.setText((pos = (col.charAt(x-1)+""+(9-y))).toUpperCase());}

    @Override public void mouseExited(MouseEvent e) {mouseField.setText("null");}
	
	/********************************************************************************/
	@Override public void actionPerformed(ActionEvent e) {
		if(gameMenuItems.indexOf(e.getSource()) == 0)
		    this.startNewGame();
		else if(gameMenuItems.indexOf(e.getSource()) == 1)
		    this.restartGame();
		else if(e.getSource() == about)
		    this.aboutGame();
		else if(e.getSource() instanceof JRadioButtonMenuItem)
			this.repaintBoard(((JRadioButtonMenuItem) e.getSource()).getText());
		else if(gameMenuItems.indexOf(e.getSource()) == 4)
		    this.quitGame();
		else if(this.gameOver)
		    return;
		else if(gameMenuItems.indexOf(e.getSource()) == 2)
			this.drawGame();
		else if(gameMenuItems.indexOf(e.getSource()) == 3)
		    this.resignGame();}
    
    /********************************************************************************/
	private void startNewGame() {
		Toolkit.getDefaultToolkit().beep();
		if(JOptionPane.showConfirmDialog(this, "Are you sure? Do you want to start a new game?", "New Game", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
			new ChessBoard();}
	
	private void restartGame() {
		Toolkit.getDefaultToolkit().beep();
		if(JOptionPane.showConfirmDialog(this, "Are you sure? Do you want to restart the game?", "Restart Game", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
		    ChessBoard newBoard = new ChessBoard();
		    newBoard.setLocationRelativeTo(this);
		    newBoard.repaintBoard(this.getThemeName());
		    this.dispose();
		  }}
	
    private void drawGame() {
    	Toolkit.getDefaultToolkit().beep();
    	if(JOptionPane.showConfirmDialog(this, "Are you both sure? Do you both accept to draw the game?", "Draw Game", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
    		this.gameOver = true;
			labels[wKing.y][wKing.x].setBackground(this.checkColor);
			labels[bKing.y][bKing.x].setBackground(this.checkColor);
			System.out.println("Match Draw!");
			this.scoreSheet.appendRow(new Object[] {"         1/2", "         1/2"});}}

    private void resignGame() {
    	Toolkit.getDefaultToolkit().beep();
    	if(JOptionPane.showConfirmDialog(this, "Are you sure? Do you want to resign the game?", "Resign Game", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
		    this.gameOver = this.onCheck = true;
			labels[(this.isWhite ? wKing : bKing).y][(this.isWhite ? wKing : bKing).x].setBackground(this.checkColor);
			System.out.println((this.isWhite ? "White" : "Black") + " resigns!");
			this.setScore(true);}}

	private void quitGame() {
		Toolkit.getDefaultToolkit().beep();
        if(JOptionPane.showConfirmDialog(this, "Are you Sure? Do you want to quit the game?", "Exit Game", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
			this.dispose();}
	
	private void aboutGame() {
		Desktop desktop = Desktop.getDesktop();
		if(desktop.isSupported(Desktop.Action.BROWSE)) {
			try {desktop.browse(new URI("https://en.wikipedia.org/wiki/Chess"));}
			catch (Exception ex) {ex.printStackTrace();}}}
}