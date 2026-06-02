package com.example.AgroGestao.controller;

import com.example.AgroGestao.model.Usuario;
import com.example.AgroGestao.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller // Usamos @Controller para conseguir retornar paginas HTML do Thymeleaf
@RequestMapping
public class UsuarioController {

    @Autowired
    private UsuarioRepository repository;

    // --- ROTAS DE VIEW (THYMELEAF) ---

    // Exibe a tela de perfil puxando os dados do usuario LOGADO na sessao
    @GetMapping("/perfil")
    public String exibirPerfil(Model model, HttpSession session) {
        // Busca o usuario que foi salvo na sessao la no LoginController
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

        // SEGURANCA: Se a sessao caiu ou nao tiver ninguem logado, manda para o login
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        // Garante que pega os dados mais atualizados do banco usando o ID da sessao
        Usuario usuarioAtualizado = repository.findById(usuarioLogado.getId()).orElse(usuarioLogado);

        model.addAttribute("usuario", usuarioAtualizado);
        return "perfil";
    }

    // Salva ou atualiza os dados editados no perfil e atualiza a sessao
    @PostMapping("/perfil/salvar")
    public String salvarPerfil(@ModelAttribute Usuario usuario, HttpSession session) {
        // Salva as alteracoes no banco MySQL
        Usuario usuarioSalvo = repository.save(usuario);

        // Atualiza o usuario na sessao para refletir o novo nome/telefone nos menus
        session.setAttribute("usuarioLogado", usuarioSalvo);

        return "redirect:/perfil?sucesso";
    }

    // --- ROTAS DE VIEW PARA CADASTRO ---

    // Exibe a tela de cadastro de conta
    @GetMapping("/usuarios/cadastro")
    public String exibirFormularioCadastro(Model model) {
        model.addAttribute("novoUsuario", new Usuario());
        return "cadastro";
    }

    // Recebe os dados da tela de cadastro e salva no banco MySQL
    @PostMapping("/usuarios/cadastro")
    public String cadastrarNovoUsuario(@ModelAttribute Usuario usuario) {
        repository.save(usuario);
        return "redirect:/login";
    }

    // --- ROTAS DE API REST ---

    @PostMapping("/usuarios")
    @ResponseBody
    public Usuario cadastrar(@RequestBody Usuario usuario) {
        return repository.save(usuario);
    }

    @GetMapping("/usuarios")
    @ResponseBody
    public List<Usuario> listar() {
        return repository.findAll();
    }
}