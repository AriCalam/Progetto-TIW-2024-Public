package servlet;

import java.io.BufferedReader;
import java.io.IOException;         
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import beans.Utente;
import dao.UtenteDAO;
import utils.ConnectionHandler;

@WebServlet("/ControllaRegistrazione")
@MultipartConfig
public class ControllaRegistrazione extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	
	public ControllaRegistrazione() {
		super();
	}
	
	//Crea la servlet
	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Utente user = (Utente) request.getSession().getAttribute("utente");
        if (request.getSession().getAttribute("utente") != null) {
        	request.getSession().setAttribute("utente", user);
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().println(user);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println("You are not logged in.");
        }
    }
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
  		String usr = null;
  		String email = null;
  		String pwd = null;
  		String ripeti = null;
  		
  		//Estrazione dei parametri dalla richiesta
  		try {
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
                buffer.append(System.lineSeparator());
            }
            String data = buffer.toString();
            JsonObject ret = new JsonObject();
            ret = new Gson().fromJson(data, JsonObject.class);

            usr = StringEscapeUtils.escapeJava(ret.get("username").getAsString());
            email = StringEscapeUtils.escapeJava(ret.get("email").getAsString());
            pwd = StringEscapeUtils.escapeJava(ret.get("password1").getAsString());
            ripeti = StringEscapeUtils.escapeJava(ret.get("password2").getAsString());
            
            if (usr == null || pwd == null || ripeti == null) {
            	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      			response.getWriter().println("Campi mancanti");
    			return;
            }
                
            if (usr.isEmpty() || pwd.isEmpty() || ripeti.isEmpty()) {
            	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      			response.getWriter().println("Campi vuoti");
    			return;
            }
            
            if(usr.length() > 16 || pwd.length() > 16 || email.length() > 45 || ripeti.length() > 16 || pwd.length() < 8
            		|| ripeti.length() < 8) {
            	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      			response.getWriter().println("Non hai rispettato la lunghezza dei campi");
    			return;
            }
                
            if (!Pattern.compile("^\\w+([\\.-]?\\w+)*@\\w+([\\.-]?\\w+)*(\\.\\w{2,3})+$", Pattern.CASE_INSENSITIVE).matcher(email).find()) {
            	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      			response.getWriter().println("Not an email");
    			return;
            }
            	
            email = email.toLowerCase();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Credenziali errate, riprova");
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            return;
        }
  		
		//Query: controllo che non ci sia già un utente con quell'username nel db
		UtenteDAO uDao = new UtenteDAO(connection);
		boolean isUnique = false;
		try {
			isUnique = uDao.checkUsername(usr);
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		response.getWriter().println("Impossibile verificare unicità username");
			return;
		}
		
		//Reindirizzamento
		if (isUnique) { //Se lo username è disponibile, allora crea l'utente e passo alla Home
			try {
				//Creo l'utente
				uDao.creaUtente(usr, email, pwd);
				Utente user = uDao.checkCredenziali(usr, pwd);
				request.getSession().setAttribute("utente", user);
				response.setStatus(HttpServletResponse.SC_OK);
				//response.getWriter().println("Utente creato con successo. Fai il login.");
			} catch (SQLException e) {
				e.printStackTrace();
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	  			response.getWriter().println("Impossibile creare l'utente");
	  			return;
			}
		} else { //Se invece esiste, mostra un messaggio di errore e torna alla Registrazione
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
  			response.getWriter().println("Utente esistente. Cambia username.");
  			return;
		}
	}
	
	//Distrugge la servlet
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}