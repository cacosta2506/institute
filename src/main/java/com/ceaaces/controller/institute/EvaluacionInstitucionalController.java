package ec.gob.ceaaces.controller;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.text.DateFormat;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsReportConfiguration;

import org.apache.commons.lang.SerializationUtils;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import ec.gob.ceaaces.catalogo.dtos.catalogos.AuditoriaDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.CarreraIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.ConceptoDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.IesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.ParametroDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.VariableDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.VariableProcesoDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.VariableValoracionDTO;
import ec.gob.ceaaces.catalogo.enumeraciones.GrupoConceptoEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.ParametroEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.TipoReferenciaEnum;
import ec.gob.ceaaces.catalogo.exceptions.ServicioException;
import ec.gob.ceaaces.data.ItemEvaluacion;
import ec.gob.ceaaces.data.MuestraDetalle;
import ec.gob.ceaaces.institutos.dtos.evaluacion.CriterioEvaluarDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaConceptoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.MallaCurricularDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.MuestraDetalleDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ResultadoCriteriosDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ValorVariableCualitativaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ValorVariableDTO;
import ec.gob.ceaaces.institutos.dtos.historico.MuestraDetalleHistoricoDTO;
import ec.gob.ceaaces.institutos.dtos.historico.ValorVariableCualitativaHistoricoDTO;
import ec.gob.ceaaces.institutos.dtos.historico.ValorVariableHistoricoDTO;
import ec.gob.ceaaces.institutos.enumeraciones.OrigenInformacionEnum;
import ec.gob.ceaaces.institutos.enumeraciones.TipoOrigenEvidenciaEnum;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.EvaluacionServicio;
import ec.gob.ceaaces.services.InstitutosServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.ArchivoUtil;
import ec.gob.ceaaces.util.JsfUtil;

