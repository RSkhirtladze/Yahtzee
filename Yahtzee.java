
/*
 * File: Yahtzee.java
 * ------------------
 * This program will eventually play the Yahtzee game.
 */

import java.util.Arrays;

import acm.io.*;
import acm.program.*;
import acm.util.*;

public class Yahtzee extends GraphicsProgram implements YahtzeeConstants {

	public static void main(String[] args) {
		new Yahtzee().start(args);
	}

	public void run() {
		IODialog dialog = getDialog();
		nPlayers = 0;
		while (nPlayers > MAX_PLAYERS || nPlayers < 1) {
			nPlayers = dialog.readInt("Enter number of players");
		}
		playerNames = new String[nPlayers];
		for (int i = 1; i <= nPlayers; i++) {
			playerNames[i - 1] = dialog.readLine("Enter name for player " + i);
		}
		display = new YahtzeeDisplay(getGCanvas(), playerNames);

		selected_categories = new boolean[nPlayers + 1][N_CATEGORIES + 1];

		playGame();

	}

	private void playGame() {

		for (int plays = 0; plays < nPlayers * N_SCORING_CATEGORIES; plays++) {

			playRound();
			player = player % nPlayers + 1;
		}

		endGame();
	}

	private void playRound() {
		rollDices();
		addScoreOnMap(getCategory() + 1);
	}

	/** This method rolls dices */
	private void rollDices() {
		firstTry();
		otherTries();
	}

	/** firstTry method rolls them for the first time */
	private void firstTry() {
		display.waitForPlayerToClickRoll(player);
		for (int i = 0; i <= N_DICE - 1; i++) {
			dice[i] = rgen.nextInt(1, 6);
		}
		display.displayDice(dice);
	}

	/** This method allows player to change selected dices */
	private void otherTries() {
		for (int z = 0; z < 2; z++) {
			display.printMessage("Select the dice you wish to re-roll and roll again");
			display.waitForPlayerToSelectDice();

			for (int i = 0; i < N_DICE; i++) {
				if (display.isDieSelected(i) == true)
					dice[i] = rgen.nextInt(1, 6);
			}

			display.displayDice(dice);

		}
	}

	/** Method returns which category was selected.
	 *  It asks to click on category until not selected one is clicked*/
	private int getCategory() {
		while (true) {
			display.printMessage("Select a category.");
			int category = display.waitForPlayerToSelectCategory() - 1;
			if (!selected_categories[player][category]) {
				selected_categories[player][category] = true;
				return category;
			}
		}
	}
	
	/** This one calculates score which depends on chosen category.
	 * 	If category is different from 1,2,...6-es this method sorts dices, so that if in dice[] array
	 * 	we have for example {1,4,2,6,4} it will return array of {1,2,4,4,6}. Because of this sort it 
	 * 	makes really easy to check if given array matches given category.*/
	private void addScoreOnMap(int type) {
		int score = 0;
		if (type <= SIXES) {
			score = numbersCase(type);
		} else {
			Arrays.sort(dice, 0, 5);
			if (type == FULL_HOUSE) {
				if (dice[0] == dice[1] && dice[3] == dice[4] && (dice[2] == dice[1] || dice[2] == dice[3])
						&& dice[0] != dice[4]) {
					score = 25;
				}
			} else if (type == THREE_OF_A_KIND) {
				if (dice[0] == dice[2] || dice[1] == dice[3] || dice[2] == dice[4]) {
					score = sumAllDices();
				}
			} else if (type == FOUR_OF_A_KIND) {
				if (dice[0] == dice[3] || dice[1] == dice[4]) {
					score = sumAllDices();
				}
			} else if (type == YAHTZEE) {
				if (dice[0] == dice[4]) {
					score = 50;
				}
			} else if (type == SMALL_STRAIGHT) {
				if (dice[1] - dice[0] == 1 && dice[2] - dice[1] == 1 && dice[3] - dice[2] == 1
						|| dice[4] - dice[3] == 1 && dice[3] - dice[2] == 1 && dice[2] - dice[1] == 1) {
					score = 30;
				}
			} else if (type == LARGE_STRAIGHT) {
				if (dice[1] - dice[0] == 1 && dice[2] - dice[1] == 1 && dice[3] - dice[2] == 1
						&& dice[4] - dice[3] == 1) {
					score = 40;
				}
			} else if (type == CHANCE) {
				score = sumAllDices();
			}
		}
		
		addOnScreen(score, type);
		
	}
	
	/** This method updates scores and prints them on board. */
	private void addOnScreen(int score, int type) {
		if (type <= SIXES) {
			points[player][0] += score;
		} else
			points[player][1] += score;
		points[player][2] += score;
		if (points[player][0] >= 63 && points[player][3] == 0) {
			points[player][2] += score;
			points[player][3] = 35;
		}
		display.updateScorecard(type, player, score);
		display.updateScorecard(UPPER_SCORE, player, points[player][0]);
		display.updateScorecard(LOWER_SCORE, player, points[player][1]);
		display.updateScorecard(UPPER_BONUS, player, points[player][3]);
		display.updateScorecard(TOTAL, player, points[player][2]);

	}
	
	/** As we use it in number of cases we have method which calculates sum of all dices*/
	private int sumAllDices() {
		int answer = 0;
		for (int i = 0; i < N_DICE; i++) {
			answer += dice[i];
		}
		return answer;
	}

	/** If player chooses upper category this method is used to calculate score. 
	 * 	It counts how many needed dices we have and sums them up.*/
	private int numbersCase(int n) {
		int result = 0;
		for (int i = 0; i < N_DICE; i++) {
			if (dice[i] == n) {
				result += n;
			}
		}
		return result;
	}

	/** This method checks who's the winner, and prints him on the screen.*/
	private void endGame() {

		int winnerPoints = 0, l = 0;
		for (int i = 1; i <= nPlayers; i++) {
			if (winnerPoints < points[i][2])
				winnerPoints = points[i][2];
		}

		String s = "";
		for (int i = 1; i <= nPlayers; i++) {
			if (winnerPoints == points[i][2]) {
				l++;
				if (l > 1)
					s += ',';
				s += playerNames[i - 1];
			}
		}

		if (l == 1)
			display.printMessage(s + " is the winner with a total score of " + winnerPoints);
		else
			display.printMessage(s + " are the winners with a total score of " + winnerPoints);

	}

	/* Private instance variables */

	private int nPlayers;
	private String[] playerNames;
	private YahtzeeDisplay display;
	private RandomGenerator rgen = new RandomGenerator();

	private int player = 1;
	private int[] dice = new int[N_DICE];
	private int[][] points = new int[5][5];
	private boolean[][] selected_categories;
}
