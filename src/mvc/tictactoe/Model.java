

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
        this.board[4][3] = "O";
        this.board[4][4] = "X";
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
            int[] newPos = new int[2];
            newPos[0] = pos[0];
            newPos[1] = pos[1];
            int count = 0;

            if (isOffBoard(newPos) == false) {
                return false;
            }

            for(int[] direction : Directions.points) {
                newPos[0] = pos[0];
                newPos[1] = pos[1];
                vector(direction, newPos);
                while (isOffBoard(newPos) == true && getSquare(newPos) == -1) {
                    vector(direction, newPos);
                    if (isOffBoard(newPos) == false) {
                        break;
                    } else if (count > 2) {
                        count++;
                        continue;
                    }  else if (getSquare(newPos) == 1 && isOffBoard(newPos) == true) {
                        System.out.println("Legal Move: " + pos[0] + " " + pos[1]);
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    
    public void score(int[] pos) {
        if(!this.board[pos[0]][pos[1]].equals("")){
            int[] newPos = new int[2];
            newPos[0] = pos[0];
            newPos[1] = pos[1];

            for(int[] direction : Directions.points) {
                newPos[0] = pos[0];
                newPos[1] = pos[1];
                vector(direction, newPos);
                while (isOffBoard(newPos) == true && getSquare(newPos) == -1) {
                    vector(direction, newPos);
                }

                if (isOffBoard(newPos) == true && getSquare(newPos) != 0) {
                    updateSquares(newPos, pos, direction);
                }
            }
        }
    }


    public void updateSquares(int[] end, int[] start, int[] direction) {
        vector(direction, start);
        while(start[0] != end[0] || start[1] != end[1]) {
            flip(start);
            vector(direction, start);
        }
    }

    private void flip(int[] pos) {
        if(this.board[pos[0]][pos[1]].equals("O")) {
            this.board[pos[0]][pos[1]] = "X";
        } else if (this.board[pos[0]][pos[1]].equals("X")){
            this.board[pos[0]][pos[1]] = "O";
        }
    }


    // pos is the position, just like in vector
    public boolean isOffBoard(int[] pos) {
        return (pos[0] >= 0 && pos[0] < 8) && (pos[1] >= 0 && pos[1] < 8);
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

            // Get the position string and convert to row and col
            String position = (String) messagePayload;
            Integer row = new Integer(position.substring(0, 1));
            Integer col = new Integer(position.substring(1, 2));

            int[] pos = new int[2];
            pos[0] = row;
            pos[1] = col;


            if (this.board[row][col].equals("") && isLegalMove(pos) == true) {
                // ... then set X or O depending on whose move it is
                if (this.whoseMove) {
                    this.board[row][col] = "X";
                    score(pos);
                    this.mvcMessaging.notify("boardChange", this.board);
                    this.whoseMove = !this.whoseMove;
                } else {
                    this.board[row][col] = "O";
                    score(pos);
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
