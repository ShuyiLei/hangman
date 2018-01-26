package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.io.File;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * Servlet implementation class WordServlet
 */
@WebServlet("/WordServlet")
public class WordServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    // private static final String[] wordpool = new String[] {"goose","cheese","daydream","pizza","strategy","confirm"
    // 		,"awkward","pixel","jazz","funny","lucky","graduate","vivid"
    // };
    private static final Random RANDOM = new Random();
	private static final Map<String, WordParser> clients = new ConcurrentHashMap<>();
	private static final Map<String, SessionStat> session_stat = new ConcurrentHashMap<>();
    // Statistic numbers
    private static int total_games = 0;
    private static int won_games = 0;
	private static int lost_games = 0;
	
	private static List<String> wordpool;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public WordServlet() {
		super();
		System.out.println("Working Directory = " +
		System.getProperty("user.dir"));
		// For test, I put a file in current directory named test to indicate it is in test mode
		// The word will be fixed in test mode
		File testfile = new File( "test" );
		if(testfile.exists()){
			wordpool = Arrays.asList("testooo");
		} else {
			try{
				wordpool = Files.readAllLines( Paths.get("words.txt") );
			} 
			catch(IOException e){
				System.out.println("File words.txt not found");
			}
		}	
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Type is a parameter of HTTP get
		String type = request.getParameter("requestType");
		// Get session and session id to manage multi-user cases
		HttpSession session = request.getSession();
		String session_id = session.getId();
		System.out.println("sid: "+ session_id);
		// write to out to response to the client
		PrintWriter out = response.getWriter();
		if(type == null) {
			// Send a 405 error to front-end
			// So front-end will reset the game.
			response.sendError(405);
			return;
		}
		if(type.equals("stat")) {
			// Statistics
			// Plain text response type
			response.setContentType("text/plain");
			// UTF-8 character set
		    response.setCharacterEncoding("UTF-8");
			String reString = "Created " + total_games + " games, won " + won_games
					+ " games, lost " + lost_games + " games.";
			System.out.println(reString);
			out.append(reString);
		}
		else if(type.equals("gamestart")) {
			// Random get an answer from word pool
			String answer = wordpool.get(RANDOM.nextInt(wordpool.size()));
			// Tomcat log the word
			System.out.println("answer: " + answer);
			// Plain text response type
			response.setContentType("application/json");
			// UTF-8 character set
			response.setCharacterEncoding("UTF-8");
			// Statistic for this session
			SessionStat stat = null;			
			if(!session_stat.containsKey(session_id)){
				stat = new SessionStat();
				session_stat.put(session_id, stat);
			} else {
				stat = session_stat.get(session_id);
			}
			// This is to help making the answer
			// If it is not null, a previous game exists. If it is still pending,
			// treat as lost
			//if(clients.containsKey(session_id) && clients.get(session_id).getStatus() == 0){
			if(clients.containsKey(session_id)){
				add_lost();
				stat.lost ++;
			}
		    WordParser wordParser = new WordParser(answer, this);
		    clients.put(session_id, wordParser);
		    // Write json of len and win/lost stat
			String res = "{\"len\":" + answer.length() + ",\"lw\":"
				+ stat.win + ",\"ll\":" + stat.lost + ",\"gw\":" 
				+ won_games + ",\"gl\":" + lost_games + "}";
			System.out.println(res);
			out.append(res);
			synchronized (this) {
				total_games ++;
			}
		}
		else {
		// If not on a gamestart, we must check if the game exist
			if(!clients.containsKey(session_id)) {
				response.sendError(404);
				return;
			}
			// Get corresponding WordParser from map
			WordParser wordParser = clients.get(session_id);
			if(type.equals("guess")) {
				String letter = request.getParameter("letter")
									   .substring(0,1).toUpperCase();
				String res = wordParser.guess(letter);
				// Log response for debugging
				System.out.println(res);
				// Now sending JSON
				response.setContentType("application/json");
			    response.setCharacterEncoding("UTF-8");
				response.getWriter().append(res);

				int status = wordParser.getStatus();
				if(status == 1){
					// Game lost
					add_lost();
					session_stat.get(session_id).lost ++;
					clients.remove(session_id);
				} else if (status == 2){
					// Game win
					add_win();
					session_stat.get(session_id).win ++;
					clients.remove(session_id);
				}

			}
			else {
				response.setContentType("text/plain");
			    response.setCharacterEncoding("UTF-8");
				response.getWriter().append("error");
			}
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

	// Statistic methods
	public synchronized void add_win() {
		won_games ++;
	}

	public synchronized void add_lost() {
		lost_games ++;
	}

	private class SessionStat {
		int win;
		int lost;
	}

}
