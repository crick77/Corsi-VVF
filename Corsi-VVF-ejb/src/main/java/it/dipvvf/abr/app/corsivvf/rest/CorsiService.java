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
import it.dipvvf.abr.app.corsivvf.model.DeltaConst;
import it.dipvvf.abr.app.corsivvf.model.Documento;
import it.dipvvf.abr.app.corsivvf.rest.security.JWTSecurityCheck;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Date;
import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Stateless;
import javax.ejb.LocalBean;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
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
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author riccardo.iovenitti
 */
@Stateless
@LocalBean
@Path("courses")
@Produces(MediaType.APPLICATION_JSON)
@JWTSecurityCheck
public class CorsiService extends BaseService {
    @PersistenceContext
    EntityManager em;
    @Resource
    EJBContext ctx;
    @Inject
    MiscServices ms;
    @Inject
    DeltaService ds;
    
    /*********************************************************
    *
    *                     GESTIONE CORSO
    *
    **********************************************************/

    /**
     * 
     * @param info
     * @return 
     */
    @GET
    public Response getCourses(@Context UriInfo info) {
        return ok(resourcesToURI(info, em.createQuery("select c.id from Corso c").getResultList()));
    }

    /**
     * 
     * @param id
     * @return 
     */
    @GET
    @Path("{id: \\d+}")
    public Response getCourseDetail(@PathParam("id") int id) {
        Corso c = em.find(Corso.class, id);
        return (c != null) ? ok(c) : notFound();
    }

    /**
     * 
     * @param corso
     * @param info
     * @return 
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addNewCourse(Corso corso, @Context UriInfo info) {
        long count = em.createQuery("SELECT COUNT(c) FROM Corso c where c.titolo = :titolo OR c.id = :id", Long.class)
                .setParameter("titolo", corso.getTitolo())
                .setParameter("id", corso.getId())
                .getSingleResult();

        if (count > 0) {
            return conflict();
        }

        try {
            corso.setId(null);
            corso.setUidRisorsa(ms.generateUID());
            em.persist(corso);
            em.flush();
            
            return created(resourceToURI(info, corso.getId()));
        } 
        catch(PersistenceException pe) {
            ctx.setRollbackOnly();
            return conflict(pe.toString());
        }
        catch(Exception e) {
            ctx.setRollbackOnly();
            return error(e.toString());
        }
    }
    
    /**
     * 
     * @param id
     * @param corso
     * @return 
     */
    @PUT
    @Path("{id: \\d+}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateCourse(@PathParam("id") int id, Corso corso) {
        try {
            corso.setId(id);
            corso.setDataAggiornamento(new Date());
            em.merge(corso);
            em.flush();
            
            return noContent();
        }
        catch(PersistenceException pe) {
            ctx.setRollbackOnly();
            return conflict(pe.toString());
        }
        catch(Exception e) {
            ctx.setRollbackOnly();
            return error(e.toString());
        }
    }
     
    /**
     * 
     * @param id
     * @return 
     */
    @DELETE
    @Path("{id: \\d+}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteCourse(@PathParam("id") int id) {
        Corso c = em.find(Corso.class, id);
        long count = em.createQuery("SELECT COUNT(c.id) FROM Categoria c WHERE c.idCorso = :idC", Long.class).setParameter("idC", c).getSingleResult();
        count += em.createQuery("SELECT COUNT(d.id) FROM Documento d WHERE d.idCorso = :idC", Long.class).setParameter("idC", c).getSingleResult();
        count += em.createQuery("SELECT COUNT(i.id) FROM Installazione i WHERE i.idCorso = :idC", Long.class).setParameter("idC", c).getSingleResult();

        if (count > 0) {
            return conflict("Corso con elementi collegati. Eliminare prima tali collegamenti e riprovare.");
        }

        try {
            em.remove(c);
            em.flush();
            
            return noContent();
        }    
        catch(PersistenceException pe) {
            ctx.setRollbackOnly();
            return conflict(pe.toString());                    
        } 
        catch (Exception e) {
            ctx.setRollbackOnly();
            return error(e.toString());
        }
    }
    
    /*********************************************************
    *
    *                     GESTIONE CATEGORIE
    *
    **********************************************************/
    
    /**
     * 
     * @param info
     * @param id
     * @return 
     */
    @GET
    @Path("{id: \\d+}/categories")
    public Response getCourseCategories(@Context UriInfo info, @PathParam("id") int id) {
        Corso c = em.find(Corso.class, id);
        if(c==null) 
            return notFound();
        else
            return ok(resourcesToURI(info, em.createQuery("SELECT cat.id from Categoria cat JOIN cat.idCorso c WHERE c.id = :idcorso").setParameter("idcorso", id).getResultList()));
    }

