package com.example.AgroGestao.controller;

import com.example.AgroGestao.model.*;
import com.example.AgroGestao.repository.*;
import jakarta.servlet.http.HttpSession;
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

@Controller // Define esta classe como um Controller do Spring MVC
public class HomeController {

    // Injeto todos os repositorios que eu preciso para varrer o banco de dados e montar o meu dashboard

    // Repositório responsável pelas operações relacionadas às propriedades
    @Autowired
    private PropriedadeRepository propriedadeRepository;

    // Repositório responsável pelas operações relacionadas às atividades
    @Autowired
    private AtividadeRepository atividadeRepository;

    // Repositório responsável pelas operações relacionadas aos insumos
    @Autowired
    private InsumosRepository insumosRepository;

    // Repositório responsável pelas operações relacionadas aos gastos
    @Autowired
    private GastoRepository gastoRepository;

    // Repositório responsável pelas operações relacionadas às safras
    @Autowired
    private SafraRepository safraRepository;

    //Metodo principal que constroi a tela inicial. Ele recebe o id da propriedade para filtro e a sessao para seguranca.

    // Mapeia a rota principal do sistema
    @GetMapping("/")
    public String exibirDashboard(@RequestParam(name = "propriedadeId", required = false) Long id, Model model, HttpSession session) {

        // Primeiro passo da minha seguranca: resgato quem e o usuario logado. Se a sessao for nula, derrubo para o login.

        // Recupera o usuário armazenado na sessão
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

        // Caso não exista usuário autenticado, redireciona para a tela de login
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        // Filtro o menu dropdown superior para carregar apenas as fazendas que pertencem ao meu usuario logado

        // Busca todas as propriedades cadastradas
        List<Propriedade> todasProp = propriedadeRepository.findAll();

        // Lista que armazenará apenas as propriedades do usuário logado
        List<Propriedade> minhasPropriedades = new ArrayList<>();

        // Percorre todas as propriedades cadastradas
        for (Propriedade p : todasProp) {

            // Adiciona apenas as propriedades pertencentes ao usuário autenticado
            if (p.getUsuario() != null && p.getUsuario().getId().equals(usuarioLogado.getId())) {
                minhasPropriedades.add(p);
            }
        }

        // Envia a lista de propriedades para a página HTML
        model.addAttribute("propriedades", minhasPropriedades);

        // Inicializo os meus contadores e acumuladores que vao preencher os cards do dashboard

        // Quantidade total de propriedades do usuário
        long qtdPropriedades = minhasPropriedades.size();

        // Contador de atividades cadastradas
        long qtdAtividades = 0;

        // Contador de insumos cadastrados
        long qtdInsumos = 0;

        // Contador de safras cadastradas
        long qtdSafras = 0;

        // Acumulador utilizado para somar todos os gastos
        double totalGastoCalculado = 0.0;

        // Lista que armazenará as atividades exibidas no dashboard
        List<Atividade> atividadesRecentes = new ArrayList<>();

        // Lista que armazenará as safras exibidas no dashboard
        List<Safra> safrasFiltradas = new ArrayList<>();

        // Gatilhos para o meu painel de alertas reativos

        // Indica se o orçamento foi ultrapassado
        boolean alertaOrcamentoEstourado = false;

        // Indica se não existem safras cadastradas
        boolean alertaSemSafrasAtivas = false;

        // Indica se existe alguma safra próxima da colheita
        boolean alertaColheitaProxima = false;

        // Indica se existem insumos abaixo do limite de estoque
        boolean alertaEstoqueBaixo = false;

        // Indica se não existem insumos cadastrados
        boolean alertaSemInsumos = false;

        // Variaveis de texto para guardar os nomes dos itens que estao com problemas

        // Armazena os nomes das safras que exigem atenção
        String nomeSafraUrgente = "";

        // Armazena os nomes dos insumos com estoque baixo
        String itensEstoqueBaixo = "";

        // Armazena o valor total estimado de faturamento
        double faturamentoProjetado = 0.0;

        // Verifica se o usuário selecionou uma propriedade específica para filtrar os dados
        if (id != null) {

            // Busca a propriedade pelo ID informado na URL
            Propriedade p = propriedadeRepository.findById(id).orElse(null);

            // Confirma se a propriedade existe e pertence ao usuário logado
            if (p != null && p.getUsuario() != null && p.getUsuario().getId().equals(usuarioLogado.getId())) {

                // Envia a propriedade selecionada para a interface
                model.addAttribute("propSelecionada", p);

                // Obtém o limite de estoque baixo configurado para a propriedade
                // Caso não exista configuração, utiliza 10 como valor padrão
                int margemSegurancaEstoque = (p.getLimiteEstoqueBaixo() != null)
                        ? p.getLimiteEstoqueBaixo()
                        : 10;

                // Busca todas as atividades cadastradas no sistema
                List<Atividade> todasAtiv = atividadeRepository.findAll();

                // Verifica se existem atividades cadastradas
                if (todasAtiv != null) {

                    // Percorre todas as atividades
                    for (Atividade a : todasAtiv) {

                        // Filtra apenas as atividades da propriedade selecionada
                        if (a != null && a.getPropriedade() != null
                                && id.equals(a.getPropriedade().getId())) {

                            // Incrementa a quantidade de atividades
                            qtdAtividades++;

                            // Adiciona a atividade à lista exibida no dashboard
                            atividadesRecentes.add(a);
                        }
                    }
                }

                // Busca todos os gastos cadastrados
                List<Gasto> todosGastos = gastoRepository.findAll();

                // Verifica se existem gastos registrados
                if (todosGastos != null) {

                    // Percorre todos os gastos
                    for (Gasto g : todosGastos) {

                        // Soma apenas os gastos pertencentes à propriedade selecionada
                        if (g != null
                                && g.getPropriedade() != null
                                && id.equals(g.getPropriedade().getId())
                                && g.getValor() != null) {

                            totalGastoCalculado += g.getValor();
                        }
                    }
                }

                // Busca todos os insumos cadastrados
                List<Insumos> todosInsumos = insumosRepository.findAll();

                // Verifica se existem insumos cadastrados
                if (todosInsumos != null) {

                    // Percorre todos os insumos
                    for (Insumos ins : todosInsumos) {

                        // Filtra apenas os insumos da propriedade selecionada
                        if (ins != null
                                && ins.getPropriedade() != null
                                && id.equals(ins.getPropriedade().getId())) {

                            // Incrementa a quantidade de insumos encontrados
                            qtdInsumos++;

                            // Verifica se o estoque está abaixo do limite configurado
                            if (ins.getQuantidade() != null
                                    && ins.getQuantidade() < margemSegurancaEstoque) {

                                // Ativa o alerta de estoque baixo
                                alertaEstoqueBaixo = true;

                                // Adiciona o insumo à lista de produtos com estoque crítico
                                if (itensEstoqueBaixo.isEmpty()) {
                                    itensEstoqueBaixo = ins.getNome() + " (" + ins.getQuantidade() + " un)";
                                } else {
                                    itensEstoqueBaixo += ", " + ins.getNome() + " (" + ins.getQuantidade() + " un)";
                                }
                            }
                        }
                    }
                }

                // Busca todas as safras cadastradas
                List<Safra> todasSafras = safraRepository.findAll();

                // Verifica se existem safras cadastradas
                if (todasSafras != null) {

                    // Percorre todas as safras
                    for (Safra s : todasSafras) {

                        // Filtra apenas as safras da propriedade selecionada
                        if (s != null
                                && s.getPropriedade() != null
                                && id.equals(s.getPropriedade().getId())) {

                            // Incrementa a quantidade de safras
                            qtdSafras++;

                            // Adiciona a safra à lista exibida na dashboard
                            safrasFiltradas.add(s);

                            // Calcula o faturamento estimado da safra
                            if (s.getProducaoEstimadaSacas() != null
                                    && s.getPrecoSacaEsperado() != null) {

                                faturamentoProjetado +=
                                        (s.getProducaoEstimadaSacas() * s.getPrecoSacaEsperado());
                            }

                            // Verifica se a safra está em fase de maturação
                            if (s.getStatus() != null
                                    && (s.getStatus().equalsIgnoreCase("EM MATURAÇÃO")
                                    || s.getStatus().equalsIgnoreCase("MATURAÇÃO"))) {

                                // Ativa o alerta de colheita próxima
                                alertaColheitaProxima = true;

                                // Adiciona a safra à lista de alertas
                                if (nomeSafraUrgente.isEmpty()) {
                                    nomeSafraUrgente = s.getNome() + " [" + s.getCultura() + "]";
                                } else {
                                    nomeSafraUrgente += ", " + s.getNome() + " [" + s.getCultura() + "]";
                                }
                            }
                        }
                    }
                }

                // Verifica se os gastos ultrapassaram o limite definido para a propriedade
                if (p.getLimiteGasto() != null
                        && totalGastoCalculado > p.getLimiteGasto()) {

                    alertaOrcamentoEstourado = true;
                }

                // Define alerta caso não existam safras cadastradas
                alertaSemSafrasAtivas = (qtdSafras == 0);

                // Define alerta caso não existam insumos cadastrados
                alertaSemInsumos = (qtdInsumos == 0);

            } else {

                // Impede acesso a propriedades que não pertencem ao usuário logado
                return "redirect:/";
            }

        } else {
            // Cenário em que nenhuma propriedade específica foi selecionada
            // Nesse caso, a Dashboard exibirá os dados consolidados de todas as propriedades do usuário


            // Busca todas as atividades cadastradas no sistema
            List<Atividade> todasAtiv = atividadeRepository.findAll();

            // Percorre todas as atividades para encontrar apenas as do usuário logado
            for (Atividade a : todasAtiv) {

                // Verifica se a atividade pertence a uma propriedade vinculada ao usuário
                if (a.getPropriedade() != null
                        && a.getPropriedade().getUsuario() != null
                        && a.getPropriedade().getUsuario().getId().equals(usuarioLogado.getId())) {

                    // Incrementa o contador de atividades
                    qtdAtividades++;

                    // Adiciona a atividade à lista que será exibida no Dashboard
                    atividadesRecentes.add(a);
                }
            }

            // Busca todos os insumos cadastrados
            List<Insumos> todosInsumosGlobais = insumosRepository.findAll();

            // Percorre todos os insumos cadastrados
            for (Insumos ins : todosInsumosGlobais) {

                // Verifica se o insumo pertence a alguma propriedade do usuário logado
                if (ins.getPropriedade() != null
                        && ins.getPropriedade().getUsuario() != null
                        && ins.getPropriedade().getUsuario().getId().equals(usuarioLogado.getId())) {

                    // Incrementa o contador de insumos
                    qtdInsumos++;

                    // Recupera o limite de estoque definido para a propriedade
                    // Caso não exista configuração, utiliza 10 como padrão
                    int limiteEspecifico = (ins.getPropriedade().getLimiteEstoqueBaixo() != null)
                            ? ins.getPropriedade().getLimiteEstoqueBaixo()
                            : 10;

                    // Verifica se a quantidade atual está abaixo do limite configurado
                    if (ins.getQuantidade() != null && ins.getQuantidade() < limiteEspecifico) {

                        // Ativa o alerta de estoque baixo
                        alertaEstoqueBaixo = true;

                        // Monta a lista de produtos com estoque crítico
                        if (itensEstoqueBaixo.isEmpty()) {
                            itensEstoqueBaixo = ins.getNome() + " (" + ins.getQuantidade() + " un)";
                        } else {
                            itensEstoqueBaixo += ", " + ins.getNome() + " (" + ins.getQuantidade() + " un)";
                        }
                    }
                }
            }

            // Busca todas as safras cadastradas
            List<Safra> todasSafrasGlobais = safraRepository.findAll();

            // Percorre todas as safras do sistema
            for (Safra s : todasSafrasGlobais) {

                // Filtra apenas as safras pertencentes ao usuário logado
                if (s.getPropriedade() != null
                        && s.getPropriedade().getUsuario() != null
                        && s.getPropriedade().getUsuario().getId().equals(usuarioLogado.getId())) {

                    // Incrementa o contador de safras
                    qtdSafras++;

                    // Adiciona a safra à lista exibida na Dashboard
                    safrasFiltradas.add(s);

                    // Calcula o faturamento projetado com base na produção estimada e preço esperado
                    if (s.getProducaoEstimadaSacas() != null && s.getPrecoSacaEsperado() != null) {
                        faturamentoProjetado += (s.getProducaoEstimadaSacas() * s.getPrecoSacaEsperado());
                    }

                    // Verifica se a safra está em fase de maturação
                    if (s.getStatus() != null
                            && (s.getStatus().equalsIgnoreCase("EM MATURAÇÃO")
                            || s.getStatus().equalsIgnoreCase("MATURAÇÃO"))) {

                        // Ativa o alerta de colheita próxima
                        alertaColheitaProxima = true;

                        // Monta a lista de safras que precisam de atenção
                        if (nomeSafraUrgente.isEmpty()) {
                            nomeSafraUrgente = s.getNome() + " [" + s.getCultura() + "]";
                        } else {
                            nomeSafraUrgente += ", " + s.getNome() + " [" + s.getCultura() + "]";
                        }
                    }
                }
            }

            // Busca todos os gastos cadastrados
            List<Gasto> listaGastos = gastoRepository.findAll();

            // Mapa auxiliar para acumular o gasto de cada propriedade individualmente,
            // necessário para verificar o orçamento por propriedade (e não apenas o total geral)
            Map<Long, Double> gastoPorPropriedade = new HashMap<>();

            // Percorre os gastos para somar apenas os pertencentes ao usuário logado
            for (Gasto g : listaGastos) {

                // Verifica se o gasto pertence a uma propriedade do usuário
                if (g.getPropriedade() != null
                        && g.getPropriedade().getUsuario() != null
                        && g.getPropriedade().getUsuario().getId().equals(usuarioLogado.getId())
                        && g.getValor() != null) {

                    // Soma o valor do gasto ao total acumulado
                    totalGastoCalculado += g.getValor();

                    // Acumula o valor também por propriedade, para checar o orçamento individual
                    Long idPropriedadeGasto = g.getPropriedade().getId();
                    gastoPorPropriedade.merge(idPropriedadeGasto, g.getValor(), Double::sum);
                }
            }

            // CORREÇÃO: antes este alerta só era calculado quando uma propriedade
            // específica estava selecionada. Agora verifica, na visão consolidada,
            // se alguma propriedade do usuário ultrapassou o próprio limite de gasto.
            for (Propriedade prop : minhasPropriedades) {
                Double gastoDaPropriedade = gastoPorPropriedade.get(prop.getId());
                if (prop.getLimiteGasto() != null
                        && gastoDaPropriedade != null
                        && gastoDaPropriedade > prop.getLimiteGasto()) {
                    alertaOrcamentoEstourado = true;
                }
            }

            // CORREÇÃO: antes estes alertas só eram definidos quando uma propriedade
            // específica estava selecionada, então a Dashboard inicial (sem filtro)
            // nunca avisava quando o usuário não tinha safras ou insumos cadastrados.
            alertaSemSafrasAtivas = (qtdSafras == 0);
            alertaSemInsumos = (qtdInsumos == 0);
        }

        // Criação da lista que armazenará os logs exibidos no painel de auditoria
        List<Map<String, Object>> logsSimulados = new ArrayList<>();

        // Cria o log inicial informando o carregamento do sistema
        Map<String, Object> log1 = new HashMap<>();
        log1.put("dataEvento", LocalDateTime.now().minusHours(1));
        log1.put("tipoAlerta", "SISTEMA");
        log1.put("mensagem", "Sessao iniciada com sucesso. Dashboard isolado ativado.");
        logsSimulados.add(log1);

        // Cria um log caso existam produtos com estoque baixo
        if (alertaEstoqueBaixo) {
            Map<String, Object> log2 = new HashMap<>();
            log2.put("dataEvento", LocalDateTime.now());
            log2.put("tipoAlerta", "ESTOQUE");
            log2.put("mensagem", "Aviso emitido: Itens operando abaixo da margem minima estabelecida: " + itensEstoqueBaixo);
            logsSimulados.add(log2);
        }

        // Cria um log caso existam safras próximas da colheita
        if (alertaColheitaProxima) {
            Map<String, Object> log3 = new HashMap<>();
            log3.put("dataEvento", LocalDateTime.now());
            log3.put("tipoAlerta", "SAFRA");
            log3.put("mensagem", "Aviso emitido: Safras proximas da colheita detectadas: " + nomeSafraUrgente);
            logsSimulados.add(log3);
        }

        // Envia todas as métricas calculadas para a página HTML
        model.addAttribute("qtdPropriedades", qtdPropriedades);
        model.addAttribute("qtdAtividades", qtdAtividades);
        model.addAttribute("qtdInsumos", qtdInsumos);
        model.addAttribute("qtdSafras", qtdSafras);
        model.addAttribute("totalGasto", totalGastoCalculado);

        // Envia as listas utilizadas pela Dashboard
        model.addAttribute("atividadesRecentes", atividadesRecentes);
        model.addAttribute("safrasFiltradas", safrasFiltradas);

        // Envia os estados dos alertas para exibição visual
        model.addAttribute("alertaOrcamento", alertaOrcamentoEstourado);
        model.addAttribute("alertaSafras", alertaSemSafrasAtivas);
        model.addAttribute("alertaColheita", alertaColheitaProxima);
        model.addAttribute("alertaEstoque", alertaEstoqueBaixo);
        model.addAttribute("alertaVazio", alertaSemInsumos);

        // Envia os nomes utilizados nos alertas
        model.addAttribute("safraUrgenteNome", nomeSafraUrgente);
        model.addAttribute("produtosFaltaNome", itensEstoqueBaixo);

        // Envia o faturamento projetado calculado
        model.addAttribute("faturamentoProjetado", faturamentoProjetado);

        // Calcula e envia o lucro líquido projetado
        model.addAttribute("lucroLiquidoProjetado", (faturamentoProjetado - totalGastoCalculado));

        // Envia os logs para a área de auditoria da Dashboard
        model.addAttribute("logsAuditoria", logsSimulados);

        // Retorna a página principal da Dashboard
        return "index";
    }

}