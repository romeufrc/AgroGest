package com.example.AgroGestao.controller;

import com.example.AgroGestao.model.*;
import com.example.AgroGestao.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private SafraRepository safraRepository;

    @GetMapping("/")
    public String exibirDashboard(@RequestParam(name = "propriedadeId", required = false) Long id, Model model) {

        model.addAttribute("propriedades", propriedadeRepository.findAll());

        long qtdPropriedades = propriedadeRepository.count();
        long qtdAtividades = 0;
        long qtdInsumos = 0;
        long qtdSafras = 0;
        double totalGastoCalculado = 0.0;
        List<Atividade> atividadesRecentes = new ArrayList<>();
        List<Safra> safrasFiltradas = new ArrayList<>();

        boolean alertaOrcamentoEstourado = false;
        boolean alertaSemSafrasAtivas = false;
        boolean alertaColheitaProxima = false;
        boolean alertaEstoqueBaixo = false;
        boolean alertaSemInsumos = false;

        String nomeSafraUrgente = "";
        String itensEstoqueBaixo = "";
        double faturamentoProjetado = 0.0;

        // CENÁRIO A: Filtro por Fazenda Ativo
        if (id != null) {
            Propriedade p = propriedadeRepository.findById(id).orElse(null);
            if (p != null) {
                model.addAttribute("propSelecionada", p);

                // Se o usuário definiu uma margem na propriedade, usamos. Se não, o padrão é 10.
                int margemSegurancaEstoque = (p.getLimiteEstoqueBaixo() != null) ? p.getLimiteEstoqueBaixo() : 10;

                // Varredura de Atividades
                List<Atividade> todasAtiv = atividadeRepository.findAll();
                if (todasAtiv != null) {
                    for (Atividade a : todasAtiv) {
                        if (a != null && a.getPropriedade() != null && id.equals(a.getPropriedade().getId())) {
                            qtdAtividades++;
                            atividadesRecentes.add(a);
                        }
                    }
                }

                // Varredura Financeira
                List<Gasto> todosGastos = gastoRepository.findAll();
                if (todosGastos != null) {
                    for (Gasto g : todosGastos) {
                        if (g != null && g.getPropriedade() != null && id.equals(g.getPropriedade().getId()) && g.getValor() != null) {
                            totalGastoCalculado += g.getValor();
                        }
                    }
                }

                // Varredura Logística com a Nova Margem Dinâmica
                List<Insumos> todosInsumos = insumosRepository.findAll();
                if (todosInsumos != null) {
                    for (Insumos ins : todosInsumos) {
                        if (ins != null && ins.getPropriedade() != null && id.equals(ins.getPropriedade().getId())) {
                            qtdInsumos++;

                            // Validação reativa contra a margem definida pelo usuário
                            if (ins.getQuantidade() != null && ins.getQuantidade() < margemSegurancaEstoque) {
                                alertaEstoqueBaixo = true;
                                if (itensEstoqueBaixo.isEmpty()) {
                                    itensEstoqueBaixo = ins.getNome() + " (" + ins.getQuantidade() + " un)";
                                } else {
                                    itensEstoqueBaixo += ", " + ins.getNome() + " (" + ins.getQuantidade() + " un)";
                                }
                            }
                        }
                    }
                }

                // Varredura de Safras e Cálculo de Faturamento Real
                List<Safra> todasSafras = safraRepository.findAll();
                if (todasSafras != null) {
                    for (Safra s : todasSafras) {
                        if (s != null && s.getPropriedade() != null && id.equals(s.getPropriedade().getId())) {
                            qtdSafras++;
                            safrasFiltradas.add(s);

                            if (s.getProducaoEstimadaSacas() != null && s.getPrecoSacaEsperado() != null) {
                                faturamentoProjetado += (s.getProducaoEstimadaSacas() * s.getPrecoSacaEsperado());
                            }

                            if (s.getStatus() != null && (s.getStatus().equalsIgnoreCase("EM MATURAÇÃO") || s.getStatus().equalsIgnoreCase("MATURAÇÃO"))) {
                                alertaColheitaProxima = true;
                                nomeSafraUrgente = s.getNome() + " [" + s.getCultura() + "]";
                            }
                        }
                    }
                }

                // Validação Financeira
                if (p.getLimiteGasto() != null && totalGastoCalculado > p.getLimiteGasto()) {
                    alertaOrcamentoEstourado = true;
                }

                alertaSemSafrasAtivas = (qtdSafras == 0);
                alertaSemInsumos = (qtdInsumos == 0);
            }
        } else {
            // CENÁRIO B: Carregamento Geral Consolidado (Global)
            qtdAtividades = atividadeRepository.count();
            qtdInsumos = insumosRepository.count();
            qtdSafras = safraRepository.count();
            totalGastoCalculado = obterTotalGastoGlobal();
            atividadesRecentes = atividadeRepository.findAll();
            safrasFiltradas = safraRepository.findAll();

            // No escopo global, o sistema avalia cada item contra o limite da sua respectiva fazenda
            List<Insumos> todosInsumosGlobais = insumosRepository.findAll();
            if (todosInsumosGlobais != null) {
                for (Insumos ins : todosInsumosGlobais) {
                    if (ins != null && ins.getQuantidade() != null) {
                        int limiteEspecifico = 10;
                        if (ins.getPropriedade() != null && ins.getPropriedade().getLimiteEstoqueBaixo() != null) {
                            limiteEspecifico = ins.getPropriedade().getLimiteEstoqueBaixo();
                        }

                        if (ins.getQuantidade() < limiteEspecifico) {
                            alertaEstoqueBaixo = true;
                            if (itensEstoqueBaixo.isEmpty()) {
                                itensEstoqueBaixo = ins.getNome() + " (" + ins.getQuantidade() + " un)";
                            } else {
                                itensEstoqueBaixo += ", " + ins.getNome() + " (" + ins.getQuantidade() + " un)";
                            }
                        }
                    }
                }
            }

            // Somatório de faturamento global real
            List<Safra> todasSafrasGlobais = safraRepository.findAll();
            if (todasSafrasGlobais != null) {
                for (Safra s : todasSafrasGlobais) {
                    if (s != null && s.getProducaoEstimadaSacas() != null && s.getPrecoSacaEsperado() != null) {
                        faturamentoProjetado += (s.getProducaoEstimadaSacas() * s.getPrecoSacaEsperado());
                    }
                }
            }
        }

        // Histórico de Logs Reativos para a View (Módulo 3)
        List<Map<String, Object>> logsSimulados = new ArrayList<>();
        Map<String, Object> log1 = new HashMap<>();
        log1.put("dataEvento", LocalDateTime.now().minusHours(1));
        log1.put("tipoAlerta", "SISTEMA");
        log1.put("mensagem", "Parâmetros operacionais atualizados: pontos de pedido recalculados dinamicamente.");
        logsSimulados.add(log1);

        if (alertaEstoqueBaixo) {
            Map<String, Object> log2 = new HashMap<>();
            log2.put("dataEvento", LocalDateTime.now());
            log2.put("tipoAlerta", "ESTOQUE");
            log2.put("mensagem", "Aviso emitido: Itens operando abaixo da margem mínima estabelecida: " + itensEstoqueBaixo);
            logsSimulados.add(log2);
        }

        // Model Bindings
        model.addAttribute("qtdPropriedades", qtdPropriedades);
        model.addAttribute("qtdAtividades", qtdAtividades);
        model.addAttribute("qtdInsumos", qtdInsumos);
        model.addAttribute("qtdSafras", qtdSafras);
        model.addAttribute("totalGasto", totalGastoCalculado);
        model.addAttribute("atividadesRecentes", atividadesRecentes);
        model.addAttribute("safrasFiltradas", safrasFiltradas);
        model.addAttribute("alertaOrcamento", alertaOrcamentoEstourado);
        model.addAttribute("alertaSafras", alertaSemSafrasAtivas);
        model.addAttribute("alertaColheita", alertaColheitaProxima);
        model.addAttribute("alertaEstoque", alertaEstoqueBaixo);
        model.addAttribute("alertaVazio", alertaSemInsumos);
        model.addAttribute("safraUrgenteNome", nomeSafraUrgente);
        model.addAttribute("produtosFaltaNome", itensEstoqueBaixo);
        model.addAttribute("faturamentoProjetado", faturamentoProjetado);
        model.addAttribute("lucroLiquidoProjetado", (faturamentoProjetado - totalGastoCalculado));
        model.addAttribute("logsAuditoria", logsSimulados);

        return "index";
    }

    private double obterTotalGastoGlobal() {
        double total = 0.0;
        List<Gasto> lista = gastoRepository.findAll();
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