package ec.gob.ceaaces.controller;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.context.FacesContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import ec.gob.ceaaces.catalogo.dtos.catalogos.AuditoriaDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseIesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.FaseSecuenciaDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.IesDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.ProcesoDTO;
import ec.gob.ceaaces.catalogo.enumeraciones.FaseSecuenciaEnum;
import ec.gob.ceaaces.catalogo.enumeraciones.TipoFaseEnum;
import ec.gob.ceaaces.catalogo.exceptions.ServicioException;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.util.EnvioMail;
import ec.gob.ceaaces.seguridad.model.dtos.UsuarioDTO;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.EvaluacionServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.JsfUtil;

/**
 * 
 * Controlador para generar reporte inicial de variables del ITTS
 * 
 * @author jhomara
 * 
 */

public class EvidenciasCambioFaseController implements Serializable {

    private static final long serialVersionUID = -4262265325931338547L;

    private static final Logger LOG = Logger
	    .getLogger(EvidenciasCambioFaseController.class.getSimpleName());

    @Autowired
    private EvaluacionServicio evaluacionServicio;

    @Autowired
    private CatalogoServicio catalogoServicio;

    @Autowired
    private RegistroServicio registroServicio;

    private ProcesoDTO procesoActual = new ProcesoDTO();
    private IesDTO iesDTO = new IesDTO();
    private InformacionIesDTO informacionIesDTO = new InformacionIesDTO();
    private FaseIesDTO faseIesDTO = new FaseIesDTO();
    private String usuario;
    private List<FaseSecuenciaDTO> fasesSecuencia = new ArrayList<FaseSecuenciaDTO>();
    private boolean cambioFaseOk = true;
    private boolean activarRectificacion;

    public EvidenciasCambioFaseController() {

    }

    @PostConstruct
    public void obtenerDatosIniciales() {
	ListaIesController controller = (ListaIesController) FacesContext
	        .getCurrentInstance().getExternalContext().getSessionMap()
	        .get("listaIesController");
	try {
	    LOG.info("IES--->: " + controller.getIes().getId());
	    informacionIesDTO = registroServicio
		    .obtenerInformacionIesPorIes(controller.getIes());
	    LOG.info("Información ies--->: " + informacionIesDTO);
	    procesoActual = catalogoServicio
		    .obtenerProcesoActualPorIdAplicacion(controller
		            .getIdAplicacion());
	    faseIesDTO = controller.getFaseIesDTO();
	    iesDTO = controller.getIes();
	    this.fasesSecuencia = catalogoServicio.obtenerSecuenciaDeFase(
		    faseIesDTO.getFaseProcesoDTO().getId(),
		    procesoActual.getId());

	    for (FaseSecuenciaDTO faseSeq : this.fasesSecuencia) {
		if (faseSeq.getFaseSiguienteDTO().getFaseDTO().getNombre()
		        .equals(FaseSecuenciaEnum.REGISTRO.name())) {
		    activarRectificacion = true;
		    break;
		}
	    }

	    usuario = controller.getUsuario();

	    SecurityContextHolder.getContext().getAuthentication().getName();

	} catch (ServicioException e) {
	    JsfUtil.msgError("Error al obtener informacion inicial. "
		    + e.getMessage());
	    e.printStackTrace();
	} catch (Exception e) {
	    JsfUtil.msgError("Error al obtener informacion inicial. Comuníquese con el administrador.");
	    e.printStackTrace();
	}
    }

    public String cambiarFase(String siguiente) {
	try {
	    this.fasesSecuencia = catalogoServicio.obtenerSecuenciaDeFase(
		    faseIesDTO.getFaseProcesoDTO().getId(),
		    procesoActual.getId());
	    if (cambioFaseOk || "INICIO".equals(siguiente)) {
		AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
		auditoriaDTO.setFechaModificacion(new Date());
		auditoriaDTO.setUsuarioModificacion(usuario);
		// obtenerDatosIniciales();
		for (FaseSecuenciaDTO faseSecDTO : fasesSecuencia) {
		    if (faseSecDTO.getFaseSiguienteDTO().getFaseDTO()
			    .getNombre().equals(siguiente)) {
			FaseIesDTO nuevaFase = faseIesDTO;
			faseIesDTO.setFechaFin(new Date());
			faseIesDTO.setAuditoriaDTO(auditoriaDTO);
			catalogoServicio.registrarCambioFase(faseIesDTO);
			nuevaFase.setId(null);
			nuevaFase.setFechaInicio(new Date());
			nuevaFase.setFaseDTO(faseSecDTO.getFaseSiguienteDTO()
			        .getFaseDTO());
			nuevaFase.setFaseProcesoDTO(faseSecDTO
			        .getFaseSiguienteDTO());
			// if (siguiente.equals(TipoFaseEnum.RECTIFICACION
			// .getValor())) {
			nuevaFase.setTipo(faseIesDTO.getTipo());
			// }
			if (faseIesDTO.getTipo().compareTo(
			        TipoFaseEnum.PRORROGA) == 0) {
			    nuevaFase.setTipo(TipoFaseEnum.PRORROGA);
			} else {
			    nuevaFase.setFechaFin(faseSecDTO
				    .getFaseSiguienteDTO().getFechaFin());
			}
			this.faseIesDTO = catalogoServicio
			        .registrarCambioFase(nuevaFase);
			ListaIesController controller = (ListaIesController) FacesContext
			        .getCurrentInstance().getExternalContext()
			        .getSessionMap().get("listaIesController");
			controller.cargarMenus();
		    }

		}
		enviarEmail();

		// EnvioMail.("Notificación de Inscripción", mensaje,
		// persona.getEmail());
		return "cambiarFase";
	    }
	} catch (ServicioException e) {
	    JsfUtil.msgError("Error al finalizar registro. Consulte con el administrador.");
	    e.printStackTrace();
	} catch (Exception e) {
	    JsfUtil.msgError("Error al finalizar registro. Consulte con el administrador.");
	    e.printStackTrace();
	}
	return null;

    }

