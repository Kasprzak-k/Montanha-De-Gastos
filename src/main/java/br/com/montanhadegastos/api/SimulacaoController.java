package br.com.montanhadegastos.api;

import br.com.montanhadegastos.habitos.Habito;
import br.com.montanhadegastos.habitos.HabitoService;
import br.com.montanhadegastos.simulacao.SimulacaoRequest;
import br.com.montanhadegastos.simulacao.SimulacaoResponse;
import br.com.montanhadegastos.simulacao.SimulacaoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SimulacaoController {

    private final HabitoService habitoService;
    private final SimulacaoService simulacaoService;

    public SimulacaoController(HabitoService habitoService, SimulacaoService simulacaoService) {
        this.habitoService = habitoService;
        this.simulacaoService = simulacaoService;
    }

    @GetMapping("/habitos")
    public List<Habito> listarHabitos() {
        return habitoService.listar();
    }

    @PostMapping("/simulacao")
    public SimulacaoResponse simular(@Valid @RequestBody SimulacaoRequest request) {
        return simulacaoService.simular(request);
    }
}

