import java.util.ArrayList;

public class dhiggins_smortBoiPlayer implements PokerSquaresPlayer {

	private final int SIZE = 5;
	private Card[][] grid = new Card[SIZE][SIZE];
	private int[] columnValue = { 0, 0, 0, 0 };

	@Override
	public void setPointSystem(PokerSquaresPointSystem system, long millis) {
		
	}

	@Override
	public void init() {
		for (int row = 0; row < SIZE; row++) {
			for (int col = 0; col < SIZE; col++) {
				grid[row][col] = null;
			}		
		}
	}

	@Override
	public int[] getPlay(Card card, long millisRemaining) {
		
		int cardRank = card.getRank();

		
		/* These four if statements setup the horseshoe structure that we need in order to get straights in the outer regions */
		
		
		//5 = Top Left
		if (grid[0][0] == null && cardRank == 4) {
			grid[0][0] = card;
			int[] playPos = { 0, 0 };
			return playPos;
		}
		//9 = Top Right
		else if (grid[0][4] == null && cardRank == 8) {
			grid[0][4] = card;
			int[] playPos = { 0, 4 };
			return playPos;
		}
		//Ace = Bottom Left
		else if (grid[4][0] == null && cardRank == 0) {
			grid[4][0] = card;
			int[] playPos = { 4, 0 };
			return playPos;
		}
		//King = Bottom Right
		else if (grid[4][4] == null && cardRank == 12) {
			grid[4][4] = card;
			int[] playPos = { 4, 4 };
			return playPos;
		}
		

		/* Next try to work on the outer straights */
		

		//2-4 Left Column
		if(!isLeftColFull() && (cardRank >= 1 && cardRank <= 3) && !doesCardExistInRowCol(cardRank, 0, false)) {
			int row = firstEmptyRowInLeftColumn();
			int[] posToPlace = {row, 0};
			grid[row][0] = card;
			return posToPlace;
		}

		//6-8 Top Row
		if(!isRowFull(0) && (cardRank >= 5 && cardRank <= 7) && !doesCardExistInRowCol(cardRank, 0, true)) {
			int posToPlace = mostPromisingCol(card.getRank(), 0);
			
			grid[0][posToPlace] = card;
			int[] playPos = { 0, posToPlace };
			return playPos;
		}
		
		//10-Q Bottom Row
		if(!isRowFull(4) && (cardRank >= 9 && cardRank <= 11) && !doesCardExistInRowCol(cardRank, 4, true)) {
			int posToPlace = mostPromisingCol(card.getRank(), 4);
			
			grid[4][posToPlace] = card;
			int[] playPos = { 4, posToPlace };
			return playPos;
		}

		
		/* If corners and outer regions are full, figure out where in the buffer it should be placed */
		
		
		if(!isBufferFull()) {
			
			//A or 5 - Play the card in the least harmful column		
			if(cardRank == 0 || cardRank == 4) {
				int row = -1;
				int column = -1;
				
				int lowestValue = Integer.MAX_VALUE;			
				
				for(int i=1; i<SIZE-1; i++) {
					
					ArrayList<Integer> openColumnsRow = this.getEmptySlotsInRow(i);
					
					if(!openColumnsRow.isEmpty()) {
						//[0] = value, [1] = index
						int[] pair = this.lowestValueForGivenRow(openColumnsRow);
						
						if(pair[0] <= lowestValue) {
							row = i;
							lowestValue = pair[0];
							column = pair[1];
						}
					}
				}

				int[] playPos = { row, column };
				grid[row][column] = card;
				return playPos;	
			}
			
			//2-4 - Play the card in the least harmful column, but try and place it in a row that has the same card rank in it
			if(cardRank >= 1 && cardRank <= 3) {
				int row = 0;
				int newRow = 0;
				
				//Check the left column for the card
				for(int i=1; i<SIZE-1; i++) {				
					if(grid[i][0] != null) {
						if(grid[i][0].getRank() == cardRank) {
							row = i;
							newRow = i;
						}
					}
				}
				
				ArrayList<Integer> openColumns = new ArrayList<Integer>();
				
				for(int i=1; i<SIZE; i++) {
					if(grid[row][i] == null)
						openColumns.add(i);
				}
				
				//If there were no empty columns in the preferred row, check the other rows
				if(openColumns.isEmpty()) {
					for(int i=1; i<SIZE-1; i++) {
						if(i != row) {
							if(openColumns.isEmpty()) {
								for(int j=1; j<SIZE; j++) {
									if(grid[i][j] == null) {
										newRow = i;
										openColumns.add(j);
									}
								}
							}						
						}
					}
				}
				
				if(row != newRow)
					row = newRow;

				//Place the card in the least-harmful column
				int lowestValue = Integer.MAX_VALUE;
				int index = -1;
				
				for(int i : openColumns) {
					if(columnValue[i-1] < lowestValue) {
						lowestValue = columnValue[i-1];
						index = i;
					}
				}
				
				int[] playPos = { row, index };
				grid[row][index] = card;
				return playPos;
			}
			
			//6-K
			else {
				int column = 0;
				
				for(int i=1; i<SIZE; i++) {
					//Check top row for the card
					if(grid[0][i] != null) {
						if(grid[0][i].getRank() == cardRank) {
							column = i;
						}
					}
					//Check bottom row for the card
					if(grid[4][i] != null) {
						if(grid[4][i].getRank() == cardRank) {
							column = i;
						}
					}
				}
				
				//Place the card in the first empty slot in the column with the same card rank
				for(int i=1; i<SIZE-1; i++) {
					if(grid[i][column] == null) {
						
						columnValue[column-1]++; //Increment the value of that column
						
						int[] playPos = { i, column };
						grid[i][column] = card;
						return playPos;
					}
				}
				
				//If there was no empty slot, place the card in the least-troublesome column
				int[] posToPlace = firstEmptySlotInBuffer();
				grid[posToPlace[0]][posToPlace[1]] = card;
				return posToPlace;
			}		
		}
		
		
		/* If the buffer is full, figure out the best outer slot for the card */
		
		
		else {
			int[] posToPlace = firstEmptySlotInWholeGrid();
			grid[posToPlace[0]][posToPlace[1]] = card;
			return posToPlace;
		}
	}

	
	/* Helper Functions - each are used for both Row and Column queries, based on the boolean passed to them */

	
	//Returns the lowest value and column for the given empty slots in a row
	private int[] lowestValueForGivenRow(ArrayList<Integer> emptyColumns) {
		int lowestValue = Integer.MAX_VALUE;
		int index = -1;
		
		for(int i : emptyColumns) {
			if(columnValue[i-1] <= lowestValue) {
				lowestValue = columnValue[i-1];
				index = i;
			}
		}
		
		int[] pair = { lowestValue, index };
		return pair;
	}
	
