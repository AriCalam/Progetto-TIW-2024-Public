package servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

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

	public SpostaDoc() {
		super();
	}

	// Crea la servlet
	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	// Cerca il documento e ne modifica le proprietà
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		//Controlli iniziali
		HttpSession session = request.getSession();
		if(session.isNew() || session.getAttribute("utente")==null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); //401
			response.getWriter().println("Non sei loggato");
			return;
		}
		else if(connection == null) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); //500
			response.getWriter().println("Impossibile connettersi con il database");
			return;			
		}

		String idDocumentoStr;
		String padre;
		int idCartella;
		int idDoc;

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

			//Estraggo l'id del documento
			idDocumentoStr = StringEscapeUtils.escapeJava(ret.get("idDocumento").getAsString());
			if (idDocumentoStr == null || idDocumentoStr.isEmpty()) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
				response.getWriter().println("Id documento mancante");
				return;
			}
			//Trasformo il parametro estratto in un intero
			try {
				idDoc = Integer.parseInt(idDocumentoStr);
			} catch (NumberFormatException e) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
				response.getWriter().println("Formato numerico errato");
				return;
			}

			//Estraggo l'id della cartella padre in cui va inserito
			padre = StringEscapeUtils.escapeJava(ret.get("idPadre").getAsString());
			if (padre == null || padre.isEmpty()) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
				response.getWriter().println("Cartella padre mancante");
				return;
			}
			//Trasformo il parametro estratto in un intero
			try {
				idCartella = Integer.parseInt(padre);
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

		Utente utente = (Utente) session.getAttribute("utente");
		DocumentoDAO docDAO = new DocumentoDAO(connection);
		Documento doc = new Documento();
		CartellaDAO cartellaDAO = new CartellaDAO(connection);
		Cartella cart = new Cartella();

		//Cerca il documento nel db
		try {
			doc = docDAO.findDocById(idDoc, utente.getUsername());
			if (doc == null) { //Se il doc non è presente, allora mando un errore
				response.setStatus(HttpServletResponse.SC_NOT_FOUND); // 404
				response.getWriter().println("Nessun docuemnto con l'id fornito");
				return;
			}

			//Se invece è presente controllo che la cartella in cui voglio spostarlo sia presente
			cart = cartellaDAO.findCartellaById(idCartella, utente.getUsername());
			if (cart == null) { // Se il doc non è presente, allora mando un errore
				response.setStatus(HttpServletResponse.SC_NOT_FOUND); // 404
				response.getWriter().println("Nessuna cartella con l'id fornito");
				return;
			}

			//Se esiste anche la cartella, allora lo sposto
			docDAO.spostaDoc(idDoc, idCartella, utente.getUsername());

			response.setStatus(HttpServletResponse.SC_OK); // 200
			response.getWriter().println("Documento spostato con successo");
			return;
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 404
			response.getWriter().println("Impossibile spostare il documento");
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