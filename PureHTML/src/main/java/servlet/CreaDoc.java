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
import beans.Documento;
import dao.DocumentoDAO;
import utils.ConnectionHandler;

@WebServlet("/CreaDoc")
public class CreaDoc extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	
	public CreaDoc() {
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
  		
  		String nomeDoc = null;
  		String sommario = null;
  		String tipo = null;
  		Utente utente = (Utente) session.getAttribute("utente");
  		String proprietario = null;
  		proprietario = utente.getUsername();
  		
  		//Estraggo dalla request i parametri String e li trasformo
  		try {
  			nomeDoc = StringEscapeUtils.escapeJava(request.getParameter("nomeDocumento"));
  			sommario = StringEscapeUtils.escapeJava(request.getParameter("sommario"));
  			tipo = StringEscapeUtils.escapeJava(request.getParameter("tipo"));
  			
  			if (nomeDoc == null || nomeDoc.isEmpty() || sommario == null || sommario.isEmpty() ||
  					tipo == null || tipo.isEmpty()) {
				throw new Exception("Parametro vuoto o mancante");
			}
  			
  			if(nomeDoc.length()>16 || sommario.length()>140 || tipo.length()>5 || nomeDoc.length()<1 || sommario.length()<1 || tipo.length()<1) {
  				String path1;
  	  			request.setAttribute("LunghezzaD", "Non hai rispettato la lunghezza minima/massima dei campi, riprova");
  				path1 = "/GoToGestioneContenuti";
  				request.getRequestDispatcher(path1).forward(request, response);
			}
  		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametri mancanti");
			return;
		}
  		
  		//Estraggo dalla request il parametro idPadre
  		String idPadre = request.getParameter("idPadre");
  		if (idPadre == null || idPadre.isEmpty()) {
  			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametro mancante");
  			return;
  		}
  		//Trasformo il parametro estratto in un intero
  		int idCartella = -1;
  		try {
  			idCartella = Integer.parseInt(idPadre);
  		} catch (NumberFormatException e) {
  			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Formato numerico errato");
  			return;
  		}
  		
  		//Query: controllo che non ci sia già un documento con quel nome per quell'utente
		DocumentoDAO dDao = new DocumentoDAO(connection);
		Documento d = new Documento();
		d = null;
		try {
			d = dDao.checkNomeDoc(proprietario, nomeDoc);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile verificare il nome del doc");
			return;
		}

		//Reindirizzamento
		String path;
		if (d == null) {
	  		long milliseconds = System.currentTimeMillis();
			Date data = new Date(milliseconds);
			try {
				//Creo il documento
				dDao.creaDocumento(proprietario, nomeDoc, data, sommario, tipo, idCartella);
				
				//Reindirizzamento
				request.setAttribute("DocCreato", "Documento creato con successo");
				path = "/GoToGestioneContenuti";
				request.getRequestDispatcher(path).forward(request, response);
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile creare il documento");
			}
		} else { //Se invece esiste, mostra un messaggio di errore e ritorna alla pagina di gestione contenuti
			//Reindirizzamento
			request.setAttribute("DocEsistente", "Documento già esistente");
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

