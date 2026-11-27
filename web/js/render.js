/**
 * Canvas renderer for arena state from the server.
 */

const TRAIL_MAX = 18;

export function createRenderer(canvas) {
  const ctx = canvas.getContext("2d");
  const trails = new Map();
  let pulse = 0;

  function ensureTrail(id) {
    if (!trails.has(id)) trails.set(id, []);
    return trails.get(id);
  }

  function drawBackground(arena) {
    const g = ctx.createLinearGradient(0, 0, arena.width, arena.height);
    g.addColorStop(0, "#0d1815");
    g.addColorStop(0.5, "#101c18");
    g.addColorStop(1, "#0b1411");
    ctx.fillStyle = g;
    ctx.fillRect(0, 0, arena.width, arena.height);

    // Soft grid
    ctx.strokeStyle = "rgba(232,242,236,0.05)";
    ctx.lineWidth = 1;
    const step = 40;
    for (let x = step; x < arena.width; x += step) {
      ctx.beginPath();
      ctx.moveTo(x, 0);
      ctx.lineTo(x, arena.height);
      ctx.stroke();
    }
    for (let y = step; y < arena.height; y += step) {
      ctx.beginPath();
      ctx.moveTo(0, y);
      ctx.lineTo(arena.width, y);
      ctx.stroke();
    }

    // Arena rim glow
    ctx.strokeStyle = "rgba(240,180,41,0.22)";
    ctx.lineWidth = 3;
    ctx.strokeRect(2, 2, arena.width - 4, arena.height - 4);
  }

  function drawPlayer(player, humanId) {
    const trail = ensureTrail(player.id);
    if (player.alive) {
      trail.push({ x: player.x, y: player.y });
      if (trail.length > TRAIL_MAX) trail.shift();
    } else if (trail.length) {
      trail.shift();
    }

    // Motion trail
    for (let i = 0; i < trail.length; i++) {
      const t = trail[i];
      const alpha = ((i + 1) / trail.length) * 0.28;
      ctx.beginPath();
      ctx.fillStyle = hexToRgba(player.color, alpha);
      ctx.arc(t.x, t.y, player.radius * (0.35 + 0.45 * (i / trail.length)), 0, Math.PI * 2);
      ctx.fill();
    }

    if (!player.alive) {
      ctx.beginPath();
      ctx.fillStyle = "rgba(120,130,125,0.35)";
      ctx.arc(player.x, player.y, player.radius * 0.7, 0, Math.PI * 2);
      ctx.fill();
      ctx.fillStyle = "rgba(200,210,205,0.45)";
      ctx.font = "bold 14px sans-serif";
      ctx.textAlign = "center";
      ctx.textBaseline = "middle";
      ctx.fillText("✕", player.x, player.y);
      return;
    }

    // Soft aura
    const aura = ctx.createRadialGradient(player.x, player.y, player.radius * 0.2, player.x, player.y, player.radius * 1.8);
    aura.addColorStop(0, hexToRgba(player.color, 0.35));
    aura.addColorStop(1, hexToRgba(player.color, 0));
    ctx.fillStyle = aura;
    ctx.beginPath();
    ctx.arc(player.x, player.y, player.radius * 1.8, 0, Math.PI * 2);
    ctx.fill();

    // Body
    ctx.beginPath();
    ctx.fillStyle = player.color;
    ctx.arc(player.x, player.y, player.radius, 0, Math.PI * 2);
    ctx.fill();

    // Highlight
    ctx.beginPath();
    ctx.fillStyle = "rgba(255,255,255,0.28)";
    ctx.arc(player.x - player.radius * 0.3, player.y - player.radius * 0.35, player.radius * 0.35, 0, Math.PI * 2);
    ctx.fill();

    // Number
    ctx.fillStyle = "#0a100e";
    ctx.font = "bold 16px Trebuchet MS, sans-serif";
    ctx.textAlign = "center";
    ctx.textBaseline = "middle";
    ctx.fillText(String(player.number), player.x, player.y + 1);

    // Human ring pulse
    if (player.id === humanId) {
      const r = player.radius + 6 + Math.sin(pulse) * 2;
      ctx.beginPath();
      ctx.strokeStyle = "rgba(240,180,41,0.85)";
      ctx.lineWidth = 2;
      ctx.setLineDash([5, 4]);
      ctx.arc(player.x, player.y, r, 0, Math.PI * 2);
      ctx.stroke();
      ctx.setLineDash([]);
    }
  }

  function draw(state) {
    pulse += 0.12;
    const arena = state.arena || { width: canvas.width, height: canvas.height };
    drawBackground(arena);
    for (const player of state.players || []) {
      drawPlayer(player, state.humanControlled);
    }
  }

  function resetTrails() {
    trails.clear();
  }

  return { draw, resetTrails };
}

function hexToRgba(hex, alpha) {
  const h = hex.replace("#", "");
  const r = parseInt(h.slice(0, 2), 16);
  const g = parseInt(h.slice(2, 4), 16);
  const b = parseInt(h.slice(4, 6), 16);
  return `rgba(${r},${g},${b},${alpha})`;
}
