package servlet;

import java.io.IOException;         
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Pattern;

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

@WebServlet("/ControllaRegistrazione")
public class ControllaRegistrazione extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	
	public ControllaRegistrazione() {
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
  		String usr = null;
  		String email = null;
  		String pwd = null;
  		String ripeti = null;
  		
  		boolean lunghezzaCampi = false;
  		boolean noCorrisp = false;
  		boolean pwdCorta = false;
  		
  		//Estraggo dalla request i parametri e li traformo
  		try {
  			usr = StringEscapeUtils.escapeJava(request.getParameter("username"));
  			email = StringEscapeUtils.escapeJava(request.getParameter("email"));
  			pwd = StringEscapeUtils.escapeJava(request.getParameter("password1"));
  			ripeti = StringEscapeUtils.escapeJava(request.getParameter("password2"));
  			
  			if (usr == null || usr.isEmpty() || email == null || email.isEmpty() ||
  					pwd == null || pwd.isEmpty() || ripeti == null || ripeti.isEmpty()) {
				throw new Exception("Campo vuoto o mancante");
  			}
  			
  			if (!pwd.equals(ripeti))
				noCorrisp = true;
  			
  			if(usr.length()>16 || pwd.length()>16 || email.length()>45 || ripeti.length()>16
  					|| usr.length()<1 || pwd.length()<8 || email.length()<1 || ripeti.length()<8) {
  				lunghezzaCampi = true;
			}
  			
  			if(pwd.length()<8)
  				pwdCorta = true;
  			
  			if (!Pattern.compile("^\\w+([\\.-]?\\w+)*@\\w+([\\.-]?\\w+)*(\\.\\w{2,3})+$", Pattern.CASE_INSENSITIVE).matcher(email).find())
                throw new Exception("Formato email non valido");
  			
            email = email.toLowerCase();
  			
  		}  catch (Exception e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Nome cartella mancante");
			return;
		}
  		
  		if(lunghezzaCampi) {
  			String path1;
  			request.setAttribute("errReg", "Non hai rispettato la lunghezza minima/massima dei campi, riprova");
			path1 = "/GoToRegistrazione";
			request.getRequestDispatcher(path1).forward(request, response);
  		}
  		
  		else if(noCorrisp) {
  			String path1;
  			request.setAttribute("errReg", "Password e Ripeti password non corrispondono, riprova");
			path1 = "/GoToRegistrazione";
			request.getRequestDispatcher(path1).forward(request, response);
  		}
  		
  		else if(pwdCorta) {
  			String path1;
  			request.setAttribute("errReg", "Password troppo corta, riprova: usa almeno 8 caratteri");
			path1 = "/GoToRegistrazione";
			request.getRequestDispatcher(path1).forward(request, response);
  		}
  		
  		else {
  		//Query: controllo che non ci sia già un utente con quelle credenziali nel db
  			UtenteDAO uDao = new UtenteDAO(connection);
  			Utente u = new Utente();
  			u = null;
  			try {
  				u = uDao.checkCredenziali(usr, pwd);
  			} catch (SQLException e) {
  				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile verificare l'utente");
  				return;
  			}

  			//Query2: controllo che non ci sia già un utente con quell'username nel db
  			boolean isUnique = false;
  			try {
  				isUnique = uDao.checkUsername(usr);
  			} catch (SQLException e) {
  				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile verificare unicità");
  				return;
  			}
  			
  			//Reindirizzamento
  			String path;
  			if (u == null && isUnique) { //Se lo username è disponibile, allora crea l'utente e passo alla Home
  				try {
  					//Creo l'utente
  					uDao.creaUtente(usr, email, pwd);
  					//Reindirizzamento
  					request.setAttribute("okReg", "Utente creato con successo, accedi");
  					path = "/GoToLogin";
  					request.getRequestDispatcher(path).forward(request, response);
  				} catch (SQLException e) {
  					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile creare l'utente");
  				}
  			} else { //Se invece esiste, mostra un messaggio di errore e torna alla Registrazione
  				request.setAttribute("errReg", "Username non disponibile, prova con un altro");
  				path = "/GoToRegistrazione";
  				request.getRequestDispatcher(path).forward(request, response);
  			}  		
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