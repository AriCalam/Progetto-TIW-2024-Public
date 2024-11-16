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
import beans.Documento;
import dao.DocumentoDAO;
import utils.ConnectionHandler;

@WebServlet("/EliminaDoc")
@MultipartConfig
public class EliminaDoc extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	
	public EliminaDoc() {
		super();
	}
	
	//Crea la servlet
	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}
	
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
  		
  		int idDoc = -1;
  		String idDocString = null;
  		Utente utente = (Utente) session.getAttribute("utente");
  		String proprietario = utente.getUsername();
  		
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
            idDocString = StringEscapeUtils.escapeJava(ret.get("idDoc").getAsString());
            if (idDocString == null || idDocString.isEmpty()) {
      			response.setStatus(HttpServletResponse.SC_BAD_REQUEST); //400
    	  			response.getWriter().println("Id documento mancante");
    				return;
      		}
      		//Trasformo il parametro estratto in un intero
      		try {
      			idDoc = Integer.parseInt(idDocString);
      		} catch (NumberFormatException e) {
      			response.setStatus(HttpServletResponse.SC_BAD_REQUEST); //400
      			response.getWriter().println("Formato numerico errato");
    			return;
      		}
            
  		} catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); //400
            response.getWriter().println("Errore");
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            return;
        }
  		
  		//Query: controllo che il documento con l'id fornito sia nel database e appartenga a quell'utente
		DocumentoDAO dDao = new DocumentoDAO(connection);
		Documento d = new Documento();
		d = null;
		try {
			d = dDao.findDocById(idDoc, proprietario);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); //500
  			response.getWriter().println("Impossibile verificare se il documento ti appartiene");
			return;
		}

		//Reindirizzamento
		if (d == null) { //Se non ho trovato il documento, allora non posso eliminarlo
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); //401
			response.getWriter().println("Il documento con l'id fornito non ti appartiene o non esiste");
			return;
		} else { //Se l'ho trovato, invece, lo elimino
			try {
				dDao.eliminaDocById(idDoc, proprietario);
			} catch (SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); //401
				response.getWriter().println("Errore durante l'eliminazione");
				return;
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

