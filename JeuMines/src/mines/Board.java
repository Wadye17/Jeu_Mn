package mines;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.security.SecureRandom;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * La classe Board représente le plateau de jeu pour le jeu de démineur.
 */
public class Board extends JPanel {
	private static final long serialVersionUID = 6195235521361212179L;

	private final int NUM_IMAGES = 13;
	private final int CELL_SIZE = 15;

	private final int COVER_FOR_CELL = 10;
	private final int MARK_FOR_CELL = 10;
	private final int EMPTY_CELL = 0;
	private final int MINE_CELL = 9;
	private final int COVERED_MINE_CELL = MINE_CELL + COVER_FOR_CELL;
	private final int MARKED_MINE_CELL = COVERED_MINE_CELL + MARK_FOR_CELL;

	private final int ROWS = 16;
	private final int COLUMNS = 16;
	private final int ALL_CELLS = ROWS * COLUMNS;

	private final int DRAW_MINE = 9;
	private final int DRAW_COVER = 10;
	private final int DRAW_MARK = 11;
	private final int DRAW_WRONG_MARK = 12;
	
	private final SecureRandom random = new SecureRandom(); 

	private int[] field;
	private boolean inGame;
	private int minesLeft;
	private Image[] images;
	private int mines = 40;
	private JLabel statusbar;


	/**
	 * Constructeur de la classe Board.
	 * Initialise le plateau de jeu et charge les images nécessaires.
	 * @param statusbar une étiquette pour afficher le statut du jeu
	 * @return none
	 */
	public Board(JLabel statusbar) {
    initializeStatusBar(statusbar);
    loadGameImages();
    enableDoubleBuffering();
    addMouseListenerForUserInput();
    newGame();
}

private void initializeStatusBar(JLabel statusbar) {
    this.statusbar = statusbar;
}

private void loadGameImages() {
    images = new Image[NUM_IMAGES];
    for (int i = 0; i < NUM_IMAGES; i++) {
        images[i] = (new ImageIcon(getClass().getClassLoader().getResource((i) + ".gif"))).getImage();
    }
}

private void enableDoubleBuffering() {
    setDoubleBuffered(true);
}

private void addMouseListenerForUserInput() {
    addMouseListener(new MinesAdapter());
}


/**
	 * Initialise une nouvelle partie de démineur.
	 * Génère un nouveau plateau de jeu avec des mines aléatoires.
	 * @param none
	 * @return none
	 */

	public void newGame() {
    initializeVariablesForNewGame();
    initializeGameBoard();
    placeMinesRandomly();
}

private void initializeVariablesForNewGame() {
    inGame = true;
    minesLeft = mines;
    field = new int[ALL_CELLS];
}

private void initializeGameBoard() {
    for (int i = 0; i < ALL_CELLS; i++) {
        field[i] = COVER_FOR_CELL;
    }
    statusbar.setText(Integer.toString(minesLeft));
}

private void placeMinesRandomly() {
    int i = 0;
    while (i < mines) {
        int position = (int) (ALL_CELLS * random.nextDouble());
        if (isValidMinePosition(position)) {
            int currentColumn = position % COLUMNS;
            placeMineAndUpdateAdjacentCells(position, currentColumn);
            i++;
        }
    }
}

private boolean isValidMinePosition(int position) {
    return (position < ALL_CELLS) && (field[position] != COVERED_MINE_CELL);
}

private void placeMineAndUpdateAdjacentCells(int position, int currentColumn) {
    field[position] = COVERED_MINE_CELL;
    updateAdjacentCells(position, currentColumn);
}

private void updateAdjacentCells(int position, int currentColumn) {
    int[] neighborOffsets = {-1, 0, 1};

    for (int rowOffset : neighborOffsets) {
        for (int colOffset : neighborOffsets) {
            if (rowOffset == 0 && colOffset == 0) {
                continue;
            }

            int cell = position + (rowOffset * COLUMNS) + colOffset;
            if (isValidCell(cell, currentColumn + colOffset)) {
                incrementAdjacentMines(cell);
            }
        }
    }
}

private boolean isValidCell(int cell, int newColumn) {
    return cell >= 0 && cell < ALL_CELLS && newColumn >= 0 && newColumn < COLUMNS;
}

private void incrementAdjacentMines(int cell) {
    if (field[cell] != COVERED_MINE_CELL) {
        field[cell] += 1;
    }
}


	/**
	 * Recherche toutes les cellules vides adjacentes à la cellule donnée et les révèle.
	 * Utilise la récursivité pour parcourir toutes les cellules vides adjacentes.
	 * @param j l'indice de la cellule à partir de laquelle la recherche doit commencer
	 * @return none
	 */
	public void findEmptyCells(int j) {
    int currentColumn = j % COLUMNS;
    int cell;

    // Parcourt toutes les cellules adjacentes à la cellule donnée
    if (currentColumn > 0) {
        checkAdjacentCells(j, -COLUMNS - 1, -1, COLUMNS - 1);
    }

    checkAdjacentCells(j, -COLUMNS, COLUMNS);

    if (currentColumn < (COLUMNS - 1)) {
        checkAdjacentCells(j, -COLUMNS + 1, 1, COLUMNS + 1);
    }
}

private void checkAdjacentCells(int j, int... offsets) {
    for (int offset : offsets) {
        int cell = j + offset;
        if (offset < 0) {
            iterateEmptyCell(cell);
        } else {
            iterateCell(cell);
        }
    }
}


