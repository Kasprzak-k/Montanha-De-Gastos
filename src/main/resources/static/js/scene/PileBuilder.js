import * as THREE from 'three';

export const MAX_HABIT_INSTANCES = 15000;
export const MAX_MONEY_INSTANCES = 10000;

function mulberry32(seed) {
  let a = seed >>> 0;
  return function () {
    a |= 0;
    a = (a + 0x6D2B79F5) | 0;
    let t = Math.imul(a ^ (a >>> 15), 1 | a);
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

/**
 * Calcula quantas instâncias visuais desenhar respeitando o teto (15.000 para hábitos).
 * Acima do limite, cada instância representa N itens reais.
 */
export function computeInstanceRepresentation(totalItems, maxInstances = MAX_HABIT_INSTANCES) {
  const safeTotal = Math.max(0, Math.floor(Number(totalItems) || 0));
  if (safeTotal === 0) {
    return { visualCount: 0, itemsPerInstance: 1 };
  }
  const itemsPerInstance = Math.max(1, Math.ceil(safeTotal / maxInstances));
  const visualCount = Math.min(maxInstances, Math.ceil(safeTotal / itemsPerInstance));
  return { visualCount, itemsPerInstance };
}

/**
 * Empilha instâncias camada por camada (de y = 0 para cima) usando espiral de Vogel em cada andar.
 * - Garante que NENHUM item flutue (toda camada L apoia-se fisicamente na camada L-1 mais larga abaixo).
 * - Garante distância mínima centro-a-centro dentro de cada camada para evitar sobreposição agressiva.
 */
export function populateConicalPile(instancedMesh, count, options) {
  const {
    itemRadius = 0.16,
    itemHeight = 0.20,
    baseRadius = 1.85,
    targetPeakHeight = 2.4,
    maxTilt = 0.05,
    seed = 1337
  } = options;

  const total = Math.max(0, Math.min(count, instancedMesh.instanceMatrix.count));
  instancedMesh.count = total;
  if (total === 0) {
    instancedMesh.instanceMatrix.needsUpdate = true;
    return;
  }

  const rand = mulberry32(seed);
  const dummy = new THREE.Object3D();

  // Calcula número de camadas (M) e itens na base (N0) para respeitar a proporção (targetPeakHeight / baseRadius)
  const desiredAspect = Math.max(0.35, targetPeakHeight / Math.max(0.5, baseRadius));
  const itemAspect = itemHeight / (itemRadius * 1.18);
  const estimatedN0 = Math.max(1, Math.pow((3 * total * itemAspect) / desiredAspect, 2 / 3));
  const numLayers = Math.max(1, Math.round((3 * total) / estimatedN0));

  // Distribui exatamente `total` itens entre as `numLayers` camadas de forma monotonicamente decrescente
  const weights = new Array(numLayers);
  let weightSum = 0;
  for (let l = 0; l < numLayers; l++) {
    const frac = 1 - l / (numLayers + 0.25);
    weights[l] = frac * frac;
    weightSum += weights[l];
  }

  const layerCounts = new Array(numLayers);
  let allocated = 0;
  for (let l = 0; l < numLayers; l++) {
    const c = Math.max(1, Math.round((weights[l] / weightSum) * total));
    layerCounts[l] = c;
    allocated += c;
  }

  // Ajusta a diferença de arredondamento mantendo N[0] >= N[1] >= ... >= N[M-1]
  let diff = total - allocated;
  let cursor = 0;
  while (diff !== 0) {
    const idx = cursor % numLayers;
    if (diff > 0) {
      layerCounts[idx]++;
      diff--;
    } else if (layerCounts[idx] > 1) {
      layerCounts[idx]--;
      diff++;
    }
    cursor++;
    if (cursor > numLayers * 4 && diff < 0) break;
  }
  layerCounts.sort((a, b) => b - a);

  const maxBaseItems = Math.max(1, layerCounts[0]);

  // Escala uniforme que garante que a base caiba no pedestal e a altura bata com targetPeakHeight
  const scaleFromRadius = baseRadius / (Math.sqrt(maxBaseItems) * itemRadius * 1.18);
  const scaleFromHeight = targetPeakHeight / (numLayers * itemHeight * 0.94);
  const unitScale = Math.max(0.42, Math.min(1.45, Math.min(scaleFromRadius, scaleFromHeight)));

  // Passo vertical exato (94% da altura escalada do item): cada andar toca o andar de baixo, zero flutuação
  const layerStepY = itemHeight * unitScale * 0.94;
  // Passo radial de Vogel (1.16 * raio escalado): distância mínima entre centros ~ 1.95 * raio (contato suave)
  const radialStep = itemRadius * unitScale * 1.16;

  let instanceIdx = 0;
  const goldenAngle = 2.399963229728653;

  for (let l = 0; l < numLayers && instanceIdx < total; l++) {
    const itemsInLayer = Math.min(layerCounts[l], total - instanceIdx);
    const yBase = 0.008 + l * layerStepY;
    const layerAngleOffset = l * 1.17;

    for (let k = 0; k < itemsInLayer && instanceIdx < total; k++) {
      // Espiral de Vogel garante espaçamento uniforme sem colisões severas
      const r = itemsInLayer === 1 ? 0 : radialStep * Math.sqrt(k + 0.38);
      const theta = k * goldenAngle + layerAngleOffset + (rand() - 0.5) * 0.14;

      // Jitter horizontal sutil (máx 8% do raio) para aspecto orgânico sem invadir o vizinho
      const jitterR = (rand() - 0.5) * itemRadius * unitScale * 0.14;
      const finalR = Math.max(0, r + jitterR);

      // Jitter vertical mínimo (apenas 2% da altura) para nunca descolar da camada inferior
      const y = yBase + rand() * itemHeight * unitScale * 0.02;

      dummy.position.set(
        Math.cos(theta) * finalR,
        y,
        Math.sin(theta) * finalR
      );

      // Inclinação suave para manter os itens apoiados uns nos outros
      dummy.rotation.set(
        (rand() - 0.5) * maxTilt,
        rand() * Math.PI * 2,
        (rand() - 0.5) * maxTilt
      );

      const scaleJitter = unitScale * (0.97 + rand() * 0.05);
      dummy.scale.set(scaleJitter, scaleJitter, scaleJitter);
      dummy.updateMatrix();
      instancedMesh.setMatrixAt(instanceIdx, dummy.matrix);
      instanceIdx++;
    }
  }

  instancedMesh.count = instanceIdx;
  instancedMesh.instanceMatrix.needsUpdate = true;
}

/**
 * Libera geometria, materiais e texturas da GPU sem vazamento de memória.
 */
export function disposeMeshAssets(instancedMesh, assets) {
  if (!instancedMesh) return;
  if (instancedMesh.parent) {
    instancedMesh.parent.remove(instancedMesh);
  }
  if (typeof instancedMesh.dispose === 'function') {
    instancedMesh.dispose();
  }
  if (assets) {
    if (assets.geometry) {
      assets.geometry.dispose();
    }
    if (Array.isArray(assets.materials)) {
      assets.materials.forEach((m) => m && m.dispose && m.dispose());
    }
    if (Array.isArray(assets.textures)) {
      assets.textures.forEach((t) => t && t.dispose && t.dispose());
    }
  }
}
