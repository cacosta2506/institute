package ec.gob.ceaaces.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.event.ValueChangeEvent;

import org.primefaces.context.RequestContext;
import org.primefaces.event.TransferEvent;
import org.primefaces.model.DualListModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import ec.gob.ceaaces.catalogo.dtos.catalogos.AuditoriaDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.CarreraIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.IesDTO;
import ec.gob.ceaaces.catalogo.exceptions.ServicioException;
import ec.gob.ceaaces.data.Evidencia;
import ec.gob.ceaaces.data.ItemEvidenciaLocal;
import ec.gob.ceaaces.institutos.dtos.evaluacion.AsignaturaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionCarreraDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.LaboratorioAsignaturaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.LaboratorioDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.MallaCurricularDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.SedeIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.SedeIesDistribucionFisicaDTO;
import ec.gob.ceaaces.institutos.enumeraciones.TipoLaboratorioEnum;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.EvidenciasServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.JsfUtil;

public class LaboratorioController implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -4967773530740528864L;

    private static final Logger LOG = Logger
	    .getLogger(LaboratorioController.class.getSimpleName());

    @Autowired
    private RegistroServicio registroServicio;

    @Autowired
    private EvidenciasServicio evidenciasServicio;

    @Autowired
    private CatalogoServicio catalogosServicio;

    private IesDTO iesDTO;
    private CarreraIesDTO carreraSeleccionada;
    private InformacionIesDTO informacionIes;
    private LaboratorioDTO laboratorioSeleccionado;
    private FaseDTO faseEvaluacion;
    private Evidencia archivoSeleccionado;
    private FaseIesDTO faseIesDTO;
    private LaboratorioAsignaturaDTO laboratorioAsignaturaDto;
    private AsignaturaDTO asignaturaDto;

    private List<SedeIesDTO> listaSedeIesDto;
    private List<SedeIesDistribucionFisicaDTO> listaSedeDistribucion;
    private List<LaboratorioDTO> listaLaboratorios;
    private List<ItemEvidenciaLocal> listaItemEvidencias;
    private List<MallaCurricularDTO> listaMallaCurricular;
    private List<SedeIesDTO> listaSedeIesAsignaturaDto;
    private List<InformacionCarreraDTO> listaInformacionCarreraDto;
    private List<MallaCurricularDTO> listaMallaDto;
    private List<AsignaturaDTO> source;
    private List<AsignaturaDTO> target;
    private List<AsignaturaDTO> targetAux;
    private DualListModel<AsignaturaDTO> listaAsignaturasDTO;

    private String usuario;
    private Long idSedeSeleccionada;
    private Long idSedeDistribucionSeleccionada;
    private String[] listaTipo;
    private String tipoLaboratorio;
    private String perfil;
    private String fase;
    private Boolean alertaEvaluador = false;
    private Boolean alertaFase = false;
    private Integer totalLaboratorios = 0;
    private Long idSedeIesSeleccionada;
    private Long idInformacionCarreraSeleccionada;
    private Long idMallaSeleccionada;

    public LaboratorioController() {

	iesDTO = new IesDTO();
	carreraSeleccionada = new CarreraIesDTO();
	informacionIes = new InformacionIesDTO();
	laboratorioSeleccionado = new LaboratorioDTO();
	faseEvaluacion = new FaseDTO();
	archivoSeleccionado = new Evidencia();
	faseIesDTO = new FaseIesDTO();
	laboratorioAsignaturaDto = new LaboratorioAsignaturaDTO();
	asignaturaDto = new AsignaturaDTO();

	listaSedeIesDto = new ArrayList<SedeIesDTO>();
	listaSedeDistribucion = new ArrayList<SedeIesDistribucionFisicaDTO>();
	listaLaboratorios = new ArrayList<LaboratorioDTO>();
	listaItemEvidencias = new ArrayList<ItemEvidenciaLocal>();
	listaMallaCurricular = new ArrayList<MallaCurricularDTO>();
	listaSedeIesAsignaturaDto = new ArrayList<SedeIesDTO>();
	listaInformacionCarreraDto = new ArrayList<InformacionCarreraDTO>();
	listaMallaDto = new ArrayList<MallaCurricularDTO>();
	source = new ArrayList<AsignaturaDTO>();
	target = new ArrayList<AsignaturaDTO>();
	listaAsignaturasDTO = new DualListModel<>();
	// listaAsignaturasDTO = new DualListModel<AsignaturaDTO>(source,
	// target);
	targetAux = new ArrayList<AsignaturaDTO>();
    }

    @PostConstruct
    public void start() {
	try {

	    ListaIesController controller = (ListaIesController) JsfUtil
		    .obtenerObjetoSesion("listaIesController");

	    this.usuario = SecurityContextHolder.getContext()
		    .getAuthentication().getName();

	    this.iesDTO = controller.getIes();
	    this.informacionIes = registroServicio
		    .obtenerInformacionIesPorIes(iesDTO);
	    this.faseIesDTO = controller.getFaseIesDTO();
	    this.perfil = controller.getPerfil().getNombre();

	    // this.perfil = "IES_EVALUADOR";
	    if (this.perfil.startsWith("IES_EVALUADOR")) {
		alertaEvaluador = true;
	    }
	    this.fase = this.faseIesDTO.getFaseDTO().getNombre();
	    // this.fase = "EVALUACION";
	    if (this.fase.startsWith("EVALUACION")) {
		alertaFase = true;
	    }

	    cargarTipoLaboratorio();
	    cargarLaboratorios();

	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}

    }

    public void cargarLaboratorios() {

	try {
	    this.listaLaboratorios.clear();
	    this.listaLaboratorios = registroServicio
		    .obtenerLaboratorioPorIes(informacionIes.getId());
	    this.totalLaboratorios = this.listaLaboratorios.size();
	} catch (Exception e) {
	    JsfUtil.msgError("Error al cargar los Laboratorios");
	}
    }

    private void cargarTipoLaboratorio() {
	listaTipo = new String[2];
	listaTipo[0] = TipoLaboratorioEnum.INFORMATICO.getValue();
	listaTipo[1] = TipoLaboratorioEnum.OTRO.getValue();
    }

    public void tomarTipoLaboratorio(ValueChangeEvent event) {
	this.tipoLaboratorio = (String) event.getNewValue();
    }

    public void nuevoLaboratorio() {
	laboratorioSeleccionado = new LaboratorioDTO();
	limpiarNuevoLaboratorioAsignatura();

	RequestContext context = RequestContext.getCurrentInstance();

	try {
	    cargarListasIes();
	    context.execute("dialogAdmin.show();");
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private void cargarListasIes() {
	listaSedeIesDto.clear();
	listaSedeIesDto = cargarSedeIes();
	listaSedeIesAsignaturaDto.clear();
	listaSedeIesAsignaturaDto = cargarSedeIes();
    }

    private void limpiarNuevoLaboratorioAsignatura() {
	tipoLaboratorio = null;
	idSedeIesSeleccionada = null;
	idSedeSeleccionada = null;
	idInformacionCarreraSeleccionada = null;
	idSedeDistribucionSeleccionada = null;
	idMallaSeleccionada = null;
	listaSedeIesAsignaturaDto.clear();
	listaInformacionCarreraDto.clear();
	listaMallaDto.clear();
	listaAsignaturasDTO.getSource().clear();
	listaAsignaturasDTO.getTarget().clear();
    }

    public List<SedeIesDTO> cargarSedeIes() {

	List<SedeIesDTO> sedesIesDto = new ArrayList<SedeIesDTO>();
	try {
	    sedesIesDto = registroServicio.obtenerSedesIes(informacionIes
		    .getId());
	    LOG.info("listaSedeIesDto.size: " + sedesIesDto.size());
	} catch (Exception e) {
	    JsfUtil.msgError("Error al cargar Sede IES");
	}
	return sedesIesDto;
    }

    public void cargarListaSedeDistribucion() {

	try {
	    this.listaSedeDistribucion.clear();
	    this.listaSedeDistribucion = registroServicio
		    .obtenerSedeDistribucionFisica(idSedeSeleccionada);
	} catch (Exception e) {
	    JsfUtil.msgError("Error al cargar Sede Distribución Física");
	}
    }

    public void guardarLaboratorio() {

	if (!validarDatosLaboratorios()) {
	    return;
	}

	if (listaAsignaturasDTO.getTarget().isEmpty()) {
	    JsfUtil.msgError("Seleccione al menos una asignatura");
	    return;
	}

	try {
	    if (laboratorioSeleccionado.getNombre() != null) {
		AuditoriaDTO audit = new AuditoriaDTO();
		audit.setFechaModificacion(new Date());
		audit.setUsuarioModificacion(usuario);
		laboratorioSeleccionado.setActivo(true);
		laboratorioSeleccionado.setAuditoria(audit);
		SedeIesDistribucionFisicaDTO campus = new SedeIesDistribucionFisicaDTO();

		campus.setId(idSedeDistribucionSeleccionada);

		laboratorioSeleccionado.setSedeDistribucionFisicaDTO(campus);
		laboratorioSeleccionado.setFaseIesDTO(this.faseIesDTO);

		// laboratorioSeleccionado.setTipo(TipoLaboratorioEnum
		// .valueOf(tipoLaboratorio));

		laboratorioSeleccionado.setTipo(TipoLaboratorioEnum
		        .parse(tipoLaboratorio));

		List<LaboratorioAsignaturaDTO> listaLaboratorioAsignatura = new ArrayList<LaboratorioAsignaturaDTO>();
		listaLaboratorioAsignatura = asignarLaboratorioAsignatura();

		laboratorioSeleccionado
		        .setLaboratorioAsignaturaDTO(listaLaboratorioAsignatura);

		laboratorioSeleccionado = registroServicio
		        .registrarLaboratorio(laboratorioSeleccionado);
		JsfUtil.msgInfo("Registro almacenado correctamente");
		limpiarDatos();
		cargarLaboratorios();
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("dialogAdmin.hide()");
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("No se pudo almacenar el registro");
	}
    }

    public void editarLaboratorio() {

	if (!validarDatosLaboratorios()) {
	    return;
	}

	// if (listaAsignaturasDTO.getTarget().isEmpty()) {
	// JsfUtil.msgError("Seleccione al menos una asignatura");
	// return;
	// }

	try {
	    if (laboratorioSeleccionado.getNombre() != null) {
		AuditoriaDTO audit = new AuditoriaDTO();
		audit.setFechaModificacion(new Date());
		audit.setUsuarioModificacion(usuario);
		laboratorioSeleccionado.setActivo(true);
		laboratorioSeleccionado.setAuditoria(audit);
		SedeIesDistribucionFisicaDTO campus = new SedeIesDistribucionFisicaDTO();
		SedeIesDTO sede = new SedeIesDTO();
		sede.setId(idSedeSeleccionada);
		campus.setSedeIes(sede);
		campus.setId(laboratorioSeleccionado
		        .getSedeDistribucionFisicaDTO().getId());
		campus.setNombre(laboratorioSeleccionado
		        .getSedeDistribucionFisicaDTO().getNombre());
		laboratorioSeleccionado.setSedeDistribucionFisicaDTO(campus);
		laboratorioSeleccionado.setFaseIesDTO(faseIesDTO);

		// laboratorioSeleccionado.setTipo(TipoLaboratorioEnum
		// .valueOf(tipoLaboratorio));

		laboratorioSeleccionado.setTipo(TipoLaboratorioEnum
		        .parse(tipoLaboratorio));

		List<LaboratorioAsignaturaDTO> listaLaboratorioAsignatura = new ArrayList<LaboratorioAsignaturaDTO>();
		listaLaboratorioAsignatura = asignarLaboratorioAsignatura();

		laboratorioSeleccionado
		        .setLaboratorioAsignaturaDTO(listaLaboratorioAsignatura);

		laboratorioSeleccionado = registroServicio
		        .registrarLaboratorio(laboratorioSeleccionado);
		obtenerAsignaturas();

		idSedeIesSeleccionada = null;
		idInformacionCarreraSeleccionada = null;
		listaInformacionCarreraDto.clear();
		idMallaSeleccionada = null;
		listaMallaDto.clear();
		listaAsignaturasDTO.getSource().clear();
		listaAsignaturasDTO.getTarget().clear();
		JsfUtil.msgInfo("Registro almacenado correctamente");
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("No se pudo almacenar el registro");
	}
    }

    public List<LaboratorioAsignaturaDTO> asignarLaboratorioAsignatura() {

	AuditoriaDTO auditoria = new AuditoriaDTO();
	auditoria.setFechaModificacion(new Date());
	auditoria.setUsuarioModificacion(usuario);

	List<LaboratorioAsignaturaDTO> listaLaboratorioAsignatura = new ArrayList<LaboratorioAsignaturaDTO>();
	List<AsignaturaDTO> lisAsignatura = new ArrayList<AsignaturaDTO>();
	lisAsignatura = listaAsignaturasDTO.getTarget();

	for (AsignaturaDTO asignaturaDto : lisAsignatura) {

	    LaboratorioAsignaturaDTO laboratorioAsignatura = new LaboratorioAsignaturaDTO();

	    laboratorioAsignatura.setAuditoria(auditoria);
	    laboratorioAsignatura.setAsignaturaDTO(asignaturaDto);
	    laboratorioAsignatura.setLaboratorioDTO(laboratorioSeleccionado);
	    laboratorioAsignatura.setActivo(true);
	    laboratorioAsignatura.setFaseIesDTO(faseIesDTO);
	    listaLaboratorioAsignatura.add(laboratorioAsignatura);
	}
	return listaLaboratorioAsignatura;
    }

    public void cancelarLaboratorio() {
	RequestContext context = RequestContext.getCurrentInstance();
	context.execute("dialogAdmin.hide();");
    }

    public void eliminarLaboratorio() {

	laboratorioSeleccionado.setActivo(false);
	AuditoriaDTO audit = new AuditoriaDTO();
	audit.setUsuarioModificacion(usuario);
	audit.setFechaModificacion(new Date());
	laboratorioSeleccionado.setAuditoria(audit);
	laboratorioSeleccionado.setFaseIesDTO(faseIesDTO);

	try {
	    registroServicio.registrarLaboratorio(laboratorioSeleccionado);

	    obtenerAsignaturas();

	    if (!laboratorioSeleccionado.getLaboratorioAsignaturaDTO()
		    .isEmpty()) {
		eliminarAsignaturaLista();
	    }

	    JsfUtil.msgInfo("Registro eliminado correctamente");
	    cargarLaboratorios();
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("No se pudo eliminar el registro");
	}
    }

    private void eliminarAsignaturaLista() {
	try {
	    AuditoriaDTO auditoria = new AuditoriaDTO();
	    auditoria.setFechaModificacion(new Date());
	    auditoria.setUsuarioModificacion(usuario);

	    for (LaboratorioAsignaturaDTO laboAsig : laboratorioSeleccionado
		    .getLaboratorioAsignaturaDTO()) {
		laboAsig.setActivo(false);
		laboAsig.setAuditoria(auditoria);
		registroServicio.registroLaboratorioAsignaturas(laboAsig);
	    }
	} catch (ServicioException e) {
	    e.printStackTrace();
	}
    }

    public void eliminarAsignatura() {
	try {
	    if (null == laboratorioAsignaturaDto.getId())
		return;

	    laboratorioAsignaturaDto.setActivo(false);
	    AuditoriaDTO auditoria = new AuditoriaDTO();
	    auditoria.setFechaModificacion(new Date());
	    auditoria.setUsuarioModificacion(usuario);
	    laboratorioAsignaturaDto.setAuditoria(auditoria);
	    registroServicio
		    .registroLaboratorioAsignaturas(laboratorioAsignaturaDto);

	    obtenerAsignaturas();

	    JsfUtil.msgInfo("Asignatura eliminada");

	} catch (ServicioException e) {
	    e.printStackTrace();
	}
    }

    public void limpiarDatos() {
	laboratorioSeleccionado = new LaboratorioDTO();
	idSedeSeleccionada = 0L;
	idSedeDistribucionSeleccionada = 0L;
    }

    public void cargarEdicion() {
	try {
	    idSedeIesSeleccionada = null;
	    idInformacionCarreraSeleccionada = null;
	    idMallaSeleccionada = null;
	    listaAsignaturasDTO.getSource().clear();
	    listaAsignaturasDTO.getTarget().clear();

	    cargarListasIes();

	    LOG.info("SEDE: "
		    + laboratorioSeleccionado.getSedeDistribucionFisicaDTO()
		            .getSedeIes().getId());

	    tipoLaboratorio = laboratorioSeleccionado.getTipo().getValue();

	    idSedeSeleccionada = laboratorioSeleccionado
		    .getSedeDistribucionFisicaDTO().getSedeIes().getId();

	    cargarListaSedeDistribucion();

	    idSedeDistribucionSeleccionada = laboratorioSeleccionado
		    .getSedeDistribucionFisicaDTO().getId();

	    LOG.info("DIS F: "
		    + laboratorioSeleccionado.getSedeDistribucionFisicaDTO()
		            .getId());

	    obtenerAsignaturas();

	} catch (ServicioException e) {
	    e.printStackTrace();
	}

    }

    private void obtenerAsignaturas() throws ServicioException {

	List<LaboratorioAsignaturaDTO> laboratorioAsignatura = new ArrayList<LaboratorioAsignaturaDTO>();
	laboratorioAsignatura = registroServicio
	        .obtenerAsignaturasPorLab(laboratorioSeleccionado.getId());
	laboratorioSeleccionado
	        .setLaboratorioAsignaturaDTO(laboratorioAsignatura);

    }

    public boolean validarDatosLaboratorios() {
	boolean valido = true;

	if (null == tipoLaboratorio || tipoLaboratorio.equals("")) {
	    JsfUtil.msgError("Seleccione el tipo de laboratorio");
	    return false;
	}

	if (null == laboratorioSeleccionado.getNombre()
	        || laboratorioSeleccionado.getNombre().equals("")) {
	    JsfUtil.msgError("Ingrese el nombre del laboratorio");
	    return false;
	}

	if (null == laboratorioSeleccionado.getUbicacion()
	        || laboratorioSeleccionado.getUbicacion().equals("")) {
	    JsfUtil.msgError("Ingrese la Ubicacion");
	    return false;
	}

	if (tipoLaboratorio.equals("INFORMÁTICO")) {
	    if (null == laboratorioSeleccionado.getNumComputadores()) {
		JsfUtil.msgError("Ingrese número de computadores");
		return false;
	    }
	}

	if (tipoLaboratorio.equals("OTRO")) {
	    if (null == laboratorioSeleccionado.getNumEquipos()
		    || laboratorioSeleccionado.getNumEquipos() == 0) {
		JsfUtil.msgError("Ingrese el número de equipos del laboratorios");
		return false;
	    }
	}

	if (null == laboratorioSeleccionado.getDescripcionEquipos()
	        || laboratorioSeleccionado.getDescripcionEquipos().equals("")) {
	    JsfUtil.msgError("Ingrese la descripción de los equipos del laboratorio");
	    return false;
	}

	if (null == idSedeSeleccionada || idSedeSeleccionada == 0L) {
	    JsfUtil.msgError("Seleccione Matriz/Extensión");
	    return false;
	}

	if (idSedeDistribucionSeleccionada == null
	        || idSedeDistribucionSeleccionada == 0L) {
	    JsfUtil.msgError("Seleccione Distribución Fisica");
	    return false;
	}

	if (alertaEvaluador) {
	    if (null == laboratorioSeleccionado.getObservacionEvaluador()
		    || laboratorioSeleccionado.getObservacionEvaluador()
		            .equals("")) {
		JsfUtil.msgError("Debe ingresar una observación");
		return false;
	    }
	}

	return valido;
    }

    public void cargarCarreras() {

	try {
	    if (null == idSedeIesSeleccionada) {
		return;
	    }

	    listaInformacionCarreraDto.clear();
	    listaInformacionCarreraDto = registroServicio
		    .obtenerInfCarreraPorSede(idSedeIesSeleccionada, null);
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    public void cargarMalla() {
	try {
	    if (null == idInformacionCarreraSeleccionada) {
		return;
	    }
	    listaMallaDto.clear();
	    InformacionCarreraDTO informacionCarreraDto = new InformacionCarreraDTO();
	    informacionCarreraDto.setId(idInformacionCarreraSeleccionada);
	    listaMallaDto = registroServicio
		    .obtenerMallaCurricular(informacionCarreraDto);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void cargarAsignaturas() {

	try {
	    if (null == idMallaSeleccionada)
		return;

	    // source.clear();
	    source = registroServicio
		    .obtenerAsignaturasPorMalla(idMallaSeleccionada);

	    target = listaAsignaturasDTO.getTarget();

	    if (null == target)
		target = new ArrayList<AsignaturaDTO>();

	    listaAsignaturasDTO = new DualListModel<AsignaturaDTO>(source,
		    target);
	} catch (ServicioException e) {
	    e.printStackTrace();
	}
    }

    public void onTransfer(TransferEvent event) {
	StringBuilder builder = new StringBuilder();

	for (Object item : event.getItems()) {
	    LOG.info("clase: " + item.getClass());
	    String nombre = ((AsignaturaDTO) item).getNombre();
	    LOG.info("nomb: " + nombre);
	    builder.append(((AsignaturaDTO) item).getNombre()).append("<br />");

	    LOG.info("nombre: " + builder.toString());
	    AsignaturaDTO asignatura = (AsignaturaDTO) item;
	    targetAux.add(asignatura);
	}

	LOG.info("lista temporal: " + targetAux.size());

    }

    public void mostrarLista() {

	if (listaAsignaturasDTO.getSource().isEmpty()) {
	    JsfUtil.msgError("No hay entrada");

	}

	if (listaAsignaturasDTO.getTarget().isEmpty()) {
	    JsfUtil.msgError("No hay salida");

	}

	LOG.info("Lista Entrada: " + listaAsignaturasDTO.getSource().size());

	LOG.info("Lista Salida:" + listaAsignaturasDTO.getTarget().size());

    }

    public String getUsuario() {
	return usuario;
    }

    public void setUsuario(String usuario) {
	this.usuario = usuario;
    }

    public CarreraIesDTO getCarreraSeleccionada() {
	return carreraSeleccionada;
    }

    public void setCarreraSeleccionada(CarreraIesDTO carreraSeleccionada) {
	this.carreraSeleccionada = carreraSeleccionada;
    }

    public InformacionIesDTO getInformacionIes() {
	return informacionIes;
    }

    public void setInformacionIes(InformacionIesDTO informacionIes) {
	this.informacionIes = informacionIes;
    }

    public List<ItemEvidenciaLocal> getListaItemEvidencias() {
	return listaItemEvidencias;
    }

    public void setListaItemEvidencias(
	    List<ItemEvidenciaLocal> listaItemEvidencias) {
	this.listaItemEvidencias = listaItemEvidencias;
    }

    public FaseDTO getFaseEvaluacion() {
	return faseEvaluacion;
    }

    public void setFaseEvaluacion(FaseDTO faseEvaluacion) {
	this.faseEvaluacion = faseEvaluacion;
    }

    public Evidencia getArchivoSeleccionado() {
	return archivoSeleccionado;
    }

    public void setArchivoSeleccionado(Evidencia archivoSeleccionado) {
	this.archivoSeleccionado = archivoSeleccionado;
    }

    public Long getIdSedeSeleccionada() {
	return idSedeSeleccionada;
    }

    public void setIdSedeSeleccionada(Long idSedeSeleccionada) {
	this.idSedeSeleccionada = idSedeSeleccionada;
    }

    public Long getIdSedeDistribucionSeleccionada() {
	return idSedeDistribucionSeleccionada;
    }

    public void setIdSedeDistribucionSeleccionada(
	    Long idSedeDistribucionSeleccionada) {
	this.idSedeDistribucionSeleccionada = idSedeDistribucionSeleccionada;
    }

    public List<SedeIesDTO> getListaSedeIesDto() {
	return listaSedeIesDto;
    }

    public void setListaSedeIesDto(List<SedeIesDTO> listaSedeIesDto) {
	this.listaSedeIesDto = listaSedeIesDto;
    }

    public IesDTO getIesDTO() {
	return iesDTO;
    }

    public void setIesDTO(IesDTO iesDTO) {
	this.iesDTO = iesDTO;
    }

    public List<SedeIesDistribucionFisicaDTO> getListaSedeDistribucion() {
	return listaSedeDistribucion;
    }

    public void setListaSedeDistribucion(
	    List<SedeIesDistribucionFisicaDTO> listaSedeDistribucion) {
	this.listaSedeDistribucion = listaSedeDistribucion;
    }

    public FaseIesDTO getFaseIesDTO() {
	return faseIesDTO;
    }

    public void setFaseIesDTO(FaseIesDTO faseIesDTO) {
	this.faseIesDTO = faseIesDTO;
    }

    public LaboratorioDTO getLaboratorioSeleccionado() {
	return laboratorioSeleccionado;
    }

    public void setLaboratorioSeleccionado(
	    LaboratorioDTO laboratorioSeleccionado) {
	this.laboratorioSeleccionado = laboratorioSeleccionado;
    }

    public List<LaboratorioDTO> getListaLaboratorios() {
	return listaLaboratorios;
    }

    public void setListaLaboratorios(List<LaboratorioDTO> listaLaboratorios) {
	this.listaLaboratorios = listaLaboratorios;
    }

    public String getTipoLaboratorio() {
	return tipoLaboratorio;
    }

    public void setTipoLaboratorio(String tipoLaboratorio) {
	this.tipoLaboratorio = tipoLaboratorio;
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

    public DualListModel<AsignaturaDTO> getListaAsignaturasDTO() {
	return listaAsignaturasDTO;
    }

    public void setListaAsignaturasDTO(
	    DualListModel<AsignaturaDTO> listaAsignaturasDTO) {
	this.listaAsignaturasDTO = listaAsignaturasDTO;
    }

    public List<MallaCurricularDTO> getListaMallaCurricular() {
	return listaMallaCurricular;
    }

    public void setListaMallaCurricular(
	    List<MallaCurricularDTO> listaMallaCurricular) {
	this.listaMallaCurricular = listaMallaCurricular;
    }

    public Long getIdSedeIesSeleccionada() {
	return idSedeIesSeleccionada;
    }

    public void setIdSedeIesSeleccionada(Long idSedeIesSeleccionada) {
	this.idSedeIesSeleccionada = idSedeIesSeleccionada;
    }

    public List<SedeIesDTO> getListaSedeIesAsignaturaDto() {
	return listaSedeIesAsignaturaDto;
    }

    public void setListaSedeIesAsignaturaDto(
	    List<SedeIesDTO> listaSedeIesAsignaturaDto) {
	this.listaSedeIesAsignaturaDto = listaSedeIesAsignaturaDto;
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

    public List<MallaCurricularDTO> getListaMallaDto() {
	return listaMallaDto;
    }

    public void setListaMallaDto(List<MallaCurricularDTO> listaMallaDto) {
	this.listaMallaDto = listaMallaDto;
    }

    public Long getIdMallaSeleccionada() {
	return idMallaSeleccionada;
    }

    public void setIdMallaSeleccionada(Long idMallaSeleccionada) {
	this.idMallaSeleccionada = idMallaSeleccionada;
    }

    public List<AsignaturaDTO> getSource() {
	return source;
    }

    public void setSource(List<AsignaturaDTO> source) {
	this.source = source;
    }

    public List<AsignaturaDTO> getTarget() {
	return target;
    }

    public void setTarget(List<AsignaturaDTO> target) {
	this.target = target;
    }

    public List<AsignaturaDTO> getTargetAux() {
	return targetAux;
    }

    public void setTargetAux(List<AsignaturaDTO> targetAux) {
	this.targetAux = targetAux;
    }

    public AsignaturaDTO getAsignaturaDto() {
	return asignaturaDto;
    }

    public void setAsignaturaDto(AsignaturaDTO asignaturaDto) {
	this.asignaturaDto = asignaturaDto;
    }

    public LaboratorioAsignaturaDTO getLaboratorioAsignaturaDto() {
	return laboratorioAsignaturaDto;
    }

    public void setLaboratorioAsignaturaDto(
	    LaboratorioAsignaturaDTO laboratorioAsignaturaDto) {
	this.laboratorioAsignaturaDto = laboratorioAsignaturaDto;
    }

    public String[] getListaTipo() {
	return listaTipo;
    }

    public void setListaTipo(String[] listaTipo) {
	this.listaTipo = listaTipo;
    }

    public Integer getTotalLaboratorios() {
	return totalLaboratorios;
    }

    public void setTotalLaboratorios(Integer totalLaboratorios) {
	this.totalLaboratorios = totalLaboratorios;
    }

    public Boolean getAlertaFase() {
	return alertaFase;
    }

    public void setAlertaFase(Boolean alertaFase) {
	this.alertaFase = alertaFase;
    }

}