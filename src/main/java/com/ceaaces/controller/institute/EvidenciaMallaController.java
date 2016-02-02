package ec.gob.ceaaces.controller;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;

import ec.gob.ceaaces.catalogo.dtos.catalogos.AuditoriaDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.CarreraIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.ConceptoDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.IesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.ParametroDTO;
import ec.gob.ceaaces.catalogo.enumeraciones.GrupoConceptoEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.ParametroEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.TipoReferenciaEnum;
import ec.gob.ceaaces.catalogo.exceptions.ServicioException;
import ec.gob.ceaaces.institutos.dtos.evaluacion.AsignaturaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ContratacionDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.DocenteDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaConceptoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.FormacionProfesionalDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionCarreraDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.MallaCurricularDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.PersonaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ProgramaEstudiosAsignaturaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.SedeIesDTO;
import ec.gob.ceaaces.institutos.enumeraciones.OrigenInformacionEnum;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.InstitutosServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.ArchivoUtil;
import ec.gob.ceaaces.util.JsfUtil;

@ManagedBean(name = "evidenciaMallaController")
public class EvidenciaMallaController implements Serializable {

    private static final long serialVersionUID = 1606541917026042135L;

    private static final Logger LOG = Logger
	    .getLogger(EvidenciaMallaController.class.getSimpleName());

    private List<DocenteDTO> listaDocentes;
    private List<ConceptoDTO> listaEvidencia;
    private List<EvidenciaConceptoDTO> listaEvideciaConcepto;
    private List<ContratacionDTO> listaContratacion;
    private ConceptoDTO conceptoSeleccionado;
    private List<SedeIesDTO> listaSede;

    private List<MallaCurricularDTO> listaMallas;
    private List<EvidenciaConceptoDTO> listaEvidenciaMalla;
    private List<FormacionProfesionalDTO> listaFormacion;
    private List<InformacionCarreraDTO> listaCarreras;
    private List<AsignaturaDTO> listaAsignatura;
    private ParametroDTO pDTO;
    private ParametroDTO peDTO;
    private FaseIesDTO faseiesDTO;

    private EvidenciaConceptoDTO evidenciaConceptoSeleccionado;
    private StreamedContent documentoDescarga;
    private IesDTO iesDTO;
    private CarreraIesDTO carretaDTO;
    private InformacionCarreraDTO infocarreraDTO;
    private String[] tabla;
    private UploadedFile file;
    private InformacionIesDTO informacionIesDto;
    private PersonaDTO docenteSeleccionado;
    private InformacionCarreraDTO carreraSeleccionada;
    private MallaCurricularDTO mallaSeleccionada;
    private AsignaturaDTO asignaturaSeleccionada;
    private MallaCurricularDTO mallaTSeleccionada;
    private FormacionProfesionalDTO formacionSeleccionada;
    private ContratacionDTO contratoSeleccionado;
    private ProgramaEstudiosAsignaturaDTO peaSeleccionado;

    private SedeIesDTO sedeSeleccionada;
    private DocenteDTO docenteDto;
    private EvidenciaDTO evidenciaDto;
    private EvidenciaDTO evidenciaSeleccionada;
    private Boolean evidencias = false;
    private Boolean evidenciaConcepto = false;
    private Boolean carreras = false;
    private String usuario;
    private Boolean evidenciaAsignatura = false;
    private Long idSede;
    private Long idCarrera;
    private Long idEvidencia;
    private String fichero;
    private String nombreFichero;
    private String url;
    private String extensionDocumento;
    private Boolean descargar = false;
    private List<AsignaturaDTO> listaAsignaturasPorPea;
    private List<AsignaturaDTO> listaAsignaturasAgregar;
    private Long idSedeIesSeleccionada;
    private Long idInformacionCarreraSeleccionada;
    private Long idMallaSeleccionada;
    private List<InformacionCarreraDTO> listaInformacionCarreraDto;
    private List<MallaCurricularDTO> listaMallaDto;
    private List<SedeIesDTO> listaSedeIesDto;

    @Autowired
    private InstitutosServicio institutosServicio;

