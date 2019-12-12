/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.dipvvf.abr.app.corsivvf.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author riccardo.iovenitti
 */
@Entity
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Corso.findAll", query = "SELECT c FROM Corso c")
    , @NamedQuery(name = "Corso.findById", query = "SELECT c FROM Corso c WHERE c.id = :id")
    , @NamedQuery(name = "Corso.findByTitolo", query = "SELECT c FROM Corso c WHERE c.titolo = :titolo")
    , @NamedQuery(name = "Corso.findByDescrizione", query = "SELECT c FROM Corso c WHERE c.descrizione = :descrizione")
    , @NamedQuery(name = "Corso.findByDataCreazione", query = "SELECT c FROM Corso c WHERE c.dataCreazione = :dataCreazione")
    , @NamedQuery(name = "Corso.findByDataAggiornamento", query = "SELECT c FROM Corso c WHERE c.dataAggiornamento = :dataAggiornamento")
    , @NamedQuery(name = "Corso.findByTipologia", query = "SELECT c FROM Corso c WHERE c.tipologia = :tipologia")
    , @NamedQuery(name = "Corso.findByNote", query = "SELECT c FROM Corso c WHERE c.note = :note")
    , @NamedQuery(name = "Corso.findByAbilitato", query = "SELECT c FROM Corso c WHERE c.abilitato = :abilitato")})
public class Corso implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 64)
    private String titolo;
    @Size(max = 240)
    private String descrizione;
    @Basic(optional = false)
    @NotNull
    @Column(name = "data_creazione")
    @Temporal(TemporalType.DATE)
    private Date dataCreazione;
    @Basic(optional = false)
    @NotNull
    @Column(name = "data_aggiornamento")
    @Temporal(TemporalType.DATE)
    private Date dataAggiornamento;
    @Size(max = 32)
    private String tipologia;
    @Size(max = 512)
    private String note;
    @Basic(optional = false)
    @NotNull
    private boolean abilitato;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idCorso")
    private Collection<Installazione> installazioneCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idCorso")
    private Collection<Categoria> categoriaCollection;
    @OneToMany(mappedBy = "idCorso")
    private Collection<Documento> documentoCollection;

    public Corso() {
    }

    public Corso(Integer id) {
        this.id = id;
    }

    public Corso(Integer id, String titolo, Date dataCreazione, Date dataAggiornamento, boolean abilitato) {
        this.id = id;
        this.titolo = titolo;
        this.dataCreazione = dataCreazione;
        this.dataAggiornamento = dataAggiornamento;
        this.abilitato = abilitato;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public Date getDataCreazione() {
        return dataCreazione;
    }

    public void setDataCreazione(Date dataCreazione) {
        this.dataCreazione = dataCreazione;
    }

    public Date getDataAggiornamento() {
        return dataAggiornamento;
    }

    public void setDataAggiornamento(Date dataAggiornamento) {
        this.dataAggiornamento = dataAggiornamento;
    }

    public String getTipologia() {
        return tipologia;
    }

    public void setTipologia(String tipologia) {
        this.tipologia = tipologia;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean getAbilitato() {
        return abilitato;
    }

    public void setAbilitato(boolean abilitato) {
        this.abilitato = abilitato;
    }

    @XmlTransient
    @JsonbTransient
    public Collection<Installazione> getInstallazioneCollection() {
        return installazioneCollection;
    }

    public void setInstallazioneCollection(Collection<Installazione> installazioneCollection) {
        this.installazioneCollection = installazioneCollection;
    }

    @XmlTransient
    @JsonbTransient
    public Collection<Categoria> getCategoriaCollection() {
        return categoriaCollection;
    }

    public void setCategoriaCollection(Collection<Categoria> categoriaCollection) {
        this.categoriaCollection = categoriaCollection;
    }

    @XmlTransient
    @JsonbTransient
    public Collection<Documento> getDocumentoCollection() {
        return documentoCollection;
    }

    public void setDocumentoCollection(Collection<Documento> documentoCollection) {
        this.documentoCollection = documentoCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Corso)) {
            return false;
        }
        Corso other = (Corso) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "it.dipvvf.abr.app.corsivvf.model.Corso[ id=" + id + " ]";
    }
    
}
