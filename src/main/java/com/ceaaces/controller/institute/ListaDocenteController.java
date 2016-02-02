package ec.gob.ceaaces.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

import org.springframework.beans.factory.annotation.Autowired;

import ec.gob.ceaaces.institutos.dtos.evaluacion.DocenteDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionCarreraDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionIesDTO;
import ec.gob.ceaaces.services.RegistroServicio;
import ec.gob.ceaaces.util.JsfUtil;

public class ListaDocenteController implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 7344054589239991373L;

    @Autowired
    private RegistroServicio registroServicio;

    private InformacionIesDTO informacionIesDTO;
    private InformacionCarreraDTO informacionCarreraDTO;
    // DOCENTES
    private int indice;
    private int registros = 10;
    private int indiceAtras;
    private int indiceSiguiente;
    private int numRegistros;
    private boolean busqueda;
    private String identificacion;
    private List<DocenteDTO> listaDocentesTodos;
    private List<DocenteDTO> docentes;
    private List<DocenteDTO> docentesFiltros;
    private static final Integer[] rangos = { 10, 20, 50, 100, 200 };
    private int contador;
    private boolean habilitarSiguiente;
    private String opcion;

    public ListaDocenteController() {
	this.habilitarSiguiente = true;
	this.listaDocentesTodos = new ArrayList<DocenteDTO>();
	this.docentes = new ArrayList<DocenteDTO>();
	this.docentesFiltros = new ArrayList<DocenteDTO>();

    }

    public void cargarDocentes() {
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

		this.listaDocentesTodos.addAll(registroServicio
		        .obtenerDocentesPorInformacionIes(docenteDTO,
		                informacionIesDTO.getId()));

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
		    this.docentes = auxLista
			    .subList(indice, indice + registros);
		    this.docentesFiltros.addAll(docentes);
		}
	    }
	    if (docentes.size() < registros) {
		habilitarSiguiente = false;
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    FacesContext.getCurrentInstance().addMessage(
		    null,
		    new FacesMessage(FacesMessage.SEVERITY_FATAL, null,
		            "Se produjo un error al listar docentes"));
	}

    }

    public void limpiarListas() {
	docentes.clear();
	docentesFiltros.clear();
	listaDocentesTodos.clear();
	indice = 0;
	registros = 10;
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

		List<DocenteDTO> aux = registroServicio
		        .obtenerDocentesPorInformacionIes(docenteDTO,
		                this.informacionIesDTO.getId());
		if (aux != null && !aux.isEmpty()) {
		    docenteDTO = aux.get(0);
		}

		if (docenteDTO != null) {
		    docentes.add(docenteDTO);
		    this.docentesFiltros.add(docenteDTO);
		    this.listaDocentesTodos.add(docenteDTO);

		    FacesContext.getCurrentInstance().addMessage(
			    null,
			    new FacesMessage(FacesMessage.SEVERITY_INFO,
			            "Docente encontrado", null));
		} else {
		    FacesContext.getCurrentInstance().addMessage(
			    null,
			    new FacesMessage(FacesMessage.SEVERITY_INFO,
			            "Docente no encontrado!", null));
		}

	    } catch (Exception e) {
		e.printStackTrace();
		FacesContext
		        .getCurrentInstance()
		        .addMessage(
		                null,
		                new FacesMessage(
		                        FacesMessage.SEVERITY_ERROR,
		                        "Falló búsqueda de docente por cédula!.",
		                        null));

	    }
	} else {
	    FacesContext.getCurrentInstance().addMessage(
		    null,
		    new FacesMessage(FacesMessage.SEVERITY_ERROR,
		            "Ingrese número de cédula!. ", null));
	}
	busqueda = true;
	habilitarSiguiente = true;
    }

    public InformacionIesDTO getInformacionIesDTO() {
	return informacionIesDTO;
    }

    public void setInformacionIesDTO(InformacionIesDTO informacionIesDTO) {
	this.informacionIesDTO = informacionIesDTO;
    }

    public InformacionCarreraDTO getInformacionCarreraDTO() {
	return informacionCarreraDTO;
    }

    public void setInformacionCarreraDTO(
	    InformacionCarreraDTO informacionCarreraDTO) {
	this.informacionCarreraDTO = informacionCarreraDTO;
    }

    public int getIndice() {
	return indice;
    }

    public void setIndice(int indice) {
	this.indice = indice;
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

    public boolean isBusqueda() {
	return busqueda;
    }

    public void setBusqueda(boolean busqueda) {
	this.busqueda = busqueda;
    }

    public List<DocenteDTO> getListaDocentesTodos() {
	return listaDocentesTodos;
    }

    public void setListaDocentesTodos(List<DocenteDTO> listaDocentesTodos) {
	this.listaDocentesTodos = listaDocentesTodos;
    }

    public List<DocenteDTO> getDocentes() {
	return docentes;
    }

    public void setDocentes(List<DocenteDTO> docentes) {
	this.docentes = docentes;
    }

    public List<DocenteDTO> getDocentesFiltros() {
	return docentesFiltros;
    }

    public void setDocentesFiltros(List<DocenteDTO> docentesFiltros) {
	this.docentesFiltros = docentesFiltros;
    }

    public int getContador() {
	return contador;
    }

    public void setContador(int contador) {
	this.contador = contador;
    }

    public boolean isHabilitarSiguiente() {
	return habilitarSiguiente;
    }

    public void setHabilitarSiguiente(boolean habilitarSiguiente) {
	this.habilitarSiguiente = habilitarSiguiente;
    }

    public int getRegistros() {
	return registros;
    }

    public Integer[] getRangos() {
	return rangos;
    }

    public void setRegistros(int registros) {
	this.registros = registros;
    }

    public String getOpcion() {
	return opcion;
    }

    public void setOpcion(String opcion) {
	ListaIesController controller = (ListaIesController) JsfUtil
	        .obtenerObjetoSesion("listaIesController");

	if (!opcion.equals(this.opcion)
	        || !controller.getIes().getId()
	                .equals(this.informacionIesDTO.getIes().getId())) {
	    limpiarListas();
	    cargarDocentes();
	    this.opcion = opcion;
	}
    }

    public String getIdentificacion() {
	return identificacion;
    }

    public void setIdentificacion(String identificacion) {
	this.identificacion = identificacion;
    }
}
