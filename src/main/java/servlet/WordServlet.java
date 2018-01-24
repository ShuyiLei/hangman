package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

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
    private static final String[] wordpool = new String[] {"goose","cheese","daydream","pizza","strategy","confirm"
    		,"awkward","pixel","jazz","funny","lucky","graduate","vivid"
    };
    private static final Random RANDOM = new Random();
    private static final Map<String, WordParser> clients = new ConcurrentHashMap<>();
    // Statistic numbers
    private static int total_games = 0;
    private static int won_games = 0;
    private static int lost_games = 0;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public WordServlet() {
        super();
        // TODO Auto-generated constructor stub
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
			String anwser = wordpool[RANDOM.nextInt(wordpool.length)];
			// Log to Tomcat for debugging
			System.out.println("answer: " + anwser);
			// Plain text response type
			response.setContentType("text/plain");
			// UTF-8 character set
		    response.setCharacterEncoding("UTF-8");
		    // This is to help making the answer
		    WordParser wordParser = new WordParser(anwser, this);
		    clients.put(session_id, wordParser);
		    // Write length
			out.append(anwser.length()+"");
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

}