    @Autowired
    private CatalogoServicio catalogoServicio;

    @Autowired
    private RegistroServicio registroServicio;

    public EvidenciaMallaController() {
	this.listaDocentes = new ArrayList<DocenteDTO>();
	iesDTO = new IesDTO();
	this.listaContratacion = new ArrayList<ContratacionDTO>();
	this.listaEvidencia = new ArrayList<ConceptoDTO>();
	this.listaMallas = new ArrayList<MallaCurricularDTO>();
	this.listaEvidenciaMalla = new ArrayList<EvidenciaConceptoDTO>();
	this.listaCarreras = new ArrayList<InformacionCarreraDTO>();
	this.listaEvideciaConcepto = new ArrayList<EvidenciaConceptoDTO>();
	this.listaFormacion = new ArrayList<FormacionProfesionalDTO>();
	this.listaSede = new ArrayList<SedeIesDTO>();
	this.listaAsignatura = new ArrayList<AsignaturaDTO>();
	this.listaSedeIesDto = new ArrayList<SedeIesDTO>();
	carreraSeleccionada = new InformacionCarreraDTO();
	conceptoSeleccionado = new ConceptoDTO();
	asignaturaSeleccionada = new AsignaturaDTO();
	carreraSeleccionada = new InformacionCarreraDTO();
	mallaSeleccionada = new MallaCurricularDTO();
	mallaTSeleccionada = new MallaCurricularDTO();
	formacionSeleccionada = new FormacionProfesionalDTO();
	informacionIesDto = new InformacionIesDTO();
	evidenciaConceptoSeleccionado = new EvidenciaConceptoDTO();
	evidenciaSeleccionada = new EvidenciaDTO();
	evidenciaDto = new EvidenciaDTO();
	pDTO = new ParametroDTO();
	peDTO = new ParametroDTO();
	faseiesDTO = new FaseIesDTO();
	listaAsignaturasPorPea = new ArrayList<AsignaturaDTO>();
	this.listaAsignaturasAgregar = new ArrayList<AsignaturaDTO>();
	this.listaInformacionCarreraDto = new ArrayList<InformacionCarreraDTO>();
	this.listaMallaDto = new ArrayList<MallaCurricularDTO>();

	// cargarDocente();

    }

