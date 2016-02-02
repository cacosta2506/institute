package ec.gob.ceaaces.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;

import org.apache.commons.lang.SerializationUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;

import ec.gob.ceaaces.catalogo.dtos.catalogos.AuditoriaDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.ConceptoDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.IesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.VariableProcesoDTO;
import ec.gob.ceaaces.catalogo.enumeraciones.GrupoConceptoEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.GrupoVariableEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.TipoVariableEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.VariableSubGrupoEnum;
import ec.gob.ceaaces.catalogo.exceptions.ServicioException;
import ec.gob.ceaaces.data.ItemEvaluacion;
import ec.gob.ceaaces.data.TituloSenescyt;
import ec.gob.ceaaces.institutos.dtos.evaluacion.CargoAcademicoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.CarreraEstudianteDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.CarreraEstudiantePeriodoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ContratacionDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ConvenioVigenteDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.CursoCapacitacionDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaConceptoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ExperienciaProfesionalDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.FormacionProfesionalDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.MuestraDetalleDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.PersonaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ValorVariableDTO;
import ec.gob.ceaaces.institutos.enumeraciones.OrigenInformacionEnum;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.EvaluacionServicio;
import ec.gob.ceaaces.services.InstitutosServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.ArchivoUtil;
import ec.gob.ceaaces.util.JsfUtil;

public class EvaluacionAmbienteInstitucionalController implements Serializable {

    private static final long serialVersionUID = 1711633276053493785L;

    private static final Logger LOG = Logger
	    .getLogger(EvaluacionAmbienteInstitucionalController.class
	            .getSimpleName());

    @Autowired
    private RegistroServicio registroServicio;

    @Autowired
    private EvaluacionServicio evaluacionServicio;

    @Autowired
    private InstitutosServicio institutosServicio;

    @Autowired
    private CatalogoServicio catalogoServicio;

    private List<ValorVariableDTO> listaValorVariable;
    private List<ValorVariableDTO> listaValorVariableDocentes;
    private List<ValorVariableDTO> listaValorVariableConvenios;
    private List<ValorVariableDTO> listaValorVariableGraduados;
    private List<ValorVariableDTO> listaValorVariableGraduadosModificada;
    private List<ValorVariableDTO> listaValorVariableDocentesModificada;
    private List<ValorVariableDTO> listaValorVariableConveniosModificada;
    private List<ValorVariableDTO> listaValorVariableModificada;
    private List<PersonaDTO> listaEstudiantes;
    private List<PersonaDTO> listaDocentes;
    private List<ConvenioVigenteDTO> listaConvenios;
    private List<CarreraEstudianteDTO> listaCarrerasEstudiante;
    private List<ItemEvaluacion> listaMuestraDetalleEstudiantes;
    private List<ItemEvaluacion> listaMuestraDetalleDocentes;
    private List<ItemEvaluacion> listaMuestraDetalleConvenios;
    private ItemEvaluacion muestraDetallestudianteSeleccionada;
    private List<ItemEvaluacion> listaMuestraDetalleEstudiantesOriginal;
    private List<ItemEvaluacion> listaMuestraDetalleDocentesOriginal;
    private List<ItemEvaluacion> listaMuestraDetalleConveniosOriginal;
    private List<MuestraDetalleDTO> listaMuestraDetalle;
    private List<CarreraEstudiantePeriodoDTO> listaCarreraPeriodo;
    private CarreraEstudiantePeriodoDTO carreraPeriodoSeleccionado;
    private CarreraEstudianteDTO carreraSeleccionada;
    private List<EvidenciaConceptoDTO> listaEvidenciaConcepto;
    private PersonaDTO estudianteSeleccionado;
    private StreamedContent documentoDescarga;
    private Long idEvidencia;
    private Long contadorVerificado;
    private Boolean cambiosVariable = false;
    private Boolean alertaEvaluador = false;
    private String perfil;
    private String extensionDocumento;
    private String nombreFichero;
    private String fichero;
    private Boolean descargar = false;
    private String url;
    private ItemEvaluacion docenteSeleccionado;

    private FaseIesDTO faseiesDTO;
    private String usuario;
    private IesDTO ies;
    private InformacionIesDTO informacionIesDto;
    private ValorVariableDTO valorVariableSeleccionada;
    private Boolean estudiantes = false;
    private Boolean matriculas = false;

    private List<TituloSenescyt> titulosSenescyt = new ArrayList<TituloSenescyt>();

    private List<CursoCapacitacionDTO> listaCapacitacion;
    private List<ContratacionDTO> listaContratacion;
    private List<FormacionProfesionalDTO> listaFormacion;
    private List<CargoAcademicoDTO> listaCargosAcademicos;
    private List<ExperienciaProfesionalDTO> listaExperienciaProfesional;
    private Boolean evidencias = false;
    private Boolean contratos = false;
    private Boolean capacitacion = false;
    private Boolean cargos = false;
    private Boolean evidenciaConcepto = false;
    private Boolean experiencia = false;
    private Boolean formacion = false;
    private ConceptoDTO conceptoSeleccionado;
    private String identificacion;
    private Boolean busqueda = false;
    private List<ConceptoDTO> listaEvidencia;
    private String idProceso;
    private FormacionProfesionalDTO formacionSeleccionada;
    private ContratacionDTO contratoSeleccionado;
    private ExperienciaProfesionalDTO experienciaSeleccionada;
    private CursoCapacitacionDTO capacitacionSeleccionada;
    private CargoAcademicoDTO cargoAcademicaSeleccionado;

    public EvaluacionAmbienteInstitucionalController() {
	this.listaValorVariable = new ArrayList<ValorVariableDTO>();
	this.listaValorVariableModificada = new ArrayList<ValorVariableDTO>();
	this.listaValorVariableDocentes = new ArrayList<ValorVariableDTO>();
	this.listaValorVariableDocentesModificada = new ArrayList<ValorVariableDTO>();
	this.listaValorVariableConveniosModificada = new ArrayList<ValorVariableDTO>();
	this.listaEstudiantes = new ArrayList<PersonaDTO>();
	this.listaMuestraDetalleEstudiantes = new ArrayList<ItemEvaluacion>();
	this.listaDocentes = new ArrayList<PersonaDTO>();
	this.listaValorVariableGraduadosModificada = new ArrayList<ValorVariableDTO>();
	this.listaMuestraDetalleDocentes = new ArrayList<ItemEvaluacion>();
	this.listaMuestraDetalle = new ArrayList<MuestraDetalleDTO>();
	this.listaCarreraPeriodo = new ArrayList<CarreraEstudiantePeriodoDTO>();
	this.listaEvidenciaConcepto = new ArrayList<EvidenciaConceptoDTO>();
	this.listaValorVariableConvenios = new ArrayList<ValorVariableDTO>();
	this.listaCarrerasEstudiante = new ArrayList<CarreraEstudianteDTO>();
	this.listaMuestraDetalleDocentesOriginal = new ArrayList<ItemEvaluacion>();
	this.listaMuestraDetalleConvenios = new ArrayList<ItemEvaluacion>();
	this.listaMuestraDetalleConveniosOriginal = new ArrayList<ItemEvaluacion>();
	conceptoSeleccionado = new ConceptoDTO();
	carreraSeleccionada = new CarreraEstudianteDTO();
	carreraPeriodoSeleccionado = new CarreraEstudiantePeriodoDTO();
	this.listaMuestraDetalleEstudiantesOriginal = new ArrayList<ItemEvaluacion>();
	muestraDetallestudianteSeleccionada = new ItemEvaluacion();
	ies = new IesDTO();
	faseiesDTO = new FaseIesDTO();
	informacionIesDto = new InformacionIesDTO();
	valorVariableSeleccionada = new ValorVariableDTO();
	estudianteSeleccionado = new PersonaDTO();
	docenteSeleccionado = new ItemEvaluacion();

    }

