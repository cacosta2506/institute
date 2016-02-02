package ec.gob.ceaaces.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.event.ValueChangeEvent;

import org.primefaces.context.RequestContext;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import ec.gob.ceaaces.catalogo.dtos.catalogos.AuditoriaDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.IesDTO;
import ec.gob.ceaaces.catalogo.exceptions.ServicioException;
import ec.gob.ceaaces.institutos.dtos.evaluacion.DocenteDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.DocenteProduccionDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaConceptoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ProduccionDTO;
import ec.gob.ceaaces.institutos.dtos.historico.ProduccionHistoricoDTO;
import ec.gob.ceaaces.institutos.enumeraciones.TipoProduccionEnum;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.EvaluacionServicio;
import ec.gob.ceaaces.services.InstitutosServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.ArchivoUtil;
import ec.gob.ceaaces.util.JsfUtil;

@ManagedBean(name = "produccionController")
public class AdministrarProduccionesController implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger
	    .getLogger(AdministrarProduccionesController.class.getSimpleName());

    @Autowired
    private RegistroServicio registroServicio;

    @Autowired
    private CatalogoServicio catalogoServicio;

    @Autowired
    private InstitutosServicio institutoServicio;

    @Autowired
    private EvaluacionServicio evaluacionServicio;

    private IesDTO iesDTO;
    private InformacionIesDTO informacionIes;
    private ProduccionDTO produccionDTO;
    private ProduccionDTO produccionSeleccionada;
    private DocenteProduccionDTO docenteProduccion;
    private FaseDTO faseEvaluacion;
    private FaseIesDTO faseIesDTO;
    private DocenteDTO docenteDTO;

    private List<ProduccionDTO> listaProduccionDTO;
    private List<ProduccionHistoricoDTO> listaProduccionHistorico;
    private List<DocenteDTO> listaDocentes;
    private List<DocenteDTO> listaDocentesFiltro = new ArrayList<DocenteDTO>();
    private List<DocenteProduccionDTO> listaDocenteProduccionDTO;
    private List<DocenteProduccionDTO> vistaParticipaciones;
    private List<DocenteDTO> listaDocenteDto;

    private String usuario;
    private String perfil;
    private String fase;
    private String accion;
    private List<String> tipoProduccionEnum;
    private String tipoProduccionSeleccionado;
    private Boolean alertaEvaluador = false;
    private Boolean alertaFase = false;
    private Boolean alertaUsuarioIes = false;
    private Boolean alertaFaseRectificacion = false;
    private Integer totalProducciones = 0;

    private List<EvidenciaConceptoDTO> listaEvidenciaConceptoDto;
    private Long idEvidencia;
    private String fichero;
    private String url;
    private String extensionDocumento;
    private Boolean descargar = false;
    private StreamedContent documentoDescarga;

    public AdministrarProduccionesController() {
	iesDTO = new IesDTO();
	informacionIes = new InformacionIesDTO();
	produccionDTO = new ProduccionDTO();
	produccionDTO.setPremio(false);
	docenteProduccion = new DocenteProduccionDTO();
	faseEvaluacion = new FaseDTO();
	faseIesDTO = new FaseIesDTO();
	docenteDTO = new DocenteDTO();
	produccionSeleccionada = new ProduccionDTO();
	listaProduccionHistorico = new ArrayList<ProduccionHistoricoDTO>();
	listaProduccionDTO = new ArrayList<ProduccionDTO>();
	listaDocentes = new ArrayList<DocenteDTO>();
	listaDocenteProduccionDTO = new ArrayList<DocenteProduccionDTO>();
	listaDocenteDto = new ArrayList<DocenteDTO>();
	vistaParticipaciones = new ArrayList<DocenteProduccionDTO>();
	this.tipoProduccionEnum = new ArrayList<>();
	listaEvidenciaConceptoDto = new ArrayList<EvidenciaConceptoDTO>();

    }

    @PostConstruct
    public void start() {
	obtenerInformacionIes();

    }

    public void obtenerInformacionIes() {
	try {
	    ListaIesController controller = (ListaIesController) JsfUtil
		    .obtenerObjetoSesion("listaIesController");

	    alertaEvaluador = false;
	    alertaUsuarioIes = false;
	    alertaFaseRectificacion = false;

	    this.usuario = SecurityContextHolder.getContext()
		    .getAuthentication().getName();

	    this.perfil = controller.getPerfil().getNombre();
	    // obtener faseIes
	    this.faseIesDTO = controller.getFaseIesDTO();

	    // this.perfil = "IES_EVALUADOR";
	    if (this.perfil.startsWith("IES_EVALUADOR")) {
		alertaEvaluador = true;
	    }
	    this.fase = this.faseIesDTO.getFaseDTO().getNombre();
	    // this.fase = "EVALUACION";
	    if (this.fase.startsWith("EVALUACION")) {
		alertaFase = true;
	    }

	    if (this.perfil.startsWith("IES_USUARIO")) {
		alertaUsuarioIes = true;
	    }

	    if (this.faseIesDTO.getTipo().name().startsWith("RECTIFICACION")) {
		alertaFaseRectificacion = true;
	    }

	    iesDTO = controller.getIes();

	    this.informacionIes = registroServicio
		    .obtenerInformacionIesPorIes(iesDTO);

	    obtenerProducciones();

	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}

    }

    public void obtenerHistoricoProduccion(ProduccionDTO produccion) {
	listaProduccionHistorico.clear();
	try {
	    listaProduccionHistorico = evaluacionServicio
		    .obtenerProduccionHistorico(produccion.getId());
	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void nuevaProduccion() {
	accion = "nuevo";
	produccionDTO = new ProduccionDTO();
	produccionDTO.setPremio(false);
	docenteDTO = new DocenteDTO();
	listaDocenteProduccionDTO = new ArrayList<>();
	vistaParticipaciones = new ArrayList<>();
	obtenerDocentes();
	RequestContext context = RequestContext.getCurrentInstance();
	context.execute("dialogAdministracion.show();");
    }

    public void obtenerDocentes() {
	DocenteDTO docenteDTO = null;
	try {
	    listaDocentes.clear();
	    listaDocentes = registroServicio.obtenerDocentesPorInformacionIes(
		    docenteDTO, informacionIes.getId());
	    LOG.info("Total docentes:" + listaDocentes.size());
	} catch (ServicioException e) {
	    e.printStackTrace();
	}
    }

    public void recargarProducciones() {
	try {
	    listaProduccionDTO = registroServicio
		    .obtenerProducciones(informacionIes.getId());
	} catch (ServicioException e) {
	    LOG.info(e.getLocalizedMessage());
	}
    }

    public void editarProduccion(Long idProduccion) {
	accion = "editar";
	docenteDTO = new DocenteDTO();
	try {

	    if (alertaFaseRectificacion && alertaUsuarioIes) {
		if (produccionDTO.getId() != null
		        && !produccionDTO.getFaseIesDTO().getTipo().name()
		                .startsWith("RECTIFICACION")) {
		    JsfUtil.msgError("No se puede editar este producción en fase de Rectificación.");
		    return;
		}
	    }
	    obtenerDocentes();
	    produccionDTO = registroServicio
		    .obtenerProduccionPorId(idProduccion);

	    tipoProduccionSeleccionado = produccionDTO.getTipo().getValue();
	    listaDocenteProduccionDTO.clear();
	    listaDocenteProduccionDTO = produccionDTO
		    .getListaDocenteProduccionDTO();
	    LOG.info("total docentes: " + listaDocenteProduccionDTO.size());
	    RequestContext context = RequestContext.getCurrentInstance();
	    context.execute("dialogAdministracionEditar.show();");

	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al cargar publicación");
	}
    }

    /**
     * Setea las publicaciones con sus respectivos hijos en true para poder
     * eliminar solo aquellas
     */
    public void comprobarProduccion(Long idProduccion) {
	boolean tieneHijos = false;

	try {

	    if (alertaFaseRectificacion && alertaUsuarioIes) {
		if (produccionDTO.getId() != null
		        && !produccionDTO.getFaseIesDTO().getTipo().name()
		                .startsWith("RECTIFICACION")) {
		    JsfUtil.msgError("No se puede eliminar esta producción en fase de Rectificación.");
		    return;
		}
	    }

	    produccionDTO = registroServicio
		    .obtenerProduccionPorId(idProduccion);
	    List<DocenteProduccionDTO> listaDocenteProduccionDTO = new ArrayList<DocenteProduccionDTO>();
	    listaDocenteProduccionDTO = produccionDTO
		    .getListaDocenteProduccionDTO();

	    if (listaDocenteProduccionDTO == null)
		return;

	    if (listaDocenteProduccionDTO.isEmpty()) {
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("deleteConfirmation.show()");
	    } else {
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("deleteConfirmation2.show()");
	    }

	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al eliminar publicación");
	}
    }

    public void eliminarProducciones() {

	try {
	    AuditoriaDTO auditoriaDto = new AuditoriaDTO();
	    auditoriaDto.setUsuarioModificacion(usuario);
	    auditoriaDto.setFechaModificacion(new Date());
	    produccionDTO.setAuditoria(auditoriaDto);
	    produccionDTO.setActivo(false);
	    produccionDTO = registroServicio.registrarProduccion(produccionDTO);

	    recargarProducciones();
	    JsfUtil.msgInfo("Registro eliminado correctamente");
	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al eliminar Producciones");
	} catch (Exception e) {

	    JsfUtil.msgError("Error al eliminar, consulte al administrador."
		    + e.getMessage());
	    e.printStackTrace();
	}
    }

    public void eliminarProduccionesParticipantes() {

	try {
	    AuditoriaDTO auditoriaDto = new AuditoriaDTO();
	    auditoriaDto.setUsuarioModificacion(usuario);
	    auditoriaDto.setFechaModificacion(new Date());
	    produccionDTO.setAuditoria(auditoriaDto);
	    produccionDTO.setActivo(false);

	    for (DocenteProduccionDTO docentePro : produccionDTO
		    .getListaDocenteProduccionDTO()) {
		AuditoriaDTO auditoria = new AuditoriaDTO();
		auditoria.setUsuarioModificacion(usuario);
		auditoria.setFechaModificacion(new Date(Calendar.getInstance(
		        TimeZone.getDefault()).getTimeInMillis()));
		docentePro.setAuditoriaDTO(auditoria);
		docentePro.setActivo(false);
		docentePro.setProduccionDTO(produccionDTO);
		docentePro = registroServicio
		        .registroDocenteProduccion(docentePro);
	    }
	    produccionDTO = registroServicio.registrarProduccion(produccionDTO);
	    recargarProducciones();
	    JsfUtil.msgInfo("Registro eliminado correctamente");
	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Error al eliminar Producciones");
	} catch (Exception e) {
	    JsfUtil.msgError("Error al eliminar, consulte al administrador."
		    + e.getMessage());
	    e.printStackTrace();
	}
    }

    public void obtenerProducciones() {
	this.tipoProduccionEnum.clear();
	this.listaProduccionDTO.clear();

	try {

	    for (TipoProduccionEnum tipoP : TipoProduccionEnum.values()) {
		this.tipoProduccionEnum.add(tipoP.getValue());
	    }

	    listaProduccionDTO = registroServicio
		    .obtenerProducciones(informacionIes.getId());
	    this.totalProducciones = this.listaProduccionDTO.size();
	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public void guardarProduccion() {

	if (!validarDatos(produccionDTO)) {
	    return;
	}

	if (listaDocenteProduccionDTO.isEmpty()) {
	    JsfUtil.msgAdvert("Debe ingresar por lo menos un participante");
	    return;
	}

	AuditoriaDTO auditoria = new AuditoriaDTO();
	auditoria.setUsuarioModificacion(usuario);
	auditoria.setFechaModificacion(new Date(Calendar.getInstance(
	        TimeZone.getDefault()).getTimeInMillis()));

	produccionDTO.setAuditoria(auditoria);
	produccionDTO.setActivo(true);
	// produccionDTO.setAceptadoEvaluador(true);
	produccionDTO.setInformacionIesDTO(informacionIes);
	produccionDTO.setFaseIesDTO(faseIesDTO);

	produccionDTO.setTipo(TipoProduccionEnum
	        .parse(this.tipoProduccionSeleccionado));

	produccionDTO.setListaDocenteProduccionDTO(listaDocenteProduccionDTO);

	try {
	    produccionDTO = registroServicio.registrarProduccion(produccionDTO);
	} catch (ServicioException e) {
	    JsfUtil.msgError("No se pudo almacenar la producción: "
		    + e.getMessage());
	} catch (Exception e) {
	    JsfUtil.msgError("Error al guardar, consulte al administrador.");
	    e.printStackTrace();
	}
	if (null != produccionDTO.getId()) {
	    JsfUtil.msgInfo("Registro almacenado correctamente");
	    limpiarDatos();
	    obtenerProducciones();
	    RequestContext context = RequestContext.getCurrentInstance();
	    context.execute("dialogAdministracion.hide()");
	}

    }

    public void guardarProduccionEditada() {

	if (!validarDatos(produccionDTO)) {
	    return;
	}

	AuditoriaDTO auditoria = new AuditoriaDTO();
	auditoria.setUsuarioModificacion(usuario);
	auditoria.setFechaModificacion(new Date());
	produccionDTO.setAuditoria(auditoria);

	produccionDTO.setActivo(true);
	// produccionDTO.setAceptadoEvaluador(true);
	LOG.info("ACEPTADO EVL: " + produccionDTO.getAceptadoEvaluador());
	LOG.info("OBSER EVL: " + produccionDTO.getObservacionEvaluador());
	produccionDTO.setInformacionIesDTO(informacionIes);
	produccionDTO.setFaseIesDTO(faseIesDTO);
	if (alertaEvaluador) {
	    produccionDTO.setVerificarEvidencia(false);
	}
	produccionDTO.setTipo(TipoProduccionEnum
	        .parse(this.tipoProduccionSeleccionado));

	produccionDTO.getListaDocenteProduccionDTO().clear();

	try {
	    produccionDTO = registroServicio.registrarProduccion(produccionDTO);
	} catch (ServicioException e) {
	    JsfUtil.msgError("No se pudo almacenar la producción: "
		    + e.getMessage());
	} catch (Exception e) {
	    JsfUtil.msgError("Error al guardar, consulte al administrador."
		    + e.getMessage());
	    e.printStackTrace();
	}
	if (null != produccionDTO.getId()) {
	    JsfUtil.msgInfo("Registro editado correctamente");
	    limpiarDatos();
	    obtenerProducciones();
	    RequestContext context = RequestContext.getCurrentInstance();
	    context.execute("dialogAdministracionEditar.hide()");
	}
    }

    public boolean validarDatos(ProduccionDTO produccion) {

	// LOG.info("OBERVA E." +
	// produccion.getObservacionEvaluador());
	if (null == produccion.getNombre() || produccion.getNombre().equals("")) {
	    JsfUtil.msgError("Ingrese el Nombre Produccion");
	    return false;
	}

	if (null == tipoProduccionSeleccionado
	        || tipoProduccionSeleccionado.equals("")) {
	    JsfUtil.msgError("Seleccione el tipo");
	    return false;
	}

	if (null == produccion.getFechaPresentacion()) {
	    JsfUtil.msgError("Ingrese la Fecha de Presentación");
	    return false;
	}

	if (null == produccion.getPremio())
	    produccion.setPremio(false);

	if (produccion.getPremio() == true) {

	    if (null == produccion.getTipoPremio()
		    || produccion.getTipoPremio().equals("")) {
		JsfUtil.msgError("Ingrese tipo de premios");
		return false;
	    }

	    if (null == produccion.getNombrePremio()
		    || produccion.getNombrePremio().equals("")) {
		JsfUtil.msgError("Ingrese los premios recibidos");
		return false;
	    }

	} else {
	    produccion.setTipoPremio("");
	    produccion.setNombrePremio("");
	}

	return true;

    }

    public void limpiarDatos() {

	produccionDTO = new ProduccionDTO();
	listaDocenteProduccionDTO.clear();
	listaDocenteDto.clear();
    }

    public void agregarListaParticipantes() {
	if (!validarDatosParticipante(docenteDTO)) {
	    return;
	}

	for (int i = 0; i < vistaParticipaciones.size(); i++) {
	    if (vistaParticipaciones.get(i).getDocenteDTO().getId()
		    .equals(docenteDTO.getId())) {
		vistaParticipaciones.remove(i);
		break;
	    }
	}

	DocenteProduccionDTO docente = new DocenteProduccionDTO();

	docente.setDocenteDTO(docenteDTO);

	AuditoriaDTO auditoria = new AuditoriaDTO();
	auditoria.setUsuarioModificacion(usuario);
	auditoria.setFechaModificacion(new Date(Calendar.getInstance(
	        TimeZone.getDefault()).getTimeInMillis()));
	docente.setAuditoriaDTO(auditoria);

	docente.setActivo(true);

	vistaParticipaciones.add(docente);

	docenteDTO = new DocenteDTO();
	listaDocenteProduccionDTO.clear();
	listaDocenteProduccionDTO.addAll(vistaParticipaciones);
	JsfUtil.msgInfo("Participante agregado, No se olvide de Guardar toda la información");
    }

    public void guardarParticipantes() {
	try {

	    if (!validarDatosParticipante(docenteDTO)) {
		return;
	    }

	    DocenteProduccionDTO docente = new DocenteProduccionDTO();
	    docente.setDocenteDTO(docenteDTO);
	    AuditoriaDTO auditoria = new AuditoriaDTO();
	    auditoria.setUsuarioModificacion(usuario);
	    auditoria.setFechaModificacion(new Date(Calendar.getInstance(
		    TimeZone.getDefault()).getTimeInMillis()));
	    docente.setAuditoriaDTO(auditoria);
	    docente.setActivo(true);
	    docente.setProduccionDTO(produccionDTO);
	    docente = registroServicio.registroDocenteProduccion(docente);
	    docenteDTO = new DocenteDTO();
	    JsfUtil.msgInfo("Participante Guardado");
	    editarProduccion(produccionDTO.getId());
	} catch (ServicioException e) {
	    e.printStackTrace();
	}
    }

    public void eliminarParticipante() {
	try {
	    DocenteProduccionDTO docentePro = new DocenteProduccionDTO();
	    docentePro = docenteProduccion;
	    AuditoriaDTO auditoria = new AuditoriaDTO();
	    auditoria.setUsuarioModificacion(usuario);
	    auditoria.setFechaModificacion(new Date(Calendar.getInstance(
		    TimeZone.getDefault()).getTimeInMillis()));
	    docentePro.setAuditoriaDTO(auditoria);
	    docentePro.setActivo(false);
	    docentePro.setProduccionDTO(produccionDTO);
	    docentePro = registroServicio.registroDocenteProduccion(docentePro);
	} catch (ServicioException e) {
	    e.printStackTrace();
	}
	docenteDTO = new DocenteDTO();
	JsfUtil.msgInfo("Participante Eliminado");
	editarProduccion(produccionDTO.getId());
    }

    public boolean validarDatosParticipante(DocenteDTO docent) {

	if (null == docent.getId()) {
	    JsfUtil.msgError("Seleccione un Docente");
	    return false;
	}

	for (DocenteProduccionDTO participacion : listaDocenteProduccionDTO) {
	    if (participacion.getDocenteDTO().getId()
		    .equals(docenteDTO.getId())) {
		JsfUtil.msgError("El docente ya ha sido asignado");
		return false;
	    }
	}

	return true;
    }

    public void cargarEvidencia() {

	try {
	    idEvidencia = produccionDTO.getId();
	    LOG.info("id produccion: " + idEvidencia.toString());
	    listaEvidenciaConceptoDto.clear();
	    listaEvidenciaConceptoDto = institutoServicio
		    .obtenerEvidenciasDeIesPorIdConceptoEIdTabla(
		            iesDTO.getId(), 8L, idEvidencia, "producciones");
	} catch (ServicioException e) {
	    e.printStackTrace();
	}
    }

    public void enviarEvidencia(EvidenciaDTO evidencia) {
	if (null == evidencia)
	    return;

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

    public void tomarTipoProduccion(ValueChangeEvent event) {
	this.tipoProduccionSeleccionado = (String) event.getNewValue();
    }

    public void tomarDocente(DocenteDTO doc) {
	this.docenteDTO = doc;
	LOG.info("NOMBRE DOC: " + doc.getNombres());
    }

    public String getUsuario() {
	return usuario;
    }

    public void setUsuario(String usuario) {
	this.usuario = usuario;
    }

    public String getPerfil() {
	return perfil;
    }

    public void setPerfil(String perfil) {
	this.perfil = perfil;
    }

    public InformacionIesDTO getInformacionIes() {
	return informacionIes;
    }

    public void setInformacionIes(InformacionIesDTO informacionIes) {
	this.informacionIes = informacionIes;
    }

    public FaseDTO getFaseEvaluacion() {
	return faseEvaluacion;
    }

    public void setFaseEvaluacion(FaseDTO faseEvaluacion) {
	this.faseEvaluacion = faseEvaluacion;
    }

    public String getAccion() {
	return accion;
    }

    public void setAccion(String accion) {
	this.accion = accion;
    }

    public FaseIesDTO getFaseIesDTO() {
	return faseIesDTO;
    }

    public void setFaseIesDTO(FaseIesDTO faseIesDTO) {
	this.faseIesDTO = faseIesDTO;
    }

    public ProduccionDTO getProduccionDTO() {
	return produccionDTO;
    }

    public void setProduccionDTO(ProduccionDTO produccionDTO) {
	this.produccionDTO = produccionDTO;
    }

    public List<ProduccionDTO> getListaProduccionDTO() {
	return listaProduccionDTO;
    }

    public void setListaProduccionDTO(List<ProduccionDTO> listaProduccionDTO) {
	this.listaProduccionDTO = listaProduccionDTO;
    }

    public List<DocenteDTO> getListaDocentes() {
	return listaDocentes;
    }

    public void setListaDocentes(List<DocenteDTO> listaDocentes) {
	this.listaDocentes = listaDocentes;
    }

    public List<DocenteProduccionDTO> getListaDocenteProduccionDTO() {
	return listaDocenteProduccionDTO;
    }

    public void setListaDocenteProduccionDTO(
	    List<DocenteProduccionDTO> listaDocenteProduccionDTO) {
	this.listaDocenteProduccionDTO = listaDocenteProduccionDTO;
    }

    public DocenteDTO getDocenteDTO() {
	return docenteDTO;
    }

    public void setDocenteDTO(DocenteDTO docenteDTO) {
	this.docenteDTO = docenteDTO;
    }

    public Date getFechaActual() {
	return new Date();
    }

    public List<DocenteProduccionDTO> getVistaParticipaciones() {
	return vistaParticipaciones;
    }

    public void setVistaParticipaciones(
	    List<DocenteProduccionDTO> vistaParticipaciones) {
	this.vistaParticipaciones = vistaParticipaciones;
    }

    public List<DocenteDTO> getListaDocenteDto() {
	return listaDocenteDto;
    }

    public void setListaDocenteDto(List<DocenteDTO> listaDocenteDto) {
	this.listaDocenteDto = listaDocenteDto;
    }

    public List<String> getTipoProduccionEnum() {
	return tipoProduccionEnum;
    }

    public void setTipoProduccionEnum(List<String> tipoProduccionEnum) {
	this.tipoProduccionEnum = tipoProduccionEnum;
    }

    public String getTipoProduccionSeleccionado() {
	return tipoProduccionSeleccionado;
    }

    public void setTipoProduccionSeleccionado(String tipoProduccionSeleccionado) {
	this.tipoProduccionSeleccionado = tipoProduccionSeleccionado;
    }

    public DocenteProduccionDTO getDocenteProduccion() {
	return docenteProduccion;
    }

    public void setDocenteProduccion(DocenteProduccionDTO docenteProduccion) {
	this.docenteProduccion = docenteProduccion;
    }

    public Boolean getAlertaEvaluador() {
	return alertaEvaluador;
    }

    public void setAlertaEvaluador(Boolean alertaEvaluador) {
	this.alertaEvaluador = alertaEvaluador;
    }

    public List<EvidenciaConceptoDTO> getListaEvidenciaConceptoDto() {
	return listaEvidenciaConceptoDto;
    }

    public void setListaEvidenciaConceptoDto(
	    List<EvidenciaConceptoDTO> listaEvidenciaConceptoDto) {
	this.listaEvidenciaConceptoDto = listaEvidenciaConceptoDto;
    }

    public Long getIdEvidencia() {
	return idEvidencia;
    }

    public void setIdEvidencia(Long idEvidencia) {
	this.idEvidencia = idEvidencia;
    }

    public String getFichero() {
	return fichero;
    }

    public void setFichero(String fichero) {
	this.fichero = fichero;
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

    public Boolean getDescargar() {
	return descargar;
    }

    public void setDescargar(Boolean descargar) {
	this.descargar = descargar;
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

    public IesDTO getIesDTO() {
	return iesDTO;
    }

    public void setIesDTO(IesDTO iesDTO) {
	this.iesDTO = iesDTO;
    }

    public Integer getTotalProducciones() {
	return totalProducciones;
    }

    public void setTotalProducciones(Integer totalProducciones) {
	this.totalProducciones = totalProducciones;
    }

    public Boolean getAlertaFase() {
	return alertaFase;
    }

    public void setAlertaFase(Boolean alertaFase) {
	this.alertaFase = alertaFase;
    }

    public ProduccionDTO getProduccionSeleccionada() {
	return produccionSeleccionada;
    }

    public void setProduccionSeleccionada(ProduccionDTO produccionSeleccionada) {
	this.produccionSeleccionada = produccionSeleccionada;
    }

    public List<ProduccionHistoricoDTO> getListaProduccionHistorico() {
	return listaProduccionHistorico;
    }

    public void setListaProduccionHistorico(
	    List<ProduccionHistoricoDTO> listaProduccionHistorico) {
	this.listaProduccionHistorico = listaProduccionHistorico;
    }

    public List<DocenteDTO> getListaDocentesFiltro() {
	return listaDocentesFiltro;
    }

    public void setListaDocentesFiltro(List<DocenteDTO> listaDocentesFiltro) {
	this.listaDocentesFiltro = listaDocentesFiltro;
    }

}
