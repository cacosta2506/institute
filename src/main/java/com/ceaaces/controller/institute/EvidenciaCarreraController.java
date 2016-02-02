package ec.gob.ceaaces.controller;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
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
import ec.gob.ceaaces.institutos.dtos.evaluacion.ContratacionDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.DocenteDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaConceptoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.FormacionProfesionalDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionCarreraDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.PersonaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.SedeIesDTO;
import ec.gob.ceaaces.institutos.enumeraciones.OrigenInformacionEnum;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.InstitutosServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.ArchivoUtil;
import ec.gob.ceaaces.util.JsfUtil;

public class EvidenciaCarreraController implements Serializable {

    private static final long serialVersionUID = 1606541917026042135L;

    private static final Logger LOG = Logger
	    .getLogger(EvidenciaCarreraController.class.getSimpleName());

    private List<DocenteDTO> listaDocentes;
    private List<ConceptoDTO> listaEvidencia;
    private List<ContratacionDTO> listaContratacion;
    private ConceptoDTO conceptoSeleccionado;
    private List<SedeIesDTO> listaSede;

    private List<EvidenciaConceptoDTO> listaEvidenciaConcepto;
    private List<FormacionProfesionalDTO> listaFormacion;
    private List<InformacionCarreraDTO> listaCarreras;
    private EvidenciaConceptoDTO evidenciaConceptoSeleccionado;
    private StreamedContent documentoDescarga;
    private IesDTO iesDTO;
    private CarreraIesDTO carretaDTO;
    private ParametroDTO pDTO;
    private ParametroDTO peDTO;
    private FaseIesDTO faseiesDTO;
    private InformacionCarreraDTO infocarreraDTO;
    private String[] tabla;
    private UploadedFile file;
    private InformacionIesDTO informacionIesDto;
    private PersonaDTO docenteSeleccionado;
    private InformacionCarreraDTO carreraSeleccionada;
    private FormacionProfesionalDTO formacionSeleccionada;
    private ContratacionDTO contratoSeleccionado;

    private SedeIesDTO sedeSeleccionada;
    private DocenteDTO docenteDto;
    private EvidenciaDTO evidenciaDto;
    private EvidenciaDTO evidenciaSeleccionada;
    private Boolean evidencias = false;
    private String usuario;
    private Boolean evidenciaConcepto = false;
    private Boolean contratos = false;
    private Boolean carreras = false;
    private final Boolean experiencia = false;
    private Boolean capacitacion = false;
    private Boolean formacion = false;
    private Long idSede;
    private String fichero;

    private String url;
    private String extensionDocumento;
    private Boolean descargar = false;
    private EvidenciaConceptoDTO evidenciaConceptoSeleccionada;

    @Autowired
    private InstitutosServicio institutosServicio;

    @Autowired
    private CatalogoServicio catalogoServicio;

    @Autowired
    private RegistroServicio registroServicio;

