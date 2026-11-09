import * as THREE from 'three';
import { mergeGeometries } from 'three/addons/utils/BufferGeometryUtils.js';

function applyVertexColor(geometry, hexColor) {
  const geom = geometry.index ? geometry.toNonIndexed() : geometry.clone();
  const count = geom.attributes.position.count;
  const color = new THREE.Color(hexColor);
  const colors = new Float32Array(count * 3);
  for (let i = 0; i < count; i++) {
    colors[i * 3] = color.r;
    colors[i * 3 + 1] = color.g;
    colors[i * 3 + 2] = color.b;
  }
  geom.setAttribute('color', new THREE.BufferAttribute(colors, 3));
  return geom;
}

export function createCoffeeCupGeometry() {
  // Xícara feita com LatheGeometry + café no topo + alça TorusGeometry
  const points = [
    new THREE.Vector2(0.0, 0.0),
    new THREE.Vector2(0.09, 0.0),
    new THREE.Vector2(0.115, 0.14),
    new THREE.Vector2(0.125, 0.24),
    new THREE.Vector2(0.108, 0.24),
    new THREE.Vector2(0.095, 0.03),
    new THREE.Vector2(0.0, 0.03)
  ];
  const cupBody = applyVertexColor(new THREE.LatheGeometry(points, 14), 0xF7F4EB);

  const coffeeLiquid = applyVertexColor(new THREE.CylinderGeometry(0.105, 0.105, 0.015, 14), 0x3A2314);
  coffeeLiquid.translate(0, 0.215, 0);

  const handle = applyVertexColor(new THREE.TorusGeometry(0.055, 0.016, 8, 12, Math.PI), 0xEFECE2);
  handle.rotateZ(-Math.PI / 2);
  handle.translate(0.11, 0.13, 0);

  const merged = mergeGeometries([cupBody, coffeeLiquid, handle], false);
  merged.computeVertexNormals();
  return merged;
}

export function createDeliveryBoxGeometry() {
  // Caixa de entrega com fita de lacre superior e etiqueta
  const box = applyVertexColor(new THREE.BoxGeometry(0.28, 0.18, 0.24), 0xC69863);
  box.translate(0, 0.09, 0);

  const tape = applyVertexColor(new THREE.BoxGeometry(0.284, 0.02, 0.055), 0xB83B2A);
  tape.translate(0, 0.175, 0);

  const label = applyVertexColor(new THREE.BoxGeometry(0.10, 0.008, 0.08), 0xF7F4EB);
  label.translate(0.04, 0.182, 0.05);

  const merged = mergeGeometries([box, tape, label], false);
  merged.computeVertexNormals();
  return merged;
}

export function createCigarettePackGeometry() {
  // Maço de cigarro bicolor (corpo branco + tampa vermelha + colarinho dourado)
  const baseBody = applyVertexColor(new THREE.BoxGeometry(0.16, 0.16, 0.075), 0xF3F1EA);
  baseBody.translate(0, 0.08, 0);

  const collar = applyVertexColor(new THREE.BoxGeometry(0.162, 0.025, 0.077), 0xC9A24D);
  collar.translate(0, 0.165, 0);

  const topLid = applyVertexColor(new THREE.BoxGeometry(0.162, 0.08, 0.077), 0xB53728);
  topLid.translate(0, 0.215, 0);

  const merged = mergeGeometries([baseBody, collar, topLid], false);
  merged.computeVertexNormals();
  return merged;
}

export function createShoppingBagGeometry() {
  // Sacola de compras estruturada com alças duplas
  const bagBody = applyVertexColor(new THREE.BoxGeometry(0.26, 0.26, 0.11), 0xC8643B);
  bagBody.translate(0, 0.13, 0);

  const topFold = applyVertexColor(new THREE.BoxGeometry(0.264, 0.03, 0.114), 0x9E4624);
  topFold.translate(0, 0.245, 0);

  const handleFront = applyVertexColor(new THREE.TorusGeometry(0.048, 0.011, 6, 12, Math.PI), 0x2B2927);
  handleFront.translate(0, 0.26, 0.038);

  const handleBack = applyVertexColor(new THREE.TorusGeometry(0.048, 0.011, 6, 12, Math.PI), 0x2B2927);
  handleBack.translate(0, 0.26, -0.038);

  const merged = mergeGeometries([bagBody, topFold, handleFront, handleBack], false);
  merged.computeVertexNormals();
  return merged;
}

