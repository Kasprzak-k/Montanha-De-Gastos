import * as THREE from 'three';
import { OrbitControls } from 'three/addons/controls/OrbitControls.js';
import { createHabitMeshAssets, createMoneyBundleAssets } from './HabitModels.js';
import {
  MAX_HABIT_INSTANCES,
  MAX_MONEY_INSTANCES,
  computeInstanceRepresentation,
  populateConicalPile,
  disposeMeshAssets
} from './PileBuilder.js';

const LEFT_X = -2.55;
const RIGHT_X = 2.55;
const BASE_HABIT_PEAK = 1.95;
const MAX_VISUAL_PEAK = 4.65;

export class Stage3D {
  constructor(canvas) {
    this.canvas = canvas;
    this.reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    this.autoRotate = !this.reducedMotion;

    this.currentHabitId = 'CAFE';
    this.habitAssets = null;
    this.habitMesh = null;
    this.habitGroup = new THREE.Group();
    this.habitGroup.position.set(LEFT_X, 0, 0);

    this.moneyAssets = null;
    this.moneyMesh = null;
    this.moneyGroup = new THREE.Group();
    this.moneyGroup.position.set(RIGHT_X, 0, 0);

    this.habitScaleCurrent = 1;
    this.habitScaleTarget = 1;
    this.moneyScaleYCurrent = 1;
    this.moneyScaleYTarget = 1;
    this.pendingSwap = null;

    this.initScene();
    this.buildStaticMoneyMesh();
    this.buildHabitMesh('CAFE', 10950);
    this.populateMoneyInstances(16, 1.9);
    this.bindResize();
    this.lastTime = performance.now();
    this.animate = this.animate.bind(this);
    this.rafId = requestAnimationFrame(this.animate);
  }

