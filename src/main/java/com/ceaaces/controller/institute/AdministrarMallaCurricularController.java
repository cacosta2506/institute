package ec.gob.ceaaces.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.primefaces.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;

import ec.gob.ceaaces.institutos.dtos.evaluacion.AsignaturaDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.InformacionCarreraDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.MallaCurricularDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.OrganizacionCurricularDTO;
import ec.gob.ceaaces.institutos.dtos.evaluacion.RequisitoAsignaturaDTO;
import ec.gob.ceaaces.services.CatalogoServicio;
import ec.gob.ceaaces.services.RegistroServicio;

public class AdministrarMallaCurricularController implements Serializable {

    /**
     * @author eviscarra
     */
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger
	    .getLogger(AdministrarMallaCurricularController.class
	            .getSimpleName());

    @Autowired
    private RegistroServicio registroServicio;

    @Autowired
    private CatalogoServicio catalogoServicio;

    private List<AsignaturaDTO> listaAsignaturas;
    private OrganizacionCurricularDTO organizacionCurricular;
    private RequisitoAsignaturaDTO requisitoMalla;
    private Long idAsignatura;
    private List<MallaCurricularDTO> listaMallaCurricular;
    private Long idMalla;
    private AsignaturaDTO asignaturaSeleccionada;

    public AdministrarMallaCurricularController() {
	this.listaAsignaturas = new ArrayList<AsignaturaDTO>();
	this.organizacionCurricular = new OrganizacionCurricularDTO();
	this.requisitoMalla = new RequisitoAsignaturaDTO();
	this.listaMallaCurricular = new ArrayList<MallaCurricularDTO>();
	this.asignaturaSeleccionada = new AsignaturaDTO();
    }

    @PostConstruct
    public void start() {
	LOG.info("POST CONSTRUCT");
	listaAsignaturas.clear();
	listaMallaCurricular.clear();
	InformacionCarreraDTO ic = new InformacionCarreraDTO();
	ic.setId(6366L);
	try {
	    listaMallaCurricular = registroServicio.obtenerMallaCurricular(ic);
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void consultarAsignaturas() {
	LOG.info("METODO consultarAsignaturas");
	LOG.info("ID MALLA: " + getIdMalla());
	listaAsignaturas.clear();
	for (int i = 0; i < listaMallaCurricular.size(); i++) {
	    LOG.info("listaMallaCurricular.get(i).getId()"
		    + listaMallaCurricular.get(i).getId());
	    if (listaMallaCurricular.get(i).getId().equals(getIdMalla())) {
		listaAsignaturas = listaMallaCurricular.get(i)
		        .getAsignaturasDTO();
		LOG.info("TAMA��O: " + listaAsignaturas.size());
	    }
	}
    }

    public void informacionAsignaturaSelec() {
	LOG.info("METODO informacionAsignaturaSelec");
	LOG.info("getIdAsignatura: " + getIdAsignatura());
	LOG.info("this.idAsignatura: " + this.idAsignatura);

	RequestContext context = RequestContext.getCurrentInstance();
	// if (this.idAsignatura != null) {
	try {
	    asignaturaSeleccionada = registroServicio
		    .obtenerMallaCurricularDetalle(16L);
	    LOG.info("LISTA1: "
		    + asignaturaSeleccionada.getPreRequisitoMalla().size());
	    LOG.info("LISTA2: "
		    + asignaturaSeleccionada.getCorRequisitoMalla().size());

	    context.execute("dlgInfAsig.show();");
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	// }

    }

    public void nuevaInformacionCarreraSede() {

    }

    public void editarInformacionCarreraSede() {

    }

    public void eliminarInformacionCarreraSede() {

    }

    public void guardarInformacionCarreraSede() {

    }

    public void salirInformacionCarreraSedeDetalle() {

    }

    public List<AsignaturaDTO> getListaAsignaturas() {
	return listaAsignaturas;
    }

    public void setListaAsignaturas(List<AsignaturaDTO> listaAsignaturas) {
	this.listaAsignaturas = listaAsignaturas;
    }

    public OrganizacionCurricularDTO getOrganizacionCurricular() {
	return organizacionCurricular;
    }

    public void setOrganizacionCurricular(
	    OrganizacionCurricularDTO organizacionCurricular) {
	this.organizacionCurricular = organizacionCurricular;
    }

    public RequisitoAsignaturaDTO getRequisitoMalla() {
	return requisitoMalla;
    }

    public void setRequisitoMalla(RequisitoAsignaturaDTO requisitoMalla) {
	this.requisitoMalla = requisitoMalla;
    }

    public List<MallaCurricularDTO> getListaMallaCurricular() {
	return listaMallaCurricular;
    }

    public void setListaMallaCurricular(
	    List<MallaCurricularDTO> listaMallaCurricular) {
	this.listaMallaCurricular = listaMallaCurricular;
    }

    public Long getIdMalla() {
	return idMalla;
    }

    public void setIdMalla(Long idMalla) {
	this.idMalla = idMalla;
    }

    public Long getIdAsignatura() {
	return idAsignatura;
    }

    public void setIdAsignatura(Long idAsignatura) {
	this.idAsignatura = idAsignatura;
    }

    public AsignaturaDTO getAsignaturaSeleccionada() {
	return asignaturaSeleccionada;
    }

    public void setAsignaturaSeleccionada(AsignaturaDTO asignaturaSeleccionada) {
	this.asignaturaSeleccionada = asignaturaSeleccionada;
    }
}
