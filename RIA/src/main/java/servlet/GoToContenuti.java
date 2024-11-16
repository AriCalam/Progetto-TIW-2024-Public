package servlet;

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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dao.CartellaDAO;
import beans.Cartella;
import dao.DocumentoDAO;
import beans.Documento;
import beans.Utente;
import utils.ConnectionHandler;

@WebServlet("/GoToContenuti")
public class GoToContenuti extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	
	public GoToContenuti() {
		super();
	}
	
	//Crea la servlet
	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}
	
	//Mostra tutte le sottocartelle e i documenti inseriti nella cartella selezionata dall'utente
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
  		
		//Recupero dell'id della cartella di cui si vuole il contenuto
  		int idFinale;
  		String idCartella1 = null;
  		int idCartella;
  		if(request.getAttribute("idCartella") != null) {
  			idCartella1 = String.valueOf(request.getAttribute("idCartella"));
  			try {
  	  			idFinale = Integer.parseInt(idCartella1);
  	  		} catch (NumberFormatException e) {
	  	  		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Formato numerico errato");
  	  			return;
  	  		}
  			idCartella = idFinale;
  		}
  		else {
  			//Estraggo dalla request della Home il parametro idCartella
  	  		String id = request.getParameter("idCartella");
  	  		if (id == null || id.isEmpty()) {
	  	  		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Parametro mancante");
  	  			return;
  	  		}
  	  		//Trasformo il parametro estratto in un intero
  	  		int actualId = -1;
  	  		try {
  	  			actualId = Integer.parseInt(id);
  	  		} catch (NumberFormatException e) {
	  	  		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Formato numerico errato");
  	  			return;
  	  		}
  	  		idCartella = actualId;
  		}
  		
  		//Creo i DAO necessari
  		Utente utente = (Utente) session.getAttribute("utente");
  		CartellaDAO cartellaDAO = new CartellaDAO(connection);
  		DocumentoDAO docDAO = new DocumentoDAO(connection);
		
  		//Cerco la cartella selezionata tramite il suo id: se non è presente ritorno un errore
		try {
			Cartella c = cartellaDAO.findCartellaById(idCartella, utente.getUsername());
			if(c == null) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				response.getWriter().println("Nessuna cartella con l'id fornito");
				return;
			}
			
			//Se invece è presente, allora cerco le sue sottocartelle e i suoi documenti
			JsonObject json = new JsonObject();
			json.addProperty("nomeCartella", c.getNomeCartella());
			json.add("cartelle", parseCartelle(c, cartellaDAO, utente.getUsername()));
			json.add("documenti", parseDocs(c, docDAO, utente.getUsername()));
			
			//Invio il risultato
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().println(json);
			response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
			return;
            
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Errore con il database");
			return;
		}
	}

	private JsonArray parseDocs(Cartella cartella, DocumentoDAO docDAO, String username) {
		JsonArray ret = new JsonArray();
		List<Documento> list;
		try {
			list = docDAO.findDocByCartella(cartella.getIdCartella(), username);
			for(Documento d : list) {
				JsonObject docJson = new JsonObject();
				docJson.addProperty("idDocumento", d.getIdDocumento());
				docJson.addProperty("nomeDocumento", d.getNomeDocumento());
				docJson.addProperty("idPadre", d.getIdPadre());
				ret.add(docJson);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	private JsonArray parseCartelle(Cartella cartella, CartellaDAO cDAO, String username) {
		JsonArray ret = new JsonArray();
		List<Cartella> list;
		try {
			list = cDAO.findSottocartelleByPadre(cartella.getIdCartella(), username);
			for(Cartella c : list) {
				JsonObject cartJson = new JsonObject();
				cartJson.addProperty("idCartella", c.getIdCartella());
				cartJson.addProperty("nomeCartella", c.getNomeCartella());
				cartJson.addProperty("idPadre", c.getIdPadre());
				ret.add(cartJson);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ret;
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