    public EvidenciaCarreraController() {
	this.listaDocentes = new ArrayList<DocenteDTO>();
	iesDTO = new IesDTO();
	this.listaContratacion = new ArrayList<ContratacionDTO>();
	this.listaEvidencia = new ArrayList<ConceptoDTO>();
	this.listaEvidenciaConcepto = new ArrayList<EvidenciaConceptoDTO>();
	this.listaCarreras = new ArrayList<InformacionCarreraDTO>();

	this.listaFormacion = new ArrayList<FormacionProfesionalDTO>();
	this.listaSede = new ArrayList<SedeIesDTO>();

	carreraSeleccionada = new InformacionCarreraDTO();
	conceptoSeleccionado = new ConceptoDTO();

	formacionSeleccionada = new FormacionProfesionalDTO();
	informacionIesDto = new InformacionIesDTO();
	evidenciaConceptoSeleccionado = new EvidenciaConceptoDTO();
	evidenciaSeleccionada = new EvidenciaDTO();
	evidenciaDto = new EvidenciaDTO();
	pDTO = new ParametroDTO();
	peDTO = new ParametroDTO();
	faseiesDTO = new FaseIesDTO();

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
	    pDTO = catalogoServicio.obtenerParametroPorCodigoYReferencia(
		    ParametroEnum.URL_CARGA_EVID.getValor(),
		    TipoReferenciaEnum.INST.getValor());
	    peDTO = catalogoServicio.obtenerParametroPorCodigoYReferencia(
		    ParametroEnum.URL_CARGA_EVID_ELIMINAR.getValor(),
		    TipoReferenciaEnum.INST.getValor());
	    carretaDTO = controller.getCarrera();

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
	    carreras = true;

	    try {
		listaCarreras = registroServicio.obtenerInfCarreraPorSede(
		        idSede, null);

	    } catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	} else {
	    evidenciaConcepto = false;
	    carreras = false;
	}
    }

    public void cargarEvidencias() {

    }

    public void cargarEConcepto() {
	if (!idSede.equals(-1L)) {
	    evidenciaConcepto = true;
	    try {
		conceptoSeleccionado = catalogoServicio
		        .obtenerConceptoPorOrigenYGrupo(
		                OrigenInformacionEnum.CARRERA.getValor(),
		                GrupoConceptoEnum.CARRERA.getValor());
		listaEvidenciaConcepto = institutosServicio
		        .obtenerEvidenciasDeIesPorIdConceptoEIdTabla(
		                iesDTO.getId(), conceptoSeleccionado.getId(),
		                carreraSeleccionada.getId(),
		                conceptoSeleccionado.getOrigen());

	    } catch (ServicioException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	} else {
	    evidenciaConcepto = false;
	}

    }

    public void verificarEvidencia() {
	Boolean fase = false;
	for (EvidenciaConceptoDTO evidenciaConceptoDTO : listaEvidenciaConcepto) {
	    if (evidenciaConceptoDTO.getId().equals(
		    evidenciaConceptoSeleccionada.getId())) {

		if (evidenciaConceptoDTO.getEvidenciasDTO().isEmpty()) {
		    carreraSeleccionada.setVerificarEvidencia(false);
		    try {
			registroServicio
			        .registrarInformacionCarrera(carreraSeleccionada);
		    } catch (ServicioException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		}

		for (EvidenciaDTO evidencia : evidenciaConceptoDTO
		        .getEvidenciasDTO()) {

		    if (evidencia.getFaseIesDTO().getFaseProcesoDTO().getId()
			    .equals(faseiesDTO.getFaseProcesoDTO().getId())) {

			carreraSeleccionada.setVerificarEvidencia(true);
			try {
			    registroServicio
				    .registrarInformacionCarrera(carreraSeleccionada);
			} catch (ServicioException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			}

			fase = true;
			break;
		    } else {

			carreraSeleccionada.setVerificarEvidencia(false);
			try {
			    registroServicio
				    .registrarInformacionCarrera(carreraSeleccionada);
			} catch (ServicioException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
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
    }

    public void eliminarEvidencia() {
	AuditoriaDTO auditoria = new AuditoriaDTO();

	LOG.info(evidenciaSeleccionada.getNombreArchivo());
	String origen = evidenciaSeleccionada.getUrl().toString().trim()
	        + evidenciaSeleccionada.getNombreArchivo().toString().trim();
	LOG.info("ORIGEN: " + origen);
	String destino = peDTO.getValor().toString().trim()
	        + faseiesDTO.getId() + "/" + iesDTO.getCodigo() + "/"
	        + carreraSeleccionada.getId() + "/"
	        + evidenciaSeleccionada.getNombreArchivo().toString().trim();
	String urlDestino = peDTO.getValor().toString().trim()
	        + faseiesDTO.getId() + "/" + iesDTO.getCodigo() + "/"
	        + carreraSeleccionada.getId() + "/";
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
	    evidenciaDto.setIdTabla(carreraSeleccionada.getId());
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

		String nombreGenerado = iesDTO.getCodigo() + "_"
		        + carreraSeleccionada.getId()

		        + "_COD_" + evidenciaConceptoSeleccionado.getId() + "_"
		        + hourdateFormat.format(date).toString() + "."
		        + extension.toString();

		LOG.info("Nombre Generado: " + nombreGenerado);

		String urlNew = pDTO.getValor() + faseiesDTO.getId() + "/"
		        + iesDTO.getCodigo() + "/"
		        + carreraSeleccionada.getId() + "/";

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
			carreraSeleccionada.setVerificarEvidencia(true);
			registroServicio
			        .registrarInformacionCarrera(carreraSeleccionada);
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
	this.contratoSeleccionado = contratoSeleccionado;
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

    public Boolean getExperiencia() {
	return experiencia;
    }

    public Boolean getCapacitacion() {
	return capacitacion;
    }

    public void setCapacitacion(Boolean capacitacion) {
	this.capacitacion = capacitacion;
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

    public EvidenciaConceptoDTO getEvidenciaConceptoSeleccionada() {
	return evidenciaConceptoSeleccionada;
    }

    public void setEvidenciaConceptoSeleccionada(
	    EvidenciaConceptoDTO evidenciaConceptoSeleccionada) {
	this.evidenciaConceptoSeleccionada = evidenciaConceptoSeleccionada;
    }

}
