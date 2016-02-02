package ec.gob.ceaaces.controller;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.beanutils.PropertyUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.RowEditEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.DualListModel;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import ec.gob.ceaaces.catalogo.dtos.catalogos.AreaConocimientoDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.AuditoriaDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.CarreraIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.ConceptoDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.GradoTituloDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.IesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.PaisDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.PuebloDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.RegionDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.SubAreaConocimientoDTO;
import ec.gob.ceaaces.catalogo.enumeraciones.EtniaEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.GrupoConceptoEnum;
import ec.gob.ceaaces.catalogo.exceptions.ServicioException;
import ec.gob.ceaaces.data.Evidencia;
import ec.gob.ceaaces.data.TituloSenescyt;
import ec.gob.ceaaces.institutos.dtos.evaluacion.AsignaturaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.CargoAcademicoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ContratacionDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ContratacionPeriodoAcademicoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.CursoCapacitacionDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.DocenteAsignaturaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.DocenteDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaConceptoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ExperienciaProfesionalDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.FormacionProfesionalDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionCarreraDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.MallaCurricularDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.PeriodoAcademicoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.PersonaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.SedeIesDTO;
import ec.gob.ceaaces.institutos.enumeraciones.AutoridadAcademicaEnum;
import ec.gob.ceaaces.institutos.enumeraciones.CategoriaTitularEnum;
import ec.gob.ceaaces.institutos.enumeraciones.DiscapacidadEnum;
import ec.gob.ceaaces.institutos.enumeraciones.GeneroEnum;
import ec.gob.ceaaces.institutos.enumeraciones.OrigenInformacionEnum;
import ec.gob.ceaaces.institutos.enumeraciones.RelacionIESEnum;
import ec.gob.ceaaces.institutos.enumeraciones.TiempoDedicacionEnum;
import ec.gob.ceaaces.institutos.enumeraciones.TipoProgramaEnum;
import ec.gob.ceaaces.seguridad.model.dtos.UsuarioDTO;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.EvidenciasServicio;
import ec.gob.ceaaces.services.InstitutosServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.services.UsuarioIesCarrerasServicio;
import ec.gob.ceaaces.util.ArchivoUtil;
import ec.gob.ceaaces.util.JsfUtil;
import ec.gob.ceaaces.util.Util;

@ManagedBean(name = "docenteController")
public class DocenteController implements Serializable {

    /**
     * 
     */

    private static final long serialVersionUID = 178648910196975660L;
    private static final Logger LOG = Logger.getLogger(DocenteController.class
	    .getSimpleName());
    private static final List<SelectItem> ANIOS = new ArrayList<>();
    private static final SelectItem[] MESES = { new SelectItem(0, "Enero"),
	    new SelectItem(1, "Febrero"), new SelectItem(2, "Marzo"),
	    new SelectItem(3, "Abril"), new SelectItem(4, "Mayo"),
	    new SelectItem(5, "Junio"), new SelectItem(6, "Julio"),
	    new SelectItem(7, "Agosto"), new SelectItem(8, "Septiembre"),
	    new SelectItem(9, "Octubre"), new SelectItem(10, "Noviembre"),
	    new SelectItem(11, "Diciembre") };

    static {
	Calendar fechaActual = Calendar.getInstance();
	for (int i = fechaActual.get(Calendar.YEAR); i >= fechaActual
	        .get(Calendar.YEAR) - 10; i--) {
	    SelectItem anio = new SelectItem(i, String.valueOf(i));
	    ANIOS.add(anio);
	}
    }

    @Autowired
    private CatalogoServicio catalagoServicio;

    @Autowired
    private InstitutosServicio institutosServicio;

    @Autowired
    private EvidenciasServicio evidenciasServicio;

    @Autowired
    private UsuarioIesCarrerasServicio uicServicio;

    @Autowired
    private RegistroServicio registroServicio;

    private int indiceTab;
    private int anioInicioPeriodoAcademico;
    private int mesInicioPeriodoAcademico;
    private int anioFinPeriodoAcademico;
    private int mesFinPeriodoAcademico;
    private StreamedContent documentoDescarga;
    private Integer totalDocentes;
    private Long idProceso;
    private Long idEvidencia;
    private String nombreFichero;
    private String fichero;
    private String url;
    private String extensionDocumento;
    private Boolean descargar = false;
    private Date fechaMin;
    private Date fechaMax;
    private Date fechaActual;
    private String fase;

    private Long idPais;
    private Long idPaisFormacion = null;
    private Long idNivelTitulo = -99L;

    private Long idSubArea;
    private Long idArea = -99L;
    private Boolean evidenciaConcepto = false;
    private List<AreaConocimientoDTO> areasConocimiento;
    private List<SubAreaConocimientoDTO> subareas;
    private List<ConceptoDTO> listaEvidencia;
    private List<EvidenciaConceptoDTO> listaEvidenciaConcepto;
    private ConceptoDTO conceptoSeleccionado;

    private List<GradoTituloDTO> gradosTitulo;
    private List<PaisDTO> paises;
    private List<RelacionIESEnum> relacionesIes;
    private List<TiempoDedicacionEnum> tiemposDedicacion;
    private List<CategoriaTitularEnum> categorias;
    private List<String> discapacidades;
    private List<String> generos;
    private List<AutoridadAcademicaEnum> cargosAcademicos;
    private List<String> tipoPrograma;
    private List<String> etnias;
    private List<PuebloDTO> pueblos;
    private List<RegionDTO> regiones;

    private List<IesDTO> listaIes;
    private List<IesDTO> iesFiltros;
    private CarreraIesDTO carreraSeleccionada;
    private IesDTO iesSeleccionada;
    private IesDTO iesFormacion;

    private List<DocenteDTO> docentes;
    private List<DocenteDTO> docentesFiltros;
    private DocenteDTO docenteSeleccionado;
    private final UsuarioDTO usuarioSistema;

    private PersonaDTO personaDocente;
    private ExperienciaProfesionalDTO experienciaProfesionalDocente;
    private ContratacionDTO contratoDocente;
    private FormacionProfesionalDTO formacionDocente;
    private CargoAcademicoDTO cargoDirectivoDocente;
    private CursoCapacitacionDTO cursoCapacitacion;
    private String usuario;
    private DocenteAsignaturaDTO docenteAsignaturaHoraClaseDTO = new DocenteAsignaturaDTO();

    private List<ContratacionDTO> contrataciones;
    private List<FormacionProfesionalDTO> formaciones;
    private List<CargoAcademicoDTO> cargosDirectivos;
    private List<ExperienciaProfesionalDTO> listaExperienciaProfesionalDTO;
    private List<CursoCapacitacionDTO> cursos;
    private List<ContratacionPeriodoAcademicoDTO> contratacionesPeriodoAcademico;

    // private List<ContratacionPeriodoAcademicoDTO> horasNoAcademicasPorAnio;
    private List<PeriodoAcademicoDTO> periodosAcademicos;
    private List<ContratacionPeriodoAcademicoDTO> horasNoAcademicasEliminar;
    private final List<ContratacionPeriodoAcademicoDTO> contratacionesPeriodoEliminar;
    private final List<PeriodoAcademicoDTO> periodosAcademicosEliminar;
    private final List<DocenteAsignaturaDTO> docentesAsignaturaHoraClase = new ArrayList<>();

    // private List<ItemEvidenciaLocal> evidenciasContrato;
    // private List<ItemEvidenciaLocal> evidenciasFormacion;
    // private List<ItemEvidenciaLocal> evidenciasCapacitacion;
    // private List<ItemEvidenciaLocal> evidenciasCargosDirectivos;
    // private List<ItemEvidenciaLocal> evidenciasSabaticos;
    // private ItemEvidenciaLocal itemEvidenciaSeleccionado;

    private boolean verTablaIes;
    private boolean verBtnIesDoc;
    private boolean verBtnIesCon;
    private boolean verBtnIesFor;
    private boolean verTabDatosPersonales;
    private boolean verTabContratacion;
    private boolean verTabFormacion;
    private boolean verTabCargosDireccion;
    private boolean verTabExperienciaProf;
    private boolean verTabCursos;
    private boolean verTabAsignaturas;
    private boolean mostrarAsignaturas;
    private Boolean alertaFase = false;

    private String discapacidad = DiscapacidadEnum.NINGUNA.getValue();
    private String cargo = AutoridadAcademicaEnum.COORDINADOR_CARRERA
	    .getValue();
    private String categoria;
    private String genero;
    private String tipoProg;
    private String pestania = "Datos Personales";
    private String accion = "Guardar cambios";
    private String msgAccion = "";
    private String cedulaDocente;
    private String mensajeErrorPeriodo;
    private String etniaSeleccionada;
    private Long regionSeleccionada;
    private Long idNivelTituloSelecionadoOtros;
    private Long idNivelTituloSelecionado;

    private FaseDTO faseEvaluacionDTO;
    private InformacionIesDTO informacionIesDTO;
    private InformacionCarreraDTO informacionCarreraDTO;
    private ContratacionPeriodoAcademicoDTO contratacionPeriodoAcademicoDTO;
    private PeriodoAcademicoDTO periodoAcademicoDTO = new PeriodoAcademicoDTO();

    // Filtros Docentes
    private int indice;

    private static final String ORIGEN_CARGA = "SISTEMA";
    private static final String GRADO_OTROS = "OTROS";

    private String clienteId;

    private Evidencia archivoSeleccionado;

    private boolean editarContrato;

    private String perfil;

    private Long idPeriodoAcademico;
    private int registros = 10;
    private int indiceAtras;
    private int indiceSiguiente;
    private int numRegistros;
    private boolean busqueda;
    private String identificacion;
    private List<DocenteDTO> listaDocentesTodos;
    private static final Integer[] rangos = { 10, 20, 50, 100, 200 };
    private int contador;
    private boolean habilitarSiguiente;
    private FaseIesDTO faseIesDTO;
    private boolean esTitular = false;
    private GradoTituloDTO gradoSeleccionado;
    private Integer[] aniosContrato;
    private Boolean alertaEvaluador = false;

    // horas asignacion
    private Boolean mostrarAsignacion = false;
    private List<DocenteAsignaturaDTO> listaAsignaturasxDocente;
    private List<SedeIesDTO> listaSedeIesAsignaturaDto;
    private List<InformacionCarreraDTO> listaInformacionCarreraDto;
    private List<MallaCurricularDTO> listaMallaDto;
    private List<AsignaturaDTO> listaAsignatura;
    private List<DocenteAsignaturaDTO> listaDocenteAsignatura;
    private List<DocenteAsignaturaDTO> listaDocenteAsignaturaEditable;
    private Long idSedeIesSeleccionada;
    private Long idInformacionCarreraSeleccionada;
    private Long idMallaSeleccionada;

    private Long idSedeIesSeleccionada2;
    private Long idInformacionCarreraSeleccionada2;
    private Long idMallaSeleccionada2;
    private List<SedeIesDTO> listaSedeIesAsignaturaDto2;
    private List<InformacionCarreraDTO> listaInformacionCarreraDto2;
    private List<MallaCurricularDTO> listaMallaDto2;
    private DualListModel<AsignaturaDTO> listaAsignaturasDTO;
    private List<AsignaturaDTO> source;
    private List<AsignaturaDTO> target;
    private List<AsignaturaDTO> targetAux;
    private DocenteAsignaturaDTO docenteAsigEditable;
    private List<DocenteAsignaturaDTO> listaDocenteAsignaturaCompartidaEliminar;
    private List<DocenteAsignaturaDTO> listaDocenteAsignaturaCompartidaEliminarOriginal;
    private final List<TituloSenescyt> titulosSenescyt = new ArrayList<TituloSenescyt>();
    private Boolean alertaUsuarioIes = false;
    private Boolean alertaFaseRectificacion = false;

    public DocenteController() {

	listaAsignaturasxDocente = new ArrayList<DocenteAsignaturaDTO>();
	listaSedeIesAsignaturaDto = new ArrayList<SedeIesDTO>();
	listaInformacionCarreraDto = new ArrayList<InformacionCarreraDTO>();
	listaMallaDto = new ArrayList<MallaCurricularDTO>();
	listaAsignatura = new ArrayList<AsignaturaDTO>();
	listaDocenteAsignatura = new ArrayList<DocenteAsignaturaDTO>();
	listaDocenteAsignaturaEditable = new ArrayList<DocenteAsignaturaDTO>();
	listaEvidenciaConcepto = new ArrayList<EvidenciaConceptoDTO>();
	docenteAsigEditable = new DocenteAsignaturaDTO();
	conceptoSeleccionado = new ConceptoDTO();

	listaSedeIesAsignaturaDto2 = new ArrayList<SedeIesDTO>();
	listaInformacionCarreraDto2 = new ArrayList<InformacionCarreraDTO>();
	listaMallaDto2 = new ArrayList<MallaCurricularDTO>();
	listaDocenteAsignaturaCompartidaEliminar = new ArrayList<DocenteAsignaturaDTO>();
	listaDocenteAsignaturaCompartidaEliminarOriginal = new ArrayList<DocenteAsignaturaDTO>();

	source = new ArrayList<AsignaturaDTO>();
	target = new ArrayList<AsignaturaDTO>();
	listaAsignaturasDTO = new DualListModel<AsignaturaDTO>(source, target);
	targetAux = new ArrayList<AsignaturaDTO>();

	this.paises = new ArrayList<>();
	this.listaIes = new ArrayList<>();
	this.iesFiltros = new ArrayList<IesDTO>();
	this.docentes = new ArrayList<>();
	this.docentesFiltros = new ArrayList<>();
	this.categorias = new ArrayList<CategoriaTitularEnum>();
	this.tiemposDedicacion = new ArrayList<>();
	this.relacionesIes = new ArrayList<>();
	this.discapacidades = new ArrayList<>();
	this.generos = new ArrayList<>();
	this.tipoPrograma = new ArrayList<>();
	this.contrataciones = new ArrayList<>();
	this.formaciones = new ArrayList<>();
	this.cargosDirectivos = new ArrayList<>();
	this.listaExperienciaProfesionalDTO = new ArrayList<>();
	this.cargosAcademicos = new ArrayList<>();
	this.cursos = new ArrayList<>();
	this.areasConocimiento = new ArrayList<>();
	this.subareas = new ArrayList<>();
	this.contratacionesPeriodoAcademico = new ArrayList<ContratacionPeriodoAcademicoDTO>();
	this.contratacionesPeriodoEliminar = new ArrayList<>();
	this.periodosAcademicosEliminar = new ArrayList<>();
	this.periodosAcademicos = new ArrayList<>();
	// this.horasNoAcademicasPorAnio = new ArrayList<>();
	this.horasNoAcademicasEliminar = new ArrayList<>();

	this.iesSeleccionada = new IesDTO();
	this.docenteSeleccionado = new DocenteDTO();
	this.usuarioSistema = new UsuarioDTO();
	this.carreraSeleccionada = new CarreraIesDTO();

	this.experienciaProfesionalDocente = new ExperienciaProfesionalDTO();
	this.personaDocente = new PersonaDTO();
	this.contratoDocente = new ContratacionDTO();
	this.formacionDocente = new FormacionProfesionalDTO();
	this.cargoDirectivoDocente = new CargoAcademicoDTO();
	this.cursoCapacitacion = new CursoCapacitacionDTO();

	this.verBtnIesDoc = true;
	this.verTabDatosPersonales = false;
	this.verTabContratacion = true;
	this.verTabFormacion = true;
	this.verTabCargosDireccion = true;
	this.verTabExperienciaProf = true;
	this.verTabCursos = true;
	this.habilitarSiguiente = true;

	this.listaDocentesTodos = new ArrayList<>();
	this.gradoSeleccionado = new GradoTituloDTO();
	this.listaEvidencia = new ArrayList<ConceptoDTO>();
	this.etnias = new ArrayList<>();
	this.pueblos = new ArrayList<>();
	this.regiones = new ArrayList<>();
	this.faseIesDTO = new FaseIesDTO();
    }

    @PostConstruct
    private void cargarDatosIniciales() {
	LOG.info("Ingresando al PostConstruct");
	LOG.info("Ingresando al PostConstruct sysout");
	try {
	    ListaIesController controller = (ListaIesController) JsfUtil
		    .obtenerObjetoSesion("listaIesController");

	    alertaFaseRectificacion = false;
	    alertaUsuarioIes = false;
	    this.alertaEvaluador = false;
	    this.discapacidades.clear();
	    this.generos.clear();
	    this.cargosAcademicos.clear();
	    this.relacionesIes.clear();
	    this.tiemposDedicacion.clear();
	    this.etnias.clear();
	    this.categorias = new ArrayList<>();
	    this.docentes.clear();
	    this.docentesFiltros.clear();
	    this.areasConocimiento.clear();
	    this.tipoPrograma.clear();
	    usuario = controller.getUsuario();

	    for (DiscapacidadEnum dis : DiscapacidadEnum.values()) {
		this.discapacidades.add(dis.getValue());
	    }

	    for (GeneroEnum gen : GeneroEnum.values()) {
		this.generos.add(gen.getValue());
	    }

	    for (TipoProgramaEnum tipoP : TipoProgramaEnum.values()) {
		this.tipoPrograma.add(tipoP.getValue());
	    }

	    for (TiempoDedicacionEnum tid : TiempoDedicacionEnum.values()) {
		if (tid.compareTo(TiempoDedicacionEnum.NO_DEFINIDO) != 0) {
		    this.tiemposDedicacion.add(tid);
		}
	    }

	    for (EtniaEnum etnia : EtniaEnum.values()) {
		this.etnias.add(etnia.getValue());
	    }

	    gradosTitulo = catalagoServicio.obtenerNivelTitulo();

	    this.areasConocimiento = catalagoServicio
		    .obtenerAreasConocimiento();

	    this.perfil = controller.getPerfil().getNombre();

	    // this.perfil = "IES_EVALUADOR";
	    if (this.perfil.startsWith("IES_EVALUADOR")) {
		alertaEvaluador = true;
	    }

	    this.iesSeleccionada = controller.getIes();
	    this.carreraSeleccionada = controller.getCarrera();

	    this.informacionIesDTO = registroServicio
		    .obtenerInformacionIesPorIes(controller.getIes());

	    this.faseIesDTO = controller.getFaseIesDTO();

	    this.fase = this.faseIesDTO.getFaseDTO().getNombre();
	    // this.fase = "EVALUACION";
	    if (this.fase.equals("EVALUACION")) {
		alertaFase = true;
	    }

	    if (this.perfil.startsWith("IES_USUARIO")) {
		alertaUsuarioIes = true;
	    }

	    if (this.faseIesDTO.getTipo().name().startsWith("RECTIFICACION")) {
		alertaFaseRectificacion = true;
	    }

	    this.regiones = catalagoServicio.obtenerRegiones();
	    idProceso = controller.getFaseIesDTO().getProcesoDTO().getId();
	    limpiarListas();
	    cargarDocentes();

	    cargarPaises();
	    cargarPeriodos();
	    contextUsuario();

	    listaEvidencia = catalagoServicio
		    .obtenerConceptoPorIdProcesoYGrupo(idProceso.toString(),
		            GrupoConceptoEnum.DOCENTE.getValor());

	    // horas asiganadas
	    cargarListasIes();
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	    return;
	}
    }

    private void cargarListasIes() {

	listaSedeIesAsignaturaDto.clear();
	listaSedeIesAsignaturaDto = cargarSedeIes();

	listaSedeIesAsignaturaDto2.clear();
	listaSedeIesAsignaturaDto2 = cargarSedeIes();
    }

