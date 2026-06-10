package com.example.AgroGestao.controller;

import com.example.AgroGestao.model.Gasto;
import com.example.AgroGestao.model.Propriedade;
import com.example.AgroGestao.repository.GastoRepository;
import com.example.AgroGestao.repository.PropriedadeRepository;
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

    @GetMapping
    public String exibirGastos(@RequestParam(name = "propriedadeId", required = false) Long propriedadeId, Model model) {

        List<Gasto> todosGastos = gastoRepository.findAll();
        List<Gasto> gastosFiltrados = new ArrayList<>();
        double total = 0.0;

        model.addAttribute("propriedades", propriedadeRepository.findAll());

        if (propriedadeId != null) {
            Propriedade prop = propriedadeRepository.findById(propriedadeId).orElse(null);
            if (prop != null) {
                model.addAttribute("propSelecionada", prop);

                if (todosGastos != null) {
                    for (Gasto g : todosGastos) {
                        if (g != null && g.getPropriedade() != null && propriedadeId.equals(g.getPropriedade().getId())) {
                            gastosFiltrados.add(g);
                            if (g.getValor() != null) {
                                total += g.getValor();
                            }
                        }
                    }
                }
                model.addAttribute("gastos", gastosFiltrados);
            } else {
                model.addAttribute("propSelecionada", null);
                model.addAttribute("gastos", todosGastos);
                total = obterSomaGlobal(todosGastos);
            }
        } else {
            model.addAttribute("propSelecionada", null);
            model.addAttribute("gastos", todosGastos);
            total = obterSomaGlobal(todosGastos);
        }

        model.addAttribute("totalGasto", total);

        if (!model.containsAttribute("novoGasto")) {
            model.addAttribute("novoGasto", new Gasto());
        }
        return "gastos";
    }

    // 🟢 CORREÇÃO: Garante o resgate físico do ID da propriedade para não gerar registros "Sem vínculo"
    @PostMapping("/salvar")
    public String salvarGasto(@ModelAttribute Gasto gasto) {
        if (gasto.getPropriedade() != null && gasto.getPropriedade().getId() != null) {
            Propriedade prop = propriedadeRepository.findById(gasto.getPropriedade().getId()).orElse(null);
            gasto.setPropriedade(prop);
        }
        gastoRepository.save(gasto);
        return "redirect:/gastos-view";
    }

    @GetMapping("/editar/{id}")
    public String editarGasto(@PathVariable Long id, Model model) {
        Gasto gasto = gastoRepository.findById(id).orElse(null);
        if (gasto != null) {
            List<Gasto> lista = gastoRepository.findAll();
            double total = obterSomaGlobal(lista);

            model.addAttribute("novoGasto", gasto);
            model.addAttribute("gastos", lista);
            model.addAttribute("propriedades", propriedadeRepository.findAll());
            model.addAttribute("propSelecionada", null);
            model.addAttribute("totalGasto", total);
            return "gastos";
        }
        return "redirect:/gastos-view";
    }

    @GetMapping("/excluir/{id}")
    public String excluirGasto(@PathVariable Long id) {
        gastoRepository.deleteById(id);
        return "redirect:/gastos-view";
    }

    private double obterSomaGlobal(List<Gasto> lista) {
        double total = 0.0;
        if (lista != null) {
            for (Gasto g : lista) {
                if (g != null && g.getValor() != null) {
                    total += g.getValor();
                }
            }
        }
        return total;
    }
}