    /**
     * 
     * @param id
     * @param cat
     * @param info
     * @return 
     */
    @POST
    @Path("{id: \\d+}/categories")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addNewCourseCategory(@PathParam("id") int id, Categoria cat, @Context UriInfo info) {
        Corso c = em.find(Corso.class, id);
        if (c == null) {
            return notFound();
        }

        try {
            cat.setId(null);
            cat.setIdCorso(c);
            cat.setUidRisorsa(ms.generateUID());
            em.persist(cat);
                       
            ds. enqueueCategoryDelta(cat, DeltaConst.Operation.ADD);

            em.flush();
            
            return created(cat.getId());
        } 
        catch(PersistenceException pe) {
            ctx.setRollbackOnly();
            return conflict(pe.toString());
        }
        catch (Exception e) {
            ctx.setRollbackOnly();
            return error(e.toString());
        }
    }
    
    /**
     * 
     * @param idCorso
     * @param idCategoria
     * @return 
     */
    @GET
    @Path("{id: \\d+}/categories/{idcat: \\d+}")
    public Response getCourseCategorieDetail(@PathParam("id") int idCorso, @PathParam("idcat") int idCategoria) {
        try {
            return ok(em.createQuery("SELECT cat from Categoria cat JOIN cat.idCorso c WHERE c.id = :idCorso AND cat.id = :idCategoria")
                    .setParameter("idCorso", idCorso)
                    .setParameter("idCategoria", idCategoria)
                    .getSingleResult());
        }
        catch(NoResultException nre) {
            return notFound();
        }
    }
    
    /**
     * 
     * @param id
     * @param idCat
     * @param categoria
     * @return 
     */
    @PUT
    @Path("{id: \\d+}/categories/{idcat: \\d+}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateCourseCategory(@PathParam("id") int id, @PathParam("idcat") Integer idCat, Categoria categoria) {
        Corso c = em.find(Corso.class, id);
        if (c == null) {
            return notFound();
        }

        try {
            categoria.setId(idCat);
            categoria.setIdCorso(c);    
            em.merge(categoria);
            
            ds.enqueueCategoryDelta(categoria, DeltaConst.Operation.UPDATE);
            
            em.flush();
            
            return noContent();
        } 
        catch(PersistenceException pe) {
            ctx.setRollbackOnly();
            return conflict(pe.toString());
        }
        catch(Exception e) {
            ctx.setRollbackOnly();
            return error(e.toString());
        }
    }
    
    /**
     * 
     * @param id
     * @param idCat
     * @return 
     */
    @DELETE
    @Path("{id: \\d+}/categories/{idcat: \\d+}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteCourseCategory(@PathParam("id") int id, @PathParam("idcat") int idCat) {
        try {
            Categoria cat = em.createQuery("SELECT cat from Categoria cat JOIN cat.idCorso c WHERE cat.id = :idCat AND c.id = :idCorso", Categoria.class)
                    .setParameter("idCat", idCat)
                    .setParameter("idCorso", id)
                    .getSingleResult();

            long count = em.createQuery("SELECT COUNT(d.id) FROM Documento d JOIN d.idCorso c WHERE c.id = :idCorso", Long.class)
                    .setParameter("idCorso", id)
                    .getSingleResult();
            if (count > 0) {
                return conflict("Corso con elementi collegati. Eliminare prima tali collegamenti e riprovare.");
            }

            em.remove(cat);
            
            ds.enqueueCategoryDelta(cat, DeltaConst.Operation.REMOVE);
            
            em.flush();
            return noContent();
        } 
        catch(NoResultException nre) {
            return notFound();
        }
        catch(PersistenceException pe) {
            ctx.setRollbackOnly();
            return conflict(pe.toString());
        }
        catch (Exception e) {
            ctx.setRollbackOnly();
            return error(e.toString());
        }
    }
    
    /*********************************************************
    *
    *                     GESTIONE DOCUMENTI
    *
    **********************************************************/
    
    /**
     * 
     * @param id
     * @param info
     * @return 
     */
    @GET
    @Path("{id: \\d+}/documents")
    public Response getCourseDocuments(@PathParam("id") int id, @Context UriInfo info) {
        return ok(resourcesToURI(info, em.createQuery("SELECT d.id FROM Documento d JOIN d.idCorso c WHERE c.id = :id").setParameter("id", id).getResultList()));
    }

