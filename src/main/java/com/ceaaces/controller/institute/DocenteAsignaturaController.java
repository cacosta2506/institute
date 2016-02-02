/**
 * 
 */
package ec.gob.ceaaces.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.LazyDataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import ec.gob.ceaaces.catalogo.dtos.catalogos.AuditoriaDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.CarreraIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.IesDTO;
import ec.gob.ceaaces.catalogo.exceptions.ServicioException;
import ec.gob.ceaaces.data.DocentesLazy;
import ec.gob.ceaaces.data.Evidencia;
import ec.gob.ceaaces.data.ItemEvidenciaLocal;
import ec.gob.ceaaces.institutos.dtos.evaluacion.AsignaturaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ContratacionDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ContratacionPeriodoAcademicoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.DocenteAsignaturaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.DocenteDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionCarreraDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.MallaCurricularDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.PeriodoAcademicoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.PersonaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.SedeIesDTO;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.EvidenciasServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.JsfUtil;

public class DocenteAsignaturaController implements Serializable {

    /**
     * Clase para gestionar requerimientos del docente
     * 
     * @author eviscarra
     * @version 29/10/2013 14:48
     */

    private static final long serialVersionUID = 1461862843525203064L;

    private static final Logger LOG = Logger
	    .getLogger(DocenteAsignaturaController.class.getSimpleName());

    @Autowired
    private RegistroServicio registroServicio;

    @Autowired
    private CatalogoServicio catalogoServicio;

    @Autowired
    private EvidenciasServicio evidenciasServicio;

    private LazyDataModel<DocenteDTO> docentesLazy;
    private DocenteDTO seleccion;
    private Date minDate;
    private Date maxDate;
    // private Long idInfCarrera;
    private String selectContratacion;
    private String usuario;
    private String perfil;
    private int indice;
    private int registros = 10;

    private IesDTO iesDTO;
    private AsignaturaDTO asignatura;
    private AsignaturaDTO selectAsignaturaDto;
    private DocenteAsignaturaDTO docenteAsignaturaDTO;
    private DocenteAsignaturaDTO seleccionAsignaturaAsignada;
    private MallaCurricularDTO selectMalla;
    private InformacionIesDTO infoIes;
    private InformacionCarreraDTO infoCarreraDTO;
    private ContratacionPeriodoAcademicoDTO contratacionPeriodoAcademicoDTO;
    private ContratacionPeriodoAcademicoDTO contratacionPeriodoSeleccionado;
    private String valorSeleccionado;
    private PeriodoAcademicoDTO periodoSeleccionado;
    private Long idSedeSeleccionada;
    private InformacionIesDTO informacionIesDto;
    private Long idInformacionCarreraSeleccionada;
    private Long idPeriodoAcademicoSeleccionado;

    private List<DocenteDTO> docentesDTO;
    private List<DocenteAsignaturaDTO> asignaturasDTO;
    private List<ContratacionDTO> contrataciones;
    private List<AsignaturaDTO> asignaturasDisponibles;
    private List<MallaCurricularDTO> mallasCurriculares;
    private List<ContratacionPeriodoAcademicoDTO> listaContratacionPeriodoAcademicoDTO;
    private List<PeriodoAcademicoDTO> listaPeriodosMatricula;
    private List<SedeIesDTO> listaSedeIesDto;
    private List<InformacionCarreraDTO> listaInformacionCarreraDto;

    // EVIDENCIAS
    private ItemEvidenciaLocal itemEvidenciaSeleccionado;
    private List<ItemEvidenciaLocal> evidenciasDocenteAsignatura;
    private Evidencia archivoSeleccionado;
    private final static String URL_ELIMINADOS = "/home/jhomara/archivosIES/evaluacionCarreras/eliminados/";
    private final static String URL = "/home/jhomara/archivosIES/evaluacionCarreras";
    private static final String ORIGEN_CARGA = "SISTEMA";

    // PERIODOS
    private boolean nuevoPeriodo;

    private Map<Long, String> listaMallas;

    private boolean editarDocenteAsignatura;
    private FaseIesDTO faseIesDTO;

