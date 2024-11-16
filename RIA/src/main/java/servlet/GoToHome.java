package servlet;

import com.google.gson.Gson;

import java.io.IOException;         
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import beans.Utente;
import dao.CartellaDAO;
import beans.Cartella;
import utils.ConnectionHandler;

@WebServlet("/GoToHome")
public class GoToHome extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	
	public GoToHome() {
		super();
	}
	
	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}
	
	//Mostra tutte le cartelle e le sottocartelle dell'utente
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		
		//Controlli iniziali
		HttpSession session = request.getSession();
		if(session.isNew() || session.getAttribute("utente")==null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println("Non sei loggato");
			//response.setHeader("Location", request.getServletContext().getContextPath() + "/login.html");
			return;
		}
		else if(connection == null) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Impossibile connettersi con il database");
			return;			
		}
		
		//Recupero dell'albero delle cartelle
		Utente user = (Utente) session.getAttribute("utente");
		String username = user.getUsername();
		CartellaDAO cDAO = new CartellaDAO(connection);
		List<Cartella> albero = null;
		try {
			albero = cDAO.getAllCartelleFromUtente(username);
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Errore inaspettato");
			return;
		}
		
		//Reindirizzamento
		String json = new Gson().toJson(albero);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
		return;
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