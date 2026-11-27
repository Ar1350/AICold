/**
 * Thin client for the Java game API.
 */

export async function fetchState() {
  const res = await fetch("/api/state");
  if (!res.ok) throw new Error("Failed to load state");
  return res.json();
}

export async function tick(input) {
  const res = await fetch("/api/tick", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(input),
  });
  if (!res.ok) throw new Error("Tick failed");
  return res.json();
}

export async function restart() {
  const res = await fetch("/api/restart", { method: "POST" });
  if (!res.ok) throw new Error("Restart failed");
  return res.json();
}

export async function setControl(playerId) {
  const res = await fetch("/api/control", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ player: playerId }),
  });
  if (!res.ok) throw new Error("Control change failed");
  return res.json();
}
