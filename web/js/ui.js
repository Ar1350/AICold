/**
 * DOM updates: elimination log, control picker, end overlay.
 */

export function createUi() {
  const logEl = document.getElementById("log");
  const overlay = document.getElementById("overlay");
  const overlayTitle = document.getElementById("overlay-title");
  const overlaySub = document.getElementById("overlay-sub");
  const picker = document.getElementById("control-picker");
  let lastElimCount = -1;
  let pickerBuilt = false;

  function buildPicker(state, onSelect) {
    if (pickerBuilt) {
      updatePickerActive(state.humanControlled);
      return;
    }
    picker.innerHTML = "";
    for (const p of state.players) {
      const btn = document.createElement("button");
      btn.type = "button";
      btn.dataset.id = p.id;
      btn.innerHTML = `<span class="dot" style="background:${p.color}"></span>${p.name}`;
      btn.addEventListener("click", () => onSelect(p.id));
      picker.appendChild(btn);
    }
    pickerBuilt = true;
    updatePickerActive(state.humanControlled);
  }

  function updatePickerActive(humanId) {
    for (const btn of picker.querySelectorAll("button")) {
      btn.classList.toggle("active", btn.dataset.id === humanId);
    }
  }

  function updateLog(state) {
    const events = state.eliminations || [];
    if (events.length === lastElimCount) return;
    lastElimCount = events.length;
    logEl.innerHTML = "";
    if (!events.length) {
      const li = document.createElement("li");
      li.className = "empty";
      li.textContent = "No eliminations yet — hunt carefully.";
      logEl.appendChild(li);
      return;
    }
    for (const e of events) {
      const li = document.createElement("li");
      li.textContent = e.message;
      logEl.appendChild(li);
    }
    logEl.scrollTop = logEl.scrollHeight;
  }

  function updateOverlay(state) {
    if (state.status !== "FINISHED") {
      overlay.classList.add("hidden");
      return;
    }
    overlay.classList.remove("hidden");
    if (state.winner) {
      overlayTitle.textContent = `${state.winner.name} wins!`;
      overlayTitle.style.color = state.winner.color;
      overlaySub.textContent = "Last survivor. Restart to play again.";
    } else {
      overlayTitle.textContent = "Draw";
      overlayTitle.style.color = "";
      overlaySub.textContent = "No survivors left.";
    }
  }

  function sync(state, onSelectControl) {
    buildPicker(state, onSelectControl);
    updateLog(state);
    updateOverlay(state);
  }

  function resetLogTracking() {
    lastElimCount = -1;
  }

  return { sync, resetLogTracking };
}
