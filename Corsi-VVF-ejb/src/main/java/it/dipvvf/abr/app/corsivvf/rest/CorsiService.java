/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.dipvvf.abr.app.corsivvf.rest;

import it.dipvvf.abr.app.corsivvf.bean.Checksum;
import it.dipvvf.abr.app.corsivvf.ejb.BaseService;
import it.dipvvf.abr.app.corsivvf.ejb.MiscServices;
import it.dipvvf.abr.app.corsivvf.model.Categoria;
import it.dipvvf.abr.app.corsivvf.model.Corso;
import it.dipvvf.abr.app.corsivvf.model.Documento;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;
import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Stateless;
import javax.ejb.LocalBean;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author riccardo.iovenitti
 */
@Stateless
@LocalBean
@Path("courses")
@Produces(MediaType.APPLICATION_JSON)
public class CorsiService extends BaseService {
    @PersistenceContext
    EntityManager em;
    @Resource
    EJBContext ctx;
    @Inject
    MiscServices ms;
    

    @GET
    public Response getCorsi(@Context UriInfo info) {
        List<Integer> lCorsi = em.createQuery("select c.id from Corso c").getResultList();
        if (lCorsi.isEmpty()) {
            return Response.noContent().build();
        } else {
            return Response.ok(resourcesToURI(info, lCorsi)).build();
        }
    }

