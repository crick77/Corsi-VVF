/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.dipvvf.abr.app.corsivvf.model;

import java.io.Serializable;
import java.util.Collection;
import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
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
    @NamedQuery(name = "Dispositivo.findAll", query = "SELECT d FROM Dispositivo d")
    , @NamedQuery(name = "Dispositivo.findById", query = "SELECT d FROM Dispositivo d WHERE d.id = :id")
    , @NamedQuery(name = "Dispositivo.findByMarca", query = "SELECT d FROM Dispositivo d WHERE d.marca = :marca")
    , @NamedQuery(name = "Dispositivo.findByModello", query = "SELECT d FROM Dispositivo d WHERE d.modello = :modello")
    , @NamedQuery(name = "Dispositivo.findBySerialnr", query = "SELECT d FROM Dispositivo d WHERE d.serialnr = :serialnr")
    , @NamedQuery(name = "Dispositivo.findByInventario", query = "SELECT d FROM Dispositivo d WHERE d.inventario = :inventario")
    , @NamedQuery(name = "Dispositivo.findByNote", query = "SELECT d FROM Dispositivo d WHERE d.note = :note")
    , @NamedQuery(name = "Dispositivo.findByAbilitato", query = "SELECT d FROM Dispositivo d WHERE d.abilitato = :abilitato")
    , @NamedQuery(name = "Dispositivo.findByDeviceid", query = "SELECT d FROM Dispositivo d WHERE d.deviceid = :deviceid")
    , @NamedQuery(name = "Dispositivo.findByToken", query = "SELECT d FROM Dispositivo d WHERE d.token = :token")})
public class Dispositivo implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 64)
    private String marca;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 2147483647)
    private String modello;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 64)
    private String serialnr;
    @Size(max = 32)
    private String inventario;
    @Size(max = 512)
    private String note;
    @Basic(optional = false)
    @NotNull
    private boolean abilitato;
    @Size(max = 256)
    private String deviceid;
    @Size(max = 256)
    private String token;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idDispositivo")
    private Collection<Installazione> installazioneCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idDispositivo")
    private Collection<Delta> deltaCollection;

    public Dispositivo() {
    }

    public Dispositivo(Integer id) {
        this.id = id;
    }

    public Dispositivo(Integer id, String marca, String modello, String serialnr, boolean abilitato) {
        this.id = id;
        this.marca = marca;
        this.modello = modello;
        this.serialnr = serialnr;
        this.abilitato = abilitato;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModello() {
        return modello;
    }

    public void setModello(String modello) {
        this.modello = modello;
    }

    public String getSerialnr() {
        return serialnr;
    }

    public void setSerialnr(String serialnr) {
        this.serialnr = serialnr;
    }

    public String getInventario() {
        return inventario;
    }

    public void setInventario(String inventario) {
        this.inventario = inventario;
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

    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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
        if (!(object instanceof Dispositivo)) {
            return false;
        }
        Dispositivo other = (Dispositivo) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "it.dipvvf.abr.app.corsivvf.model.Dispositivo[ id=" + id + " ]";
    }
    
}