    /**
     * 
     * @param id
     * @param req
     * @param info
     * @return 
     */
    @POST
    @Path("{id: \\d+}/documents")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response addNewCourseDocument(@PathParam("id") int id, @Context HttpServletRequest req, @Context UriInfo info) {
        Corso corso = em.find(Corso.class, id);
        if (corso == null) {
            return notFound();
        }
       
        try {
            Part documentData = req.getPart("document");
            if (documentData == null) {
                return badRequest();
            }                

            final byte[] bytes;
            try (InputStream in = documentData.getInputStream()) {
                bytes = new byte[(int)documentData.getSize()];
                in.read(bytes);
            }
            documentData.delete();
                  
            Documento doc = new Documento();
            doc.setIdCorso(corso);
            doc.setNomefile(Paths.get(documentData.getSubmittedFileName()).getFileName().toString());
            doc.setDimensione(bytes.length);
            doc.setChecksum(new Checksum(bytes).getCheckum());
            doc.setContenuto(bytes);
            doc.setUidRisorsa(ms.generateUID());
            
            em.persist(doc);
            
            ds.enqueueDocumentDelta(doc, DeltaConst.Operation.ADD);
                        
            em.flush();
            
            return ok(resourceToURI(info, doc.getId()));
        }
        catch(PersistenceException pe) {
            ctx.setRollbackOnly();
            return conflict(pe.toString());
        }
        catch(IOException | ServletException e) {
            ctx.setRollbackOnly();
            return error(e.toString());
        }                                
    }
    
    /**
     * 
     * @param idCorso
     * @param idDoc
     * @return 
     */
    @GET
    @Path("{id: \\d+}/documents/{iddoc: \\d+}")
    public Response getCourseDocumentDetail(@PathParam("id") int idCorso, @PathParam("iddoc") int idDoc) {
        try {
            return ok(em.createQuery("SELECT d from Documento d JOIN d.idCorso c WHERE c.id = :idCorso AND d.id = :idDoc", Documento.class)
                    .setParameter("idCorso", idCorso)
                    .setParameter("idDoc", idDoc)
                    .getSingleResult());
        }
        catch(NoResultException nre) {  
            return notFound();
        }
    }
    
    /**
     * 
     * @param idCorso
     * @param idDoc
     * @return 
     */
    @DELETE
    @Path("{id: \\d+}/documents/{iddoc: \\d+}")
    public Response deleteCourseDocument(@PathParam("id") int idCorso, @PathParam("iddoc") int idDoc) {
        try {
            return ok(em.createQuery("DELETE FROM Documento d WHERE d.idCorso.id = :idCorso AND d.id = :idDoc", Documento.class)
                    .setParameter("idCorso", idCorso)
                    .setParameter("idDoc", idDoc)
                    .executeUpdate());
        }
        
        catch(NoResultException nre) {  
            return notFound();
        }
        catch(PersistenceException pe) {
            ctx.setRollbackOnly();
            return conflict(pe.toString());
        }
    }
    
    /**
     * 
     * @param idCorso
     * @param idDoc
     * @return 
     */
    @GET
    @Path("{id: \\d+}/documents/{iddoc: \\d+}/stream")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getCourseDocumentFile(@PathParam("id") int idCorso, @PathParam("iddoc") int idDoc) {
        try {
            Documento doc = em.createQuery("SELECT d from Documento d JOIN d.idCorso c WHERE c.id = :idCorso AND d.id = :idDoc", Documento.class)
                .setParameter("idCorso", idCorso)
                .setParameter("idDoc", idDoc)
                .getSingleResult();
            
            return downloadFile(doc.getNomefile(), doc.getContenuto());
        }
        catch(NoResultException nre) {
            return notFound();
        }        
    }
        
    /**
     * 
     * @param idCorso
     * @param idCat
     * @param info
     * @return 
     */
    @GET
    @Path("{id: \\d+}/categories/{idcat: \\d+}/documents")
    public Response getCouseCategoryDocuments(@PathParam("id") int idCorso, @PathParam("idcat") int idCat, @Context UriInfo info) {
        try {
            em.createQuery("SELECT cat FROM Categoria cat JOIN cat.idCorso c WHERE c.id = :idcorso AND cat.id = :idcat", Categoria.class)
                            .setParameter("idcat", idCat)
                            .setParameter("idcorso", idCorso)
                            .getSingleResult();
        }
        catch(NoResultException nre) {
            return notFound();
        }
                
        return ok(resourcesToURI(info, em.createQuery("SELECT d.id FROM Documento d JOIN d.idCategoria cat JOIN cat.idCorso c WHERE c.id = :idcorso AND cat.id = :idcat", Integer.class)
                        .setParameter("idcorso", idCorso)
                        .setParameter("idcat", idCat)
                        .getResultList()));
    }
    