    public DocenteAsignaturaController() {
	this.listaMallas = new HashMap<>();
	this.asignatura = new AsignaturaDTO();
	this.seleccion = new DocenteDTO();
	this.docentesDTO = new ArrayList<DocenteDTO>();
	this.asignaturasDTO = new ArrayList<DocenteAsignaturaDTO>();
	this.docenteAsignaturaDTO = new DocenteAsignaturaDTO();
	this.contrataciones = new ArrayList<ContratacionDTO>();
	this.asignaturasDisponibles = new ArrayList<AsignaturaDTO>();
	this.setMallasCurriculares(new ArrayList<MallaCurricularDTO>());
	this.selectMalla = new MallaCurricularDTO();
	this.minDate = new Date();
	this.minDate.setYear(2003);
	this.minDate.setDate(1);
	this.minDate.setMonth(Calendar.JANUARY);
	this.maxDate = new Date();
	this.iesDTO = new IesDTO();
	this.infoCarreraDTO = new InformacionCarreraDTO();
	this.listaSedeIesDto = new ArrayList<>();
	this.listaInformacionCarreraDto = new ArrayList<>();
	this.periodoSeleccionado = new PeriodoAcademicoDTO();
    }

    @PostConstruct
    public void cargarDatos() {
	try {
	    ListaIesController controller = (ListaIesController) JsfUtil
		    .obtenerObjetoSesion("listaIesController");

	    this.usuario = SecurityContextHolder.getContext()
		    .getAuthentication().getName();

	    this.faseIesDTO = controller.getFaseIesDTO();
	    this.iesDTO = controller.getIes();
	    this.informacionIesDto = registroServicio
		    .obtenerInformacionIesPorIes(iesDTO);

	    obtenerDocentes();
	    cargarSedeIes();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void cargarSedeIes() {

	try {
	    this.listaSedeIesDto.clear();
	    this.listaSedeIesDto = registroServicio
		    .obtenerSedesIes(this.informacionIesDto.getId());
	} catch (Exception e) {
	    JsfUtil.msgError("Error al cargar Sede IES");
	}
    }

    public void cargarCarreras() {

	try {
	    if (null == this.idSedeSeleccionada) {
		return;
	    }

	    listaInformacionCarreraDto.clear();
	    listaInformacionCarreraDto = registroServicio
		    .obtenerInfCarreraPorSede(this.idSedeSeleccionada, null);
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    public void nuevoDocenteAsignatura() {
	docenteAsignaturaDTO = new DocenteAsignaturaDTO();
	selectAsignaturaDto = new AsignaturaDTO();
	editarDocenteAsignatura = true;
	// obtenerMallas();
    }

    public void editarAsignatura() {
	editarDocenteAsignatura = true;
	selectMalla = docenteAsignaturaDTO.getAsignaturaDTO()
	        .getMallaCurricularDTO();
	selectAsignaturaDto = docenteAsignaturaDTO.getAsignaturaDTO();
	obtenerMallas();
    }

    public void cancelarEdicion() {
	editarDocenteAsignatura = false;
	docenteAsignaturaDTO = new DocenteAsignaturaDTO();
	selectAsignaturaDto = new AsignaturaDTO();
	selectMalla = new MallaCurricularDTO();
    }

    /**
     * metodo para obtener listado de marias por id de docente
     * 
     * @author fochoa
     */
    public void obtenerAsignaturas() {
	LOG.info("--> METODO obtenerAsignaturas");

	try {
	    PeriodoAcademicoDTO periodoAcademicoDTO = new PeriodoAcademicoDTO();

	    LOG.info("periodoSeleccionado: " + periodoSeleccionado.getId());
	    LOG.info("seleccion.getId(): " + seleccion.getId());

	    asignaturasDTO.clear();
	    List<DocenteAsignaturaDTO> auxListaAsignatura = new ArrayList<>();
	    // auxListaAsignatura = registroServicio
	    // .obtenerAsignaturaPorDocente(seleccion.getId());
	    LOG.info("auxListaAsignatura: " + auxListaAsignatura.size());

	    if (periodoSeleccionado != null
		    && periodoSeleccionado.getId() != null
		    && !periodoSeleccionado.getId().equals(0L)) {

		periodoAcademicoDTO = registroServicio
		        .obtenerPeriodoAcademicoPorId(periodoSeleccionado
		                .getId());

		for (DocenteAsignaturaDTO docAsig : auxListaAsignatura) {
		    if (docAsig.getFechaInicio() != null
			    && docAsig.getFechaFin() != null) {
			if (docAsig.getFechaInicio().compareTo(
			        periodoAcademicoDTO.getFechaInicioPeriodo()) == 0
			        && (docAsig.getFechaFin().compareTo(
			                periodoAcademicoDTO
			                        .getFechaFinPeriodo()) == 0)) {
			    asignaturasDTO.add(docAsig);
			}
		    }
		}
	    } else {
		asignaturasDTO.addAll(auxListaAsignatura);
	    }
	    for (int i = 0; i < asignaturasDTO.size(); i++) {
		LOG.info("DATOS 1: "
		        + asignaturasDTO.get(i).getAsignaturaDTO().getNombre());
		LOG.info("DATOS 2: "
		        + asignaturasDTO.get(i).getAsignaturaDTO()
		                .getNumeroCreditos());
	    }
	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al obtener asignaturas");
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al obtener asignaturas");
	}
    }

    public void seleccionarContratacionPeriodo(ValueChangeEvent event) {
	if (event != null) {
	    if (event.getNewValue() != null) {
		String id = String.valueOf(event.getComponent().getAttributes()
		        .get("id"));

		periodoSeleccionado.setId(new Long(event.getNewValue()
		        .toString()));
	    }
	    obtenerAsignaturas();
	}

	if (event != null && event.getNewValue() != null) {
	    valorSeleccionado = event.getNewValue().toString();
	}
    }

    /**
     * Metodo para obtener listado de docentes
     * 
     * @author fochoa
     * 
     */
    public void obtenerDocentes() {

	docentesDTO.clear();

	DocenteDTO docenteDTO = null;

	try {

	    docentesDTO = registroServicio.obtenerDocentesPorInformacionIes(
		    docenteDTO, infoIes.getId());
	    docentesLazy = new DocentesLazy(docentesDTO);

	} catch (Exception e) {

	    LOG.info(e.getMessage());
	}
    }

    public void crearNuevoPeriodo() {
	nuevoPeriodo = true;
    }

    /**
     * Obtiene listado de asignaturas disponibles para ser asignadas a un
     * docente
     * 
     * @author fochoa
     * 
     */
    public void obtenerMallas() {
	LOG.info("--> METODO obtenerMallas");
	try {
	    this.infoCarreraDTO = new InformacionCarreraDTO();
	    LOG.info("idInformacionCarreraSeleccionada: "
		    + idInformacionCarreraSeleccionada);
	    this.infoCarreraDTO = registroServicio
		    .obtenerInformacionCarreraPorId(idInformacionCarreraSeleccionada);
	    listaMallas.clear();

	    mallasCurriculares = new ArrayList<>();
	    if (null != this.infoCarreraDTO) {
		mallasCurriculares = registroServicio
		        .obtenerMallaCurricular(infoCarreraDTO);
	    }

	    LOG.info("mallasCurriculares: " + mallasCurriculares.size());
	    LOG.info("mallasCurriculares: " + mallasCurriculares.get(0).getId());
	    LOG.info("mallasCurriculares: "
		    + mallasCurriculares.get(0).getFechaInicioVigencia());
	    for (MallaCurricularDTO malla : mallasCurriculares) {
		listaMallas.put(malla.getId(), malla.toString());
	    }

	    LOG.info("listaMallas: " + listaMallas.size());

	    // obtener contrataciones docente
	    obtenerContrataciones();

	} catch (ServicioException e) {
	    e.printStackTrace();
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}

    }

    public void obtenerAsignaturasDisponibles() {
	LOG.info("--> METODO obtenerAsignaturasDisponibles");
	try {
	    RequestContext context = RequestContext.getCurrentInstance();
	    asignaturasDisponibles.clear();

	    LOG.info("MALLA: " + selectMalla.getId());

	    asignaturasDisponibles = registroServicio
		    .obtenerAsignaturasPorMalla(selectMalla.getId());
	    LOG.info("asignaturasDisponibles: " + asignaturasDisponibles.size());
	    context.execute("mallaDlgDocente.show();");

	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}

    }

    /**
     * Obtiene lista de contreataciones
     */
    public void obtenerContrataciones() {
	try {
	    contrataciones = new ArrayList<>();
	    LOG.info("seleccion.getId(): " + seleccion.getId());
	    contrataciones = registroServicio.obtenerContrataciones(seleccion
		    .getId());
	} catch (ServicioException e) {

	    e.printStackTrace();
	}
    }

    /**
     * Metodo para desactivar una asignatura
     */

    public void eliminarAsignatura() {
	this.docenteAsignaturaDTO.setDocenteDTO(seleccion);
	PersonaDTO people = new PersonaDTO();
	people.setId(seleccion.getId());
	asignatura.setActivo(true);
	// asignatura.setActivo(false);
	this.docenteAsignaturaDTO.setAsignaturaDTO(asignatura);
	AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
	auditoriaDTO.setUsuarioModificacion(usuario);
	this.docenteAsignaturaDTO.setAuditoriaDTO(auditoriaDTO);
	this.docenteAsignaturaDTO.setActivo(false);
	this.docenteAsignaturaDTO.setFaseIesDTO(faseIesDTO);

	try {
	    registroServicio.registrarDocenteAsignatura(docenteAsignaturaDTO);

	    LOG.info("asignatura codigo: " + asignatura.getCodigo());
	    obtenerAsignaturas();
	    JsfUtil.msgInfo("Registro eliminado correctamente");
	    docenteAsignaturaDTO = new DocenteAsignaturaDTO();
	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("No se ha podido eliminar la Asignatura Seleccionada.");
	}

    }

    /**
     * Metodo para asignar materias docente
     */

    private boolean validarDatos() {
	boolean b = true;
	try {

	    if (null == idSedeSeleccionada || idSedeSeleccionada.equals(0L)) {
		JsfUtil.msgError("Matriz/Extensión es requerida");
		return false;
	    }

	    if (null == periodoSeleccionado.getId()) {
		JsfUtil.msgError("Ingrese un Periodo");
		return false;
	    }

	    PeriodoAcademicoDTO periodoAcademicoDTO = new PeriodoAcademicoDTO();
	    periodoAcademicoDTO = registroServicio
		    .obtenerPeriodoAcademicoPorId(periodoSeleccionado.getId());

	    if (nuevoPeriodo) {
		periodoAcademicoDTO
		        .setFechaInicioPeriodo(this.docenteAsignaturaDTO
		                .getFechaInicio());
		periodoAcademicoDTO
		        .setFechaFinPeriodo(this.docenteAsignaturaDTO
		                .getFechaFin());
	    }
	    if (docenteAsignaturaDTO.getFechaInicio() == null
		    || docenteAsignaturaDTO.getFechaFin() == null) {
		JsfUtil.msgError("Ingrese la fecha inicio y fin de la asignatura");
		b = false;
	    }

	    if (!docenteAsignaturaDTO.getFechaInicio().after(
		    periodoAcademicoDTO.getFechaInicioPeriodo())
		    || !docenteAsignaturaDTO.getFechaFin().before(
		            periodoAcademicoDTO.getFechaFinPeriodo())) {
		JsfUtil.msgError("La fecha de incio y fin de asignatura deben estar"
		        + " dentro del periodo de académico.");
		b = false;
	    }
	    if (!docenteAsignaturaDTO.getFechaInicio().before(
		    docenteAsignaturaDTO.getFechaFin())) {
		JsfUtil.msgError("Las fechas de fin debe ser mayor que la fecha de inicio");
		b = false;
	    }
	    if (selectAsignaturaDto.getId() == null) {
		JsfUtil.msgError("Seleccione la asignatura correspondiente a la malla");
		b = false;
	    }
	    if (docenteAsignaturaDTO.getNumParalelos() == null
		    || docenteAsignaturaDTO.getNumParalelos().equals(0)) {
		JsfUtil.msgError("Ingrese el número de paralelos");
		b = false;
	    }
	    if (docenteAsignaturaDTO.getNumHoras() == null
		    || docenteAsignaturaDTO.getNumHoras().equals(0)) {
		JsfUtil.msgError("Ingrese el número de horas");
		b = false;
	    }
	    if (docenteAsignaturaDTO.getDuracionHoraClase() == null
		    || docenteAsignaturaDTO.getDuracionHoraClase().equals(0)) {
		JsfUtil.msgError("Ingrese la duración de la hora clase");
		b = false;
	    }
	    if (docenteAsignaturaDTO.getId() == null) {
		for (DocenteAsignaturaDTO docAsignatura : asignaturasDTO) {
		    if (selectAsignaturaDto.getId().equals(
			    docAsignatura.getAsignaturaDTO().getId())) {
			if (docenteAsignaturaDTO.getFechaInicio().compareTo(
			        docAsignatura.getFechaInicio()) == 0) {
			    JsfUtil.msgError("Ya tiene la asignatura "
				    + selectAsignaturaDto.getNombre()
				    + " asignada en las fechas seleccionadas.");
			    b = false;
			    break;
			}
		    }
		}
	    }
	    return b;
	} catch (ServicioException e) {
	    e.printStackTrace();
	}
	return b;
    }

    public void agregarDocenteAsignatura() {

	RequestContext context = RequestContext.getCurrentInstance();
	DocenteController docController = (DocenteController) FacesContext
	        .getCurrentInstance().getExternalContext().getSessionMap()
	        .get("docenteController");
	try {
	    if (!validarDatos()) {
		context.addCallbackParam("esValido", false);
		return;
	    } else {

		AuditoriaDTO a = new AuditoriaDTO();
		a.setFechaModificacion(new Date());
		a.setUsuarioModificacion(usuario);
		this.docenteAsignaturaDTO.setAuditoriaDTO(a);

		ContratacionPeriodoAcademicoDTO cPeriodoAcademicoDTO = new ContratacionPeriodoAcademicoDTO();
		cPeriodoAcademicoDTO = registroServicio
		        .obtenerContratacionPeriodoAcademicoPorPeriodoDocente(
		                this.periodoSeleccionado.getId(),
		                seleccion.getId());

		this.docenteAsignaturaDTO
		        .setContratacionPeriodoAcademicoDTO(cPeriodoAcademicoDTO);

		this.docenteAsignaturaDTO.setFaseIesDTO(faseIesDTO);
		this.docenteAsignaturaDTO.setActivo(true);
		this.docenteAsignaturaDTO.setAsignaturaDTO(selectAsignaturaDto);
		this.docenteAsignaturaDTO.setDocenteDTO(seleccion);
		this.docenteAsignaturaDTO.setOrigenCarga(ORIGEN_CARGA);
		if (seleccion.getId() == null) {
		    this.asignaturasDTO.add(docenteAsignaturaDTO);
		    docController.guardar();
		} else {

		    docenteAsignaturaDTO.setFaseIesDTO(faseIesDTO);
		    docenteAsignaturaDTO = registroServicio
			    .registrarDocenteAsignatura(docenteAsignaturaDTO);
		    JsfUtil.msgInfo("Registro almacenado correctamente");
		}
		docenteAsignaturaDTO = new DocenteAsignaturaDTO();

		editarDocenteAsignatura = false;
		obtenerAsignaturas();
		selectAsignaturaDto = new AsignaturaDTO();
		mallasCurriculares = new ArrayList<>();
		asignatura = new AsignaturaDTO();

		selectContratacion = "";
	    }
	} catch (ServicioException e) {
	    JsfUtil.msgError("Fallo el ingreso");
	    e.printStackTrace();
	}
    }

    /**
     * @return the catalogoServicio
     */
    public CatalogoServicio getCatalogoServicio() {
	return catalogoServicio;
    }

    /**
     * @return the asignatura
     */
    public AsignaturaDTO getAsignatura() {
	return asignatura;
    }

    /**
     * @return the docentesDTO
     */
    public List<DocenteDTO> getDocentesDTO() {
	return docentesDTO;
    }

    /**
     * @param catalogoServicio
     *            the catalogoServicio to set
     */
    public void setCatalogoServicio(CatalogoServicio catalogoServicio) {
	this.catalogoServicio = catalogoServicio;
    }

    /**
     * @param asignatura
     *            the asignatura to set
     */
    public void setAsignatura(AsignaturaDTO asignatura) {
	this.asignatura = asignatura;
    }

    /**
     * @param docentesDTO
     *            the docentesDTO to set
     */
    public void setDocentesDTO(ArrayList<DocenteDTO> docentesDTO) {
	this.docentesDTO = docentesDTO;
    }

    public List<DocenteAsignaturaDTO> getAsignaturasDTO() {
	return asignaturasDTO;
    }

    public void setAsignaturasDTO(List<DocenteAsignaturaDTO> asignaturasDTO) {
	this.asignaturasDTO = asignaturasDTO;
    }

    /**
     * @return the idInfCarrera
     */
    public Long getIdInfCarrera() {

	ListaIesController controller = (ListaIesController) FacesContext
	        .getCurrentInstance().getExternalContext().getSessionMap()
	        .get("listaIesController");
	if (controller.getPerfil().getNombre().startsWith("CAR")) {
	    Long idCarrera = controller.getCarrera().getId();
	    String codigo = controller.getCarrera().getCodigo();
	    CarreraIesDTO car = new CarreraIesDTO();
	    car.setId(idCarrera);
	    car.setCodigo(codigo);
	    try {

		infoCarreraDTO = registroServicio
		        .obtenerInformacionCarreraPorCarrera(car);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}

	return null;

    }

    /**
     * @param docentesDTO
     *            the docentesDTO to set
     */
    public void setDocentesDTO(List<DocenteDTO> docentesDTO) {
	this.docentesDTO = docentesDTO;
    }

    public LazyDataModel<DocenteDTO> getDocentesLazy() {
	return docentesLazy;
    }

    public void setDocentesLazy(LazyDataModel<DocenteDTO> docentesLazy) {
	this.docentesLazy = docentesLazy;
    }

    public DocenteDTO getSeleccion() {
	return seleccion;
    }

    public void setSeleccion(DocenteDTO docente) {
	this.seleccion = docente;
	this.nuevoPeriodo = false;
	this.editarDocenteAsignatura = false;
	this.editarDocenteAsignatura = false;
	this.docenteAsignaturaDTO = new DocenteAsignaturaDTO();
	this.selectAsignaturaDto = new AsignaturaDTO();
	this.selectMalla = new MallaCurricularDTO();

	obtenerAsignaturas();
    }

    /**
     * @return the docenteAsignaturaDTO
     */
    public DocenteAsignaturaDTO getDocenteAsignaturaDTO() {
	return docenteAsignaturaDTO;
    }

    /**
     * @param docenteAsignaturaDTO
     *            the docenteAsignaturaDTO to set
     */
    public void setDocenteAsignaturaDTO(
	    DocenteAsignaturaDTO docenteAsignaturaDTO) {
	this.docenteAsignaturaDTO = docenteAsignaturaDTO;
    }

    /**
     * @return the selectContratacion
     */
    public String getSelectContratacion() {
	return selectContratacion;
    }

    /**
     * @return the contrataciones
     */
    public List<ContratacionDTO> getContrataciones() {
	return contrataciones;
    }

    /**
     * @param selectContratacion
     *            the selectContratacion to set
     */
    public void setSelectContratacion(String selectContratacion) {
	this.selectContratacion = selectContratacion;
    }

    /**
     * @param contrataciones
     *            the contrataciones to set
     */
    public void setContrataciones(List<ContratacionDTO> contrataciones) {
	this.contrataciones = contrataciones;
    }

    public List<AsignaturaDTO> getAsignaturasDisponibles() {
	return asignaturasDisponibles;
    }

    public void setObtenerDocentesAsignaturasDisponibles(
	    List<AsignaturaDTO> asignaturasDisponibles) {
	this.asignaturasDisponibles = asignaturasDisponibles;
    }

    public DocenteAsignaturaDTO getSeleccionAsignaturaAsignada() {
	return seleccionAsignaturaAsignada;
    }

    public void setSeleccionAsignaturaAsignada(
	    DocenteAsignaturaDTO seleccionAsignaturaAsignada) {
	this.seleccionAsignaturaAsignada = seleccionAsignaturaAsignada;
    }

    public List<MallaCurricularDTO> getMallasCurriculares() {
	return mallasCurriculares;
    }

    public void setMallasCurriculares(
	    List<MallaCurricularDTO> mallasCurriculares) {
	this.mallasCurriculares = mallasCurriculares;
    }

    public MallaCurricularDTO getSelectMalla() {
	return selectMalla;
    }

    public void setSelectMalla(MallaCurricularDTO selectMalla) {
	this.selectMalla = selectMalla;
    }

    public void onRowSelect(SelectEvent event) {
	FacesMessage msg = new FacesMessage("Malla Seleccionada",
	        ((MallaCurricularDTO) event.getObject()).getCodigoUnico());
	selectMalla = new MallaCurricularDTO();
	selectMalla = (MallaCurricularDTO) event.getObject();
	LOG.info("codigo: " + selectMalla.getCodigoUnico());

	// obtenerAsignaturasDisponibles();
	FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    /**
     * @return the selectAsignaturaDto
     */
    public AsignaturaDTO getSelectAsignaturaDto() {
	return selectAsignaturaDto;
    }

    /**
     * @param selectAsignaturaDto
     *            the selectAsignaturaDto to set
     */
    public void setSelectAsignaturaDto(AsignaturaDTO selectAsignaturaDto) {
	this.selectAsignaturaDto = selectAsignaturaDto;
    }

    /**
     * @return the minDate
     */
    public Date getMinDate() {
	Calendar cal = Calendar.getInstance();
	int anio = Calendar.getInstance().get(Calendar.YEAR) - 15;
	cal.set(anio, Calendar.JANUARY, 1);
	minDate = new Date(cal.getTimeInMillis());

	return minDate;
    }

    /**
     * @param minDate
     *            the minDate to set
     */
    public void setMinDate(Date minDate) {
	this.minDate = minDate;
    }

    /**
     * @return the maxDate
     */
    public Date getMaxDate() {
	return maxDate;
    }

    /**
     * @param maxDate
     *            the maxDate to set
     */
    public void setMaxDate(Date maxDate) {
	this.maxDate = maxDate;
    }

    /**
     * @return the usuario
     */
    public String getUsuario() {
	return usuario;
    }

    /**
     * @param usuario
     *            the usuario to set
     */
    public void setUsuario(String usuario) {
	this.usuario = usuario;
    }

    /**
     * @return the infoCarreraDTO
     */
    public InformacionCarreraDTO getInfoCarreraDTO() {
	return infoCarreraDTO;
    }

    /**
     * @param infoCarreraDTO
     *            the infoCarreraDTO to set
     */
    public void setInfoCarreraDTO(InformacionCarreraDTO infoCarreraDTO) {
	this.infoCarreraDTO = infoCarreraDTO;
    }

    /**
     * @return the infoIes
     */
    public InformacionIesDTO getInfoIes() {
	return infoIes;
    }

    /**
     * @param infoIes
     *            the infoIes to set
     */
    public void setInfoIes(InformacionIesDTO infoIes) {
	this.infoIes = infoIes;
    }

    public boolean isNuevoPeriodo() {
	return nuevoPeriodo;
    }

    public void setNuevoPeriodo(boolean nuevoPeriodo) {
	this.nuevoPeriodo = nuevoPeriodo;
    }

    public void setAsignaturasDisponibles(
	    List<AsignaturaDTO> asignaturasDisponibles) {
	this.asignaturasDisponibles = asignaturasDisponibles;
    }

    public ItemEvidenciaLocal getItemEvidenciaSeleccionado() {
	return itemEvidenciaSeleccionado;
    }

    public void setItemEvidenciaSeleccionado(
	    ItemEvidenciaLocal itemEvidenciaSeleccionado) {
	this.itemEvidenciaSeleccionado = itemEvidenciaSeleccionado;
    }

    public List<ItemEvidenciaLocal> getEvidenciasDocenteAsignatura() {
	return evidenciasDocenteAsignatura;
    }

    public void setEvidenciasDocenteAsignatura(
	    List<ItemEvidenciaLocal> evidenciasDocenteAsignatura) {
	this.evidenciasDocenteAsignatura = evidenciasDocenteAsignatura;
    }

    public Evidencia getArchivoSeleccionado() {
	return archivoSeleccionado;
    }

    public void setArchivoSeleccionado(Evidencia archivoSeleccionado) {
	this.archivoSeleccionado = archivoSeleccionado;
    }

    public boolean isEditarDocenteAsignatura() {
	return editarDocenteAsignatura;
    }

    public void setEditarDocenteAsignatura(boolean editarDocenteAsignatura) {
	this.editarDocenteAsignatura = editarDocenteAsignatura;
    }

    public Map<Long, String> getListaMallas() {
	return listaMallas;
    }

    public void setListaMallas(Map<Long, String> listaMallas) {
	this.listaMallas = listaMallas;
    }

    public void cargarPeriodosAcademicos() {
	try {
	    listaPeriodosMatricula = registroServicio
		    .obtenerPeriodosMatricula(this.idInformacionCarreraSeleccionada);
	} catch (ServicioException e) {
	    e.printStackTrace();
	}
    }

    public Map<Long, String> getPeriodosMatricula() {
	Map<Long, String> periodos = new HashMap<Long, String>();

	DocenteController controller = (DocenteController) FacesContext
	        .getCurrentInstance().getExternalContext().getSessionMap()
	        .get("docenteController");
	infoCarreraDTO = controller.getInformacionCarreraDTO();

	// TODO
	try {
	    listaPeriodosMatricula = registroServicio
		    .obtenerPeriodosMatricula(this.informacionIesDto.getId());
	    for (PeriodoAcademicoDTO periodo : listaPeriodosMatricula) {
		periodos.put(periodo.getId(), periodo.toString());
	    }
	} catch (ServicioException e) {
	    e.printStackTrace();
	}

	LOG.info("periodos: " + periodos.size());
	return periodos;
    }

    public IesDTO getIesDTO() {
	return iesDTO;
    }

    public void setIesDTO(IesDTO iesDTO) {
	this.iesDTO = iesDTO;
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

    public ContratacionPeriodoAcademicoDTO getContratacionPeriodoAcademicoDTO() {
	return contratacionPeriodoAcademicoDTO;
    }

    public void setContratacionPeriodoAcademicoDTO(
	    ContratacionPeriodoAcademicoDTO contratacionPeriodoAcademicoDTO) {
	this.contratacionPeriodoAcademicoDTO = contratacionPeriodoAcademicoDTO;
    }

    public String getValorSeleccionado() {
	return valorSeleccionado;
    }

    public void setValorSeleccionado(String valorSeleccionado) {
	this.valorSeleccionado = valorSeleccionado;
    }

    public List<ContratacionPeriodoAcademicoDTO> getListaContratacionPeriodoAcademicoDTO() {
	return listaContratacionPeriodoAcademicoDTO;
    }

    public void setListaContratacionPeriodoAcademicoDTO(
	    List<ContratacionPeriodoAcademicoDTO> listaContratacionPeriodoAcademicoDTO) {
	this.listaContratacionPeriodoAcademicoDTO = listaContratacionPeriodoAcademicoDTO;
    }

    public FaseIesDTO getFaseIesDTO() {
	return faseIesDTO;
    }

    public void setFaseIesDTO(FaseIesDTO faseIesDTO) {
	this.faseIesDTO = faseIesDTO;
    }

    public ContratacionPeriodoAcademicoDTO getContratacionPeriodoSeleccionado() {
	return contratacionPeriodoSeleccionado;
    }

    public void setContratacionPeriodoSeleccionado(
	    ContratacionPeriodoAcademicoDTO contratacionPeriodoSeleccionado) {
	this.contratacionPeriodoSeleccionado = contratacionPeriodoSeleccionado;
    }

    public PeriodoAcademicoDTO getPeriodoSeleccionado() {
	return periodoSeleccionado;
    }

    public void setPeriodoSeleccionado(PeriodoAcademicoDTO periodoSeleccionado) {
	this.periodoSeleccionado = periodoSeleccionado;
    }

    public List<PeriodoAcademicoDTO> getListaPeriodosMatricula() {
	return listaPeriodosMatricula;
    }

    public void setListaPeriodosMatricula(
	    List<PeriodoAcademicoDTO> listaPeriodosMatricula) {
	this.listaPeriodosMatricula = listaPeriodosMatricula;
    }

    public Long getIdSedeSeleccionada() {
	return idSedeSeleccionada;
    }

    public void setIdSedeSeleccionada(Long idSedeSeleccionada) {
	this.idSedeSeleccionada = idSedeSeleccionada;
    }

    public List<SedeIesDTO> getListaSedeIesDto() {
	return listaSedeIesDto;
    }

    public void setListaSedeIesDto(List<SedeIesDTO> listaSedeIesDto) {
	this.listaSedeIesDto = listaSedeIesDto;
    }

    public InformacionIesDTO getInformacionIesDto() {
	return informacionIesDto;
    }

    public void setInformacionIesDto(InformacionIesDTO informacionIesDto) {
	this.informacionIesDto = informacionIesDto;
    }

    public List<InformacionCarreraDTO> getListaInformacionCarreraDto() {
	return listaInformacionCarreraDto;
    }

    public void setListaInformacionCarreraDto(
	    List<InformacionCarreraDTO> listaInformacionCarreraDto) {
	this.listaInformacionCarreraDto = listaInformacionCarreraDto;
    }

    public Long getIdInformacionCarreraSeleccionada() {
	return idInformacionCarreraSeleccionada;
    }

    public void setIdInformacionCarreraSeleccionada(
	    Long idInformacionCarreraSeleccionada) {
	this.idInformacionCarreraSeleccionada = idInformacionCarreraSeleccionada;
    }

    public Long getIdPeriodoAcademicoSeleccionado() {
	return idPeriodoAcademicoSeleccionado;
    }

    public void setIdPeriodoAcademicoSeleccionado(
	    Long idPeriodoAcademicoSeleccionado) {
	this.idPeriodoAcademicoSeleccionado = idPeriodoAcademicoSeleccionado;
    }

}
