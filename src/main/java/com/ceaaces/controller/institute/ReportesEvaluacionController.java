package ec.gob.ceaaces.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsReportConfiguration;

import org.apache.commons.io.IOUtils;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;

import ec.gob.ceaaces.catalogo.dtos.catalogos.ParametroDTO;
import ec.gob.ceaaces.catalogo.enumeraciones.ParametroEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.TipoReferenciaEnum;
import ec.gob.ceaaces.data.ReportResource;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.recursos.Recurso;
import ec.gob.ceaaces.recursos.enumeracion.EnumClaseRecurso;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.EvaluacionServicio;
import ec.gob.ceaaces.services.RecursoServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.services.ReporteServicioDelegate;
import ec.gob.ceaaces.util.JsfUtil;

@ManagedBean(name = "reportesController")
public class ReportesEvaluacionController implements Serializable {

    /**
     * classVar1 documentation comment serialVersionUID
     */

    private static final long serialVersionUID = 8076101138688720711L;

    private final String URI_REPORTE = "/home/";

    private static final Logger LOG = Logger
	    .getLogger(ReportesEvaluacionController.class.getSimpleName());

    private Map<String, String> reportes;
    private List<ReportResource> listaReportes = new ArrayList<ReportResource>();
    private List<Recurso> listaRecursosReportes = new ArrayList<Recurso>();
    private Map<String, String> parametros;

    private InformacionIesDTO informacionIesDTO;
    private ParametroDTO pDTO;

    private byte[] archivoByte;

    @Autowired
    private EvaluacionServicio evaluacionServicio;

    @Autowired
    private RegistroServicio registroServicio;

    @Autowired
    private CatalogoServicio catalogoServicio;
    @Autowired
    private RecursoServicio recursoServicio;

    @Autowired
    private ReporteServicioDelegate reporteServicioDelegate;

    private String nombreArchivo;

    private TreeNode rootVariables = null;
    private Recurso recursoSeleccionado;
    private String usuario = null;

    @PostConstruct
    public void obtenerDatos() {
	parametros = new HashMap<>();
	recursoSeleccionado = new Recurso();
	ListaIesController controller = (ListaIesController) JsfUtil
	        .obtenerObjetoSesion("listaIesController");
	usuario = controller.getUsuario();
	try {

	    informacionIesDTO = registroServicio
		    .obtenerInformacionIesPorIes(controller.getIes());
	    pDTO = catalogoServicio.obtenerParametroPorCodigoYReferencia(
		    ParametroEnum.URL_REPORTES.getValor(),
		    TipoReferenciaEnum.INST.getValor());
	    listaRecursosReportes = recursoServicio
		    .obtenerRecursoPorFaseProceso(6L, 6L,
		            EnumClaseRecurso.REPORTE);
	    LOG.info("listaRecursosReportes..size.."
		    + listaRecursosReportes.size());

	    // rootVariables = this.createTree();
	} catch (Exception e) {
	    JsfUtil.msgError("Error al obtener información IES. Comuníquese con el administrador.");
	    e.printStackTrace();
	}
	// llenarListaReportes();

    }

