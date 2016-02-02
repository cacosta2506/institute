package ec.gob.ceaaces.controller;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsReportConfiguration;

import org.primefaces.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;

import ec.gob.ceaaces.catalogo.dtos.catalogos.AuditoriaDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseSecuenciaDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.ProcesoDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.VariableGrupoDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.VariableProcesoDTO;
import ec.gob.ceaaces.catalogo.enumeraciones.FaseSecuenciaEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.GrupoVariableEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.TipoFaseEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.TipoVariableEnum;
import ec.gob.ceaaces.catalogo.exceptions.ServicioException;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ActividadVinculacionDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ConvenioVigenteDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionCarreraDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.PersonaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ProduccionDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ProyectoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.PublicacionDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ValorVariableDTO;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.EvaluacionServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.JsfUtil;

/**
 * 
 * Controlador para generar reporte inicial de variables del ITTS
 * 
 * @author jhomara
 * 
 */

public class ReporteVerificacionController implements Serializable {

    private static final long serialVersionUID = -4262265325931338547L;

    @Autowired
    private EvaluacionServicio evaluacionServicio;

    @Autowired
    private CatalogoServicio catalogoServicio;

    @Autowired
    private RegistroServicio registroServicio;

    private List<ValorVariableDTO> listaValoresVariables = new ArrayList<ValorVariableDTO>();
    private List<ValorVariableDTO> listaValoresVariablesEvaluador = new ArrayList<ValorVariableDTO>();
    private List<VariableProcesoDTO> listaVariablesProceso = new ArrayList<VariableProcesoDTO>();
    private List<PersonaDTO> listaPersonas = new ArrayList<PersonaDTO>();
    private List<PublicacionDTO> listaPublicaciones = new ArrayList<PublicacionDTO>();
    private List<ProduccionDTO> listaProducciones = new ArrayList<ProduccionDTO>();
    private List<ProyectoDTO> listaProyectos = new ArrayList<ProyectoDTO>();
    private List<ActividadVinculacionDTO> listaActividades = new ArrayList<ActividadVinculacionDTO>();
    private List<ConvenioVigenteDTO> listaConvenios = new ArrayList<ConvenioVigenteDTO>();
    private List<InformacionCarreraDTO> listaCarreras = new ArrayList<InformacionCarreraDTO>();

    private ProcesoDTO procesoActual = new ProcesoDTO();
    private InformacionIesDTO informacionIesDTO = new InformacionIesDTO();
    private FaseIesDTO faseIesDTO = new FaseIesDTO();
    private String usuario;
    private ValorVariableDTO valorVariableSeleccionada = new ValorVariableDTO();
    private List<FaseSecuenciaDTO> fasesSecuencia = new ArrayList<FaseSecuenciaDTO>();
    private List<VariableGrupoDTO> variablesGrupo = new ArrayList<VariableGrupoDTO>();

    private byte[] reporteBytes = null;

    public ReporteVerificacionController() {
	listaValoresVariablesEvaluador = new ArrayList<ValorVariableDTO>();
    }

    @PostConstruct
    public void obtenerDatosIniciales() {
	ListaIesController controller = (ListaIesController) FacesContext
	        .getCurrentInstance().getExternalContext().getSessionMap()
	        .get("listaIesController");
	try {
	    informacionIesDTO = registroServicio
		    .obtenerInformacionIesPorIes(controller.getIes());

	    procesoActual = catalogoServicio
		    .obtenerProcesoActualPorIdAplicacion(controller
		            .getIdAplicacion());
	    faseIesDTO = controller.getFaseIesDTO();
	    // procesoActual = faseIesDTO.getProcesoDTO();
	    usuario = controller.getUsuario();
	    listaValoresVariables = evaluacionServicio
		    .obtenerInformacionVariablesActivas(null,
		            informacionIesDTO.getId(),
		            TipoVariableEnum.CUANTITATIVA.getValue(), null);
	    // listaValoresVariables = evaluacionServicio
	    // .obtenerInformacionVariables(null,
	    // informacionIesDTO.getId(),
	    // TipoVariableEnum.CUANTITATIVA.getValue(), null);

	} catch (ServicioException e) {
	    JsfUtil.msgError("Error al obtener informacion inicial. "
		    + e.getMessage());
	    e.printStackTrace();
	} catch (Exception e) {
	    JsfUtil.msgError("Error al obtener informacion inicial. Comuníquese con el administrador.");
	    e.printStackTrace();
	}
    }

