package dao;

import beans.Documento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class DocumentoDAO {
	private Connection con;

	//Crea la connessione con il database
	public DocumentoDAO(Connection connection) {
		this.con = connection;
	}
	
	//Cerca nel db tutti i documenti in una certa cartella
	public List<Documento> findDocByCartella(int idPadre, String usr) throws SQLException {
		List<Documento> docs = new ArrayList<Documento>();
		String query = "SELECT * FROM new_documento WHERE idPadre = ? AND proprietario = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, idPadre);
			pstatement.setString(2, usr);
			result = pstatement.executeQuery();
			while (result.next()) {
				Documento d = new Documento();
				d.setIdDocumento(result.getInt("idDocumento"));
				//d.setProprietario(result.getString("proprietario"));
				d.setNomeDocumento(result.getString("nomeDocumento"));
				//d.setDataCreazione(result.getDate("dataCreazione"));
				//d.setSommario(result.getString("sommario"));
				d.setTipo(result.getString("tipo"));
				d.setIdPadre(result.getInt("idPadre"));
				docs.add(d);
			}
		} catch (SQLException e) {
			throw new SQLException(e);
		} finally {
			try {
				if (result != null) {
					result.close();
				}
			} catch (Exception e1) {
				throw new SQLException("Cannot close result");
			}
			try {
				if (pstatement != null) {
					pstatement.close();
				}
			} catch (Exception e1) {
				throw new SQLException("Cannot close statement");
			}
		}
		return docs;
	}
	
	//Cerca nel db un documento tramite il suo id
	public Documento findDocById(int idDoc, String usr) throws SQLException {
		Documento d = null;
		String query = "SELECT * FROM new_documento WHERE idDocumento = ? AND proprietario = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, idDoc);
			pstatement.setString(2, usr);
			result = pstatement.executeQuery();
			while (result.next()) {
				d = new Documento();
				d.setIdDocumento(result.getInt("idDocumento"));
				d.setProprietario(result.getString("proprietario"));
				d.setNomeDocumento(result.getString("nomeDocumento"));
				d.setDataCreazione(result.getDate("dataCreazione"));
				d.setSommario(result.getString("sommario"));
				d.setTipo(result.getString("tipo"));
				d.setIdPadre(result.getInt("idPadre"));
			}
		} catch (SQLException e) {
			throw new SQLException(e);
		} finally {
			try {
				if (result != null) {
					result.close();
				}
			} catch (Exception e1) {
				throw new SQLException("Cannot close result");
			}
			try {
				if (pstatement != null) {
					pstatement.close();
				}
			} catch (Exception e1) {
				throw new SQLException("Cannot close statement");
			}
		}
		return d;
	}
	
	//Sposta un documento nella cartella selezionata
	public int spostaDoc(int idDoc, int idCartella, String usr) throws SQLException {
		String query = "UPDATE new_documento SET idPadre = ? WHERE idDocumento = ? AND proprietario = ?;";
		int code = 0;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, idCartella);
			pstatement.setInt(2, idDoc);
			pstatement.setString(3, usr);
			code = pstatement.executeUpdate();
		} catch (SQLException e) {
			throw new SQLException(e);
		} finally {
			try {
				if (pstatement != null) {
					pstatement.close();
				}
			} catch (Exception e1) {
				throw new SQLException("Cannot close statement");
			}
		}
		return code;
	}
	
	//Controlla se un documento con quel nome è già presente nel database di quell'utente
	public Documento checkNomeDoc(String usr, String nome) throws SQLException {
		String query = "SELECT nomeDocumento FROM new_documento WHERE proprietario = ? AND nomeDocumento = ?";
		PreparedStatement pstatement = null;
		ResultSet result = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, usr);
			pstatement.setString(2, nome);
			result = pstatement.executeQuery();
			if (!result.isBeforeFirst()) //Nessun risultato, nessun documento trovato
				return null;
			else { //Documento trovato
				result.next();
				Documento d = new Documento();
				d.setNomeDocumento(result.getString("nomeDocumento"));
				return d;
			}
		} catch (SQLException e) {
			throw new SQLException(e);
		}
	}
	
	//Crea un nuovo documento nel db
	public int creaDocumento(String prop, String nome, Date dataCreaz, String sommario, String tipo, int cartellaPadre) throws SQLException {
		String query = "INSERT INTO new_documento (proprietario, nomeDocumento, dataCreazione, sommario, tipo, idPadre)"
				+ " VALUES(?,?,?,?,?,?)";
		int code = 0;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, prop);
			pstatement.setString(2, nome);
			pstatement.setDate(3, dataCreaz);
			pstatement.setString(4, sommario);
			pstatement.setString(5, tipo);
			pstatement.setInt(6, cartellaPadre);
			code = pstatement.executeUpdate();
		} catch (SQLException e) {
			throw new SQLException(e);
		} finally {
			try {
				if (pstatement != null) {
					pstatement.close();
				}
			} catch (Exception e1) {
				throw new SQLException("Cannot close statement");
			}
		}
		return code;
	}
}