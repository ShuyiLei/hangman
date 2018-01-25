package launch;

import java.util.*;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.CookieManager;
import java.net.HttpURLConnection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.File;

import org.apache.catalina.startup.Tomcat;

public class Tests{
    private final static String USER_AGENT = "Mozilla/5.0";
    private final static String urlString = "http://127.0.0.1:8080/WordServlet";
    private static URL url;
    private static HttpURLConnection con;
    private static List<String> cookies;
    private static List<String> cookies1;


    public static void main(String[] args) throws Exception{
        Tomcat tomcat = null;
        try{
            // Put a test file to tell the servlet it is test mode
            if(!new File("test").exists()){
                Files.createFile(Paths.get("test"));
            }
            

            // Call the startServer of Main, but don't await
            // to do tests
            tomcat = Main.startServer();

            init();
            Map<String, String> parameters = new HashMap<>();
            parameters.put("requestType", "gamestart");
            String res;

            System.out.println("Test start a new game");
            res = get(parameters);
            assertRes("{\"len\":7,\"lw\":0,\"ll\":0,\"gw\":0,\"gl\":0}", res);
            

            System.out.println("Test abandon game before finish");
            res = get(parameters);
            assertRes("{\"len\":7,\"lw\":0,\"ll\":1,\"gw\":0,\"gl\":1}", res);

            System.out.println("Test abandon game before finish 2");
            res = get(parameters);
            assertRes("{\"len\":7,\"lw\":0,\"ll\":2,\"gw\":0,\"gl\":2}", res);


            System.out.println("Test a wrong guess");
            parameters.put("requestType", "guess");
            parameters.put("letter", "Z");
            res = get(parameters);
            assertRes("{\"res\":\"wrong\",\"state\":\"1\"}", res);


            System.out.println("Test another wrong guess");
            parameters.put("requestType", "guess");
            parameters.put("letter", "Y");
            res = get(parameters);
            assertRes("{\"res\":\"wrong\",\"state\":\"2\"}", res);

            System.out.println("Test a guessed wrong guess");
            parameters.put("requestType", "guess");
            parameters.put("letter", "Z");
            res = get(parameters);
            assertRes("{\"res\":\"guessed\"}", res);

            System.out.println("Test a correct guess, occur once");
            parameters.put("requestType", "guess");
            parameters.put("letter", "S");
            res = get(parameters);
            assertRes("{\"res\":\"ok\",\"detail\":[2]}", res);

            System.out.println("Test a correct guess, occur twice");
            parameters.put("requestType", "guess");
            parameters.put("letter", "T");
            res = get(parameters);
            assertRes("{\"res\":\"ok\",\"detail\":[0,3]}", res);

            System.out.println("Test a correct guess, occur 3 times");
            parameters.put("requestType", "guess");
            parameters.put("letter", "O");
            res = get(parameters);
            assertRes("{\"res\":\"ok\",\"detail\":[4,5,6]}", res);

            System.out.println("Test a correct guess, guessed before");
            parameters.put("requestType", "guess");
            parameters.put("letter", "T");
            res = get(parameters);
            assertRes("{\"res\":\"guessed\"}", res);

            System.out.println("Test game win");
            parameters.put("requestType", "guess");
            parameters.put("letter", "E");
            res = get(parameters);
            assertRes("{\"res\":\"pass\",\"detail\":[1]}", res);


            System.out.println("Test a new game after win");
            parameters.put("requestType", "gamestart");
            parameters.remove("letter");
            res = get(parameters);
            assertRes("{\"len\":7,\"lw\":1,\"ll\":2,\"gw\":1,\"gl\":2}", res);

            System.out.println("Test a correct guess, occur twice");
            parameters.put("requestType", "guess");
            parameters.put("letter", "T");
            res = get(parameters);
            assertRes("{\"res\":\"ok\",\"detail\":[0,3]}", res);

            System.out.println("Test 10 wrong guesses");
            parameters.put("requestType", "guess");
            parameters.put("letter", "Z");
            res = get(parameters);
            assertRes("{\"res\":\"wrong\",\"state\":\"1\"}", res);

            parameters.put("requestType", "guess");
            parameters.put("letter", "Y");
            res = get(parameters);
            assertRes("{\"res\":\"wrong\",\"state\":\"2\"}", res);

            parameters.put("requestType", "guess");
            parameters.put("letter", "W");
            res = get(parameters);
            assertRes("{\"res\":\"wrong\",\"state\":\"3\"}", res);

            parameters.put("requestType", "guess");
            parameters.put("letter", "V");
            res = get(parameters);
            assertRes("{\"res\":\"wrong\",\"state\":\"4\"}", res);

            parameters.put("requestType", "guess");
            parameters.put("letter", "U");
            res = get(parameters);
            assertRes("{\"res\":\"wrong\",\"state\":\"5\"}", res);

            // Another 5
            parameters.put("requestType", "guess");
            parameters.put("letter", "A");
            res = get(parameters);
            assertRes("{\"res\":\"wrong\",\"state\":\"6\"}", res);

            parameters.put("requestType", "guess");
            parameters.put("letter", "B");
            res = get(parameters);
            assertRes("{\"res\":\"wrong\",\"state\":\"7\"}", res);

            parameters.put("requestType", "guess");
            parameters.put("letter", "C");
            res = get(parameters);
            assertRes("{\"res\":\"wrong\",\"state\":\"8\"}", res);

            parameters.put("requestType", "guess");
            parameters.put("letter", "D");
            res = get(parameters);
            assertRes("{\"res\":\"wrong\",\"state\":\"9\"}", res);

            parameters.put("requestType", "guess");
            parameters.put("letter", "F");
            res = get(parameters);
            assertRes("{\"res\":\"failed\",\"ans\":\"TESTOOO\"}", res);

            System.out.println("Test guess after lost");
            parameters.put("requestType", "guess");
            parameters.put("letter", "Z");
            res = get(parameters);
            assertRes("404", res);

            System.out.println("Test new game after lost");
            parameters.put("requestType", "gamestart");
            parameters.remove("letter");
            res = get(parameters);
            assertRes("{\"len\":7,\"lw\":1,\"ll\":3,\"gw\":1,\"gl\":3}", res);

            System.out.println("Test new game from another user");
            parameters.put("requestType", "gamestart");
            res = anotherget(parameters);
            assertRes("{\"len\":7,\"lw\":0,\"ll\":0,\"gw\":1,\"gl\":3}", res);


            System.out.println("Cross guessing from 2 clients");
            parameters.put("requestType", "guess");
            parameters.put("letter", "Z");
            res = get(parameters);
            assertRes("{\"res\":\"wrong\",\"state\":\"1\"}", res);

            parameters.put("requestType", "guess");
            parameters.put("letter", "Y");
            res = get(parameters);
            assertRes("{\"res\":\"wrong\",\"state\":\"2\"}", res);

            parameters.put("requestType", "guess");
            parameters.put("letter", "Z");
            res = anotherget(parameters);
            assertRes("{\"res\":\"wrong\",\"state\":\"1\"}", res);

            parameters.put("requestType", "guess");
            parameters.put("letter", "T");
            res = anotherget(parameters);
            assertRes("{\"res\":\"ok\",\"detail\":[0,3]}", res);

            parameters.put("requestType", "guess");
            parameters.put("letter", "T");
            res = get(parameters);
            assertRes("{\"res\":\"ok\",\"detail\":[0,3]}", res);


            System.out.println("Test 2 clients both abandon game");
            parameters.put("requestType", "gamestart");
            parameters.remove("letter");
            res = anotherget(parameters);
            assertRes("{\"len\":7,\"lw\":0,\"ll\":1,\"gw\":1,\"gl\":4}", res);

            parameters.put("requestType", "gamestart");
            res = get(parameters);
            assertRes("{\"len\":7,\"lw\":1,\"ll\":4,\"gw\":1,\"gl\":5}", res);


            System.out.println("PASS:All tests passed");

        }
        catch (RuntimeException e){
            e.printStackTrace();
        }
        finally{
            if(tomcat != null){
                tomcat.getServer().stop();
            }
            // Clean up to avoid test mode in production
            File testf = new File("test");
            if(testf.exists()){
                testf.delete();
            }
        }
    }

