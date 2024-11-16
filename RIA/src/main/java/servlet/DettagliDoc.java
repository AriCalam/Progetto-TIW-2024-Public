package servlet;

import java.io.IOException;         
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

//import dao.CartellaDAO;
//import beans.Cartella;
import dao.DocumentoDAO;
import beans.Documento;
import beans.Utente;
import utils.ConnectionHandler;

@WebServlet("/DettagliDoc")
public class DettagliDoc extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	
	public DettagliDoc() {
		super();
	}
	
	//Crea la servlet
	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}
	
	//Mostra i dettagli del documento selezionato
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		//Controlli iniziali
		HttpSession session = request.getSession();
		if(session.isNew() || session.getAttribute("utente")==null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println("Non sei loggato");
			return;
		}
		else if(connection == null) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Impossibile connettersi con il database");
			return;			
		}
		
  		//Estraggo dalla request il parametro idDoc
  		String id = request.getParameter("idDocumento");
  		if (id == null || id.isEmpty()) {
  			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Parametro mancante");
	  		return;
  		}
  		//Trasformo il parametro estratto in un intero
  		int idDoc = -1;
  		try {
  			idDoc = Integer.parseInt(id);
  		} catch (NumberFormatException e) {
  			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Formato numerico errato");
  			return;
  		}
  		
  		Utente utente = (Utente) session.getAttribute("utente");
  		DocumentoDAO docDAO = new DocumentoDAO(connection);
  		Documento doc = new Documento();
  		
  		//Cerco il documento selezionato tramite il suo id: se non è presente ritorno un errore
		try {
			doc = docDAO.findDocById(idDoc, utente.getUsername());
			if(doc == null) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				response.getWriter().println("Nessun documento con l'id fornito");
  	  			return;
			}
			
			//Reindirizzamento
			String json = new Gson().toJson(doc);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(json);
			return;
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Problema con il database");
	  			return;
		}	
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
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