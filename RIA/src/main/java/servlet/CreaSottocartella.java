package servlet;

import java.io.IOException;         
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;

import beans.Utente;
import beans.Cartella;
import dao.CartellaDAO;
import utils.ConnectionHandler;

@WebServlet("/CreaSottocartella")
@MultipartConfig
public class CreaSottocartella extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	
	public CreaSottocartella() {
		super();
	}
	
	//Crea la servlet
	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		//Controlli iniziali
		HttpSession session = request.getSession();
		if(session.isNew() || session.getAttribute("utente")==null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); //401
			response.getWriter().println("Non sei loggato");
			return;
		}
		else if(connection == null) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); //500
			response.getWriter().println("Impossibile connettersi con il database");
			return;			
		}
  		
  		String nomeCartella = null;
  		Utente utente = (Utente) session.getAttribute("utente");
  		String proprietario = utente.getUsername();
  		
  		//Estraggo dalla request i parametri String e li trasformo
  		try {
  			nomeCartella = StringEscapeUtils.escapeJava(request.getParameter("nomeCartella"));
  			
  			if (nomeCartella == null || nomeCartella.isEmpty()) {
  				response.setStatus(HttpServletResponse.SC_BAD_REQUEST); //400
  	  			response.getWriter().println("Nome cartella mancante");
  				return;
			}
  			
  			if(nomeCartella.length()>16 || nomeCartella.length()<1) {
  				response.setStatus(HttpServletResponse.SC_BAD_REQUEST); //400
  	  			response.getWriter().println("Non hai rispettato la lunghezza minima/massima dei campi");
  				return;
			}
  		}  catch (Exception e) {
  			response.setStatus(HttpServletResponse.SC_BAD_REQUEST); //400
  			response.getWriter().println("Parametro mancante");
			return;
		}
  		
  		//Estraggo dalla request il parametro idPadre
  		String idPadre = request.getParameter("idPadre");
  		if (idPadre == null || idPadre.isEmpty()) {
  			response.setStatus(HttpServletResponse.SC_BAD_REQUEST); //400
  			response.getWriter().println("Cartella padre mancante");
			return;
  		}
  		//Trasformo il parametro estratto in un intero
  		int idCartella = -1;
  		try {
  			idCartella = Integer.parseInt(idPadre);
  		} catch (NumberFormatException e) {
  			response.setStatus(HttpServletResponse.SC_BAD_REQUEST); //400
  			response.getWriter().println("Formato numerico errato");
			return;
  		}
  		
  		//Query: controllo che non ci sia già una cartella con quel nome per quell'utente
		CartellaDAO cDao = new CartellaDAO(connection);
		Cartella c = new Cartella();
		c = null;
		try {
			c = cDao.checkNomeCartella(proprietario, nomeCartella);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); //500
  			response.getWriter().println("Impossibile verificare nome cartella");
			return;
		}

		if (c == null) { //Se il nome della cartella è disponibile per quell'utente, allora la creo
	  		long milliseconds = System.currentTimeMillis();
			Date data = new Date(milliseconds);
			try {
				cDao.creaSottocartella(proprietario, nomeCartella, data, idCartella);
				
				response.setStatus(HttpServletResponse.SC_OK); //200
				response.getWriter().println("Sottocartella creata con successo");
			} catch (SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); //500
	  			response.getWriter().println("Impossibile creare la cartella");
				return;
			}
		} else { //Se invece esiste, mostra un messaggio di errore e ritorna alla pagina di gestione contenuti
			response.setStatus(HttpServletResponse.SC_CONFLICT); //409
  			response.getWriter().println("Nome esistente, riprova");
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