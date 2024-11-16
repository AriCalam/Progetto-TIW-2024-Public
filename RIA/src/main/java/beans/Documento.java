package beans;

import java.util.Date;

public class Documento {
	private int idDocumento;
	private String proprietario;
	private String nomeDocumento;
	private Date dataCreazione; //Formato: ggmmaaaa:hhmm (Sarà vero?????)
	private String sommario;
	private String tipo;
	private int idPadre;
	
	public int getIdDocumento() {
		return idDocumento;
	}
	
	public String getProprietario() {
		return proprietario;
	}
	
	public String getNomeDocumento() {
		return nomeDocumento;
	}
	
	public Date getDataCreazione() {
		return dataCreazione;
	}
	
	public String getSommario() {
		return sommario;
	}
	
	public String getTipo() {
		return tipo;
	}
	
	public int getIdPadre() {
		return idPadre;
	}
	
	public void setIdDocumento(int idDocumento) {
		this.idDocumento = idDocumento;
	}
	
	public void setProprietario(String proprietario) {
		this.proprietario = proprietario;
	}
	
	public void setNomeDocumento(String nome) {
		this.nomeDocumento = nome;
	}
	
	public void setDataCreazione(Date dataCreazione) {
		this.dataCreazione = dataCreazione;
	}
	
	public void setSommario(String sommario) {
		this.sommario = sommario;
	}
	
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	
	public void setIdPadre(int idPadre) {
		this.idPadre = idPadre;
	}
}