  initScene() {
    this.scene = new THREE.Scene();
    this.scene.background = new THREE.Color(0xEAE6DD);

    const width = this.canvas.clientWidth || 960;
    const height = this.canvas.clientHeight || 420;

    this.camera = new THREE.PerspectiveCamera(34, width / height, 0.1, 150);
    this.defaultCameraPos = new THREE.Vector3(0, 3.85, 9.6);
    this.defaultTarget = new THREE.Vector3(0, 1.65, 0);
    this.camera.position.copy(this.defaultCameraPos);

    this.renderer = new THREE.WebGLRenderer({
      canvas: this.canvas,
      antialias: true,
      powerPreference: 'high-performance'
    });
    this.renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2));
    this.renderer.setSize(width, height, false);
    this.renderer.shadowMap.enabled = true;
    this.renderer.shadowMap.type = THREE.PCFSoftShadowMap;

    const ambient = new THREE.AmbientLight(0xFFFDF8, 0.88);
    this.scene.add(ambient);

    const hemi = new THREE.HemisphereLight(0xFFFDF9, 0xD2CBBF, 0.55);
    hemi.position.set(0, 12, 0);
    this.scene.add(hemi);

    const dirLight = new THREE.DirectionalLight(0xFFF9EE, 1.35);
    dirLight.position.set(5, 11, 7);
    dirLight.castShadow = true;
    dirLight.shadow.mapSize.width = 2048;
    dirLight.shadow.mapSize.height = 2048;
    dirLight.shadow.camera.near = 1;
    dirLight.shadow.camera.far = 30;
    const d = 7.0;
    dirLight.shadow.camera.left = -d;
    dirLight.shadow.camera.right = d;
    dirLight.shadow.camera.top = d;
    dirLight.shadow.camera.bottom = -d;
    dirLight.shadow.bias = -0.001;
    dirLight.shadow.normalBias = 0.025;
    this.scene.add(dirLight);

    const floorGeom = new THREE.PlaneGeometry(40, 40);
    const floorMat = new THREE.ShadowMaterial({ opacity: 0.14, depthWrite: false });
    const floor = new THREE.Mesh(floorGeom, floorMat);
    floor.rotation.x = -Math.PI / 2;
    floor.position.y = -0.062;
    floor.receiveShadow = true;
    this.scene.add(floor);

    const pedestalGeom = new THREE.CylinderGeometry(2.15, 2.22, 0.06, 48);
    const leftPedestalMat = new THREE.MeshStandardMaterial({
      color: 0xDFD8CC,
      roughness: 0.9,
      polygonOffset: true,
      polygonOffsetFactor: 1,
      polygonOffsetUnits: 1
    });
    const rightPedestalMat = new THREE.MeshStandardMaterial({
      color: 0xD3E0D7,
      roughness: 0.9,
      polygonOffset: true,
      polygonOffsetFactor: 1,
      polygonOffsetUnits: 1
    });

    const leftPedestal = new THREE.Mesh(pedestalGeom, leftPedestalMat);
    leftPedestal.position.set(LEFT_X, -0.03, 0);
    leftPedestal.receiveShadow = true;
    this.scene.add(leftPedestal);

    const rightPedestal = new THREE.Mesh(pedestalGeom, rightPedestalMat);
    rightPedestal.position.set(RIGHT_X, -0.03, 0);
    rightPedestal.receiveShadow = true;
    this.scene.add(rightPedestal);

    this.scene.add(this.habitGroup);
    this.scene.add(this.moneyGroup);

    this.controls = new OrbitControls(this.camera, this.canvas);
    this.controls.target.copy(this.defaultTarget);
    this.controls.enableDamping = true;
    this.controls.dampingFactor = 0.07;
    this.controls.enablePan = false;
    this.controls.minDistance = 5.5;
    this.controls.maxDistance = 18;
    this.controls.minPolarAngle = Math.PI / 8;
    this.controls.maxPolarAngle = Math.PI / 2.12;
    this.controls.autoRotate = this.autoRotate;
    this.controls.autoRotateSpeed = 0.35;
    this.controls.update();
  }

  buildStaticMoneyMesh() {
    this.moneyAssets = createMoneyBundleAssets();
    this.moneyMesh = new THREE.InstancedMesh(
      this.moneyAssets.geometry,
      this.moneyAssets.materials,
      MAX_MONEY_INSTANCES
    );
    this.moneyMesh.instanceMatrix.setUsage(THREE.DynamicDrawUsage);
    this.moneyMesh.castShadow = true;
    this.moneyMesh.receiveShadow = false;
    this.moneyGroup.add(this.moneyMesh);
  }

  buildHabitMesh(habitoId, quantidadeItens) {
    if (this.habitMesh || this.habitAssets) {
      disposeMeshAssets(this.habitMesh, this.habitAssets);
      this.habitMesh = null;
      this.habitAssets = null;
    }

    this.currentHabitId = habitoId;
    this.habitAssets = createHabitMeshAssets(habitoId);
    const material = this.habitAssets.materials[0];

    this.habitMesh = new THREE.InstancedMesh(
      this.habitAssets.geometry,
      material,
      MAX_HABIT_INSTANCES
    );
    this.habitMesh.instanceMatrix.setUsage(THREE.DynamicDrawUsage);
    this.habitMesh.castShadow = true;
    this.habitMesh.receiveShadow = false;
    this.habitGroup.add(this.habitMesh);

    return this.populateHabitInstances(quantidadeItens);
  }

  populateHabitInstances(quantidadeItens) {
    const rep = computeInstanceRepresentation(quantidadeItens, MAX_HABIT_INSTANCES);
    const countFactor = Math.min(1.18, Math.max(0.55, Math.log10(Math.max(10, rep.visualCount)) / 3.8));
    this.currentHabitPeak = BASE_HABIT_PEAK * countFactor;
    const baseRadius = Math.min(1.85, Math.max(0.95, 1.72 * countFactor));

    populateConicalPile(this.habitMesh, rep.visualCount, {
      itemRadius: this.habitAssets.itemRadius,
      itemHeight: this.habitAssets.itemHeight,
      baseRadius,
      targetPeakHeight: this.currentHabitPeak,
      maxTilt: 0.06,
      seed: 101
    });

    return rep;
  }

  populateMoneyInstances(macosCedulas, multiplicador) {
    const safeMacos = Math.max(0, Math.floor(Number(macosCedulas) || 0));
    this.moneyMesh.userData.macosCedulas = safeMacos;
    if (safeMacos === 0) {
      this.moneyMesh.count = 0;
      this.moneyMesh.instanceMatrix.needsUpdate = true;
      return;
    }

    const mult = Math.max(0.25, Number(multiplicador) || 1.0);
    const basePeak = this.currentHabitPeak || BASE_HABIT_PEAK;

    // A altura da montanha direita reflete diretamente o multiplicador em relação à esquerda (com teto visual)
    const desiredRightPeak = Math.min(MAX_VISUAL_PEAK, Math.max(0.65, basePeak * mult));
    const rightBaseRadius = Math.min(1.92, Math.max(1.05, 1.42 * Math.pow(mult, 0.35)));

    // Quantidade de maços visuais dimensionada para empilhar camada sobre camada até desiredRightPeak sem flutuar
    const volumeSupportCount = Math.round(180 * desiredRightPeak * rightBaseRadius);
    const visualBundles = Math.min(
      MAX_MONEY_INSTANCES,
      Math.max(safeMacos, volumeSupportCount + safeMacos * 10)
    );

    populateConicalPile(this.moneyMesh, visualBundles, {
      itemRadius: this.moneyAssets.itemRadius,
      itemHeight: this.moneyAssets.itemHeight,
      baseRadius: rightBaseRadius,
      targetPeakHeight: desiredRightPeak,
      maxTilt: 0.03,
      seed: 202
    });
  }

  updateFromSimulation(habitoId, simulationResponse) {
    const targetHabit = (habitoId || 'CAFE').toUpperCase();
    const quantidadeItens = simulationResponse.quantidadeItens ?? 0;
    const macosCedulas = simulationResponse.macosCedulas ?? 0;
    const multiplicador = simulationResponse.multiplicador ?? 1.0;

    const rep = computeInstanceRepresentation(quantidadeItens, MAX_HABIT_INSTANCES);

    if (targetHabit !== this.currentHabitId) {
      if (this.reducedMotion) {
        this.buildHabitMesh(targetHabit, quantidadeItens);
        this.populateMoneyInstances(macosCedulas, multiplicador);
        this.habitScaleCurrent = 1;
        this.habitScaleTarget = 1;
        this.habitGroup.scale.set(1, 1, 1);
      } else {
        this.pendingSwap = {
          habitoId: targetHabit,
          quantidadeItens,
          macosCedulas,
          multiplicador
        };
        this.habitScaleTarget = 0.001;
      }
      return rep;
    }

    this.populateHabitInstances(quantidadeItens);
    this.populateMoneyInstances(macosCedulas, multiplicador);

    if (!this.reducedMotion) {
      this.habitScaleCurrent = 0.88;
      this.habitScaleTarget = 1.0;
      this.moneyScaleYCurrent = 0.84;
      this.moneyScaleYTarget = 1.0;
    }

    return rep;
  }

  resetCamera() {
    const isNarrow = (this.canvas.clientWidth || window.innerWidth) < 680;
    const zDist = isNarrow ? 12.8 : 9.6;
    this.camera.position.set(0, 3.85, zDist);
    this.controls.target.copy(this.defaultTarget);
    this.controls.update();
  }

  toggleAutoRotate() {
    this.autoRotate = !this.autoRotate;
    this.controls.autoRotate = this.autoRotate;
    return this.autoRotate;
  }

  bindResize() {
    const handleResize = () => {
      if (!this.canvas) return;
      const width = this.canvas.clientWidth;
      const height = this.canvas.clientHeight;
      if (width === 0 || height === 0) return;

      this.camera.aspect = width / height;
      if (width < 680 && this.camera.position.z < 11.5) {
        this.camera.position.z = 12.8;
      }
      this.camera.updateProjectionMatrix();
      this.renderer.setSize(width, height, false);
    };

    this.resizeObserver = new ResizeObserver(handleResize);
    this.resizeObserver.observe(this.canvas);
    window.addEventListener('resize', handleResize);
    handleResize();
  }

  animate(now) {
    this.rafId = requestAnimationFrame(this.animate);
    const dt = Math.min(0.1, (now - this.lastTime) / 1000);
    this.lastTime = now;

    const speed = 12;
    this.habitScaleCurrent += (this.habitScaleTarget - this.habitScaleCurrent) * Math.min(1, dt * speed);

    if (this.pendingSwap && this.habitScaleCurrent <= 0.04) {
      const swap = this.pendingSwap;
      this.pendingSwap = null;
      this.buildHabitMesh(swap.habitoId, swap.quantidadeItens);
      this.populateMoneyInstances(swap.macosCedulas, swap.multiplicador);
      this.habitScaleCurrent = 0.02;
      this.habitScaleTarget = 1.0;
      this.moneyScaleYCurrent = 0.78;
      this.moneyScaleYTarget = 1.0;
    }

    this.habitGroup.scale.set(
      this.habitScaleCurrent,
      this.habitScaleCurrent,
      this.habitScaleCurrent
    );

    this.moneyScaleYCurrent += (this.moneyScaleYTarget - this.moneyScaleYCurrent) * Math.min(1, dt * 10);
    this.moneyGroup.scale.set(1, this.moneyScaleYCurrent, 1);

    this.controls.update();
    this.renderer.render(this.scene, this.camera);
  }
}
