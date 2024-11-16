package dao;

import beans.Cartella;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class CartellaDAO {
	private Connection con;

	// Crea la connessione con il database
	public CartellaDAO(Connection connection) {
		this.con = connection;
	}

	// Cerca nel db una cartella tramite il suo id
	public Cartella findCartellaById(int id, String usr) throws SQLException {
		Cartella c = null;
		String query = "SELECT * FROM cartella WHERE idCartella = ? AND proprietario = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, id);
			pstatement.setString(2, usr);
			result = pstatement.executeQuery();
			while (result.next()) {
				c = new Cartella();
				c.setIdCartella(result.getInt("idCartella"));
				c.setProprietario(result.getString("proprietario"));
				c.setNomeCartella(result.getString("nomeCartella"));
				c.setDataCreazione(result.getDate("dataCreazione"));
				c.setIdPadre(result.getInt("idPadre"));
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
		return c;
	}

	//Cerca tutte le cartelle e sottocartelle di un utente,
	//in modo da avere una lista unica di tutto, senza usare l'attributo 'sottocartelle' del bean Cartella
	public List<Cartella> getAllCartelleByUtenteSingleList(String usr) throws SQLException {
		List<Cartella> cartelle = new ArrayList<Cartella>();
		String query = "SELECT * FROM cartella WHERE proprietario = ? ORDER BY nomeCartella";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, usr);
			result = pstatement.executeQuery();
			while (result.next()) {
				Cartella c = new Cartella();
				c.setIdCartella(result.getInt("idCartella"));
				c.setProprietario(result.getString("proprietario"));
				c.setNomeCartella(result.getString("nomeCartella"));
				c.setIdPadre(result.getInt("idPadre"));
				cartelle.add(c);
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
		return cartelle;
	}
	
	//Cerca nel db le sottocartelle (figli diretti) di una cartella padre
	public List<Cartella> findSottocartelleByPadre(int idPadre, String usr) throws SQLException {
		List<Cartella> cartelle = new ArrayList<Cartella>();
		String query = "SELECT * FROM cartella WHERE proprietario = ? AND idPadre = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, usr);
			pstatement.setInt(2, idPadre);
			result = pstatement.executeQuery();
			while (result.next()) {
				Cartella c = new Cartella();
				c.setIdCartella(result.getInt("idCartella"));
				// c.setProprietario(result.getString("proprietario"));
				c.setNomeCartella(result.getString("nomeCartella"));
				// c.setDataCreazione(result.getDate("dataCreazione"));
				c.setIdPadre(result.getInt("idPadre"));
				cartelle.add(c);
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
		return cartelle;
	}

	//Controlla se una cartella con un certo nome è già presente nel db di un certo utente
	public Cartella checkNomeCartella(String usr, String nome) throws SQLException {
		String query = "SELECT nomeCartella FROM cartella WHERE proprietario = ? AND nomeCartella = ?";
		PreparedStatement pstatement = null;
		ResultSet result = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, usr);
			pstatement.setString(2, nome);
			result = pstatement.executeQuery();
			if (!result.isBeforeFirst()) // Nessuna cartella trovata
				return null;
			else { // Cartella trovata
				result.next();
				Cartella c = new Cartella();
				c.setNomeCartella(result.getString("nomeCartella"));
				return c;
			}
		} catch (SQLException e) {
			throw new SQLException(e);
		}
	}

	//Crea una cartella di primo livello nel db
	public int creaCartella(String prop, String nome, Date dataCreazione) throws SQLException {
		String query = "INSERT INTO cartella (proprietario, nomeCartella, dataCreazione) VALUES(?,?,?)";
		int code = 0;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, prop);
			pstatement.setString(2, nome);
			pstatement.setDate(3, dataCreazione);
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

	//Crea una sottocartella nel db
	public int creaSottocartella(String prop, String nome, Date dataCreazione, int idPadre) throws SQLException {
		String query = "INSERT INTO cartella (proprietario, nomeCartella, dataCreazione, idPadre) VALUES(?,?,?,?)";
		int code = 0;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, prop);
			pstatement.setString(2, nome);
			pstatement.setDate(3, dataCreazione);
			pstatement.setInt(4, idPadre);
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

	//Crea una lista con tutte le cartelle di un certo utente (utilizzando il metodo trovaFigli)
	public List<Cartella> getAllCartelleFromUtente(String username) {
		List<Cartella> cartelle = new ArrayList<>();
		String query = "SELECT * FROM cartella WHERE idPadre IS NULL AND proprietario = ?;";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, username);
			result = pstatement.executeQuery();
			int i = 0;
			while (result.next()) {
				i++;
				Cartella c = new Cartella();
				c.setIdCartella(result.getInt("idCartella"));
				c.setProprietario(result.getString("proprietario"));
				c.setNomeCartella(result.getString("nomeCartella"));
				c.setDataCreazione(result.getDate("dataCreazione"));
				c.setIdPadre(result.getInt("idPadre"));
				c.setGerarchia(Integer.toString(i));
				trovaFigli(c, username, Integer.toString(i));
				cartelle.add(c);
			}
		} catch (SQLException e) {
		}
		return cartelle;
	}

	//Metodo ricorsivo per trovare tutte le sottocartelle (figli di ogni livello) di una cartella
	private void trovaFigli(Cartella padre, String username, String marker) throws SQLException {
		int idPadre = padre.getIdCartella();
		try {
			PreparedStatement pstatement = con.prepareStatement("SELECT * FROM cartella WHERE idPadre = ?"
					+ " AND proprietario = ?;");
			pstatement.setInt(1, idPadre);
			pstatement.setString(2, username);
			ResultSet result = pstatement.executeQuery();
			int i = 0;
			while (result.next()) {
				i++;
				String newMarker = marker + Integer.toString(i);
				Cartella c = new Cartella();
				c.setIdCartella(result.getInt("idCartella"));
				c.setProprietario(result.getString("proprietario"));
				c.setNomeCartella(result.getString("nomeCartella"));
				c.setDataCreazione(result.getDate("dataCreazione"));
				c.setIdPadre(result.getInt("idPadre"));
				c.setGerarchia(newMarker);
				trovaFigli(c, username, newMarker);
				padre.getSottocartelle().add(c);
			}
		} catch (SQLException e) {
		}
	}

	//Elimina una cartella, compresa delle sue sottocartelle e documenti
	public void eliminaCartellaById(int id, String username) throws SQLException {
		String query = "DELETE FROM cartella WHERE proprietario = ? AND idCartella = ?";
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, username);
			pstatement.setInt(2, id);
			pstatement.executeUpdate();
		} finally {
			if (pstatement != null) {
				pstatement.close();
			}
		}
	}
}