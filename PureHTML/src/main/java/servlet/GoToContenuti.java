package servlet;

import java.io.IOException;         
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

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
	private TemplateEngine templateEngine;
	
	public GoToContenuti() {
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
	
	//Mostra tutte le sottocartelle e i documenti inseriti nella cartella selezionata dall'utente
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		HttpSession session = request.getSession();
  		if (session.isNew() || session.getAttribute("utente") == null) {
			response.sendRedirect(getServletContext().getContextPath() + "/Login");
			return;
		}
  		
  		int idFinale;
  		String idCartella1 = null;
  		int idCartella;
  		if(request.getAttribute("idCartella") != null) {
  			idCartella1 = String.valueOf(request.getAttribute("idCartella"));
  			try {
  	  			idFinale = Integer.parseInt(idCartella1);
  	  		} catch (NumberFormatException e) {
  	  			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Formato numerico errato");
  	  			return;
  	  		}
  			idCartella = idFinale;
  		}
  		else {
  			//Estraggo dalla request della Home il parametro idCartella
  	  		String id = request.getParameter("idCartella");
  	  		if (id == null || id.isEmpty()) {
  	  			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametro id cartella mancante");
  	  			return;
  	  		}
  	  		//Trasformo il parametro estratto in un intero
  	  		int actualId = -1;
  	  		try {
  	  			actualId = Integer.parseInt(id);
  	  		} catch (NumberFormatException e) {
  	  			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Formato numerico errato");
  	  			return;
  	  		}
  	  		idCartella = actualId;
  		}
  		
  		//Creo i DAO necessari
  		Utente utente = (Utente) session.getAttribute("utente");
  		CartellaDAO cartellaDAO = new CartellaDAO(connection);
  		List<Cartella> cartelle = new ArrayList<Cartella>();
  		DocumentoDAO docDAO = new DocumentoDAO(connection);
  		List<Documento> documenti = new ArrayList<Documento>();
		
  		//Cerco la cartella selezionata tramite il suo id: se non è presente ritorno un errore
		try {
			Cartella c = cartellaDAO.findCartellaById(idCartella, utente.getUsername());
			if(c == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Nessuna cartella con l'ID fornito");
				return;
			}
			
			//Se invece è presente, allora cerco le sue sottocartelle e i suoi documenti
			cartelle = cartellaDAO.findSottocartelleByPadre(idCartella, utente.getUsername());
			documenti = docDAO.findDocByCartella(idCartella, utente.getUsername());
			
			//Invio il risultato
			String path = "/contenuti.html";
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("cartella", c.getNomeCartella());
			ctx.setVariable("sottocartelle", cartelle);
			ctx.setVariable("documenti", documenti);
			templateEngine.process(path, ctx, response.getWriter());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Accesso al db fallito");
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