    public void generarVariables() {
	listaValoresVariablesEvaluador.clear();

	try {
	    AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
	    auditoriaDTO.setFechaModificacion(new Date());
	    auditoriaDTO.setUsuarioModificacion(usuario);
	    for (ValorVariableDTO valorVariableDTO : listaValoresVariables) {
		if (valorVariableDTO.getVariableProcesoDTO().getInforme() != null
		        && valorVariableDTO.getVariableProcesoDTO()
		                .getInforme()) {
		    if (valorVariableDTO.getVariableProcesoDTO()
			    .getSqlValorInicial() != null
			    || valorVariableDTO.getVariableProcesoDTO()
			            .getSqlListaId() != null) {
			if (!valorVariableDTO.getVariableProcesoDTO()
			        .getMuestraEstratificada()) {
			    if (valorVariableDTO.getVariableProcesoDTO()
				    .getVariablePadreDTO() == null) {
				if (valorVariableDTO.getVariableProcesoDTO()
				        .getVariableDTO().getVariableGrupoDTO() != null) {
				    // if
				    // (GrupoVariableEnum.CURRICULO.getValue()
				    // .equals(valorVariableDTO
				    // .getVariableProcesoDTO()
				    // .getVariableDTO()
				    // .getVariableGrupoDTO()
				    // .getNemonico())) {
				    //
				    // listaValoresVariablesEvaluador
				    // .add(valorVariableDTO);
				    // continue;
				    // } else

				    if (GrupoVariableEnum.INFRAESTRUCTURA
					    .getValue()
					    .equals(valorVariableDTO
					            .getVariableProcesoDTO()
					            .getVariableDTO()
					            .getVariableGrupoDTO()
					            .getNemonico())) {
					if (!valorVariableDTO
					        .getVariableProcesoDTO()
					        .getVariableDTO().getId()
					        .equals(152L)
					        && !valorVariableDTO
					                .getVariableProcesoDTO()
					                .getVariableDTO()
					                .getId().equals(153L)) {
					    listaValoresVariablesEvaluador
						    .add(valorVariableDTO);
					    continue;
					}
				    }

				    // else if (valorVariableDTO
				    // .getVariableProcesoDTO().getId()
				    // .equals(299L)
				    // || valorVariableDTO
				    // .getVariableProcesoDTO()
				    // .getId().equals(298L)
				    // || valorVariableDTO
				    // .getVariableProcesoDTO()
				    // .getId().equals(300L)) {
				    // // NUEVO REQUERIMIENTO, EL EVALUADOR
				    // // COLOCA
				    // // EL VALOR DE ESTA
				    // // VARIABLE, NO CALCULA EL SISTEMA
				    //
				    // listaValoresVariablesEvaluador
				    // .add(valorVariableDTO);
				    // continue;
				    // }
				}
				if (valorVariableDTO.getVariableProcesoDTO()
				        .getSqlValorEvaluacion() != null
				        && !valorVariableDTO
				                .getVariableProcesoDTO()
				                .getSqlValorEvaluacion()
				                .isEmpty()) {
				    valorVariableDTO
					    .setAuditoriaDTO(auditoriaDTO);
				    valorVariableDTO.setFaseIesDTO(faseIesDTO);
				    valorVariableDTO = evaluacionServicio
					    .obtenerValorVariable(
					            valorVariableDTO,
					            faseIesDTO,
					            auditoriaDTO,
					            valorVariableDTO
					                    .getVariableProcesoDTO()
					                    .getSqlValorEvaluacion());
				    listaValoresVariablesEvaluador
					    .add(valorVariableDTO);
				} else {
				    valorVariableDTO
					    .setAuditoriaDTO(auditoriaDTO);
				    valorVariableDTO.setFaseIesDTO(faseIesDTO);
				    valorVariableDTO = evaluacionServicio
					    .obtenerValorVariable(
					            valorVariableDTO,
					            faseIesDTO,
					            auditoriaDTO,
					            valorVariableDTO
					                    .getVariableProcesoDTO()
					                    .getSqlListaId());
				    listaValoresVariablesEvaluador
					    .add(valorVariableDTO);

				}

			    } else if (valorVariableDTO.getVariableProcesoDTO()
				    .getId().equals(306L)
				    || valorVariableDTO.getVariableProcesoDTO()
				            .getId().equals(55L)) {
				listaValoresVariablesEvaluador
				        .add(valorVariableDTO);
			    }
			    // Se calcula por muestra estratificada.
			    // else {
			    // listaValoresVariablesEvaluador
			    // .add(valorVariableDTO);
			    // }
			} else {
			    List<VariableProcesoDTO> hijas = catalogoServicio
				    .obtenerVariablesHijas(valorVariableDTO
				            .getVariableProcesoDTO().getId());
			    List<ValorVariableDTO> resultado = evaluacionServicio
				    .obtenerValorVariableMuestraEstratificada(
				            valorVariableDTO
				                    .getVariableProcesoDTO(),
				            hijas, this.informacionIesDTO
				                    .getId(), auditoriaDTO,
				            faseIesDTO);
			    listaValoresVariablesEvaluador.addAll(resultado);
			}
		    } else {
			// PARA ESTUDIANTES Y NO SE EJECUTA PARA LIBROS
			if (valorVariableDTO.getVariableProcesoDTO()
			        .getMuestraEstratificada()
			        && !valorVariableDTO.getVariableProcesoDTO()
			                .getId().equals(318L)) {
			    List<VariableProcesoDTO> hijas = catalogoServicio
				    .obtenerVariablesHijas(valorVariableDTO
				            .getVariableProcesoDTO().getId());
			    List<ValorVariableDTO> resultado = evaluacionServicio
				    .obtenerValorVariableMuestraEstratificada(
				            valorVariableDTO
				                    .getVariableProcesoDTO(),
				            hijas, this.informacionIesDTO
				                    .getId(), auditoriaDTO,
				            faseIesDTO);
			    listaValoresVariablesEvaluador.addAll(resultado);
			} else {
			    listaValoresVariablesEvaluador
				    .add(valorVariableDTO);
			}
		    }
		}
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al obtener datos de variables. Comuníquese con el administrador.");
	}

    }

    public void obtenerDetalle(ValorVariableDTO valorVariable) {
	valorVariableSeleccionada = valorVariable;
	String tablaVariable = valorVariableSeleccionada
	        .getVariableProcesoDTO().getVariableDTO().getTablaMuestra();
	if (tablaVariable.equals("docentes")
	        || tablaVariable.equals("estudiantes")) {
	    listaPersonas = evaluacionServicio
		    .obtenerPersonasDeListaIdPorVariable(
		            valorVariable.getVariableProcesoDTO(),
		            informacionIesDTO.getId());
	    RequestContext.getCurrentInstance().execute("dlgPersonas.show();");
	}
	if (tablaVariable.equals("proyectos")) {
	    listaProyectos = evaluacionServicio
		    .obtenerProyectosDeListaIdPorVariable(valorVariableSeleccionada);
	    RequestContext.getCurrentInstance().execute("dlgProyectos.show();");
	}
	if (tablaVariable.equals("publicaciones")) {
	    listaPublicaciones = evaluacionServicio
		    .obtenerPublicacionesDeListaIdPorVariable(valorVariableSeleccionada);
	    RequestContext.getCurrentInstance().execute(
		    "dlgPublicaciones.show();");
	}
	if (tablaVariable.equals("actividades_vinculacion")) {
	    listaActividades = evaluacionServicio
		    .obtenerActividadesDeListaIdPorVariable(valorVariableSeleccionada);
	    RequestContext.getCurrentInstance().execute(
		    "dlgActividades.show();");
	}
	if (tablaVariable.equals("convenios")) {
	    listaConvenios = evaluacionServicio
		    .obtenerConveniosDeListaIdPorVariable(valorVariableSeleccionada);
	    RequestContext.getCurrentInstance().execute("dlgConvenios.show();");
	}
	if (tablaVariable.equals("producciones")) {
	    listaProducciones = evaluacionServicio
		    .obtenerProduccionesDeListaIdPorVariable(valorVariableSeleccionada);
	    RequestContext.getCurrentInstance().execute(
		    "dlgProducciones.show();");
	}
	if (tablaVariable.equals("informacion_carrera")) {
	    listaCarreras = evaluacionServicio
		    .obtenerCarrerasDeListaIdPorVariable(valorVariableSeleccionada);
	    RequestContext.getCurrentInstance().execute("dlgCarreras.show();");
	}

    }

    public String cambiarFase() {

	try {
	    AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
	    auditoriaDTO.setFechaModificacion(new Date());
	    auditoriaDTO.setUsuarioModificacion(usuario);
	    // obtenerDatosIniciales();
	    this.fasesSecuencia = catalogoServicio.obtenerSecuenciaDeFase(
		    faseIesDTO.getFaseProcesoDTO().getId(),
		    procesoActual.getId());

	    FaseIesDTO faseActual = catalogoServicio.obtenerFaseIesActual(
		    this.informacionIesDTO.getIes().getId(),
		    this.procesoActual.getId(), new Date());
	    if (!faseActual.getFaseDTO().getId()
		    .equals(FaseSecuenciaEnum.EVALUACION.getValueId())) {
		return "cambiarFase";
	    }

	    for (FaseSecuenciaDTO faseSecDTO : fasesSecuencia) {
		if (faseSecDTO.getTipoCambioFase().compareTo(
		        TipoFaseEnum.PRORROGA) != 0
		        && !faseSecDTO
		                .getFaseSiguienteDTO()
		                .getFaseDTO()
		                .getId()
		                .equals(FaseSecuenciaEnum.REGISTRO.getValueId())
		        && !faseSecDTO
		                .getFaseSiguienteDTO()
		                .getFaseDTO()
		                .getId()
		                .equals(FaseSecuenciaEnum.CARGA_EVIDENCIAS
		                        .getValueId())) {
		    FaseIesDTO nuevaFase = faseIesDTO;
		    faseIesDTO.setFechaFin(new Date());
		    faseIesDTO.setAuditoriaDTO(auditoriaDTO);
		    catalogoServicio.registrarCambioFase(faseIesDTO);
		    nuevaFase.setId(null);
		    nuevaFase.setFechaInicio(new Date());
		    if (faseIesDTO.getTipo().compareTo(TipoFaseEnum.PRORROGA) == 0) {
			nuevaFase.setTipo(TipoFaseEnum.PRORROGA);
		    } else {
			nuevaFase.setFechaFin(faseSecDTO.getFaseSiguienteDTO()
			        .getFechaFin());
		    }
		    nuevaFase.setFaseDTO(faseSecDTO.getFaseSiguienteDTO()
			    .getFaseDTO());
		    nuevaFase.setFaseProcesoDTO(faseSecDTO
			    .getFaseSiguienteDTO());
		    this.faseIesDTO = catalogoServicio
			    .registrarCambioFase(nuevaFase);
		    ListaIesController controller = (ListaIesController) FacesContext
			    .getCurrentInstance().getExternalContext()
			    .getSessionMap().get("listaIesController");
		    controller.cargarMenus();
		    break;
		}
	    }
	} catch (ServicioException e) {
	    JsfUtil.msgError("Error al finalizar registro. Consulte con el administrador.");
	    e.printStackTrace();
	}

	return "cambiarFase";
    }

    public String rectificarVerificacion() {

	try {
	    AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
	    auditoriaDTO.setFechaModificacion(new Date());
	    auditoriaDTO.setUsuarioModificacion(usuario);
	    // obtenerDatosIniciales();
	    this.fasesSecuencia = catalogoServicio.obtenerSecuenciaDeFase(
		    faseIesDTO.getFaseProcesoDTO().getId(),
		    procesoActual.getId());

	    FaseIesDTO faseActual = catalogoServicio.obtenerFaseIesActual(
		    this.informacionIesDTO.getIes().getId(),
		    this.procesoActual.getId(), new Date());
	    if (!faseActual
		    .getFaseDTO()
		    .getId()
		    .equals(FaseSecuenciaEnum.VERIFICACION_RESULTADOS
		            .getValueId())) {
		return "cambiarFase";
	    }

	    for (FaseSecuenciaDTO faseSecDTO : fasesSecuencia) {
		if (faseSecDTO.getTipoCambioFase().compareTo(
		        TipoFaseEnum.PRORROGA) != 0
		        && !faseSecDTO
		                .getFaseSiguienteDTO()
		                .getFaseDTO()
		                .getId()
		                .equals(FaseSecuenciaEnum.REGISTRO.getValueId())
		        && !faseSecDTO
		                .getFaseSiguienteDTO()
		                .getFaseDTO()
		                .getId()
		                .equals(FaseSecuenciaEnum.CARGA_EVIDENCIAS
		                        .getValueId())) {
		    FaseIesDTO nuevaFase = faseIesDTO;
		    faseIesDTO.setFechaFin(new Date());
		    faseIesDTO.setAuditoriaDTO(auditoriaDTO);
		    catalogoServicio.registrarCambioFase(faseIesDTO);
		    nuevaFase.setId(null);
		    nuevaFase.setFechaInicio(new Date());
		    if (faseIesDTO.getTipo().compareTo(TipoFaseEnum.PRORROGA) == 0) {
			nuevaFase.setTipo(TipoFaseEnum.PRORROGA);
		    } else {
			nuevaFase.setFechaFin(faseSecDTO.getFaseSiguienteDTO()
			        .getFechaFin());
		    }
		    nuevaFase.setFaseDTO(faseSecDTO.getFaseSiguienteDTO()
			    .getFaseDTO());
		    nuevaFase.setFaseProcesoDTO(faseSecDTO
			    .getFaseSiguienteDTO());
		    this.faseIesDTO = catalogoServicio
			    .registrarCambioFase(nuevaFase);
		    ListaIesController controller = (ListaIesController) FacesContext
			    .getCurrentInstance().getExternalContext()
			    .getSessionMap().get("listaIesController");
		    controller.cargarMenus();
		    break;
		}
	    }
	} catch (ServicioException e) {
	    JsfUtil.msgError("Error al finalizar registro. Consulte con el administrador.");
	    e.printStackTrace();
	}

	return "cambiarFase";
    }

    private void cargarPropiedadesReporte(JRXlsExporter exporter) {
	SimpleXlsReportConfiguration configuracionExcel = new SimpleXlsReportConfiguration();
	// configuracionExcel.setDetectCellType(Boolean.TRUE);
	// configuracionExcel.setOnePagePerSheet(Boolean.FALSE);
	// configuracionExcel.setWhitePageBackground(Boolean.FALSE);
	// configuracionExcel.setRemoveEmptySpaceBetweenColumns(Boolean.TRUE);
	// configuracionExcel.setRemoveEmptySpaceBetweenRows(Boolean.TRUE);
	// configuracionExcel.setIgnoreCellBorder(Boolean.FALSE);
	exporter.setConfiguration(configuracionExcel);
    }

    public void generarReporteValores() {
	HttpServletRequest request = (HttpServletRequest) FacesContext
	        .getCurrentInstance().getExternalContext().getRequest();

	ByteArrayOutputStream outReporte = new ByteArrayOutputStream();
	String pathJasperReporte = request.getSession().getServletContext()
	        .getRealPath("")
	        + "/reportesJasper/variables/cuantitativasPorInformacionIes.jasper";

	Map<String, Object> parametrosReporte = new HashMap<>();

	String pathLogo = request.getSession().getServletContext()
	        .getRealPath("")
	        + "/images/logo_ceaaces.png";
	parametrosReporte.put("par_logo", pathLogo);
	parametrosReporte.put("ies", informacionIesDTO.getIes().getNombre());

	// parametrosReporte.put("SUBREPORT_DIR", request.getSession()
	// .getServletContext().getRealPath("")
	// + "/reportesJasper/mallasGrafico/");
	try {

	    JRDataSource dataSourceMallas = new JRBeanCollectionDataSource(
		    listaValoresVariables);
	    if (dataSourceMallas != null) {
		JRXlsExporter exporter = new JRXlsExporter();
		// cargarPropiedadesReporte(exporter);

		JasperPrint jasperPrint = JasperFillManager.fillReport(
		        pathJasperReporte, parametrosReporte, dataSourceMallas);
		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(
		        outReporte));

		exporter.exportReport();
		reporteBytes = outReporte.toByteArray();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al generar el reporte, comuníquese con el administrador del sistema");
	}
    }

    public void generarReporteEvaluador() {
	HttpServletRequest request = (HttpServletRequest) FacesContext
	        .getCurrentInstance().getExternalContext().getRequest();

	ByteArrayOutputStream outReporte = new ByteArrayOutputStream();
	String pathJasperReporte = request.getSession().getServletContext()
	        .getRealPath("")
	        + "/reportesJasper/variables/cuantitativasPorInformacionIes.jasper";

	Map<String, Object> parametrosReporte = new HashMap<>();

	String pathLogo = request.getSession().getServletContext()
	        .getRealPath("")
	        + "/images/logo_ceaaces.png";
	parametrosReporte.put("par_logo", pathLogo);
	parametrosReporte.put("ies", informacionIesDTO.getIes().getNombre());

	// parametrosReporte.put("SUBREPORT_DIR", request.getSession()
	// .getServletContext().getRealPath("")
	// + "/reportesJasper/mallasGrafico/");
	try {

	    JRDataSource dataSourceMallas = new JRBeanCollectionDataSource(
		    listaValoresVariablesEvaluador);
	    if (dataSourceMallas != null) {
		JRXlsExporter exporter = new JRXlsExporter();
		cargarPropiedadesReporte(exporter);

		JasperPrint jasperPrint = JasperFillManager.fillReport(
		        pathJasperReporte, parametrosReporte, dataSourceMallas);
		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(
		        outReporte));

		exporter.exportReport();
		reporteBytes = outReporte.toByteArray();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al generar el reporte, comuníquese con el administrador del sistema");
	}
    }

    public void presentarReporteXls() {
	HttpServletResponse response = (HttpServletResponse) FacesContext
	        .getCurrentInstance().getExternalContext().getResponse();
	try {
	    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
		    "dd-MM-yyyy");
	    response.setContentType("application/vnd.ms-excel");
	    response.setHeader(
		    "Content-Disposition",
		    "attachment; filename=valores_"
		            + simpleDateFormat.format(new Date()) + ".xls");
	    response.setContentLength(reporteBytes.length);
	    ServletOutputStream out = response.getOutputStream();
	    out.write(reporteBytes, 0, reporteBytes.length);
	    out.flush();
	    FacesContext.getCurrentInstance().responseComplete();
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al generar el reporte, comuníquese con el administrador del sistema");
	}
    }

