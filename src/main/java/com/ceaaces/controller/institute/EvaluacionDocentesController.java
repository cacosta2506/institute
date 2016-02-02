package ec.gob.ceaaces.controller;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.primefaces.context.RequestContext;
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;

import ec.gob.ceaaces.catalogo.dtos.catalogos.AuditoriaDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.ConceptoDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.GradoTituloDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.IesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.ParametroDTO;
import ec.gob.ceaaces.catalogo.enumeraciones.GrupoConceptoEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.ParametroEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.TipoReferenciaEnum;
import ec.gob.ceaaces.catalogo.exceptions.ServicioException;
import ec.gob.ceaaces.data.TituloSenescyt;
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
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.PeriodoAcademicoDTO;
import ec.gob.ceaaces.institutos.dtos.historico.CargosAcademicosHistoricoDTO;
import ec.gob.ceaaces.institutos.dtos.historico.ContratacionHistoricoDTO;
import ec.gob.ceaaces.institutos.dtos.historico.CursoCapacitacionHistoricoDTO;
import ec.gob.ceaaces.institutos.dtos.historico.ExperienciaProfesionalHistoricoDTO;
import ec.gob.ceaaces.institutos.dtos.historico.FormacionProfesionalHistoricoDTO;
import ec.gob.ceaaces.institutos.enumeraciones.CategoriaTitularEnum;
import ec.gob.ceaaces.institutos.enumeraciones.GeneroEnum;
import ec.gob.ceaaces.institutos.enumeraciones.OrigenInformacionEnum;
import ec.gob.ceaaces.institutos.enumeraciones.RelacionIESEnum;
import ec.gob.ceaaces.institutos.enumeraciones.TiempoDedicacionEnum;
import ec.gob.ceaaces.institutos.enumeraciones.TipoProgramaEnum;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.EvaluacionServicio;
import ec.gob.ceaaces.services.InstitutosServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.ArchivoUtil;
import ec.gob.ceaaces.util.JsfUtil;

@ManagedBean(name = "evaluacionDocentesController")
public class EvaluacionDocentesController implements Serializable {

    private static final long serialVersionUID = 1606541917026042135L;

    private static final Logger LOG = Logger
	    .getLogger(EvaluacionDocentesController.class.getSimpleName());

    private int anioInicioPeriodoAcademico;
    private int mesInicioPeriodoAcademico;
    private int anioFinPeriodoAcademico;
    private int mesFinPeriodoAcademico;
    private List<DocenteDTO> listaDocentes;
    private List<ConceptoDTO> listaEvidencia;
    private List<ContratacionPeriodoAcademicoDTO> horasNoAcademicasEliminar;
    private List<ContratacionDTO> listaContratacion;
    private List<ContratacionHistoricoDTO> listaContratacionHistorico;
    private FormacionProfesionalDTO formacionSeleccionadaHistorico;
    private List<FormacionProfesionalHistoricoDTO> listaFormacionHistorico;
    private List<GradoTituloDTO> gradosTitulo;
    private List<CursoCapacitacionHistoricoDTO> listaCapacitacionHistorico;
    private List<CargosAcademicosHistoricoDTO> listaCargoHistorico;
    private List<ExperienciaProfesionalHistoricoDTO> listaExperienciaHistorico;
    private List<ExperienciaProfesionalDTO> listaExperienciaProfesional;
    private final List<ContratacionPeriodoAcademicoDTO> contratacionesPeriodoEliminar;
    private List<CursoCapacitacionDTO> listaCapacitacion;
    private List<PeriodoAcademicoDTO> periodosAcademicos;
    private List<EvidenciaConceptoDTO> listaEvidenciaConcepto;
    private List<FormacionProfesionalDTO> listaFormacion;

    private List<CargoAcademicoDTO> listaCargosAcademicos;
    private CargoAcademicoDTO cargoSeleccionadoHistorico;
    private DocenteAsignaturaDTO docenteAsigEditable;
    private EvidenciaConceptoDTO evidenciaConceptoSeleccionado;
    private ContratacionPeriodoAcademicoDTO contratoPeriodoSeleccionado;
    private List<DocenteAsignaturaDTO> listaAsignaturasxDocente;
    private List<TipoProgramaEnum> tipoPrograma;
    private List<GeneroEnum> listaGenero;
    private List<RelacionIESEnum> relacionesIes;
    private List<TiempoDedicacionEnum> tiemposDedicacion;
    private List<CategoriaTitularEnum> categorias;
    private List<ContratacionPeriodoAcademicoDTO> contratacionesPeriodoAcademico;
    private Long idPeriodoAcademico;
    private final List<PeriodoAcademicoDTO> periodosAcademicosEliminar;
    private PeriodoAcademicoDTO periodoAcademicoDTO = new PeriodoAcademicoDTO();
    private ParametroDTO pDTO;
    private ContratacionPeriodoAcademicoDTO contratacionPeriodoAcademicoDTO;
    private ParametroDTO peDTO;
    private FaseIesDTO faseiesDTO;
    private Date fechaMin;
    private Date fechaActual;
    private StreamedContent documentoDescarga;
    private IesDTO iesDTO;
    private String[] tabla;
    private UploadedFile file;
    private String perfil;
    private String forma = "";
    private String cursos = "";
    private String cargosAcademicos = "";
    private String contrato = "";
    private String experienciaProfesional = "";
    private Boolean alertaEvaluador = false;
    private Boolean mostrarAsignacion = false;
    private boolean esTitular = false;
    private InformacionIesDTO informacionIesDto;
    private DocenteDTO docenteSeleccionado;
    private DocenteDTO docenteSeleccionadoE;
    private FormacionProfesionalDTO formacionSeleccionada;
    private ContratacionDTO contratoSeleccionado;
    private ContratacionDTO contratoSeleccionadoHistorico;
    private ExperienciaProfesionalDTO experienciaSeleccionada;
    private ExperienciaProfesionalDTO experienciaSeleccionadoHistorico;
    private CursoCapacitacionDTO capacitacionSeleccionada;
    private CursoCapacitacionDTO capacitacionSeleccionadaHistorico;
    private CargoAcademicoDTO cargoAcademicaSeleccionado;
    private ConceptoDTO conceptoSeleccionado;
    private DocenteDTO docenteDto;
    private EvidenciaDTO evidenciaDto;
    private EvidenciaDTO evidenciaSeleccionada;
    private Boolean evidencias = false;
    private Boolean acordion = false;
    private Boolean evidenciaConcepto = false;
    private Long idGrado;
    private Boolean contratos = false;
    private String usuario;
    private String mensajeErrorPeriodo;
    private Long idProceso;
    private Boolean experiencia = false;
    private Boolean capacitacion = false;
    private Boolean formacion = false;
    private Boolean cargos = false;
    private Long idEvidencia;
    private String fichero;
    private String nombreFichero;
    private String url;
    private String extensionDocumento;
    private Boolean descargar = false;
    private List<SelectItem> ANIOS = new ArrayList<>();
    private final SelectItem[] MESES = { new SelectItem(0, "Enero"),
	    new SelectItem(1, "Febrero"), new SelectItem(2, "Marzo"),
	    new SelectItem(3, "Abril"), new SelectItem(4, "Mayo"),
	    new SelectItem(5, "Junio"), new SelectItem(6, "Julio"),
	    new SelectItem(7, "Agosto"), new SelectItem(8, "Septiembre"),
	    new SelectItem(9, "Octubre"), new SelectItem(10, "Noviembre"),
	    new SelectItem(11, "Diciembre") };
    private String identificacionPersona;
    private List<TituloSenescyt> titulosSenescyt = new ArrayList<TituloSenescyt>();

    @Autowired
    private InstitutosServicio institutosServicio;

    @Autowired
    private CatalogoServicio catalogoServicio;

    @Autowired
    private RegistroServicio registroServicio;

    @Autowired
    private EvaluacionServicio evaluacionServicio;

    public EvaluacionDocentesController() {
	this.listaDocentes = new ArrayList<DocenteDTO>();
	iesDTO = new IesDTO();
	this.contratacionesPeriodoEliminar = new ArrayList<ContratacionPeriodoAcademicoDTO>();
	this.periodosAcademicosEliminar = new ArrayList<>();
	faseiesDTO = new FaseIesDTO();
	contratoPeriodoSeleccionado = new ContratacionPeriodoAcademicoDTO();
	contratoSeleccionadoHistorico = new ContratacionDTO();
	docenteSeleccionadoE = new DocenteDTO();
	docenteSeleccionado = new DocenteDTO();
	gradosTitulo = new ArrayList<GradoTituloDTO>();
	this.horasNoAcademicasEliminar = new ArrayList<ContratacionPeriodoAcademicoDTO>();
	this.listaContratacion = new ArrayList<ContratacionDTO>();
	formacionSeleccionadaHistorico = new FormacionProfesionalDTO();
	capacitacionSeleccionadaHistorico = new CursoCapacitacionDTO();
	cargoSeleccionadoHistorico = new CargoAcademicoDTO();
	this.listaContratacionHistorico = new ArrayList<ContratacionHistoricoDTO>();
	this.listaCapacitacionHistorico = new ArrayList<CursoCapacitacionHistoricoDTO>();
	this.listaCargoHistorico = new ArrayList<CargosAcademicosHistoricoDTO>();
	this.listaAsignaturasxDocente = new ArrayList<DocenteAsignaturaDTO>();
	this.listaEvidencia = new ArrayList<ConceptoDTO>();
	experienciaSeleccionadoHistorico = new ExperienciaProfesionalDTO();
	this.listaEvidenciaConcepto = new ArrayList<EvidenciaConceptoDTO>();
	this.listaGenero = new ArrayList<GeneroEnum>();
	this.listaExperienciaHistorico = new ArrayList<ExperienciaProfesionalHistoricoDTO>();
	this.listaExperienciaProfesional = new ArrayList<ExperienciaProfesionalDTO>();
	this.listaCapacitacion = new ArrayList<CursoCapacitacionDTO>();
	this.listaFormacionHistorico = new ArrayList<FormacionProfesionalHistoricoDTO>();
	this.listaFormacion = new ArrayList<FormacionProfesionalDTO>();

	this.relacionesIes = new ArrayList<>();
	this.contratacionesPeriodoAcademico = new ArrayList<ContratacionPeriodoAcademicoDTO>();
	periodosAcademicos = new ArrayList<PeriodoAcademicoDTO>();
	this.tiemposDedicacion = new ArrayList<TiempoDedicacionEnum>();
	this.categorias = new ArrayList<CategoriaTitularEnum>();
	this.tipoPrograma = new ArrayList<TipoProgramaEnum>();
	contratacionPeriodoAcademicoDTO = new ContratacionPeriodoAcademicoDTO();
	docenteAsigEditable = new DocenteAsignaturaDTO();
	experienciaSeleccionada = new ExperienciaProfesionalDTO();
	capacitacionSeleccionada = new CursoCapacitacionDTO();
	formacionSeleccionada = new FormacionProfesionalDTO();
	informacionIesDto = new InformacionIesDTO();
	evidenciaConceptoSeleccionado = new EvidenciaConceptoDTO();
	evidenciaSeleccionada = new EvidenciaDTO();
	evidenciaDto = new EvidenciaDTO();
	conceptoSeleccionado = new ConceptoDTO();
	pDTO = new ParametroDTO();
	peDTO = new ParametroDTO();
	// cargarDocente();

    }