	//Returns an ArrayList of empty column indicies
	private ArrayList<Integer> getEmptySlotsInRow(int rowNum){
		ArrayList<Integer> emptySlots = new ArrayList<Integer>();
		
		for(int i=1; i<SIZE; i++) {
			if(grid[rowNum][i] == null)
				emptySlots.add(i);
		}
		
		return emptySlots;
	}
	
	//Returns the first empty row in a column
	private int[] firstEmptySlotInBuffer() {
		int[] pos = new int[2];
		
		for(int i=1; i<SIZE-1; i++) {
			for(int j=1; j<SIZE; j++) {
				if(grid[i][j] == null) {
					pos[0] = i;
					pos[1] = j;
					return pos;
				}
			}
		}	
		return pos;
	}	
	
	//Returns the first empty position in the entire grid
	private int[] firstEmptySlotInWholeGrid() {
		int[] pos = new int[2];
		
		for(int i=0; i<SIZE; i++) {
			for(int j=0; j<SIZE; j++) {
				if(grid[i][j] == null) {
					pos[0] = i;
					pos[1] = j;
					return pos;
				}
			}
		}	
		return pos;
	}
	
	//Returns the first empty row in the left column
	private int firstEmptyRowInLeftColumn() {
		
		for(int i=1; i<SIZE-1; i++) {
			if(grid[i][0] == null) {
				return i;
			}
		}	
		return -1;
	}
	
	//Check if row/column is full
	private boolean isRowFull(int rowNum) {
		
		for(int i=1; i<SIZE-1; i++) {
			if(grid[rowNum][i] == null)
				return false;
		}	
		return true;
	}
	
	//Check if left column is full
	private boolean isLeftColFull() {
		
		for(int i=1; i<SIZE-1; i++) {
			if(grid[i][0] == null)
				return false;
		}	
		return true;
	}
	
	//Returns true if the inner 3x4 buffer is completely full
	private boolean isBufferFull() {
		
		for(int i=1; i<SIZE-1; i++) {
			for(int j=1; j<SIZE; j++) {
				if(grid[i][j] == null)
					return false;
			}
		}	
		return true;
	}
	
	//Counts how many empty buffer slots exist in a given row/column
	private int numOfEmptyBufferSlotsInRowCol(int rowColNum, boolean row) {
		int count = 0;
		
		for(int i=1; i<SIZE-1; i++) {
			if(row) {
				if(grid[rowColNum][i] == null)
					count++;
			}
			else {
				if(grid[i][rowColNum] == null)
					count++;
			}
		}	
		return count;
	}
	
	//Counts how many of a given card currently exist in the grid
	private int numOfSpecificCardInGrid(int cardRank) {
		int count = 0;
		
		for(int i=0; i<SIZE; i++) {
			for(int j=0; j<SIZE; j++) {
				if(grid[i][j] != null) {
					if(grid[i][j].getRank() == cardRank)
						count++;
				}
			}
		}
		
		return count;
	}
	
