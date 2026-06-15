package com.example.AgroGestao.controller;

import com.example.AgroGestao.model.Gasto;
import com.example.AgroGestao.model.Propriedade;
import com.example.AgroGestao.model.Usuario;
import com.example.AgroGestao.repository.GastoRepository;
import com.example.AgroGestao.repository.PropriedadeRepository;
import com.example.AgroGestao.service.GastoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/gastos-view")
public class GastoController {

    @Autowired
    private GastoRepository gastoRepository;

    @Autowired
    private PropriedadeRepository propriedadeRepository;

    @Autowired
    private GastoService gastoService;

    // Meu controlador de interface para a parte financeira. Tudo eh isolado pelo ID da sessao.
    @GetMapping
    public String exibirGastos(@RequestParam(name = "propriedadeId", required = false) Long propriedadeId, Model model, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) return "redirect:/login";

        // Carrego as propriedades relativas apenas a mim para o formulario
        List<Propriedade> minhasPropriedades = new ArrayList<>();
        for (Propriedade p : propriedadeRepository.findAll()) {
            if (p.getUsuario() != null && p.getUsuario().getId().equals(usuarioLogado.getId())) {
                minhasPropriedades.add(p);
            }
        }
        model.addAttribute("propriedades", minhasPropriedades);

        List<Gasto> meusGastos = new ArrayList<>();
        double total = 0.0;

        // Calculo o total de despesas somando apenas os meus registros
        for (Gasto g : gastoRepository.findAll()) {
            if (g.getPropriedade() != null && g.getPropriedade().getUsuario() != null && g.getPropriedade().getUsuario().getId().equals(usuarioLogado.getId())) {
                if (propriedadeId != null) {
                    if (propriedadeId.equals(g.getPropriedade().getId())) {
                        meusGastos.add(g);
                        if (g.getValor() != null) total += g.getValor();
                    }
                } else {
                    meusGastos.add(g);
                    if (g.getValor() != null) total += g.getValor();
                }
            }
        }
        model.addAttribute("gastos", meusGastos);
        model.addAttribute("totalGasto", total);

        if (propriedadeId != null) {
            Propriedade p = propriedadeRepository.findById(propriedadeId).orElse(null);
            if (p != null && p.getUsuario() != null && p.getUsuario().getId().equals(usuarioLogado.getId())) {
                model.addAttribute("propSelecionada", p);
            } else {
                return "redirect:/gastos-view";
            }
        } else {
            model.addAttribute("propSelecionada", null);
        }

        if (!model.containsAttribute("novoGasto")) {
            model.addAttribute("novoGasto", new Gasto());
        }
        return "gastos";
    }

    // Processamento do salvamento de contas passando pela validacao de usuario
    @PostMapping("/salvar")
    public String salvarGasto(@ModelAttribute Gasto gasto, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) return "redirect:/login";

        if (gasto.getPropriedade() != null && gasto.getPropriedade().getId() != null) {
            Propriedade prop = propriedadeRepository.findById(gasto.getPropriedade().getId()).orElse(null);
            if (prop != null && prop.getUsuario() != null && prop.getUsuario().getId().equals(usuarioLogado.getId())) {
                gasto.setPropriedade(prop);
            } else {
                return "redirect:/gastos-view";
            }
        }

        // Meu servico cuida de negar valores negativos
        gastoService.registrarGasto(gasto);
        return "redirect:/gastos-view";
    }

    // Busco os dados de uma despesa garantindo o isolamento
    @GetMapping("/editar/{id}")
    public String editarGasto(@PathVariable Long id, Model model, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) return "redirect:/login";

        Gasto gasto = gastoRepository.findById(id).orElse(null);
        if (gasto != null && gasto.getPropriedade() != null && gasto.getPropriedade().getUsuario() != null && gasto.getPropriedade().getUsuario().getId().equals(usuarioLogado.getId())) {

            List<Propriedade> minhasProp = new ArrayList<>();
            for (Propriedade p : propriedadeRepository.findAll()) {
                if (p.getUsuario() != null && p.getUsuario().getId().equals(usuarioLogado.getId())) minhasProp.add(p);
            }

            List<Gasto> meusGastos = new ArrayList<>();
            double total = 0.0;
            for (Gasto g : gastoRepository.findAll()) {
                if (g.getPropriedade() != null && g.getPropriedade().getUsuario() != null && g.getPropriedade().getUsuario().getId().equals(usuarioLogado.getId())) {
                    meusGastos.add(g);
                    if (g.getValor() != null) total += g.getValor();
                }
            }

            model.addAttribute("novoGasto", gasto);
            model.addAttribute("gastos", meusGastos);
            model.addAttribute("propriedades", minhasProp);
            model.addAttribute("propSelecionada", null);
            model.addAttribute("totalGasto", total);
            return "gastos";
        }
        return "redirect:/gastos-view";
    }

    // Exclusao bloqueada para terceiros
    @GetMapping("/excluir/{id}")
    public String excluirGasto(@PathVariable Long id, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) return "redirect:/login";

        Gasto gasto = gastoRepository.findById(id).orElse(null);
        if (gasto != null && gasto.getPropriedade() != null && gasto.getPropriedade().getUsuario() != null && gasto.getPropriedade().getUsuario().getId().equals(usuarioLogado.getId())) {
            gastoRepository.deleteById(id);
        }
        return "redirect:/gastos-view";
    }
}