    @GET
    @Path("{id}")
    public Response getDettaglioCorso(@PathParam("id") Integer id) {
        Corso c = em.find(Corso.class, id);
        return (c != null) ? Response.ok(c).build() : Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path("{id}/categories")
    public Response getCategorieCorso(@Context UriInfo info, @PathParam("id") Integer id) {
        Corso c = em.find(Corso.class, id);
        return (c != null)
                ? Response.ok(resourcesToURI(info, em.createQuery("SELECT c.id from Categoria c WHERE c.idCorso = :id").setParameter("id", c).getResultList())).build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path("{id}/categories/{idcat}")
    public Response getDettaglioCategoriaCorso(@PathParam("id") Integer idCorso, @PathParam("idcat") Integer idCategoria) {
        Corso corso = em.find(Corso.class, idCorso);
        if (corso == null) {
            Response.status(Response.Status.NOT_FOUND).build();
        }

        Categoria cat = em.find(Categoria.class, idCategoria);
        if (cat == null) {
            Response.status(Response.Status.NOT_FOUND).build();
        }

        List<Categoria> lCat = em.createQuery("SELECT c from Categoria c WHERE c.idCorso = :idCorso AND c.id = :idCategoria")
                .setParameter("idCorso", corso)
                .setParameter("idCategoria", idCategoria)
                .getResultList();

        return lCat.isEmpty() ? Response.status(Response.Status.NOT_FOUND).build() : Response.ok(lCat.get(0)).build();
    }

    @GET
    @Path("{id}/documents")
    public Response getDocumentiCorso(@PathParam("id") Integer id, @Context UriInfo info) {
        Corso c = em.find(Corso.class, id);
        return (c != null)
                ? Response.ok(resourcesToURI(info, em.createQuery("SELECT d.id from Documento d WHERE d.idCorso = :id").setParameter("id", c).getResultList())).build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path("{id}/documents/{iddoc}")
    public Response getDettaglioDocumentoCorso(@PathParam("id") Integer idCorso, @PathParam("iddoc") Integer idDoc) {
        Corso corso = em.find(Corso.class, idCorso);
        if (corso == null) {
            Response.status(Response.Status.NOT_FOUND).build();
        }

        Documento doc = em.find(Documento.class, idDoc);
        if (doc == null) {
            Response.status(Response.Status.NOT_FOUND).build();
        }

        List<Documento> lDoc = em.createQuery("SELECT d from Documento d WHERE d.idCorso = :idCorso AND d.id = :idDoc")
                .setParameter("idCorso", corso)
                .setParameter("idDoc", idDoc)
                .getResultList();

        return lDoc.isEmpty() ? Response.status(Response.Status.NOT_FOUND).build() : Response.ok(lDoc.get(0)).build();
    }

    @GET
    @Path("{id}/documents/{iddoc}/data")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getDatiDocumentoCorso(@PathParam("id") Integer idCorso, @PathParam("iddoc") Integer idDoc) {
        Corso corso = em.find(Corso.class, idCorso);
        if (corso == null) {
            Response.status(Response.Status.NOT_FOUND).build();
        }

        Documento doc = em.find(Documento.class, idDoc);
        if (doc == null) {
            Response.status(Response.Status.NOT_FOUND).build();
        }

        doc = em.createQuery("SELECT d from Documento d WHERE d.idCorso = :idCorso AND d.id = :idDoc", Documento.class)
                .setParameter("idCorso", corso)
                .setParameter("idDoc", idDoc)
                .getSingleResult();

        return (doc==null) ? Response.status(Response.Status.NOT_FOUND).build() : downloadFile(doc.getNomefile(), doc.getContenuto());
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addNewCourse(Corso corso, @Context UriInfo info) {
        int count = em.createQuery("SELECT c FROM Corso c where c.titolo = :titolo OR c.id = :id")
                .setParameter("titolo", corso.getTitolo())
                .setParameter("id", corso.getId())
                .getResultList()
                .size();

        if (count > 0) {
            return Response.status(Response.Status.CONFLICT).build();
        }

        try {
            corso.setAbilitato(false);
            corso.setId(null);
            corso.setUidRisorsa(ms.generateUID());
            em.persist(corso);
            em.flush();
        } catch (Exception e) {
            ctx.setRollbackOnly();
            return Response.status(Response.Status.BAD_REQUEST).entity(e.toString()).build();
        }

        UriBuilder builder = info.getAbsolutePathBuilder();
        builder.path(Integer.toString(corso.getId()));

        return Response.created(builder.build()).build();
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateCourse(@PathParam("id") Integer id, Corso corso) {
        corso.setId(id);
        try {
            em.merge(corso);
        } catch (Exception e) {
            ctx.setRollbackOnly();
            return Response.status(Response.Status.BAD_REQUEST).entity(e.toString()).build();
        }

        return Response.noContent().build();
    }

    @DELETE
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteCourse(@PathParam("id") Integer id) {
        Corso c = em.find(Corso.class, id);
        int count = em.createQuery("SELECT COUNT(c.id) FROM Categoria c WHERE c.idCorso = :idC", Integer.class).setParameter("idC", c).getSingleResult();
        count += em.createQuery("SELECT COUNT(d.id) FROM Documento d WHERE d.idCorso = :idC", Integer.class).setParameter("idC", c).getSingleResult();
        count += em.createQuery("SELECT COUNT(i.id) FROM Installazione i WHERE i.idCorso = :idC", Integer.class).setParameter("idC", c).getSingleResult();

        if (count > 0) {
            return Response.status(Response.Status.CONFLICT).entity("Corso con elementi collegati. Eliminare prima tali collegamenti e riprovare.").build();
        }

        try {
            em.remove(c);
        } catch (Exception e) {
            ctx.setRollbackOnly();
            return Response.status(Response.Status.CONFLICT).entity(e.toString()).build();
        }

        return Response.noContent().build();
    }

    @PUT
    @Path("{id}/categories/{idcat}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateCategory(@PathParam("id") Integer id, @PathParam("idcat") Integer idCat, Categoria categoria) {
        Corso c = em.find(Corso.class, id);

        if (c == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        categoria.setId(idCat);
        categoria.setIdCorso(c);
        try {
            em.merge(categoria);
        } catch (Exception e) {
            ctx.setRollbackOnly();
            return Response.status(Response.Status.BAD_REQUEST).entity(e.toString()).build();
        }

        return Response.noContent().build();
    }

    @DELETE
    @Path("{id}/categories/{idcat}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteCategory(@PathParam("id") Integer id, @PathParam("idcat") Integer idCat) {
        Corso corso = em.find(Corso.class, id);

        if (corso == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Categoria cat = em.createQuery("SELECT c from Categoria c WHERE c.id = :idCat AND c.idCorso = :idCorso", Categoria.class)
                .setParameter("idCat", idCat)
                .setParameter("idCorso", corso)
                .getSingleResult();

        int count = em.createQuery("SELECT COUNT(d.id) FROM Documento d WHERE d.idCorso = :idC", Integer.class).setParameter("idC", corso).getSingleResult();

        if (count > 0) {
            return Response.status(Response.Status.CONFLICT).entity("Corso con elementi collegati. Eliminare prima tali collegamenti e riprovare.").build();
        }

        try {
            em.remove(cat);
        } catch (Exception e) {
            ctx.setRollbackOnly();
            return Response.status(Response.Status.CONFLICT).entity(e.toString()).build();
        }

        return Response.noContent().build();
    }

    @POST
    @Path("{id}/categories")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addNewCategory(@PathParam("id") Integer id, Categoria cat, @Context UriInfo info) {
        Corso c = em.find(Corso.class, id);

        if (c == null) {
            return Response.status(Response.Status.PRECONDITION_FAILED).build();
        }

        try {
            cat.setIdCorso(c);
            cat.setUidRisorsa(ms.generateUID());
            em.persist(cat);
            em.flush();
        } catch (Exception e) {
            ctx.setRollbackOnly();
            return Response.status(Response.Status.PRECONDITION_FAILED).entity(e.toString()).build();
        }

        UriBuilder builder = info.getAbsolutePathBuilder();
        builder.path(Integer.toString(cat.getId()));

        return Response.created(builder.build()).build();
    }

    @POST
    @Path("{id}/documents")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadCourseDocument(@PathParam("id") Integer id, @Context HttpServletRequest req, @Context UriInfo info) {
        try {
            System.out.println("uploading to ID=" + id + " File=" + req.getParts());
        }
        catch(IOException | ServletException e) {            
        }
    
        Corso corso = em.find(Corso.class, id);
        if (corso == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        Part documentData;
        try {
            documentData = req.getPart("document");
            if (documentData == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }                

            Checksum cs = new Checksum();
            final byte[] bytes;
            try (InputStream in = documentData.getInputStream()) {
                bytes = new byte[(int)documentData.getSize()];
                in.read(bytes);
            }
            cs.update(bytes);

            documentData.delete();
                
            Documento doc = new Documento();
            doc.setIdCorso(corso);
            doc.setNomefile(Paths.get(documentData.getSubmittedFileName()).getFileName().toString());
            doc.setDimensione(bytes.length);
            doc.setChecksum(cs.getCheckum());
            doc.setContenuto(bytes);
            doc.setUidRisorsa(ms.generateUID());
            
            em.persist(doc);
            em.flush();
            
            return Response.ok(resourceToURI(info, doc.getId())).build();
        }
        catch(IOException | ServletException e) {
            ctx.setRollbackOnly();
            return Response.status(Response.Status.PRECONDITION_FAILED).entity(e.toString()).build();
        }                                
    }
    
    @POST
    @Path("{id}/categories/{idcat}/documents")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadCategoryDocument(@PathParam("id") Integer id, @PathParam("id") Integer idCat, @Context HttpServletRequest req, @Context UriInfo info) {
        try {
            System.out.println("uploading to ID=" + id + " File=" + req.getParts());
        }
        catch(IOException | ServletException e) {            
        }
    
        Corso corso = em.find(Corso.class, id);
        if (corso == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
                
        Categoria cat = em.createQuery("SELECT c FROM Categoria c WHERE c.id = :idCat AND c.idCorso = :idCorso", Categoria.class)
                                .setParameter("idCat", idCat)
                                .setParameter("idCorso", corso)
                                .getSingleResult();
        if (cat == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        Part documentData;
        try {
            documentData = req.getPart("document");
            if (documentData == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }                

            Checksum cs = new Checksum();
            final byte[] bytes;
            try (InputStream in = documentData.getInputStream()) {
                bytes = new byte[(int)documentData.getSize()];
                in.read(bytes);
            }
            cs.update(bytes);

            documentData.delete();
                
            Documento doc = new Documento();
            doc.setIdCategoria(cat);
            doc.setNomefile(Paths.get(documentData.getSubmittedFileName()).getFileName().toString());
            doc.setDimensione(bytes.length);
            doc.setChecksum(cs.getCheckum());
            doc.setContenuto(bytes);
            doc.setUidRisorsa(ms.generateUID());
            
            em.persist(doc);
            em.flush();
            
            return Response.ok(resourceToURI(info, doc.getId())).build();
        }
        catch(IOException | ServletException e) {
            ctx.setRollbackOnly();
            return Response.status(Response.Status.PRECONDITION_FAILED).entity(e.toString()).build();
        }                                
    }
    
    @GET
    @Path("{id}/categories/{idcat}/documents")
    public Response getDocumentiCategoria(@PathParam("id") Integer idCorso, @PathParam("idcat") Integer idCat, @Context UriInfo info) {
        Corso corso = em.find(Corso.class, idCorso);
        if (corso == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Categoria cat = em.createQuery("SELECT c FROM Categoria c WHERE c.id = :idCat AND c.idCorso = :idCorso", Categoria.class)
                            .setParameter("idCat", idCat)
                            .setParameter("idCorso", corso)
                            .getSingleResult();
        if (cat == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
               
        List<Integer> lDoc = em.createQuery("SELECT d.id FROM Documento d WHERE d.idCategoria = :idcat")
                                .setParameter("idcat", cat)
                                .getResultList();
        
        return (lDoc.isEmpty()) ? Response.status(Response.Status.NOT_FOUND).build() : Response.ok(resourcesToURI(info, lDoc)).build();
    }
    
    @GET
    @Path("{id}/categories/{idcat}/documents/{iddoc}")
    public Response getDocumentoCategoria(@PathParam("id") Integer idCorso, @PathParam("idcat") Integer idCat, @PathParam("iddoc") Integer idDoc) {
        Corso corso = em.find(Corso.class, idCorso);
        if (corso == null) {
            Response.status(Response.Status.NOT_FOUND).build();
        }

        Categoria cat = em.createQuery("SELECT c FROM Categoria c WHERE c.id = :idCat AND c.idCorso = :idCorso", Categoria.class)
                            .setParameter("idCat", idCat)
                            .setParameter("idCorso", corso)
                            .getSingleResult();
        if (cat == null) {
            Response.status(Response.Status.NOT_FOUND).build();
        }
           
        try {
            Documento doc = em.createQuery("SELECT d from Documento d WHERE d.idCategoria = :idCat AND d.id = :idDoc", Documento.class)
                    .setParameter("idCat", cat)
                    .setParameter("idDoc", idDoc)
                    .getSingleResult();
            
            return Response.ok(doc).build();
        }
        catch(NoResultException nre) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
    
    @GET
    @Path("{id}/categories/{idcat}/documents/{iddoc}/data")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getDatiDocumentoCategoria(@PathParam("id") Integer idCorso, @PathParam("idcat") Integer idCat, @PathParam("iddoc") Integer idDoc) {
        Corso corso = em.find(Corso.class, idCorso);
        if (corso == null) {
            Response.status(Response.Status.NOT_FOUND).build();
        }

        Categoria cat = em.createQuery("SELECT c FROM Categoria c WHERE c.id = :idCat AND c.idCorso = :idCorso", Categoria.class)
                            .setParameter("idCat", idCat)
                            .setParameter("idCorso", corso)
                            .getSingleResult();
        if (cat == null) {
            Response.status(Response.Status.NOT_FOUND).build();
        }
               
        Documento doc = em.createQuery("SELECT d from Documento d WHERE d.idCategoria = :idCat AND d.id = :idDoc", Documento.class)
                .setParameter("idCat", cat)
                .setParameter("idDoc", idDoc)
                .getSingleResult();

        return (doc==null) ? Response.status(Response.Status.NOT_FOUND).build() : downloadFile(doc.getNomefile(), doc.getContenuto());
    }
}
