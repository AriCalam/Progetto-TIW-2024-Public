package servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import beans.Utente;
import beans.Cartella;
import dao.CartellaDAO;
import utils.ConnectionHandler;

@WebServlet("/EliminaCartella")
@MultipartConfig
public class EliminaCartella extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public EliminaCartella() {
		super();
	}

	// Crea la servlet
	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// Controlli iniziali
		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("utente") == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
			response.getWriter().println("Non sei loggato");
			return;
		} else if (connection == null) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
			response.getWriter().println("Impossibile connettersi con il database");
			return;
		}

		int idCartella = -1;
		Utente utente = (Utente) session.getAttribute("utente");
		String proprietario = utente.getUsername();
		String idCartString = null;

		//Nuova estrazione dei parametri dalla richiesta
		try {
			StringBuilder buffer = new StringBuilder();
			BufferedReader reader = request.getReader();
			String line;
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
				buffer.append(System.lineSeparator());
			}
			String data = buffer.toString();
			JsonObject ret = new JsonObject();
			ret = new Gson().fromJson(data, JsonObject.class);

			//Estraggo l'id della cartella
			idCartString = StringEscapeUtils.escapeJava(ret.get("idCartella").getAsString());
			if (idCartString == null || idCartString.isEmpty()) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
				response.getWriter().println("Id cartella mancante");
				return;
			}
			//Trasformo il parametro estratto in un intero
			try {
				idCartella = Integer.parseInt(idCartString);
			} catch (NumberFormatException e) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
				response.getWriter().println("Formato numerico errato");
				return;
			}

		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
			response.getWriter().println("Errore");
			response.setContentType("text/plain");
			response.setCharacterEncoding("UTF-8");
			return;
		}

		//Query 1: controllo che la cartella con l'id fornito sia nel database e che appartenga a quell'utente
		CartellaDAO cDao = new CartellaDAO(connection);
		Cartella c = new Cartella();
		c = null;
		try {
			c = cDao.findCartellaById(idCartella, proprietario);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
			response.getWriter().println("Impossibile verificare se la cartella ti appartiene");
			return;
		}

		// Reindirizzamento
		if (c == null) { //Se non ho trovato la cartella, allora non posso eliminarla
			// Reindirizzamento
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
			response.getWriter().println("La cartella con l'id fornito non ti appartiene o non esiste");
			return;
		} else { //Se l'ho trovata, invece, la elimino
			try {
				cDao.eliminaCartellaById(idCartella, proprietario);
				response.setStatus(HttpServletResponse.SC_OK);
			} catch (SQLException e1) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 401
				response.getWriter().println("Errore con il deleteElements");
				return;
			}
		}
	}

	// Distrugge la servlet
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
