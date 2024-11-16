package servlet;

import java.io.BufferedReader;
import java.io.IOException;       
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
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

@WebServlet("/ControllaLogin")
@MultipartConfig
public class ControllaLogin extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
	
	public ControllaLogin() {
		super();
	}
	
	//Crea la servlet e la connessione al db
	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		connection = ConnectionHandler.getConnection(servletContext);
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
	
	//Controlla le credenziali inserite
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
	
		String usrn = null;
		String pwd = null;
		
		//Lettura dei campi dalla request
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

            usrn = StringEscapeUtils.escapeJava(ret.get("username").getAsString());
            pwd = StringEscapeUtils.escapeJava(ret.get("password").getAsString());
            if (usrn == null || pwd == null) {
            	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      			response.getWriter().println("Campi mancanti");
    			return;
            }
            if (usrn.isEmpty() || pwd.isEmpty()) {
            	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      			response.getWriter().println("Campi vuoti");
    			return;
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Credenziali sbagliate, riprova");
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            return;
        }

		//Query
		UtenteDAO userDao = new UtenteDAO(connection);
		Utente user = null;
		try {
			user = userDao.checkCredenziali(usrn, pwd);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Impossibile controllare le credenziali");
			return;
		}

		//Reindirizzamento
		if (user == null) { //Se l'utente non esiste, mostro un msg di errore e ritorno alla pagina di login
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println("Credenziali scorrette. Riprova.");
			return;
		} else { //Se invece esiste, aggigungo le info alla sessione e reindirizzo l'utente alla home page
			request.getSession().setAttribute("utente", user);
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().println(usrn);
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