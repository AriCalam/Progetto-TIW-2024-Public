package servlet;

import java.io.IOException;         
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import beans.Utente;
import beans.Cartella;
import dao.CartellaDAO;
import utils.ConnectionHandler;

@WebServlet("/CreaCartella")
public class CreaCartella extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	
	public CreaCartella() {
		super();
	}
	
	//Crea la servlet
	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		HttpSession session = request.getSession();
  		if (session.isNew() || session.getAttribute("utente") == null) {
			response.sendRedirect(getServletContext().getContextPath() + "/Login");
			return;
		}
  		
  		String nomeCartella = null;
  		Utente utente = (Utente) session.getAttribute("utente");
  		String proprietario = null;
  		proprietario = utente.getUsername();
  		
  		boolean lunghezzaCampi = false;
  		
  		//Estraggo dalla request i parametri e li traformo
  		try {
  			nomeCartella = StringEscapeUtils.escapeJava(request.getParameter("nomeCartella"));
  			
  			if (nomeCartella == null || nomeCartella.isEmpty()) {
				throw new Exception("Nome vuoto o mancante");
			}
  			
  			if(nomeCartella.length()>16 || nomeCartella.length() < 1) {
				lunghezzaCampi = true;
			}
  			
  		}  catch (Exception e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Errore nome cartella");
			return;
		}
  		
  		if(lunghezzaCampi) {
  			String path1;
  			request.setAttribute("Lunghezza", "Non hai rispettato la lunghezza minima/massima dei campi, riprova");
			path1 = "/GoToGestioneContenuti";
			request.getRequestDispatcher(path1).forward(request, response);
  		}
  		
  		//Query: controllo che non ci sia già una cartella con quel nome per quell'utente
		CartellaDAO cDao = new CartellaDAO(connection);
		Cartella c = new Cartella();
		c = null;
		try {
			c = cDao.checkNomeCartella(proprietario, nomeCartella);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile verificare il nome della cartella");
			return;
		}

		//Reindirizzamento
		String path;
		if (c == null) { //Se la cartella non esiste, crea quella nuova, mostra un msg di successo e ritorna
								//alla pagina di gestione contenuti
			
	  		long milliseconds = System.currentTimeMillis();
			Date data = new Date(milliseconds);
			try {
				cDao.creaCartella(proprietario, nomeCartella, data);
				
				request.setAttribute("CartellaCreata", "Cartella creata con successo");
				path = "/GoToGestioneContenuti";
				request.getRequestDispatcher(path).forward(request, response);
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile creare la cartella");
			}
		} else { //Se invece esiste, mostra un messaggio di errore e ritorna alla pagina di gestione contenuti
			request.setAttribute("CartellaEsistente", "Cartella già esistente");
			path = "/GoToGestioneContenuti";
			request.getRequestDispatcher(path).forward(request, response);
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
