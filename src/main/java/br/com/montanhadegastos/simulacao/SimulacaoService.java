package br.com.montanhadegastos.simulacao;

import br.com.montanhadegastos.calculo.CalculadoraCustoBruto;
import br.com.montanhadegastos.calculo.CalculadoraMontante;
import br.com.montanhadegastos.calculo.ResultadoCustoBruto;
import br.com.montanhadegastos.calculo.ResultadoMontante;
import br.com.montanhadegastos.calculo.ResultadoTaxaReal;
import br.com.montanhadegastos.habitos.Habito;
import br.com.montanhadegastos.habitos.HabitoService;
import br.com.montanhadegastos.taxas.TaxaRealService;
import br.com.montanhadegastos.taxas.TaxaRealVigente;
import org.springframework.stereotype.Service;

@Service
public class SimulacaoService {

    private final HabitoService habitoService;
    private final TaxaRealService taxaRealService;
    private final FraseImpactoService fraseImpactoService;
    private final CalculadoraCustoBruto calculadoraCustoBruto = new CalculadoraCustoBruto();
    private final CalculadoraMontante calculadoraMontante = new CalculadoraMontante();

    public SimulacaoService(
            HabitoService habitoService,
            TaxaRealService taxaRealService,
            FraseImpactoService fraseImpactoService
    ) {
        this.habitoService = habitoService;
        this.taxaRealService = taxaRealService;
        this.fraseImpactoService = fraseImpactoService;
    }

    public SimulacaoResponse simular(SimulacaoRequest request) {
        Habito habito = habitoService.buscarPorId(request.habito());
        TaxaRealVigente taxaVigente = taxaRealService.obterTaxaReal();
        ResultadoTaxaReal resultadoTaxa = new ResultadoTaxaReal(
                taxaVigente.taxaRealAnual(),
                taxaVigente.taxaRealMensal()
        );

        ResultadoCustoBruto custoBruto = calculadoraCustoBruto.calcular(
                request.valor(),
                request.frequencia(),
                request.anos()
        );
        ResultadoMontante montante = calculadoraMontante.calcular(
                custoBruto,
                resultadoTaxa,
                request.anos()
        );

        String fraseImpacto = fraseImpactoService.gerarFrase(
                habito,
                request.anos(),
                custoBruto.custoBruto(),
                custoBruto.quantidadeItens(),
                montante.montanteInvestido()
        );

        return new SimulacaoResponse(
                custoBruto.custoBruto(),
                custoBruto.quantidadeItens(),
                habito.rotuloItem(),
                montante.montanteInvestido(),
                montante.multiplicador(),
                montante.macosCedulas(),
                taxaVigente.taxaRealAnual(),
                taxaVigente.origem(),
                fraseImpacto
        );
    }
}