@ManagedBean(name = "evaluacionInstitucionalController")
public class EvaluacionInstitucionalController implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1711633276053493785L;

    private static final Logger LOG = Logger
	    .getLogger(EvaluacionInstitucionalController.class.getSimpleName());

    @Autowired
    private RegistroServicio registroServicio;

    @Autowired
    private EvaluacionServicio evaluacionServicio;

    @Autowired
    private CatalogoServicio catalogoServicio;

    @Autowired
    private InstitutosServicio institutosServicio;

    private IesDTO ies;
    private InformacionIesDTO informacionIes;
    private FaseIesDTO faseIesDTO;
    private ValorVariableDTO valorVariableDTO;
    private ValorVariableCualitativaDTO valorVariableCualitativaDTO;
    private VariableProcesoDTO variableProcesoDTO;
    private MuestraDetalleDTO muestraDetalleDTO;
    private ResultadoCriteriosDTO resultadoCriteriosDTO;
    private CarreraIesDTO carreraIesDTO;
    private EvidenciaDTO evidenciaSeleccionada;
    private String fichero;
    private Boolean mostrarCEvidencias = false;
    private String url;
    private String extensionDocumento;
    private Boolean descargar = false;
    private byte[] reporteBytes = null;

    private List<ValorVariableCualitativaDTO> listaVariablesInstitucional;
    private List<ItemEvaluacion> listaItemsModificados;
    private List<ValorVariableCualitativaDTO> listaValorVariablesCualitativasModificados;
    private List<ResultadoCriteriosDTO> listaResultadosCriteriosModificados;
    private List<ResultadoCriteriosDTO> listaResultadosCriteriosHistorico;
    private List<ResultadoCriteriosDTO> listaCriterios; // Criterios Evaluación
    private List<ValorVariableHistoricoDTO> historicosVariable;
    private List<ValorVariableCualitativaHistoricoDTO> historicosVariableCualitativa;
    private List<MuestraDetalleHistoricoDTO> historicoMuestraDetalle;
    private List<EvidenciaConceptoDTO> listaEvidenciaConcepto;

    private boolean mostrarEvidencias;

    private String idProceso;
    private String usuario;
    private Date fechaActual = new Date();
    private Boolean alertaIngresoVerificado = false;
    private Boolean alertaCriterios = false;
    private Boolean alertaHistorico = false;
    private ConceptoDTO conceptoInstitucional;
    private ConceptoDTO conceptoEvaluacion;
    private UploadedFile file;
    private EvidenciaDTO evidenciaDto;
    private EvidenciaConceptoDTO evidenciaConceptoSeleccionado;
    private ParametroDTO pDTO;
    private ParametroDTO peDTO;
    private MallaCurricularDTO mallaDTO;

    public EvaluacionInstitucionalController() {

	this.valorVariableDTO = new ValorVariableDTO();
	this.valorVariableCualitativaDTO = new ValorVariableCualitativaDTO();
	this.variableProcesoDTO = new VariableProcesoDTO();
	this.muestraDetalleDTO = new MuestraDetalleDTO();
	this.carreraIesDTO = new CarreraIesDTO();
	this.resultadoCriteriosDTO = new ResultadoCriteriosDTO();
	this.historicosVariable = new ArrayList<>();
	this.historicoMuestraDetalle = new ArrayList<>();
	this.listaEvidenciaConcepto = new ArrayList<EvidenciaConceptoDTO>();
	this.historicosVariableCualitativa = new ArrayList<>();
	this.listaValorVariablesCualitativasModificados = new ArrayList<>();
	this.listaItemsModificados = new ArrayList<>();
	this.listaVariablesInstitucional = new ArrayList<>();
	this.listaCriterios = new ArrayList<>();
	this.listaResultadosCriteriosModificados = new ArrayList<>();
	this.listaResultadosCriteriosHistorico = new ArrayList<>();
	this.pDTO = new ParametroDTO();
	this.peDTO = new ParametroDTO();
	this.evidenciaDto = new EvidenciaDTO();
	this.evidenciaSeleccionada = new EvidenciaDTO();
	this.evidenciaConceptoSeleccionado = new EvidenciaConceptoDTO();
    }

    @PostConstruct
    public void datosIniciales() {
	try {
	    ListaIesController controller = (ListaIesController) JsfUtil
		    .obtenerObjetoSesion("listaIesController");
	    idProceso = controller.getFaseIesDTO().getProcesoDTO().getId()
		    .toString();

	    this.usuario = SecurityContextHolder.getContext()
		    .getAuthentication().getName();

	    this.ies = controller.getIes();
	    this.faseIesDTO = controller.getFaseIesDTO();

	    this.informacionIes = registroServicio
		    .obtenerInformacionIesPorIes(ies);

	    conceptoInstitucional = catalogoServicio
		    .obtenerConceptoPorOrigenYGrupo(
		            OrigenInformacionEnum.INFORMACION_IES.getValor(),
		            "INSTITUCIONAL");
	    conceptoEvaluacion = catalogoServicio
		    .obtenerConceptoPorOrigenYGrupo(
		            OrigenInformacionEnum.INFORMACION_IES.getValor(),
		            "EVALUACION");

	    pDTO = catalogoServicio.obtenerParametroPorCodigoYReferencia(
		    ParametroEnum.URL_CARGA_EVID.getValor(),
		    TipoReferenciaEnum.INST.getValor());
	    peDTO = catalogoServicio.obtenerParametroPorCodigoYReferencia(
		    ParametroEnum.URL_CARGA_EVID_ELIMINAR.getValor(),
		    TipoReferenciaEnum.INST.getValor());

	    cargarVariables();

	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}

    }

    /**
     * Cargar la Lista de Variables por Grupo
     * 
     * @author eviscarra
     * @see calcularTotalVerificadoVariable
     */
    public String cargarVariables() {
	try {

	    /* Variables Cualitativas Institucional */
	    this.listaVariablesInstitucional = evaluacionServicio
		    .obtenerInformacionVariablesCualitativa("IES",
		            informacionIes.getId(), "CUALITATIVA");

	    if (listaVariablesInstitucional == null
		    || listaVariablesInstitucional.isEmpty()) {
		List<VariableDTO> listaVariables = catalogoServicio
		        .obtenerVariablesPorProcesoYTipo(new Long(idProceso),
		                "CUALITATIVA", "IES");
		cargarVariablesCualitativas(listaVariables,
		        listaVariablesInstitucional);
	    }

	    for (ValorVariableCualitativaDTO variable : listaVariablesInstitucional) {
		listaValorVariablesCualitativasModificados
		        .add((ValorVariableCualitativaDTO) SerializationUtils
		                .clone(variable));
	    }

	    ValorVariableDTO valorVariable = new ValorVariableDTO();
	    valorVariable.setVariableProcesoDTO(variableProcesoDTO);

	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}

	return "";
    }

    private void cargarVariablesCualitativas(List<VariableDTO> listaVariables,
	    List<ValorVariableCualitativaDTO> listaValorVariable)
	    throws ServicioException {
	LOG.info("Ies: " + ies.getCodigo());
	LOG.info("Usuario: " + usuario);
	LOG.info(new Date() + "--> Inicializando criterios cualitativas");
	AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
	auditoriaDTO.setFechaModificacion(new Date());
	auditoriaDTO.setUsuarioModificacion(usuario);
	for (VariableDTO variableDTO : listaVariables) {
	    ValorVariableCualitativaDTO valorVariable = new ValorVariableCualitativaDTO();
	    valorVariable.setActivo(true);
	    valorVariable.setFaseIesDTO(faseIesDTO);
	    valorVariable.setAuditoriaDTO(auditoriaDTO);
	    valorVariable.setIdInformacionIes(informacionIes.getId());

	    VariableValoracionDTO variableValoracion = catalogoServicio
		    .obtenerVariableValoracion(variableDTO.getId(), new Long(
		            idProceso));
	    valorVariable.setVariableValoracion(variableValoracion);

	    evaluacionServicio.registrarValorVariableCualitativa(valorVariable);
	    listaValorVariable.add(valorVariable);

	    List<CriterioEvaluarDTO> criterios = evaluacionServicio
		    .obtenerCriteriosPorVariableValoracion(variableValoracion
		            .getId());
	    for (CriterioEvaluarDTO criterio : criterios) {
		ResultadoCriteriosDTO resultadoCriterio = new ResultadoCriteriosDTO();
		resultadoCriterio.setActivo(true);
		resultadoCriterio.setAuditoria(auditoriaDTO);
		resultadoCriterio.setFaseIesDTO(faseIesDTO);
		resultadoCriterio.setIdFaseIes(faseIesDTO.getId());
		resultadoCriterio.setCriterioEvaluarDTO(criterio);
		resultadoCriterio.setCumple("");
		resultadoCriterio.setObservacion("");
		evaluacionServicio
		        .registrarResultadoCriterio(resultadoCriterio);
	    }
	}
    }

    /**
     * Método para llenar el cálculo de Si/No, Completo/Incompleto de las
     * Variables con Muestra
     * 
     * @author eviscarra
     * @param ListaVV
     *            Lista de tipo ValorVariableDTO
     * @param lista
     *            Lista de tipo ItemMuestraDetalle
     */
    public void calcularTotalVerificadoVariable(List<ValorVariableDTO> ListaVV,
	    List<ItemEvaluacion> lista) {
	lista.clear();
	try {

	    for (int i = 0; i < ListaVV.size(); i++) {
		if (!ListaVV.get(i).getVariableProcesoDTO()
		        .getMuestraEstratificada()) {
		    int countSi = 0;
		    int countNo = 0;

		    if (ListaVV.get(i).getCodigoMuestra() != null) {
			countSi = evaluacionServicio.obtenerValoresMuestra("1",
			        ListaVV.get(i).getCodigoMuestra());
			countNo = evaluacionServicio.obtenerValoresMuestra("0",
			        ListaVV.get(i).getCodigoMuestra());
		    }

		    MuestraDetalle md = new MuestraDetalle();
		    md.setContadorSi(countSi);
		    md.setContadorNo(countNo);
		    if (ListaVV.get(i).getTotalMuestra() != null) {
			if ((countSi + countNo) == ListaVV.get(i)
			        .getTotalMuestra()) {
			    md.setEstadoVerficacion("Completo");
			} else {
			    md.setEstadoVerficacion("Incompleto");
			}
		    }

		    ItemEvaluacion item = new ItemEvaluacion();

		    item.setValorVariable(ListaVV.get(i));
		    item.setMuestraDetalleAux(md);
		    lista.add(item);
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public void cargarCriteriosEvaluacionCualitativas() {
	mostrarEvidencias = false;
	this.alertaCriterios = false;
	try {
	    this.listaCriterios.clear();

	    VariableValoracionDTO variableValoracion = catalogoServicio
		    .obtenerVariableValoracion(valorVariableCualitativaDTO
		            .getVariableValoracion().getVariable().getId(),
		            new Long(idProceso));
	    cargarListaResultadoCriterios(variableValoracion);
	    this.alertaCriterios = true;
	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    private void cargarListaResultadoCriterios(
	    VariableValoracionDTO variableValoracion) throws ServicioException {
	String valoracionAlto = obtenerCriteriosConReglas(variableValoracion
	        .getValoracionAlto());
	String valoraciones = valoracionAlto
	        + obtenerCriteriosConReglas(variableValoracion
	                .getValoracionMedio());
	this.listaCriterios = evaluacionServicio.obtenerInformacionCriterios(
	        this.ies.getId(), variableValoracion.getId(), null);
	for (ResultadoCriteriosDTO resultadoCriterio : listaCriterios) {
	    if (valoraciones.contains(","
		    + resultadoCriterio.getCriterioEvaluarDTO().getCodigo()
		    + ",")) {
		resultadoCriterio.setTipoCampoIngreso("INPUT");
	    }
	}

	listaResultadosCriteriosModificados.clear();
	for (ResultadoCriteriosDTO resultado : listaCriterios) {
	    listaResultadosCriteriosModificados
		    .add((ResultadoCriteriosDTO) SerializationUtils
		            .clone(resultado));
	}
    }

    private String obtenerCriteriosConReglas(String reglas) {
	StringBuilder resultado = new StringBuilder(",");
	String[] valoracionReglas = reglas.split(",");
	for (int i = 0; i < valoracionReglas.length; i++) {
	    if (valoracionReglas[i].contains("<")) {
		String[] reglasSplit = valoracionReglas[i].split("<");
		resultado.append(reglasSplit[1] + ",");
	    }
	}
	return resultado.toString();
    }

    public void guardarObservacionVariablesCualitativas() {
	historicosVariable.clear();
	boolean modificado = false;
	try {
	    for (int i = 0; i < this.listaVariablesInstitucional.size(); i++) {
		ValorVariableCualitativaDTO vvdto = listaVariablesInstitucional
		        .get(i);
		if ((vvdto.getObservacion() != null
		        && !vvdto.getObservacion().isEmpty() && !vvdto
		        .getObservacion().equals(
		                listaValorVariablesCualitativasModificados.get(
		                        i).getObservacion()))
		        || (vvdto.getValor() != null && !vvdto
		                .getValor()
		                .equals(listaValorVariablesCualitativasModificados
		                        .get(i).getValor()))) {
		    AuditoriaDTO adto = new AuditoriaDTO();
		    adto.setFechaModificacion(new Date());
		    adto.setUsuarioModificacion(this.usuario);
		    vvdto.setAuditoriaDTO(adto);
		    vvdto.setFaseIesDTO(this.faseIesDTO);
		    vvdto.setIdInformacionIes(this.informacionIes.getId());
		    vvdto.setVerificarEvidencia(false);
		    modificado = true;
		    LOG.info("Ies: " + ies.getCodigo());
		    LOG.info("Usuario: " + usuario);
		    LOG.info(new Date()
			    + "--> Registrando variable: "
			    + vvdto.getVariableValoracion().getVariable()
			            .getEtiqueta() + " id: " + vvdto.getId());
		    LOG.info(new Date() + "--> Instituto: "
			    + informacionIes.getId());
		    evaluacionServicio.registrarValorVariableCualitativa(vvdto);
		    LOG.info(new Date() + "--> VariableRegistrada: "
			    + vvdto.getId());
		}
	    }
	    listaValorVariablesCualitativasModificados.clear();
	    for (ValorVariableCualitativaDTO variable : listaVariablesInstitucional) {
		listaValorVariablesCualitativasModificados
		        .add((ValorVariableCualitativaDTO) SerializationUtils
		                .clone(variable));
	    }
	    if (modificado) {
		JsfUtil.msgInfo("Registros almacenados correctamente");
	    } else {
		JsfUtil.msgAdvert("No se han realizado cambios sobre las variables.");
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public void guardarResultadosCriteriosVariable() {
	try {
	    AuditoriaDTO adto = new AuditoriaDTO();
	    adto.setFechaModificacion(new Date());
	    adto.setUsuarioModificacion(this.usuario);

	    for (int i = 0; i < this.listaCriterios.size(); i++) {
		ResultadoCriteriosDTO rcdto = listaCriterios.get(i);
		if (rcdto.getCumple().equals("-1")
		        && (!rcdto.getObservacion().equals(""))) {
		    JsfUtil.msgError("Debe seleccionar el campo Cumple");
		    return;
		}
		if (!rcdto.getCumple().equals("-1")
		        && (null == rcdto.getObservacion() || rcdto
		                .getObservacion().equals(""))) {
		    JsfUtil.msgError("Debe ingresar una observación");
		    return;
		}
		if (rcdto.getCumple() != null && !rcdto.getCumple().isEmpty()
		        && !"-1".equals(rcdto.getCumple())) {
		    if (!rcdto.getCumple().equals(
			    listaResultadosCriteriosModificados.get(i)
			            .getCumple())) {
			if (rcdto.getObservacion() == null
			        || rcdto.getObservacion().isEmpty()) {
			    JsfUtil.msgError("Debe ingresar la observación del criterio modificado");
			    return;
			} else if (rcdto.getObservacion().equals(
			        listaResultadosCriteriosModificados.get(i)
			                .getObservacion())) {
			    JsfUtil.msgAdvert("Debe cambiar la observación del criterio modificado");
			    return;
			}
		    }
		}
	    }

	    boolean modificado = false;
	    int contador = 0;
	    boolean bandera = false;
	    for (int i = 0; i < this.listaCriterios.size(); i++) {
		bandera = true;
		ResultadoCriteriosDTO rcdto = listaCriterios.get(i);
		if (rcdto.getCumple().equals("-1")) {
		    LOG.info("rcdto.getID" + rcdto.getId());
		    contador++;
		}

		if (rcdto.getCumple() != null && !rcdto.getCumple().isEmpty()
		        && !"-1".equals(rcdto.getCumple())) {
		    if (!rcdto.getCumple().equals(
			    listaResultadosCriteriosModificados.get(i)
			            .getCumple())) {

			rcdto.setAuditoria(adto);
			rcdto.setIdFaseIes(this.faseIesDTO.getId());
			evaluacionServicio.registrarResultadoCriterio(rcdto);
			modificado = true;
		    } else if (rcdto.getCumple().equals(
			    listaResultadosCriteriosModificados.get(i)
			            .getCumple())
			    && rcdto.getObservacion() != null
			    && !rcdto.getObservacion().isEmpty()
			    && !rcdto.getObservacion().equals(
			            listaResultadosCriteriosModificados.get(i)
			                    .getObservacion())) {
			rcdto.setAuditoria(adto);
			rcdto.setIdFaseIes(this.faseIesDTO.getId());
			evaluacionServicio.registrarResultadoCriterio(rcdto);
		    }
		} else if (rcdto.getObservacion() != null
		        && !rcdto.getObservacion().isEmpty()
		        && !rcdto.getObservacion().equals(
		                listaResultadosCriteriosModificados.get(i)
		                        .getObservacion())) {
		    rcdto.setAuditoria(adto);
		    rcdto.setIdFaseIes(this.faseIesDTO.getId());
		    evaluacionServicio.registrarResultadoCriterio(rcdto);
		}
	    }

	    if (modificado) {

		LOG.info("Ies: " + ies.getCodigo());
		LOG.info("Usuario: " + usuario);
		LOG.info(new Date()
		        + "--> Registrando criterios variable: "
		        + valorVariableCualitativaDTO.getVariableValoracion()
		                .getVariable().getEtiqueta());
		JsfUtil.msgAdvert("Recuerde verificar que la observación de la variable "
		        + valorVariableCualitativaDTO.getVariableValoracion()
		                .getVariable().getEtiqueta()
		        + ", corresponda a la modificación realizada en los criterios.");
	    }

	    cargarCriteriosEvaluacionCualitativas();

	    listaResultadosCriteriosModificados.clear();
	    for (ResultadoCriteriosDTO resultado : listaCriterios) {
		listaResultadosCriteriosModificados
		        .add((ResultadoCriteriosDTO) SerializationUtils
		                .clone(resultado));
	    }

	    this.valorVariableCualitativaDTO.setAuditoriaDTO(adto);
	    this.valorVariableCualitativaDTO.setFaseIesDTO(this.faseIesDTO);

	    if (contador == 0 && bandera) {
		this.valorVariableCualitativaDTO
		        .setValor(calcularValorVariable());
		evaluacionServicio
		        .registrarValorVariableCualitativa(this.valorVariableCualitativaDTO);
		// ACTUALIZA VARIABLES CUALITATIVAS INSTITUCIONAL
		this.listaVariablesInstitucional = evaluacionServicio
		        .obtenerInformacionVariablesCualitativa("IES",
		                informacionIes.getId(), "CUALITATIVA");
		listaValorVariablesCualitativasModificados.clear();
		for (ValorVariableCualitativaDTO variable : listaVariablesInstitucional) {
		    listaValorVariablesCualitativasModificados
			    .add((ValorVariableCualitativaDTO) SerializationUtils
			            .clone(variable));
		}

		JsfUtil.msgInfo("Registros almacenados correctamente");
		LOG.info("Ies: " + ies.getCodigo());
		LOG.info("Usuario: " + usuario);
		LOG.info(new Date()
		        + "--> Registrando variable: "
		        + valorVariableCualitativaDTO.getVariableValoracion()
		                .getVariable().getEtiqueta()
		        + " id: "
		        + valorVariableCualitativaDTO.getVariableValoracion()
		                .getVariable().getId());

	    } else {
		LOG.info("No se registro el valor de la variable. " + contador);
		JsfUtil.msgAdvert("Para obtener el valor verificado de la variable seleccionada debe completar toda la información ");

	    }

	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public String calcularValorVariable() {
	try {
	    // this.listaCriterios.clear();
	    Long idVariable = 0L;
	    if (this.valorVariableCualitativaDTO.getId() != null) {

		idVariable = this.valorVariableCualitativaDTO
		        .getVariableValoracion().getVariable().getId();

	    }
	    if (this.valorVariableDTO.getId() != null) {
		idVariable = this.valorVariableDTO.getVariableProcesoDTO()
		        .getVariableDTO().getId();
	    }
	    LOG.info("idVariableCriterioCalcular" + idVariable);

	    VariableValoracionDTO variableValoracion = catalogoServicio
		    .obtenerVariableValoracion(idVariable, new Long(idProceso));

	    // this.listaCriterios = evaluacionServicio
	    // .obtenerInformacionCriterios(this.ies.getId(),
	    // variableValoracion.getId(),
	    // this.muestraDetalleDTO.getId());

	    if (variableValoracion != null) {
		String resultado = "";
		if (obtenerValoracionVariable(variableValoracion
		        .getValoracionAlto())) {
		    resultado = "ALTO";
		} else if (obtenerValoracionVariable(variableValoracion
		        .getValoracionMedio())) {
		    resultado = "MEDIO";
		} else {
		    resultado = "BAJO";
		}
		cargarListaResultadoCriterios(variableValoracion);
		return resultado;
	    }
	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
	return "";
    }

    public void cargarHistoricoResultadosCriterio(Long idResultadoCriterio) {
	try {
	    this.alertaHistorico = true;
	    this.listaResultadosCriteriosHistorico = evaluacionServicio
		    .obtenerResultadosCriterioHistorico(idResultadoCriterio);
	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public void obtenerHistoricoVariable(Long idValorVariable) {
	historicosVariable.clear();
	listaEvidenciaConcepto.clear();
	historicosVariable = evaluacionServicio
	        .obtenerHistoricoValorVariable(idValorVariable);
    }

    public void obtenerHistoricoVariableCualitativa(
	    ValorVariableCualitativaDTO valorVariableCualitativa) {
	historicosVariableCualitativa.clear();
	listaEvidenciaConcepto.clear();
	historicosVariableCualitativa = evaluacionServicio
	        .obtenerValorVariableCualitativaHistorico(
	                valorVariableCualitativa.getVariableValoracion()
	                        .getId(), informacionIes.getId());
    }

    public void obtenerHistoricoMuestra(Long idMuestraDetalle) {
	historicoMuestraDetalle.clear();
	listaEvidenciaConcepto.clear();
	mostrarEvidencias = false;
	historicoMuestraDetalle = evaluacionServicio
	        .obtenerHistoricoMuestraDetalle(idMuestraDetalle);
    }

    public void cargarListaEvidencias(Long idVariable) {
	alertaCriterios = false;
	try {
	    mostrarEvidencias = true;
	    listaEvidenciaConcepto.clear();
	    listaEvidenciaConcepto = institutosServicio
		    .obtenerEvidenciasVariablesDeIesPorIdTabla(
		            informacionIes.getId(), conceptoInstitucional,
		            ies.getId(), idVariable);
	} catch (Exception e) {
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	    e.printStackTrace();
	}
    }

    public void cargarListaEvidenciasCuantitativa(Long idVariable) {
	alertaCriterios = false;
	try {
	    mostrarCEvidencias = true;
	    listaEvidenciaConcepto.clear();
	    listaEvidenciaConcepto = institutosServicio
		    .obtenerEvidenciasVariablesDeIesPorIdTabla(
		            informacionIes.getId(), conceptoInstitucional,
		            ies.getId(), idVariable);
	} catch (Exception e) {
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	    e.printStackTrace();
	}
    }

    public void cargarListaEvidencias(MuestraDetalleDTO muestra,
	    String grupoConcepto) {
	muestraDetalleDTO = muestra;
	alertaCriterios = false;
	try {
	    mostrarEvidencias = true;
	    List<ConceptoDTO> listaEvidencia = catalogoServicio
		    .obtenerConceptoPorIdProcesoYGrupo(idProceso,
		            GrupoConceptoEnum.parse(grupoConcepto).getValor());

	    ConceptoDTO conceptoSeleccionado = null;
	    for (ConceptoDTO concepto : listaEvidencia) {
		if (concepto.getOrigen().equals(
		        valorVariableDTO.getVariableProcesoDTO()
		                .getVariableDTO().getTablaMuestra())) {
		    if ("proyectos".equals(valorVariableDTO
			    .getVariableProcesoDTO().getVariableDTO()
			    .getTablaMuestra())) {
			if ("PRYVIN".equals(concepto.getCodigo())) {
			    conceptoSeleccionado = concepto;
			} else if ("PRYINV".equals(concepto.getCodigo())
			        && valorVariableDTO.getVariableProcesoDTO()
			                .getDescripcion().toLowerCase()
			                .contains("alineados")) {
			    conceptoSeleccionado = concepto;
			}
		    } else {
			conceptoSeleccionado = concepto;
		    }
		}
	    }

	    listaEvidenciaConcepto.clear();
	    if (conceptoSeleccionado != null) {
		listaEvidenciaConcepto = institutosServicio
		        .obtenerEvidenciasDeIesPorIdConceptoEIdTabla(
		                ies.getId(), conceptoSeleccionado.getId(),
		                muestraDetalleDTO.getIdTabla(),
		                valorVariableDTO.getVariableProcesoDTO()
		                        .getVariableDTO().getTablaMuestra());
	    }
	} catch (ServicioException e) {
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	    e.printStackTrace();
	}
    }

    public void enviarEvidencia(EvidenciaDTO evidencia) {
	fichero = evidencia.getNombreArchivo();
	url = evidencia.getUrl();
	String[] nombreFichero = evidencia.getNombreArchivo().split("[.]");

	extensionDocumento = nombreFichero[nombreFichero.length - 1];
	descargar = true;
	return;

    }

    public StreamedContent getDocumentoDescarga() {
	if (descargar) {
	    return ArchivoUtil
		    .obtenerDescarga(url, fichero, extensionDocumento);
	} else {
	    JsfUtil.msgError("No se pudo descargar el documento");
	    return null;
	}
    }

    private boolean obtenerValoracionVariable(String valoracion) {
	LOG.info(this.valorVariableDTO.getVariableProcesoDTO().getVariableDTO()
	        .getEtiqueta());
	String[] valoracionReglas = valoracion.split(",");
	String obligatorios = "";
	int numeroReglas = 0;
	List<String[]> reglas = new ArrayList<>();
	int cumpleAlMenos = 0;
	for (int i = 0; i < valoracionReglas.length; i++) {
	    if (valoracionReglas[i].contains("+")) {
		cumpleAlMenos = Integer.parseInt(valoracionReglas[i].replace(
		        "+", ""));
	    } else if (valoracionReglas[i].contains("<")) {
		String[] reglasSplit = valoracionReglas[i].split("<");
		reglas.add(reglasSplit);
	    } else {
		obligatorios += valoracionReglas[i] + ",";
	    }
	}

	numeroReglas = reglas.size();
	int conteoOpcionales = 0;
	for (ResultadoCriteriosDTO resultadoCriterio : this.listaCriterios) {
	    String resultadoCriterioCodigo = resultadoCriterio
		    .getCriterioEvaluarDTO().getCodigo().toString()
		    + ",";
	    if ("1".equals(resultadoCriterio.getCumple())) {
		if (obligatorios.contains(resultadoCriterioCodigo)) {
		    obligatorios = obligatorios.replace(
			    resultadoCriterioCodigo, "");
		} else {
		    conteoOpcionales++;
		}
	    }
	    for (String[] regla : reglas) {
		if (resultadoCriterio.getCumple() != null
		        && new Integer(regla[1]).equals(resultadoCriterio
		                .getCriterioEvaluarDTO().getCodigo())) {
		    if (regla.length > 2) {
			if (new Integer(resultadoCriterio.getCumple()) > new Integer(
			        regla[0])
			        && new Integer(resultadoCriterio.getCumple()) < new Integer(
			                regla[2])) {
			    numeroReglas--;
			}
		    } else {
			if (new Integer(resultadoCriterio.getCumple()) > new Integer(
			        regla[0])) {
			    numeroReglas--;
			}
		    }

		}
	    }
	}
	if (obligatorios.isEmpty() && conteoOpcionales >= cumpleAlMenos
	        && numeroReglas == 0) {
	    return true;
	} else {
	    return false;
	}
    }

    public void uploadfile() {

	if (file != null) {
	    AuditoriaDTO auditoria = new AuditoriaDTO();

	    evidenciaDto.setEvidenciaConceptoDTO(evidenciaConceptoSeleccionado);
	    evidenciaDto.setIdTabla(informacionIes.getId());
	    evidenciaDto.setFaseIesDTO(this.faseIesDTO);

	    evidenciaDto.setTabla(OrigenInformacionEnum.INFORMACION_IES
		    .getValor());

	    Date date = new Date();
	    DateFormat hourdateFormat = new SimpleDateFormat("ddMMYYHHmmss");
	    String[] nameFile = file.getFileName().split("[.]");
	    String extension = nameFile[nameFile.length - 1];
	    long max = 20 * 1024 * 1024;
	    long fileuploadsize = file.getSize();
	    if (fileuploadsize > max) {
		JsfUtil.msgError("El tamaño del archivo excede 10MB");
		return;
	    } else {

		byte[] contenido = this.file.getContents();

		LOG.info("NAME FILE: " + nameFile[0]);

		String nombreGenerado = informacionIes.getIes().getCodigo()
		        + "_" + informacionIes.getId() + "_COD_"
		        + evidenciaConceptoSeleccionado.getId() + "_"
		        + hourdateFormat.format(date).toString() + "."
		        + extension.toString();

		LOG.info("Nombre Generado: " + nombreGenerado);

		String urlNew = pDTO.getValor() + this.faseIesDTO.getId() + "/"
		        + informacionIes.getIes().getCodigo() + "/"
		        + informacionIes.getId() + "/";

		LOG.info("URL A GUARDAR: " + urlNew);
		try {
		    boolean creoArchivo = ArchivoUtil.escribirArchivo(
			    nombreGenerado, contenido, urlNew);
		    if (creoArchivo) {
			evidenciaDto.setNombreOriginal(nameFile[0] + "."
			        + extension);
			evidenciaDto.setNombreArchivo(nombreGenerado);
			evidenciaDto.setUrl(urlNew);
			evidenciaDto.setActivo(true);

			auditoria.setFechaModificacion(new Date());

			auditoria.setUsuarioModificacion(this.usuario);
			evidenciaDto.setAuditoriaDTO(auditoria);
			evidenciaDto
			        .setOrigen(TipoOrigenEvidenciaEnum.EVALUADOR
			                .getValor());
			institutosServicio.crearActualizar(evidenciaDto);

			evidenciaDto = new EvidenciaDTO();
			if (mostrarEvidencias) {
			    cargarListaEvidencias(valorVariableCualitativaDTO
				    .getVariableValoracion().getVariable()
				    .getId());
			}
			if (mostrarCEvidencias) {
			    cargarListaEvidenciasCuantitativa(valorVariableDTO
				    .getVariableProcesoDTO().getVariableDTO()
				    .getId());
			}
			JsfUtil.msgInfo("Evidencia subida correctamente");
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	} else {
	    JsfUtil.msgError("Debe escoger un documento");
	}

    }

    public void eliminarEvidencia() {
	AuditoriaDTO auditoria = new AuditoriaDTO();

	LOG.info(evidenciaSeleccionada.getNombreArchivo());
	String origen = evidenciaSeleccionada.getUrl().toString().trim()
	        + evidenciaSeleccionada.getNombreArchivo().toString().trim();
	LOG.info("ORIGEN: " + origen);
	String destino = peDTO.getValor().toString().trim()
	        + faseIesDTO.getId() + "/"
	        + informacionIes.getIes().getCodigo() + "/"
	        + informacionIes.getId() + "/"
	        + evidenciaSeleccionada.getNombreArchivo().toString().trim();
	String urlDestino = peDTO.getValor().toString().trim()
	        + faseIesDTO.getId() + "/"
	        + informacionIes.getIes().getCodigo() + "/"
	        + informacionIes.getId() + "/";
	LOG.info("DESTINO: " + destino);

	if (ArchivoUtil.crearDirectorio(urlDestino)) {
	    ArchivoUtil.moverArchivo(origen, destino);
	    evidenciaSeleccionada.setActivo(false);
	    evidenciaSeleccionada.setUrl(urlDestino);
	    auditoria.setFechaModificacion(new Date());
	    auditoria.setUsuarioModificacion(this.usuario);
	    evidenciaSeleccionada.setAuditoriaDTO(auditoria);

	    try {
		institutosServicio.crearActualizar(evidenciaSeleccionada);
	    } catch (ServicioException e) {
		e.printStackTrace();
	    }
	    if (mostrarEvidencias) {
		cargarListaEvidencias(valorVariableCualitativaDTO
		        .getVariableValoracion().getVariable().getId());
	    }
	    if (mostrarCEvidencias) {
		cargarListaEvidencias(valorVariableDTO.getVariableProcesoDTO()
		        .getVariableDTO().getId());
	    }
	    JsfUtil.msgInfo("Evidencia eliminada correctamente");
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
		    "attachment; filename=libros_"
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

    public void generarReporteMuestraLibros(ValorVariableDTO valorVariableDTO) {
	HttpServletRequest request = (HttpServletRequest) FacesContext
	        .getCurrentInstance().getExternalContext().getRequest();

	ByteArrayOutputStream outReporte = new ByteArrayOutputStream();
	String pathJasperReporte = request.getSession().getServletContext()
	        .getRealPath("")
	        + "/reportesJasper/variables/muestraDeLibros.jasper";

	Map<String, Object> parametrosReporte = new HashMap<>();

	String pathLogo = request.getSession().getServletContext()
	        .getRealPath("")
	        + "/images/logo_ceaaces.png";
	parametrosReporte.put("par_logo", pathLogo);
	parametrosReporte.put("IES", ies.getNombre());
	parametrosReporte.put("CODIGO_IES", ies.getCodigo());
	parametrosReporte.put("TOTAL_UNIVERSO",
	        valorVariableDTO.getTotalUniverso());
	parametrosReporte.put("TOTAL_MUESTRA",
	        valorVariableDTO.getTotalMuestra());
	parametrosReporte.put("CODIGO_MUESTRA", informacionIes.getId() + "-"
	        + valorVariableDTO.getVariableProcesoDTO().getId());
	try {
	    List<MuestraDetalleDTO> detalle = evaluacionServicio
		    .obtenerMuestraDetalle(informacionIes.getId() + "-"
		            + valorVariableDTO.getVariableProcesoDTO().getId());
	    if (valorVariableDTO.getTotalUniverso() > 0
		    && (detalle == null || detalle.isEmpty())) {
		VariableProcesoDTO vProcesoDTO = catalogoServicio
		        .obtenerVariableProcesoPorId(318L);
		List<VariableProcesoDTO> hijas = catalogoServicio
		        .obtenerVariablesHijas(318L);
		AuditoriaDTO auditoria = new AuditoriaDTO();
		auditoria.setUsuarioModificacion(usuario);
		auditoria.setFechaModificacion(new Date());
		evaluacionServicio.obtenerValorVariableMuestraEstratificada(
		        vProcesoDTO, hijas, informacionIes.getId(), auditoria,
		        faseIesDTO);
	    }
	} catch (Exception exc) {
	    exc.printStackTrace();
	}
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
	    reporteBytes = outReporte.toByteArray();
	    outReporte.flush();
	    outReporte.close();
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al generar el reporte, comuníquese con el administrador del sistema");
	} finally {
	    try {
		if (connection != null) {
		    connection.close();
		}
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
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

    public String getFichero() {
	return fichero;
    }

    public String getUrl() {
	return url;
    }

    public String getExtensionDocumento() {
	return extensionDocumento;
    }

    public Boolean getDescargar() {
	return descargar;
    }

    public IesDTO getIes() {
	return ies;
    }

    public void setIes(IesDTO ies) {
	this.ies = ies;
    }

    public String getUsuario() {
	return usuario;
    }

    public void setUsuario(String usuario) {
	this.usuario = usuario;
    }

    public Date getFechaActual() {
	return fechaActual;
    }

    public void setFechaActual(Date fechaActual) {
	this.fechaActual = fechaActual;
    }

    public InformacionIesDTO getInformacionIes() {
	return informacionIes;
    }

    public void setInformacionIes(InformacionIesDTO informacionIes) {
	this.informacionIes = informacionIes;
    }

    public FaseIesDTO getFaseIesDTO() {
	return faseIesDTO;
    }

    public void setFaseIesDTO(FaseIesDTO faseIesDTO) {
	this.faseIesDTO = faseIesDTO;
    }

    public ValorVariableDTO getValorVariableDTO() {
	return valorVariableDTO;
    }

    public void setValorVariableDTO(ValorVariableDTO valorVariableDTO) {
	this.valorVariableDTO = valorVariableDTO;
    }

    public Boolean getAlertaIngresoVerificado() {
	return alertaIngresoVerificado;
    }

    public void setAlertaIngresoVerificado(Boolean alertaIngresoVerificado) {
	this.alertaIngresoVerificado = alertaIngresoVerificado;
    }

    public List<ValorVariableCualitativaDTO> getListaVariablesInstitucional() {
	return listaVariablesInstitucional;
    }

    public void setListaVariablesInstitucional(
	    List<ValorVariableCualitativaDTO> listaVariablesInstitucional) {
	this.listaVariablesInstitucional = listaVariablesInstitucional;
    }

    public List<ResultadoCriteriosDTO> getListaCriterios() {
	return listaCriterios;
    }

    public void setListaCriterios(List<ResultadoCriteriosDTO> listaCriterios) {
	this.listaCriterios = listaCriterios;
    }

    public VariableProcesoDTO getVariableProcesoDTO() {
	return variableProcesoDTO;
    }

    public void setVariableProcesoDTO(VariableProcesoDTO variableProcesoDTO) {
	this.variableProcesoDTO = variableProcesoDTO;
    }

    public MuestraDetalleDTO getMuestraDetalleDTO() {
	return muestraDetalleDTO;
    }

    public void setMuestraDetalleDTO(MuestraDetalleDTO muestraDetalleDTO) {
	this.muestraDetalleDTO = muestraDetalleDTO;
    }

    public List<ResultadoCriteriosDTO> getListaResultadosCriteriosModificados() {
	return listaResultadosCriteriosModificados;
    }

    public void setListaResultadosCriteriosModificados(
	    List<ResultadoCriteriosDTO> listaResultadosCriteriosModificados) {
	this.listaResultadosCriteriosModificados = listaResultadosCriteriosModificados;
    }

    public Boolean getAlertaCriterios() {
	return alertaCriterios;
    }

    public void setAlertaCriterios(Boolean alertaCriterios) {
	this.alertaCriterios = alertaCriterios;
    }

    public Boolean getAlertaHistorico() {
	return alertaHistorico;
    }

    public void setAlertaHistorico(Boolean alertaHistorico) {
	this.alertaHistorico = alertaHistorico;
    }

    public List<ResultadoCriteriosDTO> getListaResultadosCriteriosHistorico() {
	return listaResultadosCriteriosHistorico;
    }

    public void setListaResultadosCriteriosHistorico(
	    List<ResultadoCriteriosDTO> listaResultadosCriteriosHistorico) {
	this.listaResultadosCriteriosHistorico = listaResultadosCriteriosHistorico;
    }

    public ResultadoCriteriosDTO getResultadoCriteriosDTO() {
	return resultadoCriteriosDTO;
    }

    public void setResultadoCriteriosDTO(
	    ResultadoCriteriosDTO resultadoCriteriosDTO) {
	this.resultadoCriteriosDTO = resultadoCriteriosDTO;
    }

    public CarreraIesDTO getCarreraIesDTO() {
	return carreraIesDTO;
    }

    public void setCarreraIesDTO(CarreraIesDTO carreraIesDTO) {
	this.carreraIesDTO = carreraIesDTO;
    }

    public List<ValorVariableHistoricoDTO> getHistoricosVariable() {
	return historicosVariable;
    }

    public List<MuestraDetalleHistoricoDTO> getHistoricoMuestraDetalle() {
	return historicoMuestraDetalle;
    }

    public List<EvidenciaConceptoDTO> getListaEvidenciaConcepto() {
	return listaEvidenciaConcepto;
    }

    public EvidenciaDTO getEvidenciaSeleccionada() {
	return evidenciaSeleccionada;
    }

    public void setEvidenciaSeleccionada(EvidenciaDTO evidenciaSeleccionada) {
	this.evidenciaSeleccionada = evidenciaSeleccionada;
    }

    public boolean getMostrarEvidencias() {
	return mostrarEvidencias;
    }

    public ValorVariableCualitativaDTO getValorVariableCualitativaDTO() {
	return valorVariableCualitativaDTO;
    }

    public void setValorVariableCualitativaDTO(
	    ValorVariableCualitativaDTO valorVariableCualitativaDTO) {
	this.valorVariableCualitativaDTO = valorVariableCualitativaDTO;
    }

    public List<ValorVariableCualitativaHistoricoDTO> getHistoricosVariableCualitativa() {
	return historicosVariableCualitativa;
    }

    public void setHistoricosVariableCualitativa(
	    List<ValorVariableCualitativaHistoricoDTO> historicosVariableCualitativa) {
	this.historicosVariableCualitativa = historicosVariableCualitativa;
    }

    public ConceptoDTO getConceptoInstitucional() {
	return conceptoInstitucional;
    }

    public void setConceptoInstitucional(ConceptoDTO conceptoInstitucional) {
	this.conceptoInstitucional = conceptoInstitucional;
    }

    public ConceptoDTO getConceptoEvaluacion() {
	return conceptoEvaluacion;
    }

    public void setConceptoEvaluacion(ConceptoDTO conceptoEvaluacion) {
	this.conceptoEvaluacion = conceptoEvaluacion;
    }

    public EvidenciaConceptoDTO getEvidenciaConceptoSeleccionado() {
	return evidenciaConceptoSeleccionado;
    }

    public void setEvidenciaConceptoSeleccionado(
	    EvidenciaConceptoDTO evidenciaConceptoSeleccionado) {
	this.evidenciaConceptoSeleccionado = evidenciaConceptoSeleccionado;
    }

    public List<ValorVariableCualitativaDTO> getListaValorVariablesCualitativasModificados() {
	return listaValorVariablesCualitativasModificados;
    }

    public void setListaValorVariablesCualitativasModificados(
	    List<ValorVariableCualitativaDTO> listaValorVariablesCualitativasModificados) {
	this.listaValorVariablesCualitativasModificados = listaValorVariablesCualitativasModificados;
    }

    public String getIdProceso() {
	return idProceso;
    }

    public void setIdProceso(String idProceso) {
	this.idProceso = idProceso;
    }

    public UploadedFile getFile() {
	return file;
    }

    public void setFile(UploadedFile file) {
	this.file = file;
    }

    public EvidenciaDTO getEvidenciaDto() {
	return evidenciaDto;
    }

    public void setEvidenciaDto(EvidenciaDTO evidenciaDto) {
	this.evidenciaDto = evidenciaDto;
    }

    public ParametroDTO getpDTO() {
	return pDTO;
    }

    public void setpDTO(ParametroDTO pDTO) {
	this.pDTO = pDTO;
    }

    public ParametroDTO getPeDTO() {
	return peDTO;
    }

    public void setPeDTO(ParametroDTO peDTO) {
	this.peDTO = peDTO;
    }

    public void setFichero(String fichero) {
	this.fichero = fichero;
    }

    public void setExtensionDocumento(String extensionDocumento) {
	this.extensionDocumento = extensionDocumento;
    }

    public void setHistoricosVariable(
	    List<ValorVariableHistoricoDTO> historicosVariable) {
	this.historicosVariable = historicosVariable;
    }

    public void setHistoricoMuestraDetalle(
	    List<MuestraDetalleHistoricoDTO> historicoMuestraDetalle) {
	this.historicoMuestraDetalle = historicoMuestraDetalle;
    }

    public void setListaEvidenciaConcepto(
	    List<EvidenciaConceptoDTO> listaEvidenciaConcepto) {
	this.listaEvidenciaConcepto = listaEvidenciaConcepto;
    }

    public void setMostrarEvidencias(boolean mostrarEvidencias) {
	this.mostrarEvidencias = mostrarEvidencias;
    }

    public Boolean getMostrarCEvidencias() {
	return mostrarCEvidencias;
    }

    public void setMostrarCEvidencias(Boolean mostrarCEvidencias) {
	this.mostrarCEvidencias = mostrarCEvidencias;
    }

    public List<ItemEvaluacion> getListaItemsModificados() {
	return listaItemsModificados;
    }

    public void setListaItemsModificados(
	    List<ItemEvaluacion> listaItemsModificados) {
	this.listaItemsModificados = listaItemsModificados;
    }

    public MallaCurricularDTO getMallaDTO() {
	return mallaDTO;
    }

    public void setMallaDTO(MallaCurricularDTO mallaDTO) {
	this.mallaDTO = mallaDTO;
    }

}