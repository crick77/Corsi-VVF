/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.dipvvf.abr.app.corsivvf.web;

import it.dipvvf.abr.app.corsivvf.ejb.MiscServices;
import it.dipvvf.abr.app.corsivvf.model.Documento;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.EJB;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author ospite
 */
@WebServlet(name = "UploadServlet", urlPatterns = {"/upload/*", })
@MultipartConfig
public class UploadServlet extends HttpServlet {
    @EJB
    MiscServices ms;
    private MessageDigest md;
    private final Pattern pattern = Pattern.compile("\\d+");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo.startsWith("/")) {
            pathInfo = pathInfo.substring(1);
        }
        Matcher m = pattern.matcher(pathInfo);
        if (m.matches()) {
            int idCorso = Integer.parseInt(m.group());

            Part filePart = request.getPart("filedoc");
            String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            InputStream fileContent = filePart.getInputStream();

            File tempFile = File.createTempFile("CorsiVVF-", ".tmp");
            tempFile.deleteOnExit();
            md.reset();
            try (OutputStream out = new FileOutputStream(tempFile)) {
                int read;
                final byte[] bytes = new byte[1024];

                while ((read = fileContent.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                    md.update(bytes, 0, read);
                }
            }

            String hash = DatatypeConverter.printHexBinary(md.digest()).toUpperCase();

            Documento doc = new Documento();
            doc.setDimensione(filePart.getSize());
            doc.setNomefile(fileName);
            doc.setChecksum(hash);
            ms.saveDocument(doc);

            response.setStatus(HttpServletResponse.SC_OK);            
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    public String getServletInfo() {
        return "Document Upload Servlet";
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException nse) {
            throw new ServletException("Cannot initialize due to: " + nse, nse);
        }
    }

}

