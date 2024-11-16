package servlet;

import java.io.IOException;         
import java.sql.Connection;
import java.sql.SQLException;

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
import dao.DocumentoDAO;
import beans.Documento;
import beans.Cartella;
import beans.Utente;
import utils.ConnectionHandler;

@WebServlet("/GoToDocPage")
public class GoToDocPage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	
	public GoToDocPage() {
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
	
	//Mostra i dettagli del documento selezionato
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
			
			int idPadre = doc.getIdPadre();
			CartellaDAO cDAO = new CartellaDAO(connection);
			Cartella c = new Cartella();
			c = cDAO.findCartellaById(idPadre, utente.getUsername());
			
			//Se invece è presente, allora invio il risultato
			String path = "/docPage.html";
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("documento", doc);
			ctx.setVariable("cartellaPadre", c);
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