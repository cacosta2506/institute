package ec.gob.ceaaces.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.event.ValueChangeEvent;

import org.springframework.beans.factory.annotation.Autowired;

import ec.gob.ceaaces.catalogo.dtos.catalogos.AuditoriaDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.ProcesoDTO;
import ec.gob.ceaaces.catalogo.dtos.catalogos.VariableProcesoDTO;
import ec.gob.ceaaces.catalogo.exceptions.ServicioException;
import ec.gob.ceaaces.data.ResultadoVariable;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionCarreraDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.ValorVariableDTO;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.EvaluacionServicio;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.JsfUtil;

public class ResultadoController implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1711633276053493785L;

    @Autowired
    private RegistroServicio registroServicio;
    @Autowired
    private EvaluacionServicio evaluacionServicio;

    @Autowired
    private CatalogoServicio catalogoServicio;

    private List<ResultadoVariable> listaVariablesCuantitativas;
    private List<ResultadoVariable> listaVariablesCualitativas;
    private InformacionCarreraDTO informacionCarrera;
    private InformacionIesDTO informacionIesDTO;
    private String usuario;
    private ProcesoDTO procesoSeleccionado;
    private List<ProcesoDTO> procesosEvaluacion;

    public ResultadoController() {
	listaVariablesCualitativas = new ArrayList<ResultadoVariable>();
	listaVariablesCuantitativas = new ArrayList<ResultadoVariable>();
	procesosEvaluacion = new ArrayList<ProcesoDTO>();
	procesoSeleccionado = new ProcesoDTO();
    }

    @PostConstruct
    public void obtenerValoresReporte() {
	ListaIesController controller = (ListaIesController) JsfUtil
	        .obtenerObjetoSesion("listaIesController");
	try {

	    // informacionCarrera = institutosServicio
	    // .obtenerInformacionCarreraPorCarrera(controller
	    // .getCarrera());
	    informacionIesDTO = registroServicio
		    .obtenerInformacionIesPorIes(controller.getIes());
	    List<ProcesoDTO> procesos = catalogoServicio
		    .obtenerTodosProcesosPorIdAplicacion(controller
		            .getIdAplicacion());
	    for (ProcesoDTO proceso : procesos) {
		if (proceso.getFechaFinProceso().before(new Date())) {
		    procesosEvaluacion.add(proceso);
		}
	    }

	    usuario = controller.getUsuario();
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void cambiarFase() {
	// try {
	// ListaIesController controller = (ListaIesController) FacesContext
	// .getCurrentInstance().getExternalContext().getSessionMap()
	// .get("listaIesController");

	// DESACTIVAR ULTIMA FASE
	//
	// if (infoIesFaseActual != null) {
	// infoIesFaseActual.setActiva(false);
	// institutosServicio.actualizarFaseIes(infoIesFaseActual);
	// }

	// CREAR NUEVA FASE
	// InformacionIesFaseDTO infoIesFase = new InformacionIesFaseDTO();
	// infoIesFase.setInformacionIesDTO(informacionIesDTO);
	// infoIesFase.setFechaInicio(new Date());
	// FaseDTO faseNueva = new FaseDTO();
	// faseNueva.setId(4L);
	// infoIesFase.setFaseEvaluacionDTO(faseNueva);
	// AuditoriaDTO audit = new AuditoriaDTO();
	// audit.setUsuarioModificacion(controller.getUsuario());
	// audit.setFechaModificacion(new Date());
	// infoIesFase.setAuditoriaDTO(audit);
	// if (informacionIesDTO.getIdFase().equals(3L)) {
	// informacionIesDTO.setIdFase(4L);
	// infoIesFase.setActiva(true);
	// institutosServicio.actualizarFaseIes(infoIesFase);
	// institutosServicio.crearActualizar(informacionIesDTO);
	// } else {
	// if (informacionIesDTO.getIdFase().equals(9L)) {
	// informacionIesDTO.setIdFase(10L);
	// }
	// }
	// LogoutController logoutController = (LogoutController)
	// FacesContext
	// .getCurrentInstance().getExternalContext().getSessionMap()
	// .get("logoutController");
	// logoutController.logout();
	// ExternalContext ectx = FacesContext.getCurrentInstance()
	// .getExternalContext();
	// HttpSession session = (HttpSession) ectx.getSession(false);
	// session.invalidate();
	// ectx.redirect("https://localhost:8443/cas-3.5.1/login");
	// } catch (ServicioException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
    }

    public void llenarValoresVariablesCuantitativas() {

	listaVariablesCuantitativas.clear();
	List<ValorVariableDTO> valoresDTO = new ArrayList<>();

	try {

	    List<VariableProcesoDTO> variables = catalogoServicio
		    .obtenerVariablesPorProcesoTipoYGrupo(
		            procesoSeleccionado.getId(), "EST", "CUANTITATIVA");

	    AuditoriaDTO auditoriaDTO = new AuditoriaDTO();

	    auditoriaDTO.setUsuarioModificacion(usuario);
	    auditoriaDTO.setFechaModificacion(new Date());
	    // TODO
	    // VERIFICAR ID CARRERA
	    valoresDTO = evaluacionServicio.obtenerValoresVariableProcesoFase(
		    procesoSeleccionado.getId(), null,
		    informacionIesDTO.getId());

	    for (ValorVariableDTO vvDTO : valoresDTO) {
		if (vvDTO.getVariableProcesoDTO().getVariableDTO()
		        .getTipoVariable().getValue().equals("CUANTITATIVA")) {
		    ResultadoVariable rv = new ResultadoVariable();
		    // if (vvDTO.getCodigoMuestra() != null) {
		    // rv.setAceptados(institutosServicio.obtenerValoresMuestra("Si",
		    // vvDTO.getCodigoMuestra()));
		    // }
		    // List<ValorVariableHistoricoDTO> vvHistorico =
		    // institutosServicio
		    // .obtenerUltimoValorHistorico(vvDTO, null);
		    // if (vvHistorico != null && !vvHistorico.isEmpty()) {
		    // rv.setVariableHistoricoDTO(vvHistorico.get(0));
		    // }
		    rv.setValorVariableDTO(vvDTO);
		    listaVariablesCuantitativas.add(rv);
		}
	    }
	} catch (ServicioException e) {
	    e.printStackTrace();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void llenarValorVariablesCualitativas() {
	listaVariablesCualitativas.clear();
	List<ValorVariableDTO> valoresDTO = new ArrayList<>();
	try {

	    // TODO
	    // VERIFICAR ID CARRERA
	    valoresDTO = evaluacionServicio.obtenerValoresVariableProcesoFase(
		    procesoSeleccionado.getId(), null,
		    informacionIesDTO.getId());
	    for (ValorVariableDTO vvDTO : valoresDTO) {
		if (vvDTO.getVariableProcesoDTO().getVariableDTO()
		        .getTipoVariable().getValue().equals("CUALITATIVA")) {
		    ResultadoVariable rv = new ResultadoVariable();
		    rv.setValorVariableDTO(vvDTO);
		    // List<ValorVariableHistoricoDTO> vvHistorico =
		    // institutosServicio
		    // .obtenerUltimoValorHistorico(vvDTO, null);
		    // if (vvHistorico != null && !vvHistorico.isEmpty()) {
		    // rv.setVariableHistoricoDTO(vvHistorico.get(0));
		    // }
		    listaVariablesCualitativas.add(rv);
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void seleccionarProceso(ValueChangeEvent ev) {
	if (ev != null && ev.getNewValue() != null) {
	    procesoSeleccionado.setId(new Long(ev.getNewValue().toString()));
	    llenarValoresVariablesCuantitativas();
	    llenarValorVariablesCualitativas();
	}
    }

    public List<ResultadoVariable> getListaVariablesCuantitativas() {
	return listaVariablesCuantitativas;
    }

    public void setListaVariablesCuantitativas(
	    List<ResultadoVariable> listaVariablesCuantitativas) {
	this.listaVariablesCuantitativas = listaVariablesCuantitativas;
    }

    public List<ResultadoVariable> getListaVariablesCualitativas() {
	return listaVariablesCualitativas;
    }

    public void setListaVariablesCualitativas(
	    List<ResultadoVariable> listaVariablesCualitativas) {
	this.listaVariablesCualitativas = listaVariablesCualitativas;
    }

    public String getUsuario() {
	return usuario;
    }

    public void setUsuario(String usuario) {
	this.usuario = usuario;
    }

    public InformacionCarreraDTO getInformacionCarrera() {
	return informacionCarrera;
    }

    public void setInformacionCarrera(InformacionCarreraDTO informacionCarrera) {
	this.informacionCarrera = informacionCarrera;
    }

    public InformacionIesDTO getInformacionIesDTO() {
	return informacionIesDTO;
    }

    public void setInformacionIesDTO(InformacionIesDTO informacionIesDTO) {
	this.informacionIesDTO = informacionIesDTO;
    }

    public ProcesoDTO getProcesoSeleccionado() {
	return procesoSeleccionado;
    }

    public void setProcesoSeleccionado(ProcesoDTO procesoSeleccionado) {
	this.procesoSeleccionado = procesoSeleccionado;
    }

    public List<ProcesoDTO> getProcesosEvaluacion() {
	return procesosEvaluacion;
    }

    public void setProcesosEvaluacion(List<ProcesoDTO> procesosEvaluacion) {
	this.procesosEvaluacion = procesosEvaluacion;
    }

}