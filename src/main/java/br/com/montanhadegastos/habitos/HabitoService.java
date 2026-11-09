package br.com.montanhadegastos.habitos;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@EnableConfigurationProperties(HabitosProperties.class)
public class HabitoService {

    private final List<Habito> habitos;

    public HabitoService(HabitosProperties properties) {
        this.habitos = properties.habitos();
    }

    public List<Habito> listar() {
        return habitos;
    }

    public Habito buscarPorId(String id) {
        if (id == null || id.isBlank()) {
            throw new HabitoNaoEncontradoException(String.valueOf(id));
        }
        return habitos.stream()
                .filter(h -> h.id().equalsIgnoreCase(id.trim()))
                .findFirst()
                .orElseThrow(() -> new HabitoNaoEncontradoException(id));
    }
}

