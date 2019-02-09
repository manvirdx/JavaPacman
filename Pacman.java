import java.awt.EventQueue;
import javax.swing.JFrame;

public class Pacman extends JFrame {

	/***
	 * A constructor that initialises an instance of the GUI
	 */
    public Pacman() {
        init();
    }
    
    /***
     * A simple method that defines basic GUI operations
     * @author Manvir Ubhi
     * @version 1.0
     */
    private void init() {
        //Loads a board to display the game on
        add(new Board());
        
        setTitle("Pacman");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(380, 420);
        setLocationRelativeTo(null);
        setVisible(true);        
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            Pacman pacman = new Pacman();
            pacman.setVisible(true);
        });
    }
}