    public void presentarReporteEvaluadorXls() {
	HttpServletResponse response = (HttpServletResponse) FacesContext
	        .getCurrentInstance().getExternalContext().getResponse();
	try {
	    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
		    "dd-MM-yyyy");
	    response.setContentType("application/vnd.ms-excel");
	    response.setHeader(
		    "Content-Disposition",
		    "attachment; filename=valores_"
		            + simpleDateFormat.format(new Date()) + ".xls");
	    response.setContentLength(reporteBytes.length);
	    ServletOutputStream out = response.getOutputStream();
	    out.write(reporteBytes, 0, reporteBytes.length);
	    out.flush();
	    FacesContext.getCurrentInstance().responseComplete();
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al generar el reporte, comuníquese con el administrador del sistema");
	}
    }

    public List<ValorVariableDTO> getListaValoresVariables() {
	return listaValoresVariables;
    }

    public void setListaValoresVariables(
	    List<ValorVariableDTO> listaValoresVariables) {
	this.listaValoresVariables = listaValoresVariables;
    }

    public List<VariableProcesoDTO> getListaVariablesProceso() {
	return listaVariablesProceso;
    }

    public void setListaVariablesProceso(
	    List<VariableProcesoDTO> listaVariablesProceso) {
	this.listaVariablesProceso = listaVariablesProceso;
    }

    public List<PersonaDTO> getListaPersonas() {
	return listaPersonas;
    }

    public void setListaPersonas(List<PersonaDTO> listaPersonas) {
	this.listaPersonas = listaPersonas;
    }

    public List<PublicacionDTO> getListaPublicaciones() {
	return listaPublicaciones;
    }

    public void setListaPublicaciones(List<PublicacionDTO> listaPublicaciones) {
	this.listaPublicaciones = listaPublicaciones;
    }

    public List<ProduccionDTO> getListaProducciones() {
	return listaProducciones;
    }

    public void setListaProducciones(List<ProduccionDTO> listaProducciones) {
	this.listaProducciones = listaProducciones;
    }

    public List<ProyectoDTO> getListaProyectos() {
	return listaProyectos;
    }

    public void setListaProyectos(List<ProyectoDTO> listaProyectos) {
	this.listaProyectos = listaProyectos;
    }

    public List<ActividadVinculacionDTO> getListaActividades() {
	return listaActividades;
    }

    public void setListaActividades(
	    List<ActividadVinculacionDTO> listaActividades) {
	this.listaActividades = listaActividades;
    }

    public List<ConvenioVigenteDTO> getListaConvenios() {
	return listaConvenios;
    }

    public void setListaConvenios(List<ConvenioVigenteDTO> listaConvenios) {
	this.listaConvenios = listaConvenios;
    }

    public List<InformacionCarreraDTO> getListaCarreras() {
	return listaCarreras;
    }

    public void setListaCarreras(List<InformacionCarreraDTO> listaCarreras) {
	this.listaCarreras = listaCarreras;
    }

    public ProcesoDTO getProcesoActual() {
	return procesoActual;
    }

    public void setProcesoActual(ProcesoDTO procesoActual) {
	this.procesoActual = procesoActual;
    }

    public InformacionIesDTO getInformacionIesDTO() {
	return informacionIesDTO;
    }

    public void setInformacionIesDTO(InformacionIesDTO informacionIesDTO) {
	this.informacionIesDTO = informacionIesDTO;
    }

    public FaseIesDTO getFaseIesDTO() {
	return faseIesDTO;
    }

    public void setFaseIesDTO(FaseIesDTO faseIesDTO) {
	this.faseIesDTO = faseIesDTO;
    }

    public String getUsuario() {
	return usuario;
    }

    public void setUsuario(String usuario) {
	this.usuario = usuario;
    }

    public ValorVariableDTO getValorVariableSeleccionada() {
	return valorVariableSeleccionada;
    }

    public void setValorVariableSeleccionada(
	    ValorVariableDTO valorVariableSeleccionada) {
	this.valorVariableSeleccionada = valorVariableSeleccionada;
    }

    public List<ValorVariableDTO> getListaValoresVariablesEvaluador() {
	return listaValoresVariablesEvaluador;
    }

    public void setListaValoresVariablesEvaluador(
	    List<ValorVariableDTO> listaValoresVariablesEvaluador) {
	this.listaValoresVariablesEvaluador = listaValoresVariablesEvaluador;
    }

    public List<VariableGrupoDTO> getVariablesGrupo() {
	return variablesGrupo;
    }

    public void setVariablesGrupo(List<VariableGrupoDTO> variablesGrupo) {
	this.variablesGrupo = variablesGrupo;
    }

}
