import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Board extends JPanel implements ActionListener {

	private Dimension boardDimensions;
	private final Font smallFont = new Font("Helvetica", Font.BOLD, 14);

	private Image ii;
	private final Color dotColor = new Color(192, 192, 0);
	private Color mazeColor;

	private boolean inGame = false;
	private boolean dying = false;

	private final int blockSize = 24;
	private final int n = 15;
	private final int boardSize = n * blockSize;
	private final int pacmanAnimationDelay = 2;
	private final int pacmanAnimations = 4;
	private final int ghostLimit = 12;
	private final int pacmanSpeed = 6;

	private int pacAnimCount = pacmanAnimationDelay;
	private int pacAnimDir = 1;
	private int position = 0;
	private int N_GHOSTS = 6;
	private int pacsLeft, score;
	private int[] dx, dy;
	private int[] ghost_x, ghost_y, ghost_dx, ghost_dy, ghostSpeed;

	private Image ghost;
	private Image pacman1, pacman2up, pacman2left, pacman2right, pacman2down;
	private Image pacman3up, pacman3down, pacman3left, pacman3right;
	private Image pacman4up, pacman4down, pacman4left, pacman4right;

	private int pacmanX, pacmanY, pacmand_x, pacmand_y;
	private int req_dx, req_dy, view_dx, view_dy;

	/***
	 * Manages directional delta changes
	 * @author  
	 */
	private final short levelData[] = {
			19, 26, 26, 26, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 22,
			21, 0, 0, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
			21, 0, 0, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
			21, 0, 0, 0, 17, 16, 16, 24, 16, 16, 16, 16, 16, 16, 20,
			17, 18, 18, 18, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 20,
			17, 16, 16, 16, 16, 16, 20, 0, 17, 16, 16, 16, 16, 24, 20,
			25, 16, 16, 16, 24, 24, 28, 0, 25, 24, 24, 16, 20, 0, 21,
			1, 17, 16, 20, 0, 0, 0, 0, 0, 0, 0, 17, 20, 0, 21,
			1, 17, 16, 16, 18, 18, 22, 0, 19, 18, 18, 16, 20, 0, 21,
			1, 17, 16, 16, 16, 16, 20, 0, 17, 16, 16, 16, 20, 0, 21,
			1, 17, 16, 16, 16, 16, 20, 0, 17, 16, 16, 16, 20, 0, 21,
			1, 17, 16, 16, 16, 16, 16, 18, 16, 16, 16, 16, 20, 0, 21,
			1, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0, 21,
			1, 25, 24, 24, 24, 24, 24, 24, 24, 24, 16, 16, 16, 18, 20,
			9, 8, 8, 8, 8, 8, 8, 8, 8, 8, 25, 24, 24, 24, 28
	};

	private final int validSpeeds[] = {1, 2, 3, 4, 6, 8};
	private final int maxSpeed = 6;

	private int currentSpeed = 3;
	private short[] screenData;
	private Timer timer;

	public Board() {

		loadImages();
		initVariables();
		initBoard();
	}

	private void initBoard() {

		addKeyListener(new keyCheck());

		setFocusable(true);

		setBackground(Color.black);
		setDoubleBuffered(true);        
	}

	private void initVariables() {

		screenData = new short[n * n];
		mazeColor = new Color(5, 100, 5);
		boardDimensions = new Dimension(500, 500);
		ghost_x = new int[ghostLimit];
		ghost_dx = new int[ghostLimit];
		ghost_y = new int[ghostLimit];
		ghost_dy = new int[ghostLimit];
		ghostSpeed = new int[ghostLimit];
		dx = new int[4];
		dy = new int[4];

		timer = new Timer(40, this);
		timer.start();
	}

	@Override
	public void addNotify() {
		super.addNotify();

		initGame();
	}

	private void doAnim() {

		pacAnimCount--;

		if (pacAnimCount <= 0) {
			pacAnimCount = pacmanAnimationDelay;
			position = position + pacAnimDir;

			if (position == (pacmanAnimations - 1) || position == 0) {
				pacAnimDir = -pacAnimDir;
			}
		}
	}

	private void playGame(Graphics2D g) {

		if (dying) {
			death();

		} else {

			movePacman();
			drawPacman(g);
			moveGhosts(g);
			checkMaze();
		}
	}

	private void showIntroScreen(Graphics2D g) {

		g.setColor(new Color(0, 32, 48));
		g.fillRect(50, boardSize / 2 - 30, boardSize - 100, 50);
		g.setColor(Color.white);
		g.drawRect(50, boardSize / 2 - 30, boardSize - 100, 50);

		String startString = "Press s to start.";
		Font small = new Font("Helvetica", Font.BOLD, 14);
		FontMetrics metr = this.getFontMetrics(small);

		g.setColor(Color.white);
		g.setFont(small);
		g.drawString(startString, (boardSize - metr.stringWidth(startString)) / 2, boardSize / 2);
	}

	private void drawScore(Graphics2D g) {

		int i;
		String s;

		g.setFont(smallFont);
		g.setColor(new Color(96, 128, 255));
		s = "Score: " + score;
		g.drawString(s, boardSize / 2 + 96, boardSize + 16);

		for (i = 0; i < pacsLeft; i++) {
			g.drawImage(pacman3left, i * 28 + 8, boardSize + 1, this);
		}
	}

	private void checkMaze() {

		short i = 0;
		boolean finished = true;

		while (i < n * n && finished) {

			if ((screenData[i] & 48) != 0) {
				finished = false;
			}

			i++;
		}

		if (finished) {

			score += 50;

			if (N_GHOSTS < ghostLimit) {
				N_GHOSTS++;
			}

			if (currentSpeed < maxSpeed) {
				currentSpeed++;
			}

			initLevel();
		}
	}

	private void death() {

		pacsLeft--;

		if (pacsLeft == 0) {
			inGame = false;
		}

		continueLevel();
	}

	private void moveGhosts(Graphics2D g) {

		short i;
		int pos;
		int count;

		for (i = 0; i < N_GHOSTS; i++) {
			if (ghost_x[i] % blockSize == 0 && ghost_y[i] % blockSize == 0) {
				pos = ghost_x[i] / blockSize + n * (int) (ghost_y[i] / blockSize);

				count = 0;

				if ((screenData[pos] & 1) == 0 && ghost_dx[i] != 1) {
					dx[count] = -1;
					dy[count] = 0;
					count++;
				}

				if ((screenData[pos] & 2) == 0 && ghost_dy[i] != 1) {
					dx[count] = 0;
					dy[count] = -1;
					count++;
				}

				if ((screenData[pos] & 4) == 0 && ghost_dx[i] != -1) {
					dx[count] = 1;
					dy[count] = 0;
					count++;
				}

				if ((screenData[pos] & 8) == 0 && ghost_dy[i] != -1) {
					dx[count] = 0;
					dy[count] = 1;
					count++;
				}

				if (count == 0) {

					if ((screenData[pos] & 15) == 15) {
						ghost_dx[i] = 0;
						ghost_dy[i] = 0;
					} else {
						ghost_dx[i] = -ghost_dx[i];
						ghost_dy[i] = -ghost_dy[i];
					}

				} else {

					count = (int) (Math.random() * count);

					if (count > 3) {
						count = 3;
					}

					ghost_dx[i] = dx[count];
					ghost_dy[i] = dy[count];
				}

			}

			ghost_x[i] = ghost_x[i] + (ghost_dx[i] * ghostSpeed[i]);
			ghost_y[i] = ghost_y[i] + (ghost_dy[i] * ghostSpeed[i]);
			drawGhost(g, ghost_x[i] + 1, ghost_y[i] + 1);

			if (pacmanX > (ghost_x[i] - 12) && pacmanX < (ghost_x[i] + 12)
					&& pacmanY > (ghost_y[i] - 12) && pacmanY < (ghost_y[i] + 12)
					&& inGame) {

				dying = true;
			}
		}
	}

	private void drawGhost(Graphics2D g, int x, int y) {

		g.drawImage(ghost, x, y, this);
	}

	/***
	 * A method that controls Pacman's movement on the board
	 */
	private void movePacman() {

		int pos;
		short ch;

		if (req_dx == -pacmand_x && req_dy == -pacmand_y) {
			pacmand_x = req_dx;
			pacmand_y = req_dy;
			view_dx = pacmand_x;
			view_dy = pacmand_y;
		}

		if (pacmanX % blockSize == 0 && pacmanY % blockSize == 0) {
			pos = pacmanX / blockSize + n * (int) (pacmanY / blockSize);
			ch = screenData[pos];

			if ((ch & 16) != 0) {
				screenData[pos] = (short) (ch & 15);
				score++;
			}

			if (req_dx != 0 || req_dy != 0) {
				if (!((req_dx == -1 && req_dy == 0 && (ch & 1) != 0)
						|| (req_dx == 1 && req_dy == 0 && (ch & 4) != 0)
						|| (req_dx == 0 && req_dy == -1 && (ch & 2) != 0)
						|| (req_dx == 0 && req_dy == 1 && (ch & 8) != 0))) {
					pacmand_x = req_dx;
					pacmand_y = req_dy;
					view_dx = pacmand_x;
					view_dy = pacmand_y;
				}
			}

			// Check for standstill
			if ((pacmand_x == -1 && pacmand_y == 0 && (ch & 1) != 0)
					|| (pacmand_x == 1 && pacmand_y == 0 && (ch & 4) != 0)
					|| (pacmand_x == 0 && pacmand_y == -1 && (ch & 2) != 0)
					|| (pacmand_x == 0 && pacmand_y == 1 && (ch & 8) != 0)) {
				pacmand_x = 0;
				pacmand_y = 0;
			}
		}
		pacmanX = pacmanX + pacmanSpeed * pacmand_x;
		pacmanY = pacmanY + pacmanSpeed * pacmand_y;
	}

	/***
	 * A method that draws the Pacman onto the screen 
	 * @param g
	 */
	private void drawPacman(Graphics2D g) {
		if (view_dx == -1) {
			left(g);
		} 
		else if (view_dx == 1) {
			right(g);
		} 
		else if (view_dy == -1) {
			up(g);
		} 
		else {
			down(g);
		}
	}

	/***
	 * A method that manages the animation of Pacman when he is moving up
	 * @param g an instance of Graphics2D that renders the Pacman's up movements
	 */
	private void up(Graphics2D g) {

		switch (position) {
		case 1:
			g.drawImage(pacman2up, pacmanX + 1, pacmanY + 1, this);
			break;
		case 2:
			g.drawImage(pacman3up, pacmanX + 1, pacmanY + 1, this);
			break;
		case 3:
			g.drawImage(pacman4up, pacmanX + 1, pacmanY + 1, this);
			break;
		default:
			g.drawImage(pacman1, pacmanX + 1, pacmanY + 1, this);
			break;
		}
	}

	/***
	 * A method that manages the animation of Pacman when he is moving down
	 * @param g an instance of Graphics2D that renders the Pacman's down movements
	 */
	private void down(Graphics2D g) {

		switch (position) {
		case 1:
			g.drawImage(pacman2down, pacmanX + 1, pacmanY + 1, this);
			break;
		case 2:
			g.drawImage(pacman3down, pacmanX + 1, pacmanY + 1, this);
			break;
		case 3:
			g.drawImage(pacman4down, pacmanX + 1, pacmanY + 1, this);
			break;
		default:
			g.drawImage(pacman1, pacmanX + 1, pacmanY + 1, this);
			break;
		}
	}

	/***
	 * A method that manages the animation of Pacman when he is moving left
	 * @param g an instance of Graphics2D that renders the Pacman's left movements
	 */
	private void left(Graphics2D g) {
		switch (position) {
		case 1:
			g.drawImage(pacman2left, pacmanX + 1, pacmanY + 1, this);
			break;
		case 2:
			g.drawImage(pacman3left, pacmanX + 1, pacmanY + 1, this);
			break;
		case 3:
			g.drawImage(pacman4left, pacmanX + 1, pacmanY + 1, this);
			break;
		default:
			g.drawImage(pacman1, pacmanX + 1, pacmanY + 1, this);
			break;
		}
	}

	/***
	 * A method that manages the animation of Pacman when he is moving right
	 * @param g an instance of Graphics2D that renders the Pacman's right movements
	 */
	private void right(Graphics2D g) {

		switch (position) {
		case 1:
			g.drawImage(pacman2right, pacmanX + 1, pacmanY + 1, this);
			break;
		case 2:
			g.drawImage(pacman3right, pacmanX + 1, pacmanY + 1, this);
			break;
		case 3:
			g.drawImage(pacman4right, pacmanX + 1, pacmanY + 1, this);
			break;
		default:
			g.drawImage(pacman1, pacmanX + 1, pacmanY + 1, this);
			break;
		}
	}

	/***
	 * A method 
	 * @param g an instance of Graphics2D to allow rendering of the shapes on the board 
	 */
	private void drawBoard(Graphics2D g) {

		short i = 0;
		int x, y;

		for (y = 0; y < boardSize; y += blockSize) {
			for (x = 0; x < boardSize; x += blockSize) {

				g.setColor(mazeColor);
				g.setStroke(new BasicStroke(2));

				if ((screenData[i] & 1) != 0) { 
					g.drawLine(x, y, x, y + blockSize - 1);
				}

				if ((screenData[i] & 2) != 0) { 
					g.drawLine(x, y, x + blockSize - 1, y);
				}

				if ((screenData[i] & 4) != 0) { 
					g.drawLine(x + blockSize - 1, y, x + blockSize - 1,
							y + blockSize - 1);
				}

				if ((screenData[i] & 8) != 0) { 
					g.drawLine(x, y + blockSize - 1, x + blockSize - 1,
							y + blockSize - 1);
				}

				if ((screenData[i] & 16) != 0) { 
					g.setColor(dotColor);
					g.fillRect(x + 11, y + 11, 2, 2);
				}

				i++;
			}
		}
	}

	public void initGame() {

		pacsLeft = 3;
		score = 0;
		initLevel();
		N_GHOSTS = 6;
		currentSpeed = 3;
	}

	private void initLevel() {

		int i;
		for (i = 0; i < n * n; i++) {
			screenData[i] = levelData[i];
		}

		continueLevel();
	}

	private void continueLevel() {

		short i;
		int dx = 1;
		int random;

		for (i = 0; i < N_GHOSTS; i++) {

			ghost_y[i] = 4 * blockSize;
			ghost_x[i] = 4 * blockSize;
			ghost_dy[i] = 0;
			ghost_dx[i] = dx;
			dx = -dx;
			random = (int) (Math.random() * (currentSpeed + 1));

			if (random > currentSpeed) {
				random = currentSpeed;
			}

			ghostSpeed[i] = validSpeeds[random];
		}

		pacmanX = 7 * blockSize;
		pacmanY = 11 * blockSize;
		pacmand_x = 0;
		pacmand_y = 0;
		req_dx = 0;
		req_dy = 0;
		view_dx = -1;
		view_dy = 0;
		dying = false;
	}

	private void loadImages() {

		ghost = new ImageIcon("ghost.png").getImage();
		pacman1 = new ImageIcon("pacman.png").getImage();
		pacman2up = new ImageIcon("up2.png").getImage();
		pacman3up = new ImageIcon("up3.png").getImage();
		pacman4up = new ImageIcon("up4.png").getImage();
		pacman2down = new ImageIcon("down2.png").getImage();
		pacman3down = new ImageIcon("down3.png").getImage();
		pacman4down = new ImageIcon("down4.png").getImage();
		pacman2left = new ImageIcon("left2.png").getImage();
		pacman3left = new ImageIcon("left3.png").getImage();
		pacman4left = new ImageIcon("left4.png").getImage();
		pacman2right = new ImageIcon("right2.png").getImage();
		pacman3right = new ImageIcon("right3.png").getImage();
		pacman4right = new ImageIcon("right4.png").getImage();

	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		doDrawing(g);
	}

	private void doDrawing(Graphics g2d) {

		Graphics2D g = (Graphics2D) g2d;

		g.setColor(Color.black);
		g.fillRect(0, 0, boardDimensions.width, boardDimensions.height);

		drawBoard(g);
		drawScore(g);
		doAnim();

		if (inGame) {
			playGame(g);
		} else {
			showIntroScreen(g);
		}

		g.drawImage(ii, 5, 5, this);
		Toolkit.getDefaultToolkit().sync();
		g.dispose();
	}

	
	@Override
	public void actionPerformed(ActionEvent e) {
		repaint();
	}
	
	/***
	 * A separate class that manages key presses such as directions and play/pause
	 */
	class keyCheck extends KeyAdapter {
	
	public void keyPressed(KeyEvent e) {

		int key = e.getKeyCode();

		//If the user is in game, then only the following 6 keys will trigger a keyEvent
		//Otherwise, it would be in the start menu and the only event that can happen is to start the game
		if (inGame) {
			if (key == KeyEvent.VK_LEFT) {
				req_dx = -1;
				req_dy = 0;
			} 
			else if (key == KeyEvent.VK_RIGHT) {
				req_dx = 1;
				req_dy = 0;
			} 
			else if (key == KeyEvent.VK_UP) {
				req_dx = 0;
				req_dy = -1;
			} 
			else if (key == KeyEvent.VK_DOWN) {
				req_dx = 0;
				req_dy = 1;
			} 
			else if (key == KeyEvent.VK_ESCAPE && timer.isRunning()) {
				inGame = false;
			} 
			else if (key == KeyEvent.VK_PAUSE) {
				if (timer.isRunning()) {
					timer.stop();
				} 
				else {
					timer.start();
				}
			}
		} 
		else {
			if (key == 's' || key == 'S') {
				inGame = true;
				new Board().initGame();
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {

		int key = e.getKeyCode();

		if (key == Event.LEFT || key == Event.RIGHT
				|| key == Event.UP || key == Event.DOWN) {
			req_dx = 0;
			req_dy = 0;
		}
	}
}
}