    public void validarCambioFase(String siguiente) {
	try {

	    this.fasesSecuencia = catalogoServicio.obtenerSecuenciaDeFase(
		    faseIesDTO.getFaseProcesoDTO().getId(),
		    procesoActual.getId());
	    FaseIesDTO faseActual = catalogoServicio.obtenerFaseIesActual(
		    this.informacionIesDTO.getIes().getId(),
		    this.procesoActual.getId(), new Date());
	    if (!faseActual.getId().equals(faseIesDTO.getId())) {
		ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) FacesContext
		        .getCurrentInstance().getApplication()
		        .getNavigationHandler();
		handler.getNavigationCases();
		handler.performNavigation("index.jsf?faces-redirect=true");
		cambioFaseOk = false;
	    }

	    for (FaseSecuenciaDTO faseSecDTO : fasesSecuencia) {
		if (faseSecDTO.getFaseSiguienteDTO().getFaseDTO().getNombre()
		        .equals(FaseSecuenciaEnum.REGISTRO.name())
		        && siguiente.equals(FaseSecuenciaEnum.REGISTRO.name())) {
		    if (faseSecDTO.getFaseSiguienteDTO().getFechaFin()
			    .equals(new Date())
			    && faseSecDTO.getFaseSiguienteDTO().getFechaFin()
			            .before(new Date())) {
			JsfUtil.msgError("Operación no permitida. El plazo para realizar el registro de datos y "
			        + "carga de evidencias ha concluido. Por favor presione el botón FINALIZAR");
			cambioFaseOk = false;
		    }
		    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		    JsfUtil.msgAdvert("Recuerde que la fecha límite para"
			    + " finalizar la carga de evidencias es:  "
			    + sdf.format(faseIesDTO.getFaseProcesoDTO()
			            .getFechaFin()));
		    cambioFaseOk = true;
		}
	    }
	    cambioFaseOk = true;
	} catch (Exception e) {
	    JsfUtil.msgError("Error al obtener datos. Consulte con el administrador");
	    e.printStackTrace();
	    cambioFaseOk = false;
	}

    }

    private void enviarEmail() throws Exception {
	UsuarioDTO username = new UsuarioDTO();
	username = registroServicio.obtenerUsuario(usuario);
	String text2 = "", text3 = "", text4 = "";
	String mensaje1 = "<html><body><table style=\"width:800px; \">"
	        + "<tr style=\"background-color:#000000; height:30px; \">"
	        + "<td colspan=\"2\" ><a href=\"http://www.ceaaces.gob.ec/sitio/\"><img src=\"http://www.ceaaces.gob.ec/sitio/wp-content/uploads/2014/01/logo1.png\" title=\"CEAACES\" /></a></td>"
	        + "</tr>"
	        + "<tr style=\"height:450px; vertical-align:middle; background: #066dab; "
	        + "background: -moz-linear-gradient(-45deg,  #066dab 0%, #8abbd7 69%, #c5deea 100%);"
	        + "background: -webkit-gradient(linear, left top, right bottom, color-stop(0%,#066dab), color-stop(69%,#8abbd7), color-stop(100%,#c5deea));"
	        + "background: -webkit-linear-gradient(-45deg,  #066dab 0%,#8abbd7 69%,#c5deea 100%);"
	        + "background: -o-linear-gradient(-45deg,  #066dab 0%,#8abbd7 69%,#c5deea 100%); "
	        + "background: -ms-linear-gradient(-45deg,  #066dab 0%,#8abbd7 69%,#c5deea 100%); "
	        + "background: linear-gradient(135deg,  #066dab 0%,#8abbd7 69%,#c5deea 100%); "
	        + "filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#066dab', endColorstr='#c5deea',GradientType=1 );\">";

	if (!username.getEmail().split("@")[1].split("[.]")[1].equals("gmail")) {
	    text2 = "<td align=\"left\" colspan=\"2\" />";
	    // +
	    // "style=\"background-image:url('http://www.ceaaces.gob.ec/sitio/wp-content/uploads/2014/04/lapiz-ceaaces.png'); background-repeat:no-repeat; background-position: right center;\" background=\"http://www.ceaaces.gob.ec/sitio/wp-content/uploads/2014/04/lapiz-ceaaces.png\">";

	} else {
	    text3 = "<td align=\"left\" colspan=\"2\" />";
	    // +
	    // "style=\"background-image:url('http://www.ceaaces.gob.ec/sitio/wp-content/uploads/2014/04/lapiz-ceaaces.png'); background-repeat:no-repeat; background-position: right center;\" >";

	}

	text4 = "<p style=\"color:#ffffff; text-align:center;  font-size:xx-large; font-weight:bold;\">Sistema GIIES</p>"
	        + "<p style=\"color:#ffffff; padding:20px; font-size:20px;\">Estimad@ "
	        + username.getNombresCompletos()
	        + ":<br/> Usted ha seleccionado la opción finalizar carga de evidencias en el sistema GIIES, por tanto el INSTITUTO "
	        + iesDTO.getNombre()
	        + " ingresa automáticamente a la fase de evaluación.<br/>"
	        + "<br/> En caso de tener alguna duda, por favor contáctese con el administrador del sistema mediante la mesa de ayuda <a href='http://giies.ceaaces.gob.ec:8080/otrs/customer.pl' target='_blank'>aquí</a>"

	        + "<tr style=\"background-color:#000000; height:30px; color:#FFFFFF; padding:10px;\">"
	        + "<td><strong>Contáctenos:</strong> <br/>"
	        + "Dirección: Calle Germán Alemán E11 – 32 y Javier Arauz<br/>"
	        + "Teléfonos: (593 2) 225 6470 / 292 0003 / 292 0098 / 224 5988 "
	        + "Ext: 136 - 137<br/>"
	        + "Email: info@ceaaces.gob.ec"
	        +

	        "</td>"
	        + "<td><strong>S&iacute;guenos en:</strong> <br/><p align=\"center\"><a href=\"https://www.facebook.com/ceaaces\" target=\"_blank\"><img style=\"padding:1px;\" src=\"http://www.civismo.org/files/redes/facebook.png\" width=\"32px\" height=\"32px\" title=\"Facebook\" /></a>"
	        + "<a href=\"https://twitter.com/ceaaces\" target=\"_blank\"><img style=\"padding:1px;\" src=\"http://wwwext.amgen.com/img/global/twitter.png\" width=\"32px\" height=\"32px\" title=\"twitter\" /></a>"
	        + "<a href=\"https://www.flickr.com/photos/80809022@N03\" target=\"_blank\"><img style=\"background-color:#fff;padding:1px; border-radius:5px;\" src=\"http://desarrollandoelmundorural.com/sites/default/files/LogoFlickr.png\" width=\"32px\" height=\"32px\" title=\"Flickr\" /></a>"
	        + "<a href=\"https://www.youtube.com/user/CEAACES\" target=\"_blank\"><img style=\"background-color:#fff;padding:1px; border-radius:5px;\" src=\"http://www.briceadvertising.com/wp-content/themes/Nimble/images/youtube.png\" width=\"32px\" height=\"32px\" title=\"YouTube\" /></a></p>"
	        + "</tr>" + "</table></body></html>";

	String mensaje = mensaje1 + text2 + text3 + text4;

	EnvioMail objeto = new EnvioMail();
	objeto.enviarCorreo("Evidencia", username.getEmail(), mensaje);
    }

    public ProcesoDTO getProcesoActual() {
	return procesoActual;
    }

    public void setProcesoActual(ProcesoDTO procesoActual) {
	this.procesoActual = procesoActual;
    }

    public InformacionIesDTO getInformacionIesDTO() {
	return informacionIesDTO;
    }

    public void setInformacionIesDTO(InformacionIesDTO informacionIesDTO) {
	this.informacionIesDTO = informacionIesDTO;
    }

    public FaseIesDTO getFaseIesDTO() {
	return faseIesDTO;
    }

    public void setFaseIesDTO(FaseIesDTO faseIesDTO) {
	this.faseIesDTO = faseIesDTO;
    }

    public String getUsuario() {
	return usuario;
    }

    public void setUsuario(String usuario) {
	this.usuario = usuario;
    }

    public List<FaseSecuenciaDTO> getFasesSecuencia() {
	return fasesSecuencia;
    }

    public void setFasesSecuencia(List<FaseSecuenciaDTO> fasesSecuencia) {
	this.fasesSecuencia = fasesSecuencia;
    }

    public boolean isCambioFaseOk() {
	return cambioFaseOk;
    }

    public void setCambioFaseOk(boolean cambioFaseOk) {
	this.cambioFaseOk = cambioFaseOk;
    }

    public boolean isActivarRectificacion() {
	return activarRectificacion;
    }

    public void setActivarRectificacion(boolean activarRectificacion) {
	this.activarRectificacion = activarRectificacion;
    }

    public IesDTO getIesDTO() {
	return iesDTO;
    }

    public void setIesDTO(IesDTO iesDTO) {
	this.iesDTO = iesDTO;
    }

}