    @PostConstruct
    public void start() {
	DocenteDTO docenteDto = null;
	// evidencias = false;
	try {
	    ListaIesController controller = (ListaIesController) FacesContext
		    .getCurrentInstance().getExternalContext().getSessionMap()
		    .get("listaIesController");
	    faseiesDTO = controller.getFaseIesDTO();
	    usuario = controller.getUsuario();
	    iesDTO = controller.getIes();
	    categorias = Arrays.asList(CategoriaTitularEnum.values());
	    this.perfil = controller.getPerfil().getNombre();
	    idProceso = controller.getFaseIesDTO().getProcesoDTO().getId();
	    pDTO = catalogoServicio.obtenerParametroPorCodigoYReferencia(
		    ParametroEnum.URL_CARGA_EVID.getValor(),
		    TipoReferenciaEnum.INST.getValor());
	    peDTO = catalogoServicio.obtenerParametroPorCodigoYReferencia(
		    ParametroEnum.URL_CARGA_EVID_ELIMINAR.getValor(),
		    TipoReferenciaEnum.INST.getValor());
	    this.informacionIesDto = registroServicio
		    .obtenerInformacionIesPorIes(iesDTO);
	    listaDocentes = registroServicio.obtenerDocentesPorInformacionIes(
		    docenteDto, informacionIesDto.getId());

	    gradosTitulo = catalogoServicio.obtenerNivelTitulo();
	    for (GradoTituloDTO gradoDTO : gradosTitulo) {
		if (gradoDTO.getId().equals(10L)) {
		    gradosTitulo.remove(gradoDTO);
		    break;
		}
	    }
	    if (this.perfil.startsWith("IES_EVALUADOR")) {
		alertaEvaluador = true;
	    }
	    listaGenero.clear();
	    tipoPrograma.clear();
	    this.relacionesIes.clear();
	    this.tiemposDedicacion.clear();
	    cargaAnios();
	    for (TiempoDedicacionEnum tid : TiempoDedicacionEnum.values()) {
		if (tid.compareTo(TiempoDedicacionEnum.NO_DEFINIDO) != 0) {
		    this.tiemposDedicacion.add(tid);
		}
	    }

	    for (GeneroEnum gen : GeneroEnum.values()) {
		this.listaGenero.add(gen);

	    }

	    for (TipoProgramaEnum prog : TipoProgramaEnum.values()) {
		this.tipoPrograma.add(prog);

	    }
	    LOG.info("start this.faseiesDTO...." + this.faseiesDTO);
	    if (this.faseiesDTO != null) {
		LOG.info("start this.faseiesDTO.id...."
		        + this.faseiesDTO.getId());
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public void cancelarPeriodosAcademicos() {
	cargarPeriodos();
    }

    public void nuevoPeriodoAcademico() {
	periodoAcademicoDTO = new PeriodoAcademicoDTO();
	periodoAcademicoDTO.setFaseIesDTO(faseiesDTO);
	anioInicioPeriodoAcademico = Calendar.getInstance().get(Calendar.YEAR);
	mesInicioPeriodoAcademico = Calendar.getInstance().get(Calendar.MONTH);
	anioFinPeriodoAcademico = Calendar.getInstance().get(Calendar.YEAR);
	mesFinPeriodoAcademico = Calendar.getInstance().get(Calendar.MONTH);
	idPeriodoAcademico = null;
	mensajeErrorPeriodo = null;
    }

    public void obtenerHistoricoContrato(ContratacionDTO contrato) {
	listaContratacionHistorico.clear();
	try {
	    listaContratacionHistorico = evaluacionServicio
		    .obtenerContratacionHistorico(contrato.getId());
	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void obtenerHistoricoFormacion(FormacionProfesionalDTO formacion) {
	listaFormacionHistorico.clear();
	try {
	    listaFormacionHistorico = evaluacionServicio
		    .obtenerFormacionProfesionalHistorico(formacion.getId());
	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void obtenerHistoricoCapacitacion(CursoCapacitacionDTO capacitacion) {
	listaCapacitacionHistorico.clear();
	try {
	    listaCapacitacionHistorico = evaluacionServicio
		    .obtenerCursoCapacitacionHistorico(capacitacion.getId());
	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void obtenerHistoricoCargo(CargoAcademicoDTO cargo) {
	listaCargoHistorico.clear();
	try {
	    listaCargoHistorico = evaluacionServicio
		    .obtenerCargoAcademicoHistorico(cargo.getId());
	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void obtenerHistoricoExperiencia(
	    ExperienciaProfesionalDTO experiencia) {
	listaExperienciaHistorico.clear();
	try {
	    listaExperienciaHistorico = evaluacionServicio
		    .obtenerExperienciaProfesionalHistorico(experiencia.getId());
	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void eliminarContratacionPeriodo() {
	if (contratacionPeriodoAcademicoDTO.getId() != null) {
	    for (int i = 0; i < contratoSeleccionado
		    .getContratacionPeriodoAcademicoDTO().size(); i++) {
		if (contratacionPeriodoAcademicoDTO
		        .getPeriodoAcademicoDTO()
		        .getFechaInicioPeriodo()
		        .equals(contratoSeleccionado
		                .getContratacionPeriodoAcademicoDTO().get(i)
		                .getPeriodoAcademicoDTO()
		                .getFechaInicioPeriodo())
		        && contratacionPeriodoAcademicoDTO
		                .getPeriodoAcademicoDTO()
		                .getFechaFinPeriodo()
		                .equals(contratoSeleccionado
		                        .getContratacionPeriodoAcademicoDTO()
		                        .get(i).getPeriodoAcademicoDTO()
		                        .getFechaFinPeriodo())) {
		    contratoSeleccionado.getContratacionPeriodoAcademicoDTO()
			    .get(i).setActivo(false);
		    registroServicio
			    .registrarContratacionesPeriodoAcademico(contratoSeleccionado
			            .getContratacionPeriodoAcademicoDTO());
		    contratoSeleccionado.getContratacionPeriodoAcademicoDTO()
			    .remove(contratacionPeriodoAcademicoDTO);

		}
	    }

	}

	JsfUtil.msgAdvert("Para hacer el cambio permanente debe oprimir el boton guardar");
    }

    private boolean valicarCamposContratacion() {
	if (contratoSeleccionado.getActivo() == null) {
	    contratoSeleccionado.setActivo(false);
	}

	if (contratoSeleccionado.getNumeroContrato() == null
	        || contratoSeleccionado.getNumeroContrato().equals("")) {
	    JsfUtil.msgAdvert("Número de contrato es requerido");
	    return false;
	}
	if (contratoSeleccionado.getRelacionIes() == null
	        || contratoSeleccionado.getNumeroContrato().equals("")) {
	    JsfUtil.msgAdvert("Relación IES es requerido");
	    return false;
	}

	if (contratoSeleccionado.getTiempoDedicacion() == null
	        || contratoSeleccionado.getTiempoDedicacion().compareTo(
	                TiempoDedicacionEnum.NO_DEFINIDO) == 0) {
	    JsfUtil.msgAdvert("Dedicación es requerida");
	    return false;
	}

	if (contratoSeleccionado.getCategoria() == null) {
	    JsfUtil.msgAdvert("Categoría es requerida");
	    return false;
	}

	if (contratoSeleccionado.getFechaInicio() == null) {
	    JsfUtil.msgAdvert("Fecha inicio es requerida");
	    return false;
	} else {
	    if (contratoSeleccionado.getFechaInicio().getTime() > new Date()
		    .getTime()) {
		JsfUtil.msgAdvert("Fecha inicio no puede ser mayor que fecha actual");
		return false;
	    }
	}

	if (contratoSeleccionado.getFechaInicio() != null
	        && contratoSeleccionado.getFechaFin() != null) {
	    if (contratoSeleccionado.getFechaInicio().getTime() > contratoSeleccionado
		    .getFechaFin().getTime()) {
		JsfUtil.msgAdvert("Fecha inicio no puede ser mayor que Fecha fin");
		return false;
	    }
	}
	if (contratoSeleccionado.getTiempoDedicacion().compareTo(
	        TiempoDedicacionEnum.TIEMPO_PARCIAL) != 0) {
	    if (contratoSeleccionado.getRemuneracionContrato() instanceof Number) {
		if (contratoSeleccionado.getRemuneracionContrato() < 0) {
		    JsfUtil.msgAdvert("Remuneración de contrato no debe ser negativa");
		    return false;
		}
	    } else {
		JsfUtil.msgAdvert("Remuneración de contrato no válida.");
		return false;
	    }
	} else {
	    if (contratoSeleccionado.getRemuneracionPorHora() instanceof Number) {
		if (contratoSeleccionado.getRemuneracionPorHora() < 0) {
		    JsfUtil.msgAdvert("Remuneración por Hora no debe ser negativa");
		    return false;
		}
	    }
	}

	for (ContratacionDTO contrato : listaContratacion) {
	    if ((contratoSeleccionado.getTiempoDedicacion() != null && contrato
		    .getTiempoDedicacion().compareTo(
		            contratoSeleccionado.getTiempoDedicacion()) != 0)
		    && contrato.getCategoria().compareTo(
		            contratoSeleccionado.getCategoria()) != 0) {
		if (contrato.getFechaFin() == null) {
		    if (contratoSeleccionado.getFechaFin() == null
			    && !contrato.getId().equals(
			            contratoSeleccionado.getId())) {
			JsfUtil.msgError("Ya existe un contrato sin una fecha de finalización.");
			return false;
		    } else if (contratoSeleccionado.getId() == null) {
			if (contrato.getFechaInicio().compareTo(
			        contratoSeleccionado.getFechaFin()) <= 0) {
			    JsfUtil.msgError("La fechas de contrato ingresados, están siendo incluidas en un contrato existente.");
			    return false;
			}
		    } else {
			if (!contrato.getId().equals(
			        contratoSeleccionado.getId())
			        && contrato.getFechaInicio().compareTo(
			                contratoSeleccionado.getFechaFin()) <= 0) {
			    JsfUtil.msgError("La fechas de contrato ingresados, están siendo incluidas en un contrato existente.");
			    return false;
			}
		    }
		} else if (contratoSeleccionado.getFechaFin() == null) {
		    if (contratoSeleccionado.getId() == null) {
			if (contratoSeleccionado.getFechaInicio().compareTo(
			        contrato.getFechaFin()) <= 0) {
			    JsfUtil.msgError("La fechas de contrato ingresados, incluyen a un contrato existente.");
			    return false;
			}
		    } else {
			if (!contrato.getId().equals(
			        contratoSeleccionado.getId())
			        && contratoSeleccionado.getFechaInicio()
			                .compareTo(contrato.getFechaFin()) <= 0) {
			    JsfUtil.msgError("La fechas de contrato ingresados, incluyen a un contrato existente.");
			    return false;
			}
		    }
		}
	    }
	}
	return true;
    }

    public void guardarContrato() {
	LOG.info("guardar contrato");
	if (valicarCamposContratacion() == false) {
	    return;
	}
	boolean repetido = false;
	boolean nuevo = false;
	for (ContratacionDTO conDTO : listaContratacion) {
	    if (conDTO.getNumeroContrato().equalsIgnoreCase(
		    contratoSeleccionado.getNumeroContrato())) {
		if (contratoSeleccionado.getId() == null) {
		    repetido = true;
		}
		break;
	    }
	}

	if (repetido) {
	    JsfUtil.msgError("El número de contrato está duplicado");
	    return;
	}
	Calendar fechaInicioContrato = Calendar.getInstance();
	fechaInicioContrato.setTime(contratoSeleccionado.getFechaInicio());
	fechaInicioContrato = asignarHoraCero(fechaInicioContrato);

	Calendar fechaFinContrato = Calendar.getInstance();
	if (contratoSeleccionado.getFechaFin() != null) {
	    fechaFinContrato.setTime(contratoSeleccionado.getFechaFin());
	    fechaFinContrato = asignarHoraCero(fechaFinContrato);
	} else {
	    fechaFinContrato = null;
	}

	for (ContratacionPeriodoAcademicoDTO contratacion : contratoSeleccionado
	        .getContratacionPeriodoAcademicoDTO()) {
	    Calendar fechaInicioPeriodo = Calendar.getInstance();
	    fechaInicioPeriodo.setTime(contratacion.getPeriodoAcademicoDTO()
		    .getFechaInicioPeriodo());
	    fechaInicioPeriodo = asignarHoraCero(fechaInicioPeriodo);

	    Calendar fechaFinPeriodo = Calendar.getInstance();
	    fechaFinPeriodo.setTime(contratacion.getPeriodoAcademicoDTO()
		    .getFechaFinPeriodo());
	    fechaFinPeriodo = asignarHoraCero(fechaFinPeriodo);
	    if (contratoSeleccionado.getFechaFin() != null) {
		if ((fechaInicioPeriodo.compareTo(fechaInicioContrato) < 0 && fechaFinPeriodo
		        .compareTo(fechaInicioContrato) <= 0)
		        || (fechaInicioPeriodo.compareTo(fechaFinContrato) >= 0 && fechaFinPeriodo
		                .compareTo(fechaFinContrato) > 0)) {
		    if (!alertaEvaluador) {
			JsfUtil.msgError("El periodo: "
			        + contratacion.getPeriodoAcademicoDTO()
			        + ", se encuentra fuera de las fechas de contrato.");
			return;
		    } else {
			if (contratacion.getAceptadoEvaluador()) {
			    JsfUtil.msgError("Debe invalidar el periodo académico: "
				    + contratacion.getPeriodoAcademicoDTO()
				    + " para guardar el contrato.");
			    return;
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
			return;
		    } else {
			if (contratacion.getAceptadoEvaluador()) {
			    JsfUtil.msgError("Debe invalidar el periodo académico: "
				    + contratacion.getPeriodoAcademicoDTO()
				    + " para guardar el contrato.");
			    return;
			}
		    }
		}
	    }
	}

	docenteSeleccionado.getContratacionDTO().clear();
	// contratoDocente.setCategoria(CategoriaTitularEnum.parse(categoria));
	contratoSeleccionado.setFaseIesDTO(faseiesDTO);
	if (alertaEvaluador) {
	    contratoSeleccionado.setVerificarEvidencia(false);
	}
	contratoSeleccionado.setActivo(true);

	try {
	    if (docenteSeleccionado.getId() == null) {
		if (contratoSeleccionado.getContratacionPeriodoAcademicoDTO()
		        .isEmpty()) {
		    JsfUtil.msgAdvert("Ingrese los datos de las horas de contratación por periodo.");

		    return;
		}

		listaContratacion.add(contratoSeleccionado);

		if (FacesContext.getCurrentInstance().getMessages().hasNext()) {
		    return;
		}
		// personaDocente.setId(docenteSeleccionado.getId());
		contratoSeleccionado.setPersonaDTO(docenteSeleccionado);
		contratoSeleccionado.setFaseIesDTO(faseiesDTO);
		AuditoriaDTO auditoria = new AuditoriaDTO();
		auditoria.setFechaModificacion(new Date());
		auditoria.setUsuarioModificacion(usuario);
		contratoSeleccionado.setAuditoriaDTO(auditoria);
		contratoSeleccionado.setIdInformacionIes(this.informacionIesDto
		        .getId());
		contratoSeleccionado.setFaseIesDTO(faseiesDTO);
		contratoSeleccionado = registroServicio
		        .registrarContratacionDTO(contratoSeleccionado);

		JsfUtil.msgInfo("La información se almacenó con éxito");

	    } else {
		if (!alertaEvaluador) {
		    if (contratoSeleccionado
			    .getContratacionPeriodoAcademicoDTO().isEmpty()) {
			JsfUtil.msgAdvert("Ingrese los datos de las horas de contratación por periodo.");

			return;
		    }
		}

		if (contratoSeleccionado.getId() == null) {
		    nuevo = true;
		}
		AuditoriaDTO auditoria = new AuditoriaDTO();
		auditoria.setFechaModificacion(new Date());
		auditoria.setUsuarioModificacion(usuario);
		contratoSeleccionado.setFaseIesDTO(faseiesDTO);
		contratoSeleccionado.setAuditoriaDTO(auditoria);
		contratoSeleccionado = registroServicio
		        .registrarContratacionDTO(contratoSeleccionado);
		if (nuevo) {
		    listaContratacion.add(contratoSeleccionado);
		    JsfUtil.msgInfo("El registro se almacenó con éxito");
		} else {
		    JsfUtil.msgInfo("El registro ha sido actualizado con éxito");
		}
	    }

	    docenteSeleccionado.getContratacionDTO().addAll(listaContratacion);

	} catch (ServicioException e) {
	    JsfUtil.msgError("Ocurrió un error al guardar el registro.");
	    return;
	} catch (Exception e) {
	    JsfUtil.msgError("Error al guardar, consulte al administrador."
		    + e.getMessage());
	    e.printStackTrace();
	}

	try {
	    for (ContratacionPeriodoAcademicoDTO contratacion : contratoSeleccionado
		    .getContratacionPeriodoAcademicoDTO()) {
		contratacion.getContratacionDTO().setId(
		        contratoSeleccionado.getId());
	    }
	    contratoSeleccionado.getContratacionPeriodoAcademicoDTO().addAll(
		    contratacionesPeriodoEliminar);
	    contratoSeleccionado.getContratacionPeriodoAcademicoDTO().addAll(
		    horasNoAcademicasEliminar);
	    registroServicio
		    .registrarContratacionesPeriodoAcademico(contratoSeleccionado
		            .getContratacionPeriodoAcademicoDTO());

	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ocurrió un error al guardar el registro.");
	}
    }

    // resultado = guardarPestania();

    public void guardarContratacionesPeriodo() {
	RequestContext context = RequestContext.getCurrentInstance();
	context.addCallbackParam("cerrarVentana", false);
	Calendar fechaInicioContrato = Calendar.getInstance();
	fechaInicioContrato.setTime(contratoSeleccionado.getFechaInicio());
	fechaInicioContrato = asignarHoraCero(fechaInicioContrato);

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

	if (contratacionPeriodoAcademicoDTO.getActivo() == null) {
	    contratacionPeriodoAcademicoDTO.setActivo(Boolean.TRUE);

	    AuditoriaDTO auditoria = new AuditoriaDTO();
	    auditoria.setUsuarioModificacion(usuario);
	    auditoria.setFechaModificacion(new Date());
	    contratacionPeriodoAcademicoDTO.setAuditoriaDTO(auditoria);
	    contratacionPeriodoAcademicoDTO.setFaseIesDTO(faseiesDTO);

	    contratacionPeriodoAcademicoDTO
		    .setContratacionDTO(contratoSeleccionado);

	} else {
	    AuditoriaDTO auditoria = contratacionPeriodoAcademicoDTO
		    .getAuditoriaDTO();
	    auditoria.setUsuarioModificacion(usuario);
	    auditoria.setFechaModificacion(new Date());
	    contratacionPeriodoAcademicoDTO.setAuditoriaDTO(auditoria);
	}
	for (PeriodoAcademicoDTO periodo : periodosAcademicos) {
	    if (periodo.getId().equals(idPeriodoAcademico)) {
		contratacionPeriodoAcademicoDTO.setPeriodoAcademicoDTO(periodo);
		break;
	    }
	}
	contratacionesPeriodoAcademico.clear();
	contratacionesPeriodoAcademico.add(contratacionPeriodoAcademicoDTO);
	for (ContratacionPeriodoAcademicoDTO contratacion : contratacionesPeriodoAcademico) {
	    Calendar fechaInicioPeriodo = Calendar.getInstance();
	    fechaInicioPeriodo.setTime(contratacion.getPeriodoAcademicoDTO()
		    .getFechaInicioPeriodo());
	    fechaInicioPeriodo = asignarHoraCero(fechaInicioPeriodo);
	    Calendar fechaFinContrato = Calendar.getInstance();

	    Calendar fechaFinPeriodo = Calendar.getInstance();
	    fechaFinPeriodo.setTime(contratacion.getPeriodoAcademicoDTO()
		    .getFechaFinPeriodo());
	    fechaFinPeriodo = asignarHoraCero(fechaFinPeriodo);
	    if (contratoSeleccionado.getFechaFin() != null) {
		if ((fechaInicioPeriodo.compareTo(fechaInicioContrato) < 0 && fechaFinPeriodo
		        .compareTo(fechaInicioContrato) <= 0)
		        || (fechaInicioPeriodo.compareTo(fechaFinContrato) >= 0 && fechaFinPeriodo
		                .compareTo(fechaFinContrato) > 0)) {
		    if (!alertaEvaluador) {
			JsfUtil.msgError("El periodo: "
			        + contratacion.getPeriodoAcademicoDTO()
			        + ", se encuentra fuera de las fechas de contrato.");
			return;
		    } else {
			if (contratacion.getAceptadoEvaluador()) {
			    JsfUtil.msgError("Debe invalidar el periodo académico: "
				    + contratacion.getPeriodoAcademicoDTO()
				    + " para guardar el contrato.");
			    return;
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
			return;
		    } else {
			if (contratacion.getAceptadoEvaluador()) {
			    JsfUtil.msgError("Debe invalidar el periodo académico: "
				    + contratacion.getPeriodoAcademicoDTO()
				    + " para guardar el contrato.");
			    return;
			}
		    }
		}
	    }
	}

	for (ContratacionPeriodoAcademicoDTO contratacionLista : contratoSeleccionado
	        .getContratacionPeriodoAcademicoDTO()) {
	    if (idPeriodoAcademico.equals(contratacionLista
		    .getPeriodoAcademicoDTO().getId())) {
		List<ContratacionPeriodoAcademicoDTO> aux = new ArrayList<>();
		aux = registroServicio
		        .obtenerContratacionesPeriodoAcademico(contratoSeleccionado
		                .getId());

		contratoSeleccionado.setContratacionPeriodoAcademicoDTO(aux);
		JsfUtil.msgError("Ya existe un registro para el periodo ingresado");
		return;
	    }

	}

	contratoSeleccionado.getContratacionPeriodoAcademicoDTO().add(
	        contratacionPeriodoAcademicoDTO);
	// contratoSeleccionado
	// .setContratacionPeriodoAcademicoDTO(contratoSeleccionado
	// .getContratacionPeriodoAcademicoDTO());
	registroServicio
	        .registrarContratacionesPeriodoAcademico(contratoSeleccionado
	                .getContratacionPeriodoAcademicoDTO());

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

    public void mostrarAsignacionHoras() {

	mostrarAsignacion = true;
	listaAsignaturasxDocente.clear();
	listaAsignaturasxDocente = obtenerListaAsignaturaDocentes();
    }

    public void eliminarPeriodoAcademico() {
	if (periodoAcademicoDTO.getId() != null) {
	    periodoAcademicoDTO.setActivo(false);
	    periodosAcademicosEliminar.add(periodoAcademicoDTO);
	}
	periodosAcademicos.remove(periodoAcademicoDTO);
    }

    public void guardarPeriodosAcademicos() {
	try {
	    periodosAcademicos.addAll(periodosAcademicosEliminar);
	    registroServicio.registrarPeriodosAcademicos(periodosAcademicos);
	    JsfUtil.msgInfo("Los periodos se guardaron correctamente");
	    cargarPeriodos();
	} catch (Exception e) {
	    // LOG.error("Error al registrar los periodos", e);
	    JsfUtil.msgError("Error al registrar los periodos");
	}
    }

    private void cargaAnios() {
	Calendar fechaActual = Calendar.getInstance();
	int z = fechaActual.get(Calendar.YEAR) + 3;
	for (int i = z; i >= fechaActual.get(Calendar.YEAR) - 9; i--) {
	    SelectItem anio = new SelectItem(i, String.valueOf(i));
	    ANIOS.add(anio);

	}
    }

    private void cargarPeriodos() {
	try {
	    this.periodosAcademicos.clear();

	    periodosAcademicos = registroServicio
		    .obtenerPeriodosMatricula(informacionIesDto.getId());

	} catch (Exception e) {

	}
    }

    public void nuevoContratacionPeriodo() {
	contratacionPeriodoAcademicoDTO = new ContratacionPeriodoAcademicoDTO();
	idPeriodoAcademico = null;
	cargarPeriodos();
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

    private Calendar asignarHoraCero(Calendar fecha) {
	fecha.set(Calendar.HOUR_OF_DAY, 0);
	fecha.set(Calendar.MINUTE, 0);
	fecha.set(Calendar.SECOND, 0);
	fecha.set(Calendar.MILLISECOND, 0);
	return fecha;
    }

    public int obtenerUltimoDiaMes(int anio, int mes) {

	Calendar calendario = Calendar.getInstance();
	calendario.set(anio, mes, 1);
	int diaUltimo = calendario.getActualMaximum(Calendar.DAY_OF_MONTH);
	LOG.info("Dia ultimo del mes: " + mes + " es: " + diaUltimo);
	return diaUltimo;
    }

    public void guardarPeriodoAcademico() {

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
		JsfUtil.msgError("El periodo de inicio es mayor o igual al periodo final");
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
		LOG.info("fecha fin lista: " + fechaFinLista.getTime());

		if (fechaInicio.compareTo(fechaInicioLista) >= 0
		        && fechaFin.compareTo(fechaFinLista) <= 0) {
		    JsfUtil.msgError("El periodo ingresado, está incluido en un periodo existente");
		    return;
		} else if (fechaInicio.compareTo(fechaInicioLista) <= 0
		        && fechaFin.compareTo(fechaFinLista) >= 0) {
		    JsfUtil.msgError("El periodo ingresado, incluye a un periodo existente");
		    return;
		}
	    }

	    if (periodoAcademicoDTO.getActivo() == null) {
		periodoAcademicoDTO.setActivo(Boolean.TRUE);
		periodoAcademicoDTO
		        .setFechaInicioPeriodo(fechaInicio.getTime());
		periodoAcademicoDTO.setFechaFinPeriodo(fechaFin.getTime());

		AuditoriaDTO auditoria = new AuditoriaDTO();
		auditoria.setUsuarioModificacion(usuario);
		periodoAcademicoDTO.setAuditoriaDTO(auditoria);

		ListaIesController controller = (ListaIesController) FacesContext
		        .getCurrentInstance().getExternalContext()
		        .getSessionMap().get("listaIesController");
		informacionIesDto = registroServicio
		        .obtenerInformacionIesPorIes(controller.getIes());
		periodoAcademicoDTO.setInformacionIes(informacionIesDto);
		periodoAcademicoDTO.setFaseIesDTO(faseiesDTO);
		periodosAcademicos.add(periodoAcademicoDTO);
		JsfUtil.msgInfo("Periodo agregado correctamente");
	    } else {
		AuditoriaDTO auditoria = periodoAcademicoDTO.getAuditoriaDTO();
		auditoria.setUsuarioModificacion(usuario);
		periodoAcademicoDTO
		        .setFechaInicioPeriodo(fechaInicio.getTime());
		periodoAcademicoDTO.setFechaFinPeriodo(fechaFin.getTime());
		periodoAcademicoDTO.setFaseIesDTO(faseiesDTO);
		JsfUtil.msgInfo("Periodo agregado correctamente");
	    }
	    context.addCallbackParam("cerrarVentana", true);
	} catch (Exception e) {
	    JsfUtil.msgError("Error al guardar el periodo academico");
	    context.addCallbackParam("cerrarVentana", false);

	}
    }

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

    public void cargarDocente() {

	acordion = true;
	evidenciaConcepto = false;
	formacion = false;
	cargos = false;
	experiencia = false;
	capacitacion = false;
	try {
	    listaContratacion = registroServicio.obtenerContratacionPorPersona(
		    docenteSeleccionado.getId(), informacionIesDto.getId());

	    listaExperienciaProfesional = registroServicio
		    .obtenerExperienciaProfesionalPorDocenteEIes(
		            docenteSeleccionado.getId(), iesDTO.getId());

	    listaCapacitacion = registroServicio.obtenerCursosPorDocenteEIes(
		    docenteSeleccionado.getId(), iesDTO.getId());
	    listaFormacion = registroServicio
		    .obtenerFormacionProfesionalPorPersonaEIes(
		            docenteSeleccionado.getId(), iesDTO.getId());

	    listaCargosAcademicos = registroServicio
		    .obtenerCargosAcademicosPorDocenteEIes(
		            docenteSeleccionado.getId(), iesDTO.getId());

	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    public void actualizarFormacion(RowEditEvent event) {
	formacionSeleccionada = (FormacionProfesionalDTO) event.getObject();
	LOG.info(" formacionSeleccionada " + formacionSeleccionada.getId()
	        + " .");
	for (GradoTituloDTO grado : gradosTitulo) {
	    if (formacionSeleccionada.getGrado().getId().equals(grado.getId())) {
		formacionSeleccionada.setGrado(grado);
		break;
	    }
	}
	LOG.info(" formacionSeleccionada 1");
	insertarNivel();
	LOG.info(" formacionSeleccionada 2");
	if (validarCamposFormacion(formacionSeleccionada)) {
	    try {
		LOG.info(" registrando info 1");
		AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
		auditoriaDTO.setFechaModificacion(new Date());
		auditoriaDTO.setUsuarioModificacion(usuario);
		LOG.info(" registrando info 2");
		formacionSeleccionada.setAuditoriaDTO(auditoriaDTO);
		formacionSeleccionada.setFaseIesDTO(this.faseiesDTO);
		formacionSeleccionada.setVerificarEvidencia(false);
		LOG.info(" registrando info 3");
		registroServicio.registrarFormacion(formacionSeleccionada);
		LOG.info(" registrando info 4");
		listaFormacion = registroServicio
		        .obtenerFormacionProfesionalPorPersonaEIes(
		                docenteSeleccionado.getId(), iesDTO.getId());
		LOG.info(" registrando info 5");
		JsfUtil.msgInfo("La información se almacenó con éxito");
	    } catch (ServicioException e) {
		JsfUtil.msgInfo("La información no se pudo almacenar");
		e.printStackTrace();
	    }
	    return;
	} else {
	    try {
		listaFormacion = registroServicio
		        .obtenerFormacionProfesionalPorPersonaEIes(
		                docenteSeleccionado.getId(), iesDTO.getId());

	    } catch (ServicioException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

    }

    public void cancelarFormacion(RowEditEvent event) {
	try {
	    listaFormacion = registroServicio
		    .obtenerFormacionProfesionalPorPersonaEIes(
		            docenteSeleccionado.getId(), iesDTO.getId());
	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void actualizarCapacitacion(RowEditEvent event) {
	capacitacionSeleccionada = (CursoCapacitacionDTO) event.getObject();

	if (validarCamposCapacitacion()) {
	    try {

		AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
		auditoriaDTO.setFechaModificacion(new Date());
		auditoriaDTO.setUsuarioModificacion(usuario);
		LOG.info("actualizarCapacitacion - this.faseiesDTO.."
		        + this.faseiesDTO);
		if (this.faseiesDTO != null) {
		    LOG.info("actualizarCapacitacion - this.faseiesDTO.id..."
			    + this.faseiesDTO.getId());
		}
		capacitacionSeleccionada.setFaseIesDTO(this.faseiesDTO);
		capacitacionSeleccionada.setAuditoriaDTO(auditoriaDTO);
		capacitacionSeleccionada.setVerificarEvidencia(false);
		registroServicio
		        .registrarCapacitacion(capacitacionSeleccionada);
		listaCapacitacion = registroServicio
		        .obtenerCursosPorDocenteEIes(
		                docenteSeleccionado.getId(), iesDTO.getId());

		JsfUtil.msgInfo("La información se almacenó con éxito");
	    } catch (ServicioException e) {

	    }
	    return;
	} else {
	    try {
		listaCapacitacion = registroServicio
		        .obtenerCursosPorDocenteEIes(
		                docenteSeleccionado.getId(), iesDTO.getId());
	    } catch (ServicioException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

    }

    public void cancelarCapacitacion(RowEditEvent event) {
	try {
	    listaCapacitacion = registroServicio.obtenerCursosPorDocenteEIes(
		    docenteSeleccionado.getId(), iesDTO.getId());
	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void actualizarCargo(RowEditEvent event) {
	cargoAcademicaSeleccionado = (CargoAcademicoDTO) event.getObject();

	if (validarCamposCargo()) {
	    try {

		AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
		auditoriaDTO.setFechaModificacion(new Date());
		auditoriaDTO.setUsuarioModificacion(usuario);
		cargoAcademicaSeleccionado.setFaseIesDTO(this.faseiesDTO);
		cargoAcademicaSeleccionado.setAuditoriaDTO(auditoriaDTO);
		cargoAcademicaSeleccionado.setVerificarEvidencia(false);
		registroServicio
		        .registrarCargoAcademico(cargoAcademicaSeleccionado);
		listaCargosAcademicos = registroServicio
		        .obtenerCargosAcademicosPorDocenteEIes(
		                docenteSeleccionado.getId(), iesDTO.getId());

		JsfUtil.msgInfo("La información se almacenó con éxito");
	    } catch (ServicioException e) {

	    }
	    return;
	} else {
	    try {
		listaCargosAcademicos = registroServicio
		        .obtenerCargosAcademicosPorDocenteEIes(
		                docenteSeleccionado.getId(), iesDTO.getId());
	    } catch (ServicioException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

    }

    public void cancelarCargo(RowEditEvent event) {
	try {
	    listaCargosAcademicos = registroServicio
		    .obtenerCargosAcademicosPorDocenteEIes(
		            docenteSeleccionado.getId(), iesDTO.getId());
	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void actualizarDocente(RowEditEvent event) {
	docenteSeleccionado = (DocenteDTO) event.getObject();
	AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
	docenteSeleccionado.setIdInformacionIes(informacionIesDto.getId());
	docenteSeleccionado.setIesDTO(iesDTO);
	docenteSeleccionado.setFaseIesDTO(faseiesDTO);
	auditoriaDTO.setFechaModificacion(new Date());
	auditoriaDTO.setUsuarioModificacion(usuario);
	docenteSeleccionado.setAuditoria(auditoriaDTO);
	try {
	    registroServicio.registrarDocente(docenteSeleccionado);
	    listaDocentes = registroServicio.obtenerDocentesPorInformacionIes(
		    null, informacionIesDto.getId());
	    JsfUtil.msgInfo("La información se almacenó con éxito");
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    public void actualizarExperiencia(RowEditEvent event) {
	experienciaSeleccionada = (ExperienciaProfesionalDTO) event.getObject();

	if (validarCamposExperiencia()) {
	    try {

		AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
		auditoriaDTO.setFechaModificacion(new Date());
		auditoriaDTO.setUsuarioModificacion(usuario);
		experienciaSeleccionada.setAuditoriaDTO(auditoriaDTO);
		experienciaSeleccionada.setFaseIesDTO(this.faseiesDTO);
		experienciaSeleccionada.setVerificarEvidencia(false);
		registroServicio
		        .registrarExperienciaProfDocente(experienciaSeleccionada);
		listaExperienciaProfesional = registroServicio
		        .obtenerExperienciaProfesionalPorDocenteEIes(
		                docenteSeleccionado.getId(), iesDTO.getId());

		JsfUtil.msgInfo("La información se almacenó con éxito");
	    } catch (ServicioException e) {

	    }
	    return;
	} else {
	    try {
		listaExperienciaProfesional = registroServicio
		        .obtenerExperienciaProfesionalPorDocenteEIes(
		                docenteSeleccionado.getId(), iesDTO.getId());
	    } catch (ServicioException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

    }

    public void actualizarContratacionPeriodo(RowEditEvent event) {
	JsfUtil.msgAdvert("Para hacer el cambio permanente debe oprimir el boton guardar");

    }

    public void actualizarDocenteAsig(RowEditEvent event) {
	docenteAsigEditable = (DocenteAsignaturaDTO) event.getObject();
	if (validarContrato()) {
	    AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
	    auditoriaDTO.setFechaModificacion(new Date());
	    auditoriaDTO.setUsuarioModificacion(usuario);
	    docenteAsigEditable.setAuditoriaDTO(auditoriaDTO);
	    docenteAsigEditable.setFaseIesDTO(this.faseiesDTO);
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

    public void cancelarExperiencia(RowEditEvent event) {
	try {
	    listaExperienciaProfesional = registroServicio
		    .obtenerExperienciaProfesionalPorDocenteEIes(
		            docenteSeleccionado.getId(), iesDTO.getId());
	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    private boolean validarCamposFormacion(
	    FormacionProfesionalDTO formacionDocente) {
	LOG.info(" vi formacionSeleccionada " + formacionSeleccionada.getId()
	        + " .");
	if (formacionSeleccionada.getAceptadoEvaluador().booleanValue()) {
	    if (formacionSeleccionada.getCursando()) {
		if (formacionSeleccionada.getFechaInicioEstudios() == null) {
		    JsfUtil.msgAdvert("Fecha Inicio de Estudio es requerida");
		    return false;
		}
	    } else {
		if (formacionSeleccionada.getFechaGraduacion() == null) {
		    JsfUtil.msgAdvert("Fecha de Graduación es requerida");
		    return false;
		}
	    }

	    if (formacionSeleccionada.getFechaInicioEstudios() != null
		    && formacionSeleccionada.getFechaGraduacion() != null) {
		if (formacionSeleccionada.getFechaInicioEstudios().getTime() > formacionSeleccionada
		        .getFechaGraduacion().getTime()) {
		    JsfUtil.msgAdvert("Fecha Inicio de Estudio no puede ser mayor que Fecha de Graduación");
		    return false;
		}
	    }
	}
	if (formacionSeleccionada.getObservacionEvaluador() == null
	        || formacionSeleccionada.getObservacionEvaluador().equals("")) {
	    JsfUtil.msgAdvert("La observación es requerida");
	    return false;
	}

	LOG.info(" v formacionSeleccionada " + formacionSeleccionada.getId()
	        + " .");
	List<FormacionProfesionalDTO> listaFormacionTemporal = new ArrayList<FormacionProfesionalDTO>();
	FormacionProfesionalDTO antiguaFpDTO = new FormacionProfesionalDTO();
	try {
	    listaFormacionTemporal = registroServicio
		    .obtenerFormacionProfesionalPorPersonaEIes(
		            docenteSeleccionado.getId(), iesDTO.getId());
	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    JsfUtil.msgAdvert("Falla en el registro, comuniquese con el administrador");
	    return false;
	}
	LOG.info(" v listaFormacionTemporal.size() "
	        + listaFormacionTemporal.size() + " .");

	for (FormacionProfesionalDTO fpDTO : listaFormacionTemporal) {
	    LOG.info("v fpDTO " + fpDTO.getId() + " .");
	    if (fpDTO.getId().equals(formacionSeleccionada.getId())) {
		antiguaFpDTO = fpDTO;
	    }
	}

	LOG.info("v antiguaFpDTO " + antiguaFpDTO.getId() + " .");
	LOG.info("v antiguaFpDTO.getGrado().getId()"
	        + antiguaFpDTO.getGrado().getId() + " .");
	LOG.info("v formacionSeleccionada.getGrado().getId()"
	        + formacionSeleccionada.getGrado().getId() + " .");
	// LOG.info("v antiguaFpDTO.getGrado()" + antiguaFpDTO.getGrado() +
	// " .");
	// LOG.info("v antiguaFpDTO.getGrado() id:"
	// + antiguaFpDTO.getGrado().getId() + " .");

	if (!antiguaFpDTO.getGrado().getId()
	        .equals(formacionSeleccionada.getGrado().getId())) {
	    // LOG.info("v formacionSeleccionada v1");
	    if (antiguaFpDTO.getObservacionEvaluador() != null
		    && (antiguaFpDTO.getObservacionEvaluador()
		            .equalsIgnoreCase(formacionSeleccionada
		                    .getObservacionEvaluador()))) {
		// LOG.info("v formacionSeleccionada v2");
		JsfUtil.msgAdvert("Modificar la observación si desea registrar un cambio en el nivel del título");
		return false;
	    }
	    LOG.info("v formacionSeleccionada v3");
	}
	LOG.info("return true");
	return true;
    }

    private void insertarNivel() {
	if (formacionSeleccionada.getGrado().getNombre()
	        .equals("DIPLOMA SUPERIOR")
	        || formacionSeleccionada.getGrado().getNombre()
	                .equals("DOCTOR EN FILOSOFIA O JURISPRUDENCIA")
	        || formacionSeleccionada.getGrado().getNombre()
	                .equals("DOCTOR (Ph.D)")
	        || formacionSeleccionada.getGrado().getNombre()
	                .equals("MAGISTER O EQUIVALENTE")) {
	    formacionSeleccionada.setNivel("CUARTO NIVEL");

	}

	else if (formacionSeleccionada.getGrado().getNombre()
	        .equals("TECNICO SUPERIOR")) {
	    formacionSeleccionada.setNivel("TECNICO");
	}

	else if (formacionSeleccionada.getGrado().getNombre()
	        .equals("TECNOLOGO SUPERIOR")) {
	    formacionSeleccionada.setNivel("TECNOLOGO");
	}

	else if (formacionSeleccionada.getGrado().getNombre()
	        .equals("TERCER NIVEL")) {
	    formacionSeleccionada.setNivel("TERCER NIVEL");
	}

    }

    private boolean validarCamposCapacitacion() {
	if (capacitacionSeleccionada.getFechaInicio() == null) {
	    JsfUtil.msgAdvert("Fecha Inicio es requerida");
	    return false;
	}

	if (capacitacionSeleccionada.getFechaFin() == null) {
	    JsfUtil.msgAdvert("Fecha Fin es requerida");
	    return false;
	}

	if (capacitacionSeleccionada.getObservacionEvaluador() == null
	        || capacitacionSeleccionada.getObservacionEvaluador()
	                .equals("")) {
	    JsfUtil.msgAdvert("La observación es requerida");
	    return false;
	}

	if (capacitacionSeleccionada.getFechaInicio() != null
	        && capacitacionSeleccionada.getFechaFin() != null) {
	    if (capacitacionSeleccionada.getFechaInicio().getTime() > capacitacionSeleccionada
		    .getFechaFin().getTime()) {
		JsfUtil.msgAdvert("Fecha Inicio no puede ser mayor que Fecha Fin");
		return false;
	    }
	}
	return true;
    }

    private boolean validarCamposCargo() {

	if (cargoAcademicaSeleccionado.getFechaInicio() == null) {
	    JsfUtil.msgAdvert("Fecha Inicio es requerida");
	    return false;
	}

	if (cargoAcademicaSeleccionado.getFechaFin() == null) {
	    JsfUtil.msgAdvert("Fecha Fin es requerida");
	    return false;
	}

	if (cargoAcademicaSeleccionado.getObservacionEvaluador() == null
	        || cargoAcademicaSeleccionado.getObservacionEvaluador().equals(
	                "")) {
	    JsfUtil.msgAdvert("La observación es requerida");
	    return false;
	}

	if (cargoAcademicaSeleccionado.getFechaInicio() != null
	        && cargoAcademicaSeleccionado.getFechaFin() != null) {
	    if (cargoAcademicaSeleccionado.getFechaInicio().getTime() > cargoAcademicaSeleccionado
		    .getFechaFin().getTime()) {
		JsfUtil.msgAdvert("Fecha Inicio no puede ser mayor que Fecha Fin");
		return false;
	    }
	}
	return true;
    }

    private boolean validarCamposExperiencia() {

	if (experienciaSeleccionada.getFechaInicio() == null) {
	    JsfUtil.msgAdvert("Fecha Inicio es requerida");
	    return false;
	}

	if (experienciaSeleccionada.getFechaFin() == null) {
	    JsfUtil.msgAdvert("Fecha Fin es requerida");
	    return false;
	}

	if (experienciaSeleccionada.getObservacionEvaluador() == null
	        || experienciaSeleccionada.getObservacionEvaluador().equals("")) {
	    JsfUtil.msgAdvert("La observación es requerida");
	    return false;
	}

	if (experienciaSeleccionada.getFechaInicio() != null
	        && experienciaSeleccionada.getFechaFin() != null) {
	    if (experienciaSeleccionada.getFechaInicio().getTime() > experienciaSeleccionada
		    .getFechaFin().getTime()) {
		JsfUtil.msgAdvert("Fecha Inicio no puede ser mayor que Fecha Fin");
		return false;
	    }
	}
	return true;
    }

    private boolean validarContrato() {

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

    public void cargarEvidencias() {
	LOG.info("conceptoSeleccionado: " + conceptoSeleccionado.getOrigen());
	LOG.info("docenteSeleccionado.getId(): " + docenteSeleccionado.getId());
	LOG.info("informacionIesDto.getId(): " + informacionIesDto.getId());
	LOG.info("IES: " + iesDTO.getId());
	System.out
	        .println("ORIGEN: " + OrigenInformacionEnum.CARGOS.getValor());
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
			    .obtenerContratacionPorPersona(
			            docenteSeleccionado.getId(),
			            informacionIesDto.getId());
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
			            docenteSeleccionado.getId(), iesDTO.getId());
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
			    .obtenerCursosPorDocenteEIes(
			            docenteSeleccionado.getId(), iesDTO.getId());
		    LOG.info("listaCapacitacion: " + listaCapacitacion.size());

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
			            docenteSeleccionado.getId(), iesDTO.getId());
		    LOG.info("listaFormacion: " + listaFormacion.size());

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
			            docenteSeleccionado.getId(), iesDTO.getId());

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

    public void cargarEConcepto() {
	try {
	    evidenciaConcepto = true;
	    if (experienciaProfesional.equals("experienciaProfesional")) {
		formacion = false;
		cargos = false;
		experiencia = true;
		capacitacion = false;
		cargosAcademicos = "";
		experienciaProfesional = "";
		forma = "";
		cursos = "";
		contrato = "";
		conceptoSeleccionado = catalogoServicio
		        .obtenerConceptoPorOrigenYGrupo(
		                OrigenInformacionEnum.EXPERIENCIA.getValor(),
		                GrupoConceptoEnum.DOCENTE.getValor());

		idEvidencia = experienciaSeleccionada.getId();
		nombreFichero = iesDTO.getCodigo() + "_"
		        + experienciaSeleccionada.getId();

	    }

	    else if (cursos.equals("cursosCapacitacion")) {
		conceptoSeleccionado = catalogoServicio
		        .obtenerConceptoPorOrigenYGrupo(
		                OrigenInformacionEnum.CAPACITACION.getValor(),
		                GrupoConceptoEnum.DOCENTE.getValor());
		formacion = false;
		cargos = false;
		experiencia = false;
		capacitacion = true;
		cargosAcademicos = "";
		contrato = "";
		experienciaProfesional = "";
		forma = "";
		cursos = "";
		idEvidencia = capacitacionSeleccionada.getId();
		nombreFichero = iesDTO.getCodigo() + "_"
		        + capacitacionSeleccionada.getId();

	    }

	    else if (forma.equals("formacion")) {
		conceptoSeleccionado = catalogoServicio
		        .obtenerConceptoPorOrigenYGrupo(
		                OrigenInformacionEnum.FORMACION.getValor(),
		                GrupoConceptoEnum.DOCENTE.getValor());
		formacion = true;
		cargos = false;
		experiencia = false;
		capacitacion = false;
		cargosAcademicos = "";
		experienciaProfesional = "";
		forma = "";
		cursos = "";
		contrato = "";
		idEvidencia = formacionSeleccionada.getId();
		nombreFichero = iesDTO.getCodigo() + "_"
		        + formacionSeleccionada.getId();

	    }

	    else if (cargosAcademicos.equals("cargoAcademico")) {
		formacion = false;
		cargos = true;
		experiencia = false;
		capacitacion = false;
		cargosAcademicos = "";
		experienciaProfesional = "";
		forma = "";
		cursos = "";
		contrato = "";
		conceptoSeleccionado = catalogoServicio
		        .obtenerConceptoPorOrigenYGrupo(
		                OrigenInformacionEnum.CARGOS.getValor(),
		                GrupoConceptoEnum.DOCENTE.getValor());
		formacionSeleccionada = null;
		experienciaSeleccionada = null;
		capacitacionSeleccionada = null;
		idEvidencia = cargoAcademicaSeleccionado.getId();
		nombreFichero = iesDTO.getCodigo() + "_"
		        + cargoAcademicaSeleccionado.getId();

	    }

	    else if (contrato.equals("contrato")) {
		formacion = false;
		cargos = false;
		experiencia = false;
		capacitacion = false;
		cargosAcademicos = "";
		experienciaProfesional = "";
		forma = "";
		contrato = "";
		cursos = "";
		conceptoSeleccionado = catalogoServicio
		        .obtenerConceptoPorOrigenYGrupo(
		                OrigenInformacionEnum.CONTRATOS.getValor(),
		                GrupoConceptoEnum.DOCENTE.getValor());
		formacionSeleccionada = null;
		experienciaSeleccionada = null;
		capacitacionSeleccionada = null;
		idEvidencia = contratoSeleccionado.getId();
		nombreFichero = iesDTO.getCodigo() + "_"
		        + contratoSeleccionado.getId();

	    }

	    listaEvidenciaConcepto = institutosServicio
		    .obtenerEvidenciasDeIesPorIdConceptoEIdTabla(
		            iesDTO.getId(), conceptoSeleccionado.getId(),
		            idEvidencia, conceptoSeleccionado.getOrigen());

	} catch (ServicioException e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public void verificarEvidencia() {
	Boolean fase = false;
	for (EvidenciaConceptoDTO evidenciaConceptoDTO : listaEvidenciaConcepto) {
	    for (EvidenciaDTO evidencia : evidenciaConceptoDTO
		    .getEvidenciasDTO()) {

		if (evidencia.getFaseIesDTO().getFaseProcesoDTO().getId()
		        .equals(faseiesDTO.getFaseProcesoDTO().getId())) {
		    if (conceptoSeleccionado.getOrigen().equals(
			    OrigenInformacionEnum.CONTRATOS.getValor())) {
			contratoSeleccionado.setVerificarEvidencia(true);
			try {
			    registroServicio
				    .registrarContratacionDTO(contratoSeleccionado);
			} catch (ServicioException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			}

		    } else if (conceptoSeleccionado.getOrigen().equals(
			    OrigenInformacionEnum.EXPERIENCIA.getValor())) {
			experienciaSeleccionada.setVerificarEvidencia(true);
			try {
			    registroServicio
				    .registrarExperienciaProfDocente(experienciaSeleccionada);
			} catch (ServicioException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			}

		    }

		    else if (conceptoSeleccionado.getOrigen().equals(
			    OrigenInformacionEnum.CAPACITACION.getValor())) {
			capacitacionSeleccionada.setVerificarEvidencia(true);
			try {
			    registroServicio
				    .registrarCapacitacion(capacitacionSeleccionada);
			} catch (ServicioException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			}

		    }

		    else if (conceptoSeleccionado.getOrigen().equals(
			    OrigenInformacionEnum.FORMACION.getValor())) {
			formacionSeleccionada.setVerificarEvidencia(true);
			try {
			    registroServicio
				    .registrarFormacion(formacionSeleccionada);
			} catch (ServicioException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			}

		    }

		    else if (conceptoSeleccionado.getOrigen().equals(
			    OrigenInformacionEnum.CARGOS.getValor())) {
			cargoAcademicaSeleccionado.setVerificarEvidencia(true);
			try {
			    registroServicio
				    .registrarCargoAcademico(cargoAcademicaSeleccionado);
			} catch (ServicioException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			}

		    }
		    fase = true;
		    break;
		} else {
		    if (conceptoSeleccionado.getOrigen().equals(
			    OrigenInformacionEnum.CONTRATOS.getValor())) {
			contratoSeleccionado.setVerificarEvidencia(false);
			try {
			    registroServicio
				    .registrarContratacionDTO(contratoSeleccionado);
			} catch (ServicioException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			}

		    } else if (conceptoSeleccionado.getOrigen().equals(
			    OrigenInformacionEnum.EXPERIENCIA.getValor())) {
			experienciaSeleccionada.setVerificarEvidencia(false);
			try {
			    registroServicio
				    .registrarExperienciaProfDocente(experienciaSeleccionada);
			} catch (ServicioException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			}

		    }

		    else if (conceptoSeleccionado.getOrigen().equals(
			    OrigenInformacionEnum.CAPACITACION.getValor())) {
			capacitacionSeleccionada.setVerificarEvidencia(false);
			try {
			    registroServicio
				    .registrarCapacitacion(capacitacionSeleccionada);
			} catch (ServicioException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			}

		    }

		    else if (conceptoSeleccionado.getOrigen().equals(
			    OrigenInformacionEnum.FORMACION.getValor())) {
			formacionSeleccionada.setVerificarEvidencia(false);
			try {
			    registroServicio
				    .registrarFormacion(formacionSeleccionada);
			} catch (ServicioException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			}

		    }

		    else if (conceptoSeleccionado.getOrigen().equals(
			    OrigenInformacionEnum.CARGOS.getValor())) {
			cargoAcademicaSeleccionado.setVerificarEvidencia(false);
			try {
			    registroServicio
				    .registrarCargoAcademico(cargoAcademicaSeleccionado);
			} catch (ServicioException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			}

		    }

		}
		if (fase) {
		    break;
		}
	    }
	    if (fase) {
		break;
	    }
	}
    }

    public void modificarContratacionPeriodo() {
	idPeriodoAcademico = contratacionPeriodoAcademicoDTO
	        .getPeriodoAcademicoDTO().getId();
    }

    public void eliminarEvidencia() {
	AuditoriaDTO auditoria = new AuditoriaDTO();

	LOG.info(evidenciaSeleccionada.getNombreArchivo());
	String origen = evidenciaSeleccionada.getUrl().toString().trim()
	        + evidenciaSeleccionada.getNombreArchivo().toString().trim();
	LOG.info("ORIGEN: " + origen);
	String destino = peDTO.getValor().toString().trim()
	        + faseiesDTO.getId() + "/" + iesDTO.getCodigo() + "/"
	        + idEvidencia + "/"
	        + evidenciaSeleccionada.getNombreArchivo().toString().trim();
	String urlDestino = peDTO.getValor().toString().trim()
	        + faseiesDTO.getId() + "/" + iesDTO.getCodigo() + "/"
	        + idEvidencia + "/";
	LOG.info("DESTINO: " + destino);

	if (ArchivoUtil.crearDirectorio(urlDestino)) {
	    ArchivoUtil.moverArchivo(origen, destino);
	    evidenciaSeleccionada.setActivo(false);
	    evidenciaSeleccionada.setUrl(urlDestino);
	    auditoria.setFechaModificacion(new Date());
	    auditoria.setUsuarioModificacion(usuario);
	    evidenciaSeleccionada.setAuditoriaDTO(auditoria);

	    try {
		institutosServicio.crearActualizar(evidenciaSeleccionada);
	    } catch (ServicioException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    cargarEConcepto();
	    verificarEvidencia();
	    JsfUtil.msgInfo("Evidencia eliminada correctamente");

	}

    }

    public void uploadfile() {

	if (file != null) {
	    AuditoriaDTO auditoria = new AuditoriaDTO();

	    evidenciaDto.setEvidenciaConceptoDTO(evidenciaConceptoSeleccionado);
	    evidenciaDto.setIdTabla(idEvidencia);
	    evidenciaDto.setFaseIesDTO(faseiesDTO);

	    evidenciaDto.setTabla(conceptoSeleccionado.getOrigen());

	    Date date = new Date();
	    DateFormat hourdateFormat = new SimpleDateFormat("ddMMYYHHmmss");
	    String[] nameFile = file.getFileName().split("[.]");
	    String extension = nameFile[nameFile.length - 1];
	    long max = 20 * 1024 * 1024;
	    long fileuploadsize = file.getSize();
	    if (fileuploadsize > max) {
		JsfUtil.msgError("El tamaño del archivo excede 10MB");
		return;
	    } else {

		byte[] contenido = this.file.getContents();

		LOG.info("NAME FILE: " + nameFile[0]);

		String nombreGenerado = nombreFichero + "_COD_"
		        + evidenciaConceptoSeleccionado.getId() + "_"
		        + hourdateFormat.format(date).toString() + "."
		        + extension.toString();

		LOG.info("Nombre Generado: " + nombreGenerado);

		String urlNew = pDTO.getValor() + faseiesDTO.getId() + "/"
		        + iesDTO.getCodigo() + "/" + idEvidencia + "/";

		LOG.info("URL A GUARDAR: " + urlNew);
		try {
		    boolean creoArchivo = ArchivoUtil.escribirArchivo(
			    nombreGenerado, contenido, urlNew);
		    if (creoArchivo) {
			evidenciaDto.setNombreOriginal(nameFile[0] + "."
			        + extension);
			evidenciaDto.setNombreArchivo(nombreGenerado);
			evidenciaDto.setUrl(urlNew);
			evidenciaDto.setActivo(true);

			auditoria.setFechaModificacion(new Date());

			auditoria.setUsuarioModificacion(usuario);
			evidenciaDto.setAuditoriaDTO(auditoria);

			institutosServicio.crearActualizar(evidenciaDto);
			if (conceptoSeleccionado.getOrigen().equals(
			        OrigenInformacionEnum.CONTRATOS.getValor())) {
			    contratoSeleccionado.setVerificarEvidencia(true);
			    registroServicio
				    .registrarContratacionDTO(contratoSeleccionado);

			} else if (conceptoSeleccionado.getOrigen().equals(
			        OrigenInformacionEnum.EXPERIENCIA.getValor())) {
			    experienciaSeleccionada.setVerificarEvidencia(true);
			    registroServicio
				    .registrarExperienciaProfDocente(experienciaSeleccionada);

			}

			else if (conceptoSeleccionado.getOrigen().equals(
			        OrigenInformacionEnum.CAPACITACION.getValor())) {
			    capacitacionSeleccionada
				    .setVerificarEvidencia(true);
			    registroServicio
				    .registrarCapacitacion(capacitacionSeleccionada);

			}

			else if (conceptoSeleccionado.getOrigen().equals(
			        OrigenInformacionEnum.FORMACION.getValor())) {
			    formacionSeleccionada.setVerificarEvidencia(true);
			    registroServicio
				    .registrarFormacion(formacionSeleccionada);

			}

			else if (conceptoSeleccionado.getOrigen().equals(
			        OrigenInformacionEnum.CARGOS.getValor())) {
			    cargoAcademicaSeleccionado
				    .setVerificarEvidencia(true);
			    registroServicio
				    .registrarCargoAcademico(cargoAcademicaSeleccionado);

			}

			evidenciaDto = new EvidenciaDTO();
			cargarEConcepto();
			JsfUtil.msgInfo("Evidencia subida correctamente");
		    }
		} catch (Exception e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }
	} else {
	    JsfUtil.msgError("Debe escoger un documento");
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

    public List<DocenteDTO> getListaDocentes() {
	return listaDocentes;
    }

    public void setListaDocentes(List<DocenteDTO> listaDocentes) {
	this.listaDocentes = listaDocentes;
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

    public DocenteDTO getDocenteDto() {
	return docenteDto;
    }

    public void setDocenteDto(DocenteDTO docenteDto) {
	this.docenteDto = docenteDto;
    }

    public Boolean getEvidencias() {
	return evidencias;
    }

    public void setEvidencias(Boolean evidencias) {
	this.evidencias = evidencias;
    }

    public Long getIdEvidencia() {
	return idEvidencia;
    }

    public void setIdEvidencia(Long idEvidencia) {
	this.idEvidencia = idEvidencia;
    }

    public List<ConceptoDTO> getListaEvidencia() {
	return listaEvidencia;
    }

    public void setListaEvidencia(List<ConceptoDTO> listaEvidencia) {
	this.listaEvidencia = listaEvidencia;
    }

    public Boolean getContratos() {
	return contratos;
    }

    public void setContratos(Boolean contratos) {
	this.contratos = contratos;
    }

    public List<ContratacionDTO> getListaContratacion() {
	return listaContratacion;
    }

    public void setListaContratacion(List<ContratacionDTO> listaContratacion) {
	this.listaContratacion = listaContratacion;
    }

    public ContratacionDTO getContratoSeleccionado() {
	return contratoSeleccionado;
    }

    public void setContratoSeleccionado(ContratacionDTO contratoSeleccionado) {
	List<ContratacionPeriodoAcademicoDTO> aux = new ArrayList<>();
	aux = registroServicio
	        .obtenerContratacionesPeriodoAcademico(contratoSeleccionado
	                .getId());

	contratoSeleccionado.setContratacionPeriodoAcademicoDTO(aux);
	mostrarAsignacion = false;
	contrato = "contrato";
	this.contratoSeleccionado = contratoSeleccionado;
	cargarEConcepto();
    }

    public List<EvidenciaConceptoDTO> getListaEvidenciaConcepto() {
	return listaEvidenciaConcepto;
    }

    public void setListaEvidenciaConcepto(
	    List<EvidenciaConceptoDTO> listaEvidenciaConcepto) {
	this.listaEvidenciaConcepto = listaEvidenciaConcepto;
    }

    public EvidenciaConceptoDTO getEvidenciaConceptoSeleccionado() {
	return evidenciaConceptoSeleccionado;
    }

    public void setEvidenciaConceptoSeleccionado(
	    EvidenciaConceptoDTO evidenciaConceptoSeleccionado) {
	this.evidenciaConceptoSeleccionado = evidenciaConceptoSeleccionado;
    }

    public UploadedFile getFile() {
	return file;
    }

    public void setFile(UploadedFile file) {
	this.file = file;
    }

    public String[] getTabla() {
	return tabla;
    }

    public void setTabla(String[] tabla) {
	this.tabla = tabla;
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

    public EvidenciaDTO getEvidenciaSeleccionada() {
	return evidenciaSeleccionada;
    }

    public void setEvidenciaSeleccionada(EvidenciaDTO evidenciaSeleccionada) {
	this.evidenciaSeleccionada = evidenciaSeleccionada;
    }

    public ConceptoDTO getConceptoSeleccionado() {
	return conceptoSeleccionado;
    }

    public void setConceptoSeleccionado(ConceptoDTO conceptoSeleccionado) {
	this.conceptoSeleccionado = conceptoSeleccionado;
    }

    public List<ExperienciaProfesionalDTO> getListaExperienciaProfesional() {
	return listaExperienciaProfesional;
    }

    public void setListaExperienciaProfesional(
	    List<ExperienciaProfesionalDTO> listaExperienciaProfesional) {
	this.listaExperienciaProfesional = listaExperienciaProfesional;
    }

    public Boolean getExperiencia() {
	return experiencia;
    }

    public void setExperiencia(Boolean experiencia) {
	this.experiencia = experiencia;
    }

    public ExperienciaProfesionalDTO getExperienciaSeleccionada() {
	return experienciaSeleccionada;
    }

    public void setExperienciaSeleccionada(
	    ExperienciaProfesionalDTO experienciaSeleccionada) {
	this.experienciaSeleccionada = experienciaSeleccionada;
    }

    public Boolean getCapacitacion() {
	return capacitacion;
    }

    public void setCapacitacion(Boolean capacitacion) {
	this.capacitacion = capacitacion;
    }

    public List<CursoCapacitacionDTO> getListaCapacitacion() {
	return listaCapacitacion;
    }

    public void setListaCapacitacion(
	    List<CursoCapacitacionDTO> listaCapacitacion) {
	this.listaCapacitacion = listaCapacitacion;
    }

    public CursoCapacitacionDTO getCapacitacionSeleccionada() {
	return capacitacionSeleccionada;
    }

    public void setCapacitacionSeleccionada(
	    CursoCapacitacionDTO capacitacionSeleccionada) {
	this.capacitacionSeleccionada = capacitacionSeleccionada;
    }

    public Boolean getFormacion() {
	return formacion;
    }

    public void setFormacion(Boolean formacion) {
	this.formacion = formacion;
    }

    public List<FormacionProfesionalDTO> getListaFormacion() {
	return listaFormacion;
    }

    public void setListaFormacion(List<FormacionProfesionalDTO> listaFormacion) {
	this.listaFormacion = listaFormacion;
    }

    public FormacionProfesionalDTO getFormacionSeleccionada() {
	return formacionSeleccionada;
    }

    public void setFormacionSeleccionada(
	    FormacionProfesionalDTO formacionSeleccionada) {
	this.formacionSeleccionada = formacionSeleccionada;
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

    public Boolean getEvidenciaConcepto() {
	return evidenciaConcepto;
    }

    public void setEvidenciaConcepto(Boolean evidenciaConcepto) {
	this.evidenciaConcepto = evidenciaConcepto;
    }

    public String getNombreFichero() {
	return nombreFichero;
    }

    public void setNombreFichero(String nombreFichero) {
	this.nombreFichero = nombreFichero;
    }

    public String getExtensionDocumento() {
	return extensionDocumento;
    }

    public void setExtensionDocumento(String extensionDocumento) {
	this.extensionDocumento = extensionDocumento;
    }

    public FaseIesDTO getFaseiesDTO() {
	return faseiesDTO;
    }

    public void setFaseiesDTO(FaseIesDTO faseiesDTO) {
	this.faseiesDTO = faseiesDTO;
    }

    public String getUsuario() {
	return usuario;
    }

    public void setUsuario(String usuario) {
	this.usuario = usuario;
    }

    public Boolean getCargos() {
	return cargos;
    }

    public void setCargos(Boolean cargos) {
	this.cargos = cargos;
    }

    public List<CargoAcademicoDTO> getListaCargosAcademicos() {
	return listaCargosAcademicos;
    }

    public void setListaCargosAcademicos(
	    List<CargoAcademicoDTO> listaCargosAcademicos) {
	this.listaCargosAcademicos = listaCargosAcademicos;
    }

    public CargoAcademicoDTO getCargoAcademicaSeleccionado() {
	return cargoAcademicaSeleccionado;
    }

    public void setCargoAcademicaSeleccionado(
	    CargoAcademicoDTO cargoAcademicaSeleccionado) {
	this.cargoAcademicaSeleccionado = cargoAcademicaSeleccionado;
    }

    public Long getIdProceso() {
	return idProceso;
    }

    public void setIdProceso(Long idProceso) {
	this.idProceso = idProceso;
    }

    public Boolean getAcordion() {
	return acordion;
    }

    public void setAcordion(Boolean acordion) {
	this.acordion = acordion;
    }

    public Date getFechaMin() {
	Calendar cal = Calendar.getInstance();
	int anio = Calendar.getInstance().get(Calendar.YEAR) - 75;
	cal.set(anio, Calendar.JANUARY, 1);
	fechaMin = new Date(cal.getTimeInMillis());
	return fechaMin;
    }

    public void setFechaMin(Date fechaMin) {
	this.fechaMin = fechaMin;
    }

    public String getPerfil() {
	return perfil;
    }

    public void setPerfil(String perfil) {
	this.perfil = perfil;
    }

    public Boolean getAlertaEvaluador() {
	return alertaEvaluador;
    }

    public void setAlertaEvaluador(Boolean alertaEvaluador) {
	this.alertaEvaluador = alertaEvaluador;
    }

    public List<TipoProgramaEnum> getTipoPrograma() {
	return tipoPrograma;
    }

    public void setTipoPrograma(List<TipoProgramaEnum> tipoPrograma) {
	this.tipoPrograma = tipoPrograma;
    }

    public String getForma() {
	return forma;
    }

    public void setForma(String forma) {
	this.forma = forma;
    }

    public String getCursos() {
	return cursos;
    }

    public void setCursos(String cursos) {
	this.cursos = cursos;
    }

    public String getCargosAcademicos() {
	return cargosAcademicos;
    }

    public void setCargosAcademicos(String cargosAcademicos) {
	this.cargosAcademicos = cargosAcademicos;
    }

    public String getExperienciaProfesional() {
	return experienciaProfesional;
    }

    public void setExperienciaProfesional(String experienciaProfesional) {
	this.experienciaProfesional = experienciaProfesional;
    }

    public List<RelacionIESEnum> getRelacionesIes() {
	return Arrays.asList(RelacionIESEnum.values());
    }

    public void setRelacionesIes(List<RelacionIESEnum> relacionesIes) {
	this.relacionesIes = relacionesIes;
    }

    public List<CategoriaTitularEnum> getCategorias() {
	return categorias;
    }

    public void setCategorias(List<CategoriaTitularEnum> categorias) {
	this.categorias = categorias;
    }

    public List<TiempoDedicacionEnum> getTiemposDedicacion() {
	return tiemposDedicacion;
    }

    public void setTiemposDedicacion(
	    List<TiempoDedicacionEnum> tiemposDedicacion) {
	this.tiemposDedicacion = tiemposDedicacion;
    }

    public boolean isEsTitular() {
	return esTitular;
    }

    public void setEsTitular(boolean esTitular) {
	this.esTitular = esTitular;
    }

    public Date getFechaActual() {
	Calendar cal = Calendar.getInstance();
	fechaActual = new Date(cal.getTimeInMillis());
	return fechaActual;
    }

    public void setFechaActual(Date fechaActual) {
	this.fechaActual = fechaActual;
    }

    public ContratacionPeriodoAcademicoDTO getContratacionPeriodoAcademicoDTO() {
	return contratacionPeriodoAcademicoDTO;
    }

    public void setContratacionPeriodoAcademicoDTO(
	    ContratacionPeriodoAcademicoDTO contratacionPeriodoAcademicoDTO) {
	this.contratacionPeriodoAcademicoDTO = contratacionPeriodoAcademicoDTO;
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

    public void setPeriodosAcademicos(
	    List<PeriodoAcademicoDTO> periodosAcademicos) {
	this.periodosAcademicos = periodosAcademicos;
    }

    public List<ContratacionPeriodoAcademicoDTO> getContratacionesPeriodoAcademico() {
	return contratacionesPeriodoAcademico;
    }

    public void setContratacionesPeriodoAcademico(
	    List<ContratacionPeriodoAcademicoDTO> contratacionesPeriodoAcademico) {
	this.contratacionesPeriodoAcademico = contratacionesPeriodoAcademico;
    }

    public String getMensajeErrorPeriodo() {
	return mensajeErrorPeriodo;
    }

    public void setMensajeErrorPeriodo(String mensajeErrorPeriodo) {
	this.mensajeErrorPeriodo = mensajeErrorPeriodo;
    }

    public int getAnioInicioPeriodoAcademico() {
	return anioInicioPeriodoAcademico;
    }

    public void setAnioInicioPeriodoAcademico(int anioInicioPeriodoAcademico) {
	this.anioInicioPeriodoAcademico = anioInicioPeriodoAcademico;
    }

    public int getMesInicioPeriodoAcademico() {
	return mesInicioPeriodoAcademico;
    }

    public void setMesInicioPeriodoAcademico(int mesInicioPeriodoAcademico) {
	this.mesInicioPeriodoAcademico = mesInicioPeriodoAcademico;
    }

    public int getAnioFinPeriodoAcademico() {
	return anioFinPeriodoAcademico;
    }

    public void setAnioFinPeriodoAcademico(int anioFinPeriodoAcademico) {
	this.anioFinPeriodoAcademico = anioFinPeriodoAcademico;
    }

    public int getMesFinPeriodoAcademico() {
	return mesFinPeriodoAcademico;
    }

    public void setMesFinPeriodoAcademico(int mesFinPeriodoAcademico) {
	this.mesFinPeriodoAcademico = mesFinPeriodoAcademico;
    }

    public PeriodoAcademicoDTO getPeriodoAcademicoDTO() {
	return periodoAcademicoDTO;
    }

    public void setPeriodoAcademicoDTO(PeriodoAcademicoDTO periodoAcademicoDTO) {
	this.periodoAcademicoDTO = periodoAcademicoDTO;
    }

    public List<SelectItem> getANIOS() {
	return ANIOS;
    }

    public void setANIOS(List<SelectItem> aNIOS) {
	ANIOS = aNIOS;
    }

    public SelectItem[] getMESES() {
	return MESES;
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

    public List<PeriodoAcademicoDTO> getPeriodosAcademicosEliminar() {
	return periodosAcademicosEliminar;
    }

    public List<ContratacionPeriodoAcademicoDTO> getHorasNoAcademicasEliminar() {
	return horasNoAcademicasEliminar;
    }

    public void setHorasNoAcademicasEliminar(
	    List<ContratacionPeriodoAcademicoDTO> horasNoAcademicasEliminar) {
	this.horasNoAcademicasEliminar = horasNoAcademicasEliminar;
    }

    public ContratacionPeriodoAcademicoDTO getContratoPeriodoSeleccionado() {
	return contratoPeriodoSeleccionado;
    }

    public void setContratoPeriodoSeleccionado(
	    ContratacionPeriodoAcademicoDTO contratoPeriodoSeleccionado) {
	this.contratoPeriodoSeleccionado = contratoPeriodoSeleccionado;
    }

    public DocenteAsignaturaDTO getDocenteAsigEditable() {
	return docenteAsigEditable;
    }

    public void setDocenteAsigEditable(DocenteAsignaturaDTO docenteAsigEditable) {
	this.docenteAsigEditable = docenteAsigEditable;
    }

    public String getContrato() {
	return contrato;
    }

    public void setContrato(String contrato) {
	this.contrato = contrato;
    }

    public List<ContratacionHistoricoDTO> getListaContratacionHistorico() {
	return listaContratacionHistorico;
    }

    public void setListaContratacionHistorico(
	    List<ContratacionHistoricoDTO> listaContratacionHistorico) {
	this.listaContratacionHistorico = listaContratacionHistorico;
    }

    public ContratacionDTO getContratoSeleccionadoHistorico() {
	return contratoSeleccionadoHistorico;
    }

    public void setContratoSeleccionadoHistorico(
	    ContratacionDTO contratoSeleccionadoHistorico) {
	this.contratoSeleccionadoHistorico = contratoSeleccionadoHistorico;
    }

    public List<FormacionProfesionalHistoricoDTO> getListaFormacionHistorico() {
	return listaFormacionHistorico;
    }

    public void setListaFormacionHistorico(
	    List<FormacionProfesionalHistoricoDTO> listaFormacionHistorico) {
	this.listaFormacionHistorico = listaFormacionHistorico;
    }

    public FormacionProfesionalDTO getFormacionSeleccionadaHistorico() {
	return formacionSeleccionadaHistorico;
    }

    public void setFormacionSeleccionadaHistorico(
	    FormacionProfesionalDTO formacionSeleccionadaHistorico) {
	this.formacionSeleccionadaHistorico = formacionSeleccionadaHistorico;
    }

    public List<CursoCapacitacionHistoricoDTO> getListaCapacitacionHistorico() {
	return listaCapacitacionHistorico;
    }

    public void setListaCapacitacionHistorico(
	    List<CursoCapacitacionHistoricoDTO> listaCapacitacionHistorico) {
	this.listaCapacitacionHistorico = listaCapacitacionHistorico;
    }

    public CursoCapacitacionDTO getCapacitacionSeleccionadaHistorico() {
	return capacitacionSeleccionadaHistorico;
    }

    public void setCapacitacionSeleccionadaHistorico(
	    CursoCapacitacionDTO capacitacionSeleccionadaHistorico) {
	this.capacitacionSeleccionadaHistorico = capacitacionSeleccionadaHistorico;
    }

    public CargoAcademicoDTO getCargoSeleccionadoHistorico() {
	return cargoSeleccionadoHistorico;
    }

    public void setCargoSeleccionadoHistorico(
	    CargoAcademicoDTO cargoSeleccionadoHistorico) {
	this.cargoSeleccionadoHistorico = cargoSeleccionadoHistorico;
    }

    public List<ExperienciaProfesionalHistoricoDTO> getListaExperienciaHistorico() {
	return listaExperienciaHistorico;
    }

    public void setListaExperienciaHistorico(
	    List<ExperienciaProfesionalHistoricoDTO> listaExperienciaHistorico) {
	this.listaExperienciaHistorico = listaExperienciaHistorico;
    }

    public ExperienciaProfesionalDTO getExperienciaSeleccionadoHistorico() {
	return experienciaSeleccionadoHistorico;
    }

    public void setExperienciaSeleccionadoHistorico(
	    ExperienciaProfesionalDTO experienciaSeleccionadoHistorico) {
	this.experienciaSeleccionadoHistorico = experienciaSeleccionadoHistorico;
    }

    public List<CargosAcademicosHistoricoDTO> getListaCargoHistorico() {
	return listaCargoHistorico;
    }

    public void setListaCargoHistorico(
	    List<CargosAcademicosHistoricoDTO> listaCargoHistorico) {
	this.listaCargoHistorico = listaCargoHistorico;
    }

    public List<GeneroEnum> getListaGenero() {
	return listaGenero;
    }

    public void setListaGenero(List<GeneroEnum> listaGenero) {
	this.listaGenero = listaGenero;
    }

    public DocenteDTO getDocenteSeleccionado() {
	return docenteSeleccionado;
    }

    public void setDocenteSeleccionado(DocenteDTO docenteSeleccionado) {
	this.docenteSeleccionado = docenteSeleccionado;
    }

    public DocenteDTO getDocenteSeleccionadoE() {
	return docenteSeleccionadoE;
    }

    public void setDocenteSeleccionadoE(DocenteDTO docenteSeleccionadoE) {
	this.docenteSeleccionadoE = docenteSeleccionadoE;
    }

    public List<GradoTituloDTO> getGradosTitulo() {
	return gradosTitulo;
    }

    public void setGradosTitulo(List<GradoTituloDTO> gradosTitulo) {
	this.gradosTitulo = gradosTitulo;
    }

    public Long getIdGrado() {
	return idGrado;
    }

    public void setIdGrado(Long idGrado) {
	this.idGrado = idGrado;
    }

    public String getIdentificacionPersona() {
	return identificacionPersona;
    }

    public void setIdentificacionPersona(String identificacionPersona) {
	this.identificacionPersona = identificacionPersona;
    }

    public void mostrarVerificarSenescyt() {

	titulosSenescyt.clear();
	LOG.info("identificacionPersona: " + identificacionPersona + " .");
	WebServiceController wsController = (WebServiceController) FacesContext
	        .getCurrentInstance().getExternalContext().getSessionMap()
	        .get("webServiceController");
	LOG.info("2 .");

	wsController.cargarIdentificacion(identificacionPersona);
	LOG.info("3 .");
	wsController.consultaSenescyt();
	LOG.info("4 .");
	titulosSenescyt = wsController.getTitulosReconocidos();
	LOG.info("titulosSenescyt: " + titulosSenescyt.size() + " .");

    }

    public void verificarSenescyt() {
	List<FormacionProfesionalDTO> listaFormacionVerificar = new ArrayList<FormacionProfesionalDTO>();
	try {

	    listaFormacionVerificar = registroServicio
		    .obtenerFormacionProfesionalPorPersonaEIes(
		            docenteSeleccionado.getId(), iesDTO.getId());
	    int numeroTitulosValidados = 0;

	    for (FormacionProfesionalDTO fpDTO : listaFormacionVerificar) {
		LOG.info(" fpDTO.getNivel() " + fpDTO.getNivel() + " - "
		        + fpDTO.getNumeroRegistroSenescyt() + " .");
		LOG.info(" fpDTO.getObservacionEvaluador() "
		        + fpDTO.getObservacionEvaluador());

		if (fpDTO.getObservacionEvaluador() == null) {

		    for (TituloSenescyt ts : titulosSenescyt) {

			if (ts.getRegistroSenescyt()
			        .trim()
			        .equalsIgnoreCase(
			                fpDTO.getNumeroRegistroSenescyt()
			                        .trim())) {
			    LOG.info(" .." + numeroTitulosValidados
				    + "..ts.getNivel() " + ts.getNivel()
				    + " ..ts.REGISTRO().."
				    + ts.getRegistroSenescyt());
			    if (ts.getNivel()
				    .equalsIgnoreCase(fpDTO.getNivel())) {
				fpDTO.setObservacionEvaluador("Validado Senescyt");
				fpDTO.setValidadoSenescyt(true);
				fpDTO.setAceptadoEvaluador(true);

				AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
				auditoriaDTO.setFechaModificacion(new Date());
				auditoriaDTO.setUsuarioModificacion(usuario);
				fpDTO.setAuditoriaDTO(auditoriaDTO);
				fpDTO.setFaseIesDTO(this.faseiesDTO);
				fpDTO.setVerificarEvidencia(false);

				registroServicio.registrarFormacion(fpDTO);
				listaFormacion = registroServicio
				        .obtenerFormacionProfesionalPorPersonaEIes(
				                docenteSeleccionado.getId(),
				                iesDTO.getId());
				numeroTitulosValidados++;

			    }
			}

		    }
		}
	    }
	    JsfUtil.msgInfo("Se validaron " + numeroTitulosValidados
		    + " títulos.");

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    public List<TituloSenescyt> getTitulosSenescyt() {
	return titulosSenescyt;
    }

    public void setTitulosSenescyt(List<TituloSenescyt> titulosSenescyt) {
	this.titulosSenescyt = titulosSenescyt;
    }

}
