/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.dipvvf.abr.app.corsivvf.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author ospite
 */
@Entity
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Delta.findAll", query = "SELECT d FROM Delta d")
    , @NamedQuery(name = "Delta.findById", query = "SELECT d FROM Delta d WHERE d.id = :id")
    , @NamedQuery(name = "Delta.findByDataSincronizzazione", query = "SELECT d FROM Delta d WHERE d.dataSincronizzazione = :dataSincronizzazione")
    , @NamedQuery(name = "Delta.findByTipologia", query = "SELECT d FROM Delta d WHERE d.tipologia = :tipologia")
    , @NamedQuery(name = "Delta.findByRisorsa", query = "SELECT d FROM Delta d WHERE d.risorsa = :risorsa")
    , @NamedQuery(name = "Delta.findByMd5", query = "SELECT d FROM Delta d WHERE d.md5 = :md5")
    , @NamedQuery(name = "Delta.findByOrdine", query = "SELECT d FROM Delta d WHERE d.ordine = :ordine")
    , @NamedQuery(name = "Delta.findByStato", query = "SELECT d FROM Delta d WHERE d.stato = :stato")
    , @NamedQuery(name = "Delta.findByOperazione", query = "SELECT d FROM Delta d WHERE d.operazione = :operazione")
    , @NamedQuery(name = "Delta.findByUidRisorsaPadre", query = "SELECT d FROM Delta d WHERE d.uidRisorsaPadre = :uidRisorsaPadre")
    , @NamedQuery(name = "Delta.findByDimensione", query = "SELECT d FROM Delta d WHERE d.dimensione = :dimensione")
    , @NamedQuery(name = "Delta.findByTipoRisorsaPadre", query = "SELECT d FROM Delta d WHERE d.tipoRisorsaPadre = :tipoRisorsaPadre")
    , @NamedQuery(name = "Delta.findByUidRisorsa", query = "SELECT d FROM Delta d WHERE d.uidRisorsa = :uidRisorsa")})
public class Delta implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Column(name = "data_sincronizzazione")
    @Temporal(TemporalType.DATE)
    private Date dataSincronizzazione;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 32)
    private String tipologia;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    private String risorsa;
    @Size(max = 64)
    private String md5;
    @Basic(optional = false)
    @NotNull
    private int ordine;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 16)
    private String stato;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 16)
    private String operazione;
    @Size(max = 36)
    @Column(name = "uid_risorsa_padre")
    private String uidRisorsaPadre;
    @Basic(optional = false)
    @NotNull
    private long dimensione;
    @Size(max = 255)
    @Column(name = "tipo_risorsa_padre")
    private String tipoRisorsaPadre;
    @Size(max = 36)
    @Column(name = "uid_risorsa")
    private String uidRisorsa;
    @JoinColumn(name = "id_sincronizzazione", referencedColumnName = "id")
    @ManyToOne
    private Sincronizzazione idSincronizzazione;

    public Delta() {
    }

    public Delta(Integer id) {
        this.id = id;
    }

    public Delta(Integer id, Date dataSincronizzazione, String tipologia, String risorsa, int ordine, String stato, String operazione, long dimensione) {
        this.id = id;
        this.dataSincronizzazione = dataSincronizzazione;
        this.tipologia = tipologia;
        this.risorsa = risorsa;
        this.ordine = ordine;
        this.stato = stato;
        this.operazione = operazione;
        this.dimensione = dimensione;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getDataSincronizzazione() {
        return dataSincronizzazione;
    }

    public void setDataSincronizzazione(Date dataSincronizzazione) {
        this.dataSincronizzazione = dataSincronizzazione;
    }

    public String getTipologia() {
        return tipologia;
    }

    public void setTipologia(String tipologia) {
        this.tipologia = tipologia;
    }

    public String getRisorsa() {
        return risorsa;
    }

    public void setRisorsa(String risorsa) {
        this.risorsa = risorsa;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public int getOrdine() {
        return ordine;
    }

    public void setOrdine(int ordine) {
        this.ordine = ordine;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

    public String getOperazione() {
        return operazione;
    }

    public void setOperazione(String operazione) {
        this.operazione = operazione;
    }

    public String getUidRisorsaPadre() {
        return uidRisorsaPadre;
    }

    public void setUidRisorsaPadre(String uidRisorsaPadre) {
        this.uidRisorsaPadre = uidRisorsaPadre;
    }

    public long getDimensione() {
        return dimensione;
    }

    public void setDimensione(long dimensione) {
        this.dimensione = dimensione;
    }

    public String getTipoRisorsaPadre() {
        return tipoRisorsaPadre;
    }

    public void setTipoRisorsaPadre(String tipoRisorsaPadre) {
        this.tipoRisorsaPadre = tipoRisorsaPadre;
    }

    public String getUidRisorsa() {
        return uidRisorsa;
    }

    public void setUidRisorsa(String uidRisorsa) {
        this.uidRisorsa = uidRisorsa;
    }

    public Sincronizzazione getIdSincronizzazione() {
        return idSincronizzazione;
    }

    public void setIdSincronizzazione(Sincronizzazione idSincronizzazione) {
        this.idSincronizzazione = idSincronizzazione;
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
        if (!(object instanceof Delta)) {
            return false;
        }
        Delta other = (Delta) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "it.dipvvf.abr.app.corsivvf.model.Delta[ id=" + id + " ]";
    }
    
}