    private static void init() throws Exception{
        url = new URL(urlString);
        
    }

    private static String get(Map<String, String> parameters) throws Exception{
        return getBase(parameters, 0);
    }

    private static String anotherget(Map<String, String> parameters) throws Exception{
        return getBase(parameters, 1);
    }

    private static String getBase(Map<String, String> parameters, int i) throws Exception{

        con = (HttpURLConnection) url.openConnection();
        // The games in backend is managed by session-id
        // So needs to restore cookies
        // 2 set of cookies, depends on i, for multiple user test
        if(i == 0){
            if (cookies != null) {
                for (String cookie : cookies) {
                    con.addRequestProperty("Cookie", cookie.split(";", 2)[0]);
                }
            }
        } else if (i == 1){
            if (cookies1 != null) {
                for (String cookie : cookies1) {
                    con.addRequestProperty("Cookie", cookie.split(";", 2)[0]);
                }
            }
        }

        // optional default is GET
        con.setRequestMethod("GET");
        // add request header
        con.setRequestProperty("User-Agent", USER_AGENT);
        // Write out the parameters to the connection
        con.setDoOutput(true);
        DataOutputStream out = new DataOutputStream(con.getOutputStream());
        out.writeBytes(ParameterStringBuilder.getParamsString(parameters));
        out.flush();
        out.close();
        
        // Get response code
        int responseCode = con.getResponseCode();
        //System.out.println("\nSending 'GET' request to URL : " + url);
        //System.out.println("Response Code : " + responseCode);
        if(responseCode > 400){
            return ""+responseCode;
        }

        // If asked to set cookie, record it
        // To let the server keep the state, know who we are
        if(i == 0){
            if(cookies == null)
                cookies = con.getHeaderFields().get("Set-Cookie");
        } else if( i == 1){
            if(cookies1 == null)
                cookies1 = con.getHeaderFields().get("Set-Cookie");            
        }
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    private static void assertRes(String expect, String actual) throws RuntimeException{
        // Check if the response match expect
        if(!expect.equals(actual)){
            throw new RuntimeException("Expect: " + expect + ", Actual: " + actual);
        }
    }

    public static class ParameterStringBuilder {
        public static String getParamsString(Map<String, String> params) 
          throws Exception{
            StringBuilder result = new StringBuilder();
     
            for (Map.Entry<String, String> entry : params.entrySet()) {
              result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
              result.append("=");
              result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
              result.append("&");
            }
     
            String resultString = result.toString();
            return resultString.length() > 0
              ? resultString.substring(0, resultString.length() - 1)
              : resultString;
        }
    }
}
