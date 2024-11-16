package beans;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Cartella {
	private int idCartella;
	private String proprietario;
	private String nomeCartella;
	private Date dataCreazione;
	private int idPadre;
	private List<Cartella> sottocartelle = new ArrayList<>();
	private String gerarchia;

	public int getIdCartella() {
		return idCartella;
	}
	
	public String getProprietario() {
		return proprietario;
	}
	
	public String getNomeCartella() {
		return nomeCartella;
	}
	
	public Date getDataCreazione() {
		return dataCreazione;
	}
	
	public int getIdPadre() {
		return idPadre;
	}
	
	public List<Cartella> getSottocartelle() {
		return sottocartelle;
	}
	
	public String getGerarchia() {
		return gerarchia;
	}
	
	public void setIdCartella(int idCartella) {
		this.idCartella = idCartella;
	}
	
	public void setProprietario(String proprietario) {
		this.proprietario = proprietario;
	}
	
	public void setNomeCartella(String nomeCartella) {
		this.nomeCartella = nomeCartella;
	}
	
	public void setDataCreazione(Date dataCreazione) {
		this.dataCreazione = dataCreazione;
	}
	
	public void setIdPadre(int padre) {
		this.idPadre = padre;
	}
	
	public void setGerarchia(String gerarchia) {
		this.gerarchia = gerarchia;
	}
	
	public void setSottocartelle(List<Cartella> sottocartelle) {
		this.sottocartelle = sottocartelle;
	}
}