import { Stage3D } from './scene/Stage3D.js';

const DEBOUNCE_MS = 150;

const state = {
  habito: 'CAFE',
  valor: 8.00,
  frequencia: 'DIA',
  anos: 30,
  presets: new Map()
};

const dom = {
  canvas: document.getElementById('visualizer-canvas'),
  presetContainer: document.getElementById('preset-buttons'),
  freqContainer: document.getElementById('freq-buttons'),
  anosContainer: document.getElementById('anos-buttons'),
  inputValor: document.getElementById('input-valor'),
  valorCustoBruto: document.getElementById('valor-custo-bruto'),
  contagemItens: document.getElementById('contagem-itens'),
  escalaItens: document.getElementById('escala-itens'),
  valorMontante: document.getElementById('valor-montante'),
  tagMultiplicador: document.getElementById('tag-multiplicador'),
  contagemMacos: document.getElementById('contagem-macos'),
  fraseImpacto: document.getElementById('frase-impacto'),
  erroValidacao: document.getElementById('erro-validacao'),
  taxaBadge: document.getElementById('taxa-badge'),
  taxaTexto: document.getElementById('taxa-texto'),
  btnCameraReset: document.getElementById('btn-camera-reset'),
  btnCameraToggle: document.getElementById('btn-camera-toggle'),
  iconCameraToggle: document.getElementById('icon-camera-toggle')
};

const fmtInteiro = new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 0 });
const fmtMoedaInteira = new Intl.NumberFormat('pt-BR', {
  style: 'currency',
  currency: 'BRL',
  minimumFractionDigits: 0,
  maximumFractionDigits: 0
});
const fmtMoedaCentavos = new Intl.NumberFormat('pt-BR', {
  style: 'currency',
  currency: 'BRL',
  minimumFractionDigits: 2,
  maximumFractionDigits: 2
});
const fmtDecimal1 = new Intl.NumberFormat('pt-BR', {
  minimumFractionDigits: 1,
  maximumFractionDigits: 1
});
const fmtPercentual = new Intl.NumberFormat('pt-BR', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2
});

function formatarReais(valor) {
  const num = Number(valor) || 0;
  const temCentavos = Math.abs(Math.round(num * 100) % 100) > 0;
  return temCentavos ? fmtMoedaCentavos.format(num) : fmtMoedaInteira.format(num);
}

let stage3D = null;
let debounceTimer = null;
let activeAbortController = null;

function setActiveButton(container, attrName, value) {
  const buttons = container.querySelectorAll('.seg-btn');
  buttons.forEach((btn) => {
    const active = btn.getAttribute(attrName) === String(value);
    btn.classList.toggle('is-active', active);
    btn.setAttribute('aria-pressed', active ? 'true' : 'false');
  });
}

function mostrarErro(mensagem) {
  if (!mensagem) {
    dom.erroValidacao.hidden = true;
    dom.erroValidacao.textContent = '';
    return;
  }
  dom.erroValidacao.textContent = mensagem;
  dom.erroValidacao.hidden = false;
}

function atualizarPlacares(resposta, rep) {
  mostrarErro('');

  dom.valorCustoBruto.textContent = formatarReais(resposta.custoBruto);
  dom.contagemItens.textContent = `${fmtInteiro.format(resposta.quantidadeItens)} ${resposta.rotuloItem}`;

  if (rep && rep.itemsPerInstance > 1) {
    dom.escalaItens.textContent = `1 item 3D = ${fmtInteiro.format(rep.itemsPerInstance)} ${resposta.rotuloItem}`;
    dom.escalaItens.hidden = false;
  } else {
    dom.escalaItens.hidden = true;
  }

  dom.valorMontante.textContent = formatarReais(resposta.montanteInvestido);
  dom.tagMultiplicador.textContent = `${fmtDecimal1.format(resposta.multiplicador)}x maior`;

  const macos = Number(resposta.macosCedulas) || 0;
  dom.contagemMacos.textContent =
    macos === 1 ? '1 maço de R$ 10.000' : `${fmtInteiro.format(macos)} maços de R$ 10.000`;

  dom.fraseImpacto.textContent = resposta.fraseImpacto;

  const taxaPct = fmtPercentual.format((Number(resposta.taxaRealAnual) || 0) * 100);
  const origem = resposta.origemTaxa || 'padrao';
  dom.taxaBadge.setAttribute('data-origem', origem);
  dom.taxaTexto.textContent =
    origem === 'bcb'
      ? `Taxa real: ${taxaPct}% a.a. · Banco Central (SGS)`
      : `Taxa real: ${taxaPct}% a.a. · Taxa padrão (fallback)`;
}

const OCORRENCIAS_POR_MES = {
  DIA: 365 / 12,
  SEMANA: 52 / 12,
  MES: 1
};

const ROTULOS_PADRAO = {
  CAFE: 'cafés',
  DELIVERY: 'pedidos',
  FUMO: 'maços de cigarro',
  COMPRAS: 'compras'
};