    public void llenarListaReportes() {

	ReportResource rr = new ReportResource("RepInformePreliminar",
	        "Variables Cuantitativas");
	listaReportes.add(rr);
	rr = new ReportResource("RepVariableCualitativa",
	        "Variables Cualitativas");
	listaReportes.add(rr);

	rr = new ReportResource("RepVariableCualitativaPrincipal",
	        "Variables Cualitativas Criterios");
	listaReportes.add(rr);

	rr = new ReportResource("RepCarreras", "Carreras");
	listaReportes.add(rr);

	rr = new ReportResource("RepPerfilesEgreso", "Perfiles de Egreso");
	listaReportes.add(rr);

	rr = new ReportResource("RepPerfilesConsultados",
	        "Perfiles Consultados");
	listaReportes.add(rr);

	rr = new ReportResource("RepPlanCurricular", "Planes Curriculares");
	listaReportes.add(rr);

	rr = new ReportResource("RepPerfilesEgresoDetalle",
	        "Perfiles de Egreso Criterios");
	listaReportes.add(rr);

	rr = new ReportResource("RepPerfilesConsultadosDetalle",
	        "Perfiles Consultados Criterios");
	listaReportes.add(rr);

	rr = new ReportResource("RepPlanCurricularDetalle",
	        "Planes curriculares Criterios");
	listaReportes.add(rr);

	rr = new ReportResource("RepPeas", "Programas Estudio Asignatura");
	listaReportes.add(rr);

	rr = new ReportResource("RepCapacitacion", "Docentes Capacitaciones");
	listaReportes.add(rr);

	rr = new ReportResource("RepCargosAcademicos",
	        "Docentes Cargos Académicos");
	listaReportes.add(rr);

	rr = new ReportResource("RepDocentesAfines", "Docentes Afines");
	listaReportes.add(rr);

	rr = new ReportResource("RepContrataciones", "Docentes Contrataciones");
	listaReportes.add(rr);

	rr = new ReportResource("RepExperienciaProfesional",
	        "Docentes Experiencia Profesional");
	listaReportes.add(rr);

	rr = new ReportResource("RepFormacionProfesional",
	        "Docentes Formación Profesional");
	listaReportes.add(rr);

	rr = new ReportResource("RepDocentesTPMT",
	        "Docentes MT y TP con ejercicio profesional");
	listaReportes.add(rr);

	rr = new ReportResource("RepRenumeracionTC", "Docentes Remuneración TC");
	listaReportes.add(rr);

	rr = new ReportResource("RepRenumeracionTP", "Docentes Remuneración TP");
	listaReportes.add(rr);

	rr = new ReportResource("RepActividadVinculacion",
	        "Actividades de Vinculación");
	listaReportes.add(rr);

	rr = new ReportResource("RepProyectosInvestigacion",
	        "Proyectos de Investigación");
	listaReportes.add(rr);

	rr = new ReportResource("RepProyectoVinculacion",
	        "Proyectos de Vinculación");
	listaReportes.add(rr);

	rr = new ReportResource("RepContrarosAnchoBanda",
	        "Contratos Ancho de Banda");
	listaReportes.add(rr);

	rr = new ReportResource("RepConvenios", "Convenios");
	listaReportes.add(rr);

	rr = new ReportResource("RepProducciones", "Producciones");
	listaReportes.add(rr);

	rr = new ReportResource("RepPublicaciones", "Publicaciones");
	listaReportes.add(rr);

	rr = new ReportResource("RepEstudiantesDetalle", "Estudiantes");
	listaReportes.add(rr);

	rr = new ReportResource("RepDocentesDetalle", "Docentes");
	listaReportes.add(rr);

    }

    public void generarReporteN() {

	LOG.info("recursoSeleccionado: " + recursoSeleccionado.getIdRecurso());

	MenuController mController = (MenuController) JsfUtil
	        .obtenerObjetoSesion("menuController");

	parametros.put("id", informacionIesDTO.getIes().getId().toString());
	try {
	    InputStream is = reporteServicioDelegate.getReporte(mController
		    .getAppId().toString(), recursoSeleccionado
		    .getRecursoRuta(), usuario, parametros, "xls");

	    byte[] contenidoReporte = IOUtils.toByteArray(is);
	    // Llamar a servlet para presentar reporte
	    HttpServletResponse response = (HttpServletResponse) FacesContext
		    .getCurrentInstance().getExternalContext().getResponse();

	    ServletOutputStream out = response.getOutputStream();
	    response.setContentType("application/xls");
	    response.setContentLength(contenidoReporte.length);
	    String nmbFile = (recursoSeleccionado.getIdRecurso() + "_"
		    + usuario + "" + "(IES-"
		    + informacionIesDTO.getIes().getCodigo() + ")" + ".xls");
	    response.setHeader("Content-Disposition", "attachment; filename="
		    + nmbFile);
	    // presenta el reporte
	    out.write(contenidoReporte, 0, contenidoReporte.length);
	    out.flush();
	    out.close();
	    FacesContext.getCurrentInstance().responseComplete();
	} catch (Exception e) {
	    JsfUtil.msgError("Ha ocurrido un error en la generación del reporte. Comuníquese con el administrador.");
	    e.printStackTrace();
	}

    }

