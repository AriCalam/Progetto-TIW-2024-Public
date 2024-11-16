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

import beans.Utente;
import dao.CartellaDAO;
import beans.Cartella;
import utils.ConnectionHandler;

@WebServlet("/GoToGestioneContenuti")
public class GoToGestioneContenuti extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	
	public GoToGestioneContenuti() {
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
	
	//Permette all'utente di andare alla pagina di aggiunta di cartelle o documenti
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		HttpSession session = request.getSession();
  		if (session.isNew() || session.getAttribute("utente") == null) {
			response.sendRedirect(getServletContext().getContextPath() + "/login.html");
			return;
		}
  		
  		Utente utente = (Utente) session.getAttribute("utente");
  		CartellaDAO cartellaDAO = new CartellaDAO(connection);
  		List<Cartella> cartelle = new ArrayList<Cartella>();
		
		try {
			cartelle = cartellaDAO.getAllCartelleByUtenteSingleList(utente.getUsername());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile trovare cartelle");
			return;
		}
		
		//Redirect alla pagina caricando le cartelle da db
		String path = "/gestioneContenuti.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		
		ctx.setVariable("cartelle", cartelle);
		
		if(request.getAttribute("CartellaEsistente") != null)
			ctx.setVariable("errCrea", request.getAttribute("CartellaEsistente"));
		
		if(request.getAttribute("CartellaCreata") != null)
			ctx.setVariable("okCrea", request.getAttribute("CartellaCreata"));
		
		if(request.getAttribute("DocEsistente") != null)
			ctx.setVariable("errCreaD", request.getAttribute("DocEsistente"));
		
		if(request.getAttribute("DocCreato") != null)
			ctx.setVariable("okCreaD", request.getAttribute("DocCreato"));
		
		if(request.getAttribute("SottocartellaEsistente") != null)
			ctx.setVariable("errCreaS", request.getAttribute("SottocartellaEsistente"));
		
		if(request.getAttribute("SottocartellaCreata") != null)
			ctx.setVariable("okCreaS", request.getAttribute("SottocartellaCreata"));
	
		if(request.getAttribute("Lunghezza") != null)
			ctx.setVariable("errCrea", request.getAttribute("Lunghezza"));
		
		if(request.getAttribute("LunghezzaS") != null)
			ctx.setVariable("errCreaS", request.getAttribute("LunghezzaS"));
		
		if(request.getAttribute("LunghezzaD") != null)
			ctx.setVariable("errCreaD", request.getAttribute("LunghezzaD"));
		
		templateEngine.process(path, ctx, response.getWriter());	
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