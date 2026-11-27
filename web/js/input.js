/**
 * Keyboard capture for the human-controlled fighter.
 */

const KEY_MAP = {
  ArrowUp: "up",
  ArrowDown: "down",
  ArrowLeft: "left",
  ArrowRight: "right",
  w: "up",
  W: "up",
  s: "down",
  S: "down",
  a: "left",
  A: "left",
  d: "right",
  D: "right",
};

export function createInput() {
  const state = { up: false, down: false, left: false, right: false };

  function onKey(event, pressed) {
    const dir = KEY_MAP[event.key];
    if (!dir) return;
    event.preventDefault();
    state[dir] = pressed;
  }

  window.addEventListener("keydown", (e) => onKey(e, true));
  window.addEventListener("keyup", (e) => onKey(e, false));
  window.addEventListener("blur", () => {
    state.up = state.down = state.left = state.right = false;
  });

  return {
    snapshot() {
      return { ...state };
    },
  };
}