    @PostConstruct
    public void start() {

	// evidencias = false;
	try {
	    ListaIesController controller = (ListaIesController) FacesContext
		    .getCurrentInstance().getExternalContext().getSessionMap()
		    .get("listaIesController");
	    iesDTO = controller.getIes();
	    carretaDTO = controller.getCarrera();
	    pDTO = catalogoServicio.obtenerParametroPorCodigoYReferencia(
		    ParametroEnum.URL_CARGA_EVID.getValor(),
		    TipoReferenciaEnum.INST.getValor());

	    peDTO = catalogoServicio.obtenerParametroPorCodigoYReferencia(
		    ParametroEnum.URL_CARGA_EVID_ELIMINAR.getValor(),
		    TipoReferenciaEnum.INST.getValor());

	    faseiesDTO = controller.getFaseIesDTO();

	    usuario = controller.getUsuario();

	    this.informacionIesDto = registroServicio
		    .obtenerInformacionIesPorIes(iesDTO);
	    listaSede = registroServicio.obtenerSedeIes(informacionIesDto
		    .getId());

	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void cargarCarreras() {
	if (!idSede.equals(-1L)) {
	    listaCarreras.clear();
	    try {
		listaCarreras = registroServicio.obtenerInfCarreraPorSede(
		        idSede, null);

	    } catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	} else {
	    listaCarreras.clear();
	    evidenciaConcepto = false;
	    carreras = false;
	    evidenciaAsignatura = false;
	    mallaTSeleccionada = null;
	}
    }

    public void cargarEvidencias() {

    }

    public void cargarEConcepto() {
	if (carreraSeleccionada != null && carreraSeleccionada.getId() != null) {
	    LOG.log(Level.INFO, "Evidencia Malla Carrera: {0} ",
		    carreraSeleccionada.getId());
	    evidenciaConcepto = true;
	    listaAsignatura.clear();
	    listaMallas.clear();

	    try {
		Date fechaMaxima = new Date();
		Calendar cal = Calendar.getInstance();
		cal.set(2014, 06, 30);
		List<MallaCurricularDTO> listaMallaDto = registroServicio
		        .obtenerUltimaMallaPorCarrera(
		                carreraSeleccionada.getId(), cal.getTime());
		List<MallaCurricularDTO> listaMallaEvDto = registroServicio
		        .obtenerMallaCurricularConEvidencias(carreraSeleccionada);
		for (MallaCurricularDTO malla : listaMallaEvDto) {
		    if (listaMallaDto.get(0).getId().equals(malla.getId())) {
			listaMallas.add(malla);
		    }

		}

	    } catch (ServicioException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	} else {
	    evidenciaAsignatura = false;
	    evidenciaConcepto = false;
	    mallaTSeleccionada = null;

	}

    }

    public void cargarEMConcepto() {
	asignaturaSeleccionada = null;
	conceptoSeleccionado = catalogoServicio.obtenerConceptoPorOrigenYGrupo(
	        OrigenInformacionEnum.MALLA.getValor(),
	        GrupoConceptoEnum.CARRERA.getValor());
	try {
	    listaEvideciaConcepto = institutosServicio
		    .obtenerConceptosDocumento(conceptoSeleccionado.getId());
	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void cargarEAConcepto() {
	mallaSeleccionada = null;
	conceptoSeleccionado = catalogoServicio.obtenerConceptoPorOrigenYGrupo(
	        OrigenInformacionEnum.ASIGNATURA.getValor(),
	        GrupoConceptoEnum.CARRERA.getValor());
	try {
	    listaEvideciaConcepto = institutosServicio
		    .obtenerConceptosDocumento(conceptoSeleccionado.getId());
	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void cargarPEAConcepto() {
	mallaSeleccionada = null;
	conceptoSeleccionado = catalogoServicio.obtenerConceptoPorOrigenYGrupo(
	        OrigenInformacionEnum.PEA.getValor(),
	        GrupoConceptoEnum.CARRERA.getValor());
	try {
	    listaEvideciaConcepto = institutosServicio
		    .obtenerConceptosDocumento(conceptoSeleccionado.getId());

	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void cargarAsignatura() {

	if (mallaTSeleccionada != null) {
	    evidenciaAsignatura = true;
	    try {
		listaAsignatura = registroServicio
		        .obtenerAsignaturasConEvidenciasPorMalla(mallaTSeleccionada
		                .getId());

	    } catch (ServicioException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	} else {
	    evidenciaAsignatura = false;
	}
    }

    public void verificarEvidencia() {
	Boolean fase = false;
	mallaSeleccionada.setFaseIesDTO(this.faseiesDTO);
	for (MallaCurricularDTO malla : listaMallas) {
	    if (malla.getId().equals(mallaSeleccionada.getId())) {
		mallaSeleccionada = malla;
		break;
	    }
	}

	if (mallaSeleccionada.getEvidenciasDTO().isEmpty()) {
	    mallaSeleccionada.setVerificarEvidencia(false);
	    try {
		registroServicio.registrarMallaCurricular(mallaSeleccionada);
	    } catch (ServicioException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	for (EvidenciaDTO evidencia : mallaSeleccionada.getEvidenciasDTO()) {

	    if (evidencia.getFaseIesDTO().getFaseProcesoDTO().getId()
		    .equals(faseiesDTO.getFaseProcesoDTO().getId())) {

		mallaSeleccionada.setVerificarEvidencia(true);
		try {
		    registroServicio
			    .registrarMallaCurricular(mallaSeleccionada);
		} catch (ServicioException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}

		fase = true;
		break;
	    } else {

		mallaSeleccionada.setVerificarEvidencia(false);
		try {
		    registroServicio
			    .registrarMallaCurricular(mallaSeleccionada);
		} catch (ServicioException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}

	    }
	    if (fase) {
		break;
	    }
	}

    }

    public void eliminarEvidencia() {
	if (mallaSeleccionada != null) {
	    idEvidencia = mallaSeleccionada.getId();

	} else {
	    idEvidencia = peaSeleccionado.getId();

	}
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
	    if (mallaSeleccionada != null) {
		verificarEvidencia();
	    }
	    // cargarAsignatura();
	    JsfUtil.msgInfo("Evidencia eliminada correctamente");

	}

    }

    public void eliminarEvidenciaPea() {
	AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
	auditoriaDTO.setUsuarioModificacion(this.usuario);
	auditoriaDTO.setFechaModificacion(new Date());

	this.peaSeleccionado.setActivo(false);
	this.peaSeleccionado.setAuditoriaDTO(auditoriaDTO);
	this.peaSeleccionado.setFaseIesDTO(this.faseiesDTO);
	this.listaAsignaturasPorPea.clear();
	this.asignaturaSeleccionada = new AsignaturaDTO();
	peaSeleccionado.setVerificarEvidencia(false);
	if (peaSeleccionado.getEvidenciasDTO() != null
	        && !peaSeleccionado.getEvidenciasDTO().isEmpty()) {
	    evidenciaSeleccionada = this.peaSeleccionado.getEvidenciasDTO()
		    .get(0);
	    eliminarEvidencia();
	}

    }

    public void uploadfile() {

	if (file != null) {
	    AuditoriaDTO auditoria = new AuditoriaDTO();

	    if (this.conceptoSeleccionado != null
		    && OrigenInformacionEnum.MALLA.getValor().equals(
		            this.conceptoSeleccionado.getOrigen())) {
		evidenciaDto.setEvidenciaConceptoDTO(listaEvideciaConcepto
		        .get(0));
		idEvidencia = mallaSeleccionada.getId();
		nombreFichero = iesDTO.getCodigo() + "_"
		        + mallaSeleccionada.getId();
		mallaSeleccionada.setFaseIesDTO(this.faseiesDTO);
		mallaSeleccionada.setVerificarEvidencia(true);
		try {
		    registroServicio
			    .registrarMallaCurricular(mallaSeleccionada);
		} catch (ServicioException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}

		evidenciaDto.setIdTabla(mallaSeleccionada.getId());
	    } else {
		cargarPEAConcepto();
		evidenciaDto.setEvidenciaConceptoDTO(listaEvideciaConcepto
		        .get(0));
		// AdminProgramasEstudioAsignaturaController controller =
		// (AdminProgramasEstudioAsignaturaController) JsfUtil
		// .obtenerObjetoSesion("peasController");
		// this.peaSeleccionado = controller.getPeaSeleccionado();
		this.peaSeleccionado = new ProgramaEstudiosAsignaturaDTO();
		this.peaSeleccionado.setVerificarEvidencia(true);
		this.peaSeleccionado.setActivo(true);
		this.peaSeleccionado.setFaseIesDTO(this.faseiesDTO);
		listaAsignaturasPorPea.clear();
		listaAsignaturasAgregar.clear();
		registrarPeasPorAsignatura();

		nombreFichero = iesDTO.getCodigo() + "_"
		        + peaSeleccionado.getId();
		idEvidencia = peaSeleccionado.getId();
		evidenciaDto.setIdTabla(peaSeleccionado.getId());

	    }
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
		        + listaEvideciaConcepto.get(0).getId() + "_"
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

			evidenciaDto = new EvidenciaDTO();
			conceptoSeleccionado = null;
			cargarEConcepto();
			cargarAsignatura();
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

    public void descargarPea(ProgramaEstudiosAsignaturaDTO peaDTO) {

	if (peaDTO.getEvidenciasDTO() != null
	        && !peaDTO.getEvidenciasDTO().isEmpty()) {
	    enviarEvidencia(peaDTO.getEvidenciasDTO().get(0));
	} else {
	    JsfUtil.msgInfo("El PEA no tiene evidencia, presione el botón editar para cargar la evidencia.");
	}

    }

    public void registrarPeasPorAsignatura() {

	try {

	    AuditoriaDTO auditoria = new AuditoriaDTO();
	    auditoria.setFechaModificacion(new Date());
	    auditoria.setUsuarioModificacion(usuario);

	    List<AsignaturaDTO> lisAsignatura = new ArrayList<AsignaturaDTO>();
	    lisAsignatura = listaAsignaturasPorPea;// listaAsignaturasDTO.getTarget();
	    LOG.info("LISTA ASIGNATURAS: " + lisAsignatura.size());

	    if (peaSeleccionado.getId() == null) {
		DateFormat hourdateFormat = new SimpleDateFormat("ddMMYYHHmmss");
		peaSeleccionado.setCodigo("PEA" + this.iesDTO.getCodigo() + "_"
		        + hourdateFormat.format(new Date()).toString());
	    }
	    if (lisAsignatura.isEmpty()) {
		if (asignaturaSeleccionada.getId() != null) {
		    lisAsignatura.add(this.asignaturaSeleccionada);
		}
	    }
	    peaSeleccionado.setAsignaturasDTO(lisAsignatura);
	    peaSeleccionado.setAuditoriaDTO(auditoria);
	    peaSeleccionado.setVerificarEvidencia(true);
	    peaSeleccionado = registroServicio.registrarPEA(peaSeleccionado);
	    cargarAsignatura();

	} catch (Exception e) {
	    e.printStackTrace();
	    JsfUtil.msgError("Ha ocurrido un error en el sistema. Comuníquese con el Administrador");
	}
    }

    public List<SedeIesDTO> cargarSedeIes() {

	List<SedeIesDTO> sedesIesDto = new ArrayList<SedeIesDTO>();
	try {
	    sedesIesDto = registroServicio.obtenerSedesIes(informacionIesDto
		    .getId());
	    LOG.info("listaSedeIesDto.size: " + sedesIesDto.size());
	} catch (Exception e) {
	    JsfUtil.msgError("Error al cargar Sede IES");
	}
	return sedesIesDto;
    }

    public void cargarCarrerasAux() {

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
	    Date fechaMaxima = new Date();
	    Calendar cal = Calendar.getInstance();
	    cal.set(2014, 06, 30);
	    listaMallaDto = registroServicio.obtenerUltimaMallaPorCarrera(
		    informacionCarreraDto.getId(), cal.getTime());
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void cargarAsignaturasAux() {

	if (null == idMallaSeleccionada)
	    return;

	// source.clear();
	listaAsignaturasAgregar = registroServicio
	        .obtenerAsignaturasSinPEAPorMalla(idMallaSeleccionada);

	// target = listaAsignaturasDTO.getTarget();
	//
	// if (null == target)
	// target = new ArrayList<AsignaturaDTO>();
	//
	// listaAsignaturasDTO = new DualListModel<AsignaturaDTO>(source,
	// target);
	// for (AsignaturaDTO asignaturaDTO :
	// listaAsignaturasDTO.getTarget()) {
	// for (AsignaturaDTO aDTO : listaAsignaturasDTO.getSource()) {
	// if (asignaturaDTO.getId().equals(aDTO.getId())) {
	// listaAsignaturasDTO.getSource().remove(aDTO);
	// break;
	// }
	// }
	// }

    }

    public void agregarAsignatura(AsignaturaDTO asignatura) {
	for (AsignaturaDTO asignaturaDTO : listaAsignaturasPorPea) {
	    if (asignaturaDTO.getId().equals(asignatura.getId())) {
		JsfUtil.msgAdvert("Asignatura ya ha sido agregada");
		return;
	    }
	}
	asignatura.setFaseIesDTO(this.faseiesDTO);
	listaAsignaturasPorPea.add(asignatura);
    }

    public void eliminarAsignatura() {
	for (AsignaturaDTO asignatura : listaAsignaturasPorPea) {
	    if (asignatura.getId().equals(asignaturaSeleccionada.getId())) {
		listaAsignaturasPorPea.remove(asignatura);
		break;
	    }
	}

    }

    private void cargarListasIes() {
	listaSedeIesDto.clear();
	listaSedeIesDto = cargarSedeIes();
    }

    public void cargarEdicion(ProgramaEstudiosAsignaturaDTO peaDTO) {

	peaSeleccionado = peaDTO;

	idSedeIesSeleccionada = null;
	idInformacionCarreraSeleccionada = null;
	idMallaSeleccionada = null;
	listaAsignaturasAgregar.clear();
	cargarListasIes();

	obtenerAsignaturas();

    }

    private void obtenerAsignaturas() {

	try {
	    listaAsignaturasPorPea = registroServicio
		    .obtenerAsignaturasPorPEA(this.peaSeleccionado.getId());
	    // listaAsignaturasDTO.setTarget(listaAsignaturas);
	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

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

    public PersonaDTO getDocenteSeleccionado() {
	return docenteSeleccionado;
    }

    public void setDocenteSeleccionado(PersonaDTO docenteSeleccionado) {
	this.docenteSeleccionado = docenteSeleccionado;
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

    public List<ConceptoDTO> getListaEvidencia() {
	return listaEvidencia;
    }

    public void setListaEvidencia(List<ConceptoDTO> listaEvidencia) {
	this.listaEvidencia = listaEvidencia;
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
	this.contratoSeleccionado = contratoSeleccionado;
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

    public SedeIesDTO getSedeSeleccionada() {
	return sedeSeleccionada;
    }

    public void setSedeSeleccionada(SedeIesDTO sedeSeleccionada) {
	this.sedeSeleccionada = sedeSeleccionada;
    }

    public List<SedeIesDTO> getListaSede() {
	return listaSede;
    }

    public void setListaSede(List<SedeIesDTO> listaSede) {
	this.listaSede = listaSede;
    }

    public Boolean getCarreras() {
	return carreras;
    }

    public void setCarreras(Boolean carreras) {
	this.carreras = carreras;
    }

    public List<InformacionCarreraDTO> getListaCarreras() {
	return listaCarreras;
    }

    public void setListaCarreras(List<InformacionCarreraDTO> listaCarreras) {
	this.listaCarreras = listaCarreras;
    }

    public Long getIdSede() {
	return idSede;
    }

    public void setIdSede(Long idSede) {
	this.idSede = idSede;
    }

    public InformacionCarreraDTO getCarreraSeleccionada() {
	return carreraSeleccionada;
    }

    public void setCarreraSeleccionada(InformacionCarreraDTO carreraSeleccionada) {
	this.carreraSeleccionada = carreraSeleccionada;
    }

    public CarreraIesDTO getCarretaDTO() {
	return carretaDTO;
    }

    public void setCarretaDTO(CarreraIesDTO carretaDTO) {
	this.carretaDTO = carretaDTO;
    }

    public InformacionCarreraDTO getInfocarreraDTO() {
	return infocarreraDTO;
    }

    public void setInfocarreraDTO(InformacionCarreraDTO infocarreraDTO) {
	this.infocarreraDTO = infocarreraDTO;
    }

    public ConceptoDTO getConceptoSeleccionado() {
	return conceptoSeleccionado;
    }

    public void setConceptoSeleccionado(ConceptoDTO conceptoSeleccionado) {
	this.conceptoSeleccionado = conceptoSeleccionado;
    }

    public Long getIdCarrera() {
	return idCarrera;
    }

    public void setIdCarrera(Long idCarrera) {
	this.idCarrera = idCarrera;
    }

    public List<MallaCurricularDTO> getListaMallas() {
	return listaMallas;
    }

    public void setListaMallas(List<MallaCurricularDTO> listaMallas) {
	this.listaMallas = listaMallas;
    }

    public MallaCurricularDTO getMallaSeleccionada() {
	return mallaSeleccionada;
    }

    public void setMallaSeleccionada(MallaCurricularDTO mallaSeleccionada) {
	this.mallaSeleccionada = mallaSeleccionada;
    }

    public List<EvidenciaConceptoDTO> getListaEvidenciaMalla() {
	return listaEvidenciaMalla;
    }

    public void setListaEvidenciaMalla(
	    List<EvidenciaConceptoDTO> listaEvidenciaMalla) {
	this.listaEvidenciaMalla = listaEvidenciaMalla;
    }

    public List<EvidenciaConceptoDTO> getListaEvideciaConcepto() {
	return listaEvideciaConcepto;
    }

    public void setListaEvideciaConcepto(
	    List<EvidenciaConceptoDTO> listaEvideciaConcepto) {
	this.listaEvideciaConcepto = listaEvideciaConcepto;
    }

    public List<AsignaturaDTO> getListaAsignatura() {
	return listaAsignatura;
    }

    public void setListaAsignatura(List<AsignaturaDTO> listaAsignatura) {
	this.listaAsignatura = listaAsignatura;
    }

    public Boolean getEvidenciaAsignatura() {
	return evidenciaAsignatura;
    }

    public void setEvidenciaAsignatura(Boolean evidenciaAsignatura) {
	this.evidenciaAsignatura = evidenciaAsignatura;
    }

    public AsignaturaDTO getAsignaturaSeleccionada() {
	return asignaturaSeleccionada;
    }

    public void setAsignaturaSeleccionada(AsignaturaDTO asignaturaSeleccionada) {
	this.asignaturaSeleccionada = asignaturaSeleccionada;
    }

    public MallaCurricularDTO getMallaTSeleccionada() {
	return mallaTSeleccionada;
    }

    public void setMallaTSeleccionada(MallaCurricularDTO mallaTSeleccionada) {
	this.mallaTSeleccionada = mallaTSeleccionada;
    }

    public Long getIdEvidencia() {
	return idEvidencia;
    }

    public void setIdEvidencia(Long idEvidencia) {
	this.idEvidencia = idEvidencia;
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

    public ProgramaEstudiosAsignaturaDTO getPeaSeleccionado() {
	return peaSeleccionado;
    }

    public void setPeaSeleccionado(ProgramaEstudiosAsignaturaDTO peaSeleccionado) {
	this.peaSeleccionado = peaSeleccionado;
    }

    public List<AsignaturaDTO> getListaAsignaturasPorPea() {
	return listaAsignaturasPorPea;
    }

    public void setListaAsignaturasPorPea(
	    List<AsignaturaDTO> listaAsignaturasPorPea) {
	this.listaAsignaturasPorPea = listaAsignaturasPorPea;
    }

    public List<AsignaturaDTO> getListaAsignaturasAgregar() {
	return listaAsignaturasAgregar;
    }

    public void setListaAsignaturasAgregar(
	    List<AsignaturaDTO> listaAsignaturasAgregar) {
	this.listaAsignaturasAgregar = listaAsignaturasAgregar;
    }

    public Long getIdInformacionCarreraSeleccionada() {
	return idInformacionCarreraSeleccionada;
    }

    public void setIdInformacionCarreraSeleccionada(
	    Long idInformacionCarreraSeleccionada) {
	this.idInformacionCarreraSeleccionada = idInformacionCarreraSeleccionada;
    }

    public Long getIdMallaSeleccionada() {
	return idMallaSeleccionada;
    }

    public void setIdMallaSeleccionada(Long idMallaSeleccionada) {
	this.idMallaSeleccionada = idMallaSeleccionada;
    }

    public List<InformacionCarreraDTO> getListaInformacionCarreraDto() {
	return listaInformacionCarreraDto;
    }

    public void setListaInformacionCarreraDto(
	    List<InformacionCarreraDTO> listaInformacionCarreraDto) {
	this.listaInformacionCarreraDto = listaInformacionCarreraDto;
    }

    public List<MallaCurricularDTO> getListaMallaDto() {
	return listaMallaDto;
    }

    public void setListaMallaDto(List<MallaCurricularDTO> listaMallaDto) {
	this.listaMallaDto = listaMallaDto;
    }

    public Long getIdSedeIesSeleccionada() {
	return idSedeIesSeleccionada;
    }

    public void setIdSedeIesSeleccionada(Long idSedeIesSeleccionada) {
	this.idSedeIesSeleccionada = idSedeIesSeleccionada;
    }

    public List<SedeIesDTO> getListaSedeIesDto() {
	return listaSedeIesDto;
    }

    public void setListaSedeIesDto(List<SedeIesDTO> listaSedeIesDto) {
	this.listaSedeIesDto = listaSedeIesDto;
    }

}
