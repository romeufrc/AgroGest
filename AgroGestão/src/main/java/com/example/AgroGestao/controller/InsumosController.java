package com.example.AgroGestao.controller;

import com.example.AgroGestao.model.Insumos;
import com.example.AgroGestao.model.Propriedade;
import com.example.AgroGestao.model.Usuario;
import com.example.AgroGestao.repository.InsumosRepository;
import com.example.AgroGestao.repository.PropriedadeRepository;
import com.example.AgroGestao.service.InsumoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/insumos-view")
public class InsumosController {

    @Autowired
    private InsumosRepository insumosRepository;

    @Autowired
    private PropriedadeRepository propriedadeRepository;

    @Autowired
    private InsumoService insumoService;

    // Meu metodo que traz o estoque da conta atual bloqueando acesso a dados alheios
    @GetMapping
    public String exibirInsumos(@RequestParam(name = "propriedadeId", required = false) Long propriedadeId, Model model, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) return "redirect:/login";

        List<Propriedade> minhasPropriedades = new ArrayList<>();
        for (Propriedade p : propriedadeRepository.findAll()) {
            if (p.getUsuario() != null && p.getUsuario().getId().equals(usuarioLogado.getId())) {
                minhasPropriedades.add(p);
            }
        }
        model.addAttribute("propriedades", minhasPropriedades);

        List<Insumos> meusInsumos = new ArrayList<>();
        for (Insumos ins : insumosRepository.findAll()) {
            if (ins.getPropriedade() != null && ins.getPropriedade().getUsuario() != null && ins.getPropriedade().getUsuario().getId().equals(usuarioLogado.getId())) {
                if (propriedadeId != null) {
                    if (propriedadeId.equals(ins.getPropriedade().getId())) meusInsumos.add(ins);
                } else {
                    meusInsumos.add(ins);
                }
            }
        }
        model.addAttribute("insumos", meusInsumos);

        if (propriedadeId != null) {
            Propriedade p = propriedadeRepository.findById(propriedadeId).orElse(null);
            if (p != null && p.getUsuario() != null && p.getUsuario().getId().equals(usuarioLogado.getId())) {
                model.addAttribute("propSelecionada", p);
            } else {
                return "redirect:/insumos-view";
            }
        } else {
            model.addAttribute("propSelecionada", null);
        }

        if (!model.containsAttribute("novoInsumo")) {
            model.addAttribute("novoInsumo", new Insumos());
        }
        return "insumos";
    }

    // Faco a validacao de dono antes de acionar a deducao logistica do Service
    @PostMapping("/salvar")
    public String salvarInsumo(@ModelAttribute Insumos insumo, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) return "redirect:/login";

        if (insumo.getPropriedade() != null && insumo.getPropriedade().getId() != null) {
            Propriedade prop = propriedadeRepository.findById(insumo.getPropriedade().getId()).orElse(null);
            if (prop != null && prop.getUsuario() != null && prop.getUsuario().getId().equals(usuarioLogado.getId())) {
                insumo.setPropriedade(prop);
            } else {
                return "redirect:/insumos-view";
            }
        }

        insumoService.salvarInsumo(insumo);
        return "redirect:/insumos-view";
    }

    // Carrego o insumo para o formulario protegendo pela sessao
    @GetMapping("/editar/{id}")
    public String editarInsumo(@PathVariable Long id, Model model, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) return "redirect:/login";

        Insumos insumo = insumosRepository.findById(id).orElse(null);
        if (insumo != null && insumo.getPropriedade() != null && insumo.getPropriedade().getUsuario() != null && insumo.getPropriedade().getUsuario().getId().equals(usuarioLogado.getId())) {

            List<Propriedade> minhasProp = new ArrayList<>();
            for (Propriedade p : propriedadeRepository.findAll()) {
                if (p.getUsuario() != null && p.getUsuario().getId().equals(usuarioLogado.getId())) minhasProp.add(p);
            }

            List<Insumos> meusInsumos = new ArrayList<>();
            for (Insumos i : insumosRepository.findAll()) {
                if (i.getPropriedade() != null && i.getPropriedade().getUsuario() != null && i.getPropriedade().getUsuario().getId().equals(usuarioLogado.getId())) meusInsumos.add(i);
            }

            model.addAttribute("novoInsumo", insumo);
            model.addAttribute("insumos", meusInsumos);
            model.addAttribute("propriedades", minhasProp);
            return "insumos";
        }
        return "redirect:/insumos-view";
    }

    // Exclusao de item do estoque bloqueada para terceiros
    @GetMapping("/excluir/{id}")
    public String excluirInsumo(@PathVariable Long id, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) return "redirect:/login";

        Insumos insumo = insumosRepository.findById(id).orElse(null);
        if (insumo != null && insumo.getPropriedade() != null && insumo.getPropriedade().getUsuario() != null && insumo.getPropriedade().getUsuario().getId().equals(usuarioLogado.getId())) {
            insumoService.deletarInsumo(id);
        }
        return "redirect:/insumos-view";
    }
}