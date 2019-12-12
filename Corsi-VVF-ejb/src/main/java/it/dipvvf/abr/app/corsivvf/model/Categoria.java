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
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
    @NamedQuery(name = "Categoria.findAll", query = "SELECT c FROM Categoria c")
    , @NamedQuery(name = "Categoria.findById", query = "SELECT c FROM Categoria c WHERE c.id = :id")
    , @NamedQuery(name = "Categoria.findByNome", query = "SELECT c FROM Categoria c WHERE c.nome = :nome")
    , @NamedQuery(name = "Categoria.findByDescrizione", query = "SELECT c FROM Categoria c WHERE c.descrizione = :descrizione")})
public class Categoria implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 32)
    private String nome;
    @Size(max = 250)
    private String descrizione;
    @JoinColumn(name = "id_corso", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Corso idCorso;
    @OneToMany(mappedBy = "idCategoria")
    private Collection<Documento> documentoCollection;

    public Categoria() {
    }

    public Categoria(Integer id) {
        this.id = id;
    }

    public Categoria(Integer id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    @JsonbTransient
    public Corso getIdCorso() {
        return idCorso;
    }

    public void setIdCorso(Corso idCorso) {
        this.idCorso = idCorso;
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
        if (!(object instanceof Categoria)) {
            return false;
        }
        Categoria other = (Categoria) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "it.dipvvf.abr.app.corsivvf.model.Categoria[ id=" + id + " ]";
    }
    
}