    @PostConstruct
    public void start() {
	ListaIesController controller = (ListaIesController) JsfUtil
	        .obtenerObjetoSesion("listaIesController");
	ies = controller.getIes();
	usuario = controller.getUsuario();
	this.perfil = controller.getPerfil().getNombre();
	faseiesDTO = controller.getFaseIesDTO();
	this.idProceso = controller.getFaseIesDTO().getProcesoDTO().getId()
	        .toString();
	if (this.perfil.startsWith("IES_EVALUADOR")) {
	    alertaEvaluador = true;
	}

	try {
	    informacionIesDto = registroServicio
		    .obtenerInformacionIesPorIes(ies);
	} catch (Exception e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	}

	try {

	    listaValorVariable = evaluacionServicio
		    .obtenerInformacionVariables(
		            GrupoVariableEnum.ESTUDIANTES.getValue(),
		            informacionIesDto.getId(),
		            TipoVariableEnum.CUANTITATIVA.getValue(), null);
	    listaValorVariableDocentes = evaluacionServicio
		    .obtenerInformacionVariables(
		            GrupoVariableEnum.AMBIENTE_INSTITUCIONAL.getValue(),
		            informacionIesDto.getId(),
		            TipoVariableEnum.CUANTITATIVA.getValue(),
		            VariableSubGrupoEnum.AMBIENTE_INSTITUCIONAL_DOC
		                    .getValor());

	    listaValorVariableConvenios = evaluacionServicio
		    .obtenerInformacionVariables(
		            GrupoVariableEnum.AMBIENTE_INSTITUCIONAL.getValue(),
		            informacionIesDto.getId(),
		            TipoVariableEnum.CUANTITATIVA.getValue(),
		            VariableSubGrupoEnum.AMBIENTE_INSTITUCIONAL_CONV
		                    .getValor());
	    listaValorVariableGraduados = evaluacionServicio
		    .obtenerInformacionVariables(
		            GrupoVariableEnum.GRADUADOS.getValue(),
		            informacionIesDto.getId(),
		            TipoVariableEnum.CUANTITATIVA.getValue(), null);
	    cargarVariables();

	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    public void cargarVariables() {
	listaValorVariableModificada.clear();
	for (ValorVariableDTO variable : listaValorVariable) {
	    listaValorVariableModificada
		    .add((ValorVariableDTO) SerializationUtils.clone(variable));
	}
	listaValorVariableDocentesModificada.clear();
	for (ValorVariableDTO variable : listaValorVariableDocentes) {
	    listaValorVariableDocentesModificada
		    .add((ValorVariableDTO) SerializationUtils.clone(variable));
	}
	listaValorVariableConveniosModificada.clear();
	for (ValorVariableDTO variable : listaValorVariableConvenios) {
	    listaValorVariableConveniosModificada
		    .add((ValorVariableDTO) SerializationUtils.clone(variable));
	}
	listaValorVariableGraduadosModificada.clear();
	for (ValorVariableDTO variable : listaValorVariableGraduados) {
	    listaValorVariableGraduadosModificada
		    .add((ValorVariableDTO) SerializationUtils.clone(variable));
	}
    }

    public void calcularVariablesEstudiantes() {
	try {
	    AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
	    auditoriaDTO.setFechaModificacion(new Date());
	    auditoriaDTO.setUsuarioModificacion(usuario);

	    // VARIABLES ESTUDIANTES
	    Set<Long> idVarPadres = new HashSet<Long>();
	    for (ValorVariableDTO vvdto : listaValorVariable) {
		if (vvdto.getVariableProcesoDTO().getVariablePadreDTO() != null) {
		    idVarPadres.add(vvdto.getVariableProcesoDTO()
			    .getVariablePadreDTO().getId());
		}
	    }
	    for (Long vvid : idVarPadres) {
		VariableProcesoDTO varPadre = catalogoServicio
		        .obtenerVariableProcesoPorId(vvid);
		List<VariableProcesoDTO> variablesHijas = catalogoServicio
		        .obtenerVariablesHijas(vvid);
		evaluacionServicio.obtenerValorVariableMuestraEstratificada(
		        varPadre, variablesHijas,
		        this.informacionIesDto.getId(), auditoriaDTO,
		        faseiesDTO);

	    }
	    listaValorVariable = evaluacionServicio
		    .obtenerInformacionVariables(
		            GrupoVariableEnum.ESTUDIANTES.getValue(),
		            informacionIesDto.getId(),
		            TipoVariableEnum.CUANTITATIVA.getValue(), null);

	    for (ValorVariableDTO variable : listaValorVariable) {
		listaValorVariableModificada
		        .add((ValorVariableDTO) SerializationUtils
		                .clone(variable));
	    }

	    ValorVariableDTO vvDto = listaValorVariableGraduados.get(0);
	    evaluacionServicio.obtenerValorVariable(vvDto, faseiesDTO,
		    auditoriaDTO, vvDto.getVariableProcesoDTO()
		            .getSqlValorEvaluacion());

	    listaValorVariableGraduados = evaluacionServicio
		    .obtenerInformacionVariables(
		            GrupoVariableEnum.GRADUADOS.getValue(),
		            informacionIesDto.getId(),
		            TipoVariableEnum.CUANTITATIVA.getValue(), null);

	    JsfUtil.msgInfo("Valores actualizados con éxito.");
	} catch (Exception se) {
	    se.printStackTrace();
	    JsfUtil.msgError("Error al calcular las variables. Comuníquese con el Administrador");
	}
    }

    public void calcularVariables() {
	RequestContext context = RequestContext.getCurrentInstance();
	try {
	    AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
	    auditoriaDTO.setFechaModificacion(new Date());
	    auditoriaDTO.setUsuarioModificacion(usuario);
	    List<ValorVariableDTO> valoresVariableNuevos = new ArrayList<ValorVariableDTO>();
	    for (ValorVariableDTO valorVariableActual : listaValorVariableDocentes) {
		ValorVariableDTO valorVariableNuevo = evaluacionServicio
		        .obtenerValorVariable(valorVariableActual, faseiesDTO,
		                auditoriaDTO, valorVariableActual
		                        .getVariableProcesoDTO()
		                        .getSqlListaId());
		valoresVariableNuevos.add(valorVariableNuevo);
	    }

	    valoresVariableNuevos = new ArrayList<ValorVariableDTO>();
	    for (ValorVariableDTO valorVariableActual : listaValorVariableConvenios) {
		ValorVariableDTO valorVariableNuevo = evaluacionServicio
		        .obtenerValorVariable(valorVariableActual, faseiesDTO,
		                auditoriaDTO, valorVariableActual
		                        .getVariableProcesoDTO()
		                        .getSqlListaId());
		valoresVariableNuevos.add(valorVariableNuevo);
	    }
	    context.execute("dlgCalcularVariable.hide()");
	    cargarVariables();

	} catch (Exception e) {

	    JsfUtil.msgError("Error del sistema. "
		    + " Comuníquese con el administrador.");
	}

    }

    public void guardarEvidencia() {
	carreraPeriodoSeleccionado.setVerificarEvidencia(false);
	try {
	    registroServicio
		    .registrarCarreraEstudiantePeriodo(carreraPeriodoSeleccionado);
	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	JsfUtil.msgInfo("Registro almacenado correctamente");
    }

    public void cargarItemMuestraDetalle() {
	listaMuestraDetalleEstudiantesOriginal.clear();
	for (ItemEvaluacion muestra : listaMuestraDetalleEstudiantes) {
	    listaMuestraDetalleEstudiantesOriginal
		    .add((ItemEvaluacion) SerializationUtils.clone(muestra));
	}
    }

    public void cargarItemMuestraDetalleDocentes() {
	listaMuestraDetalleDocentesOriginal.clear();
	for (ItemEvaluacion muestra : listaMuestraDetalleDocentes) {
	    listaMuestraDetalleDocentesOriginal
		    .add((ItemEvaluacion) SerializationUtils.clone(muestra));
	}
    }

    public void cargarItemMuestraDetalleConvenios() {
	listaMuestraDetalleConveniosOriginal.clear();
	for (ItemEvaluacion muestra : listaMuestraDetalleConvenios) {
	    listaMuestraDetalleConveniosOriginal
		    .add((ItemEvaluacion) SerializationUtils.clone(muestra));
	}
    }

    public void guardarValorVariable() {
	try {
	    AuditoriaDTO adto = new AuditoriaDTO();
	    adto.setFechaModificacion(new Date());
	    adto.setUsuarioModificacion(this.usuario);
	    boolean modificado = false;
	    Set<Long> idVarPadres = new HashSet<Long>();
	    for (int i = 0; i < this.listaValorVariable.size(); i++) {
		ValorVariableDTO vvdto = listaValorVariable.get(i);

		if ((vvdto.getObservacion() != null && vvdto.getValor() != null
		        && !vvdto.getObservacion().isEmpty() && !vvdto
		        .getValor().isEmpty())
		        && (vvdto.getTotalUniverso().intValue() > 0)
		        && (!vvdto.getValor().equals(
		                listaValorVariableModificada.get(i).getValor()) || !vvdto
		                .getObservacion().equals(
		                        listaValorVariableModificada.get(i)
		                                .getObservacion()))) {

		    vvdto.setAuditoriaDTO(adto);
		    vvdto.setFaseIesDTO(this.faseiesDTO);
		    vvdto.setIdInformacionIes(this.informacionIesDto.getId());
		    vvdto.setModificado(false);
		    evaluacionServicio.registrarValorVariable(vvdto);
		    modificado = true;

		} else if (vvdto.getObservacion() != null
		        && (vvdto.getValor() == null || vvdto.getValor()
		                .equals("0.0"))
		        && vvdto.getTotalUniverso().intValue() == 0
		        && (!vvdto.getValor().equals(
		                listaValorVariableModificada.get(i).getValor()) || !vvdto
		                .getObservacion().equals(
		                        listaValorVariableModificada.get(i)
		                                .getObservacion()))) {
		    vvdto.setValor("0.0");
		    vvdto.setAuditoriaDTO(adto);
		    vvdto.setFaseIesDTO(this.faseiesDTO);
		    vvdto.setIdInformacionIes(this.informacionIesDto.getId());
		    vvdto.setModificado(false);
		    evaluacionServicio.registrarValorVariable(vvdto);
		    modificado = true;
		}
	    }

	    listaValorVariable = evaluacionServicio
		    .obtenerInformacionVariables(
		            GrupoVariableEnum.ESTUDIANTES.getValue(),
		            informacionIesDto.getId(),
		            TipoVariableEnum.CUANTITATIVA.getValue(), null);
	    for (ValorVariableDTO vvdto : listaValorVariable) {
		if (vvdto.getVariableProcesoDTO().getVariablePadreDTO() != null) {
		    idVarPadres.add(vvdto.getVariableProcesoDTO()
			    .getVariablePadreDTO().getVariableDTO().getId());
		}
	    }
	    for (Long vvid : idVarPadres) {
		Double valor = 0.0;
		ValorVariableDTO vvp = evaluacionServicio
		        .obtenerUltimoValorVariable(vvid, null,
		                informacionIesDto.getId());
		for (ValorVariableDTO vv : listaValorVariable) {

		    if (vv.getVariableProcesoDTO().getVariablePadreDTO()
			    .getVariableDTO().getId().equals(vvid)) {
			valor += Double.parseDouble(vv.getValor());
		    }
		}
		vvp.setValor(valor.toString());
		vvp.setAuditoriaDTO(adto);
		vvp.setFaseIesDTO(this.faseiesDTO);
		evaluacionServicio.registrarValorVariable(vvp);
	    }

	    actualizarValorGraduados();
	    cargarVariables();
	    if (modificado) {
		JsfUtil.msgInfo("Registros almacenados correctamente");
	    } else {
		JsfUtil.msgInfo("No se ha realizado cambios");
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public void guardarValorVariableDocentes() {
	try {
	    for (int i = 0; i < this.listaValorVariableDocentes.size(); i++) {
		// listaValorVariablesCualitativasModificados
		ValorVariableDTO vvdto = listaValorVariableDocentes.get(i);

		// if (vvdto.getValor() != null
		// && (vvdto.getObservacion() == null || vvdto
		// .getObservacion().isEmpty())) {
		// JsfUtil.msgError("Debe ingresar la observacion para la variable: "
		// + vvdto.getVariableProcesoDTO().getVariableDTO()
		// .getEtiqueta());
		// return;
		// }

		if ((vvdto.getObservacion() != null && vvdto.getValor() != null
		        && !vvdto.getObservacion().isEmpty() && !vvdto
		        .getValor().isEmpty())
		        && (!vvdto.getValor().equals(
		                listaValorVariableDocentesModificada.get(i)
		                        .getValor()) || !vvdto.getObservacion()
		                .equals(listaValorVariableDocentesModificada
		                        .get(i).getObservacion()))) {
		    AuditoriaDTO adto = new AuditoriaDTO();
		    adto.setFechaModificacion(new Date());
		    adto.setUsuarioModificacion(this.usuario);
		    vvdto.setAuditoriaDTO(adto);
		    vvdto.setFaseIesDTO(this.faseiesDTO);
		    vvdto.setIdInformacionIes(this.informacionIesDto.getId());
		    vvdto.setModificado(false);
		    evaluacionServicio.registrarValorVariable(vvdto);

		}

	    }
	    listaValorVariableDocentes = evaluacionServicio
		    .obtenerInformacionVariables(
		            GrupoVariableEnum.AMBIENTE_INSTITUCIONAL.getValue(),
		            informacionIesDto.getId(),
		            TipoVariableEnum.CUANTITATIVA.getValue(),
		            VariableSubGrupoEnum.AMBIENTE_INSTITUCIONAL_DOC
		                    .getValor());
	    JsfUtil.msgInfo("Registros almacenados correctamente");
	    cargarVariables();
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public void guardarValorVariableConvenios() {
	try {
	    for (int i = 0; i < this.listaValorVariableConvenios.size(); i++) {
		// listaValorVariablesCualitativasModificados
		ValorVariableDTO vvdto = listaValorVariableConvenios.get(i);

		// if (vvdto.getValor() != null
		// && (vvdto.getObservacion() == null || vvdto
		// .getObservacion().isEmpty())) {
		// JsfUtil.msgError("Debe ingresar la observacion para la variable: "
		// + vvdto.getVariableProcesoDTO().getVariableDTO()
		// .getEtiqueta());
		// return;
		// }

		if ((vvdto.getObservacion() != null && vvdto.getValor() != null
		        && !vvdto.getObservacion().isEmpty() && !vvdto
		        .getValor().isEmpty())
		        && (!vvdto.getValor().equals(
		                listaValorVariableConveniosModificada.get(i)
		                        .getValor()) || !vvdto.getObservacion()
		                .equals(listaValorVariableConveniosModificada
		                        .get(i).getObservacion()))) {
		    AuditoriaDTO adto = new AuditoriaDTO();
		    adto.setFechaModificacion(new Date());
		    adto.setUsuarioModificacion(this.usuario);
		    vvdto.setAuditoriaDTO(adto);
		    vvdto.setFaseIesDTO(this.faseiesDTO);
		    vvdto.setIdInformacionIes(this.informacionIesDto.getId());
		    vvdto.setModificado(false);
		    evaluacionServicio.registrarValorVariable(vvdto);

		}

	    }
	    listaValorVariableConvenios = evaluacionServicio
		    .obtenerInformacionVariables(
		            GrupoVariableEnum.AMBIENTE_INSTITUCIONAL.getValue(),
		            informacionIesDto.getId(),
		            TipoVariableEnum.CUANTITATIVA.getValue(),
		            VariableSubGrupoEnum.AMBIENTE_INSTITUCIONAL_CONV
		                    .getValor());
	    JsfUtil.msgInfo("Registros almacenados correctamente");
	    cargarVariables();
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public void guardarValorVariableGraduados() {
	try {
	    boolean modificada = false;
	    for (int i = 0; i < this.listaValorVariableGraduados.size(); i++) {
		// listaValorVariablesCualitativasModificados
		ValorVariableDTO vvdto = listaValorVariableGraduados.get(i);

		if (vvdto.getValor() != null
		        && (vvdto.getObservacion() == null || vvdto
		                .getObservacion().isEmpty())) {
		    JsfUtil.msgError("Debe ingresar la observacion para la variable: "
			    + vvdto.getVariableProcesoDTO().getVariableDTO()
			            .getEtiqueta());
		    return;
		}

		if ((vvdto.getObservacion() != null && vvdto.getValor() != null)
		        && (!vvdto.getValor().equals(
		                listaValorVariableGraduados.get(i).getValor()) || !vvdto
		                .getObservacion().equals(
		                        listaValorVariableGraduadosModificada
		                                .get(i).getObservacion()))) {
		    AuditoriaDTO adto = new AuditoriaDTO();
		    adto.setFechaModificacion(new Date());
		    adto.setUsuarioModificacion(this.usuario);
		    vvdto.setAuditoriaDTO(adto);
		    vvdto.setFaseIesDTO(this.faseiesDTO);
		    vvdto.setIdInformacionIes(this.informacionIesDto.getId());
		    vvdto.setModificado(false);
		    evaluacionServicio.registrarValorVariable(vvdto);
		    modificada = true;
		}

	    }
	    if (modificada) {
		listaValorVariableGraduados = evaluacionServicio
		        .obtenerInformacionVariables(
		                GrupoVariableEnum.GRADUADOS.getValue(),
		                informacionIesDto.getId(),
		                TipoVariableEnum.CUANTITATIVA.getValue(), null);
		JsfUtil.msgInfo("Registros almacenados correctamente");
		cargarVariables();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    private void actualizarValorGraduados() {
	AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
	auditoriaDTO.setFechaModificacion(new Date());
	auditoriaDTO.setUsuarioModificacion(this.usuario);
	ValorVariableDTO vvDto = new ValorVariableDTO();
	try {

	    listaValorVariableGraduados = evaluacionServicio
		    .obtenerInformacionVariables(
		            GrupoVariableEnum.GRADUADOS.getValue(),
		            informacionIesDto.getId(),
		            TipoVariableEnum.CUANTITATIVA.getValue(), null);
	    if (!listaValorVariableGraduados.isEmpty()
		    && listaValorVariableGraduados.size() == 1) {
		vvDto = listaValorVariableGraduados.get(0);
		evaluacionServicio.obtenerValorVariable(vvDto, faseiesDTO,
		        auditoriaDTO, vvDto.getVariableProcesoDTO()
		                .getSqlValorEvaluacion());
	    }
	    listaValorVariableGraduados = evaluacionServicio
		    .obtenerInformacionVariables(
		            GrupoVariableEnum.GRADUADOS.getValue(),
		            informacionIesDto.getId(),
		            TipoVariableEnum.CUANTITATIVA.getValue(), null);
	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error inesperado, comuníquese con el administrador.");
	}

    }

    public void cargarMuestraVariableDetalleEstudiante(
	    List<PersonaDTO> estudiantes, List<MuestraDetalleDTO> muestra) {
	listaMuestraDetalleEstudiantes.clear();
	for (int i = 0; i < estudiantes.size(); i++) {
	    for (int j = 0; j < muestra.size(); j++) {
		if (muestra.get(j).getIdTabla()
		        .equals(estudiantes.get(i).getId())) {
		    ItemEvaluacion item = new ItemEvaluacion();
		    item.setEstudiante(estudiantes.get(i));
		    item.setMuestraDetalle(muestra.get(j));
		    this.listaMuestraDetalleEstudiantes.add(item);
		    LOG.info("listaMuestraDetalle: "
			    + listaMuestraDetalleEstudiantes.size());
		}
	    }
	}
    }

    public void cargarMuestraVariableDetalleDocente(List<PersonaDTO> docentes,
	    List<MuestraDetalleDTO> muestra) {
	listaMuestraDetalleDocentes.clear();
	for (int i = 0; i < docentes.size(); i++) {
	    for (int j = 0; j < muestra.size(); j++) {
		if (muestra.get(j).getIdTabla().equals(docentes.get(i).getId())) {
		    ItemEvaluacion item = new ItemEvaluacion();
		    item.setDocente(docentes.get(i));
		    item.setMuestraDetalle(muestra.get(j));
		    this.listaMuestraDetalleDocentes.add(item);
		    LOG.info("listaMuestraDetalle: "
			    + listaMuestraDetalleDocentes.size());
		}
	    }
	}
    }

    public void cargarMuestraVariableDetalleConvenio(
	    List<ConvenioVigenteDTO> convenios, List<MuestraDetalleDTO> muestra) {
	listaMuestraDetalleConvenios.clear();
	for (int i = 0; i < convenios.size(); i++) {
	    for (int j = 0; j < muestra.size(); j++) {
		if (muestra.get(j).getIdTabla()
		        .equals(convenios.get(i).getId())) {
		    ItemEvaluacion item = new ItemEvaluacion();
		    item.setConvenioDTO(convenios.get(i));
		    item.setMuestraDetalle(muestra.get(j));
		    this.listaMuestraDetalleConvenios.add(item);
		    LOG.info("listaMuestraConvenios: "
			    + listaMuestraDetalleConvenios.size());
		}
	    }
	}
    }

    public void cargarEstudiantes() {
	estudiantes = true;
	matriculas = false;
	evidenciaConcepto = false;
	RequestContext context = RequestContext.getCurrentInstance();
	try {
	    listaEstudiantes = evaluacionServicio
		    .obtenerMuestraDetallePersonas(valorVariableSeleccionada
		            .getCodigoMuestra());
	    listaMuestraDetalle = evaluacionServicio
		    .obtenerMuestraDetalle(valorVariableSeleccionada
		            .getCodigoMuestra());
	    cargarMuestraVariableDetalleEstudiante(listaEstudiantes,
		    listaMuestraDetalle);
	    cargarItemMuestraDetalle();
	    // context.execute("dlgMuestraInstitucional.show()");

	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void cargarDocentes() {
	docenteSeleccionado = null;
	evidencias = false;
	identificacion = "";
	busqueda = false;
	contratos = false;
	evidenciaConcepto = false;
	experiencia = false;
	capacitacion = false;
	formacion = false;
	cargos = false;
	estudiantes = false;
	matriculas = false;
	try {
	    listaDocentes = evaluacionServicio
		    .obtenerMuestraDetallePersonas(valorVariableSeleccionada
		            .getCodigoMuestra());
	    listaMuestraDetalle = evaluacionServicio
		    .obtenerMuestraDetalle(valorVariableSeleccionada
		            .getCodigoMuestra());
	    cargarMuestraVariableDetalleDocente(listaDocentes,
		    listaMuestraDetalle);
	    cargarItemMuestraDetalleDocentes();

	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void cargarConvenios() {
	try {
	    listaConvenios = evaluacionServicio
		    .obtenerMuestraDetalleConvenios(valorVariableSeleccionada
		            .getCodigoMuestra());

	    listaMuestraDetalle = evaluacionServicio
		    .obtenerMuestraDetalle(valorVariableSeleccionada
		            .getCodigoMuestra());
	    cargarMuestraVariableDetalleConvenio(listaConvenios,
		    listaMuestraDetalle);
	    cargarItemMuestraDetalleConvenios();

	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void cargarConceptos() {
	evidencias = true;
	contratos = false;
	capacitacion = false;
	evidenciaConcepto = false;
	experiencia = false;
	formacion = false;
	cargos = false;
	conceptoSeleccionado = null;
	try {
	    listaEvidencia = catalogoServicio
		    .obtenerConceptoPorIdProcesoYGrupo(idProceso.toString(),
		            GrupoConceptoEnum.DOCENTE.getValor());

	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}

    }

    public void cargarEConceptoContrato() {
	contratos = true;
	conceptoSeleccionado = null;
	evidenciaConcepto = false;
	try {
	    listaEvidencia = catalogoServicio
		    .obtenerConceptoPorIdProcesoYGrupo(idProceso.toString(),
		            GrupoConceptoEnum.DOCENTE.getValor());
	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	for (ConceptoDTO variable : listaEvidencia) {
	    if (variable.getOrigen().equals(
		    OrigenInformacionEnum.CONTRATOS.getValor())) {
		conceptoSeleccionado = variable;
		break;
	    }
	}

	if (conceptoSeleccionado != null) {
	    if (conceptoSeleccionado.getOrigen().equals(
		    OrigenInformacionEnum.CONTRATOS.getValor())) {

		contratos = true;

		try {
		    listaContratacion = registroServicio
			    .obtenerContratacionPorPersona(docenteSeleccionado
			            .getDocente().getId(), informacionIesDto
			            .getId());
		} catch (ServicioException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		LOG.info("listaContratacion: " + listaContratacion.size());

	    }
	}

    }

    public void cargarEConcepto() {
	try {
	    evidenciaConcepto = true;
	    if (conceptoSeleccionado.getOrigen().equals(
		    OrigenInformacionEnum.CONTRATOS.getValor())) {
		idEvidencia = contratoSeleccionado.getId();
		nombreFichero = ies.getCodigo() + "_"
		        + contratoSeleccionado.getId();

	    } else if (conceptoSeleccionado.getOrigen().equals(
		    OrigenInformacionEnum.EXPERIENCIA.getValor())) {
		idEvidencia = experienciaSeleccionada.getId();
		nombreFichero = ies.getCodigo() + "_"
		        + experienciaSeleccionada.getId();

	    }

	    else if (conceptoSeleccionado.getOrigen().equals(
		    OrigenInformacionEnum.CAPACITACION.getValor())) {
		idEvidencia = capacitacionSeleccionada.getId();
		nombreFichero = ies.getCodigo() + "_"
		        + capacitacionSeleccionada.getId();

	    }

	    else if (conceptoSeleccionado.getOrigen().equals(
		    OrigenInformacionEnum.FORMACION.getValor())) {
		idEvidencia = formacionSeleccionada.getId();
		nombreFichero = ies.getCodigo() + "_"
		        + formacionSeleccionada.getId();

	    }

	    else if (conceptoSeleccionado.getOrigen().equals(
		    OrigenInformacionEnum.CARGOS.getValor())) {
		idEvidencia = cargoAcademicaSeleccionado.getId();
		nombreFichero = ies.getCodigo() + "_"
		        + cargoAcademicaSeleccionado.getId();

	    }

	    listaEvidenciaConcepto = institutosServicio
		    .obtenerEvidenciasDeIesPorIdConceptoEIdTabla(ies.getId(),
		            conceptoSeleccionado.getId(), idEvidencia,
		            conceptoSeleccionado.getOrigen());

	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public void cargarEvidencias() {

	try {
	    if (conceptoSeleccionado != null) {
		if (conceptoSeleccionado.getOrigen().equals(
		        OrigenInformacionEnum.CONTRATOS.getValor())) {

		    contratos = true;
		    experiencia = false;
		    evidenciaConcepto = false;
		    capacitacion = false;
		    formacion = false;
		    cargos = false;

		    listaContratacion = registroServicio
			    .obtenerContratacionPorPersona(docenteSeleccionado
			            .getDocente().getId(), informacionIesDto
			            .getId());
		    LOG.info("listaContratacion: " + listaContratacion.size());

		} else if (conceptoSeleccionado.getOrigen().equals(
		        OrigenInformacionEnum.EXPERIENCIA.getValor())) {
		    contratos = false;
		    experiencia = true;
		    evidenciaConcepto = false;
		    capacitacion = false;
		    formacion = false;
		    cargos = false;

		    listaExperienciaProfesional = registroServicio
			    .obtenerExperienciaProfesionalPorDocenteEIes(
			            docenteSeleccionado.getDocente().getId(),
			            ies.getId());
		    LOG.info("listaExperienciaProfesional: "
			    + listaExperienciaProfesional.size());

		}

		else if (conceptoSeleccionado.getOrigen().equals(
		        OrigenInformacionEnum.CAPACITACION.getValor())) {
		    contratos = false;
		    experiencia = false;
		    evidenciaConcepto = false;
		    capacitacion = true;
		    formacion = false;
		    cargos = false;

		    listaCapacitacion = registroServicio
			    .obtenerCursosPorDocenteEIes(docenteSeleccionado
			            .getDocente().getId(), ies.getId());

		} else if (conceptoSeleccionado.getOrigen().equals(
		        OrigenInformacionEnum.FORMACION.getValor())) {
		    contratos = false;
		    experiencia = false;
		    evidenciaConcepto = false;
		    capacitacion = false;
		    formacion = true;
		    cargos = false;

		    listaFormacion = registroServicio
			    .obtenerFormacionProfesionalPorPersonaEIes(
			            docenteSeleccionado.getDocente().getId(),
			            ies.getId());

		} else if (conceptoSeleccionado.getOrigen().equals(
		        OrigenInformacionEnum.CARGOS.getValor())) {
		    contratos = false;
		    experiencia = false;
		    evidenciaConcepto = false;
		    capacitacion = false;
		    formacion = false;
		    cargos = true;

		    listaCargosAcademicos = registroServicio
			    .obtenerCargosAcademicosPorDocenteEIes(
			            docenteSeleccionado.getDocente().getId(),
			            ies.getId());

		}
	    } else {
		contratos = false;
		evidenciaConcepto = false;
		experiencia = false;
		capacitacion = false;
		formacion = false;
		cargos = false;
	    }
	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public void guardarMuestraDetalle() {
	cambiosVariable = false;
	contadorVerificado = 0L;
	Integer modificados = 0;
	try {
	    for (int i = 0; i < this.listaMuestraDetalleEstudiantes.size(); i++) {
		// listaValorVariablesCualitativasModificados
		ItemEvaluacion vvdto = listaMuestraDetalleEstudiantes.get(i);
		if (!vvdto
		        .getMuestraDetalle()
		        .getVerificado()
		        .equals(listaMuestraDetalleEstudiantesOriginal.get(i)
		                .getMuestraDetalle().getVerificado())
		        || (vvdto.getMuestraDetalle().getObservaciones() != null && !vvdto
		                .getMuestraDetalle()
		                .getObservaciones()
		                .equals(listaMuestraDetalleEstudiantesOriginal
		                        .get(i).getMuestraDetalle()
		                        .getObservaciones()))) {

		    if (vvdto.getMuestraDetalle().getVerificado().equals("-1")
			    && (!vvdto.getMuestraDetalle().getObservaciones()
			            .equals(""))) {
			JsfUtil.msgError("Debe seleccionar alguna opción del campo aceptado para: "
			        + vvdto.getEstudiante().getNombres()
			        + " "
			        + vvdto.getEstudiante().getApellidoPaterno()
			        + " "
			        + vvdto.getEstudiante().getApellidoMaterno());
			return;
		    }
		    if (!vvdto.getMuestraDetalle().getVerificado().equals("-1")
			    && (null == vvdto.getMuestraDetalle()
			            .getObservaciones() || vvdto
			            .getMuestraDetalle().getObservaciones()
			            .equals(""))) {
			JsfUtil.msgError("Debe ingresar la observación para: "
			        + vvdto.getEstudiante().getNombres() + " "
			        + vvdto.getEstudiante().getApellidoPaterno()
			        + " "
			        + vvdto.getEstudiante().getApellidoMaterno());
			return;

		    }
		    if (vvdto
			    .getMuestraDetalle()
			    .getObservaciones()
			    .equals(listaMuestraDetalleEstudiantesOriginal
			            .get(i).getMuestraDetalle()
			            .getObservaciones())) {
			JsfUtil.msgAdvert("Debe cambiar la observación de: "
			        + vvdto.getEstudiante().getNombres() + " "
			        + vvdto.getEstudiante().getApellidoPaterno()
			        + " "
			        + vvdto.getEstudiante().getApellidoMaterno());
			return;
		    }
		} else {
		    modificados++;
		}
	    }
	    Double valor = 0.0;
	    Double todos = 0.0;
	    Double aceptado = 0.0;
	    Integer noAceptado = 0;
	    for (int i = 0; i < this.listaMuestraDetalleEstudiantes.size(); i++) {
		// listaValorVariablesCualitativasModificados
		ItemEvaluacion vvdto = listaMuestraDetalleEstudiantes.get(i);
		if (vvdto.getMuestraDetalle().getVerificado().equals("1")) {
		    aceptado++;
		    valor += vvdto.getMuestraDetalle().getPonderacion();
		} else if (vvdto.getMuestraDetalle().getVerificado()
		        .equals("0")
		        && null != valorVariableSeleccionada
		                .getValorVerificado()) {
		    noAceptado++;
		}
		todos += vvdto.getMuestraDetalle().getPonderacion();
		if (vvdto.getMuestraDetalle().getObservaciones() != null
		        && !vvdto.getMuestraDetalle().getObservaciones()
		                .isEmpty()
		        && (!vvdto
		                .getMuestraDetalle()
		                .getVerificado()
		                .equals(listaMuestraDetalleEstudiantesOriginal
		                        .get(i).getMuestraDetalle()
		                        .getVerificado()) || !vvdto
		                .getMuestraDetalle()
		                .getObservaciones()
		                .equals(listaMuestraDetalleEstudiantesOriginal
		                        .get(i).getMuestraDetalle()
		                        .getObservaciones()))) {

		    vvdto.getMuestraDetalle().getAuditoria()
			    .setUsuarioModificacion(this.usuario);
		    vvdto.getMuestraDetalle().getAuditoria()
			    .setFechaModificacion(new Date());
		    vvdto.getMuestraDetalle().setFaseIesDTO(this.faseiesDTO);
		    evaluacionServicio.registrarMuestraDetalle(vvdto
			    .getMuestraDetalle());
		    cambiosVariable = true;

		}

	    }

	    valor = Math.rint(valor * 100) / 100;
	    todos = Math.rint(todos * 100) / 100;
	    Double valorfinal = valorVariableSeleccionada
		    .getValorInicialPonderado() * valor / todos;
	    valorfinal = Math.rint(valorfinal * 100) / 100;
	    if (valorfinal.isNaN()) {
		valorfinal = 0.0;

	    }
	    valorVariableSeleccionada.setValor(valorfinal.toString());
	    valorVariableSeleccionada.setValorVerificado(aceptado);
	    valorVariableSeleccionada.setRegistrosNoAceptados(noAceptado);
	    valorVariableSeleccionada.setModificado(false);
	    evaluacionServicio
		    .registrarValorVariable(valorVariableSeleccionada);

	    listaMuestraDetalle = evaluacionServicio
		    .obtenerMuestraDetalle(valorVariableSeleccionada
		            .getCodigoMuestra());
	    listaValorVariable = evaluacionServicio
		    .obtenerInformacionVariables(
		            GrupoVariableEnum.ESTUDIANTES.getValue(),
		            informacionIesDto.getId(),
		            TipoVariableEnum.CUANTITATIVA.getValue(), null);
	    if (!valorVariableSeleccionada.getTotalMuestra()
		    .equals(modificados)) {
		JsfUtil.msgAdvert("Recuerde verificar que la observación de la Variable "
		        + valorVariableSeleccionada.getVariableProcesoDTO()
		                .getVariableDTO().getEtiqueta()
		        + ", corresponda a la modificación realizada en los criterios.");
		JsfUtil.msgInfo("Registros almacenados correctamente");
	    } else {
		JsfUtil.msgAdvert("No se ha realizado ningún cambio");
	    }

	    cargarMuestraVariableDetalleEstudiante(listaEstudiantes,
		    listaMuestraDetalle);
	    cargarItemMuestraDetalle();
	    cargarVariables();
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public void cargarMatricula() {
	matriculas = true;
	evidenciaConcepto = false;
	listaCarreraPeriodo = registroServicio
	        .obtenerMatriculasPorEstudianteEIes(
	                muestraDetallestudianteSeleccionada.getEstudiante()
	                        .getId(), informacionIesDto.getId());

    }

    public void cargarCarrera() {
	matriculas = true;
	evidenciaConcepto = false;
	try {
	    listaCarrerasEstudiante = registroServicio
		    .obtenerCarrerasEstudiante(muestraDetallestudianteSeleccionada
		            .getEstudiante().getId());
	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    public void cargarEConceptoMatricula() {
	evidenciaConcepto = true;
	conceptoSeleccionado = catalogoServicio.obtenerConceptoPorOrigenYGrupo(
	        OrigenInformacionEnum.CARRERA_ESTUDIANTE_PERIODO.getValor(),
	        GrupoConceptoEnum.ESTUDIANTE.getValor());
	if (conceptoSeleccionado.getOrigen().equals(
	        OrigenInformacionEnum.CARRERA_ESTUDIANTE_PERIODO.getValor())) {
	    idEvidencia = carreraPeriodoSeleccionado.getId();
	    nombreFichero = ies.getCodigo() + "_"
		    + carreraPeriodoSeleccionado.getId();
	}

	try {
	    listaEvidenciaConcepto = institutosServicio
		    .obtenerEvidenciasDeIesPorIdConceptoEIdTabla(ies.getId(),
		            conceptoSeleccionado.getId(), idEvidencia,
		            conceptoSeleccionado.getOrigen());

	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void cargarEConceptoCarrera() {
	evidenciaConcepto = true;
	conceptoSeleccionado = catalogoServicio.obtenerConceptoPorOrigenYGrupo(
	        OrigenInformacionEnum.CARRERA_ESTUDIANTE.getValor(),
	        GrupoConceptoEnum.ESTUDIANTE.getValor());

	if (conceptoSeleccionado.getOrigen().equals(
	        OrigenInformacionEnum.CARRERA_ESTUDIANTE.getValor())) {
	    idEvidencia = carreraSeleccionada.getId();
	    nombreFichero = ies.getCodigo() + "_" + carreraSeleccionada.getId();
	}

	try {
	    listaEvidenciaConcepto = institutosServicio
		    .obtenerEvidenciasDeIesPorIdConceptoEIdTabla(ies.getId(),
		            conceptoSeleccionado.getId(), idEvidencia,
		            conceptoSeleccionado.getOrigen());

	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void enviarEvidencia(EvidenciaDTO evidencia) {

	LOG.info(evidencia.getNombreArchivo());

	fichero = evidencia.getNombreArchivo();
	url = evidencia.getUrl();
	String[] nombreFichero = evidencia.getNombreArchivo().split("[.]");

	extensionDocumento = nombreFichero[nombreFichero.length - 1];
	LOG.info(extensionDocumento);
	LOG.info(url);
	descargar = true;
	return;

    }

    public void verificarSenescyt() {

	this.cargarTitulosSenescyt(muestraDetallestudianteSeleccionada
	        .getEstudiante().getIdentificacion(), ies);

	// ACTUALIZAR MUESTRA DETALLE, PONIENDO VERIFICADO SI en los
	// que
	// se validaron con el Senescyt.
	// ACTUALIZAR OBSERVACIÓN DE MUESTRA DETALLE COLOCANDO EN LA
	// OBSERVACIÓN: Validado Senescyt.

    }

    public void validarMuestraGraduado() {
	RequestContext context = RequestContext.getCurrentInstance();
	muestraDetallestudianteSeleccionada.getMuestraDetalle().setVerificado(
	        "1");
	muestraDetallestudianteSeleccionada.getMuestraDetalle()
	        .setObservaciones("Validado Senescyt");

	JsfUtil.msgInfo("Registro Validado Senescyt");
	context.execute("dlgMuestraSene.hide()");

    }

    public void cargarTitulosSenescyt(String cedula, IesDTO iesDto) {
	titulosSenescyt.clear();
	WebServiceController wsController = (WebServiceController) FacesContext
	        .getCurrentInstance().getExternalContext().getSessionMap()
	        .get("webServiceController");

	wsController.cargarIdentificacion(cedula);
	wsController.consultaSenescyt();
	List<TituloSenescyt> aux = new ArrayList<TituloSenescyt>();
	aux = wsController.getTitulosReconocidos();
	for (int i = 0; i < aux.size(); i++) {

	    titulosSenescyt.add(aux.get(i));

	}
    }

    public List<ValorVariableDTO> getListaValorVariable() {
	return listaValorVariable;
    }

    public void setListaValorVariable(List<ValorVariableDTO> listaValorVariable) {
	this.listaValorVariable = listaValorVariable;
    }

    public IesDTO getIes() {
	return ies;
    }

    public void setIes(IesDTO ies) {
	this.ies = ies;
    }

    public InformacionIesDTO getInformacionIesDto() {
	return informacionIesDto;
    }

    public void setInformacionIesDto(InformacionIesDTO informacionIesDto) {
	this.informacionIesDto = informacionIesDto;
    }

    public ValorVariableDTO getValorVariableSeleccionada() {
	return valorVariableSeleccionada;
    }

    public void setValorVariableSeleccionada(
	    ValorVariableDTO valorVariableSeleccionada) {
	this.valorVariableSeleccionada = valorVariableSeleccionada;
    }

    public List<PersonaDTO> getListaEstudiantes() {
	return listaEstudiantes;
    }

    public void setListaEstudiantes(List<PersonaDTO> listaEstudiantes) {
	this.listaEstudiantes = listaEstudiantes;
    }

    public Boolean getEstudiantes() {
	return estudiantes;
    }

    public void setEstudiantes(Boolean estudiantes) {
	this.estudiantes = estudiantes;
    }

    public PersonaDTO getEstudianteSeleccionado() {
	return estudianteSeleccionado;
    }

    public void setEstudianteSeleccionado(PersonaDTO estudianteSeleccionado) {
	this.estudianteSeleccionado = estudianteSeleccionado;
    }

    public List<ValorVariableDTO> getListaValorVariableModificada() {
	return listaValorVariableModificada;
    }

    public void setListaValorVariableModificada(
	    List<ValorVariableDTO> listaValorVariableModificada) {
	this.listaValorVariableModificada = listaValorVariableModificada;
    }

    public String getUsuario() {
	return usuario;
    }

    public void setUsuario(String usuario) {
	this.usuario = usuario;
    }

    public FaseIesDTO getFaseiesDTO() {
	return faseiesDTO;
    }

    public void setFaseiesDTO(FaseIesDTO faseiesDTO) {
	this.faseiesDTO = faseiesDTO;
    }

    public List<ItemEvaluacion> getListaMuestraDetalleEstudiantes() {
	return listaMuestraDetalleEstudiantes;
    }

    public void setListaMuestraDetalleEstudiantes(
	    List<ItemEvaluacion> listaMuestraDetalleEstudiantes) {
	this.listaMuestraDetalleEstudiantes = listaMuestraDetalleEstudiantes;
    }

    public List<MuestraDetalleDTO> getListaMuestraDetalle() {
	return listaMuestraDetalle;
    }

    public void setListaMuestraDetalle(
	    List<MuestraDetalleDTO> listaMuestraDetalle) {
	this.listaMuestraDetalle = listaMuestraDetalle;
    }

    public List<ItemEvaluacion> getListaMuestraDetalleEstudiantesOriginal() {
	return listaMuestraDetalleEstudiantesOriginal;
    }

    public void setListaMuestraDetalleEstudiantesOriginal(
	    List<ItemEvaluacion> listaMuestraDetalleEstudiantesOriginal) {
	this.listaMuestraDetalleEstudiantesOriginal = listaMuestraDetalleEstudiantesOriginal;
    }

    public ItemEvaluacion getMuestraDetallestudianteSeleccionada() {
	return muestraDetallestudianteSeleccionada;
    }

    public void setMuestraDetallestudianteSeleccionada(
	    ItemEvaluacion muestraDetallestudianteSeleccionada) {
	this.muestraDetallestudianteSeleccionada = muestraDetallestudianteSeleccionada;
    }

    public Boolean getMatriculas() {
	return matriculas;
    }

    public void setMatriculas(Boolean matriculas) {
	this.matriculas = matriculas;
    }

    public List<CarreraEstudiantePeriodoDTO> getListaCarreraPeriodo() {
	return listaCarreraPeriodo;
    }

    public void setListaCarreraPeriodo(
	    List<CarreraEstudiantePeriodoDTO> listaCarreraPeriodo) {
	this.listaCarreraPeriodo = listaCarreraPeriodo;
    }

    public Boolean getEvidenciaConcepto() {
	return evidenciaConcepto;
    }

    public void setEvidenciaConcepto(Boolean evidenciaConcepto) {
	this.evidenciaConcepto = evidenciaConcepto;
    }

    public ConceptoDTO getConceptoSeleccionado() {
	return conceptoSeleccionado;
    }

    public void setConceptoSeleccionado(ConceptoDTO conceptoSeleccionado) {
	this.conceptoSeleccionado = conceptoSeleccionado;
    }

    public Long getIdEvidencia() {
	return idEvidencia;
    }

    public void setIdEvidencia(Long idEvidencia) {
	this.idEvidencia = idEvidencia;
    }

    public String getNombreFichero() {
	return nombreFichero;
    }

    public void setNombreFichero(String nombreFichero) {
	this.nombreFichero = nombreFichero;
    }

    public List<EvidenciaConceptoDTO> getListaEvidenciaConcepto() {
	return listaEvidenciaConcepto;
    }

    public void setListaEvidenciaConcepto(
	    List<EvidenciaConceptoDTO> listaEvidenciaConcepto) {
	this.listaEvidenciaConcepto = listaEvidenciaConcepto;
    }

    public CarreraEstudiantePeriodoDTO getCarreraPeriodoSeleccionado() {
	return carreraPeriodoSeleccionado;
    }

    public void setCarreraPeriodoSeleccionado(
	    CarreraEstudiantePeriodoDTO carreraPeriodoSeleccionado) {
	this.carreraPeriodoSeleccionado = carreraPeriodoSeleccionado;
    }

    public String getFichero() {
	return fichero;
    }

    public void setFichero(String fichero) {
	this.fichero = fichero;
    }

    public Boolean getDescargar() {
	return descargar;
    }

    public void setDescargar(Boolean descargar) {
	this.descargar = descargar;
    }

    public String getUrl() {
	return url;
    }

    public void setUrl(String url) {
	this.url = url;
    }

    public String getExtensionDocumento() {
	return extensionDocumento;
    }

    public void setExtensionDocumento(String extensionDocumento) {
	this.extensionDocumento = extensionDocumento;
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

    public void setDocumentoDescarga(StreamedContent documentoDescarga) {
	this.documentoDescarga = documentoDescarga;
    }

    public Long getContadorVerificado() {
	return contadorVerificado;
    }

    public void setContadorVerificado(Long contadorVerificado) {
	this.contadorVerificado = contadorVerificado;
    }

    public List<ValorVariableDTO> getListaValorVariableDocentes() {
	return listaValorVariableDocentes;
    }

    public void setListaValorVariableDocentes(
	    List<ValorVariableDTO> listaValorVariableDocentes) {
	this.listaValorVariableDocentes = listaValorVariableDocentes;
    }

    public List<ValorVariableDTO> getListaValorVariableDocentesModificada() {
	return listaValorVariableDocentesModificada;
    }

    public void setListaValorVariableDocentesModificada(
	    List<ValorVariableDTO> listaValorVariableDocentesModificada) {
	this.listaValorVariableDocentesModificada = listaValorVariableDocentesModificada;
    }

    public List<ItemEvaluacion> getListaMuestraDetalleDocentes() {
	return listaMuestraDetalleDocentes;
    }

    public void setListaMuestraDetalleDocentes(
	    List<ItemEvaluacion> listaMuestraDetalleDocentes) {
	this.listaMuestraDetalleDocentes = listaMuestraDetalleDocentes;
    }

    public List<PersonaDTO> getListaDocentes() {
	return listaDocentes;
    }

    public void setListaDocentes(List<PersonaDTO> listaDocentes) {
	this.listaDocentes = listaDocentes;
    }

    public List<ItemEvaluacion> getListaMuestraDetalleDocentesOriginal() {
	return listaMuestraDetalleDocentesOriginal;
    }

    public void setListaMuestraDetalleDocentesOriginal(
	    List<ItemEvaluacion> listaMuestraDetalleDocentesOriginal) {
	this.listaMuestraDetalleDocentesOriginal = listaMuestraDetalleDocentesOriginal;
    }

    public List<ValorVariableDTO> getListaValorVariableConvenios() {
	return listaValorVariableConvenios;
    }

    public void setListaValorVariableConvenios(
	    List<ValorVariableDTO> listaValorVariableConvenios) {
	this.listaValorVariableConvenios = listaValorVariableConvenios;
    }

    public List<ValorVariableDTO> getListaValorVariableConveniosModificada() {
	return listaValorVariableConveniosModificada;
    }

    public void setListaValorVariableConveniosModificada(
	    List<ValorVariableDTO> listaValorVariableConveniosModificada) {
	this.listaValorVariableConveniosModificada = listaValorVariableConveniosModificada;
    }

    public Boolean getCambiosVariable() {
	return cambiosVariable;
    }

    public void setCambiosVariable(Boolean cambiosVariable) {
	this.cambiosVariable = cambiosVariable;
    }

    public List<ValorVariableDTO> getListaValorVariableGraduados() {
	return listaValorVariableGraduados;
    }

    public void setListaValorVariableGraduados(
	    List<ValorVariableDTO> listaValorVariableGraduados) {
	this.listaValorVariableGraduados = listaValorVariableGraduados;
    }

    public List<CarreraEstudianteDTO> getListaCarrerasEstudiante() {
	return listaCarrerasEstudiante;
    }

    public void setListaCarrerasEstudiante(
	    List<CarreraEstudianteDTO> listaCarrerasEstudiante) {
	this.listaCarrerasEstudiante = listaCarrerasEstudiante;
    }

    public CarreraEstudianteDTO getCarreraSeleccionada() {
	return carreraSeleccionada;
    }

    public void setCarreraSeleccionada(CarreraEstudianteDTO carreraSeleccionada) {
	this.carreraSeleccionada = carreraSeleccionada;
    }

    public List<ValorVariableDTO> getListaValorVariableGraduadosModificada() {
	return listaValorVariableGraduadosModificada;
    }

    public void setListaValorVariableGraduadosModificada(
	    List<ValorVariableDTO> listaValorVariableGraduadosModificada) {
	this.listaValorVariableGraduadosModificada = listaValorVariableGraduadosModificada;
    }

    public List<TituloSenescyt> getTitulosSenescyt() {
	return titulosSenescyt;
    }

    public void setTitulosSenescyt(List<TituloSenescyt> titulosSenescyt) {
	this.titulosSenescyt = titulosSenescyt;
    }

    public ItemEvaluacion getDocenteSeleccionado() {
	return docenteSeleccionado;
    }

    public void setDocenteSeleccionado(ItemEvaluacion docenteSeleccionado) {
	this.docenteSeleccionado = docenteSeleccionado;
    }

    public List<CursoCapacitacionDTO> getListaCapacitacion() {
	return listaCapacitacion;
    }

    public void setListaCapacitacion(
	    List<CursoCapacitacionDTO> listaCapacitacion) {
	this.listaCapacitacion = listaCapacitacion;
    }

    public Boolean getEvidencias() {
	return evidencias;
    }

    public void setEvidencias(Boolean evidencias) {
	this.evidencias = evidencias;
    }

    public Boolean getContratos() {
	return contratos;
    }

    public void setContratos(Boolean contratos) {
	this.contratos = contratos;
    }

    public Boolean getCapacitacion() {
	return capacitacion;
    }

    public void setCapacitacion(Boolean capacitacion) {
	this.capacitacion = capacitacion;
    }

    public Boolean getCargos() {
	return cargos;
    }

    public void setCargos(Boolean cargos) {
	this.cargos = cargos;
    }

    public Boolean getExperiencia() {
	return experiencia;
    }

    public void setExperiencia(Boolean experiencia) {
	this.experiencia = experiencia;
    }

    public Boolean getFormacion() {
	return formacion;
    }

    public void setFormacion(Boolean formacion) {
	this.formacion = formacion;
    }

    public String getIdentificacion() {
	return identificacion;
    }

    public void setIdentificacion(String identificacion) {
	this.identificacion = identificacion;
    }

    public Boolean getBusqueda() {
	return busqueda;
    }

    public void setBusqueda(Boolean busqueda) {
	this.busqueda = busqueda;
    }

    public List<ConceptoDTO> getListaEvidencia() {
	return listaEvidencia;
    }

    public void setListaEvidencia(List<ConceptoDTO> listaEvidencia) {
	this.listaEvidencia = listaEvidencia;
    }

    public String getIdProceso() {
	return idProceso;
    }

    public void setIdProceso(String idProceso) {
	this.idProceso = idProceso;
    }

    public List<ContratacionDTO> getListaContratacion() {
	return listaContratacion;
    }

    public void setListaContratacion(List<ContratacionDTO> listaContratacion) {
	this.listaContratacion = listaContratacion;
    }

    public FormacionProfesionalDTO getFormacionSeleccionada() {
	return formacionSeleccionada;
    }

    public void setFormacionSeleccionada(
	    FormacionProfesionalDTO formacionSeleccionada) {
	this.formacionSeleccionada = formacionSeleccionada;
    }

    public ContratacionDTO getContratoSeleccionado() {
	return contratoSeleccionado;
    }

    public void setContratoSeleccionado(ContratacionDTO contratoSeleccionado) {
	this.contratoSeleccionado = contratoSeleccionado;
    }

    public ExperienciaProfesionalDTO getExperienciaSeleccionada() {
	return experienciaSeleccionada;
    }

    public void setExperienciaSeleccionada(
	    ExperienciaProfesionalDTO experienciaSeleccionada) {
	this.experienciaSeleccionada = experienciaSeleccionada;
    }

    public CursoCapacitacionDTO getCapacitacionSeleccionada() {
	return capacitacionSeleccionada;
    }

    public void setCapacitacionSeleccionada(
	    CursoCapacitacionDTO capacitacionSeleccionada) {
	this.capacitacionSeleccionada = capacitacionSeleccionada;
    }

    public CargoAcademicoDTO getCargoAcademicaSeleccionado() {
	return cargoAcademicaSeleccionado;
    }

    public void setCargoAcademicaSeleccionado(
	    CargoAcademicoDTO cargoAcademicaSeleccionado) {
	this.cargoAcademicaSeleccionado = cargoAcademicaSeleccionado;
    }

    public List<FormacionProfesionalDTO> getListaFormacion() {
	return listaFormacion;
    }

    public void setListaFormacion(List<FormacionProfesionalDTO> listaFormacion) {
	this.listaFormacion = listaFormacion;
    }

    public List<CargoAcademicoDTO> getListaCargosAcademicos() {
	return listaCargosAcademicos;
    }

    public void setListaCargosAcademicos(
	    List<CargoAcademicoDTO> listaCargosAcademicos) {
	this.listaCargosAcademicos = listaCargosAcademicos;
    }

    public List<ExperienciaProfesionalDTO> getListaExperienciaProfesional() {
	return listaExperienciaProfesional;
    }

    public void setListaExperienciaProfesional(
	    List<ExperienciaProfesionalDTO> listaExperienciaProfesional) {
	this.listaExperienciaProfesional = listaExperienciaProfesional;
    }

    public List<ConvenioVigenteDTO> getListaConvenios() {
	return listaConvenios;
    }

    public void setListaConvenios(List<ConvenioVigenteDTO> listaConvenios) {
	this.listaConvenios = listaConvenios;
    }

    public List<ItemEvaluacion> getListaMuestraDetalleConvenios() {
	return listaMuestraDetalleConvenios;
    }

    public void setListaMuestraDetalleConvenios(
	    List<ItemEvaluacion> listaMuestraDetalleConvenios) {
	this.listaMuestraDetalleConvenios = listaMuestraDetalleConvenios;
    }

    public List<ItemEvaluacion> getListaMuestraDetalleConveniosOriginal() {
	return listaMuestraDetalleConveniosOriginal;
    }

    public void setListaMuestraDetalleConveniosOriginal(
	    List<ItemEvaluacion> listaMuestraDetalleConveniosOriginal) {
	this.listaMuestraDetalleConveniosOriginal = listaMuestraDetalleConveniosOriginal;
    }

    public Boolean getAlertaEvaluador() {
	return alertaEvaluador;
    }

    public void setAlertaEvaluador(Boolean alertaEvaluador) {
	this.alertaEvaluador = alertaEvaluador;
    }

    public String getPerfil() {
	return perfil;
    }

    public void setPerfil(String perfil) {
	this.perfil = perfil;
    }

}
