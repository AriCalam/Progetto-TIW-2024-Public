package servlet;

import java.io.IOException;         
import java.sql.Connection;
import java.sql.SQLException;
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

import beans.Utente;
import beans.Documento;
import dao.CartellaDAO;
import dao.DocumentoDAO;
import beans.Cartella;
import utils.ConnectionHandler;

@WebServlet("/GoToSpostamento")
public class GoToSpostamento extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	
	public GoToSpostamento() {
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
	
	//Mostra tutte le cartelle e le sottocartelle dell'utente
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		HttpSession session = request.getSession();
  		if (session.isNew() || session.getAttribute("utente") == null) {
			response.sendRedirect(getServletContext().getContextPath() + "/Login");
			return;
		}
		
  		//Estraggo dalla request il parametro idDoc
  		String id = request.getParameter("idDoc");
  		if (id == null || id.isEmpty()) {
  			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametro mancante");
  			return;
  		}
  		//Trasformo il parametro estratto in un intero
  		int idDoc = -1;
  		try {
  			idDoc = Integer.parseInt(id);
  		} catch (NumberFormatException e) {
  			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Formato numerico errato");
  			return;
  		}
  		
  		Utente utente = (Utente) session.getAttribute("utente");
  		DocumentoDAO docDAO = new DocumentoDAO(connection);
  		Documento doc = new Documento();
  		
  		//Cerco il documento selezionato tramite il suo id: se non è presente ritorno un errore
		try {
			doc = docDAO.findDocById(idDoc, utente.getUsername());
			if(doc == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Nessun documento con l'ID fornito");
				return;
			}
			
			//Se invece è presente, allora invio il risultato
			CartellaDAO cartellaDAO = new CartellaDAO(connection);
			List<Cartella> albero = null;
	  		albero = cartellaDAO.getAllCartelleFromUtente(utente.getUsername());
			
			Cartella padre = cartellaDAO.findCartellaById(doc.getIdPadre(), utente.getUsername());
			
			//Redirect alla home per spostamento caricando le cartelle da db
			String path = "/homeSpostamento.html";
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("cartelle", albero);
			ctx.setVariable("documento", doc);
			ctx.setVariable("padre", padre);
			templateEngine.process(path, ctx, response.getWriter());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Accesso al db fallito");
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