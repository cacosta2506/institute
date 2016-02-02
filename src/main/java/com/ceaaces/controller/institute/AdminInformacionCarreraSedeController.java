package ec.gob.ceaaces.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.primefaces.context.RequestContext;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import ec.gob.ceaaces.catalogo.dtos.catalogos.AreaConocimientoDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.AuditoriaDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.CarreraIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.ConceptoDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.GradoTituloDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.IesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.SubAreaConocimientoDTO;
import ec.gob.ceaaces.catalogo.enumeraciones.EstadoCarreraEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.GrupoConceptoEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.ModalidadEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.NivelEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.NivelIesEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.TipoCarreraEnum;
import ec.gob.ceaaces.catalogo.exceptions.ServicioException;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaConceptoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionCarreraDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.MallaCurricularDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.SedeIesDTO;
import ec.gob.ceaaces.institutos.enumeraciones.OrigenInformacionEnum;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.InstitutosServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.ArchivoUtil;
import ec.gob.ceaaces.util.JsfUtil;

public class AdminInformacionCarreraSedeController implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger
	    .getLogger(AdminInformacionCarreraSedeController.class
	            .getSimpleName());

    private static final String GRADO_OTROS = "OTROS";
    private static final Long GRADO_OTROS_ID = -1L;

    private List<InformacionCarreraDTO> listaInformacionCarreraDto;
    private List<InformacionCarreraDTO> listaInformacionCarreraFiltros;
    private List<String> modalidades;
    private List<SedeIesDTO> listaSedeIesDto;
    private Boolean evidenciaConcepto = false;
    private ConceptoDTO conceptoSeleccionado;
    private List<MallaCurricularDTO> mallas;
    private List<CarreraIesDTO> listaCarreraIesDto;
    private List<CarreraIesDTO> listaCarreraIesPorModalidad;
    private List<GradoTituloDTO> listaNivelTitulo;
    private String fichero;
    private String url;
    private String extensionDocumento;
    private StreamedContent documentoDescarga;
    private Boolean descargar = false;
    private List<GradoTituloDTO> listaNivelTituloInstitutos;
    private List<InformacionCarreraDTO> informacionCarreras;
    private List<AreaConocimientoDTO> listaAreaConocimientoDto;
    private List<SubAreaConocimientoDTO> listaSubAreaConocimientoDto;

    private InformacionCarreraDTO infoCarreraDto;
    private IesDTO iesDTO;
    private InformacionIesDTO informacionIesDto;
    private FaseIesDTO faseIesDTO;
    private CarreraIesDTO carreraIesDTO;
    private List<EvidenciaConceptoDTO> listaEvidenciaConcepto;

    private Long idSedeSeleccionada;
    private Long idCarreraIesSeleccionada;
    private Long idNivelTituloSelecionado;
    private Long idNivelGradoSelecionado;
    private String usuario;
    private String codIes;
    private Boolean requierePracticasPreprofesionales;
    private Boolean requiereConciertoGrado;
    private Long idNivelTituloSelecionadoOtros;

    private boolean alertaIes = false;
    private String perfil;

    private String[] tipoCarrera;
    private String tipoSeleccionado;

    private String[] modalidadEnum;
    private String modalidadSeleccionada;

    private List<String> nivelEnum;

    private String[] estadoCarreraEnum;
    private String estadoCarreraSeleccionado;

    private Long idAreaConocimientoSeleccionado;
    private Long idSubAreaConocimientoSeleccionado;

    private Boolean alertaEvaluador = false;
    private boolean sinAccionBtnGuardar;
    private String codigo;
    private Boolean en = false;
    private String nombreGrado;
    private String Nivel;
    private Boolean alertaUsuarioIes = false;
    private Boolean alertaFaseRectificacion = false;

    @Autowired
    private RegistroServicio registroServicio;

    @Autowired
    private CatalogoServicio catalogoServicio;

    @Autowired
    private InstitutosServicio institutosServicio;

    private Date fechaActual;

    public AdminInformacionCarreraSedeController() {
	infoCarreraDto = new InformacionCarreraDTO();
	iesDTO = new IesDTO();
	informacionIesDto = new InformacionIesDTO();
	faseIesDTO = new FaseIesDTO();
	carreraIesDTO = new CarreraIesDTO();
	conceptoSeleccionado = new ConceptoDTO();
	listaEvidenciaConcepto = new ArrayList<EvidenciaConceptoDTO>();

	listaInformacionCarreraDto = new ArrayList<InformacionCarreraDTO>();
	listaInformacionCarreraFiltros = new ArrayList<InformacionCarreraDTO>();
	modalidades = new ArrayList<>();
	listaSedeIesDto = new ArrayList<SedeIesDTO>();
	mallas = new ArrayList<>();
	listaCarreraIesDto = new ArrayList<>();
	listaCarreraIesPorModalidad = new ArrayList<CarreraIesDTO>();
	listaNivelTitulo = new ArrayList<GradoTituloDTO>();
	listaNivelTituloInstitutos = new ArrayList<GradoTituloDTO>();
	informacionCarreras = new ArrayList<>();
	listaAreaConocimientoDto = new ArrayList<>();
	listaSubAreaConocimientoDto = new ArrayList<>();
	nivelEnum = new ArrayList<>();
    }

    @PostConstruct
    public void start() {

	try {
	    ListaIesController controller = (ListaIesController) JsfUtil
		    .obtenerObjetoSesion("listaIesController");

	    alertaEvaluador = false;
	    alertaUsuarioIes = false;
	    alertaFaseRectificacion = false;

	    this.usuario = SecurityContextHolder.getContext()
		    .getAuthentication().getName();

	    this.iesDTO = controller.getIes();
	    this.codIes = iesDTO.getCodigo();
	    this.informacionIesDto = registroServicio
		    .obtenerInformacionIesPorIes(iesDTO);

	    this.perfil = controller.getPerfil().getNombre();
	    this.faseIesDTO = controller.getFaseIesDTO();
	    // Todas las informacionCarreras de la IES (sin restriccion de
	    // sedes)
	    informacionCarreras = registroServicio
		    .obtenerInformacionCarreraPorInformacionIes(informacionIesDto
		            .getId());

	    if (this.perfil.startsWith("IES_EVALUADOR")) {
		alertaEvaluador = true;
	    }

	    if (this.perfil.startsWith("IES_USUARIO")) {
		alertaUsuarioIes = true;
	    }

	    if (this.faseIesDTO.getTipo().name().startsWith("RECTIFICACION")) {
		alertaFaseRectificacion = true;
	    }

	    cargarDatos();
	    cargarCarreraIes();

	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}

    }

    public void cargarEConcepto() {
	if (!idSedeSeleccionada.equals("")) {
	    evidenciaConcepto = true;
	    try {
		conceptoSeleccionado = catalogoServicio
		        .obtenerConceptoPorOrigenYGrupo(
		                OrigenInformacionEnum.CARRERA.getValor(),
		                GrupoConceptoEnum.CARRERA.getValor());
		listaEvidenciaConcepto = institutosServicio
		        .obtenerEvidenciasDeIesPorIdConceptoEIdTabla(
		                iesDTO.getId(), conceptoSeleccionado.getId(),
		                infoCarreraDto.getId(),
		                conceptoSeleccionado.getOrigen());

	    } catch (ServicioException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	} else {
	    evidenciaConcepto = false;
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

    private void cargarModadalidades() {

	try {
	    modalidades.clear();
	    modalidades = catalogoServicio.obtenerModalidadesIes(codIes);
	} catch (ServicioException e) {
	    e.printStackTrace();
	    return;
	}
    }

    public void cargarDatos() {
	tipoCarrera = new String[2];
	tipoCarrera[0] = TipoCarreraEnum.CARRERA.getValue();
	tipoCarrera[1] = TipoCarreraEnum.PROGRAMA.getValue();

	modalidadEnum = new String[4];
	modalidadEnum[0] = ModalidadEnum.PRESENCIAL.getValue();
	modalidadEnum[1] = ModalidadEnum.SEMIPRESENCIAL.getValue();
	modalidadEnum[2] = ModalidadEnum.DISTANCIA.getValue();
	modalidadEnum[3] = ModalidadEnum.VIRTUAL.getValue();

	estadoCarreraEnum = new String[3];
	estadoCarreraEnum[0] = EstadoCarreraEnum.VIGENTE.getValue();
	estadoCarreraEnum[1] = EstadoCarreraEnum.NO_VIGENTE.getValue();
	estadoCarreraEnum[2] = EstadoCarreraEnum.NO_VIGENTE_HABILITADO_PARA_REGISTRO_DE_TITULOS
	        .getValue();

	cargarSedeIes();
	cargarCarreraIes();
	cargarNivelTitulo();
	cargarModadalidades();
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

    public void cargarNivelTitulo() {
	try {
	    this.listaNivelTitulo.clear();
	    this.listaNivelTitulo = catalogoServicio.obtenerNivelTitulo();
	    obtenerTitulosInstitutos();
	} catch (Exception e) {
	    JsfUtil.msgError("Error al cargar Sede IES");
	}
    }

    private void obtenerTitulosInstitutos() {
	listaNivelTituloInstitutos.clear();
	for (GradoTituloDTO nivel : listaNivelTitulo) {
	    if (NivelIesEnum.TECNICO.equals(nivel.getClase())) {
		listaNivelTituloInstitutos.add(nivel);
	    }
	}
    }

    public void cargarCarreraIes() {
	try {
	    this.listaCarreraIesDto.clear();
	    this.listaCarreraIesDto = catalogoServicio
		    .obtenerCarrerasPorIes(codIes);

	} catch (Exception e) {
	    JsfUtil.msgError("Error al cargar carreras..");
	}
    }

    public void nuevaInformacionCarreraSede() {
	if ((null == idSedeSeleccionada) || (idSedeSeleccionada == 0L)) {
	    JsfUtil.msgAdvert("Seleccione una Matriz/Extensión");
	    return;
	}
	limpiar();

	RequestContext.getCurrentInstance().reset("formInfoCarreraSede");

	RequestContext context = RequestContext.getCurrentInstance();
	context.execute("dlgInforCarreraSede.show()");
    }

    public void cargarListaInfoCarrera(Long idSede) {

	evidenciaConcepto = false;

	try {
	    listaInformacionCarreraDto.clear();
	    listaInformacionCarreraFiltros.clear();
	    listaInformacionCarreraDto = registroServicio
		    .obtenerInfCarreraPorSede(idSede, null);
	    listaInformacionCarreraFiltros.addAll(listaInformacionCarreraDto);
	} catch (Exception e) {
	    JsfUtil.msgError("Error al cargar Información Carrera");
	}
    }

    public void cargarInformacionCarreraCombo() {
	cargarListaInfoCarrera(idSedeSeleccionada);
    }

    public void editarInfoCarreraSede() {

	if (alertaFaseRectificacion && alertaUsuarioIes) {
	    if (infoCarreraDto.getId() != null
		    && !infoCarreraDto.getFaseIesDTO().getTipo().name()
		            .startsWith("RECTIFICACION")) {
		JsfUtil.msgError("No se puede editar este carrera en fase de Rectificación.");
		return;
	    }
	}

	idCarreraIesSeleccionada = infoCarreraDto.getCarreraIesDTO().getId();

	if (GRADO_OTROS.equals(infoCarreraDto.getGradoTituloDTO().getGrado())) {
	    idNivelGradoSelecionado = GRADO_OTROS_ID;
	    idNivelGradoSelecionado = infoCarreraDto.getGradoTituloDTO()
		    .getId();
	} else {
	    idNivelTituloSelecionado = infoCarreraDto.getGradoTituloDTO()
		    .getId();
	}

	String grado = infoCarreraDto.getNivel();
	if (grado == null || grado.equals("TERCER NIVEL")) {
	    idNivelTituloSelecionado = 1L;

	} else {
	    idNivelTituloSelecionado = 2L;
	}

	estadoCarreraSeleccionado = infoCarreraDto.getEstado();

	Nivel = "TERCER NIVEL";
	LOG.info("MODALIDAD: "
	        + infoCarreraDto.getCarreraIesDTO().getModalidad());
	modalidadSeleccionada = infoCarreraDto.getCarreraIesDTO()
	        .getModalidad();
	LOG.info("MODALIDAD SELECCIONADA: " + modalidadSeleccionada);
	tomarModalidad();
	cargarCuartonivel();
	obtenerNivel();
	cargarGrado();
	listaCarreraIesDto.clear();
	RequestContext context = RequestContext.getCurrentInstance();
	context.execute("dlgInforCarreraSede.show();");
    }

    public void eliminarInformacionCarreraSede() {
	try {
	    if (alertaFaseRectificacion && alertaUsuarioIes) {
		if (infoCarreraDto.getId() != null
		        && !infoCarreraDto.getFaseIesDTO().getTipo().name()
		                .startsWith("RECTIFICACION")) {
		    JsfUtil.msgError("No se puede eliminar esta carrera en fase de Rectificación.");
		    return;
		}
	    }

	    if (!confirmarEliminar()) {
		return;
	    }

	    AuditoriaDTO auditorDto = new AuditoriaDTO();
	    auditorDto.setUsuarioModificacion(usuario);
	    auditorDto.setFechaModificacion(new Date(Calendar.getInstance(
		    TimeZone.getDefault()).getTimeInMillis()));

	    List<MallaCurricularDTO> mallas = registroServicio
		    .obtenerMallaCurricular(infoCarreraDto);
	    boolean existeEstudiates = registroServicio
		    .existeInformacionCarreraAsignadoaEstudiante(infoCarreraDto
		            .getId());

	    if (!mallas.isEmpty()) {
		JsfUtil.msgAdvert("La carrera seleccionada tiene Mallas curriculares relacionadas. Elimine estos registros e intente nuevamente.");
		return;
	    } else if (existeEstudiates) {
		JsfUtil.msgAdvert("La carrera seleccionada tiene estudiantes matriculados. No se puede realizar esta operación. Utilice la opción editar.");
		return;
	    } else {
		infoCarreraDto.setAuditoriaDTO(auditorDto);
		infoCarreraDto.setActivo(false);

		infoCarreraDto = registroServicio
		        .registrarInformacionCarrera(infoCarreraDto);

		if (null != infoCarreraDto.getId()) {

		    JsfUtil.msgInfo("Registro eliminado correctamente");
		    infoCarreraDto = new InformacionCarreraDTO();
		    cargarListaInfoCarrera(idSedeSeleccionada);
		}
	    }

	} catch (ServicioException e) {
	    JsfUtil.msgError("error al guardar, consulte con el administrador");
	    e.printStackTrace();
	} catch (Exception e) {
	    JsfUtil.msgError("error al guardar, consulte con el administrador");
	    e.printStackTrace();
	}
    }

    public Boolean confirmarEliminar() throws ServicioException {
	cargarMallas();
	if (!mallas.isEmpty()) {
	    JsfUtil.msgError("No puede eliminar la Carrera Ies ya que la actual tiene malla(s) curricular(e)s activa(s)");
	    return false;
	}

	boolean existeInformacioCarreraEstudiante = registroServicio
	        .existeInformacionCarreraAsignadoaEstudiante(infoCarreraDto
	                .getId());
	if (existeInformacioCarreraEstudiante) {
	    JsfUtil.msgError("No puede eliminar la Carrera Ies ya que la actual esta asignada a estudiantes");
	    return false;
	}

	boolean existeInfoCarreraProyectos = registroServicio
	        .existeInformacionCarreraEnProyectosParticipacion(infoCarreraDto
	                .getId());
	if (existeInfoCarreraProyectos) {
	    JsfUtil.msgError("No puede eliminar la Carrera Ies ya que la actual esta asignada a proyectos");
	    return false;
	}

	return true;
    }

    public void guardarInformacionCarreraSede() {

	try {
	    if (!validarDatos(infoCarreraDto)) {
		return;
	    }

	    AuditoriaDTO auditorDto = new AuditoriaDTO();
	    auditorDto.setUsuarioModificacion(usuario);
	    auditorDto.setFechaModificacion(new Date());

	    infoCarreraDto.setAuditoriaDTO(auditorDto);
	    infoCarreraDto.setActivo(true);
	    SedeIesDTO sdeIesDto = new SedeIesDTO();
	    sdeIesDto.setId(idSedeSeleccionada);
	    infoCarreraDto.setSedeIesDTO(sdeIesDto);

	    CarreraIesDTO carreraIesDto = new CarreraIesDTO();
	    carreraIesDto.setId(idCarreraIesSeleccionada);
	    infoCarreraDto.setCarreraIesDTO(carreraIesDto);

	    infoCarreraDto.setNivel("TECNICO");

	    infoCarreraDto.setEstado(estadoCarreraSeleccionado);

	    // if (idNivelTituloSelecionado == 1L) {
	    // infoCarreraDto.setNivel("TECNICO");
	    // idNivelGradoSelecionado = null;
	    // } else if (idNivelTituloSelecionado == 2L) {
	    // infoCarreraDto.setNivel("TERCER NIVEL");
	    // idNivelGradoSelecionado = null;
	    // } else {
	    // infoCarreraDto.setNivel("CUARTO NIVEL");
	    // }

	    GradoTituloDTO nivelTitulo = new GradoTituloDTO();
	    nivelTitulo.setId(idNivelTituloSelecionado);

	    // if (idNivelGradoSelecionado == null || idNivelGradoSelecionado ==
	    // 0) {
	    // nivelTitulo.setId(-99L);
	    // } else if (GRADO_OTROS_ID.equals(idNivelGradoSelecionado)
	    // && idNivelGradoSelecionado != null) {
	    // nivelTitulo.setId(idNivelTituloSelecionadoOtros);
	    // } else {
	    // nivelTitulo.setId(idNivelGradoSelecionado);
	    // }

	    infoCarreraDto.setGradoTituloDTO(nivelTitulo);

	    infoCarreraDto.setFaseIesDTO(this.faseIesDTO);
	    infoCarreraDto.setIdInformacionIes(this.informacionIesDto.getId());
	    infoCarreraDto.setVerificarEvidencia(false);
	    infoCarreraDto = registroServicio
		    .registrarInformacionCarrera(infoCarreraDto);

	    if (null != infoCarreraDto.getId()) {
		JsfUtil.msgInfo("Registro almacenado correctamente");
		infoCarreraDto = new InformacionCarreraDTO();
		cargarListaInfoCarrera(idSedeSeleccionada);
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("dlgInforCarreraSede.hide()");
	    }
	} catch (ServicioException e) {
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}

    }

    public void salirInformacionCarreraSedeDetalle() {

    }

    public boolean validarDatos(InformacionCarreraDTO info) {

	if (null == idNivelTituloSelecionado || idNivelTituloSelecionado == 0) {
	    JsfUtil.msgError("Seleccione Nivel Titulo");
	    return false;
	}

	if (null == idCarreraIesSeleccionada || idCarreraIesSeleccionada == 0) {
	    JsfUtil.msgError("Seleccione la Carrera");
	    return false;
	}

	if (null == info.getId()) {
	    for (InformacionCarreraDTO infoCarrera : listaInformacionCarreraDto) {
		if (infoCarrera.getCarreraIesDTO().getId()
		        .equals(idCarreraIesSeleccionada)) {
		    JsfUtil.msgAdvert("La Carrera Seleccionada ya se encuentra registrada");
		    return false;
		}
	    }
	}

	if (null == info.getFechaCreacion()) {
	    JsfUtil.msgError("Fecha Aprobación es requerida");
	    return false;
	}

	if (info.getFechaAprobacion() != null) {
	    if (info.getFechaCreacion().after(info.getFechaAprobacion())) {
		JsfUtil.msgAdvert("La Fecha de solicitud debe ser posterior a la Fecha de aprobación");
		return false;
	    }
	}

	if (null == info.getTituloOtorga() || info.getTituloOtorga().equals("")) {
	    JsfUtil.msgError("Ingrese titulo que Otorga");
	    return false;
	}

	if (null == estadoCarreraSeleccionado) {
	    JsfUtil.msgError("Estado es requerido");
	    return false;
	}

	if (null == info.getFechaPrimeraPromocion()) {
	    JsfUtil.msgError("Fecha primera matricula es requerida");
	    return false;
	}
	/*
	 * else { if
	 * (info.getFechaCreacion().after(info.getFechaPrimeraPromocion())) {
	 * JsfUtil.msgAdvert(
	 * "La Fecha de primera matrícula debe ser posterior a la Fecha de creación"
	 * ); return false; } }
	 */
	if (null == info.getFechaUltimaMatricula()) {
	    JsfUtil.msgError("Fecha última matricula es requerida");
	    return false;
	} else {
	    /*
	     * if
	     * (info.getFechaCreacion().after(info.getFechaUltimaMatricula())) {
	     * JsfUtil.msgAdvert(
	     * "La Fecha de última matrícula debe ser posterior a la Fecha de creación"
	     * ); return false; }
	     */
	    if (info.getFechaPrimeraPromocion().after(
		    info.getFechaUltimaMatricula())) {
		JsfUtil.msgAdvert("La Fecha de última matrícula debe ser posterior a la Fecha de primera matrícula");
		return false;
	    }
	}

	return true;
    }

    public void obtenerNivel() {
	sinAccionBtnGuardar = verificarSeleccionCarrera();

	if (sinAccionBtnGuardar && infoCarreraDto.getId() == null) {
	    JsfUtil.msgError("La carrera seleccionada ya se dicta en otra sede");
	    return;
	}

	if (infoCarreraDto.getId() != null
	        && !idCarreraIesSeleccionada.equals(infoCarreraDto
	                .getCarreraIesDTO().getId())) {

	    if (!mallas.isEmpty()) {
		JsfUtil.msgError("No puede cambiar la Carrera Ies ya que la actual tiene malla(s) curricular(e)s activa(s)");
		sinAccionBtnGuardar = true;
		return;
	    }
	}

	// obtener carreras
	cargarCarreraIes();

	System.out.println("carreraConsultada idCarreraIesSeleccionada: "
	        + idCarreraIesSeleccionada);
	// obtener el obj CarreraDto
	CarreraIesDTO carreraConsultada = new CarreraIesDTO();
	// for (CarreraIesDTO car : listaCarreraIesDto) {
	for (CarreraIesDTO car : listaCarreraIesPorModalidad) {

	    if (car.getId().equals(idCarreraIesSeleccionada)) {
		carreraConsultada = car;
		break;
	    }
	}

	// de la carrera obtenida, obtener el id de nivel asignado
	listaNivelTituloInstitutos.size();
	System.out
	        .println("carreraConsultada id: " + carreraConsultada.getId());
	System.out.println("carreraConsultada nivel: "
	        + carreraConsultada.getNivel());
	for (GradoTituloDTO nivel : listaNivelTituloInstitutos) {
	    if (nivel.getNombre().equals(carreraConsultada.getNivel())) {
		idNivelTituloSelecionado = nivel.getId();
		break;
	    }
	}

	// catalogoServicio.obtenerCarreraPorIesYCodigo(codigoIes, codCarrera);

	// for (CarreraIesDTO gr : listaCarreraIesDto) {
	// if (idCarreraIesSeleccionada.equals(gr.getId())) {
	// codigo = gr.getCodigo();
	// break;
	// }
	// }
	//
	// if (codigo.charAt(0) == 'P') {
	// idNivelTituloSelecionado = 2L;
	// setEn(true);
	// } else {
	// idNivelTituloSelecionado = 1L;
	// setEn(false);
	// }
    }

    // Se verifica si la carrera seleccionada para una nueva informacionCarrera
    // ya se está dictando en otra sede.
    private boolean verificarSeleccionCarrera() {
	for (InformacionCarreraDTO infoCarrera : informacionCarreras) {
	    if (!idSedeSeleccionada.equals(infoCarrera.getSedeIesDTO().getId())) {
		if (infoCarrera.getCarreraIesDTO().getId()
		        .equals(idCarreraIesSeleccionada)) {
		    return true;
		}
	    }
	}

	return false;
    }

    public void cargarCuartonivel() {
	if (idNivelTituloSelecionado == 1L) {
	    en = false;
	} else if (idNivelTituloSelecionado == 2L) {
	    en = true;
	}

    }

    /**
     * 
     * ...method doSomethingElse documentation comment...
     * 
     * @author eviscarra
     * @version 15/07/2014 - 12:47:11
     */
    private void cargarMallas() {
	mallas.clear();
	try {
	    mallas = registroServicio.obtenerMallaCurricular(infoCarreraDto);
	} catch (Exception e) {
	    e.printStackTrace();
	    return;
	}
    }

    public void cargarGrado() {
	setNombreGrado("");
	Long idNivel = idNivelTituloSelecionado;
	if (GRADO_OTROS_ID.equals(idNivel)) {
	    idNivel = idNivelTituloSelecionadoOtros;
	} else {
	    idNivelTituloSelecionadoOtros = null;
	}
	for (GradoTituloDTO gr : listaNivelTitulo) {
	    if (idNivel == gr.getId()) {
		setNombreGrado(gr.getNombre());
		en = true;
		break;
	    }
	}
    }

    public void crearNuevaCarrera() {
	this.nivelEnum.clear();
	carreraIesDTO = new CarreraIesDTO();
	idAreaConocimientoSeleccionado = -1L;

	for (NivelEnum nivel : NivelEnum.values()) {
	    this.nivelEnum.add(nivel.getValue());
	}
	cargarAreaConocimiento();
    }

    public void cargarAreaConocimiento() {
	listaAreaConocimientoDto.clear();
	try {
	    listaAreaConocimientoDto = catalogoServicio
		    .obtenerAreasConocimiento();
	} catch (ServicioException e) {
	    JsfUtil.msgAdvert("Error al cargar lista Area Conocimiento");
	    e.printStackTrace();
	}
    }

    public void cargarSubAreaConocimiento() {
	try {
	    listaSubAreaConocimientoDto.clear();
	    listaSubAreaConocimientoDto = catalogoServicio
		    .obtenerSubAreasConocimientoPorArea(idAreaConocimientoSeleccionado);
	} catch (ServicioException e) {
	    LOG.info("Error al cargar SubAreaConocimiento");
	}
    }

    public boolean validarDatos(CarreraIesDTO info) {

	if (null == info.getNombre() || info.getNombre().equals("")) {
	    JsfUtil.msgError("Ingrese el nombre de la Carrera");
	    return false;
	}

	if (null == info.getNivel() || info.getNivel().equals("")) {
	    JsfUtil.msgError("Seleccione el nivel de la carrera");
	    return false;
	}

	if (null == info.getModalidad() || info.getModalidad().equals("")) {
	    JsfUtil.msgAdvert("Seleccione la modalidad de la carrera");
	    return false;
	}

	if (null == info.getSubAreaConocimiento().getId()
	        || info.getSubAreaConocimiento().getId().equals(0L)) {
	    JsfUtil.msgError("Seleccione la Subárea de Conocimiento");
	    return false;
	}

	return true;
    }

    public void guardarCarrera() {
	RequestContext context = RequestContext.getCurrentInstance();

	try {

	    if (!validarDatos(carreraIesDTO)) {
		return;
	    }

	    /* AUDITORIA */
	    AuditoriaDTO adto = new AuditoriaDTO();
	    adto.setFechaModificacion(new Date());
	    adto.setUsuarioModificacion(this.usuario);
	    carreraIesDTO.setAuditoria(adto);

	    carreraIesDTO.setActivo(true);
	    String codigo = "";
	    codigo = "NR-" + iesDTO.getCodigo() + '-'
		    + obtenerCodigoSecuencial();
	    carreraIesDTO.setCodigo(codigo);
	    carreraIesDTO.setCodigoCarrera(codigo);
	    carreraIesDTO.setEstado(EstadoCarreraEnum.VIGENTE);
	    carreraIesDTO.setCodigoIes(iesDTO.getCodigo());

	    catalogoServicio.registrarActualizarCarrerasIes(carreraIesDTO);
	    JsfUtil.msgInfo("El registro ha sido almacenado correctamente");
	    limpiar();
	    context.execute("dialogNuevaCarrera.hide();");
	    cargarModadalidades();
	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    private void limpiar() {
	infoCarreraDto = new InformacionCarreraDTO();
	idCarreraIesSeleccionada = null;
	idNivelTituloSelecionado = null;
	modalidadSeleccionada = "";
	listaCarreraIesPorModalidad.clear();
	idCarreraIesSeleccionada = -1L;
	estadoCarreraSeleccionado = "";
    }

    public String obtenerCodigoSecuencial() {

	Integer max = 0;
	cargarCarreraIes();
	LOG.info("Total carreras: " + listaCarreraIesDto.size());
	for (CarreraIesDTO carrera : listaCarreraIesDto) {
	    if (carrera.getCodigo().startsWith("NR")) {
		LOG.info("id carrera: " + carrera.getId());
		max++;
	    }
	}
	max++;
	LOG.info("codigo secuencial max: " + max.toString());
	return max.toString();
    }

    /**
     * @return the listaInformacionCarreraDto
     */
    public List<InformacionCarreraDTO> getListaInformacionCarreraDto() {
	return listaInformacionCarreraDto;
    }

    /**
     * @param listaInformacionCarreraDto
     *            the listaInformacionCarreraDto to set
     */
    public void setListaInformacionCarreraDto(
	    List<InformacionCarreraDTO> listaInformacionCarreraDto) {
	this.listaInformacionCarreraDto = listaInformacionCarreraDto;
    }

    /**
     * @return the listaSedeIesDto
     */
    public List<SedeIesDTO> getListaSedeIesDto() {
	return listaSedeIesDto;
    }

    /**
     * @param listaSedeIesDto
     *            the listaSedeIesDto to set
     */
    public void setListaSedeIesDto(List<SedeIesDTO> listaSedeIesDto) {
	this.listaSedeIesDto = listaSedeIesDto;
    }

    /**
     * @return the infoCarreraDto
     */
    public InformacionCarreraDTO getInfoCarreraDto() {
	return infoCarreraDto;
    }

    /**
     * @param infoCarreraDto
     *            the infoCarreraDto to set
     */
    public void setInfoCarreraDto(InformacionCarreraDTO infoCarreraDto) {
	// Se obtienen las mallas curriculares de la InformacionCarrera
	// seleccionada, para su uso en caso de editar o eliminar una
	// InformacionCarrera.
	this.infoCarreraDto = infoCarreraDto;
	if (this.infoCarreraDto != null && this.infoCarreraDto.getId() != null) {
	    cargarMallas();
	} else {
	    mallas.clear();
	}
    }

    public void tomarModalidad() {
	listaCarreraIesPorModalidad.clear();

	if (modalidadSeleccionada != null || !modalidadSeleccionada.isEmpty()) {
	    try {
		listaCarreraIesPorModalidad = catalogoServicio
		        .obtenerCarrerasPorIesPorModalidad(codIes,
		                modalidadSeleccionada);
	    } catch (ServicioException e) {
		e.printStackTrace();
		JsfUtil.msgError("No se pudo obtener las carreras");
		return;
	    }
	}
	LOG.info("MODALIDAD***: " + modalidadSeleccionada);
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
     * @return the idSedeSeleccionada
     */
    public Long getIdSedeSeleccionada() {
	return idSedeSeleccionada;
    }

    /**
     * @param idSedeSeleccionada
     *            the idSedeSeleccionada to set
     */
    public void setIdSedeSeleccionada(Long idSedeSeleccionada) {
	this.idSedeSeleccionada = idSedeSeleccionada;
    }

    /**
     * @return the idCarreraIesSeleccionada
     */
    public Long getIdCarreraIesSeleccionada() {
	return idCarreraIesSeleccionada;
    }

    /**
     * @param idCarreraIesSeleccionada
     *            the idCarreraIesSeleccionada to set
     */
    public void setIdCarreraIesSeleccionada(Long idCarreraIesSeleccionada) {
	this.idCarreraIesSeleccionada = idCarreraIesSeleccionada;
    }

    public List<GradoTituloDTO> getListaNivelTitulo() {
	return listaNivelTitulo;
    }

    public void setListaNivelTitulo(List<GradoTituloDTO> listaNivelTitulo) {
	this.listaNivelTitulo = listaNivelTitulo;
    }

    public Long getIdNivelTituloSelecionado() {
	return idNivelTituloSelecionado;
    }

    public void setIdNivelTituloSelecionado(Long idNivelTituloSelecionado) {
	this.idNivelTituloSelecionado = idNivelTituloSelecionado;
    }

    public Boolean getRequierePracticasPreprofesionales() {
	return requierePracticasPreprofesionales;
    }

    public void setRequierePracticasPreprofesionales(
	    Boolean requierePracticasPreprofesionales) {
	this.requierePracticasPreprofesionales = requierePracticasPreprofesionales;
    }

    public Boolean getRequiereConciertoGrado() {
	return requiereConciertoGrado;
    }

    public void setRequiereConciertoGrado(Boolean requiereConciertoGrado) {
	this.requiereConciertoGrado = requiereConciertoGrado;
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

    public String getCodIes() {
	return codIes;
    }

    public void setCodIes(String codIes) {
	this.codIes = codIes;
    }

    public String[] getTipoCarrera() {
	return tipoCarrera;
    }

    public void setTipoCarrera(String[] tipoCarrera) {
	this.tipoCarrera = tipoCarrera;
    }

    public String getTipoSeleccionado() {
	return tipoSeleccionado;
    }

    public void setTipoSeleccionado(String tipoSeleccionado) {
	this.tipoSeleccionado = tipoSeleccionado;
    }

    public String[] getModalidadEnum() {
	return modalidadEnum;
    }

    public void setModalidadEnum(String[] modalidadEnum) {
	this.modalidadEnum = modalidadEnum;
    }

    public String getModalidadSeleccionada() {
	return modalidadSeleccionada;
    }

    public void setModalidadSeleccionada(String modalidadSeleccionada) {
	this.modalidadSeleccionada = modalidadSeleccionada;
    }

    public String[] getEstadoCarreraEnum() {
	return estadoCarreraEnum;
    }

    public void setEstadoCarreraEnum(String[] estadoCarreraEnum) {
	this.estadoCarreraEnum = estadoCarreraEnum;
    }

    public String getEstadoCarreraSeleccionado() {
	return estadoCarreraSeleccionado;
    }

    public void setEstadoCarreraSeleccionado(String estadoCarreraSeleccionado) {
	this.estadoCarreraSeleccionado = estadoCarreraSeleccionado;
    }

    public List<CarreraIesDTO> getListaCarreraIesDto() {
	return listaCarreraIesDto;
    }

    public void setListaCarreraIesDto(List<CarreraIesDTO> listaCarreraIesDto) {
	this.listaCarreraIesDto = listaCarreraIesDto;
    }

    public boolean isAlertaIes() {
	return alertaIes;
    }

    public void setAlertaIes(boolean alertaIes) {
	this.alertaIes = alertaIes;
    }

    public String getPerfil() {
	return perfil;
    }

    public void setPerfil(String perfil) {
	this.perfil = perfil;
    }

    public List<InformacionCarreraDTO> getListaInformacionCarreraFiltros() {
	return listaInformacionCarreraFiltros;
    }

    public void setListaInformacionCarreraFiltros(
	    List<InformacionCarreraDTO> listaInformacionCarreraFiltros) {
	this.listaInformacionCarreraFiltros = listaInformacionCarreraFiltros;
    }

    public Date getFechaActual() {
	return new Date();
    }

    public void setFechaActual(Date fechaActual) {
	this.fechaActual = fechaActual;
    }

    public List<String> getModalidades() {
	return modalidades;
    }

    public void setModalidades(List<String> modalidades) {
	this.modalidades = modalidades;
    }

    public Boolean getAlertaEvaluador() {
	return alertaEvaluador;
    }

    public void setAlertaEvaluador(Boolean alertaEvaluador) {
	this.alertaEvaluador = alertaEvaluador;
    }

    public FaseIesDTO getFaseIesDTO() {
	return faseIesDTO;
    }

    public void setFaseIesDTO(FaseIesDTO faseIesDTO) {
	this.faseIesDTO = faseIesDTO;
    }

    public List<MallaCurricularDTO> getMallas() {
	return mallas;
    }

    public void setMallas(List<MallaCurricularDTO> mallas) {
	this.mallas = mallas;
    }

    public List<InformacionCarreraDTO> getInformacionCarreras() {
	return informacionCarreras;
    }

    public void setInformacionCarreras(
	    List<InformacionCarreraDTO> informacionCarreras) {
	this.informacionCarreras = informacionCarreras;
    }

    public boolean isSinAccionBtnGuardar() {
	return sinAccionBtnGuardar;
    }

    public void setSinAccionBtnGuardar(boolean sinAccionBtnGuardar) {
	this.sinAccionBtnGuardar = sinAccionBtnGuardar;
    }

    public String getCodigo() {
	return codigo;
    }

    public void setCodigo(String codigo) {
	this.codigo = codigo;
    }

    public Boolean getEn() {
	return en;
    }

    public void setEn(Boolean en) {
	this.en = en;
    }

    public Long getIdNivelGradoSelecionado() {
	return idNivelGradoSelecionado;
    }

    public void setIdNivelGradoSelecionado(Long idNivelGradoSelecionado) {
	this.idNivelGradoSelecionado = idNivelGradoSelecionado;
    }

    public Long getIdNivelTituloSelecionadoOtros() {
	return idNivelTituloSelecionadoOtros;
    }

    public void setIdNivelTituloSelecionadoOtros(
	    Long idNivelTituloSelecionadoOtros) {
	this.idNivelTituloSelecionadoOtros = idNivelTituloSelecionadoOtros;
    }

    public String getNombreGrado() {
	return nombreGrado;
    }

    public void setNombreGrado(String nombreGrado) {
	this.nombreGrado = nombreGrado;
    }

    public String getNivel() {
	return Nivel;
    }

    public void setNivel(String nivel) {
	Nivel = nivel;
    }

    public CarreraIesDTO getCarreraIesDTO() {
	return carreraIesDTO;
    }

    public void setCarreraIesDTO(CarreraIesDTO carreraIesDTO) {
	this.carreraIesDTO = carreraIesDTO;
    }

    public List<String> getNivelEnum() {
	return nivelEnum;
    }

    public void setNivelEnum(List<String> nivelEnum) {
	this.nivelEnum = nivelEnum;
    }

    public Long getIdAreaConocimientoSeleccionado() {
	return idAreaConocimientoSeleccionado;
    }

    public void setIdAreaConocimientoSeleccionado(
	    Long idAreaConocimientoSeleccionado) {
	this.idAreaConocimientoSeleccionado = idAreaConocimientoSeleccionado;
    }

    public Long getIdSubAreaConocimientoSeleccionado() {
	return idSubAreaConocimientoSeleccionado;
    }

    public void setIdSubAreaConocimientoSeleccionado(
	    Long idSubAreaConocimientoSeleccionado) {
	this.idSubAreaConocimientoSeleccionado = idSubAreaConocimientoSeleccionado;
    }

    public List<AreaConocimientoDTO> getListaAreaConocimientoDto() {
	return listaAreaConocimientoDto;
    }

    public void setListaAreaConocimientoDto(
	    List<AreaConocimientoDTO> listaAreaConocimientoDto) {
	this.listaAreaConocimientoDto = listaAreaConocimientoDto;
    }

    public List<SubAreaConocimientoDTO> getListaSubAreaConocimientoDto() {
	return listaSubAreaConocimientoDto;
    }

    public void setListaSubAreaConocimientoDto(
	    List<SubAreaConocimientoDTO> listaSubAreaConocimientoDto) {
	this.listaSubAreaConocimientoDto = listaSubAreaConocimientoDto;
    }

    public List<GradoTituloDTO> getListaNivelTituloInstitutos() {
	return listaNivelTituloInstitutos;
    }

    public void setListaNivelTituloInstitutos(
	    List<GradoTituloDTO> listaNivelTituloInstitutos) {
	this.listaNivelTituloInstitutos = listaNivelTituloInstitutos;
    }

    public List<CarreraIesDTO> getListaCarreraIesPorModalidad() {
	return listaCarreraIesPorModalidad;
    }

    public void setListaCarreraIesPorModalidad(
	    List<CarreraIesDTO> listaCarreraIesPorModalidad) {
	this.listaCarreraIesPorModalidad = listaCarreraIesPorModalidad;
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

    public List<EvidenciaConceptoDTO> getListaEvidenciaConcepto() {
	return listaEvidenciaConcepto;
    }

    public void setListaEvidenciaConcepto(
	    List<EvidenciaConceptoDTO> listaEvidenciaConcepto) {
	this.listaEvidenciaConcepto = listaEvidenciaConcepto;
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

}