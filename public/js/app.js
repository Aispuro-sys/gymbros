const API = '/api';
let token = localStorage.getItem('gym_bros_token');
let currentUser = null;
let exerciseLimit = 24;
let routineView = 'list';
let calendarMonth = new Date().getMonth();
let calendarYear = new Date().getFullYear();
let weeklyRoutines = [];
let allRoutinesWithGifs = [];
let currentPage = 'overview';
let pollingIntervals = {};

// ===== Theme =====
function initTheme() {
  const saved = localStorage.getItem('gym_bros_theme') || 'dark';
  document.documentElement.setAttribute('data-theme', saved);
}
function toggleTheme() {
  const current = document.documentElement.getAttribute('data-theme') || 'dark';
  const next = current === 'dark' ? 'light' : 'dark';
  document.documentElement.setAttribute('data-theme', next);
  localStorage.setItem('gym_bros_theme', next);
}
initTheme();

// ===== API Helper =====
async function apiCall(endpoint, method = 'GET', body = null) {
  const headers = { 'Content-Type': 'application/json' };
  if (token) headers['Authorization'] = `Bearer ${token}`;
  const res = await fetch(`${API}${endpoint}`, {
    method, headers,
    body: body ? JSON.stringify(body) : null,
  });
  const data = await res.json();
  if (!res.ok) throw new Error(data.error || 'Request failed');
  return data;
}

// ===== Auth =====
function showRegister() {
  document.getElementById('login-view').style.display = 'none';
  document.getElementById('register-view').style.display = 'block';
}
function showLogin() {
  document.getElementById('register-view').style.display = 'none';
  document.getElementById('login-view').style.display = 'block';
}
function showAuthError(id, msg) {
  const el = document.getElementById(id);
  el.textContent = msg; el.classList.add('show');
}

document.getElementById('login-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  document.getElementById('login-error').classList.remove('show');
  try {
    const data = await apiCall('/auth/login', 'POST', {
      email: document.getElementById('login-email').value,
      password: document.getElementById('login-password').value,
    });
    token = data.token;
    localStorage.setItem('gym_bros_token', token);
    currentUser = data.user;
    showDashboard();
  } catch (err) { showAuthError('login-error', err.message); }
});

document.getElementById('register-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  document.getElementById('register-error').classList.remove('show');
  try {
    const data = await apiCall('/auth/register', 'POST', {
      username: document.getElementById('reg-username').value,
      email: document.getElementById('reg-email').value,
      password: document.getElementById('reg-password').value,
      age: parseInt(document.getElementById('reg-age').value) || undefined,
      height_cm: parseFloat(document.getElementById('reg-height').value) || undefined,
      weight_kg: parseFloat(document.getElementById('reg-weight').value) || undefined,
      goal: document.getElementById('reg-goal').value,
      body_type: document.getElementById('reg-body-type').value || undefined,
      gender: document.getElementById('reg-gender').value || 'M',
    });
    token = data.token;
    localStorage.setItem('gym_bros_token', token);
    currentUser = data.user;
    showDashboard();
  } catch (err) { showAuthError('register-error', err.message); }
});

function logout() {
  token = null; currentUser = null;
  localStorage.removeItem('gym_bros_token');
  document.getElementById('dashboard-page').style.display = 'none';
  document.getElementById('auth-page').style.display = 'flex';
}

// ===== Dashboard =====
async function showDashboard() {
  document.getElementById('auth-page').style.display = 'none';
  document.getElementById('dashboard-page').style.display = 'flex';
  try {
    const data = await apiCall('/auth/me');
    currentUser = data.user;
  } catch { logout(); return; }

  const u = currentUser;
  document.getElementById('user-name').textContent = u.username;
  document.getElementById('user-goal').textContent = goalLabel(u.goal);
  document.getElementById('user-avatar').textContent = u.username.charAt(0).toUpperCase();
  document.getElementById('today-date').textContent = new Date().toLocaleDateString('es-ES', {
    weekday: 'long', year: 'numeric', month: 'long', day: 'numeric',
  });
  checkAIStatus();
  navigateTo('overview');
}

function goalLabel(g) {
  return { BULKING: 'Volumen', CUTTING: 'Definición', MAINTENANCE: 'Mantener' }[g] || 'Mantener';
}
function timeLabel(t) {
  return { MORNING: 'Mañana', PRE_WORKOUT: 'Pre-entreno', NIGHT: 'Noche' }[t] || t;
}
function emptyState(text) {
  return `<div class="empty-state"><div class="empty-state-icon">—</div><div class="empty-state-text">${text}</div></div>`;
}
function loadingHtml() {
  return `<div class="loading"><div class="loading-spinner"></div><div>Generando...</div></div>`;
}

function navigateTo(page) {
  currentPage = page;
  clearPolling();
  document.querySelectorAll('.page-section').forEach((s) => s.classList.remove('active'));
  document.querySelectorAll('.nav-item').forEach((n) => n.classList.remove('active'));
  document.querySelectorAll('.bottom-nav-item').forEach((n) => n.classList.remove('active'));
  document.getElementById(`page-${page}`).classList.add('active');
  const sidebarItem = document.querySelector(`.nav-item[data-page="${page}"]`);
  if (sidebarItem) sidebarItem.classList.add('active');
  const bottomItem = document.querySelector(`.bottom-nav-item[data-page="${page}"]`);
  if (bottomItem) bottomItem.classList.add('active');
  window.scrollTo(0, 0);
  if (page === 'overview') { loadOverview(); startPolling('overview', () => loadOverview(), 30000); }
  if (page === 'ai-coach') loadAICoach();
  if (page === 'routines') loadRoutines();
  if (page === 'exercises') loadExercises();
  if (page === 'nutrition') { loadMacros(); startPolling('nutrition', () => loadMacros(), 30000); }
  if (page === 'supplements') loadSupplements();
  if (page === 'teams') loadTeams();
  if (page === 'recipes') loadRecipes();
  if (page === 'community') { loadCommunityFeed(); startPolling('community', () => loadCommunityFeed(true), 5000); }
  if (page === 'profile') loadProfile();
}

function clearPolling() {
  Object.values(pollingIntervals).forEach((id) => clearInterval(id));
  pollingIntervals = {};
}

function startPolling(name, fn, intervalMs) {
  if (pollingIntervals[name]) clearInterval(pollingIntervals[name]);
  pollingIntervals[name] = setInterval(fn, intervalMs);
}

// ===== AI Status =====
async function checkAIStatus() {
  try {
    const data = await apiCall('/ai/status');
    const title = document.getElementById('ai-status-title');
    const desc = document.getElementById('ai-status-desc');
    if (data.ai_powered) {
      title.textContent = 'IA Coach — Activo (OpenAI)';
      desc.textContent = 'Conectado a GPT-4o. Rutinas generadas con IA real basadas en tu perfil.';
    } else {
      title.textContent = 'IA Coach — Modo local';
      desc.textContent = 'Generando rutinas con algoritmo inteligente basado en 1,324 ejercicios. Agrega OPENAI_API_KEY al .env para activar GPT-4o.';
    }
  } catch {}
}

// ===== Exercise GIF helper =====
let completedExercises = [];

function exerciseGifHtml(ex) {
  const gif = ex.gif_url ? `/exercises-dataset/${ex.gif_url}` : null;
  const img = ex.image ? `/exercises-dataset/${ex.image}` : null;
  const mediaSrc = gif || img;
  const isDone = completedExercises.includes(ex.id);
  return `
    <div class="exercise-with-gif ${isDone ? 'exercise-done' : ''}">
      ${mediaSrc ? `<img src="${mediaSrc}" alt="${ex.name}" class="exercise-gif" loading="lazy" />` : ''}
      <div class="exercise-info">
        <div class="exercise-name">${ex.name}</div>
        <div class="exercise-stats-row">
          <span>${ex.sets} series</span>
          <span>${ex.reps} reps</span>
          <span>${ex.rest_seconds}s descanso</span>
        </div>
      </div>
      <label class="exercise-check" onclick="toggleExercise('${ex.routine_id || ''}', '${ex.id}', event)">
        <input type="checkbox" ${isDone ? 'checked' : ''} />
        <span class="exercise-check-mark"></span>
      </label>
    </div>
  `;
}

async function loadCompletedExercises() {
  try {
    const data = await apiCall('/routines/completed-today');
    completedExercises = data.completed || [];
  } catch (err) { console.error('Completed exercises error:', err); }
}

async function toggleExercise(routineId, exerciseId, event) {
  if (event) { event.preventDefault(); event.stopPropagation(); }
  try {
    await apiCall(`/routines/${routineId}/exercises/${exerciseId}/toggle`, 'POST');
    if (completedExercises.includes(exerciseId)) {
      completedExercises = completedExercises.filter((id) => id !== exerciseId);
    } else {
      completedExercises.push(exerciseId);
    }
    if (currentPage === 'routines') renderRoutinesList();
    if (currentPage === 'overview') loadOverview();
  } catch (err) { alert(err.message); }
}

