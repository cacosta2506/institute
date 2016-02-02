package ec.gob.ceaaces.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;

import ec.gob.ceaaces.catalogo.dtos.catalogos.AuditoriaDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.ConceptoDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.IesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.ParametroDTO;
import ec.gob.ceaaces.catalogo.enumeraciones.GrupoConceptoEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.ParametroEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.TipoReferenciaEnum;
import ec.gob.ceaaces.catalogo.exceptions.ServicioException;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ActividadVinculacionDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ConvenioVigenteDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.DocenteDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaConceptoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.PersonaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ProduccionDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ProyectoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.PublicacionDTO;
import ec.gob.ceaaces.institutos.enumeraciones.OrigenInformacionEnum;
import ec.gob.ceaaces.institutos.enumeraciones.TipoProyectoEnum;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.InstitutosServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.ArchivoUtil;
import ec.gob.ceaaces.util.JsfUtil;

@ManagedBean(name = "evidenciaVinculacionController")
public class EvidenciaVinculacionController implements Serializable {

    private static final long serialVersionUID = 1606541917026042135L;

    private static final Logger LOG = Logger
	    .getLogger(EvidenciaVinculacionController.class.getSimpleName());

    private List<DocenteDTO> listaDocentes;
    private List<ConceptoDTO> listaEvidencia;
    private List<ActividadVinculacionDTO> listaVinculacion;
    private List<ConvenioVigenteDTO> listaConvenio;
    private List<PublicacionDTO> listaPublicacion;
    private List<ProyectoDTO> listaProyecto;
    private List<EvidenciaConceptoDTO> listaEvidenciaConcepto;
    private List<ProduccionDTO> listaProduccion;
    private EvidenciaConceptoDTO evidenciaConceptoSeleccionado;
    private ParametroDTO pDTO;
    private ParametroDTO peDTO;
    private FaseIesDTO faseiesDTO;
    private StreamedContent documentoDescarga;
    private IesDTO iesDTO;
    private String[] tabla;
    private UploadedFile file;
    private InformacionIesDTO informacionIesDto;
    private PersonaDTO docenteSeleccionado;
    private ProduccionDTO produccionSeleccionada;
    private ActividadVinculacionDTO vinculacionSeleccionada;
    private ConvenioVigenteDTO convenioSeleccionado;
    private PublicacionDTO publicacionSeleccionada;
    private ConceptoDTO conceptoSeleccionado;
    private ProyectoDTO proyectoSeleccionado;
    private DocenteDTO docenteDto;
    private EvidenciaDTO evidenciaDto;
    private EvidenciaDTO evidenciaSeleccionada;
    private Boolean evidencias = false;
    private Boolean evidenciaConcepto = false;
    private Boolean vinculacion = false;
    private Boolean convenio = false;
    private Boolean publicacion = false;
    private Boolean produccion = false;
    private Boolean proyectoE = false;
    private Boolean fase = false;
    private Long idProceso;
    private Long idEvidencia;
    private String fichero;
    private String nombreFichero;
    private String usuario;
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