	//Counts how many of a given card currently exist in a given row/column
	private int numOfSpecificCardInRowCol(int cardRank, int rowColNum, boolean row) {
		int count = 0;
		
		for(int i=0; i<SIZE; i++) {
			if(row) {
				if(grid[rowColNum][i] != null) {
					if(grid[rowColNum][i].getRank() == cardRank)
						count++;
				}
			}
			else {
				if(grid[i][rowColNum] != null) {
					if(grid[i][rowColNum].getRank() == cardRank)
						count++;
				}
			}
		}	
		
		return count;
	}
	
	//Check if a card exists in a row/column - used for straights
	private boolean doesCardExistInRowCol(int cardRank, int rowColNum, boolean row) {
		
		for(int i=0; i<SIZE; i++) {
			if(row) {
				if(grid[rowColNum][i] != null) {
					if(grid[rowColNum][i].getRank() == cardRank)
						return true;
				}
			}
			else {
				if(grid[i][rowColNum] != null) {
					if(grid[i][rowColNum].getRank() == cardRank)
						return true;
				}
			}
		}	
		return false;		
	}
	
	//Returns the most promising row/column, given a card
	private int mostPromisingCol(int cardRank, int rowNum) {
		
		double value[] = new double[3];
		double threeOfKind = 0;
		double fourOfKind = 0;
		
		//return firstEmptySlotInRow(rowNum);
		
		for(int i=1; i<SIZE-1; i++) {
			threeOfKind = chance3OfKind(cardRank, rowNum, true);
			fourOfKind = chance4OfKind(cardRank, rowNum, true);
			value[i-1] = threeOfKind + fourOfKind;
		}
		
		double maxVal = 0;
		int maxIndex = -1;
		
		for(int i=0; i<value.length; i++) {
			if(grid[rowNum][i+1] == null) {
				if(value[i] >= maxVal) {
					maxVal = value[i];
					maxIndex = i+1;
				}
			}
		}
		
		return maxIndex;
		
	}
	
	//Calculates the chance that 3 of a kind gives
	private double chance3OfKind(int cardRank, int rowColNum, boolean row) {
		int emptySlotsInRowCol = numOfEmptyBufferSlotsInRowCol(rowColNum, row);
		int givenCardInGrid = numOfSpecificCardInGrid(cardRank);
		int givenCardInRowCol = numOfSpecificCardInRowCol(cardRank, rowColNum, row);
		
		//Case - Three cards of given rank exist in the grid
		if(givenCardInGrid == 3) {
			if(givenCardInRowCol < 2)
				return 0;
			else
				return 6;
		}
		
		//Case - Two cards of given rank exist in the grid
		if(givenCardInGrid == 2) {
			if(givenCardInRowCol == 0)
				return 0;
			else
				if(emptySlotsInRowCol == 0)
					return 0;
				else
					return (givenCardInRowCol * 3) + emptySlotsInRowCol;
		}
		
		//Case - One card of given rank exists in the grid
		if(givenCardInGrid == 1) {
			if(givenCardInRowCol == 0 && emptySlotsInRowCol < 2)
				return 0;
			else {
				if(givenCardInRowCol == 0)
					return -2 + emptySlotsInRowCol;
				else
					return 3 + emptySlotsInRowCol;
			}
		}
		
		//Case - No cards of given rank exist in the grid yet
		return emptySlotsInRowCol;
	}
	
	//Calculates the chance that 4 of a kind gives
	private double chance4OfKind(int cardRank, int rowColNum, boolean row) {
		int emptySlotsInRowCol = numOfEmptyBufferSlotsInRowCol(rowColNum, row);
		int givenCardInGrid = numOfSpecificCardInGrid(cardRank);
		int givenCardInRowCol = numOfSpecificCardInRowCol(cardRank, rowColNum, row);
		
		//Case - Three cards of given rank exist in the grid
		if(givenCardInGrid == 3) {
			if(givenCardInRowCol < 3)
				return 0;
			else
				return 16;
		}
		
		//Case - Two cards of given rank exist in the grid
		if(givenCardInGrid == 2) {
			if(givenCardInRowCol < 2)
				return 0;
			else
				if(emptySlotsInRowCol == 1)
					return 12;
				else
					return 0;
		}
		
		//Case - One card of given rank exists in the grid
		if(givenCardInGrid == 1) {
			if(givenCardInRowCol < 1)
				return 0;
			else {
				if(emptySlotsInRowCol == 2)
					return 8;
				else
					return 0;
			}
		}
		
		//Case - No cards of given rank exist in the grid yet
		return 6;
	}
	
	/* Default Methods */
	
	public double[] getPlayProbabilities(int[][] data) {
		return null;
	}

	@Override
	public String getName() {
		return "smortBoi";
	}

	public static void main(String[] args) {
		PokerSquaresPointSystem system = PokerSquaresPointSystem.getBritishPointSystem();
		System.out.println(system);
		new PokerSquares(new dhiggins_smortBoiPlayer(), system).play();
	}

}
