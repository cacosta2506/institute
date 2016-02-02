package ec.gob.ceaaces.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.commons.lang.SerializationUtils;
import org.primefaces.context.RequestContext;
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
import ec.gob.ceaaces.catalogo.dtos.catalogos.VariableProcesoDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.VariableValoracionDTO;
import ec.gob.ceaaces.catalogo.enumeraciones.GrupoConceptoEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.ParametroEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.TipoReferenciaEnum;
import ec.gob.ceaaces.catalogo.exceptions.ServicioException;
import ec.gob.ceaaces.data.ItemEvaluacion;
import ec.gob.ceaaces.data.MuestraDetalle;
import ec.gob.ceaaces.institutos.dtos.evaluacion.AsignaturaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.CriterioEvaluarDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaConceptoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionCarreraDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.MallaCurricularDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.MuestraDetalleDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ProgramaEstudiosAsignaturaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ResultadoCriteriosDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ValorVariableCualitativaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ValorVariableDTO;
import ec.gob.ceaaces.institutos.dtos.historico.MuestraDetalleHistoricoDTO;
import ec.gob.ceaaces.institutos.dtos.historico.ValorVariableCualitativaHistoricoDTO;
import ec.gob.ceaaces.institutos.dtos.historico.ValorVariableHistoricoDTO;
import ec.gob.ceaaces.institutos.enumeraciones.OrigenInformacionEnum;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.EvaluacionServicio;
import ec.gob.ceaaces.services.InstitutosServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.ArchivoUtil;
import ec.gob.ceaaces.util.JsfUtil;

