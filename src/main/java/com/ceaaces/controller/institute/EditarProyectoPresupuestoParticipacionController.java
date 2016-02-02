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

public class EditarProyectoPresupuestoParticipacionController implements
        Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger
	    .getLogger(EditarProyectoPresupuestoParticipacionController.class
	            .getSimpleName());

    private int indiceTab;
    private boolean verTabDetalle;
    private Long idInformacionIes;
    private Long idInformacionCarrera;
    private String personaSeleccionada;
    private String usuario;
    private String opcion;
    private String band;
    private IesDTO iesDTO;
    private ProyectoDTO proyectoDto;
    private PresupuestoProyectoDTO presupuestoProyecDto;
    private ParticipacionProyectoDTO participacionProyecDto;
    private InformacionIesDTO informacionIesDto;
    private PersonaDTO personaDto;
    private List<ParticipacionProyectoDTO> listaParticipacionPersonas;
    private List<ParticipacionProyectoDTO> listaParticipacionCarreras;
    private List<PersonaDTO> listaPersonaDto;
    private List<String> listaParticipanteCombo;
    private List<DocenteDTO> docentes;
    private Double horasPersona;
    private String[] tipoProyecto;
    private String tipoSeleccionado;
    private int indice;
    private int registros = 10;
    private String perfil;
    private FaseIesDTO faseIesDTO;
    private Boolean alertaEvaluador = false;

    private List<CarreraInformacion> listaCarrerasDTO;
    private List<ParticipacionProyectoDTO> vistaParticipaciones;

    @Autowired
    private RegistroServicio registroServicio;

    @Autowired
    private CatalogoServicio catalogoServicio;

    public EditarProyectoPresupuestoParticipacionController() {
	iesDTO = new IesDTO();
	informacionIesDto = new InformacionIesDTO();
	proyectoDto = new ProyectoDTO();
	personaDto = new PersonaDTO();
	presupuestoProyecDto = new PresupuestoProyectoDTO();
	participacionProyecDto = new ParticipacionProyectoDTO();
	listaParticipanteCombo = new ArrayList<String>();
	listaPersonaDto = new ArrayList<PersonaDTO>();
	listaCarrerasDTO = new ArrayList<CarreraInformacion>();
	listaParticipacionPersonas = new ArrayList<ParticipacionProyectoDTO>();
	listaParticipacionCarreras = new ArrayList<ParticipacionProyectoDTO>();
	personaSeleccionada = "estudiantes";
	vistaParticipaciones = new ArrayList<>();
	docentes = new ArrayList<>();
    }

    /**
     * @return the vistaParticipaciones
     */
    public List<ParticipacionProyectoDTO> getVistaParticipaciones() {
	return vistaParticipaciones;
    }

    /**
     * @param vistaParticipaciones
     *            the vistaParticipaciones to set
     */
    public void setVistaParticipaciones(
	    List<ParticipacionProyectoDTO> vistaParticipaciones) {
	this.vistaParticipaciones = vistaParticipaciones;
    }

    @PostConstruct
    public void start() {

	try {

	    this.usuario = SecurityContextHolder.getContext()
		    .getAuthentication().getName();
	    cargarDatos();
	    obtenerParticipante();

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void cargarDatos() {
	try {
	    ListaIesController controller = (ListaIesController) JsfUtil
		    .obtenerObjetoSesion("listaIesController");

	    this.alertaEvaluador = false;

	    this.iesDTO = controller.getIes();

	    this.informacionIesDto = registroServicio
		    .obtenerInformacionIesPorIes(iesDTO);
	    this.idInformacionIes = informacionIesDto.getId();
	    this.faseIesDTO = controller.getFaseIesDTO();
	    this.perfil = controller.getPerfil().getNombre();

	    if (this.perfil.startsWith("IES_EVALUADOR")) {
		alertaEvaluador = true;
	    }

	    tipoProyecto = new String[2];
	    tipoProyecto[0] = TipoProyectoEnum.VINCULACION.getValue();
	    tipoProyecto[1] = TipoProyectoEnum.INVESTIGACION_E_INNOVACION
		    .getValue();

	} catch (Exception e) {
	    e.printStackTrace();
	}

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
		for (ParticipacionProyectoDTO part : listaParticipacionCarreras) {
		    if (part.getInformacionCarreraDTO().getId()
			    .equals(carrera.getId())) {
			carreraInfo.setTake(true);
		    }
		}
		if (carrera.getId().equals(idInformacionCarrera)) {
		    carreraInfo.setEnable(false);
		    carreraInfo.setTake(true);
		    ParticipacionProyectoDTO carreraParticipanteDTO = new ParticipacionProyectoDTO();
		    carreraParticipanteDTO.setActivo(true);
		    carreraParticipanteDTO.setTabla("informacion_carrera");
		    AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
		    auditoriaDTO.setUsuarioModificacion(usuario);
		    auditoriaDTO.setFechaModificacion(new Date());
		    carreraParticipanteDTO.setAuditoriaDTO(auditoriaDTO);
		    carreraParticipanteDTO.setInformacionCarreraDTO(carreraInfo
			    .getInformacionCarreraDTO());
		    listaParticipacionCarreras.add(carreraParticipanteDTO);
		}
		listaCarrerasDTO.add(carreraInfo);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("No se pudo cargar las carreras");
	    return;
	}
    }

    public void agregarCarreras(CarreraInformacion carreraI) {
	ParticipacionProyectoDTO carreraParticipanteDTO = new ParticipacionProyectoDTO();
	carreraParticipanteDTO.setActivo(carreraI.isTake());
	carreraParticipanteDTO.setTabla("informacion_carrera");

	AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
	auditoriaDTO.setUsuarioModificacion(usuario);
	auditoriaDTO.setFechaModificacion(new Date());

	carreraParticipanteDTO.setAuditoriaDTO(auditoriaDTO);
	carreraParticipanteDTO.setInformacionCarreraDTO(carreraI
	        .getInformacionCarreraDTO());
	try {
	    if (carreraI.isTake()) {
		carreraParticipanteDTO.setActivo(true);
		carreraParticipanteDTO.setTabla("informacion_carrera");
		carreraParticipanteDTO.setProyectoDTO(this.proyectoDto);
		carreraParticipanteDTO.setFaseIesDTO(faseIesDTO);
		listaParticipacionCarreras.add(carreraParticipanteDTO);
		participacionProyecDto.setFaseIesDTO(faseIesDTO);
		participacionProyecDto = registroServicio
		        .registrarParticipacionProyecto(carreraParticipanteDTO);

	    } else {
		for (int i = 0; i < listaParticipacionCarreras.size(); i++) {
		    if (listaParticipacionCarreras
			    .get(i)
			    .getInformacionCarreraDTO()
			    .getId()
			    .equals(carreraI.getInformacionCarreraDTO().getId())) {

			participacionProyecDto = listaParticipacionCarreras
			        .get(i);
			listaParticipacionCarreras.remove(i);
			eliminarParticipacion();
			break;
		    }
		}
	    }
	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void actualizarTodo() {
	listaParticipacionCarreras.clear();
	listaParticipacionPersonas.clear();
	try {
	    proyectoDto = registroServicio.obtenerProyectoPorId(proyectoDto
		    .getId());
	    List<PresupuestoProyectoDTO> listaPresupuesto = new ArrayList<PresupuestoProyectoDTO>();
	    for (PresupuestoProyectoDTO presupuesto : proyectoDto
		    .getListaPresupuestoProyectosDTO()) {
		if (presupuesto.getActivo()) {
		    listaPresupuesto.add(presupuesto);
		}
	    }
	    proyectoDto.getListaPresupuestoProyectosDTO().clear();
	    proyectoDto.setListaPresupuestoProyectosDTO(listaPresupuesto);

	    List<ParticipacionProyectoDTO> listaParticipacion = new ArrayList<ParticipacionProyectoDTO>();
	    for (ParticipacionProyectoDTO participacion : proyectoDto
		    .getListaParticipacionProyectosDTO()) {
		if (participacion.getActivo()) {
		    listaParticipacion.add(participacion);
		    if (participacion.getTabla().equals("informacion_carrera")) {
			listaParticipacionCarreras.add(participacion);
		    } else {
			listaParticipacionPersonas.add(participacion);
		    }
		}
	    }
	    proyectoDto.getListaParticipacionProyectosDTO().clear();
	    proyectoDto.setListaParticipacionProyectosDTO(listaParticipacion);
	    cargarCarreras();

	} catch (ServicioException e) {
	    e.printStackTrace();
	}
    }

    public void nuevoPresupuesto() {
	presupuestoProyecDto = new PresupuestoProyectoDTO();
	RequestContext.getCurrentInstance().reset("formPresupuesto");
    }

    public void editarPresupuesto() {
	RequestContext.getCurrentInstance().reset("formPresupuesto");

	RequestContext context = RequestContext.getCurrentInstance();
	context.execute("dlgPresupuesto.show()");
    }

    public void eliminarPresupuesto() {
	try {

	    if (proyectoDto.getListaPresupuestoProyectosDTO().size() == 1) {

		JsfUtil.msgError("No se puede eliminar Presupuesto, debe tener al menos 1 registro ingresado");
		return;
	    }

	    AuditoriaDTO auditoria = new AuditoriaDTO();
	    auditoria.setFechaModificacion(new Date(Calendar.getInstance(
		    TimeZone.getDefault()).getTimeInMillis()));
	    auditoria.setUsuarioModificacion(usuario);
	    presupuestoProyecDto.setAuditoriaDTO(auditoria);
	    presupuestoProyecDto.setActivo(false);
	    presupuestoProyecDto = registroServicio
		    .registrarPresupuestoProyecto(presupuestoProyecDto);

	    if (null != presupuestoProyecDto.getId()) {
		JsfUtil.msgInfo("Registro eliminado correctamente");

		proyectoDto.getListaPresupuestoProyectosDTO().clear();
		List<PresupuestoProyectoDTO> listaPresupuesto = new ArrayList<PresupuestoProyectoDTO>();
		listaPresupuesto = registroServicio
		        .obtenerPresupuestosPorProyecto(proyectoDto.getId());
		proyectoDto.setListaPresupuestoProyectosDTO(listaPresupuesto);

	    } else {
		JsfUtil.msgError("Error al eliminar presupuesto");
	    }
	} catch (ServicioException e) {
	    e.printStackTrace();
	}

    }

    public void agregarPresupuestoLista() {

	try {

	    if (null == presupuestoProyecDto.getAnio()
		    || presupuestoProyecDto.getAnio() <= 0) {
		JsfUtil.msgError("Ingrese un Año valido Positivo");
		return;
	    }
	    Calendar inicio = Calendar.getInstance();
	    inicio.setTime(proyectoDto.getFechaInicio());
	    Calendar fin = Calendar.getInstance();
	    fin.setTime(proyectoDto.getFechaFinPlaneado());
	    // int anioInicio = Calendar.getInstance().get(inicio.YEAR);
	    // int anioFin = Calendar.getInstance().get(fin.YEAR);
	    LOG.info("inicio== " + inicio);
	    LOG.info("fin== " + fin);

	    // if (presupuestoProyecDto.getAnio() != anioInicio
	    // && presupuestoProyecDto.getAnio() != anioFin) {
	    // JsfUtil.msgError("El año del presupuesto debe ser igual ha alguna fecha del proyecto");
	    // return;
	    // }
	    if (inicio.get(Calendar.YEAR) > presupuestoProyecDto.getAnio()
		    || fin.get(Calendar.YEAR) < presupuestoProyecDto.getAnio()) {
		JsfUtil.msgAdvert("El presupuesto del año "
		        + presupuestoProyecDto.getAnio()
		        + ", se encuentra fuera de las fechas de inicio y fin del proyecto.");
		return;
	    }

	    if (presupuestoProyecDto.getPresupuestoPlanificado() < 0) {
		JsfUtil.msgError("Presupuesto Planificado no puede ser negativo");
		return;
	    }

	    if (presupuestoProyecDto.getPresupuestoPlanificado() > 999999) {
		JsfUtil.msgError("Presupuesto Planificado debe ser menor a 999999");
		return;
	    }

	    if (presupuestoProyecDto.getPresupuestoEjecutado() < 0) {
		JsfUtil.msgError("Presupuesto Ejecutado no puede ser negativo");
		return;
	    }

	    if (presupuestoProyecDto.getPresupuestoEjecutado() > 999999) {
		JsfUtil.msgError("Presupuesto Ejecutado debe ser menor a 999999");
		return;
	    }

	    AuditoriaDTO auditoria = new AuditoriaDTO();
	    auditoria.setFechaModificacion(new Date(Calendar.getInstance(
		    TimeZone.getDefault()).getTimeInMillis()));
	    auditoria.setUsuarioModificacion(usuario);
	    presupuestoProyecDto.setAuditoriaDTO(auditoria);

	    presupuestoProyecDto.setActivo(true);

	    presupuestoProyecDto.setProyectoDTO(proyectoDto);

	    presupuestoProyecDto.setFaseIesDTO(faseIesDTO);

	    presupuestoProyecDto = registroServicio
		    .registrarPresupuestoProyecto(presupuestoProyecDto);

	    if (null != presupuestoProyecDto.getId()) {
		JsfUtil.msgInfo("Presupuesto guardado correctamente");
		actualizarTodo();
	    } else {
		JsfUtil.msgError("Error al guardar el presupuesto");
	    }
	} catch (ServicioException e) {
	    e.printStackTrace();
	}
	RequestContext context = RequestContext.getCurrentInstance();
	context.execute("dlgPresupuesto.hide()");
    }

    public void nuevaParticipacion() {
	participacionProyecDto = new ParticipacionProyectoDTO();
	listaPersonaDto.clear();
	personaDto = new PersonaDTO();
	horasPersona = null;
	band = "N"; // nuevo
	vistaParticipaciones.clear();
	RequestContext.getCurrentInstance().reset("formParticipacion");
    }

    public void editarParticipacion() {
	RequestContext.getCurrentInstance().reset("formParticipacion");

	band = "E"; // editar
	personaDto.setId(participacionProyecDto.getPersonaDTO().getId());

	personaDto.setIdentificacion(participacionProyecDto.getPersonaDTO()
	        .getIdentificacion());

	personaDto.setNombres(participacionProyecDto.getPersonaDTO()
	        .getNombres());

	personaDto.setApellidoPaterno(participacionProyecDto.getPersonaDTO()
	        .getApellidoPaterno());

	personaDto.setApellidoMaterno(participacionProyecDto.getPersonaDTO()
	        .getApellidoMaterno());

	// horasPersona = participacionProyecDto.getHorasVinculacionAnio();
	personaSeleccionada = participacionProyecDto.getTabla();

	listaPersonaDto.clear();
	listaPersonaDto = obtenerListaEstudianteDocente();

	RequestContext context = RequestContext.getCurrentInstance();
	context.execute("dlgParticipacion.show()");
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

	personaDto.setActivo(true);

	AuditoriaDTO auditoria = new AuditoriaDTO();
	auditoria.setFechaModificacion(new Date(Calendar.getInstance(
	        TimeZone.getDefault()).getTimeInMillis()));
	auditoria.setUsuarioModificacion(usuario);

	participacionProyecDto.setAuditoriaDTO(auditoria);
	participacionProyecDto.setActivo(true);
	participacionProyecDto.setFaseIesDTO(faseIesDTO);
	participacionProyecDto.setPersonaDTO(personaDto);
	participacionProyecDto.setTabla(personaSeleccionada);
	participacionProyecDto.setHorasParticipacion(horasPersona);

	for (ParticipacionProyectoDTO part : listaParticipacionPersonas) {
	    if (part.getPersonaDTO().getId().equals(personaDto.getId())) {
		JsfUtil.msgAdvert("La persona ya se encuentra asignada");
		return;
	    }
	}

	for (int i = 0; i < vistaParticipaciones.size(); i++) {
	    if (vistaParticipaciones.get(i).getPersonaDTO().getId()
		    .equals(personaDto.getId())) {
		vistaParticipaciones.remove(i);
		break;
	    }
	}

	vistaParticipaciones.add(participacionProyecDto);

	participacionProyecDto = new ParticipacionProyectoDTO();
	personaDto = new PersonaDTO();
	horasPersona = null;
    }

    // De la tabla temporal del popup a una tabla de pagina
    public void agregarParticipacionLista() {
	int registros = 0;
	for (ParticipacionProyectoDTO pp : vistaParticipaciones) {
	    try {
		pp.setProyectoDTO(proyectoDto);
		participacionProyecDto = registroServicio
		        .registrarParticipacionProyecto(pp);
		++registros;
	    } catch (ServicioException e) {
		e.printStackTrace();
	    }
	}

	if (registros == vistaParticipaciones.size()) {
	    JsfUtil.msgInfo("Registros almacenados correctamente");
	    actualizarTodo();
	} else {
	    JsfUtil.msgAdvert("Error al guardar la participacion");
	}

	vistaParticipaciones.clear();

	RequestContext context = RequestContext.getCurrentInstance();
	context.execute("dlgParticipacion.hide()");
    }

    public void eliminarParticipacion() {

	try {

	    if (proyectoDto.getListaParticipacionProyectosDTO().size() == 1) {

		JsfUtil.msgAdvert("No se puede eliminar Participación, debe tener al menos 1 registro ingresado");
		return;
	    }
	    // personaDto.setActivo(true);

	    AuditoriaDTO auditoria = new AuditoriaDTO();
	    auditoria.setFechaModificacion(new Date(Calendar.getInstance(
		    TimeZone.getDefault()).getTimeInMillis()));
	    auditoria.setUsuarioModificacion(usuario);
	    participacionProyecDto.setAuditoriaDTO(auditoria);

	    participacionProyecDto.setActivo(false);

	    participacionProyecDto = registroServicio
		    .registrarParticipacionProyecto(participacionProyecDto);

	    if (null != participacionProyecDto.getId()) {
		JsfUtil.msgInfo("Registro eliminado correctamente");
		actualizarTodo();
	    } else {
		JsfUtil.msgAdvert("Error al eliminar la participacion");
	    }
	} catch (ServicioException e) {
	    e.printStackTrace();
	}

	RequestContext context = RequestContext.getCurrentInstance();
	context.execute("dlgParticipacion.hide()");
    }

    public void guardarProyecto() {

	if (!this.validarProyectos(proyectoDto)) {
	    return;
	}

	// if (!this.validarFechasProyectosProgramas(proyectoDto)) {
	// return;
	// }

	try {

	    if ("VINCULACION".equals(proyectoDto.getLineaInvestigacion())
		    || !proyectoDto.getAceptadoEvaluador()) {
		proyectoDto.setAlineado(null);
	    }

	    AuditoriaDTO auditorDto = new AuditoriaDTO();
	    auditorDto.setUsuarioModificacion(usuario);
	    auditorDto.setFechaModificacion(new Date(Calendar.getInstance(
		    TimeZone.getDefault()).getTimeInMillis()));
	    proyectoDto.setAuditoriaDTO(auditorDto);
	    proyectoDto.setActivo(true);

	    InformacionCarreraDTO informacionCarrera = new InformacionCarreraDTO();
	    informacionCarrera.setId(idInformacionCarrera);
	    proyectoDto.setInformacionIesDTO(informacionIesDto);
	    proyectoDto.setTipo(TipoProyectoEnum.parse(tipoSeleccionado));
	    if (TipoProyectoEnum.VINCULACION.name().equals(
		    proyectoDto.getTipo().name())) {
		if (proyectoDto.getLineaInvestigacion() != null) {
		    proyectoDto.setLineaInvestigacion(null);
		}
	    }
	    proyectoDto.setFaseIesDTO(faseIesDTO);
	    if (alertaEvaluador) {
		proyectoDto.setVerificarEvidencia(false);
	    } else {
		proyectoDto
		        .setCategoriaEvaluacion(CategoriaEvaluacionEnum.PROYECTO
		                .name());
	    }
	    proyectoDto = registroServicio.registrarProyecto(proyectoDto);

	    if (null != proyectoDto.getId()) {
		JsfUtil.msgInfo("Registro almacenado correctamente");
		actualizarTodo();
	    } else {
		JsfUtil.msgError("Error al Guardar Proyectos");
	    }
	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al Guardar Proyectos, consulte con el Administrador del Sistema");
	    return;
	}
    }

    public void obtenerParticipante() {
	listaParticipanteCombo.add("docentes");
	listaParticipanteCombo.add("estudiantes");
    }

    public List<PersonaDTO> obtenerListaEstudianteDocente() {
	List<PersonaDTO> lisPersonaED = new ArrayList<PersonaDTO>();
	if (personaSeleccionada.equals("estudiantes")) {
	    List<EstudianteDTO> listaEstudiante = new ArrayList<EstudianteDTO>();
	    try {
		listaEstudiante.clear();
		// TODO PROGRAMAR LA CONSULTA POR CARRERA
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
	    List<DocenteDTO> listaDocente = new ArrayList<DocenteDTO>();

	    DocenteDTO docenteDTO = null;
	    try {
		listaDocente.clear();
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

    }

    public void limpiarDatos() {
	personaSeleccionada = "";
	proyectoDto = new ProyectoDTO();
	personaDto = new PersonaDTO();
	presupuestoProyecDto = new PresupuestoProyectoDTO();
	participacionProyecDto = new ParticipacionProyectoDTO();
	horasPersona = null;
	listaPersonaDto.clear();
    }

    public void regresarProyectos() {

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
	    JsfUtil.msgError("Ingrese el nombre del proyecto");
	    return false;
	}

	// if (null == proyect.getLineaInvestigacion()
	// || proyect.getLineaInvestigacion().equals("")) {
	// JsfUtil.msgError("Ingrese la línea de investigación del proyecto");
	// return false;
	// }

	if (null == proyect.getTipo() || proyect.getTipo().equals("")) {
	    JsfUtil.msgError("Ingrese el tipo");
	    return false;
	}

	if (null == proyect.getFechaInicio()) {
	    JsfUtil.msgError("Ingrese la Fecha de Inicio");
	    return false;
	}

	if (null == proyect.getFechaFinPlaneado()) {
	    JsfUtil.msgError("Ingrese Fecha Fin Planeado");
	    return false;
	}

	if (proyect.getFechaInicio().after(proyect.getFechaFinPlaneado())) {
	    JsfUtil.msgAdvert("La Fecha Fin Planeado debe ser mayor que la Fecha de Inicio");
	    return false;
	}

	// if (null == proyect.getFechaFinReal()) {
	// JsfUtil.msgError("Ingrese Fecha Fin Real");
	// return false;
	// }

	if (proyect.getFechaFinReal() != null) {
	    if (proyect.getFechaInicio().after(proyect.getFechaFinReal())) {
		JsfUtil.msgAdvert("La Fecha Fin Real debe ser mayor que la Fecha de Inicio");
		return false;
	    }
	}

	if (!"VINCULACION".equals(proyectoDto.getLineaInvestigacion())
	        && proyectoDto.getAceptadoEvaluador() == null) {
	    JsfUtil.msgError("Ingrese el valor del campo Alineado");
	    return false;
	}
	if (alertaEvaluador) {
	    if (proyectoDto.getCategoriaEvaluacion() == null
		    || proyectoDto.getCategoriaEvaluacion().isEmpty()) {
		JsfUtil.msgError("Ingrese la Categoría");
		return false;
	    }
	}

	return true;
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
	actualizarTodo();
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

    public Double getHorasPersona() {
	return horasPersona;
    }

    public void setHorasPersona(Double horasPersona) {
	this.horasPersona = horasPersona;
    }

    public String getBand() {
	return band;
    }

    public void setBand(String band) {
	this.band = band;
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

    public String getPerfil() {
	return perfil;
    }

    public void setPerfil(String perfil) {
	this.perfil = perfil;
    }

    public List<CarreraInformacion> getListaCarrerasDTO() {
	return listaCarrerasDTO;
    }

    public void setListaCarrerasDTO(List<CarreraInformacion> listaCarrerasDTO) {
	this.listaCarrerasDTO = listaCarrerasDTO;
    }

    public Integer[] getRangoAnios() {
	Integer[] rangoAnios = new Integer[21];

	Calendar calendar = Calendar.getInstance();
	int anio = calendar.get(Calendar.YEAR) - 10;

	for (int i = 0; i < 21; i++) {
	    rangoAnios[i] = i + anio;
	}

	return rangoAnios;
    }

    public List<ParticipacionProyectoDTO> getListaParticipacionPersonas() {
	return listaParticipacionPersonas;
    }

    public void setListaParticipacionPersonas(
	    List<ParticipacionProyectoDTO> listaParticipacionPersonas) {
	this.listaParticipacionPersonas = listaParticipacionPersonas;
    }

    public List<ParticipacionProyectoDTO> getListaParticipacionCarreras() {
	return listaParticipacionCarreras;
    }

    public void setListaParticipacionCarreras(
	    List<ParticipacionProyectoDTO> listaParticipacionCarreras) {
	this.listaParticipacionCarreras = listaParticipacionCarreras;
    }

    public FaseIesDTO getFaseIesDTO() {
	return faseIesDTO;
    }

    public void setFaseIesDTO(FaseIesDTO faseIesDTO) {
	this.faseIesDTO = faseIesDTO;
    }

    public Date getFechaActual() {
	return new Date();

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