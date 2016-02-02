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
import ec.gob.ceaaces.catalogo.dtos.catalogos.ConceptoDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.IesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.ParametroDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.VariableDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.VariableProcesoDTO;
import ec.gob.ceaaces.catalogo.enumeraciones.GrupoVariableEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.ParametroEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.TipoReferenciaEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.TipoVariableEnum;
import ec.gob.ceaaces.catalogo.exceptions.ServicioException;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaConceptoDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.EvidenciaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ValorVariableCualitativaDTO;
import ec.gob.ceaaces.institutos.enumeraciones.OrigenEvidenciaEnum;
import ec.gob.ceaaces.institutos.enumeraciones.OrigenInformacionEnum;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.EvaluacionServicio;
import ec.gob.ceaaces.services.InstitutosServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.ArchivoUtil;
import ec.gob.ceaaces.util.JsfUtil;

public class EvidenciaInstitucionalController implements Serializable {

    private static final long serialVersionUID = 1606541917026042135L;

    private static final Logger LOG = Logger
	    .getLogger(EvidenciaInstitucionalController.class.getSimpleName());

    private List<VariableDTO> listaEvidenciaIntitucional;
    private VariableDTO institucionSeleccionada;
    private List<EvidenciaConceptoDTO> listaEvidenciaConcepto;
    private List<EvidenciaConceptoDTO> listaEvidenciaIES;
    private FaseIesDTO faseiesDTO;
    private IesDTO iesDTO;
    private EvidenciaDTO evidenciaDto;
    private EvidenciaDTO evidenciaSeleccionada;
    private StreamedContent documentoDescarga;
    private EvidenciaConceptoDTO evidenciaConceptoSeleccionado;
    private ParametroDTO pDTO;
    private ParametroDTO peDTO;
    private Long idVariable;
    private UploadedFile file;
    private String fichero;
    private String usuario;

    private String url;
    private String extensionDocumento;
    private Boolean descargar = false;
    private Boolean evidenciaConcepto = false;
    private VariableProcesoDTO variableProcesoDTO;
    private EvidenciaConceptoDTO evidenciaConceptoSeleccionada;

    @Autowired
    private InstitutosServicio institutosServicio;
    private InformacionIesDTO informacionIesDTO;
    private ConceptoDTO conceptoDTO;

    @Autowired
    private CatalogoServicio catalogoServicio;
    @Autowired
    private RegistroServicio registroServicio;

    @Autowired
    private EvaluacionServicio evaluacionServicio;

    public EvidenciaInstitucionalController() {
	this.listaEvidenciaIntitucional = new ArrayList<VariableDTO>();
	faseiesDTO = new FaseIesDTO();
	evidenciaDto = new EvidenciaDTO();
	institucionSeleccionada = new VariableDTO();
	evidenciaSeleccionada = new EvidenciaDTO();
	evidenciaConceptoSeleccionado = new EvidenciaConceptoDTO();
	iesDTO = new IesDTO();
	pDTO = new ParametroDTO();
	peDTO = new ParametroDTO();
	listaEvidenciaConcepto = new ArrayList<EvidenciaConceptoDTO>();
    }

