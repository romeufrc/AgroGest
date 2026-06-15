package com.example.AgroGestao.controller;

import com.example.AgroGestao.model.Atividade;
import com.example.AgroGestao.model.Propriedade;
import com.example.AgroGestao.model.Usuario;
import com.example.AgroGestao.repository.AtividadeRepository;
import com.example.AgroGestao.repository.PropriedadeRepository;
import com.example.AgroGestao.service.AtividadeService;
import jakarta.servlet.http.HttpSession;
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

    @Autowired
    private AtividadeService atividadeService;

    // Meu metodo principal para listar as atividades. Ele varre o banco e mostra apenas o que pertence ao usuario logado.
    @GetMapping
    public String exibirAtividades(@RequestParam(name = "propriedadeId", required = false) Long propriedadeId, Model model, HttpSession session) {
        // Resgato a sessao atual para garantir a seguranca
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) return "redirect:/login";

        // Filtro o menu dropdown para carregar apenas as minhas propriedades
        List<Propriedade> minhasPropriedades = new ArrayList<>();
        for (Propriedade p : propriedadeRepository.findAll()) {
            if (p.getUsuario() != null && p.getUsuario().getId().equals(usuarioLogado.getId())) {
                minhasPropriedades.add(p);
            }
        }
        model.addAttribute("propriedades", minhasPropriedades);

        // Filtro a tabela para carregar apenas as minhas atividades, cruzando o ID do usuario
        List<Atividade> minhasAtividades = new ArrayList<>();
        for (Atividade a : atividadeRepository.findAll()) {
            if (a.getPropriedade() != null && a.getPropriedade().getUsuario() != null && a.getPropriedade().getUsuario().getId().equals(usuarioLogado.getId())) {
                if (propriedadeId != null) {
                    if (propriedadeId.equals(a.getPropriedade().getId())) minhasAtividades.add(a);
                } else {
                    minhasAtividades.add(a);
                }
            }
        }
        model.addAttribute("atividades", minhasAtividades);

        // Bloqueio de acesso: Verifico se a propriedade pesquisada realmente eh minha
        if (propriedadeId != null) {
            Propriedade p = propriedadeRepository.findById(propriedadeId).orElse(null);
            if (p != null && p.getUsuario() != null && p.getUsuario().getId().equals(usuarioLogado.getId())) {
                model.addAttribute("propSelecionada", p);
            } else {
                return "redirect:/atividades-view";
            }
        } else {
            model.addAttribute("propSelecionada", null);
        }

        if (!model.containsAttribute("novaAtividade")) {
            model.addAttribute("novaAtividade", new Atividade());
        }
        return "atividades";
    }

    // Meu metodo para salvar dados. Ele valida a propriedade antes de mandar para o Service.
    @PostMapping("/salvar")
    public String salvarAtividade(@ModelAttribute Atividade atividade, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) return "redirect:/login";

        // Confirmo o vinculo de propriedade para evitar salvar na fazenda de outro usuario
        if (atividade.getPropriedade() != null && atividade.getPropriedade().getId() != null) {
            Propriedade prop = propriedadeRepository.findById(atividade.getPropriedade().getId()).orElse(null);
            if (prop != null && prop.getUsuario() != null && prop.getUsuario().getId().equals(usuarioLogado.getId())) {
                atividade.setPropriedade(prop);
            } else {
                return "redirect:/atividades-view";
            }
        }

        if (atividade.getTipo() == null || atividade.getTipo().isEmpty()) {
            atividade.setTipo("Campo");
        }

        // Aciono o meu Service para cuidar do controle de estoque e auditoria
        atividadeService.salvarEAtualizarEstoque(atividade);
        return "redirect:/atividades-view";
    }

    // Recupero os dados para edicao aplicando a mesma blindagem de usuario
    @GetMapping("/editar/{id}")
    public String editarAtividade(@PathVariable Long id, Model model, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) return "redirect:/login";

        Atividade atividade = atividadeRepository.findById(id).orElse(null);

        // Regra de seguranca: So deixo editar se o registro existir e for meu
        if (atividade != null && atividade.getPropriedade() != null && atividade.getPropriedade().getUsuario() != null && atividade.getPropriedade().getUsuario().getId().equals(usuarioLogado.getId())) {

            List<Propriedade> minhasProp = new ArrayList<>();
            for (Propriedade p : propriedadeRepository.findAll()) {
                if (p.getUsuario() != null && p.getUsuario().getId().equals(usuarioLogado.getId())) minhasProp.add(p);
            }

            List<Atividade> minhasAtiv = new ArrayList<>();
            for (Atividade a : atividadeRepository.findAll()) {
                if (a.getPropriedade() != null && a.getPropriedade().getUsuario() != null && a.getPropriedade().getUsuario().getId().equals(usuarioLogado.getId())) minhasAtiv.add(a);
            }

            model.addAttribute("novaAtividade", atividade);
            model.addAttribute("atividades", minhasAtiv);
            model.addAttribute("propriedades", minhasProp);
            model.addAttribute("propSelecionada", null);
            return "atividades";
        }
        return "redirect:/atividades-view";
    }

    // Metodo de exclusao protegido por dono da conta
    @GetMapping("/excluir/{id}")
    public String excluirAtividade(@PathVariable Long id, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) return "redirect:/login";

        Atividade atividade = atividadeRepository.findById(id).orElse(null);
        if (atividade != null && atividade.getPropriedade() != null && atividade.getPropriedade().getUsuario() != null && atividade.getPropriedade().getUsuario().getId().equals(usuarioLogado.getId())) {
            atividadeRepository.deleteById(id);
        }
        return "redirect:/atividades-view";
    }
}