    /**
     * 
     * @param id
     * @param idCat
     * @param req
     * @param info
     * @return 
     */
    @POST
    @Path("{id: \\d+}/categories/{idcat: \\d+}/documents")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response addNewCourseCategoryDocument(@PathParam("id") int id, @PathParam("id") int idCat, @Context HttpServletRequest req, @Context UriInfo info) {
        try {
            Categoria cat = em.createQuery("SELECT cat FROM Categoria cat JOIN cat.idCorso c WHERE cat.id = :idcat AND c.id = :idcorso", Categoria.class)
                                    .setParameter("idcat", idCat)
                                    .setParameter("idcorso", id)
                                    .getSingleResult();
        
            Part documentData = req.getPart("document");
            if (documentData == null) {
                return badRequest();
            }                

            final byte[] bytes;
            try (InputStream in = documentData.getInputStream()) {
                bytes = new byte[(int)documentData.getSize()];
                in.read(bytes);
            }
            documentData.delete();
                
            Documento doc = new Documento();
            doc.setIdCategoria(cat);
            doc.setNomefile(Paths.get(documentData.getSubmittedFileName()).getFileName().toString());
            doc.setDimensione(bytes.length);
            doc.setChecksum(new Checksum(bytes).getCheckum());
            doc.setContenuto(bytes);
            doc.setUidRisorsa(ms.generateUID());
            
            em.persist(doc);
            
            ds.enqueueDocumentDelta(doc, DeltaConst.Operation.ADD);
            
            em.flush();
            
            return ok(resourceToURI(info, doc.getId()));
        }
        catch(PersistenceException pe) {
            ctx.setRollbackOnly();
            return conflict(pe.toString());
        }
        catch(IOException | ServletException e) {
            ctx.setRollbackOnly();
            return error(e.toString());
        }                                
    }    
    
    /**
     * 
     * @param idCorso
     * @param idCat
     * @param idDoc
     * @return 
     */
    @GET
    @Path("{id: \\d+}/categories/{idcat: \\d+}/documents/{iddoc: \\d+}")
    public Response getCouseCategoryDocumentDetail(@PathParam("id") int idCorso, @PathParam("idcat") int idCat, @PathParam("iddoc") int idDoc) {
        try {
            return ok(em.createQuery("SELECT d from Documento d JOIN d.idCategoria cat JOIN cat.idCorso c WHERE cat.id = :idcat AND c.id = :idcorso AND d.id = :iddoc", Documento.class)
                    .setParameter("idcat", idCat)
                    .setParameter("idcorso", idCorso)
                    .setParameter("iddoc", idDoc)
                    .getSingleResult());
        }
        catch(NoResultException nre) {
            return notFound();
        }
    }
    
     /**
     * 
     * @param id
     * @param idCat
     * @param idDoc
     * @return 
     */
    @DELETE
    @Path("{id: \\d+}/categories/{idcat: \\d+}/documents/{iddoc: \\d+}/")
    public Response deleteCourseCategoryDocument(@PathParam("id") int id, @PathParam("idcat") int idCat, @PathParam("iddoc") int idDoc) {
        try {
            Documento doc = em.createQuery("SELECT d FROM Documento d JOIN d.idCategoria cat JOIN cat.idCorso c WHERE d.id = :iddoc AND cat.id = :idcat AND c.id = :idcorso", Documento.class)
                    .setParameter("iddoc", idDoc)
                    .setParameter("idcat", idCat)
                    .setParameter("idcorso", id)
                    .getSingleResult();
        
            em.remove(doc);
            
            ds.enqueueDocumentDelta(doc, DeltaConst.Operation.REMOVE);
            
            em.flush();
            
            return noContent();
        }
        catch(PersistenceException pe) {
            ctx.setRollbackOnly();
            return conflict(pe.toString());
        }
        catch(Exception e) {
            ctx.setRollbackOnly();
            return error(e.toString());
        }
    }
    
    /**
     * 
     * @param idCorso
     * @param idCat
     * @param idDoc
     * @return 
     */
    @GET
    @Path("{id: \\d+}/categories/{idcat: \\d+}/documents/{iddoc: \\d+}/stream")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getCouseCategoryDocumentFile(@PathParam("id") int idCorso, @PathParam("idcat") int idCat, @PathParam("iddoc") int idDoc) {
        try {
            Documento doc = em.createQuery("SELECT d from Documento d JOIN d.idCategoria cat JOIN cat.idCorso c WHERE cat.id = :idcat AND c.id = :idcorso AND d.id = :iddoc", Documento.class)
                    .setParameter("idcat", idCat)
                    .setParameter("idcorso", idCorso)
                    .setParameter("iddoc", idDoc)
                    .getSingleResult();
            
            return downloadFile(doc.getNomefile(), doc.getContenuto());
        }
        catch(NoResultException nre) {
            return notFound();
        }
    }             
}
