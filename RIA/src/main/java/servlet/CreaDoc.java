package servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
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

@WebServlet("/CreaDoc")
@MultipartConfig
public class CreaDoc extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	
	public CreaDoc() {
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
			response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
			return;			
		}
  		
  		String nomeDoc = null;
  		String sommario = null;
  		String tipo = null;
  		Utente utente = (Utente) session.getAttribute("utente");
  		String proprietario = null;
  		proprietario = utente.getUsername();
  		String padre = null;
  		int idCartella = -1;
  		
  		//boolean campiTroppoLunghi = false;
  		  
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

            nomeDoc = StringEscapeUtils.escapeJava(ret.get("nomeDocumento").getAsString());
            tipo = StringEscapeUtils.escapeJava(ret.get("tipo").getAsString());
            sommario = StringEscapeUtils.escapeJava(ret.get("sommario").getAsString());
            
            padre = StringEscapeUtils.escapeJava(ret.get("idPadre").getAsString());
            if (padre == null || padre.isEmpty()) {
      			response.setStatus(HttpServletResponse.SC_BAD_REQUEST); //400
    	  			response.getWriter().println("Cartella padre mancante");
    				return;
      		}
      		//Trasformo il parametro estratto in un intero
      		try {
      			idCartella = Integer.parseInt(padre);
      		} catch (NumberFormatException e) {
      			response.setStatus(HttpServletResponse.SC_BAD_REQUEST); //400
      			response.getWriter().println("Formato numerico errato");
    			return;
      		}
            
            if (nomeDoc == null || tipo == null || sommario == null) {
            	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      			response.getWriter().println("Campi mancanti");
    			return;
            }
                
            if (nomeDoc.isEmpty() || tipo.isEmpty() || sommario.isEmpty()) {
            	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      			response.getWriter().println("Campi vuoti");
    			return;
            }
            
            if(nomeDoc.length()>16 || tipo.length()>5 || sommario.length()>140 ||
            		nomeDoc.length()<1 || tipo.length()<1 || sommario.length()<1) {
            	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      			response.getWriter().println("Non hai rispettato la lunghezza dei campi");
    			return;
            }
  		} catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Errore");
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            return;
        }
  		
  		//Query: controllo che non ci sia già un documento con quel nome per quell'utente
		DocumentoDAO dDao = new DocumentoDAO(connection);
		Documento d = new Documento();
		d = null;
		try {
			d = dDao.checkNome2(proprietario, nomeDoc);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); //500
  			response.getWriter().println("Impossibile verificare nome documento");
			return;
		}
		
		//Query: creazione
		if (d == null) { //Se non esiste già un doc con quel nome per quell'utente allora lo creo
	  		long milliseconds = System.currentTimeMillis();
			Date data = new Date(milliseconds);
			try {
				dDao.creaDocumento(proprietario, nomeDoc, data, sommario, tipo, idCartella);
				
				response.setStatus(HttpServletResponse.SC_OK); //200
				response.getWriter().println("Documento creato con successo");
				
			} catch (SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); //500
	  			response.getWriter().println("Impossibile creare il documento");
				return;
			}
		} else { //Se invece esiste, mostra un messaggio di errore e ritorna alla pagina di gestione contenuti
			response.setStatus(HttpServletResponse.SC_CONFLICT); //409
  			response.getWriter().println("Nome esistente, riprova");
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