/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.dipvvf.abr.app.corsivvf.model;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
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
    @NamedQuery(name = "Documento.findAll", query = "SELECT d FROM Documento d")
    , @NamedQuery(name = "Documento.findById", query = "SELECT d FROM Documento d WHERE d.id = :id")
    , @NamedQuery(name = "Documento.findByNomefile", query = "SELECT d FROM Documento d WHERE d.nomefile = :nomefile")
    , @NamedQuery(name = "Documento.findByDimensione", query = "SELECT d FROM Documento d WHERE d.dimensione = :dimensione")
    , @NamedQuery(name = "Documento.findByChecksum", query = "SELECT d FROM Documento d WHERE d.checksum = :checksum")
    , @NamedQuery(name = "Documento.findByUidRisorsa", query = "SELECT d FROM Documento d WHERE d.uidRisorsa = :uidRisorsa")})
public class Documento implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    private String nomefile;
    @Basic(optional = false)
    @NotNull
    private long dimensione;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 64)
    private String checksum;
    @Basic(optional = false)
    @NotNull
    @Lob
    private byte[] contenuto;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 36)
    @Column(name = "uid_risorsa")
    private String uidRisorsa;
    @JoinColumn(name = "id_categoria", referencedColumnName = "id")
    @ManyToOne
    private Categoria idCategoria;
    @JoinColumn(name = "id_corso", referencedColumnName = "id")
    @ManyToOne
    private Corso idCorso;

    public Documento() {
    }

    public Documento(Integer id) {
        this.id = id;
    }

    public Documento(Integer id, String nomefile, long dimensione, String checksum, byte[] contenuto, String uidRisorsa) {
        this.id = id;
        this.nomefile = nomefile;
        this.dimensione = dimensione;
        this.checksum = checksum;
        this.contenuto = contenuto;
        this.uidRisorsa = uidRisorsa;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNomefile() {
        return nomefile;
    }

    public void setNomefile(String nomefile) {
        this.nomefile = nomefile;
    }

    public long getDimensione() {
        return dimensione;
    }

    public void setDimensione(long dimensione) {
        this.dimensione = dimensione;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public byte[] getContenuto() {
        return contenuto;
    }

    public void setContenuto(byte[] contenuto) {
        this.contenuto = contenuto;
    }

    public String getUidRisorsa() {
        return uidRisorsa;
    }

    public void setUidRisorsa(String uidRisorsa) {
        this.uidRisorsa = uidRisorsa;
    }

    public Categoria getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Categoria idCategoria) {
        this.idCategoria = idCategoria;
    }

    public Corso getIdCorso() {
        return idCorso;
    }

    public void setIdCorso(Corso idCorso) {
        this.idCorso = idCorso;
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
        if (!(object instanceof Documento)) {
            return false;
        }
        Documento other = (Documento) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "it.dipvvf.abr.app.corsivvf.model.Documento[ id=" + id + " ]";
    }
    
}
