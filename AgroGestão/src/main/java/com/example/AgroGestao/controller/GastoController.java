package com.example.AgroGestao.controller;

import com.example.AgroGestao.model.Gasto;
import com.example.AgroGestao.model.Propriedade;
import com.example.AgroGestao.repository.GastoRepository;
import com.example.AgroGestao.repository.PropriedadeRepository;
import com.example.AgroGestao.service.GastoService; // Importei a minha classe Service do módulo financeiro
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
    private GastoService gastoService; // Injetei o Service para garantir que nenhum gasto zerado passe

    //Método para listar os gastos na tela e calcular o total
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

    //Garante o resgate físico do ID da propriedade para não gerar registros "Sem vínculo"
    @PostMapping("/salvar")
    public String salvarGasto(@ModelAttribute Gasto gasto) {
        //Vinculo a despesa à fazenda correta antes de validar
        if (gasto.getPropriedade() != null && gasto.getPropriedade().getId() != null) {
            Propriedade prop = propriedadeRepository.findById(gasto.getPropriedade().getId()).orElse(null);
            gasto.setPropriedade(prop);
        }

        //AJUSTE DE ARQUITETURA: Em vez de salvar direto no repository, passo a bola para o Service.
        //Se o valor for negativo ou zero, a RegraNegocioException é lançada e o log é gravado.
        gastoService.registrarGasto(gasto);

        return "redirect:/gastos-view";
    }

    // Carrega os dados da despesa na tela para edição
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

    // Deleta a despesa pelo ID
    @GetMapping("/excluir/{id}")
    public String excluirGasto(@PathVariable Long id) {
        gastoRepository.deleteById(id);
        return "redirect:/gastos-view";
    }

    //Método auxiliar para somar todos os gastos e mostrar no card da tela
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