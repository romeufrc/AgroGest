package com.example.AgroGestao.controller;

import com.example.AgroGestao.model.Insumos;
import com.example.AgroGestao.model.Propriedade;
import com.example.AgroGestao.repository.InsumosRepository;
import com.example.AgroGestao.repository.PropriedadeRepository;
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

    @GetMapping
    public String exibirInsumos(@RequestParam(name = "propriedadeId", required = false) Long propriedadeId, Model model) {
        model.addAttribute("propriedades", propriedadeRepository.findAll());

        List<Insumos> todosInsumos = insumosRepository.findAll();
        List<Insumos> filtrados = new ArrayList<>();

        if (propriedadeId != null) {
            Propriedade p = propriedadeRepository.findById(propriedadeId).orElse(null);
            if (p != null) {
                model.addAttribute("propSelecionada", p);
                for (Insumos ins : todosInsumos) {
                    if (ins.getPropriedade() != null && propriedadeId.equals(ins.getPropriedade().getId())) {
                        filtrados.add(ins);
                    }
                }
                model.addAttribute("insumos", filtrados);
            } else {
                model.addAttribute("insumos", todosInsumos);
            }
        } else {
            model.addAttribute("propSelecionada", null);
            model.addAttribute("insumos", todosInsumos);
        }

        if (!model.containsAttribute("novoInsumo")) {
            model.addAttribute("novoInsumo", new Insumos());
        }
        return "insumos";
    }

    @PostMapping("/salvar")
    public String salvarInsumo(@ModelAttribute Insumos insumo) {
        insumosRepository.save(insumo);
        return "redirect:/insumos-view";
    }

    @GetMapping("/editar/{id}")
    public String editarInsumo(@PathVariable Long id, Model model) {
        Insumos insumo = insumosRepository.findById(id).orElse(null);
        if (insumo != null) {
            model.addAttribute("novoInsumo", insumo);
            model.addAttribute("insumos", insumosRepository.findAll());
            // CORRIGIDO: Removido o ".repository" duplicado que quebrava o fluxo
            model.addAttribute("propriedades", propriedadeRepository.findAll());
            return "insumos";
        }
        return "redirect:/insumos-view";
    }

    @GetMapping("/excluir/{id}")
    public String excluirInsumo(@PathVariable Long id) {
        insumosRepository.deleteById(id);
        return "redirect:/insumos-view";
    }
}