package ec.gob.ceaaces.controller;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

import org.primefaces.context.RequestContext;
import org.primefaces.event.TabChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import ec.gob.ceaaces.catalogo.dtos.catalogos.AreaConocimientoDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.AuditoriaDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.CarreraIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.IesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.PaisDTO;
import ec.gob.ceaaces.catalogo.enumeraciones.EtniaEnum;
import ec.gob.ceaaces.catalogo.exceptions.ServicioException;
import ec.gob.ceaaces.data.CarreraInformacion;
import ec.gob.ceaaces.data.EstudianteCarrera;
import ec.gob.ceaaces.institutos.dtos.evaluacion.AsignaturaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.CarreraEstudianteAsignaturaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.CarreraEstudianteDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.CarreraEstudiantePeriodoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.DocenteDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EstudianteDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionCarreraDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.MallaCurricularDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.PeriodoAcademicoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.PracticaPreprofesionalDTO;
import ec.gob.ceaaces.institutos.enumeraciones.DiscapacidadEnum;
import ec.gob.ceaaces.institutos.enumeraciones.GeneroEnum;
import ec.gob.ceaaces.institutos.enumeraciones.MotivoBecaEnum;
import ec.gob.ceaaces.institutos.enumeraciones.TipoAyudaBecaEnum;
import ec.gob.ceaaces.institutos.enumeraciones.TipoSeccionEnum;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.JsfUtil;
import ec.gob.ceaaces.util.Util;

/**
 * Class description goes here.
 * 
 * @author jhomara
 * 
 */

public class EstudianteController implements Serializable {

    /**
     * classVar1 documentation comment serialVersionUID
     */

    private static final long serialVersionUID = -4483113728968709783L;

    private static final Logger LOG = Logger
	    .getLogger(EstudianteController.class.getSimpleName());

    @Autowired
    private RegistroServicio registroServicio;
    @Autowired
    private CatalogoServicio catalogoServicio;

    private static final Integer[] rangos = { 10, 20, 50, 100, 200 };
    private int registros = 10;
    private int indice;
    private int indiceAtras;
    private int indiceSiguiente;
    private List<EstudianteDTO> listaEstudiantesIesDTO;
    private List<EstudianteDTO> listaEstudiantesIesDTOEncontrados;
    private List<EstudianteDTO> listaEstudiantesTodos;
    private List<EstudianteCarrera> listaCarreraEstudiante;
    private EstudianteCarrera estudianteCarreraSeleccionada;
    private EstudianteDTO estudianteSeleccionado;
    private List<CarreraInformacion> listaMatriculasEstudiante;
    private List<CarreraEstudianteDTO> listaCarreraEstudianteFiltradoDTO;
    private List<PaisDTO> listaPaisDTO;
    private List<PracticaPreprofesionalDTO> listaPracticaPreprofesionalDTO;
    private CarreraEstudianteDTO carreraEstudianteDTO;
    private PracticaPreprofesionalDTO practicaPreprofesionalDTO;
    private CarreraIesDTO carreraSeleccionada = new CarreraIesDTO();
    private AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
    private String[] tipoDocumentaciones;
    private String montoBeca;
    private String identificacion;
    private IesDTO iesDTO;
    private InformacionIesDTO informacionIesDTO;
    private Long idInformacionIes;
    private int numRegistros;
    private boolean errorValidacion;
    private boolean activarPestanias;
    private List<Integer> listaAniosMatricula;
    private List<SelectItem> areasConocimiento;
    private String usuario;
    private DocenteDTO docentePractica;
    private List<SelectItem> subAreasConocimiento;
    private String cedulaDocenteTutor;
    private InformacionCarreraDTO informacionCarrera;
    private int pestaniaActiva;
    private CarreraIesDTO carerraSeleccionada;
    private List<Integer> listaAniosBeca;
    private List<CarreraEstudianteAsignaturaDTO> listaCarreraEstudianteMalla;
    private CarreraEstudianteAsignaturaDTO seleccion;
    private CarreraEstudianteAsignaturaDTO seleccionEditar;
    private List<MallaCurricularDTO> mallasCurriculares;
    private MallaCurricularDTO selectMalla;
    private List<AsignaturaDTO> asignaturasDisponibles;
    private AsignaturaDTO selectAsignaturaDto;
    private Date minDate;
    private Date maxDate;
    private boolean busqueda;
    private List<PeriodoAcademicoDTO> listaPeriodosMatricula;
    private Long idPeriodoSeleccionado;
    private Long idCarreraEstudianteSeleccionada;
    private Map<Long, String> listaMallas;
    private CarreraEstudiantePeriodoDTO periodoSeleccionado;
    private boolean nuevoPeriodo;
    private Long idMallaSeleccionada;
    // private String perfil;
    // private boolean perfilCar = false;
    private boolean editarMallaEstudiante = false;
    private List<InformacionCarreraDTO> carrerasIesDTO;
    private List<CarreraEstudiantePeriodoDTO> listaCarreraEstudiantePeriodos;
    private boolean habilitarSiguiente;
    private Double totalCreditos;
    private Date maxFechaNacimiento;
    private FaseIesDTO faseIesDTO;
    private Integer numeroEstudiantes = 0;
    private List<TipoSeccionEnum> secciones;
    private String seccionSeleccionada;
    private List<String> etnias;

    private boolean ingresoPorConvalidacion;
    private boolean esGraduado;
    private Date fechaActual;

    private List<PeriodoAcademicoDTO> periodosAcademicos = new ArrayList<>();
    private Long idPeriodoAcademico = -1L;
    private Boolean alertaUsuarioIes = false;
    private Boolean alertaFaseRectificacion = false;
    private String perfil;

    public EstudianteController() {

	listaCarreraEstudianteFiltradoDTO = new ArrayList<CarreraEstudianteDTO>();
	listaPaisDTO = new ArrayList<PaisDTO>();
	carreraEstudianteDTO = new CarreraEstudianteDTO();
	estudianteSeleccionado = new EstudianteDTO();
	listaEstudiantesIesDTO = new ArrayList<EstudianteDTO>();
	listaEstudiantesIesDTOEncontrados = new ArrayList<EstudianteDTO>();
	listaEstudiantesTodos = new ArrayList<EstudianteDTO>();
	listaAniosMatricula = new ArrayList<Integer>();
	practicaPreprofesionalDTO = new PracticaPreprofesionalDTO();
	listaPracticaPreprofesionalDTO = new ArrayList<PracticaPreprofesionalDTO>();
	areasConocimiento = new ArrayList<SelectItem>();
	listaCarreraEstudiante = new ArrayList<EstudianteCarrera>();
	docentePractica = new DocenteDTO();
	subAreasConocimiento = new ArrayList<SelectItem>();
	listaAniosBeca = new ArrayList<Integer>();
	listaCarreraEstudianteMalla = new ArrayList<CarreraEstudianteAsignaturaDTO>();
	pestaniaActiva = 0;
	seleccion = new CarreraEstudianteAsignaturaDTO();
	mallasCurriculares = new ArrayList<>();
	selectMalla = new MallaCurricularDTO();
	asignaturasDisponibles = new ArrayList<>();
	selectAsignaturaDto = new AsignaturaDTO();
	seleccionEditar = new CarreraEstudianteAsignaturaDTO();
	this.minDate = new Date();
	this.setMaxDate(new Date());
	this.listaMallas = new HashMap<>();
	this.listaPeriodosMatricula = new ArrayList<>();
	this.periodoSeleccionado = new CarreraEstudiantePeriodoDTO();
	this.carrerasIesDTO = new ArrayList<>();
	this.listaCarreraEstudiantePeriodos = new ArrayList<CarreraEstudiantePeriodoDTO>();
	this.habilitarSiguiente = true;
	this.etnias = new ArrayList<String>();

    }

    @PostConstruct
    public void start() throws Exception {

	ListaIesController controller = (ListaIesController) JsfUtil
	        .obtenerObjetoSesion("listaIesController");

	usuario = SecurityContextHolder.getContext().getAuthentication()
	        .getName();
	this.etnias.clear();
	alertaFaseRectificacion = false;
	alertaUsuarioIes = false;
	auditoriaDTO.setUsuarioModificacion(usuario);
	// TIPO DOCUMENTO
	tipoDocumentaciones = new String[2];
	tipoDocumentaciones[0] = "CEDULA";
	tipoDocumentaciones[1] = "PASAPORTE";
	secciones = Arrays.asList(TipoSeccionEnum.values());
	cargarEstudiantes();
	llenarPaisNacionalidad();

	for (EtniaEnum etnia : EtniaEnum.values()) {
	    this.etnias.add(etnia.getValue());
	}

	this.perfil = controller.getPerfil().getNombre();

	if (this.perfil.startsWith("IES_USUARIO")) {
	    alertaUsuarioIes = true;
	}

	if (this.faseIesDTO.getTipo().name().startsWith("RECTIFICACION")) {
	    alertaFaseRectificacion = true;
	}

    }

    public void buscarEstudiantePorCedula() {

	if (!identificacion.equals("")) {
	    indice = 0;
	    EstudianteDTO estudianteDTO = new EstudianteDTO();
	    this.listaEstudiantesIesDTO.clear();
	    this.listaEstudiantesIesDTOEncontrados.clear();
	    this.listaEstudiantesTodos.clear();
	    estudianteDTO.setIdentificacion(identificacion);
	    estudianteDTO.setIdInformacionIes(this.informacionIesDTO.getId());

	    try {

		estudianteDTO = registroServicio
		        .obtenerEstudiantePorCedula(estudianteDTO);

		if (estudianteDTO != null) {
		    listaEstudiantesIesDTO.add(estudianteDTO);
		    this.listaEstudiantesIesDTOEncontrados.add(estudianteDTO);
		    this.listaEstudiantesTodos.add(estudianteDTO);

		    JsfUtil.msgInfo("Estudiante encontrado");
		} else {
		    JsfUtil.msgInfo("Estudiante NO encontrado");
		}

	    } catch (Exception e) {
		e.printStackTrace();
		JsfUtil.msgError("Falló búsqueda de estudiante por cédula");

	    }
	} else {
	    JsfUtil.msgError("Ingrese número de cédula");
	}
	busqueda = true;
	habilitarSiguiente = true;
    }

