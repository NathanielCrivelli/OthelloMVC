package mvc.tictactoe;

import java.lang.reflect.Array;
import java.text.BreakIterator;
import java.util.ArrayList;

import javax.swing.text.Position;

import com.mrjaffesclass.apcs.messenger.*;

/**
 * The model represents the data that the app uses.
 *
 * @author Roger Jaffe
 * @version 1.0
 */
public class Model implements MessageHandler {

    
    // Messaging system for the MVC
    private final Messenger mvcMessaging;


    // Model's data variables
    private boolean whoseMove;
    private boolean gameOver;
    private String[][] board;


    /**
     * Model constructor: Create the data representation of the program
     *
     * @param messages Messaging class instantiated by the Controller for local
     * messages between Model, View, and controller
     */
    public Model(Messenger messages) {
        mvcMessaging = messages;
    }


    /**
     * Initialize the model here and subscribe to any required messages
     */
    public void init() {
        this.board = new String[8][8];
        this.board[3][3] = "O";
        this.board[3][4] = "X";
        this.board[4][3] = "X";
        this.board[4][4] = "O";
        this.newGame();
        this.mvcMessaging.subscribe("playerMove", this);
        this.mvcMessaging.subscribe("newGame", this);
        this.mvcMessaging.subscribe("gameOver", this);
        this.mvcMessaging.subscribe("Tie", this);
        this.whoseMove = false;
        this.gameOver = false;
    }


    /**
     * Reset the state for a new game
     */
    private void newGame() {
        for (int row = 0; row < this.board.length; row++) {
            for (int col = 0; col < this.board[0].length; col++) {
                this.board[row][col] = "";
            }
        }
        this.board[3][3] = "O";
        this.board[3][4] = "X";
        this.board[4][3] = "X";
        this.board[4][4] = "O";

        this.whoseMove = false;
        this.gameOver = false;
    }


    public boolean isLegalMove(int[] pos) {
        if (this.board[pos[0]][pos[1]].equals("")) {
            for(int[] direction : Directions.points) {
                int[] newPos = pos;
                vector(direction, newPos);

                if (isOffBoard(newPos) == true || getSquare(newPos) == 1 || getSquare(newPos) == 0) {
                    continue;
                }

                while (isOffBoard(newPos) == false && getSquare(newPos) == -1 && getSquare(newPos) != 0) {
                    vector(direction, newPos);
                    if (getSquare(pos) == getSquare(newPos)) {
                        System.out.println("Legal Move: " + pos[0] + " " + pos[1]);
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    

    // public ArrayList<int[]> setLegalMoves() {
    //     ArrayList<int[]> moves = new ArrayList<int[]>();
    //     for(int i = 0; i < this.board.length; i++) {
    //         for(int j = 0; j < this.board[i].length; j++) {
    //             int[] position = {i, j};
    //             if (isLegalMove(position)) {
    //                 moves.add(position);
    //                 System.out.println(position[0] + " " + position[1]);
    //             }
    //         }
    //     }
        
    //     return moves;
    // }


    public ArrayList<int[]> testLegalMoves() {
        ArrayList<int[]> moves = new ArrayList<int[]>();
        int[] position = {0,0};
        int[] position2 = {0,1};
        moves.add(position);
        moves.add(position2);
        return moves;
    }


    private boolean step(int[] position, int[] direction, int count, boolean whoseMove) {
        vector(direction, position);
        if (isOffBoard(position)) {
            return false;
        } else if (this.board[position[0]][position[1]].equals(!whoseMove)) {
            flip(position);
            return this.step(position, direction, count + 1, whoseMove);
        } else if (this.board[position[0]][position[1]].equals(whoseMove)) {
            return count > 0;
        } else {
            return false;
        }
    }


    private void flip(int[] pos) {
        if(this.board[pos[0]][pos[1]].equals("O")) {
            this.board[pos[0]][pos[1]] = "X";
        } else {
            this.board[pos[0]][pos[1]] = "O";
        }
    }


    // pos is the position, just like in vector
    public boolean isOffBoard(int[] pos) {
        return (pos[0] < 0 || pos[0] >= this.board.length || pos[1] < 0 || pos[1] >= this.board.length);
    }


    // vector[] is directions, pos is current position
    public void vector(int[] vector, int[] pos) {
        pos[0] += vector[0];
        pos[1] += vector[1];
    }


    public int getSquare(int[] pos) {
        if(this.board[pos[0]][pos[1]].equals("O") && whoseMove == false) {
            return 1;
        } else if (this.board[pos[0]][pos[1]].equals("O") && whoseMove == true) {
            return -1;
        } else if (this.board[pos[0]][pos[1]].equals("X") && whoseMove == false) {
            return -1;
        } else if (this.board[pos[0]][pos[1]].equals("X") && whoseMove == true) {
            return 1;
        } else if (this.board[pos[0]][pos[1]].equals("")) {
            return 0;
        }
        return 0;
    }


    @Override
    public void messageHandler(String messageName, Object messagePayload) {
        // Display the message to the console for debugging
        if (messagePayload != null) {
            System.out.println("MSG: received by model: " + messageName + " | " + messagePayload.toString());
        } else {
            System.out.println("MSG: received by model: " + messageName + " | No data sent");
        }

        // playerMove message handler
        if (messageName.equals("playerMove") && this.gameOver == false) {
            // Check the rows and columns for a tic tac toe

            // Get the position string and convert to row and col
            String position = (String) messagePayload;
            Integer row = new Integer(position.substring(0, 1));
            Integer col = new Integer(position.substring(1, 2));
            // If square is blank...
            int[] pos = new int[2];
            pos[0] = row;
            pos[1] = col;
            // ArrayList<int[]> moves = new ArrayList<int[]>();
            // moves = setLegalMoves();
            // for(int i = 0; i < moves.size(); i++){
            if (this.board[row][col].equals("") && isLegalMove(pos) == true) {
                // ... then set X or O depending on whose move it is
                if (this.whoseMove) {
                    this.board[row][col] = "X";
                    this.mvcMessaging.notify("boardChange", this.board);
                    this.whoseMove = !this.whoseMove;
                } else {
                    this.board[row][col] = "O";
                    this.mvcMessaging.notify("boardChange", this.board);
                    this.whoseMove = !this.whoseMove;
                }
            }
        // }
                // Send the boardChange message along with the new board 
            }
            if (!board[0][0].equals("") && !board[0][1].equals("") && !board[0][2].equals("") && !board[1][0].equals("") && !board[1][1].equals("")
                    && !board[1][2].equals("") && !board[2][0].equals("") && !board[2][1].equals("") && !board[2][2].equals("")) {
                this.mvcMessaging.notify("Tie");
                gameOver = true;
            }

            // newGame message handler
         else if (messageName.equals("newGame")) {
            // Reset the app state
            this.newGame();
            // Send the boardChange message along with the new board 
            this.mvcMessaging.notify("boardChange", this.board);
        }

    }
}