// ===== Overview =====
async function loadOverview() {
  try {
    const [routinesData, macrosData, suppData] = await Promise.all([
      apiCall('/ai/routines-with-gifs'),
      apiCall(`/macros?date=${new Date().toISOString().split('T')[0]}`),
      apiCall('/supplements'),
    ]);
    const u = currentUser;
    const todayLog = macrosData.logs[0];
    const todayDow = new Date().getDay() === 0 ? 7 : new Date().getDay();
    const todayRoutine = routinesData.routines.find((r) => r.day_of_week === todayDow);
    document.getElementById('stats-grid').innerHTML = `
      <div class="stat-card"><div class="stat-label">Peso</div><div class="stat-value">${u.weight_kg || '--'}<span class="stat-unit"> kg</span></div></div>
      <div class="stat-card"><div class="stat-label">Calorías hoy</div><div class="stat-value">${todayLog ? todayLog.calories : 0}<span class="stat-unit"> kcal</span></div></div>
      <div class="stat-card"><div class="stat-label">Rutinas</div><div class="stat-value">${routinesData.routines.length}</div></div>
      <div class="stat-card"><div class="stat-label">Objetivo</div><div class="stat-value" style="font-size:1.3rem;">${goalLabel(u.goal)}</div></div>
    `;
    if (todayRoutine) {
      document.getElementById('overview-routine').innerHTML = `
        <div style="margin-bottom:8px;"><strong>${todayRoutine.name}</strong>
        <span class="list-item-badge badge-ai" style="margin-left:6px;">${todayRoutine.ai_generated ? 'IA' : 'Manual'}</span></div>
        ${todayRoutine.exercises.map((ex) => exerciseGifHtml(ex)).join('')}
      `;
    } else {
      const latest = routinesData.routines[0];
      if (latest) {
        document.getElementById('overview-routine').innerHTML = `
          <div style="margin-bottom:8px;"><strong>${latest.name}</strong>
          <span class="list-item-badge ${latest.ai_generated ? 'badge-ai' : 'badge-manual'}" style="margin-left:6px;">${latest.ai_generated ? 'IA' : 'Manual'}</span></div>
          ${latest.exercises.map((ex) => exerciseGifHtml(ex)).join('')}
        `;
      } else {
        document.getElementById('overview-routine').innerHTML = emptyState('Sin rutinas. Usa el IA Coach para crear un plan semanal.');
      }
    }
    if (todayLog) {
      document.getElementById('overview-macros').innerHTML = `
        <div class="exercise-row"><span class="exercise-name">Calorías</span><span class="exercise-stat">${todayLog.calories} kcal</span></div>
        <div class="exercise-row"><span class="exercise-name">Proteína</span><span class="exercise-stat">${todayLog.protein_g}g</span></div>
        <div class="exercise-row"><span class="exercise-name">Carbs</span><span class="exercise-stat">${todayLog.carbs_g}g</span></div>
        <div class="exercise-row"><span class="exercise-name">Grasas</span><span class="exercise-stat">${todayLog.fats_g}g</span></div>
      `;
    } else {
      document.getElementById('overview-macros').innerHTML = emptyState('Sin registro de macros hoy');
    }
    if (suppData.supplements.length > 0) {
      document.getElementById('overview-supplements').innerHTML = suppData.supplements.map((s) => `
        <div class="list-item"><div><div class="list-item-name">${s.name}</div><div class="list-item-meta">${s.dosage} · ${s.is_medication ? 'Medicina' : 'Suplemento'}</div></div>
        <span class="list-item-badge badge-${s.time_of_day.toLowerCase().replace('_', '-')}">${timeLabel(s.time_of_day)}</span></div>
      `).join('');
    } else {
      document.getElementById('overview-supplements').innerHTML = emptyState('Sin suplementos registrados');
    }
  } catch (err) { console.error('Overview error:', err); }
}

// ===== AI Coach =====
function loadAICoach() {
  document.getElementById('ai-weekly-result').innerHTML = '';
  document.getElementById('ai-routine-result').innerHTML = '';
  checkProfileComplete();
}

function checkProfileComplete() {
  const u = currentUser;
  const missing = [];
  if (!u.weight_kg) missing.push('peso');
  if (!u.height_cm) missing.push('altura');
  if (!u.goal) missing.push('objetivo');
  if (!u.body_type) missing.push('tipo de cuerpo');

  const banner = document.getElementById('ai-profile-warning');
  if (banner) banner.remove();

  if (missing.length > 0) {
    const html = `<div class="auth-error show" id="ai-profile-warning" style="margin-bottom:1rem;">
      Tu perfil está incompleto. Faltan: <strong>${missing.join(', ')}</strong>.
      <a onclick="navigateTo('profile')" style="color:var(--accent); cursor:pointer; text-decoration:underline; margin-left:6px;">Ir a Perfil</a>
    </div>`;
    const aiSection = document.getElementById('page-ai-coach');
    aiSection.insertAdjacentHTML('afterbegin', html);
    const btn = document.getElementById('ai-generate-btn');
    if (btn) { btn.disabled = true; btn.style.opacity = '0.5'; btn.textContent = 'Completa tu perfil primero'; }
  } else {
    const btn = document.getElementById('ai-generate-btn');
    if (btn) { btn.disabled = false; btn.style.opacity = '1'; btn.textContent = 'Generar plan semanal'; }
  }
  return missing.length === 0;
}

async function generateWeeklyPlan(e) {
  e.preventDefault();
  if (!checkProfileComplete()) return;
  const btn = document.getElementById('ai-generate-btn');
  btn.disabled = true; btn.textContent = 'Generando plan...';
  document.getElementById('ai-weekly-result').innerHTML = loadingHtml();
  try {
    const data = await apiCall('/ai/generate-weekly', 'POST', {
      days_per_week: parseInt(document.getElementById('ai-days').value),
      equipment: document.getElementById('ai-equipment').value,
      notes: document.getElementById('ai-notes').value,
    });
    document.getElementById('ai-weekly-result').innerHTML = `
      <div class="card" style="margin-top:1.5rem;">
        <div class="card-header">
          <div class="card-title">${data.plan_name}</div>
          <span class="list-item-badge badge-ai">${data.ai_powered ? 'GPT-4o' : 'Algoritmo local'}</span>
        </div>
        ${data.ai_notes ? `<div class="ai-notes"><strong>Coach IA:</strong> ${data.ai_notes}</div>` : ''}
      </div>
      ${data.days.map((day) => `
        <div class="routine-card">
          <div class="routine-header">
            <div>
              <div class="routine-name">${day.name}</div>
              <span class="list-item-badge badge-ai" style="margin-top:4px; display:inline-block;">Día ${day.day_of_week}</span>
            </div>
          </div>
          ${day.exercises.map((ex) => exerciseGifHtml(ex)).join('')}
        </div>
      `).join('')}
    `;
    btn.textContent = 'Generar otro plan';
  } catch (err) {
    document.getElementById('ai-weekly-result').innerHTML = `<div class="auth-error show">${err.message}</div>`;
    btn.textContent = 'Generar plan semanal';
  }
  btn.disabled = false;
}

async function generateNutritionPlan() {
  if (!checkProfileComplete()) return;
  const container = document.getElementById('ai-nutrition-result');
  container.innerHTML = loadingHtml();
  try {
    const data = await apiCall('/ai/nutrition-plan', 'POST', {});
    const p = data.plan;
    let html = `
      <div class="exercise-row"><span class="exercise-name">Calorías objetivo</span><span class="exercise-stat">${p.calories} kcal</span></div>
      <div class="exercise-row"><span class="exercise-name">Proteína</span><span class="exercise-stat">${p.protein_g}g</span></div>
      <div class="exercise-row"><span class="exercise-name">Carbohidratos</span><span class="exercise-stat">${p.carbs_g}g</span></div>
      <div class="exercise-row"><span class="exercise-name">Grasas</span><span class="exercise-stat">${p.fats_g}g</span></div>
    `;
    if (p.meals && p.meals.length > 0) {
      html += `<div class="meal-section"><div class="meal-section-title">Comidas recomendadas</div>`;
      p.meals.forEach((meal) => {
        html += `
          <div class="meal-card">
            <div class="meal-header">
              <div class="meal-title">${meal.meal}</div>
              <div class="meal-macros">${meal.macros}</div>
            </div>
            <ul class="meal-foods">${meal.foods.map((f) => `<li>${f}</li>`).join('')}</ul>
          </div>
        `;
      });
      html += `</div>`;
    }
    if (p.notes) html += `<div class="ai-notes"><strong>Coach IA:</strong> ${p.notes}</div>`;
    container.innerHTML = html;
  } catch (err) {
    container.innerHTML = `<div class="auth-error show">${err.message}</div>`;
  }
}

// ===== Routines — Weekly/Monthly/List =====
function setRoutineView(view) {
  routineView = view;
  document.querySelectorAll('.view-toggle-btn').forEach((b) => b.classList.remove('active'));
  document.getElementById(`view-${view}-btn`).classList.add('active');
  document.getElementById('routine-weekly-view').style.display = view === 'week' ? 'block' : 'none';
  document.getElementById('routine-monthly-view').style.display = view === 'month' ? 'block' : 'none';
  document.getElementById('routine-list-view').style.display = view === 'list' ? 'block' : 'none';
  if (view === 'week') renderWeeklyCalendar();
  if (view === 'month') renderMonthlyCalendar();
}

async function loadRoutines() {
  try {
    const data = await apiCall('/ai/routines-with-gifs');
    allRoutinesWithGifs = data.routines;
    await loadCompletedExercises();
    renderRoutinesList();
    if (routineView === 'week') renderWeeklyCalendar();
    if (routineView === 'month') renderMonthlyCalendar();
  } catch (err) { console.error('Routines error:', err); }
}

function renderRoutinesList() {
  const list = document.getElementById('routines-list');
  if (allRoutinesWithGifs.length === 0) {
    list.innerHTML = emptyState('Sin rutinas. Usa el IA Coach para generar un plan semanal.');
    return;
  }
  list.innerHTML = allRoutinesWithGifs.map((r) => {
    const doneCount = r.exercises.filter((ex) => completedExercises.includes(ex.id)).length;
    const totalCount = r.exercises.length;
    const pct = totalCount > 0 ? Math.round((doneCount / totalCount) * 100) : 0;
    return `
    <div class="routine-card">
      <div class="routine-header">
        <div>
          <div class="routine-name">${r.name}</div>
          <span class="list-item-badge ${r.ai_generated ? 'badge-ai' : 'badge-manual'}" style="margin-top:4px; display:inline-block;">${r.ai_generated ? 'IA' : 'Manual'}</span>
        </div>
        <button class="logout-btn" onclick="deleteRoutine('${r.id}')" title="Eliminar">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
        </button>
      </div>
      ${totalCount > 0 ? `<div class="routine-progress"><div class="routine-progress-bar" style="width:${pct}%"></div><span class="routine-progress-text">${doneCount}/${totalCount} completados</span></div>` : ''}
      ${r.exercises.map((ex) => exerciseGifHtml(ex)).join('')}
    </div>
  `;
  }).join('');
}

const DAY_NAMES = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'];
const DAY_NAMES_FULL = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo'];

function renderWeeklyCalendar() {
  const today = new Date();
  const todayDow = today.getDay() === 0 ? 7 : today.getDay();
  const monday = new Date(today);
  monday.setDate(today.getDate() - todayDow + 1);

  const routineByDay = {};
  allRoutinesWithGifs.forEach((r) => {
    if (r.day_of_week) routineByDay[r.day_of_week] = r;
  });

  const html = DAY_NAMES.map((dayName, i) => {
    const dow = i + 1;
    const date = new Date(monday);
    date.setDate(monday.getDate() + i);
    const isToday = dow === todayDow;
    const routine = routineByDay[dow];
    const isRest = !routine;

    return `
      <div class="week-day ${isToday ? 'active' : ''} ${isRest ? 'rest' : ''}" onclick="showDayDetail(${dow})">
        <div class="week-day-header">
          <span>${dayName}</span>
          <span class="day-num">${date.getDate()}</span>
        </div>
        ${routine ? `
          <div class="week-day-focus">${routine.name.split(' - ')[1] || routine.name}</div>
          <div class="week-day-meta">${routine.exercises.length} ejercicios</div>
          <span class="week-day-badge">${routine.ai_generated ? 'IA' : 'Manual'}</span>
        ` : `
          <div class="week-day-focus">Descanso</div>
          <span class="week-day-badge rest-badge">Rest</span>
        `}
      </div>
    `;
  }).join('');

  document.getElementById('weekly-calendar').innerHTML = html;
  showDayDetail(todayDow);
}

