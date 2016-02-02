package ec.gob.ceaaces.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaDTO;
import ec.gob.ceaaces.services.EvidenciasServicio;

public class ServletDownload extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 5711148250377138686L;
    private static final int BYTES_DOWNLOAD = 1024;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
	    throws IOException {

	String parametro = request.getParameter("id");
	if (parametro != null && !parametro.equals("0")
	        && !parametro.equals("")) {
	    try {
		Long id = new Long(parametro);
		int read = 0;

		byte[] bytes = new byte[BYTES_DOWNLOAD];
		ApplicationContext applicationContext = WebApplicationContextUtils
		        .getWebApplicationContext(getServletContext());

		EvidenciasServicio service = (EvidenciasServicio) applicationContext
		        .getBean("evidenciasServicio");

		EvidenciaDTO archivoSeleccionado = null;
		try {
		    // archivoSeleccionado = service.obtenerEvidenciaPorId(id);
		} catch (Exception e1) {
		    // TODO Auto-generated catch block
		    e1.printStackTrace();

		    return;
		}
		File doc = new File(archivoSeleccionado.getUrl().concat("/")
		        .concat(archivoSeleccionado.getNombreArchivo()));
		String extension = archivoSeleccionado.getNombreArchivo()
		        .split("[.]")[1];
		if (extension.equalsIgnoreCase("xls")) {
		    response.setContentType("application/xls");
		} else if (extension.equalsIgnoreCase("pdf")) {
		    response.setContentType("application/pdf");
		}
		if (doc.exists()) {

		    FileInputStream fis = new FileInputStream(doc);
		    byte[] bloque = new byte[(int) doc.length()];
		    fis.read(bloque);

		    ByteArrayInputStream bais = new ByteArrayInputStream(bloque);
		    ServletOutputStream out;
		    // response.setContentType("application/pdf");
		    response.setHeader("Content-Disposition",
			    "attachment;filename="
			    // + archivoSeleccionado.getInformacionIes()
			    // .getIes().getCodigo() + "_"
			    // + archivoSeleccionado.getIdTabla() + "_"
			            + archivoSeleccionado.getNombreArchivo());

		    out = response.getOutputStream();
		    while ((read = bais.read(bytes)) != -1) {
			out.write(bytes, 0, read);
		    }

		    out.flush();
		    out.close();
		}
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	}
    }
}