    public EvidenciaVinculacionController() {
	this.listaDocentes = new ArrayList<DocenteDTO>();
	iesDTO = new IesDTO();
	this.listaVinculacion = new ArrayList<ActividadVinculacionDTO>();
	this.listaConvenio = new ArrayList<ConvenioVigenteDTO>();
	this.listaEvidencia = new ArrayList<ConceptoDTO>();
	this.listaEvidenciaConcepto = new ArrayList<EvidenciaConceptoDTO>();
	this.listaProduccion = new ArrayList<ProduccionDTO>();
	this.listaPublicacion = new ArrayList<PublicacionDTO>();
	this.listaProyecto = new ArrayList<ProyectoDTO>();
	publicacionSeleccionada = new PublicacionDTO();
	proyectoSeleccionado = new ProyectoDTO();
	produccionSeleccionada = new ProduccionDTO();
	informacionIesDto = new InformacionIesDTO();
	evidenciaConceptoSeleccionado = new EvidenciaConceptoDTO();
	evidenciaSeleccionada = new EvidenciaDTO();
	evidenciaDto = new EvidenciaDTO();
	conceptoSeleccionado = new ConceptoDTO();
	pDTO = new ParametroDTO();
	peDTO = new ParametroDTO();
	faseiesDTO = new FaseIesDTO();
	evidenciaConceptoSeleccionada = new EvidenciaConceptoDTO();
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
	    idProceso = controller.getFaseIesDTO().getProcesoDTO().getId();
	    pDTO = catalogoServicio.obtenerParametroPorCodigoYReferencia(
		    ParametroEnum.URL_CARGA_EVID.getValor(),
		    TipoReferenciaEnum.INST.getValor());
	    peDTO = catalogoServicio.obtenerParametroPorCodigoYReferencia(
		    ParametroEnum.URL_CARGA_EVID_ELIMINAR.getValor(),
		    TipoReferenciaEnum.INST.getValor());

	    this.informacionIesDto = registroServicio
		    .obtenerInformacionIesPorIes(iesDTO);
	    listaEvidencia = catalogoServicio
		    .obtenerConceptoPorIdProcesoYGrupo(idProceso.toString(),
		            GrupoConceptoEnum.VINCULACION_INVESTIGACION
		                    .getValor());

	    faseiesDTO = controller.getFaseIesDTO();
	    usuario = controller.getUsuario();

	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void cargarEvidencias() {
	try {
	    if (conceptoSeleccionado != null) {
		if (conceptoSeleccionado.getCodigo().equals("PRYVIN")
		        || conceptoSeleccionado.getCodigo().equals("PRYINV")) {
		    String variable = "";
		    if (conceptoSeleccionado.getCodigo().equals("PRYVIN")) {
			variable = TipoProyectoEnum.VINCULACION.toString();
		    } else

		    {
			variable = TipoProyectoEnum.INVESTIGACION_E_INNOVACION
			        .toString();
		    }
		    proyectoE = true;
		    vinculacion = false;
		    convenio = false;
		    publicacion = false;
		    evidenciaConcepto = false;
		    produccion = false;

		    listaProyecto = registroServicio.obtenerProyectos(
			    informacionIesDto.getId(), variable);

		} else if (conceptoSeleccionado.getOrigen().equals(
		        OrigenInformacionEnum.VINCULACION.getValor())) {

		    vinculacion = true;
		    convenio = false;
		    publicacion = false;
		    evidenciaConcepto = false;
		    proyectoE = false;
		    produccion = false;

		    listaVinculacion = registroServicio
			    .obtenerActividadesVinculacionPorIes(informacionIesDto
			            .getId());

		} else if (conceptoSeleccionado.getOrigen().equals(
		        OrigenInformacionEnum.CONVENIOS.getValor())) {

		    convenio = true;
		    vinculacion = false;
		    evidenciaConcepto = false;
		    publicacion = false;
		    produccion = false;
		    proyectoE = false;
		    listaConvenio = registroServicio
			    .obtenerConvenioVigentePorIes(informacionIesDto
			            .getId());

		}

		else if (conceptoSeleccionado.getOrigen().equals(
		        OrigenInformacionEnum.PUBLICACIONES.getValor())) {
		    publicacion = true;
		    vinculacion = false;
		    convenio = false;
		    evidenciaConcepto = false;
		    produccion = false;
		    proyectoE = false;

		    listaPublicacion = registroServicio
			    .obtenerPublicaciones(informacionIesDto.getId());

		} else if (conceptoSeleccionado.getOrigen().equals(
		        OrigenInformacionEnum.PRODUCCIONES.getValor())) {
		    vinculacion = false;
		    convenio = false;
		    evidenciaConcepto = false;
		    publicacion = false;
		    produccion = true;
		    proyectoE = false;

		    listaProduccion = registroServicio
			    .obtenerProducciones(informacionIesDto.getId());

		}
	    } else {
		vinculacion = false;
		evidenciaConcepto = false;
		convenio = false;
		publicacion = false;
		produccion = false;
		proyectoE = false;
	    }
	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void cargarEConcepto() {
	evidenciaConcepto = true;
	if (conceptoSeleccionado.getCodigo().equals("PRYVIN")
	        || conceptoSeleccionado.getCodigo().equals("PRYINV")) {
	    idEvidencia = proyectoSeleccionado.getId();
	    nombreFichero = iesDTO.getCodigo() + "_"
		    + proyectoSeleccionado.getId();

	} else if (conceptoSeleccionado.getOrigen().equals(
	        OrigenInformacionEnum.VINCULACION.getValor())) {
	    idEvidencia = vinculacionSeleccionada.getId();
	    nombreFichero = iesDTO.getCodigo() + "_"
		    + vinculacionSeleccionada.getId();
	} else if (conceptoSeleccionado.getOrigen().equals(
	        OrigenInformacionEnum.CONVENIOS.getValor())) {
	    idEvidencia = convenioSeleccionado.getId();
	    nombreFichero = iesDTO.getCodigo() + "_"
		    + convenioSeleccionado.getId();

	}

	else if (conceptoSeleccionado.getOrigen().equals(
	        OrigenInformacionEnum.PUBLICACIONES.getValor())) {
	    idEvidencia = publicacionSeleccionada.getId();
	    nombreFichero = iesDTO.getCodigo() + "_"
		    + publicacionSeleccionada.getId();

	}

	else if (conceptoSeleccionado.getOrigen().equals(
	        OrigenInformacionEnum.PRODUCCIONES.getValor())) {
	    idEvidencia = produccionSeleccionada.getId();
	    nombreFichero = iesDTO.getCodigo() + "_"
		    + produccionSeleccionada.getId();

	}

	try {
	    listaEvidenciaConcepto = institutosServicio
		    .obtenerEvidenciasDeIesPorIdConceptoEIdTabla(
		            iesDTO.getId(), conceptoSeleccionado.getId(),
		            idEvidencia, conceptoSeleccionado.getOrigen());

	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void cambioBanderaPositivo() {
	if (conceptoSeleccionado.getCodigo().equals("PRYVIN")
	        || conceptoSeleccionado.getCodigo().equals("PRYINV")) {
	    proyectoSeleccionado.setVerificarEvidencia(true);
	    try {
		registroServicio.registrarProyecto(proyectoSeleccionado);
	    } catch (ServicioException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	} else if (conceptoSeleccionado.getOrigen().equals(
	        OrigenInformacionEnum.VINCULACION.getValor())) {
	    vinculacionSeleccionada.setVerificarEvidencia(true);
	    try {
		registroServicio
		        .registrarActividadVinculacion(vinculacionSeleccionada);
	    } catch (ServicioException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	} else if (conceptoSeleccionado.getOrigen().equals(
	        OrigenInformacionEnum.CONVENIOS.getValor())) {
	    convenioSeleccionado.setVerificarEvidencia(true);
	    try {
		registroServicio.registrarConvenio(convenioSeleccionado);
	    } catch (ServicioException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	}

	else if (conceptoSeleccionado.getOrigen().equals(
	        OrigenInformacionEnum.PUBLICACIONES.getValor())) {

	    publicacionSeleccionada.setVerificarEvidencia(true);
	    try {
		registroServicio.registrarPublicacion(publicacionSeleccionada);
	    } catch (ServicioException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	}

	else if (conceptoSeleccionado.getOrigen().equals(
	        OrigenInformacionEnum.PRODUCCIONES.getValor())) {

	    produccionSeleccionada.setVerificarEvidencia(true);
	    try {
		registroServicio.registrarProduccion(produccionSeleccionada);
	    } catch (ServicioException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	}

    }

    public void cambioBanderaNegativo() {

	if (conceptoSeleccionado.getCodigo().equals("PRYVIN")
	        || conceptoSeleccionado.getCodigo().equals("PRYINV")) {
	    proyectoSeleccionado.setVerificarEvidencia(false);
	    try {
		registroServicio.registrarProyecto(proyectoSeleccionado);
	    } catch (ServicioException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	} else if (conceptoSeleccionado.getOrigen().equals(
	        OrigenInformacionEnum.VINCULACION.getValor())) {
	    vinculacionSeleccionada.setVerificarEvidencia(false);
	    try {
		registroServicio
		        .registrarActividadVinculacion(vinculacionSeleccionada);
	    } catch (ServicioException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	} else if (conceptoSeleccionado.getOrigen().equals(
	        OrigenInformacionEnum.CONVENIOS.getValor())) {
	    convenioSeleccionado.setVerificarEvidencia(false);
	    try {
		registroServicio.registrarConvenio(convenioSeleccionado);
	    } catch (ServicioException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	}

	else if (conceptoSeleccionado.getOrigen().equals(
	        OrigenInformacionEnum.PUBLICACIONES.getValor())) {

	    publicacionSeleccionada.setVerificarEvidencia(false);
	    try {
		registroServicio.registrarPublicacion(publicacionSeleccionada);
	    } catch (ServicioException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	}

	else if (conceptoSeleccionado.getOrigen().equals(
	        OrigenInformacionEnum.PRODUCCIONES.getValor())) {

	    produccionSeleccionada.setVerificarEvidencia(false);
	    try {
		registroServicio.registrarProduccion(produccionSeleccionada);
	    } catch (ServicioException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

    }

    public void verificarEvidencia() {
	Boolean fase = false;
	for (EvidenciaConceptoDTO evidenciaConceptoDTO : listaEvidenciaConcepto) {
	    if (evidenciaConceptoDTO.getId().equals(
		    evidenciaConceptoSeleccionada.getId())) {
		if (evidenciaConceptoDTO.getEvidenciasDTO().isEmpty()) {
		    cambioBanderaNegativo();
		}

		for (EvidenciaDTO evidencia : evidenciaConceptoDTO
		        .getEvidenciasDTO()) {

		    if (evidencia.getFaseIesDTO().getFaseProcesoDTO().getId()
			    .equals(faseiesDTO.getFaseProcesoDTO().getId())) {
			cambioBanderaPositivo();
			fase = true;
			break;
		    } else {
			cambioBanderaNegativo();
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
	        + idEvidencia + "/"
	        + evidenciaSeleccionada.getNombreArchivo().toString().trim();
	String urlDestino = peDTO.getValor().toString().trim()
	        + faseiesDTO.getId() + "/" + iesDTO.getCodigo() + "/"
	        + idEvidencia + "/";
	Boolean isCreated = ArchivoUtil.crearDirectorio(urlDestino);
	if (isCreated) {
	    ArchivoUtil.moverArchivo(origen, destino);
	    evidenciaSeleccionada.setActivo(false);
	    evidenciaSeleccionada.setUrl(urlDestino);
	    auditoria.setFechaModificacion(new Date());
	    auditoria.setUsuarioModificacion(usuario);
	    evidenciaSeleccionada.setAuditoriaDTO(auditoria);

	    try {
		institutosServicio.crearActualizar(evidenciaSeleccionada);

		cargarEConcepto();
		verificarEvidencia();

		JsfUtil.msgInfo("Evidencia eliminada correctamente");
	    } catch (ServicioException e) {
		// TODO Auto-generated catch block
		LOG.info("Error: ");
		e.printStackTrace();
	    }

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
		    boolean creoArchivo = escribirArchivo(nombreGenerado,
			    contenido, urlNew);
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
			if (conceptoSeleccionado.getCodigo().equals("PRYVIN")
			        || conceptoSeleccionado.getCodigo().equals(
			                "PRYINV")) {
			    proyectoSeleccionado.setVerificarEvidencia(true);
			    registroServicio
				    .registrarProyecto(proyectoSeleccionado);

			} else if (conceptoSeleccionado.getOrigen().equals(
			        OrigenInformacionEnum.VINCULACION.getValor())) {
			    vinculacionSeleccionada.setVerificarEvidencia(true);
			    try {
				registroServicio
				        .registrarActividadVinculacion(vinculacionSeleccionada);
			    } catch (ServicioException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			    }

			} else if (conceptoSeleccionado.getOrigen().equals(
			        OrigenInformacionEnum.CONVENIOS.getValor())) {
			    convenioSeleccionado.setVerificarEvidencia(true);
			    registroServicio
				    .registrarConvenio(convenioSeleccionado);

			}

			else if (conceptoSeleccionado.getOrigen().equals(
			        OrigenInformacionEnum.PUBLICACIONES.getValor())) {

			    publicacionSeleccionada.setVerificarEvidencia(true);
			    registroServicio
				    .registrarPublicacion(publicacionSeleccionada);

			}

			else if (conceptoSeleccionado.getOrigen().equals(
			        OrigenInformacionEnum.PRODUCCIONES.getValor())) {

			    produccionSeleccionada.setVerificarEvidencia(true);
			    registroServicio
				    .registrarProduccion(produccionSeleccionada);

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

    private boolean escribirArchivo(String nombre, byte[] contenido, String url1)
	    throws Exception {
	boolean correcto = false;

	FileOutputStream fos = null;
	File f = new File(url1);

	boolean existe = false;
	String[] arrayUrl = url1.split("/");
	String auxUrl = "";
	for (int i = 1; i < arrayUrl.length; i++) {
	    auxUrl = auxUrl.concat("/" + arrayUrl[i]);
	    f = new File(auxUrl);
	    if (!f.exists()) {
		if (f.mkdir()) {
		    existe = true;
		    continue;
		} else {
		    JsfUtil.msgError("No se pudo crear el directorio para el archivo");
		}
	    } else {
		existe = true;
	    }
	}

	if (existe) {
	    File f2 = new File(f.getAbsolutePath() + "/" + nombre);
	    if (!f2.exists()) {
		try {
		    f2.createNewFile();
		    fos = new FileOutputStream(f2);
		    fos.write(contenido);
		    correcto = true;
		} catch (IOException e) {
		    JsfUtil.msgError("Error al crear Archivo");
		    throw new Exception(e.getMessage());
		} finally {
		    try {
			fos.close();
		    } catch (IOException e) {
			e.printStackTrace();
		    }
		}
	    } else {
		JsfUtil.msgAdvert("Ya existe un documento con el mismo nombre");
	    }
	}
	return correcto;
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

    public EvidenciaDTO getEvidenciaDto() {
	return evidenciaDto;
    }

    public void setEvidenciaDto(EvidenciaDTO evidenciaDto) {
	this.evidenciaDto = evidenciaDto;
    }

    public Boolean getVinculacion() {
	return vinculacion;
    }

    public void setVinculacion(Boolean vinculacion) {
	this.vinculacion = vinculacion;
    }

    public List<ActividadVinculacionDTO> getListaVinculacion() {
	return listaVinculacion;
    }

    public void setListaVinculacion(
	    List<ActividadVinculacionDTO> listaVinculacion) {
	this.listaVinculacion = listaVinculacion;
    }

    public ActividadVinculacionDTO getVinculacionSeleccionada() {
	return vinculacionSeleccionada;
    }

    public void setVinculacionSeleccionada(
	    ActividadVinculacionDTO vinculacionSeleccionada) {
	this.vinculacionSeleccionada = vinculacionSeleccionada;
    }

    public List<ConvenioVigenteDTO> getListaConvenio() {
	return listaConvenio;
    }

    public void setListaConvenio(List<ConvenioVigenteDTO> listaConvenio) {
	this.listaConvenio = listaConvenio;
    }

    public Boolean getConvenio() {
	return convenio;
    }

    public void setConvenio(Boolean convenio) {
	this.convenio = convenio;
    }

    public ConvenioVigenteDTO getConvenioSeleccionado() {
	return convenioSeleccionado;
    }

    public void setConvenioSeleccionado(ConvenioVigenteDTO convenioSeleccionado) {
	this.convenioSeleccionado = convenioSeleccionado;
    }

    public Boolean getPublicacion() {
	return publicacion;
    }

    public void setPublicacion(Boolean publicacion) {
	this.publicacion = publicacion;
    }

    public List<PublicacionDTO> getListaPublicacion() {
	return listaPublicacion;
    }

    public void setListaPublicacion(List<PublicacionDTO> listaPublicacion) {
	this.listaPublicacion = listaPublicacion;
    }

    public PublicacionDTO getPublicacionSeleccionada() {
	return publicacionSeleccionada;
    }

    public void setPublicacionSeleccionada(
	    PublicacionDTO publicacionSeleccionada) {
	this.publicacionSeleccionada = publicacionSeleccionada;
    }

    public Boolean getProduccion() {
	return produccion;
    }

    public void setProduccion(Boolean produccion) {
	this.produccion = produccion;
    }

    public List<ProduccionDTO> getListaProduccion() {
	return listaProduccion;
    }

    public void setListaProduccion(List<ProduccionDTO> listaProduccion) {
	this.listaProduccion = listaProduccion;
    }

    public ProduccionDTO getProduccionSeleccionada() {
	return produccionSeleccionada;
    }

    public void setProduccionSeleccionada(ProduccionDTO produccionSeleccionada) {
	this.produccionSeleccionada = produccionSeleccionada;
    }

    public List<ProyectoDTO> getListaProyecto() {
	return listaProyecto;
    }

    public void setListaProyecto(List<ProyectoDTO> listaProyecto) {
	this.listaProyecto = listaProyecto;
    }

    public Boolean getProyectoE() {
	return proyectoE;
    }

    public void setProyectoE(Boolean proyectoE) {
	this.proyectoE = proyectoE;
    }

    public ProyectoDTO getProyectoSeleccionado() {
	return proyectoSeleccionado;
    }

    public void setProyectoSeleccionado(ProyectoDTO proyectoSeleccionado) {
	this.proyectoSeleccionado = proyectoSeleccionado;
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

    public Long getIdProceso() {
	return idProceso;
    }

    public void setIdProceso(Long idProceso) {
	this.idProceso = idProceso;
    }

    public Boolean getFase() {
	return fase;
    }

    public void setFase(Boolean fase) {
	this.fase = fase;
    }

    public EvidenciaConceptoDTO getEvidenciaConceptoSeleccionada() {
	return evidenciaConceptoSeleccionada;
    }

    public void setEvidenciaConceptoSeleccionada(
	    EvidenciaConceptoDTO evidenciaConceptoSeleccionada) {
	this.evidenciaConceptoSeleccionada = evidenciaConceptoSeleccionada;
    }

}