function showDayDetail(dow) {
  const routine = allRoutinesWithGifs.find((r) => r.day_of_week === dow);
  const container = document.getElementById('week-day-detail');
  if (!routine) {
    container.innerHTML = `
      <div class="day-detail">
        <div class="day-detail-header">
          <div class="day-detail-title">${DAY_NAMES_FULL[dow - 1]}</div>
          <div class="day-detail-focus">Día de descanso</div>
        </div>
        <div class="empty-state"><div class="empty-state-text">Recupérate. Mañana se entrena.</div></div>
      </div>
    `;
    return;
  }
  container.innerHTML = `
    <div class="day-detail">
      <div class="day-detail-header">
        <div>
          <div class="day-detail-title">${DAY_NAMES_FULL[dow - 1]}</div>
          <div class="day-detail-focus">${routine.name}</div>
        </div>
        <span class="list-item-badge ${routine.ai_generated ? 'badge-ai' : 'badge-manual'}">${routine.ai_generated ? 'IA' : 'Manual'}</span>
      </div>
      ${routine.exercises.map((ex) => exerciseGifHtml(ex)).join('')}
    </div>
  `;
}

function renderMonthlyCalendar() {
  const monthNames = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];
  document.getElementById('monthly-title').textContent = `${monthNames[calendarMonth]} ${calendarYear}`;

  const firstDay = new Date(calendarYear, calendarMonth, 1);
  const lastDay = new Date(calendarYear, calendarMonth + 1, 0);
  let startDow = firstDay.getDay() === 0 ? 7 : firstDay.getDay();
  startDow -= 1;

  const today = new Date();
  const isCurrentMonth = today.getMonth() === calendarMonth && today.getFullYear() === calendarYear;

  const routineByDay = {};
  allRoutinesWithGifs.forEach((r) => {
    if (r.day_of_week) routineByDay[r.day_of_week] = r;
  });

  let html = DAY_NAMES.map((d) => `<div class="monthly-day-header">${d}</div>`).join('');

  for (let i = 0; i < startDow; i++) {
    html += `<div class="monthly-cell empty"></div>`;
  }

  for (let d = 1; d <= lastDay.getDate(); d++) {
    const date = new Date(calendarYear, calendarMonth, d);
    const dow = date.getDay() === 0 ? 7 : date.getDay();
    const hasRoutine = !!routineByDay[dow];
    const isToday = isCurrentMonth && d === today.getDate();

    html += `
      <div class="monthly-cell ${isToday ? 'today' : ''} ${hasRoutine ? 'has-routine' : 'rest-day'}" onclick="showMonthDayDetail(${dow}, ${d})">
        <div class="day-num">${d}</div>
        <div class="day-dot"></div>
      </div>
    `;
  }

  document.getElementById('monthly-grid').innerHTML = html;
}

function showMonthDayDetail(dow, dayNum) {
  const routine = allRoutinesWithGifs.find((r) => r.day_of_week === dow);
  const container = document.getElementById('month-day-detail');
  if (!routine) {
    container.innerHTML = `
      <div class="day-detail">
        <div class="day-detail-header">
          <div class="day-detail-title">${dayNum} — ${DAY_NAMES_FULL[dow - 1]}</div>
          <div class="day-detail-focus">Día de descanso</div>
        </div>
      </div>
    `;
    return;
  }
  container.innerHTML = `
    <div class="day-detail">
      <div class="day-detail-header">
        <div>
          <div class="day-detail-title">${dayNum} — ${DAY_NAMES_FULL[dow - 1]}</div>
          <div class="day-detail-focus">${routine.name}</div>
        </div>
        <span class="list-item-badge ${routine.ai_generated ? 'badge-ai' : 'badge-manual'}">${routine.ai_generated ? 'IA' : 'Manual'}</span>
      </div>
      ${routine.exercises.map((ex) => exerciseGifHtml(ex)).join('')}
    </div>
  `;
}

function prevMonth() {
  calendarMonth--;
  if (calendarMonth < 0) { calendarMonth = 11; calendarYear--; }
  renderMonthlyCalendar();
}
function nextMonth() {
  calendarMonth++;
  if (calendarMonth > 11) { calendarMonth = 0; calendarYear++; }
  renderMonthlyCalendar();
}

async function deleteRoutine(id) {
  if (!confirm('¿Eliminar rutina?')) return;
  try {
    await apiCall(`/routines/${id}`, 'DELETE');
    loadRoutines();
  } catch (err) { alert(err.message); }
}

function openRoutineModal() {
  showModal(`
    <div class="modal-header"><div class="modal-title">Nueva rutina</div><button class="modal-close" onclick="closeModal()">&times;</button></div>
    <form onsubmit="createRoutine(event)">
      <div class="form-group"><label>Nombre</label><input type="text" class="form-input" id="routine-name" placeholder="Push Day" required /></div>
      <div id="exercises-container"></div>
      <button type="button" class="btn-secondary" style="margin-bottom:0.75rem;" onclick="addExerciseField()">+ Ejercicio</button>
      <button type="submit" class="btn-primary">Crear</button>
    </form>
  `);
  addExerciseField();
}

function addExerciseField() {
  const container = document.getElementById('exercises-container');
  const index = container.children.length;
  const div = document.createElement('div');
  div.style.cssText = 'border:1px solid var(--border); border-radius:6px; padding:0.75rem; margin-bottom:0.5rem;';
  div.innerHTML = `
    <div class="form-group"><label>Ejercicio ${index + 1}</label><input type="text" class="form-input ex-name" placeholder="Press Banca" required /></div>
    <div class="form-row">
      <div class="form-group"><label>Series</label><input type="number" class="form-input ex-sets" value="4" min="1" /></div>
      <div class="form-group"><label>Reps</label><input type="text" class="form-input ex-reps" value="8-12" /></div>
      <div class="form-group"><label>Descanso</label><input type="number" class="form-input ex-rest" value="90" min="0" /></div>
    </div>
  `;
  container.appendChild(div);
}

async function createRoutine(e) {
  e.preventDefault();
  const exercises = [];
  document.querySelectorAll('#exercises-container > div').forEach((div) => {
    exercises.push({
      name: div.querySelector('.ex-name').value,
      sets: parseInt(div.querySelector('.ex-sets').value) || 3,
      reps: div.querySelector('.ex-reps').value || '8-12',
      rest_seconds: parseInt(div.querySelector('.ex-rest').value) || 90,
    });
  });
  try {
    await apiCall('/routines', 'POST', {
      name: document.getElementById('routine-name').value, ai_generated: false, exercises,
    });
    closeModal(); loadRoutines();
  } catch (err) { alert(err.message); }
}

// ===== Exercise Browser =====
async function loadExercises() {
  try {
    const cats = await apiCall('/exercises/categories');
    const select = document.getElementById('exercise-filter-category');
    select.innerHTML = '<option value="">Todas las categorías</option>' +
      cats.categories.map((c) => `<option value="${c.value}">${c.label}</option>`).join('');
    searchExercises();
  } catch (err) { console.error('Exercise categories error:', err); }
}

async function searchExercises() {
  const q = document.getElementById('exercise-search').value;
  const category = document.getElementById('exercise-filter-category').value;
  const grid = document.getElementById('exercises-grid');
  try {
    const data = await apiCall(`/exercises?q=${encodeURIComponent(q)}&category=${encodeURIComponent(category)}&limit=${exerciseLimit}`);
    grid.innerHTML = data.exercises.map((ex) => `
      <div class="exercise-tile" onclick="viewExercise('${ex.id}')">
        <img src="/exercises-dataset/${ex.image}" alt="${ex.name}" loading="lazy" />
        <div class="exercise-tile-name">${ex.name}</div>
        <div class="exercise-tile-meta">${ex.category_es} · ${ex.equipment_es}</div>
      </div>
    `).join('');
    document.getElementById('load-more-btn').style.display = data.exercises.length >= exerciseLimit ? 'inline-block' : 'none';
  } catch (err) {
    grid.innerHTML = `<div class="empty-state"><div class="empty-state-text">Error cargando ejercicios</div></div>`;
  }
}

function loadMoreExercises() { exerciseLimit += 24; searchExercises(); }

function viewExercise(id) {
  apiCall(`/exercises/${id}`).then((data) => {
    const ex = data.exercise;
    const steps = ex.instruction_steps && ex.instruction_steps.es ? ex.instruction_steps.es : [];
    showModal(`
      <div class="modal-header"><div class="modal-title">${ex.name}</div><button class="modal-close" onclick="closeModal()">&times;</button></div>
      <div style="text-align:center; margin-bottom:1rem;">
        <img src="/exercises-dataset/${ex.gif_url}" alt="${ex.name}" style="width:180px; height:180px; border-radius:8px; border:1px solid var(--border);" />
      </div>
      <div style="display:flex; gap:6px; flex-wrap:wrap; margin-bottom:1rem;">
        <span class="list-item-badge badge-manual">${ex.category_es}</span>
        <span class="list-item-badge badge-manual">${ex.equipment_es}</span>
        <span class="list-item-badge badge-manual">${ex.target_es}</span>
        ${ex.muscle_group_es ? `<span class="list-item-badge badge-manual">${ex.muscle_group_es}</span>` : ''}
      </div>
      ${steps.length > 0 ? `
        <div style="margin-bottom:1rem;">
          <div style="font-size:0.75rem; font-weight:600; color:var(--text-2); text-transform:uppercase; letter-spacing:0.5px; margin-bottom:0.5rem;">Instrucciones</div>
          <ol style="padding-left:1.25rem; font-size:0.85rem; color:var(--text-2); line-height:1.6;">
            ${steps.map((s) => `<li style="margin-bottom:0.4rem;">${s}</li>`).join('')}
          </ol>
        </div>
      ` : ''}
      ${ex.secondary_muscles_es && ex.secondary_muscles_es.length > 0 ? `<div style="font-size:0.8rem; color:var(--text-3);">Músculos secundarios: ${ex.secondary_muscles_es.join(', ')}</div>` : ''}
    `);
  }).catch((err) => alert(err.message));
}

// ===== Macros & Meals =====
const MEAL_TYPE_LABELS = { BREAKFAST: 'Desayuno', LUNCH: 'Comida', DINNER: 'Cena', SNACK: 'Snack', POST_WORKOUT: 'Post-entreno' };

async function loadMacros() {
  try {
    const [macrosData, mealsData] = await Promise.all([
      apiCall('/macros'),
      apiCall('/meals'),
    ]);

    const list = document.getElementById('macros-list');
    if (macrosData.logs.length === 0) {
      list.innerHTML = emptyState('Sin registros. Registra tu primer dia.');
    } else {
      list.innerHTML = macrosData.logs.map((l) => `
        <div class="list-item"><div>
          <div class="list-item-name">${new Date(l.date).toLocaleDateString('es-ES', { weekday: 'short', day: 'numeric', month: 'short' })}</div>
          <div class="list-item-meta">${l.calories} kcal · P:${l.protein_g}g · C:${l.carbs_g}g · G:${l.fats_g}g</div>
        </div></div>
      `).join('');
    }

    renderMeals(mealsData);
    renderDailySummary(mealsData);
  } catch (err) { console.error('Macros error:', err); }
}

