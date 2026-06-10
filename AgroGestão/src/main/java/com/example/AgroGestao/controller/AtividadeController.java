package com.example.AgroGestao.controller;

import com.example.AgroGestao.model.Atividade;
import com.example.AgroGestao.model.Propriedade;
import com.example.AgroGestao.repository.AtividadeRepository;
import com.example.AgroGestao.repository.PropriedadeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/atividades-view")
public class AtividadeController {

    @Autowired
    private AtividadeRepository atividadeRepository;

    @Autowired
    private PropriedadeRepository propriedadeRepository;

    @GetMapping
    public String exibirAtividades(@RequestParam(name = "propriedadeId", required = false) Long propriedadeId, Model model) {

        List<Atividade> todasAtividades = atividadeRepository.findAll();
        List<Atividade> atividadesFiltradas = new ArrayList<>();

        model.addAttribute("propriedades", propriedadeRepository.findAll());

        if (propriedadeId != null) {
            Propriedade prop = propriedadeRepository.findById(propriedadeId).orElse(null);
            if (prop != null) {
                model.addAttribute("propSelecionada", prop);

                if (todasAtividades != null) {
                    for (Atividade a : todasAtividades) {
                        if (a != null && a.getPropriedade() != null && propriedadeId.equals(gethId(a.getPropriedade()))) {
                            atividadesFiltradas.add(a);
                        }
                    }
                }
                model.addAttribute("atividades", atividadesFiltradas);
            } else {
                model.addAttribute("propSelecionada", null);
                model.addAttribute("atividades", todasAtividades);
            }
        } else {
            model.addAttribute("propSelecionada", null);
            model.addAttribute("atividades", todasAtividades);
        }

        if (!model.containsAttribute("novaAtividade")) {
            model.addAttribute("novaAtividade", new Atividade());
        }
        return "atividades";
    }

    @PostMapping("/salvar")
    public String salvarAtividade(@ModelAttribute Atividade atividade) {
        if (atividade.getPropriedade() != null && atividade.getPropriedade().getId() != null) {
            Propriedade prop = propriedadeRepository.findById(atividade.getPropriedade().getId()).orElse(null);
            atividade.setPropriedade(prop);
        }
        // Define o tipo com base no nome ou descrição se o seu banco exigir a coluna preenchida
        if (atividade.getTipo() == null || atividade.getTipo().isEmpty()) {
            atividade.setTipo("Campo");
        }
        atividadeRepository.save(atividade);
        return "redirect:/atividades-view";
    }

    @GetMapping("/editar/{id}")
    public String editarAtividade(@PathVariable Long id, Model model) {
        Atividade atividade = atividadeRepository.findById(id).orElse(null);
        if (atividade != null) {
            model.addAttribute("novaAtividade", atividade);
            model.addAttribute("atividades", atividadeRepository.findAll());
            model.addAttribute("propriedades", propriedadeRepository.findAll());
            model.addAttribute("propSelecionada", null);
            return "atividades";
        }
        return "redirect:/atividades-view";
    }

    @GetMapping("/excluir/{id}")
    public String excluirAtividade(@PathVariable Long id) {
        atividadeRepository.deleteById(id);
        return "redirect:/atividades-view";
    }

    private Long gethId(Propriedade p) {
        return p != null ? p.getId() : null;
    }
}