    private void cargarPeriodosAcademicos() {
	try {
	    idPeriodoAcademico = -1L;
	    periodosAcademicos.clear();
	    periodosAcademicos = registroServicio
		    .obtenerPeriodosAcademicosPorIes(informacionIesDTO.getId());
	} catch (Exception e) {
	    JsfUtil.msgError("Error al cargar periodos academicos de Ies, comuníquese con el administrador del sistema");
	    return;
	}
    }

    public void buscarEstudiantePorPeriodoAcademico() {
	limpiarListas();
	listarEstudiantes();
    }

    public void nuevoEstudiante() {
	estudianteSeleccionado = new EstudianteDTO();
	listaCarreraEstudianteMalla.clear();
	listaCarreraEstudiante.clear();
	listaCarreraEstudianteFiltradoDTO.clear();
	listaPracticaPreprofesionalDTO.clear();
	activarPestanias = false;
	idPeriodoSeleccionado = null;
	idPeriodoAcademico = -1L;
	periodoSeleccionado = new CarreraEstudiantePeriodoDTO();
	pestaniaActiva = 0;
	// if (perfilCar) {
	// obtenerMallas();
	// }
	ExternalContext ec = FacesContext.getCurrentInstance()
	        .getExternalContext();
	try {
	    ec.redirect("/" + ec.getContextName()
		    + "/estudiantes/editarEstudiante.jsf");
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public void nuevaMatricula() {
	ingresoPorConvalidacion = false;
	esGraduado = false;
	carreraEstudianteDTO = new CarreraEstudianteDTO();
	// if (!perfilCar) {
	carreraSeleccionada = new CarreraIesDTO();
	// }
    }

    public void nuevaPractica() {
	practicaPreprofesionalDTO = new PracticaPreprofesionalDTO();
	practicaPreprofesionalDTO.setCarreraEstudianteDTO(carreraEstudianteDTO);
	this.cedulaDocenteTutor = null;
    }

    public void llenarPaisNacionalidad() {
	try {
	    listaPaisDTO = catalogoServicio.obtenerPaises();
	} catch (ServicioException e) {
	    e.printStackTrace();
	}
    }

    public void obtenerCarrerasIes() {
	try {
	    // carrerasIesDTO =
	    // catalogoServicio.obtenerCarrerasPorIes(this.iesDTO
	    // .getCodigo());
	    LOG.info("this.iesDTO: " + this.iesDTO.getId());
	    carrerasIesDTO = registroServicio
		    .obtenerInformacionCarreraPorInformacionIes(this.informacionIesDTO
		            .getId());
	    LOG.info("carrerasIesDTO: " + carrerasIesDTO.size());
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void tomarTituloTab(TabChangeEvent event) {

    }

    public void guardarEstudiante() {

	// if (alertaFaseRectificacion && alertaUsuarioIes) {
	// if (estudianteSeleccionado.getId() != null
	// && !estudianteSeleccionado.getFaseIesDTO().getTipo().name()
	// .startsWith("RECTIFICACION")) {
	// JsfUtil.msgError("No se puede editar este estudiante en fase de Rectificación.");
	// return;
	// }
	// }

	estudianteSeleccionado.setIdInformacionIes(informacionIesDTO.getId());
	estudianteSeleccionado.setFaseIesDTO(faseIesDTO);
	auditoriaDTO.setUsuarioModificacion(this.usuario);
	auditoriaDTO.setFechaModificacion(new Date());
	if (!validarDatosEstudiante()) {
	    errorValidacion = true;
	    return;
	}
	try {
	    // estudiante nuevo
	    if (estudianteSeleccionado.getId() == null) {
		EstudianteDTO estudianteDTO = null;
		try {
		    estudianteDTO = registroServicio
			    .obtenerEstudiantePorCedula(estudianteSeleccionado);
		} catch (Exception e) {

		}
		if (estudianteDTO == null) {
		    // becas
		    // if (listaBecasDTO != null && listaBecasDTO.size() >= 1) {
		    // estudianteSeleccionado.setBecas(listaBecasDTO);
		    // }
		    // existe la matrcula
		    if (listaCarreraEstudianteFiltradoDTO != null
			    && listaCarreraEstudianteFiltradoDTO.size() >= 1) {
			// activo
			estudianteSeleccionado.setActivo(true);
			// auditoria
			estudianteSeleccionado.setAuditoria(auditoriaDTO);

			// origen
			estudianteSeleccionado.setOrigenCarga("SISTEMA");
			estudianteSeleccionado
			        .setCarrerasEstudianteDTO(listaCarreraEstudianteFiltradoDTO);

			estudianteSeleccionado = registroServicio
			        .registrarEstudiante(estudianteSeleccionado);

			JsfUtil.msgInfo("Registro almacenado correctamente");

			List<CarreraEstudianteDTO> carrerasEstudiante = registroServicio
			        .obtenerCarrerasEstudiante(estudianteSeleccionado
			                .getId());
			obtenerCarrerasEstudiante(carrerasEstudiante);

			errorValidacion = false;

		    } else {
			JsfUtil.msgAdvert("Ingresar información de la carrera en la pestaña Carreras");
			activarPestanias = true;
			return;
		    }
		} else {
		    JsfUtil.msgError("El estudiante con cedula: "
			    + estudianteSeleccionado.getIdentificacion()
			    + " ya se encuentra registrado");
		}
	    } else {
		estudianteSeleccionado.setAuditoria(auditoriaDTO);

		// origen
		estudianteSeleccionado.setOrigenCarga("SISTEMA");

		estudianteSeleccionado = registroServicio
		        .registrarEstudiante(estudianteSeleccionado);

		JsfUtil.msgInfo("Registro almacenado correctamente");
		errorValidacion = false;
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al guardar Estudiante");
	}
    }

    // BUSCAR SUBAREA
    public PaisDTO buscarPais(Long idPais) {
	for (PaisDTO paisDTO : listaPaisDTO) {
	    if (paisDTO.getId().equals(idPais)) {
		return paisDTO;
	    }
	}
	return null;
    }

    public void guardarMatricula() {

	RequestContext context = RequestContext.getCurrentInstance();
	context.addCallbackParam("cerrarVentana", false);

	// if (alertaFaseRectificacion && alertaUsuarioIes) {
	// if (carreraEstudianteDTO.getId() != null
	// && !carreraEstudianteDTO.getFaseIesDTO().getTipo().name()
	// .startsWith("RECTIFICACION")) {
	// JsfUtil.msgError("No se puede editar esta matrícula en fase de Rectificación.");
	// return;
	// }
	// }

	if (!validarDatosMatricula()) {
	    errorValidacion = true;
	    return;
	}

	try {
	    // TODO
	    // idInformacionIes = controller.getIdInformacionIes();

	    // activo
	    carreraEstudianteDTO.setActivo(true);

	    // origen carga
	    carreraEstudianteDTO.setOrigenCarga("SISTEMA");

	    // auditoria carrera
	    auditoriaDTO.setFechaModificacion(new Date());

	    // auditoria carreraEstudiante
	    carreraEstudianteDTO.setAuditoriaDTO(auditoriaDTO);

	    InformacionCarreraDTO infoCar = registroServicio
		    .obtenerInformacionCarreraPorCarrera(carreraSeleccionada);
	    carreraEstudianteDTO.setInformacionCarreraDTO(infoCar);
	    carreraEstudianteDTO.setFaseIesDTO(this.faseIesDTO);

	    // existe estudiante
	    if (estudianteSeleccionado.getId() != null) {
		carreraEstudianteDTO.setEstudianteDTO(estudianteSeleccionado);
		for (EstudianteCarrera c : listaCarreraEstudiante) {
		    if (carreraEstudianteDTO
			    .getInformacionCarreraDTO()
			    .getId()
			    .equals(c.getCarreraEstudianteDTO()
			            .getInformacionCarreraDTO().getId())
			    && carreraEstudianteDTO.getId() == null) {
			JsfUtil.msgError("La carrera ya está asignada al estudiante");
			return;
		    }
		}

		carreraEstudianteDTO = registroServicio
		        .registrarMatricula(carreraEstudianteDTO);
		errorValidacion = false;
		List<CarreraEstudianteDTO> carrerasEstudiante = registroServicio
		        .obtenerCarrerasEstudiante(estudianteSeleccionado
		                .getId());
		obtenerCarrerasEstudiante(carrerasEstudiante);
		carreraEstudianteDTO = new CarreraEstudianteDTO();

		JsfUtil.msgInfo("Registro almacenado correctamente");

		RequestContext.getCurrentInstance().execute(
		        "dlgMatricula.hide();");
	    } else {
		EstudianteCarrera estudianteCarrera = new EstudianteCarrera();
		estudianteCarrera.setCarreraEstudianteDTO(carreraEstudianteDTO);
		estudianteCarrera.setEditable(true);
		listaCarreraEstudiante.add(estudianteCarrera);
		listaCarreraEstudianteFiltradoDTO.add(carreraEstudianteDTO);
		guardarEstudiante();
	    }
	    context.addCallbackParam("cerrarVentana", true);
	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al guardar Matrícula");
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al guardar Matrícula");
	}
    }

    public void guardarPractica() {
	if (validarDatosPractica()) {
	    try {
		AuditoriaDTO auditoria = new AuditoriaDTO();
		auditoria.setFechaModificacion(new Date());
		auditoria.setUsuarioModificacion(usuario);
		practicaPreprofesionalDTO.setAuditoriaDTO(auditoria);
		practicaPreprofesionalDTO.setActivo(true);

		LOG.info("idCarreraEstudianteSeleccionada: "
		        + idCarreraEstudianteSeleccionada);
		CarreraEstudianteDTO carreraEstudianteDTO = new CarreraEstudianteDTO();
		carreraEstudianteDTO.setId(idCarreraEstudianteSeleccionada);
		practicaPreprofesionalDTO
		        .setCarreraEstudianteDTO(carreraEstudianteDTO);

		practicaPreprofesionalDTO.setFaseIesDTO(faseIesDTO);

		practicaPreprofesionalDTO = registroServicio
		        .registrarPracticaPreprofesional(practicaPreprofesionalDTO);
		listaPracticaPreprofesionalDTO = registroServicio
		        .obtenerPracticasPorEstudiante(practicaPreprofesionalDTO
		                .getCarreraEstudianteDTO().getId());
		JsfUtil.msgInfo("Registro almacenado correctamente");

	    } catch (Exception e) {
		e.printStackTrace();
		JsfUtil.msgError("Error al guardar el Registro");
	    }

	}
    }

    public void guardarPeriodo() {

	for (CarreraEstudiantePeriodoDTO periodo : listaCarreraEstudiantePeriodos) {
	    if (periodoSeleccionado.getPeriodoAcademicoDTO().getId() != null) {
		if (periodoSeleccionado.getPeriodoAcademicoDTO().getId()
		        .equals(periodo.getPeriodoAcademicoDTO().getId())) {
		    if (periodoSeleccionado.getId() != null
			    && periodoSeleccionado.getId().equals(
			            periodo.getId())) {
			continue;
		    } else {
			FacesContext
			        .getCurrentInstance()
			        .addMessage(
			                null,
			                new FacesMessage(
			                        FacesMessage.SEVERITY_INFO,
			                        "El periodo académico ya ha sido registrado",
			                        null));
			return;

		    }
		}
	    }
	}
	auditoriaDTO.setFechaModificacion(new Date());
	periodoSeleccionado.setAuditoriaDTO(auditoriaDTO);
	periodoSeleccionado.setActivo(true);
	periodoSeleccionado.setCarreraEstudianteDTO(this.carreraEstudianteDTO);
	periodoSeleccionado.setFaseIesDTO(faseIesDTO);
	try {
	    periodoSeleccionado.setFaseIesDTO(faseIesDTO);
	    periodoSeleccionado = registroServicio
		    .registrarCarreraEstudiantePeriodo(periodoSeleccionado);
	    obtenerPeriodosAcademicos();
	    JsfUtil.msgInfo("Registro almacenado correctamente");
	    RequestContext.getCurrentInstance().execute("dlgPeriodo.hide();");

	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void nuevoPeriodo() {
	periodoSeleccionado = new CarreraEstudiantePeriodoDTO();
	RequestContext.getCurrentInstance().reset("formPeriodoAcademico");
    }

    public void editarPeriodo() {
	LOG.info("Periodo Seleccionado"
	        + periodoSeleccionado.getPeriodoAcademicoDTO());
	// if (alertaFaseRectificacion && alertaUsuarioIes) {
	// if (periodoSeleccionado.getId() != null
	// && !periodoSeleccionado.getFaseIesDTO().getTipo().name()
	// .startsWith("RECTIFICACION")) {
	// JsfUtil.msgError("No se puede editar esta matrícula en fase de Rectificación.");
	// return;
	// }
	// }
	seccionSeleccionada = periodoSeleccionado.getSeccion().getValor();
	RequestContext.getCurrentInstance().reset("formPeriodoAcademico");
	RequestContext.getCurrentInstance().execute("dlgPeriodo.show();");

    }

    public void eliminarPeriodo() {
	// if (alertaFaseRectificacion && alertaUsuarioIes) {
	// if (periodoSeleccionado.getId() != null
	// && !periodoSeleccionado.getFaseIesDTO().getTipo().name()
	// .startsWith("RECTIFICACION")) {
	// JsfUtil.msgError("No se puede eliminar este periodo en fase de Rectificación.");
	// return;
	// }
	// }

	auditoriaDTO.setFechaModificacion(new Date());
	periodoSeleccionado.setAuditoriaDTO(auditoriaDTO);
	periodoSeleccionado.setActivo(false);
	periodoSeleccionado.setCarreraEstudianteDTO(this.carreraEstudianteDTO);
	try {
	    periodoSeleccionado.setFaseIesDTO(faseIesDTO);
	    periodoSeleccionado = registroServicio
		    .registrarCarreraEstudiantePeriodo(periodoSeleccionado);
	    obtenerPeriodosAcademicos();
	    JsfUtil.msgInfo("Registro eliminado correctamente");
	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void eliminarPractica() {

	if (alertaFaseRectificacion && alertaUsuarioIes) {
	    if (practicaPreprofesionalDTO.getId() != null
		    && !practicaPreprofesionalDTO.getFaseIesDTO().getTipo()
		            .name().startsWith("RECTIFICACION")) {
		JsfUtil.msgError("No se puede eliminar esta práctica en fase de Rectificación.");
		return;
	    }
	}

	AuditoriaDTO auditoria = new AuditoriaDTO();
	auditoria.setFechaModificacion(new Date());
	auditoria.setUsuarioModificacion(usuario);
	practicaPreprofesionalDTO.setAuditoriaDTO(auditoria);
	practicaPreprofesionalDTO.setActivo(false);
	practicaPreprofesionalDTO.setFaseIesDTO(faseIesDTO);
	try {
	    practicaPreprofesionalDTO = registroServicio
		    .registrarPracticaPreprofesional(practicaPreprofesionalDTO);

	    JsfUtil.msgInfo("Registro eliminado correctamente");

	    JsfUtil.msgInfo("Registro eliminado correctamente");

	    listaPracticaPreprofesionalDTO = registroServicio
		    .obtenerPracticasPorEstudiante(carreraEstudianteDTO.getId());
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public boolean validarDatosEstudiante() {

	boolean valido = true;

	if (estudianteSeleccionado.getTipoIdentificacion().equals("")) {
	    JsfUtil.msgError("Seleccione el tipo de documento");
	    return false;
	}

	if (estudianteSeleccionado.getTipoIdentificacion().equals("CEDULA")) {
	    if (!Util.validarCedula(estudianteSeleccionado.getIdentificacion())) {
		JsfUtil.msgError("Número de cédula incorrecto");
		return false;
	    }
	}

	if (estudianteSeleccionado.getIdentificacion().equals("")) {
	    JsfUtil.msgError("Ingrese número de cédula");
	    return false;
	}

	if (estudianteSeleccionado.getNombres().equals("")) {
	    JsfUtil.msgError("Ingrese los nombres");
	    return false;
	}
	if (estudianteSeleccionado.getApellidoPaterno().equals("")) {
	    JsfUtil.msgError("Ingrese primer apellido");
	    return false;
	}

	if (estudianteSeleccionado.getApellidoMaterno().equals("")) {
	    JsfUtil.msgError("Ingrese segundo apellido");
	    return false;
	}

	if (estudianteSeleccionado.getSexo() == null) {
	    JsfUtil.msgError("Seleccione el sexo");
	    return false;
	}

	if (null == estudianteSeleccionado.getEtnia()) {
	    JsfUtil.msgError("Auto-identificación Étnica es requerido");
	    return false;
	}

	if (estudianteSeleccionado.getDiscapacidad().equals(
	        DiscapacidadEnum.NINGUNA)) {
	    estudianteSeleccionado.setNumeroConadis(null);
	} else {
	    if (null == estudianteSeleccionado
		    || estudianteSeleccionado.getNumeroConadis().equals("")) {
		JsfUtil.msgError("Número Conadis es requerido");
		return false;
	    }
	}

	// if (estudianteSeleccionado.getDireccion().equals("")) {
	// JsfUtil.msgError("Ingrese dirección");
	// return false;
	// }
	// if (estudianteSeleccionado.getPaisOrigen().getId() == null) {
	// JsfUtil.msgError("Seleccione el país de nacionalidad");
	// return false;
	// }
	return valido;
    }

    public Boolean validarDatosMatricula2() {
	boolean valido = true;
	// cr��ditos
	// if (!Util.validarEsEntero(carreraEstudianteDTO
	// .getNumeroCreditosAprobados().toString())
	// || carreraEstudianteDTO.getNumeroCreditosAprobados().toString()
	// .length() > 3) {
	// JsfUtil.msgError("Ingrese un número de créditos válido");
	// return false;
	// }

	// cr��ditos aprobados
	// if (!Util.validarEsPositivo(carreraEstudianteDTO
	// .getNumeroCreditosAprobados().toString())) {
	// JsfUtil.msgError("Ingrese un número de créditos positivo");
	// return false;
	// }

	if (carreraEstudianteDTO.getNumeroCreditosAprobados() == null) {
	    JsfUtil.msgError("Ingrese el número de créditos");
	}

	// fecha inicio primer nivel
	if (carreraEstudianteDTO.getFechaInicioPrimerNivel() == null) {
	    JsfUtil.msgError("Ingrese fecha de inicio de primer nivel");
	    return false;
	}

	if (carreraEstudianteDTO.getFechaGraduacion() != null) {
	    if (carreraEstudianteDTO.getFechaGraduacion().compareTo(
		    carreraEstudianteDTO.getFechaInicioPrimerNivel()) == 0
		    || carreraEstudianteDTO.getFechaGraduacion().compareTo(
		            carreraEstudianteDTO.getFechaInicioPrimerNivel()) < 1) {
		JsfUtil.msgError("Fecha de graduacion debe ser mayor a la fecha inicio de carrera");
		return false;
	    }
	}
	if (carreraEstudianteDTO.getFechaInicioPrimerNivel() != null
	        && carreraEstudianteDTO.getFechaInicioPrimerNivel().compareTo(
	                new Date()) >= 0) {
	    JsfUtil.msgError("Fecha de inicio de carrera debe ser menor a la fecha actual");
	    return false;
	}
	if (carreraEstudianteDTO.getFechaGraduacion() != null
	        && carreraEstudianteDTO.getFechaGraduacion().compareTo(
	                new Date()) >= 0) {
	    JsfUtil.msgError("Fecha de graduacion debe ser menor a la fecha actual");
	    return false;
	}
	return valido;
    }

    public Boolean validarDatosMatricula() {

	// cr��ditos

	// fecha inicio primer nivel

	if (ingresoPorConvalidacion) {

	    carreraEstudianteDTO.setFechaInicioPrimerNivel(null);

	    if (null == carreraEstudianteDTO.getFechaConvalidacion()) {
		JsfUtil.msgError("Ingrese fecha de ingreso por convalidación");
		return false;
	    }

	    if (esGraduado) {

		if (null == carreraEstudianteDTO.getFechaGraduacion()) {
		    JsfUtil.msgError("Ingrese fecha graduación");
		    return false;
		}

		if (carreraEstudianteDTO.getFechaGraduacion().compareTo(
		        carreraEstudianteDTO.getFechaConvalidacion()) == 0
		        || carreraEstudianteDTO.getFechaGraduacion().compareTo(
		                carreraEstudianteDTO.getFechaConvalidacion()) < 1) {
		    JsfUtil.msgError("Fecha de graduacion debe ser mayor a la fecha de ingreso por convalidación.");
		    return false;
		}

	    } else { // si no es graduado
		carreraEstudianteDTO.setFechaGraduacion(null);
		carreraEstudianteDTO.setNumeroRegistroSenescyt(null);
	    }

	} else {

	    carreraEstudianteDTO.setFechaConvalidacion(null);

	    if (null == carreraEstudianteDTO.getFechaInicioPrimerNivel()) {
		JsfUtil.msgError("Ingrese fecha de inicio de primer nivel");
		return false;
	    }

	    if (esGraduado) {

		if (null == carreraEstudianteDTO.getFechaGraduacion()) {
		    JsfUtil.msgError("Ingrese fecha de graduación.");
		    return false;
		}

		if (carreraEstudianteDTO.getFechaGraduacion().compareTo(
		        carreraEstudianteDTO.getFechaInicioPrimerNivel()) == 0
		        || carreraEstudianteDTO.getFechaGraduacion().compareTo(
		                carreraEstudianteDTO
		                        .getFechaInicioPrimerNivel()) < 1) {
		    JsfUtil.msgError("Fecha de graduacion debe ser mayor a la fecha inicio de carrera");
		    return false;
		}
	    } else {
		// si no es graduado
		carreraEstudianteDTO.setFechaGraduacion(null);
		carreraEstudianteDTO.setNumeroRegistroSenescyt(null);
	    }
	}

	return true;
    }

    public boolean validarDatosPractica() {
	if (estudianteSeleccionado.getId() == null) {
	    JsfUtil.msgAdvert("Ingresar información de la carrera en la pestaña Carreras");
	    return false;
	}
	if (practicaPreprofesionalDTO.getInstitucion() == null
	        || practicaPreprofesionalDTO.getInstitucion().isEmpty()) {
	    JsfUtil.msgError("Ingrese nombre de la Institución");
	    return false;
	}
	if (practicaPreprofesionalDTO.getNumeroHoras() == null
	        || practicaPreprofesionalDTO.getNumeroHoras().intValue() == 0) {
	    JsfUtil.msgError("Ingrese el número de horas");
	    return false;
	}

	if (practicaPreprofesionalDTO.getFechaInicio() == null) {
	    JsfUtil.msgError("Ingrese fecha de Inicio");
	    return false;
	}

	if (practicaPreprofesionalDTO.getFechaFin() != null) {
	    if (practicaPreprofesionalDTO.getFechaFin().compareTo(
		    practicaPreprofesionalDTO.getFechaInicio()) == 0
		    || practicaPreprofesionalDTO.getFechaFin().compareTo(
		            practicaPreprofesionalDTO.getFechaInicio()) < 1) {
		JsfUtil.msgError("Fecha fin debe ser mayor a fecha inicio de práctica");
		return false;
	    }
	}

	return true;
    }

    public void editarAsignatura() {
	LOG.info("Editando");

	if (alertaFaseRectificacion && alertaUsuarioIes) {
	    if (carreraEstudianteDTO.getId() != null
		    && !carreraEstudianteDTO.getFaseIesDTO().getTipo().name()
		            .startsWith("RECTIFICACION")) {
		JsfUtil.msgError("No se puede editar esta matrícula en fase de Rectificación.");
		return;
	    }
	}
	LOG.info(seleccion.getAsignaturaDTO().getNombre());
	CarreraIesDTO carIesDTO = new CarreraIesDTO();
	carIesDTO.setId(carreraEstudianteDTO.getInformacionCarreraDTO()
	        .getCarreraIesDTO().getId());
	carIesDTO.setNombre(carreraEstudianteDTO.getInformacionCarreraDTO()
	        .getCarreraIesDTO().getNombre());
	carIesDTO.setCodigo(carreraEstudianteDTO.getInformacionCarreraDTO()
	        .getCarreraIesDTO().getCodigo());

	if (carreraEstudianteDTO.getFechaInicioPrimerNivel() != null) {
	    ingresoPorConvalidacion = false;
	} else {
	    ingresoPorConvalidacion = true;
	}
	if (carreraEstudianteDTO.getFechaGraduacion() != null) {
	    esGraduado = true;
	} else {
	    esGraduado = false;
	}

	selectAsignaturaDto = seleccion.getAsignaturaDTO();
	idMallaSeleccionada = seleccion.getAsignaturaDTO()
	        .getMallaCurricularDTO().getId();
	this.periodoSeleccionado = seleccion.getCarreraEstudiantePeriodoDTO();

	carreraSeleccionada = carIesDTO;
    }

    public void editarMatricula() {
	// TODO

	LOG.info("editarMatricula..");

	// if (alertaFaseRectificacion && alertaUsuarioIes) {
	// if (carreraEstudianteDTO.getId() != null
	// && !carreraEstudianteDTO.getFaseIesDTO().getTipo().name()
	// .startsWith("RECTIFICACION")) {
	// JsfUtil.msgError("No se puede editar esta matrícula en fase de Rectificación.");
	// return;
	// }
	// }

	CarreraIesDTO carIesDTO = new CarreraIesDTO();
	carIesDTO.setId(carreraEstudianteDTO.getInformacionCarreraDTO()
	        .getCarreraIesDTO().getId());
	carIesDTO.setNombre(carreraEstudianteDTO.getInformacionCarreraDTO()
	        .getCarreraIesDTO().getNombre());
	carIesDTO.setCodigo(carreraEstudianteDTO.getInformacionCarreraDTO()
	        .getCarreraIesDTO().getCodigo());

	if (carreraEstudianteDTO.getFechaInicioPrimerNivel() != null) {
	    ingresoPorConvalidacion = false;
	} else {
	    ingresoPorConvalidacion = true;
	}
	if (carreraEstudianteDTO.getFechaGraduacion() != null) {
	    esGraduado = true;
	} else {
	    esGraduado = false;
	}

	carreraSeleccionada = carIesDTO;

	RequestContext context = RequestContext.getCurrentInstance();
	context.execute("dlgMatricula.show();");
    }

    public void editarEstudiante() {

	/*
	 * if (alertaFaseRectificacion && alertaUsuarioIes) { if
	 * (estudianteSeleccionado.getId() != null &&
	 * !estudianteSeleccionado.getFaseIesDTO().getTipo().name()
	 * .startsWith("RECTIFICACION")) { JsfUtil.msgError(
	 * "No se puede editar esta matrícula en fase de Rectificación.");
	 * return; } }
	 */
	listaCarreraEstudiante.clear();
	this.carreraEstudianteDTO = new CarreraEstudianteDTO();
	estudianteSeleccionado = registroServicio
	        .obtenerEstudiantePorId(estudianteSeleccionado.getId());
	obtenerCarrerasEstudiante(estudianteSeleccionado
	        .getCarrerasEstudianteDTO());

	try {
	    // if (perfilCar) {
	    CarreraEstudianteDTO carreraEstudianteDTO = new CarreraEstudianteDTO();
	    for (EstudianteCarrera car : listaCarreraEstudiante) {

		// if (car.getCarreraEstudianteDTO().getInformacionCarreraDTO()
		// .getId().equals(informacionCarrera.getId())) {
		carreraEstudianteDTO = car.getCarreraEstudianteDTO();
		listaPracticaPreprofesionalDTO = registroServicio
		        .obtenerPracticasPorEstudiante(carreraEstudianteDTO
		                .getId());
		practicaPreprofesionalDTO
		        .setCarreraEstudianteDTO(carreraEstudianteDTO);
		break;
		// }
	    }
	    // }
	    activarPestanias = true;
	    FacesContext context1 = FacesContext.getCurrentInstance();
	    ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) context1
		    .getApplication().getNavigationHandler();
	    handler.performNavigation("editarEstudiante?faces-redirect=true");
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    public void obtenerCarrerasEstudiante(
	    List<CarreraEstudianteDTO> carrerasEstudiante) {
	listaCarreraEstudiante.clear();

	for (CarreraEstudianteDTO car : carrerasEstudiante) {
	    EstudianteCarrera est = new EstudianteCarrera();
	    est.setCarreraEstudianteDTO(car);
	    est.setEditable(true);
	    est.setAbrirMalla(false);

	    try {
		listaPracticaPreprofesionalDTO = registroServicio
		        .obtenerPracticasPorEstudiante(car.getId());
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    idPeriodoSeleccionado = null;
	    periodoSeleccionado = new CarreraEstudiantePeriodoDTO();
	    listaCarreraEstudiante.add(est);
	    // obtenerMallas();
	    editarMallaEstudiante = true;

	}

    }

    public void eliminarEstudiante() {

	// if (alertaFaseRectificacion && alertaUsuarioIes) {
	// if (estudianteSeleccionado.getId() != null
	// && !estudianteSeleccionado.getFaseIesDTO().getTipo().name()
	// .startsWith("RECTIFICACION")) {
	// JsfUtil.msgError("No se puede eliminar este estudiante en fase de Rectificación.");
	// return;
	// }
	// }

	for (int i = 0; i < listaEstudiantesIesDTOEncontrados.size(); i++) {
	    if (listaEstudiantesIesDTOEncontrados.get(i).getId()
		    .equals(estudianteSeleccionado.getId())) {
		try {
		    estudianteSeleccionado.setActivo(false);
		    estudianteSeleccionado.setAuditoria(auditoriaDTO);
		    estudianteSeleccionado.setFaseIesDTO(faseIesDTO);
		    estudianteSeleccionado
			    .setIdInformacionIes(informacionIesDTO.getId());
		    registroServicio
			    .registrarEstudiante(estudianteSeleccionado);
		    listaEstudiantesIesDTOEncontrados.remove(i);
		    JsfUtil.msgInfo("Registro eliminado correctamente");

		    return;

		} catch (ServicioException e) {
		    e.printStackTrace();
		    JsfUtil.msgError("Eliminar estudiante:"
			    + e.getLocalizedMessage());

		} catch (Exception e) {
		    e.printStackTrace();
		    JsfUtil.msgError("Eliminar estudiante");

		}

	    }
	}
	estudianteSeleccionado = new EstudianteDTO();
    }

    public void eliminarMatricula() {

	// if (alertaFaseRectificacion && alertaUsuarioIes) {
	// if (carreraEstudianteDTO.getId() != null
	// && !carreraEstudianteDTO.getFaseIesDTO().getTipo().name()
	// .startsWith("RECTIFICACION")) {
	// JsfUtil.msgError("No se puede eliminar esta matrícula en fase de Rectificación.");
	// return;
	// }
	// }

	if (listaCarreraEstudiante.size() == 1) {
	    JsfUtil.msgInfo("El estudiante debe tener al menos una carrera asignada, ingrese la nueva carrera antes de eliminar la actual");

	    return;
	}
	for (int i = 0; i < listaCarreraEstudiante.size(); i++) {
	    if (listaCarreraEstudiante.get(i).getCarreraEstudianteDTO().getId()
		    .equals(carreraEstudianteDTO.getId())) {
		carreraEstudianteDTO.setActivo(false);
		carreraEstudianteDTO.setAuditoriaDTO(auditoriaDTO);
		try {
		    carreraEstudianteDTO.setFaseIesDTO(faseIesDTO);
		    registroServicio.registrarMatricula(carreraEstudianteDTO);
		    listaCarreraEstudiante.remove(i);
		    JsfUtil.msgInfo("Registro eliminado correctamente");
		    carreraEstudianteDTO = new CarreraEstudianteDTO();
		} catch (ServicioException e) {
		    e.printStackTrace();
		    JsfUtil.msgError("Error al eliminar carrera");
		    carreraEstudianteDTO = new CarreraEstudianteDTO();
		} catch (Exception e) {
		    e.printStackTrace();
		    JsfUtil.msgError("Error al eliminar carrera");
		    carreraEstudianteDTO = new CarreraEstudianteDTO();
		}
	    }
	}

    }

    public void tomarRango(ValueChangeEvent event) {
	FacesContext context = FacesContext.getCurrentInstance();
	registros = ((Integer) event.getNewValue()).intValue();

	indice = 0;
	numRegistros = 0;
	listaEstudiantesIesDTO.clear();
	listaEstudiantesTodos.clear();
	listaEstudiantesIesDTOEncontrados.clear();
	listarEstudiantes();

	// indice = indice - registros;

	if (registros == 0) {
	    listaEstudiantesIesDTO.clear();
	    listaEstudiantesIesDTOEncontrados.clear();
	}

	context.renderResponse();
    }

    public void cargarEstudiantes() {
	ListaIesController controller = (ListaIesController) JsfUtil
	        .obtenerObjetoSesion("listaIesController");
	// perfil = controller.getPerfil().getNombre();
	usuario = controller.getUsuario();
	carreraSeleccionada = controller.getCarrera();
	docentePractica = new DocenteDTO();
	iesDTO = controller.getIes();
	try {

	    iesDTO = controller.getIes();
	    informacionIesDTO = registroServicio
		    .obtenerInformacionIesPorIes(controller.getIes());
	    if (idPeriodoAcademico == -1) {
		numeroEstudiantes = registroServicio.totalEstudiantesPorIes(
		        informacionIesDTO.getId(), null);
	    } else {
		numeroEstudiantes = registroServicio.totalEstudiantesPorIes(
		        informacionIesDTO.getId(), idPeriodoAcademico);
	    }
	    informacionCarrera = null;
	    carreraSeleccionada = new CarreraIesDTO();
	    carreraEstudianteDTO = new CarreraEstudianteDTO();

	} catch (Exception e) {
	    e.printStackTrace();
	}
	faseIesDTO = controller.getFaseIesDTO();
	cargarPeriodosAcademicos();
	obtenerCarrerasIes();
	limpiarListas();
	listarEstudiantes();
	activarPestanias = false;
	editarMallaEstudiante = false;

    }

    public void listarEstudiantes() {

	if (busqueda) {
	    limpiarListas();
	    busqueda = false;
	    listarEstudiantes();
	    identificacion = null;
	    habilitarSiguiente = true;
	}

	if (indice < 0 || registros == 0) {
	    listaEstudiantesIesDTO.clear();
	    listaEstudiantesIesDTOEncontrados.clear();
	    indice = 0;
	    return;
	}

	// EstudianteDTO EstudianteDTO = null;
	this.listaEstudiantesIesDTO.clear();
	this.listaEstudiantesIesDTOEncontrados.clear();

	try {
	    if (listaEstudiantesTodos.size() <= (numRegistros)) {
		int indiceaux = 0;
		if (listaEstudiantesTodos.size() != 0) {
		    indiceaux = listaEstudiantesTodos.size() - 1;
		} else {
		    indiceaux = 0;
		}
		LOG.info("DATOS: IdInfIes: " + informacionIesDTO.getId()
		        + " - " + indiceaux + "-" + registros + "PA:==="
		        + idPeriodoAcademico);
		if (idPeriodoAcademico == -1) {

		    this.listaEstudiantesTodos.addAll(registroServicio
			    .obtenerEstudiantesPorInstituto(
			            informacionIesDTO.getId(), null, indiceaux,
			            registros));
		} else {
		    this.listaEstudiantesTodos.clear();
		    this.listaEstudiantesTodos.addAll(registroServicio
			    .obtenerEstudiantesPorInstituto(
			            informacionIesDTO.getId(),
			            idPeriodoAcademico, indiceaux, registros));

		    numeroEstudiantes = registroServicio
			    .totalEstudiantesPorIes(informacionIesDTO.getId(),
			            idPeriodoAcademico);
		}
		List<EstudianteDTO> auxLista = new ArrayList<EstudianteDTO>();
		auxLista.addAll(listaEstudiantesTodos);
		if (auxLista.size() != 0) {
		    if (listaEstudiantesTodos.size() < indice + registros) {
			this.listaEstudiantesIesDTO = auxLista.subList(indice,
			        listaEstudiantesTodos.size());
		    } else {
			if (listaEstudiantesTodos.size() < indice) {

			} else {
			    this.listaEstudiantesIesDTO = auxLista.subList(
				    indice, indice + registros);
			}
		    }
		}
		this.listaEstudiantesIesDTOEncontrados
		        .addAll(listaEstudiantesIesDTO);
	    } else {
		List<EstudianteDTO> auxLista = new ArrayList<EstudianteDTO>();
		auxLista.addAll(listaEstudiantesTodos);
		if (auxLista.size() != 0) {

		    this.listaEstudiantesIesDTO = auxLista.subList(indice,
			    indice + registros);
		    this.listaEstudiantesIesDTOEncontrados
			    .addAll(listaEstudiantesIesDTO);
		}
	    }
	    if (listaEstudiantesIesDTO.size() < registros) {
		habilitarSiguiente = false;
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Se produjo un error al listar estudiantes");
	}
	if (listaEstudiantesTodos.isEmpty()) {
	    JsfUtil.msgInfo("No existen estudiantes registrados en el periodo seleccionado");
	    limpiarListas();
	}

    }

    public void modificarIndice(ActionEvent event) {
	FacesContext context = FacesContext.getCurrentInstance();
	String botonId = event.getComponent().getClientId(context).toString();

	if (botonId.contains("btnAtras")) {
	    if (listaEstudiantesTodos.size() < registros) {
		indiceAtras = indice - listaEstudiantesIesDTO.size();
	    } else {
		indiceAtras = indice - registros;
	    }
	    indice = indiceAtras;
	    indiceAtras = 0;
	    habilitarSiguiente = true;
	}

	if (botonId.contains("btnSiguiente")) {
	    if (listaEstudiantesTodos.size() < registros) {
		indice = 0;
	    } else {
		indiceSiguiente = indice + registros;
		indice = indiceSiguiente;
	    }
	    indiceSiguiente = 0;
	    if ((registros + indice) > numRegistros) {
		numRegistros = numRegistros + registros;
	    }
	}

    }

    public void selectCarreraEstudiante() {
	try {
	    pestaniaActiva = 0;
	    editarMallaEstudiante = true;
	    listaCarreraEstudianteMalla = new ArrayList<>();
	    List<CarreraEstudianteAsignaturaDTO> auxEstudianteMalla = new ArrayList<>();

	    if (idPeriodoSeleccionado != null) {
		auxEstudianteMalla = registroServicio
		        .obtenerAsignaturasPorPeriodo(idPeriodoSeleccionado);
	    } else {
		auxEstudianteMalla = registroServicio
		        .obtenerAsignaturasPorIDCarrera(carreraEstudianteDTO
		                .getId());
	    }
	    listaCarreraEstudianteMalla.addAll(auxEstudianteMalla);
	    obtenerPeriodosAcademicos();
	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgInfo("Error al obtener asignaturas");
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgInfo("Error al obtener asignaturas");
	}
    }

    public void seleccionarPeriodo(ValueChangeEvent event) {

	if (event != null) {
	    String id = String.valueOf(event.getComponent().getAttributes()
		    .get("id"));
	    if (event.getNewValue() == null) {
		idPeriodoSeleccionado = null;
	    } else {
		idPeriodoSeleccionado = new Long(event.getNewValue().toString());

	    }
	    if (id.equals("cmbPeriodoListaAsignatura")) {
		selectCarreraEstudiante();
	    }
	}
    }

    public void limpiarListas() {
	listaEstudiantesIesDTO.clear();
	listaEstudiantesIesDTOEncontrados.clear();
	// listaBecasDTO.clear();
	listaCarreraEstudianteFiltradoDTO.clear();
	listaEstudiantesTodos.clear();
	// listaMatriculasEstudiante.clear();
	numRegistros = 0;
	indice = 0;
	registros = 10;
    }

    public void limpiarValoresAsignatura() {
	selectAsignaturaDto = new AsignaturaDTO();
	seleccion = new CarreraEstudianteAsignaturaDTO();
	idMallaSeleccionada = null;
	selectMalla = new MallaCurricularDTO();
	nuevoPeriodo = false;
    }

    public void creaCarreraEstudianteAsignatura() {

	try {
	    if (!validarExisteAsignatura()) {
		// Auditoria
		AuditoriaDTO auditoria = new AuditoriaDTO();
		auditoria.setUsuarioModificacion(usuario);
		auditoria.setFechaModificacion(new Date());
		seleccion.setAuditoriaDTO(auditoria);

		seleccion.setCarreraEstudiantePeriodoDTO(periodoSeleccionado);
		seleccion.setAsignaturaDTO(selectAsignaturaDto);
		seleccion.setFaseIesDTO(faseIesDTO);

		if (seleccion.getId() == null) {
		    seleccion.setActivo(true);
		    registroServicio
			    .registrarCarreraEstudianteAsignatura(seleccion);
		    JsfUtil.msgInfo("Registro almacenado correctamente");

		    RequestContext.getCurrentInstance().execute(
			    "dlgNuevoCarreraEstAsig.hide();");

		    seleccion = new CarreraEstudianteAsignaturaDTO();
		    selectAsignaturaDto = new AsignaturaDTO();
		    mallasCurriculares = new ArrayList<>();
		} else {
		    registroServicio
			    .registrarCarreraEstudianteAsignatura(seleccion);
		    JsfUtil.msgInfo("Registro actualizado correctamente");
		    RequestContext.getCurrentInstance().execute(
			    "dlgNuevoCarreraEstAsig.hide();");

		    seleccion = new CarreraEstudianteAsignaturaDTO();
		    selectAsignaturaDto = new AsignaturaDTO();
		    mallasCurriculares = new ArrayList<>();

		}
		selectCarreraEstudiante();

	    } else {
		JsfUtil.msgError("La asignatura "
		        + selectAsignaturaDto.getCodigo()
		        + " ya ha sido registrada para el estudiante.");
	    }
	} catch (ServicioException e) {
	    JsfUtil.msgError("Ocurrio un error inesperado");
	    e.printStackTrace();
	}

    }

    public boolean validarExisteAsignatura() {
	if (seleccion.getId() == null) {
	    for (CarreraEstudianteAsignaturaDTO carEstAsig : listaCarreraEstudianteMalla) {
		if (carEstAsig.getAsignaturaDTO().getId()
		        .equals(selectAsignaturaDto.getId())) {
		    return true;
		}
	    }
	}
	return false;
    }

    public void eliminarCarreraEstudianteAsignatura() {

	if (alertaFaseRectificacion && alertaUsuarioIes) {
	    if (seleccion.getId() != null
		    && !seleccion.getFaseIesDTO().getTipo().name()
		            .startsWith("RECTIFICACION")) {
		JsfUtil.msgError("No se puede eliminar esta carrera en fase de Rectificación.");
		return;
	    }
	}

	try {
	    // Auditoria
	    AuditoriaDTO auditoria = new AuditoriaDTO();
	    auditoria.setUsuarioModificacion(usuario);
	    auditoria.setFechaModificacion(new Date());

	    seleccion.setAuditoriaDTO(auditoria);
	    seleccion.setActivo(false);
	    seleccion.setFaseIesDTO(faseIesDTO);
	    registroServicio.registrarCarreraEstudianteAsignatura(seleccion);
	    JsfUtil.msgInfo("Registro eliminado correctamente");
	    selectCarreraEstudiante();
	    seleccion = new CarreraEstudianteAsignaturaDTO();
	} catch (ServicioException e) {
	    JsfUtil.msgError("Ocurrio un error inesperado");
	    e.printStackTrace();
	}
    }

    public void actualizarCarreraEstudianteAsignatura() {

	CarreraEstudianteAsignaturaDTO carreraAsignatura = new CarreraEstudianteAsignaturaDTO();
	// LOG.info("ID RELACION: " + seleccion.getId());
	try {
	    // Auditoria
	    AuditoriaDTO auditoria = new AuditoriaDTO();
	    auditoria.setUsuarioModificacion(usuario);
	    carreraAsignatura = seleccionEditar;
	    carreraAsignatura.setAuditoriaDTO(auditoria);
	    if (carreraAsignatura
		    .getCarreraEstudiantePeriodoDTO()
		    .getPeriodoAcademicoDTO()
		    .getFechaInicioPeriodo()
		    .before(carreraAsignatura.getCarreraEstudiantePeriodoDTO()
		            .getPeriodoAcademicoDTO().getFechaFinPeriodo())) {
		carreraAsignatura.setFaseIesDTO(faseIesDTO);
		registroServicio
		        .registrarCarreraEstudianteAsignatura(carreraAsignatura);
		JsfUtil.msgInfo("Registro actualizado correctamente");

		RequestContext.getCurrentInstance().execute(
		        "dlgEditarCarreraEstAsig.hide();");
	    } else {
		JsfUtil.msgError("La fecha de inicio debe ser menor que la fecha de fin de periodo");
	    }
	} catch (ServicioException e) {
	    JsfUtil.msgError("Ocurrio un error inesperado");
	    e.printStackTrace();
	}
    }

    /**
     * Obtiene listado de asignaturas disponibles para ser asignadas a un
     * docente
     * 
     * @author fochoa
     * 
     */
    // public void obtenerMallas() {
    // asignaturasDisponibles.clear();
    // try {
    // mallasCurriculares = new ArrayList<>();
    // mallasCurriculares = registroServicio
    // .obtenerMallaCurricular(informacionCarrera);
    // // for (CarreraEstudianteAsignaturaDTO carEstAsig :
    // // listaCarreraEstudianteMalla) {
    // for (MallaCurricularDTO malla : mallasCurriculares) {
    // // if (carEstAsig.getAsignaturaDTO().getMallaCurricularDTO()
    // // .getId().equals(malla.getId())) {
    // // mallasCurriculares.clear();
    // // mallasCurriculares.add(malla);
    // // }
    // listaMallas.put(malla.getId(), malla.toString());
    // }
    // // }
    // } catch (ServicioException e) {
    // e.printStackTrace();
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }

    /**
     * 
     * Obtener asignaturas disponibles en base a la malla seleccioanda
     * 
     * @author fochoa
     * @version 11/12/2013 - 9:17:47
     */
    public void obtenerAsignaturasDisponibles(ValueChangeEvent event) {
	if (event != null) {
	    if (event.getNewValue() != null) {
		idMallaSeleccionada = new Long(event.getNewValue().toString());
		try {
		    asignaturasDisponibles = registroServicio
			    .obtenerAsignaturasPorMalla(idMallaSeleccionada);
		} catch (ServicioException e) {
		    e.printStackTrace();
		}
	    }
	}

    }

    public List<Integer> getListaAniosMatricula() {
	if (listaAniosMatricula.isEmpty()) {

	    for (int i = 2012; i <= Calendar.getInstance(TimeZone.getDefault())
		    .get(Calendar.YEAR); i++) {
		listaAniosMatricula.add(i);
	    }
	}
	return listaAniosMatricula;

    }

    public List<Integer> getListaAniosBeca() {
	if (listaAniosBeca.isEmpty()) {

	    for (int i = 2012; i <= Calendar.getInstance(TimeZone.getDefault())
		    .get(Calendar.YEAR); i++) {
		listaAniosBeca.add(i);
	    }
	}
	return listaAniosBeca;

    }

    public void obtenerPeriodosAcademicos() {
	listaPeriodosMatricula.clear();

	try {
	    listaCarreraEstudiantePeriodos = registroServicio
		    .obtenerPeriodosPorCarreraEstudiante(carreraEstudianteDTO
		            .getId());
	    totalCreditos = 0.0;
	    for (CarreraEstudiantePeriodoDTO periodo : listaCarreraEstudiantePeriodos) {
		totalCreditos = totalCreditos
		        + periodo.getNumeroCreditosAprobados();
	    }
	} catch (ServicioException e) {
	    e.printStackTrace();
	}

    }

    public List<TipoAyudaBecaEnum> getTipoAyudaBecaEnum() {
	return Arrays.asList(TipoAyudaBecaEnum.values());
    }

    public List<MotivoBecaEnum> getMotivoBecaEnum() {
	return Arrays.asList(MotivoBecaEnum.values());
    }

    /**
     * Retorna Lista de objetos GeneroEnum utilizada para listar valores en
     * combo.
     * 
     * @author jhomara
     * @version 30/08/2013 - 10:41:39
     * @return
     */
    public List<GeneroEnum> getGenerosEnum() {
	return Arrays.asList(GeneroEnum.values());
    }

    /**
     * 
     * Retorna lista de objetos DiscapacidadEnum utilizada para listar valores
     * en combo
     * 
     * @author jhomara
     * @version 30/08/2013 - 10:48:19
     * @return
     */
    public List<DiscapacidadEnum> getDiscapacidadesEnum() {
	return Arrays.asList(DiscapacidadEnum.values());
    }

    public List<SelectItem> getAreasConocimiento() {
	if (areasConocimiento.isEmpty()) {
	    List<AreaConocimientoDTO> areasConocimientoDTO;
	    try {
		areasConocimientoDTO = catalogoServicio
		        .obtenerAreasConocimiento();
		for (AreaConocimientoDTO areaDTO : areasConocimientoDTO) {
		    SelectItem item = new SelectItem(areaDTO.getId(),
			    areaDTO.getNombre());
		    areasConocimiento.add(item);
		}
	    } catch (ServicioException e) {
		e.printStackTrace();
	    }
	}
	return areasConocimiento;

    }

    public void regresarAdministracion() {
	ExternalContext ec = FacesContext.getCurrentInstance()
	        .getExternalContext();
	try {
	    idPeriodoAcademico = -1L;
	    limpiarListas();
	    listarEstudiantes();
	    ec.redirect("/" + ec.getContextName()
		    + "/estudiantes/administrarEstudiantes.jsf");
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public void validarFecha(FacesContext context, UIComponent component,
	    Object value) throws ValidatorException {
	DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
	if (value != null) {
	    try {
		df.parse(value.toString());
	    } catch (ParseException exception) {
		throw new ValidatorException(new FacesMessage("'"
		        + value.toString() + "' No es una fecha válida."));
	    }
	}
    }

    public void crearNuevoPeriodo() {
	nuevoPeriodo = true;
    }

    public void cambiarPestania(int pestania) {
	pestaniaActiva = pestania;
    }

    public EstudianteDTO getEstudianteSeleccionado() {
	return estudianteSeleccionado;
    }

    public void setEstudianteSeleccionado(EstudianteDTO estudianteSeleccionado) {
	this.estudianteSeleccionado = estudianteSeleccionado;

    }

    public List<EstudianteDTO> getListaEstudiantesIesDTO() {

	return listaEstudiantesIesDTO;

    }

    public void setListaEstudiantesIesDTO(
	    List<EstudianteDTO> listaEstudiantesIesDTO) {
	this.listaEstudiantesIesDTO = listaEstudiantesIesDTO;

    }

    public InformacionIesDTO getInformacionIesDTO() {
	return informacionIesDTO;
    }

    public void setInformacionIesDTO(InformacionIesDTO informacionIesDTO) {
	this.informacionIesDTO = informacionIesDTO;
    }

    public Long getIdInformacionIes() {
	return idInformacionIes;
    }

    public void setIdInformacionIes(Long idInformacionIes) {
	this.idInformacionIes = idInformacionIes;
    }

    public List<EstudianteDTO> getListaEstudiantesIesDTOEncontrados() {
	return listaEstudiantesIesDTOEncontrados;
    }

    public void setListaEstudiantesIesDTOEncontrados(
	    List<EstudianteDTO> listaEstudiantesIesDTOEncontrados) {
	this.listaEstudiantesIesDTOEncontrados = listaEstudiantesIesDTOEncontrados;
    }

    public List<CarreraInformacion> getListaMatriculasEstudiante() {
	return listaMatriculasEstudiante;
    }

    public void setListaMatriculasEstudiante(
	    List<CarreraInformacion> listaMatriculasEstudiante) {
	this.listaMatriculasEstudiante = listaMatriculasEstudiante;
    }

    public List<CarreraEstudianteDTO> getListaCarreraEstudianteFiltradoDTO() {
	return listaCarreraEstudianteFiltradoDTO;
    }

    public void setListaCarreraEstudianteFiltradoDTO(
	    List<CarreraEstudianteDTO> listaCarreraEstudianteFiltradoDTO) {
	this.listaCarreraEstudianteFiltradoDTO = listaCarreraEstudianteFiltradoDTO;
    }

    public CarreraEstudianteDTO getCarreraEstudianteDTO() {
	return carreraEstudianteDTO;
    }

    public void setCarreraEstudianteDTO(
	    CarreraEstudianteDTO carreraEstudianteDTO) {
	this.carreraEstudianteDTO = carreraEstudianteDTO;
    }

    public CarreraIesDTO getCarreraSeleccionada() {
	return carreraSeleccionada;
    }

    public void setCarreraSeleccionada(CarreraIesDTO carreraSeleccionada) {
	this.carreraSeleccionada = carreraSeleccionada;
    }

    public String[] getTipoDocumentaciones() {
	return tipoDocumentaciones;
    }

    public void setTipoDocumentaciones(String[] tipoDocumentaciones) {
	this.tipoDocumentaciones = tipoDocumentaciones;
    }

    public List<PaisDTO> getListaPaisDTO() {
	return listaPaisDTO;
    }

    public void setListaPaisDTO(List<PaisDTO> listaPaisDTO) {
	this.listaPaisDTO = listaPaisDTO;
    }

    public IesDTO getIesDTO() {
	return iesDTO;
    }

    public void setIesDTO(IesDTO iesDTO) throws Exception {
	this.iesDTO = iesDTO;
    }

    public int getRegistros() {
	return registros;
    }

    public void setRegistros(int registros) {
	this.registros = registros;
    }

    public int getIndice() {
	return indice;
    }

    public void setIndice(int indice) {
	this.indice = indice;
    }

    public int getIndiceAtras() {
	return indiceAtras;
    }

    public void setIndiceAtras(int indiceAtras) {
	this.indiceAtras = indiceAtras;
    }

    public int getIndiceSiguiente() {
	return indiceSiguiente;
    }

    public void setIndiceSiguiente(int indiceSiguiente) {
	this.indiceSiguiente = indiceSiguiente;
    }

    public Integer[] getRangos() {
	return rangos;
    }

    public int getNumRegistros() {
	return numRegistros;
    }

    public void setNumRegistros(int numRegistros) {
	this.numRegistros = numRegistros;
    }

    public boolean isErrorValidacion() {
	return errorValidacion;
    }

    public void setErrorValidacion(boolean errorValidacion) {
	this.errorValidacion = errorValidacion;
    }

    public boolean isActivarPestanias() {
	return activarPestanias;
    }

    public void setActivarPestanias(boolean activarPestanias) {
	this.activarPestanias = activarPestanias;
    }

    public void setListaAniosMatricula(List<Integer> listaAniosMatricula) {
	this.listaAniosMatricula = listaAniosMatricula;
    }

    public AuditoriaDTO getAuditoriaDTO() {
	return auditoriaDTO;
    }

    public void setAuditoriaDTO(AuditoriaDTO auditoriaDTO) {
	this.auditoriaDTO = auditoriaDTO;
    }

    public List<EstudianteDTO> getListaEstudiantesTodos() {
	return listaEstudiantesTodos;
    }

    public InformacionCarreraDTO getInformacionCarrera() {
	return informacionCarrera;
    }

    public void setInformacionCarrera(InformacionCarreraDTO informacionCarrera) {
	this.informacionCarrera = informacionCarrera;
    }

    public List<PracticaPreprofesionalDTO> getListaPracticaPreprofesionalDTO() {
	return listaPracticaPreprofesionalDTO;
    }

    public void setListaEstudiantesTodos(
	    List<EstudianteDTO> listaEstudiantesTodos) {
	this.listaEstudiantesTodos = listaEstudiantesTodos;
    }

    public void setListaPracticaPreprofesionalDTO(
	    List<PracticaPreprofesionalDTO> listaPracticaPreprofesionalDTO) {
	this.listaPracticaPreprofesionalDTO = listaPracticaPreprofesionalDTO;
    }

    public PracticaPreprofesionalDTO getPracticaPreprofesionalDTO() {
	return practicaPreprofesionalDTO;
    }

    public void setPracticaPreprofesionalDTO(
	    PracticaPreprofesionalDTO practicaPreprofesionalDTO) {
	this.practicaPreprofesionalDTO = practicaPreprofesionalDTO;
    }

    public void setAreasConocimiento(List<SelectItem> areasConocimiento) {
	this.areasConocimiento = areasConocimiento;
    }

    public List<EstudianteCarrera> getListaCarreraEstudiante() {
	return listaCarreraEstudiante;
    }

    public void setListaCarreraEstudiante(
	    List<EstudianteCarrera> listaCarreraEstudiante) {
	this.listaCarreraEstudiante = listaCarreraEstudiante;
    }

    public String getUsuario() {
	return usuario;
    }

    public void setUsuario(String usuario) {
	this.usuario = usuario;
    }

    public DocenteDTO getDocentePractica() {
	return docentePractica;
    }

    public void setDocentePractica(DocenteDTO docentePractica) {
	this.docentePractica = docentePractica;
    }

    public List<SelectItem> getSubAreasConocimiento() {
	return subAreasConocimiento;
    }

    public void setSubAreasConocimiento(List<SelectItem> subAreasConocimiento) {
	this.subAreasConocimiento = subAreasConocimiento;
    }

    public String getCedulaDocenteTutor() {
	return cedulaDocenteTutor;
    }

    public void setCedulaDocenteTutor(String cedulaDocenteTutor) {
	this.cedulaDocenteTutor = cedulaDocenteTutor;
    }

    public CarreraIesDTO getCarerraSeleccionada() {
	return carerraSeleccionada;
    }

    public void setCarerraSeleccionada(CarreraIesDTO carerraSeleccionada) {
	this.carerraSeleccionada = carerraSeleccionada;
    }

    public String getMontoBeca() {
	return montoBeca;
    }

    public void setMontoBeca(String montoBeca) {
	this.montoBeca = montoBeca;
    }

    public void setListaAniosBeca(List<Integer> listaAniosBeca) {
	this.listaAniosBeca = listaAniosBeca;
    }

    public List<CarreraEstudianteAsignaturaDTO> getListaCarreraEstudianteMalla() {
	return listaCarreraEstudianteMalla;
    }

    public void setListaCarreraEstudianteMalla(
	    List<CarreraEstudianteAsignaturaDTO> listaCarreraEstudianteMalla) {
	this.listaCarreraEstudianteMalla = listaCarreraEstudianteMalla;
    }

    public EstudianteCarrera getEstudianteCarreraSeleccionada() {
	return estudianteCarreraSeleccionada;
    }

    public void setEstudianteCarreraSeleccionada(
	    EstudianteCarrera estudianteCarreraSeleccionada) {
	this.estudianteCarreraSeleccionada = estudianteCarreraSeleccionada;
    }

    public int getPestaniaActiva() {
	return pestaniaActiva;
    }

    public void setPestaniaActiva(int pestaniaActiva) {
	this.pestaniaActiva = pestaniaActiva;
    }

    public CarreraEstudianteAsignaturaDTO getSeleccion() {
	return seleccion;
    }

    public void setSeleccion(CarreraEstudianteAsignaturaDTO seleccion) {
	this.seleccion = new CarreraEstudianteAsignaturaDTO();
	this.seleccion = seleccion;
    }

    public boolean isBusqueda() {
	return busqueda;
    }

    public void setBusqueda(boolean busqueda) {
	this.busqueda = busqueda;
    }

    public List<MallaCurricularDTO> getMallasCurriculares() {
	return mallasCurriculares;
    }

    public MallaCurricularDTO getSelectMalla() {
	return selectMalla;
    }

    public void setMallasCurriculares(
	    List<MallaCurricularDTO> mallasCurriculares) {
	this.mallasCurriculares = mallasCurriculares;
    }

    public List<AsignaturaDTO> getAsignaturasDisponibles() {
	return asignaturasDisponibles;
    }

    public void setSelectMalla(MallaCurricularDTO selectMalla) {
	this.selectMalla = selectMalla;
    }

    public void setAsignaturasDisponibles(
	    List<AsignaturaDTO> asignaturasDisponibles) {
	this.asignaturasDisponibles = asignaturasDisponibles;
    }

    public AsignaturaDTO getSelectAsignaturaDto() {
	return selectAsignaturaDto;
    }

    public void setSelectAsignaturaDto(AsignaturaDTO selectAsignaturaDto) {
	this.selectAsignaturaDto = selectAsignaturaDto;
    }

    public CarreraEstudianteAsignaturaDTO getSeleccionEditar() {
	return seleccionEditar;
    }

    public void setSeleccionEditar(
	    CarreraEstudianteAsignaturaDTO seleccionEditar) {
	this.seleccionEditar = seleccionEditar;
    }

    public Date getMaxDate() {
	Calendar cal = Calendar.getInstance();
	maxDate = new Date(cal.getTimeInMillis());
	return maxDate;
    }

    public void setMaxDate(Date maxDate) {
	this.maxDate = maxDate;
    }

    public Date getMinDate() {
	Calendar cal = Calendar.getInstance();
	int anio = Calendar.getInstance().get(Calendar.YEAR) - 20;
	cal.set(anio, Calendar.JANUARY, 1);
	minDate = new Date(cal.getTimeInMillis());

	return minDate;
    }

    public void setMinDate(Date minDate) {
	this.minDate = minDate;
    }

    public List<PeriodoAcademicoDTO> getListaPeriodosMatricula() {
	try {
	    listaPeriodosMatricula = registroServicio
		    .obtenerPeriodosMatricula(informacionIesDTO.getId());
	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return listaPeriodosMatricula;
    }

    public Map<Long, String> getPeriodosMatricula() {
	Map<Long, String> periodos = new HashMap<Long, String>();
	try {
	    // if (perfil.startsWith("CAR")) {
	    listaPeriodosMatricula = registroServicio
		    .obtenerPeriodosMatricula(informacionIesDTO.getId());
	    for (PeriodoAcademicoDTO periodo : listaPeriodosMatricula) {
		periodos.put(periodo.getId(), periodo.toString());
	    }
	    // }
	} catch (ServicioException e) {
	    e.printStackTrace();
	}

	return periodos;
    }

    public void setListaPeriodosMatricula(
	    List<PeriodoAcademicoDTO> listaPeriodosMatricula) {
	this.listaPeriodosMatricula = listaPeriodosMatricula;
    }

    public Long getIdPeriodoSeleccionado() {
	return idPeriodoSeleccionado;
    }

    public void setIdPeriodoSeleccionado(Long periodoSeleccionado) {
	this.idPeriodoSeleccionado = periodoSeleccionado;
    }

    public Map<Long, String> getListaMallas() {
	return listaMallas;
    }

    public void setListaMallas(Map<Long, String> listaMallas) {
	this.listaMallas = listaMallas;
    }

    public CarreraEstudiantePeriodoDTO getPeriodoSeleccionado() {
	return periodoSeleccionado;
    }

    public void setPeriodoSeleccionado(
	    CarreraEstudiantePeriodoDTO periodoSeleccionado) {
	this.periodoSeleccionado = periodoSeleccionado;
    }

    public boolean isNuevoPeriodo() {
	return nuevoPeriodo;
    }

    public void setNuevoPeriodo(boolean nuevoPeriodo) {
	this.nuevoPeriodo = nuevoPeriodo;
    }

    public Long getIdMallaSeleccionada() {
	return idMallaSeleccionada;
    }

    public void setIdMallaSeleccionada(Long idMallaSeleccionada) {
	this.idMallaSeleccionada = idMallaSeleccionada;
    }

    public boolean isEditarMallaEstudiante() {
	return editarMallaEstudiante;
    }

    public void setEditarMallaEstudiante(boolean editarMallaEstudiante) {
	this.editarMallaEstudiante = editarMallaEstudiante;
    }

    public List<InformacionCarreraDTO> getCarrerasIesDTO() {
	return carrerasIesDTO;
    }

    public void setCarrerasIesDTO(List<InformacionCarreraDTO> carrerasIesDTO) {
	this.carrerasIesDTO = carrerasIesDTO;
    }

    public List<CarreraEstudiantePeriodoDTO> getListaCarreraEstudiantePeriodos() {
	return listaCarreraEstudiantePeriodos;
    }

    public void setListaCarreraEstudiantePeriodos(
	    List<CarreraEstudiantePeriodoDTO> listaCarreraEstudiantePeriodos) {
	this.listaCarreraEstudiantePeriodos = listaCarreraEstudiantePeriodos;
    }

    public boolean isHabilitarSiguiente() {
	return habilitarSiguiente;
    }

    public void setHabilitarSiguiente(boolean habilitarSiguiente) {
	this.habilitarSiguiente = habilitarSiguiente;
    }

    public Double getTotalCreditos() {
	return totalCreditos;
    }

    public void setTotalCreditos(Double totalCreditos) {
	this.totalCreditos = totalCreditos;
    }

    public Date getMaxFechaNacimiento() {
	Calendar cal = Calendar.getInstance();
	int anio = Calendar.getInstance().get(Calendar.YEAR) - 17;
	cal.set(anio, Calendar.JANUARY, 1);
	maxFechaNacimiento = new Date(cal.getTimeInMillis());
	return maxFechaNacimiento;
    }

    public void setMaxFechaNacimiento(Date maxFechaNacimiento) {
	this.maxFechaNacimiento = maxFechaNacimiento;
    }

    public FaseIesDTO getFaseIesDTO() {
	return faseIesDTO;
    }

    public void setFaseIesDTO(FaseIesDTO faseIesDTO) {
	this.faseIesDTO = faseIesDTO;
    }

    public String getIdentificacion() {
	return identificacion;
    }

    public void setIdentificacion(String identificacion) {
	this.identificacion = identificacion;
    }

    public Long getIdCarreraEstudianteSeleccionada() {
	return idCarreraEstudianteSeleccionada;
    }

    public void setIdCarreraEstudianteSeleccionada(
	    Long idCarreraEstudianteSeleccionada) {
	this.idCarreraEstudianteSeleccionada = idCarreraEstudianteSeleccionada;
    }

    public Integer getNumeroEstudiantes() {
	return numeroEstudiantes;
    }

    public void setNumeroEstudiantes(Integer numeroEstudiantes) {
	this.numeroEstudiantes = numeroEstudiantes;
    }

    /**
     * @return the secciones
     */
    public List<TipoSeccionEnum> getSecciones() {
	return secciones;
    }

    /**
     * @param secciones
     *            the secciones to set
     */
    public void setSecciones(List<TipoSeccionEnum> secciones) {
	this.secciones = secciones;
    }

    /**
     * @return the seccionSeleccionada
     */
    public String getSeccionSeleccionada() {
	return seccionSeleccionada;
    }

    /**
     * @param seccionSeleccionada
     *            the seccionSeleccionada to set
     */
    public void setSeccionSeleccionada(String seccionSeleccionada) {
	this.seccionSeleccionada = seccionSeleccionada;
    }

    public List<String> getEtnias() {
	return etnias;
    }

    public void setEtnias(List<String> etnias) {
	this.etnias = etnias;
    }

    public boolean isIngresoPorConvalidacion() {
	return ingresoPorConvalidacion;
    }

    public void setIngresoPorConvalidacion(boolean ingresoPorConvalidacion) {
	carreraEstudianteDTO.setFechaConvalidacion(null);
	carreraEstudianteDTO.setFechaInicioPrimerNivel(null);
	this.ingresoPorConvalidacion = ingresoPorConvalidacion;
    }

    public boolean isEsGraduado() {
	return esGraduado;
    }

    public void setEsGraduado(boolean esGraduado) {
	this.esGraduado = esGraduado;
    }

    public Date getFechaActual() {
	return fechaActual;
    }

    public void setFechaActual(Date fechaActual) {
	this.fechaActual = fechaActual;
    }

    public Long getIdPeriodoAcademico() {
	return idPeriodoAcademico;
    }

    public void setIdPeriodoAcademico(Long idPeriodoAcademico) {
	this.idPeriodoAcademico = idPeriodoAcademico;
    }

    public List<PeriodoAcademicoDTO> getPeriodosAcademicos() {
	return periodosAcademicos;
    }

    public Boolean getAlertaUsuarioIes() {
	return alertaUsuarioIes;
    }

    public void setAlertaUsuarioIes(Boolean alertaUsuarioIes) {
	this.alertaUsuarioIes = alertaUsuarioIes;
    }

    public Boolean getAlertaFaseRectificacion() {
	return alertaFaseRectificacion;
    }

    public void setAlertaFaseRectificacion(Boolean alertaFaseRectificacion) {
	this.alertaFaseRectificacion = alertaFaseRectificacion;
    }

    public String getPerfil() {
	return perfil;
    }

    public void setPerfil(String perfil) {
	this.perfil = perfil;
    }

}