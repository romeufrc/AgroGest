package com.example.AgroGestao.controller;

import com.example.AgroGestao.model.Gasto;
import com.example.AgroGestao.repository.GastoRepository;
import com.example.AgroGestao.repository.PropriedadeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/gastos-view")
public class GastoController {

    @Autowired
    private GastoRepository gastoRepository;

    @Autowired
    private PropriedadeRepository propriedadeRepository;

    // Exibe a tela financeira, calcula o total acumulado e lista as fazendas
    @GetMapping
    public String exibirGastos(Model model) {
        // Soma o valor de todos os gastos lançados
        double total = gastoRepository.findAll().stream().mapToDouble(Gasto::getValor).sum();

        model.addAttribute("gastos", gastoRepository.findAll());
        model.addAttribute("propriedades", propriedadeRepository.findAll()); // Carrega o select de fazendas
        model.addAttribute("totalGasto", String.format("%.2f", total)); // Envia o total formatado

        // Se nao estiver editando, envia um gasto vazio para o form
        if (!model.containsAttribute("novoGasto")) {
            model.addAttribute("novoGasto", new Gasto());
        }
        return "gastos";
    }

    // Salva uma nova despesa ou atualiza uma existente
    @PostMapping("/salvar")
    public String salvarGasto(@ModelAttribute Gasto gasto) {
        gastoRepository.save(gasto);
        return "redirect:/gastos-view";
    }

    // Busca a despesa pelo ID e joga de volta no form para editar
    @GetMapping("/editar/{id}")
    public String editarGasto(@PathVariable Long id, Model model) {
        Gasto gasto = gastoRepository.findById(id).orElse(null);
        if (gasto != null) {
            double total = gastoRepository.findAll().stream().mapToDouble(Gasto::getValor).sum();

            model.addAttribute("novoGasto", gasto); // Coloca a despesa no form
            model.addAttribute("gastos", gastoRepository.findAll());
            model.addAttribute("propriedades", propriedadeRepository.findAll());
            model.addAttribute("totalGasto", String.format("%.2f", total));
            return "gastos";
        }
        return "redirect:/gastos-view";
    }

    // Exclui a despesa usando o ID
    @GetMapping("/excluir/{id}")
    public String excluirGasto(@PathVariable Long id) {
        gastoRepository.deleteById(id);
        return "redirect:/gastos-view";
    }
}