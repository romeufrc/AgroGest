package com.example.AgroGestao.controller;

import com.example.AgroGestao.model.Atividade;
import com.example.AgroGestao.repository.AtividadeRepository;
import com.example.AgroGestao.service.AtividadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/atividades")
public class AtividadeController {

    @Autowired
    private AtividadeRepository repository;

    @Autowired
    private AtividadeService service; // Injetando a nossa nova camada de serviço

    // Cadastrar atividade com baixa automática no estoque (RF05 + Sprint 4)
    @PostMapping
    public Atividade cadastrar(@RequestBody Atividade atividade) {
        return service.salvarEAtualizarEstoque(atividade);
    }

    // Listar atividades (RF09)
    @GetMapping
    public List<Atividade> listar() {
        return repository.findAll();
    }

    // Editar Atividade (RF10)
    @PutMapping("/{id}")
    public Atividade editar(@PathVariable Long id, @RequestBody Atividade novaAtividade) {
        return repository.findById(id)
                .map(atividade -> {
                    atividade.setNome(novaAtividade.getNome());
                    atividade.setDescricao(novaAtividade.getDescricao());
                    atividade.setTipo(novaAtividade.getTipo());
                    atividade.setData(novaAtividade.getData());
                    atividade.setPropriedade(novaAtividade.getPropriedade());
                    return repository.save(atividade);
                }).orElseGet(() -> {
                    novaAtividade.setId(id);
                    return repository.save(novaAtividade);
                });
    }

    // Excluir Atividade (RF12)
    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        repository.deleteById(id);
    }
}