package com.example.AgroGestao.controller;

import com.example.AgroGestao.model.Usuario;
import com.example.AgroGestao.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping
public class UsuarioController {

    @Autowired
    private UsuarioRepository repository;

    @GetMapping("/perfil")
    public String exibirPerfil(Model model, HttpSession session) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");

        if (usuarioId == null) {
            return "redirect:/login";
        }

        Usuario usuarioAtualizado = repository.findById(usuarioId).orElse(null);

        if (usuarioAtualizado == null) {
            return "redirect:/login";
        }

        model.addAttribute("usuario", usuarioAtualizado);
        return "perfil";
    }

    @PostMapping("/perfil/salvar")
    public String salvarPerfil(@ModelAttribute Usuario usuario, HttpSession session) {

        // Busco o usuario atual no banco para nao perder a senha caso ele atualize apenas o nome
        Usuario usuarioAntigo = repository.findById(usuario.getId()).orElse(null);

        if (usuarioAntigo != null) {
            // Se ele digitou uma senha nova, eu criptografo. Se deixou em branco, mantenho o hash antigo.
            if (usuario.getSenha() != null && !usuario.getSenha().trim().isEmpty() && !usuario.getSenha().equals(usuarioAntigo.getSenha())) {
                String novoHash = BCrypt.hashpw(usuario.getSenha(), BCrypt.gensalt());
                usuario.setSenha(novoHash);
            } else {
                usuario.setSenha(usuarioAntigo.getSenha());
            }
        }

        Usuario usuarioSalvo = repository.save(usuario);

        session.setAttribute("usuarioId", usuarioSalvo.getId());
        session.setAttribute("usuarioLogado", usuarioSalvo);

        return "redirect:/perfil?sucesso";
    }

    @GetMapping("/usuarios/cadastro")
    public String exibirFormularioCadastro(Model model) {
        model.addAttribute("novoUsuario", new Usuario());
        return "cadastro";
    }

    @PostMapping("/usuarios/cadastro")
    public String cadastrarNovoUsuario(@ModelAttribute Usuario usuario) {
        // Intercepto a senha do formulario e converto em hash antes de mandar para o banco
        if (usuario.getSenha() != null && !usuario.getSenha().trim().isEmpty()) {
            String hash = BCrypt.hashpw(usuario.getSenha(), BCrypt.gensalt());
            usuario.setSenha(hash);
        }

        repository.save(usuario);
        return "redirect:/login";
    }

    @PostMapping("/usuarios")
    @ResponseBody
    public Usuario cadastrar(@RequestBody Usuario usuario) {
        // Protecao adicional na rota REST
        if (usuario.getSenha() != null) {
            usuario.setSenha(BCrypt.hashpw(usuario.getSenha(), BCrypt.gensalt()));
        }
        return repository.save(usuario);
    }

    @GetMapping("/usuarios")
    @ResponseBody
    public List<Usuario> listar() {
        return repository.findAll();
    }
}