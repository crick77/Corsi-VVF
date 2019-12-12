/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.dipvvf.abr.app.corsivvf.model;

import java.io.Serializable;
import java.util.Date;
import javax.json.bind.annotation.JsonbTransient;
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
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author riccardo.iovenitti
 */
@Entity
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Installazione.findAll", query = "SELECT i FROM Installazione i")
    , @NamedQuery(name = "Installazione.findById", query = "SELECT i FROM Installazione i WHERE i.id = :id")
    , @NamedQuery(name = "Installazione.findByDataInstallazione", query = "SELECT i FROM Installazione i WHERE i.dataInstallazione = :dataInstallazione")})
public class Installazione implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Column(name = "data_installazione")
    @Temporal(TemporalType.DATE)
    private Date dataInstallazione;
    @JoinColumn(name = "id_corso", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Corso idCorso;
    @JoinColumn(name = "id_dispositivo", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Dispositivo idDispositivo;

    public Installazione() {
    }

    public Installazione(Integer id) {
        this.id = id;
    }

    public Installazione(Integer id, Date dataInstallazione) {
        this.id = id;
        this.dataInstallazione = dataInstallazione;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getDataInstallazione() {
        return dataInstallazione;
    }

    public void setDataInstallazione(Date dataInstallazione) {
        this.dataInstallazione = dataInstallazione;
    }

    public Corso getIdCorso() {
        return idCorso;
    }

    public void setIdCorso(Corso idCorso) {
        this.idCorso = idCorso;
    }

    @JsonbTransient
    public Dispositivo getIdDispositivo() {
        return idDispositivo;
    }

    public void setIdDispositivo(Dispositivo idDispositivo) {
        this.idDispositivo = idDispositivo;
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
        if (!(object instanceof Installazione)) {
            return false;
        }
        Installazione other = (Installazione) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "it.dipvvf.abr.app.corsivvf.model.Installazione[ id=" + id + " ]";
    }
    
}
