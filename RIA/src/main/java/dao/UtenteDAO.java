package dao;

import beans.Utente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UtenteDAO {
	private Connection con;

	//Crea la connessione con il database
	public UtenteDAO(Connection connection) {
		this.con = connection;
	}

	//Crea un nuovo utente nel database
	public int creaUtente(String usr, String email, String pwd) throws SQLException {
		String query = "INSERT INTO Utente (username, email, password) VALUES(?,?,?)";
		int code = 0;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, usr);
			pstatement.setString(2, email);
			pstatement.setString(3, pwd);
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

	//Controlla unicità username
	public boolean checkUsername(String username) throws SQLException {
		String query = "SELECT username FROM Utente  WHERE username = ?";
		PreparedStatement pstatement = null;
		ResultSet result = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, username);
			result = pstatement.executeQuery();
			if (!result.isBeforeFirst()) //Ritorna true se non trova lo username nel database
				return true;
			else {
				return false;
			}
		} catch (SQLException e) {
			throw new SQLException(e);
		}
	}
	
	//Controlla che utente e password siano presenti nel database
	public Utente checkCredenziali(String usr, String pwd) throws SQLException {
		String query = "SELECT username, password FROM utente  WHERE username = ? AND password = ?";
		PreparedStatement pstatement = null;
		ResultSet result = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, usr);
			pstatement.setString(2, pwd);
			result = pstatement.executeQuery();
			if (!result.isBeforeFirst()) //Nessun risultato, check credenziali fallito
				return null;
			else {
				result.next();
				Utente utente = new Utente();
				utente.setUsername(result.getString("username"));
				return utente;
			}
		} catch (SQLException e) {
			throw new SQLException(e);
		}
	}
}