function calcularFallbackLocal(habito, valor, frequencia, anos) {
  const meses = anos * 12;
  const ocorrencias = OCORRENCIAS_POR_MES[frequencia] || 1;
  const valorMensal = Math.round(valor * ocorrencias * 100) / 100;
  const custoBruto = Math.round(valor * ocorrencias * meses * 100) / 100;
  const quantidadeItens = Math.round(ocorrencias * meses);
  const taxaRealAnual = 0.04;
  const taxaMensal = Math.pow(1 + taxaRealAnual, 1 / 12) - 1;
  const montanteInvestido =
    Math.round(valorMensal * ((Math.pow(1 + taxaMensal, meses) - 1) / taxaMensal) * 100) / 100;
  const multiplicador = Math.round((montanteInvestido / custoBruto) * 10) / 10;
  const macosCedulas = Math.floor(montanteInvestido / 10000);
  const rotuloItem = ROTULOS_PADRAO[habito] || 'itens';
  const fraseImpacto = `Em ${anos} ${anos === 1 ? 'ano' : 'anos'}, seus ${fmtInteiro.format(quantidadeItens)} ${rotuloItem} custam ${formatarReais(custoBruto)} e, investidos, virariam ${formatarReais(montanteInvestido)}.`;

  return {
    custoBruto,
    quantidadeItens,
    rotuloItem,
    montanteInvestido,
    multiplicador,
    macosCedulas,
    taxaRealAnual,
    origemTaxa: 'padrao',
    fraseImpacto
  };
}

async function executarSimulacao() {
  if (activeAbortController) {
    activeAbortController.abort();
  }

  const valorNum = Number(state.valor);
  if (!Number.isFinite(valorNum) || valorNum < 0.01 || valorNum > 1000000) {
    mostrarErro('Informe um valor entre R$ 0,01 e R$ 1.000.000,00.');
    return;
  }

  const controller = new AbortController();
  activeAbortController = controller;

  try {
    const response = await fetch('/api/simulacao', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        habito: state.habito,
        valor: valorNum,
        frequencia: state.frequencia,
        anos: state.anos
      }),
      signal: controller.signal
    });

    if (response.status === 404 || response.status === 405) {
      const fallback = calcularFallbackLocal(state.habito, valorNum, state.frequencia, state.anos);
      const rep = stage3D ? stage3D.updateFromSimulation(state.habito, fallback) : null;
      atualizarPlacares(fallback, rep);
      return;
    }

    const data = await response.json();
    if (!response.ok) {
      mostrarErro(data.mensagem || 'Não foi possível calcular a simulação.');
      return;
    }

    const rep = stage3D ? stage3D.updateFromSimulation(state.habito, data) : null;
    atualizarPlacares(data, rep);
  } catch (err) {
    if (err && err.name === 'AbortError') {
      return;
    }
    const fallback = calcularFallbackLocal(state.habito, valorNum, state.frequencia, state.anos);
    const rep = stage3D ? stage3D.updateFromSimulation(state.habito, fallback) : null;
    atualizarPlacares(fallback, rep);
  }
}

function agendarSimulacao() {
  if (debounceTimer) {
    clearTimeout(debounceTimer);
  }
  debounceTimer = setTimeout(() => {
    executarSimulacao();
  }, DEBOUNCE_MS);
}

async function carregarPresets() {
  try {
    const response = await fetch('/api/habitos');
    if (!response.ok) return;
    const habitos = await response.json();
    if (!Array.isArray(habitos) || habitos.length === 0) return;

    habitos.forEach((h) => {
      state.presets.set(h.id.toUpperCase(), h);
    });

    const inicial = state.presets.get(state.habito) || habitos[0];
    if (inicial) {
      state.habito = inicial.id.toUpperCase();
      state.valor = Number(inicial.valorPadrao);
      state.frequencia = inicial.frequenciaPadrao;
      dom.inputValor.value = Number(state.valor).toFixed(2);
      setActiveButton(dom.presetContainer, 'data-habito', state.habito);
      setActiveButton(dom.freqContainer, 'data-frequencia', state.frequencia);
    }
  } catch (_) {
    // Mantém os presets estáticos do HTML caso o carregamento inicial falhe
  }
}

function bindControls() {
  dom.presetContainer.addEventListener('click', (event) => {
    const btn = event.target.closest('[data-habito]');
    if (!btn) return;
    const habitoId = btn.getAttribute('data-habito');
    state.habito = habitoId;
    setActiveButton(dom.presetContainer, 'data-habito', habitoId);

    const preset = state.presets.get(habitoId);
    if (preset) {
      state.valor = Number(preset.valorPadrao);
      state.frequencia = preset.frequenciaPadrao;
      dom.inputValor.value = Number(state.valor).toFixed(2);
      setActiveButton(dom.freqContainer, 'data-frequencia', state.frequencia);
    }
    agendarSimulacao();
  });

  dom.freqContainer.addEventListener('click', (event) => {
    const btn = event.target.closest('[data-frequencia]');
    if (!btn) return;
    state.frequencia = btn.getAttribute('data-frequencia');
    setActiveButton(dom.freqContainer, 'data-frequencia', state.frequencia);
    agendarSimulacao();
  });

  dom.anosContainer.addEventListener('click', (event) => {
    const btn = event.target.closest('[data-anos]');
    if (!btn) return;
    state.anos = Number(btn.getAttribute('data-anos'));
    setActiveButton(dom.anosContainer, 'data-anos', state.anos);
    agendarSimulacao();
  });

  dom.inputValor.addEventListener('input', () => {
    state.valor = dom.inputValor.value;
    agendarSimulacao();
  });

  dom.btnCameraReset.addEventListener('click', () => {
    if (stage3D) {
      stage3D.resetCamera();
    }
  });

  dom.btnCameraToggle.addEventListener('click', () => {
    if (!stage3D) return;
    const rotating = stage3D.toggleAutoRotate();
    dom.btnCameraToggle.setAttribute('aria-pressed', rotating ? 'false' : 'true');
    dom.iconCameraToggle.textContent = rotating ? '❚❚' : '▶';
  });
}

async function init() {
  if (dom.canvas) {
    try {
      stage3D = new Stage3D(dom.canvas);
      window.__stage3D = stage3D;
    } catch (e) {
      console.error('WebGL indisponível no navegador atual:', e);
    }
  }
  bindControls();
  await carregarPresets();
  await executarSimulacao();
}

init();

