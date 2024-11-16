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

import org.apache.commons.lang.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import beans.Utente;
import dao.UtenteDAO;
import utils.ConnectionHandler;

@WebServlet("/ControllaLogin")
public class ControllaLogin extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	
	public ControllaLogin() {
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
	
	/*
	 * Crea due stringhe (usrn e pwd) a cui assegna i valori inseriti dall'utente, controlla che non siano vuoti/nulli
	 * e poi crea un oggetto UtenteDAO per fare una query SQL controllando che nel db siano presenti le credenziali
	 * inserite. Se non sono presenti manda errore.
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	
		String usrn = null;
		String pwd = null;
		
		try { //Controlla che i valori inseriti non siano vuoti o nulli
			usrn = StringEscapeUtils.escapeJava(request.getParameter("username"));
			pwd = StringEscapeUtils.escapeJava(request.getParameter("password1"));
			
			if (usrn == null || pwd == null || usrn.isEmpty() || pwd.isEmpty()) {
				throw new Exception("Credenziali vuote o mancanti");
			}
			
			if(usrn.length()>16 || pwd.length()>16 || usrn.length()<1 || pwd.length()<8) {
				String path1;
	  			request.setAttribute("errLogin", "Non hai rispettato la lunghezza minima/massima dei campi. Riprova.");
				path1 = "/GoToLogin";
				request.getRequestDispatcher(path1).forward(request, response);
				return;
			}
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Credenziali mancanti");
			return;
		}

		//Query
		UtenteDAO userDao = new UtenteDAO(connection);
		Utente user = null;
		try {
			user = userDao.checkCredenziali(usrn, pwd);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile verificare le credenziali");
			return;
		}

		//Reindirizzamento
		String path;
		if (user == null) { //Se l'utente non esiste, mostra un msg di errore e ritorna alla pagina di login
			String path1;
  			request.setAttribute("errLogin", "L'utente non esiste, riprova o registrati");
			path1 = "/GoToLogin";
			request.getRequestDispatcher(path1).forward(request, response);
		} else { //Se invece esiste, aggigungi le info alla sessione e reindirizza l'utente alla home page
			
			//Creo la sessione chiamando la request, il metodo getSession e settando l'attributo "utente" con
			//l'oggetto utente
			request.getSession().setAttribute("utente", user);
			path = getServletContext().getContextPath() + "/GoToHome";
			response.sendRedirect(path);
			//In questo modo faccio il reindirizzamento alla socket GoToHome, che ha il compito di estrarre le
			//info dal db per caricare la home page
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