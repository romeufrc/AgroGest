package com.example.AgroGestao.controller;

import com.example.AgroGestao.model.Atividade;
import com.example.AgroGestao.model.Insumos;
import com.example.AgroGestao.model.Propriedade;
import com.example.AgroGestao.model.Gasto;
import com.example.AgroGestao.repository.AtividadeRepository;
import com.example.AgroGestao.repository.InsumosRepository;
import com.example.AgroGestao.repository.PropriedadeRepository;
import com.example.AgroGestao.repository.GastoRepository;
import com.example.AgroGestao.service.AtividadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private PropriedadeRepository propriedadeRepository;

    @Autowired
    private AtividadeRepository atividadeRepository;

    @Autowired
    private InsumosRepository insumosRepository;

    @Autowired
    private GastoRepository gastoRepository;

    @Autowired
    private AtividadeService atividadeService;

    // ==========================================
    // 1. DASHBOARD PRINCIPAL (AGORA DINÂMICO!)
    // ==========================================
    @GetMapping("/")
    public String exibirDashboard(Model model) {
        // Coleta de dados estatísticos do banco
        long qtdPropriedades = propriedadeRepository.count();
        long qtdAtividades = atividadeRepository.count();
        long qtdInsumos = insumosRepository.count();

        List<Gasto> listaGastos = gastoRepository.findAll();
        double totalGasto = 0.0;
        if (listaGastos != null) {
            for (Gasto g : listaGastos) {
                if (g.getValor() != null) {
                    totalGasto += g.getValor();
                }
            }
        }

        // Enviando os contadores para o HTML
        model.addAttribute("qtdPropriedades", qtdPropriedades);
        model.addAttribute("qtdAtividades", qtdAtividades);
        model.addAttribute("qtdInsumos", qtdInsumos);
        model.addAttribute("totalGasto", totalGasto);

        // Pega a lista completa para o histórico rápido
        model.addAttribute("atividadesRecentes", atividadeRepository.findAll());

        return "index";
    }

    // ==========================================
    // 2. MÓDULO DE PROPRIEDADES (FAZENDAS)
    // ==========================================
    @GetMapping("/propriedades-view")
    public String exibirPropriedades(Model model) {
        model.addAttribute("propriedades", propriedadeRepository.findAll());
        model.addAttribute("novaPropriedade", new Propriedade());
        return "propriedades";
    }

    @PostMapping("/propriedades-view/salvar")
    public String salvarNovaPropriedade(@ModelAttribute Propriedade propriedade) {
        propriedadeRepository.save(propriedade);
        return "redirect:/propriedades-view";
    }

    // ==========================================
    // 3. MÓDULO DE ATIVIDADES DE CAMPO
    // ==========================================
    @GetMapping("/atividades-view")
    public String exibirAtividades(Model model) {
        model.addAttribute("atividades", atividadeRepository.findAll());
        model.addAttribute("propriedades", propriedadeRepository.findAll());
        model.addAttribute("novaAtividade", new Atividade());
        return "atividades";
    }

    @PostMapping("/atividades-view/salvar")
    public String salvarNovaAtividade(@ModelAttribute Atividade atividade,
                                      @RequestParam(value = "tipo", required = false) String tipoPersonalizado) {
        if (tipoPersonalizado != null && !tipoPersonalizado.trim().isEmpty()) {
            atividade.setTipo(tipoPersonalizado);
        }
        atividadeService.salvarEAtualizarEstoque(atividade);
        return "redirect:/atividades-view";
    }

    @GetMapping("/atividades-view/excluir/{id}")
    public String excluirAtividadeVisual(@PathVariable Long id) {
        atividadeRepository.deleteById(id);
        return "redirect:/atividades-view";
    }

    // ==========================================
    // 4. MÓDULO DE ESTOQUE (INSUMOS)
    // ==========================================
    @GetMapping("/insumos-view")
    public String exibirInsumos(Model model) {
        model.addAttribute("insumos", insumosRepository.findAll());
        model.addAttribute("novoInsumo", new Insumos());
        return "insumos";
    }

    @PostMapping("/insumos-view/salvar")
    public String salvarNovoInsumo(@ModelAttribute Insumos insumo) {
        insumosRepository.save(insumo);
        return "redirect:/insumos-view";
    }

    @GetMapping("/insumos-view/excluir/{id}")
    public String excluirInsumoVisual(@PathVariable Long id) {
        insumosRepository.deleteById(id);
        return "redirect:/insumos-view";
    }

    // ==========================================
    // 5. MÓDULO FINANCEIRO (GASTOS)
    // ==========================================
    @GetMapping("/gastos-view")
    public String exibirGastos(Model model) {
        List<Gasto> listaGastos = gastoRepository.findAll();

        double totalGasto = 0.0;
        if (listaGastos != null) {
            for (Gasto g : listaGastos) {
                if (g.getValor() != null) {
                    totalGasto += g.getValor();
                }
            }
        }

        model.addAttribute("gastos", listaGastos);
        model.addAttribute("propriedades", propriedadeRepository.findAll());
        model.addAttribute("novoGasto", new Gasto());
        model.addAttribute("totalGasto", totalGasto);
        return "gastos";
    }

    @PostMapping("/gastos-view/salvar")
    public String salvarNovoGasto(@ModelAttribute Gasto gasto) {
        gastoRepository.save(gasto);
        return "redirect:/gastos-view";
    }

    @GetMapping("/gastos-view/excluir/{id}")
    public String excluirGastoVisual(@PathVariable Long id) {
        gastoRepository.deleteById(id);
        return "redirect:/gastos-view";
    }
}