function renderDailySummary(mealsData) {
  const summary = document.getElementById('daily-calories-summary');
  if (!summary) return;
  const t = mealsData.totals;
  const confirmedCount = mealsData.meals.filter((m) => m.confirmed).length;
  summary.innerHTML = `
    <div class="card">
      <div class="card-header"><div class="card-title">Resumen de hoy</div></div>
      <div class="calories-summary-grid">
        <div class="calories-stat"><div class="calories-stat-val">${t.calories}</div><div class="calories-stat-label">kcal</div></div>
        <div class="calories-stat"><div class="calories-stat-val">${t.protein_g}g</div><div class="calories-stat-label">Proteina</div></div>
        <div class="calories-stat"><div class="calories-stat-val">${t.carbs_g}g</div><div class="calories-stat-label">Carbos</div></div>
        <div class="calories-stat"><div class="calories-stat-val">${t.fats_g}g</div><div class="calories-stat-label">Grasas</div></div>
      </div>
      <div class="calories-meals-count">${mealsData.meals.length} comidas · ${confirmedCount} confirmadas con foto</div>
    </div>
  `;
}

function renderMeals(mealsData) {
  const list = document.getElementById('meals-list');
  if (!list) return;
  if (mealsData.meals.length === 0) {
    list.innerHTML = emptyState('Sin comidas registradas hoy. Usa Foto Comida para agregar.');
    return;
  }
  list.innerHTML = mealsData.meals.map((m) => `
    <div class="meal-item">
      <div class="meal-item-info">
        <div class="meal-item-name">${m.name}</div>
        <div class="meal-item-meta">${MEAL_TYPE_LABELS[m.meal_type] || m.meal_type} · ${m.calories} kcal · P:${m.protein_g}g C:${m.carbs_g}g G:${m.fats_g}g</div>
        ${m.confirmed ? '<span class="meal-confirmed-badge">Confirmada</span>' : '<span class="meal-unconfirmed-badge">Sin confirmar</span>'}
      </div>
      <div class="meal-item-actions">
        ${m.photo_url ? `<img src="${m.photo_url}" class="meal-thumb" onclick="viewMealPhoto('${m.photo_url}')" />` : `<button class="btn-secondary" style="width:auto; padding:6px 10px; font-size:0.7rem;" onclick="confirmMealPhoto('${m.id}')">Confirmar</button>`}
        <button class="logout-btn" onclick="deleteMeal('${m.id}')" title="Eliminar">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
        </button>
      </div>
    </div>
  `).join('');
}

function viewMealPhoto(url) {
  showModal(`<div style="text-align:center;"><img src="${url}" style="max-width:100%; border-radius:8px;" /></div>`);
}