export function createHabitMeshAssets(habitoId) {
  const id = (habitoId || 'CAFE').toUpperCase();
  let geometry;
  let itemRadius = 0.16;
  let itemHeight = 0.24;

  switch (id) {
    case 'DELIVERY':
      geometry = createDeliveryBoxGeometry();
      itemRadius = 0.19;
      itemHeight = 0.18;
      break;
    case 'FUMO':
      geometry = createCigarettePackGeometry();
      itemRadius = 0.13;
      itemHeight = 0.25;
      break;
    case 'COMPRAS':
      geometry = createShoppingBagGeometry();
      itemRadius = 0.18;
      itemHeight = 0.29;
      break;
    case 'CAFE':
    default:
      geometry = createCoffeeCupGeometry();
      itemRadius = 0.15;
      itemHeight = 0.24;
      break;
  }

  const material = new THREE.MeshStandardMaterial({
    vertexColors: true,
    roughness: 0.62,
    metalness: 0.06
  });

  return {
    geometry,
    materials: [material],
    textures: [],
    itemRadius,
    itemHeight
  };
}

/**
 * Cria o maço de cédulas (R$ 10.000 = 100 notas de R$ 100) com CanvasTexture procedural:
 * - Face superior/inferior: nota de R$ 100 com valor "100" impresso e fita bancária central com "10.000".
 * - Faces laterais: folhas empilhadas e continuação da fita bancária.
 */
export function createMoneyBundleAssets() {
  const topCanvas = document.createElement('canvas');
  topCanvas.width = 512;
  topCanvas.height = 256;
  const ctx = topCanvas.getContext('2d');

  // Base azul-petróleo característica da cédula de R$ 100
  ctx.fillStyle = '#2F6F8F';
  ctx.fillRect(0, 0, 512, 256);

  // Moldura gráfica interna da nota
  ctx.strokeStyle = '#9ED0E6';
  ctx.lineWidth = 6;
  ctx.strokeRect(16, 16, 480, 224);

  ctx.fillStyle = '#255B77';
  ctx.fillRect(28, 28, 456, 200);

  // Círculo ornamental da efígie
  ctx.beginPath();
  ctx.arc(140, 128, 56, 0, Math.PI * 2);
  ctx.fillStyle = '#3B82A6';
  ctx.fill();
  ctx.lineWidth = 3;
  ctx.strokeStyle = '#B7E1F2';
  ctx.stroke();

  // Valor "100" impresso na nota
  ctx.fillStyle = '#F4F9FC';
  ctx.font = 'bold 48px Inter, Arial, sans-serif';
  ctx.textAlign = 'left';
  ctx.textBaseline = 'top';
  ctx.fillText('100', 42, 38);

  ctx.textAlign = 'right';
  ctx.textBaseline = 'bottom';
  ctx.fillText('100', 470, 218);

  ctx.font = '600 20px Inter, Arial, sans-serif';
  ctx.fillStyle = '#CFEAF5';
  ctx.fillText('REAIS', 468, 72);

  // Fita bancária central com o texto "10.000"
  ctx.fillStyle = '#F4EFE4';
  ctx.fillRect(206, 0, 100, 256);
  ctx.strokeStyle = '#C7BFA8';
  ctx.lineWidth = 3;
  ctx.strokeRect(206, 0, 100, 256);

  ctx.save();
  ctx.translate(256, 128);
  ctx.rotate(-Math.PI / 2);
  ctx.fillStyle = '#161614';
  ctx.font = 'bold 28px Inter, Arial, sans-serif';
  ctx.textAlign = 'center';
  ctx.textBaseline = 'middle';
  ctx.fillText('10.000', 0, 0);
  ctx.restore();

  // Textura lateral (folhas de papel-moeda + fita)
  const sideCanvas = document.createElement('canvas');
  sideCanvas.width = 256;
  sideCanvas.height = 64;
  const sctx = sideCanvas.getContext('2d');

  sctx.fillStyle = '#D5E3EA';
  sctx.fillRect(0, 0, 256, 64);
  sctx.fillStyle = '#89A8B8';
  for (let y = 2; y < 64; y += 5) {
    sctx.fillRect(0, y, 256, 2);
  }
  // Cinta bancária na lateral
  sctx.fillStyle = '#F4EFE4';
  sctx.fillRect(103, 0, 50, 64);

  const topTexture = new THREE.CanvasTexture(topCanvas);
  topTexture.colorSpace = THREE.SRGBColorSpace;
  topTexture.anisotropy = 4;

  const sideTexture = new THREE.CanvasTexture(sideCanvas);
  sideTexture.colorSpace = THREE.SRGBColorSpace;

  const topMat = new THREE.MeshStandardMaterial({
    map: topTexture,
    roughness: 0.55,
    metalness: 0.04
  });
  const sideMat = new THREE.MeshStandardMaterial({
    map: sideTexture,
    roughness: 0.7,
    metalness: 0.02
  });

  const geometry = new THREE.BoxGeometry(0.36, 0.075, 0.18);
  geometry.translate(0, 0.0375, 0);

  // Ordem de faces do BoxGeometry: +x, -x, +y (topo), -y (base), +z, -z
  const materials = [sideMat, sideMat, topMat, topMat, sideMat, sideMat];

  return {
    geometry,
    materials,
    textures: [topTexture, sideTexture],
    itemRadius: 0.19,
    itemHeight: 0.075
  };
}

