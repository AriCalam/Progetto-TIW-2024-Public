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
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import beans.Utente;
import dao.DocumentoDAO;
import beans.Documento;
import dao.CartellaDAO;
import beans.Cartella;
import utils.ConnectionHandler;

@WebServlet("/SpostaDoc")
public class SpostaDoc extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	
	public SpostaDoc() {
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
	
	//Cerca il documento e ne modifica le proprietà
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		HttpSession session = request.getSession();
  		if (session.isNew() || session.getAttribute("utente") == null) {
			response.sendRedirect(getServletContext().getContextPath() + "/Login");
			return;
		}
		
  		//Estraggo dalla request il parametro idDoc e lo trasformo
  		String id = request.getParameter("idDoc");
  		if (id == null || id.isEmpty()) {
  			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametro doc mancante");
  			return;
  		}
  		int idDoc = -1;
  		try {
  			idDoc = Integer.parseInt(id);
  		} catch (NumberFormatException e) {
  			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Formato numerico errato");
  			return;
  		}
  		
  		//Faccio lo stesso con l'idPadre della cartella in cui spostare il documento
  		String idP = request.getParameter("idPadre");
  		if (idP == null || idP.isEmpty()) {
  			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametro padre mancante");
  			return;
  		}
  		int idPadre = -1;
  		try {
  			idPadre = Integer.parseInt(idP);
  		} catch (NumberFormatException e) {
  			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Formato numerico errato");
  			return;
  		}
  		
  		Utente utente = (Utente) session.getAttribute("utente");
  		DocumentoDAO docDAO = new DocumentoDAO(connection);
  		Documento doc = new Documento();
  		CartellaDAO cartellaDAO = new CartellaDAO(connection);
  		Cartella cart = new Cartella();
		
  		//Cerca il documento nel db
		try {
			doc = docDAO.findDocById(idDoc, utente.getUsername());
			if(doc == null) { //Se il doc non è presente, allora mando un errore
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Nessun documento con l'ID fornito");
				return;
			}
			
			//Se invece è presente controllo che la cartella in cui voglio spostarlo sia presente
			cart = cartellaDAO.findCartellaById(idPadre, utente.getUsername());
			if(cart == null) { //Se il doc non è presente, allora mando un errore
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Nessuna cartella con l'ID fornito");
				return;
			}
			
			//Se esiste anche la cartella, allora lo sposto
			docDAO.spostaDoc(idDoc, idPadre, utente.getUsername());
			
			String path;
			path = getServletContext().getContextPath() + "/GoToHome";
			response.sendRedirect(path);
			
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile spostare documento");
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