    public List<SedeIesDTO> cargarSedeIes() {

	List<SedeIesDTO> sedesIesDto = new ArrayList<SedeIesDTO>();
	try {
	    sedesIesDto = registroServicio.obtenerSedesIes(informacionIesDTO
		    .getId());
	    LOG.info("listaSedeIesDto.size: " + sedesIesDto.size());
	} catch (Exception e) {
	    JsfUtil.msgError("Error al cargar Sede IES");
	}
	return sedesIesDto;
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

	    listaAsignatura.clear();
	    listaAsignatura = registroServicio
		    .obtenerAsignaturasPorMalla(idMallaSeleccionada);
	    // listaAsignatura = registroServicio
	    // .obtenerAsignaturasPorMallaNoAsignadasPeriodo(
	    // idMallaSeleccionada,
	    // contratacionPeriodoAcademicoDTO.getId());
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error al obtener asignaturas. Comuníquese con el administrador.");
	}
    }

    public void cargarCarreras2() {

	try {
	    if (null == idSedeIesSeleccionada2) {
		return;
	    }

	    listaInformacionCarreraDto2.clear();
	    listaInformacionCarreraDto2 = registroServicio
		    .obtenerInfCarreraPorSede(idSedeIesSeleccionada2, null);
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    public void cargarMalla2() {
	try {
	    if (null == idInformacionCarreraSeleccionada2) {
		return;
	    }
	    listaMallaDto2.clear();
	    InformacionCarreraDTO informacionCarreraDto = new InformacionCarreraDTO();
	    informacionCarreraDto.setId(idInformacionCarreraSeleccionada2);
	    listaMallaDto2 = registroServicio
		    .obtenerMallaCurricular(informacionCarreraDto);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void cargarAsignaturas2() {

	try {
	    if (null == idMallaSeleccionada2)
		return;

	    source.clear();
	    source = registroServicio
		    .obtenerAsignaturasPorMalla(idMallaSeleccionada2);

	    target = listaAsignaturasDTO.getTarget();

	    if (null == target)
		target = new ArrayList<AsignaturaDTO>();

	    listaAsignaturasDTO = new DualListModel<AsignaturaDTO>(source,
		    target);

	} catch (ServicioException e) {
	    e.printStackTrace();
	}
    }

    public void cargarAsignaturaEditable(AsignaturaDTO asignatura) {

	for (DocenteAsignaturaDTO asg : listaDocenteAsignaturaEditable) {
	    if (asg.getAsignaturaDTO().getId().equals(asignatura.getId())) {
		JsfUtil.msgAdvert("Asignatura ya se encuentra asignada");
		return;
	    }
	}

	DocenteAsignaturaDTO docenteAsignatura = new DocenteAsignaturaDTO();
	docenteAsignatura.setAsignaturaDTO(asignatura);
	listaDocenteAsignaturaEditable.add(docenteAsignatura);
	tomarAsignaturaAccion(docenteAsignatura);
    }

    public void quitarAsignaturaEditable(DocenteAsignaturaDTO item) {
	listaDocenteAsignaturaEditable.remove(item);
    }

    public void eliminarAsignaturaCompartida(DocenteAsignaturaDTO item) {
	listaDocenteAsignaturaCompartidaEliminarOriginal.remove(item);
	listaDocenteAsignaturaCompartidaEliminar.add(item);
    }

    public void actualizarAsignaturaCompartida() {
	if (listaDocenteAsignaturaCompartidaEliminar.isEmpty())
	    return;

	eliminarAsignaturaCompartidaHijos();
	obtenerListaAsignaturaDocentesTodos();
	listaDocenteAsignaturaCompartidaEliminarOriginal.clear();
	JsfUtil.msgInfo("Asignaturas Compartidas fueron eliminadas correctamente");
    }

    private void eliminarAsignaturaCompartidaHijos() {

	try {
	    if (listaDocenteAsignaturaCompartidaEliminar.isEmpty())
		return;

	    AuditoriaDTO auditoria = new AuditoriaDTO();
	    auditoria.setFechaModificacion(new Date());
	    auditoria.setUsuarioModificacion(usuarioSistema.getUsuario());

	    for (DocenteAsignaturaDTO docAsigEliminar : listaDocenteAsignaturaCompartidaEliminar) {
		docAsigEliminar.setActivo(false);
		docAsigEliminar.setAuditoriaDTO(auditoria);
		docAsigEliminar.setDocenteAsignaturasDTO(null);

		registroServicio.registrarDocenteAsignatura(docAsigEliminar);
	    }

	    listaDocenteAsignaturaCompartidaEliminar.clear();
	} catch (ServicioException e) {
	    e.printStackTrace();
	}
    }

    public void limpiarAsignaturas() {

	listaAsignatura.clear();
	listaDocenteAsignatura.clear();
	listaDocenteAsignaturaEditable.clear();
	idSedeIesSeleccionada = null;
	idInformacionCarreraSeleccionada = null;
	idMallaSeleccionada = null;
	listaInformacionCarreraDto.clear();
	listaMallaDto.clear();
    }

    public void cargarAsignaturasEditables() {
	limpiarAsignaturas();
	listaDocenteAsignaturaEditable = obtenerListaAsignaturaDocentes();
    }

    public void limpiarAsignaturas2() {

	idSedeIesSeleccionada2 = null;
	idInformacionCarreraSeleccionada2 = null;
	idMallaSeleccionada2 = null;
	listaInformacionCarreraDto2.clear();
	listaMallaDto2.clear();
	listaAsignaturasDTO.getSource().clear();
	listaAsignaturasDTO.getTarget().clear();
    }

    public void limpiarAsignaturaCompartidaEliminar() {
	listaDocenteAsignaturaCompartidaEliminar.clear();
	listaDocenteAsignaturaCompartidaEliminarOriginal.clear();
	listaDocenteAsignaturaCompartidaEliminarOriginal
	        .addAll(docenteAsigEditable.getDocenteAsignaturasDTO());

    }

    public void tomarAsignaturaAccion(DocenteAsignaturaDTO item) {

	// if (item.getNumHoras() < 0) {
	// JsfUtil.msgAdvert("Las Horas no pueden ser negativas");
	// item.setNumHoras(null);
	// return;
	// }

	listaDocenteAsignatura.add(item);
	for (int i = 0; i < this.listaDocenteAsignatura.size() - 1; i++) {
	    if (this.listaDocenteAsignatura.get(i).getAsignaturaDTO().getId()
		    .equals(item.getAsignaturaDTO().getId())) {
		this.listaDocenteAsignatura.remove(i);
	    }
	}
    }

    public void actualizarDocenteAsig(RowEditEvent event) {
	docenteAsigEditable = (DocenteAsignaturaDTO) event.getObject();
	if (validarAsignaturaDocente()) {
	    AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
	    auditoriaDTO.setFechaModificacion(new Date());
	    auditoriaDTO.setUsuarioModificacion(usuario);
	    docenteAsigEditable.setAuditoriaDTO(auditoriaDTO);
	    docenteAsigEditable.setFaseIesDTO(this.faseIesDTO);
	    try {
		registroServicio
		        .registrarDocenteAsignatura(docenteAsigEditable);
	    } catch (ServicioException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    JsfUtil.msgInfo("La información se almacenó con éxito");
	} else {
	    listaAsignaturasxDocente = obtenerListaAsignaturaDocentes();
	}

    }

    public void cancelarDocenteAsig(RowEditEvent event) {

	listaAsignaturasxDocente = obtenerListaAsignaturaDocentes();
    }

    private boolean validarAsignaturaDocente() {

	if (docenteAsigEditable.getNumHoras() < 0) {
	    JsfUtil.msgAdvert("Las Horas no pueden ser negativas");
	    docenteAsigEditable.setNumHoras(null);
	    return false;
	}

	if (docenteAsigEditable.getFechaInicio() == null) {
	    JsfUtil.msgAdvert("Fecha Inicio es requerida");
	    return false;
	}

	if (docenteAsigEditable.getFechaFin() == null) {
	    JsfUtil.msgAdvert("Fecha Fin es requerida");
	    return false;
	}

	if (docenteAsigEditable.getFechaInicio() != null
	        && docenteAsigEditable.getFechaFin() != null) {
	    if (docenteAsigEditable.getFechaInicio().getTime() > docenteAsigEditable
		    .getFechaFin().getTime()) {
		JsfUtil.msgAdvert("Fecha Inicio no puede ser mayor que Fecha Fin");
		return false;
	    }
	}
	return true;
    }

    private Boolean validarListaDocenteAsignaturaHijos() {

	for (AsignaturaDTO asigHijoNuevo : listaAsignaturasDTO.getTarget()) {

	    for (DocenteAsignaturaDTO docAsigEditablePadre : listaDocenteAsignaturaEditable) {

		if (asigHijoNuevo.getId().equals(
		        docAsigEditablePadre.getAsignaturaDTO().getId())) {
		    JsfUtil.msgError("La asignatura: "
			    + asigHijoNuevo.getNombre()
			    + " ya se encuentra ingresada como asignatura principal, no puede ser una asignatura compartida");
		    return false;
		}

		for (DocenteAsignaturaDTO docAsigHijoPropio : docAsigEditablePadre
		        .getDocenteAsignaturasDTO()) {

		    if (asigHijoNuevo.getId().equals(
			    docAsigHijoPropio.getAsignaturaDTO().getId())) {
			JsfUtil.msgError("La asignatura: "
			        + asigHijoNuevo.getNombre()
			        + " ya se encuentra como asignatura compartida en la asignatura: "
			        + docAsigEditablePadre.getAsignaturaDTO()
			                .getNombre());
			return false;
		    }

		}

	    }

	}

	return true;
    }

    public void guardarDocenteAsignaturaHijos() {

	try {
	    if (listaAsignaturasDTO.getTarget().isEmpty()) {
		JsfUtil.msgAdvert("Seleccione Asignaturas");
		return;
	    }

	    if (!validarListaDocenteAsignaturaHijos())
		return;

	    AuditoriaDTO auditoria = new AuditoriaDTO();
	    auditoria.setFechaModificacion(new Date());
	    auditoria.setUsuarioModificacion(usuarioSistema.getUsuario());

	    DocenteDTO docente = new DocenteDTO();
	    docente.setId(docenteSeleccionado.getId());

	    List<AsignaturaDTO> listaAsignaturas = new ArrayList<AsignaturaDTO>();
	    listaAsignaturas.addAll(listaAsignaturasDTO.getTarget());
	    List<DocenteAsignaturaDTO> listaDocenteAsignatura = new ArrayList<DocenteAsignaturaDTO>();

	    for (AsignaturaDTO asg : listaAsignaturas) {
		DocenteAsignaturaDTO docenteAsigHijoDTO = new DocenteAsignaturaDTO();
		docenteAsigHijoDTO.setId(null);
		docenteAsigHijoDTO.setAsignaturaDTO(asg);
		docenteAsigHijoDTO
		        .setIdDocenteAsignaturaPadre(docenteAsigEditable
		                .getId());
		docenteAsigHijoDTO.setAuditoriaDTO(auditoria);
		docenteAsigHijoDTO.setActivo(true);
		docenteAsigHijoDTO.setFaseIesDTO(faseIesDTO);
		docenteAsigHijoDTO.setOrigenCarga("SISTEMA");
		docenteAsigHijoDTO
		        .setContratacionPeriodoAcademicoDTO(contratacionPeriodoAcademicoDTO);
		docenteAsigHijoDTO.setDocenteDTO(docente);

		listaDocenteAsignatura.add(docenteAsigHijoDTO);

		// registroServicio.registrarDocenteAsignatura(docenteAsigHijoDTO);
	    }

	    docenteAsigEditable
		    .setDocenteAsignaturasDTO(listaDocenteAsignatura);
	    registroServicio.registrarDocenteAsignatura(docenteAsigEditable);
	    obtenerListaAsignaturaDocentesTodos();
	    JsfUtil.msgInfo("Asignaturas compartidas se guardaron correctamente");
	} catch (ServicioException e) {
	    e.printStackTrace();
	}
    }

    public void guardarDocenteAsignatura() {

	AuditoriaDTO auditoria = new AuditoriaDTO();
	auditoria.setFechaModificacion(new Date());

	auditoria.setUsuarioModificacion(usuarioSistema.getUsuario());
	DocenteDTO docente = new DocenteDTO();
	docente.setId(docenteSeleccionado.getId());

	try {
	    LOG.info("listaDocenteAsignatura size: "
		    + listaDocenteAsignatura.size());
	    if (listaDocenteAsignatura.isEmpty()) {
		JsfUtil.msgAdvert("Seleccione la asignatura que dicta el docente");
		return;
	    }
	    for (DocenteAsignaturaDTO docAsig : listaDocenteAsignatura) {
		docAsig.setActivo(true);
		docAsig.setAuditoriaDTO(auditoria);
		docAsig.setFaseIesDTO(faseIesDTO);
		docAsig.setDocenteDTO(docente);
		docAsig.setContratacionPeriodoAcademicoDTO(contratacionPeriodoAcademicoDTO);
		docAsig.setOrigenCarga("SISTEMA");
		docAsig.setDocenteAsignaturasDTO(null);
		if (docAsig.getNumParalelos() == null
		        || docAsig.getNumParalelos().equals(0)) {
		    docAsig.setNumParalelos(1);
		}
		DocenteAsignaturaDTO docenteok = registroServicio
		        .registrarDocenteAsignatura(docAsig);
	    }
	} catch (ServicioException e) {
	    e.printStackTrace();
	}
	obtenerListaAsignaturaDocentesTodos();
	listaDocenteAsignatura.clear();
	JsfUtil.msgInfo("Asignaturas Guardadas Correctamente");
    }

    public void mostrarAsignacionHoras() {

	mostrarAsignacion = true;
	listaAsignaturasxDocente.clear();
	listaAsignaturasxDocente = obtenerListaAsignaturaDocentes();
    }

    public void eliminarDocenteAsignaturaPadre() {

	try {

	    if (alertaFaseRectificacion && alertaUsuarioIes) {
		if (docenteAsigEditable.getId() != null
		        && !docenteAsigEditable.getFaseIesDTO().getTipo()
		                .name().startsWith("RECTIFICACION")) {
		    JsfUtil.msgError("No se puede eliminar este docente en fase de Rectificación.");
		    return;
		}
	    }
	    AuditoriaDTO auditoria = new AuditoriaDTO();
	    auditoria.setFechaModificacion(new Date());
	    auditoria.setUsuarioModificacion(usuarioSistema.getUsuario());
	    docenteAsigEditable.setAuditoriaDTO(auditoria);
	    docenteAsigEditable.setActivo(false);
	    docenteAsigEditable.setFaseIesDTO(faseIesDTO);
	    registroServicio.registrarDocenteAsignatura(docenteAsigEditable);

	    List<DocenteAsignaturaDTO> listaHijosEliminar = new ArrayList<>();
	    listaHijosEliminar.addAll(docenteAsigEditable
		    .getDocenteAsignaturasDTO());

	    if (null != listaHijosEliminar || !listaHijosEliminar.isEmpty()) {

		listaDocenteAsignaturaCompartidaEliminar
		        .addAll(listaHijosEliminar);
		eliminarAsignaturaCompartidaHijos();
	    }
	    obtenerListaAsignaturaDocentesTodos();
	    JsfUtil.msgInfo("Asignatura Eliminada Correctamente");
	} catch (ServicioException e) {
	    e.printStackTrace();
	}
    }

    private List<DocenteAsignaturaDTO> obtenerListaAsignaturaDocentes() {

	List<DocenteAsignaturaDTO> listaAsignatura = new ArrayList<DocenteAsignaturaDTO>();
	try {
	    listaAsignatura = registroServicio.obtenerAsignaturaPorDocente(
		    docenteSeleccionado.getId(),
		    contratacionPeriodoAcademicoDTO.getId());

	    if (null == listaAsignatura)
		listaAsignatura = new ArrayList<DocenteAsignaturaDTO>();

	} catch (ServicioException e) {
	    e.printStackTrace();
	}
	return listaAsignatura;
    }

    private void obtenerListaAsignaturaDocentesTodos() {
	listaAsignaturasxDocente.clear();
	listaDocenteAsignaturaEditable.clear();
	listaAsignaturasxDocente = obtenerListaAsignaturaDocentes();
	listaDocenteAsignaturaEditable = obtenerListaAsignaturaDocentes();
    }

    /**
     * Seteo de usuario automatico
     * 
     * @author eteran
     * @version 09/09/2013 - 16:28:20
     */
    private void contextUsuario() {
	this.usuarioSistema.setUsuario(SecurityContextHolder.getContext()
	        .getAuthentication().getName());
    }

    /**
     * 
     * Carga todas las Ies
     * 
     * @author eteran
     * @version 04/09/2013 - 11:18:18
     */
    public void cargarIes() {
	this.listaIes.clear();

	try {
	    this.listaIes = uicServicio.obtenerIes();
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("No se cargaron las Ies");
	    return;
	}
    }

    /**
     * 
     * Carga la lista de docentes
     * 
     * @author eteran
     * @version 12/09/2013 - 11:40:50
     */
    public void listarDocentes() {
	if (iesSeleccionada == null) {
	    JsfUtil.msgAdvert("No hay ies seleccionada");
	    return;
	}
	cargarDocentes();
    }

    private void cargarDocentes() {
	docentes.clear();
	docentesFiltros.clear();
	DocenteDTO docenteDTO = null;
	if (busqueda) {
	    limpiarListas();
	    busqueda = false;
	    identificacion = null;
	    habilitarSiguiente = true;
	    cargarDocentes();
	    return;
	}

	if (indice < 0 || registros == 0) {
	    docentes.clear();
	    indice = 0;
	    return;
	}
	try {

	    if (listaDocentesTodos.size() <= (numRegistros)) {
		int indiceaux = 0;
		if (listaDocentesTodos.size() != 0) {
		    indiceaux = listaDocentesTodos.size() - 1;
		} else {
		    indiceaux = 0;
		}
		// if (perfil.startsWith("CAR")) {
		// this.listaDocentesTodos.addAll(institutoServicio
		// .obtenerDocentesPorInformacionIes(docenteDTO,
		// informacionIesDTO.getId()));
		// } else {
		this.listaDocentesTodos = registroServicio
		        .obtenerDocentesPorInformacionIes(docenteDTO,
		                informacionIesDTO.getId());
		// }
		List<DocenteDTO> auxLista = new ArrayList<DocenteDTO>();
		auxLista.addAll(listaDocentesTodos);
		if (auxLista.size() != 0) {
		    if (listaDocentesTodos.size() < indice + registros) {
			this.docentes = auxLista.subList(indice,
			        listaDocentesTodos.size());
		    } else {
			this.docentes = auxLista.subList(indice, indice
			        + registros);
		    }
		}
		this.docentesFiltros.addAll(docentes);
	    } else {
		List<DocenteDTO> auxLista = new ArrayList<DocenteDTO>();
		auxLista.addAll(listaDocentesTodos);
		if (auxLista.size() != 0) {
		    if ((indice + registros) > listaDocentesTodos.size()) {
			this.docentes = auxLista.subList(indice,
			        listaDocentesTodos.size());
		    } else {
			this.docentes = auxLista.subList(indice, indice
			        + registros);
		    }
		    this.docentesFiltros.addAll(docentes);
		}
	    }
	    if (docentes.size() < registros) {
		habilitarSiguiente = false;
	    }
	    obtenerTotalDocentes();

	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Se produjo un error al listar docentes. Comuníquese con el Administrador");
	}

    }

    public void buscarDocentePorCedula() {

	if (!identificacion.equals("")) {
	    indice = 0;
	    DocenteDTO docenteDTO = new DocenteDTO();
	    this.docentes.clear();
	    this.listaDocentesTodos.clear();
	    this.docentesFiltros.clear();
	    docenteDTO.setIdentificacion(identificacion);
	    docenteDTO.setIdInformacionIes(this.informacionIesDTO.getId());

	    try {
		// if (this.perfil.startsWith("CAR")) {
		// List<DocenteDTO> aux = institutoServicio
		// .obtenerDocentesPorInformacionIes(docenteDTO, null);
		// if (aux != null && !aux.isEmpty()) {
		// docenteDTO = aux.get(0);
		// }
		// } else {
		docenteDTO = registroServicio
		        .obtenerDocentePorCedula(docenteDTO);
		// }
		if (docenteDTO != null && docenteDTO.getActivo()) {
		    docentes.add(docenteDTO);
		    this.docentesFiltros.add(docenteDTO);
		    this.listaDocentesTodos.add(docenteDTO);

		    JsfUtil.msgInfo("Docente encontrado");
		} else {
		    JsfUtil.msgInfo("Docente no encontrado!");
		}

	    } catch (Exception e) {
		e.printStackTrace();
		JsfUtil.msgError("Falló búsqueda de docente por cédula!");
	    }
	} else {
	    JsfUtil.msgError("Ingrese número de cédula!");
	}
	busqueda = true;
	habilitarSiguiente = true;
    }

    public void limpiarListas() {
	docentes.clear();
	docentesFiltros.clear();
	listaDocentesTodos.clear();
	indice = 0;
	registros = 10;
    }

    public void llenarPueblos(ValueChangeEvent event) {
	if (event != null) {
	    if (event.getNewValue() != null) {
		Long idRegion = new Long(event.getNewValue().toString());
		this.pueblos.clear();
		try {
		    this.pueblos = catalagoServicio
			    .obtenerPueblosPorRegion(idRegion);
		} catch (Exception e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }
	}
    }

    /**
     * 
     * Para controlar la navegacion entre tabs del tabView
     * 
     * @author eteran
     * @version 10/09/2013 - 10:49:28
     * @param event
     */
    public void tomarTab(TabChangeEvent event) {
	this.pestania = event.getTab().getTitle();
	activarTab();
    }

    /**
     * 
     * Activa el tab de tabView segun la pestaña
     * 
     * @author eteran
     * @version 12/09/2013 - 14:40:19
     */
    public void activarTab() {
	if (this.pestania.equals("Datos Personales")) {
	    evidenciaConcepto = false;
	    this.indiceTab = 0;
	}

	if (this.pestania.equals("Relación Laboral")) {
	    evidenciaConcepto = false;
	    this.indiceTab = 1;
	    for (ConceptoDTO conceptoDTO : listaEvidencia) {
		if (conceptoDTO.getOrigen().equals(
		        OrigenInformacionEnum.CONTRATOS.getValor())) {
		    conceptoSeleccionado = conceptoDTO;
		    break;
		}
	    }

	}

	// if (this.pestania.equals("Horas Clase")) {
	// this.indiceTab = 2;
	// }

	if (this.pestania.equals("Formación Profesional")) {
	    evidenciaConcepto = false;
	    this.indiceTab = 2;
	    for (ConceptoDTO conceptoDTO : listaEvidencia) {
		if (conceptoDTO.getOrigen().equals(
		        OrigenInformacionEnum.FORMACION.getValor())) {
		    conceptoSeleccionado = conceptoDTO;
		    break;
		}
	    }
	}

	if (this.pestania.equals("Cursos de Capacitación")) {
	    evidenciaConcepto = false;
	    this.indiceTab = 3;
	    for (ConceptoDTO conceptoDTO : listaEvidencia) {
		if (conceptoDTO.getOrigen().equals(
		        OrigenInformacionEnum.CAPACITACION.getValor())) {
		    conceptoSeleccionado = conceptoDTO;
		    break;
		}
	    }
	}

	if (this.pestania.equals("Cargos Directivos")) {
	    evidenciaConcepto = false;
	    this.indiceTab = 4;
	    for (ConceptoDTO conceptoDTO : listaEvidencia) {
		if (conceptoDTO.getOrigen().equals(
		        OrigenInformacionEnum.CARGOS.getValor())) {
		    conceptoSeleccionado = conceptoDTO;
		    break;
		}
	    }
	}

	if (this.pestania.equals("Experiencia Profesional")) {
	    evidenciaConcepto = false;
	    this.indiceTab = 5;
	    for (ConceptoDTO conceptoDTO : listaEvidencia) {
		if (conceptoDTO.getOrigen().equals(
		        OrigenInformacionEnum.EXPERIENCIA.getValor())) {
		    conceptoSeleccionado = conceptoDTO;
		    break;
		}
	    }
	}

	if (docenteSeleccionado.getId() == null) {
	    this.verTabDatosPersonales = false;
	    this.verTabAsignaturas = true;
	    this.verTabContratacion = true;
	    this.verTabFormacion = true;
	    this.verTabCargosDireccion = true;
	    this.verTabExperienciaProf = true;
	    this.verTabCursos = true;
	} else {
	    this.verTabAsignaturas = false;
	    this.verTabDatosPersonales = false;
	    this.verTabContratacion = false;
	    this.verTabFormacion = false;
	    this.verTabCargosDireccion = false;
	    this.verTabExperienciaProf = false;
	    this.verTabCursos = false;
	}
    }

    /**
     * 
     * @return
     */
    public String regresar() {
	limpiarListas();
	cargarDocentes();
	editarContrato = false;
	return "administracionDocentes?faces-redirect=true";
    }

    /**
     * 
     * ...method doSomethingElse documentation comment...
     * 
     * @author cam
     * @version 19/08/2014 - 11:56:29
     */
    public void cargarEConcepto() {
	try {
	    evidenciaConcepto = true;
	    if (conceptoSeleccionado.getOrigen().equals(
		    OrigenInformacionEnum.CONTRATOS.getValor())) {
		idEvidencia = contratoDocente.getId();
		nombreFichero = iesSeleccionada.getCodigo() + "_"
		        + contratoDocente.getId();

	    } else if (conceptoSeleccionado.getOrigen().equals(
		    OrigenInformacionEnum.EXPERIENCIA.getValor())) {
		idEvidencia = experienciaProfesionalDocente.getId();
		nombreFichero = iesSeleccionada.getCodigo() + "_"
		        + experienciaProfesionalDocente.getId();

	    }

	    else if (conceptoSeleccionado.getOrigen().equals(
		    OrigenInformacionEnum.CAPACITACION.getValor())) {
		idEvidencia = cursoCapacitacion.getId();
		nombreFichero = iesSeleccionada.getCodigo() + "_"
		        + cursoCapacitacion.getId();

	    } else if (conceptoSeleccionado.getOrigen().equals(
		    OrigenInformacionEnum.FORMACION.getValor())) {
		idEvidencia = formacionDocente.getId();
		nombreFichero = iesSeleccionada.getCodigo() + "_"
		        + formacionDocente.getId();

	    }

	    else if (conceptoSeleccionado.getOrigen().equals(
		    OrigenInformacionEnum.CARGOS.getValor())) {
		idEvidencia = cargoDirectivoDocente.getId();
		nombreFichero = iesSeleccionada.getCodigo() + "_"
		        + cargoDirectivoDocente.getId();

	    }

	    listaEvidenciaConcepto = institutosServicio
		    .obtenerEvidenciasDeIesPorIdConceptoEIdTabla(
		            iesSeleccionada.getId(),
		            conceptoSeleccionado.getId(), idEvidencia,
		            conceptoSeleccionado.getOrigen());

	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
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

    /**
     * @author jhomara
     * @version 15/05/2014
     */
    public void cargarCategorias(ValueChangeEvent ev) {
	categorias = new ArrayList<CategoriaTitularEnum>();
	if (ev != null && ev.getNewValue() != null) {
	    String valor = ev.getNewValue().toString();
	    if (valor
		    .equals(RelacionIESEnum.CONTRATO_SIN_RELACION_DE_DEPENDENCIA
		            .toString())) {
		for (CategoriaTitularEnum cat : CategoriaTitularEnum.values()) {
		    if (cat.getValue().startsWith("TITULAR")) {
			continue;
		    } else {
			this.categorias.add(cat);
		    }
		}
	    } else {
		categorias = Arrays.asList(CategoriaTitularEnum.values());
	    }
	}
    }

    /**
     * 
     * Limpia el objeto iesSeleccionada
     * 
     * @author eteran
     * @version 03/09/2013 - 16:24:46
     */
    public void cancelarSeleccionIes() {
	if (this.pestania.equals("Formacion")) {
	    this.iesFormacion = new IesDTO();
	}
    }

    /**
     * 
     * Inicializa el objeto docente
     * 
     * @author eteran
     * @version 12/09/2013 - 14:41:17
     * @param event
     */
    public void iniciarNuevoDocente(ActionEvent event) {
	this.docenteSeleccionado = new DocenteDTO();
	this.accion = "Guardar";
	this.indiceTab = 0;
	cargarPaises();
    }

    private void cargarPaises() {
	this.paises.clear();
	try {
	    this.paises = catalagoServicio.obtenerPaises();
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("No se pudo cargar los paises");
	    return;
	}
    }

    private void cargarPeriodos() {
	try {
	    this.periodosAcademicos.clear();

	    periodosAcademicos = registroServicio
		    .obtenerPeriodosMatricula(informacionIesDTO.getId());

	} catch (Exception e) {
	    LOG.log(Level.INFO,
		    "Error al obtener Periodos, método cargarPeriodos");
	    LOG.log(Level.SEVERE, e.getMessage(), e);
	}
    }

    private void nuevoRedirect() {
	if (this.docenteSeleccionado == null) {
	    return;
	}

	this.contrataciones.clear();
	this.formaciones.clear();
	this.listaExperienciaProfesionalDTO.clear();
	this.cargosDirectivos.clear();
	this.cursos.clear();
	List<CursoCapacitacionDTO> cursosDTO;
	try {
	    cargarDatosIniciales();

	    if (docenteSeleccionado.getId() == null) {
		docenteSeleccionado = new DocenteDTO();
		idPais = -99L;
	    }

	    if (this.docenteSeleccionado.getId() != null) {
		this.personaDocente = new PersonaDTO();

		try {
		    PropertyUtils.copyProperties(personaDocente,
			    docenteSeleccionado);
		} catch (IllegalAccessException e1) {
		    e1.printStackTrace();
		} catch (InvocationTargetException e1) {
		    e1.printStackTrace();
		} catch (NoSuchMethodException e1) {
		    e1.printStackTrace();
		}

		cargarPaises();

		if (docenteSeleccionado.getIesDTO() == null) {
		    personaDocente.setIesDTO(this.iesSeleccionada);
		    docenteSeleccionado.setIesDTO(this.iesSeleccionada);
		}

		if (docenteSeleccionado.getPaisOrigen() != null) {
		    idPais = docenteSeleccionado.getPaisOrigen().getId();
		} else {
		    idPais = -99L;
		}

		if (docenteSeleccionado.getDiscapacidad() != null) {
		    discapacidad = docenteSeleccionado.getDiscapacidad()
			    .getValue();
		} else {
		    docenteSeleccionado
			    .setDiscapacidad(DiscapacidadEnum.NINGUNA);
		    discapacidad = docenteSeleccionado.getDiscapacidad()
			    .getValue();
		}

		if (docenteSeleccionado.getSexo() != null) {
		    genero = docenteSeleccionado.getSexo().getValue();
		} else {
		    genero = docenteSeleccionado.getSexo().getValue();
		}

		if (docenteSeleccionado.getContratacionDTO() != null) {

		    for (int i = 0; i < docenteSeleccionado
			    .getContratacionDTO().size(); i++) {
			if (docenteSeleccionado.getContratacionDTO().get(i)
			        .getActivo()) {
			    contrataciones.add(docenteSeleccionado
				    .getContratacionDTO().get(i));
			}
		    }
		    contrataciones.removeAll(Collections.singleton(null));
		}

		if (this.docenteSeleccionado.getFormacionDTO() != null) {

		    List<FormacionProfesionalDTO> fdto = new ArrayList<>();

		    for (int i = 0; i < this.docenteSeleccionado
			    .getFormacionDTO().size(); i++) {
			if (this.docenteSeleccionado.getFormacionDTO().get(i)
			        .getActivo()) {
			    fdto.add(docenteSeleccionado.getFormacionDTO().get(
				    i));
			}
		    }
		    for (FormacionProfesionalDTO formaDTO : fdto) {
			for (IesDTO ies : listaIes) {
			    if (formaDTO.getNombreIesInternacional().equals(
				    ies.getNombre())) {
				IesDTO iesDTO = new IesDTO();
				iesDTO.setId(ies.getId());
				iesDTO.setNombre(ies.getNombre());
				iesDTO.setCodigo(ies.getCodigo());
				formaDTO.setIes(this.iesSeleccionada);
			    }
			}
			formaciones.add(formaDTO);
		    }
		}

		if (this.docenteSeleccionado.getCursosCapacitacionDTO() != null) {

		    cursosDTO = registroServicio.obtenerCursosPorDocenteEIes(
			    docenteSeleccionado.getId(), informacionIesDTO
			            .getIes().getId());

		    for (int i = 0; i < cursosDTO.size(); i++) {
			if (cursosDTO.get(i).getActivo()) {
			    cursos.add(cursosDTO.get(i));
			}
		    }

		    cursos.removeAll(Collections.singleton(null));
		}

		if (this.docenteSeleccionado.getCargosAcademicosDTO() != null) {

		    for (int i = 0; i < this.docenteSeleccionado
			    .getCargosAcademicosDTO().size(); i++) {
			if (this.docenteSeleccionado.getCargosAcademicosDTO()
			        .get(i).getActivo()) {
			    cargosDirectivos.add(this.docenteSeleccionado
				    .getCargosAcademicosDTO().get(i));
			}
		    }

		    cargosDirectivos.removeAll(Collections.singleton(null));

		    for (CargoAcademicoDTO cargodir : cargosDirectivos) {
			if (cargodir.getAutoridadAcademica() == null) {
			    cargodir.setAutoridadAcademica(AutoridadAcademicaEnum
				    .parse(cargo));
			}
		    }
		}

		if (this.docenteSeleccionado.getExperienciaProfesionalDTO() != null) {
		    for (int i = 0; i < docenteSeleccionado
			    .getExperienciaProfesionalDTO().size(); i++) {
			if (docenteSeleccionado.getExperienciaProfesionalDTO()
			        .get(i).getActivo()) {
			    listaExperienciaProfesionalDTO
				    .add(docenteSeleccionado
				            .getExperienciaProfesionalDTO()
				            .get(i));
			}
		    }

		    listaExperienciaProfesionalDTO.removeAll(Collections
			    .singleton(null));
		}

	    }
	    activarTab();
	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    public void nuevo() {

	nuevoRedirect();

	ExternalContext ec = FacesContext.getCurrentInstance()
	        .getExternalContext();
	try {
	    ec.redirect("/" + ec.getContextName() + "/docentes/docente.jsf");
	    // DocenteAsignaturaController asignaturaController =
	    // (DocenteAsignaturaController) FacesContext
	    // .getCurrentInstance().getExternalContext().getSessionMap()
	    // .get("docenteAsigController");
	    // if (asignaturaController != null) {
	    // asignaturaController.setInfoIes(informacionIesDTO);
	    // asignaturaController.setInfoCarreraDTO(informacionCarreraDTO);
	    // asignaturaController.getAsignaturasDTO().clear();
	    // asignaturaController.setSeleccion(docenteSeleccionado);
	    // periodosAcademicos = registroServicio
	    // .obtenerPeriodosMatricula(this.informacionIesDTO
	    // .getId());
	    // }
	    periodosAcademicos = registroServicio
		    .obtenerPeriodosMatricula(this.informacionIesDTO.getId());

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    public void cancelarIesFormacion() {
	this.iesFormacion = new IesDTO();
    }

    public void eliminarDocente() {

	try {

	    if (alertaFaseRectificacion && alertaUsuarioIes) {
		if (docenteSeleccionado.getId() != null
		        && !docenteSeleccionado.getFaseIesDTO().getTipo()
		                .name().startsWith("RECTIFICACION")) {
		    JsfUtil.msgError("No se puede eliminar este docente en fase de Rectificación.");
		    return;
		}
	    }

	    if (docenteSeleccionado.getMotivoSalida() == null
		    || docenteSeleccionado.getMotivoSalida().trim().isEmpty()) {
		JsfUtil.msgError("Ingrese el motivo de la salida.");
		return;

	    }

	    this.docenteSeleccionado = registroServicio
		    .obtenerDocentePorId(docenteSeleccionado.getId());
	    for (ContratacionDTO cont : this.docenteSeleccionado
		    .getContratacionDTO()) {
		if (cont.getFechaFin() == null) {
		    JsfUtil.msgError("Es necesario que cierre todos los contratos para identificar la fecha de salida del docente.");
		    return;
		}
	    }
	    docenteSeleccionado.setFaseIesDTO(this.faseIesDTO);
	    registroServicio.eliminarDocente(docenteSeleccionado);
	    docentes.remove(docenteSeleccionado);
	    limpiarListas();
	    cargarDocentes();
	    JsfUtil.msgInfo("Docente eliminado correctamente");

	    RequestContext context = RequestContext.getCurrentInstance();
	    context.execute("dialogEliminarDoc.hide()");
	} catch (ServicioException e) {
	    JsfUtil.msgError(e.getMessage());
	} catch (Exception e) {
	    JsfUtil.msgError("No se pudo eliminar al docente seleccionado");
	}
    }

    public void eliminarContratacion() {
	if (alertaFaseRectificacion && alertaUsuarioIes) {
	    if (contratoDocente.getId() != null
		    && !contratoDocente.getFaseIesDTO().getTipo().name()
		            .startsWith("RECTIFICACION")) {
		JsfUtil.msgError("No se puede eliminar este registro en fase de Rectificación.");
		return;
	    }
	}

	if (contrataciones.size() <= 1) {
	    JsfUtil.msgError("No se puede eliminar la contratación, debe existir al menos una contratación");
	    return;
	}
	boolean eliminar = false;
	for (int i = 0; i < contrataciones.size(); i++) {
	    if (contratoDocente.getId() != null
		    && contratoDocente.getId().equals(
		            contrataciones.get(i).getId())) {
		contrataciones.get(i).setActivo(false);

		personaDocente.setOrigenCarga(ORIGEN_CARGA);
		personaDocente.setIesDTO(this.iesSeleccionada);
		eliminar = true;
		break;
	    }
	}

	if (eliminar) {
	    try {
		contratoDocente.setActivo(false);
		contratoDocente.setPersonaDTO(docenteSeleccionado);
		registroServicio.registrarContratacionDTO(contratoDocente);
		contrataciones.remove(contratoDocente);

		contratacionesPeriodoAcademico = registroServicio
		        .obtenerContratacionesPeriodoAcademico(contratoDocente
		                .getId());
		for (ContratacionPeriodoAcademicoDTO contratoPeriodoELiminar : contratacionesPeriodoAcademico) {
		    contratoPeriodoELiminar.setActivo(Boolean.FALSE);
		}

		registroServicio
		        .registrarContratacionesPeriodoAcademico(contratacionesPeriodoAcademico);
		JsfUtil.msgInfo("El contrato se elimino correctamente");
	    } catch (Exception e) {
		e.printStackTrace();
		JsfUtil.msgError("No se pudo eliminar el contrato seleccionado");
		return;
	    }
	}

    }

    public void eliminarFormacion() {
	if (alertaFaseRectificacion && alertaUsuarioIes) {
	    if (formacionDocente.getId() != null
		    && !formacionDocente.getFaseIesDTO().getTipo().name()
		            .startsWith("RECTIFICACION")) {
		JsfUtil.msgError("No se puede eliminar este registro en fase de Rectificación.");
		return;
	    }
	}

	boolean eliminar = false;
	for (int i = 0; i < formaciones.size(); i++) {
	    if (formacionDocente.getId() != null
		    && formacionDocente.getId().equals(
		            formaciones.get(i).getId())) {
		formaciones.get(i).setActivo(false);
		personaDocente.setOrigenCarga(ORIGEN_CARGA);
		personaDocente.setIesDTO(this.iesSeleccionada);
		eliminar = true;
		break;
	    }
	}

	if (eliminar) {
	    try {
		formacionDocente.setActivo(false);
		formacionDocente.setPersona(this.docenteSeleccionado);
		registroServicio.registrarFormacion(formacionDocente);
		formaciones.remove(formacionDocente);
		JsfUtil.msgInfo("El registro se eliminó correctamente");
	    } catch (Exception e) {
		e.printStackTrace();
		JsfUtil.msgError("No se pudo eliminar el registro seleccionado");
		return;
	    }
	}

    }

    public void eliminarCurso() {

	if (alertaFaseRectificacion && alertaUsuarioIes) {
	    if (cursoCapacitacion.getId() != null
		    && !cursoCapacitacion.getFaseIesDTO().getTipo().name()
		            .startsWith("RECTIFICACION")) {
		JsfUtil.msgError("No se puede eliminar este registro en fase de Rectificación.");
		return;
	    }
	}

	boolean eliminar = false;
	for (int i = 0; i < cursos.size(); i++) {
	    if (cursoCapacitacion.getId() != null
		    && cursoCapacitacion.getId().equals(cursos.get(i).getId())) {
		cursos.get(i).setActivo(false);
		personaDocente.setOrigenCarga(ORIGEN_CARGA);
		personaDocente.setIesDTO(this.iesSeleccionada);
		eliminar = true;
		break;
	    }
	}

	if (eliminar) {
	    try {
		cursoCapacitacion.setActivo(false);
		cursoCapacitacion.setDocenteDTO(this.docenteSeleccionado);
		registroServicio.registrarCapacitacion(cursoCapacitacion);
		cursos.remove(cursoCapacitacion);
		JsfUtil.msgInfo("El registro se elimino correctamente");
	    } catch (Exception e) {
		e.printStackTrace();
		JsfUtil.msgError("No se pudo eliminar el curso seleccionado");
		return;
	    }
	}
    }

    public void eliminarCargoDirectivo() {
	if (alertaFaseRectificacion && alertaUsuarioIes) {
	    if (cargoDirectivoDocente.getId() != null
		    && !cargoDirectivoDocente.getFaseIesDTO().getTipo().name()
		            .startsWith("RECTIFICACION")) {
		JsfUtil.msgError("No se puede eliminar este registro en fase de Rectificación.");
		return;
	    }
	}

	boolean eliminar = false;
	for (int i = 0; i < cargosDirectivos.size(); i++) {
	    if (cargoDirectivoDocente.getId() != null
		    && cargoDirectivoDocente.getId().equals(
		            cargosDirectivos.get(i).getId())) {
		cargosDirectivos.get(i).setActivo(false);
		personaDocente.setOrigenCarga(ORIGEN_CARGA);
		personaDocente.setIesDTO(this.iesSeleccionada);
		eliminar = true;
		break;
	    }
	}

	if (eliminar) {
	    try {
		cargoDirectivoDocente.setActivo(false);
		cargoDirectivoDocente.setDocenteDTO(this.docenteSeleccionado);
		registroServicio.registrarCargoAcademico(cargoDirectivoDocente);
		cargosDirectivos.remove(cargoDirectivoDocente);
		JsfUtil.msgInfo("El registro se elimino correctamente");
	    } catch (Exception e) {
		e.printStackTrace();
		JsfUtil.msgError("No se pudo eliminar el cargo directivo seleccionado");
		return;
	    }
	}

    }

    public void eliminarExperienciaProf() {
	if (alertaFaseRectificacion && alertaUsuarioIes) {
	    if (experienciaProfesionalDocente.getId() != null
		    && !experienciaProfesionalDocente.getFaseIesDTO().getTipo()
		            .name().startsWith("RECTIFICACION")) {
		JsfUtil.msgError("No se puede eliminar este registro en fase de Rectificación.");
		return;
	    }
	}

	boolean eliminar = false;
	for (int i = 0; i < listaExperienciaProfesionalDTO.size(); i++) {
	    if (experienciaProfesionalDocente.getId() != null
		    && experienciaProfesionalDocente.getId().equals(
		            listaExperienciaProfesionalDTO.get(i).getId())) {
		listaExperienciaProfesionalDTO.get(i).setActivo(false);
		personaDocente.setOrigenCarga(ORIGEN_CARGA);
		personaDocente.setIesDTO(this.iesSeleccionada);
		eliminar = true;
		break;
	    }
	}

	if (eliminar) {
	    try {
		experienciaProfesionalDocente.setActivo(false);
		AuditoriaDTO auditoria = new AuditoriaDTO();
		auditoria.setFechaModificacion(new Date());
		auditoria.setUsuarioModificacion(usuarioSistema.getUsuario());
		experienciaProfesionalDocente.setAuditoriaDTO(auditoria);

		experienciaProfesionalDocente
		        .setDocenteDTO(this.docenteSeleccionado);
		registroServicio
		        .registrarExperienciaProfDocente(experienciaProfesionalDocente);
		listaExperienciaProfesionalDTO
		        .remove(experienciaProfesionalDocente);
		JsfUtil.msgInfo("El registro se eliminó correctamente");
	    } catch (Exception e) {
		e.printStackTrace();
		JsfUtil.msgError("No se pudo eliminar el sabatico seleccionado");
		return;
	    }
	}

    }

    public void llenarAniosContrato() {
	Calendar cal = Calendar.getInstance();
	if (contratoDocente.getFechaInicio() != null) {
	    cal.setTime(contratoDocente.getFechaInicio());
	    int anio1 = cal.get(Calendar.YEAR);
	    if (contratoDocente.getFechaFin() != null) {
		cal.setTime(contratoDocente.getFechaFin());
	    } else {
		cal.setTime(new Date());
	    }
	    int anio2 = cal.get(Calendar.YEAR);
	    int tamanio = 1;
	    if ((anio2 - anio1) == 0) {
		aniosContrato = new Integer[tamanio];
	    } else {
		aniosContrato = new Integer[tamanio + anio2 - anio1];
	    }
	    aniosContrato[0] = anio1;
	    for (int i = 1; i <= anio2 - anio1; i++) {
		aniosContrato[i] = anio1 + i;
	    }
	}

    }

    /**
     * 
     * Se toma la discapacidad del combo discapacidades
     * 
     * @author eteran
     * @version 12/09/2013 - 14:42:46
     * @param event
     */
    public void tomarDiscapacidad(ValueChangeEvent event) {
	this.discapacidad = (String) event.getNewValue();
    }

    /**
     * 
     * Se toma el genero del combo sexo
     * 
     * @author eteran
     * @version 12/09/2013 - 14:43:31
     * @param event
     */
    public void tomarGenero(ValueChangeEvent event) {
	this.genero = (String) event.getNewValue();
    }

    public void tomarAreaConocimiento() {
	cargarSubareasConocimiento(idArea);

	if (this.idArea.equals(-99L)) {
	    subareas.clear();
	}
    }

    public void tomarSubArea(ValueChangeEvent event) {
	this.idSubArea = (Long) event.getNewValue();

	if (idSubArea != -99L) {
	    SubAreaConocimientoDTO sub = new SubAreaConocimientoDTO();
	    sub.setId(this.idSubArea);
	}
    }

    private void cargarSubareasConocimiento(Long idArea) {
	this.subareas.clear();
	try {
	    this.subareas = catalagoServicio
		    .obtenerSubAreasConocimientoPorArea(idArea);
	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("No se pudo cargar las subareas de conocimiento");
	    return;
	}
    }

    public void tomarTipoProg(ValueChangeEvent event) {
	this.tipoProg = (String) event.getNewValue();
    }

    public void guardar() {

	if (alertaFaseRectificacion && alertaUsuarioIes) {
	    if (docenteSeleccionado.getId() != null
		    && !docenteSeleccionado.getFaseIesDTO().getTipo().name()
		            .startsWith("RECTIFICACION")) {
		JsfUtil.msgError("No se puede editar este docente en fase de Rectificación.");
		return;
	    }
	}

	if (docenteSeleccionado == null) {
	    return;
	}
	if (validarDatosPersonales() == false) {
	    return;
	}

	docenteSeleccionado.setOrigenCarga(ORIGEN_CARGA);
	docenteSeleccionado.setIesDTO(this.iesSeleccionada);
	docenteSeleccionado.setFaseIesDTO(this.faseIesDTO);

	PersonaDTO perdoc = null;

	try {
	    perdoc = new PersonaDTO();
	    PropertyUtils.copyProperties(perdoc, docenteSeleccionado);
	} catch (IllegalAccessException e1) {
	    e1.printStackTrace();
	} catch (InvocationTargetException e1) {
	    e1.printStackTrace();
	} catch (NoSuchMethodException e1) {
	    e1.printStackTrace();
	}

	personaDocente = perdoc;
	personaDocente.setIesDTO(this.iesSeleccionada);
	PaisDTO nac = new PaisDTO();
	nac.setId(idPais);
	this.personaDocente.setPaisOrigen(nac);
	this.personaDocente.setDiscapacidad(DiscapacidadEnum
	        .parse(this.discapacidad));
	// this.personaDocente.setEtnia(EtniaEnum.parse(this.etniaSeleccionada));

	personaDocente.setSexo(GeneroEnum.parse(genero));
	personaDocente.setIesDTO(this.iesSeleccionada);
	personaDocente.setFaseIesDTO(faseIesDTO);

	DocenteDTO resultado = null;
	try {
	    // if (pestania.equals("Datos Personales")) {

	    if (docenteSeleccionado.getId() == null) {
		personaDocente.setActivo(true);
		if (contrataciones.isEmpty()) {
		    JsfUtil.msgAdvert("Ingrese los datos de contratación.");
		    indiceTab = 1;
		    verTabContratacion = false;
		    return;
		}
		docenteSeleccionado.setActivo(Boolean.TRUE);
		resultado = registroServicio.crearActualizar(personaDocente,
		        docenteSeleccionado, usuarioSistema);
		docenteSeleccionado.setId(resultado.getId());
		personaDocente.setId(resultado.getId());
	    } else {
		resultado = registroServicio.crearActualizar(personaDocente,
		        docenteSeleccionado, usuarioSistema);
		docentes.add(docenteSeleccionado);
		JsfUtil.msgInfo("Datos almacenados con éxito");

	    }
	} catch (Exception e) {
	    JsfUtil.msgError("No se puede almacenar el registro.");
	}
	if (docenteSeleccionado.getId() != null) {
	    activarTab();
	}
    }

    public boolean validarContratoPeriodosAcademicos() {
	Calendar fechaInicioContrato = Calendar.getInstance();
	fechaInicioContrato.setTime(contratoDocente.getFechaInicio());
	fechaInicioContrato = asignarHoraCero(fechaInicioContrato);

	Calendar fechaFinContrato = Calendar.getInstance();
	if (contratoDocente.getFechaFin() != null) {
	    fechaFinContrato.setTime(contratoDocente.getFechaFin());
	    fechaFinContrato = asignarHoraCero(fechaFinContrato);
	} else {
	    fechaFinContrato = null;
	}

	for (ContratacionPeriodoAcademicoDTO contratacion : contratacionesPeriodoAcademico) {
	    Calendar fechaInicioPeriodo = Calendar.getInstance();
	    fechaInicioPeriodo.setTime(contratacion.getPeriodoAcademicoDTO()
		    .getFechaInicioPeriodo());
	    fechaInicioPeriodo = asignarHoraCero(fechaInicioPeriodo);

	    Calendar fechaFinPeriodo = Calendar.getInstance();
	    fechaFinPeriodo.setTime(contratacion.getPeriodoAcademicoDTO()
		    .getFechaFinPeriodo());
	    fechaFinPeriodo = asignarHoraCero(fechaFinPeriodo);
	    if (contratoDocente.getFechaFin() != null) {
		if ((fechaInicioPeriodo.compareTo(fechaInicioContrato) < 0 && fechaFinPeriodo
		        .compareTo(fechaInicioContrato) <= 0)
		        || (fechaInicioPeriodo.compareTo(fechaFinContrato) >= 0 && fechaFinPeriodo
		                .compareTo(fechaFinContrato) > 0)) {
		    if (!alertaEvaluador) {
			JsfUtil.msgError("El periodo: "
			        + contratacion.getPeriodoAcademicoDTO()
			        + ", se encuentra fuera de las fechas de contrato.");
			return false;
		    } else {
			if (contratacion.getAceptadoEvaluador()) {
			    JsfUtil.msgError("Debe invalidar el periodo académico: "
				    + contratacion.getPeriodoAcademicoDTO()
				    + " para guardar el contrato.");
			    return false;
			}
		    }
		}
	    } else {
		if (fechaInicioPeriodo.compareTo(fechaInicioContrato) < 0
		        && fechaFinPeriodo.compareTo(fechaInicioContrato) <= 0) {
		    if (!alertaEvaluador) {
			JsfUtil.msgError("El periodo: "
			        + contratacion.getPeriodoAcademicoDTO()
			        + ", se encuentra fuera de las fechas de contrato.");
			return false;
		    } else {
			if (contratacion.getAceptadoEvaluador()) {
			    JsfUtil.msgError("Debe invalidar el periodo académico: "
				    + contratacion.getPeriodoAcademicoDTO()
				    + " para guardar el contrato.");
			    return false;
			}
		    }
		}
	    }
	}
	return true;
    }

    public boolean validarNuevoPeriodoAcademico() {
	Calendar fechaInicioContrato = Calendar.getInstance();
	fechaInicioContrato.setTime(contratoDocente.getFechaInicio());
	fechaInicioContrato = asignarHoraCero(fechaInicioContrato);

	Calendar fechaFinContrato = Calendar.getInstance();
	if (contratoDocente.getFechaFin() != null) {
	    fechaFinContrato.setTime(contratoDocente.getFechaFin());
	    fechaFinContrato = asignarHoraCero(fechaFinContrato);
	} else {
	    fechaFinContrato = null;
	}

	Calendar fechaInicioPeriodo = Calendar.getInstance();
	fechaInicioPeriodo.setTime(contratacionPeriodoAcademicoDTO
	        .getPeriodoAcademicoDTO().getFechaInicioPeriodo());
	fechaInicioPeriodo = asignarHoraCero(fechaInicioPeriodo);

	Calendar fechaFinPeriodo = Calendar.getInstance();
	fechaFinPeriodo.setTime(contratacionPeriodoAcademicoDTO
	        .getPeriodoAcademicoDTO().getFechaFinPeriodo());
	fechaFinPeriodo = asignarHoraCero(fechaFinPeriodo);
	if (contratoDocente.getFechaFin() != null) {
	    if ((fechaInicioPeriodo.compareTo(fechaInicioContrato) < 0 && fechaFinPeriodo
		    .compareTo(fechaInicioContrato) <= 0)
		    || (fechaInicioPeriodo.compareTo(fechaFinContrato) >= 0 && fechaFinPeriodo
		            .compareTo(fechaFinContrato) > 0)) {
		if (!alertaEvaluador) {
		    JsfUtil.msgError("El periodo: "
			    + contratacionPeriodoAcademicoDTO
			            .getPeriodoAcademicoDTO()
			    + ", se encuentra fuera de las fechas de contrato.");
		    return false;
		} else {
		    if (contratacionPeriodoAcademicoDTO.getAceptadoEvaluador()) {
			JsfUtil.msgError("Debe invalidar el periodo académico: "
			        + contratacionPeriodoAcademicoDTO
			                .getPeriodoAcademicoDTO()
			        + " para guardar el contrato.");
			return false;
		    }
		}
	    }
	} else {
	    if (fechaInicioPeriodo.compareTo(fechaInicioContrato) < 0
		    && fechaFinPeriodo.compareTo(fechaInicioContrato) <= 0) {
		if (!alertaEvaluador) {
		    JsfUtil.msgError("El periodo: "
			    + contratacionPeriodoAcademicoDTO
			            .getPeriodoAcademicoDTO()
			    + ", se encuentra fuera de las fechas de contrato.");
		    return false;
		} else {
		    if (contratacionPeriodoAcademicoDTO.getAceptadoEvaluador()) {
			JsfUtil.msgError("Debe invalidar el periodo académico: "
			        + contratacionPeriodoAcademicoDTO
			                .getPeriodoAcademicoDTO()
			        + " para guardar el contrato.");
			return false;
		    }
		}
	    }
	}

	return true;
    }

    public void guardarContratoPeriodosAcademicos() {
	try {

	    for (ContratacionPeriodoAcademicoDTO contratacion : contratacionesPeriodoAcademico) {
		contratacion.getContratacionDTO()
		        .setId(contratoDocente.getId());
	    }
	    contratacionesPeriodoAcademico
		    .addAll(contratacionesPeriodoEliminar);
	    contratacionesPeriodoAcademico.addAll(horasNoAcademicasEliminar);
	    registroServicio
		    .registrarContratacionesPeriodoAcademico(contratacionesPeriodoAcademico);

	    contratacionesPeriodoAcademico = registroServicio
		    .obtenerContratacionesPeriodoAcademico(contratoDocente
		            .getId());
	    // activarTab();

	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ocurrió un error al guardar el registro.");
	}
    }

    public void guardarContrato() {
	LOG.info("guardar contrato");

	if (alertaFaseRectificacion && alertaUsuarioIes) {
	    if (contratoDocente.getId() != null
		    && !contratoDocente.getFaseIesDTO().getTipo().name()
		            .startsWith("RECTIFICACION")) {
		JsfUtil.msgError("No se puede editar este docente en fase de Rectificación.");
		return;
	    }
	}

	if (valicarCamposContratacion() == false) {
	    return;
	}
	boolean repetido = false;
	boolean nuevo = false;
	for (ContratacionDTO conDTO : contrataciones) {
	    if (conDTO.getNumeroContrato().equalsIgnoreCase(
		    contratoDocente.getNumeroContrato())) {
		if (contratoDocente.getId() == null) {
		    repetido = true;
		}
		break;
	    }
	}

	if (repetido) {
	    JsfUtil.msgError("El número de contrato está duplicado");
	    return;
	} else {
	    clienteId = "";
	}

	personaDocente.getContratacionDTO().clear();
	// contratoDocente.setCategoria(CategoriaTitularEnum.parse(categoria));
	contratoDocente.setFaseIesDTO(faseIesDTO);
	if (alertaEvaluador) {
	    contratoDocente.setVerificarEvidencia(false);
	}
	contratoDocente.setActivo(true);

	try {
	    if (docenteSeleccionado.getId() == null) {
		if (contratacionesPeriodoAcademico.isEmpty()) {
		    JsfUtil.msgAdvert("Ingrese los datos de las horas de contratación por periodo.");
		    indiceTab = 1;
		    verTabContratacion = false;
		    return;
		}
		if (contratoDocente.getId() != null) {
		    if (!validarContratoPeriodosAcademicos()) {
			return;
		    }
		}
		contrataciones.add(contratoDocente);
		guardar();
		if (FacesContext.getCurrentInstance().getMessages().hasNext()) {
		    return;
		}
		personaDocente.setId(docenteSeleccionado.getId());
		contratoDocente.setPersonaDTO(personaDocente);
		contratoDocente.setFaseIesDTO(faseIesDTO);
		AuditoriaDTO auditoria = new AuditoriaDTO();
		auditoria.setFechaModificacion(new Date());
		auditoria.setUsuarioModificacion(usuarioSistema.getUsuario());
		contratoDocente.setAuditoriaDTO(auditoria);
		contratoDocente.setIdInformacionIes(this.informacionIesDTO
		        .getId());
		contratoDocente.setFaseIesDTO(faseIesDTO);
		contratoDocente = registroServicio
		        .registrarContratacionDTO(contratoDocente);
		verTabAsignaturas = false;
		// verTabFormacion = true;
		indiceTab = 1;
		editarContrato = true;
		JsfUtil.msgInfo("La información se almacenó con éxito");
		guardarContratoPeriodosAcademicos();
		activarTab();

	    } else {
		if (!alertaEvaluador) {
		    if (contratacionesPeriodoAcademico.isEmpty()) {
			JsfUtil.msgAdvert("Ingrese los datos de las horas de contratación por periodo.");
			indiceTab = 1;
			verTabContratacion = false;
			return;
		    }
		}

		if (contratoDocente.getId() == null) {
		    nuevo = true;
		}
		AuditoriaDTO auditoria = new AuditoriaDTO();
		auditoria.setFechaModificacion(new Date());
		auditoria.setUsuarioModificacion(usuario);
		contratoDocente.setFaseIesDTO(faseIesDTO);
		contratoDocente.setAuditoriaDTO(auditoria);
		contratoDocente.setIdInformacionIes(this.informacionIesDTO
		        .getId());
		if (validarContratoPeriodosAcademicos()) {
		    contratoDocente = registroServicio
			    .registrarContratacionDTO(contratoDocente);
		    guardarContratoPeriodosAcademicos();
		    if (nuevo) {
			contrataciones.add(contratoDocente);
			JsfUtil.msgInfo("El registro se almacenó con éxito");
		    } else {
			JsfUtil.msgInfo("El registro ha sido actualizado con éxito");
		    }
		    personaDocente.getContratacionDTO().addAll(contrataciones);
		    editarContrato = true;
		}

	    }

	} catch (ServicioException e) {
	    JsfUtil.msgError("Ocurrió un error al guardar el registro.");
	    return;
	} catch (Exception e) {
	    JsfUtil.msgError("Error al guardar, consulte al administrador."
		    + e.getMessage());
	    e.printStackTrace();
	}

	// resultado = guardarPestania();

    }

    public void guardarFormacion() {
	try {
	    RequestContext context = RequestContext.getCurrentInstance();
	    context.addCallbackParam("cerrarVentana", false);
	    if (validarCamposFormacion() == false) {
		return;
	    }

	    docenteSeleccionado.getFormacionDTO().clear();
	    personaDocente.getFormacionDTO().clear();

	    LOG.info("1");
	    formacionDocente.setIes(this.iesSeleccionada);
	    LOG.info("2");
	    // AUDITORIA
	    AuditoriaDTO audit = new AuditoriaDTO();
	    audit.setUsuarioModificacion(this.usuarioSistema.getUsuario());
	    audit.setFechaModificacion(new Date());
	    formacionDocente.setAuditoriaDTO(audit);
	    LOG.info("3");
	    LOG.info("FASE IES: " + this.faseIesDTO.getId());
	    formacionDocente.setFaseIesDTO(faseIesDTO);
	    if (alertaEvaluador) {
		formacionDocente.setVerificarEvidencia(false);
	    }
	    LOG.info("4");
	    if (iesFormacion == null || iesFormacion.getId() == null) {
		formacionDocente.setIes(null);
	    }
	    for (PaisDTO pais : this.paises) {
		if (pais.getId().equals(idPaisFormacion)) {
		    formacionDocente.setPais(pais);
		    break;
		}
	    }

	    boolean nuevo = false;
	    if (formacionDocente.getId() == null) {
		formacionDocente.setActivo(true);
		nuevo = true;
	    }

	    LOG.info("5");
	    if (gradoSeleccionado.getId() == null
		    || gradoSeleccionado.getId().equals(0L)
		    || gradoSeleccionado.getId().equals(-1L)) {
		formacionDocente.setGrado(null);
	    } else {
		formacionDocente.setGrado(gradoSeleccionado);
	    }
	    LOG.info("6");
	    formacionDocente = registroServicio
		    .registrarFormacion(formacionDocente);
	    for (PaisDTO pais : paises) {
		if (pais.getId().equals(formacionDocente.getPais().getId())) {
		    formacionDocente.setPais(pais);
		    break;
		}
	    }
	    for (GradoTituloDTO grado : gradosTitulo) {
		if (formacionDocente.getGrado() != null) {
		    if (formacionDocente.getNivel() != null
			    && grado.getId().equals(
			            formacionDocente.getGrado().getId())) {
			formacionDocente.setGrado(grado);
			break;
		    }
		}
	    }

	    if (nuevo) {
		formaciones.add(formacionDocente);
	    }

	    JsfUtil.msgInfo("El registro se almacenó correctamente");
	    context.update(":tabViewFormularios:formFormacion");
	    context.addCallbackParam("cerrarVentana", true);

	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ocurrió un error al registrar la formación del docente");
	} catch (Exception e) {
	    JsfUtil.msgError("Error al guardar, consulte al administrador."
		    + e.getMessage());
	    e.printStackTrace();
	}

    }

    public List<Integer> getAniosTitulo() {
	List<Integer> aniosTitulo = new ArrayList<>();

	Calendar cal = Calendar.getInstance();
	int anio = cal.get(Calendar.YEAR);
	for (int i = 2014; i >= (anio - 60); i--) {
	    aniosTitulo.add(new Integer(i));
	}
	return aniosTitulo;
    }

    public void guardarCapacitacion() {
	RequestContext context = RequestContext.getCurrentInstance();
	context.addCallbackParam("cerrarVentana", false);

	AuditoriaDTO auditoria = new AuditoriaDTO();
	auditoria.setUsuarioModificacion(this.usuarioSistema.getUsuario());
	auditoria.setFechaModificacion(new Date());
	cursoCapacitacion.setAuditoriaDTO(auditoria);

	this.cursoCapacitacion.setTipoPrograma(TipoProgramaEnum
	        .parse(this.tipoProg));

	boolean nuevo = false;
	if (cursoCapacitacion.getId() == null) {
	    nuevo = true;
	    cursoCapacitacion.setActivo(true);
	}
	if (validarCursosCapacitacion()) {
	    try {
		cursoCapacitacion.setFaseIesDTO(faseIesDTO);
		if (alertaEvaluador) {
		    cursoCapacitacion.setVerificarEvidencia(false);
		}
		cursoCapacitacion.setDocenteDTO(docenteSeleccionado);
		cursoCapacitacion = registroServicio
		        .registrarCapacitacion(cursoCapacitacion);
		cursos = registroServicio.obtenerCursosPorDocenteEIes(
		        docenteSeleccionado.getId(), informacionIesDTO.getIes()
		                .getId());
		// if (nuevo) {
		// cursos.add(cursoCapacitacion);
		// }
		JsfUtil.msgInfo("El registro se almacenó correctamente");
		context.addCallbackParam("cerrarVentana", true);
	    } catch (ServicioException e) {
		JsfUtil.msgAdvert("Se produjo un error al guardar el registro");
	    } catch (Exception e) {
		JsfUtil.msgError("Error al guardar, consulte al administrador."
		        + e.getMessage());
		e.printStackTrace();
	    }
	}

    }

    public void guardarCargoAcademico() {
	RequestContext context = RequestContext.getCurrentInstance();
	context.addCallbackParam("cerrarVentana", false);

	AuditoriaDTO auditoria = new AuditoriaDTO();
	auditoria.setUsuarioModificacion(this.usuarioSistema.getUsuario());
	auditoria.setFechaModificacion(new Date());
	cargoDirectivoDocente.setAuditoriaDTO(auditoria);
	cargoDirectivoDocente.setAutoridadAcademica(AutoridadAcademicaEnum
	        .parse(cargo));
	cargoDirectivoDocente.setFaseIesDTO(this.faseIesDTO);
	if (alertaEvaluador) {
	    cargoDirectivoDocente.setVerificarEvidencia(false);
	}

	boolean nuevo = false;
	if (cargoDirectivoDocente.getId() == null) {
	    nuevo = true;
	    cargoDirectivoDocente.setActivo(true);
	}
	if (validadCargosDirectivos()) {
	    try {
		LOG.info("CA");
		cargoDirectivoDocente = registroServicio
		        .registrarCargoAcademico(cargoDirectivoDocente);
		LOG.info("1 CA");
		if (nuevo) {
		    cargosDirectivos.add(cargoDirectivoDocente);
		}
		JsfUtil.msgInfo("El registro se almacenó correctamente");
		context.addCallbackParam("cerrarVentana", true);
	    } catch (ServicioException e) {
		JsfUtil.msgError("Se produjo un error al guardar el registro");
	    }
	}
    }

    public void guardarExperienciaProfesional() {
	RequestContext context = RequestContext.getCurrentInstance();
	context.addCallbackParam("cerrarVentana", false);

	AuditoriaDTO auditoria = new AuditoriaDTO();
	auditoria.setUsuarioModificacion(this.usuarioSistema.getUsuario());
	auditoria.setFechaModificacion(new Date());
	experienciaProfesionalDocente.setAuditoriaDTO(auditoria);

	boolean nuevo = false;

	if (experienciaProfesionalDocente.getId() == null) {
	    nuevo = true;
	    experienciaProfesionalDocente.setActivo(true);
	}
	if (validarExperienciaProfesional()) {
	    try {
		experienciaProfesionalDocente.setFaseIesDTO(faseIesDTO);
		if (alertaEvaluador) {
		    experienciaProfesionalDocente.setVerificarEvidencia(false);
		}
		experienciaProfesionalDocente = registroServicio
		        .registrarExperienciaProfDocente(experienciaProfesionalDocente);
		if (nuevo) {
		    listaExperienciaProfesionalDTO
			    .add(experienciaProfesionalDocente);
		}
		JsfUtil.msgInfo("El registro se almacenó correctamente");
		context.addCallbackParam("cerrarVentana", true);
	    } catch (ServicioException e) {
		JsfUtil.msgError("Se produjo un error al guardar el registro");
	    }
	}
    }

    private DocenteDTO guardarPestania() {
	DocenteDTO resultado = null;

	try {
	    resultado = registroServicio.crearActualizar(personaDocente,
		    docenteSeleccionado, usuarioSistema);
	} catch (ServicioException se) {
	    LOG.info(se.getMessage());
	    JsfUtil.msgAdvert(se.getMessage());
	} catch (RuntimeException re) {
	    LOG.info(re.getMessage());
	    JsfUtil.msgAdvert(re.getMessage());
	} catch (Exception e) {
	    LOG.info(e.getMessage());
	    e.printStackTrace();
	}

	cargarDocentes();
	obtenerDocente();

	return resultado;
    }

    private void obtenerDocente() {
	DocenteDTO d = docenteSeleccionado;
	for (DocenteDTO doc : docentes) {
	    if (doc.getId().equals(d.getId())) {
		docenteSeleccionado = doc;
		break;
	    }
	}
    }

    private boolean validarExperienciaProfesional() {

	if (experienciaProfesionalDocente.getFechaInicio() == null) {
	    JsfUtil.msgError("Fecha inicio es requerida");
	    return false;
	} else {
	    if (experienciaProfesionalDocente.getFechaInicio().getTime() > new Date()
		    .getTime()) {
		JsfUtil.msgError("Fecha inicio no puede ser mayor que fecha actual");
		return false;
	    }
	}

	if (experienciaProfesionalDocente.getFechaFin() == null) {
	    JsfUtil.msgError("Fecha fin es requerida");
	    return false;
	}

	if (experienciaProfesionalDocente.getFechaInicio() != null
	        && experienciaProfesionalDocente.getFechaFin() != null) {
	    if (experienciaProfesionalDocente.getFechaInicio().getTime() > experienciaProfesionalDocente
		    .getFechaFin().getTime()) {
		JsfUtil.msgError("Fecha inicio no puede ser mayor que Fecha fin");
		return false;
	    }
	}

	return true;
    }

    private boolean validadCargosDirectivos() {
	cargoDirectivoDocente.setAutoridadAcademica(AutoridadAcademicaEnum
	        .parse(cargo));
	if (cargoDirectivoDocente.getNumeroDocumento() == null
	        || cargoDirectivoDocente.getNumeroDocumento().isEmpty()) {
	    JsfUtil.msgError("Numero de Documento es requerido");
	    return false;
	}

	if (cargoDirectivoDocente.getDocumento() == null
	        || cargoDirectivoDocente.getDocumento().equals("")) {
	    JsfUtil.msgError("Seleccione el documento");
	    return false;
	}

	if (cargoDirectivoDocente.getFechaInicio() == null) {
	    JsfUtil.msgError("Fecha inicio es requerida");
	    return false;
	}

	if (cargoDirectivoDocente.getFechaFin() == null) {
	    JsfUtil.msgError("Fecha fin es requerida");
	    return false;
	}

	if (cargoDirectivoDocente.getFechaInicio() != null
	        && cargoDirectivoDocente.getFechaFin() != null) {
	    if (cargoDirectivoDocente.getFechaInicio().getTime() > cargoDirectivoDocente
		    .getFechaFin().getTime()) {
		JsfUtil.msgError("Fecha inicio no puede ser mayor que Fecha fin");
		return false;
	    }
	}

	if (cargo.equals("OTRO")) {
	    if (null == cargoDirectivoDocente.getOtraAutoridad()
		    || cargoDirectivoDocente.getOtraAutoridad().equals("")) {
		JsfUtil.msgError("Otra Autoridad es requerida");
		return false;
	    }
	}

	LOG.info("RETURN TRUE CA");
	return true;
    }

    private boolean validarDatosPersonales() {
	if (docenteSeleccionado.getTipoIdentificacion().equals("CEDULA")) {
	    if (!Util.validarCedula(docenteSeleccionado.getIdentificacion())) {
		JsfUtil.msgError("Número de cédula es inválido.");
		return false;
	    }
	}

	DocenteDTO docenteDTO = new DocenteDTO();
	docenteDTO.setIdentificacion(docenteSeleccionado.getIdentificacion());
	docenteDTO.setIdInformacionIes(this.informacionIesDTO.getId());
	try {
	    docenteDTO = registroServicio.obtenerDocentePorCedula(docenteDTO);
	    if (docenteDTO != null
		    && !(docenteDTO.getId().equals(docenteSeleccionado.getId()))) {
		JsfUtil.msgError("El número de cédula ingresado ya existe.");
		return false;
	    } else {
		if (docenteSeleccionado.getId() == null) {
		    docenteDTO = new DocenteDTO();
		    docenteDTO.setIdentificacion(docenteSeleccionado
			    .getIdentificacion());
		    docenteDTO.setIdInformacionIes(this.informacionIesDTO
			    .getId());
		    docenteDTO = registroServicio
			    .obtenerDocenteInactivoPorCedula(docenteDTO,
			            this.informacionIesDTO.getId());
		    if (docenteDTO != null) {
			RequestContext.getCurrentInstance().execute(
			        "dlgConfirmarReingreso.show();");
			docenteSeleccionado = docenteDTO;
			return false;
		    }
		}

	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

	if (docenteSeleccionado.getNombres() == null
	        || docenteSeleccionado.getNombres().isEmpty()) {
	    JsfUtil.msgError("Nombres son requeridos");
	    return false;
	}

	if (docenteSeleccionado.getApellidoPaterno() == null
	        || docenteSeleccionado.getApellidoPaterno().isEmpty()) {
	    JsfUtil.msgError("Apellido Paterno es requerido");
	    return false;
	}

	if (docenteSeleccionado.getIdentificacion() == null
	        || docenteSeleccionado.getIdentificacion().isEmpty()) {
	    JsfUtil.msgError("Cédula es requerida");
	    return false;
	}

	// if (docenteSeleccionado.getEmailPersonal() != null
	// || !docenteSeleccionado.getEmailPersonal().isEmpty()) {
	// if (Util.validarEmail(docenteSeleccionado.getEmailPersonal()) ==
	// false) {
	// JsfUtil.msgError("Email Personal incorrecto");
	// return false;
	// }
	// }

	if (docenteSeleccionado.getEtnia() == null
	        || docenteSeleccionado.getEtnia().equals("")) {
	    JsfUtil.msgError("Seleccione la autoidentificación étnia");
	    return false;
	}

	if (discapacidad.equals(DiscapacidadEnum.NINGUNA.getValue())) {
	    docenteSeleccionado.setNumeroConadis(null);
	} else {
	    if (null == docenteSeleccionado.getNumeroConadis()
		    || docenteSeleccionado.getNumeroConadis().equals("")) {
		JsfUtil.msgError("Número Conadis es requerido");
		return false;
	    }
	}

	if ("INDIGENA".equals(docenteSeleccionado.getEtnia())) {
	    if (docenteSeleccionado.getPuebloDTO().getId() == null) {
		JsfUtil.msgError("Seleccione el Pueblo");
		return false;
	    }
	}

	return true;
    }

    public void activarDocente() {
	try {
	    docenteSeleccionado = registroServicio
		    .activarDocente(docenteSeleccionado.getId());
	    nuevoRedirect();

	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    private boolean validarCamposFormacion() {

	if (!idPaisFormacion.equals(1L)) {
	    if (formacionDocente.getNombreIesInternacional() == null
		    || formacionDocente.getNombreIesInternacional().isEmpty()) {
		JsfUtil.msgError("Nombre de la IES es requerido");
		return false;
	    }
	} else {
	    if (iesFormacion.getId() == null || iesFormacion.getId().equals(0L)) {
		JsfUtil.msgError("Seleccione la IES correspondiente");
		return false;
	    }
	}

	LOG.info("formacionDocente.getNivel(): " + formacionDocente.getNivel());
	if (formacionDocente.getNivel() == null
	        || formacionDocente.getNivel().isEmpty()) {
	    JsfUtil.msgError("Seleccione el nivel");
	    return false;
	}

	if (formacionDocente.getNivel().equals("CUARTO NIVEL")) {
	    if (idNivelTituloSelecionado == null
		    || idNivelTituloSelecionado.equals(0L)) {
		JsfUtil.msgError("Seleccione el grado");
		return false;
	    } else {
		if (idNivelTituloSelecionado.equals(-1L)) {
		    if (idNivelTituloSelecionadoOtros == null
			    || idNivelTituloSelecionadoOtros.equals(0L)
			    || idNivelTituloSelecionadoOtros.equals(-1L)) {
			JsfUtil.msgError("Seleccione el grado");
			return false;
		    } else {
			gradoSeleccionado.setId(idNivelTituloSelecionadoOtros);
		    }
		} else {
		    gradoSeleccionado.setId(idNivelTituloSelecionado);
		}
	    }
	} else {
	    for (GradoTituloDTO grado : gradosTitulo) {
		if (formacionDocente.getNivel() != null
		        && grado.getNombre().contains(
		                formacionDocente.getNivel())) {
		    gradoSeleccionado = grado;
		    formacionDocente.setGrado(grado);
		    break;
		}
	    }
	}

	if (formacionDocente.getTitulo() == null
	        || formacionDocente.getTitulo().equals("")) {
	    JsfUtil.msgError("Nombre del titulo es requerido");
	    return false;
	}

	// if (formacionDocente.getSubAreaConocimientoDTO().getId() == null) {
	// JsfUtil.msgError("Subarea de conocimiento es requerida");
	// return false;
	// }

	// if (formacionDocente.getAnioObtuvoTitulo() instanceof Number) {
	// if (formacionDocente.getAnioObtuvoTitulo() < 1920) {
	// msgError("Año de título no es valido");
	// return false;
	// }
	// } else {
	// msgError("Anio Titulo debe ser un numero");
	// return false;
	// }

	if (formacionDocente.getCursando() == null) {
	    formacionDocente.setCursando(false);
	}

	if (!formacionDocente.getCursando()) {

	    formacionDocente.setFechaInicioEstudios(null);
	    if (formacionDocente.getFechaRegistroSenescyt() == null) {
		JsfUtil.msgError("Ingrese fecha de registro del título");
		return false;
	    }

	    if (!alertaEvaluador) {

		if (null == formacionDocente.getFechaGraduacion()) {
		    JsfUtil.msgError("Ingrese Fecha obtuvo el título");
		    return false;
		}

		if (formacionDocente.getFechaRegistroSenescyt().before(
		        formacionDocente.getFechaGraduacion())) {
		    JsfUtil.msgError("Fecha de registro en la SENESCYT debe ser posterior a la fecha de obtención del título");
		    return false;
		}
	    }

	    if (formacionDocente.getNumeroRegistroSenescyt() != null
		    && !formacionDocente.getNumeroRegistroSenescyt().isEmpty()
		    && formacionDocente.getFechaRegistroSenescyt() == null) {
		JsfUtil.msgError("Ingrese la fecha de fin de estudios");
		return false;
	    }

	} else {
	    formacionDocente.setFechaGraduacion(null);
	    formacionDocente.setFechaRegistroSenescyt(null);
	    formacionDocente.setNumeroRegistroSenescyt(null);
	}

	if (formacionDocente.getCursando()
	        && formacionDocente.getFechaInicioEstudios() == null) {
	    if (!alertaEvaluador) {
		JsfUtil.msgError("Ingrese la fecha de incio de estudios");
		return false;
	    }
	}
	LOG.info("RETURN TRUE");
	return true;
    }

    private boolean validarCursosCapacitacion() {

	if (cursoCapacitacion.getFechaFin() == null
	        || cursoCapacitacion.getFechaInicio() == null) {
	    JsfUtil.msgError("Las fechas son requeridas");
	    return false;
	}

	if (cursoCapacitacion.getFechaInicio().getTime() > cursoCapacitacion
	        .getFechaFin().getTime()) {
	    JsfUtil.msgError("La fecha de inicio no debe ser mayor que fecha fin");
	    return false;
	}

	if (cursoCapacitacion.getInstitutoCapacitacion().isEmpty()
	        || cursoCapacitacion.getInstitutoCapacitacion() == null) {
	    JsfUtil.msgError("Institucion es requerido");
	    return false;
	}

	if (cursoCapacitacion.getNombre().isEmpty()
	        || cursoCapacitacion.getNombre() == null) {
	    JsfUtil.msgError("El nombre del curso es requerido");
	    return false;
	}

	if (cursoCapacitacion.getNumeroHoras() instanceof Number) {
	    if (cursoCapacitacion.getNumeroHoras() < 0) {
		JsfUtil.msgError("Numero de horas incorrecto");
		return false;
	    }
	    if (cursoCapacitacion.getNumeroHoras() < 8
		    || cursoCapacitacion.getNumeroHoras() > 1000) {
		JsfUtil.msgError("Numero de horas mínimo requerido 8 y máximo 1000");
		return false;
	    }
	} else {
	    JsfUtil.msgError("Valor en numero de horas invalido");
	    return false;
	}

	return true;
    }

    private boolean valicarCamposContratacion() {
	if (contratoDocente.getActivo() == null) {
	    contratoDocente.setActivo(false);
	}

	if (contratoDocente.getNumeroContrato() == null
	        || contratoDocente.getNumeroContrato().equals("")) {
	    JsfUtil.msgAdvert("Número de contrato es requerido");
	    return false;
	}
	if (contratoDocente.getRelacionIes() == null
	        || contratoDocente.getNumeroContrato().equals("")) {
	    JsfUtil.msgAdvert("Relación IES es requerido");
	    return false;
	}

	if (contratoDocente.getTiempoDedicacion() == null
	        || contratoDocente.getTiempoDedicacion().compareTo(
	                TiempoDedicacionEnum.NO_DEFINIDO) == 0) {
	    JsfUtil.msgAdvert("Dedicación es requerida");
	    return false;
	}

	if (contratoDocente.getCategoria() == null) {
	    JsfUtil.msgAdvert("Categoría es requerida");
	    return false;
	}

	if (contratoDocente.getFechaInicio() == null) {
	    JsfUtil.msgAdvert("Fecha inicio es requerida");
	    return false;
	} else {
	    if (contratoDocente.getFechaInicio().getTime() > new Date()
		    .getTime()) {
		JsfUtil.msgAdvert("Fecha inicio no puede ser mayor que fecha actual");
		return false;
	    }
	}

	if (contratoDocente.getFechaInicio() != null
	        && contratoDocente.getFechaFin() != null) {
	    if (contratoDocente.getFechaInicio().getTime() > contratoDocente
		    .getFechaFin().getTime()) {
		JsfUtil.msgAdvert("Fecha inicio no puede ser mayor que Fecha fin");
		return false;
	    }
	}
	if (contratoDocente.getTiempoDedicacion().compareTo(
	        TiempoDedicacionEnum.TIEMPO_PARCIAL) != 0) {
	    if (contratoDocente.getRemuneracionContrato() instanceof Number) {
		if (contratoDocente.getRemuneracionContrato() < 0) {
		    JsfUtil.msgAdvert("Remuneración de contrato no debe ser negativa");
		    return false;
		}
	    } else {
		JsfUtil.msgAdvert("Remuneración de contrato no válida.");
		return false;
	    }
	} else {
	    if (contratoDocente.getRemuneracionPorHora() instanceof Number) {
		if (contratoDocente.getRemuneracionPorHora() < 0) {
		    JsfUtil.msgAdvert("Remuneración por Hora no debe ser negativa");
		    return false;
		}
	    }
	}

	for (ContratacionDTO contrato : contrataciones) {
	    if ((contratoDocente.getTiempoDedicacion() != null && contrato
		    .getTiempoDedicacion().compareTo(
		            contratoDocente.getTiempoDedicacion()) != 0)
		    && contrato.getCategoria().compareTo(
		            contratoDocente.getCategoria()) != 0) {
		if (contrato.getFechaFin() == null) {
		    if (contratoDocente.getFechaFin() == null
			    && !contrato.getId()
			            .equals(contratoDocente.getId())) {
			JsfUtil.msgError("Ya existe un contrato sin una fecha de finalización.");
			return false;
		    } else if (contratoDocente.getId() == null) {
			if (contrato.getFechaInicio().compareTo(
			        contratoDocente.getFechaFin()) <= 0) {
			    JsfUtil.msgError("La fechas de contrato ingresados, están siendo incluidas en un contrato existente.");
			    return false;
			}
		    } else {
			if (!contrato.getId().equals(contratoDocente.getId())
			        && contrato.getFechaInicio().compareTo(
			                contratoDocente.getFechaFin()) <= 0) {
			    JsfUtil.msgError("La fechas de contrato ingresados, están siendo incluidas en un contrato existente.");
			    return false;
			}
		    }
		} else if (contratoDocente.getFechaFin() == null) {
		    if (contratoDocente.getId() == null) {
			if (contratoDocente.getFechaInicio().compareTo(
			        contrato.getFechaFin()) <= 0) {
			    JsfUtil.msgError("La fechas de contrato ingresados, incluyen a un contrato existente.");
			    return false;
			}
		    } else {
			if (!contrato.getId().equals(contratoDocente.getId())
			        && contratoDocente.getFechaInicio().compareTo(
			                contrato.getFechaFin()) <= 0) {
			    JsfUtil.msgError("La fechas de contrato ingresados, incluyen a un contrato existente.");
			    return false;
			}
		    }
		}
	    }
	}
	return true;
    }

    public void nuevoContrato() {
	LOG.info("--> METODO nuevoContrato");
	this.contratoDocente = new ContratacionDTO();
	this.contratoDocente.getPersonaDTO().setId(
	        this.docenteSeleccionado.getId());
	editarContrato = true;
	contratacionesPeriodoAcademico.clear();

	limpiarDocenteAsignatura();
    }

    public void modificarContrato() {

	contratacionesPeriodoAcademico.clear();
	horasNoAcademicasEliminar.clear();
	contratacionesPeriodoEliminar.clear();
	List<ContratacionPeriodoAcademicoDTO> aux = new ArrayList<>();
	aux = registroServicio
	        .obtenerContratacionesPeriodoAcademico(contratoDocente.getId());
	for (ContratacionPeriodoAcademicoDTO cont : aux) {
	    if (cont.getPeriodoAcademicoDTO() != null) {
		contratacionesPeriodoAcademico.add(cont);
	    }
	}
	this.contratoDocente.getPersonaDTO().setId(
	        this.docenteSeleccionado.getId());
	editarContrato = true;
	// evidenciasContrato.clear();

	limpiarDocenteAsignatura();
    }

    private void limpiarDocenteAsignatura() {
	mostrarAsignacion = false;
	listaAsignaturasxDocente.clear();
	contratacionPeriodoAcademicoDTO = new ContratacionPeriodoAcademicoDTO();
	limpiarAsignaturas();
	limpiarAsignaturas2();
    }

    public void verContrato() {
	contratacionesPeriodoAcademico.clear();
	contratacionesPeriodoAcademico = registroServicio
	        .obtenerContratacionesPeriodoAcademico(contratoDocente.getId());
    }

    public void cancelarContrato() {
	this.contratoDocente = new ContratacionDTO();
	this.editarContrato = false;
	// evidenciasContrato.clear();
    }

    public void nuevaFormacion() {
	this.formacionDocente = new FormacionProfesionalDTO();
	this.formacionDocente.getPersona().setId(
	        this.docenteSeleccionado.getId());
	this.idNivelTitulo = null;
	this.idPais = null;
	this.idPaisFormacion = null;
	this.idSubArea = null;
	this.idArea = null;
	this.subareas.clear();

    }

    public void cancelarFormacion() {
	this.formacionDocente = new FormacionProfesionalDTO();
	this.iesFormacion = new IesDTO();
	this.idNivelTitulo = null;
	this.idPais = null;
	this.idPaisFormacion = null;
	this.idSubArea = null;
	this.idArea = null;
	this.subareas.clear();
    }

    public void nuevoCurso() {
	this.cursoCapacitacion = new CursoCapacitacionDTO();
	this.cursoCapacitacion.getDocenteDTO().setId(
	        this.docenteSeleccionado.getId());
	// listarEvidenciasCapacitacion();
    }

    public void cancelarCurso() {
	this.cursoCapacitacion = new CursoCapacitacionDTO();
    }

    public void nuevoCargoDirectivo() {
	this.cargoDirectivoDocente = new CargoAcademicoDTO();
	this.cargoDirectivoDocente.getDocenteDTO().setId(
	        this.docenteSeleccionado.getId());
	// listarEvidenciasCargosDireccion();
    }

    public void cancelarCargoDirectivo() {
	this.cargoDirectivoDocente = new CargoAcademicoDTO();
    }

    public void nuevoExperienciaProfesional() {
	this.experienciaProfesionalDocente = new ExperienciaProfesionalDTO();
	this.experienciaProfesionalDocente.getDocenteDTO().setId(
	        this.docenteSeleccionado.getId());
    }

    public void cancelarExperienciaProfesional() {
	this.experienciaProfesionalDocente = new ExperienciaProfesionalDTO();
    }

    public void editarFormacionLista() {
	if (alertaFaseRectificacion && alertaUsuarioIes) {
	    if (formacionDocente.getId() != null
		    && !formacionDocente.getFaseIesDTO().getTipo().name()
		            .startsWith("RECTIFICACION")) {
		JsfUtil.msgError("No se puede editar este registro en fase de Rectificación.");
		return;
	    }
	}

	if (this.formacionDocente.getPais() != null
	        && this.formacionDocente.getPais().getId() != null) {
	    this.idPaisFormacion = this.formacionDocente.getPais().getId();
	}

	if (this.formacionDocente.getIes() != null) {
	    this.iesFormacion = new IesDTO();
	    this.iesFormacion.setId(formacionDocente.getIes().getId());
	    this.iesFormacion.setNombre(formacionDocente.getIes().getNombre());

	}

	if (formacionDocente.getPais().getId() != null) {
	    if (formacionDocente.getPais().getId().equals(1L)) {
		IesDTO iesDTO = new IesDTO();
		iesDTO.setId(formacionDocente.getIes().getId());
		this.iesFormacion = iesDTO;
	    }
	}
	idNivelTituloSelecionadoOtros = -1L;
	idNivelTituloSelecionado = null;
	if (formacionDocente.getNivel().equals("CUARTO NIVEL")) {
	    gradoSeleccionado = new GradoTituloDTO();
	    if (this.formacionDocente.getGrado() != null) {
		this.idNivelTituloSelecionado = this.formacionDocente
		        .getGrado().getId();
		if (idNivelTituloSelecionado != null) {
		    obtenerGradoSeleccionado(idNivelTituloSelecionado);

		    if (gradoSeleccionado.getGrado().equals("OTROS")) {
			LOG.info("otros: " + gradoSeleccionado.getNombre());
			idNivelTituloSelecionadoOtros = idNivelTituloSelecionado;
			idNivelTituloSelecionado = -1L;
		    }
		}
	    }
	}
	RequestContext context = RequestContext.getCurrentInstance();
	context.execute("editarFormacionDlg.show()");
    }

    public void editarCapacitacion() {
	if (alertaFaseRectificacion && alertaUsuarioIes) {
	    if (cursoCapacitacion.getId() != null
		    && !cursoCapacitacion.getFaseIesDTO().getTipo().name()
		            .startsWith("RECTIFICACION")) {
		JsfUtil.msgError("No se puede editar este registro en fase de Rectificación.");
		return;
	    }
	}
	this.tipoProg = this.cursoCapacitacion.getTipoPrograma().getValue();
	this.cursoCapacitacion.setDocenteDTO(this.docenteSeleccionado);
	RequestContext context = RequestContext.getCurrentInstance();
	context.execute("editarCapacitacionDlg.show()");
    }

    public void editarCargoDirectivo() {
	if (alertaFaseRectificacion && alertaUsuarioIes) {
	    if (cargoDirectivoDocente.getId() != null
		    && !cargoDirectivoDocente.getFaseIesDTO().getTipo().name()
		            .startsWith("RECTIFICACION")) {
		JsfUtil.msgError("No se puede editar este registro en fase de Rectificación.");
		return;
	    }
	}
	this.cargoDirectivoDocente.setDocenteDTO(this.docenteSeleccionado);
	RequestContext context = RequestContext.getCurrentInstance();
	context.execute("editarDirectivoDlg.show()");
    }

    public void editarExperienciaProfesional() {
	if (alertaFaseRectificacion && alertaUsuarioIes) {
	    if (experienciaProfesionalDocente.getId() != null
		    && !experienciaProfesionalDocente.getFaseIesDTO().getTipo()
		            .name().startsWith("RECTIFICACION")) {
		JsfUtil.msgError("No se puede editar este registro en fase de Rectificación.");
		return;
	    }
	}
	this.experienciaProfesionalDocente
	        .setDocenteDTO(this.docenteSeleccionado);
	RequestContext context = RequestContext.getCurrentInstance();
	context.execute("editarExperienciaProfDlg.show()");

    }

    public void tomarBotonGuardarDialogos(ActionEvent event) {
	FacesContext context = FacesContext.getCurrentInstance();

	msgAccion = "";
	String idBotonGuardar = null;
	idBotonGuardar = event.getComponent().getClientId(context);

	if (idBotonGuardar.contains("btnBuscar")) {
	    msgAccion = "buscando docente";
	    return;
	} else {
	    msgAccion = "";
	    return;
	}
    }

    public void seleccionarCategoria(ValueChangeEvent e) {
	if (e != null && e.getNewValue() != null) {
	    String valor = e.getNewValue().toString();
	    if (valor.startsWith("TITULAR")) {
		esTitular = true;
	    } else {
		esTitular = false;
	    }
	}
    }

    //
    public void seleccionarNivelTitulo(ValueChangeEvent e) {
	// evidenciasFormacion.clear();
	if (e != null && e.getNewValue() != null) {
	    idNivelTitulo = new Long(e.getNewValue().toString());
	}
	// listarEvidenciasFormacion();
    }

    public void seleccionarGradoTitulo(ValueChangeEvent e) {
	// setIdNivelTituloSelecionadoOtros(null);
	formacionDocente.setTitulo(null);
	gradoSeleccionado = new GradoTituloDTO();
	if (e != null && e.getNewValue() != null) {
	    Long idGrado = new Long(e.getNewValue().toString());
	    obtenerGradoSeleccionado(idGrado);
	}
    }

    private void obtenerGradoSeleccionado(Long idGrado) {
	for (GradoTituloDTO grado : gradosTitulo) {
	    if (grado.getId().equals(idGrado)) {
		gradoSeleccionado = grado;
	    }
	}
    }

    //
    public void seleccionarCursandoFormacion(ValueChangeEvent e) {
	// evidenciasFormacion.clear();
	if (e != null) {
	    if (new Boolean(e.getNewValue().toString())) {
		return;
	    }
	    // listarEvidenciasFormacion();
	}
    }

    public void modificarIndice(ActionEvent event) {
	FacesContext context = FacesContext.getCurrentInstance();
	String botonId = event.getComponent().getClientId(context).toString();
	if (botonId.contains("btnAtras")) {
	    if (listaDocentesTodos.size() < registros) {
		indiceAtras = indice - docentes.size();
	    } else {
		indiceAtras = indice - registros;
	    }
	    indice = indiceAtras;
	    indiceAtras = 0;
	    habilitarSiguiente = true;
	}

	if (botonId.contains("btnSiguiente")) {
	    if (listaDocentesTodos.size() < registros) {
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

    public void tomarRango(ValueChangeEvent event) {
	FacesContext context = FacesContext.getCurrentInstance();
	registros = ((Integer) event.getNewValue()).intValue();
	// indice = indice - registros;
	indice = 0;
	numRegistros = 0;
	listaDocentesTodos.clear();
	cargarDocentes();
	if (registros == 0) {
	    docentes.clear();
	    docentesFiltros.clear();
	}
	context.renderResponse();
    }

    public void ocultarAsignacionHoras() {
	mostrarAsignacion = false;
    }

    public void guardarContratacionesPeriodo() {
	RequestContext context = RequestContext.getCurrentInstance();
	context.addCallbackParam("cerrarVentana", false);

	if (contratacionPeriodoAcademicoDTO.getHorasClase60min() > 800) {
	    JsfUtil.msgError("Horas de clase por periodo no puede ser mayor a 800");
	    return;
	}

	if (contratacionPeriodoAcademicoDTO.getHorasClase60min() < 0) {
	    JsfUtil.msgError("Horas de clase por periodo no puede negativo");
	    return;
	}

	if (contratacionPeriodoAcademicoDTO.getHorasClaseSemanal() > 40) {
	    JsfUtil.msgError("Horas de clase semanal no puede ser mayor a 40");
	    return;
	}

	if (contratacionPeriodoAcademicoDTO.getHorasClaseSemanal() < 0) {
	    JsfUtil.msgError("Horas de clase semanal no puede ser negativo");
	    return;
	}

	if (contratacionPeriodoAcademicoDTO.getHorasClaseSemanal() > contratacionPeriodoAcademicoDTO
	        .getHorasClase60min()) {
	    JsfUtil.msgError("Horas de clase semanal no puede ser mayor al número de horas clase por periodo.");
	    return;
	}
	// if (!validarContratoPeriodosAcademicos()) {
	// return;
	// }

	for (ContratacionPeriodoAcademicoDTO contratacionLista : contratacionesPeriodoAcademico) {
	    if (idPeriodoAcademico.equals(contratacionLista
		    .getPeriodoAcademicoDTO().getId())
		    && contratacionPeriodoAcademicoDTO.getActivo() == null) {
		JsfUtil.msgError("Ya existe un registro para el periodo ingresado");
		return;
	    }

	}
	for (PeriodoAcademicoDTO periodo : periodosAcademicos) {
	    if (periodo.getId().equals(idPeriodoAcademico)) {
		contratacionPeriodoAcademicoDTO.setPeriodoAcademicoDTO(periodo);
		break;
	    }
	}
	if (validarNuevoPeriodoAcademico()) {

	    if (contratacionPeriodoAcademicoDTO.getActivo() == null
		    || contratacionPeriodoAcademicoDTO.getActivo()) {
		contratacionPeriodoAcademicoDTO.setActivo(Boolean.TRUE);

		AuditoriaDTO auditoria = new AuditoriaDTO();
		auditoria.setUsuarioModificacion(usuarioSistema.getUsuario());
		auditoria.setFechaModificacion(new Date());
		contratacionPeriodoAcademicoDTO.setAuditoriaDTO(auditoria);
		contratacionPeriodoAcademicoDTO.setFaseIesDTO(faseIesDTO);

		contratacionPeriodoAcademicoDTO
		        .setContratacionDTO(contratoDocente);
		boolean guardarContrato = false;

		if (contratacionesPeriodoAcademico.isEmpty()) {
		    guardarContrato = true;
		}
		if (contratacionPeriodoAcademicoDTO.getId() == null) {
		    contratacionesPeriodoAcademico
			    .add(contratacionPeriodoAcademicoDTO);
		}
		contratoDocente
		        .setContratacionPeriodoAcademicoDTO(contratacionesPeriodoAcademico);

		if (guardarContrato) {
		    guardarContrato();
		} else {
		    guardarContratoPeriodosAcademicos();
		}
	    } else {
		AuditoriaDTO auditoria = contratacionPeriodoAcademicoDTO
		        .getAuditoriaDTO();
		auditoria.setUsuarioModificacion(usuarioSistema.getUsuario());
		auditoria.setFechaModificacion(new Date());
		contratacionPeriodoAcademicoDTO.setAuditoriaDTO(auditoria);
	    }

	    context.addCallbackParam("cerrarVentana", true);
	}
    }

    public void guardarHorasNoAcademicas() {
	RequestContext context = RequestContext.getCurrentInstance();
	context.addCallbackParam("cerrarVentana", false);

	// if (contratacionPeriodoAcademicoDTO.getHorasAdministrativas() > 800
	// || contratacionPeriodoAcademicoDTO.getHorasAdministrativas() <= 1) {
	// msgError("El número de horas administrativas no puede ser mayor a 800");
	// return;
	// }
	// if (contratacionPeriodoAcademicoDTO.getHorasInvestigacion() > 800
	// || contratacionPeriodoAcademicoDTO.getHorasInvestigacion() <= 1) {
	// msgError("El número de horas de investigación no puede ser mayor a 800");
	// return;
	// }
	// if (contratacionPeriodoAcademicoDTO.getHorasVinculacion() > 800
	// || contratacionPeriodoAcademicoDTO.getHorasVinculacion() <= 1) {
	// msgError("El número de horas de investigación no puede ser mayor a 800");
	// return;
	// }

	if (contratacionPeriodoAcademicoDTO.getActivo() == null) {
	    contratacionPeriodoAcademicoDTO.setActivo(Boolean.TRUE);

	    AuditoriaDTO auditoria = new AuditoriaDTO();
	    auditoria.setUsuarioModificacion(usuarioSistema.getUsuario());
	    contratacionPeriodoAcademicoDTO.setAuditoriaDTO(auditoria);
	    contratacionPeriodoAcademicoDTO.setFaseIesDTO(faseIesDTO);
	    contratacionPeriodoAcademicoDTO.setContratacionDTO(contratoDocente);
	} else {
	    AuditoriaDTO auditoria = contratacionPeriodoAcademicoDTO
		    .getAuditoriaDTO();
	    auditoria.setUsuarioModificacion(usuarioSistema.getUsuario());
	    contratacionPeriodoAcademicoDTO.setFaseIesDTO(faseIesDTO);
	    contratacionPeriodoAcademicoDTO.setAuditoriaDTO(auditoria);
	}

	context.addCallbackParam("cerrarVentana", true);
    }

    public void nuevoContratacionPeriodo() {
	contratacionPeriodoAcademicoDTO = new ContratacionPeriodoAcademicoDTO();
	idPeriodoAcademico = null;
	cargarPeriodos();
    }

    public void eliminarContratacionPeriodo() {
	if (contratacionPeriodoAcademicoDTO.getId() != null) {
	    if (alertaFaseRectificacion && alertaUsuarioIes) {
		if (!contratacionPeriodoAcademicoDTO.getFaseIesDTO().getTipo()
		        .name().startsWith("RECTIFICACION")) {
		    JsfUtil.msgError("No se puede eliminar el perido en fase de Rectificación.");
		    return;
		}
	    }
	    contratacionPeriodoAcademicoDTO.setActivo(false);
	    contratacionPeriodoAcademicoDTO.setFaseIesDTO(faseIesDTO);
	    contratacionesPeriodoEliminar.add(contratacionPeriodoAcademicoDTO);
	}
	contratacionesPeriodoAcademico.remove(contratacionPeriodoAcademicoDTO);
	guardarContratoPeriodosAcademicos();
    }

    public void eliminarHorasNoAcad() {
	if (contratacionPeriodoAcademicoDTO.getId() != null) {
	    contratacionPeriodoAcademicoDTO.setActivo(false);
	    contratacionPeriodoAcademicoDTO.setFaseIesDTO(faseIesDTO);
	    horasNoAcademicasEliminar.add(contratacionPeriodoAcademicoDTO);
	}
    }

    public void eliminarPeriodoAcademico() {
	if (alertaFaseRectificacion && alertaUsuarioIes) {
	    if (!periodoAcademicoDTO.getFaseIesDTO().getTipo().name()
		    .startsWith("RECTIFICACION")) {
		JsfUtil.msgError("No se puede eliminar el periodo seleccionado en Rectificación");
		return;
	    }
	}

	if (periodoAcademicoDTO.getId() != null) {
	    periodoAcademicoDTO.setActivo(false);
	    periodosAcademicosEliminar.add(periodoAcademicoDTO);
	}
	periodosAcademicos.remove(periodoAcademicoDTO);
    }

    public void seleccionarContratoEditar(ActionEvent actionEvent) {
	contratacionPeriodoAcademicoDTO = new ContratacionPeriodoAcademicoDTO();
    }

    public int obtenerUltimoDiaMes(int anio, int mes) {

	Calendar calendario = Calendar.getInstance();
	calendario.set(anio, mes, 1);
	int diaUltimo = calendario.getActualMaximum(Calendar.DAY_OF_MONTH);
	LOG.info("Dia ultimo del mes: " + mes + " es: " + diaUltimo);
	return diaUltimo;
    }

    public void guardarPeriodoAcademico() {
	mensajeErrorPeriodo = "";
	RequestContext context = RequestContext.getCurrentInstance();
	context.addCallbackParam("cerrarVentana", false);
	try {
	    Calendar fechaInicio = Calendar.getInstance();
	    fechaInicio.set(anioInicioPeriodoAcademico,
		    mesInicioPeriodoAcademico, 1);

	    fechaInicio = asignarHoraCero(fechaInicio);

	    Calendar fechaFin = Calendar.getInstance();

	    fechaFin.set(
		    anioFinPeriodoAcademico,
		    mesFinPeriodoAcademico,
		    obtenerUltimoDiaMes(anioFinPeriodoAcademico,
		            mesFinPeriodoAcademico));

	    fechaFin = asignarHoraCero(fechaFin);

	    LOG.info("Fecha Inicio : " + fechaInicio.getTime());
	    LOG.info("Fecha Fin : " + fechaFin.getTime());

	    if (fechaInicio.compareTo(fechaFin) >= 0) {
		mensajeErrorPeriodo = "El periodo de inicio es mayor o igual al periodo final";
		return;
	    }

	    for (PeriodoAcademicoDTO periodoLista : periodosAcademicos) {

		Calendar fechaInicioLista = Calendar.getInstance();
		fechaInicioLista.setTime(periodoLista.getFechaInicioPeriodo());
		// fechaInicioLista.set(Calendar.DAY_OF_MONTH, 1);
		// fechaInicioLista = asignarHoraCero(fechaInicioLista);

		Calendar fechaFinLista = Calendar.getInstance();
		fechaFinLista.setTime(periodoLista.getFechaFinPeriodo());
		// fechaFinLista.set(Calendar.DAY_OF_MONTH, 1);
		// fechaFinLista = asignarHoraCero(fechaFinLista);
		LOG.info("Fecha Inicio lista: " + fechaInicioLista.getTime());
		LOG.info("Fecha Inicio lista: " + fechaInicioLista.getTime());
		LOG.info("fecha fin lista: " + fechaFinLista.getTime());

		if (fechaInicio.compareTo(fechaInicioLista) >= 0
		        && fechaFin.compareTo(fechaFinLista) <= 0) {
		    mensajeErrorPeriodo = "El periodo ingresado, está incluido en un periodo existente";
		    return;
		} else if (fechaInicio.compareTo(fechaInicioLista) <= 0
		        && fechaFin.compareTo(fechaFinLista) >= 0) {
		    mensajeErrorPeriodo = "El periodo ingresado, incluye a un periodo existente";
		    return;
		}
	    }

	    if (periodoAcademicoDTO.getActivo() == null) {
		periodoAcademicoDTO.setActivo(Boolean.TRUE);
		periodoAcademicoDTO
		        .setFechaInicioPeriodo(fechaInicio.getTime());
		periodoAcademicoDTO.setFechaFinPeriodo(fechaFin.getTime());

		AuditoriaDTO auditoria = new AuditoriaDTO();
		auditoria.setUsuarioModificacion(usuarioSistema.getUsuario());
		periodoAcademicoDTO.setAuditoriaDTO(auditoria);

		ListaIesController controller = (ListaIesController) FacesContext
		        .getCurrentInstance().getExternalContext()
		        .getSessionMap().get("listaIesController");
		informacionIesDTO = registroServicio
		        .obtenerInformacionIesPorIes(controller.getIes());
		periodoAcademicoDTO.setInformacionIes(informacionIesDTO);
		periodoAcademicoDTO.setFaseIesDTO(faseIesDTO);
		periodosAcademicos.add(periodoAcademicoDTO);
	    } else {
		AuditoriaDTO auditoria = periodoAcademicoDTO.getAuditoriaDTO();
		auditoria.setUsuarioModificacion(usuarioSistema.getUsuario());
		periodoAcademicoDTO
		        .setFechaInicioPeriodo(fechaInicio.getTime());
		periodoAcademicoDTO.setFechaFinPeriodo(fechaFin.getTime());
		periodoAcademicoDTO.setFaseIesDTO(faseIesDTO);
	    }
	    context.addCallbackParam("cerrarVentana", true);
	} catch (Exception e) {
	    mensajeErrorPeriodo = "Error al guardar el periodo academico";
	    context.addCallbackParam("cerrarVentana", false);
	    LOG.severe("Error al guardar el periodo academico");
	    LOG.log(Level.SEVERE, e.getMessage(), e);
	}
    }

    public void nuevoPeriodoAcademico() {
	periodoAcademicoDTO = new PeriodoAcademicoDTO();
	periodoAcademicoDTO.setFaseIesDTO(faseIesDTO);
	anioInicioPeriodoAcademico = Calendar.getInstance().get(Calendar.YEAR);
	mesInicioPeriodoAcademico = Calendar.getInstance().get(Calendar.MONTH);
	anioFinPeriodoAcademico = Calendar.getInstance().get(Calendar.YEAR);
	mesFinPeriodoAcademico = Calendar.getInstance().get(Calendar.MONTH);
	idPeriodoAcademico = null;
	mensajeErrorPeriodo = null;
    }

    public void guardarPeriodosAcademicos() {
	try {
	    periodosAcademicos.addAll(periodosAcademicosEliminar);
	    registroServicio.registrarPeriodosAcademicos(periodosAcademicos);
	    JsfUtil.msgInfo("Los periodos se guardaron correctamente");
	    cargarPeriodos();
	} catch (Exception e) {
	    LOG.info("Error al registrar los periodos");
	    LOG.log(Level.SEVERE, e.getMessage(), e);
	    JsfUtil.msgError("Error al registrar los periodos");
	}
    }

    private void obtenerTotalDocentes() {
	totalDocentes = 0;

	try {
	    totalDocentes = registroServicio
		    .totalDocentesPorIes(this.informacionIesDTO.getId());
	} catch (ServicioException e) {
	    e.printStackTrace();
	    return;
	}
    }

    public List<GradoTituloDTO> getListaNivelTituloGrado() {
	List<GradoTituloDTO> listaSoloGrados = new ArrayList<>();
	for (GradoTituloDTO gradoAcademico : gradosTitulo) {
	    if ("GRADO".equals(gradoAcademico.getGrado())) {
		listaSoloGrados.add(gradoAcademico);
	    }
	}
	return listaSoloGrados;
    }

    public List<GradoTituloDTO> getListaNivelTituloOtros() {
	List<GradoTituloDTO> listaOtros = new ArrayList<>();
	for (GradoTituloDTO gradoAcademico : gradosTitulo) {
	    if (GRADO_OTROS.equals(gradoAcademico.getGrado())) {
		listaOtros.add(gradoAcademico);
	    }
	}
	return listaOtros;
    }

    public void modificarContratacionPeriodo() {
	idPeriodoAcademico = contratacionPeriodoAcademicoDTO
	        .getPeriodoAcademicoDTO().getId();
    }

    public void nuevoDocenteAsignaturaHoraClase() {
	docenteAsignaturaHoraClaseDTO = new DocenteAsignaturaDTO();
    }

    public void cancelarPeriodosAcademicos() {
	cargarPeriodos();
    }

    public Long getIdPeriodoAcademico() {
	return idPeriodoAcademico;
    }

    public void setIdPeriodoAcademico(Long idPeriodoAcademico) {
	this.idPeriodoAcademico = idPeriodoAcademico;
    }

    public boolean isHabilitarSiguiente() {
	return habilitarSiguiente;
    }

    public void setHabilitarSiguiente(boolean habilitarSiguiente) {
	this.habilitarSiguiente = habilitarSiguiente;
    }

    public List<PeriodoAcademicoDTO> getPeriodosAcademicos() {
	return periodosAcademicos;
    }

    /**
     * @param periodosAcademicos
     *            the periodosAcademicos to set
     */
    public void setPeriodosAcademicos(
	    List<PeriodoAcademicoDTO> periodosAcademicos) {
	this.periodosAcademicos = periodosAcademicos;
    }

    /**
     * @return the periodoAcademicoDTO
     */
    public PeriodoAcademicoDTO getPeriodoAcademicoDTO() {
	return periodoAcademicoDTO;
    }

    /**
     * @param periodoAcademicoDTO
     *            the periodoAcademicoDTO to set
     */
    public void setPeriodoAcademicoDTO(PeriodoAcademicoDTO periodoAcademicoDTO) {
	this.periodoAcademicoDTO = periodoAcademicoDTO;
    }

    /**
     * @return the mensajeErrorPeriodo
     */
    public String getMensajeErrorPeriodo() {
	return mensajeErrorPeriodo;
    }

    /**
     * @param mensajeErrorPeriodo
     *            the mensajeErrorPeriodo to set
     */
    public void setMensajeErrorPeriodo(String mensajeErrorPeriodo) {
	this.mensajeErrorPeriodo = mensajeErrorPeriodo;
    }

    /**
     * @return the docenteAsignaturaHoraClaseDTO
     */
    public DocenteAsignaturaDTO getDocenteAsignaturaHoraClaseDTO() {
	return docenteAsignaturaHoraClaseDTO;
    }

    /**
     * @param docenteAsignaturaHoraClaseDTO
     *            the docenteAsignaturaHoraClaseDTO to set
     */
    public void setDocenteAsignaturaHoraClaseDTO(
	    DocenteAsignaturaDTO docenteAsignaturaHoraClaseDTO) {
	this.docenteAsignaturaHoraClaseDTO = docenteAsignaturaHoraClaseDTO;
    }

    /**
     * @return the docentesAsignaturaHoraClase
     */
    public List<DocenteAsignaturaDTO> getDocentesAsignaturaHoraClase() {
	return docentesAsignaturaHoraClase;
    }

    /**
     * @return the aNIOS
     */
    public List<SelectItem> getANIOS() {
	return ANIOS;
    }

    /**
     * @return the meses
     */
    public SelectItem[] getMESES() {
	return MESES;
    }

    /**
     * @return the anioInicioPeriodoAcademico
     */
    public int getAnioInicioPeriodoAcademico() {
	return anioInicioPeriodoAcademico;
    }

    /**
     * @param anioInicioPeriodoAcademico
     *            the anioInicioPeriodoAcademico to set
     */
    public void setAnioInicioPeriodoAcademico(int anioInicioPeriodoAcademico) {
	this.anioInicioPeriodoAcademico = anioInicioPeriodoAcademico;
    }

    /**
     * @return the mesInicioPeriodoAcademico
     */
    public int getMesInicioPeriodoAcademico() {
	return mesInicioPeriodoAcademico;
    }

    /**
     * @param mesInicioPeriodoAcademico
     *            the mesInicioPeriodoAcademico to set
     */
    public void setMesInicioPeriodoAcademico(int mesInicioPeriodoAcademico) {
	this.mesInicioPeriodoAcademico = mesInicioPeriodoAcademico;
    }

    /**
     * @return the anioFinPeriodoAcademico
     */
    public int getAnioFinPeriodoAcademico() {
	return anioFinPeriodoAcademico;
    }

    /**
     * @param anioFinPeriodoAcademico
     *            the anioFinPeriodoAcademico to set
     */
    public void setAnioFinPeriodoAcademico(int anioFinPeriodoAcademico) {
	this.anioFinPeriodoAcademico = anioFinPeriodoAcademico;
    }

    /**
     * @return the mesFinPeriodoAcademico
     */
    public int getMesFinPeriodoAcademico() {
	return mesFinPeriodoAcademico;
    }

    /**
     * @param mesFinPeriodoAcademico
     *            the mesFinPeriodoAcademico to set
     */
    public void setMesFinPeriodoAcademico(int mesFinPeriodoAcademico) {
	this.mesFinPeriodoAcademico = mesFinPeriodoAcademico;
    }

    public FaseIesDTO getFaseIesDTO() {
	return faseIesDTO;
    }

    public void setFaseIesDTO(FaseIesDTO faseIesDTO) {
	this.faseIesDTO = faseIesDTO;
    }

    public List<RelacionIESEnum> getRelacionesIes() {
	return Arrays.asList(RelacionIESEnum.values());
    }

    public List<TiempoDedicacionEnum> getTiemposDedicacion() {
	return tiemposDedicacion;
    }

    public List<CategoriaTitularEnum> getCategorias() {
	return categorias;
    }

    public List<AutoridadAcademicaEnum> getCargosAcademicos() {
	return Arrays.asList(AutoridadAcademicaEnum.values());
    }

    public void setRelacionesIes(List<RelacionIESEnum> relacionesIes) {
	this.relacionesIes = relacionesIes;
    }

    public void setTiemposDedicacion(
	    List<TiempoDedicacionEnum> tiemposDedicacion) {
	this.tiemposDedicacion = tiemposDedicacion;
    }

    public void setCategorias(List<CategoriaTitularEnum> categorias) {
	this.categorias = categorias;
    }

    public void setCargosAcademicos(
	    List<AutoridadAcademicaEnum> cargosAcademicos) {
	this.cargosAcademicos = cargosAcademicos;
    }

    public List<GradoTituloDTO> getGradosTitulo() {
	return gradosTitulo;
    }

    public void setGradosTitulo(List<GradoTituloDTO> gradosTitulo) {
	this.gradosTitulo = gradosTitulo;
    }

    private Calendar asignarHoraCero(Calendar fecha) {
	fecha.set(Calendar.HOUR_OF_DAY, 0);
	fecha.set(Calendar.MINUTE, 0);
	fecha.set(Calendar.SECOND, 0);
	fecha.set(Calendar.MILLISECOND, 0);
	return fecha;
    }

    public boolean isEsTitular() {
	return esTitular;
    }

    public void setEsTitular(boolean esTitular) {
	this.esTitular = esTitular;
    }

    public GradoTituloDTO getGradoSeleccionado() {
	return gradoSeleccionado;
    }

    public void setGradoSeleccionado(GradoTituloDTO gradoSeleccionado) {
	this.gradoSeleccionado = gradoSeleccionado;
    }

    public List<ContratacionPeriodoAcademicoDTO> getHorasNoAcademicasEliminar() {
	return horasNoAcademicasEliminar;
    }

    public void setHorasNoAcademicasEliminar(
	    List<ContratacionPeriodoAcademicoDTO> horasNoAcademicasEliminar) {
	this.horasNoAcademicasEliminar = horasNoAcademicasEliminar;
    }

    public Integer[] getAniosContrato() {
	llenarAniosContrato();
	return aniosContrato;
    }

    public void setAniosContrato(Integer[] aniosContrato) {
	this.aniosContrato = aniosContrato;
    }

    public ExperienciaProfesionalDTO getExperienciaProfesionalDocente() {
	return experienciaProfesionalDocente;
    }

    public void setExperienciaProfesionalDocente(
	    ExperienciaProfesionalDTO experienciaProfesionalDocente) {
	this.experienciaProfesionalDocente = experienciaProfesionalDocente;
    }

    public List<ExperienciaProfesionalDTO> getListaExperienciaProfesionalDTO() {
	return listaExperienciaProfesionalDTO;
    }

    public void setListaExperienciaProfesionalDTO(
	    List<ExperienciaProfesionalDTO> listaExperienciaProfesionalDTO) {
	this.listaExperienciaProfesionalDTO = listaExperienciaProfesionalDTO;
    }

    public String getIdentificacion() {
	return identificacion;
    }

    public void setIdentificacion(String identificacion) {
	this.identificacion = identificacion;
    }

    public boolean isVerTabExperienciaProf() {
	return verTabExperienciaProf;
    }

    public void setVerTabExperienciaProf(boolean verTabExperienciaProf) {
	this.verTabExperienciaProf = verTabExperienciaProf;
    }

    public List<String> getTipoPrograma() {
	return tipoPrograma;
    }

    public void setTipoPrograma(List<String> tipoPrograma) {
	this.tipoPrograma = tipoPrograma;
    }

    public String getTipoProg() {
	return tipoProg;
    }

    public void setTipoProg(String tipoProg) {
	this.tipoProg = tipoProg;
    }

    public Boolean getAlertaEvaluador() {
	return alertaEvaluador;
    }

    public void setAlertaEvaluador(Boolean alertaEvaluador) {
	this.alertaEvaluador = alertaEvaluador;
    }

    public Integer getTotalDocentes() {
	return totalDocentes;
    }

    public void setTotalDocentes(Integer totalDocentes) {
	this.totalDocentes = totalDocentes;
    }

    public String getEtniaSeleccionada() {
	return etniaSeleccionada;
    }

    public void setEtniaSeleccionada(String etniaSeleccionada) {
	this.etniaSeleccionada = etniaSeleccionada;
    }

    public List<String> getEtnias() {
	return etnias;
    }

    public void setEtnias(List<String> etnias) {
	this.etnias = etnias;
    }

    public List<PuebloDTO> getPueblos() {
	return pueblos;
    }

    public void setPueblos(List<PuebloDTO> pueblos) {
	this.pueblos = pueblos;
    }

    public List<RegionDTO> getRegiones() {
	return regiones;
    }

    public void setRegiones(List<RegionDTO> regiones) {
	this.regiones = regiones;
    }

    public Long getRegionSeleccionada() {
	return regionSeleccionada;
    }

    public void setRegionSeleccionada(Long regionSeleccionada) {
	this.regionSeleccionada = regionSeleccionada;
    }

    public Long getIdNivelTituloSelecionadoOtros() {
	return idNivelTituloSelecionadoOtros;
    }

    public void setIdNivelTituloSelecionadoOtros(
	    Long idNivelTituloSelecionadoOtros) {
	this.idNivelTituloSelecionadoOtros = idNivelTituloSelecionadoOtros;
    }

    public Long getIdNivelTituloSelecionado() {
	return idNivelTituloSelecionado;
    }

    public void setIdNivelTituloSelecionado(Long idNivelTituloSelecionado) {
	this.idNivelTituloSelecionado = idNivelTituloSelecionado;
    }

    public Boolean getMostrarAsignacion() {
	return mostrarAsignacion;
    }

    public void setMostrarAsignacion(Boolean mostrarAsignacion) {
	this.mostrarAsignacion = mostrarAsignacion;
    }

    public List<DocenteAsignaturaDTO> getListaAsignaturasxDocente() {
	return listaAsignaturasxDocente;
    }

    public void setListaAsignaturasxDocente(
	    List<DocenteAsignaturaDTO> listaAsignaturasxDocente) {
	this.listaAsignaturasxDocente = listaAsignaturasxDocente;
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

    public Long getIdInformacionCarreraSeleccionada() {
	return idInformacionCarreraSeleccionada;
    }

    public void setIdInformacionCarreraSeleccionada(
	    Long idInformacionCarreraSeleccionada) {
	this.idInformacionCarreraSeleccionada = idInformacionCarreraSeleccionada;
    }

    public List<InformacionCarreraDTO> getListaInformacionCarreraDto() {
	return listaInformacionCarreraDto;
    }

    public void setListaInformacionCarreraDto(
	    List<InformacionCarreraDTO> listaInformacionCarreraDto) {
	this.listaInformacionCarreraDto = listaInformacionCarreraDto;
    }

    public Long getIdMallaSeleccionada() {
	return idMallaSeleccionada;
    }

    public void setIdMallaSeleccionada(Long idMallaSeleccionada) {
	this.idMallaSeleccionada = idMallaSeleccionada;
    }

    public List<MallaCurricularDTO> getListaMallaDto() {
	return listaMallaDto;
    }

    public void setListaMallaDto(List<MallaCurricularDTO> listaMallaDto) {
	this.listaMallaDto = listaMallaDto;
    }

    public List<AsignaturaDTO> getListaAsignatura() {
	return listaAsignatura;
    }

    public void setListaAsignatura(List<AsignaturaDTO> listaAsignatura) {
	this.listaAsignatura = listaAsignatura;
    }

    public List<DocenteAsignaturaDTO> getListaDocenteAsignatura() {
	return listaDocenteAsignatura;
    }

    public void setListaDocenteAsignatura(
	    List<DocenteAsignaturaDTO> listaDocenteAsignatura) {
	this.listaDocenteAsignatura = listaDocenteAsignatura;
    }

    public List<DocenteAsignaturaDTO> getListaDocenteAsignaturaEditable() {
	return listaDocenteAsignaturaEditable;
    }

    public void setListaDocenteAsignaturaEditable(
	    List<DocenteAsignaturaDTO> listaDocenteAsignaturaEditable) {
	this.listaDocenteAsignaturaEditable = listaDocenteAsignaturaEditable;
    }

    public Long getIdSedeIesSeleccionada2() {
	return idSedeIesSeleccionada2;
    }

    public void setIdSedeIesSeleccionada2(Long idSedeIesSeleccionada2) {
	this.idSedeIesSeleccionada2 = idSedeIesSeleccionada2;
    }

    public Long getIdInformacionCarreraSeleccionada2() {
	return idInformacionCarreraSeleccionada2;
    }

    public void setIdInformacionCarreraSeleccionada2(
	    Long idInformacionCarreraSeleccionada2) {
	this.idInformacionCarreraSeleccionada2 = idInformacionCarreraSeleccionada2;
    }

    public Long getIdMallaSeleccionada2() {
	return idMallaSeleccionada2;
    }

    public void setIdMallaSeleccionada2(Long idMallaSeleccionada2) {
	this.idMallaSeleccionada2 = idMallaSeleccionada2;
    }

    public List<SedeIesDTO> getListaSedeIesAsignaturaDto2() {
	return listaSedeIesAsignaturaDto2;
    }

    public void setListaSedeIesAsignaturaDto2(
	    List<SedeIesDTO> listaSedeIesAsignaturaDto2) {
	this.listaSedeIesAsignaturaDto2 = listaSedeIesAsignaturaDto2;
    }

    public List<InformacionCarreraDTO> getListaInformacionCarreraDto2() {
	return listaInformacionCarreraDto2;
    }

    public void setListaInformacionCarreraDto2(
	    List<InformacionCarreraDTO> listaInformacionCarreraDto2) {
	this.listaInformacionCarreraDto2 = listaInformacionCarreraDto2;
    }

    public List<MallaCurricularDTO> getListaMallaDto2() {
	return listaMallaDto2;
    }

    public void setListaMallaDto2(List<MallaCurricularDTO> listaMallaDto2) {
	this.listaMallaDto2 = listaMallaDto2;
    }

    public DualListModel<AsignaturaDTO> getListaAsignaturasDTO() {
	return listaAsignaturasDTO;
    }

    public void setListaAsignaturasDTO(
	    DualListModel<AsignaturaDTO> listaAsignaturasDTO) {
	this.listaAsignaturasDTO = listaAsignaturasDTO;
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

    public DocenteAsignaturaDTO getDocenteAsigEditable() {
	return docenteAsigEditable;
    }

    public void setDocenteAsigEditable(DocenteAsignaturaDTO docenteAsigEditable) {
	this.docenteAsigEditable = docenteAsigEditable;
    }

    public List<DocenteAsignaturaDTO> getListaDocenteAsignaturaCompartidaEliminar() {
	return listaDocenteAsignaturaCompartidaEliminar;
    }

    public void setListaDocenteAsignaturaCompartidaEliminar(
	    List<DocenteAsignaturaDTO> listaDocenteAsignaturaCompartidaEliminar) {
	this.listaDocenteAsignaturaCompartidaEliminar = listaDocenteAsignaturaCompartidaEliminar;
    }

    public List<DocenteAsignaturaDTO> getListaDocenteAsignaturaCompartidaEliminarOriginal() {
	return listaDocenteAsignaturaCompartidaEliminarOriginal;
    }

    public void setListaDocenteAsignaturaCompartidaEliminarOriginal(
	    List<DocenteAsignaturaDTO> listaDocenteAsignaturaCompartidaEliminarOriginal) {
	this.listaDocenteAsignaturaCompartidaEliminarOriginal = listaDocenteAsignaturaCompartidaEliminarOriginal;
    }

    // get and set

    public FaseDTO getFaseEvaluacionDTO() {

	return faseEvaluacionDTO;
    }

    public void setFaseEvaluacionDTO(FaseDTO faseEvaluacionDTO) {
	this.faseEvaluacionDTO = faseEvaluacionDTO;
    }

    public InformacionIesDTO getInformacionIesDTO() {

	return informacionIesDTO;
    }

    public void setInformacionIesDTO(InformacionIesDTO informacionIesDTO) {
	this.informacionIesDTO = informacionIesDTO;
    }

    public InformacionCarreraDTO getInformacionCarreraDTO() {
	try {
	    // carreraServicio
	    // .obtenerInformacionCarreraPorCarrera(carrerasCarrera);
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return informacionCarreraDTO;
    }

    public void setInformacionCarreraDTO(
	    InformacionCarreraDTO informacionCarreraDTO) {
	this.informacionCarreraDTO = informacionCarreraDTO;
    }

    public boolean isEditarContrato() {
	return editarContrato;
    }

    public void setEditarContrato(boolean editarContrato) {
	this.editarContrato = editarContrato;
    }

    public Evidencia getArchivoSeleccionado() {
	return archivoSeleccionado;
    }

    public void setArchivoSeleccionado(Evidencia archivoSeleccionado) {
	this.archivoSeleccionado = archivoSeleccionado;
    }

    public boolean isVerTabAsignaturas() {
	return verTabAsignaturas;
    }

    public void setVerTabAsignaturas(boolean verTabAsignaturas) {
	this.verTabAsignaturas = verTabAsignaturas;
    }

    /**
     * @return the idArea
     */
    public Long getIdArea() {
	return idArea;
    }

    /**
     * @return the areasConocimiento
     */
    public List<AreaConocimientoDTO> getAreasConocimiento() {
	return areasConocimiento;
    }

    /**
     * @param idArea
     *            the idArea to set
     */
    public void setIdArea(Long idArea) {
	this.idArea = idArea;
    }

    /**
     * @param areasConocimiento
     *            the areasConocimiento to set
     */
    public void setAreasConocimiento(List<AreaConocimientoDTO> areasConocimiento) {
	this.areasConocimiento = areasConocimiento;
    }

    /**
     * @return the idSubArea
     */
    public Long getIdSubArea() {
	return idSubArea;
    }

    /**
     * @return the subareas
     */
    public List<SubAreaConocimientoDTO> getSubareas() {
	return subareas;
    }

    /**
     * @param idSubArea
     *            the idSubArea to set
     */
    public void setIdSubArea(Long idSubArea) {
	this.idSubArea = idSubArea;
    }

    /**
     * @param subareas
     *            the subareas to set
     */
    public void setSubareas(List<SubAreaConocimientoDTO> subareas) {
	this.subareas = subareas;
    }

    /**
     * @return the idNivelTitulo
     */
    public Long getIdNivelTitulo() {
	return idNivelTitulo;
    }

    /**
     * @param idNivelTitulo
     *            the idNivelTitulo to set
     */
    public void setIdNivelTitulo(Long idNivelTitulo) {
	this.idNivelTitulo = idNivelTitulo;
    }

    /**
     * @return the cursoCapacitacion
     */
    public CursoCapacitacionDTO getCursoCapacitacion() {
	return cursoCapacitacion;
    }

    /**
     * @param cursoCapacitacion
     *            the cursoCapacitacion to set
     */
    public void setCursoCapacitacion(CursoCapacitacionDTO cursoCapacitacion) {
	this.cursoCapacitacion = cursoCapacitacion;
    }

    /**
     * @return the cursos
     */
    public List<CursoCapacitacionDTO> getCursos() {
	return cursos;
    }

    /**
     * @param cursos
     *            the cursos to set
     */
    public void setCursos(List<CursoCapacitacionDTO> cursos) {
	this.cursos = cursos;
    }

    /**
     * @return the verTabCursos
     */
    public boolean isVerTabCursos() {
	return verTabCursos;
    }

    /**
     * @param verTabCursos
     *            the verTabCursos to set
     */
    public void setVerTabCursos(boolean verTabCursos) {
	this.verTabCursos = verTabCursos;
    }

    /**
     * @return the categoria
     */
    public String getCategoria() {
	return categoria;
    }

    /**
     * @param categoria
     *            the categoria to set
     */
    public void setCategoria(String categoria) {
	this.categoria = categoria;
    }

    /**
     * @return the carreraSeleccionada
     */
    public CarreraIesDTO getCarreraSeleccionada() {
	ListaIesController controller = (ListaIesController) FacesContext
	        .getCurrentInstance().getExternalContext().getSessionMap()
	        .get("listaIesController");
	carreraSeleccionada = controller.getCarrera();

	return carreraSeleccionada;
    }

    /**
     * @param carreraSeleccionada
     *            the carreraSeleccionada to set
     */
    public void setCarreraSeleccionada(CarreraIesDTO carreraSeleccionada) {
	this.carreraSeleccionada = carreraSeleccionada;
    }

    /**
     * @return the fechaMin
     */
    public Date getFechaMin() {
	Calendar cal = Calendar.getInstance();
	int anio = Calendar.getInstance().get(Calendar.YEAR) - 75;
	cal.set(anio, Calendar.JANUARY, 1);
	fechaMin = new Date(cal.getTimeInMillis());
	return fechaMin;
    }

    /**
     * @param fechaMin
     *            the fechaMin to set
     */
    public void setFechaMin(Date fechaMin) {
	this.fechaMin = fechaMin;
    }

    /**
     * @return the fechaMax
     */

    public Date getFechaMax() {
	Calendar cal = Calendar.getInstance();
	int anio = Calendar.getInstance().get(Calendar.YEAR) - 21;
	cal.set(anio, Calendar.JANUARY, 365);
	fechaMax = new Date(cal.getTimeInMillis());
	return fechaMax;
    }

    /**
     * @param fechaMax
     *            the fechaMax to set
     */
    public void setFechaMax(Date fechaMax) {
	this.fechaMax = fechaMax;
    }

    /**
     * @return the fechaActual
     */
    public Date getFechaActual() {
	Calendar cal = Calendar.getInstance();
	fechaActual = new Date(cal.getTimeInMillis());
	return fechaActual;
    }

    /**
     * @param fechaActual
     *            the fechaActual to set
     */
    public void setFechaActual(Date fechaActual) {
	this.fechaActual = fechaActual;
    }

    /**
     * @return the idPaisFormacion
     */
    public Long getIdPaisFormacion() {
	return idPaisFormacion;
    }

    /**
     * @param idPaisFormacion
     *            the idPaisFormacion to set
     */
    public void setIdPaisFormacion(Long idPaisFormacion) {
	this.idPaisFormacion = idPaisFormacion;
    }

    /**
     * @return the cargo
     */
    public String getCargo() {
	return cargo;
    }

    /**
     * @param cargo
     *            the cargo to set
     */
    public void setCargo(String cargo) {
	this.cargo = cargo;
    }

    /**
     * @return the cedulaDocente
     */
    public String getCedulaDocente() {
	return cedulaDocente;
    }

    /**
     * @param cedulaDocente
     *            the cedulaDocente to set
     */
    public void setCedulaDocente(String cedulaDocente) {
	this.cedulaDocente = cedulaDocente;
    }

    /**
     * @return the msgAccion
     */
    public String getMsgAccion() {
	return msgAccion;
    }

    /**
     * @param msgAccion
     *            the msgAccion to set
     */
    public void setMsgAccion(String msgAccion) {
	this.msgAccion = msgAccion;
    }

    /**
     * @return the cargoDirectivoDocente
     */
    public CargoAcademicoDTO getCargoDirectivoDocente() {
	return cargoDirectivoDocente;
    }

    /**
     * @param cargoDirectivoDocente
     *            the cargoDirectivoDocente to set
     */
    public void setCargoDirectivoDocente(CargoAcademicoDTO cargoDirectivoDocente) {
	this.cargoDirectivoDocente = cargoDirectivoDocente;
    }

    /**
     * @return the cargosDirectivos
     */
    public List<CargoAcademicoDTO> getCargosDirectivos() {
	return cargosDirectivos;
    }

    /**
     * @param cargosDirectivos
     *            the cargosDirectivos to set
     */
    public void setCargosDirectivos(List<CargoAcademicoDTO> cargosDirectivos) {
	this.cargosDirectivos = cargosDirectivos;
    }

    /**
     * @return the formaciones
     */
    public List<FormacionProfesionalDTO> getFormaciones() {
	return formaciones;
    }

    /**
     * @param formaciones
     *            the formaciones to set
     */
    public void setFormaciones(List<FormacionProfesionalDTO> formaciones) {
	this.formaciones = formaciones;
    }

    /**
     * @return the contrataciones
     */
    public List<ContratacionDTO> getContrataciones() {
	return contrataciones;
    }

    /**
     * @param contrataciones
     *            the contrataciones to set
     */
    public void setContrataciones(List<ContratacionDTO> contrataciones) {
	this.contrataciones = contrataciones;
    }

    /**
     * @return the idPais
     */
    public Long getIdPais() {
	return idPais;
    }

    /**
     * @param idPais
     *            the idPais to set
     */
    public void setIdPais(Long idPais) {
	this.idPais = idPais;
    }

    /**
     * @return the paises
     */
    public List<PaisDTO> getPaises() {
	return paises;
    }

    /**
     * @param paises
     *            the paises to set
     */
    public void setPaises(List<PaisDTO> paises) {
	this.paises = paises;
    }

    /**
     * @return the indiceTab
     */
    public int getIndiceTab() {
	return indiceTab;
    }

    /**
     * @param indiceTab
     *            the indiceTab to set
     */
    public void setIndiceTab(int indiceTab) {
	this.indiceTab = indiceTab;
    }

    /**
     * @return the verTabDatosPersonales
     */
    public boolean isVerTabDatosPersonales() {
	return verTabDatosPersonales;
    }

    /**
     * @param verTabDatosPersonales
     *            the verTabDatosPersonales to set
     */
    public void setVerTabDatosPersonales(boolean verTabDatosPersonales) {
	this.verTabDatosPersonales = verTabDatosPersonales;
    }

    /**
     * @return the verTabContratacion
     */
    public boolean isVerTabContratacion() {
	return verTabContratacion;
    }

    /**
     * @param verTabContratacion
     *            the verTabContratacion to set
     */
    public void setVerTabContratacion(boolean verTabContratacion) {
	this.verTabContratacion = verTabContratacion;
    }

    /**
     * @return the verTabFormacion
     */
    public boolean isVerTabFormacion() {
	return verTabFormacion;
    }

    /**
     * @param verTabFormacion
     *            the verTabFormacion to set
     */
    public void setVerTabFormacion(boolean verTabFormacion) {
	this.verTabFormacion = verTabFormacion;
    }

    /**
     * @return the verTabCargosDireccion
     */
    public boolean isVerTabCargosDireccion() {
	return verTabCargosDireccion;
    }

    /**
     * @param verTabCargosDireccion
     *            the verTabCargosDireccion to set
     */
    public void setVerTabCargosDireccion(boolean verTabCargosDireccion) {
	this.verTabCargosDireccion = verTabCargosDireccion;
    }

    /**
     * @return the clienteId
     */
    public String getClienteId() {
	return clienteId;
    }

    /**
     * @param clienteId
     *            the clienteId to set
     */
    public void setClienteId(String clienteId) {
	this.clienteId = clienteId;
    }

    /**
     * @return the accion
     */
    public String getAccion() {
	return accion;
    }

    /**
     * @param accion
     *            the accion to set
     */
    public void setAccion(String accion) {
	this.accion = accion;
    }

    /**
     * @return the personaDocente
     */
    public PersonaDTO getPersonaDocente() {
	return personaDocente;
    }

    /**
     * @param personaDocente
     *            the personaDocente to set
     */
    public void setPersonaDocente(PersonaDTO personaDocente) {
	this.personaDocente = personaDocente;
    }

    /**
     * @return the formacionDocente
     */
    public FormacionProfesionalDTO getFormacionDocente() {
	return formacionDocente;
    }

    /**
     * @param formacionDocente
     *            the formacionDocente to set
     */
    public void setFormacionDocente(FormacionProfesionalDTO formacionDocente) {
	this.formacionDocente = formacionDocente;
    }

    /**
     * @return the contratoDocente
     */
    public ContratacionDTO getContratoDocente() {
	return contratoDocente;
    }

    /**
     * @param contratoDocente
     *            the contratoDocente to set
     */
    public void setContratoDocente(ContratacionDTO contratoDocente) {
	this.contratoDocente = contratoDocente;
	this.contratoDocente.setPersonaDTO(docenteSeleccionado);
	if (contratoDocente.getCategoria() != null) {
	    if (contratoDocente.getCategoria().toString().startsWith("TITULAR")) {
		esTitular = true;
	    } else {
		esTitular = false;
	    }
	}

	categorias = new ArrayList<CategoriaTitularEnum>();
	if (contratoDocente.getRelacionIes() != null) {
	    if (contratoDocente
		    .getRelacionIes()
		    .toString()
		    .equals(RelacionIESEnum.CONTRATO_SIN_RELACION_DE_DEPENDENCIA
		            .toString())) {
		for (CategoriaTitularEnum cat : CategoriaTitularEnum.values()) {
		    if (cat.getValue().startsWith("TITULAR")) {
			continue;
		    } else {
			this.categorias.add(cat);
		    }
		}
	    } else {
		categorias = Arrays.asList(CategoriaTitularEnum.values());
	    }
	}
	llenarAniosContrato();
	// listarEvidenciasContrato();
    }

    /**
     * @return the verBtnIesDoc
     */
    public boolean isVerBtnIesDoc() {
	return verBtnIesDoc;
    }

    /**
     * @param verBtnIesDoc
     *            the verBtnIesDoc to set
     */
    public void setVerBtnIesDoc(boolean verBtnIesDoc) {
	this.verBtnIesDoc = verBtnIesDoc;
    }

    /**
     * @return the verBtnIesCon
     */
    public boolean isVerBtnIesCon() {
	return verBtnIesCon;
    }

    /**
     * @param verBtnIesCon
     *            the verBtnIesCon to set
     */
    public void setVerBtnIesCon(boolean verBtnIesCon) {
	this.verBtnIesCon = verBtnIesCon;
    }

    /**
     * @return the verBtnIesFor
     */
    public boolean isVerBtnIesFor() {
	return verBtnIesFor;
    }

    /**
     * @param verBtnIesFor
     *            the verBtnIesFor to set
     */
    public void setVerBtnIesFor(boolean verBtnIesFor) {
	this.verBtnIesFor = verBtnIesFor;
    }

    /**
     * @return the pestania
     */
    public String getPestania() {
	return pestania;
    }

    /**
     * @param pestania
     *            the pestania to set
     */
    public void setPestania(String pestania) {
	this.pestania = pestania;
    }

    /**
     * @return the iesFormacion
     */
    public IesDTO getIesFormacion() {
	return iesFormacion;
    }

    /**
     * @param iesFormacion
     *            the iesFormacion to set
     */
    public void setIesFormacion(IesDTO iesFormacion) {
	this.iesFormacion = iesFormacion;
	formacionDocente.setNombreIesInternacional(iesFormacion.getNombre());
	formacionDocente.setIes(iesFormacion);
    }

    /**
     * @return the verTablaIes
     */
    public boolean isVerTablaIes() {
	return verTablaIes;
    }

    /**
     * @param verTablaIes
     *            the verTablaIes to set
     */
    public void setVerTablaIes(boolean verTablaIes) {
	this.verTablaIes = verTablaIes;
    }

    /**
     * @return the genero
     */
    public String getGenero() {
	return genero;
    }

    /**
     * @param genero
     *            the genero to set
     */
    public void setGenero(String genero) {
	this.genero = genero;
    }

    /**
     * @return the generos
     */
    public List<String> getGeneros() {
	return generos;
    }

    /**
     * @param generos
     *            the generos to set
     */
    public void setGeneros(List<String> generos) {
	this.generos = generos;
    }

    /**
     * @return the discapacidad
     */
    public String getDiscapacidad() {
	return discapacidad;
    }

    /**
     * @param discapacidad
     *            the discapacidad to set
     */
    public void setDiscapacidad(String discapacidad) {
	this.discapacidad = discapacidad;
    }

    /**
     * @return the discapacidades
     */
    public List<String> getDiscapacidades() {
	return discapacidades;
    }

    /**
     * @param discapacidades
     *            the discapacidades to set
     */
    public void setDiscapacidades(List<String> discapacidades) {
	this.discapacidades = discapacidades;
    }

    /**
     * @return the docenteSeleccionado
     */
    public DocenteDTO getDocenteSeleccionado() {
	return docenteSeleccionado;
    }

    /**
     * @param docenteSeleccionado
     *            the docenteSeleccionado to set
     */
    public void setDocenteSeleccionado(DocenteDTO docenteSeleccionado) {
	this.docenteSeleccionado = docenteSeleccionado;
	try {
	    this.docenteSeleccionado = registroServicio
		    .obtenerDocentePorId(docenteSeleccionado.getId());

	    this.pestania = "Datos Personales";
	    // if (perfil.startsWith("CAR")) {
	    // DocenteAsignaturaController asignaturaController =
	    // (DocenteAsignaturaController) FacesContext
	    // .getCurrentInstance().getExternalContext().getSessionMap()
	    // .get("docenteAsigController");
	    // if (asignaturaController != null) {
	    // asignaturaController.setInfoIes(informacionIesDTO);
	    // asignaturaController.setInfoCarreraDTO(informacionCarreraDTO);
	    // asignaturaController.getAsignaturasDTO().clear();
	    // asignaturaController.setSeleccion(docenteSeleccionado);
	    // }
	    // validarTituloSenescyt();
	    activarTab();
	    editarContrato = false;
	    mostrarAsignaturas = true;
	    // }
	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ocurrió un error al consultar el docente.");
	}
    }

    public void validarTituloSenescyt() {
	try {
	    boolean tituloEncontrado = false;

	    tituloEncontrado = false;
	    this.cargarTitulosSenescyt(docenteSeleccionado.getIdentificacion());
	    List<FormacionProfesionalDTO> formacion = docenteSeleccionado
		    .getFormacionDTO();
	    if (formacion != null) {
		for (FormacionProfesionalDTO tituloDoc : formacion) {
		    if (tituloDoc.getObservacionEvaluador() == null
			    || tituloDoc.getObservacionEvaluador().isEmpty()) {
			for (TituloSenescyt titulo : this.titulosSenescyt) {
			    if (titulo.getRegistroSenescyt().equals(
				    tituloDoc.getNumeroRegistroSenescyt()
				            .trim())
				    && tituloDoc.getNivel().equalsIgnoreCase(
				            titulo.getNivel())) {
				LOG.info("registro senescyt");
				tituloEncontrado = true;
				tituloDoc.setValidadoSenescyt(true);
				tituloDoc.setAceptadoEvaluador(true);
				tituloDoc
				        .setObservacionEvaluador("Validado Senescyt");
				if (tituloDoc.getGrado().getId() == null) {
				    tituloDoc.setGrado(null);
				}
				registroServicio.registrarFormacion(tituloDoc);
				break;
			    }
			    LOG.info("Validando Senescyt");
			}

		    } else {
			if (tituloDoc.getAceptadoEvaluador()) {
			    tituloDoc.setValidadoSenescyt(true);
			}
		    }
		    LOG.info("siguiente título");

		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	    return;
	}
    }

    /**
     * @return the docentesFiltros
     */
    public List<DocenteDTO> getDocentesFiltros() {
	return docentesFiltros;
    }

    /**
     * @param docentesFiltros
     *            the docentesFiltros to set
     */
    public void setDocentesFiltros(List<DocenteDTO> docentesFiltros) {
	this.docentesFiltros = docentesFiltros;
    }

    /**
     * @return the iesFiltros
     */
    public List<IesDTO> getIesFiltros() {
	return iesFiltros;
    }

    /**
     * @param iesFiltros
     *            the iesFiltros to set
     */
    public void setIesFiltros(List<IesDTO> iesFiltros) {
	this.iesFiltros = iesFiltros;
    }

    /**
     * @return the listaIes
     */
    public List<IesDTO> getListaIes() {
	iesFiltros.clear();
	return listaIes;
    }

    /**
     * @param listaIes
     *            the listaIes to set
     */
    public void setListaIes(List<IesDTO> listaIes) {
	this.listaIes = listaIes;
    }

    /**
     * @return the docentes
     */
    public List<DocenteDTO> getDocentes() {
	boolean cargar = false;
	if (!busqueda) {
	    try {
		ListaIesController controller = (ListaIesController) FacesContext
		        .getCurrentInstance().getExternalContext()
		        .getSessionMap().get("listaIesController");
		perfil = controller.getPerfil().getNombre();
		if (informacionIesDTO == null) {
		    iesSeleccionada = controller.getIes();
		    informacionIesDTO = registroServicio
			    .obtenerInformacionIesPorIes(controller.getIes());

		    cargar = true;

		} else {
		    // if (controller.getPerfil().getNombre().startsWith("CAR"))
		    // {
		    // if (informacionCarreraDTO == null) {
		    // informacionCarreraDTO = institutoServicio
		    // .obtenerInformacionCarreraPorCarrera(controller
		    // .getCarrera());
		    // cargar = true;
		    // } else {
		    // if (!informacionCarreraDTO.getCarreraIesDTO()
		    // .getId()
		    // .equals(controller.getCarrera().getId())) {
		    // cargar = true;
		    // }
		    // }
		    // } else if (!controller.getIes().getId()
		    // .equals(iesSeleccionada.getId())
		    // || informacionCarreraDTO != null) {
		    // cargar = true;
		    // informacionCarreraDTO = null;
		    //
		    // }
		}
		if (cargar) {
		    this.faseIesDTO = controller.getFaseIesDTO();
		    limpiarListas();
		    cargarDocentes();
		}
	    } catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	}
	return docentes;
    }

    /**
     * @param docentes
     *            the docentes to set
     */
    public void setDocentes(List<DocenteDTO> docentes) {
	this.docentes = docentes;
    }

    /**
     * Seteo de ies
     * 
     * @param iesSeleccionada
     *            the iesSeleccionada to set
     */
    public void setIesSeleccionada(IesDTO iesSeleccionada) {
	this.iesSeleccionada = iesSeleccionada;
    }

    /**
     * @return the iesSeleccionada
     */
    public IesDTO getIesSeleccionada() {
	ListaIesController controller = (ListaIesController) FacesContext
	        .getCurrentInstance().getExternalContext().getSessionMap()
	        .get("listaIesController");
	iesSeleccionada = controller.getIes();
	return iesSeleccionada;
    }

    public String getPerfil() {
	return perfil;
    }

    public void setPerfil(String perfil) {
	this.perfil = perfil;
    }

    public boolean isMostrarAsignaturas() {
	return mostrarAsignaturas;
    }

    public void setMostrarAsignaturas(boolean mostrarAsignaturas) {
	this.mostrarAsignaturas = mostrarAsignaturas;
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

    public int getNumRegistros() {
	return numRegistros;
    }

    public void setNumRegistros(int numRegistros) {
	this.numRegistros = numRegistros;
    }

    public List<DocenteDTO> getListaDocentesTodos() {
	return listaDocentesTodos;
    }

    public void setListaDocentesTodos(List<DocenteDTO> listaDocentesTodos) {
	this.listaDocentesTodos = listaDocentesTodos;
    }

    public boolean isBusqueda() {
	return busqueda;
    }

    public void setBusqueda(boolean busqueda) {
	this.busqueda = busqueda;
    }

    public Integer[] getRangos() {
	return rangos;
    }

    public int getContador() {
	return contador;
    }

    public void setContador(int contador) {
	this.contador = contador;
    }

    public List<ContratacionPeriodoAcademicoDTO> getContratacionesPeriodoAcademico() {
	return contratacionesPeriodoAcademico;
    }

    public void setContratacionesPeriodoAcademico(
	    List<ContratacionPeriodoAcademicoDTO> contratacionesPeriodoAcademico) {
	this.contratacionesPeriodoAcademico = contratacionesPeriodoAcademico;
    }

    public ContratacionPeriodoAcademicoDTO getContratacionPeriodoAcademicoDTO() {
	return contratacionPeriodoAcademicoDTO;
    }

    public void setContratacionPeriodoAcademicoDTO(
	    ContratacionPeriodoAcademicoDTO contratacionPeriodoAcademicoDTO) {
	this.contratacionPeriodoAcademicoDTO = contratacionPeriodoAcademicoDTO;
    }

    public List<ConceptoDTO> getListaEvidencia() {
	return listaEvidencia;
    }

    public void setListaEvidencia(List<ConceptoDTO> listaEvidencia) {
	this.listaEvidencia = listaEvidencia;
    }

    public Long getIdProceso() {
	return idProceso;
    }

    public void setIdProceso(Long idProceso) {
	this.idProceso = idProceso;
    }

    public ConceptoDTO getConceptoSeleccionado() {
	return conceptoSeleccionado;
    }

    public void setConceptoSeleccionado(ConceptoDTO conceptoSeleccionado) {
	this.conceptoSeleccionado = conceptoSeleccionado;
    }

    public Boolean getEvidenciaConcepto() {
	return evidenciaConcepto;
    }

    public void setEvidenciaConcepto(Boolean evidenciaConcepto) {
	this.evidenciaConcepto = evidenciaConcepto;
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

    public void cargarTitulosSenescyt(String cedula) {
	WebServiceController wsController = (WebServiceController) FacesContext
	        .getCurrentInstance().getExternalContext().getSessionMap()
	        .get("webServiceController");
	System.out
	        .println("----------------------_#### TÍTULOS ###############-----------------------");
	wsController.cargarIdentificacion(cedula);
	wsController.consultaSenescyt();
	List<TituloSenescyt> aux = new ArrayList<TituloSenescyt>();
	aux = wsController.getTitulosReconocidos();
	for (int i = 0; i < aux.size(); i++) {
	    LOG.info(aux.get(i).getNivel());
	    LOG.info(aux.get(i).getRegistroSenescyt());
	    titulosSenescyt.add(aux.get(i));
	}

    }

    public Boolean getAlertaFase() {
	return alertaFase;
    }

    public void setAlertaFase(Boolean alertaFase) {
	this.alertaFase = alertaFase;
    }

    public String getUsuario() {
	return usuario;
    }

    public void setUsuario(String usuario) {
	this.usuario = usuario;
    }

}
