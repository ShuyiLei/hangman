package servlet;
import java.util.*;

public class WordParser {
	private Map<String, Letter> letterList= new HashMap<>();
	private int state = 0;
	private int letter_num = 0; // Number of different letters
	private WordServlet wordServlet;
	private boolean failed = false; // Anti-cheat
	private String word;
	public WordParser(String w, WordServlet wordServlet) {
		// This is to call statistic methods
		this.wordServlet = wordServlet;
		word = w.toUpperCase();
		for( int i = 0; i < word.length(); i ++) {
			// Get ist letter as String
			String l = word.substring(i, i + 1);
			// Add to hashmap
			if(letterList.containsKey(l)) {
				letterList.get(l).addOccur(i);
			} else {
				// This doesn't have a comma because it would be the first index
				letterList.put(l, new Letter(i));
				letter_num ++;
			}
		}
		for(Letter letter:letterList.values()) {
			letter.finishInit();
		}
	}


	public String guess(String l) {
		// Guess return a JSON string that will be parsed in front-end
		// To avoid more libraries involved, construct JSON string manually
		// Since the structure is simple
		if(failed) {
			// Anti-cheat
			return "{\"res\":\"failed\"}";
		}
		StringBuffer stringBuffer = new StringBuffer("{");
		if(!letterList.containsKey(l)) {
			// Not in our correct letter list, guessed a wrong one
			// state is the wrong times a user guessed
			state ++;
			if(state > 9) {
				// More than 9 wrong guess, game lost
				//stringBuffer.append("\"res\":\"failed\",\"ans\"=\"");
				//stringBuffer.append(word + "\"");
				stringBuffer.append("\"res\":\"failed\",\"ans\":\"" + word + "\"");
				wordServlet.add_lost();
				failed = true;
			}
			else {
				// Wrong guess, not lost yet
				// Tell front-end how many wrong guesses it got
				stringBuffer.append("\"res\":\"wrong\",\"state\":\"" + state + "\"");
			}

		}
		else {
			// Guess in our guess list
			Letter letter = letterList.get(l);
			if(letter.getGuessed()) {
				// Has already guessed, tell front-end
				// Note it is only for correct guess, since it is already displayed
				// in front-end. Won't do so for wrong guesses to make the game more fun
				stringBuffer.append("\"res\":\"guessed\"");
			}
			else {
				// Never guessed letter, one less remaining letter
				letter_num--;
				if(letter_num == 0) {
					// No remaining covered letter, game won!
					stringBuffer.append("\"res\":\"pass\"");
					wordServlet.add_win();
				}
				else {
					stringBuffer.append("\"res\":\"ok\"");
				}
				stringBuffer.append(",\"detail\":");
				// Tell front-end the indexes of the letter guessed
				stringBuffer.append(letter.guess());
			}
		}

		stringBuffer.append("}");
		return stringBuffer.toString();
	}

	private static class Letter{
		private boolean guessed = false;
		private String jsonList;

		// jsonList is manually constructed
		// it is a JSON integer array, so surround with square brackets when finished
		protected Letter(int i) {
			jsonList = "" + i;
		}

		protected void addOccur(int i) {
			jsonList = jsonList + "," + i;
		}

		protected void finishInit() {
			jsonList = "[" + jsonList + "]";
		}

		protected boolean getGuessed() {
			return guessed;
		}

		protected String guess() {
			guessed = true;
			return jsonList;
		}
	}
}