async function confirmMealPhoto(mealId) {
  const input = document.createElement('input');
  input.type = 'file';
  input.accept = 'image/*';
  input.capture = 'environment';
  input.onchange = (e) => {
    const file = e.target.files[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = async (ev) => {
      try {
        await apiCall(`/meals/${mealId}/confirm`, 'PUT', { photo_url: ev.target.result });
        loadMacros();
      } catch (err) { alert(err.message); }
    };
    reader.readAsDataURL(file);
  };
  input.click();
}

async function deleteMeal(id) {
  if (!confirm('Eliminar esta comida?')) return;
  try {
    await apiCall(`/meals/${id}`, 'DELETE');
    loadMacros();
  } catch (err) { alert(err.message); }
}

function openMacrosModal() {
  const today = new Date().toISOString().split('T')[0];
  showModal(`
    <div class="modal-header"><div class="modal-title">Registrar macros</div><button class="modal-close" onclick="closeModal()">&times;</button></div>
    <form onsubmit="saveMacros(event)">
      <div class="form-group"><label>Fecha</label><input type="date" class="form-input" id="macro-date" value="${today}" required /></div>
      <div class="form-row">
        <div class="form-group"><label>Calorías</label><input type="number" class="form-input" id="macro-calories" placeholder="2500" min="0" /></div>
        <div class="form-group"><label>Proteína (g)</label><input type="number" class="form-input" id="macro-protein" placeholder="180" min="0" /></div>
      </div>
      <div class="form-row">
        <div class="form-group"><label>Carbs (g)</label><input type="number" class="form-input" id="macro-carbs" placeholder="300" min="0" /></div>
        <div class="form-group"><label>Grasas (g)</label><input type="number" class="form-input" id="macro-fats" placeholder="70" min="0" /></div>
      </div>
      <button type="submit" class="btn-primary">Guardar</button>
    </form>
  `);
}

async function saveMacros(e) {
  e.preventDefault();
  try {
    await apiCall('/macros', 'POST', {
      date: document.getElementById('macro-date').value,
      calories: parseInt(document.getElementById('macro-calories').value) || 0,
      protein_g: parseInt(document.getElementById('macro-protein').value) || 0,
      carbs_g: parseInt(document.getElementById('macro-carbs').value) || 0,
      fats_g: parseInt(document.getElementById('macro-fats').value) || 0,
    });
    closeModal(); loadMacros();
  } catch (err) { alert(err.message); }
}

// ===== Meal recommendations in nutrition page =====
async function loadMealRecommendations() {
  if (!checkProfileComplete()) {
    const section = document.getElementById('nutrition-meals-section');
    section.style.display = 'block';
    section.innerHTML = `<div class="auth-error show">Tu perfil está incompleto. Ve a Perfil para registrar tus datos primero. <a onclick="navigateTo('profile')" style="color:var(--accent); cursor:pointer; text-decoration:underline; margin-left:6px;">Ir a Perfil</a></div>`;
    return;
  }
  const section = document.getElementById('nutrition-meals-section');
  section.style.display = 'block';
  section.innerHTML = loadingHtml();
  try {
    const data = await apiCall('/ai/nutrition-plan', 'POST', {});
    const p = data.plan;
    let html = `
      <div class="card" style="margin-bottom:1rem;">
        <div class="card-header"><div class="card-title">Macros objetivo — ${goalLabel(currentUser.goal)}</div></div>
        <div class="exercise-row"><span class="exercise-name">Calorías</span><span class="exercise-stat">${p.calories} kcal</span></div>
        <div class="exercise-row"><span class="exercise-name">Proteína</span><span class="exercise-stat">${p.protein_g}g</span></div>
        <div class="exercise-row"><span class="exercise-name">Carbs</span><span class="exercise-stat">${p.carbs_g}g</span></div>
        <div class="exercise-row"><span class="exercise-name">Grasas</span><span class="exercise-stat">${p.fats_g}g</span></div>
      </div>
      <div class="meal-section-title">Comidas recomendadas para ${goalLabel(currentUser.goal).toLowerCase()}</div>
    `;
    p.meals.forEach((meal) => {
      html += `
        <div class="meal-card">
          <div class="meal-header"><div class="meal-title">${meal.meal}</div><div class="meal-macros">${meal.macros}</div></div>
          <ul class="meal-foods">${meal.foods.map((f) => `<li>${f}</li>`).join('')}</ul>
        </div>
      `;
    });
    if (p.notes) html += `<div class="ai-notes"><strong>Coach IA:</strong> ${p.notes}</div>`;
    section.innerHTML = html;
  } catch (err) {
    section.innerHTML = `<div class="auth-error show">${err.message}</div>`;
  }
}

// ===== Supplements =====
async function loadSupplements() {
  try {
    const data = await apiCall('/supplements');
    const list = document.getElementById('supplements-list');
    if (data.supplements.length === 0) {
      list.innerHTML = emptyState('Sin suplementos registrados.');
      return;
    }
    list.innerHTML = data.supplements.map((s) => `
      <div class="list-item"><div><div class="list-item-name">${s.name}</div><div class="list-item-meta">${s.dosage} · ${s.is_medication ? 'Medicina' : 'Suplemento'}</div></div>
      <div style="display:flex; align-items:center; gap:10px;">
        <span class="list-item-badge badge-${s.time_of_day.toLowerCase().replace('_', '-')}">${timeLabel(s.time_of_day)}</span>
        <button class="logout-btn" onclick="deleteSupplement('${s.id}')"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg></button>
      </div></div>
    `).join('');
  } catch (err) { console.error('Supplements error:', err); }
}

async function deleteSupplement(id) {
  if (!confirm('¿Eliminar?')) return;
  try { await apiCall(`/supplements/${id}`, 'DELETE'); loadSupplements(); } catch (err) { alert(err.message); }
}

function openSupplementModal() {
  showModal(`
    <div class="modal-header"><div class="modal-title">Agregar suplemento</div><button class="modal-close" onclick="closeModal()">&times;</button></div>
    <form onsubmit="createSupplement(event)">
      <div class="form-group"><label>Nombre</label><input type="text" class="form-input" id="supp-name" placeholder="Creatina monohidratada" required /></div>
      <div class="form-group"><label>Dosis</label><input type="text" class="form-input" id="supp-dosage" placeholder="5g" required /></div>
      <div class="form-row">
        <div class="form-group"><label>Momento</label><select class="form-input" id="supp-time"><option value="MORNING">Mañana</option><option value="PRE_WORKOUT">Pre-entreno</option><option value="NIGHT">Noche</option></select></div>
        <div class="form-group"><label>Tipo</label><select class="form-input" id="supp-med"><option value="false">Suplemento</option><option value="true">Medicina</option></select></div>
      </div>
      <button type="submit" class="btn-primary">Agregar</button>
    </form>
  `);
}

async function createSupplement(e) {
  e.preventDefault();
  try {
    await apiCall('/supplements', 'POST', {
      name: document.getElementById('supp-name').value,
      dosage: document.getElementById('supp-dosage').value,
      time_of_day: document.getElementById('supp-time').value,
      is_medication: document.getElementById('supp-med').value === 'true',
    });
    closeModal(); loadSupplements();
  } catch (err) { alert(err.message); }
}

// ===== Teams =====
let currentTeamId = null;

async function loadTeams() {
  try {
    const data = await apiCall('/teams');
    const list = document.getElementById('teams-list');
    if (data.teams.length === 0) {
      list.innerHTML = emptyState('No perteneces a ningun equipo. Crea uno o unete con un codigo.');
      return;
    }
    list.innerHTML = data.teams.map((t) => `
      <div class="routine-card team-card" onclick="openTeamDetail('${t.id}')" style="cursor:pointer;">
        <div class="routine-header"><div>
          <div class="routine-name">${t.name}</div>
          <div class="list-item-meta">${t.members.length} miembro(s) · Tu rol: ${t.role === 'ADMIN' ? 'Admin' : 'Miembro'}</div>
        </div>
        <span class="list-item-badge ${t.role === 'ADMIN' ? 'badge-ai' : 'badge-manual'}">${t.role === 'ADMIN' ? 'Admin' : 'Miembro'}</span></div>
        <div style="margin-top:0.5rem; font-size:0.75rem; color:var(--text-3);">Codigo: <strong style="color:var(--text-2);">${t.invite_code}</strong></div>
      </div>
    `).join('');
  } catch (err) { console.error('Teams error:', err); }
}

async function openTeamDetail(teamId) {
  currentTeamId = teamId;
  document.getElementById('teams-list-view').style.display = 'none';
  const detail = document.getElementById('team-detail-view');
  detail.style.display = 'block';
  detail.innerHTML = loadingHtml();

  await refreshTeamDetail(teamId);

  startPolling('teamDetail', () => refreshTeamDetail(teamId, true), 5000);
}

async function refreshTeamDetail(teamId, isPolling) {
  try {
    const data = await apiCall(`/teams/${teamId}`);
    const { team, role } = data;
    const goalLabels = { BULK: 'Volumen', CUT: 'Definicion', MAINTENANCE: 'Mantener' };
    const btLabels = { ECTOMORPH: 'Ectomorfo', MESOMORPH: 'Mesomorfo', ENDOMORPH: 'Endomorfo' };
    const detail = document.getElementById('team-detail-view');
    if (!detail) return;

    let feedInputValue = '';
    if (isPolling) {
      const input = document.getElementById('feed-post-input');
      if (input) feedInputValue = input.value;
    }

    detail.innerHTML = `
      <div class="page-header">
        <div>
          <h1 class="page-title">${team.name}</h1>
          <div class="page-date">${team.members.length} miembros · ${role === 'ADMIN' ? 'Admin' : 'Miembro'}</div>
        </div>
        <div style="display:flex; gap:8px; flex-wrap:wrap;">
          <button class="btn-secondary" style="width:auto; padding:8px 14px;" onclick="copyInviteCode('${team.invite_code}')">Copiar codigo</button>
          <button class="btn-secondary" style="width:auto; padding:8px 14px;" onclick="openShareRoutineModal()">Compartir rutina</button>
          <button class="btn-secondary" style="width:auto; padding:8px 14px; color:var(--danger);" onclick="leaveTeam('${team.id}')">Salir</button>
          <button class="btn-secondary" style="width:auto; padding:8px 14px;" onclick="backToTeamsList()">Volver</button>
        </div>
      </div>

      <div class="content-grid">
        <div class="card">
          <div class="card-header"><div class="card-title">Miembros (${team.members.length})</div></div>
          ${team.members.map((m) => `
            <div class="list-item">
              <div style="display:flex; align-items:center; gap:8px;">
                <div class="user-avatar" style="width:28px; height:28px; font-size:11px;">${m.user.username.charAt(0).toUpperCase()}</div>
                <div>
                  <div class="list-item-name">${m.user.username}${m.user.id === currentUser.id ? ' (tu)' : ''}</div>
                  <div class="list-item-meta">${m.role === 'ADMIN' ? 'Admin' : 'Miembro'}${m.user.goal ? ' · ' + (goalLabels[m.user.goal] || m.user.goal) : ''}${m.user.body_type ? ' · ' + (btLabels[m.user.body_type] || m.user.body_type) : ''}</div>
                </div>
              </div>
              <span class="list-item-badge ${m.role === 'ADMIN' ? 'badge-ai' : 'badge-manual'}">${m.role === 'ADMIN' ? 'Admin' : 'Miembro'}</span>
            </div>
          `).join('')}
        </div>

        <div class="card">
          <div class="card-header"><div class="card-title">Rutinas compartidas (${team.shared_routines.length})</div></div>
          ${team.shared_routines.length === 0 ? emptyState('Sin rutinas compartidas') : team.shared_routines.map((sr) => `
            <div class="routine-card" style="margin-bottom:0.5rem;">
              <div class="routine-header">
                <div>
                  <div class="routine-name">${sr.routine.name}</div>
                  <div class="list-item-meta">Compartida por ${sr.user.username} · ${sr.routine.exercises.length} ejercicios</div>
                </div>
                ${sr.routine.user_id === currentUser.id ? '' : `<button class="btn-secondary" style="width:auto; padding:6px 12px; font-size:0.72rem;" onclick="copyRoutine('${team.id}', '${sr.routine_id}')">Copiar</button>`}
              </div>
              ${sr.routine.exercises.map((ex) => `
                <div class="exercise-row" style="grid-template-columns: 1fr auto; font-size:0.75rem; padding:4px 0;">
                  <span class="exercise-name">${ex.name}</span>
                  <span class="exercise-stat">${ex.sets}x${ex.reps}</span>
                </div>
              `).join('')}
            </div>
          `).join('')}
        </div>
      </div>

      <div class="card" style="margin-top:1rem;">
        <div class="card-header"><div class="card-title">Feed del equipo</div></div>
        <form onsubmit="postToFeed(event)" style="margin-bottom:1rem;">
          <div style="display:flex; gap:8px;">
            <input type="text" class="form-input" id="feed-post-input" placeholder="Escribe algo al equipo..." style="flex:1;" />
            <button type="submit" class="btn-primary" style="width:auto; padding:10px 16px;">Publicar</button>
          </div>
        </form>
        <div id="team-feed">
          ${team.posts.length === 0 ? emptyState('Sin publicaciones. Se el primero!') : team.posts.map((p) => `
            <div class="feed-post">
              <div class="feed-post-header">
                <div class="user-avatar" style="width:28px; height:28px; font-size:11px;">${p.user.username.charAt(0).toUpperCase()}</div>
                <div>
                  <div class="feed-post-author">${p.user.username}</div>
                  <div class="feed-post-time">${new Date(p.created_at).toLocaleDateString('es-ES', { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' })}</div>
                </div>
              </div>
              <div class="feed-post-content">${p.content}</div>
            </div>
          `).join('')}
        </div>
      </div>
    `;
    if (feedInputValue) {
      const input = document.getElementById('feed-post-input');
      if (input) input.value = feedInputValue;
    }
  } catch (err) {
    detail.innerHTML = `<div class="auth-error show">${err.message}</div>`;
  }
}

function backToTeamsList() {
  clearPolling();
  document.getElementById('teams-list-view').style.display = 'block';
  document.getElementById('team-detail-view').style.display = 'none';
  currentTeamId = null;
  loadTeams();
}

function copyInviteCode(code) {
  navigator.clipboard.writeText(code).then(() => {
    alert('Codigo copiado: ' + code);
  }).catch(() => {
    prompt('Copia este codigo:', code);
  });
}

async function openShareRoutineModal() {
  try {
    const data = await apiCall('/routines');
    const options = data.routines.map((r) => `<option value="${r.id}">${r.name} (${r.exercises.length} ej.)</option>`).join('');
    showModal(`
      <div class="modal-header"><div class="modal-title">Compartir rutina al equipo</div><button class="modal-close" onclick="closeModal()">&times;</button></div>
      <form onsubmit="shareRoutine(event)">
        <div class="form-group"><label>Selecciona una rutina</label><select class="form-input" id="share-routine-id" required>${options}</select></div>
        <button type="submit" class="btn-primary">Compartir</button>
      </form>
    `);
  } catch (err) { alert(err.message); }
}

async function shareRoutine(e) {
  e.preventDefault();
  const routineId = document.getElementById('share-routine-id').value;
  try {
    await apiCall(`/teams/${currentTeamId}/share-routine`, 'POST', { routine_id: routineId });
    closeModal();
  } catch (err) { alert(err.message); }
}

async function copyRoutine(teamId, routineId) {
  if (!confirm('Copiar esta rutina a tus rutinas?')) return;
  try {
    await apiCall(`/teams/${teamId}/copy-routine/${routineId}`, 'POST');
    alert('Rutina copiada a tus rutinas');
  } catch (err) { alert(err.message); }
}

async function postToFeed(e) {
  e.preventDefault();
  const content = document.getElementById('feed-post-input').value.trim();
  if (!content) return;
  try {
    await apiCall(`/teams/${currentTeamId}/posts`, 'POST', { content });
    document.getElementById('feed-post-input').value = '';
  } catch (err) { alert(err.message); }
}

async function leaveTeam(teamId) {
  if (!confirm('Seguro que quieres salir del equipo?')) return;
  try {
    await apiCall(`/teams/${teamId}/leave`, 'DELETE');
    backToTeamsList();
  } catch (err) { alert(err.message); }
}

function openCreateTeamModal() {
  showModal(`<div class="modal-header"><div class="modal-title">Crear equipo</div><button class="modal-close" onclick="closeModal()">&times;</button></div>
  <form onsubmit="createTeam(event)"><div class="form-group"><label>Nombre</label><input type="text" class="form-input" id="team-name" placeholder="Los Bros" required /></div>
  <button type="submit" class="btn-primary">Crear</button></form>`);
}
async function createTeam(e) {
  e.preventDefault();
  try { await apiCall('/teams', 'POST', { name: document.getElementById('team-name').value }); closeModal(); loadTeams(); } catch (err) { alert(err.message); }
}

function openJoinTeamModal() {
  showModal(`<div class="modal-header"><div class="modal-title">Unirse a equipo</div><button class="modal-close" onclick="closeModal()">&times;</button></div>
  <form onsubmit="joinTeam(event)"><div class="form-group"><label>Codigo</label><input type="text" class="form-input" id="team-code" placeholder="GYM-ABC123" required style="text-transform:uppercase;" /></div>
  <button type="submit" class="btn-primary">Unirse</button></form>`);
}
async function joinTeam(e) {
  e.preventDefault();
  try { await apiCall('/teams/join', 'POST', { invite_code: document.getElementById('team-code').value.toUpperCase() }); closeModal(); loadTeams(); } catch (err) { alert(err.message); }
}

// ===== Photo Capture: Food & Supplements =====
function openFoodCamera() {
  const section = document.getElementById('food-photo-section');
  section.style.display = 'block';
  section.innerHTML = `
    <div class="card photo-capture-card">
      <div class="card-header"><div class="card-title">Analizar comida con IA</div><button class="modal-close" onclick="document.getElementById('food-photo-section').style.display='none'">&times;</button></div>
      <p style="font-size:0.8rem; color:var(--text-2); margin-bottom:1rem;">Toma una foto de tu comida y la IA estimara calorias, proteinas, carbos y grasas.</p>
      <div class="photo-capture-area">
        <input type="file" id="food-file-input" accept="image/*" capture="environment" style="display:none;" onchange="handleFoodPhoto(event)" />
        <button class="btn-primary photo-capture-btn" onclick="document.getElementById('food-file-input').click()">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:20px;height:20px;"><path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/><circle cx="12" cy="13" r="4"/></svg>
          Tomar foto / Subir imagen
        </button>
      </div>
      <div id="food-photo-preview" style="display:none;"></div>
      <div id="food-analysis-result"></div>
    </div>
  `;
}

async function handleFoodPhoto(event) {
  const file = event.target.files[0];
  if (!file) return;

  const reader = new FileReader();
  reader.onload = async (e) => {
    const dataUrl = e.target.result;
    document.getElementById('food-photo-preview').style.display = 'block';
    document.getElementById('food-photo-preview').innerHTML = `<img src="${dataUrl}" class="photo-preview-img" />`;
    document.getElementById('food-analysis-result').innerHTML = loadingHtml();

    try {
      const data = await apiCall('/ai/analyze-food', 'POST', { image: dataUrl });
      const a = data.analysis;
      const confidenceColor = a.confidence === 'high' ? 'var(--text)' : a.confidence === 'medium' ? 'var(--text-2)' : 'var(--danger)';
      let extraHtml = '';
      if (a.fiber_g != null && a.fiber_g > 0) extraHtml += `<div class="analysis-extra">Fibra: ${a.fiber_g}g</div>`;
      if (a.sugar_g != null && a.sugar_g > 0) extraHtml += `<div class="analysis-extra">Azucar: ${a.sugar_g}g</div>`;
      if (a.sodium_mg != null && a.sodium_mg > 0) extraHtml += `<div class="analysis-extra">Sodio: ${a.sodium_mg}mg</div>`;
      document.getElementById('food-analysis-result').innerHTML = `
        <div class="analysis-result-card">
          <div class="analysis-header">
            <div class="analysis-name">${a.food_name}</div>
            <span class="analysis-badge" style="color:${confidenceColor}; border-color:${confidenceColor};">${a.confidence || 'low'}</span>
          </div>
          ${a.estimated_portion && a.estimated_portion !== 'N/A' ? `<div class="analysis-portion">${a.estimated_portion}</div>` : ''}
          <div class="analysis-macros-grid">
            <div class="macro-pill"><div class="macro-pill-val">${a.calories}</div><div class="macro-pill-label">kcal</div></div>
            <div class="macro-pill"><div class="macro-pill-val">${a.protein_g}g</div><div class="macro-pill-label">Proteina</div></div>
            <div class="macro-pill"><div class="macro-pill-val">${a.carbs_g}g</div><div class="macro-pill-label">Carbos</div></div>
            <div class="macro-pill"><div class="macro-pill-val">${a.fats_g}g</div><div class="macro-pill-label">Grasas</div></div>
          </div>
          ${extraHtml}
          ${a.notes && a.notes !== 'N/A' ? `<div class="analysis-notes">${a.notes}</div>` : ''}
          <div style="display:flex; gap:8px; margin-top:0.75rem;">
            <button class="btn-primary" style="width:auto; padding:8px 16px;" onclick="saveFoodAsMacros(${a.calories}, ${a.protein_g}, ${a.carbs_g}, ${a.fats_g}, '${(a.food_name || 'Comida').replace(/'/g, "\\'")}', '${dataUrl}')">Registrar comida</button>
            <button class="btn-secondary" style="width:auto; padding:8px 16px;" onclick="openFoodCamera()">Otra foto</button>
          </div>
        </div>
      `;
    } catch (err) {
      document.getElementById('food-analysis-result').innerHTML = `<div class="auth-error show">${err.message}</div>`;
    }
  };
  reader.readAsDataURL(file);
}

function saveFoodAsMacros(cal, protein, carbs, fats, foodName, photoUrl) {
  const name = foodName || 'Comida';
  apiCall('/meals', 'POST', {
    name,
    meal_type: 'SNACK',
    calories: cal,
    protein_g: protein,
    carbs_g: carbs,
    fats_g: fats,
    photo_url: photoUrl || null,
  })
    .then(() => {
      document.getElementById('food-photo-section').style.display = 'none';
      loadMacros();
    })
    .catch((err) => alert(err.message));
}

function openSupplementCamera() {
  const section = document.getElementById('supplement-photo-section');
  section.style.display = 'block';
  section.innerHTML = `
    <div class="card photo-capture-card">
      <div class="card-header"><div class="card-title">Analizar etiqueta con IA</div><button class="modal-close" onclick="document.getElementById('supplement-photo-section').style.display='none'">&times;</button></div>
      <p style="font-size:0.8rem; color:var(--text-2); margin-bottom:1rem;">Toma una foto del frasco o etiqueta nutricional y la IA extraera la informacion del suplemento.</p>
      <div class="photo-capture-area">
        <input type="file" id="supp-file-input" accept="image/*" capture="environment" style="display:none;" onchange="handleSupplementPhoto(event)" />
        <button class="btn-primary photo-capture-btn" onclick="document.getElementById('supp-file-input').click()">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:20px;height:20px;"><path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/><circle cx="12" cy="13" r="4"/></svg>
          Tomar foto / Subir imagen
        </button>
      </div>
      <div id="supp-photo-preview" style="display:none;"></div>
      <div id="supp-analysis-result"></div>
    </div>
  `;
}

async function handleSupplementPhoto(event) {
  const file = event.target.files[0];
  if (!file) return;

  const reader = new FileReader();
  reader.onload = async (e) => {
    const dataUrl = e.target.result;
    document.getElementById('supp-photo-preview').style.display = 'block';
    document.getElementById('supp-photo-preview').innerHTML = `<img src="${dataUrl}" class="photo-preview-img" />`;
    document.getElementById('supp-analysis-result').innerHTML = loadingHtml();

    try {
      const data = await apiCall('/ai/analyze-supplement', 'POST', { image: dataUrl });
      const a = data.analysis;
      const confidenceColor = a.confidence === 'high' ? 'var(--text)' : a.confidence === 'medium' ? 'var(--text-2)' : 'var(--danger)';
      document.getElementById('supp-analysis-result').innerHTML = `
        <div class="analysis-result-card">
          <div class="analysis-header">
            <div class="analysis-name">${a.name}</div>
            <span class="analysis-badge" style="color:${confidenceColor}; border-color:${confidenceColor};">${a.confidence || 'low'}</span>
          </div>
          ${a.brand ? `<div class="analysis-brand">${a.brand}</div>` : ''}
          <div class="analysis-info-grid">
            <div class="analysis-info-item"><span>Categoria</span><strong>${a.category}</strong></div>
            <div class="analysis-info-item"><span>Porcion</span><strong>${a.serving_size}</strong></div>
            ${a.servings_per_container ? `<div class="analysis-info-item"><span>Porciones</span><strong>${a.servings_per_container}</strong></div>` : ''}
            ${a.calories ? `<div class="analysis-info-item"><span>Calorias</span><strong>${a.calories} kcal</strong></div>` : ''}
            ${a.protein_g ? `<div class="analysis-info-item"><span>Proteina</span><strong>${a.protein_g}g</strong></div>` : ''}
            ${a.carbs_g ? `<div class="analysis-info-item"><span>Carbos</span><strong>${a.carbs_g}g</strong></div>` : ''}
            ${a.fats_g ? `<div class="analysis-info-item"><span>Grasas</span><strong>${a.fats_g}g</strong></div>` : ''}
          </div>
          ${a.key_ingredients && a.key_ingredients.length > 0 ? `<div class="analysis-ingredients"><strong>Ingredientes:</strong> ${a.key_ingredients.join(', ')}</div>` : ''}
          ${a.dose_per_serving && a.dose_per_serving !== 'N/A' ? `<div class="analysis-extra">Dosis: ${a.dose_per_serving}</div>` : ''}
          ${a.usage_instructions ? `<div class="analysis-extra">Uso: ${a.usage_instructions}</div>` : ''}
          ${a.warnings ? `<div class="analysis-extra" style="color:var(--danger);">Advertencias: ${a.warnings}</div>` : ''}
          ${a.notes ? `<div class="analysis-notes">${a.notes}</div>` : ''}
          <div style="display:flex; gap:8px; margin-top:0.75rem;">
            <button class="btn-primary" style="width:auto; padding:8px 16px;" onclick='saveSupplementFromPhoto(${JSON.stringify(a).replace(/'/g, "&#39;")})'>Agregar suplemento</button>
            <button class="btn-secondary" style="width:auto; padding:8px 16px;" onclick="openSupplementCamera()">Otra foto</button>
          </div>
        </div>
      `;
    } catch (err) {
      document.getElementById('supp-analysis-result').innerHTML = `<div class="auth-error show">${err.message}</div>`;
    }
  };
  reader.readAsDataURL(file);
}

function saveSupplementFromPhoto(analysis) {
  const name = analysis.name || 'Suplemento';
  const dosage = analysis.dose_per_serving || analysis.serving_size || '';
  const timeOfDay = 'MORNING';
  apiCall('/supplements', 'POST', { name, dosage, time_of_day: timeOfDay, is_medication: false })
    .then(() => { alert('Suplemento agregado'); document.getElementById('supplement-photo-section').style.display = 'none'; loadSupplements(); })
    .catch((err) => alert(err.message));
}

// ===== Modal =====
function showModal(html) {
  document.getElementById('modal-content').innerHTML = html;
  document.getElementById('modal-overlay').classList.add('show');
}
function closeModal() { document.getElementById('modal-overlay').classList.remove('show'); }
document.getElementById('modal-overlay').addEventListener('click', (e) => { if (e.target.id === 'modal-overlay') closeModal(); });

// ===== Body Type & Gender Selection =====
const BODY_TYPE_IMAGES = {
  ECTOMORPH: { M: '/assets/images/Ectomorfo Masculino.png', F: '/assets/images/Ectomorfo Femenino.png' },
  MESOMORPH: { M: '/assets/images/Mesomorfo Masculino.png', F: '/assets/images/Mesomorfo Femenino.png' },
  ENDOMORPH: { M: '/assets/images/Endomorfo Masculino.png', F: '/assets/images/Endomorfo Femenino.png' },
};

function selectBodyType(prefix, type) {
  const grid = document.getElementById(`${prefix}-body-type-grid`);
  grid.querySelectorAll('.body-type-card').forEach((c) => c.classList.remove('selected'));
  grid.querySelector(`[data-body-type="${type}"]`).classList.add('selected');
  document.getElementById(`${prefix}-body-type`).value = type;
}

function selectGender(prefix, gender) {
  const toggle = document.getElementById(`${prefix}-gender-toggle`);
  if (!toggle) return;
  toggle.querySelectorAll('.gender-btn').forEach((b) => b.classList.remove('active'));
  toggle.querySelector(`[data-gender="${gender}"]`).classList.add('active');
  document.getElementById(`${prefix}-gender`).value = gender;

  const types = ['ECTOMORPH', 'MESOMORPH', 'ENDOMORPH'];
  types.forEach((type) => {
    const img = document.getElementById(`${prefix}-img-${type}`);
    if (img) img.src = BODY_TYPE_IMAGES[type][gender];
  });
}

// ===== Profile =====
function loadProfile() {
  const u = currentUser;
  document.getElementById('prof-username').value = u.username || '';
  document.getElementById('prof-email').value = u.email || '';
  document.getElementById('prof-age').value = u.age || '';
  document.getElementById('prof-height').value = u.height_cm || '';
  document.getElementById('prof-weight').value = u.weight_kg || '';
  document.getElementById('prof-goal').value = u.goal || 'MAINTENANCE';

  const userGender = u.gender || 'M';
  selectGender('prof', userGender);

  if (u.body_type) {
    selectBodyType('prof', u.body_type);
  }

  const bmiCard = document.getElementById('profile-bmi-card');
  if (u.weight_kg && u.height_cm) {
    const bmi = (u.weight_kg / Math.pow(u.height_cm / 100, 2));
    const bmiCat = bmi < 18.5 ? 'Bajo peso' : bmi < 25 ? 'Peso normal' : bmi < 30 ? 'Sobrepeso' : 'Obesidad';
    bmiCard.style.display = 'block';
    document.getElementById('profile-bmi-content').innerHTML = `
      <div class="exercise-row"><span class="exercise-name">IMC</span><span class="exercise-stat">${bmi.toFixed(1)}</span></div>
      <div class="exercise-row"><span class="exercise-name">Categoría</span><span class="exercise-stat">${bmiCat}</span></div>
      <div class="exercise-row"><span class="exercise-name">Peso</span><span class="exercise-stat">${u.weight_kg} kg</span></div>
      <div class="exercise-row"><span class="exercise-name">Altura</span><span class="exercise-stat">${u.height_cm} cm</span></div>
      ${u.body_type ? `<div class="exercise-row"><span class="exercise-name">Somatotipo</span><span class="exercise-stat">${bodyTypeLabel(u.body_type)}</span></div>` : ''}
    `;
  } else {
    bmiCard.style.display = 'none';
  }
}

function bodyTypeLabel(t) {
  return { ECTOMORPH: 'Ectomorfo', MESOMORPH: 'Mesomorfo', ENDOMORPH: 'Endomorfo' }[t] || 'No definido';
}

async function saveProfile(e) {
  e.preventDefault();
  try {
    const data = await apiCall('/auth/profile', 'PUT', {
      username: document.getElementById('prof-username').value,
      age: parseInt(document.getElementById('prof-age').value) || undefined,
      height_cm: parseFloat(document.getElementById('prof-height').value) || undefined,
      weight_kg: parseFloat(document.getElementById('prof-weight').value) || undefined,
      goal: document.getElementById('prof-goal').value,
      gender: document.getElementById('prof-gender').value,
    });
    currentUser = data.user;
    document.getElementById('user-name').textContent = data.user.username;
    document.getElementById('user-goal').textContent = goalLabel(data.user.goal);
    loadProfile();
    alert('Perfil actualizado');
  } catch (err) { alert(err.message); }
}

async function saveBodyType() {
  const bodyType = document.getElementById('prof-body-type').value;
  const gender = document.getElementById('prof-gender').value;
  if (!bodyType) { alert('Selecciona un tipo de cuerpo'); return; }
  try {
    const data = await apiCall('/auth/profile', 'PUT', { body_type: bodyType, gender });
    currentUser = data.user;
    alert('Tipo de cuerpo guardado: ' + bodyTypeLabel(bodyType));
  } catch (err) { alert(err.message); }
}

// ===== Recipes =====
const MEAL_TYPE_RECIPE_LABELS = { BREAKFAST: 'Desayuno', LUNCH: 'Comida', DINNER: 'Cena', SNACK: 'Snack', POST_WORKOUT: 'Post-entreno', ANY: 'General' };

async function loadRecipes() {
  try {
    const search = document.getElementById('recipe-search')?.value || '';
    const mealType = document.getElementById('recipe-meal-filter')?.value || 'ANY';
    const params = new URLSearchParams();
    if (search) params.set('search', search);
    if (mealType && mealType !== 'ANY') params.set('meal_type', mealType);
    const data = await apiCall(`/recipes?${params.toString()}`);
    const grid = document.getElementById('recipes-grid');
    if (!data.recipes || data.recipes.length === 0) {
      grid.innerHTML = emptyState('No hay recetas aún');
      return;
    }
    grid.innerHTML = data.recipes.map((r) => `
      <div class="recipe-card" onclick="viewRecipe('${r.id}')">
        ${r.image_url ? `<img class="recipe-img" src="${r.image_url}" alt="${r.name}" />` : '<div class="recipe-img-placeholder">🥗</div>'}
        <div class="recipe-info">
          <div class="recipe-name">${r.name}</div>
          <div class="recipe-meta">${r.calories} cal · ${r.protein_g}g prot · ${r.prep_time_min}min</div>
          <div class="recipe-tags">${(r.diet_tags || []).map((t) => `<span class="recipe-tag">${t}</span>`).join('')}</div>
        </div>
      </div>
    `).join('');
  } catch (err) { console.error('Recipes error:', err); }
}

function viewRecipe(id) {
  showModal(`<div id="recipe-detail-view">${loadingHtml()}</div>`);
  loadRecipeDetail(id);
}

async function loadRecipeDetail(id) {
  try {
    const data = await apiCall(`/recipes/${id}`);
    const r = data.recipe;
    document.getElementById('recipe-detail-view').innerHTML = `
      <div class="modal-header">
        <h2>${r.name}</h2>
        <button class="modal-close" onclick="closeModal()">×</button>
      </div>
      ${r.image_url ? `<img src="${r.image_url}" style="width:100%; border-radius:8px; margin-bottom:1rem;" />` : ''}
      <p style="color:var(--text-2); margin-bottom:1rem;">${r.description || ''}</p>
      <div class="recipe-macros-grid">
        <div class="recipe-macro"><span class="recipe-macro-val">${r.calories}</span><span class="recipe-macro-label">Cal</span></div>
        <div class="recipe-macro"><span class="recipe-macro-val">${r.protein_g}g</span><span class="recipe-macro-label">Prot</span></div>
        <div class="recipe-macro"><span class="recipe-macro-val">${r.carbs_g}g</span><span class="recipe-macro-label">Carbs</span></div>
        <div class="recipe-macro"><span class="recipe-macro-val">${r.fats_g}g</span><span class="recipe-macro-label">Grasas</span></div>
      </div>
      <div style="margin:1rem 0;">
        <strong>Ingredientes (${r.servings} porciones)</strong>
        <ul style="margin-top:0.5rem; padding-left:1.5rem; color:var(--text-2);">
          ${(r.ingredients || []).map((i) => `<li>${i}</li>`).join('')}
        </ul>
      </div>
      <div style="margin:1rem 0;">
        <strong>Preparación</strong>
        <ol style="margin-top:0.5rem; padding-left:1.5rem; color:var(--text-2);">
          ${(r.instructions || []).map((i) => `<li>${i}</li>`).join('')}
        </ol>
      </div>
      <button class="btn-primary" onclick="addRecipeToMeals('${r.id}')">Agregar a comidas de hoy</button>
    `;
  } catch (err) { document.getElementById('recipe-detail-view').innerHTML = `<div class="auth-error show">${err.message}</div>`; }
}

async function addRecipeToMeals(recipeId) {
  try {
    const data = await apiCall(`/recipes/${recipeId}`);
    const r = data.recipe;
    await apiCall('/meals', 'POST', {
      name: r.name,
      meal_type: r.meal_type === 'ANY' ? 'SNACK' : r.meal_type,
      calories: r.calories,
      protein_g: r.protein_g,
      carbs_g: r.carbs_g,
      fats_g: r.fats_g,
    });
    closeModal();
    alert('Receta agregada a comidas de hoy');
  } catch (err) { alert(err.message); }
}

function openRecipeModal() {
  showModal(`
    <div class="modal-header">
      <h2>Nueva receta</h2>
      <button class="modal-close" onclick="closeModal()">×</button>
    </div>
    <form onsubmit="saveRecipe(event)">
      <div class="form-group"><label>Nombre</label><input type="text" class="form-input" id="recipe-name" required /></div>
      <div class="form-group"><label>Descripción</label><textarea class="form-input" id="recipe-desc" rows="2"></textarea></div>
      <div class="form-row">
        <div class="form-group"><label>Calorías</label><input type="number" class="form-input" id="recipe-cal" value="0" /></div>
        <div class="form-group"><label>Proteína (g)</label><input type="number" class="form-input" id="recipe-protein" value="0" /></div>
      </div>
      <div class="form-row">
        <div class="form-group"><label>Carbs (g)</label><input type="number" class="form-input" id="recipe-carbs" value="0" /></div>
        <div class="form-group"><label>Grasas (g)</label><input type="number" class="form-input" id="recipe-fats" value="0" /></div>
      </div>
      <div class="form-row">
        <div class="form-group"><label>Tiempo (min)</label><input type="number" class="form-input" id="recipe-time" value="15" /></div>
        <div class="form-group"><label>Porciones</label><input type="number" class="form-input" id="recipe-servings" value="1" /></div>
      </div>
      <div class="form-group"><label>Tipo</label><select class="form-input" id="recipe-meal-type"><option value="ANY">General</option><option value="BREAKFAST">Desayuno</option><option value="LUNCH">Comida</option><option value="DINNER">Cena</option><option value="SNACK">Snack</option><option value="POST_WORKOUT">Post-entreno</option></select></div>
      <div class="form-group"><label>Ingredientes (uno por línea)</label><textarea class="form-input" id="recipe-ingredients" rows="4" placeholder="200g pollo&#10;1 taza arroz"></textarea></div>
      <div class="form-group"><label>Preparación (un paso por línea)</label><textarea class="form-input" id="recipe-instructions" rows="4" placeholder="Cocinar el arroz&#10;Sazonar el pollo"></textarea></div>
      <button type="submit" class="btn-primary">Guardar receta</button>
    </form>
  `);
}

async function saveRecipe(e) {
  e.preventDefault();
  try {
    await apiCall('/recipes', 'POST', {
      name: document.getElementById('recipe-name').value,
      description: document.getElementById('recipe-desc').value,
      calories: parseInt(document.getElementById('recipe-cal').value) || 0,
      protein_g: parseInt(document.getElementById('recipe-protein').value) || 0,
      carbs_g: parseInt(document.getElementById('recipe-carbs').value) || 0,
      fats_g: parseInt(document.getElementById('recipe-fats').value) || 0,
      prep_time_min: parseInt(document.getElementById('recipe-time').value) || 0,
      servings: parseInt(document.getElementById('recipe-servings').value) || 1,
      meal_type: document.getElementById('recipe-meal-type').value,
      ingredients: document.getElementById('recipe-ingredients').value.split('\n').filter((l) => l.trim()),
      instructions: document.getElementById('recipe-instructions').value.split('\n').filter((l) => l.trim()),
    });
    closeModal();
    loadRecipes();
  } catch (err) { alert(err.message); }
}

// ===== Community =====
let communityMediaData = null;
let communityMediaType = null;

const ROLE_LABELS = { NORMAL: 'Usuario', MODERATOR: 'Moderador', ATHLETE: 'Atleta destacado', ADMIN: 'Admin' };
const ROLE_BADGES = { NORMAL: '', MODERATOR: 'community-badge-mod', ATHLETE: 'community-badge-athlete', ADMIN: 'community-badge-mod' };

async function loadCommunityFeed(isPolling) {
  try {
    const data = await apiCall('/community/feed');
    const feed = document.getElementById('community-feed');
    if (!feed) return;

    let composerText = '';
    if (isPolling) {
      const input = document.getElementById('community-post-input');
      if (input) composerText = input.value;
    }

    if (!data.posts || data.posts.length === 0) {
      feed.innerHTML = emptyState('No hay publicaciones aún. ¡Sé el primero!');
      return;
    }

    feed.innerHTML = data.posts.map((p) => renderCommunityPost(p)).join('');

    if (composerText) {
      const input = document.getElementById('community-post-input');
      if (input) input.value = composerText;
    }
  } catch (err) { console.error('Community feed error:', err); }
}

function renderCommunityPost(p) {
  const roleBadge = p.user.role && p.user.role !== 'NORMAL'
    ? `<span class="community-role-badge ${ROLE_BADGES[p.user.role] || ''}">${ROLE_LABELS[p.user.role] || p.user.role}</span>`
    : '';
  const avatar = p.user.profile_photo
    ? `<img src="${p.user.profile_photo}" class="community-avatar" />`
    : `<div class="community-avatar community-avatar-text">${p.user.username.charAt(0).toUpperCase()}</div>`;

  let mediaHtml = '';
  if (p.media_url) {
    if (p.media_type === 'VIDEO') {
      mediaHtml = `<video src="${p.media_url}" controls class="community-media"></video>`;
    } else {
      mediaHtml = `<img src="${p.media_url}" class="community-media" onclick="viewMealPhoto('${p.media_url}')" />`;
    }
  }

  const reactions = (p.reactions || []);
  const reactionEmojis = ['🔥', '💪', '❤️', '👏', '🚀'];
  const reactionCounts = reactionEmojis.map((emoji) => ({
    emoji,
    count: reactions.filter((r) => r.emoji === emoji).length,
  }));

  return `
    <div class="community-post">
      <div class="community-post-header">
        ${avatar}
        <div class="community-post-user">
          <div class="community-post-name">${p.user.username} ${roleBadge}</div>
          <div class="community-post-time">${new Date(p.created_at).toLocaleDateString('es-ES', { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' })}</div>
        </div>
        ${p.user_id === currentUser.id || (currentUser.role === 'MODERATOR' || currentUser.role === 'ADMIN') ? `<button class="community-delete-btn" onclick="deleteCommunityPost('${p.id}')">×</button>` : ''}
      </div>
      ${p.content ? `<div class="community-post-content">${escapeHtml(p.content)}</div>` : ''}
      ${mediaHtml}
      <div class="community-reactions">
        ${reactionCounts.map((r) => `
          <button class="reaction-btn ${r.count > 0 ? 'has-count' : ''}" onclick="reactToPost('${p.id}', '${r.emoji}')">
            ${r.emoji} ${r.count > 0 ? `<span class="reaction-count">${r.count}</span>` : ''}
          </button>
        `).join('')}
      </div>
      <div class="community-replies">
        ${(p.replies || []).map((r) => renderCommunityReply(r, p.id)).join('')}
        <div class="community-reply-input">
          <input type="text" class="form-input" placeholder="Responder..." id="reply-input-${p.id}" onkeypress="if(event.key==='Enter'){replyToPost('${p.id}');}" />
        </div>
      </div>
    </div>
  `;
}

function renderCommunityReply(r, postId) {
  const roleBadge = r.user.role && r.user.role !== 'NORMAL'
    ? `<span class="community-role-badge ${ROLE_BADGES[r.user.role] || ''}">${ROLE_LABELS[r.user.role] || r.user.role}</span>`
    : '';
  const avatar = r.user.profile_photo
    ? `<img src="${r.user.profile_photo}" class="community-avatar community-avatar-sm" />`
    : `<div class="community-avatar community-avatar-sm community-avatar-text">${r.user.username.charAt(0).toUpperCase()}</div>`;

  return `
    <div class="community-reply">
      ${avatar}
      <div class="community-reply-body">
        <div class="community-reply-name">${r.user.username} ${roleBadge}</div>
        ${r.media_url ? `<img src="${r.media_url}" class="community-reply-media" onclick="viewMealPhoto('${r.media_url}')" />` : ''}
        <div class="community-reply-text">${escapeHtml(r.content)}</div>
        <div class="community-reply-time">${new Date(r.created_at).toLocaleDateString('es-ES', { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' })}</div>
      </div>
    </div>
  `;
}

function escapeHtml(text) {
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
}

async function postCommunity() {
  const content = document.getElementById('community-post-input').value.trim();
  if (!content && !communityMediaData) { alert('Escribe algo o sube una foto'); return; }
  try {
    await apiCall('/community/posts', 'POST', {
      content,
      media_url: communityMediaData || null,
      media_type: communityMediaType || 'TEXT',
    });
    document.getElementById('community-post-input').value = '';
    communityMediaData = null;
    communityMediaType = null;
    document.getElementById('community-media-preview').style.display = 'none';
    document.getElementById('community-media-preview').innerHTML = '';
    document.getElementById('community-media-input').value = '';
  } catch (err) { alert(err.message); }
}

async function replyToPost(postId) {
  const input = document.getElementById(`reply-input-${postId}`);
  const content = input.value.trim();
  if (!content) return;
  try {
    await apiCall(`/community/posts/${postId}/replies`, 'POST', { content });
    input.value = '';
  } catch (err) { alert(err.message); }
}

async function reactToPost(postId, emoji) {
  try {
    await apiCall(`/community/posts/${postId}/react`, 'POST', { emoji });
  } catch (err) { console.error('React error:', err); }
}

async function deleteCommunityPost(postId) {
  if (!confirm('¿Eliminar esta publicación?')) return;
  try {
    await apiCall(`/community/posts/${postId}`, 'DELETE');
  } catch (err) { alert(err.message); }
}

function handleCommunityMedia(event) {
  const file = event.target.files[0];
  if (!file) return;
  if (file.size > 5 * 1024 * 1024) { alert('El archivo no puede pesar más de 5MB'); return; }
  communityMediaType = file.type.startsWith('video/') ? 'VIDEO' : 'IMAGE';
  const reader = new FileReader();
  reader.onload = (e) => {
    communityMediaData = e.target.result;
    const preview = document.getElementById('community-media-preview');
    preview.style.display = 'block';
    if (communityMediaType === 'VIDEO') {
      preview.innerHTML = `<video src="${communityMediaData}" style="width:100%; height:100%; object-fit:cover;"></video>`;
    } else {
      preview.innerHTML = `<img src="${communityMediaData}" style="width:100%; height:100%; object-fit:cover;" />`;
    }
  };
  reader.readAsDataURL(file);
}

async function searchCommunityUsers() {
  const q = document.getElementById('community-user-search').value.trim();
  const results = document.getElementById('community-user-results');
  if (q.length < 2) { results.innerHTML = ''; return; }
  try {
    const data = await apiCall(`/community/users/search?q=${encodeURIComponent(q)}`);
    results.innerHTML = (data.users || []).map((u) => `
      <div class="community-user-result" onclick="viewUserProfile('${u.id}')">
        ${u.profile_photo ? `<img src="${u.profile_photo}" class="community-avatar community-avatar-sm" />` : `<div class="community-avatar community-avatar-sm community-avatar-text">${u.username.charAt(0).toUpperCase()}</div>`}
        <span>${u.username}</span>
        ${u.role !== 'NORMAL' ? `<span class="community-role-badge ${ROLE_BADGES[u.role] || ''}">${ROLE_LABELS[u.role] || u.role}</span>` : ''}
      </div>
    `).join('');
  } catch (err) { console.error('User search error:', err); }
}

function viewUserProfile(userId) {
  showModal(`<div id="user-profile-view">${loadingHtml()}</div>`);
  loadUserProfileDetail(userId);
}

async function loadUserProfileDetail(userId) {
  try {
    const data = await apiCall(`/community/profile/${userId}`);
    const u = data.user;
    const avatar = u.profile_photo
      ? `<img src="${u.profile_photo}" class="community-avatar community-avatar-lg" />`
      : `<div class="community-avatar community-avatar-lg community-avatar-text">${u.username.charAt(0).toUpperCase()}</div>`;
    const roleBadge = u.role && u.role !== 'NORMAL'
      ? `<span class="community-role-badge ${ROLE_BADGES[u.role] || ''}">${ROLE_LABELS[u.role] || u.role}</span>`
      : '';

    document.getElementById('user-profile-view').innerHTML = `
      <div class="modal-header">
        <h2>Perfil de ${u.username}</h2>
        <button class="modal-close" onclick="closeModal()">×</button>
      </div>
      <div style="text-align:center; margin-bottom:1.5rem;">
        ${avatar}
        <h3 style="margin-top:0.5rem;">${u.username} ${roleBadge}</h3>
        ${u.bio ? `<p style="color:var(--text-2);">${u.bio}</p>` : ''}
      </div>
      <div class="community-profile-stats">
        <div class="community-profile-stat"><span class="community-profile-stat-val">${u.post_count || 0}</span><span class="community-profile-stat-label">Posts</span></div>
        <div class="community-profile-stat"><span class="community-profile-stat-val">${u.routine_count || 0}</span><span class="community-profile-stat-label">Rutinas</span></div>
      </div>
      <div style="margin-top:1rem;">
        ${u.goal ? `<div class="exercise-row"><span class="exercise-name">Objetivo</span><span class="exercise-stat">${goalLabel(u.goal)}</span></div>` : ''}
        ${u.body_type ? `<div class="exercise-row"><span class="exercise-name">Tipo de cuerpo</span><span class="exercise-stat">${bodyTypeLabel(u.body_type)}</span></div>` : ''}
        ${u.height_cm ? `<div class="exercise-row"><span class="exercise-name">Altura</span><span class="exercise-stat">${u.height_cm} cm</span></div>` : ''}
        ${u.weight_kg ? `<div class="exercise-row"><span class="exercise-name">Peso</span><span class="exercise-stat">${u.weight_kg} kg</span></div>` : ''}
      </div>
    `;
  } catch (err) { document.getElementById('user-profile-view').innerHTML = `<div class="auth-error show">${err.message}</div>`; }
}

// ===== Init =====
if (token) { showDashboard(); }