@ManagedBean(name = "evaluacionCurriculoController")
public class EvaluacionCurriculoController implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1711633276053493785L;

    private static final Logger LOG = Logger
	    .getLogger(EvaluacionCurriculoController.class.getSimpleName());

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

    private List<ValorVariableDTO> listaVariablesCurriculoPerfiles;
    private List<ValorVariableDTO> listaVariablesCurriculoPEAS;
    private List<ValorVariableDTO> listaVariablesCurriculoAsignaturas;
    private List<ItemEvaluacion> listaEvaluacionCurriculoPEAS;

    private List<ItemEvaluacion> listaMuestraDetalle;

    private List<ItemEvaluacion> listaItemsModificados;
    private List<ValorVariableDTO> variablesModificadosCurriculo;
    private List<ValorVariableDTO> variablesModificadosPeas;
    private List<ValorVariableDTO> variablesModificadosAsignaturas;

    private List<ResultadoCriteriosDTO> listaResultadosCriteriosModificados;
    private List<ResultadoCriteriosDTO> listaResultadosCriteriosHistorico;
    private List<ResultadoCriteriosDTO> listaCriterios; // Criterios Evaluación

    private List<ValorVariableHistoricoDTO> historicosVariable;
    private List<ValorVariableCualitativaHistoricoDTO> historicosVariableCualitativa;
    private List<MuestraDetalleHistoricoDTO> historicoMuestraDetalle;
    private List<EvidenciaConceptoDTO> listaEvidenciaConcepto;

    private InformacionCarreraDTO informacionCarreraDTO = new InformacionCarreraDTO();

    private boolean mostrarEvidencias;

    private String idProceso;
    private String usuario;
    private Date fechaActual = new Date();
    private Boolean alertaIngresoVerificado = false;
    private Boolean alertaCriterios = false;
    private Boolean alertaHistorico = false;
    private ConceptoDTO conceptoEvaluacion;
    private UploadedFile file;
    private EvidenciaDTO evidenciaDto;
    private EvidenciaConceptoDTO evidenciaConceptoSeleccionado;
    private ParametroDTO pDTO;
    private ParametroDTO peDTO;
    private MallaCurricularDTO mallaDTO;

    public EvaluacionCurriculoController() {

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
	this.listaVariablesCurriculoPerfiles = new ArrayList<>();
	this.listaVariablesCurriculoPEAS = new ArrayList<>();
	this.listaEvaluacionCurriculoPEAS = new ArrayList<>();
	this.listaMuestraDetalle = new ArrayList<>();
	this.listaItemsModificados = new ArrayList<>();

	this.listaCriterios = new ArrayList<>();
	this.variablesModificadosCurriculo = new ArrayList<>();
	this.variablesModificadosPeas = new ArrayList<>();
	this.listaResultadosCriteriosModificados = new ArrayList<>();
	this.listaResultadosCriteriosHistorico = new ArrayList<>();
	this.pDTO = new ParametroDTO();
	this.peDTO = new ParametroDTO();
	this.evidenciaDto = new EvidenciaDTO();
	this.evidenciaSeleccionada = new EvidenciaDTO();
	this.evidenciaConceptoSeleccionado = new EvidenciaConceptoDTO();
	this.mallaDTO = new MallaCurricularDTO();

	this.listaVariablesCurriculoAsignaturas = new ArrayList<ValorVariableDTO>();
	this.variablesModificadosAsignaturas = new ArrayList<ValorVariableDTO>();
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

	    /* Variables Curriculo */
	    this.listaVariablesCurriculoPerfiles = evaluacionServicio
		    .obtenerInformacionVariables("CURR",
		            this.informacionIes.getId(), "%CUANTITATIVA%",
		            "perfiles");

	    for (ValorVariableDTO variable : listaVariablesCurriculoPerfiles) {
		variablesModificadosCurriculo
		        .add((ValorVariableDTO) SerializationUtils
		                .clone(variable));
	    }

	    LOG.info("LISTA VARIABLES PERFILES: "
		    + listaVariablesCurriculoPerfiles.size());
	    this.listaVariablesCurriculoPEAS = evaluacionServicio
		    .obtenerInformacionVariables("CURR",
		            this.informacionIes.getId(), "%CUANTITATIVA%",
		            "peas");
	    LOG.info("LISTA VARIABLES PEAS: "
		    + listaVariablesCurriculoPEAS.size());
	    calcularTotalVerificadoVariable(listaVariablesCurriculoPEAS,
		    listaEvaluacionCurriculoPEAS);
	    variablesModificadosPeas.clear();
	    for (ValorVariableDTO variable : listaVariablesCurriculoPEAS) {
		if (variable.getVariableProcesoDTO().getVariablePadreDTO() != null) {
		    variablesModificadosPeas
			    .add((ValorVariableDTO) SerializationUtils
			            .clone(variable));
		}
	    }
	    variablesModificadosAsignaturas.clear();
	    for (ValorVariableDTO variable : listaVariablesCurriculoAsignaturas) {
		variablesModificadosAsignaturas
		        .add((ValorVariableDTO) SerializationUtils
		                .clone(variable));
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}

	return "";
    }

    public void calcularVariablesCurriculo() {
	RequestContext context = RequestContext.getCurrentInstance();
	try {
	    historicosVariable.clear();
	    AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
	    auditoriaDTO.setFechaModificacion(new Date());
	    auditoriaDTO.setUsuarioModificacion(usuario);
	    List<ValorVariableDTO> valoresVariableNuevos = new ArrayList<ValorVariableDTO>();
	    for (ValorVariableDTO valorVariableActual : listaVariablesCurriculoPerfiles) {
		// valorVariableActual.setCodigoMuestra(new
		// Integer(informacionIes
		// .getId() + variableProcesoDTO.getId().toString()));
		valorVariableActual.setAuditoriaDTO(auditoriaDTO);
		valorVariableActual.setFaseIesDTO(faseIesDTO);
		ValorVariableDTO valorVariableNuevo = evaluacionServicio
		        .obtenerValorVariable(valorVariableActual, faseIesDTO,
		                auditoriaDTO, valorVariableActual
		                        .getVariableProcesoDTO()
		                        .getSqlListaId());
		valoresVariableNuevos.add(valorVariableNuevo);
	    }
	    listaVariablesCurriculoPerfiles.clear();
	    listaVariablesCurriculoPerfiles.addAll(valoresVariableNuevos);

	    variablesModificadosPeas.clear();
	    for (ValorVariableDTO variable : listaVariablesCurriculoPEAS) {
		variablesModificadosPeas
		        .add((ValorVariableDTO) SerializationUtils
		                .clone(variable));
	    }
	    // SE AGREGA LÍNEA PARA REALIZAR CÁLCULO DE VARIABLES PEAS EN UN
	    // SOLO BOTÓN.
	    calcularVariablesPeas();
	    context.execute("dlgCalcularVariableCurriculo.hide()");

	} catch (Exception se) {
	    se.printStackTrace();
	    JsfUtil.msgError("Error al calcular las variables. Comuníquese con el Administrador");
	}
    }

    public void calcularVariablesPeas() {
	RequestContext context = RequestContext.getCurrentInstance();
	try {
	    historicosVariable.clear();
	    AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
	    auditoriaDTO.setFechaModificacion(new Date());
	    auditoriaDTO.setUsuarioModificacion(usuario);
	    List<VariableProcesoDTO> otrasVariables = new ArrayList<VariableProcesoDTO>();
	    List<VariableProcesoDTO> hijas = new ArrayList<VariableProcesoDTO>();
	    List<VariableProcesoDTO> padres = new ArrayList<VariableProcesoDTO>();
	    for (ValorVariableDTO valorVariableActual : listaVariablesCurriculoPEAS) {
		if (valorVariableActual.getVariableProcesoDTO()
		        .getVariablePadreDTO() == null) {
		    if (valorVariableActual.getVariableProcesoDTO()
			    .getMuestraEstratificada()) {
			padres.add(valorVariableActual.getVariableProcesoDTO());
		    } else {
			otrasVariables.add(valorVariableActual
			        .getVariableProcesoDTO());
		    }
		}
	    }
	    for (VariableProcesoDTO padre : padres) {
		hijas = catalogoServicio.obtenerVariablesHijas(padre.getId());
		evaluacionServicio.obtenerValorVariableMuestraEstratificada(
		        padre, hijas, this.informacionIes.getId(),
		        auditoriaDTO, faseIesDTO);
	    }
	    for (VariableProcesoDTO varProcesoDTO : otrasVariables) {

		// evaluacionServicio.calcularValorVariableYMuestra(varProcesoDTO,
		// informacionIes.getId(), faseIesDTO, auditoriaDTO);

		ValorVariableDTO vvOld = evaluacionServicio
		        .obtenerUltimoValorVariable(varProcesoDTO
		                .getVariableDTO().getId(), null, informacionIes
		                .getId());
		vvOld = evaluacionServicio
		        .obtenerValorVariable(vvOld, faseIesDTO, auditoriaDTO,
		                varProcesoDTO.getSqlListaId());
		vvOld.setValorInicial(String.valueOf(vvOld.getValorVerificado()));
		vvOld.setValorInicialReportado(vvOld.getValorVerificado());
		vvOld.setAuditoriaDTO(auditoriaDTO);
		vvOld.setFaseIesDTO(faseIesDTO);
		evaluacionServicio.registrarValorVariable(vvOld);

	    }
	    this.listaVariablesCurriculoPEAS = evaluacionServicio
		    .obtenerInformacionVariables("CURR",
		            this.informacionIes.getId(), "%CUANTITATIVA%",
		            "peas");
	    LOG.info("LISTA VARIABLES PEAS: "
		    + listaVariablesCurriculoPEAS.size());
	    calcularTotalVerificadoVariable(listaVariablesCurriculoPEAS,
		    listaEvaluacionCurriculoPEAS);

	    for (ValorVariableDTO variable : listaVariablesCurriculoPEAS) {
		variablesModificadosPeas
		        .add((ValorVariableDTO) SerializationUtils
		                .clone(variable));
	    }

	    context.execute("dlgCalcularVariablePeas.hide()");
	} catch (Exception se) {
	    se.printStackTrace();
	    JsfUtil.msgError("Error al calcular las variables. Comuníquese con el Administrador");
	}

    }

    /**
     * 
     * Obtener Muestra Detalle de Perfiles y Asignaturas
     * 
     * @author eviscarra
     * @version 12/06/2014 - 9:25:19
     */
    public void cargarMuestraDetalleVariablesCurriculo() {
	RequestContext context = RequestContext.getCurrentInstance();
	mostrarEvidencias = false;
	alertaCriterios = false;
	try {
	    this.listaMuestraDetalle.clear();

	    List<MuestraDetalleDTO> listaMuestraDetalle = new ArrayList<>();
	    List<AsignaturaDTO> listaAsignaturas = new ArrayList<>();
	    List<MallaCurricularDTO> listaMallaCurricular = new ArrayList<>();
	    List<InformacionCarreraDTO> listaCarreras = new ArrayList<>();

	    listaMuestraDetalle = evaluacionServicio
		    .obtenerMuestraDetalle(this.valorVariableDTO
		            .getCodigoMuestra());

	    if ("peas".equals(this.valorVariableDTO.getVariableProcesoDTO()
		    .getVariableDTO().getSubGrupo())) {// VARIABLE PEAS

		listaAsignaturas = evaluacionServicio
		        .obtenerMuestraDetalleAsignaturas(this.valorVariableDTO
		                .getCodigoMuestra());

		// GENERA MUESTRA DETALLE DE PEAS

		if (valorVariableDTO.getTotalUniverso() > 0
		        && (listaAsignaturas == null || listaAsignaturas
		                .isEmpty())) {
		    VariableProcesoDTO vProcesoDTO = catalogoServicio
			    .obtenerVariableProcesoPorId(14L);
		    List<VariableProcesoDTO> hijas = catalogoServicio
			    .obtenerVariablesHijas(14L);
		    AuditoriaDTO auditoria = new AuditoriaDTO();
		    auditoria.setUsuarioModificacion(usuario);
		    auditoria.setFechaModificacion(new Date());
		    evaluacionServicio
			    .obtenerValorVariableMuestraEstratificada(
			            vProcesoDTO, hijas, informacionIes.getId(),
			            auditoria, faseIesDTO);
		    listaAsignaturas = evaluacionServicio
			    .obtenerMuestraDetalleAsignaturas(this.valorVariableDTO
			            .getCodigoMuestra());
		}

		for (int i = 0; i < listaAsignaturas.size(); i++) {
		    for (int j = 0; j < listaMuestraDetalle.size(); j++) {
			if (listaMuestraDetalle
			        .get(j)
			        .getIdTabla()
			        .equals(listaAsignaturas.get(i).getPeaDTO()
			                .getId())) {
			    boolean bandera = false;
			    for (ItemEvaluacion itemEvaluacion : this.listaMuestraDetalle) {
				if (listaAsignaturas
				        .get(i)
				        .getPeaDTO()
				        .getId()
				        .equals(itemEvaluacion.getPeas()
				                .getId())) {
				    itemEvaluacion.getPeas()
					    .getAsignaturasDTO()
					    .add(listaAsignaturas.get(i));
				    bandera = true;
				    break;
				}
			    }
			    if (!bandera) {
				ItemEvaluacion item = new ItemEvaluacion();
				// item.setAsignaturaDTO(listaAsignaturas.get(i));
				ProgramaEstudiosAsignaturaDTO pea = listaAsignaturas
				        .get(i).getPeaDTO();
				pea.getAsignaturasDTO().add(
				        listaAsignaturas.get(i));

				item.setPeas(pea);
				item.setMuestraDetalle(listaMuestraDetalle
				        .get(j));

				this.listaMuestraDetalle.add(item);
			    }
			}
		    }
		}
		context.execute("dlgMuestraAsignaturas.show()");
		// PLAN CURRICULAR
	    } else if ("Plan Curricular".equals(this.valorVariableDTO
		    .getVariableProcesoDTO().getVariableDTO().getNombre())) {
		listaMallaCurricular = evaluacionServicio
		        .obtenerMuestraDetalleMallaCurricular(this.valorVariableDTO
		                .getCodigoMuestra());
		for (int i = 0; i < listaMallaCurricular.size(); i++) {
		    for (int j = 0; j < listaMuestraDetalle.size(); j++) {
			if (listaMuestraDetalle.get(j).getIdTabla()
			        .equals(listaMallaCurricular.get(i).getId())) {
			    ItemEvaluacion item = new ItemEvaluacion();

			    item.setMallaCurricularDTO(listaMallaCurricular
				    .get(i));
			    item.setMuestraDetalle(listaMuestraDetalle.get(j));
			    this.listaMuestraDetalle.add(item);
			}
		    }
		}
		context.execute("dlgMuestraMallas.show()");

	    } else if ("Perfiles Egreso".equals(this.valorVariableDTO
		    .getVariableProcesoDTO().getVariableDTO().getNombre())
		    || "Perfiles Consultados".equals(this.valorVariableDTO
		            .getVariableProcesoDTO().getVariableDTO()
		            .getNombre())) {// PERFILES
		listaCarreras = evaluacionServicio
		        .obtenerMuestraDetalleCarreras(this.valorVariableDTO
		                .getCodigoMuestra());

		LOG.info("LISTA CARRERAS: " + listaCarreras.size());

		for (int i = 0; i < listaCarreras.size(); i++) {
		    for (int j = 0; j < listaMuestraDetalle.size(); j++) {
			if (listaMuestraDetalle.get(j).getIdTabla()
			        .equals(listaCarreras.get(i).getId())) {
			    ItemEvaluacion item = new ItemEvaluacion();

			    item.setInformacionCarreraDTO(listaCarreras.get(i));
			    item.setMuestraDetalle(listaMuestraDetalle.get(j));
			    this.listaMuestraDetalle.add(item);
			    LOG.info("listaMuestraDetalle: "
				    + listaMuestraDetalle.size());
			}
		    }
		}
		context.execute("dlgMuestraCarreras.show()");

	    }
	    listaItemsModificados.clear();
	    for (ItemEvaluacion item : this.listaMuestraDetalle) {
		this.listaItemsModificados
		        .add((ItemEvaluacion) SerializationUtils.clone(item));
	    }
	} catch (ServicioException e1) {
	    e1.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public void registrarValoresMuestraDetalles(String seccion) {
	AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
	auditoriaDTO.setFechaModificacion(new Date());
	auditoriaDTO.setUsuarioModificacion(usuario);
	try {
	    if ("PEA".equals(seccion)) {
		for (int i = 0; i < this.listaMuestraDetalle.size(); i++) {
		    ItemEvaluacion vvdto = listaMuestraDetalle.get(i);
		    if (vvdto.getMuestraDetalle().getVerificado() != null) {
			if (!vvdto
			        .getMuestraDetalle()
			        .getVerificado()
			        .equals(listaItemsModificados.get(i)
			                .getMuestraDetalle().getVerificado())) {
			    if (vvdto.getMuestraDetalle().getObservaciones() == null
				    || vvdto.getMuestraDetalle()
				            .getObservaciones().isEmpty()) {
				JsfUtil.msgError("Debe ingresar la observación de la PEA: "
				        + vvdto.getPeas().getCodigo());
				return;
			    } else if (vvdto
				    .getMuestraDetalle()
				    .getObservaciones()
				    .equals(listaItemsModificados.get(i)
				            .getMuestraDetalle()
				            .getObservaciones())) {
				JsfUtil.msgAdvert("Debe cambiar la observación de la PEA: "
				        + vvdto.getPeas().getCodigo());
				return;
			    }
			}
		    }
		}
	    } else {
		for (int i = 0; i < this.listaMuestraDetalle.size(); i++) {
		    ItemEvaluacion vvdto = listaMuestraDetalle.get(i);
		    if (vvdto.getMuestraDetalle().getVerificado() != null) {
			if (!vvdto
			        .getMuestraDetalle()
			        .getVerificado()
			        .equals(listaItemsModificados.get(i)
			                .getMuestraDetalle().getVerificado())) {
			    if (vvdto.getMuestraDetalle().getObservaciones() == null
				    || vvdto.getMuestraDetalle()
				            .getObservaciones().isEmpty()) {
				if ("PLAN-CURRICULAR".equals(seccion)) {
				    JsfUtil.msgError("Debe ingresar la observación del plan: "
					    + vvdto.getMallaCurricularDTO()
					            .getCodigoUnico());
				}
				return;
			    } else if (vvdto
				    .getMuestraDetalle()
				    .getObservaciones()
				    .equals(listaItemsModificados.get(i)
				            .getMuestraDetalle()
				            .getObservaciones())) {
				JsfUtil.msgAdvert("Debe cambiar la observación del proyecto: "
				        + vvdto.getProyecto().getNombre());
				return;
			    }
			}
		    }
		}
	    }

	    boolean modificado = false;
	    for (int i = 0; i < this.listaMuestraDetalle.size(); i++) {
		ItemEvaluacion vvdto = listaMuestraDetalle.get(i);
		if (vvdto.getMuestraDetalle().getVerificado() != null) {
		    if (!vvdto
			    .getMuestraDetalle()
			    .getVerificado()
			    .equals(listaItemsModificados.get(i)
			            .getMuestraDetalle().getVerificado())) {

			listaMuestraDetalle.get(i).getMuestraDetalle()
			        .setAuditoria(auditoriaDTO);
			listaMuestraDetalle.get(i).getMuestraDetalle()
			        .setFaseIesDTO(this.faseIesDTO);
			if ("PEA".equals(seccion)) {
			    listaMuestraDetalle.get(i).getPeas()
				    .setAuditoriaDTO(auditoriaDTO);
			    listaMuestraDetalle.get(i).getPeas()
				    .setVerificarEvidencia(false);
			    registroServicio.registrarPEA(listaMuestraDetalle
				    .get(i).getPeas());
			}
			if ("PLAN-CURRICULAR".equals(seccion)) {
			    listaMuestraDetalle.get(i).getMallaCurricularDTO()
				    .setAuditoria(auditoriaDTO);
			    listaMuestraDetalle.get(i).getMallaCurricularDTO()
				    .setVerificarEvidencia(false);
			    registroServicio
				    .registrarMallaCurricular(listaMuestraDetalle
				            .get(i).getMallaCurricularDTO());
			} else if ("CARRERA-PERFIL".equals(seccion)) {
			    listaMuestraDetalle.get(i)
				    .getInformacionCarreraDTO()
				    .setAuditoriaDTO(auditoriaDTO);
			    listaMuestraDetalle.get(i)
				    .getInformacionCarreraDTO()
				    .setVerificarEvidencia(false);
			    registroServicio
				    .registrarInformacionCarrera(listaMuestraDetalle
				            .get(i).getInformacionCarreraDTO());
			}

			evaluacionServicio
			        .registrarMuestraDetalle(listaMuestraDetalle
			                .get(i).getMuestraDetalle());
			modificado = true;

		    } else if (vvdto
			    .getMuestraDetalle()
			    .getVerificado()
			    .equals(listaItemsModificados.get(i)
			            .getMuestraDetalle().getVerificado())
			    && vvdto.getMuestraDetalle().getObservaciones() != null
			    && !vvdto.getMuestraDetalle().getObservaciones()
			            .isEmpty()
			    && !vvdto
			            .getMuestraDetalle()
			            .getObservaciones()
			            .equals(listaItemsModificados.get(i)
			                    .getMuestraDetalle()
			                    .getObservaciones())) {
			listaMuestraDetalle.get(i).getMuestraDetalle()
			        .setAuditoria(auditoriaDTO);
			listaMuestraDetalle.get(i).getMuestraDetalle()
			        .setFaseIesDTO(this.faseIesDTO);
			if ("PLAN-CURRICULAR".equals(seccion)) {
			    listaMuestraDetalle.get(i).getMallaCurricularDTO()
				    .setAuditoria(auditoriaDTO);
			    listaMuestraDetalle.get(i).getMallaCurricularDTO()
				    .setVerificarEvidencia(false);
			    registroServicio
				    .registrarMallaCurricular(listaMuestraDetalle
				            .get(i).getMallaCurricularDTO());
			} else if ("CARRERA-PERFIL".equals(seccion)) {
			    listaMuestraDetalle.get(i)
				    .getInformacionCarreraDTO()
				    .setAuditoriaDTO(auditoriaDTO);
			    listaMuestraDetalle.get(i)
				    .getInformacionCarreraDTO()
				    .setVerificarEvidencia(false);
			    registroServicio
				    .registrarInformacionCarrera(listaMuestraDetalle
				            .get(i).getInformacionCarreraDTO());
			}
			if ("PEA".equals(seccion)) {
			    listaMuestraDetalle.get(i).getPeas()
				    .setAuditoriaDTO(auditoriaDTO);
			    listaMuestraDetalle.get(i).getPeas()
				    .setVerificarEvidencia(false);
			    registroServicio.registrarPEA(listaMuestraDetalle
				    .get(i).getPeas());
			}
			evaluacionServicio
			        .registrarMuestraDetalle(listaMuestraDetalle
			                .get(i).getMuestraDetalle());
			modificado = true;
		    }
		} else if (vvdto.getMuestraDetalle().getObservaciones() != null
		        && !vvdto.getMuestraDetalle().getObservaciones()
		                .isEmpty()
		        && !vvdto
		                .getMuestraDetalle()
		                .getObservaciones()
		                .equals(listaItemsModificados.get(i)
		                        .getMuestraDetalle().getObservaciones())) {
		    listaMuestraDetalle.get(i).getMuestraDetalle()
			    .setAuditoria(auditoriaDTO);
		    listaMuestraDetalle.get(i).getMuestraDetalle()
			    .setFaseIesDTO(this.faseIesDTO);
		    evaluacionServicio
			    .registrarMuestraDetalle(listaMuestraDetalle.get(i)
			            .getMuestraDetalle());
		    modificado = true;
		}
	    }

	    listaItemsModificados.clear();
	    for (ItemEvaluacion item : this.listaMuestraDetalle) {
		this.listaItemsModificados
		        .add((ItemEvaluacion) SerializationUtils.clone(item));
	    }

	    if ("PEA".equals(seccion)) {
		int siPeas = 0;
		int noPeas = 0;
		for (ItemEvaluacion item : this.listaMuestraDetalle) {
		    if ("1".equals(item.getMuestraDetalle().getVerificado())) {
			siPeas++;
		    } else if ("0".equals(item.getMuestraDetalle()
			    .getVerificado())) {
			noPeas++;
		    }
		}

		Double valor = new Double("0");
		try {
		    valor = (double) ((siPeas * valorVariableDTO
			    .getTotalUniverso()) / valorVariableDTO
			    .getTotalMuestra());
		} catch (Exception e) {
		    e.printStackTrace();
		}

		valorVariableDTO.setValorVerificado(new Double(siPeas));
		valorVariableDTO.setRegistrosNoAceptados(noPeas);
		valorVariableDTO.setAuditoriaDTO(auditoriaDTO);
		valorVariableDTO.setValor(valor.toString());
		valorVariableDTO.setModificado(false);
		valorVariableDTO.setFaseIesDTO(faseIesDTO);
		evaluacionServicio.registrarValorVariable(valorVariableDTO);

		this.listaVariablesCurriculoPEAS = evaluacionServicio
		        .obtenerInformacionVariables("CURR",
		                this.informacionIes.getId(), "%CUANTITATIVA%",
		                "peas");

		this.calcularTotalVerificadoVariable(
		        listaVariablesCurriculoPEAS,
		        listaEvaluacionCurriculoPEAS);

		variablesModificadosPeas.clear();
		for (ValorVariableDTO variable : listaVariablesCurriculoPEAS) {
		    variablesModificadosPeas
			    .add((ValorVariableDTO) SerializationUtils
			            .clone(variable));
		}

	    } else if ("CARRERA-PERFIL".equals(seccion)
		    || "PLAN-CURRICULAR".equals(seccion)) {
		guardarValorVariables();
	    }
	    if (modificado) {
		JsfUtil.msgAdvert("Recuerde verificar que la observación de la variable "
		        + valorVariableDTO.getVariableProcesoDTO()
		                .getVariableDTO().getEtiqueta()
		        + ", corresponda a la modificación realizada en las muestras.");
		JsfUtil.msgInfo("Registros actualizados correctamente");
	    } else {
		JsfUtil.msgAdvert("No se ha realizado ningún cambio");
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
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
	listaVariablesCurriculoAsignaturas.clear();
	try {

	    for (int i = 0; i < ListaVV.size(); i++) {
		if (!ListaVV.get(i).getVariableProcesoDTO()
		        .getMuestraEstratificada()
		        && ListaVV.get(i).getVariableProcesoDTO()
		                .getVariablePadreDTO() != null) {
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
		} else if (!ListaVV.get(i).getVariableProcesoDTO()
		        .getMuestraEstratificada()) {
		    listaVariablesCurriculoAsignaturas.add(ListaVV.get(i));
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public void guardarValorVariables() {
	try {
	    String totalValorAlto = "";
	    String totalValorMedio = "";
	    String totalValorBajo = "";
	    if ("Perfiles Egreso".equals(this.valorVariableDTO
		    .getVariableProcesoDTO().getVariableDTO().getNombre())
		    || "Perfiles Consultados".equals(this.valorVariableDTO
		            .getVariableProcesoDTO().getVariableDTO()
		            .getNombre())
		    || "Plan Curricular".equals(this.valorVariableDTO
		            .getVariableProcesoDTO().getVariableDTO()
		            .getNombre())) {

		totalValorAlto = evaluacionServicio.obtenerValoresMuestra(
		        "ALTO", this.valorVariableDTO.getCodigoMuestra())
		        .toString();
		totalValorMedio = evaluacionServicio.obtenerValoresMuestra(
		        "MEDIO", this.valorVariableDTO.getCodigoMuestra())
		        .toString();
		totalValorBajo = evaluacionServicio.obtenerValoresMuestra(
		        "BAJO", this.valorVariableDTO.getCodigoMuestra())
		        .toString();

		ValorVariableDTO valorVariableAlto = new ValorVariableDTO();
		ValorVariableDTO valorVariableMedio = new ValorVariableDTO();
		ValorVariableDTO valorVariableBajo = new ValorVariableDTO();

		for (ValorVariableDTO variable : listaVariablesCurriculoPerfiles) {
		    variablesModificadosCurriculo
			    .add((ValorVariableDTO) SerializationUtils
			            .clone(variable));
		    if (variable
			    .getVariableProcesoDTO()
			    .getVariableDTO()
			    .getEtiqueta()
			    .startsWith(
			            valorVariableDTO.getVariableProcesoDTO()
			                    .getVariableDTO().getEtiqueta())) {
			if (variable.getVariableProcesoDTO().getVariableDTO()
			        .getEtiqueta().endsWith("ALTO")) {
			    valorVariableAlto = (ValorVariableDTO) SerializationUtils
				    .clone(variable);
			} else if (variable.getVariableProcesoDTO()
			        .getVariableDTO().getEtiqueta()
			        .endsWith("MEDIO")) {
			    valorVariableMedio = (ValorVariableDTO) SerializationUtils
				    .clone(variable);
			} else if (variable.getVariableProcesoDTO()
			        .getVariableDTO().getEtiqueta()
			        .endsWith("BAJO")) {
			    valorVariableBajo = (ValorVariableDTO) SerializationUtils
				    .clone(variable);
			}
		    }
		}

		AuditoriaDTO a = new AuditoriaDTO();
		a.setFechaModificacion(new Date());
		a.setUsuarioModificacion(usuario);

		valorVariableAlto.setAuditoriaDTO(a);
		valorVariableAlto.setFaseIesDTO(faseIesDTO);
		valorVariableAlto.setValor(totalValorAlto);

		valorVariableMedio.setAuditoriaDTO(a);
		valorVariableMedio.setFaseIesDTO(faseIesDTO);
		valorVariableMedio.setValor(totalValorMedio);

		valorVariableBajo.setAuditoriaDTO(a);
		valorVariableBajo.setFaseIesDTO(faseIesDTO);
		valorVariableBajo.setValor(totalValorBajo);

		if ("Perfiles Egreso".equals(this.valorVariableDTO
		        .getVariableProcesoDTO().getVariableDTO().getNombre())) {
		    valorVariableAlto.getVariableProcesoDTO().setId(5L);
		    valorVariableMedio.getVariableProcesoDTO().setId(6L);
		    valorVariableBajo.getVariableProcesoDTO().setId(7L);
		} else if ("Perfiles Consultados".equals(this.valorVariableDTO
		        .getVariableProcesoDTO().getVariableDTO().getNombre())) {
		    valorVariableAlto.getVariableProcesoDTO().setId(8L);
		    valorVariableMedio.getVariableProcesoDTO().setId(9L);
		    valorVariableBajo.getVariableProcesoDTO().setId(10L);
		} else if ("Plan Curricular".equals(this.valorVariableDTO
		        .getVariableProcesoDTO().getVariableDTO().getNombre())) {
		    valorVariableAlto.getVariableProcesoDTO().setId(11L);
		    valorVariableMedio.getVariableProcesoDTO().setId(12L);
		    valorVariableBajo.getVariableProcesoDTO().setId(13L);
		}
		valorVariableAlto.setModificado(false);
		valorVariableMedio.setModificado(false);
		valorVariableBajo.setModificado(false);
		evaluacionServicio.registrarValorVariable(valorVariableAlto);
		evaluacionServicio.registrarValorVariable(valorVariableMedio);
		evaluacionServicio.registrarValorVariable(valorVariableBajo);

		Integer total = Integer.parseInt(totalValorAlto)
		        + Integer.parseInt(totalValorMedio)
		        + Integer.parseInt(totalValorBajo);
		this.valorVariableDTO.setValor(total.toString());
		this.valorVariableDTO.setModificado(false);
		this.valorVariableDTO.setFaseIesDTO(faseIesDTO);

		evaluacionServicio
		        .registrarValorVariable(this.valorVariableDTO);

		/* Variables Curriculo */
		this.listaVariablesCurriculoPerfiles = evaluacionServicio
		        .obtenerInformacionVariables("CURR",
		                this.informacionIes.getId(), "%CUANTITATIVA%",
		                "perfiles");
	    }
	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    /**
     * 
     * Método para la listar los Criterios de una Variable seleccionada
     * 
     * @author eviscarra
     * @version 19/06/2014 - 9:46:14
     */
    public void cargarCriteriosEvaluacion() {
	LOG.info("this.ies.getId(): " + this.ies.getId());
	mostrarEvidencias = false;
	this.alertaCriterios = false;

	LOG.info("Fecha: " + new Date());
	LOG.info("Usuario: " + usuario);
	LOG.info("Obteniendo Resultado Criterios: Variable  "
	        + this.valorVariableDTO.getVariableProcesoDTO()
	                .getVariableDTO().getId());

	try {
	    this.listaCriterios.clear();

	    VariableValoracionDTO variableValoracion = catalogoServicio
		    .obtenerVariableValoracion(valorVariableDTO
		            .getVariableProcesoDTO().getVariableDTO().getId(),
		            new Long(idProceso));

	    this.listaCriterios = evaluacionServicio
		    .obtenerInformacionCriterios(this.ies.getId(),
		            variableValoracion.getId(),
		            muestraDetalleDTO.getId());

	    if (listaCriterios == null || listaCriterios.isEmpty()) {
		List<CriterioEvaluarDTO> criterios = evaluacionServicio
		        .obtenerCriteriosPorVariableValoracion(variableValoracion
		                .getId());
		AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
		auditoriaDTO.setFechaModificacion(new Date());
		auditoriaDTO.setUsuarioModificacion(usuario);
		for (CriterioEvaluarDTO criterio : criterios) {
		    ResultadoCriteriosDTO resultadoCriterio = new ResultadoCriteriosDTO();
		    resultadoCriterio.setActivo(true);
		    resultadoCriterio.setAuditoria(auditoriaDTO);
		    resultadoCriterio.setFaseIesDTO(faseIesDTO);
		    resultadoCriterio.setIdFaseIes(faseIesDTO.getId());
		    resultadoCriterio.setCriterioEvaluarDTO(criterio);
		    resultadoCriterio.setCumple("-1");
		    resultadoCriterio.setObservacion("");
		    resultadoCriterio.setIdMuestraDetalle(muestraDetalleDTO
			    .getId());
		    evaluacionServicio
			    .registrarResultadoCriterio(resultadoCriterio);
		}
		this.listaCriterios = evaluacionServicio
		        .obtenerInformacionCriterios(this.ies.getId(),
		                variableValoracion.getId(),
		                muestraDetalleDTO.getId());
	    }

	    this.alertaCriterios = true;

	    listaResultadosCriteriosModificados.clear();
	    for (ResultadoCriteriosDTO resultado : listaCriterios) {
		listaResultadosCriteriosModificados
		        .add((ResultadoCriteriosDTO) SerializationUtils
		                .clone(resultado));
	    }
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
	LOG.info("Fecha: " + new Date());
	LOG.info("Usuario: " + usuario);
	LOG.info("Obteniendo Resultado Criterios: Variable  "
	        + variableValoracion.getVariable().getId());
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

    public void guardarObservacionVariablesCurriculo() {
	try {
	    AuditoriaDTO adto = new AuditoriaDTO();
	    adto.setFechaModificacion(new Date());
	    adto.setUsuarioModificacion(this.usuario);
	    boolean modificado = false;
	    for (int i = 0; i < this.listaVariablesCurriculoPerfiles.size(); i++) {
		ValorVariableDTO vvdto = listaVariablesCurriculoPerfiles.get(i);
		if ((vvdto.getValor() != null && !vvdto.getValor().equals(
		        variablesModificadosCurriculo.get(i).getValor()))
		        || (vvdto.getObservacion() != null
		                && !vvdto.getObservacion().isEmpty() && !vvdto
		                .getObservacion().equals(
		                        variablesModificadosCurriculo.get(i)
		                                .getObservacion()))) {
		    vvdto.setAuditoriaDTO(adto);
		    vvdto.setFaseIesDTO(this.faseIesDTO);
		    vvdto.setIdInformacionIes(this.informacionIes.getId());
		    modificado = true;
		    vvdto.setModificado(false);

		    LOG.info("Usuario: " + usuario);
		    LOG.info(new Date()
			    + "--> Registrando variable: "
			    + vvdto.getVariableProcesoDTO().getVariableDTO()
			            .getEtiqueta() + " id: " + vvdto.getId());
		    LOG.info(new Date() + "--> Instituto: "
			    + informacionIes.getId());
		    evaluacionServicio.registrarValorVariable(vvdto);
		    LOG.info(new Date() + "--> VariableRegistrada: "
			    + vvdto.getId());
		}
	    }

	    this.listaVariablesCurriculoPerfiles = evaluacionServicio
		    .obtenerInformacionVariables("CURR",
		            this.informacionIes.getId(), "%CUANTITATIVA%",
		            "perfiles");

	    variablesModificadosCurriculo.clear();
	    for (ValorVariableDTO variable : listaVariablesCurriculoPerfiles) {
		variablesModificadosCurriculo
		        .add((ValorVariableDTO) SerializationUtils
		                .clone(variable));
	    }
	    if (modificado) {
		JsfUtil.msgInfo("Registros almacenados correctamente");
	    } else {
		JsfUtil.msgAdvert("No se han realizado cambios sobre las variables.");
	    }
	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public void guardarObservacionVariablesPeas() {
	try {
	    AuditoriaDTO adto = new AuditoriaDTO();
	    adto.setFechaModificacion(new Date());
	    adto.setUsuarioModificacion(this.usuario);
	    boolean modificado = false;

	    for (int i = 0; i < this.listaEvaluacionCurriculoPEAS.size(); i++) {
		ValorVariableDTO vvdto = listaEvaluacionCurriculoPEAS.get(i)
		        .getValorVariable();
		if ((vvdto.getValor() != null && !vvdto.getValor().equals(
		        variablesModificadosPeas.get(i).getValor()))
		        || (vvdto.getObservacion() != null && !vvdto
		                .getObservacion().equals(
		                        variablesModificadosPeas.get(i)
		                                .getObservacion()))) {
		    vvdto.setAuditoriaDTO(adto);
		    vvdto.setFaseIesDTO(this.faseIesDTO);
		    vvdto.setIdInformacionIes(this.informacionIes.getId());
		    modificado = true;
		    vvdto.setModificado(false);
		    evaluacionServicio.registrarValorVariable(vvdto);

		}
	    }

	    for (int i = 0; i < this.listaVariablesCurriculoAsignaturas.size(); i++) {
		ValorVariableDTO vvdto = listaVariablesCurriculoAsignaturas
		        .get(i);
		if ((vvdto.getValor() != null && !vvdto.getValor().equals(
		        variablesModificadosAsignaturas.get(i).getValor()))
		        || (vvdto.getObservacion() != null && !vvdto
		                .getObservacion().equals(
		                        variablesModificadosAsignaturas.get(i)
		                                .getObservacion()))) {
		    vvdto.setAuditoriaDTO(adto);
		    vvdto.setFaseIesDTO(this.faseIesDTO);
		    vvdto.setIdInformacionIes(this.informacionIes.getId());
		    modificado = true;
		    vvdto.setModificado(false);
		    evaluacionServicio.registrarValorVariable(vvdto);

		}
	    }

	    ValorVariableDTO vvPadre = evaluacionServicio
		    .obtenerUltimoValorVariable(
		            this.listaVariablesCurriculoPEAS.get(0)
		                    .getVariableProcesoDTO()
		                    .getVariablePadreDTO().getVariableDTO()
		                    .getId(), null, this.informacionIes.getId());
	    // 115 Numero de asignaturas revisadas
	    // ValorVariableDTO vvAdicional = evaluacionServicio
	    // .obtenerUltimoValorVariable(115L, null,
	    // this.informacionIes.getId());

	    this.listaVariablesCurriculoPEAS = evaluacionServicio
		    .obtenerInformacionVariables("CURR",
		            this.informacionIes.getId(), "%CUANTITATIVA%",
		            "peas");

	    Double valorPadre = 0.0;
	    // Double valorVarAdicional = 0.0;
	    variablesModificadosPeas.clear();
	    for (ValorVariableDTO variable : listaVariablesCurriculoPEAS) {
		if (variable.getVariableProcesoDTO().getVariablePadreDTO() != null) {
		    variablesModificadosPeas
			    .add((ValorVariableDTO) SerializationUtils
			            .clone(variable));
		    if (variable.getVariableProcesoDTO().getVariablePadreDTO() != null) {
			valorPadre += Double.parseDouble(variable.getValor());
			// valorVarAdicional += variable.getTotalUniverso();
		    }
		}
	    }

	    vvPadre.setValor(valorPadre.toString());
	    vvPadre.setValorVerificado(valorPadre);
	    vvPadre.setAuditoriaDTO(adto);
	    vvPadre.setFaseIesDTO(this.faseIesDTO);
	    evaluacionServicio.registrarValorVariable(vvPadre);

	    this.listaVariablesCurriculoPEAS = evaluacionServicio
		    .obtenerInformacionVariables("CURR",
		            this.informacionIes.getId(), "%CUANTITATIVA%",
		            "peas");

	    this.calcularTotalVerificadoVariable(listaVariablesCurriculoPEAS,
		    listaEvaluacionCurriculoPEAS);

	    variablesModificadosAsignaturas.clear();
	    for (ValorVariableDTO variable : listaVariablesCurriculoAsignaturas) {
		variablesModificadosAsignaturas
		        .add((ValorVariableDTO) SerializationUtils
		                .clone(variable));
	    }

	    // vvAdicional.setValor(valorVarAdicional.toString());
	    // vvAdicional.setAuditoriaDTO(adto);
	    // vvAdicional.setFaseIesDTO(this.faseIesDTO);
	    // evaluacionServicio.registrarValorVariable(vvAdicional);

	    if (modificado) {
		JsfUtil.msgInfo("Registros almacenados correctamente.");
	    } else {
		JsfUtil.msgAdvert("No se han realizado cambios sobre las variables.");
	    }
	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public void guardarResultadosCriteriosMuestraDetalle() {
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
	    Integer contador = 0;
	    boolean bandera = false;
	    for (int i = 0; i < this.listaCriterios.size(); i++) {
		bandera = true;
		ResultadoCriteriosDTO rcdto = listaCriterios.get(i);
		if (rcdto.getCumple().equals("-1")) {
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
			    && !rcdto.getObservacion().equals(
			            listaResultadosCriteriosModificados.get(i)
			                    .getObservacion())) {
			rcdto.setAuditoria(adto);
			rcdto.setIdFaseIes(this.faseIesDTO.getId());
			evaluacionServicio.registrarResultadoCriterio(rcdto);
		    }
		} else if (rcdto.getObservacion() != null
		        && !rcdto.getObservacion().equals(
		                listaResultadosCriteriosModificados.get(i)
		                        .getObservacion())) {
		    rcdto.setAuditoria(adto);
		    rcdto.setIdFaseIes(this.faseIesDTO.getId());
		    evaluacionServicio.registrarResultadoCriterio(rcdto);
		}

	    }
	    if (contador.equals(0) && bandera) {
		this.muestraDetalleDTO.setVerificado(calcularValorVariable());
	    } else {
		JsfUtil.msgAdvert("Para obtener el valor verificado de la variable seleccionada debe completar toda la información ");

	    }
	    if (modificado) {

		this.muestraDetalleDTO.setAuditoria(adto);
		this.muestraDetalleDTO.setFaseIesDTO(this.faseIesDTO);

		if (OrigenInformacionEnum.MALLA.getValor().equals(
		        muestraDetalleDTO.getTabla().getValor())) {
		    mallaDTO.setAuditoria(adto);
		    mallaDTO.setVerificarEvidencia(false);
		    registroServicio.registrarMallaCurricular(mallaDTO);
		}

		if (OrigenInformacionEnum.CARRERA.getValor().equals(
		        muestraDetalleDTO.getTabla().getValor())) {
		    informacionCarreraDTO.setAuditoriaDTO(adto);
		    informacionCarreraDTO.setVerificarEvidencia(false);
		    registroServicio
			    .registrarInformacionCarrera(informacionCarreraDTO);
		}
		evaluacionServicio
		        .registrarMuestraDetalle(this.muestraDetalleDTO);
		cargarMuestraDetalleVariablesCurriculo();
		JsfUtil.msgAdvert("Recuerde verificar que la observación de la carrera "
		        + carreraIesDTO.getNombre()
		        + ", corresponda a la modificación realizada en los criterios.");
	    }
	    cargarCriteriosEvaluacion();

	    listaResultadosCriteriosModificados.clear();
	    for (ResultadoCriteriosDTO resultado : listaCriterios) {
		listaResultadosCriteriosModificados
		        .add((ResultadoCriteriosDTO) SerializationUtils
		                .clone(resultado));
	    }
	    alertaCriterios = false;
	    JsfUtil.msgInfo("Registros almacenados correctamente");

	    LOG.info("Usuario: " + usuario);
	    LOG.info(new Date() + "--> Registrando criterios muestra detalle");

	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
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

    public void obtenerHistoricoVariable(ValorVariableDTO valorVariable) {
	historicosVariable.clear();
	listaEvidenciaConcepto.clear();
	// historicosVariable = evaluacionServicio
	// .obtenerHistoricoValorVariable(idValorVariable);
	historicosVariable = evaluacionServicio
	        .obtenerHistoricoPorIesYVariableProceso(valorVariable
	                .getIdInformacionIes(), valorVariable
	                .getVariableProcesoDTO().getId());
    }

    public void obtenerHistoricoMuestra(Long idMuestraDetalle) {
	historicoMuestraDetalle.clear();
	listaEvidenciaConcepto.clear();
	mostrarEvidencias = false;
	historicoMuestraDetalle = evaluacionServicio
	        .obtenerHistoricoMuestraDetalle(idMuestraDetalle);
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

    public List<ValorVariableDTO> getListaVariablesCurriculoPerfiles() {
	return listaVariablesCurriculoPerfiles;
    }

    public void setListaVariablesCurriculoPerfiles(
	    List<ValorVariableDTO> listaVariablesCurriculoPerfiles) {
	this.listaVariablesCurriculoPerfiles = listaVariablesCurriculoPerfiles;
    }

    public List<ValorVariableDTO> getListaVariablesCurriculoPEAS() {
	return listaVariablesCurriculoPEAS;
    }

    public void setListaVariablesCurriculoPEAS(
	    List<ValorVariableDTO> listaVariablesCurriculoPEAS) {
	this.listaVariablesCurriculoPEAS = listaVariablesCurriculoPEAS;
    }

    public List<ItemEvaluacion> getListaEvaluacionCurriculoPEAS() {
	return listaEvaluacionCurriculoPEAS;
    }

    public void setListaEvaluacionCurriculoPEAS(
	    List<ItemEvaluacion> listaEvaluacionCurriculoPEAS) {
	this.listaEvaluacionCurriculoPEAS = listaEvaluacionCurriculoPEAS;
    }

    public ValorVariableDTO getValorVariableDTO() {
	return valorVariableDTO;
    }

    public void setValorVariableDTO(ValorVariableDTO valorVariableDTO) {
	this.valorVariableDTO = valorVariableDTO;
    }

    public List<ItemEvaluacion> getListaMuestraDetalle() {
	return listaMuestraDetalle;
    }

    public void setListaMuestraDetalle(List<ItemEvaluacion> listaMuestraDetalle) {
	this.listaMuestraDetalle = listaMuestraDetalle;
    }

    public Boolean getAlertaIngresoVerificado() {
	return alertaIngresoVerificado;
    }

    public void setAlertaIngresoVerificado(Boolean alertaIngresoVerificado) {
	this.alertaIngresoVerificado = alertaIngresoVerificado;
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

    public List<ValorVariableDTO> getVariablesModificadosCurriculo() {
	return variablesModificadosCurriculo;
    }

    public void setVariablesModificadosCurriculo(
	    List<ValorVariableDTO> variablesModificadosCurriculo) {
	this.variablesModificadosCurriculo = variablesModificadosCurriculo;
    }

    public List<ValorVariableDTO> getVariablesModificadosPeas() {
	return variablesModificadosPeas;
    }

    public void setVariablesModificadosPeas(
	    List<ValorVariableDTO> variablesModificadosPeas) {
	this.variablesModificadosPeas = variablesModificadosPeas;
    }

    public MallaCurricularDTO getMallaDTO() {
	return mallaDTO;
    }

    public void setMallaDTO(MallaCurricularDTO mallaDTO) {
	this.mallaDTO = mallaDTO;
    }

    public InformacionCarreraDTO getInformacionCarreraDTO() {
	return informacionCarreraDTO;
    }

    public void setInformacionCarreraDTO(
	    InformacionCarreraDTO informacionCarreraDTO) {
	this.informacionCarreraDTO = informacionCarreraDTO;
    }

    public List<ValorVariableDTO> getListaVariablesCurriculoAsignaturas() {
	return listaVariablesCurriculoAsignaturas;
    }

    public void setListaVariablesCurriculoAsignaturas(
	    List<ValorVariableDTO> listaVariablesCurriculoAsignaturas) {
	this.listaVariablesCurriculoAsignaturas = listaVariablesCurriculoAsignaturas;
    }

    public List<ValorVariableDTO> getVariablesModificadosAsignaturas() {
	return variablesModificadosAsignaturas;
    }

    public void setVariablesModificadosAsignaturas(
	    List<ValorVariableDTO> variablesModificadosAsignaturas) {
	this.variablesModificadosAsignaturas = variablesModificadosAsignaturas;
    }

}