	private void iterateEmptyCell(int cell) {
		if (cell >= 0 && field[cell] > MINE_CELL) {
			uncoverCell(cell);
			if (field[cell] == EMPTY_CELL) {
				findEmptyCells(cell);
			}
		}
	}

	private void iterateCell(int cell) {
		if (cell < ALL_CELLS && field[cell] > MINE_CELL) {
			uncoverCell(cell);
			if (field[cell] == EMPTY_CELL) {
				findEmptyCells(cell);
			}
		}
	}

	private void uncoverCell(int cell) {
		field[cell] -= COVER_FOR_CELL;
	}


	/**
	 * Dessine le champ de jeu en utilisant les images appropriées pour chaque cellule.
	 * @param g l'objet Graphics utilisé pour dessiner le champ de jeu
	 * @return none
	 */
	public void paint(Graphics g) {
    int cell;
    int uncover = 0;

    for (int i = 0; i < ROWS; i++) {
        for (int j = 0; j < COLUMNS; j++) {
            cell = field[(i * COLUMNS) + j];

            if (inGame && cell == MINE_CELL) {
                inGame = false;
            }

            cell = determineCellImage(cell);

            if (inGame && cell == DRAW_COVER) {
                uncover++;
            }

            drawCellImage(g, cell, i, j);
        }
    }

    updateStatusBar(uncover);
}

private int determineCellImage(int cell) {
    if (!inGame) {
        if (cell == COVERED_MINE_CELL) {
            return DRAW_MINE;
        } else if (cell == MARKED_MINE_CELL) {
            return DRAW_MARK;
        } else if (cell > COVERED_MINE_CELL) {
            return DRAW_WRONG_MARK;
        } else if (cell > MINE_CELL) {
            return DRAW_COVER;
        }
    } else {
        if (cell > COVERED_MINE_CELL) {
            return DRAW_MARK;
        } else if (cell > MINE_CELL) {
            return DRAW_COVER;
        }
    }
    return cell;
}

private void drawCellImage(Graphics g, int cell, int row, int column) {
    g.drawImage(images[cell], (column * CELL_SIZE), (row * CELL_SIZE), this);
}

	// Vérifie si le jeu est terminé et met à jour la barre de statut en conséquence
	private void updateStatusBar(int uncover) {

		if (uncover == 0 && inGame) {
			inGame = false;
			statusbar.setText("Game won");
		} else if (!inGame)
			statusbar.setText("Game lost");
	}



	/**
	 * Classe qui gère les événements de souris pour le jeu de démineur.
	 */
	class MinesAdapter extends MouseAdapter {

		/**
 * Gère l'événement de clic de souris.
 * @param e l'objet MouseEvent qui contient les informations sur l'événement de clic de souris
 * @return none
 */
public void mousePressed(MouseEvent e) {
    int x = e.getX();
    int y = e.getY();

    int cellRow = x / CELL_SIZE;
    int cellColumn = y / CELL_SIZE;

    boolean mustRepaint = false;

    if (!inGame) {
        newGame();
        repaint();
    }

    if (isClickWithinGameBounds(x, y)) {
        if (isRightMouseButtonClicked(e)) {
            mustRepaint = handleRightClick(cellRow, cellColumn);
        } else {
            mustRepaint = handleLeftClick(cellRow, cellColumn);
        }

        if (mustRepaint) {
            repaint();
        }
    }
}

private boolean isClickWithinGameBounds(int x, int y) {
    return (x < COLUMNS * CELL_SIZE) && (y < ROWS * CELL_SIZE);
}

private boolean isRightMouseButtonClicked(MouseEvent e) {
    return e.getButton() == MouseEvent.BUTTON3;
}

private boolean handleRightClick(int cellRow, int cellColumn) {
    int cellIndex = (cellColumn * COLUMNS) + cellRow;
    if (field[cellIndex] > MINE_CELL) {
        if (field[cellIndex] <= COVERED_MINE_CELL) {
            if (minesLeft > 0) {
                field[cellIndex] += MARK_FOR_CELL;
                minesLeft--;
                statusbar.setText(Integer.toString(minesLeft));
            } else {
                statusbar.setText("No marks left");
            }
        } else {
            field[cellIndex] -= MARK_FOR_CELL;
            minesLeft++;
            statusbar.setText(Integer.toString(minesLeft));
        }
        return true;
    }
    return false;
}

private boolean handleLeftClick(int cellRow, int cellColumn) {
    int cellIndex = (cellColumn * COLUMNS) + cellRow;
    if (field[cellIndex] > COVERED_MINE_CELL) {
        return false;
    }

    if ((field[cellIndex] > MINE_CELL) && (field[cellIndex] < MARKED_MINE_CELL)) {
        field[cellIndex] -= COVER_FOR_CELL;

        if (field[cellIndex] == MINE_CELL) {
            inGame = false;
        }
        if (field[cellIndex] == EMPTY_CELL) {
            findEmptyCells(cellIndex);
        }
        return true;
    }
    return false;
}

	}

}
