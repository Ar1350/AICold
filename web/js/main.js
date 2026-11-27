import { createInput } from "./input.js";
import { createRenderer } from "./render.js";
import { createUi } from "./ui.js";
import * as api from "./api.js";

const TICK_MS = 1000 / 60;

const canvas = document.getElementById("arena");
const input = createInput();
const renderer = createRenderer(canvas);
const ui = createUi();

let running = true;
let busy = false;
let lastState = null;

async function applyState(state) {
  lastState = state;
  renderer.draw(state);
  ui.sync(state, async (playerId) => {
    try {
      const next = await api.setControl(playerId);
      await applyState(next);
    } catch (err) {
      console.error(err);
    }
  });
  running = state.status === "RUNNING";
}

async function restartGame() {
  try {
    ui.resetLogTracking();
    renderer.resetTrails();
    const state = await api.restart();
    await applyState(state);
  } catch (err) {
    console.error(err);
  }
}

document.getElementById("restart-btn").addEventListener("click", restartGame);
document.getElementById("restart-side").addEventListener("click", restartGame);

async function loop() {
  if (busy) return;
  busy = true;
  try {
    if (running) {
      const state = await api.tick(input.snapshot());
      await applyState(state);
    } else if (lastState) {
      // Keep drawing pulse on finished screen
      renderer.draw(lastState);
    }
  } catch (err) {
    console.error(err);
  } finally {
    busy = false;
  }
}

async function boot() {
  try {
    const state = await api.fetchState();
    await applyState(state);
  } catch (err) {
    console.error("Could not reach game server", err);
    const ctx = canvas.getContext("2d");
    ctx.fillStyle = "#0d1815";
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    ctx.fillStyle = "#e8f2ec";
    ctx.font = "16px sans-serif";
    ctx.textAlign = "center";
    ctx.fillText("Cannot reach /api — is the Java server running?", canvas.width / 2, canvas.height / 2);
  }
  setInterval(loop, TICK_MS);
}

boot();
