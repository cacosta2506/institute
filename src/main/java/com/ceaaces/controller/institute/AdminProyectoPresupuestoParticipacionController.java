package ec.gob.ceaaces.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import org.primefaces.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import ec.gob.ceaaces.catalogo.dtos.catalogos.AuditoriaDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.IesDTO;
import ec.gob.ceaaces.catalogo.exceptions.ServicioException;
import ec.gob.ceaaces.data.CarreraInformacion;
import ec.gob.ceaaces.institutos.dtos.evaluacion.DocenteDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EstudianteDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionCarreraDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ParticipacionProyectoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.PersonaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.PresupuestoProyectoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ProyectoDTO;
import ec.gob.ceaaces.institutos.enumeraciones.CategoriaEvaluacionEnum;
import ec.gob.ceaaces.institutos.enumeraciones.TipoProyectoEnum;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.JsfUtil;

public class AdminProyectoPresupuestoParticipacionController implements
        Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger
	    .getLogger(AdminProyectoPresupuestoParticipacionController.class
	            .getSimpleName());

    @Autowired
    private RegistroServicio registroServicio;

    @Autowired
    private CatalogoServicio catalogoServicio;

    private int indiceTab;
    private boolean verTabDetalle;
    private boolean mensaje;
    private Long idInformacionIes;
    private Long idInformacionCarrera;
    private String personaSeleccionada;
    private String usuario;
    private String opcion;
    private Double horasPersona;
    private String[] tipoProyecto;
    private String tipoSeleccionado;
    private String perfil;
    private int indice;
    private int registros = 10;

    private IesDTO iesDTO;
    private ProyectoDTO proyectoDto;
    private PresupuestoProyectoDTO presupuestoProyecDto;
    private ParticipacionProyectoDTO participacionProyecDto;
    private InformacionIesDTO informacionIesDto;
    private PersonaDTO personaDto;
    private AuditoriaDTO auditoriaDTO;

    private List<PresupuestoProyectoDTO> listaPresupuesto;
    private List<ParticipacionProyectoDTO> listaParticipacion;
    private List<PersonaDTO> listaPersonaDto;
    private List<String> listaParticipanteCombo;
    private List<CarreraInformacion> listaCarrerasDTO;
    private List<ParticipacionProyectoDTO> carrerasParticipantes;
    private List<DocenteDTO> docentes;

    private InformacionCarreraDTO informacionCarreraDTO;
    private FaseIesDTO faseIesDTO;

    private Date fechaActual;

    private List<ParticipacionProyectoDTO> vistaParticipaciones;
    private Boolean alertaEvaluador = false;

    public AdminProyectoPresupuestoParticipacionController() {
	iesDTO = new IesDTO();
	informacionIesDto = new InformacionIesDTO();
	proyectoDto = new ProyectoDTO();
	personaDto = new PersonaDTO();
	presupuestoProyecDto = new PresupuestoProyectoDTO();
	participacionProyecDto = new ParticipacionProyectoDTO();
	listaPresupuesto = new ArrayList<PresupuestoProyectoDTO>();
	listaParticipacion = new ArrayList<ParticipacionProyectoDTO>();
	listaParticipanteCombo = new ArrayList<String>();
	listaPersonaDto = new ArrayList<PersonaDTO>();
	listaCarrerasDTO = new ArrayList<CarreraInformacion>();
	carrerasParticipantes = new ArrayList<ParticipacionProyectoDTO>();
	personaSeleccionada = "estudiantes";
	vistaParticipaciones = new ArrayList<>();
	docentes = new ArrayList<>();
    }

    public List<ParticipacionProyectoDTO> getVistaParticipaciones() {
	return vistaParticipaciones;
    }

    public void setVistaParticipaciones(
	    List<ParticipacionProyectoDTO> vistaParticipaciones) {
	this.vistaParticipaciones = vistaParticipaciones;
    }

    @PostConstruct
    public void start() {

	try {

	    ListaIesController controller = (ListaIesController) JsfUtil
		    .obtenerObjetoSesion("listaIesController");

	    setMensaje(false);
	    this.alertaEvaluador = false;

	    this.usuario = SecurityContextHolder.getContext()
		    .getAuthentication().getName();

	    this.iesDTO = controller.getIes();
	    this.perfil = controller.getPerfil().getNombre();
	    this.faseIesDTO = controller.getFaseIesDTO();

	    this.informacionIesDto = registroServicio
		    .obtenerInformacionIesPorIes(iesDTO);
	    this.idInformacionIes = informacionIesDto.getId();

	    if (this.perfil.startsWith("IES_EVALUADOR")) {
		alertaEvaluador = true;
	    }

	    obtenerParticipante();
	    cargarDatos();
	    cargarCarreras();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void cargarDatos() {
	tipoProyecto = new String[2];
	tipoProyecto[0] = TipoProyectoEnum.VINCULACION.getValue();
	tipoProyecto[1] = TipoProyectoEnum.INVESTIGACION_E_INNOVACION
	        .getValue();
    }

    public void cargarCarreras() {
	listaCarrerasDTO.clear();
	List<InformacionCarreraDTO> carreras = new ArrayList<InformacionCarreraDTO>();
	try {
	    LOG.info("informacionIesDto.getId(): " + informacionIesDto.getId());
	    carreras = registroServicio
		    .obtenerInformacionCarreraPorInformacionIes(informacionIesDto
		            .getId());
	    for (InformacionCarreraDTO carrera : carreras) {
		CarreraInformacion carreraInfo = new CarreraInformacion();
		carreraInfo.setInformacionCarreraDTO(carrera);
		if (carrera.getId().equals(idInformacionCarrera)) {
		    carreraInfo.setEnable(false);
		    carreraInfo.setTake(true);
		    ParticipacionProyectoDTO carreraParticipanteDTO = new ParticipacionProyectoDTO();
		    carreraParticipanteDTO.setActivo(true);
		    carreraParticipanteDTO.setTabla("informacion_carrera");
		    auditoriaDTO = new AuditoriaDTO();
		    auditoriaDTO.setUsuarioModificacion(usuario);
		    auditoriaDTO.setFechaModificacion(new Date());
		    carreraParticipanteDTO.setAuditoriaDTO(auditoriaDTO);
		    carreraParticipanteDTO.setInformacionCarreraDTO(carreraInfo
			    .getInformacionCarreraDTO());
		    carrerasParticipantes.add(carreraParticipanteDTO);
		}
		listaCarrerasDTO.add(carreraInfo);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("No se pudo cargar las carreras");
	    return;
	}
    }

    public void nuevoPresupuesto() {
	presupuestoProyecDto = new PresupuestoProyectoDTO();
	RequestContext.getCurrentInstance().reset("formPresupuesto");
    }

    public void nuevaParticipacion() {
	participacionProyecDto = new ParticipacionProyectoDTO();
	listaPersonaDto.clear();
	personaDto = new PersonaDTO();
	horasPersona = null;
	vistaParticipaciones.clear();
	RequestContext.getCurrentInstance().reset("formParticipacion");
    }

    public void agregarCarreras(CarreraInformacion carreraI) {
	ParticipacionProyectoDTO carreraParticipanteDTO = new ParticipacionProyectoDTO();
	carreraParticipanteDTO.setActivo(carreraI.isTake());
	carreraParticipanteDTO.setTabla("informacion_carrera");

	auditoriaDTO = new AuditoriaDTO();
	auditoriaDTO.setUsuarioModificacion(usuario);
	auditoriaDTO.setFechaModificacion(new Date());

	carreraParticipanteDTO.setAuditoriaDTO(auditoriaDTO);
	carreraParticipanteDTO.setInformacionCarreraDTO(carreraI
	        .getInformacionCarreraDTO());

	if (carreraI.isTake()) {
	    carrerasParticipantes.add(carreraParticipanteDTO);
	} else {
	    for (int i = 0; i < carrerasParticipantes.size(); i++) {
		if (carrerasParticipantes.get(i).getInformacionCarreraDTO()
		        .getId()
		        .equals(carreraI.getInformacionCarreraDTO().getId())) {
		    carrerasParticipantes.remove(i);
		    break;
		}
	    }
	}
    }

    public void guardarProyecto() {
	if (!this.validarProyectos(proyectoDto)) {
	    return;
	}

	if (listaParticipacion.isEmpty()) {
	    JsfUtil.msgAdvert("Ingrese la Participación del Proyecto");
	    return;
	}

	if (carrerasParticipantes.isEmpty()) {
	    JsfUtil.msgAdvert("Ingrese las Carreras Participantes");
	    return;
	}

	try {
	    AuditoriaDTO auditorDto = new AuditoriaDTO();
	    auditorDto.setUsuarioModificacion(usuario);
	    auditorDto.setFechaModificacion(new Date(Calendar.getInstance(
		    TimeZone.getDefault()).getTimeInMillis()));
	    proyectoDto.setAuditoriaDTO(auditorDto);
	    proyectoDto.setActivo(true);
	    proyectoDto.setFaseIesDTO(faseIesDTO);

	    InformacionIesDTO informacionIes = new InformacionIesDTO();
	    informacionIes.setId(idInformacionIes);
	    proyectoDto.setInformacionIesDTO(informacionIes);

	    proyectoDto.setListaPresupuestoProyectosDTO(listaPresupuesto);
	    if (perfil.startsWith("CAR")) {
		CarreraInformacion car = new CarreraInformacion();
		car.setInformacionCarreraDTO(informacionCarreraDTO);
		car.setTake(true);
		agregarCarreras(car);
	    }
	    for (ParticipacionProyectoDTO pp : carrerasParticipantes) {
		listaParticipacion.add(pp);
	    }

	    proyectoDto.setListaParticipacionProyectosDTO(listaParticipacion);
	    proyectoDto.setCategoriaEvaluacion(CategoriaEvaluacionEnum.PROYECTO
		    .name());
	    proyectoDto = registroServicio.registrarProyecto(proyectoDto);

	    if (null != proyectoDto.getId()) {
		JsfUtil.msgInfo("Registro almacenado correctamente");
		limpiarDatos();
	    } else {
		JsfUtil.msgError("Error al Guardar Proyectos");
	    }
	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al Guardar Proyectos, consulte con el Administrador del Sistema");
	    return;
	} catch (Exception e) {
	    JsfUtil.msgError("Error al guardar, consulte al administrador."
		    + e.getMessage());
	    e.printStackTrace();
	}
    }

    public void obtenerParticipante() {
	listaParticipanteCombo.add("estudiantes");
	listaParticipanteCombo.add("docentes");
    }

    public List<PersonaDTO> obtenerListaEstudianteDocente() {
	List<PersonaDTO> lisPersonaED = new ArrayList<PersonaDTO>();
	if (personaSeleccionada.equals("estudiantes")) {
	    List<EstudianteDTO> listaEstudiante = new ArrayList<EstudianteDTO>();

	    try {
		listaEstudiante = registroServicio
		        .obtenerEstudiantesPorInstituto(
		                informacionIesDto.getId(), null, 1, 9000);
		for (EstudianteDTO estudianteDTO : listaEstudiante) {
		    PersonaDTO persona = new PersonaDTO();
		    persona.setId(estudianteDTO.getId());
		    persona.setIdentificacion(estudianteDTO.getIdentificacion());
		    persona.setApellidoMaterno(estudianteDTO
			    .getApellidoMaterno());
		    persona.setApellidoPaterno(estudianteDTO
			    .getApellidoPaterno());
		    persona.setNombres(estudianteDTO.getNombres());
		    lisPersonaED.add(persona);
		}
	    } catch (ServicioException e) {
		e.printStackTrace();
	    }
	} else if (personaSeleccionada.equals("docentes")) {
	    DocenteDTO docenteDTO = null;
	    List<DocenteDTO> listaDocente = new ArrayList<DocenteDTO>();

	    try {
		listaDocente = registroServicio
		        .obtenerDocentesPorInformacionIes(docenteDTO,
		                idInformacionIes);
		for (DocenteDTO docentesDTO : listaDocente) {
		    PersonaDTO person = new PersonaDTO();
		    person.setId(docentesDTO.getId());
		    person.setIdentificacion(docentesDTO.getIdentificacion());
		    person.setApellidoPaterno(docentesDTO.getApellidoPaterno());
		    person.setApellidoMaterno(docentesDTO.getApellidoMaterno());
		    person.setNombres(docentesDTO.getNombres());
		    lisPersonaED.add(person);
		}
	    } catch (ServicioException e) {
		e.printStackTrace();
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	return lisPersonaED;
    }

    public void seleccionarPersona() {
	// LOG.info("ingreso al metodo combo");
	listaPersonaDto.clear();
	horasPersona = null;
	personaDto = new PersonaDTO();
	DocenteDTO docenteDTO = null;

	if (personaSeleccionada.equals("docentes")) {
	    try {
		docentes = registroServicio.obtenerDocentesPorInformacionIes(
		        docenteDTO, informacionIesDto.getId());
	    } catch (ServicioException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	// listaPersonaDto = obtenerListaEstudianteDocente();
    }

    public void limpiarDatos() {
	personaSeleccionada = "";
	proyectoDto = new ProyectoDTO();
	personaDto = new PersonaDTO();
	presupuestoProyecDto = new PresupuestoProyectoDTO();
	participacionProyecDto = new ParticipacionProyectoDTO();
	horasPersona = null;
	listaPersonaDto.clear();
	listaPresupuesto.clear();
	listaParticipacion.clear();
	carrerasParticipantes.clear();
    }

    public void regresarProyectos() {
	LOG.info("RegresarProyectos");

	limpiarDatos();

	ExternalContext ec = FacesContext.getCurrentInstance()
	        .getExternalContext();
	try {
	    ec.redirect("/" + ec.getContextName()
		    + "/proyectos/administracionProyectos.jsf");
	    AdministrarProyectoController controller = (AdministrarProyectoController) ec
		    .getSessionMap().get("proyectoController");
	    controller.cargarProyectos();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public boolean validarProyectos(ProyectoDTO proyect) {

	if (null == proyect.getNombre() || proyect.getNombre().equals("")) {
	    JsfUtil.msgAdvert("Ingrese el nombre del proyecto");
	    return false;
	}

	// if (!"VINCULACION".equals(proyect.getTipo().getValue())) {
	// if (null == proyect.getLineaInvestigacion()
	// || proyect.getLineaInvestigacion().equals("")) {
	// JsfUtil.msgAdvert("Ingrese la línea de investigación del proyecto");
	// return false;
	// }
	// }

	if (null == proyect.getTipo() || proyect.getTipo().equals("")) {
	    JsfUtil.msgAdvert("Ingrese el tipo");
	    return false;
	}

	if (null == proyect.getFechaInicio()) {
	    JsfUtil.msgAdvert("Ingrese la Fecha de Inicio");
	    return false;
	}

	if (null == proyect.getFechaFinPlaneado()) {
	    JsfUtil.msgAdvert("Ingrese Fecha Fin Planeado");
	    return false;
	}

	if (proyect.getFechaInicio().after(proyect.getFechaFinPlaneado())) {
	    JsfUtil.msgAdvert("La Fecha Fin Planeado debe ser mayor que la Fecha de Inicio");
	    return false;
	}

	// if (null == proyect.getFechaFinReal()) {
	// JsfUtil.msgAdvert("Ingrese Fecha Fin Real");
	// return false;
	// }

	if (proyect.getFechaFinReal() != null) {
	    if (proyect.getFechaInicio().after(proyect.getFechaFinReal())) {
		JsfUtil.msgAdvert("La Fecha Fin Real debe ser mayor que la Fecha de Inicio");
		return false;
	    }
	}

	return true;
    }

    public void agregarPresupuestoLista() {

	if (proyectoDto.getFechaInicio().after(new Date())) {
	    JsfUtil.msgError("La fecha de inicio del proyecto es superior a la actual");
	    return;
	}

	// if (presupuestoProyecDto.getFechaInicio().before(
	// proyectoDto.getFechaInicioPlaneado())
	// || presupuestoProyecDto.getFechaFin().after(
	// proyectoDto.getFechaFinReal())) {
	// msgError("El periodo de ejecución del presupuesto no se encuentra en el periodo del proyecto.");
	// return;
	// }
	//
	// if (presupuestoProyecDto.getFechaInicio().after(
	// presupuestoProyecDto.getFechaFin())) {
	// msgAdvert("La fecha de Fin  debe ser mayor que la Fecha de Inicio");
	// return;
	// }

	if (presupuestoProyecDto.getPresupuestoPlanificado() < 0) {
	    JsfUtil.msgAdvert("Presupuesto Planificado no puede ser negativo");
	    return;
	}

	if (presupuestoProyecDto.getPresupuestoPlanificado() > 999999) {
	    JsfUtil.msgAdvert("Debe ser menor a 999999");
	    return;
	}

	if (presupuestoProyecDto.getPresupuestoEjecutado() < 0) {
	    JsfUtil.msgAdvert("Presupuesto Ejecutado no puede ser negativo");
	    return;
	}

	if (presupuestoProyecDto.getPresupuestoEjecutado() > 999999) {
	    JsfUtil.msgAdvert("Debe ser menor a 999999");
	    return;
	}
	presupuestoProyecDto.setFaseIesDTO(faseIesDTO);
	presupuestoProyecDto.setActivo(true);
	listaPresupuesto.add(presupuestoProyecDto);
	RequestContext context = RequestContext.getCurrentInstance();
	context.execute("dlgPresupuesto.hide()");
    }

    public void eliminarParticipacion() {
	if (personaDto.getId() != null) {
	    for (ParticipacionProyectoDTO participacion : listaParticipacion) {
		if (participacion.getPersonaDTO().getId()
		        .equals(personaDto.getId())) {
		    listaParticipacion.remove(participacion);
		    JsfUtil.msgInfo("Participante eliminado correctamente.");
		    return;
		}
	    }
	}
    }

    // Una tabla temporal de participaciones en un popup
    public void agregarParticipacion() {
	if (null == personaDto.getId()) {
	    JsfUtil.msgAdvert("Seleccione un Participante");
	    return;
	}

	if (null == horasPersona || horasPersona == 0) {
	    JsfUtil.msgAdvert("Ingrese la hora");
	    return;
	}

	if (horasPersona <= 14) {
	    JsfUtil.msgAdvert("El múmero de horas debe ser mayor a 15");
	    return;
	}

	for (ParticipacionProyectoDTO part : listaParticipacion) {
	    if (part.getPersonaDTO().getId().equals(personaDto.getId())) {
		JsfUtil.msgAdvert("La persona ya se encuentra asignada");
		return;
	    }
	}

	personaDto.setActivo(true);
	participacionProyecDto = new ParticipacionProyectoDTO();
	participacionProyecDto.setActivo(true);
	participacionProyecDto.setPersonaDTO(personaDto);
	participacionProyecDto.setTabla(personaSeleccionada);
	participacionProyecDto.setHorasParticipacion(horasPersona);
	participacionProyecDto.setFaseIesDTO(faseIesDTO);

	for (int i = 0; i < vistaParticipaciones.size(); i++) {
	    if (vistaParticipaciones.get(i).getPersonaDTO().getId()
		    .equals(personaDto.getId())) {
		vistaParticipaciones.remove(i);
		break;
	    }
	}

	vistaParticipaciones.add(participacionProyecDto);

	personaDto = new PersonaDTO();
	horasPersona = null;
    }

    // De la tabla temporal del popup a una tabla de pagina
    public void agregarParticipacionLista() {
	// listaParticipacion.clear();
	for (ParticipacionProyectoDTO pp : vistaParticipaciones) {
	    listaParticipacion.add(pp);
	}

	vistaParticipaciones.clear();

	RequestContext context = RequestContext.getCurrentInstance();
	context.execute("dlgParticipacion.hide()");
    }

    public IesDTO getIesDTO() {
	return iesDTO;
    }

    public void setIesDTO(IesDTO iesDTO) {
	this.iesDTO = iesDTO;
    }

    public InformacionIesDTO getInformacionIesDto() {
	return informacionIesDto;
    }

    public void setInformacionIesDto(InformacionIesDTO informacionIesDto) {
	this.informacionIesDto = informacionIesDto;
    }

    public String getUsuario() {
	return usuario;
    }

    public void setUsuario(String usuario) {
	this.usuario = usuario;
    }

    public String getOpcion() {
	return opcion;
    }

    public void setOpcion(String opcion) {
	this.opcion = opcion;
    }

    public Long getIdInformacionIes() {
	return idInformacionIes;
    }

    public void setIdInformacionIes(Long idInformacionIes) {
	this.idInformacionIes = idInformacionIes;
    }

    public int getIndiceTab() {
	return indiceTab;
    }

    public void setIndiceTab(int indiceTab) {
	this.indiceTab = indiceTab;
    }

    public boolean isVerTabDetalle() {
	return verTabDetalle;
    }

    public void setVerTabDetalle(boolean verTabDetalle) {
	this.verTabDetalle = verTabDetalle;
    }

    public ProyectoDTO getProyectoDto() {
	return proyectoDto;
    }

    public void setProyectoDto(ProyectoDTO proyectoDto) {
	this.proyectoDto = proyectoDto;
    }

    public List<PresupuestoProyectoDTO> getListaPresupuesto() {
	return listaPresupuesto;
    }

    public void setListaPresupuesto(
	    List<PresupuestoProyectoDTO> listaPresupuesto) {
	this.listaPresupuesto = listaPresupuesto;
    }

    public List<ParticipacionProyectoDTO> getListaParticipacion() {
	return listaParticipacion;
    }

    public void setListaParticipacion(
	    List<ParticipacionProyectoDTO> listaParticipacion) {
	this.listaParticipacion = listaParticipacion;
    }

    public PresupuestoProyectoDTO getPresupuestoProyecDto() {
	return presupuestoProyecDto;
    }

    public void setPresupuestoProyecDto(
	    PresupuestoProyectoDTO presupuestoProyecDto) {
	this.presupuestoProyecDto = presupuestoProyecDto;
    }

    public Long getIdInformacionCarrera() {
	return idInformacionCarrera;
    }

    public void setIdInformacionCarrera(Long idInformacionCarrera) {
	this.idInformacionCarrera = idInformacionCarrera;
    }

    public boolean isMensaje() {
	return mensaje;
    }

    public void setMensaje(boolean mensaje) {
	this.mensaje = mensaje;
    }

    public ParticipacionProyectoDTO getParticipacionProyecDto() {
	return participacionProyecDto;
    }

    public void setParticipacionProyecDto(
	    ParticipacionProyectoDTO participacionProyecDto) {
	this.participacionProyecDto = participacionProyecDto;
    }

    public List<String> getListaParticipanteCombo() {
	return listaParticipanteCombo;
    }

    public void setListaParticipanteCombo(List<String> listaParticipanteCombo) {
	this.listaParticipanteCombo = listaParticipanteCombo;
    }

    public List<PersonaDTO> getListaPersonaDto() {
	return listaPersonaDto;
    }

    public void setListaPersonaDto(List<PersonaDTO> listaPersonaDto) {
	this.listaPersonaDto = listaPersonaDto;
    }

    public String getPersonaSeleccionada() {
	return personaSeleccionada;
    }

    public void setPersonaSeleccionada(String personaSeleccionada) {
	this.personaSeleccionada = personaSeleccionada;
    }

    public PersonaDTO getPersonaDto() {
	return personaDto;
    }

    public void setPersonaDto(PersonaDTO personaDto) {
	this.personaDto = personaDto;
    }

    public String[] getTipoProyecto() {
	return tipoProyecto;
    }

    public void setTipoProyecto(String[] tipoProyecto) {
	this.tipoProyecto = tipoProyecto;
    }

    public String getTipoSeleccionado() {
	return tipoSeleccionado;
    }

    public void setTipoSeleccionado(String tipoSeleccionado) {
	this.tipoSeleccionado = tipoSeleccionado;
    }

    public AuditoriaDTO getAuditoriaDTO() {
	return auditoriaDTO;
    }

    public void setAuditoriaDTO(AuditoriaDTO auditoriaDTO) {
	this.auditoriaDTO = auditoriaDTO;
    }

    public List<CarreraInformacion> getListaCarrerasDTO() {
	return listaCarrerasDTO;
    }

    public void setListaCarrerasDTO(List<CarreraInformacion> listaCarrerasDTO) {
	this.listaCarrerasDTO = listaCarrerasDTO;
    }

    public List<ParticipacionProyectoDTO> getCarrerasParticipantes() {
	return carrerasParticipantes;
    }

    public void setCarrerasParticipantes(
	    List<ParticipacionProyectoDTO> carrerasParticipantes) {
	this.carrerasParticipantes = carrerasParticipantes;
    }

    public String getPerfil() {
	return perfil;
    }

    public void setPerfil(String perfil) {
	this.perfil = perfil;
    }

    public int getIndice() {
	return indice;
    }

    public void setIndice(int indice) {
	this.indice = indice;
    }

    public int getRegistros() {
	return registros;
    }

    public void setRegistros(int registros) {
	this.registros = registros;
    }

    public Integer[] getRangoAnios() {
	Calendar calendar = Calendar.getInstance();
	int anio = calendar.get(Calendar.YEAR) - 10;
	Integer[] rangoAnios = new Integer[21];
	for (int i = 0; i < 21; i++) {
	    rangoAnios[i] = i + anio;
	}

	return rangoAnios;
    }

    public void tomarTipo(ValueChangeEvent event) {
	proyectoDto
	        .setTipo(TipoProyectoEnum.parse((String) event.getNewValue()));
    }

    public Date getFechaActual() {
	Calendar cal = Calendar.getInstance();
	fechaActual = new Date(cal.getTimeInMillis());
	return fechaActual;

    }

    public void setFechaActual(Date fechaActual) {
	this.fechaActual = fechaActual;
    }

    public InformacionCarreraDTO getInformacionCarreraDTO() {
	return informacionCarreraDTO;
    }

    public void setInformacionCarreraDTO(
	    InformacionCarreraDTO informacionCarreraDTO) {
	this.informacionCarreraDTO = informacionCarreraDTO;
    }

    public FaseIesDTO getFaseIesDTO() {
	return faseIesDTO;
    }

    public void setFaseIesDTO(FaseIesDTO faseIesDTO) {
	this.faseIesDTO = faseIesDTO;
    }

    public void setHorasPersona(Double horasPersona) {
	this.horasPersona = horasPersona;
    }

    public Double getHorasPersona() {
	return horasPersona;
    }

    public List<DocenteDTO> getDocentes() {
	return docentes;
    }

    public void setDocentes(List<DocenteDTO> docentes) {
	this.docentes = docentes;
    }

    public Boolean getAlertaEvaluador() {
	return alertaEvaluador;
    }

    public void setAlertaEvaluador(Boolean alertaEvaluador) {
	this.alertaEvaluador = alertaEvaluador;
    }

}