    public void presentarReporteNXls() {

    }

    public void generarReporte(String nombreReporte) {
	nombreArchivo = nombreReporte;
	ByteArrayOutputStream outReporte = new ByteArrayOutputStream();
	String pathJasperReporte = pDTO.getValor() + "variables/"
	        + nombreReporte + ".jasper";

	Map<String, Object> parametrosReporte = new HashMap<>();
	parametrosReporte.put("id", informacionIesDTO.getIes().getId());
	DataSource dataSource = null;
	Connection connection = null;
	try {
	    InitialContext ic = new InitialContext();
	    Context xmlContext = (Context) ic.lookup("java:comp/env");

	    dataSource = (DataSource) xmlContext.lookup("jdbc/casDbConnection");
	    connection = dataSource.getConnection();
	    JRXlsExporter exporter = new JRXlsExporter();
	    cargarPropiedadesReporte(exporter);
	    JasperPrint jasperPrint = JasperFillManager.fillReport(
		    pathJasperReporte, parametrosReporte, connection);
	    exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
	    exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(
		    outReporte));

	    exporter.exportReport();
	    archivoByte = outReporte.toByteArray();
	    outReporte.flush();
	    outReporte.close();
	} catch (java.io.FileNotFoundException e) {
	    JsfUtil.msgError("El recurso no se encuentra disponible.");
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al generar el reporte, comuníquese con el administrador del sistema");
	} finally {
	    try {
		if (connection != null) {
		    connection.close();
		}
	    } catch (Exception ex) {
		JsfUtil.msgError("El recurso no se encuentra disponible.");
		ex.printStackTrace();
	    }
	}
    }

    public void presentarReporteXls() {
	HttpServletResponse response = (HttpServletResponse) FacesContext
	        .getCurrentInstance().getExternalContext().getResponse();
	try {
	    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
		    "dd-MM-yyyy");
	    response.setContentType("application/vnd.ms-excel");
	    response.setHeader("Content-Disposition",
		    "attachment; filename=" + nombreArchivo + "_"
		            + informacionIesDTO.getIes().getCodigo()
		            + simpleDateFormat.format(new Date()) + ".xls");
	    response.setContentLength(archivoByte.length);
	    ServletOutputStream out = response.getOutputStream();
	    out.write(archivoByte, 0, archivoByte.length);
	    out.flush();
	    FacesContext.getCurrentInstance().responseComplete();
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al generar el reporte, comuníquese con el administrador del sistema");
	}
    }

    private void cargarPropiedadesReporte(JRXlsExporter exporter) {
	SimpleXlsReportConfiguration configuracionExcel = new SimpleXlsReportConfiguration();
	configuracionExcel.setDetectCellType(Boolean.TRUE);
	configuracionExcel.setOnePagePerSheet(Boolean.FALSE);
	configuracionExcel.setWhitePageBackground(Boolean.FALSE);
	configuracionExcel.setRemoveEmptySpaceBetweenColumns(Boolean.TRUE);
	configuracionExcel.setRemoveEmptySpaceBetweenRows(Boolean.TRUE);
	configuracionExcel.setIgnoreCellBorder(Boolean.FALSE);
	exporter.setConfiguration(configuracionExcel);
    }

    // public void generarReporte() {
    // try {
    // Map<String, String> parameter = new HashMap<>();
    // parameter.put("id", "200");
    //
    // String uriResource =
    // "/reports/Institutos/Variables/RepConvenios";
    // String outputFormat = "pdf";
    // ReportFactory factory = ReportFactoryImpl.getInstance();
    // try {
    // Report report = factory.createSession(hostname, port, username,
    // passwd);
    // InputStream inputStream = report.getReportFromResource(
    // uriResource, parameter, outputFormat);
    // IOUtils.copy(inputStream, new FileOutputStream(home_user
    // + "/RepConvenios.pdf"));
    // } catch (Exception ex) {
    // ex.printStackTrace();
    // }

    // archivoByte = new byte[inputStream.available()];
    // inputStream.read(archivoByte);
    // inputStream.close();
    // descargarArchivo();
    // } catch (Throwable e) {
    // e.printStackTrace();
    // }
    // }

    public void descargarArchivo() {

	HttpServletResponse response = (HttpServletResponse) FacesContext
	        .getCurrentInstance().getExternalContext().getResponse();
	response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
	response.setHeader("Content-Disposition", "attachment; filename=\""
	        + "prueba" + "\"");
	response.setContentLength(archivoByte.length);
	OutputStream os;
	try {
	    os = response.getOutputStream();
	    os.write(archivoByte);
	    os.flush();
	    os.close();

	    FacesContext.getCurrentInstance().responseComplete();
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (Throwable e) {
	    e.printStackTrace();
	}

    }

    public InformacionIesDTO getInformacionIesDTO() {
	return informacionIesDTO;
    }

    public void setInformacionIesDTO(InformacionIesDTO informacionIesDTO) {
	this.informacionIesDTO = informacionIesDTO;
    }

    public Map<String, String> getReportes() {
	return reportes;
    }

    public void setReportes(Map<String, String> reportes) {
	this.reportes = reportes;
    }

    public List<ReportResource> getListaReportes() {
	return listaReportes;
    }

    public void setListaReportes(List<ReportResource> listaReportes) {
	this.listaReportes = listaReportes;
    }

    public ParametroDTO getpDTO() {
	return pDTO;
    }

    public void setpDTO(ParametroDTO pDTO) {
	this.pDTO = pDTO;
    }

    public String getNombreArchivo() {
	return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
	this.nombreArchivo = nombreArchivo;
    }

    public List<Recurso> getListaRecursosReportes() {
	return listaRecursosReportes;
    }

    public void setListaRecursosReportes(List<Recurso> listaRecursosReportes) {
	this.listaRecursosReportes = listaRecursosReportes;
    }

    public TreeNode getRootVariables() {
	return rootVariables;
    }

    public void setRootVariables(TreeNode rootVariables) {
	this.rootVariables = rootVariables;
    }

    private TreeNode createTree() {
	// TODO Auto-generated method stub
	TreeNode rootTemp = new DefaultTreeNode(new Recurso(), null);

	Recurso firstRecurso = new Recurso();
	firstRecurso.setDescripcion("Recursos Uno");

	TreeNode firstNode = new DefaultTreeNode(firstRecurso, rootTemp);

	Recurso secondRecurso = new Recurso();
	secondRecurso.setDescripcion("Recursos Dos");
	TreeNode secondNode = new DefaultTreeNode(secondRecurso, rootTemp);

	for (Recurso r : listaRecursosReportes) {
	    if (r.getDescripcion().contains("ca")) {
		TreeNode firstGroupNode = new DefaultTreeNode(r, firstNode);
	    } else {
		TreeNode secondGroupNode = new DefaultTreeNode(r, secondNode);
	    }

	}
	return rootTemp;
    }

    public Map<String, String> getParametros() {
	return parametros;
    }

    public void setParametros(Map<String, String> parametros) {
	this.parametros = parametros;
    }

    public Recurso getRecursoSeleccionado() {
	return recursoSeleccionado;
    }

    public void setRecursoSeleccionado(Recurso recursoSeleccionado) {
	this.recursoSeleccionado = recursoSeleccionado;
    }

    public String getUsuario() {
	return usuario;
    }

    public void setUsuario(String usuario) {
	this.usuario = usuario;
    }

}
