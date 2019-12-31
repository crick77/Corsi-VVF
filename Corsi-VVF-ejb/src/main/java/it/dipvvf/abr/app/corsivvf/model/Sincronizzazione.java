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
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
 * @author ospite
 */
@Entity
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Sincronizzazione.findAll", query = "SELECT s FROM Sincronizzazione s")})
public class Sincronizzazione implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataora;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 32)
    private String stato;
    @JoinColumn(name = "id_installazione", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Installazione idInstallazione;
    @OneToMany(mappedBy = "idSincronizzazione")
    private Collection<Delta> deltaCollection;

    public Sincronizzazione() {
    }

    public Sincronizzazione(Integer id) {
        this.id = id;
    }

    public Sincronizzazione(Integer id, Date dataora, String stato) {
        this.id = id;
        this.dataora = dataora;
        this.stato = stato;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getDataora() {
        return dataora;
    }

    public void setDataora(Date dataora) {
        this.dataora = dataora;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

    @XmlTransient
    @JsonbTransient
    public Installazione getIdInstallazione() {
        return idInstallazione;
    }

    public void setIdInstallazione(Installazione idInstallazione) {
        this.idInstallazione = idInstallazione;
    }

    @XmlTransient
    @JsonbTransient
    public Collection<Delta> getDeltaCollection() {
        return deltaCollection;
    }

    public void setDeltaCollection(Collection<Delta> deltaCollection) {
        this.deltaCollection = deltaCollection;
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
        if (!(object instanceof Sincronizzazione)) {
            return false;
        }
        Sincronizzazione other = (Sincronizzazione) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "it.dipvvf.abr.app.corsivvf.model.Sincronizzazione[ id=" + id + " ]";
    }
    
}
