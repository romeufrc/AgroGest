package com.example.AgroGestao.controller;

import com.example.AgroGestao.model.Safra;
import com.example.AgroGestao.model.Propriedade;
import com.example.AgroGestao.model.Usuario;
import com.example.AgroGestao.repository.SafraRepository;
import com.example.AgroGestao.repository.PropriedadeRepository;
import com.example.AgroGestao.service.SafraService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/safras-view")
public class SafraController {

    @Autowired
    private SafraRepository safraRepository;

    @Autowired
    private PropriedadeRepository propriedadeRepository;

    @Autowired
    private SafraService safraService;

    // Meu metodo que traz as safras isoladas para o usuario
    @GetMapping
    public String exibirSafras(@RequestParam(name = "propriedadeId", required = false) Long propriedadeId, Model model, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) return "redirect:/login";

        List<Propriedade> minhasPropriedades = new ArrayList<>();
        for (Propriedade p : propriedadeRepository.findAll()) {
            if (p.getUsuario() != null && p.getUsuario().getId().equals(usuarioLogado.getId())) {
                minhasPropriedades.add(p);
            }
        }
        model.addAttribute("propriedades", minhasPropriedades);

        List<Safra> minhasSafras = new ArrayList<>();
        for (Safra s : safraRepository.findAll()) {
            if (s.getPropriedade() != null && s.getPropriedade().getUsuario() != null && s.getPropriedade().getUsuario().getId().equals(usuarioLogado.getId())) {
                if (propriedadeId != null) {
                    if (propriedadeId.equals(s.getPropriedade().getId())) minhasSafras.add(s);
                } else {
                    minhasSafras.add(s);
                }
            }
        }
        model.addAttribute("safras", minhasSafras);

        if (propriedadeId != null) {
            Propriedade p = propriedadeRepository.findById(propriedadeId).orElse(null);
            if (p != null && p.getUsuario() != null && p.getUsuario().getId().equals(usuarioLogado.getId())) {
                model.addAttribute("propSelecionada", p);
            } else {
                return "redirect:/safras-view";
            }
        } else {
            model.addAttribute("propSelecionada", null);
        }

        if (!model.containsAttribute("novaSafra")) {
            model.addAttribute("novaSafra", new Safra());
        }
        return "safra";
    }

    // Salvo o planejamento da colheita garantindo a propriedade e usando a regra do Service
    @PostMapping("/salvar")
    public String salvarSafra(@ModelAttribute Safra safra, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) return "redirect:/login";

        if (safra.getPropriedade() != null && safra.getPropriedade().getId() != null) {
            Propriedade prop = propriedadeRepository.findById(safra.getPropriedade().getId()).orElse(null);
            if (prop != null && prop.getUsuario() != null && prop.getUsuario().getId().equals(usuarioLogado.getId())) {
                safra.setPropriedade(prop);
            } else {
                return "redirect:/safras-view";
            }
        }

        safraService.salvarSafra(safra);
        return "redirect:/safras-view";
    }

    // Recupero uma safra especifica validando o ID da sessao
    @GetMapping("/editar/{id}")
    public String editarSafra(@PathVariable Long id, Model model, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) return "redirect:/login";

        Safra safra = safraRepository.findById(id).orElse(null);
        if (safra != null && safra.getPropriedade() != null && safra.getPropriedade().getUsuario() != null && safra.getPropriedade().getUsuario().getId().equals(usuarioLogado.getId())) {

            List<Propriedade> minhasProp = new ArrayList<>();
            for (Propriedade p : propriedadeRepository.findAll()) {
                if (p.getUsuario() != null && p.getUsuario().getId().equals(usuarioLogado.getId())) minhasProp.add(p);
            }

            List<Safra> minhasSafras = new ArrayList<>();
            for (Safra s : safraRepository.findAll()) {
                if (s.getPropriedade() != null && s.getPropriedade().getUsuario() != null && s.getPropriedade().getUsuario().getId().equals(usuarioLogado.getId())) minhasSafras.add(s);
            }

            model.addAttribute("novaSafra", safra);
            model.addAttribute("safras", minhasSafras);
            model.addAttribute("propriedades", minhasProp);
            return "safra";
        }
        return "redirect:/safras-view";
    }

    // Delecao segura passando pelo meu Service
    @GetMapping("/excluir/{id}")
    public String excluirSafra(@PathVariable Long id, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) return "redirect:/login";

        Safra safra = safraRepository.findById(id).orElse(null);
        if (safra != null && safra.getPropriedade() != null && safra.getPropriedade().getUsuario() != null && safra.getPropriedade().getUsuario().getId().equals(usuarioLogado.getId())) {
            safraService.deletarSafra(id);
        }
        return "redirect:/safras-view";
    }
}