    @PostConstruct
    public void start() {

	try {

	    ListaIesController controller = (ListaIesController) FacesContext
		    .getCurrentInstance().getExternalContext().getSessionMap()
		    .get("listaIesController");
	    faseiesDTO = controller.getFaseIesDTO();
	    LOG.info("faseIesDTO: " + faseiesDTO.getId());
	    iesDTO = controller.getIes();

	    informacionIesDTO = registroServicio
		    .obtenerInformacionIesPorIes(iesDTO);
	    usuario = controller.getUsuario();
	    pDTO = catalogoServicio.obtenerParametroPorCodigoYReferencia(
		    ParametroEnum.URL_CARGA_EVID.getValor(),
		    TipoReferenciaEnum.INST.getValor());
	    peDTO = catalogoServicio.obtenerParametroPorCodigoYReferencia(
		    ParametroEnum.URL_CARGA_EVID_ELIMINAR.getValor(),
		    TipoReferenciaEnum.INST.getValor());
	    conceptoDTO = catalogoServicio.obtenerConceptoPorOrigenYGrupo(
		    OrigenInformacionEnum.INFORMACION_IES.getValor(),
		    "INSTITUCIONAL");

	    // List<VariableProcesoDTO> varProceso = catalogoServicio
	    // .obtenerVariablesPorProcesoTipoYGrupo(faseiesDTO
	    // .getProcesoDTO().getId(), TipoIesRazonEnum.IES
	    // .getValue(), TipoVariableEnum.CUALITATIVA
	    // .getValue());
	    // varProceso.addAll(catalogoServicio
	    // .obtenerVariablesPorProcesoTipoYGrupo(faseiesDTO
	    // .getProcesoDTO().getId(),
	    // GrupoVariableEnum.INFRAESTRUCTURA.getValue(),
	    // TipoVariableEnum.CUANTITATIVA.getValue()));
	    //
	    // varProceso.addAll(catalogoServicio
	    // .obtenerVariablesPorProcesoTipoYGrupo(faseiesDTO
	    // .getProcesoDTO().getId(),
	    // GrupoVariableEnum.INFRAESTRUCTURA.getValue(),
	    // TipoVariableEnum.CUALITATIVA.getValue()));
	    //
	    // for (VariableProcesoDTO vpDTO : varProceso) {
	    // listaEvidenciaIntitucional.add(vpDTO.getVariableDTO());
	    // }
	    //
	    // // listaEvidenciaIntitucional = catalogoServicio
	    // // .obtenerVariablesPorProcesoTipoYGrupo(faseiesDTO
	    // // .getProcesoDTO().getId(), TipoIesRazonEnum.IES
	    // // .getValue(), TipoVariableEnum.CUALITATIVA
	    // // .getValue());

	    listaEvidenciaIntitucional = catalogoServicio
		    .obtenerVariablesPorProcesoYTipo(faseiesDTO.getProcesoDTO()
		            .getId(), TipoVariableEnum.CUALITATIVA.getValue(),
		            GrupoVariableEnum.IES.getValue());

	    VariableDTO variable = catalogoServicio.obtenerVariablePorId(174L);

	    listaEvidenciaIntitucional.addAll(catalogoServicio
		    .obtenerVariablesPorProcesoYTipo(faseiesDTO.getProcesoDTO()
		            .getId(), TipoVariableEnum.CUALITATIVA.getValue(),
		            GrupoVariableEnum.INFRAESTRUCTURA.getValue()));

	    if (variable != null) {
		listaEvidenciaIntitucional.add(variable);
	    }

	    listaEvidenciaIES = institutosServicio
		    .obtenerEvidenciasVariablesDeIesPorIdTabla(
		            informacionIesDTO.getId(), conceptoDTO,
		            informacionIesDTO.getIes().getId(), 174L);

	} catch (ServicioException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    public void cargarEvidencias() {
	listaEvidenciaConcepto.clear();

	if (!idVariable.equals(-1)) {
	    evidenciaConcepto = true;
	    try {
		List<EvidenciaConceptoDTO> evidenciasConcepto = institutosServicio
		        .obtenerEvidenciasVariablesDeIesPorIdTabla(
		                informacionIesDTO.getId(), conceptoDTO,
		                iesDTO.getId(), idVariable);
		for (EvidenciaConceptoDTO ecDTO : evidenciasConcepto) {
		    if (OrigenEvidenciaEnum.EVALUADOR.compareTo(ecDTO
			    .getOrigenEvidencia()) == 0) {
			continue;
		    }
		    listaEvidenciaConcepto.add(ecDTO);
		}
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
		    ValorVariableCualitativaDTO valorVariableCualitativaDTO = evaluacionServicio
			    .obtenerValorVariableCualitativa(idVariable,
			            informacionIesDTO.getId());
		    if (valorVariableCualitativaDTO != null) {

			valorVariableCualitativaDTO
			        .setFaseIesDTO(this.faseiesDTO);
			valorVariableCualitativaDTO
			        .setVerificarEvidencia(false);
			evaluacionServicio
			        .registrarValorVariableCualitativa(valorVariableCualitativaDTO);

		    }
		}

		for (EvidenciaDTO evidencia : evidenciaConceptoDTO
		        .getEvidenciasDTO()) {

		    if (evidencia.getFaseIesDTO().getFaseProcesoDTO().getId()
			    .equals(faseiesDTO.getFaseProcesoDTO().getId())) {

			ValorVariableCualitativaDTO valorVariableCualitativaDTO = evaluacionServicio
			        .obtenerValorVariableCualitativa(idVariable,
			                informacionIesDTO.getId());
			if (valorVariableCualitativaDTO != null) {
			    if (valorVariableCualitativaDTO.getFaseIesDTO()
				    .getId() != this.faseiesDTO.getId()) {
				valorVariableCualitativaDTO
				        .setFaseIesDTO(this.faseiesDTO);
				valorVariableCualitativaDTO
				        .setVerificarEvidencia(true);
				evaluacionServicio
				        .registrarValorVariableCualitativa(valorVariableCualitativaDTO);
			    }
			}

			fase = true;
			break;
		    } else {
			ValorVariableCualitativaDTO valorVariableCualitativaDTO = evaluacionServicio
			        .obtenerValorVariableCualitativa(idVariable,
			                informacionIesDTO.getId());
			if (valorVariableCualitativaDTO != null) {
			    if (valorVariableCualitativaDTO.getFaseIesDTO()
				    .getId() != this.faseiesDTO.getId()) {
				valorVariableCualitativaDTO
				        .setFaseIesDTO(this.faseiesDTO);
				valorVariableCualitativaDTO
				        .setVerificarEvidencia(false);
				evaluacionServicio
				        .registrarValorVariableCualitativa(valorVariableCualitativaDTO);
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
    }

    public void eliminarEvidencia() {
	AuditoriaDTO auditoria = new AuditoriaDTO();

	LOG.info(evidenciaSeleccionada.getNombreArchivo());
	String origen = evidenciaSeleccionada.getUrl().toString().trim()
	        + evidenciaSeleccionada.getNombreArchivo().toString().trim();
	LOG.info("ORIGEN: " + origen);
	String destino = peDTO.getValor().toString().trim()
	        + faseiesDTO.getId() + "/" + iesDTO.getCodigo() + "/"
	        + idVariable + "/"
	        + evidenciaSeleccionada.getNombreArchivo().toString().trim();
	String urlDestino = peDTO.getValor().toString().trim()
	        + faseiesDTO.getId() + "/" + iesDTO.getCodigo() + "/"
	        + idVariable + "/";
	LOG.info("DESTINO: " + destino);

	if (ArchivoUtil.crearDirectorio(urlDestino)) {
	    ArchivoUtil.moverArchivo(origen, destino);
	    evidenciaSeleccionada.setActivo(false);
	    evidenciaSeleccionada.setUrl(urlDestino);
	    auditoria.setFechaModificacion(new Date());
	    auditoria.setUsuarioModificacion(this.usuario);
	    evidenciaSeleccionada.setAuditoriaDTO(auditoria);

	    try {
		institutosServicio.crearActualizar(evidenciaSeleccionada);
	    } catch (ServicioException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    cargarEvidencias();
	    verificarEvidencia();
	    JsfUtil.msgInfo("Evidencia eliminada correctamente");

	}

    }

    public void uploadfile() {

	if (file != null) {
	    AuditoriaDTO auditoria = new AuditoriaDTO();

	    evidenciaDto.setEvidenciaConceptoDTO(evidenciaConceptoSeleccionado);
	    evidenciaDto.setIdTabla(informacionIesDTO.getId());
	    evidenciaDto.setFaseIesDTO(this.faseiesDTO);

	    evidenciaDto.setTabla(OrigenInformacionEnum.INFORMACION_IES
		    .getValor());

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

		String nombreGenerado = iesDTO.getCodigo() + "_" + idVariable
		        + "_COD_" + evidenciaConceptoSeleccionado.getId() + "_"
		        + hourdateFormat.format(date).toString() + "."
		        + extension.toString();

		LOG.info("Nombre Generado: " + nombreGenerado);

		String urlNew = pDTO.getValor() + this.faseiesDTO.getId() + "/"
		        + iesDTO.getCodigo() + "/" + idVariable + "/";

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

			auditoria.setUsuarioModificacion(this.usuario);
			evidenciaDto.setAuditoriaDTO(auditoria);

			institutosServicio.crearActualizar(evidenciaDto);

			evidenciaDto = new EvidenciaDTO();
			cargarEvidencias();
			// MODIFICAR VALOR_VARIABLE

			ValorVariableCualitativaDTO valorVariableCualitativaDTO = evaluacionServicio
			        .obtenerValorVariableCualitativa(idVariable,
			                informacionIesDTO.getId());
			if (valorVariableCualitativaDTO != null) {
			    if (valorVariableCualitativaDTO.getFaseIesDTO()
				    .getId() != this.faseiesDTO.getId()) {
				valorVariableCualitativaDTO
				        .setFaseIesDTO(this.faseiesDTO);
				valorVariableCualitativaDTO
				        .setVerificarEvidencia(true);
				evaluacionServicio
				        .registrarValorVariableCualitativa(valorVariableCualitativaDTO);
			    }
			}

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

    public List<VariableDTO> getListaEvidenciaIntitucional() {
	return listaEvidenciaIntitucional;
    }

    public void setListaEvidenciaIntitucional(
	    List<VariableDTO> listaEvidenciaIntitucional) {
	this.listaEvidenciaIntitucional = listaEvidenciaIntitucional;
    }

    public FaseIesDTO getFaseiesDTO() {
	return faseiesDTO;
    }

    public void setFaseiesDTO(FaseIesDTO faseiesDTO) {
	this.faseiesDTO = faseiesDTO;
    }

    public InformacionIesDTO getInformacionIesDTO() {
	return informacionIesDTO;
    }

    public void setInformacionIesDTO(InformacionIesDTO informacionIesDTO) {
	this.informacionIesDTO = informacionIesDTO;
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

    public InstitutosServicio getInstitutosServicio() {
	return institutosServicio;
    }

    public void setInstitutosServicio(InstitutosServicio institutosServicio) {
	this.institutosServicio = institutosServicio;
    }

    public Boolean getDescargar() {
	return descargar;
    }

    public void setDescargar(Boolean descargar) {
	this.descargar = descargar;
    }

    public EvidenciaDTO getEvidenciaDto() {
	return evidenciaDto;
    }

    public void setEvidenciaDto(EvidenciaDTO evidenciaDto) {
	this.evidenciaDto = evidenciaDto;
    }

    public EvidenciaDTO getEvidenciaSeleccionada() {
	return evidenciaSeleccionada;
    }

    public void setEvidenciaSeleccionada(EvidenciaDTO evidenciaSeleccionada) {
	this.evidenciaSeleccionada = evidenciaSeleccionada;
    }

    public EvidenciaConceptoDTO getEvidenciaConceptoSeleccionado() {
	return evidenciaConceptoSeleccionado;
    }

    public void setEvidenciaConceptoSeleccionado(
	    EvidenciaConceptoDTO evidenciaConceptoSeleccionado) {
	this.evidenciaConceptoSeleccionado = evidenciaConceptoSeleccionado;
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

    public UploadedFile getFile() {
	return file;
    }

    public void setFile(UploadedFile file) {
	this.file = file;
    }

    public IesDTO getIesDTO() {
	return iesDTO;
    }

    public void setIesDTO(IesDTO iesDTO) {
	this.iesDTO = iesDTO;
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

    public String getUsuario() {
	return usuario;
    }

    public void setUsuario(String usuario) {
	this.usuario = usuario;
    }

    public VariableProcesoDTO getVariableProcesoDTO() {
	return variableProcesoDTO;
    }

    public void setVariableProcesoDTO(VariableProcesoDTO variableProcesoDTO) {
	this.variableProcesoDTO = variableProcesoDTO;
    }

    public Long getIdVariable() {
	return idVariable;
    }

    public void setIdVariable(Long idVariable) {
	this.idVariable = idVariable;
    }

    public ConceptoDTO getConceptoDTO() {
	return conceptoDTO;
    }

    public void setConceptoDTO(ConceptoDTO conceptoDTO) {
	this.conceptoDTO = conceptoDTO;
    }

    public List<EvidenciaConceptoDTO> getListaEvidenciaIES() {
	return listaEvidenciaIES;
    }

    public void setListaEvidenciaIES(
	    List<EvidenciaConceptoDTO> listaEvidenciaIES) {
	this.listaEvidenciaIES = listaEvidenciaIES;
    }

    public VariableDTO getInstitucionSeleccionada() {
	return institucionSeleccionada;
    }

    public void setInstitucionSeleccionada(VariableDTO institucionSeleccionada) {
	this.institucionSeleccionada = institucionSeleccionada;
    }

    public EvidenciaConceptoDTO getEvidenciaConceptoSeleccionada() {
	return evidenciaConceptoSeleccionada;
    }

    public void setEvidenciaConceptoSeleccionada(
	    EvidenciaConceptoDTO evidenciaConceptoSeleccionada) {
	this.evidenciaConceptoSeleccionada = evidenciaConceptoSeleccionada;
    }

}
